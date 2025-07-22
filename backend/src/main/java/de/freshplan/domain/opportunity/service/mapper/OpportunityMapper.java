package de.freshplan.domain.opportunity.service.mapper;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper für Opportunity Entities zu DTOs
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class OpportunityMapper {

  /** Konvertiert Opportunity Entity zu Response DTO */
  public OpportunityResponse toResponse(Opportunity opportunity) {
    if (opportunity == null) {
      return null;
    }

    return OpportunityResponse.builder()
        .id(opportunity.getId())
        .name(opportunity.getName())
        .description(opportunity.getDescription())
        .stage(opportunity.getStage())
        .customerId(opportunity.getCustomer() != null ? opportunity.getCustomer().getId() : null)
        .customerName(
            opportunity.getCustomer() != null ? opportunity.getCustomer().getCompanyName() : null)
        .assignedToId(
            opportunity.getAssignedTo() != null ? opportunity.getAssignedTo().getId() : null)
        .assignedToName(
            opportunity.getAssignedTo() != null ? opportunity.getAssignedTo().getUsername() : null)
        .expectedValue(opportunity.getExpectedValue())
        .expectedCloseDate(opportunity.getExpectedCloseDate())
        .probability(opportunity.getProbability())
        .createdAt(opportunity.getCreatedAt())
        .stageChangedAt(opportunity.getStageChangedAt())
        .updatedAt(opportunity.getUpdatedAt())
        .activities(null) // TODO: Implement activity mapping
        .build();
  }
}
