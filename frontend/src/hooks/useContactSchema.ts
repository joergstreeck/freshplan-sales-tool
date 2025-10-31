/**
 * Contact Schema Hook
 *
 * Sprint 2.1.7.2 D11.1: Server-Driven UI for Contact Forms
 *
 * Fetches contact schema from backend API to enable dynamic form rendering.
 * Backend: GET /api/contacts/schema (ContactSchemaResource.java)
 *
 * Schema Structure:
 * - cardId: "contact_details"
 * - sections: [basic_info, relationship, social_business]
 * - fields: Field definitions with type, label, validation, etc.
 *
 * Benefits:
 * - Backend controls form structure (Single Source of Truth)
 * - V2 fields (LinkedIn, XING, Notes) automatically included
 * - No hardcoded field definitions in frontend
 * - Easy to add/remove fields without frontend changes
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';

/**
 * Field Types supported by backend schema
 */
export type FieldType =
  | 'TEXT'
  | 'TEXTAREA'
  | 'NUMBER'
  | 'BOOLEAN'
  | 'DATE'
  | 'ENUM'
  | 'CURRENCY'
  | 'GROUP'
  | 'ARRAY';

/**
 * Field Definition from backend schema
 */
export interface FieldDefinition {
  fieldKey: string;
  label: string;
  type: FieldType;
  required?: boolean;
  readonly?: boolean;
  enumSource?: string; // e.g. "/api/enums/contact-salutations"
  placeholder?: string;
  helpText?: string;
  gridCols?: number;
  validationRules?: string[];
  fields?: FieldDefinition[]; // For GROUP type
  itemSchema?: FieldDefinition; // For ARRAY type
  showInWizard?: boolean;
  wizardStep?: number;
  wizardOrder?: number;
  wizardSectionId?: string;
  wizardSectionTitle?: string;
  showDividerAfter?: boolean;
}

/**
 * Card Section from backend schema
 */
export interface CardSection {
  sectionId: string;
  title: string;
  subtitle?: string;
  fields: FieldDefinition[];
  collapsible?: boolean;
  defaultCollapsed?: boolean;
}

/**
 * Card Schema from backend
 */
export interface CardSchema {
  cardId: string;
  title: string;
  subtitle?: string;
  icon?: string;
  order?: number;
  sections: CardSection[];
  defaultCollapsed?: boolean;
}

/**
 * Fetch contact schema from backend
 *
 * GET /api/contacts/schema
 * Returns: Array of CardSchema (currently 1 card with 3 sections)
 */
async function fetchContactSchema(): Promise<CardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/contacts/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch contact schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch contact schema for dynamic form rendering
 *
 * Usage:
 * ```tsx
 * const { data: schemas, isLoading } = useContactSchema();
 * const contactSchema = schemas?.[0]; // First (and only) card
 * const basicInfoSection = contactSchema?.sections.find(s => s.sectionId === 'basic_info');
 * ```
 *
 * @returns Query result with contact schema data
 */
export function useContactSchema() {
  return useQuery({
    queryKey: ['schema', 'contacts'],
    queryFn: fetchContactSchema,
    staleTime: 10 * 60 * 1000, // 10 minutes (schema rarely changes)
    gcTime: 30 * 60 * 1000, // 30 minutes
  });
}
