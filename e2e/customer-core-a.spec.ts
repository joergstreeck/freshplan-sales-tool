import { test, expect } from '@playwright/test';

test.describe('Customer Core A - Basic Form Operations', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/?phase2=true');
    await page.waitForLoadState('networkidle');
    
    // Warte auf App-Initialisierung
    await page.waitForSelector('.nav', { timeout: 15000 });
    
    // Navigiere zum Kundendaten-Tab
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customerForm', { state: 'visible', timeout: 15000 });
  });

  test('Form loads correctly', async ({ page }) => {
    // Check form exists
    await expect(page.locator('#customerForm')).toBeVisible();
    
    // Check essential fields exist
    await expect(page.locator('#companyName')).toBeVisible();
    await expect(page.locator('#legalForm')).toBeVisible();
    await expect(page.locator('#customerType')).toBeVisible();
    await expect(page.locator('.header-btn-save')).toBeVisible();
    await expect(page.locator('.header-btn-clear')).toBeVisible();
  });

  test('Required fields validation', async ({ page }) => {
    // Try to save empty form
    let dialogMessage = '';
    page.once('dialog', async dialog => {
      dialogMessage = dialog.message();
      await dialog.accept();
    });
    
    await page.click('.header-btn-save');
    await page.waitForTimeout(1000); // Give dialog time to appear
    
    expect(dialogMessage).toBeTruthy();
    expect(dialogMessage).toContain('Pflichtfeld');
  });

  test('Save with valid data', async ({ page }) => {
    // Fill minimum required fields
    await page.fill('#companyName', 'Test Company GmbH');
    await page.selectOption('#legalForm', 'gmbh');
    await page.selectOption('#customerType', 'neukunde');
    await page.selectOption('#industry', 'hotel');
    await page.fill('#street', 'Teststraße 123');
    await page.fill('#postalCode', '12345');
    await page.fill('#city', 'Berlin');
    await page.fill('#contactName', 'Test Person');
    await page.fill('#contactPhone', '030123456789');
    await page.fill('#contactEmail', 'test@example.com');
    await page.selectOption('#paymentMethod', 'vorkasse');
    
    // Save and expect success
    let dialogMessage = '';
    page.once('dialog', async dialog => {
      dialogMessage = dialog.message();
      await dialog.accept();
    });
    
    await page.click('.header-btn-save');
    await page.waitForTimeout(1000);
    
    expect(dialogMessage).toContain('erfolgreich');
  });

  test('Clear form with confirmation', async ({ page }) => {
    // Add some data
    await page.fill('#companyName', 'Test to Clear');
    
    // Clear with confirmation
    let dialogType = '';
    let dialogMessage = '';
    page.once('dialog', async dialog => {
      dialogType = dialog.type();
      dialogMessage = dialog.message();
      await dialog.accept();
    });
    
    await page.click('.header-btn-clear');
    await page.waitForTimeout(1000);
    
    expect(dialogType).toBe('confirm');
    expect(dialogMessage).toContain('löschen');
    
    // Check field is cleared
    await expect(page.locator('#companyName')).toHaveValue('');
  });

  test('Clear form cancel keeps data', async ({ page }) => {
    // Add some data
    const testValue = 'Test Keep Data';
    await page.fill('#companyName', testValue);
    
    // Clear but cancel
    page.once('dialog', async dialog => {
      await dialog.dismiss(); // Cancel
    });
    
    await page.click('.header-btn-clear');
    await page.waitForTimeout(1000);
    
    // Check data is still there
    await expect(page.locator('#companyName')).toHaveValue(testValue);
  });
});