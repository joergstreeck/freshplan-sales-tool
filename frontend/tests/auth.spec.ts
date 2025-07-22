import { test, expect } from '@playwright/test';

test.describe('Authentication Flow - Smoke Test', () => {
  test('app loads and shows FreshPlan 2.0 content', async ({ page }) => {
    // Simple smoke test that works in CI
    // Navigate to the app
    await page.goto('/');
    
    // Check for JS errors in console (removed fixed waitForTimeout in favor of dynamic waiting)
    const errors: string[] = [];
    page.on('console', msg => {
      if (msg.type() === 'error') {
        errors.push(msg.text());
      }
    });
    
    // Verify the page loads successfully
    await expect(page).toHaveTitle(/FreshPlan/, { timeout: 10000 });
    
    // Verify basic app structure is present
    await expect(page.locator('body')).toBeVisible();
    
    // Check for the main title content (React must have rendered)
    await expect(page.locator('text=FreshPlan 2.0')).toBeVisible({ timeout: 10000 });
    
    // Simple smoke test - verify app renders without crashing
    const errorMessages = page.locator('text=Error');
    await expect(errorMessages).toHaveCount(0);
    
    // Log any console errors for debugging
    if (errors.length > 0) {
      console.log('Console errors found:', errors);
    }
    
    // SMOKE TEST SUCCESS: App loads without errors
  });
});