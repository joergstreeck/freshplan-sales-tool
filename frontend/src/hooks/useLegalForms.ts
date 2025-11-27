import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from '../features/leads/hooks/shared';

/**
 * Fetch legal forms from backend API
 *
 * GET /api/enums/legal-forms
 * Returns: [{ value: "GMBH", label: "GmbH" }, ...]
 */
async function fetchLegalForms(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/legal-forms`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch legal forms: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch legal forms for dropdowns
 *
 * Sprint 2.1.7.7 - Customer Management Field Architecture
 * - Values served from backend LegalForm enum
 * - NO hardcoding in frontend
 * - Used in Customer Schema for "legalForm" field
 *
 * @returns Query result with legal forms data
 */
export function useLegalForms() {
  return useQuery({
    queryKey: ['enums', 'legalForms'],
    queryFn: fetchLegalForms,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes (was cacheTime in v4)
  });
}
