/**
 * Field Definitions Hook
 *
 * @deprecated Sprint 2.1.7.2 D11 - REPLACED by useCustomerSchema Hook
 *
 * This hook uses client-side fieldCatalog.json which is being phased out
 * in favor of server-driven schema from /api/customers/schema.
 *
 * Migration Path:
 * - Wizard Steps 1-2: Now use useCustomerSchema (server-driven)
 * - Detail Tabs: Already use useCustomerSchema
 * - This hook: Will be removed in future sprint
 *
 * DO NOT USE in new code! Use useCustomerSchema instead.
 *
 * Loads and manages field definitions from the Field Catalog.
 * Filters fields based on entity type and industry.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/hooks/useCustomerSchema.ts (NEW!)
 */

import { useEffect, useState, useMemo } from 'react';
import type { FieldDefinition, FieldCatalog } from '../types/field.types';
import { useCustomerOnboardingStore } from '../stores/customerOnboardingStore';
import fieldCatalog from '../data/fieldCatalog.json';
import fieldCatalogExtensions from '../data/fieldCatalogExtensions.json';

interface UseFieldDefinitionsResult {
  /** Customer base fields */
  customerFields: FieldDefinition[];
  /** Location base fields */
  locationFields: FieldDefinition[];
  /** Get industry-specific fields */
  getIndustryFields: (industry: string) => FieldDefinition[];
  /** Get field by key */
  getFieldByKey: (key: string) => FieldDefinition | undefined;
  /** All available fields */
  allFields: FieldDefinition[];
  /** All field definitions for debug */
  fieldDefinitions: FieldDefinition[];
  /** Loading state */
  isLoading: boolean;
  /** Error state */
  error: Error | null;
}

/**
 * Hook to load and manage field definitions
 *
 * Loads the field catalog and provides filtered field definitions
 * based on entity type and industry selection.
 */
export const useFieldDefinitions = (): UseFieldDefinitionsResult => {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const { setFieldDefinitions } = useCustomerOnboardingStore();

  // Type the imported JSON
  const catalog = fieldCatalog as unknown as FieldCatalog;
  const extensions = fieldCatalogExtensions as unknown as Record<string, FieldDefinition[]>;

  // Merge all fields from extensions
  const allExtensionFields = useMemo(() => {
    const fields: FieldDefinition[] = [];
    Object.values(extensions).forEach(categoryFields => {
      if (Array.isArray(categoryFields)) {
        fields.push(...categoryFields);
      }
    });
    return fields;
  }, [extensions]);

  // Create a map for quick field lookup
  const fieldMap = useMemo(() => {
    const map = new Map<string, FieldDefinition>();

    // Add base fields
    catalog.customer.base?.forEach(field => map.set(field.key, field));
    catalog.location?.base?.forEach(field => map.set(field.key, field));

    // Add industry-specific fields
    if (catalog.customer.industrySpecific) {
      Object.values(catalog.customer.industrySpecific).forEach(fields => {
        fields.forEach(field => map.set(field.key, field));
      });
    }

    // Add extension fields
    allExtensionFields.forEach(field => map.set(field.key, field));

    return map;
  }, [catalog, allExtensionFields]);

  useEffect(() => {
    try {
      // Load base fields
      const customerFields = catalog.customer.base || [];
      const locationFields = catalog.location?.base || [];

      // Set fields in store
      setFieldDefinitions(customerFields, locationFields);

      setIsLoading(false);
    } catch (_err) {
      void _err;
      setError(err as Error);
      setIsLoading(false);
    }
  }, [setFieldDefinitions, catalog]);

  /**
   * Get industry-specific fields for a given industry
   *
   * @param industry BusinessType value (uppercase) or legacy industry value (lowercase)
   * @since 2.1.6 - Updated to handle both uppercase BusinessType and lowercase legacy values
   */
  const getIndustryFields = (industry: string): FieldDefinition[] => {
    if (!industry) return [];

    // Normalize to uppercase for BusinessType comparison (Sprint 2.1.6 Phase 2)
    const normalizedIndustry = industry.toUpperCase();

    // Try uppercase first (new BusinessType), fallback to lowercase (legacy)
    const industryFields =
      catalog.customer.industrySpecific?.[normalizedIndustry] ??
      catalog.customer.industrySpecific?.[normalizedIndustry.toLowerCase()] ??
      [];

    // Also get industry-specific fields from extensions (case-insensitive matching)
    const hotelFields = normalizedIndustry === 'HOTEL' ? extensions.hotelServices || [] : [];
    const hospitalFields =
      normalizedIndustry === 'KRANKENHAUS' || normalizedIndustry === 'GESUNDHEIT'
        ? extensions.hospitalServices || []
        : [];

    return [...industryFields, ...hotelFields, ...hospitalFields];
  };

  /**
   * Get field by key
   */
  const getFieldByKey = (key: string): FieldDefinition | undefined => {
    return fieldMap.get(key);
  };

  return {
    customerFields: catalog.customer.base || [],
    locationFields: catalog.location?.base || [],
    getIndustryFields,
    getFieldByKey,
    allFields: Array.from(fieldMap.values()),
    fieldDefinitions: Array.from(fieldMap.values()),
    isLoading,
    error,
  };
};
