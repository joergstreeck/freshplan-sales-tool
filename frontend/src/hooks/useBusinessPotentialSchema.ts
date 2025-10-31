/**
 * Business Potential Schema Hook
 *
 * Sprint 2.1.7.2 D11: Server-Driven UI for Business Potential Assessment
 *
 * Fetches business potential schema from backend API to enable dynamic form rendering.
 * Backend: GET /api/business-potentials/schema (BusinessPotentialSchemaResource.java)
 *
 * Schema Structure:
 * - cardId: "business_potential"
 * - sections: [potential_assessment] (1 section with 6 fields)
 * - fields: Field definitions with type, label, validation, etc.
 *
 * Fields (6):
 * - businessType (ENUM, required) - Geschäftsart
 * - kitchenSize (ENUM) - Küchengröße
 * - employeeCount (NUMBER) - Mitarbeiteranzahl
 * - estimatedVolume (CURRENCY) - Geschätztes Jahresvolumen
 * - branchCount (NUMBER) - Anzahl Filialen/Standorte
 * - isChain (BOOLEAN) - Kettenbetrieb
 *
 * Benefits:
 * - Backend controls Business Potential form structure (Single Source of Truth)
 * - Enum sources from backend (/api/enums/...)
 * - No frontend/backend parity issues
 * - BusinessPotentialDialog uses this schema for dynamic rendering
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';
import type { CardSchema } from './useContactSchema'; // Reuse types

/**
 * Fetch business potential schema from backend
 *
 * GET /api/business-potentials/schema
 * Returns: Array of CardSchema (currently 1 card with 1 section for Business Potential Assessment)
 */
async function fetchBusinessPotentialSchema(): Promise<CardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/business-potentials/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch business potential schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch Business Potential schema for dynamic form rendering
 *
 * Usage in BusinessPotentialDialog:
 * ```tsx
 * const { data: schemas, isLoading } = useBusinessPotentialSchema();
 * const businessPotentialSchema = schemas?.find(s => s.cardId === 'business_potential');
 * const assessmentSection = businessPotentialSchema?.sections
 *   .find(s => s.sectionId === 'potential_assessment');
 * const fields = assessmentSection?.fields; // All 6 fields for dynamic rendering
 * ```
 *
 * @returns Query result with business potential schema data
 */
export function useBusinessPotentialSchema() {
  return useQuery({
    queryKey: ['schema', 'business-potentials'],
    queryFn: fetchBusinessPotentialSchema,
    staleTime: 10 * 60 * 1000, // 10 minutes (schema rarely changes)
    gcTime: 30 * 60 * 1000, // 30 minutes
  });
}
