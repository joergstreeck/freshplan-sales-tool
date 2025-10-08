import type { Lead, Problem } from './types';

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

export async function createLead(payload: { name: string; email?: string }) {
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

// RFC7807 Error Handling
export async function toProblem(res: Response): Promise<Problem> {
  try {
    return await res.json();
  } catch {
    return { title: res.statusText, status: res.status };
  }
}
