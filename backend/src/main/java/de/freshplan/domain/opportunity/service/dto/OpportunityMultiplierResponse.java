package de.freshplan.domain.opportunity.service.dto;

import de.freshplan.domain.opportunity.entity.OpportunityMultiplier;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for Opportunity Multipliers (Sprint 2.1.7.3)
 *
 * <p>Used by GET /api/settings/opportunity-multipliers endpoint
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.3
 */
public class OpportunityMultiplierResponse {

  private UUID id;
  private String businessType;
  private String opportunityType;
  private BigDecimal multiplier;

  // Default constructor
  public OpportunityMultiplierResponse() {}

  /**
   * Create response from entity
   *
   * @param entity OpportunityMultiplier entity
   * @return OpportunityMultiplierResponse
   */
  public static OpportunityMultiplierResponse fromEntity(OpportunityMultiplier entity) {
    if (entity == null) {
      return null;
    }

    OpportunityMultiplierResponse response = new OpportunityMultiplierResponse();
    response.id = entity.getId();
    response.businessType = entity.getBusinessType();
    response.opportunityType = entity.getOpportunityType();
    response.multiplier = entity.getMultiplier();

    return response;
  }

  // ============================================================================
  // GETTERS & SETTERS
  // ============================================================================

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getBusinessType() {
    return businessType;
  }

  public void setBusinessType(String businessType) {
    this.businessType = businessType;
  }

  public String getOpportunityType() {
    return opportunityType;
  }

  public void setOpportunityType(String opportunityType) {
    this.opportunityType = opportunityType;
  }

  public BigDecimal getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(BigDecimal multiplier) {
    this.multiplier = multiplier;
  }
}
