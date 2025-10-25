/**
 * Customer Card Schema Types
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * TypeScript types matching Backend DTOs:
 * - FieldType.java
 * - FieldDefinition.java
 * - CardSection.java
 * - CustomerCardSchema.java
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

/**
 * Field Type Enum
 *
 * Maps to backend FieldType.java enum
 *
 * Determines which UI component to render:
 * - TEXT → TextField
 * - TEXTAREA → TextField multiline
 * - CURRENCY → TextField with € formatting
 * - NUMBER → TextField type="number"
 * - DECIMAL → TextField with decimal formatting
 * - ENUM → Autocomplete (options from enumSource endpoint)
 * - BOOLEAN → Checkbox
 * - DATE → DatePicker
 * - DATETIME → DateTimePicker
 * - EMAIL → TextField type="email"
 * - PHONE → TextField with phone formatting
 * - URL → TextField type="url"
 * - LABEL → Typography (read-only display)
 * - CHIP → Chip component (status badges, tags)
 */
export type FieldType =
  | 'TEXT'
  | 'TEXTAREA'
  | 'CURRENCY'
  | 'NUMBER'
  | 'DECIMAL'
  | 'ENUM'
  | 'BOOLEAN'
  | 'DATE'
  | 'DATETIME'
  | 'EMAIL'
  | 'PHONE'
  | 'URL'
  | 'LABEL'
  | 'CHIP';

/**
 * Field Definition
 *
 * Maps to backend FieldDefinition.java record
 */
export interface FieldDefinition {
  fieldKey: string;
  label: string;
  type: FieldType;
  required: boolean;
  readonly: boolean;
  enumSource: string | null;
  placeholder: string | null;
  helpText: string | null;
  gridCols: number | null;
  validationRules: string[];
}

/**
 * Card Section
 *
 * Maps to backend CardSection.java record
 */
export interface CardSection {
  sectionId: string;
  title: string;
  subtitle: string | null;
  fields: FieldDefinition[];
  collapsible: boolean;
  defaultCollapsed: boolean;
}

/**
 * Customer Card Schema
 *
 * Maps to backend CustomerCardSchema.java record
 */
export interface CustomerCardSchema {
  cardId: string;
  title: string;
  subtitle: string | null;
  icon: string;
  order: number;
  sections: CardSection[];
  defaultCollapsed: boolean;
}
