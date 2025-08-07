/**
 * Validation Module Export
 *
 * Zentrale Exportstelle für alle Validation-Komponenten.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-validation.md
 */

// Base Schemas
export * from './baseSchemas';

// Entity Schemas
export * from './customerSchemas';
export * from './locationSchemas';

// Schema Builder
export { buildFieldSchema, buildFormSchema, validateField, validateFields } from './schemaBuilder';

// Cross-Field Validation
export {
  customerCrossFieldValidators,
  locationCrossFieldValidators,
  validateCustomerCrossFields,
  validateLocationCrossFields,
  type ValidationResult,
} from './crossFieldValidation';

// React Hook Form Integration
export {
  useFormValidation,
  useStepValidation,
  type UseFormValidationOptions,
  type UseFormValidationResult,
} from './useFormValidation';

// Re-export commonly used Zod types
export { z } from 'zod';
export type { ZodError, ZodSchema } from 'zod';
