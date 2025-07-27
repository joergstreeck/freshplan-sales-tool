/**
 * Field Definitions Hook
 * 
 * Loads and manages field definitions from the Field Catalog.
 * Filters fields based on entity type and industry.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 */

import { useEffect, useState } from 'react';
import type { FieldDefinition, FieldCatalog } from '../types/field.types';
import { useCustomerOnboardingStore } from '../stores/customerOnboardingStore';
import fieldCatalog from '../data/fieldCatalog.json';

interface UseFieldDefinitionsResult {
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
  }, [setFieldDefinitions]);
  
  /**
   * Get industry-specific fields for a given industry
   */
  const getIndustryFields = (industry: string): FieldDefinition[] => {
    if (!industry) return [];
    
    const industryFields = catalog.customer.industrySpecific?.[industry] || [];
    return industryFields;
  };
  
  return {
    customerFields: catalog.customer.base || [],
    locationFields: catalog.location?.base || [],
    getIndustryFields,
    isLoading,
    error
  };
};