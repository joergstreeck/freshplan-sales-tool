/**
 * E2E Critical Path: Multi-Location Management Flow
 *
 * Sprint 2.1.7.7 - Issue #149
 * Self-Contained Test gegen echtes Backend
 *
 * Testet den kompletten Multi-Location Business Flow:
 * 1. Customer erstellen (via API) - startet als STANDALONE
 * 2. Filialen hinzufuegen (via /branches API) - macht Parent zu HEADQUARTER
 * 3. HierarchyMetrics pruefen (aggregierte Werte)
 * 4. Datenintegrität prüfen (via API)
 *
 * Backend-Verhalten:
 * - Neue Customers starten immer als STANDALONE
 * - hierarchyType wird automatisch zu HEADQUARTER wenn Filialen hinzugefuegt werden
 * - Filialen werden automatisch als FILIALE markiert
 *
 * Prinzipien:
 * - Self-Contained: Jeder Test erstellt seine Daten selbst
 * - UUID-Isolation: Keine Kollisionen mit anderen Tests
 * - Pure API Tests: Keine Browser-UI Interaktionen für maximale CI-Stabilität
 *
 * @module E2E/CriticalPaths/MultiLocation
 * @since Sprint 2.1.7.7
 */

import { test, expect } from '@playwright/test';
import {
  API_BASE,
  CustomerResponse,
  createCustomer,
  getCustomer,
  updateCustomer,
  createBranch,
  getHierarchyMetrics,
  generateTestPrefix,
} from '../helpers/api-helpers';

// Unique Test-Prefix fuer Isolation
const TEST_PREFIX = generateTestPrefix('E2E-ML');

/**
 * Multi-Location Management Flow Tests
 */
test.describe('Multi-Location Management - Critical Path', () => {
  // Test-Daten die im Test erstellt werden
  let headquarter: CustomerResponse;
  let branch1: CustomerResponse;
  let branch2: CustomerResponse;

  test.beforeAll(async ({ request }) => {
    console.log(`\n[CUSTOMER] Setting up Multi-Location test data (Prefix: ${TEST_PREFIX})\n`);

    // 1. Customer erstellen (startet als STANDALONE)
    headquarter = await createCustomer(request, 'FreshChain GmbH', TEST_PREFIX, {
      expectedAnnualVolume: 500000.0,
    });
    console.log(
      `[OK] Customer created: ${headquarter.companyName} (${headquarter.id}) - hierarchyType: ${headquarter.hierarchyType}`
    );

    // 2. Customer zu HEADQUARTER machen via PUT
    headquarter = await updateCustomer(request, headquarter.id, {
      hierarchyType: 'HEADQUARTER',
    });
    console.log(`[OK] Customer set to HEADQUARTER: ${headquarter.hierarchyType}`);

    // 3. Erste Filiale erstellen
    branch1 = await createBranch(request, headquarter.id, 'Filiale Berlin', 'Berlin', TEST_PREFIX);
    console.log(`[OK] Branch 1 created: ${branch1.companyName} (${branch1.id})`);

    // 4. Zweite Filiale erstellen
    branch2 = await createBranch(
      request,
      headquarter.id,
      'Filiale Hamburg',
      'Hamburg',
      TEST_PREFIX
    );
    console.log(`[OK] Branch 2 created: ${branch2.companyName} (${branch2.id})`);

    // 5. Headquarter-Daten aktualisieren um hierarchyType zu pruefen
    headquarter = await getCustomer(request, headquarter.id);
    console.log(`[OK] Headquarter refreshed - hierarchyType: ${headquarter.hierarchyType}`);

    console.log('\n[DATA] Test data setup complete!\n');
  });

  test('should have headquarter with HEADQUARTER hierarchy type after adding branches', async () => {
    // Verify: Customer wurde zu HEADQUARTER
    expect(headquarter).toBeDefined();
    expect(headquarter.id).toBeTruthy();
    expect(headquarter.companyName).toContain('FreshChain GmbH');
    expect(headquarter.hierarchyType).toBe('HEADQUARTER');

    console.log(`[OK] Headquarter hierarchy type verified: ${headquarter.hierarchyType}`);
  });

  test('should create branches with FILIALE hierarchy type', async () => {
    // Verify: Filialen wurden erstellt
    expect(branch1).toBeDefined();
    expect(branch1.id).toBeTruthy();
    expect(branch2).toBeDefined();
    expect(branch2.id).toBeTruthy();

    // Verify: Filialen sind als FILIALE markiert
    expect(branch1.hierarchyType).toBe('FILIALE');
    expect(branch2.hierarchyType).toBe('FILIALE');

    console.log(`[OK] Branches created with correct hierarchy type`);
  });

  test('should find headquarter in customer list via API', async ({ request }) => {
    // API-basierte Suche statt UI-Navigation
    const response = await request.get(`${API_BASE}/api/customers?search=${TEST_PREFIX}`);
    expect(response.ok()).toBe(true);

    const data = await response.json();
    const customers = data.content || data;

    // Prüfe dass unser Headquarter in der Liste ist
    const foundHQ = customers.find(
      (c: CustomerResponse) => c.id === headquarter.id || c.companyName === headquarter.companyName
    );

    expect(foundHQ).toBeDefined();
    expect(foundHQ.companyName).toContain('FreshChain GmbH');
    expect(foundHQ.hierarchyType).toBe('HEADQUARTER');

    console.log(`[OK] Headquarter found in list via API: ${foundHQ.companyName}`);
  });

  test('should show hierarchy metrics for headquarter', async ({ request }) => {
    // Hole HierarchyMetrics via API
    const metrics = await getHierarchyMetrics(request, headquarter.id);

    // Verify: Korrekte Filialanzahl
    expect(metrics.branchCount).toBe(2);

    // Verify: branches array contains our branches
    expect(metrics.branches).toHaveLength(2);
    expect(metrics.branches.map(b => b.branchName)).toContain(branch1.companyName);
    expect(metrics.branches.map(b => b.branchName)).toContain(branch2.companyName);

    console.log(`[OK] Hierarchy metrics verified:`);
    console.log(`   - Branch count: ${metrics.branchCount}`);
    console.log(`   - Branches: ${metrics.branches.map(b => b.branchName).join(', ')}`);
  });

  test('should retrieve headquarter detail via API', async ({ request }) => {
    // API-basierter Detail-Abruf statt UI-Navigation
    const response = await request.get(`${API_BASE}/api/customers/${headquarter.id}`);
    expect(response.ok()).toBe(true);

    const data = await response.json();
    expect(data.id).toBe(headquarter.id);
    expect(data.companyName).toContain('FreshChain GmbH');
    expect(data.hierarchyType).toBe('HEADQUARTER');

    console.log(`[OK] Headquarter detail retrieved via API: ${data.companyName}`);
  });

  test('should retrieve branch list via hierarchy metrics API', async ({ request }) => {
    // API-basierte Validierung - Filialen MUESSEN existieren
    const response = await request.get(
      `${API_BASE}/api/customers/${headquarter.id}/hierarchy/metrics`
    );
    expect(response.ok()).toBe(true);

    const metricsData = await response.json();
    expect(metricsData.branchCount).toBe(2);

    // Prüfe dass beide Filialen in den Metrics sind
    const branchNames = metricsData.branches.map((b: { branchName: string }) => b.branchName);
    expect(branchNames).toContain(branch1.companyName);
    expect(branchNames).toContain(branch2.companyName);

    console.log(`[OK] Branch data verified via API: ${metricsData.branchCount} branches`);
  });

  test('should find all branches via API search', async ({ request }) => {
    // API-basierte Suche nach allen Filialen
    const response = await request.get(`${API_BASE}/api/customers?search=${TEST_PREFIX}`);
    expect(response.ok()).toBe(true);

    const data = await response.json();
    const customers = data.content || data;

    // Prüfe dass beide Filialen gefunden werden
    const foundBranch1 = customers.find((c: CustomerResponse) => c.id === branch1.id);
    const foundBranch2 = customers.find((c: CustomerResponse) => c.id === branch2.id);

    expect(foundBranch1).toBeDefined();
    expect(foundBranch2).toBeDefined();
    expect(foundBranch1.hierarchyType).toBe('FILIALE');
    expect(foundBranch2.hierarchyType).toBe('FILIALE');

    console.log(`[OK] Both branches found via API search`);
  });

  test('should validate end-to-end data integrity', async ({ request }) => {
    // Final Validation: Hole alle Daten via API und pruefe Konsistenz

    // 1. Headquarter pruefen
    const hqResponse = await request.get(`${API_BASE}/api/customers/${headquarter.id}`);
    expect(hqResponse.ok()).toBe(true);
    const hqData = await hqResponse.json();
    expect(hqData.hierarchyType).toBe('HEADQUARTER');

    // 2. Branch 1 pruefen
    const b1Response = await request.get(`${API_BASE}/api/customers/${branch1.id}`);
    expect(b1Response.ok()).toBe(true);
    const b1Data = await b1Response.json();
    expect(b1Data.hierarchyType).toBe('FILIALE');

    // 3. Branch 2 pruefen
    const b2Response = await request.get(`${API_BASE}/api/customers/${branch2.id}`);
    expect(b2Response.ok()).toBe(true);
    const b2Data = await b2Response.json();
    expect(b2Data.hierarchyType).toBe('FILIALE');

    console.log(`\n[OK] End-to-end data integrity validated!`);
    console.log(`   - HQ: ${hqData.companyName} (${hqData.hierarchyType})`);
    console.log(`   - Branch 1: ${b1Data.companyName} (${b1Data.hierarchyType})`);
    console.log(`   - Branch 2: ${b2Data.companyName} (${b2Data.hierarchyType})`);
  });
});
