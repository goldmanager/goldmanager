import { defineConfig, devices } from '@playwright/test';
import { execSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import path from 'node:path';

// Helper: resolve repo root when executed from e2e/
const root = path.resolve(__dirname, '..');
const containerResultsDir = process.env.E2E_RESULTS_DIR ?? 'test-results';
const containerHtmlResultsDir = process.env.E2E_RESULTS_HTML_DIR ?? 'test-results-html';
const jsonReportPath = path.join(containerResultsDir, 'report.json');

export default defineConfig({
  testDir: path.join(__dirname, 'tests'),
  timeout: 30_000,
  expect: {
    timeout: 5_000,
  },
  fullyParallel: true,
  retries: 0,
  reporter: [
    ['list'],
    ['html', { outputFolder: containerHtmlResultsDir, open: 'never' }],
    ['json', { outputFile: jsonReportPath }],
  ],
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },

  // Build frontend, build backend, start server. Requires MariaDB running.
  webServer: {
    command: 'node ./scripts/start-app.cjs',
    url: 'http://localhost:8080/api/health',
    reuseExistingServer: true,
    timeout: Number(process.env.E2E_WEBSERVER_TIMEOUT_MS || '720000'),
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
