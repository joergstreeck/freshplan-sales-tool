import { test, expect } from '@playwright/test';
test('threads page smoke', async ({ page }) => {
  await page.goto('/comm');
  await expect(page).toHaveTitle(/FreshPlan/);
});
