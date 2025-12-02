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
