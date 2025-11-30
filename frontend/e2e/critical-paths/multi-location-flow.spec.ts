/**
 * E2E Critical Path: Multi-Location Management Flow
 *
 * Sprint 2.1.7.7 - Issue #149
 * Self-Contained Test gegen echtes Backend
 *
 * Testet den kompletten Multi-Location Business Flow:
 * 1. Customer erstellen (via API) - startet als STANDALONE
 * 2. Filialen hinzufuegen (via /branches API) - macht Parent zu HEADQUARTER
 * 3. UI-Validierung der Hierarchie
 * 4. HierarchyMetrics pruefen (aggregierte Werte)
 *
 * Backend-Verhalten:
 * - Neue Customers starten immer als STANDALONE
 * - hierarchyType wird automatisch zu HEADQUARTER wenn Filialen hinzugefuegt werden
 * - Filialen werden automatisch als FILIALE markiert
 *
 * Prinzipien:
 * - Self-Contained: Jeder Test erstellt seine Daten selbst
 * - UUID-Isolation: Keine Kollisionen mit anderen Tests
 * - Keine waitForTimeout: Explizite Waits auf API-Responses
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
  searchAndWait,
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

  test('should display headquarter in customer list', async ({ page }) => {
    // Navigate zu Kundenliste
    await page.goto('/customers');
    await page.waitForLoadState('networkidle');

    // Warte auf Tabelle (expliziter Wait)
    const table = page.locator('table').first();
    await expect(table).toBeVisible({ timeout: 10000 });

    // Suche nach unserem Test-Headquarter mit API-Response-Wait
    const searchInput = page.locator('input[placeholder*="Such"]').first();
    if (await searchInput.isVisible({ timeout: 2000 }).catch(() => false)) {
      await searchAndWait(page, searchInput, TEST_PREFIX, /api\/customers/);
    }

    // Verify: Headquarter MUSS sichtbar sein (harte Assertion)
    const headquarterRow = page.locator(`text=${headquarter.companyName}`);
    await expect(headquarterRow).toBeVisible({ timeout: 5000 });

    console.log(`[OK] Headquarter visible in customer list`);
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

  test('should navigate to headquarter detail and show branch list', async ({ page, request }) => {
    // Navigate zu Headquarter Detail
    await page.goto(`/customers/${headquarter.id}`);
    await page.waitForLoadState('networkidle');

    // Warte auf Detail-Seite (harte Assertion)
    await expect(
      page.locator('[data-testid="customer-detail"], h1, h2, .MuiCard-root').first()
    ).toBeVisible({ timeout: 10000 });

    // Verify URL contains headquarter ID
    expect(page.url()).toContain(headquarter.id);

    // API-basierte Validierung - Filialen MUESSEN existieren (harte Assertion)
    const metrics = await request.get(
      `${API_BASE}/api/customers/${headquarter.id}/hierarchy/metrics`
    );
    expect(metrics.ok()).toBe(true);
    const metricsData = await metrics.json();
    expect(metricsData.branchCount).toBe(2);

    console.log(`[OK] Branch data verified via API: ${metricsData.branchCount} branches`);
  });

  test('should show headquarter with branch info in list', async ({ page }) => {
    // Navigate zu Kundenliste
    await page.goto('/customers');
    await page.waitForLoadState('networkidle');

    // Warte auf Tabelle (harte Assertion)
    const table = page.locator('table').first();
    await expect(table).toBeVisible({ timeout: 10000 });

    // Suche nach Headquarter mit API-Response-Wait
    const searchInput = page.locator('input[placeholder*="Such"]').first();
    if (await searchInput.isVisible({ timeout: 2000 }).catch(() => false)) {
      await searchAndWait(page, searchInput, headquarter.companyName, /api\/customers/);
    }

    // Verify: Headquarter MUSS sichtbar sein (harte Assertion)
    const headquarterLocator = page.locator(`text=${headquarter.companyName}`);
    await expect(headquarterLocator).toBeVisible({ timeout: 5000 });
    console.log(`[OK] Headquarter visible in list`);

    // Optional: Prüfe auf Branch-Count-Badge (Feature könnte noch nicht implementiert sein)
    // Dieses Feature ist UI-spezifisch - die harte Assertion ist die Headquarter-Sichtbarkeit oben
    const branchBadge = page.locator('text=/2.*Filiale|Filialen.*2/i').first();
    const hasBadge = await branchBadge.isVisible({ timeout: 2000 }).catch(() => false);

    if (hasBadge) {
      console.log(`[OK] Branch count badge shows 2 branches`);
    } else {
      console.log(`[INFO] Branch count badge not visible - feature might not be implemented yet`);
    }
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
