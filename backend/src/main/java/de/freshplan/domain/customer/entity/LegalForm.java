package de.freshplan.domain.customer.entity;

/**
 * Legal Form Classification for Customers.
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Legal Form Enum
 *
 * <p>Represents the legal structure of a company (Rechtsform).
 *
 * <p>Single Source of Truth for legal form dropdown in Customer forms.
 */
public enum LegalForm {
  /** GmbH - Gesellschaft mit beschränkter Haftung. */
  GMBH("GmbH"),

  /** AG - Aktiengesellschaft. */
  AG("AG"),

  /** GmbH & Co. KG - Kommanditgesellschaft. */
  GMBH_CO_KG("GmbH & Co. KG"),

  /** KG - Kommanditgesellschaft. */
  KG("KG"),

  /** OHG - Offene Handelsgesellschaft. */
  OHG("OHG"),

  /** Einzelunternehmen - Sole proprietorship. */
  EINZELUNTERNEHMEN("Einzelunternehmen"),

  /** GbR - Gesellschaft bürgerlichen Rechts. */
  GBR("GbR"),

  /** e.V. - Eingetragener Verein (registered association). */
  EV("e.V."),

  /** Stiftung - Foundation. */
  STIFTUNG("Stiftung"),

  /** Sonstige - Other legal forms. */
  SONSTIGE("Sonstige");

  private final String displayName;

  LegalForm(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get human-readable display name.
   *
   * @return Display name (e.g., "GmbH", "AG", "GmbH & Co. KG")
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get LegalForm from string value (case-insensitive).
   *
   * @param value String value (enum name or display name)
   * @return LegalForm enum or null if not found
   */
  public static LegalForm fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    String normalized = value.toUpperCase().trim();

    // Try exact enum match first
    try {
      return LegalForm.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      // Fall through to display name matching
    }

    // Try display name match (case-insensitive)
    for (LegalForm form : LegalForm.values()) {
      if (form.displayName.equalsIgnoreCase(value)) {
        return form;
      }
    }

    return null;
  }
}
