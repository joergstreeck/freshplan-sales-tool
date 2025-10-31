package de.freshplan.domain.customer.entity;

/**
 * Expansion Plan Classification for Customers.
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Expansion Plan Enum
 *
 * <p>Represents whether the customer is planning business expansion (Erweiterung geplant).
 *
 * <p>Single Source of Truth for expansion plan dropdown in Customer forms.
 */
public enum ExpansionPlan {
  /** Ja - Expansion is planned. */
  JA("Ja, Expansion geplant"),

  /** Nein - No expansion planned. */
  NEIN("Nein"),

  /** Geplant - Expansion is being planned. */
  GEPLANT("In Planung"),

  /** Unklar - Unclear whether expansion is planned. */
  UNKLAR("Unklar");

  private final String displayName;

  ExpansionPlan(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get human-readable display name.
   *
   * @return Display name (e.g., "Ja, Expansion geplant", "Nein")
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get ExpansionPlan from string value (case-insensitive).
   *
   * @param value String value (enum name or display name)
   * @return ExpansionPlan enum or null if not found
   */
  public static ExpansionPlan fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String normalized = value.toUpperCase().trim();

    // Try exact enum match first
    try {
      return ExpansionPlan.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      // Fall through to display name matching
    }

    // Try display name match (case-insensitive)
    for (ExpansionPlan plan : ExpansionPlan.values()) {
      if (plan.displayName.equalsIgnoreCase(value)) {
        return plan;
      }
    }

    return null;
  }
}
