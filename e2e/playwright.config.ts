import { defineConfig, devices } from '@playwright/test';
import { execSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import path from 'node:path';

// Helper: resolve repo root when executed from e2e/
const root = path.resolve(__dirname, '..');

export default defineConfig({
  testDir: path.join(__dirname, 'tests'),
  timeout: 30_000,
  expect: {
    timeout: 5_000,
  },
  fullyParallel: true,
  retries: 0,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
  },

  // Build frontend, build backend, start server. Requires MariaDB running.
  webServer: {
    command: 'node ./scripts/start-app.cjs',
    url: 'http://localhost:8080/api/health',
    reuseExistingServer: true,
    timeout: 480_000,
    cwd: __dirname,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],
});
