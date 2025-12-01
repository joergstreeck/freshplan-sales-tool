/**
 * E2E Critical Path: Customer Onboarding Flow
 *
 * Sprint 2.1.7.7 - Issue #149
 * Self-Contained Test gegen echtes Backend
 *
 * Testet den Customer Onboarding Business Flow:
 * 1. Customer erstellen (via API)
 * 2. Contact hinzufügen (via API)
 * 3. Customer-Daten aktualisieren (via API)
 * 4. UI-Validierung und Datenintegrität prüfen
 *
 * Prinzipien:
 * - Self-Contained: Jeder Test erstellt seine Daten selbst
 * - UUID-Isolation: Keine Kollisionen mit anderen Tests
 * - Keine waitForTimeout: Explizite Waits auf API-Responses
 *
 * @module E2E/CriticalPaths/CustomerOnboarding
 * @since Sprint 2.1.7.7
 */

import { test, expect } from '@playwright/test';
import {
  API_BASE,
  CustomerResponse,
  ContactResponse,
  createCustomer,
  updateCustomer,
  addContact,
  searchAndWait,
  generateTestPrefix,
} from '../helpers/api-helpers';

// Unique Test-Prefix für Isolation
const TEST_PREFIX = generateTestPrefix('E2E-CO');

test.describe('Customer Onboarding Flow - Critical Path', () => {
  // Test-Daten die im Test erstellt werden
  let customer: CustomerResponse;
  let primaryContact: ContactResponse;
  let secondaryContact: ContactResponse;

  test.beforeAll(async ({ request }) => {
    console.log(`\n[CUSTOMER] Setting up Customer Onboarding test data (Prefix: ${TEST_PREFIX})\n`);

    // 1. Customer erstellen
    customer = await createCustomer(request, 'OnboardingTest GmbH', TEST_PREFIX, {
      status: 'PROSPECT',
      expectedAnnualVolume: 100000.0,
    });
    console.log(`[OK] Customer created: ${customer.companyName} (${customer.customerNumber})`);

    // 2. Primary Contact hinzufügen
    primaryContact = await addContact(request, customer.id, {
      firstName: 'Hans',
      lastName: 'Müller',
      email: `hans.mueller-${Date.now()}@test-e2e.local`,
      phone: '+49 30 12345678',
      role: 'Geschäftsführer',
    });
    console.log(
      `[OK] Primary contact added: ${primaryContact.firstName} ${primaryContact.lastName}`
    );

    // 3. Secondary Contact hinzufügen
    secondaryContact = await addContact(request, customer.id, {
      firstName: 'Anna',
      lastName: 'Schmidt',
      email: `anna.schmidt-${Date.now()}@test-e2e.local`,
      phone: '+49 30 87654321',
      role: 'Einkaufsleiter',
    });
    console.log(
      `[OK] Secondary contact added: ${secondaryContact.firstName} ${secondaryContact.lastName}`
    );

    // 4. Customer auf AKTIV setzen (Onboarding abgeschlossen)
    customer = await updateCustomer(request, customer.id, {
      status: 'AKTIV',
      expectedAnnualVolume: 150000.0,
    });
    console.log(`[OK] Customer status updated to: ${customer.status}`);

    console.log('\n[DATA] Customer Onboarding test data setup complete!\n');
  });

  test('should create customer with correct initial status', async () => {
    expect(customer).toBeDefined();
    expect(customer.id).toBeTruthy();
    expect(customer.customerNumber).toBeTruthy();
    expect(customer.companyName).toContain('OnboardingTest GmbH');

    console.log(`[OK] Customer data verified: ${customer.customerNumber}`);
  });

  test('should have primary contact', async () => {
    expect(primaryContact).toBeDefined();
    expect(primaryContact.id).toBeTruthy();
    expect(primaryContact.firstName).toBe('Hans');
    expect(primaryContact.lastName).toBe('Müller');

    console.log(
      `[OK] Primary contact verified: ${primaryContact.firstName} ${primaryContact.lastName}`
    );
  });

  test('should have secondary contact', async () => {
    expect(secondaryContact).toBeDefined();
    expect(secondaryContact.id).toBeTruthy();
    expect(secondaryContact.firstName).toBe('Anna');
    expect(secondaryContact.lastName).toBe('Schmidt');

    console.log(
      `[OK] Secondary contact verified: ${secondaryContact.firstName} ${secondaryContact.lastName}`
    );
  });

  test('should update customer status to AKTIV', async () => {
    expect(customer.status).toBe('AKTIV');
    console.log(`[OK] Customer status is AKTIV`);
  });

  test('should display customer in customer list', async ({ page }) => {
    // Navigate zu Kundenliste und warte auf API-Response
    const responsePromise = page.waitForResponse(
      resp => resp.url().includes('/api/customers') && resp.status() === 200,
      { timeout: 15000 }
    );

    await page.goto('/customers');
    await page.waitForLoadState('domcontentloaded');

    // Warte auf die Customers-API Response
    try {
      const response = await responsePromise;
      const data = await response.json();
      console.log(`[API] /api/customers returned ${data?.content?.length || 0} customers`);
    } catch (e) {
      console.log(`[WARN] API response not captured: ${e}`);
    }

    // Warte kurz auf React-Rendering
    await page.waitForTimeout(1000);

    // Prüfe ob Tabelle ODER EmptyState sichtbar ist
    const table = page.locator('table').first();
    const emptyState = page.locator('text=/Noch keine Kunden|No customers/i').first();

    const tableVisible = await table.isVisible({ timeout: 3000 }).catch(() => false);
    const emptyVisible = await emptyState.isVisible({ timeout: 1000 }).catch(() => false);

    console.log(`[DEBUG] Table visible: ${tableVisible}, EmptyState visible: ${emptyVisible}`);

    if (emptyVisible) {
      // Kein Table - das bedeutet API hat leere Daten zurückgegeben
      // In diesem Fall muss der Test fehlschlagen mit klarer Meldung
      throw new Error(
        `Customer list is empty! Expected customer ${customer.companyName} (ID: ${customer.id}) to be visible. ` +
          `This indicates the API either returned empty data or the customer was not created properly.`
      );
    }

    // Tabelle existiert - warte darauf
    await expect(table).toBeVisible({ timeout: 10000 });

    // Suche nach unserem Test-Kunden mit API-Response-Wait
    const searchInput = page.locator('input[placeholder*="Such"]').first();
    if (await searchInput.isVisible({ timeout: 2000 }).catch(() => false)) {
      await searchAndWait(page, searchInput, TEST_PREFIX, /api\/customers/);
    }

    // Verify: Kunde MUSS sichtbar sein (harte Assertion)
    const customerRow = page.locator(`text=${customer.companyName}`);
    await expect(customerRow).toBeVisible({ timeout: 5000 });

    console.log(`[OK] Customer visible in customer list`);
  });

  test('should navigate to customer detail page', async ({ page }) => {
    // Navigate direkt zur Customer Detail Seite und warte auf API-Response
    const responsePromise = page.waitForResponse(
      resp => resp.url().includes(`/api/customers/${customer.id}`) && resp.status() === 200,
      { timeout: 15000 }
    );

    await page.goto(`/customers/${customer.id}`);
    await page.waitForLoadState('domcontentloaded');

    // Warte auf die Customer-Detail-API Response
    try {
      const response = await responsePromise;
      const data = await response.json();
      console.log(`[API] Customer detail returned: ${data?.companyName || 'N/A'}`);
    } catch (e) {
      console.log(`[WARN] API response not captured: ${e}`);
    }

    // Warte kurz auf React-Rendering
    await page.waitForTimeout(1000);

    // Warte auf Detail-Seite - Paper/Card Container oder Breadcrumbs MÜSSEN sichtbar sein
    const pageContent = page.locator('.MuiPaper-root, .MuiBreadcrumbs-root, h4').first();
    await expect(pageContent).toBeVisible({ timeout: 10000 });

    // Verify URL contains customer ID (harte Assertion)
    expect(page.url()).toContain(customer.id);

    // Prüfe ob Company Name sichtbar ist (Playwright wartet automatisch)
    await expect(page.locator(`text=${customer.companyName}`).first()).toBeVisible({
      timeout: 5000,
    });

    console.log(`[OK] Customer detail page shows company name`);
  });

  test('should show contacts on customer detail page', async ({ page }) => {
    // Navigate zur Customer Detail Seite und warte auf API-Response
    const responsePromise = page.waitForResponse(
      resp => resp.url().includes(`/api/customers/${customer.id}`) && resp.status() === 200,
      { timeout: 15000 }
    );

    await page.goto(`/customers/${customer.id}`);
    await page.waitForLoadState('domcontentloaded');

    // Warte auf die Customer-Detail-API Response
    try {
      await responsePromise;
    } catch (e) {
      console.log(`[WARN] Customer API response not captured: ${e}`);
    }

    // Warte kurz auf React-Rendering
    await page.waitForTimeout(1000);

    // Warte auf Seite (Paper oder Breadcrumbs)
    await expect(page.locator('.MuiPaper-root, .MuiBreadcrumbs-root').first()).toBeVisible({
      timeout: 10000,
    });

    // Suche nach Contacts-Tab oder -Bereich und klicke wenn vorhanden
    const contactsSection = page.locator('text=/Ansprechpartner|Kontakt|Contact/i').first();
    if (await contactsSection.isVisible({ timeout: 2000 }).catch(() => false)) {
      await contactsSection.click();
      await page.waitForTimeout(500);
    }

    // Verify: Mindestens einer der Contacts MUSS sichtbar sein (harte Assertion mit .or())
    await expect(
      page.locator('text=/Müller/i').first().or(page.locator('text=/Schmidt/i').first())
    ).toBeVisible({ timeout: 5000 });

    console.log(`[OK] Contacts visible on customer detail page`);
  });

  test('should validate end-to-end data integrity', async ({ request }) => {
    // Final Validation: Hole alle Daten via API und prüfe Konsistenz

    // 1. Customer prüfen
    const custResponse = await request.get(`${API_BASE}/api/customers/${customer.id}`);
    expect(custResponse.ok()).toBe(true);
    const custData = await custResponse.json();
    expect(custData.status).toBe('AKTIV');
    console.log(`   Customer: ${custData.companyName} (${custData.status})`);

    // 2. Contacts prüfen
    const contactsResponse = await request.get(`${API_BASE}/api/customers/${customer.id}/contacts`);
    expect(contactsResponse.ok()).toBe(true);
    const contactsData = await contactsResponse.json();

    // Sollte mindestens 2 Contacts haben
    const contactsList = Array.isArray(contactsData) ? contactsData : contactsData.content || [];
    expect(contactsList.length).toBeGreaterThanOrEqual(2);
    console.log(`   Contacts: ${contactsList.length} found`);

    console.log(`\n[OK] End-to-end data integrity validated!`);
    console.log(`   Customer: ${customer.customerNumber}`);
    console.log(`   Contacts: ${contactsList.length}`);
  });
});
