export type Lead = {
  id: string;
  name: string;
  email?: string;
  createdAt?: string;
};

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function authHeaders() {
  // Platzhalter: JWT aus bestehender App übernehmen, falls vorhanden
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function listLeads(): Promise<Lead[]> {
  const res = await fetch(`${BASE}/api/leads`, {
    headers: { 'Accept': 'application/json', ...authHeaders() },
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

export async function createLead(payload: { name: string; email?: string }) {
  const res = await fetch(`${BASE}/api/leads`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify(payload),
    credentials: 'include',
  });
  if (!res.ok) throw await toProblem(res);
  return res.json();
}

// RFC7807 Error Handling
export type Problem = {
  type?: string;
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string[]>
};

export async function toProblem(res: Response): Promise<Problem> {
  try {
    return await res.json();
  } catch {
    return { title: res.statusText, status: res.status };
  }
}