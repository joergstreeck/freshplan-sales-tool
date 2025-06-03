import { test, expect } from '@playwright/test';

test('Debug Save Button Click', async ({ page }) => {
  // Enable console logging
  page.on('console', msg => console.log('[Browser]', msg.text()));
  page.on('pageerror', err => console.error('[Page Error]', err));
  
  // Mock alert and track what happens
  await page.addInitScript(() => {
    window.__saveClicked = false;
    window.__alertCalled = false;
    window.__alertMessage = '';
    
    // Override alert
    window.alert = (msg: string) => {
      window.__alertCalled = true;
      window.__alertMessage = msg;
      console.log('🚨 Alert called:', msg);
    };
    
    // Track save button clicks at the earliest point
    document.addEventListener('click', (e) => {
      const target = e.target as HTMLElement;
      if (target.classList.contains('header-btn-save')) {
        window.__saveClicked = true;
        console.log('🎯 Save button clicked!', {
          defaultPrevented: e.defaultPrevented,
          propagationStopped: e.cancelBubble
        });
      }
    }, { capture: true }); // Use capture phase
    
    // Track customer:saved events
    window.addEventListener('customer:saved', (e) => {
      console.log('✅ customer:saved event fired!', e.detail);
    });
  });
  
  await page.goto('/?phase2=true');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  // Go to customer tab
  await page.click('[data-tab="customer"]');
  await page.waitForSelector('#customer.tab-panel', { state: 'visible' });
  await page.waitForTimeout(1000);
  
  // Check if save button exists and is visible
  const saveButton = await page.$('.header-btn-save');
  expect(saveButton).toBeTruthy();
  
  const isVisible = await saveButton?.isVisible();
  console.log('Save button visible:', isVisible);
  
  // Get button info
  const buttonInfo = await page.evaluate(() => {
    const btn = document.querySelector('.header-btn-save') as HTMLElement;
    if (!btn) return null;
    
    return {
      tagName: btn.tagName,
      innerHTML: btn.innerHTML,
      onclick: !!btn.onclick,
      hasClickListeners: !!(btn as any)._listeners,
      computedDisplay: getComputedStyle(btn).display,
      computedVisibility: getComputedStyle(btn).visibility,
      offsetWidth: btn.offsetWidth,
      offsetHeight: btn.offsetHeight
    };
  });
  console.log('Button info:', buttonInfo);
  
  // Fill minimal form to avoid validation errors
  await page.fill('#companyName', 'Debug Test');
  await page.selectOption('#legalForm', 'gmbh');
  await page.selectOption('#customerType', 'bestandskunde');
  await page.selectOption('#industry', 'hotel');
  await page.fill('#street', 'Test 1');
  await page.fill('#postalCode', '12345');
  await page.fill('#city', 'Berlin');
  await page.fill('#contactName', 'Tester');
  await page.fill('#contactPhone', '123456');
  await page.fill('#contactEmail', 'test@test.com');
  
  // Try clicking the save button
  console.log('\n=== Attempting to click save button ===');
  await page.click('.header-btn-save');
  await page.waitForTimeout(1000);
  
  // Check what happened
  const results = await page.evaluate(() => {
    return {
      saveClicked: (window as any).__saveClicked,
      alertCalled: (window as any).__alertCalled,
      alertMessage: (window as any).__alertMessage
    };
  });
  
  console.log('Results:', results);
  
  expect(results.saveClicked).toBeTruthy();
  expect(results.alertCalled).toBeTruthy();
});