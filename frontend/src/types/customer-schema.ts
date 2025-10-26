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

  // ========== WIZARD METADATA (Sprint 2.1.7.2 D11) ==========
  /**
   * Show this field in Customer Wizard?
   *
   * Sprint 2.1.7.2 D11: Single Source of Truth for Wizard + Detail-Tabs
   *
   * If true, field appears in wizard step defined by wizardStep
   */
  showInWizard?: boolean;

  /**
   * Wizard step number (1-4)
   *
   * Sprint 2.1.7.2 D11: Wizard Steps
   *
   * 1 = Basis & Filialstruktur
   * 2 = Herausforderungen & Potenzial
   * 3 = Multi-Contact Management
   * 4 = Angebot & Services
   *
   * Only relevant if showInWizard = true
   */
  wizardStep?: number;

  /**
   * Order within wizard step (1, 2, 3, ...)
   *
   * Sprint 2.1.7.2 D11: Field ordering in wizard
   *
   * Fields with lower order appear first
   *
   * Only relevant if showInWizard = true
   */
  wizardOrder?: number;

  /**
   * Wizard section identifier (for grouping fields)
   *
   * Sprint 2.1.7.2 D11 Option B: Server-Driven Sections
   *
   * Groups fields visually in wizard steps
   *
   * Examples: "company_basic", "address", "business_model", "chain_structure"
   *
   * Only relevant if showInWizard = true
   */
  wizardSectionId?: string;

  /**
   * Wizard section title (display text for section heading)
   *
   * Sprint 2.1.7.2 D11 Option B: Server-Driven Sections
   *
   * Displayed as section heading in wizard
   *
   * Examples: "Unternehmensdaten", "📍 Adresse Hauptstandort", "💰 Geschäftsmodell"
   *
   * Only relevant if showInWizard = true
   */
  wizardSectionTitle?: string;

  /**
   * Show divider after this field?
   *
   * Sprint 2.1.7.2 D11 Option B: Server-Driven Sections
   *
   * If true, renders a divider line after this field (section separator)
   *
   * Only relevant if showInWizard = true
   */
  showDividerAfter?: boolean;
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
