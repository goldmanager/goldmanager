import { test, expect } from '@playwright/test';

const ADMIN = { username: 'admin', password: 'admin1Password!' };
const METAL = { name: `Testium-${Math.random().toString(36).slice(2, 8)}`, price1: 123.45, price2: 234.56 };

test('admin can create, edit, and delete a metal', async ({ page }) => {
  // Ensure clean state if a previous run left the metal
  await page.request.post('/api/auth/login', { data: ADMIN }).catch(() => {});
  // Best-effort cleanup by name via API list
  const listBefore = await page.request.get('/api/materials');
  if (listBefore.ok()) {
    const items = await listBefore.json();
    const match = Array.isArray(items) && items.find((m: any) => m?.name === METAL.name);
    if (match?.id) await page.request.delete(`/api/materials/${match.id}`).catch(() => {});
  }

  // Login via UI
  await page.goto('/login');
  await page.getByPlaceholder('Username').fill(ADMIN.username);
  await page.getByPlaceholder('Password').fill(ADMIN.password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Prices' })).toBeVisible();

  // Navigate to Metals
  await page.getByRole('link', { name: 'Metals' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Metals' })).toBeVisible();

  // Create new metal in the add row
  const addRow = page.locator('tbody tr').first();
  await addRow.getByPlaceholder('Name', { exact: true }).fill(METAL.name);
  await addRow.getByPlaceholder('Price', { exact: true }).fill(String(METAL.price1));
  await addRow.getByRole('button', { name: 'Add New' }).click();

  // Filter and assert it shows up
  await page.getByPlaceholder('Search by metal name').fill(METAL.name);
  const metalRow = page.locator('tbody tr').nth(1);
  await expect(metalRow).toContainText(METAL.name);

  // Edit the metal's price using Edit -> Save flow
  await metalRow.getByRole('button', { name: 'Edit' }).click();
  const editRow = page.locator('tbody tr').first();
  await editRow.getByPlaceholder('Price', { exact: true }).fill(String(METAL.price2));
  await editRow.getByRole('button', { name: 'Save' }).click();

  // Re-filter and assert updated price is visible
  await page.getByPlaceholder('Search by metal name').fill(METAL.name);
  await expect(page.locator('tbody tr').nth(1)).toContainText(String(METAL.price2));

  // Delete the metal
  page.once('dialog', d => d.accept());
  await page.locator('tbody tr').nth(1).getByRole('button', { name: 'Delete' }).click();

  // Wait until the metal no longer exists via API (server-side confirmation)
  for (let i = 0; i < 10; i++) {
    const check = await page.request.get('/api/materials');
    if (check.ok()) {
      const items = await check.json();
      const exists = Array.isArray(items) && items.some((m: any) => m?.name === METAL.name);
      if (!exists) break;
    }
    await page.waitForTimeout(300);
  }

  // Filter and assert there is no row containing the metal name
  await page.getByPlaceholder('Search by metal name').fill(METAL.name);
  await expect(page.locator('tbody tr', { hasText: METAL.name })).toHaveCount(0);
});
