/**
 * Lead Schema Hook
 *
 * Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Forms (Progressive Profiling)
 *
 * Fetches lead schema from backend API to enable dynamic form rendering.
 * Backend: GET /api/leads/schema (LeadSchemaResource.java)
 *
 * Schema Structure:
 * - cardId: "lead_progressive_profiling"
 * - sections: [stage_0_pre_claim, stage_1_vollschutz, stage_2_nurturing]
 * - fields: Field definitions with type, label, validation, etc.
 *
 * Progressive Profiling Stages:
 * - Stage 0: Pre-Claim (10 Tage Schutz) - 8 Felder
 * - Stage 1: Vollschutz (6 Monate Schutz) - 6 Felder
 * - Stage 2: Nurturing & Qualifikation - 13 Felder
 *
 * Benefits:
 * - Backend controls Lead form structure (Single Source of Truth)
 * - Progressive Profiling stages defined in backend
 * - Enum sources from backend (/api/enums/...)
 * - No frontend/backend parity issues
 * - LeadWizard + LeadEditDialog use SAME schema
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';
import type { CardSchema } from './useContactSchema'; // Reuse types

/**
 * Fetch lead schema from backend
 *
 * GET /api/leads/schema
 * Returns: Array of CardSchema (currently 1 card with 3 sections for Progressive Profiling)
 */
async function fetchLeadSchema(): Promise<CardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/leads/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch lead schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch Lead schema for dynamic form rendering
 *
 * Usage in LeadWizard (Progressive Profiling):
 * ```tsx
 * const { data: schemas, isLoading } = useLeadSchema();
 * const progressiveSchema = schemas?.find(s => s.cardId === 'lead_progressive_profiling');
 * const stage0Section = progressiveSchema?.sections
 *   .find(s => s.sectionId === 'stage_0_pre_claim');
 * const stage0Fields = stage0Section?.fields;
 * ```
 *
 * Usage in LeadEditDialog:
 * ```tsx
 * const { data: schemas, isLoading } = useLeadSchema();
 * const progressiveSchema = schemas?.[0];
 * // Render all sections dynamically or filter by specific stage
 * ```
 *
 * @returns Query result with lead schema data
 */
export function useLeadSchema() {
  return useQuery({
    queryKey: ['schema', 'leads'],
    queryFn: fetchLeadSchema,
    staleTime: 10 * 60 * 1000, // 10 minutes (schema rarely changes)
    gcTime: 30 * 60 * 1000, // 30 minutes
  });
}
