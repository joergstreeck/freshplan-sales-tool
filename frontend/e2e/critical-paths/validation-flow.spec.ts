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

import { test, expect } from '@playwright/test';
import {
  API_BASE,
  LeadResponse,
  OpportunityResponse,
  createLead,
  qualifyLead,
  convertLeadToOpportunity,
  createCustomer,
  updateCustomer,
  createBranch,
  generateTestPrefix,
} from '../helpers/api-helpers';

// Unique Test-Prefix für Isolation
const TEST_PREFIX = generateTestPrefix('E2E-VAL');

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

      // Lead erstellen
      lead = await createLead(request, 'ValidationTest GmbH', TEST_PREFIX);
      console.log(`✅ Lead created: ${lead.companyName} (ID: ${lead.id})`);

      // Lead qualifizieren
      lead = await qualifyLead(request, lead.id);
      console.log(`✅ Lead qualified`);

      // Opportunity erstellen
      opportunity = await convertLeadToOpportunity(request, lead.id);
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
      const lead = await createLead(request, 'UnqualifiedLead GmbH', TEST_PREFIX);

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
      const firstCustomer = await createCustomer(request, 'DuplicateTest GmbH', TEST_PREFIX);
      console.log(`✅ First customer created: ${firstCustomer.companyName}`);

      // Versuch denselben Customer nochmal zu erstellen (gleicher Name)
      const duplicateResponse = await request.post(`${API_BASE}/api/customers`, {
        data: {
          companyName: firstCustomer.companyName, // Exakt gleicher Name
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
      const headquarter = await createCustomer(request, 'HQ für Hierarchy Test', TEST_PREFIX);

      // Zu HEADQUARTER machen
      await updateCustomer(request, headquarter.id, { hierarchyType: 'HEADQUARTER' });

      // Filiale erstellen
      const filiale = await createBranch(
        request,
        headquarter.id,
        'Filiale Alpha',
        'Berlin',
        TEST_PREFIX
      );
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
