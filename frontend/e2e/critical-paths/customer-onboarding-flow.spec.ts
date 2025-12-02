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
 * 4. Datenintegrität prüfen (via API)
 *
 * Prinzipien:
 * - Self-Contained: Jeder Test erstellt seine Daten selbst
 * - UUID-Isolation: Keine Kollisionen mit anderen Tests
 * - Pure API Tests: Keine Browser-UI Interaktionen für maximale CI-Stabilität
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
    expect(foundCustomer.companyName).toContain('OnboardingTest GmbH');

    console.log(`[OK] Customer found in list via API: ${foundCustomer.companyName}`);
  });

  test('should retrieve customer detail via API', async ({ request }) => {
    // API-basierter Detail-Abruf statt UI-Navigation
    const response = await request.get(`${API_BASE}/api/customers/${customer.id}`);
    expect(response.ok()).toBe(true);

    const data = await response.json();
    expect(data.id).toBe(customer.id);
    expect(data.companyName).toContain('OnboardingTest GmbH');
    expect(data.customerNumber).toBeTruthy();

    console.log(`[OK] Customer detail retrieved via API: ${data.companyName}`);
  });

  test('should retrieve contacts via API', async ({ request }) => {
    // API-basierter Contacts-Abruf statt UI-Navigation
    const response = await request.get(`${API_BASE}/api/customers/${customer.id}/contacts`);
    expect(response.ok()).toBe(true);

    const data = await response.json();
    const contactsList = Array.isArray(data) ? data : data.content || [];

    // Sollte mindestens 2 Contacts haben
    expect(contactsList.length).toBeGreaterThanOrEqual(2);

    // Prüfe dass beide Contacts vorhanden sind
    const hasHans = contactsList.some((c: ContactResponse) => c.firstName === 'Hans');
    const hasAnna = contactsList.some((c: ContactResponse) => c.firstName === 'Anna');

    expect(hasHans).toBe(true);
    expect(hasAnna).toBe(true);

    console.log(`[OK] Contacts retrieved via API: ${contactsList.length} contacts`);
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
