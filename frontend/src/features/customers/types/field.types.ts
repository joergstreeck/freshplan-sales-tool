/**
 * Field System Types
 * 
 * Types for the dynamic field system that powers customer data management.
 * Based on the Field Catalog JSON structure.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/data/fieldCatalog.json
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-field-rendering.md
 */

/**
 * Supported field types in the system
 */
export type FieldType = 
  | 'text'
  | 'select'
  | 'multiselect'
  | 'email'
  | 'number'
  | 'range'
  | 'date'
  | 'datetime'
  | 'boolean'
  | 'textarea';

/**
 * Entity types that can have field values
 */
export enum EntityType {
  CUSTOMER = 'customer',
  LOCATION = 'location',
  DETAILED_LOCATION = 'detailedLocation'
}

/**
 * Option for select/multiselect fields
 */
export interface SelectOption {
  value: string;
  label: string;
  /** Optional icon or color for visual differentiation */
  icon?: string;
  /** Disable this option */
  disabled?: boolean;
}

/**
 * Grid breakpoints for responsive layout
 */
export interface GridSize {
  /** Extra small devices (phones, < 600px) */
  xs?: number;
  /** Small devices (tablets, ≥ 600px) */
  sm?: number;
  /** Medium devices (desktops, ≥ 960px) */
  md?: number;
  /** Large devices (large desktops, ≥ 1280px) */
  lg?: number;
}

/**
 * Conditional trigger for wizard steps
 */
export interface TriggerCondition {
  /** Field value that triggers the condition */
  when: string | string[];
  /** Wizard step to show when condition is met */
  step: string;
}

/**
 * Field validation rules
 */
export interface FieldValidation {
  /** Field is required */
  required?: boolean;
  /** Minimum length for text fields */
  minLength?: number;
  /** Maximum length for text fields */
  maxLength?: number;
  /** Minimum value for number fields */
  min?: number;
  /** Maximum value for number fields */
  max?: number;
  /** Regex pattern for validation */
  pattern?: string;
  /** Custom validation message */
  message?: string;
  /** Reference to validation rule in catalog */
  validation?: string;
}

/**
 * Complete field definition from catalog
 */
export interface FieldDefinition {
  /** Unique field identifier */
  key: string;
  /** Display label (German) */
  label: string;
  /** Entity type this field belongs to */
  entityType: EntityType | string;
  /** Field input type */
  fieldType: FieldType;
  /** Field is required */
  required?: boolean;
  /** Options for select fields */
  options?: SelectOption[];
  /** Validation rules */
  validation?: FieldValidation | string;
  /** Default value */
  defaultValue?: any;
  /** Maximum length hint */
  maxLength?: number;
  /** Minimum value hint */
  min?: number;
  /** Maximum value hint */
  max?: number;
  /** Placeholder text */
  placeholder?: string;
  /** Help text shown below field */
  helpText?: string;
  /** Responsive grid sizing */
  gridSize?: GridSize;
  /** Conditional wizard trigger */
  triggerWizardStep?: TriggerCondition;
  /** Field contains sensitive data (DSGVO) */
  sensitive?: boolean;
  /** Field is read-only */
  readonly?: boolean;
  /** Field is disabled */
  disabled?: boolean;
}

/**
 * Stored field value in database
 */
export interface FieldValue {
  /** UUID primary key */
  id: string;
  /** Reference to field definition key */
  fieldDefinitionId: string;
  /** UUID of the entity this value belongs to */
  entityId: string;
  /** Type of entity */
  entityType: EntityType;
  /** Actual stored value (JSONB) */
  value: any;
  /** Last update timestamp */
  updatedAt: string;
}

/**
 * Field value for form handling
 */
export interface FormFieldValue {
  /** Field definition key */
  key: string;
  /** Current value */
  value: any;
  /** Validation errors */
  error?: string;
  /** Field has been touched */
  touched?: boolean;
  /** Field is currently validating */
  validating?: boolean;
}

/**
 * Field catalog structure from JSON
 */
export interface FieldCatalog {
  /** Customer entity fields */
  customer: {
    /** Base fields for all customers */
    base: FieldDefinition[];
    /** Industry-specific fields */
    industrySpecific: Record<string, FieldDefinition[]>;
  };
  /** Location entity fields */
  location: {
    /** Base fields for all locations */
    base: FieldDefinition[];
    /** Industry-specific fields */
    industrySpecific?: Record<string, FieldDefinition[]>;
  };
  /** Validation rule definitions */
  validationRules: Record<string, {
    pattern?: string;
    minLength?: number;
    maxLength?: number;
    message: string;
  }>;
}