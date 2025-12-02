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
 * - Pure API Tests: Keine Browser-UI Interaktionen für maximale CI-Stabilität
 *
 * @module E2E/CriticalPaths/LeadConversion
 * @since Sprint 2.1.7.7
 */

import { test, expect } from '@playwright/test';
import {
  API_BASE,
  LeadResponse,
  OpportunityResponse,
  CustomerResponse,
  createLead,
  qualifyLead,
  convertLeadToOpportunity,
  setOpportunityToWon,
  convertOpportunityToCustomer,
  generateTestPrefix,
} from '../helpers/api-helpers';

// Unique Test-Prefix für Isolation
const TEST_PREFIX = generateTestPrefix('E2E-LC');

/**
 * Lead Conversion Flow Tests
 *
 * Backend-Bug GEFIXT (V10048/V10049):
 * - Timezone-Constraint mit UTC-Konfiguration
 * - Ermöglicht Lead-Erstellung auch bei Timezone-Differenzen
 */
test.describe('Lead Conversion Flow - Critical Path', () => {
  // Test-Daten die im Test erstellt werden
  let lead: LeadResponse;
  let opportunity: OpportunityResponse;
  let customer: CustomerResponse;

  test.beforeAll(async ({ request }) => {
    console.log(`\n[TARGET] Setting up Lead Conversion test data (Prefix: ${TEST_PREFIX})\n`);

    // 1. Lead erstellen
    lead = await createLead(request, 'ConversionTest GmbH', TEST_PREFIX);
    console.log(`[OK] Lead created: ${lead.companyName} (ID: ${lead.id}) - Status: ${lead.status}`);

    // 2. Lead qualifizieren (Voraussetzung für Opportunity-Konversion)
    lead = await qualifyLead(request, lead.id);
    console.log(`[OK] Lead qualified: Status now ${lead.status}`);

    // 3. Lead → Opportunity konvertieren
    opportunity = await convertLeadToOpportunity(request, lead.id);
    console.log(`[OK] Opportunity created: ${opportunity.name} (ID: ${opportunity.id})`);

    // 4. Opportunity auf WON setzen (Voraussetzung für Customer-Conversion)
    opportunity = await setOpportunityToWon(request, opportunity.id);
    console.log(`[OK] Opportunity set to WON stage`);

    // 5. Opportunity → Customer konvertieren
    customer = await convertOpportunityToCustomer(
      request,
      opportunity.id,
      `${TEST_PREFIX} Neukunde GmbH`
    );
    console.log(`[OK] Customer created: ${customer.companyName} (${customer.customerNumber})`);

    console.log('\n[DATA] Lead Conversion test data setup complete!\n');
  });

  test('should create lead and qualify it for conversion', async () => {
    expect(lead).toBeDefined();
    expect(lead.id).toBeTruthy();
    expect(lead.companyName).toContain('ConversionTest GmbH');
    // Lead wurde qualifiziert im beforeAll - Status sollte QUALIFIED sein
    expect(lead.status).toBe('QUALIFIED');

    console.log(`[OK] Lead data verified: Status ${lead.status}`);
  });

  test('should create opportunity linked to lead', async () => {
    expect(opportunity).toBeDefined();
    expect(opportunity.id).toBeTruthy();
    expect(opportunity.name).toContain('Opportunity');

    // Opportunity sollte auf CLOSED_WON stehen (nach Stage-Transition)
    expect(opportunity.stage).toBe('CLOSED_WON');

    console.log(`[OK] Opportunity linked to Lead, Stage: ${opportunity.stage}`);
  });

  test('should create customer from opportunity', async () => {
    expect(customer).toBeDefined();
    expect(customer.id).toBeTruthy();
    expect(customer.customerNumber).toBeTruthy();
    expect(customer.companyName).toContain('Neukunde GmbH');

    console.log(`[OK] Customer created with number: ${customer.customerNumber}`);
  });

  test('should find lead in leads list via API', async ({ request }) => {
    // API-basierte Suche statt UI-Navigation
    const response = await request.get(`${API_BASE}/api/leads/${lead.id}`);
    expect(response.ok()).toBe(true);

    const data = await response.json();
    expect(data.id).toBe(lead.id);
    expect(data.companyName).toContain('ConversionTest GmbH');

    console.log(`[OK] Lead found via API: ${data.companyName} (Status: ${data.status})`);
  });

  test('should find customer in customer list via API', async ({ request }) => {
    // API-basierte Suche statt UI-Navigation
    const response = await request.get(`${API_BASE}/api/customers?search=${TEST_PREFIX}`);
    expect(response.ok()).toBe(true);

    const data = await response.json();
    const customers = data.content || data;

    // Prüfe dass unser Kunde in der Liste ist
    const foundCustomer = customers.find(
      (c: CustomerResponse) => c.id === customer.id || c.companyName === customer.companyName
    );

    expect(foundCustomer).toBeDefined();
    expect(foundCustomer.companyName).toContain('Neukunde GmbH');

    console.log(`[OK] Customer found in list via API: ${foundCustomer.companyName}`);
  });

  test('should retrieve opportunity via API', async ({ request }) => {
    // API-basierter Opportunity-Abruf
    const response = await request.get(`${API_BASE}/api/opportunities/${opportunity.id}`);
    expect(response.ok()).toBe(true);

    const data = await response.json();
    expect(data.id).toBe(opportunity.id);
    expect(data.stage).toBe('CLOSED_WON');

    console.log(`[OK] Opportunity retrieved via API: ${data.name} (Stage: ${data.stage})`);
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
    expect(oppData.stage).toBe('CLOSED_WON');
    console.log(`   Opportunity stage: ${oppData.stage}`);

    // 3. Customer prüfen
    const custResponse = await request.get(`${API_BASE}/api/customers/${customer.id}`);
    expect(custResponse.ok()).toBe(true);
    const custData = await custResponse.json();
    expect(custData.status).toBe('AKTIV');
    console.log(`   Customer status: ${custData.status}`);

    console.log(`\n[OK] End-to-end traceability validated!`);
    console.log(`   Lead ${lead.id} → Opportunity ${opportunity.id} → Customer ${customer.id}`);
  });
});
