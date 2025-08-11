/**
 * Field Definitions Hook
 *
 * Loads and manages field definitions from the Field Catalog.
 * Filters fields based on entity type and industry.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
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
    } catch (err) {
      setError(err as Error);
      setIsLoading(false);
    }
  }, [setFieldDefinitions, catalog]);

  /**
   * Get industry-specific fields for a given industry
   */
  const getIndustryFields = (industry: string): FieldDefinition[] => {
    if (!industry) return [];

    const industryFields = catalog.customer.industrySpecific?.[industry] || [];

    // Also get industry-specific fields from extensions
    const hotelFields = industry === 'hotel' ? extensions.hotelServices || [] : [];
    const hospitalFields = industry === 'krankenhaus' ? extensions.hospitalServices || [] : [];

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
