import { test, expect } from '@playwright/test';

// This test verifies that ConfigInitializationRunner actions were executed
// on a fresh application start: default admin user creation (via successful login),
// initial Material 'Gold', and initial Unit 'Oz'.
test('initialization runner creates default admin, Gold material, and Oz unit', async ({ page }) => {
  // Login via API to obtain JWT cookie in this browser context
  const loginResp = await page.request.post('/api/auth/login', {
    data: { username: 'admin', password: 'admin1Password!' },
  });
  expect(loginResp.ok()).toBeTruthy();

  // Verify users list contains the default admin
  const usersResp = await page.request.get('/api/userService');
  expect(usersResp.ok()).toBeTruthy();
  const usersJson = await usersResp.json();
  // Shape: { userInfos: [{ userName: string, active: boolean }, ...] }
  const usernames: string[] = (usersJson?.userInfos || []).map((u: any) => u.userName);
  expect(usernames).toContain('admin');

  // Verify initial Material "Gold" exists with a valid price and date
  const materialsResp = await page.request.get('/api/materials');
  expect(materialsResp.ok()).toBeTruthy();
  const materials = await materialsResp.json();
  const gold = Array.isArray(materials) ? materials.find((m: any) => m?.name === 'Gold') : undefined;
  expect(gold).toBeTruthy();
  expect(typeof gold.price === 'number' && gold.price > 0).toBeTruthy();
  expect(gold.entryDate).toBeTruthy();

  // Verify initial Unit "Oz" exists with factor 1
  const unitsResp = await page.request.get('/api/units');
  expect(unitsResp.ok()).toBeTruthy();
  const units = await unitsResp.json();
  const oz = Array.isArray(units) ? units.find((u: any) => u?.name === 'Oz') : undefined;
  expect(oz).toBeTruthy();
  expect(oz.factor).toBe(1);
});

