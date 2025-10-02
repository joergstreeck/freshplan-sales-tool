import type { Lead, Problem } from './types';

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
export async function createLead(payload: {
  // Stage 0: Company Basics
  stage?: number;
  companyName: string;
  city?: string;
  postalCode?: string;
  businessType?: string;
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
  kitchenSize?: string;
  employeeCount?: number;
  website?: string;
  industry?: string;
  // Legacy support
  name?: string;
  email?: string;
}) {
  const res = await fetch(`${BASE}/api/leads`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify({ ...payload, source: 'manual' }),
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
