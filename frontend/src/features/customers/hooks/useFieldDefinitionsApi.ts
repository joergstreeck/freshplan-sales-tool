/**
 * Field Definitions API Hook
 *
 * Erweiterte Version des Field Definitions Hooks, die Field Definitions
 * vom Backend lädt. Fällt auf lokale Daten zurück wenn API nicht verfügbar.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-api-integration.md
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 */

import { useEffect } from 'react';
import { useFieldDefinitions as useFieldDefinitionsQuery } from '../services';
import { useCustomerOnboardingStore } from '../stores/customerOnboardingStore';
import type { FieldDefinition, FieldCatalog } from '../types/field.types';
import { EntityType } from '../types/field.types';
import fieldCatalog from '../data/fieldCatalog.json';

interface UseFieldDefinitionsApiResult {
  /** Customer base fields */
  customerFields: FieldDefinition[];
  /** Location base fields */
  locationFields: FieldDefinition[];
  /** Get industry-specific fields */
  getIndustryFields: (industry: string) => FieldDefinition[];
  /** Loading state */
  isLoading: boolean;
  /** Error state */
  error: Error | null;
  /** Whether using API or local data */
  source: 'api' | 'local';
}

/**
 * Hook to load field definitions from API with local fallback
 *
 * First tries to load from API for real-time updates, falls back
 * to local JSON catalog if API is unavailable.
 */
export const useFieldDefinitionsApi = (): UseFieldDefinitionsApiResult => {
  const { setFieldDefinitions, customerData } = useCustomerOnboardingStore();

  // Type the imported JSON
  const localCatalog = fieldCatalog as unknown as FieldCatalog;

  // Get current industry from customer data
  const currentIndustry = customerData.industry as string | undefined;

  // Load customer fields from API
  const {
    data: apiCustomerFields,
    isLoading: isLoadingCustomer,
    error: customerError,
  } = useFieldDefinitionsQuery(EntityType.CUSTOMER, currentIndustry);

  // Load location fields from API
  const {
    data: apiLocationFields,
    isLoading: isLoadingLocation,
    error: locationError,
  } = useFieldDefinitionsQuery(EntityType.LOCATION);

  // Update store when API data is loaded
  useEffect(() => {
    if (apiCustomerFields && apiLocationFields) {
      setFieldDefinitions(apiCustomerFields, apiLocationFields);
    }
  }, [apiCustomerFields, apiLocationFields, setFieldDefinitions]);

  // Determine source and fields
  const hasApiData = !!(apiCustomerFields && apiLocationFields);
  const hasApiError = !!(customerError || locationError);
  const isLoading = isLoadingCustomer || isLoadingLocation;

  // Use API data if available, otherwise fall back to local
  const customerFields = apiCustomerFields || localCatalog.customer.base || [];
  const locationFields = apiLocationFields || localCatalog.location?.base || [];
  const source: 'api' | 'local' = hasApiData ? 'api' : 'local';

  /**
   * Get industry-specific fields
   * Merges API and local data for completeness
   */
  const getIndustryFields = (industry: string): FieldDefinition[] => {
    if (!industry) return [];

    // If we have API data with industry fields, use those
    if (apiCustomerFields && currentIndustry === industry) {
      // API already returns merged base + industry fields
      return apiCustomerFields.filter(field => field.metadata?.industry === industry);
    }

    // Fall back to local catalog
    const localIndustryFields = localCatalog.customer.industrySpecific?.[industry] || [];
    return localIndustryFields;
  };

  return {
    customerFields,
    locationFields,
    getIndustryFields,
    isLoading,
    error: hasApiError ? ((customerError || locationError) as Error) : null,
    source,
  };
};
