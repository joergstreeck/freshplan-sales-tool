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

  /**
   * Bereinigt Opportunity-Namen von OpportunityType-Präfixen (Sprint 2.1.7.1 - Production Safety
   * Layer)
   *
   * @param name Original Opportunity Name
   * @return Bereinigter Name ohne Type-Präfix
   */
  private String cleanOpportunityName(String name) {
    if (name == null) {
      return null;
    }
    // Case-insensitive regex für konsistente Bereinigung (Gemini Code Review)
    return name.replaceFirst(
        "(?i)^(Neuer Standort|Sortimentserweiterung|Verlängerung|Verlaengerung|Neugeschäft|Neugeschaeft):\\s*",
        "");
  }

  /** Konvertiert Opportunity Entity zu Response DTO */
  public OpportunityResponse toResponse(Opportunity opportunity) {
    if (opportunity == null) {
      return null;
    }

    return OpportunityResponse.builder()
        .id(opportunity.getId())
        .name(cleanOpportunityName(opportunity.getName()))
        .description(opportunity.getDescription())
        .stage(opportunity.getStage())
        .opportunityType(opportunity.getOpportunityType()) // Sprint 2.1.7.1
        .customerId(opportunity.getCustomer() != null ? opportunity.getCustomer().getId() : null)
        .customerName(
            opportunity.getCustomer() != null ? opportunity.getCustomer().getCompanyName() : null)
        .leadId(opportunity.getLead() != null ? opportunity.getLead().id : null) // Sprint 2.1.7.1
        .leadCompanyName(
            opportunity.getLead() != null
                ? opportunity.getLead().companyName
                : null) // Sprint 2.1.7.1
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
