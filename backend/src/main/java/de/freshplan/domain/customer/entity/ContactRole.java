package de.freshplan.domain.customer.entity;

/**
 * Contact Role in Gastronomy/Hospitality Business.
 *
 * Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
 *
 * Realistic roles in German gastronomy businesses for better targeting.
 */
public enum ContactRole {
  /** Küchenchef / Chefkoch - responsible for kitchen operations */
  KUECHENCHEF("Küchenchef"),

  /** Sous Chef - second in command in kitchen */
  SOUS_CHEF("Sous Chef"),

  /** Einkaufsleiter / Procurement Manager */
  EINKAUFSLEITER("Einkaufsleiter"),

  /** Einkäufer / Buyer */
  EINKAEUFER("Einkäufer"),

  /** Betriebsleiter / Operations Manager */
  BETRIEBSLEITER("Betriebsleiter"),

  /** Geschäftsführer / Managing Director */
  GESCHAEFTSFUEHRER("Geschäftsführer"),

  /** Restaurantleiter / Restaurant Manager */
  RESTAURANTLEITER("Restaurantleiter"),

  /** F&B Manager (Food & Beverage) */
  FB_MANAGER("F&B Manager"),

  /** Serviceleiter / Service Manager */
  SERVICELEITER("Serviceleiter"),

  /** Inhaber / Owner */
  INHABER("Inhaber"),

  /** Küchen-Manager */
  KUECHEN_MANAGER("Küchen-Manager"),

  /** Controlling / Finance */
  CONTROLLING("Controlling"),

  /** Marketing Manager */
  MARKETING("Marketing Manager"),

  /** Qualitätsmanager / Quality Manager */
  QUALITAETSMANAGER("Qualitätsmanager"),

  /** Sonstiges / Other */
  SONSTIGES("Sonstiges");

  private final String displayName;

  ContactRole(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
