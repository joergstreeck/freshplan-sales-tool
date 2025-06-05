import { test, expect } from '@playwright/test';

test.describe('CustomerModuleV2 Phase 2 - Basic Test', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate with phase2 flag
    await page.goto('/?phase2=true');
    
    // Wait for network to be idle
    await page.waitForLoadState('networkidle');
    
    // Wait for app to be ready (simplified approach)
    await page.waitForSelector('.nav', { timeout: 15000 });
    
    // Navigate to customer tab
    const customerTab = page.locator('[data-tab="customer"]');
    await customerTab.click();
    
    // Wait for customer form to be visible
    await page.waitForSelector('#customerForm', { state: 'visible', timeout: 15000 });
  });

  test('Legacy script should be disabled', async ({ page }) => {
    // Reload to capture initialization logs
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Verify legacy functions don't exist
    const hasLegacyFunctions = await page.evaluate(() => {
      return !!(window as any).handleSaveForm || 
             !!(window as any).handleClearForm ||
             !!(window as any).updateCalculator;
    });
    expect(hasLegacyFunctions).toBeFalsy();
  });

  test('Save button should respond to clicks', async ({ page }) => {
    // Fill only the most essential fields
    await page.fill('#companyName', 'Test GmbH');
    await page.selectOption('#legalForm', 'gmbh');
    await page.selectOption('#customerType', 'neukunde');
    
    // Setup dialog handler
    let dialogAppeared = false;
    page.once('dialog', async dialog => {
      dialogAppeared = true;
      await dialog.accept();
    });
    
    // Click save
    await page.click('.header-btn-save');
    
    // Wait a bit for dialog
    await page.waitForTimeout(2000);
    
    // Either dialog appeared or form was cleared (both are valid)
    const companyNameValue = await page.inputValue('#companyName');
    expect(dialogAppeared || companyNameValue === '').toBeTruthy();
  });
});