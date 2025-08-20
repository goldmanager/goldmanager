import { test, expect } from '@playwright/test';

// This test drives the UI to create an admin user, verifies login,
// changes the password, verifies login with the new password, and finally deletes the user.
// It uses the Users page UI (search + per-row actions) and verifies state via API when useful.

const DEFAULT_ADMIN = { username: 'admin', password: 'admin1Password!' };
const ADMIN2 = { username: 'admin2', initialPassword: 'TGBbgt5!', newPassword: 'MJU/7ujm' };

test('admin can create, update password, and delete another admin user', async ({ page, browserName }) => {
  // 0) Safety cleanup (in case of previous partial runs)
  await page.request.post('/api/auth/login', { data: DEFAULT_ADMIN }).catch(() => {});
  await page.request.delete(`/api/userService/deleteuser/${ADMIN2.username}`).catch(() => {});

  // 1) Login as default admin
  await page.goto('/login');
  await page.getByPlaceholder('Username').fill(DEFAULT_ADMIN.username);
  await page.getByPlaceholder('Password').fill(DEFAULT_ADMIN.password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL(/\/?$/);
  await expect(page.getByRole('heading', { level: 1, name: /Prices/i })).toBeVisible();

  // 2) Create a new admin user "admin2" with password ADMIN2.initialPassword
  await page.getByRole('link', { name: 'Users' }).click();
  // Add new user row is the first row in <tbody>
  const addRow = page.locator('tbody tr').first();
  await addRow.getByPlaceholder('Name', { exact: true }).fill(ADMIN2.username);
  await addRow.getByPlaceholder('Password', { exact: true }).fill(ADMIN2.initialPassword);
  await addRow.getByPlaceholder('Password (Confirm)', { exact: true }).fill(ADMIN2.initialPassword);
  await addRow.getByRole('button', { name: 'Add New' }).click();

  // Verify user was created using UI table (filter and assert row shows admin2)
  await page.getByPlaceholder('Search by user name').fill(ADMIN2.username);
  const createdRow = page.locator('tbody tr').nth(1).locator('input[type="text"]');
  await expect(createdRow).toHaveValue(ADMIN2.username);

  // 3) Logout
  await page.getByRole('button', { name: 'Logout' }).click();
  await expect(page).toHaveURL(/\/login$/);

  // 4) Login as admin2 (expect success)
  await page.getByPlaceholder('Username').fill(ADMIN2.username);
  await page.getByPlaceholder('Password').fill(ADMIN2.initialPassword);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL(/\/?$/);
  await expect(page.getByRole('heading', { level: 1, name: /Prices/i })).toBeVisible();

  // 5) Logout
  await page.getByRole('button', { name: 'Logout' }).click();
  await expect(page).toHaveURL(/\/login$/);

  // 6) Login as default admin
  await page.getByPlaceholder('Username').fill(DEFAULT_ADMIN.username);
  await page.getByPlaceholder('Password').fill(DEFAULT_ADMIN.password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('heading', { level: 1, name: /Prices/i })).toBeVisible();

  // 7) Change password of admin2 to ADMIN2.newPassword (via Users UI)
  await page.getByRole('link', { name: 'Users' }).click();
  // Filter to admin2 so the v-for table shows exactly that user row
  await page.getByPlaceholder('Search by user name').fill(ADMIN2.username);
  // The second row is the row for the filtered user (first is the add row)
  const userRow = page.locator('tbody tr').nth(1);
  await userRow.getByPlaceholder('Password', { exact: true }).fill(ADMIN2.newPassword);
  await userRow.getByPlaceholder('Password (Confirm)', { exact: true }).fill(ADMIN2.newPassword);
  await userRow.getByRole('button', { name: 'Update Password' }).click();

  // Quick confirmation: optional status message appears (do not fail test if missing)
  // We still verify by logging in next.

  // 8) Logout
  await page.getByRole('button', { name: 'Logout' }).click();
  await expect(page).toHaveURL(/\/login$/);

  // 9) Login as admin2 with the new password (expect success)
  await page.getByPlaceholder('Username').fill(ADMIN2.username);
  await page.getByPlaceholder('Password').fill(ADMIN2.newPassword);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('heading', { level: 1, name: /Prices/i })).toBeVisible();

  // 10) Logout
  await page.getByRole('button', { name: 'Logout' }).click();
  await expect(page).toHaveURL(/\/login$/);

  // 11) Login as default admin
  await page.getByPlaceholder('Username').fill(DEFAULT_ADMIN.username);
  await page.getByPlaceholder('Password').fill(DEFAULT_ADMIN.password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('heading', { level: 1, name: /Prices/i })).toBeVisible();

  // 12) Delete admin2 (via Users UI), expect user deleted
  await page.getByRole('link', { name: 'Users' }).click();
  await page.getByPlaceholder('Search by user name').fill(ADMIN2.username);
  // Accept the confirmation dialog when clicking Delete
  page.once('dialog', async (dialog) => {
    await dialog.accept();
  });
  await page.locator('tbody tr').nth(1).getByRole('button', { name: 'Delete' }).click();

  // Verify via UI that user is gone: filtering should leave only the add-new row
  await page.getByPlaceholder('Search by user name').fill(ADMIN2.username);
  await expect(page.locator('tbody tr')).toHaveCount(1);
});
