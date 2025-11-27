package de.freshplan.domain.customer.dto;

/**
 * Field Type Enum for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * <p>Defines all supported field types for dynamic rendering in Frontend.
 *
 * <p>Frontend uses this to determine which component to render: - TEXT → TextField - CURRENCY →
 * CurrencyField - ENUM → Select/Dropdown - BOOLEAN → Checkbox - DATE → DatePicker - etc.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum FieldType {
  /** Plain text field (single line) */
  TEXT,

  /** Multi-line text field */
  TEXTAREA,

  /** Currency field with € symbol and decimal formatting */
  CURRENCY,

  /** Integer number field */
  NUMBER,

  /** Decimal/Float number field */
  DECIMAL,

  /** Enum dropdown (requires enumSource) - single selection */
  ENUM,

  /**
   * Multi-select field for multiple selections (Sprint 2.1.7.7)
   *
   * <p>Example: assignedLocationIds for Contact-Location assignment
   */
  MULTISELECT,

  /** Boolean checkbox */
  BOOLEAN,

  /** Date picker (LocalDate) */
  DATE,

  /** Date-Time picker (LocalDateTime) */
  DATETIME,

  /** Email field with validation */
  EMAIL,

  /** Phone number field with formatting */
  PHONE,

  /** URL field with validation */
  URL,

  /**
   * Read-only label (calculated/derived values)
   *
   * <p>Example: Health Score, Days Since Last Order
   */
  LABEL,

  /**
   * Chip/Tag field (visual indicator)
   *
   * <p>Example: Status Badge (AKTIV → Green, PROSPECT → Yellow)
   */
  CHIP,

  /**
   * Group of fields (nested structure)
   *
   * <p>Example: mainAddress with street, postalCode, city, countryCode (Sprint 2.1.7.2 D11)
   */
  GROUP,

  /**
   * Array of items with schema (repeatable structure)
   *
   * <p>Example: deliveryAddresses with multiple address items (Sprint 2.1.7.2 D11)
   */
  ARRAY
}
