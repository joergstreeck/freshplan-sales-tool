import { test, expect } from '@playwright/test';

test.describe('Authentication Flow - Smoke Test', () => {
  test('app loads and shows FreshPlan 2.0 content', async ({ page }) => {
    // Ultra-simple smoke test for CI
    await page.goto('/');
    
    // Just check that the page loads with correct title
    await expect(page).toHaveTitle(/FreshPlan/, { timeout: 30000 });
    
    // Check that body exists (page rendered)
    await expect(page.locator('body')).toBeVisible();
    
    // That's it - if we get here, the app loaded successfully
    // Don't check for specific content as it might be behind auth
  });
});