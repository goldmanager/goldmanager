import base from './playwright.config';
import { defineConfig } from '@playwright/test';

// Derive from the standard config but disable the webServer to avoid
// running the local build/start script. Assumes the app is already
// reachable at http://localhost:8080.
export default defineConfig({
  ...base,
  webServer: undefined as any,
  use: {
    ...base.use,
    baseURL: 'http://localhost:8080',
  },
});

