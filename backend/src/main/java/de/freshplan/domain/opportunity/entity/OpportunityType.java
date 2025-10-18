package de.freshplan.domain.opportunity.entity;

/**
 * Opportunity Type Enum - Freshfoodz-spezifische Geschäftstypen
 *
 * <p>Definiert die verschiedenen Arten von Verkaufschancen basierend auf der Freshfoodz
 * Business-Logik.
 *
 * <p>Pattern: VARCHAR + CHECK Constraint (NICHT PostgreSQL ENUM Type!) Begründung:
 * JPA-Standard @Enumerated(STRING), flexibler, wartbarer
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.1
 * @see <a
 *     href="docs/planung/features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md">ENUM_MIGRATION_STRATEGY.md</a>
 */
public enum OpportunityType {
  /** Kunde kauft zum ersten Mal (egal ob Artikel oder Konzept) */
  NEUGESCHAEFT("Neugeschäft"),

  /** Neue Produktkategorie (einzelner Artikel bis Gesamtkonzept) */
  SORTIMENTSERWEITERUNG("Sortimentserweiterung"),

  /** Zusätzlicher Standort beim bestehenden Kunden */
  NEUER_STANDORT("Neuer Standort"),

  /** Rahmenvertrag-Renewal (Verlängerung) */
  VERLAENGERUNG("Vertragsverlängerung");

  private final String label;

  OpportunityType(String label) {
    this.label = label;
  }

  /**
   * Get German display label
   *
   * @return Deutsche Bezeichnung für UI
   */
  public String getLabel() {
    return label;
  }

  /**
   * Check if this type represents a completely new business relationship
   *
   * @return true für NEUGESCHAEFT
   */
  public boolean isNewBusiness() {
    return this == NEUGESCHAEFT;
  }

  /**
   * Check if this type represents recurring business (renewal)
   *
   * @return true für VERLAENGERUNG
   */
  public boolean isRenewal() {
    return this == VERLAENGERUNG;
  }

  /**
   * Check if this type represents expansion of existing business
   *
   * @return true für SORTIMENTSERWEITERUNG oder NEUER_STANDORT
   */
  public boolean isExpansion() {
    return this == SORTIMENTSERWEITERUNG || this == NEUER_STANDORT;
  }
}
