/**
 * Form Validation Hook
 * 
 * React Hook Form Integration mit Zod-Schemas.
 * Bietet typsichere Form-Validierung mit Field Definitions.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-validation.md
 */

import { useForm } from 'react-hook-form';
import type { UseFormProps, FieldValues, Path } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useCallback, useEffect, useMemo } from 'react';
import type { FieldDefinition, EntityType } from '../types/field.types';
import { useCustomerOnboardingStore } from '../stores/customerOnboardingStore';
import { buildFormSchema, validateField } from './schemaBuilder';
import { getCustomerSchema, getCustomerDraftSchema } from './customerSchemas';
import { getLocationSchema } from './locationSchemas';
import { debounce } from '../utils/debounce';

interface UseFormValidationOptions<T extends FieldValues = FieldValues> {
  /** Field definitions for dynamic schema */
  fields?: FieldDefinition[];
  /** Entity type for predefined schemas */
  entityType?: EntityType;
  /** Industry for entity-specific schemas */
  industry?: string;
  /** Enable draft mode (partial validation) */
  isDraft?: boolean;
  /** React Hook Form options */
  formOptions?: UseFormProps<T>;
  /** Enable real-time validation */
  realtimeValidation?: boolean;
  /** Debounce delay for real-time validation */
  validationDelay?: number;
}

interface UseFormValidationResult<T extends FieldValues = FieldValues> {
  /** React Hook Form instance */
  form: ReturnType<typeof useForm<T>>;
  /** Validate single field */
  validateField: (fieldName: Path<T>, value: any) => Promise<boolean>;
  /** Validate all fields */
  validateAll: () => Promise<boolean>;
  /** Clear validation errors */
  clearErrors: (fieldName?: Path<T>) => void;
  /** Check if form is valid */
  isValid: boolean;
  /** Current validation errors */
  errors: Record<string, string>;
}

/**
 * Hook for form validation with dynamic schemas
 */
export function useFormValidation<T extends FieldValues = FieldValues>(
  options: UseFormValidationOptions<T> = {}
): UseFormValidationResult<T> {
  const {
    fields,
    entityType,
    industry,
    isDraft = false,
    formOptions = {},
    realtimeValidation = true,
    validationDelay = 300
  } = options;
  
  const {
    validationErrors,
    setValidationError,
    clearValidationError,
    clearAllValidationErrors
  } = useCustomerOnboardingStore();
  
  // Build validation schema
  const schema = useMemo(() => {
    // If fields are provided, build dynamic schema
    if (fields && fields.length > 0) {
      return buildFormSchema(fields, { partial: isDraft });
    }
    
    // Otherwise use predefined schemas based on entity type
    switch (entityType) {
      case EntityType.CUSTOMER:
        return isDraft 
          ? getCustomerDraftSchema(industry)
          : getCustomerSchema(industry);
      case EntityType.LOCATION:
        return getLocationSchema(industry);
      default:
        return z.object({});
    }
  }, [fields, entityType, industry, isDraft]);
  
  // Initialize React Hook Form
  const form = useForm<T>({
    ...formOptions,
    resolver: zodResolver(schema),
    mode: realtimeValidation ? 'onChange' : 'onSubmit',
    reValidateMode: 'onChange',
    criteriaMode: 'all'
  });
  
  const {
    formState: { errors: formErrors, isValid: formIsValid },
    setError,
    clearErrors: clearFormErrors,
    trigger
  } = form;
  
  // Sync form errors with store
  useEffect(() => {
    // Convert form errors to store format
    Object.entries(formErrors).forEach(([field, error]) => {
      if (error?.message) {
        setValidationError(field, error.message);
      }
    });
    
    // Clear errors that are no longer present
    Array.from(validationErrors.keys()).forEach(field => {
      if (!formErrors[field]) {
        clearValidationError(field);
      }
    });
  }, [formErrors, setValidationError, clearValidationError, validationErrors]);
  
  /**
   * Validate single field with debouncing
   */
  const validateFieldImmediate = useCallback(
    async (fieldName: Path<T>, value: any): Promise<boolean> => {
      // Find field definition
      const fieldDef = fields?.find(f => f.key === fieldName);
      if (!fieldDef) {
        // Use React Hook Form validation
        const result = await trigger(fieldName);
        return result;
      }
      
      // Use field-level validation
      const validationResult = await validateField(fieldDef, value);
      
      if (validationResult.isValid) {
        clearFormErrors(fieldName);
        clearValidationError(fieldName as string);
        return true;
      } else {
        setError(fieldName, {
          type: 'manual',
          message: validationResult.error
        });
        setValidationError(fieldName as string, validationResult.error!);
        return false;
      }
    },
    [fields, trigger, setError, clearFormErrors, setValidationError, clearValidationError]
  );
  
  // Debounced version for real-time validation
  const validateFieldDebounced = useMemo(
    () => debounce(validateFieldImmediate, validationDelay),
    [validateFieldImmediate, validationDelay]
  );
  
  const validateField = realtimeValidation 
    ? validateFieldDebounced 
    : validateFieldImmediate;
  
  /**
   * Validate all fields
   */
  const validateAll = useCallback(async (): Promise<boolean> => {
    const result = await trigger();
    return result;
  }, [trigger]);
  
  /**
   * Clear errors
   */
  const clearErrors = useCallback((fieldName?: Path<T>) => {
    if (fieldName) {
      clearFormErrors(fieldName);
      clearValidationError(fieldName as string);
    } else {
      clearFormErrors();
      clearAllValidationErrors();
    }
  }, [clearFormErrors, clearValidationError, clearAllValidationErrors]);
  
  /**
   * Get current errors as simple object
   */
  const errors = useMemo(() => {
    const errorMap: Record<string, string> = {};
    
    // Add form errors
    Object.entries(formErrors).forEach(([field, error]) => {
      if (error?.message) {
        errorMap[field] = error.message;
      }
    });
    
    // Add store errors
    validationErrors.forEach((error, field) => {
      if (!errorMap[field]) {
        errorMap[field] = error;
      }
    });
    
    return errorMap;
  }, [formErrors, validationErrors]);
  
  return {
    form,
    validateField,
    validateAll,
    clearErrors,
    isValid: formIsValid && validationErrors.size === 0,
    errors
  };
}

/**
 * Hook for step validation in multi-step forms
 */
export function useStepValidation(currentStep: number) {
  const { customerData, locations, detailedLocations } = useCustomerOnboardingStore();
  
  /**
   * Validate current step
   */
  const validateStep = useCallback(async (): Promise<{
    isValid: boolean;
    errors: Map<string, string>;
  }> => {
    const errors = new Map<string, string>();
    
    switch (currentStep) {
      case 0: // Customer data
        // Required fields
        if (!customerData.companyName) {
          errors.set('companyName', 'Firmenname ist erforderlich');
        }
        if (!customerData.industry) {
          errors.set('industry', 'Branche ist erforderlich');
        }
        if (!customerData.chainCustomer) {
          errors.set('chainCustomer', 'Bitte wählen Sie ob es sich um einen Ketten-Kunden handelt');
        }
        break;
        
      case 1: // Locations
        if (locations.length === 0) {
          errors.set('locations', 'Mindestens ein Standort ist erforderlich');
        }
        
        // Validate each location
        locations.forEach((location, index) => {
          if (!location.name) {
            errors.set(`locations.${index}.name`, 'Standortname ist erforderlich');
          }
          if (!location.locationType) {
            errors.set(`locations.${index}.locationType`, 'Standorttyp ist erforderlich');
          }
        });
        break;
        
      case 2: // Detailed locations
        // Optional step, no required validation
        break;
    }
    
    return {
      isValid: errors.size === 0,
      errors
    };
  }, [currentStep, customerData, locations, detailedLocations]);
  
  /**
   * Check if can proceed to next step
   */
  const canProceed = useCallback(async (): Promise<boolean> => {
    const { isValid } = await validateStep();
    return isValid;
  }, [validateStep]);
  
  return {
    validateStep,
    canProceed
  };
}