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
 * - TEXT → TextField / Typography (read-only)
 * - TEXTAREA → TextField multiline / Typography (read-only)
 * - CURRENCY → TextField with € formatting / Typography (read-only)
 * - NUMBER → TextField type="number" / Typography (read-only)
 * - DECIMAL → TextField with decimal formatting / Typography (read-only)
 * - ENUM → Autocomplete / Typography with label (read-only)
 * - BOOLEAN → Checkbox / Typography Ja/Nein (read-only)
 * - DATE → DatePicker / Typography formatted (read-only)
 * - DATETIME → DateTimePicker / Typography formatted (read-only)
 * - EMAIL → TextField type="email" / Typography (read-only)
 * - PHONE → TextField with phone formatting / Typography (read-only)
 * - URL → TextField type="url" / Link (read-only)
 * - LABEL → Typography (always read-only)
 * - CHIP → Chip component (status badges, tags)
 * - GROUP → Nested field group (Sprint 2.1.7.2 D11)
 * - ARRAY → Repeatable items (Sprint 2.1.7.2 D11)
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
  | 'CHIP'
  | 'GROUP'
  | 'ARRAY';

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

  // ========== GROUP/ARRAY SUPPORT (Sprint 2.1.7.2 D11) ==========
  /**
   * Nested fields (for GROUP type)
   *
   * Example: mainAddress (GROUP) with fields: street, postalCode, city, countryCode
   *
   * Only used when type = GROUP, null otherwise
   */
  fields?: FieldDefinition[];

  /**
   * Item schema (for ARRAY type)
   *
   * Example: deliveryAddresses (ARRAY) with itemSchema defining address structure
   *
   * Only used when type = ARRAY, null otherwise
   */
  itemSchema?: FieldDefinition;

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

  // ========== CONDITIONAL VISIBILITY (Sprint 2.1.7.7) ==========

  /**
   * Conditional visibility: Field key to check
   *
   * Sprint 2.1.7.7: Server-Driven Conditional Fields
   *
   * This field is only visible when another field has a specific value.
   *
   * Example: branchCount is only visible when isChain = true
   *
   * Usage: visibleWhenField = "isChain", visibleWhenValue = "true"
   */
  visibleWhenField?: string;

  /**
   * Conditional visibility: Expected value
   *
   * Sprint 2.1.7.7: Server-Driven Conditional Fields
   *
   * The value that visibleWhenField must have for this field to be visible.
   *
   * For BOOLEAN fields: "true" or "false"
   * For ENUM fields: the enum value (e.g. "ja", "nein")
   * For TEXT fields: exact string match
   */
  visibleWhenValue?: string;
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
