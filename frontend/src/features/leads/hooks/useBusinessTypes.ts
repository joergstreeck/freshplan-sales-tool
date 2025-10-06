import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from './shared';

/**
 * Fetch business types from backend API
 *
 * GET /api/enums/business-types
 * Returns: [{ value: "RESTAURANT", label: "Restaurant" }, ...]
 */
async function fetchBusinessTypes(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/business-types`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
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
    gcTime: 10 * 60 * 1000, // 10 minutes (was cacheTime in v4)
  });
}
