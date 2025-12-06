import type { Lead, LeadContactDTO, Problem } from './types';

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function authHeaders() {
  // Dev Mode: Use dev-auth-user from sessionStorage (bypass Keycloak)
  const devUser = sessionStorage.getItem('dev-auth-user');
  if (devUser) {
    const user = JSON.parse(devUser);
    // Create mock JWT for dev mode (Backend expects JWT in dev mode)
    const mockToken = `dev.${user.id}.${user.username}`;
    return { Authorization: `Bearer ${mockToken}` };
  }

  // Production: JWT aus localStorage (Keycloak)
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function listLeads(): Promise<Lead[]> {
  const res = await fetch(`${BASE}/api/leads`, {
    headers: { Accept: 'application/json', ...authHeaders() },
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  const json = await res.json();
  // Backend returns PaginatedResponse { data: Lead[], page, size, total }
  return json.data || [];
}

export async function createLead(payload: Record<string, unknown>) {
  const res = await fetch(`${BASE}/api/leads`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify(payload),
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

export async function updateLead(
  id: number,
  payload: {
    stopClock?: boolean;
    stopReason?: string;
    companyName?: string;
    contactPerson?: string;
    email?: string;
    phone?: string;
    website?: string;
    street?: string;
    postalCode?: string;
    city?: string;
  }
): Promise<Lead> {
  const res = await fetch(`${BASE}/api/leads/${id}`, {
    method: 'PATCH', // Backend uses @PATCH (partial update)
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'If-Match': '*', // Optimistic locking - use wildcard for now
      ...authHeaders(),
    },
    body: JSON.stringify(payload),
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

export async function addFirstContact(
  id: number,
  payload: {
    contactPerson: string;
    email?: string;
    phone?: string;
    contactDate?: string;
    notes?: string;
  }
): Promise<Lead> {
  const res = await fetch(`${BASE}/api/leads/${id}/first-contact`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify(payload),
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

export async function getLeadById(id: string): Promise<Lead> {
  const res = await fetch(`${BASE}/api/leads/${id}`, {
    headers: {
      Accept: 'application/json',
      'Cache-Control': 'no-cache, no-store, must-revalidate',
      Pragma: 'no-cache',
      ...authHeaders(),
    },
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

export async function deleteLead(id: number): Promise<void> {
  const res = await fetch(`${BASE}/api/leads/${id}`, {
    method: 'DELETE',
    headers: {
      Accept: 'application/json',
      'If-Match': '*', // Optimistic locking - use wildcard for now
      ...authHeaders(),
    },
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
}

// ===========================
// Lead Contacts API - Sprint 2.1.6 Phase 5+
// ===========================

export async function createLeadContact(
  leadId: number,
  contactData: Partial<LeadContactDTO>
): Promise<LeadContactDTO> {
  const res = await fetch(`${BASE}/api/leads/${leadId}/contacts`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify(contactData),
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

export async function updateLeadContact(
  leadId: number,
  contactId: string,
  contactData: Partial<LeadContactDTO>
): Promise<LeadContactDTO> {
  const res = await fetch(`${BASE}/api/leads/${leadId}/contacts/${contactId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify(contactData),
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

export async function deleteLeadContact(leadId: number, contactId: string): Promise<void> {
  const res = await fetch(`${BASE}/api/leads/${leadId}/contacts/${contactId}`, {
    method: 'DELETE',
    headers: {
      Accept: 'application/json',
      ...authHeaders(),
    },
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
}

export async function setLeadContactAsPrimary(
  leadId: number,
  contactId: string
): Promise<LeadContactDTO> {
  const res = await fetch(`${BASE}/api/leads/${leadId}/contacts/${contactId}/primary`, {
    method: 'PATCH',
    headers: {
      Accept: 'application/json',
      ...authHeaders(),
    },
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

/**
 * Recalculate lead score manually.
 * Sprint 2.1.6+ Lead Scoring System.
 */
export async function recalculateLeadScore(leadId: number): Promise<{
  leadScore: number;
  painScore: number;
  revenueScore: number;
  fitScore: number;
  engagementScore: number;
}> {
  const res = await fetch(`${BASE}/api/leads/${leadId}/recalculate-score`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    credentials: 'include',
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({ error: 'Unknown error' }));
    throw new Error(error.message || 'Failed to recalculate score');
  }

  return res.json();
}

// ===========================
// Fuzzy Search API - Sprint 2.1.8 Phase 4
// ===========================

/**
 * Fuzzy search response type
 */
export interface FuzzySearchResponse {
  data: Lead[];
  total: number;
  query: string;
  fuzzyEnabled: boolean;
}

/**
 * Duplicate check response type
 */
export interface DuplicateCheckResponse {
  duplicates: Array<{
    lead: Lead;
    similarity: number;
    matchedFields: string[];
  }>;
  count: number;
  searchCriteria: {
    companyName: string;
    email: string;
  };
}

/**
 * Fuzzy-Suche für Leads mit pg_trgm.
 * Findet auch bei Tippfehlern und Varianten.
 *
 * @param query Suchbegriff
 * @param limit Maximale Anzahl Ergebnisse (default: 20)
 * @param includeInactive Auch inaktive Leads einschließen
 */
export async function fuzzySearchLeads(
  query: string,
  limit: number = 20,
  includeInactive: boolean = false
): Promise<FuzzySearchResponse> {
  const params = new URLSearchParams({
    q: query,
    limit: limit.toString(),
    includeInactive: includeInactive.toString(),
  });

  const res = await fetch(`${BASE}/api/leads/search/fuzzy?${params}`, {
    headers: {
      Accept: 'application/json',
      ...authHeaders(),
    },
    credentials: 'include',
  });

  if (!res.ok) throw await toProblem(res);
  return res.json();
}

/**
 * Prüft auf potentielle Duplikate.
 * Nützlich für Import-Vorschau und Lead-Erstellung.
 *
 * @param companyName Firmenname (required)
 * @param email E-Mail (optional, erhöht Genauigkeit)
 * @param limit Maximale Anzahl Ergebnisse (default: 5)
 */
export async function checkDuplicates(
  companyName: string,
  email?: string,
  limit: number = 5
): Promise<DuplicateCheckResponse> {
  const params = new URLSearchParams({
    companyName,
    limit: limit.toString(),
  });

  if (email) {
    params.append('email', email);
  }

  const res = await fetch(`${BASE}/api/leads/search/duplicates?${params}`, {
    headers: {
      Accept: 'application/json',
      ...authHeaders(),
    },
    credentials: 'include',
  });

  if (!res.ok) throw await toProblem(res);
  return res.json();
}

/**
 * Berechnet die Ähnlichkeit zwischen zwei Strings.
 *
 * @param text1 Erster String
 * @param text2 Zweiter String
 * @returns Ähnlichkeit (0-100%)
 */
export async function calculateSimilarity(
  text1: string,
  text2: string
): Promise<{ similarity: number; isMatch: boolean }> {
  const params = new URLSearchParams({ text1, text2 });

  const res = await fetch(`${BASE}/api/leads/search/similarity?${params}`, {
    headers: {
      Accept: 'application/json',
      ...authHeaders(),
    },
    credentials: 'include',
  });

  if (!res.ok) throw await toProblem(res);
  return res.json();
}

/**
 * Prüft ob pg_trgm Extension verfügbar ist.
 */
export async function getSearchStatus(): Promise<{
  pgTrgmEnabled: boolean;
  fuzzySearchAvailable: boolean;
  fallbackMode: boolean;
}> {
  const res = await fetch(`${BASE}/api/leads/search/status`, {
    headers: {
      Accept: 'application/json',
      ...authHeaders(),
    },
    credentials: 'include',
  });

  if (!res.ok) throw await toProblem(res);
  return res.json();
}

// RFC7807 Error Handling
export async function toProblem(res: Response): Promise<Problem> {
  try {
    return await res.json();
  } catch {
    return { title: res.statusText, status: res.status };
  }
}
