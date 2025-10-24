package de.freshplan.domain.communication.entity;

/**
 * Entity Type Enum for Polymorphic Activity System
 *
 * <p>Sprint 2.1.7.2: Unified Communication System (D8)
 *
 * <p>Determines which entity type an Activity belongs to:
 *
 * <ul>
 *   <li>LEAD: Activity linked to Lead (modules.leads.domain.Lead)
 *   <li>CUSTOMER: Activity linked to Customer (domain.customer.entity.Customer)
 * </ul>
 *
 * <p>Future extensibility: PARTNER, SUPPLIER, etc.
 *
 * <p>CRM Best Practice: Salesforce, HubSpot, Dynamics all use this polymorphic pattern!
 *
 * @see Activity
 */
public enum EntityType {
  /** Activity for a Lead (Pre-Customer) */
  LEAD("Lead", "Pre-customer entity in sales pipeline"),

  /** Activity for a Customer (Post-Lead) */
  CUSTOMER("Customer", "Converted customer entity");

  private final String displayName;
  private final String description;

  EntityType(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  /**
   * Get display name for UI.
   *
   * @return Localized display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Get description for documentation.
   *
   * @return Technical description
   */
  public String getDescription() {
    return description;
  }
}
