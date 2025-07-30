package de.freshplan.domain.customer.entity;

/**
 * Financing type for customer business model.
 * Part of Sprint 2 - Sales-focused customer management.
 * 
 * @since 2.0.0
 */
public enum FinancingType {
  /** Private financing - entrepreneurial business model */
  PRIVATE("private", "Privat finanziert"),
  
  /** Public financing - government or institutional funding */
  PUBLIC("public", "Öffentlich finanziert"),
  
  /** Mixed financing - combination of private and public */
  MIXED("mixed", "Gemischt finanziert");

  private final String value;
  private final String displayName;

  FinancingType(String value, String displayName) {
    this.value = value;
    this.displayName = displayName;
  }

  public String getValue() {
    return value;
  }

  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get enum from string value.
   * 
   * @param value the string value
   * @return the corresponding enum or null if not found
   */
  public static FinancingType fromValue(String value) {
    if (value == null) {
      return null;
    }
    
    for (FinancingType type : FinancingType.values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    
    return null;
  }
}