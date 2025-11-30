/**
 * E2E Critical Path: Validation & Error Handling Flow
 *
 * Sprint 2.1.7.7 - Issue #149
 * Tests "Unhappy Paths" - ensures system correctly rejects invalid operations
 *
 * Testet:
 * 1. Invalid Stage Transitions (z.B. direkt von NEW_LEAD auf CLOSED_WON)
 * 2. Duplicate Customer Detection (gleicher Name/Email)
 * 3. Invalid Hierarchy Operations (Zirkelbezüge)
 * 4. Lead Conversion without Qualification
 *
 * Prinzipien:
 * - Tests die "Nein" Antworten erwarten
 * - Prüft dass Validierungen greifen
 * - Business Rules werden durchgesetzt
 *
 * @module E2E/CriticalPaths/Validation
 * @since Sprint 2.1.7.7
 */

import { test, expect, APIRequestContext } from '@playwright/test';

// API Base URL (vom CI-Workflow gesetzt)
const API_BASE = process.env.VITE_API_URL || 'http://localhost:8080';

// Unique Test-Prefix für Isolation
const TEST_PREFIX = `[E2E-VAL-${Date.now()}-${Math.random().toString(36).substring(7)}]`;

interface LeadResponse {
  id: number;
  companyName: string;
  status: string;
  stage: number;
}

interface OpportunityResponse {
  id: string;
  name: string;
  stage: string;
}

/**
 * Helper: Erstellt einen Lead via API
 */
async function createLead(request: APIRequestContext, name: string): Promise<LeadResponse> {
  const uniqueId = `${Date.now()}-${Math.random().toString(36).substring(7)}`;
  const response = await request.post(`${API_BASE}/api/leads`, {
    data: {
      companyName: `${TEST_PREFIX} ${name}`,
      stage: 1,
      contact: {
        firstName: 'Max',
        lastName: 'Mustermann',
        email: `validation-${uniqueId}@test-e2e.local`,
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
 * Validation & Error Handling Tests
 *
 * Diese Tests prüfen dass das System ungültige Operationen ablehnt
 */
test.describe('Validation & Error Handling - Critical Path', () => {
  test.describe('Stage Transition Validation', () => {
    let lead: LeadResponse;
    let opportunity: OpportunityResponse;

    test.beforeAll(async ({ request }) => {
      console.log(`\n🔒 Setting up Validation test data (Prefix: ${TEST_PREFIX})\n`);

      // Lead erstellen und qualifizieren
      lead = await createLead(request, 'ValidationTest GmbH');
      console.log(`✅ Lead created: ${lead.companyName} (ID: ${lead.id})`);

      // Lead qualifizieren
      const etag = `"lead-${lead.id}-0"`;
      const qualifyResponse = await request.patch(`${API_BASE}/api/leads/${lead.id}`, {
        data: { status: 'QUALIFIED' },
        headers: { 'Content-Type': 'application/json', 'If-Match': etag },
      });

      if (!qualifyResponse.ok()) {
        const body = await qualifyResponse.text();
        throw new Error(`Failed to qualify lead: ${qualifyResponse.status()} - ${body}`);
      }
      lead = await qualifyResponse.json();
      console.log(`✅ Lead qualified`);

      // Opportunity erstellen
      const oppResponse = await request.post(`${API_BASE}/api/opportunities/from-lead/${lead.id}`, {
        data: {
          name: `Opportunity from Lead ${lead.id}`,
          dealType: 'Liefervertrag',
          expectedValue: 75000,
          timeframe: 'Q2 2025',
        },
        headers: { 'Content-Type': 'application/json' },
      });

      if (!oppResponse.ok()) {
        const body = await oppResponse.text();
        throw new Error(`Failed to create opportunity: ${oppResponse.status()} - ${body}`);
      }
      opportunity = await oppResponse.json();
      console.log(`✅ Opportunity created: ${opportunity.name} (ID: ${opportunity.id})`);
    });

    test('should reject direct jump from NEW_LEAD to CLOSED_WON', async ({ request }) => {
      // Versuch direkt von NEW_LEAD auf CLOSED_WON zu springen (sollte fehlschlagen)
      const response = await request.put(
        `${API_BASE}/api/opportunities/${opportunity.id}/stage/CLOSED_WON`
      );

      // Erwartung: Fehler 400 Bad Request
      expect(response.ok()).toBe(false);
      expect(response.status()).toBe(400);

      const body = await response.json();
      expect(body.error || body.message).toBeTruthy();

      console.log(`✅ Correctly rejected invalid stage transition: ${JSON.stringify(body)}`);
    });

    test('should reject converting non-WON opportunity to customer', async ({ request }) => {
      // Opportunity ist noch auf NEW_LEAD - Conversion sollte fehlschlagen
      const response = await request.post(
        `${API_BASE}/api/opportunities/${opportunity.id}/convert-to-customer`,
        {
          data: {
            companyName: `${TEST_PREFIX} ShouldFail GmbH`,
            street: 'Teststraße 1',
            postalCode: '10115',
            city: 'Berlin',
            country: 'Deutschland',
            hierarchyType: 'STANDALONE',
          },
          headers: { 'Content-Type': 'application/json' },
        }
      );

      // Erwartung: Fehler 400 Bad Request
      expect(response.ok()).toBe(false);
      expect(response.status()).toBe(400);

      const body = await response.json();
      expect(body.error).toContain('won');

      console.log(`✅ Correctly rejected conversion of non-WON opportunity`);
    });
  });

  test.describe('Lead Conversion Validation', () => {
    test('should reject creating opportunity from unqualified lead', async ({ request }) => {
      // Neuen Lead erstellen (Status ist REGISTERED, nicht QUALIFIED)
      const lead = await createLead(request, 'UnqualifiedLead GmbH');

      // Versuch Opportunity zu erstellen ohne Qualifizierung
      const response = await request.post(`${API_BASE}/api/opportunities/from-lead/${lead.id}`, {
        data: {
          name: `Opportunity from unqualified Lead ${lead.id}`,
          dealType: 'Liefervertrag',
          expectedValue: 50000,
        },
        headers: { 'Content-Type': 'application/json' },
      });

      // Erwartung: Fehler 400 Bad Request (Lead muss QUALIFIED sein)
      expect(response.ok()).toBe(false);
      expect(response.status()).toBe(400);

      const body = await response.json();
      console.log(
        `✅ Correctly rejected opportunity from unqualified lead: ${JSON.stringify(body)}`
      );
    });
  });

  test.describe('Duplicate Detection', () => {
    test('should detect duplicate customer creation', async ({ request }) => {
      const uniqueName = `${TEST_PREFIX} DuplicateTest GmbH`;

      // Ersten Customer erstellen
      const firstResponse = await request.post(`${API_BASE}/api/customers`, {
        data: {
          companyName: uniqueName,
          customerType: 'UNTERNEHMEN',
          businessType: 'RESTAURANT',
          expectedAnnualVolume: 100000.0,
        },
        headers: { 'Content-Type': 'application/json' },
      });

      expect(firstResponse.ok()).toBe(true);
      console.log(`✅ First customer created: ${uniqueName}`);

      // Versuch denselben Customer nochmal zu erstellen
      const duplicateResponse = await request.post(`${API_BASE}/api/customers`, {
        data: {
          companyName: uniqueName,
          customerType: 'UNTERNEHMEN',
          businessType: 'RESTAURANT',
          expectedAnnualVolume: 200000.0,
        },
        headers: { 'Content-Type': 'application/json' },
      });

      // Erwartung: Fehler 409 Conflict
      expect(duplicateResponse.ok()).toBe(false);
      expect(duplicateResponse.status()).toBe(409);

      const body = await duplicateResponse.json();
      expect(body.error).toBe('CUSTOMER_ALREADY_EXISTS');

      console.log(`✅ Correctly rejected duplicate customer creation`);
    });
  });

  test.describe('Hierarchy Validation', () => {
    test('should not allow FILIALE to have its own branches', async ({ request }) => {
      // Headquarter erstellen
      const hqResponse = await request.post(`${API_BASE}/api/customers`, {
        data: {
          companyName: `${TEST_PREFIX} HQ für Hierarchy Test`,
          customerType: 'UNTERNEHMEN',
          businessType: 'RESTAURANT',
          expectedAnnualVolume: 500000.0,
        },
        headers: { 'Content-Type': 'application/json' },
      });
      const headquarter = await hqResponse.json();

      // Zu HEADQUARTER machen
      await request.put(`${API_BASE}/api/customers/${headquarter.id}`, {
        data: { hierarchyType: 'HEADQUARTER' },
        headers: { 'Content-Type': 'application/json' },
      });

      // Filiale erstellen
      const branchResponse = await request.post(
        `${API_BASE}/api/customers/${headquarter.id}/branches`,
        {
          data: {
            companyName: `${TEST_PREFIX} Filiale Alpha`,
            customerType: 'UNTERNEHMEN',
            businessType: 'RESTAURANT',
            expectedAnnualVolume: 75000.0,
          },
          headers: { 'Content-Type': 'application/json' },
        }
      );
      const filiale = await branchResponse.json();
      console.log(`✅ Created branch: ${filiale.companyName}`);

      // Versuch eine Sub-Filiale unter der Filiale zu erstellen (sollte fehlschlagen)
      const subBranchResponse = await request.post(
        `${API_BASE}/api/customers/${filiale.id}/branches`,
        {
          data: {
            companyName: `${TEST_PREFIX} Sub-Filiale (ungültig)`,
            customerType: 'UNTERNEHMEN',
            businessType: 'RESTAURANT',
            expectedAnnualVolume: 25000.0,
          },
          headers: { 'Content-Type': 'application/json' },
        }
      );

      // Erwartung: Fehler - FILIALE kann keine Sub-Branches haben
      expect(subBranchResponse.ok()).toBe(false);

      console.log(`✅ Correctly rejected sub-branch under FILIALE`);
    });
  });
});
