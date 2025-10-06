import { useQuery } from '@tanstack/react-query';

/**
 * Lead Source Enum Value from Backend
 */
export interface EnumValue {
  value: string;
  label: string;
}

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function authHeaders() {
  // Dev Mode: Use dev-auth-user from sessionStorage (bypass Keycloak)
  const devUser = sessionStorage.getItem('dev-auth-user');
  if (devUser) {
    const user = JSON.parse(devUser);
    const mockToken = `dev.${user.id}.${user.username}`;
    return { Authorization: `Bearer ${mockToken}` };
  }

  // Production: JWT from localStorage (Keycloak)
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/**
 * Fetch lead sources from backend API
 *
 * GET /api/enums/lead-sources
 * Returns: [{ value: "MESSE", label: "Messe" }, ...]
 */
async function fetchLeadSources(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE}/api/enums/lead-sources`, {
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch lead sources: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch lead sources for dropdowns
 *
 * Single Source of Truth: NO hardcoding in frontend
 * Values served from backend EnumResource
 *
 * @returns Query result with lead sources data
 */
export function useLeadSources() {
  return useQuery({
    queryKey: ['enums', 'leadSources'],
    queryFn: fetchLeadSources,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes (was cacheTime in v4)
  });
}
