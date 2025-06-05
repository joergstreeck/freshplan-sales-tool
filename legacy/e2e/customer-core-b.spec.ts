import { test, expect } from '@playwright/test';

test.describe('Customer Core B - Validation & Business Rules', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/?phase2=true');
    await page.waitForLoadState('networkidle');
    
    // Wait for app initialization
    await page.waitForSelector('.nav', { timeout: 15000 });
    
    // Navigate to customer tab
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customerForm', { state: 'visible', timeout: 15000 });
  });

  test('Email validation', async ({ page }) => {
    // Fill form with invalid email
    await page.fill('#companyName', 'Test GmbH');
    await page.fill('#contactEmail', 'invalid-email');
    
    // Try to save
    let errorMessage = '';
    page.once('dialog', async dialog => {
      errorMessage = dialog.message();
      await dialog.accept();
    });
    
    await page.click('.header-btn-save');
    await page.waitForTimeout(1000);
    
    expect(errorMessage).toContain('E-Mail');
  });

  test('Phone number validation', async ({ page }) => {
    // Fill form with short phone number
    await page.fill('#companyName', 'Test GmbH');
    await page.fill('#contactPhone', '123'); // Too short
    await page.fill('#contactEmail', 'test@example.com');
    
    // Try to save
    let errorMessage = '';
    page.once('dialog', async dialog => {
      errorMessage = dialog.message();
      await dialog.accept();
    });
    
    await page.click('.header-btn-save');
    await page.waitForTimeout(1000);
    
    expect(errorMessage).toContain('Telefonnummer');
  });

  test('Payment warning for new customer with invoice', async ({ page }) => {
    // Select new customer
    await page.selectOption('#customerType', 'neukunde');
    
    // Setup dialog handler for warning
    let warningShown = false;
    page.once('dialog', async dialog => {
      if (dialog.message().includes('Bonitätsprüfung')) {
        warningShown = true;
      }
      await dialog.accept();
    });
    
    // Select invoice payment
    await page.selectOption('#paymentMethod', 'rechnung');
    await page.waitForTimeout(1000);
    
    // Should show warning
    expect(warningShown).toBeTruthy();
  });

  test('No warning for existing customer with invoice', async ({ page }) => {
    // Select existing customer
    await page.selectOption('#customerType', 'bestandskunde');
    
    // Setup dialog handler
    let warningShown = false;
    page.on('dialog', async dialog => {
      if (dialog.message().includes('Bonitätsprüfung')) {
        warningShown = true;
      }
      await dialog.accept();
    });
    
    // Select invoice payment
    await page.selectOption('#paymentMethod', 'rechnung');
    await page.waitForTimeout(1000);
    
    // Should NOT show warning
    expect(warningShown).toBeFalsy();
  });

  test('Data persistence after save', async ({ page }) => {
    const testData = {
      company: 'Persistence Test GmbH',
      contact: 'Test Person',
      phone: '030123456789'
    };
    
    // Fill and save
    await page.fill('#companyName', testData.company);
    await page.selectOption('#legalForm', 'gmbh');
    await page.selectOption('#customerType', 'bestandskunde');
    await page.selectOption('#industry', 'hotel');
    await page.fill('#street', 'Teststraße 1');
    await page.fill('#postalCode', '12345');
    await page.fill('#city', 'Berlin');
    await page.fill('#contactName', testData.contact);
    await page.fill('#contactPhone', testData.phone);
    await page.fill('#contactEmail', 'test@example.com');
    await page.selectOption('#paymentMethod', 'vorkasse');
    
    // Save
    page.once('dialog', async dialog => {
      await dialog.accept();
    });
    await page.click('.header-btn-save');
    await page.waitForTimeout(1000);
    
    // Reload page
    await page.reload();
    await page.waitForLoadState('networkidle');
    await page.click('[data-tab="customer"]');
    await page.waitForSelector('#customerForm', { state: 'visible' });
    
    // Check data is restored
    await expect(page.locator('#companyName')).toHaveValue(testData.company);
    await expect(page.locator('#contactName')).toHaveValue(testData.contact);
    await expect(page.locator('#contactPhone')).toHaveValue(testData.phone);
  });
});