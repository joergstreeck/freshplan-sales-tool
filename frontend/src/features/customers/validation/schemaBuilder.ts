/**
 * Dynamic Schema Builder
 * 
 * Generiert Zod-Schemas dynamisch basierend auf Field Definitions.
 * Ermöglicht flexible Validierung basierend auf Field Catalog.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/02-field-system.md
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 */

import { z } from 'zod';
import { FieldDefinition, FieldType } from '../types/field.types';
import {
  emailSchema,
  germanPhoneSchema,
  germanPostalCodeSchema,
  urlSchema,
  textFieldSchema,
  numberFieldSchema,
  dateStringSchema,
  multiSelectSchema
} from './baseSchemas';

/**
 * Custom Validation Function Type
 */
type CustomValidator = (value: any) => boolean | Promise<boolean>;

/**
 * Validation Error Messages
 */
const DEFAULT_MESSAGES = {
  required: 'Dieses Feld ist erforderlich',
  minLength: (min: number) => `Mindestens ${min} Zeichen erforderlich`,
  maxLength: (max: number) => `Maximal ${max} Zeichen erlaubt`,
  min: (min: number) => `Wert muss mindestens ${min} sein`,
  max: (max: number) => `Wert darf maximal ${max} sein`,
  pattern: 'Ungültiges Format',
  email: 'Ungültige E-Mail-Adresse',
  phone: 'Ungültige Telefonnummer',
  url: 'Ungültige URL',
  date: 'Ungültiges Datum',
  number: 'Muss eine Zahl sein',
  integer: 'Muss eine ganze Zahl sein',
  enum: 'Ungültige Auswahl'
};

/**
 * Build Zod Schema from Field Definition
 */
export function buildFieldSchema(field: FieldDefinition): z.ZodType<any> {
  let schema: z.ZodType<any> = z.any();
  
  // Base schema based on field type
  switch (field.fieldType) {
    case 'text':
      schema = buildTextSchema(field);
      break;
      
    case 'textarea':
      schema = buildTextareaSchema(field);
      break;
      
    case 'email':
      schema = emailSchema;
      break;
      
    case 'phone':
      schema = germanPhoneSchema;
      break;
      
    case 'url':
      schema = urlSchema;
      break;
      
    case 'number':
      schema = buildNumberSchema(field);
      break;
      
    case 'select':
      schema = buildSelectSchema(field);
      break;
      
    case 'multiselect':
      schema = buildMultiSelectSchema(field);
      break;
      
    case 'boolean':
      schema = z.boolean({
        required_error: field.required ? DEFAULT_MESSAGES.required : undefined,
        invalid_type_error: 'Muss ein Wahrheitswert sein'
      });
      break;
      
    case 'date':
      schema = dateStringSchema;
      break;
      
    case 'custom':
      schema = buildCustomSchema(field);
      break;
      
    default:
      console.warn(`Unknown field type: ${field.fieldType}`);
      schema = z.any();
  }
  
  // Apply custom validations
  if (field.validation) {
    schema = applyCustomValidations(schema, field);
  }
  
  // Handle required/optional
  if (field.required) {
    // For required fields, ensure they're not empty
    schema = schema.refine(
      (val) => val !== null && val !== undefined && val !== '',
      DEFAULT_MESSAGES.required
    );
  } else {
    schema = schema.optional().nullable();
  }
  
  // Add description for documentation
  if (field.description) {
    schema = schema.describe(field.description);
  }
  
  return schema;
}

/**
 * Build text field schema
 */
function buildTextSchema(field: FieldDefinition): z.ZodSchema {
  const minLength = field.validation?.minLength || 0;
  const maxLength = field.validation?.maxLength || 255;
  
  let schema = textFieldSchema(minLength, maxLength);
  
  // Apply pattern if exists
  if (field.validation?.pattern) {
    const pattern = new RegExp(field.validation.pattern);
    const message = field.validation.patternMessage || DEFAULT_MESSAGES.pattern;
    schema = schema.regex(pattern, message);
  }
  
  // Special handling for specific field keys
  switch (field.key) {
    case 'postalCode':
      return germanPostalCodeSchema;
    case 'companyName':
      return schema.refine(
        (val) => !val.match(/[<>]/) && !val.toLowerCase().includes('script'),
        'Ungültiger Firmenname'
      );
  }
  
  return schema;
}

/**
 * Build textarea schema
 */
function buildTextareaSchema(field: FieldDefinition): z.ZodSchema {
  const maxLength = field.validation?.maxLength || 1000;
  return z.string()
    .max(maxLength, DEFAULT_MESSAGES.maxLength(maxLength))
    .trim();
}

/**
 * Build number field schema
 */
function buildNumberSchema(field: FieldDefinition): z.ZodSchema {
  const min = field.validation?.min;
  const max = field.validation?.max;
  const integer = field.validation?.integer;
  
  let schema = numberFieldSchema(min, max);
  
  if (integer) {
    schema = schema.int(DEFAULT_MESSAGES.integer);
  }
  
  // Special handling for percentage fields
  if (field.key?.includes('percentage') || field.key?.includes('percent')) {
    schema = schema.min(0, 'Prozent muss zwischen 0 und 100 liegen')
      .max(100, 'Prozent muss zwischen 0 und 100 liegen');
  }
  
  return schema;
}

/**
 * Build select field schema
 */
function buildSelectSchema(field: FieldDefinition): z.ZodSchema {
  if (!field.options || field.options.length === 0) {
    console.warn(`Select field ${field.key} has no options`);
    return z.string();
  }
  
  const values = field.options.map(opt => opt.value);
  
  // Create enum schema
  if (values.length === 1) {
    return z.literal(values[0]);
  }
  
  return z.enum(values as [string, ...string[]], {
    required_error: field.required ? DEFAULT_MESSAGES.required : undefined,
    invalid_type_error: DEFAULT_MESSAGES.enum
  });
}

/**
 * Build multi-select field schema
 */
function buildMultiSelectSchema(field: FieldDefinition): z.ZodSchema {
  if (!field.options || field.options.length === 0) {
    return z.array(z.string());
  }
  
  const values = field.options.map(opt => opt.value);
  return multiSelectSchema(values);
}

/**
 * Build custom field schema
 */
function buildCustomSchema(field: FieldDefinition): z.ZodSchema {
  // For custom fields, we need to check metadata for validation hints
  const customType = field.metadata?.customType;
  
  switch (customType) {
    case 'iban':
      return z.string().regex(/^[A-Z]{2}[0-9]{2}[A-Z0-9]+$/, 'Ungültige IBAN');
    case 'bic':
      return z.string().regex(/^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$/, 'Ungültiger BIC');
    case 'taxNumber':
      return z.string().regex(/^[0-9\/\-]+$/, 'Ungültige Steuernummer');
    default:
      return z.any();
  }
}

/**
 * Apply custom validations to schema
 */
function applyCustomValidations(
  schema: z.ZodType<any>,
  field: FieldDefinition
): z.ZodType<any> {
  if (!field.validation?.custom) return schema;
  
  field.validation.custom.forEach(validation => {
    switch (validation.type) {
      case 'unique':
        // This would need async validation
        schema = schema.refine(
          async (val) => {
            // In real implementation, this would call an API
            console.log(`Checking uniqueness for ${field.key}: ${val}`);
            return true;
          },
          validation.message || `${field.label} muss eindeutig sein`
        );
        break;
        
      case 'dependency':
        // Cross-field validation handled separately
        break;
        
      case 'businessRule':
        // Custom business rule validation
        if (validation.validator) {
          schema = schema.refine(
            validation.validator as CustomValidator,
            validation.message || 'Geschäftsregel verletzt'
          );
        }
        break;
    }
  });
  
  return schema;
}

/**
 * Build complete form schema from field definitions
 */
export function buildFormSchema(
  fields: FieldDefinition[],
  options: {
    partial?: boolean; // For draft validation
    includeOptional?: boolean;
  } = {}
): z.ZodSchema {
  const schemaShape: Record<string, z.ZodType<any>> = {};
  
  fields.forEach(field => {
    // Skip disabled fields
    if (field.disabled) return;
    
    // Skip optional fields if not included
    if (!options.includeOptional && !field.required) return;
    
    const fieldSchema = buildFieldSchema(field);
    schemaShape[field.key] = fieldSchema;
  });
  
  let schema = z.object(schemaShape);
  
  // Make all fields optional for partial validation (drafts)
  if (options.partial) {
    schema = schema.partial();
  }
  
  return schema;
}

/**
 * Validate single field value
 */
export async function validateField(
  field: FieldDefinition,
  value: any
): Promise<{ isValid: boolean; error?: string }> {
  const schema = buildFieldSchema(field);
  
  try {
    await schema.parseAsync(value);
    return { isValid: true };
  } catch (error) {
    if (error instanceof z.ZodError) {
      return {
        isValid: false,
        error: error.errors[0]?.message || 'Validierungsfehler'
      };
    }
    return {
      isValid: false,
      error: 'Unbekannter Validierungsfehler'
    };
  }
}

/**
 * Validate multiple fields
 */
export async function validateFields(
  fields: Array<{ field: FieldDefinition; value: any }>
): Promise<Map<string, string>> {
  const errors = new Map<string, string>();
  
  await Promise.all(
    fields.map(async ({ field, value }) => {
      const result = await validateField(field, value);
      if (!result.isValid && result.error) {
        errors.set(field.key, result.error);
      }
    })
  );
  
  return errors;
}