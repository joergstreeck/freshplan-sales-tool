package de.freshplan.modules.leads.domain;

/**
 * Urgency Level for Lead Scoring - Sprint 2.1.6 Phase 5+ (V278)
 *
 * <p>Separate Dimension für Zeitdruck (nicht Pain!). Wird in Dringlichkeits-Berechnung verwendet.
 *
 * <p><strong>Lead-Score Integration:</strong> Dringlichkeit = (Pain/62 × 60%) + (Urgency/25 × 40%)
 *
 * <p><strong>Unterschied zu Pain:</strong>
 *
 * <ul>
 *   <li>Pain = "Was tut weh?" (Personalmangel, Kosten, Qualität)
 *   <li>Urgency = "Wie schnell muss es gelöst werden?" (Zeitdruck, Deadline)
 * </ul>
 *
 * <p><strong>Beispiel:</strong>
 *
 * <ul>
 *   <li>Hoher Pain + geringe Urgency = Nurturing-Lead (langfristiger Sales Cycle)
 *   <li>Hoher Pain + hohe Urgency = Hot Lead (sofort schließen)
 *   <li>Geringer Pain + hohe Urgency = Achtung! Vielleicht nicht der richtige Fit
 * </ul>
 *
 * @see Lead#calculateUrgencyDimension()
 */
public enum UrgencyLevel {
  /** Kein Zeitdruck - "Wir schauen uns das in 6+ Monaten an" */
  NORMAL(0, "Normal (6+ Monate)"),

  /** Mittlerer Zeitdruck - "Wir wollen in 1-3 Monaten umstellen" */
  MEDIUM(5, "Mittel (1-3 Monaten)"),

  /** Hoher Zeitdruck - "Wir müssen nächsten Monat eine Lösung haben" */
  HIGH(10, "Hoch (nächsten Monat)"),

  /** Notfall - "Koch hat gekündigt, wir stehen mit dem Rücken zur Wand, Start in 2 Wochen" */
  EMERGENCY(25, "Notfall (sofort)");

  private final int points;
  private final String label;

  UrgencyLevel(int points, String label) {
    this.points = points;
    this.label = label;
  }

  /**
   * Get point value for this urgency level.
   *
   * @return Points (0-25)
   */
  public int getPoints() {
    return points;
  }

  /**
   * Get German label for UI display.
   *
   * @return Localized label
   */
  public String getLabel() {
    return label;
  }
}
