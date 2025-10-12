package de.freshplan.domain.shared;

/**
 * Kitchen Size Classification for B2B Food Service.
 *
 * <p>Sprint 2.1.6 Phase 5: Enum-Migration Phase 1 (Lead-Modul)
 *
 * <p>Used for estimating volume potential and matching products/services. Helps LeadScoring with
 * business fit factor.
 *
 * <p>Performance: Enum-based queries are ~10x faster than String-LIKE.
 *
 * <p>Reference: ENUM_MIGRATION_STRATEGY.md - 3-Phase Migration Plan
 */
public enum KitchenSize {
  /** Small kitchen (1-10 employees). Estimated volume: €500-2k/month. */
  KLEIN("Klein (1-10 Mitarbeiter)"),

  /** Medium kitchen (11-50 employees). Estimated volume: €2k-10k/month. */
  MITTEL("Mittel (11-50 Mitarbeiter)"),

  /** Large kitchen (51-200 employees). Estimated volume: €10k-50k/month. */
  GROSS("Groß (51-200 Mitarbeiter)"),

  /** Very large kitchen (200+ employees). Estimated volume: €50k+/month. */
  SEHR_GROSS("Sehr Groß (200+ Mitarbeiter)");

  private final String displayName;

  KitchenSize(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get human-readable display name.
   *
   * @return Display name (e.g., "Klein (1-10 Mitarbeiter)")
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get estimated monthly volume for this kitchen size (in EUR).
   *
   * <p>Used for LeadScoring "Business Fit" factor.
   *
   * @return Estimated monthly volume midpoint in EUR
   */
  public int getEstimatedMonthlyVolume() {
    return switch (this) {
      case KLEIN -> 1250; // €500-2k midpoint
      case MITTEL -> 6000; // €2k-10k midpoint
      case GROSS -> 30000; // €10k-50k midpoint
      case SEHR_GROSS -> 75000; // €50k+ midpoint (conservative)
    };
  }

  /**
   * Check if this kitchen size qualifies as "high-value" (volume ≥€10k/month).
   *
   * <p>Used for LeadScoring prioritization.
   *
   * @return true if GROSS or SEHR_GROSS, false otherwise
   */
  public boolean isHighValue() {
    return this == GROSS || this == SEHR_GROSS;
  }

  /**
   * Get KitchenSize from string value (case-insensitive).
   *
   * @param value String value (enum name or display name)
   * @return KitchenSize enum or null if not found
   */
  public static KitchenSize fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String normalized = value.toUpperCase().trim();

    // Try exact enum match first
    try {
      return KitchenSize.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      // Fall through to display name matching
    }

    // Try display name match (case-insensitive)
    for (KitchenSize size : KitchenSize.values()) {
      if (size.displayName.equalsIgnoreCase(value)) {
        return size;
      }
    }

    return null;
  }
}
