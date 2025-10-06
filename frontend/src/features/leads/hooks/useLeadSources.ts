import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from './shared';

/**
 * Fetch lead sources from backend API
 *
 * GET /api/enums/lead-sources
 * Returns: [{ value: "MESSE", label: "Messe" }, ...]
 */
async function fetchLeadSources(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/lead-sources`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
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
