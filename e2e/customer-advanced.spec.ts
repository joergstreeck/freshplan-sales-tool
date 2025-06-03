import { test, expect } from '@playwright/test';

test.describe('Customer Advanced - Language & Special Cases', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/?phase2=true');
    await page.waitForLoadState('networkidle');
    
    // Wait for app initialization
    await page.waitForSelector('.nav', { timeout: 15000 });
  });

  test('Language switch preserves form data', async ({ page }) => {
    // Navigate to customer form
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customerForm', { state: 'visible' });
    
    // Fill some data
    const testValue = 'Test Company International';
    await page.fill('#companyName', testValue);
    
    // Switch language
    await page.selectOption('#languageSelect', 'en');
    await page.waitForTimeout(500);
    
    // Data should be preserved
    await expect(page.locator('#companyName')).toHaveValue(testValue);
    
    // Switch back
    await page.selectOption('#languageSelect', 'de');
    await page.waitForTimeout(500);
    
    // Data still preserved
    await expect(page.locator('#companyName')).toHaveValue(testValue);
  });

  test('Postal code validation (5 digits)', async ({ page }) => {
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customerForm', { state: 'visible' });
    
    // Try invalid postal codes
    await page.fill('#postalCode', '123'); // Too short
    await page.fill('#companyName', 'Test');
    
    let errorMessage = '';
    page.once('dialog', async dialog => {
      errorMessage = dialog.message();
      await dialog.accept();
    });
    
    await page.click('.header-btn-save');
    await page.waitForTimeout(1000);
    
    expect(errorMessage).toContain('PLZ');
  });

  test('Calculator integration - values persist', async ({ page }) => {
    // Set calculator values
    await page.locator('#orderValue').fill('50000');
    await page.locator('#leadTime').fill('20');
    
    // Navigate to customer
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customerForm', { state: 'visible' });
    
    // Navigate back to calculator
    await page.click('[data-tab="calculator"]');
    
    // Values should be preserved
    await expect(page.locator('#orderValue')).toHaveValue('50000');
    await expect(page.locator('#leadTime')).toHaveValue('20');
  });

  test('Form state after failed save', async ({ page }) => {
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customerForm', { state: 'visible' });
    
    // Fill partial data (missing required fields)
    const testCompany = 'Partial Data Corp';
    await page.fill('#companyName', testCompany);
    await page.selectOption('#legalForm', 'gmbh');
    // Missing other required fields
    
    // Try to save
    page.once('dialog', async dialog => {
      await dialog.accept();
    });
    
    await page.click('.header-btn-save');
    await page.waitForTimeout(1000);
    
    // Form data should still be there after failed save
    await expect(page.locator('#companyName')).toHaveValue(testCompany);
    await expect(page.locator('#legalForm')).toHaveValue('gmbh');
  });
});