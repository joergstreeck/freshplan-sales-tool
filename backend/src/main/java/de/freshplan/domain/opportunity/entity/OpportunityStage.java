package de.freshplan.domain.opportunity.entity;

/**
 * Opportunity Pipeline Stages
 *
 * <p>Definiert die verschiedenen Stufen einer Verkaufschance im Sales-Prozess. Jede Stage hat
 * spezifische Business Rules und verfügbare Aktionen.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public enum OpportunityStage {

  /**
   * Neuer Lead - Erste Kontaktaufnahme Standard-Wahrscheinlichkeit: 10% Nächste Aktionen:
   * Qualifizierung, Erste Kontaktaufnahme
   */
  NEW_LEAD("Neuer Lead", "#ff9800", 10),

  /**
   * Qualifizierung - Lead wird qualifiziert Standard-Wahrscheinlichkeit: 25% Nächste Aktionen:
   * Bedarfsanalyse, Terminvereinbarung
   */
  QUALIFICATION("Qualifizierung", "#2196f3", 25),

  /**
   * Bedarfsanalyse - Kundenanforderungen ermitteln Standard-Wahrscheinlichkeit: 40% Nächste
   * Aktionen: Angebotserstellung, Calculator
   */
  NEEDS_ANALYSIS("Bedarfsanalyse", "#009688", 40),

  /**
   * Angebotserstellung - Proposal Phase ⭐ Standard-Wahrscheinlichkeit: 60% Verfügbare Tools:
   * Calculator, E-Mail-Templates Nächste Aktionen: Angebot senden, Nachfassen
   */
  PROPOSAL("Angebotserstellung", "#94c456", 60),

  /**
   * Verhandlung - Finale Verhandlungsphase Standard-Wahrscheinlichkeit: 80% Nächste Aktionen:
   * Verhandlung, Vertragsgestaltung
   */
  NEGOTIATION("Verhandlung", "#4caf50", 80),

  /**
   * Abgeschlossen - Gewonnen 🎉 Standard-Wahrscheinlichkeit: 100% Automatische Aktionen: Kunde
   * anlegen, Onboarding starten
   */
  CLOSED_WON("Gewonnen", "#4caf50", 100),

  /**
   * Abgeschlossen - Verloren Standard-Wahrscheinlichkeit: 0% Automatische Aktionen: Verlustgrund
   * dokumentieren
   */
  CLOSED_LOST("Verloren", "#f44336", 0);

  private final String displayName;
  private final String color;
  private final int defaultProbability;

  OpportunityStage(String displayName, String color, int defaultProbability) {
    this.displayName = displayName;
    this.color = color;
    this.defaultProbability = defaultProbability;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getColor() {
    return color;
  }

  public int getDefaultProbability() {
    return defaultProbability;
  }

  /** Prüft ob die Stage ein aktiver Status ist (nicht abgeschlossen) */
  public boolean isActive() {
    return this != CLOSED_WON && this != CLOSED_LOST;
  }

  /** Prüft ob die Stage abgeschlossen ist */
  public boolean isClosed() {
    return this == CLOSED_WON || this == CLOSED_LOST;
  }

  /** Gibt die nächsten möglichen Stages zurück (Business Rules) */
  public OpportunityStage[] getNextPossibleStages() {
    return switch (this) {
      case NEW_LEAD -> new OpportunityStage[] {QUALIFICATION, CLOSED_LOST};
      case QUALIFICATION -> new OpportunityStage[] {NEEDS_ANALYSIS, CLOSED_LOST};
      case NEEDS_ANALYSIS -> new OpportunityStage[] {PROPOSAL, CLOSED_LOST};
      case PROPOSAL -> new OpportunityStage[] {NEGOTIATION, NEEDS_ANALYSIS, CLOSED_LOST};
      case NEGOTIATION -> new OpportunityStage[] {CLOSED_WON, PROPOSAL, CLOSED_LOST};
      case CLOSED_WON -> new OpportunityStage[] {}; // Keine weiteren Änderungen
      case CLOSED_LOST -> new OpportunityStage[] {}; // Keine weiteren Änderungen
    };
  }

  /** Prüft ob ein Stage-Wechsel erlaubt ist */
  public boolean canTransitionTo(OpportunityStage targetStage) {
    OpportunityStage[] allowedStages = getNextPossibleStages();
    for (OpportunityStage stage : allowedStages) {
      if (stage == targetStage) {
        return true;
      }
    }
    return false;
  }
}
