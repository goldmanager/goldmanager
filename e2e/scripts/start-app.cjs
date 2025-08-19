#!/usr/bin/env node
/* Cross-platform launcher for GoldManager E2E tests.
 * - Ensures MariaDB dev DB is running (tries docker compose if needed)
 * - Builds frontend and copies to backend static resources
 * - Builds backend bootJar
 * - Starts Spring Boot app and keeps running
 */

const { spawn, spawnSync } = require('node:child_process');
const fs = require('node:fs');
const fsp = require('node:fs/promises');
const path = require('node:path');
const net = require('node:net');

const scriptDir = __dirname;
const repoRoot = path.resolve(scriptDir, '..', '..');
const frontendDir = path.join(repoRoot, 'frontend');
const backendDir = path.join(repoRoot, 'backend');

function log(msg) { console.log(`[e2e] ${msg}`); }
function err(msg) { console.error(`[e2e] ${msg}`); }

function which(cmd) {
  const isWin = process.platform === 'win32';
  const exts = isWin ? (process.env.PATHEXT || '.EXE;.CMD;.BAT').split(';') : [''];
  const paths = (process.env.PATH || '').split(path.delimiter);
  for (const p of paths) {
    const full = path.join(p, cmd);
    for (const ext of exts) {
      const file = full + ext;
      if (fs.existsSync(file) && fs.statSync(file).isFile()) return file;
    }
  }
  return null;
}

async function waitForTcp(host, port, timeoutMs) {
  const start = Date.now();
  const deadline = start + timeoutMs;
  while (Date.now() < deadline) {
    const ok = await new Promise(resolve => {
      const s = net.createConnection({ host, port, timeout: 1000 }, () => {
        s.destroy();
        resolve(true);
      });
      s.on('error', () => resolve(false));
      s.on('timeout', () => { s.destroy(); resolve(false); });
    });
    if (ok) return true;
    await new Promise(r => setTimeout(r, 1000));
  }
  return false;
}

function run(cmd, args, opts = {}) {
  log(`$ ${cmd} ${args.join(' ')}`);
  const res = spawnSync(cmd, args, { stdio: 'inherit', ...opts });
  if (res.status !== 0) {
    throw new Error(`${cmd} exited with code ${res.status}`);
  }
}

async function ensureDb() {
  const host = process.env.E2E_DB_HOST || 'localhost';
  const port = Number(process.env.E2E_DB_PORT || 3317); // E2E DB defaults to 3317
  const isLocalHost = ['localhost', '127.0.0.1', '::1'].includes(host);
  const dbWaitMs = Number(process.env.E2E_DB_WAIT_MS || 360000); // up to 6 minutes for first-time init
  log(`Checking MariaDB at ${host}:${port} ...`);
  if (await waitForTcp(host, port, 3000)) {
    log('MariaDB is up.');
    return;
  }

  const docker = which('docker');
  if (isLocalHost) {
    if (!docker) {
      err('Docker not found; cannot auto-start E2E DB. Please start it manually:');
      err('  docker compose -f e2e/dev-db/compose.yaml up -d');
    } else {
      log('Attempting to start E2E MariaDB via docker compose (clean start) ...');
      const composeFile = process.env.E2E_DB_COMPOSE_FILE || path.join(repoRoot, 'e2e', 'dev-db', 'compose.yaml');
      // Clean any previous instance/volume and start fresh to ensure clean DB
      try {
        run(docker, ['compose', '-f', composeFile, 'down', '-v']);
      } catch (e) {
        err('Ignoring compose down errors (possibly first run).');
      }
      try {
        run(docker, ['compose', '-f', composeFile, 'up', '-d']);
      } catch (e) {
        // Fallback: docker-compose (legacy)
        const dockerCompose = which('docker-compose');
        if (dockerCompose) {
          try { run(dockerCompose, ['-f', composeFile, 'down', '-v']); } catch {}
          run(dockerCompose, ['-f', composeFile, 'up', '-d']);
        } else {
          throw e;
        }
      }
    }
  } else {
    log('DB host is not local; skipping docker auto-start.');
  }

  log(`Waiting for MariaDB to become ready (up to ${dbWaitMs} ms) ...`);
  const ready = await waitForTcp(host, port, dbWaitMs);
  if (!ready) {
    throw new Error('MariaDB did not become ready in time');
  }
  log('MariaDB is ready.');
}

async function buildFrontendAndSync() {
  log('Installing frontend dependencies ...');
  const npmCmd = process.platform === 'win32' ? 'npm.cmd' : 'npm';
  const lockfile = path.join(frontendDir, 'package-lock.json');
  if (fs.existsSync(lockfile)) {
    run(npmCmd, ['ci'], { cwd: frontendDir });
  } else {
    run(npmCmd, ['install'], { cwd: frontendDir });
  }
  log('Building frontend ...');
  run(npmCmd, ['run', 'build'], { cwd: frontendDir });

  const distDir = path.join(frontendDir, 'dist');
  const staticDir = path.join(backendDir, 'src', 'main', 'resources', 'static');
  log('Copying frontend dist to backend static resources ...');
  await fsp.rm(staticDir, { recursive: true, force: true });
  await fsp.mkdir(staticDir, { recursive: true });
  // Copy dist recursively
  // Node 20 supports fs.cp
  await fsp.cp(distDir, staticDir, { recursive: true });
}

function gradleCmd() {
  return process.platform === 'win32' ? 'gradlew.bat' : './gradlew';
}

async function buildBackend() {
  log('Building backend bootJar ...');
  const cmd = gradleCmd();
  const exists = fs.existsSync(path.join(backendDir, cmd.replace('./', '')));
  if (!exists) throw new Error('Gradle wrapper not found in backend/');
  run(cmd, ['clean', 'bootJar', '--no-daemon'], { cwd: backendDir, shell: process.platform === 'win32' });
}

function findBootJar() {
  const libs = path.join(backendDir, 'build', 'libs');
  const files = fs.existsSync(libs) ? fs.readdirSync(libs) : [];
  // Prefer boot jar, exclude plain jars
  const jar = files.find(f => f.endsWith('.jar') && !f.includes('-plain'));
  if (!jar) return null;
  return path.join(libs, jar);
}

async function startApp() {
  const jarPath = findBootJar();
  if (!jarPath) throw new Error('Could not locate backend JAR under backend/build/libs');
  log(`Starting backend: ${jarPath}`);
  // If running in a container with an external DB host, derive datasource URL
  const dbHost = process.env.E2E_DB_HOST || 'localhost';
  const dbPort = Number(process.env.E2E_DB_PORT || 3317);
  const isLocalHost = ['localhost', '127.0.0.1', '::1'].includes(dbHost);
  const defaultDevPort = 3307;
  if (!process.env.SPRING_DATASOURCE_URL && (!isLocalHost || dbPort !== defaultDevPort)) {
    process.env.SPRING_DATASOURCE_URL = `jdbc:mariadb://${dbHost}:${dbPort}/goldmanager`;
    log(`Derived SPRING_DATASOURCE_URL=${process.env.SPRING_DATASOURCE_URL}`);
  }
  const javaArgs = [
    '-Dspring.profiles.active=dev',
    '-DAPP_DEFAULT_USER=admin',
    '-DAPP_DEFAULT_PASSWORD=admin1Password!',
    '-jar',
    jarPath,
  ];
  const javaCmd = process.platform === 'win32' ? 'java.exe' : 'java';
  const child = spawn(javaCmd, javaArgs, { stdio: 'inherit' });
  child.on('exit', code => {
    process.exit(code ?? 1);
  });
}

(async () => {
  try {
    await ensureDb();
    await buildFrontendAndSync();
    await buildBackend();
    await startApp();
    // Wait for health endpoint to report ready
    const http = require('node:http');
    async function waitForHealth(timeoutMs) {
      const start = Date.now();
      while (Date.now() - start < timeoutMs) {
        const ok = await new Promise(resolve => {
          const req = http.get('http://localhost:8080/api/health', res => {
            if (res.statusCode === 200) {
              resolve(true);
            } else {
              resolve(false);
            }
            res.resume();
          });
          req.on('error', () => resolve(false));
        });
        if (ok) return true;
        await new Promise(r => setTimeout(r, 1000));
      }
      return false;
    }
    const healthTimeoutMs = Number(process.env.E2E_HEALTH_TIMEOUT_MS || 600_000);
    log(`Waiting for application health check (up to ${healthTimeoutMs} ms) ...`);
    const healthy = await waitForHealth(healthTimeoutMs);
    if (!healthy) {
      throw new Error('Application health endpoint did not become ready in time');
    }
    log('Application is healthy.');
  } catch (e) {
    err(String(e.stack || e));
    process.exit(1);
  }
})();
