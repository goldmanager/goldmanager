import { test, expect } from '@playwright/test';
import { HEADING_TIMEOUT } from './support/timeouts';

test('unauthenticated user is redirected to login on protected routes', async ({ page }) => {
  // Direct access to a protected route redirects to /login
  await page.goto('/users');
  await expect(page).toHaveURL(/\/login$/, { timeout: HEADING_TIMEOUT });
  await expect(page.getByRole('heading', { level: 1, name: /login/i })).toBeVisible({ timeout: HEADING_TIMEOUT });

  // Home route is also protected; should redirect as well
  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/, { timeout: HEADING_TIMEOUT });

  // Navbar shows Login link when unauthenticated
  await expect(page.getByRole('link', { name: 'Login' })).toBeVisible({ timeout: HEADING_TIMEOUT });
});
