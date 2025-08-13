package de.freshplan.domain.opportunity.service.query;

import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.PipelineOverviewResponse;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.domain.opportunity.service.mapper.OpportunityMapper;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opportunity Query Service - CQRS Read Side
 *
 * <p>Behandelt alle lesenden Operationen für Opportunities:
 * - Find operations
 * - Pipeline analytics
 * - Forecasting
 *
 * <p>WICHTIG: Dieser Service hat KEINE @Transactional Annotation,
 * da nur lesende Operationen durchgeführt werden.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class OpportunityQueryService {

  private static final Logger logger = LoggerFactory.getLogger(OpportunityQueryService.class);

  @Inject OpportunityRepository opportunityRepository;

  @Inject UserRepository userRepository;

  @Inject OpportunityMapper opportunityMapper;

  // =====================================
  // BASIC QUERY OPERATIONS
  // =====================================

  /**
   * Findet alle Opportunities mit Paginierung
   * EXAKTE KOPIE von OpportunityService.findAllOpportunities() Zeile 135-138
   */
  public List<OpportunityResponse> findAllOpportunities(Page page) {
    List<Opportunity> opportunities = opportunityRepository.findAllActive(page);
    return opportunities.stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Findet eine Opportunity by ID
   * EXAKTE KOPIE von OpportunityService.findById() Zeile 141-147
   */
  public OpportunityResponse findById(UUID id) {
    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new OpportunityNotFoundException(id));
    return opportunityMapper.toResponse(opportunity);
  }

  /**
   * Find all opportunities without pagination.
   * EXAKTE KOPIE von OpportunityService.findAll() Zeile 398-402
   *
   * @return list of all opportunities
   */
  public List<OpportunityResponse> findAll() {
    logger.debug("Finding all opportunities");
    List<Opportunity> opportunities = opportunityRepository.listAll();
    return opportunities.stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
  }

  // =====================================
  // FILTERED QUERIES
  // =====================================

  /**
   * Findet Opportunities eines bestimmten Verkäufers
   * EXAKTE KOPIE von OpportunityService.findByAssignedTo() Zeile 290-298
   */
  public List<OpportunityResponse> findByAssignedTo(UUID userId) {
    User user =
        userRepository
            .findByIdOptional(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    List<Opportunity> opportunities = opportunityRepository.findByAssignedTo(user);
    return opportunities.stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Findet Opportunities nach Stage
   * EXAKTE KOPIE von OpportunityService.findByStage() Zeile 301-304
   */
  public List<OpportunityResponse> findByStage(OpportunityStage stage) {
    List<Opportunity> opportunities = opportunityRepository.findByStage(stage);
    return opportunities.stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
  }

  // =====================================
  // PIPELINE ANALYTICS
  // =====================================

  /**
   * Pipeline Übersicht mit Stage-Statistiken
   * EXAKTE KOPIE von OpportunityService.getPipelineOverview() Zeile 259-287
   */
  public PipelineOverviewResponse getPipelineOverview() {
    logger.debug("Generating pipeline overview");

    List<Object[]> stageStats = opportunityRepository.getPipelineOverview();
    BigDecimal totalForecast = opportunityRepository.calculateForecast();
    Double conversionRate = opportunityRepository.getConversionRate();

    List<Opportunity> highPriority = opportunityRepository.findHighPriorityOpportunities(5);
    List<Opportunity> overdue = opportunityRepository.findOverdueOpportunities();

    return PipelineOverviewResponse.builder()
        .stageStatistics(
            stageStats.stream()
                .map(
                    stat ->
                        PipelineOverviewResponse.StageStatistic.builder()
                            .stage((OpportunityStage) stat[0])
                            .count((Long) stat[1])
                            .totalValue((BigDecimal) stat[2])
                            .build())
                .collect(Collectors.toList()))
        .totalForecast(totalForecast)
        .conversionRate(conversionRate)
        .highPriorityOpportunities(
            highPriority.stream().map(opportunityMapper::toResponse).collect(Collectors.toList()))
        .overdueOpportunities(
            overdue.stream().map(opportunityMapper::toResponse).collect(Collectors.toList()))
        .build();
  }

  // =====================================
  // ANALYTICS & FORECASTING
  // =====================================

  /**
   * Berechnet den Forecast basierend auf erwarteten Werten und Wahrscheinlichkeiten
   * EXAKTE KOPIE von OpportunityService.calculateForecast() Zeile 332-335
   */
  public BigDecimal calculateForecast() {
    logger.debug("Calculating opportunity forecast");
    return opportunityRepository.calculateForecast();
  }
}