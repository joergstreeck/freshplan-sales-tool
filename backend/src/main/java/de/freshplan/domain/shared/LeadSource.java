package de.freshplan.domain.shared;

/**
 * Lead Source Classification - Where the lead came from.
 *
 * <p>Sprint 2.1.6 Phase 5: Enum-Migration Phase 1 (Lead-Modul)
 *
 * <p>Business Rule: MESSE and TELEFON require documented first contact for lead protection. See
 * {@link #requiresFirstContact()} for Pre-Claim Logic.
 *
 * <p>Performance: Enum-based queries are ~10x faster than String-LIKE (Index vs. Full-Table-Scan).
 *
 * <p>Reference: ENUM_MIGRATION_STRATEGY.md - 3-Phase Migration Plan
 */
public enum LeadSource {
  /** Trade show / Exhibition / Event (Messe). Requires first contact documentation. */
  MESSE("Messe/Event"),

  /** Referral / Recommendation (Empfehlung). Optional Pre-Claim. */
  EMPFEHLUNG("Empfehlung"),

  /** Phone contact / Cold calling (Telefon). Requires first contact documentation. */
  TELEFON("Telefon"),

  /** Web form / Online inquiry (Web-Formular). Optional Pre-Claim. */
  WEB_FORMULAR("Web-Formular"),

  /** Partner referral (Partner). Optional Pre-Claim. */
  PARTNER("Partner"),

  /** Other sources not covered above (Sonstige). Optional Pre-Claim. */
  SONSTIGES("Sonstige");

  private final String displayName;

  LeadSource(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get human-readable display name.
   *
   * @return Display name (e.g., "Messe/Event", "Telefon")
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Check if this lead source requires documented first contact for lead protection.
   *
   * <p>Business Rule (Handelsvertretervertrag §2(8)(a)): MESSE and TELEFON sources require
   * documented first contact (contact person name + date) to activate lead protection
   * (registeredAt timestamp).
   *
   * <p>Other sources (EMPFEHLUNG, WEB_FORMULAR, PARTNER, SONSTIGES) allow Pre-Claim: Lead
   * protection starts with 10-day window to document first contact.
   *
   * @return true if first contact documentation is required (MESSE, TELEFON), false otherwise
   */
  public boolean requiresFirstContact() {
    return this == MESSE || this == TELEFON;
  }

  /**
   * Get LeadSource from string value (case-insensitive).
   *
   * @param value String value (enum name or display name)
   * @return LeadSource enum or null if not found
   */
  public static LeadSource fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String normalized = value.toUpperCase().trim();

    // Try exact enum match first
    try {
      return LeadSource.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      // Fall through to display name matching
    }

    // Try display name match (case-insensitive)
    for (LeadSource source : LeadSource.values()) {
      if (source.displayName.equalsIgnoreCase(value)) {
        return source;
      }
    }

    return null;
  }
}
