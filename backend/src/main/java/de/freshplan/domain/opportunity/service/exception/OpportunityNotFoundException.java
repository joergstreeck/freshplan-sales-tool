package de.freshplan.domain.opportunity.service.exception;

import java.util.UUID;

/**
 * Exception für nicht gefundene Opportunities
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class OpportunityNotFoundException extends RuntimeException {

  private final UUID opportunityId;

  public OpportunityNotFoundException(UUID opportunityId) {
    super("Opportunity not found with ID: " + opportunityId);
    this.opportunityId = opportunityId;
  }

  public UUID getOpportunityId() {
    return opportunityId;
  }
}
