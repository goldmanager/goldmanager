import { test, expect } from '@playwright/test';

const ADMIN = { username: 'admin', password: 'admin1Password!' };
const UNIT = { name: `TestUnit-${Math.random().toString(36).slice(2, 8)}`, factor1: 2.5, factor2: 3.75 };

test('admin can create, edit, and delete a unit', async ({ page }) => {
  // Ensure clean state if left over from previous run
  await page.request.post('/api/auth/login', { data: ADMIN }).catch(() => {});
  // Best-effort cleanup by name via API list
  const listBefore = await page.request.get('/api/units');
  if (listBefore.ok()) {
    const items = await listBefore.json();
    const match = Array.isArray(items) && items.find((u: any) => u?.name === UNIT.name);
    if (match?.name) await page.request.delete(`/api/units/${match.name}`).catch(() => {});
  }

  // Login via UI
  await page.goto('/login');
  await page.getByPlaceholder('Username').fill(ADMIN.username);
  await page.getByPlaceholder('Password').fill(ADMIN.password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Prices' })).toBeVisible();

  // Navigate to Units
  await page.getByRole('link', { name: 'Units' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Units' })).toBeVisible();

  // Create new unit in the add row
  const addRow = page.locator('tbody tr').first();
  await addRow.getByPlaceholder('Name', { exact: true }).fill(UNIT.name);
  await addRow.getByPlaceholder('Factor', { exact: true }).fill(String(UNIT.factor1));
  await addRow.getByRole('button', { name: 'Add New' }).click();

  // Filter and assert it shows up
  await page.getByPlaceholder('Search by unit name').fill(UNIT.name);
  const unitRow = page.locator('tbody tr').nth(1);
  await expect(unitRow).toContainText(UNIT.name);

  // Edit the unit's factor using Edit -> Save flow
  await unitRow.getByRole('button', { name: 'Edit' }).click();
  const editRow = page.locator('tbody tr').first();
  await editRow.getByPlaceholder('Factor', { exact: true }).fill(String(UNIT.factor2));
  await editRow.getByRole('button', { name: 'Save' }).click();

  // Assert updated factor is visible in the row by name (auto-retries until it refreshes)
  const rowByName = page.locator('tbody tr', { hasText: UNIT.name }).first();
  await expect(rowByName).toContainText(String(UNIT.factor2));

  // Delete the unit
  page.once('dialog', d => d.accept());
  await rowByName.getByRole('button', { name: 'Delete' }).click();

  // Wait until the unit no longer exists via API (server-side confirmation)
  for (let i = 0; i < 10; i++) {
    const check = await page.request.get('/api/units');
    if (check.ok()) {
      const items = await check.json();
      const exists = Array.isArray(items) && items.some((u: any) => u?.name === UNIT.name);
      if (!exists) break;
    }
    await page.waitForTimeout(300);
  }

  // Assert there is no row containing the unit name (auto-retries while UI refreshes)
  await expect(page.locator('tbody tr', { hasText: UNIT.name })).toHaveCount(0);
});
