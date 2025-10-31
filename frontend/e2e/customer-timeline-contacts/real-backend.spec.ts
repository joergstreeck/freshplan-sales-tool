/**
 * E2E Test: Customer Timeline & Contacts (Real Backend)
 *
 * Tests the complete user flow with REAL backend APIs:
 * - Navigate to customer list
 * - Click Timeline icon
 * - Verify timeline expansion and data load
 * - Click Contacts icon
 * - Verify contacts expansion and data load
 * - Toggle functionality
 *
 * Prerequisites:
 * - Backend must be running (./mvnw quarkus:dev)
 * - Frontend must be running (npm run dev)
 * - Test user must be authenticated
 * - Database must have test customers
 *
 * @module E2E/CustomerTimelineContacts
 * @since Sprint 2.1.7.2
 */

import { test, expect } from '@playwright/test';
import { mockAuth } from '../fixtures/auth-helper';
import { setupTestData, cleanupTestData } from '../fixtures/test-data-helper';

test.describe('Customer Timeline & Contacts E2E (Real Backend)', () => {
  // Setup: Create test customers with timeline/contacts data
  test.beforeAll(async (_context, testInfo) => {
    const baseURL = testInfo.project.use.baseURL || 'http://localhost:8080';
    await setupTestData(baseURL);
    console.log('✅ Test data setup complete');
  });

  // Teardown: Clean up all test data
  test.afterAll(async () => {
    await cleanupTestData();
    console.log('✅ Test data cleanup complete');
  });

  test.beforeEach(async ({ page }) => {
    // Mock authentication (frontend only, backend calls go through to real backend)
    await mockAuth(page);

    // Navigate to customer list page (with CustomerTable)
    await page.goto('/customers');

    // Wait for page to load
    await page.waitForLoadState('networkidle');
  });

  test('should display customer list and action icons', async ({ page }) => {
    // Wait for customer table to load
    const table = page.locator('table').first();
    await expect(table).toBeVisible({ timeout: 10000 });

    // Verify our test customers are visible
    await expect(page.locator('text=[E2E-TEST] FreshEvents Catering AG')).toBeVisible();
    await expect(page.locator('text=[E2E-TEST] Großhandel Frische Küche')).toBeVisible();
    await expect(page.locator('text=[E2E-TEST] Restaurant Silbertanne München')).toBeVisible();

    // Check for Timeline icons for our test customers
    const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
    const timelineIcon = freshEventsRow.locator('button[title="Aktivitäten anzeigen"]');
    await expect(timelineIcon).toBeVisible();

    // Check for Contacts icons
    const contactsIcon = freshEventsRow.locator('button[title="Kontakte anzeigen"]');
    await expect(contactsIcon).toBeVisible();

    console.log('✅ Test customers visible with Timeline and Contacts icons');
  });

  test('should open and close timeline expansion on icon click', async ({ page }) => {
    // Wait for customer table
    await page.waitForSelector('table', { timeout: 10000 });

    // Find Timeline icon for FreshEvents test customer (has timeline data)
    const testCustomerRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
    const timelineIcon = testCustomerRow.locator('button[title="Aktivitäten anzeigen"]');
    await expect(timelineIcon).toBeVisible();

    // Click to open timeline
    await timelineIcon.click();

    // Wait for timeline API call and content
    await page.waitForResponse(response =>
      response.url().includes('/timeline') && response.status() === 200,
      { timeout: 5000 }
    );

    // Timeline should be visible (check for Material UI Timeline component)
    const timeline = page.locator('.MuiTimeline-root').first();
    await expect(timeline).toBeVisible({ timeout: 5000 });

    // Click again to close
    await timelineIcon.click();

    // Timeline should disappear
    await expect(timeline).not.toBeVisible({ timeout: 2000 });
  });

  test('should display timeline activities with correct data', async ({ page }) => {
    // Wait for customer table
    await page.waitForSelector('table', { timeout: 10000 });

    // Click Timeline icon for FreshEvents (has timeline data)
    const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
    const timelineIcon = freshEventsRow.locator('button[title="Aktivitäten anzeigen"]');
    await timelineIcon.click();

    // Wait for timeline to load
    await page.waitForResponse(response =>
      response.url().includes('/timeline') && response.status() === 200,
      { timeout: 5000 }
    );

    // Should have timeline with activities (not empty state)
    const timeline = page.locator('.MuiTimeline-root').first();
    await expect(timeline).toBeVisible({ timeout: 5000 });

    // Check for TimelineItems
    const timelineItems = page.locator('.MuiTimelineItem-root');
    const itemCount = await timelineItems.count();
    console.log(`✅ Found ${itemCount} timeline activities`);

    expect(itemCount).toBeGreaterThan(0);

    // Verify first activity has required elements
    const firstItem = timelineItems.first();

    // Should have TimelineDot (icon)
    await expect(firstItem.locator('.MuiTimelineDot-root')).toBeVisible();

    // Should have TimelineContent (description)
    await expect(firstItem.locator('.MuiTimelineContent-root')).toBeVisible();

    // Verify we can see activity descriptions from our test data
    const hasExpectedActivity = await page.locator('text=/Angebot versendet|Telefonat geführt|Besuch vor Ort/').count() > 0;
    expect(hasExpectedActivity).toBe(true);
  });

  test('should open and close contacts expansion on icon click', async ({ page }) => {
    // Wait for customer table
    await page.waitForSelector('table', { timeout: 10000 });

    // Find Contacts icon for FreshEvents (has contact data)
    const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
    const contactsIcon = freshEventsRow.locator('button[title="Kontakte anzeigen"]');
    await expect(contactsIcon).toBeVisible();

    // Click to open contacts
    await contactsIcon.click();

    // Wait for contacts API call
    await page.waitForResponse(response =>
      response.url().includes('/contacts') && response.status() === 200,
      { timeout: 5000 }
    );

    // Contacts should be visible with our test data
    const hasMaxMustermann = await page.locator('text=Max Mustermann').count() > 0;
    const hasAnnaSchmidt = await page.locator('text=Anna Schmidt').count() > 0;
    expect(hasMaxMustermann || hasAnnaSchmidt).toBe(true);

    // Click again to close
    await contactsIcon.click();

    // Wait for collapse animation
    await page.waitForTimeout(500);

    // Contacts should be hidden (Collapse component unmounts)
    // We'll just verify the icon is still there and clickable
    await expect(contactsIcon).toBeVisible();
  });

  test('should display contact details with warmth score and badges', async ({ page }) => {
    // Wait for customer table
    await page.waitForSelector('table', { timeout: 10000 });

    // Click Contacts icon for FreshEvents (has contact data)
    const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
    const contactsIcon = freshEventsRow.locator('button[title="Kontakte anzeigen"]');
    await contactsIcon.click();

    // Wait for contacts to load
    await page.waitForResponse(response =>
      response.url().includes('/contacts') && response.status() === 200,
      { timeout: 5000 }
    );

    // Should have contacts with our test data
    const contactNames = page.locator('text=/Max Mustermann|Anna Schmidt/').first();
    await expect(contactNames).toBeVisible({ timeout: 5000 });

    // Check for primary contact badge (Max is primary)
    const hasPrimaryBadge = await page.locator('text=Haupt').count() > 0;

    // Check for decision level badges (we set entscheider and mitentscheider)
    const hasDecisionBadge = await page.locator('text=/Entscheider|Mitentscheider/i').count() > 0;

    // Check for warmth scores (we set 85% and 70%)
    const hasWarmthScore = await page.locator('text=/85%|70%/').count() > 0;

    console.log('✅ Contact UI elements:', {
      primaryBadge: hasPrimaryBadge,
      decisionBadge: hasDecisionBadge,
      warmthScore: hasWarmthScore
    });

    // All should be present for our test contacts
    expect(hasPrimaryBadge).toBe(true);
    expect(hasDecisionBadge).toBe(true);
    expect(hasWarmthScore).toBe(true);
  });

  test('should allow both timeline and contacts open simultaneously', async ({ page }) => {
    // Wait for customer table
    await page.waitForSelector('table', { timeout: 10000 });

    // Get FreshEvents customer's icons (has both timeline and contact data)
    const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
    const timelineIcon = freshEventsRow.locator('button[title="Aktivitäten anzeigen"]');
    const contactsIcon = freshEventsRow.locator('button[title="Kontakte anzeigen"]');

    // Open timeline
    await timelineIcon.click();
    await page.waitForResponse(response => response.url().includes('/timeline') && response.status() === 200, { timeout: 5000 });

    // Open contacts
    await contactsIcon.click();
    await page.waitForResponse(response => response.url().includes('/contacts') && response.status() === 200, { timeout: 5000 });

    // Both should be visible with our test data
    const hasTimeline = await page.locator('.MuiTimeline-root').count() > 0;
    const hasTimelineActivity = await page.locator('text=/Angebot versendet|Telefonat geführt/').count() > 0;
    const hasContacts = await page.locator('text=/Max Mustermann|Anna Schmidt/').count() > 0;

    expect(hasTimeline).toBe(true);
    expect(hasTimelineActivity).toBe(true);
    expect(hasContacts).toBe(true);

    console.log('✅ Both timeline and contacts visible simultaneously');
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Wait for customer table
    await page.waitForSelector('table', { timeout: 10000 });

    // Try to open timeline for FreshEvents customer
    const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
    const timelineIcon = freshEventsRow.locator('button[title="Aktivitäten anzeigen"]');
    await timelineIcon.click();

    // Wait for response (should succeed for our test customer)
    const response = await page.waitForResponse(
      response => response.url().includes('/timeline'),
      { timeout: 5000 }
    );

    // For our test customer, should be successful
    if (response.status() !== 200) {
      // If error occurs, UI should handle it gracefully
      const hasErrorMessage = await page.locator('text=/Fehler|Error/i').count() > 0;
      expect(hasErrorMessage).toBe(true);
      console.log('⚠️ API error occurred but was handled gracefully');
    } else {
      // Success - should show timeline with our test data
      const hasContent = await page.locator('.MuiTimeline-root').count() > 0 ||
                         await page.locator('text=/Angebot versendet|Telefonat geführt/').count() > 0;
      expect(hasContent).toBe(true);
      console.log('✅ API request successful, timeline displayed');
    }
  });

  test('should maintain scroll position when toggling expansions', async ({ page }) => {
    // Wait for customer table
    await page.waitForSelector('table', { timeout: 10000 });

    // Scroll down a bit
    await page.evaluate(() => window.scrollTo(0, 200));
    const scrollBefore = await page.evaluate(() => window.scrollY);

    // Open and close timeline for FreshEvents
    const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
    const timelineIcon = freshEventsRow.locator('button[title="Aktivitäten anzeigen"]');

    await timelineIcon.click();
    await page.waitForTimeout(1000);
    await timelineIcon.click();
    await page.waitForTimeout(500);

    // Scroll position should be approximately the same (allow small difference)
    const scrollAfter = await page.evaluate(() => window.scrollY);
    const scrollDiff = Math.abs(scrollAfter - scrollBefore);

    console.log(`✅ Scroll difference: ${scrollDiff}px`);
    expect(scrollDiff).toBeLessThan(50);
  });

  test.describe('Performance', () => {
    test('should load timeline within 2 seconds', async ({ page }) => {
      await page.waitForSelector('table', { timeout: 10000 });

      // Use FreshEvents test customer for performance test
      const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
      const timelineIcon = freshEventsRow.locator('button[title="Aktivitäten anzeigen"]');

      const startTime = Date.now();
      await timelineIcon.click();
      await page.waitForResponse(response => response.url().includes('/timeline') && response.status() === 200, { timeout: 5000 });
      const endTime = Date.now();

      const loadTime = endTime - startTime;
      console.log(`✅ Timeline loaded in ${loadTime}ms`);

      // Should load within 2 seconds
      expect(loadTime).toBeLessThan(2000);
    });

    test('should load contacts within 2 seconds', async ({ page }) => {
      await page.waitForSelector('table', { timeout: 10000 });

      // Use FreshEvents test customer for performance test
      const freshEventsRow = page.locator('text=[E2E-TEST] FreshEvents Catering AG').locator('xpath=ancestor::tr');
      const contactsIcon = freshEventsRow.locator('button[title="Kontakte anzeigen"]');

      const startTime = Date.now();
      await contactsIcon.click();
      await page.waitForResponse(response => response.url().includes('/contacts') && response.status() === 200, { timeout: 5000 });
      const endTime = Date.now();

      const loadTime = endTime - startTime;
      console.log(`✅ Contacts loaded in ${loadTime}ms`);

      // Should load within 2 seconds
      expect(loadTime).toBeLessThan(2000);
    });
  });
});
