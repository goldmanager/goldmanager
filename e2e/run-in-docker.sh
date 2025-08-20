#!/usr/bin/env bash
set -euo pipefail

# Playwright image to use (can override via PLAYWRIGHT_IMAGE)
IMAGE="${PLAYWRIGHT_IMAGE:-goldmanager/e2e-playwright:local}"

# Database connection for the backend from inside the container
DB_HOST="${E2E_DB_HOST:-host.docker.internal}"
DB_PORT="${E2E_DB_PORT:-3317}"

# Increased waits for fresh DB initialization and app startup
export E2E_DB_WAIT_MS="${E2E_DB_WAIT_MS:-360000}"          # 6 minutes
export E2E_HEALTH_TIMEOUT_MS="${E2E_HEALTH_TIMEOUT_MS:-600000}"  # 10 minutes
export E2E_WEBSERVER_TIMEOUT_MS="${E2E_WEBSERVER_TIMEOUT_MS:-720000}" # 12 minutes

# Linux compatibility: map host.docker.internal to host-gateway
EXTRA_HOST="--add-host=host.docker.internal:host-gateway"

echo "[e2e-docker] Using image: ${IMAGE}"
echo "[e2e-docker] DB: ${DB_HOST}:${DB_PORT}"

# Determine Playwright version to bake into the image (can override via PLAYWRIGHT_VERSION)
PLAYWRIGHT_VERSION="${PLAYWRIGHT_VERSION:-1.54.2}"
echo "[e2e-docker] Playwright version: ${PLAYWRIGHT_VERSION}"

# Build local image if not present
if ! docker image inspect "${IMAGE}" >/dev/null 2>&1; then
  echo "[e2e-docker] Building local E2E Playwright image ..."
  docker build --build-arg PLAYWRIGHT_VERSION="${PLAYWRIGHT_VERSION}" -f e2e/Dockerfile -t "${IMAGE}" .
fi

# Ensure E2E MariaDB is up on host, then reset DB quickly
COMPOSE_FILE="e2e/dev-db/compose.yaml"
RESET_MODE="${E2E_DB_RESET_MODE:-drop}"  # 'drop' (fast) or 'compose' (down -v; slower)
echo "[e2e-docker] Ensuring E2E DB stack is up using ${COMPOSE_FILE} ..."
if [ "${RESET_MODE}" = "compose" ]; then
  echo "[e2e-docker] RESET_MODE=compose -> full reset (down -v; up -d)"
  docker compose -f "${COMPOSE_FILE}" down -v || true
  docker compose -f "${COMPOSE_FILE}" up -d
else
  echo "[e2e-docker] RESET_MODE=drop -> fast SQL reset after startup"
  docker compose -f "${COMPOSE_FILE}" up -d
fi

# Helper to print container status/logs on failure
dump_db_diagnostics() {
  echo "[e2e-docker] --- docker ps ---" >&2
  docker ps --filter name=goldmanager-e2e-mariadb >&2 || true
  echo "[e2e-docker] --- docker inspect (health) ---" >&2
  docker inspect -f '{{.State.Health.Status}}' goldmanager-e2e-mariadb >&2 || true
  echo "[e2e-docker] --- docker inspect (health JSON) ---" >&2
  docker inspect -f '{{json .State.Health}}' goldmanager-e2e-mariadb >&2 || true
  echo "[e2e-docker] --- last 200 logs ---" >&2
  docker logs --tail 200 goldmanager-e2e-mariadb >&2 || true
}

# Wait for DB health=healthy and TCP accept using nc (fallback to /dev/tcp)
echo "[e2e-docker] Waiting for MariaDB health + TCP on ${DB_HOST}:${DB_PORT} ..."
DEADLINE=$((SECONDS + 600)) # 10 minutes max
while :; do
  HEALTH=$(docker inspect -f '{{.State.Health.Status}}' goldmanager-e2e-mariadb 2>/dev/null || echo unknown)
  if command -v nc >/dev/null 2>&1; then
    nc -z "${DB_HOST}" "${DB_PORT}" >/dev/null 2>&1 && TCP_OK=1 || TCP_OK=0
  else
    (echo > /dev/tcp/${DB_HOST}/${DB_PORT}) >/dev/null 2>&1 && TCP_OK=1 || TCP_OK=0
  fi

  if [ "${HEALTH}" = "healthy" ] || [ ${TCP_OK} -eq 1 ]; then
    echo "[e2e-docker] DB ready: health=${HEALTH}, tcp=${TCP_OK}"
    break
  fi

  if (( SECONDS > DEADLINE )); then
    echo "[e2e-docker] Timeout waiting for DB health/reachability" >&2
    dump_db_diagnostics
    exit 1
  fi

  echo "[e2e-docker] Waiting... health=${HEALTH}, tcp=${TCP_OK}"
  sleep 5
done

# If using fast drop mode, reset the goldmanager DB inside the container
if [ "${RESET_MODE}" = "drop" ]; then
  echo "[e2e-docker] Dropping and recreating database 'goldmanager' for clean state ..."
  if ! docker exec goldmanager-e2e-mariadb sh -lc "mariadb -h 127.0.0.1 -uroot -p\$MARIADB_ROOT_PASSWORD -e \"DROP DATABASE IF EXISTS goldmanager; CREATE DATABASE goldmanager; GRANT ALL PRIVILEGES ON goldmanager.* TO 'myuser'@'%'; FLUSH PRIVILEGES;\""; then
    echo "[e2e-docker] Database reset failed; dumping diagnostics" >&2
    dump_db_diagnostics
    exit 1
  fi
  echo "[e2e-docker] Database reset completed."
fi

docker run --rm --user root ${EXTRA_HOST} --shm-size=1g \
  -v "${PWD}":/work -w /work \
  -e E2E_DB_HOST="${DB_HOST}" -e E2E_DB_PORT="${DB_PORT}" \
  -e E2E_DB_WAIT_MS -e E2E_HEALTH_TIMEOUT_MS -e E2E_WEBSERVER_TIMEOUT_MS \
  -e SPRING_DATASOURCE_URL="jdbc:mariadb://${DB_HOST}:${DB_PORT}/goldmanager" \
  -p 8080:8080 \
  "${IMAGE}" bash -lc '
set -euo pipefail
# Use existing node_modules to avoid network installs when available
cd /work/e2e
if [ -d node_modules/@playwright/test ]; then
  echo "[e2e-docker] Using existing node_modules for Playwright. Skipping npm ci."
else
  echo "[e2e-docker] node_modules missing; running npm ci ..."
  npm ci
fi

# Start backend from prebuilt JAR if present and wait for health
cd /work
JAR=$(ls -1 backend/build/libs/*.jar 2>/dev/null | grep -v -- '-plain.jar' | head -n1 || true)
if [ -z "${JAR}" ]; then
  echo "[e2e-docker] ERROR: Backend JAR not found at backend/build/libs. Build it first (./gradlew bootJar)." >&2
  exit 1
fi
echo "[e2e-docker] Starting backend: ${JAR}"
JAVA_OPTS="-Dspring.profiles.active=dev -DAPP_DEFAULT_USER=admin -DAPP_DEFAULT_PASSWORD=admin1Password!"
(java ${JAVA_OPTS} -jar "${JAR}" >/work/e2e/app.log 2>&1 &) 

echo "[e2e-docker] Waiting for app health endpoint ..."
deadline=$((SECONDS + ${E2E_HEALTH_TIMEOUT_MS:-600000} / 1000))
until curl -sf http://localhost:8080/api/health >/dev/null 2>&1; do
  if (( SECONDS > deadline )); then
    echo "[e2e-docker] App failed to become healthy in time" >&2
    tail -n 200 /work/e2e/app.log || true
    exit 1
  fi
  sleep 2
done
echo "[e2e-docker] App is healthy; running Playwright tests ..."

cd /work/e2e
node node_modules/@playwright/test/cli.js test --config=playwright.no-server.config.ts
'
