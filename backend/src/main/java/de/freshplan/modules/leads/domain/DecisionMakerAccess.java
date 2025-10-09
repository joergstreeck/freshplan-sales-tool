package de.freshplan.modules.leads.domain;

/**
 * Decision Maker Access - Zugang zum Entscheider
 *
 * <p>Sprint 2.1.6 Phase 5+ - V280 Migration
 *
 * <p>Wird verwendet für Engagement-Score-Berechnung (60% Gewicht). Orthogonal zu
 * RelationshipStatus (wie gut ist die Beziehung?) - hier geht es um: Sprechen wir mit der
 * richtigen Person?
 *
 * <p>Punkte-System:
 *
 * <ul>
 *   <li>UNKNOWN (0): Entscheider noch nicht identifiziert (frühe Discovery-Phase)
 *   <li>BLOCKED (-3): Entscheider bekannt, aber kein Zugang (Gatekeeper blockt ab)
 *   <li>INDIRECT (10): Zugang über Dritte (Assistent, Mitarbeiter, Partner, Küchenchef → Chef)
 *   <li>DIRECT (20): Direkter Kontakt zum Entscheider etabliert
 *   <li>IS_DECISION_MAKER (25): Unser Kontakt IST der Entscheider selbst
 * </ul>
 *
 * <p>WICHTIG: Decision Maker Access ist der kritischste Faktor für Win-Rate:
 *
 * <ul>
 *   <li>IS_DECISION_MAKER: ~70-80% Win-Rate
 *   <li>DIRECT: ~50-60% Win-Rate
 *   <li>INDIRECT: ~25-35% Win-Rate
 *   <li>BLOCKED/UNKNOWN: ~10-15% Win-Rate
 * </ul>
 *
 * <p>Ein Lead mit hohem Pain + Urgency aber BLOCKED/UNKNOWN ist meist Zeitverschwendung.
 *
 * @see RelationshipStatus
 * @since 2.1.6
 */
public enum DecisionMakerAccess {
  UNKNOWN(0, "Entscheider noch nicht identifiziert"),
  BLOCKED(-3, "Entscheider bekannt, aber Zugang blockiert"),
  INDIRECT(10, "Zugang über Dritte (Assistent, Mitarbeiter, Partner)"),
  DIRECT(20, "Direkter Kontakt zum Entscheider"),
  IS_DECISION_MAKER(25, "Unser Kontakt IST der Entscheider");

  private final int points;
  private final String label;

  DecisionMakerAccess(int points, String label) {
    this.points = points;
    this.label = label;
  }

  /**
   * Punkte für Engagement-Score-Berechnung
   *
   * <p>BLOCKED hat -3 Punkte, um aktive Blockade zu signalisieren (vs. UNKNOWN = neutral).
   *
   * @return Punktwert (-3 bis +25)
   */
  public int getPoints() {
    return points;
  }

  /**
   * Deutsche Beschreibung für UI
   *
   * @return Label (z.B. "Direkter Kontakt zum Entscheider")
   */
  public String getLabel() {
    return label;
  }
}
