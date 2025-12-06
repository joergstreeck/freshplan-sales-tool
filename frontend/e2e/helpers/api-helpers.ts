/**
 * Shared E2E Test Helpers for API Operations
 *
 * Diese Datei enthält wiederverwendbare Helper-Funktionen für E2E-Tests,
 * um Code-Duplikation zu vermeiden und konsistentes Verhalten sicherzustellen.
 *
 * @module E2E/Helpers/ApiHelpers
 * @since Sprint 2.1.7.7 - Issue #149
 */

import { APIRequestContext, Page, APIResponse } from '@playwright/test';

// API Base URL (vom CI-Workflow gesetzt)
export const API_BASE = process.env.VITE_API_URL || 'http://localhost:8080';

// =============================================================================
// RETRY CONFIGURATION FOR RATE LIMITING
// =============================================================================

const MAX_RETRIES = 5;
const DEFAULT_BACKOFF_MS = 5000; // 5 seconds default

/**
 * Extrahiert die retryAfter-Zeit aus dem Response-Body (in ms)
 * Server kann "60 seconds" oder numerisch senden
 */
function parseRetryAfter(body: string): number {
  try {
    const json = JSON.parse(body);
    if (json.retryAfter) {
      const match = json.retryAfter.match(/(\d+)/);
      if (match) {
        return parseInt(match[1], 10) * 1000; // seconds to ms
      }
    }
  } catch {
    // Ignore parse errors
  }
  return DEFAULT_BACKOFF_MS;
}

/**
 * Wrapper fuer API-Requests mit automatischem Retry bei Rate Limit (429)
 * Respektiert den server-seitigen retryAfter Header/Body
 * Verwendet: retryAfter vom Server, oder exponentielles Backoff 5s, 10s, 20s, 40s, 80s
 */
async function withRetry<T>(
  operation: () => Promise<{ response: APIResponse; parseResult: () => Promise<T> }>,
  context: string
): Promise<T> {
  let lastError: Error | null = null;

  for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
    const { response, parseResult } = await operation();

    if (response.ok()) {
      return parseResult();
    }

    // Bei Rate Limit (429) warten und retry
    if (response.status() === 429 && attempt < MAX_RETRIES) {
      const body = await response.text();
      // Respektiere server retryAfter, aber cap bei 30 Sekunden fuer CI
      const serverRetryMs = parseRetryAfter(body);
      const cappedRetryMs = Math.min(serverRetryMs, 30000);
      const exponentialBackoffMs = DEFAULT_BACKOFF_MS * Math.pow(2, attempt);
      const waitMs = Math.max(cappedRetryMs, exponentialBackoffMs);

      console.log(
        `[RATE LIMIT] ${context}: 429 received (server says ${serverRetryMs}ms), waiting ${waitMs}ms before retry ${attempt + 1}/${MAX_RETRIES}`
      );
      await new Promise(resolve => setTimeout(resolve, waitMs));
      continue;
    }

    // Andere Fehler
    const body = await response.text();
    lastError = new Error(`${context}: ${response.status()} - ${body}`);
    throw lastError;
  }

  throw lastError || new Error(`${context}: Max retries exceeded`);
}

// =============================================================================
// INTERFACES
// =============================================================================

export interface LeadResponse {
  id: number;
  companyName: string;
  status: string;
  stage: number;
  email?: string;
  city?: string;
}

export interface OpportunityResponse {
  id: string;
  name: string;
  stage: string;
  leadId?: number;
  expectedValue?: number;
}

export interface CustomerResponse {
  id: string;
  customerNumber: string;
  companyName: string;
  hierarchyType: string | null;
  status: string;
  branchCount?: number;
}

export interface ContactResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  role?: string;
}

export interface HierarchyMetricsResponse {
  branchCount: number;
  totalRevenue: number;
  averageRevenue: number;
  totalOpenOpportunities: number;
  branches: Array<{
    branchId: string;
    branchName: string;
    city: string;
    country: string;
    revenue: number;
    percentage: number;
    openOpportunities: number;
    status: string;
  }>;
}

// =============================================================================
// LEAD HELPERS
// =============================================================================

/**
 * Erstellt einen Lead via API
 * Mit automatischem Retry bei Rate Limit (429)
 * @param request - Playwright API Request Context
 * @param name - Firmenname (wird mit TEST_PREFIX versehen)
 * @param testPrefix - Einzigartiges Test-Prefix für Isolation
 */
export async function createLead(
  request: APIRequestContext,
  name: string,
  testPrefix: string
): Promise<LeadResponse> {
  const uniqueId = `${Date.now()}-${Math.random().toString(36).substring(7)}`;
  return withRetry(async () => {
    const response = await request.post(`${API_BASE}/api/leads`, {
      data: {
        companyName: `${testPrefix} ${name}`,
        stage: 1, // REGISTRIERUNG
        contact: {
          firstName: 'Max',
          lastName: 'Mustermann',
          email: `lead-${uniqueId}@test-e2e.local`,
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
    return {
      response,
      parseResult: () => response.json() as Promise<LeadResponse>,
    };
  }, 'Failed to create lead');
}

/**
 * Qualifiziert einen Lead (setzt Status auf QUALIFIED)
 * Leads müssen QUALIFIED oder ACTIVE sein um zu Opportunity konvertiert zu werden
 *
 * Die Leads API erfordert PATCH mit ETag (optimistisches Locking):
 * - ETag Format: "lead-{id}-{version}"
 * - Neu erstellte Leads haben version 0
 */
export async function qualifyLead(
  request: APIRequestContext,
  leadId: number,
  version: number = 0
): Promise<LeadResponse> {
  const etag = `"lead-${leadId}-${version}"`;
  const response = await request.patch(`${API_BASE}/api/leads/${leadId}`, {
    data: {
      status: 'QUALIFIED',
    },
    headers: {
      'Content-Type': 'application/json',
      'If-Match': etag,
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to qualify lead: ${response.status()} - ${body}`);
  }

  return response.json();
}

// =============================================================================
// OPPORTUNITY HELPERS
// =============================================================================

/**
 * Konvertiert Lead zu Opportunity via API
 * Voraussetzung: Lead muss QUALIFIED oder ACTIVE sein
 */
export async function convertLeadToOpportunity(
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
 * Setzt Opportunity auf CLOSED_WON Stage via API
 *
 * WICHTIG: Das Backend hat strikte Stage-Transition-Regeln!
 * Man kann nicht direkt von NEW_LEAD auf CLOSED_WON springen.
 * Erlaubte Transitions (siehe OpportunityStage.java):
 *   NEW_LEAD → QUALIFICATION → NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON
 *
 * Diese Funktion durchläuft alle notwendigen Stages automatisch.
 */
export async function setOpportunityToWon(
  request: APIRequestContext,
  opportunityId: string
): Promise<OpportunityResponse> {
  const stageSequence = [
    'QUALIFICATION',
    'NEEDS_ANALYSIS',
    'PROPOSAL',
    'NEGOTIATION',
    'CLOSED_WON',
  ];

  let currentOpp: OpportunityResponse | null = null;

  for (const stage of stageSequence) {
    const response = await request.put(
      `${API_BASE}/api/opportunities/${opportunityId}/stage/${stage}`,
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok()) {
      const body = await response.text();
      throw new Error(`Failed to set opportunity to ${stage}: ${response.status()} - ${body}`);
    }

    currentOpp = await response.json();
  }

  return currentOpp!;
}

/**
 * Konvertiert Opportunity zu Customer via API
 */
export async function convertOpportunityToCustomer(
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

// =============================================================================
// CUSTOMER HELPERS
// =============================================================================

/**
 * Erstellt einen Customer via API (startet als STANDALONE)
 * Mit automatischem Retry bei Rate Limit (429)
 */
export async function createCustomer(
  request: APIRequestContext,
  name: string,
  testPrefix: string,
  options: {
    status?: string;
    hierarchyType?: string;
    expectedAnnualVolume?: number;
  } = {}
): Promise<CustomerResponse> {
  return withRetry(async () => {
    const response = await request.post(`${API_BASE}/api/customers`, {
      data: {
        companyName: `${testPrefix} ${name}`,
        customerType: 'UNTERNEHMEN',
        businessType: 'RESTAURANT',
        status: options.status || 'AKTIV',
        expectedAnnualVolume: options.expectedAnnualVolume || 500000.0,
        hierarchyType: options.hierarchyType || 'STANDALONE',
      },
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return {
      response,
      parseResult: () => response.json() as Promise<CustomerResponse>,
    };
  }, 'Failed to create customer');
}

/**
 * Holt Customer-Details via API
 */
export async function getCustomer(
  request: APIRequestContext,
  customerId: string
): Promise<CustomerResponse> {
  const response = await request.get(`${API_BASE}/api/customers/${customerId}`);

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to get customer: ${response.status()} - ${body}`);
  }

  return response.json();
}

/**
 * Aktualisiert einen Customer via API
 * Mit automatischem Retry bei Rate Limit (429)
 */
export async function updateCustomer(
  request: APIRequestContext,
  customerId: string,
  data: Record<string, unknown>
): Promise<CustomerResponse> {
  return withRetry(async () => {
    const response = await request.put(`${API_BASE}/api/customers/${customerId}`, {
      data,
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return {
      response,
      parseResult: () => response.json() as Promise<CustomerResponse>,
    };
  }, 'Failed to update customer');
}

/**
 * Erstellt eine Filiale unter einem Headquarter via API
 * Das Erstellen einer Filiale macht den Parent automatisch zum HEADQUARTER
 * Mit automatischem Retry bei Rate Limit (429)
 */
export async function createBranch(
  request: APIRequestContext,
  headquarterId: string,
  name: string,
  city: string,
  testPrefix: string
): Promise<CustomerResponse> {
  return withRetry(async () => {
    const response = await request.post(`${API_BASE}/api/customers/${headquarterId}/branches`, {
      data: {
        companyName: `${testPrefix} ${name}`,
        customerType: 'UNTERNEHMEN',
        businessType: 'RESTAURANT',
        expectedAnnualVolume: 75000.0,
        address: {
          street: 'Teststrasse 1',
          postalCode: '12345',
          city: city,
          country: 'DE',
        },
        contact: {
          phone: '+49 123 456789',
          email: `${name.toLowerCase().replace(/[^a-z0-9.-]/g, '')}@test.local`,
        },
      },
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return {
      response,
      parseResult: () => response.json() as Promise<CustomerResponse>,
    };
  }, 'Failed to create branch');
}

/**
 * Holt Hierarchy-Metriken für einen Headquarter via API
 */
export async function getHierarchyMetrics(
  request: APIRequestContext,
  customerId: string
): Promise<HierarchyMetricsResponse> {
  const response = await request.get(`${API_BASE}/api/customers/${customerId}/hierarchy/metrics`);

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to get hierarchy metrics: ${response.status()} - ${body}`);
  }

  return response.json();
}

// =============================================================================
// CONTACT HELPERS
// =============================================================================

/**
 * Fügt einen Ansprechpartner zu einem Customer hinzu via API
 * Mit automatischem Retry bei Rate Limit (429)
 */
export async function addContact(
  request: APIRequestContext,
  customerId: string,
  contact: {
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    role?: string;
  }
): Promise<ContactResponse> {
  return withRetry(async () => {
    const response = await request.post(`${API_BASE}/api/customers/${customerId}/contacts`, {
      data: contact,
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return {
      response,
      parseResult: () => response.json() as Promise<ContactResponse>,
    };
  }, 'Failed to add contact');
}

// =============================================================================
// UI WAIT HELPERS (Anti-Pattern Vermeidung)
// =============================================================================

/**
 * Wartet auf eine API-Antwort nach einer Sucheingabe
 * Ersetzt waitForTimeout() für Debounce-Logik
 */
export async function waitForSearchResponse(
  page: Page,
  searchPattern: RegExp | string = /api\/(customers|leads)/
): Promise<void> {
  await page.waitForResponse(
    resp => {
      const url = resp.url();
      if (typeof searchPattern === 'string') {
        return url.includes(searchPattern);
      }
      return searchPattern.test(url);
    },
    { timeout: 10000 }
  );
}

/**
 * Füllt ein Suchfeld und wartet auf die API-Antwort
 * Ersetzt: searchInput.fill() + waitForTimeout(500)
 */
export async function searchAndWait(
  page: Page,
  searchInput: ReturnType<Page['locator']>,
  searchTerm: string,
  apiPattern: RegExp | string = /api\/(customers|leads)/
): Promise<void> {
  // Starte das Warten auf die Antwort BEVOR wir tippen
  const responsePromise = page.waitForResponse(
    resp => {
      const url = resp.url();
      if (typeof apiPattern === 'string') {
        return url.includes(apiPattern);
      }
      return apiPattern.test(url);
    },
    { timeout: 10000 }
  );

  await searchInput.fill(searchTerm);
  await responsePromise;
}

/**
 * Generiert ein einzigartiges Test-Prefix für Isolation
 */
export function generateTestPrefix(prefix: string): string {
  return `[${prefix}-${Date.now()}-${Math.random().toString(36).substring(7)}]`;
}

// =============================================================================
// LEAD IMPORT HELPERS (Sprint 2.1.8 - Self-Service Import)
// =============================================================================

export interface QuotaInfoResponse {
  currentOpenLeads: number;
  maxOpenLeads: number;
  todayImports: number; // API returns todayImports, not importsToday
  maxImportsPerDay: number;
  maxLeadsPerImport: number;
  remainingCapacity: number;
  canImport?: boolean; // Computed from remainingCapacity > 0
}

export interface ImportUploadResponse {
  uploadId: string;
  columns: string[];
  rowCount: number;
  suggestedMapping: Record<string, string>; // API returns suggestedMapping
  fileType: string;
  charset: string;
  availableFields: Array<{ key: string; label: string; required: boolean }>;
}

export interface ImportPreviewResponse {
  uploadId: string;
  validation: {
    totalRows: number;
    validRows: number;
    errorRows: number;
    duplicateRows: number;
  };
  previewRows: Array<{
    row: number;
    status: string;
    data: Record<string, string>;
  }>;
  errors: Array<{
    row: number;
    column: string;
    message: string;
    value: string;
  }>;
  duplicates: Array<{
    row: number;
    existingLeadId: number;
    existingCompanyName: string;
    type: string;
    similarity: number;
  }>;
  quotaCheck: {
    approved: boolean;
    message: string;
    currentOpenLeads: number;
    maxOpenLeads: number;
    remainingCapacity: number;
  };
}

export interface ImportExecuteResponse {
  success: boolean;
  importId: string | null;
  imported: number;
  skipped: number;
  errors: number;
  status: string;
  message: string;
}

/**
 * Holt die Quota-Info für den aktuellen User
 */
export async function getImportQuota(request: APIRequestContext): Promise<QuotaInfoResponse> {
  const response = await request.get(`${API_BASE}/api/leads/import/quota`);

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to get import quota: ${response.status()} - ${body}`);
  }

  return response.json();
}

/**
 * Lädt eine CSV-Datei für den Import hoch
 */
export async function uploadImportFile(
  request: APIRequestContext,
  csvContent: string,
  fileName: string = 'test-import.csv'
): Promise<ImportUploadResponse> {
  // Create multipart form data with CSV content
  const response = await request.post(`${API_BASE}/api/leads/import/upload`, {
    multipart: {
      file: {
        name: fileName,
        mimeType: 'text/csv',
        buffer: Buffer.from(csvContent, 'utf-8'),
      },
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to upload import file: ${response.status()} - ${body}`);
  }

  return response.json();
}

/**
 * Erstellt eine Import-Vorschau mit Validierung
 */
export async function createImportPreview(
  request: APIRequestContext,
  uploadId: string,
  mapping: Record<string, string>
): Promise<ImportPreviewResponse> {
  const response = await request.post(`${API_BASE}/api/leads/import/${uploadId}/preview`, {
    data: { mapping },
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to create import preview: ${response.status()} - ${body}`);
  }

  return response.json();
}

/**
 * Führt den Import aus
 */
export async function executeImport(
  request: APIRequestContext,
  uploadId: string,
  options: {
    mapping: Record<string, string>;
    duplicateAction?: 'SKIP' | 'CREATE';
    source?: string;
    ignoreErrors?: boolean;
  }
): Promise<ImportExecuteResponse> {
  const response = await request.post(`${API_BASE}/api/leads/import/${uploadId}/execute`, {
    data: {
      mapping: options.mapping,
      duplicateAction: options.duplicateAction || 'SKIP',
      source: options.source || 'E2E-TEST',
      ignoreErrors: options.ignoreErrors || false,
    },
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to execute import: ${response.status()} - ${body}`);
  }

  return response.json();
}

// =============================================================================
// CLEANUP HELPERS (für Test-Daten Bereinigung)
// =============================================================================

/**
 * Löscht einen Lead via API mit ETag-Header
 * Die API erfordert If-Match Header für optimistisches Locking
 */
export async function deleteLead(
  request: APIRequestContext,
  leadId: number,
  version: number = 0
): Promise<boolean> {
  const etag = `"lead-${leadId}-${version}"`;
  const response = await request.delete(`${API_BASE}/api/leads/${leadId}`, {
    headers: {
      'If-Match': etag,
    },
  });

  if (response.status() === 429) {
    // Rate limit - wait and retry once
    await new Promise(resolve => setTimeout(resolve, 5000));
    const retryResponse = await request.delete(`${API_BASE}/api/leads/${leadId}`, {
      headers: {
        'If-Match': etag,
      },
    });
    return retryResponse.ok() || retryResponse.status() === 404;
  }

  return response.ok() || response.status() === 404; // 404 = already deleted
}

/**
 * Löscht alle Test-Leads die mit einem bestimmten Prefix beginnen
 * Nützlich für Cleanup nach E2E-Tests
 *
 * @param request - Playwright API Request Context
 * @param prefixPattern - Pattern zum Matchen (z.B. "[E2E-IMP-")
 * @returns Anzahl der gelöschten Leads
 */
export async function deleteTestLeadsByPrefix(
  request: APIRequestContext,
  prefixPattern: string
): Promise<number> {
  let deletedCount = 0;
  let page = 0;
  const pageSize = 50;

  console.log(`[CLEANUP] Searching for leads matching: ${prefixPattern}`);

  // Durchsuche alle Seiten nach Test-Leads
  while (true) {
    const response = await request.get(`${API_BASE}/api/leads?page=${page}&size=${pageSize}`);

    if (!response.ok()) {
      console.log(`[CLEANUP] Failed to fetch leads page ${page}`);
      break;
    }

    const data = await response.json();
    const leads = data.data || data.content || [];

    if (leads.length === 0) {
      break;
    }

    // Filtere Leads die mit dem Prefix beginnen
    const testLeads = leads.filter(
      (lead: LeadResponse) => lead.companyName && lead.companyName.includes(prefixPattern)
    );

    // Lösche gefundene Test-Leads
    for (const lead of testLeads) {
      const deleted = await deleteLead(request, lead.id, 0);
      if (deleted) {
        deletedCount++;
        console.log(`[CLEANUP] Deleted lead ${lead.id}: ${lead.companyName}`);
      }
      // Kleine Pause um Rate-Limiting zu vermeiden
      await new Promise(resolve => setTimeout(resolve, 100));
    }

    page++;

    // Safety limit - nicht mehr als 10 Seiten durchsuchen
    if (page >= 10) {
      console.log(`[CLEANUP] Reached page limit, stopping search`);
      break;
    }
  }

  console.log(`[CLEANUP] Deleted ${deletedCount} test leads`);
  return deletedCount;
}

/**
 * Löscht einen Customer via API
 */
export async function deleteCustomer(
  request: APIRequestContext,
  customerId: string
): Promise<boolean> {
  const response = await request.delete(`${API_BASE}/api/customers/${customerId}`);

  if (response.status() === 429) {
    // Rate limit - wait and retry once
    await new Promise(resolve => setTimeout(resolve, 5000));
    const retryResponse = await request.delete(`${API_BASE}/api/customers/${customerId}`);
    return retryResponse.ok() || retryResponse.status() === 404;
  }

  return response.ok() || response.status() === 404; // 404 = already deleted
}

/**
 * Löscht alle Test-Customers die mit einem bestimmten Prefix beginnen
 * Nützlich für Cleanup nach E2E-Tests
 *
 * @param request - Playwright API Request Context
 * @param prefixPattern - Pattern zum Matchen (z.B. "[E2E-CO-")
 * @returns Anzahl der gelöschten Customers
 */
export async function deleteTestCustomersByPrefix(
  request: APIRequestContext,
  prefixPattern: string
): Promise<number> {
  let deletedCount = 0;
  let page = 0;
  const pageSize = 50;

  console.log(`[CLEANUP] Searching for customers matching: ${prefixPattern}`);

  // Durchsuche alle Seiten nach Test-Customers
  while (true) {
    const response = await request.get(`${API_BASE}/api/customers?page=${page}&size=${pageSize}`);

    if (!response.ok()) {
      console.log(`[CLEANUP] Failed to fetch customers page ${page}`);
      break;
    }

    const data = await response.json();
    const customers = data.content || data;

    if (customers.length === 0) {
      break;
    }

    // Filtere Customers die mit dem Prefix beginnen
    const testCustomers = customers.filter(
      (cust: CustomerResponse) => cust.companyName && cust.companyName.includes(prefixPattern)
    );

    // Lösche gefundene Test-Customers
    for (const cust of testCustomers) {
      const deleted = await deleteCustomer(request, cust.id);
      if (deleted) {
        deletedCount++;
        console.log(`[CLEANUP] Deleted customer ${cust.id}: ${cust.companyName}`);
      }
      // Kleine Pause um Rate-Limiting zu vermeiden
      await new Promise(resolve => setTimeout(resolve, 100));
    }

    page++;

    // Safety limit - nicht mehr als 10 Seiten durchsuchen
    if (page >= 10) {
      console.log(`[CLEANUP] Reached page limit, stopping search`);
      break;
    }
  }

  console.log(`[CLEANUP] Deleted ${deletedCount} test customers`);
  return deletedCount;
}

/**
 * Generiert eine CSV-Datei mit Test-Leads
 *
 * @param testPrefix - Prefix für Firmennamen (zur Isolation)
 * @param count - Anzahl der zu generierenden Leads
 * @param options - Optionale Konfiguration
 * @param options.includeErrors - Fügt eine Zeile mit Validierungsfehlern hinzu
 * @param options.includeDuplicates - Fügt ein Duplikat der ersten Zeile hinzu
 * @param options.includeHistoricalDates - Fügt originalCreatedAt Spalte hinzu (Sprint 2.1.8)
 */
export function generateTestLeadsCsv(
  testPrefix: string,
  count: number,
  options: {
    includeErrors?: boolean;
    includeDuplicates?: boolean;
    includeHistoricalDates?: boolean;
  } = {}
): string {
  const headers = options.includeHistoricalDates
    ? ['Firma', 'E-Mail', 'Telefon', 'Stadt', 'PLZ', 'Branche', 'Erstelldatum']
    : ['Firma', 'E-Mail', 'Telefon', 'Stadt', 'PLZ', 'Branche'];
  const rows: string[] = [headers.join(';')];

  for (let i = 0; i < count; i++) {
    const uniqueId = `${Date.now()}-${i}`;
    let companyName = `${testPrefix} TestFirma ${i + 1}`;
    let email = `test-${uniqueId}@e2e-import.local`;

    // Füge Fehler hinzu (leere Firma)
    if (options.includeErrors && i === count - 1) {
      companyName = ''; // Pflichtfeld fehlt
      email = 'invalid-email'; // Ungültiges Format
    }

    // Basis-Zeile
    const rowData = [
      companyName,
      email,
      `+49 ${100 + i} 123456`,
      i % 2 === 0 ? 'Berlin' : 'München',
      i % 2 === 0 ? '10115' : '80331',
      'RESTAURANT',
    ];

    // Historisches Datum hinzufügen (Sprint 2.1.8)
    if (options.includeHistoricalDates) {
      // Generiere Datum: 6 Monate bis 2 Jahre in der Vergangenheit
      const monthsAgo = 6 + i * 3; // 6, 9, 12, 15, ... Monate
      const historicalDate = new Date();
      historicalDate.setMonth(historicalDate.getMonth() - monthsAgo);
      // Format: YYYY-MM-DD (ISO 8601 Date)
      rowData.push(historicalDate.toISOString().split('T')[0]);
    }

    rows.push(rowData.join(';'));
  }

  // Füge Duplikat hinzu (gleiche Firma wie erste Zeile)
  if (options.includeDuplicates && count > 0) {
    const uniqueId = `${Date.now()}-dup`;
    const dupRow = [
      `${testPrefix} TestFirma 1`, // Duplikat der ersten Zeile
      `dup-${uniqueId}@e2e-import.local`,
      '+49 999 999999',
      'Hamburg',
      '20095',
      'RESTAURANT',
    ];

    if (options.includeHistoricalDates) {
      const historicalDate = new Date();
      historicalDate.setMonth(historicalDate.getMonth() - 12); // 1 Jahr
      dupRow.push(historicalDate.toISOString().split('T')[0]);
    }

    rows.push(dupRow.join(';'));
  }

  return rows.join('\n');
}
