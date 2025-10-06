package de.freshplan.domain.shared;

/**
 * Business Type Classification for Leads and Customers.
 *
 * <p>Sprint 2.1.6 Phase 2 Review: Unified business type across Lead and Customer domains. Replaces
 * hardcoded frontend values and Customer Industry enum.
 *
 * <p>Single Source of Truth for business classification in B2B Food CRM.
 */
public enum BusinessType {
  /** Restaurant and dining establishments. */
  RESTAURANT("Restaurant"),

  /** Hotel and accommodation services. */
  HOTEL("Hotel"),

  /** Catering services and event catering. */
  CATERING("Catering"),

  /** Canteen and cafeteria (company/institutional dining). */
  KANTINE("Kantine"),

  /** Wholesale trade (Großhandel). */
  GROSSHANDEL("Großhandel"),

  /** Retail food stores (LEH - Lebensmitteleinzelhandel). */
  LEH("LEH"),

  /** Educational institutions (schools, universities). */
  BILDUNG("Bildung"),

  /** Healthcare facilities (hospitals, care homes). */
  GESUNDHEIT("Gesundheit"),

  /** Other business types not covered above. */
  SONSTIGES("Sonstiges");

  private final String displayName;

  BusinessType(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get human-readable display name.
   *
   * @return Display name (e.g., "Restaurant", "Großhandel")
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get BusinessType from string value (case-insensitive).
   *
   * @param value String value (enum name or display name)
   * @return BusinessType enum or null if not found
   */
  public static BusinessType fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String normalized = value.toUpperCase().trim();

    // Try exact enum match first
    try {
      return BusinessType.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      // Fall through to display name matching
    }

    // Try display name match (case-insensitive)
    for (BusinessType type : BusinessType.values()) {
      if (type.displayName.equalsIgnoreCase(value)) {
        return type;
      }
    }

    return null;
  }

  /**
   * Convert from legacy Industry enum to BusinessType.
   *
   * @param industry Legacy Industry enum
   * @return BusinessType equivalent or SONSTIGES if no match
   */
  public static BusinessType fromLegacyIndustry(
      de.freshplan.domain.customer.entity.Industry industry) {
    if (industry == null) {
      return null;
    }

    return switch (industry) {
      case HOTEL -> HOTEL;
      case RESTAURANT -> RESTAURANT;
      case CATERING -> CATERING;
      case KANTINE -> KANTINE;
      case GESUNDHEITSWESEN -> GESUNDHEIT;
      case BILDUNG -> BILDUNG;
      case EINZELHANDEL -> LEH; // Retail → LEH (Lebensmitteleinzelhandel)
      case VERANSTALTUNG, SONSTIGE -> SONSTIGES;
    };
  }

  /**
   * Convert to legacy Industry enum (for backward compatibility).
   *
   * @return Legacy Industry enum equivalent or SONSTIGES if no match
   */
  public de.freshplan.domain.customer.entity.Industry toLegacyIndustry() {
    return switch (this) {
      case HOTEL -> de.freshplan.domain.customer.entity.Industry.HOTEL;
      case RESTAURANT -> de.freshplan.domain.customer.entity.Industry.RESTAURANT;
      case CATERING -> de.freshplan.domain.customer.entity.Industry.CATERING;
      case KANTINE -> de.freshplan.domain.customer.entity.Industry.KANTINE;
      case GESUNDHEIT -> de.freshplan.domain.customer.entity.Industry.GESUNDHEITSWESEN;
      case BILDUNG -> de.freshplan.domain.customer.entity.Industry.BILDUNG;
      case LEH, GROSSHANDEL -> de.freshplan.domain.customer.entity.Industry.EINZELHANDEL;
      case SONSTIGES -> de.freshplan.domain.customer.entity.Industry.SONSTIGE;
    };
  }
}
