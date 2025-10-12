package de.freshplan.modules.leads.domain;

/**
 * Relationship Status - Qualität der Beziehung zum Lead-Kontakt
 *
 * <p>Sprint 2.1.6 Phase 5+ - V280 Migration
 *
 * <p>Wird verwendet für Engagement-Score-Berechnung (40% Gewicht). Orthogonal zu
 * DecisionMakerAccess (wer ist der Kontakt?) - hier geht es um die Beziehungsqualität.
 *
 * <p>Punkte-System:
 *
 * <ul>
 *   <li>COLD (0): Kein Kontakt, nur Name/Firma bekannt
 *   <li>CONTACTED (5): Erstkontakt hergestellt (E-Mail/Anruf/Messe)
 *   <li>ENGAGED_SKEPTICAL (8): Mehrere Touchpoints, aber skeptisch/vorsichtig
 *   <li>ENGAGED_POSITIVE (12): Mehrere Touchpoints, offen und interessiert
 *   <li>TRUSTED (17): Vertrauensbasis, offener Austausch, teilt interne Infos
 *   <li>ADVOCATE (25): Kämpft aktiv für uns intern, verteidigt uns, drängt auf Entscheidung
 * </ul>
 *
 * <p>Wichtig: ADVOCATE muss NICHT der Entscheider sein (kann auch Küchenchef sein, der für uns
 * kämpft). Für Entscheider-Zugang siehe {@link DecisionMakerAccess}.
 *
 * @see DecisionMakerAccess
 * @since 2.1.6
 */
public enum RelationshipStatus {
  COLD(0, "Kein Kontakt"),
  CONTACTED(5, "Erstkontakt hergestellt"),
  ENGAGED_SKEPTICAL(8, "Mehrere Touchpoints, skeptisch"),
  ENGAGED_POSITIVE(12, "Mehrere Touchpoints, positiv"),
  TRUSTED(17, "Vertrauensbasis vorhanden"),
  ADVOCATE(25, "Kämpft aktiv für uns");

  private final int points;
  private final String label;

  RelationshipStatus(int points, String label) {
    this.points = points;
    this.label = label;
  }

  /**
   * Punkte für Engagement-Score-Berechnung
   *
   * @return Punktwert (0-25)
   */
  public int getPoints() {
    return points;
  }

  /**
   * Deutsche Beschreibung für UI
   *
   * @return Label (z.B. "Vertrauensbasis vorhanden")
   */
  public String getLabel() {
    return label;
  }
}
