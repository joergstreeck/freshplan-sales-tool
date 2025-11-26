/**
 * LeadWizard E2E Tests - Complete User Flows
 *
 * Migrated from Integration Tests (9 tests were skipped due to MUI Autocomplete limitations)
 * E2E tests can properly interact with MUI Autocomplete components.
 *
 * Coverage:
 * 1. Stage 0: Required fields + Form submission
 * 2. Erstkontakt Logic: MESSE/TELEFON vs WEB_FORMULAR
 * 3. Stage 1: Contact Person fields + validation
 * 4. Multi-Stage Navigation: Stage 0 → Stage 1 → Submit
 * 5. API Integration: Payload validation
 */
import { test, expect, Page, Route } from '@playwright/test';
import { mockAuth } from '../fixtures/auth-helper';

// ============================================================================
// API Mocking Helper
// ============================================================================

async function mockLeadAPIs(page: Page) {
  // Mock Lead Schema Endpoint (Server-Driven UI)
  await page.route('**/api/leads/schema', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([
        {
          cardId: 'lead_progressive_profiling',
          title: 'Lead erfassen',
          sections: [
            // Stage 0: Pre-Claim
            {
              sectionId: 'stage_0_pre_claim',
              title: 'Basis-Informationen',
              fields: [
                {
                  fieldKey: 'companyName',
                  label: 'Firmenname',
                  type: 'TEXT',
                  required: true,
                },
                {
                  fieldKey: 'source',
                  label: 'Quelle',
                  type: 'ENUM',
                  enumSource: '/api/enums/lead-sources',
                  required: true,
                },
                {
                  fieldKey: 'website',
                  label: 'Website',
                  type: 'TEXT',
                },
                {
                  fieldKey: 'phone',
                  label: 'Telefon',
                  type: 'TEXT',
                },
                {
                  fieldKey: 'email',
                  label: 'E-Mail',
                  type: 'TEXT',
                },
              ],
            },
            // Stage 1: Vollschutz
            {
              sectionId: 'stage_1_vollschutz',
              title: 'Erweiterte Informationen',
              fields: [
                {
                  fieldKey: 'businessType',
                  label: 'Branche',
                  type: 'ENUM',
                  enumSource: '/api/enums/business-types',
                },
                {
                  fieldKey: 'kitchenSize',
                  label: 'Küchengröße',
                  type: 'ENUM',
                  enumSource: '/api/enums/kitchen-sizes',
                },
              ],
            },
          ],
        },
      ]),
    });
  });

  // Mock Enum: Lead Sources
  await page.route('**/api/enums/lead-sources', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([
        { value: 'MESSE', label: 'Messe' },
        { value: 'EMPFEHLUNG', label: 'Empfehlung' },
        { value: 'TELEFON', label: 'Telefon (Kaltakquise)' },
        { value: 'WEB_FORMULAR', label: 'Web-Formular' },
      ]),
    });
  });

  // Mock Enum: Business Types
  await page.route('**/api/enums/business-types', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([
        { value: 'RESTAURANT', label: 'Restaurant' },
        { value: 'HOTEL', label: 'Hotel' },
        { value: 'CATERING', label: 'Catering' },
      ]),
    });
  });

  // Mock Enum: Kitchen Sizes
  await page.route('**/api/enums/kitchen-sizes', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([
        { value: 'KLEIN', label: 'Klein (< 50 m²)' },
        { value: 'MITTEL', label: 'Mittel (50-150 m²)' },
        { value: 'GROSS', label: 'Groß (> 150 m²)' },
      ]),
    });
  });

  // Mock Lead Creation POST
  await page.route('**/api/leads', async (route: Route) => {
    if (route.request().method() === 'POST') {
      const payload = route.request().postDataJSON();
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'lead-' + Date.now(),
          ...payload,
          createdAt: new Date().toISOString(),
        }),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ content: [], totalElements: 0 }),
      });
    }
  });

  // Mock auth endpoints
  await page.route('**/api/auth/**', async (route: Route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ authenticated: true }),
    });
  });
}

// ============================================================================
// Helper: Open Lead Wizard
// ============================================================================

async function openLeadWizard(page: Page) {
  await page.goto('/lead-generation/leads');
  await page.waitForLoadState('networkidle');

  // Click "Lead erfassen" button to open wizard
  await page.click('button:has-text("Lead erfassen")');

  // Wait for wizard dialog to appear
  await page.waitForSelector('text=/Lead erfassen/i', { timeout: 5000 });
}

// ============================================================================
// Tests
// ============================================================================

test.describe('LeadWizard E2E - Complete Flows', () => {
  test.beforeEach(async ({ page }) => {
    await mockAuth(page);
    await mockLeadAPIs(page);
  });

  // ========================================================================
  // 1. Stage 0: Required Fields + Submit Button
  // ========================================================================

  test('should enable submit button when required fields are filled', async ({ page }) => {
    await openLeadWizard(page);

    // Fill Firmenname (required) - TextField has no name attribute, use label
    await page.getByLabel(/firmenname.*\*/i).fill('Test Restaurant GmbH');

    // Fill Quelle (required Autocomplete) - MUI Autocomplete
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Web-Formular")');

    // Submit button should be enabled
    const submitButton = page.locator('button:has-text("Vormerkung speichern")');
    await expect(submitButton).toBeEnabled();
  });

  // ========================================================================
  // 2. Erstkontakt Logic: MESSE source shows Erstkontakt fields
  // ========================================================================

  test('should show Erstkontakt fields for MESSE source', async ({ page }) => {
    await openLeadWizard(page);

    await page.getByLabel(/firmenname.*\*/i).fill('Messe Test GmbH');

    // Select MESSE as source
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Messe")');

    // Erstkontakt block should appear
    await expect(page.locator('text=/erstkontakt dokumentieren.*pflicht/i')).toBeVisible();
  });

  test('should NOT show Erstkontakt fields for WEB_FORMULAR source', async ({ page }) => {
    await openLeadWizard(page);

    await page.getByLabel(/firmenname.*\*/i).fill('Web Test GmbH');

    // Select WEB_FORMULAR as source
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Web-Formular")');

    // Erstkontakt block should NOT appear (MESSE/TELEFON only)
    await expect(page.locator('text=/erstkontakt dokumentieren.*pflicht/i')).not.toBeVisible();
  });

  // ========================================================================
  // 3. Stage 1: Contact Person Fields
  // ========================================================================

  test('should display contact person fields in Stage 1', async ({ page }) => {
    await openLeadWizard(page);

    // Fill Stage 0
    await page.getByLabel(/firmenname.*\*/i).fill('Stage 1 Test GmbH');
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Empfehlung")');

    // Navigate to Stage 1
    await page.click('button:has-text("Weiter")');

    // Wait for Stage 1 contact person fields (no name attributes, use labels)
    await expect(page.getByLabel(/vorname/i)).toBeVisible();
    await expect(page.getByLabel(/nachname/i)).toBeVisible();
    await expect(page.getByLabel(/e-mail/i)).toBeVisible();
  });

  test('should require at least email OR phone for contact person', async ({ page }) => {
    await openLeadWizard(page);

    // Navigate to Stage 1
    await page.getByLabel(/firmenname.*\*/i).fill('Validation Test GmbH');
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Empfehlung")');
    await page.click('button:has-text("Weiter")');

    await expect(page.getByLabel(/vorname/i)).toBeVisible();

    // Fill contact name but NO email/phone
    await page.getByLabel(/vorname/i).fill('Max');
    await page.getByLabel(/nachname/i).fill('Mustermann');

    // Try to navigate to Stage 2 (or submit)
    await page.click('button:has-text("Weiter")');

    // Should show validation error (use .first() to handle multiple matching elements)
    await expect(page.locator('text=/mindestens e-mail oder telefon/i').first()).toBeVisible();
  });

  // ========================================================================
  // 4. Form Submission: API Integration
  // ========================================================================

  test('should submit Stage 0 data successfully', async ({ page }) => {
    await openLeadWizard(page);

    // Fill required fields
    await page.getByLabel(/firmenname.*\*/i).fill('FreshFood GmbH');
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Empfehlung")');

    // Wait for successful API response
    const responsePromise = page.waitForResponse(
      response =>
        response.url().includes('/api/leads') &&
        response.request().method() === 'POST' &&
        response.status() === 201
    );

    // Submit
    await page.click('button:has-text("Vormerkung speichern")');

    // Verify success by waiting for API response
    const response = await responsePromise;
    expect(response.status()).toBe(201);
  });

  test('should include stage number in API payload', async ({ page }) => {
    let capturedPayload: unknown = null;

    // Override handler to capture payload
    await page.route('**/api/leads', async (route: Route) => {
      if (route.request().method() === 'POST') {
        capturedPayload = route.request().postDataJSON();
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({ id: 'test-123' }),
        });
      } else {
        // Let other requests (GET, etc.) pass through to default handler
        await route.fallback();
      }
    });

    await openLeadWizard(page);

    await page.getByLabel(/firmenname.*\*/i).fill('Payload Test GmbH');
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Web-Formular")');

    await page.click('button:has-text("Vormerkung speichern")');

    // Wait for API call
    await page.waitForTimeout(1000);

    // Verify payload structure
    expect(capturedPayload).toBeTruthy();
    expect(capturedPayload.stage).toBe(0);
    expect(capturedPayload.companyName).toBe('Payload Test GmbH');
    expect(capturedPayload.source).toBe('WEB_FORMULAR');
  });

  // ========================================================================
  // 5. Multi-Stage Navigation
  // ========================================================================

  test('should navigate from Stage 0 to Stage 1', async ({ page }) => {
    await openLeadWizard(page);

    // Fill Stage 0
    await page.getByLabel(/firmenname.*\*/i).fill('Navigation Test GmbH');
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Empfehlung")');

    // Navigate to Stage 1
    await page.click('button:has-text("Weiter")');

    // Verify Stage 1 fields appear
    await expect(page.getByLabel(/vorname/i)).toBeVisible();
    await expect(page.locator('text=/erweiterte informationen/i')).toBeVisible();
  });

  test('should show Zurück button in Stage 1', async ({ page }) => {
    await openLeadWizard(page);

    // Navigate to Stage 1
    await page.getByLabel(/firmenname.*\*/i).fill('Back Button Test GmbH');
    const quelleInput = page.getByLabel(/quelle.*\*/i);
    await quelleInput.click();
    await page.click('li[role="option"]:has-text("Empfehlung")');
    await page.click('button:has-text("Weiter")');

    await expect(page.getByLabel(/vorname/i)).toBeVisible();

    // Zurück button should be visible
    await expect(page.locator('button:has-text("Zurück")')).toBeVisible();

    // Click Zurück
    await page.click('button:has-text("Zurück")');

    // Should navigate back to Stage 0
    await expect(page.getByLabel(/firmenname.*\*/i)).toBeVisible();
    await expect(page.locator('text=/basis-informationen/i')).toBeVisible();
  });
});
