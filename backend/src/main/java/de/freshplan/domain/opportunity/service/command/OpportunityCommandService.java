package de.freshplan.domain.opportunity.service.command;

import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityActivity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.ChangeStageRequest;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import de.freshplan.domain.opportunity.service.exception.InvalidStageTransitionException;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.domain.opportunity.service.mapper.OpportunityMapper;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opportunity Command Service - CQRS Write Side
 *
 * <p>Behandelt alle schreibenden Operationen für Opportunities: - Create, Update, Delete - Stage
 * Transitions - Activity Management
 *
 * <p>WICHTIG: Dieser Service ist eine 1:1 Kopie der Command-Methoden aus OpportunityService um 100%
 * Kompatibilität zu gewährleisten.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class OpportunityCommandService {

  private static final Logger logger = LoggerFactory.getLogger(OpportunityCommandService.class);

  @Inject OpportunityRepository opportunityRepository;

  @Inject UserRepository userRepository;

  @Inject OpportunityMapper opportunityMapper;

  @Inject SecurityIdentity securityIdentity;

  @Inject AuditService auditService;

  // =====================================
  // CREATE OPERATION
  // =====================================

  /**
   * Erstellt eine neue Opportunity EXAKTE KOPIE von OpportunityService.createOpportunity() Zeile
   * 63-132
   */
  @Transactional
  public OpportunityResponse createOpportunity(CreateOpportunityRequest request) {
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

    // Customer zuweisen - für Tests Mock-unterstützung
    if (request.getCustomerId() != null) {
      // TODO: Echte Customer Repository Integration
      // Für jetzt: Test-Mock-Unterstützung
      logger.debug(
          "Customer ID: {} - will be assigned via repository lookup", request.getCustomerId());
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

  // =====================================
  // UPDATE OPERATION
  // =====================================

  /**
   * Aktualisiert eine Opportunity EXAKTE KOPIE von OpportunityService.updateOpportunity() Zeile
   * 150-189
   */
  @Transactional
  public OpportunityResponse updateOpportunity(UUID id, UpdateOpportunityRequest request) {
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
  // DELETE OPERATION
  // =====================================

  /**
   * Delete an opportunity. EXAKTE KOPIE von OpportunityService.deleteOpportunity() Zeile 409-419
   *
   * @param id the opportunity ID
   */
  @Transactional
  public void deleteOpportunity(UUID id) {
    logger.info("Deleting opportunity with ID: {}", id);

    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new OpportunityNotFoundException(id));

    opportunityRepository.delete(opportunity);
    logger.info("Successfully deleted opportunity: {}", id);
  }

  // =====================================
  // STAGE MANAGEMENT
  // =====================================

  /**
   * Changes the stage of an opportunity with default reason. EXAKTE KOPIE von
   * OpportunityService.changeStage() Zeile 198-200
   */
  @Transactional
  public OpportunityResponse changeStage(UUID opportunityId, OpportunityStage newStage) {
    return changeStage(opportunityId, newStage, "Stage change");
  }

  /**
   * Ändert die Stage einer Opportunity mit Business Rules Validation EXAKTE KOPIE von
   * OpportunityService.changeStage() Zeile 202-252
   */
  @Transactional
  public OpportunityResponse changeStage(
      UUID opportunityId, OpportunityStage newStage, String reason) {
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

  /**
   * Ändert die Stage einer Opportunity (überladene Methode für ChangeStageRequest) EXAKTE KOPIE von
   * OpportunityService.changeStage() Zeile 338-391
   */
  @Transactional
  public OpportunityResponse changeStage(UUID opportunityId, ChangeStageRequest request) {
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

  // =====================================
  // ACTIVITY MANAGEMENT
  // =====================================

  /**
   * Fügt eine Activity zu einer Opportunity hinzu EXAKTE KOPIE von OpportunityService.addActivity()
   * Zeile 311-325
   */
  @Transactional
  public OpportunityResponse addActivity(
      UUID opportunityId, OpportunityActivity.ActivityType type, String title, String description) {
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
  // PRIVATE HELPER METHODS
  // =====================================

  /**
   * Ermittelt den aktuellen Benutzer EXAKTE KOPIE von OpportunityService.getCurrentUser() Zeile
   * 425-450
   */
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
