import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from './shared';

/**
 * Fetch kitchen sizes from backend API
 *
 * GET /api/enums/kitchen-sizes
 * Returns: [{ value: "small", label: "Klein" }, ...]
 */
async function fetchKitchenSizes(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/kitchen-sizes`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
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
