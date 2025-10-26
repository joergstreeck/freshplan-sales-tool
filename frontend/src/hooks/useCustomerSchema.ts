import { useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';
import type { CustomerCardSchema, FieldDefinition } from '../types/customer-schema';

/**
 * Fetch customer card schema from backend API
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards + Wizard
 *
 * GET /api/customers/schema
 * Returns: Array of 7 CustomerCardSchema objects defining all customer cards
 *
 * This is the **Single Source of Truth** for customer UI structure.
 * Backend defines:
 * - Which fields exist
 * - Field types and validation
 * - Section grouping
 * - Display order
 * - Wizard metadata (showInWizard, wizardStep, wizardOrder)
 *
 * Frontend just renders what backend defines → No more fieldCatalog.json!
 */
async function fetchCustomerSchema(): Promise<CustomerCardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/customers/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch customer schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch customer card schema with Wizard support
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards + Wizard
 *
 * Usage (Wizard):
 * ```tsx
 * const { getWizardFields, isLoading } = useCustomerSchema();
 *
 * if (isLoading) return <CircularProgress />;
 *
 * const step1Fields = getWizardFields(1); // Get fields for Step 1
 * return <Step1BasisFilialstruktur fields={step1Fields} />;
 * ```
 *
 * Usage (Detail Tabs):
 * ```tsx
 * const { data: cardSchemas, getCardFields } = useCustomerSchema();
 *
 * const companyCard = getCardFields('company_profile');
 * return <DynamicCustomerCard schema={companyCard} customerId={id} />;
 * ```
 *
 * @returns Query result + helper methods for Wizard and Detail-Tabs
 */
export function useCustomerSchema() {
  const query = useQuery({
    queryKey: ['customers', 'schema'],
    queryFn: fetchCustomerSchema,
    staleTime: 30 * 60 * 1000, // 30 minutes (schema changes rarely)
    gcTime: 60 * 60 * 1000, // 60 minutes (was cacheTime in v4)
  });

  /**
   * Flatten hierarchical schema to a flat list of all fields
   *
   * Cards → Sections → Fields becomes FieldDefinition[]
   */
  const allFields = useMemo(() => {
    if (!query.data) return [];

    const fields: FieldDefinition[] = [];
    for (const card of query.data) {
      for (const section of card.sections) {
        fields.push(...section.fields);
      }
    }
    return fields;
  }, [query.data]);

  /**
   * Get fields for a specific wizard step
   *
   * Filters fields by:
   * - showInWizard = true
   * - wizardStep = step
   *
   * Sorted by wizardOrder (ascending)
   *
   * @param step Wizard step number (1-4)
   * @returns Array of fields for this step, sorted by wizardOrder
   */
  const getWizardFields = useMemo(
    () => (step: number): FieldDefinition[] => {
      return allFields
        .filter((f) => f.showInWizard === true && f.wizardStep === step)
        .sort((a, b) => (a.wizardOrder ?? 0) - (b.wizardOrder ?? 0));
    },
    [allFields]
  );

  /**
   * Get card schema by cardId
   *
   * Returns the full hierarchical card with sections
   *
   * @param cardId Card identifier (e.g. 'company_profile')
   * @returns Card schema or undefined
   */
  const getCardFields = useMemo(
    () =>
      (cardId: string): CustomerCardSchema | undefined => {
        return query.data?.find((card) => card.cardId === cardId);
      },
    [query.data]
  );

  /**
   * Get single field by fieldKey
   *
   * @param key Field key (e.g. 'companyName')
   * @returns Field definition or undefined
   */
  const getFieldByKey = useMemo(
    () =>
      (key: string): FieldDefinition | undefined => {
        return allFields.find((f) => f.fieldKey === key);
      },
    [allFields]
  );

  return {
    // React Query standard properties
    ...query,

    // Helper methods
    getWizardFields,
    getCardFields,
    getFieldByKey,
    allFields,
  };
}
