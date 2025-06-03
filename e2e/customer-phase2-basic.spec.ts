import { test, expect } from '@playwright/test';

test.describe('CustomerModuleV2 Phase 2 - Basic Smoke Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Log console messages
    page.on('console', msg => console.log('[Browser]', msg.type(), msg.text()));
    page.on('pageerror', err => console.error('[PageError]', err));
    
    // Enable debug logging
    await page.addInitScript(() => {
      localStorage.setItem('FP_DEBUG_EVENTS', 'true');
    });
    
    // Navigate with phase2 flag
    await page.goto('/?phase2=true');
    
    // Wait for app initialization - try multiple strategies
    await page.waitForLoadState('networkidle');
    
    // Wait for app to be loaded (with multiple fallbacks)
    const appReady = await Promise.race([
      // Strategy 1: Wait for loaded class
      page.waitForSelector('#app.loaded', { timeout: 30000 }).then(() => 'loaded-class'),
      
      // Strategy 2: Wait for global ready flag
      page.waitForFunction(() => (window as any).__FP_APP_READY__ === true, { timeout: 30000 }).then(() => 'global-flag'),
      
      // Strategy 3: Wait 5s and check if app is visible
      page.waitForTimeout(5000).then(async () => {
        const appVisible = await page.locator('#app').isVisible();
        const loadingHidden = await page.locator('#loading').isHidden().catch(() => true);
        if (appVisible && loadingHidden) {
          return 'visible-fallback';
        }
        throw new Error('App not ready after 5s');
      })
    ]).catch(err => {
      console.error('All app ready strategies failed:', err);
      throw new Error('App failed to initialize');
    });
    
    console.log(`App ready via: ${appReady}`);
    
    // Navigate to customer tab
    await page.click('[data-tab="customer"]');
    
    // Wait for customer panel to be visible
    await page.waitForTimeout(2000); // Give more time for tab animation and event binding
    await page.waitForSelector('#customer.tab-panel', { state: 'visible', timeout: 30000 });
    await page.waitForSelector('#customerForm', { state: 'visible', timeout: 30000 });
    
    // Wait for select options to be populated
    await page.waitForSelector('#customerType option[value="neukunde"]', { timeout: 30000 });
    await page.waitForSelector('#paymentMethod option[value="rechnung"]', { timeout: 30000 });
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
    // Debug: Check what options are available
    console.log('=== Debugging Select Options ===');
    const legalFormOptions = await page.locator('#legalForm').innerHTML();
    console.log('LegalForm options:', legalFormOptions);
    
    const customerTypeOptions = await page.locator('#customerType').innerHTML();
    console.log('CustomerType options:', customerTypeOptions);
    
    const industryOptions = await page.locator('#industry').innerHTML();
    console.log('Industry options:', industryOptions);
    
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
    let dialogAppeared = false;
    page.once('dialog', async dialog => {
      dialogMessage = dialog.message();
      dialogAppeared = true;
      await dialog.accept();
    });
    
    // Click save
    await page.click('.header-btn-save');
    
    // Wait for dialog OR toast notification
    await Promise.race([
      page.waitForEvent('dialog', { timeout: 2000 }).catch(() => null),
      page.waitForSelector('.toast, .notification, .fp-toast', { timeout: 2000 }).catch(() => null)
    ]);
    
    // Check what happened
    if (dialogAppeared) {
      expect(dialogMessage).toBeTruthy();
      expect(dialogMessage).toContain('erfolgreich');
      console.log('Save dialog:', dialogMessage);
    } else {
      // Check for toast notifications
      const toast = await page.locator('.toast, .notification, .fp-toast').first();
      if (await toast.isVisible()) {
        const toastText = await toast.textContent();
        expect(toastText).toContain('erfolgreich');
        console.log('Save toast:', toastText);
      } else {
        // Maybe success is indicated differently - check if form was cleared
        const companyNameValue = await page.inputValue('#companyName');
        expect(companyNameValue).toBe(''); // Form should be cleared after successful save
      }
    }
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
    
    // Debug: Log available options
    const customerTypeHTML = await page.locator('#customerType').innerHTML();
    console.log('CustomerType select options:', customerTypeHTML);
    
    // Wait for options to be available
    await page.waitForSelector('#customerType option', { timeout: 5000 });
    
    // Select Neukunde (using customerType for compatibility)
    await page.selectOption('#customerType', 'neukunde');
    
    // Setup dialog handler
    let warningShown = false;
    let warningMessage = '';
    page.once('dialog', async dialog => {
      warningMessage = dialog.message();
      if (warningMessage.includes('Bonitätsprüfung')) {
        warningShown = true;
      }
      await dialog.accept();
    });
    
    // Debug: Log payment method options
    const paymentMethodHTML = await page.locator('#paymentMethod').innerHTML();
    console.log('PaymentMethod select options:', paymentMethodHTML);
    
    // Wait for payment options
    await page.waitForSelector('#paymentMethod option', { timeout: 5000 });
    
    // Select Rechnung - should trigger warning
    await page.selectOption('#paymentMethod', 'rechnung');
    
    // Wait for event or toast
    const [eventFired, possibleToast] = await Promise.all([
      creditCheckPromise,
      page.waitForSelector('.toast, .notification, .fp-toast', { timeout: 2000 }).catch(() => null)
    ]);
    
    // Check if warning was shown (dialog or toast)
    if (!warningShown && possibleToast) {
      const toastText = await possibleToast.textContent();
      if (toastText && toastText.includes('Bonitätsprüfung')) {
        warningShown = true;
        warningMessage = toastText;
        console.log('Warning shown as toast:', toastText);
      }
    }
    
    expect(warningShown).toBeTruthy();
    expect(warningMessage).toContain('Bonitätsprüfung');
    expect(eventFired).toBeTruthy(); // Verify event was fired
  });
});