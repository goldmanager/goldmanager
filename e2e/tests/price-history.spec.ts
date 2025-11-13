import { test, expect } from '@playwright/test';
import { HEADING_TIMEOUT } from './support/timeouts';

const ADMIN = { username: 'admin', password: 'admin1Password!' };

test('price history page loads and shows controls', async ({ page }) => {
  // Login via UI
  await page.goto('/login');
  await page.getByPlaceholder('Username').fill(ADMIN.username);
  await page.getByPlaceholder('Password').fill(ADMIN.password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Prices' })).toBeVisible({ timeout: HEADING_TIMEOUT });

  // Navigate to Price History
  await page.getByRole('link', { name: 'Price History' }).click();
  await expect(page).toHaveURL(/\/priceHistory$/, { timeout: HEADING_TIMEOUT });
  await expect(page.getByRole('heading', { level: 1, name: 'Price History' })).toBeVisible({ timeout: HEADING_TIMEOUT });

  // Assert key controls render
  await expect(page.locator('select#metals')).toBeVisible({ timeout: HEADING_TIMEOUT });
  await expect(page.getByRole('button', { name: 'Delete complete Metal Price History' })).toBeVisible({ timeout: HEADING_TIMEOUT });

  // Wait for either placeholder text or a rendered chart canvas.
  const placeholder = page.getByText('No Price History available', { exact: false });
  const canvas = page.locator('canvas').first();
  try {
    // If placeholder appears quickly, accept it; else expect the chart.
    await placeholder.waitFor({ state: 'visible', timeout: 2_000 });
  } catch {
    await expect(canvas).toBeVisible({ timeout: 10_000 });
  }
});
