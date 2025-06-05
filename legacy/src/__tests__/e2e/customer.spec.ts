import { test, expect } from '@playwright/test';

test.describe('Customer Module', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Navigate to customer tab
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customer-form');
  });

  test('should fill and save customer data', async ({ page }) => {
    // Fill basic info
    await page.fill('#companyName', 'Test Restaurant GmbH');
    await page.fill('#contactName', 'Max Mustermann');
    await page.fill('#contactEmail', 'max@test.de');
    await page.fill('#contactPhone', '089/123456');
    
    // Select industry
    await page.selectOption('#industry', 'restaurant');
    
    // Fill address
    await page.fill('#street', 'Teststraße 123');
    await page.fill('#zipCode', '80331');
    await page.fill('#city', 'München');
    
    // Save
    await page.click('#saveCustomerBtn');
    
    // Check success notification
    await expect(page.locator('.notification.success')).toBeVisible();
  });

  test('should validate required fields', async ({ page }) => {
    // Try to save without filling required fields
    await page.click('#saveCustomerBtn');
    
    // Check for validation errors
    await expect(page.locator('#companyName')).toHaveClass(/error/);
    await expect(page.locator('.error-message')).toBeVisible();
  });

  test('should show industry-specific fields', async ({ page }) => {
    // Select hotel
    await page.selectOption('#industry', 'hotel');
    
    // Check hotel-specific fields
    await expect(page.locator('#rooms')).toBeVisible();
    await expect(page.locator('#occupancyRate')).toBeVisible();
    await expect(page.locator('#breakfastPrice')).toBeVisible();
    
    // Switch to restaurant
    await page.selectOption('#industry', 'restaurant');
    
    // Check restaurant-specific fields
    await expect(page.locator('#seats')).toBeVisible();
    await expect(page.locator('#turnsPerDay')).toBeVisible();
    await expect(page.locator('#operatingDays')).toBeVisible();
  });

  test('should toggle customer type', async ({ page }) => {
    // Click chain option
    await page.click('[data-type="chain"]');
    
    // Check chain fields are visible
    await expect(page.locator('#chainQuickEntry')).toBeVisible();
    await expect(page.locator('#totalLocations')).toBeVisible();
    await expect(page.locator('#avgOrderPerLocation')).toBeVisible();
  });
});