import { test, expect } from '@playwright/test';

test.describe('Calculator Module', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Wait for app to initialize
    await page.waitForSelector('#orderValue');
  });

  test('should calculate discount correctly', async ({ page }) => {
    // Set order value
    await page.fill('#orderValue', '20000');
    
    // Set lead time
    await page.fill('#leadTime', '15');
    
    // Enable pickup
    await page.click('#pickupToggle');
    
    // Check results
    await expect(page.locator('#baseDiscountValue')).toContainText('8%');
    await expect(page.locator('#earlyDiscountValue')).toContainText('2%');
    await expect(page.locator('#pickupDiscountValue')).toContainText('2%');
    await expect(page.locator('#totalDiscountValue')).toContainText('12%');
  });

  test('should apply scenarios', async ({ page }) => {
    // Click on "Geplante Bestellung" scenario
    await page.click('[data-scenario="planned"]');
    
    // Verify values are set
    const orderValue = await page.inputValue('#orderValue');
    expect(parseInt(orderValue)).toBe(25000);
    
    const leadTime = await page.inputValue('#leadTime');
    expect(parseInt(leadTime)).toBe(10);
  });

  test('should update slider progress', async ({ page }) => {
    const slider = page.locator('#orderValue');
    
    // Check initial progress
    const initialProgress = await slider.evaluate((el: HTMLInputElement) => 
      getComputedStyle(el).getPropertyValue('--progress')
    );
    
    // Change value
    await slider.fill('50000');
    
    // Check updated progress
    const updatedProgress = await slider.evaluate((el: HTMLInputElement) => 
      getComputedStyle(el).getPropertyValue('--progress')
    );
    
    expect(initialProgress).not.toBe(updatedProgress);
  });
});