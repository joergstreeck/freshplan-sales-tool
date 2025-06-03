import { test, expect } from '@playwright/test';

test.describe('Debug CustomerModuleV2', () => {
  test('Check tab navigation and module loading', async ({ page }) => {
    // Enable debug logging and capture console
    const consoleLogs: string[] = [];
    page.on('console', msg => {
      const text = msg.text();
      consoleLogs.push(text);
      console.log('[Browser]', text);
    });
    
    await page.addInitScript(() => {
      localStorage.setItem('FP_DEBUG_EVENTS', 'true');
    });
    
    // Navigate with phase2 flag
    await page.goto('/?phase2=true');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);
    
    // Debug: Check if tabs are visible
    const tabsVisible = await page.locator('.nav-tabs').isVisible();
    console.log('Tabs visible:', tabsVisible);
    
    // Debug: Check all tab buttons
    const tabButtons = await page.locator('.nav-tab').all();
    console.log('Number of tab buttons:', tabButtons.length);
    
    for (const button of tabButtons) {
      const tabName = await button.getAttribute('data-tab');
      const text = await button.textContent();
      console.log(`Tab: ${tabName} - Text: ${text?.trim()}`);
    }
    
    // Debug: Check current active tab
    const activeTab = await page.locator('.nav-tab.active').first();
    const activeTabName = await activeTab.getAttribute('data-tab');
    console.log('Active tab:', activeTabName);
    
    // Try to click customer tab
    console.log('Clicking customer tab...');
    await page.click('[data-tab="customer"]');
    await page.waitForTimeout(1000);
    
    // Check if customer panel is visible now
    const customerPanelVisible = await page.locator('#customer').isVisible();
    console.log('Customer panel visible after click:', customerPanelVisible);
    
    // Check display style
    const displayStyle = await page.locator('#customer').evaluate(el => {
      return window.getComputedStyle(el).display;
    });
    console.log('Customer panel display style:', displayStyle);
    
    // Check if form exists
    const formExists = await page.locator('#customerForm').count() > 0;
    console.log('Customer form exists:', formExists);
    
    // Print relevant console logs
    console.log('\n=== Console logs containing "legacy" or "customer" ===');
    consoleLogs
      .filter(log => log.toLowerCase().includes('legacy') || log.toLowerCase().includes('customer'))
      .forEach(log => console.log(log));
    
    // Take screenshot
    await page.screenshot({ path: 'debug-customer-tab.png', fullPage: true });
    
    // Basic assertions
    expect(tabsVisible).toBeTruthy();
    expect(tabButtons.length).toBeGreaterThan(0);
  });
});