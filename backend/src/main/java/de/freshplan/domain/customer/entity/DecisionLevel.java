package de.freshplan.domain.customer.entity;

/**
 * Decision Level for Contacts (Sales Strategy).
 *
 * Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
 *
 * Understanding the decision-making level is crucial for B2B sales strategy.
 * Helps prioritize contacts and tailor communication approach.
 */
public enum DecisionLevel {
  /** Executive level - C-Level, Owner (final decision maker) */
  EXECUTIVE("Geschäftsführer/Inhaber"),

  /** Manager level - Department heads, location managers (recommend/influence) */
  MANAGER("Führungskraft"),

  /** Operational level - Kitchen chef, buyer (day-to-day operations) */
  OPERATIONAL("Operative Ebene"),

  /** Influencer - No formal authority but influences decisions */
  INFLUENCER("Berater/Einflussnehmer");

  private final String displayName;

  DecisionLevel(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
