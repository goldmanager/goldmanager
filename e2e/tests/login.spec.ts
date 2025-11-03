import { test, expect } from '@playwright/test';
import { HEADING_TIMEOUT } from './support/timeouts';

test('user can login with default admin credentials', async ({ page }) => {
  // Go to login page
  await page.goto('/login');

  // Fill credentials defined in start-app launcher
  await page.getByPlaceholder('Username').fill('admin');
  await page.getByPlaceholder('Password').fill('admin1Password!');

  // Submit the form
  await page.getByRole('button', { name: 'Login' }).click();

  // Expect redirect to home and visible Prices heading
  await expect(page).toHaveURL(/\/?$/, { timeout: HEADING_TIMEOUT });
  await expect(page.getByRole('heading', { level: 1, name: /Prices/i })).toBeVisible({ timeout: HEADING_TIMEOUT });
});
