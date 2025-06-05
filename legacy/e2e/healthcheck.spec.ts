import { test, expect } from '@playwright/test';

test.describe('Healthcheck', () => {
  test('App loads and shows navigation', async ({ page }) => {
    // Navigate to app
    await page.goto('/');
    
    // Wait for navigation to be visible
    await page.waitForSelector('.nav', { timeout: 10000 });
    
    // Check navigation exists
    await expect(page.locator('.nav')).toBeVisible();
    
    // Check at least one tab is visible
    await expect(page.locator('.nav-tab').first()).toBeVisible();
    
    // Check that calculator tab is active by default
    await expect(page.locator('.nav-tab.active')).toHaveText(/Rabattrechner/);
    
    // Check main content area is visible
    await expect(page.locator('.tab-panel.active')).toBeVisible();
  });
});