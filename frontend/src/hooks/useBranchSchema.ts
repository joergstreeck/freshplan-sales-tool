/**
 * useBranchSchema Hook
 *
 * Sprint 2.1.7.7: Server-Driven Branch Creation Schema
 *
 * Fetches schema for CreateBranchDialog from backend.
 * Backend is Single Source of Truth for field definitions.
 *
 * @see BranchSchemaResource.java
 */

import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';

/**
 * Section from backend schema
 */
interface SchemaSection {
  sectionId: string;
  title: string;
  subtitle?: string;
  fields: SchemaField[];
}

/**
 * Field from backend schema
 */
interface SchemaField {
  fieldKey: string;
  label: string;
  type: string;
  required?: boolean;
  gridCols?: number;
  placeholder?: string;
  helpText?: string;
  enumSource?: string;
  rows?: number;
}

/**
 * Card schema from backend
 */
interface CardSchema {
  cardId: string;
  title: string;
  subtitle?: string;
  icon?: string;
  sections: SchemaSection[];
}

/**
 * Fetch branch schema from backend
 *
 * GET /api/branches/schema
 * Returns: Array of CardSchema (1 card with 2 sections)
 */
async function fetchBranchSchema(): Promise<CardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/branches/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch branch schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch Branch creation schema from backend
 *
 * Usage:
 * ```tsx
 * const { data: schemas, isLoading } = useBranchSchema();
 * const branchSchema = schemas?.[0];
 * const basicInfoSection = branchSchema?.sections.find(s => s.sectionId === 'basic_info');
 * ```
 *
 * @returns React Query result with schema data
 */
export const useBranchSchema = () => {
  return useQuery<CardSchema[]>({
    queryKey: ['schema', 'branches'],
    queryFn: fetchBranchSchema,
    staleTime: 10 * 60 * 1000, // 10 minutes (schema rarely changes)
    gcTime: 30 * 60 * 1000, // 30 minutes
  });
};

export type { CardSchema, SchemaSection, SchemaField };
