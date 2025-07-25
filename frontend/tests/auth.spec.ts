import { test, expect } from '@playwright/test';

test.describe('Authentication Flow - Smoke Test', () => {
  test('app loads and shows FreshPlan 2.0 content', async ({ page }) => {
    // Simple smoke test that works in CI
    // Navigate to the app
    await page.goto('/');
    
    // Wait longer for the app to initialize (API call + React render)
    await page.waitForTimeout(3000);
    
    // Check for JS errors in console
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
    // Try multiple selectors to find the title
    const titleLocator = page.locator('h1.main-title, h1:has-text("FreshPlan"), text=FreshPlan 2.0').first();
    await expect(titleLocator).toBeVisible({ timeout: 15000 });
    
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