package de.freshplan.domain.customer.dto;

import java.util.List;

/**
 * Field Definition for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * <p>Defines a single field in a Customer Card.
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
    FieldDefinition itemSchema) {

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
          itemSchema);
    }
  }
}
