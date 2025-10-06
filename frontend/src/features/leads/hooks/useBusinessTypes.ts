import { useQuery } from '@tanstack/react-query';

/**
 * Business Type Enum Value from Backend
 * Sprint 2.1.6: Single Source of Truth for dropdown values
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
 * Fetch business types from backend API
 *
 * GET /api/enums/business-types
 * Returns: [{ value: "RESTAURANT", label: "Restaurant" }, ...]
 */
async function fetchBusinessTypes(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE}/api/enums/business-types`, {
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch business types: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch business types for dropdowns
 *
 * Sprint 2.1.6: Harmonized BusinessType values across Lead and Customer
 * - 9 unified values: RESTAURANT, HOTEL, CATERING, KANTINE, GROSSHANDEL, LEH, BILDUNG, GESUNDHEIT, SONSTIGES
 * - NO hardcoding in frontend
 * - Values served from backend BusinessType enum
 *
 * @returns Query result with business types data
 */
export function useBusinessTypes() {
  return useQuery({
    queryKey: ['enums', 'businessTypes'],
    queryFn: fetchBusinessTypes,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    cacheTime: 10 * 60 * 1000, // 10 minutes
  });
}
