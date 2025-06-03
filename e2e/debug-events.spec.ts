import { test, expect } from '@playwright/test';

test('Debug event firing', async ({ page }) => {
  // Enable debug and capture console
  const consoleLogs: string[] = [];
  page.on('console', msg => {
    consoleLogs.push(msg.text());
    console.log('[Browser]', msg.text());
  });
  
  await page.addInitScript(() => {
    localStorage.setItem('FP_DEBUG_EVENTS', 'true');
  });
  
  // Set up event listeners BEFORE navigation
  await page.addInitScript(() => {
    window.addEventListener('customer:saved', (e) => {
      console.log('🎯 Event captured: customer:saved', e.detail);
    });
    window.addEventListener('customer:cleared', (e) => {
      console.log('🎯 Event captured: customer:cleared', e.detail);
    });
    window.addEventListener('customer:creditCheckRequired', (e) => {
      console.log('🎯 Event captured: customer:creditCheckRequired', e.detail);
    });
  });
  
  await page.goto('/?phase2=true');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(2000);
  
  // Navigate to customer tab
  await page.click('[data-tab="customer"]');
  await page.waitForTimeout(1000);
  
  // Fill minimal required fields
  await page.fill('#companyName', 'Debug Test GmbH');
  await page.selectOption('#legalForm', 'gmbh');
  await page.selectOption('#customerType', 'neukunde');
  await page.selectOption('#industry', 'hotel');
  await page.fill('#street', 'Teststraße 1');
  await page.fill('#postalCode', '12345');
  await page.fill('#city', 'Berlin');
  await page.fill('#contactName', 'Debug Tester');
  await page.fill('#contactPhone', '030123456');
  await page.fill('#contactEmail', 'debug@test.com');
  await page.selectOption('#paymentMethod', 'vorkasse');
  
  // Accept dialog
  page.on('dialog', d => d.accept());
  
  // Click save
  console.log('\n=== Clicking Save Button ===');
  await page.click('.header-btn-save');
  await page.waitForTimeout(2000);
  
  // Check logs
  const eventLogs = consoleLogs.filter(log => log.includes('Event captured'));
  console.log('\n=== Event Logs ===');
  eventLogs.forEach(log => console.log(log));
  
  expect(eventLogs.length).toBeGreaterThan(0);
});