package de.freshplan.domain.shared;

/**
 * Country Code Enum for Address Fields
 *
 * <p>Sprint 2.1.7.2 D11: Structured Address Support
 *
 * <p>Supported Countries: Germany, Switzerland, Austria
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum CountryCode {
  /** Germany / Deutschland */
  DE("Deutschland", "DE"),

  /** Switzerland / Schweiz */
  CH("Schweiz", "CH"),

  /** Austria / Österreich */
  AT("Österreich", "AT");

  private final String displayName;
  private final String isoCode;

  CountryCode(String displayName, String isoCode) {
    this.displayName = displayName;
    this.isoCode = isoCode;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getIsoCode() {
    return isoCode;
  }
}
