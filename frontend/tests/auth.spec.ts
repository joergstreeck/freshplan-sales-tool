import { test, expect } from '@playwright/test';

test.describe('Authentication Flow - Smoke Test', () => {
  test('app loads and shows FreshPlan 2.0 content', async ({ page }) => {
    // Simple smoke test that works in CI
    // Navigate to the app
    await page.goto('/');
    
    // Verify the page loads successfully
    await expect(page).toHaveTitle(/FreshPlan/);
    
    // Verify basic app structure is present
    await expect(page.locator('body')).toBeVisible();
    
    // Simple smoke test - verify app renders without crashing
    const errorMessages = page.locator('text=Error');
    await expect(errorMessages).toHaveCount(0);
    
    // SMOKE TEST SUCCESS: App loads without errors
  });
});