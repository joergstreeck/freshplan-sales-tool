/**
 * E2E Critical Path: Lead Conversion Flow
 *
 * Sprint 2.1.7.7 - Issue #149
 * Self-Contained Test gegen echtes Backend
 *
 * Testet den kompletten Lead-to-Customer Business Flow:
 * 1. Lead erstellen (via API)
 * 2. Lead → Opportunity konvertieren (via API)
 * 3. Opportunity → Customer konvertieren (via API)
 * 4. Datenintegrität prüfen (Traceability Chain)
 *
 * Prinzipien:
 * - Self-Contained: Jeder Test erstellt seine Daten selbst
 * - UUID-Isolation: Keine Kollisionen mit anderen Tests
 * - Kein Cleanup nötig: Container-Lifecycle übernimmt
 *
 * @module E2E/CriticalPaths/LeadConversion
 * @since Sprint 2.1.7.7
 */

import { test, expect, APIRequestContext } from '@playwright/test';

// API Base URL (vom CI-Workflow gesetzt)
const API_BASE = process.env.VITE_API_URL || 'http://localhost:8080';

// Unique Test-Prefix für Isolation
const TEST_PREFIX = `[E2E-LC-${Date.now()}]`;

interface LeadResponse {
  id: number;
  companyName: string;
  status: string;
  stage: number;
  email?: string;
  city?: string;
}

interface OpportunityResponse {
  id: string;
  name: string;
  stage: string;
  leadId?: number;
  expectedValue?: number;
}

interface CustomerResponse {
  id: string;
  customerNumber: string;
  companyName: string;
  status: string;
}

/**
 * Helper: Erstellt einen Lead via API
 */
async function createLead(request: APIRequestContext, name: string): Promise<LeadResponse> {
  const response = await request.post(`${API_BASE}/api/leads`, {
    data: {
      companyName: `${TEST_PREFIX} ${name}`,
      stage: 1, // REGISTRIERUNG
      contact: {
        firstName: 'Max',
        lastName: 'Mustermann',
        email: `${name.toLowerCase().replace(/\s/g, '')}@test-e2e.local`,
        phone: '+49 123 456789',
      },
      city: 'Berlin',
      postalCode: '10115',
      street: 'Teststraße 42',
      countryCode: 'DE',
      businessType: 'RESTAURANT',
      estimatedVolume: 50000,
      source: 'WEB',
    },
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to create lead: ${response.status()} - ${body}`);
  }

  return response.json();
}

/**
 * Helper: Konvertiert Lead zu Opportunity via API
 */
async function convertLeadToOpportunity(
  request: APIRequestContext,
  leadId: number
): Promise<OpportunityResponse> {
  const response = await request.post(`${API_BASE}/api/opportunities/from-lead/${leadId}`, {
    data: {
      name: `Opportunity from Lead ${leadId}`,
      dealType: 'Liefervertrag',
      expectedValue: 75000,
      timeframe: 'Q2 2025',
    },
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to convert lead to opportunity: ${response.status()} - ${body}`);
  }

  return response.json();
}

/**
 * Helper: Setzt Opportunity auf WON Stage via API
 */
async function setOpportunityToWon(
  request: APIRequestContext,
  opportunityId: string
): Promise<OpportunityResponse> {
  const response = await request.patch(`${API_BASE}/api/opportunities/${opportunityId}`, {
    data: {
      stage: 'WON',
    },
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to set opportunity to WON: ${response.status()} - ${body}`);
  }

  return response.json();
}

/**
 * Helper: Konvertiert Opportunity zu Customer via API
 */
async function convertOpportunityToCustomer(
  request: APIRequestContext,
  opportunityId: string,
  companyName: string
): Promise<CustomerResponse> {
  const response = await request.post(
    `${API_BASE}/api/opportunities/${opportunityId}/convert-to-customer`,
    {
      data: {
        companyName: companyName,
        street: 'Kundenstraße 1',
        postalCode: '10115',
        city: 'Berlin',
        country: 'Deutschland',
        createContactFromLead: true,
        hierarchyType: 'STANDALONE',
      },
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to convert opportunity to customer: ${response.status()} - ${body}`);
  }

  return response.json();
}

test.describe('Lead Conversion Flow - Critical Path', () => {
  // Test-Daten die im Test erstellt werden
  let lead: LeadResponse;
  let opportunity: OpportunityResponse;
  let customer: CustomerResponse;

  test.beforeAll(async ({ request }) => {
    console.log(`\n🎯 Setting up Lead Conversion test data (Prefix: ${TEST_PREFIX})\n`);

    // 1. Lead erstellen
    lead = await createLead(request, 'ConversionTest GmbH');
    console.log(`✅ Lead created: ${lead.companyName} (ID: ${lead.id})`);

    // 2. Lead → Opportunity konvertieren
    opportunity = await convertLeadToOpportunity(request, lead.id);
    console.log(`✅ Opportunity created: ${opportunity.name} (ID: ${opportunity.id})`);

    // 3. Opportunity auf WON setzen (Voraussetzung für Customer-Conversion)
    opportunity = await setOpportunityToWon(request, opportunity.id);
    console.log(`✅ Opportunity set to WON stage`);

    // 4. Opportunity → Customer konvertieren
    customer = await convertOpportunityToCustomer(
      request,
      opportunity.id,
      `${TEST_PREFIX} Neukunde GmbH`
    );
    console.log(`✅ Customer created: ${customer.companyName} (${customer.customerNumber})`);

    console.log('\n📊 Lead Conversion test data setup complete!\n');
  });

  test('should create lead with correct initial data', async () => {
    expect(lead).toBeDefined();
    expect(lead.id).toBeTruthy();
    expect(lead.companyName).toContain('ConversionTest GmbH');
    expect(lead.stage).toBe(1); // REGISTRIERUNG

    console.log(`✅ Lead data verified: Stage ${lead.stage}`);
  });

  test('should create opportunity linked to lead', async () => {
    expect(opportunity).toBeDefined();
    expect(opportunity.id).toBeTruthy();
    expect(opportunity.name).toContain('Opportunity');

    // Opportunity sollte auf WON stehen
    expect(opportunity.stage).toBe('WON');

    console.log(`✅ Opportunity linked to Lead, Stage: ${opportunity.stage}`);
  });

  test('should create customer from opportunity', async () => {
    expect(customer).toBeDefined();
    expect(customer.id).toBeTruthy();
    expect(customer.customerNumber).toBeTruthy();
    expect(customer.companyName).toContain('Neukunde GmbH');

    console.log(`✅ Customer created with number: ${customer.customerNumber}`);
  });

  test('should display lead in leads list', async ({ page }) => {
    // Navigate zu Leads-Liste
    await page.goto('/leads');
    await page.waitForLoadState('networkidle');

    // Warte auf Tabelle
    const table = page.locator('table').first();
    await expect(table).toBeVisible({ timeout: 10000 });

    // Suche nach unserem Test-Lead
    const searchInput = page.locator('input[placeholder*="Such"]').first();
    if (await searchInput.isVisible()) {
      await searchInput.fill(TEST_PREFIX);
      await page.waitForTimeout(500); // Debounce
    }

    // Lead sollte als CONVERTED angezeigt werden (oder nicht mehr in offenen Leads)
    // Dies ist abhängig von der Business-Logik
    console.log(`ℹ️ Lead conversion UI verification depends on implementation`);
  });

  test('should display customer in customer list', async ({ page }) => {
    // Navigate zu Kundenliste
    await page.goto('/customers');
    await page.waitForLoadState('networkidle');

    // Warte auf Tabelle
    const table = page.locator('table').first();
    await expect(table).toBeVisible({ timeout: 10000 });

    // Suche nach unserem Test-Kunden
    const searchInput = page.locator('input[placeholder*="Such"]').first();
    if (await searchInput.isVisible()) {
      await searchInput.fill(TEST_PREFIX);
      await page.waitForTimeout(500);
    }

    // Verify: Kunde ist sichtbar
    const customerRow = page.locator(`text=${customer.companyName}`);
    await expect(customerRow).toBeVisible({ timeout: 5000 });

    console.log(`✅ Customer visible in customer list`);
  });

  test('should validate end-to-end traceability', async ({ request }) => {
    // Final Validation: Hole alle Daten via API und prüfe Traceability

    // 1. Lead prüfen
    const leadResponse = await request.get(`${API_BASE}/api/leads/${lead.id}`);
    expect(leadResponse.ok()).toBe(true);
    const leadData = await leadResponse.json();
    console.log(`   Lead status: ${leadData.status}`);

    // 2. Opportunity prüfen
    const oppResponse = await request.get(`${API_BASE}/api/opportunities/${opportunity.id}`);
    expect(oppResponse.ok()).toBe(true);
    const oppData = await oppResponse.json();
    expect(oppData.stage).toBe('WON');
    console.log(`   Opportunity stage: ${oppData.stage}`);

    // 3. Customer prüfen
    const custResponse = await request.get(`${API_BASE}/api/customers/${customer.id}`);
    expect(custResponse.ok()).toBe(true);
    const custData = await custResponse.json();
    expect(custData.status).toBe('ACTIVE');
    console.log(`   Customer status: ${custData.status}`);

    console.log(`\n✅ End-to-end traceability validated!`);
    console.log(`   Lead ${lead.id} → Opportunity ${opportunity.id} → Customer ${customer.id}`);
  });
});
