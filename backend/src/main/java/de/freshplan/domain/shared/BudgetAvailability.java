package de.freshplan.domain.shared;

/**
 * Budget Availability - Verfügbarkeit von Budget beim Lead/Customer
 *
 * <p>Sprint 2.1.7.2 D11.2: Server-Driven UI for Lead Scoring (Revenue Score)
 *
 * <p>Wird verwendet für Revenue-Score-Berechnung und Deal-Qualifizierung.
 *
 * <p>Business Rule: Budget-Verfügbarkeit ist ein kritischer Faktor für Sales-Priorisierung:
 *
 * <ul>
 *   <li>YES: Budget ist freigegeben/verfügbar → Hohe Priorität
 *   <li>NO: Kein Budget vorhanden → Niedriger Priority (Nurturing-Lead)
 *   <li>UNKNOWN: Noch nicht geklärt → Discovery-Phase
 * </ul>
 *
 * @since 2.1.7.2
 */
public enum BudgetAvailability {
  /** Budget ist verfügbar/freigegeben */
  YES("Ja, Budget verfügbar"),

  /** Kein Budget vorhanden */
  NO("Nein, kein Budget"),

  /** Budget-Status noch nicht geklärt */
  UNKNOWN("Noch nicht bekannt");

  private final String label;

  BudgetAvailability(String label) {
    this.label = label;
  }

  /**
   * Deutsche Beschreibung für UI
   *
   * @return Label (z.B. "Ja, Budget verfügbar")
   */
  public String getLabel() {
    return label;
  }
}
