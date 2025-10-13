import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from '../features/leads/hooks/shared';

/**
 * Fetch activity outcomes from backend API
 *
 * GET /api/enums/activity-outcomes
 * Returns: [{ value: "SUCCESSFUL", label: "Erfolgreich" }, ...]
 */
async function fetchActivityOutcomes(): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}/api/enums/activity-outcomes`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch activity outcomes: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch activity outcomes for dropdowns
 *
 * Sprint 2.1.7 - Issue #126: ActivityOutcome Enum Integration
 * - 7 standardized values: SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, CALLBACK_REQUESTED, INFO_SENT, QUALIFIED, DISQUALIFIED
 * - NO hardcoding in frontend
 * - Values served from backend ActivityOutcome enum
 *
 * Used for:
 * - ActivityDialog: Track activity success/outcome
 * - LeadActivityForm: Document activity results
 * - Activity Timeline: Display activity outcomes with color coding
 *
 * @returns Query result with activity outcomes data
 */
export function useActivityOutcomes() {
  return useQuery({
    queryKey: ['enums', 'activityOutcomes'],
    queryFn: fetchActivityOutcomes,
    staleTime: 5 * 60 * 1000, // 5 minutes (enum values rarely change)
    gcTime: 10 * 60 * 1000, // 10 minutes (was cacheTime in v4)
  });
}
