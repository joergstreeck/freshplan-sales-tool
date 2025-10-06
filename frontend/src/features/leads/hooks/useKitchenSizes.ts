import { useQuery } from '@tanstack/react-query';

/**
 * Kitchen Size Enum Value from Backend
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
 * Fetch kitchen sizes from backend API
 *
 * GET /api/enums/kitchen-sizes
 * Returns: [{ value: "small", label: "Klein" }, ...]
 */
async function fetchKitchenSizes(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE}/api/enums/kitchen-sizes`, {
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch kitchen sizes: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch kitchen sizes for dropdowns
 *
 * Single Source of Truth: NO hardcoding in frontend
 * Values served from backend EnumResource
 *
 * @returns Query result with kitchen sizes data
 */
export function useKitchenSizes() {
  return useQuery({
    queryKey: ['enums', 'kitchenSizes'],
    queryFn: fetchKitchenSizes,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes (was cacheTime in v4)
  });
}
