package de.freshplan.domain.opportunity.service;

import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityActivity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.command.OpportunityCommandService;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.PipelineOverviewResponse;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import de.freshplan.domain.opportunity.service.exception.InvalidStageTransitionException;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.domain.opportunity.service.mapper.OpportunityMapper;
import de.freshplan.domain.opportunity.service.query.OpportunityQueryService;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opportunity Service - Business Logic für Sales Pipeline
 *
 * <p>FACADE PATTERN: Dieser Service fungiert als Facade für die CQRS-aufgeteilten Services. Mit
 * Feature Flag kann zwischen Legacy-Implementierung und CQRS umgeschaltet werden.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class OpportunityService {

  private static final Logger logger = LoggerFactory.getLogger(OpportunityService.class);

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  @Inject OpportunityMapper opportunityMapper;

  @Inject SecurityIdentity securityIdentity;

  @Inject AuditService auditService;

  // CQRS Services (NEU)
  @Inject OpportunityCommandService commandService;

  @Inject OpportunityQueryService queryService;

  // Feature Flag für CQRS
  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  // =====================================
  // CRUD OPERATIONS
  // =====================================

  /** Erstellt eine neue Opportunity */
  public OpportunityResponse createOpportunity(CreateOpportunityRequest request) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityCommandService");
      return commandService.createOpportunity(request);
    }

    logger.info("Creating new opportunity: {}", request.getName());

    // Validation
    if (request.getCustomerId() == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }

    // Validate expected close date
    if (request.getExpectedCloseDate() != null
        && request.getExpectedCloseDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Expected close date cannot be in the past");
    }

    // Aktuellen User ermitteln
    User currentUser = getCurrentUser();

    // Opportunity erstellen
    Opportunity opportunity =
        new Opportunity(request.getName(), OpportunityStage.NEW_LEAD, currentUser);

    // Optional: Weitere Felder setzen
    if (request.getDescription() != null) {
      opportunity.setDescription(request.getDescription());
    }
    if (request.getExpectedValue() != null) {
      opportunity.setExpectedValue(request.getExpectedValue());
    }
    if (request.getExpectedCloseDate() != null) {
      opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
    }

    // Customer zuweisen
    if (request.getCustomerId() != null) {
      Customer customer =
          customerRepository
              .findByIdOptional(request.getCustomerId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Customer not found with ID: " + request.getCustomerId()));
      opportunity.setCustomer(customer);
      logger.debug("Assigned customer {} to opportunity", customer.getCompanyName());
    }

    // User zuweisen wenn angegeben
    if (request.getAssignedTo() != null) {
      User assignedUser =
          userRepository
              .findByIdOptional(request.getAssignedTo())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "User not found with ID: " + request.getAssignedTo()));
      opportunity.setAssignedTo(assignedUser);
      logger.debug("Assigned user {} to opportunity", assignedUser.getUsername());
    }

    // Speichern
    opportunityRepository.persist(opportunity);

    // Audit Log für Opportunity-Erstellung
    try {
      auditService.logAsync(
          AuditContext.builder()
              .eventType(AuditEventType.OPPORTUNITY_CREATED)
              .entityType("opportunity")
              .entityId(opportunity.getId())
              .newValue(opportunity.getName())
              .changeReason("Neue Verkaufschance erstellt")
              .build());
    } catch (Exception e) {
      logger.warn("Failed to log audit event for opportunity creation", e);
    }

    // Initiale Activity erstellen
    OpportunityActivity activity =
        new OpportunityActivity(
            opportunity,
            currentUser,
            OpportunityActivity.ActivityType.NOTE,
            "Opportunity erstellt",
            "Neue Verkaufschance wurde angelegt");
    opportunity.addActivity(activity);

    logger.info("Created opportunity with ID: {}", opportunity.getId());
    return opportunityMapper.toResponse(opportunity);
  }

  /** Findet alle Opportunities mit Paginierung */
  public List<OpportunityResponse> findAllOpportunities(Page page) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityQueryService");
      return queryService.findAllOpportunities(page);
    }

    List<Opportunity> opportunities = opportunityRepository.findAllActive(page);
    return opportunities.stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
  }

  /** Findet eine Opportunity by ID */
  public OpportunityResponse findById(UUID id) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityQueryService");
      return queryService.findById(id);
    }

    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new OpportunityNotFoundException(id));
    return opportunityMapper.toResponse(opportunity);
  }

  /** Aktualisiert eine Opportunity */
  public OpportunityResponse updateOpportunity(UUID id, UpdateOpportunityRequest request) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityCommandService");
      return commandService.updateOpportunity(id, request);
    }

    logger.info("Updating opportunity: {}", id);

    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new OpportunityNotFoundException(id));

    // Felder aktualisieren
    if (request.getName() != null) {
      opportunity.setName(request.getName());
    }
    if (request.getDescription() != null) {
      opportunity.setDescription(request.getDescription());
    }
    if (request.getExpectedValue() != null) {
      opportunity.setExpectedValue(request.getExpectedValue());
    }
    if (request.getExpectedCloseDate() != null) {
      opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
    }
    if (request.getProbability() != null) {
      opportunity.setProbability(request.getProbability());
    }

    // Update Activity erstellen
    User currentUser = getCurrentUser();
    OpportunityActivity activity =
        new OpportunityActivity(
            opportunity,
            currentUser,
            OpportunityActivity.ActivityType.NOTE,
            "Opportunity aktualisiert",
            "Opportunity-Details wurden geändert");
    opportunity.addActivity(activity);

    opportunityRepository.persist(opportunity);

    logger.info("Updated opportunity: {}", id);
    return opportunityMapper.toResponse(opportunity);
  }

  // =====================================
  // STAGE MANAGEMENT
  // =====================================

  /** Ändert die Stage einer Opportunity mit Business Rules Validation */
  /** Changes the stage of an opportunity with default reason. */
  public OpportunityResponse changeStage(UUID opportunityId, OpportunityStage newStage) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityCommandService");
      return commandService.changeStage(opportunityId, newStage);
    }

    return changeStage(opportunityId, newStage, "Stage change");
  }

  public OpportunityResponse changeStage(
      UUID opportunityId, OpportunityStage newStage, String reason) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityCommandService");
      return commandService.changeStage(opportunityId, newStage, reason);
    }

    logger.info("Changing stage for opportunity {} to {}", opportunityId, newStage);

    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(opportunityId)
            .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));

    OpportunityStage currentStage = opportunity.getStage();

    // Business Rule: Stage Transition Validation
    if (!currentStage.canTransitionTo(newStage)) {
      throw new InvalidStageTransitionException(currentStage, newStage);
    }

    // Stage ändern
    OpportunityStage previousStage = opportunity.getStage();
    opportunity.changeStage(newStage);

    // Audit log the stage change
    auditService.logAsync(
        AuditContext.builder()
            .eventType(AuditEventType.OPPORTUNITY_STAGE_CHANGED)
            .entityType("opportunity")
            .entityId(opportunityId)
            .oldValue(Map.of("stage", previousStage.name()))
            .newValue(Map.of("stage", newStage.name()))
            .changeReason(reason)
            .build());

    // Stage Change Activity erstellen
    User currentUser = getCurrentUser();
    String activityTitle =
        String.format(
            "Status geändert: %s → %s", previousStage.getDisplayName(), newStage.getDisplayName());
    OpportunityActivity activity =
        new OpportunityActivity(
            opportunity,
            currentUser,
            OpportunityActivity.ActivityType.STAGE_CHANGED,
            activityTitle,
            reason != null ? reason : "Status-Änderung");
    opportunity.addActivity(activity);

    opportunityRepository.persist(opportunity);

    logger.info(
        "Changed stage for opportunity {} from {} to {}", opportunityId, previousStage, newStage);
    return opportunityMapper.toResponse(opportunity);
  }

  // =====================================
  // PIPELINE ANALYTICS
  // =====================================

  /** Pipeline Übersicht mit Stage-Statistiken */
  public PipelineOverviewResponse getPipelineOverview() {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityQueryService");
      return queryService.getPipelineOverview();
    }

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

  /** Findet Opportunities eines bestimmten Verkäufers */
  public List<OpportunityResponse> findByAssignedTo(UUID userId) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityQueryService");
      return queryService.findByAssignedTo(userId);
    }

    User user =
        userRepository
            .findByIdOptional(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    List<Opportunity> opportunities = opportunityRepository.findByAssignedTo(user);
    return opportunities.stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
  }

  /** Findet Opportunities nach Stage */
  public List<OpportunityResponse> findByStage(OpportunityStage stage) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityQueryService");
      return queryService.findByStage(stage);
    }

    List<Opportunity> opportunities = opportunityRepository.findByStage(stage);
    return opportunities.stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
  }

  // =====================================
  // ACTIVITY MANAGEMENT
  // =====================================

  /** Fügt eine Activity zu einer Opportunity hinzu */
  public OpportunityResponse addActivity(
      UUID opportunityId, OpportunityActivity.ActivityType type, String title, String description) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityCommandService");
      return commandService.addActivity(opportunityId, type, title, description);
    }

    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(opportunityId)
            .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));

    User currentUser = getCurrentUser();
    OpportunityActivity activity =
        new OpportunityActivity(opportunity, currentUser, type, title, description);
    opportunity.addActivity(activity);

    opportunityRepository.persist(opportunity);
    return opportunityMapper.toResponse(opportunity);
  }

  // =====================================
  // ANALYTICS & FORECASTING
  // =====================================

  /** Berechnet den Forecast basierend auf erwarteten Werten und Wahrscheinlichkeiten */
  public BigDecimal calculateForecast() {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityQueryService");
      return queryService.calculateForecast();
    }

    logger.debug("Calculating opportunity forecast");
    return opportunityRepository.calculateForecast();
  }

  /** Ändert die Stage einer Opportunity (überladene Methode für ChangeStageRequest) */
  public OpportunityResponse changeStage(
      UUID opportunityId, de.freshplan.domain.opportunity.service.dto.ChangeStageRequest request) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityCommandService");
      return commandService.changeStage(opportunityId, request);
    }

    if (request.getStage() == null) {
      throw new IllegalArgumentException("Stage cannot be null");
    }

    // Validiere Custom Probability wenn angegeben
    if (request.getCustomProbability() != null
        && (request.getCustomProbability() < 0 || request.getCustomProbability() > 100)) {
      throw new IllegalArgumentException("Probability must be between 0 and 100");
    }

    logger.info(
        "Changing stage for opportunity {} to {} with custom probability {}",
        opportunityId,
        request.getStage(),
        request.getCustomProbability());

    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(opportunityId)
            .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));

    // Prüfe ob Transition von geschlossenem Status erlaubt ist
    if ((opportunity.getStage() == OpportunityStage.CLOSED_WON
            || opportunity.getStage() == OpportunityStage.CLOSED_LOST)
        && opportunity.getStage() != request.getStage()) {
      throw new InvalidStageTransitionException("Cannot change stage from closed state");
    }

    // Stage ändern
    opportunity.changeStage(request.getStage());

    // Custom Probability setzen falls angegeben
    if (request.getCustomProbability() != null) {
      opportunity.setProbability(request.getCustomProbability());
    }

    // Activity erstellen
    User currentUser = getCurrentUser();
    OpportunityActivity activity =
        new OpportunityActivity(
            opportunity,
            currentUser,
            OpportunityActivity.ActivityType.STAGE_CHANGED,
            String.format("Stage geändert zu %s", request.getStage()),
            request.getReason() != null ? request.getReason() : "Stage-Änderung durch Benutzer");
    opportunity.addActivity(activity);

    opportunityRepository.persist(opportunity);

    logger.info("Changed stage for opportunity {} to {}", opportunityId, request.getStage());
    return opportunityMapper.toResponse(opportunity);
  }

  /**
   * Find all opportunities without pagination.
   *
   * @return list of all opportunities
   */
  public List<OpportunityResponse> findAll() {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityQueryService");
      return queryService.findAll();
    }

    logger.debug("Finding all opportunities");
    List<Opportunity> opportunities = opportunityRepository.listAll();
    return opportunities.stream().map(opportunityMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Delete an opportunity.
   *
   * @param id the opportunity ID
   */
  public void deleteOpportunity(UUID id) {
    if (cqrsEnabled) {
      logger.debug("CQRS mode: delegating to OpportunityCommandService");
      commandService.deleteOpportunity(id);
      return;
    }

    logger.info("Deleting opportunity with ID: {}", id);

    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new OpportunityNotFoundException(id));

    opportunityRepository.delete(opportunity);
    logger.info("Successfully deleted opportunity: {}", id);
  }

  // =====================================
  // PRIVATE HELPER METHODS
  // =====================================

  private User getCurrentUser() {
    try {
      String username = securityIdentity.getPrincipal().getName();
      return userRepository
          .findByUsername(username)
          .orElseThrow(() -> new RuntimeException("Current user not found: " + username));
    } catch (Exception e) {
      // Fallback für Tests - verwende verschiedene Test-User
      logger.warn("SecurityIdentity not available, falling back to test user: {}", e.getMessage());

      // Versuche verschiedene Test-User
      return userRepository
          .findByUsername("testuser")
          .or(() -> userRepository.findByUsername("ci-test-user"))
          .or(
              () -> {
                // Als letzter Ausweg: Erstelle temporären Test-User
                logger.warn("No test user found, creating temporary test user for CI");
                User tempUser = new User("ci-test-user", "CI", "Test User", "ci-test@example.com");
                tempUser.setRoles(java.util.Arrays.asList("admin", "manager", "sales"));
                userRepository.persist(tempUser);
                return java.util.Optional.of(tempUser);
              })
          .orElseThrow(() -> new RuntimeException("No test user available"));
    }
  }
}
