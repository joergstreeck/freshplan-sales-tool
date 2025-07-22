package de.freshplan.domain.opportunity.service.exception;

import de.freshplan.domain.opportunity.entity.OpportunityStage;

/**
 * Exception für ungültige Stage-Übergänge
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class InvalidStageTransitionException extends RuntimeException {

  private final OpportunityStage fromStage;
  private final OpportunityStage toStage;

  public InvalidStageTransitionException(OpportunityStage fromStage, OpportunityStage toStage) {
    super(String.format("Invalid stage transition from %s to %s", fromStage, toStage));
    this.fromStage = fromStage;
    this.toStage = toStage;
  }

  public InvalidStageTransitionException(String message) {
    super(message);
    this.fromStage = null;
    this.toStage = null;
  }

  public OpportunityStage getFromStage() {
    return fromStage;
  }

  public OpportunityStage getToStage() {
    return toStage;
  }
}
