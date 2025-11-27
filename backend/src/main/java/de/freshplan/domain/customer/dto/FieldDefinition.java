package de.freshplan.domain.customer.dto;

import java.util.List;

/**
 * Field Definition for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards + Wizard
 *
 * <p>Defines a single field in a Customer Card AND/OR Wizard Step.
 *
 * <p>Frontend reads this definition and renders the appropriate component.
 *
 * <p>Example Usage:
 *
 * <pre>
 * FieldDefinition companyName = FieldDefinition.builder()
 *   .fieldKey("companyName")
 *   .label("Firmenname")
 *   .type(FieldType.TEXT)
 *   .required(true)
 *   .showInWizard(true)      // Show in Wizard
 *   .wizardStep(1)           // Wizard Step 1
 *   .wizardOrder(2)          // Order within Step
 *   .build();
 * </pre>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record FieldDefinition(
    /** Backend field key (e.g., "companyName", "expectedAnnualVolume") */
    String fieldKey,

    /** Human-readable label (German, e.g., "Firmenname", "Erwarteter Jahresumsatz") */
    String label,

    /** Field type (determines Frontend component) */
    FieldType type,

    /** Is this field required? */
    boolean required,

    /** Is this field read-only? */
    boolean readonly,

    /**
     * Enum source endpoint (for ENUM type fields)
     *
     * <p>Example: "/api/enums/business-types"
     */
    String enumSource,

    /**
     * Placeholder text (optional)
     *
     * <p>Example: "Bitte Firmennamen eingeben..."
     */
    String placeholder,

    /**
     * Help text / Tooltip (optional)
     *
     * <p>Example: "Der offizielle Firmenname laut Handelsregister"
     */
    String helpText,

    /**
     * Grid column span (1-12, default 6 = half width)
     *
     * <p>Used for responsive layout in Frontend (MUI Grid)
     */
    Integer gridCols,

    /**
     * Validation rules (optional)
     *
     * <p>Example: ["min:0", "max:1000000", "email"]
     */
    List<String> validationRules,

    /**
     * Nested fields (for GROUP type)
     *
     * <p>Sprint 2.1.7.2 D11: Structured Address Support
     *
     * <p>Example: mainAddress (GROUP) with fields: street, postalCode, city, countryCode
     *
     * <p>Only used when type = GROUP, null otherwise
     */
    List<FieldDefinition> fields,

    /**
     * Item schema (for ARRAY type)
     *
     * <p>Sprint 2.1.7.2 D11: Repeatable Items Support
     *
     * <p>Example: deliveryAddresses (ARRAY) with itemSchema defining address structure
     *
     * <p>Only used when type = ARRAY, null otherwise
     */
    FieldDefinition itemSchema,

    // ========== WIZARD METADATA (Sprint 2.1.7.2 D11) ==========

    /**
     * Show this field in Customer Wizard?
     *
     * <p>Sprint 2.1.7.2 D11: Single Source of Truth for Wizard + Detail-Tabs
     *
     * <p>If true, field appears in wizard step defined by wizardStep
     */
    Boolean showInWizard,

    /**
     * Wizard step number (1-4)
     *
     * <p>Sprint 2.1.7.2 D11: Wizard Steps
     *
     * <ul>
     *   <li>1 = Basis & Filialstruktur
     *   <li>2 = Herausforderungen & Potenzial
     *   <li>3 = Multi-Contact Management
     *   <li>4 = Angebot & Services
     * </ul>
     *
     * <p>Only relevant if showInWizard = true
     */
    Integer wizardStep,

    /**
     * Order within wizard step (1, 2, 3, ...)
     *
     * <p>Sprint 2.1.7.2 D11: Field ordering in wizard
     *
     * <p>Fields with lower order appear first
     *
     * <p>Only relevant if showInWizard = true
     */
    Integer wizardOrder,

    /**
     * Wizard section identifier (for grouping fields)
     *
     * <p>Sprint 2.1.7.2 D11 Option B: Server-Driven Sections
     *
     * <p>Groups fields visually in wizard steps
     *
     * <p>Examples: "company_basic", "address", "business_model", "chain_structure"
     *
     * <p>Only relevant if showInWizard = true
     */
    String wizardSectionId,

    /**
     * Wizard section title (display text for section heading)
     *
     * <p>Sprint 2.1.7.2 D11 Option B: Server-Driven Sections
     *
     * <p>Displayed as section heading in wizard
     *
     * <p>Examples: "Unternehmensdaten", "📍 Adresse Hauptstandort", "💰 Geschäftsmodell"
     *
     * <p>Only relevant if showInWizard = true
     */
    String wizardSectionTitle,

    /**
     * Show divider after this field?
     *
     * <p>Sprint 2.1.7.2 D11 Option B: Server-Driven Sections
     *
     * <p>If true, renders a divider line after this field (section separator)
     *
     * <p>Only relevant if showInWizard = true
     */
    Boolean showDividerAfter,

    /**
     * Number of rows for TEXTAREA fields
     *
     * <p>Sprint 2.1.7.7: Compact textarea layout
     *
     * <p>Controls the height of textarea fields (default: 4)
     */
    Integer rows,

    // ========== CONDITIONAL VISIBILITY (Sprint 2.1.7.7) ==========

    /**
     * Conditional visibility: Field key to check
     *
     * <p>Sprint 2.1.7.7: Server-Driven Conditional Fields
     *
     * <p>This field is only visible when another field has a specific value.
     *
     * <p>Example: branchCount is only visible when isChain = true
     *
     * <p>Usage: visibleWhenField = "isChain", visibleWhenValue = "true"
     */
    String visibleWhenField,

    /**
     * Conditional visibility: Expected value
     *
     * <p>Sprint 2.1.7.7: Server-Driven Conditional Fields
     *
     * <p>The value that visibleWhenField must have for this field to be visible.
     *
     * <p>For BOOLEAN fields: "true" or "false" For ENUM fields: the enum value (e.g. "ja", "nein")
     * For TEXT fields: exact string match
     */
    String visibleWhenValue) {

  /** Builder for convenient FieldDefinition creation */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String fieldKey;
    private String label;
    private FieldType type = FieldType.TEXT;
    private boolean required = false;
    private boolean readonly = false;
    private String enumSource;
    private String placeholder;
    private String helpText;
    private Integer gridCols = 6; // Default: half width
    private List<String> validationRules = List.of();
    private List<FieldDefinition> fields; // Sprint 2.1.7.2 D11: GROUP type support
    private FieldDefinition itemSchema; // Sprint 2.1.7.2 D11: ARRAY type support
    // Wizard metadata (Sprint 2.1.7.2 D11)
    private Boolean showInWizard = false; // Default: nicht im Wizard
    private Integer wizardStep;
    private Integer wizardOrder;
    // Wizard section metadata (Sprint 2.1.7.2 D11 Option B)
    private String wizardSectionId;
    private String wizardSectionTitle;
    private Boolean showDividerAfter = false;
    private Integer rows; // Sprint 2.1.7.7: Textarea rows
    // Conditional visibility (Sprint 2.1.7.7)
    private String visibleWhenField;
    private String visibleWhenValue;

    public Builder fieldKey(String fieldKey) {
      this.fieldKey = fieldKey;
      return this;
    }

    public Builder label(String label) {
      this.label = label;
      return this;
    }

    public Builder type(FieldType type) {
      this.type = type;
      return this;
    }

    public Builder required(boolean required) {
      this.required = required;
      return this;
    }

    public Builder readonly(boolean readonly) {
      this.readonly = readonly;
      return this;
    }

    public Builder enumSource(String enumSource) {
      this.enumSource = enumSource;
      return this;
    }

    public Builder placeholder(String placeholder) {
      this.placeholder = placeholder;
      return this;
    }

    public Builder helpText(String helpText) {
      this.helpText = helpText;
      return this;
    }

    public Builder gridCols(Integer gridCols) {
      this.gridCols = gridCols;
      return this;
    }

    public Builder validationRules(List<String> validationRules) {
      this.validationRules = validationRules;
      return this;
    }

    /**
     * Set nested fields (for GROUP type)
     *
     * @param fields list of nested field definitions
     * @return this builder
     */
    public Builder fields(List<FieldDefinition> fields) {
      this.fields = fields;
      return this;
    }

    /**
     * Set item schema (for ARRAY type)
     *
     * @param itemSchema schema definition for array items
     * @return this builder
     */
    public Builder itemSchema(FieldDefinition itemSchema) {
      this.itemSchema = itemSchema;
      return this;
    }

    /**
     * Show this field in Customer Wizard?
     *
     * @param showInWizard true = show in wizard, false = only in detail-tabs
     * @return this builder
     */
    public Builder showInWizard(Boolean showInWizard) {
      this.showInWizard = showInWizard;
      return this;
    }

    /**
     * Set wizard step number (1-4)
     *
     * @param wizardStep step number (1=Basis, 2=Herausforderungen, 3=Kontakte, 4=Angebot)
     * @return this builder
     */
    public Builder wizardStep(Integer wizardStep) {
      this.wizardStep = wizardStep;
      return this;
    }

    /**
     * Set order within wizard step
     *
     * @param wizardOrder order number (1, 2, 3, ...)
     * @return this builder
     */
    public Builder wizardOrder(Integer wizardOrder) {
      this.wizardOrder = wizardOrder;
      return this;
    }

    /**
     * Set wizard section identifier (for grouping fields)
     *
     * @param wizardSectionId section ID (e.g. "company_basic", "address")
     * @return this builder
     */
    public Builder wizardSectionId(String wizardSectionId) {
      this.wizardSectionId = wizardSectionId;
      return this;
    }

    /**
     * Set wizard section title (display text for section heading)
     *
     * @param wizardSectionTitle section title (e.g. "Unternehmensdaten", "📍 Adresse")
     * @return this builder
     */
    public Builder wizardSectionTitle(String wizardSectionTitle) {
      this.wizardSectionTitle = wizardSectionTitle;
      return this;
    }

    /**
     * Show divider after this field?
     *
     * @param showDividerAfter true = show divider, false = no divider
     * @return this builder
     */
    public Builder showDividerAfter(Boolean showDividerAfter) {
      this.showDividerAfter = showDividerAfter;
      return this;
    }

    /**
     * Set number of rows for TEXTAREA fields
     *
     * <p>Sprint 2.1.7.7: Compact textarea layout
     *
     * @param rows number of rows (default: 4)
     * @return this builder
     */
    public Builder rows(Integer rows) {
      this.rows = rows;
      return this;
    }

    /**
     * Set conditional visibility field
     *
     * <p>Sprint 2.1.7.7: Server-Driven Conditional Fields
     *
     * @param visibleWhenField the field key to check for visibility
     * @return this builder
     */
    public Builder visibleWhenField(String visibleWhenField) {
      this.visibleWhenField = visibleWhenField;
      return this;
    }

    /**
     * Set conditional visibility value
     *
     * <p>Sprint 2.1.7.7: Server-Driven Conditional Fields
     *
     * @param visibleWhenValue the expected value for the field to be visible
     * @return this builder
     */
    public Builder visibleWhenValue(String visibleWhenValue) {
      this.visibleWhenValue = visibleWhenValue;
      return this;
    }

    public FieldDefinition build() {
      return new FieldDefinition(
          fieldKey,
          label,
          type,
          required,
          readonly,
          enumSource,
          placeholder,
          helpText,
          gridCols,
          validationRules,
          fields,
          itemSchema,
          showInWizard,
          wizardStep,
          wizardOrder,
          wizardSectionId,
          wizardSectionTitle,
          showDividerAfter,
          rows,
          visibleWhenField,
          visibleWhenValue);
    }
  }
}
