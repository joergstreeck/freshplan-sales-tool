/**
 * Activity Schema Hook
 *
 * Sprint 2.1.7.2 D11: Server-Driven UI for Activity Management
 *
 * Fetches activity schema from backend API to enable dynamic form rendering.
 * Backend: GET /api/activities/schema (ActivitySchemaResource.java)
 *
 * Schema Structure:
 * - cardId: "activity"
 * - sections: [activity_details] (1 section with 3 fields)
 * - fields: Field definitions with type, label, validation, etc.
 *
 * Fields (3 - Activity Logging):
 * - activityType (ENUM, required) - Aktivitätstyp (CALL, EMAIL, MEETING, NOTE, etc.)
 * - description (TEXTAREA, required) - Beschreibung der Aktivität
 * - outcome (ENUM, optional) - Ergebnis (SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, etc.)
 *
 * Benefits:
 * - Backend controls Activity form structure (Single Source of Truth)
 * - Enum sources from backend (/api/enums/...)
 * - No frontend/backend parity issues
 * - ActivityDialog uses this schema for dynamic rendering
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';
import type { CardSchema } from './useContactSchema'; // Reuse types

/**
 * Fetch activity schema from backend
 *
 * GET /api/activities/schema
 * Returns: Array of CardSchema (currently 1 card with 1 section for Activity Logging)
 */
async function fetchActivitySchema(): Promise<CardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/activities/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch activity schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch Activity schema for dynamic form rendering
 *
 * Usage in ActivityDialog:
 * ```tsx
 * const { data: schemas, isLoading } = useActivitySchema();
 * const activitySchema = schemas?.find(s => s.cardId === 'activity');
 * const detailsSection = activitySchema?.sections
 *   .find(s => s.sectionId === 'activity_details');
 * const fields = detailsSection?.fields; // All 3 fields for dynamic rendering
 * ```
 *
 * @returns Query result with activity schema data
 */
export function useActivitySchema() {
  return useQuery({
    queryKey: ['schema', 'activities'],
    queryFn: fetchActivitySchema,
    staleTime: 10 * 60 * 1000, // 10 minutes (schema rarely changes)
    gcTime: 30 * 60 * 1000, // 30 minutes
  });
}
