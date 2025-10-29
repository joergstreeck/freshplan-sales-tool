/**
 * Score Schema Hook
 *
 * Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Scoring (Pain, Revenue, Engagement)
 *
 * Fetches score schemas from backend API to enable dynamic form rendering.
 * Backend: GET /api/scores/schema (ScoreSchemaResource.java)
 *
 * Schema Structure:
 * - 3 separate CardSchemas returned:
 *   1. pain_score - Schmerzpunkte-Bewertung (8 pain points + notes)
 *   2. revenue_score - Umsatzpotenzial-Bewertung
 *   3. engagement_score - Engagement-Level-Bewertung
 *
 * Benefits:
 * - Backend controls Score form structures (Single Source of Truth)
 * - Enum sources from backend (/api/enums/pain-intensity, etc.)
 * - No frontend/backend parity issues
 * - 3 separate scoring forms for comprehensive Lead qualification
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';
import type { CardSchema } from './useContactSchema'; // Reuse types

/**
 * Fetch score schemas from backend
 *
 * GET /api/scores/schema
 * Returns: Array of 3 CardSchemas (pain_score, revenue_score, engagement_score)
 */
async function fetchScoreSchemas(): Promise<CardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/scores/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch score schemas: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch Score schemas for dynamic scoring form rendering
 *
 * Usage in PainScoreForm:
 * ```tsx
 * const { data: schemas, isLoading } = useScoreSchema();
 * const painSchema = schemas?.find(s => s.cardId === 'pain_score');
 * const painSection = painSchema?.sections[0];
 * const painFields = painSection?.fields;
 * ```
 *
 * Usage in RevenueScoreForm:
 * ```tsx
 * const { data: schemas, isLoading } = useScoreSchema();
 * const revenueSchema = schemas?.find(s => s.cardId === 'revenue_score');
 * ```
 *
 * Usage in EngagementScoreForm:
 * ```tsx
 * const { data: schemas, isLoading } = useScoreSchema();
 * const engagementSchema = schemas?.find(s => s.cardId === 'engagement_score');
 * ```
 *
 * @returns Query result with score schema data (3 CardSchemas)
 */
export function useScoreSchema() {
  return useQuery({
    queryKey: ['schema', 'scores'],
    queryFn: fetchScoreSchemas,
    staleTime: 10 * 60 * 1000, // 10 minutes (schema rarely changes)
    gcTime: 30 * 60 * 1000, // 30 minutes
  });
}
