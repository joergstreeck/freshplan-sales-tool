import { test, expect } from '@playwright/test';

test.describe('Authentication Flow - Smoke Test', () => {
  test('user can login and access Sales Cockpit', async ({ page }) => {
    // Navigate to the app
    await page.goto('/');
    
    // Clear any existing authentication
    await page.context().clearCookies();
    await page.evaluate(() => {
      localStorage.clear();
    });
    
    // Navigate to the login bypass page (current dev setup)
    await page.goto('/login-bypass');
    
    // Verify we're on the login bypass page
    await expect(page.locator('text="Development Login Bypass"')).toBeVisible();
    
    // Click to login as Sales user (sales@freshplan.de)
    await page.getByRole('button', { name: 'Login as Sales' }).click();
    
    // Should redirect to home page
    await page.waitForURL('/');
    await expect(page.locator('h1:has-text("FreshPlan 2.0")')).toBeVisible();
    
    // Navigate to Sales Cockpit
    await page.getByRole('link', { name: /Sales Cockpit öffnen/i }).click();
    
    // Wait for navigation and verify we're on the Sales Cockpit
    await page.waitForURL('/cockpit');
    await expect(page.locator('[data-testid="sales-cockpit"]')).toBeVisible({ timeout: 10000 });
    
    // ACCEPTANCE CRITERIA: Verify the three main columns are visible
    await expect(page.locator('text="Mein Tag"')).toBeVisible();
    await expect(page.locator('text="Fokus-Liste"')).toBeVisible();
    await expect(page.locator('text="Aktions-Center"')).toBeVisible();
    
    // SMOKE TEST SUCCESS: User logged in as sales@freshplan.de and reached Sales Cockpit
  });
});