import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from '../features/leads/hooks/shared';

/**
 * Fetch expansion plans from backend API
 *
 * GET /api/enums/expansion-plan
 * Returns: [{ value: "KEINE", label: "Keine Expansion geplant" }, ...]
 */
async function fetchExpansionPlan(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/expansion-plan`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch expansion plan: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch expansion plan options for dropdowns
 *
 * Sprint 2.1.7.7 - Customer Management Field Architecture
 * - Values served from backend ExpansionPlan enum
 * - NO hardcoding in frontend
 * - Used in Customer Schema for "expansionPlan" field
 *
 * @returns Query result with expansion plan data
 */
export function useExpansionPlan() {
  return useQuery({
    queryKey: ['enums', 'expansionPlan'],
    queryFn: fetchExpansionPlan,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes (was cacheTime in v4)
  });
}
