package de.freshplan.domain.customer.entity;

/**
 * Enumeration for different categories of timeline events.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum EventCategory {
  SYSTEM("System"),
  STATUS_UPDATE("Status Update"),
  DATA_CHANGE("Datenänderung"),
  COMMUNICATION("Kommunikation"),
  MEETING("Meeting"),
  PHONE_CALL("Telefonat"),
  EMAIL("E-Mail"),
  NOTE("Notiz"),
  DOCUMENT("Dokument"),
  TASK("Aufgabe"),
  QUOTE("Angebot"),
  ORDER("Bestellung"),
  INVOICE("Rechnung"),
  PAYMENT("Zahlung"),
  COMPLAINT("Beschwerde"),
  SUPPORT("Support"),
  FOLLOW_UP("Follow-up"),
  PARTNER_ACTIVITY("Partner-Aktivität"),
  RISK_ASSESSMENT("Risikobewertung"),
  OTHER("Sonstige");

  private final String displayName;

  EventCategory(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
