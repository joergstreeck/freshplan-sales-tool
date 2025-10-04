import type { Lead, Problem, LeadStage, BusinessType } from './types';

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function authHeaders() {
  // Platzhalter: JWT aus bestehender App übernehmen, falls vorhanden
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function listLeads(): Promise<Lead[]> {
  const res = await fetch(`${BASE}/api/leads`, {
    headers: { Accept: 'application/json', ...authHeaders() },
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

// Sprint 2.1.5 - Progressive Profiling API
export async function createLead(
  payload: {
    // Stage 0: Company Basics
    stage?: LeadStage;
    companyName: string;
    city?: string;
    postalCode?: string;
    businessType?: BusinessType;
    source?: string; // Sprint 2.1.5: MESSE, EMPFEHLUNG, TELEFON, etc.
    // Stage 1: Contact + DSGVO Consent
    contact?: {
      firstName?: string;
      lastName?: string;
      email?: string;
      phone?: string;
    };
    consentGivenAt?: string; // ISO 8601 timestamp
    // Stage 2: Business Details
    estimatedVolume?: number;
    kitchenSize?: 'small' | 'medium' | 'large';
    employeeCount?: number;
    website?: string;
    industry?: string;
    // Sprint 2.1.5: Erstkontakt → activities[]
    activities?: Array<{
      activityType: string;
      performedAt: string;
      summary: string;
      countsAsProgress: boolean;
      metadata?: Record<string, unknown>;
    }>;
    // Legacy support
    name?: string;
    email?: string;
  },
  options?: {
    params?: {
      reason?: string; // Soft Duplicate Override (min. 10 Zeichen)
      overrideReason?: string; // Hard Duplicate Override (Manager/Admin, min. 10 Zeichen)
    };
  }
) {
  const queryParams = new URLSearchParams();
  if (options?.params?.reason) queryParams.set('reason', options.params.reason);
  if (options?.params?.overrideReason)
    queryParams.set('overrideReason', options.params.overrideReason);

  const url = queryParams.toString()
    ? `${BASE}/api/leads?${queryParams}`
    : `${BASE}/api/leads`;

  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify(payload), // ⚠️ KEIN hardcoded source: 'manual'
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
