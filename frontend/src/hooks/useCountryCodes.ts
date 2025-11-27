import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from '../features/leads/hooks/shared';

/**
 * Fetch country codes from backend API
 *
 * GET /api/enums/country-codes
 * Returns: [{ value: "DE", label: "Deutschland" }, ...]
 */
async function fetchCountryCodes(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/country-codes`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch country codes: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch country codes for dropdowns
 *
 * Sprint 2.1.7.7 - Customer Management Field Architecture
 * - Values served from backend CountryCode enum
 * - NO hardcoding in frontend
 * - Used in Customer Schema for "countryCode" fields
 *
 * @returns Query result with country codes data
 */
export function useCountryCodes() {
  return useQuery({
    queryKey: ['enums', 'countryCodes'],
    queryFn: fetchCountryCodes,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes (was cacheTime in v4)
  });
}
