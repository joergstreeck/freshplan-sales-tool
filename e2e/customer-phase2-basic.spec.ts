import { test, expect } from '@playwright/test';

test.describe('CustomerModuleV2 Phase 2 - Basic Smoke Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Enable debug logging
    await page.addInitScript(() => {
      localStorage.setItem('FP_DEBUG_EVENTS', 'true');
    });
    
    // Navigate with phase2 flag
    await page.goto('/?phase2=true');
    
    // Wait for app initialization
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000); // 3s für CI Vite-Start
    
    // Navigate to customer tab
    await page.click('[data-tab="customer"]');
    
    // Wait for customer panel to be visible
    await page.waitForTimeout(2000); // Give more time for tab animation and event binding
    await page.waitForSelector('#customer.tab-panel', { state: 'visible', timeout: 30000 });
    await page.waitForSelector('#customerForm', { state: 'visible', timeout: 30000 });
  });

  test('Legacy script should be disabled', async ({ page }) => {
    // Check console for skip message
    const consoleMessages: string[] = [];
    page.on('console', msg => consoleMessages.push(msg.text()));
    
    // Reload to capture initialization logs
    await page.reload();
    await page.waitForTimeout(1000);
    
    // Verify legacy skip message
    const hasSkipMessage = consoleMessages.some(msg => 
      msg.includes('Legacy script disabled')
    );
    expect(hasSkipMessage).toBeTruthy();
    
    // Verify legacy functions don't exist
    const hasLegacyFunctions = await page.evaluate(() => {
      return !!(window as any).handleSaveForm || 
             !!(window as any).handleClearForm ||
             !!(window as any).updateCalculator;
    });
    expect(hasLegacyFunctions).toBeFalsy();
  });

  test('Save button should respond to clicks', async ({ page }) => {
    // Fill ALL required fields to avoid validation errors
    await page.fill('#companyName', 'Smoke Test GmbH');
    await page.selectOption('#legalForm', 'gmbh');
    await page.selectOption('#customerType', 'neukunde');
    await page.selectOption('#industry', 'hotel');
    await page.fill('#street', 'Teststraße 123');
    await page.fill('#postalCode', '12345');
    await page.fill('#city', 'Berlin');
    await page.fill('#contactName', 'Test Person');
    await page.fill('#contactPhone', '0301234567');
    await page.fill('#contactEmail', 'smoke@test.com');
    await page.selectOption('#paymentMethod', 'vorkasse');
    
    // Setup dialog handler
    let dialogMessage = '';
    page.on('dialog', async dialog => {
      dialogMessage = dialog.message();
      await dialog.accept();
    });
    
    // Click save
    await page.click('.header-btn-save');
    
    // Wait for dialog
    await page.waitForTimeout(500);
    
    // Either we get a success dialog or validation error
    expect(dialogMessage).toBeTruthy();
    expect(dialogMessage).toContain('erfolgreich');
    console.log('Save dialog:', dialogMessage);
  });

  test('Clear button should respond to clicks', async ({ page }) => {
    // Add some data first
    await page.fill('#companyName', 'To Clear');
    
    // Setup dialog handler
    let confirmDialogShown = false;
    page.on('dialog', async dialog => {
      if (dialog.type() === 'confirm') {
        confirmDialogShown = true;
        await dialog.accept();
      }
    });
    
    // Click clear
    await page.click('.header-btn-clear');
    await page.waitForTimeout(500);
    
    // Should show confirmation dialog
    expect(confirmDialogShown).toBeTruthy();
  });

  test('Event bus should fire customer events', async ({ page }) => {
    // Set up event capturing (already done in beforeEach)
    await page.evaluate(() => {
      window.__capturedEvents = [];
      window.addEventListener('customer:saved', (e) => {
        window.__capturedEvents.push('saved');
        console.log('🎯 Event captured in test: customer:saved', e.detail);
      });
      window.addEventListener('customer:cleared', (e) => {
        window.__capturedEvents.push('cleared');
        console.log('🎯 Event captured in test: customer:cleared', e.detail);
      });
      window.addEventListener('customer:creditCheckRequired', (e) => {
        window.__capturedEvents.push('creditCheck');
        console.log('🎯 Event captured in test: customer:creditCheckRequired', e.detail);
      });
    });
    
    // Fill ALL required fields for save
    await page.fill('#companyName', 'Event Test GmbH');
    await page.selectOption('#legalForm', 'gmbh');
    await page.selectOption('#customerType', 'bestandskunde');
    await page.selectOption('#industry', 'restaurant');
    await page.fill('#street', 'Eventstraße 1');
    await page.fill('#postalCode', '12345');
    await page.fill('#city', 'Hamburg');
    await page.fill('#contactName', 'Event Tester');
    await page.fill('#contactPhone', '040-1234567'); // Longer phone number
    await page.fill('#contactEmail', 'event@test.com');
    await page.selectOption('#paymentMethod', 'vorkasse');
    
    // Click save
    await page.click('.header-btn-save');
    await page.waitForTimeout(500);
    
    // Check if event was captured
    const events = await page.evaluate(() => window.__capturedEvents || []);
    console.log('Captured events:', events);
    
    // We should have at least one event
    expect(events.length).toBeGreaterThan(0);
    expect(events).toContain('saved');
  });

  test('Payment warning should trigger for Neukunde + Rechnung', async ({ page }) => {
    // Setup event listener for creditCheckRequired
    const creditCheckPromise = page.evaluate(() => {
      return new Promise<boolean>(resolve => {
        window.addEventListener('customer:creditCheckRequired', () => {
          resolve(true);
        });
        // Timeout fallback
        setTimeout(() => resolve(false), 2000);
      });
    });
    
    // Select Neukunde (using customerType for compatibility)
    await page.selectOption('#customerType', 'neukunde');
    
    // Setup dialog handler
    let warningShown = false;
    let warningMessage = '';
    page.on('dialog', async dialog => {
      warningMessage = dialog.message();
      if (warningMessage.includes('Bonitätsprüfung')) {
        warningShown = true;
      }
      await dialog.accept();
    });
    
    // Select Rechnung - should trigger warning
    await page.selectOption('#paymentMethod', 'rechnung');
    
    // Wait for event
    const eventFired = await creditCheckPromise;
    
    expect(warningShown).toBeTruthy();
    expect(warningMessage).toContain('Bonitätsprüfung');
    expect(eventFired).toBeTruthy(); // Verify event was fired
  });
});