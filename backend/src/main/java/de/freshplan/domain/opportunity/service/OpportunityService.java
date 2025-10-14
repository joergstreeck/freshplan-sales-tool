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
import de.freshplan.domain.opportunity.service.dto.ConvertToCustomerRequest;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityForCustomerRequest;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityFromLeadRequest;
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
  // LEAD → OPPORTUNITY CONVERSION
  // =====================================

  /**
   * Creates an Opportunity from a qualified Lead.
   *
   * <p>Business Rules:
   *
   * <ul>
   *   <li>Lead must be QUALIFIED or ACTIVE status
   *   <li>Opportunity starts in NEW_LEAD stage (10% probability)
   *   <li>lead_id FK is set (enables Lead → Opportunity traceability)
   *   <li>Name auto-generated if not provided: "Vertragschance - {companyName} ({dealType})"
   *   <li>Owner transferred from Lead to Opportunity
   * </ul>
   *
   * @param leadId the lead ID to convert
   * @param request the opportunity creation parameters
   * @return the created opportunity response
   * @throws IllegalArgumentException if lead not found or not qualified
   * @since Sprint 2.1.6.2 Phase 2 (V10026)
   */
  @Transactional
  public OpportunityResponse createFromLead(Long leadId, CreateOpportunityFromLeadRequest request) {
    logger.info("Creating opportunity from lead ID: {}", leadId);

    // 1. Load Lead (using Panache Active Record pattern)
    de.freshplan.modules.leads.domain.Lead lead =
        de.freshplan.modules.leads.domain.Lead.<de.freshplan.modules.leads.domain.Lead>findByIdOptional(leadId)
            .orElseThrow(
                () -> new IllegalArgumentException("Lead not found with ID: " + leadId));

    // 2. Validate Lead Status (must be QUALIFIED or ACTIVE)
    if (lead.status != de.freshplan.modules.leads.domain.LeadStatus.QUALIFIED
        && lead.status != de.freshplan.modules.leads.domain.LeadStatus.ACTIVE) {
      throw new IllegalArgumentException(
          String.format(
              "Lead must be QUALIFIED or ACTIVE to create opportunity. Current status: %s",
              lead.status));
    }

    // 3. Determine opportunity name
    String opportunityName = request.getName();
    if (opportunityName == null || opportunityName.isBlank()) {
      opportunityName = generateOpportunityName(lead, request);
    }

    // 4. Determine assigned user (from request or lead owner)
    User assignedUser = null;
    if (request.getAssignedTo() != null) {
      assignedUser =
          userRepository
              .findByIdOptional(request.getAssignedTo())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "User not found with ID: " + request.getAssignedTo()));
    } else if (lead.ownerUserId != null) {
      // Try to find user by lead's ownerUserId (String)
      assignedUser = userRepository.findByUsername(lead.ownerUserId).orElse(null);
    }

    // Fallback to current user if no assignee found
    if (assignedUser == null) {
      assignedUser = getCurrentUser();
    }

    // 5. Create Opportunity entity
    Opportunity opportunity = new Opportunity(opportunityName, OpportunityStage.NEW_LEAD, assignedUser);

    // 6. Set lead FK (enables Lead → Opportunity → Customer workflow)
    opportunity.setLead(lead);

    // 7. Set business fields from request
    if (request.getDescription() != null) {
      opportunity.setDescription(request.getDescription());
    }
    if (request.getExpectedValue() != null) {
      opportunity.setExpectedValue(request.getExpectedValue());
    }
    if (request.getExpectedCloseDate() != null) {
      opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
    }

    // 8. Save to DB
    opportunityRepository.persist(opportunity);

    // 8b. Set Lead to CONVERTED (Industry Standard: ONE-WAY conversion)
    lead.status = de.freshplan.modules.leads.domain.LeadStatus.CONVERTED;
    lead.updatedAt = java.time.LocalDateTime.now();
    lead.persist();
    logger.info("Lead {} marked as CONVERTED (converted to Opportunity {})", leadId, opportunity.getId());

    // 9. Create initial activity
    User currentUser = getCurrentUser();
    OpportunityActivity activity =
        new OpportunityActivity(
            opportunity,
            currentUser,
            OpportunityActivity.ActivityType.NOTE,
            "Opportunity erstellt aus Lead",
            String.format(
                "Opportunity wurde aus Lead '%s' erstellt. %s",
                lead.companyName,
                request.getDealType() != null ? "Deal-Typ: " + request.getDealType() : ""));
    opportunity.addActivity(activity);

    // 10. Audit Log
    try {
      auditService.logAsync(
          AuditContext.builder()
              .eventType(AuditEventType.OPPORTUNITY_CREATED)
              .entityType("opportunity")
              .entityId(opportunity.getId())
              .newValue(
                  String.format(
                      "Created from Lead ID %d: %s", leadId, opportunity.getName()))
              .changeReason("Lead → Opportunity conversion")
              .build());
    } catch (Exception e) {
      logger.warn("Failed to log audit event for opportunity creation from lead", e);
    }

    logger.info(
        "Created opportunity '{}' (ID: {}) from lead ID: {}",
        opportunityName,
        opportunity.getId(),
        leadId);

    return opportunityMapper.toResponse(opportunity);
  }

  /**
   * Generates a default opportunity name from lead data.
   *
   * <p>Pattern: "Vertragschance - {companyName} ({dealType})"
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>"Vertragschance - ABC Catering GmbH (Liefervertrag)"
   *   <li>"Vertragschance - XYZ Restaurant (Rahmenvertrag)"
   * </ul>
   *
   * @param lead the source lead
   * @param request the opportunity request (for dealType)
   * @return generated opportunity name
   */
  private String generateOpportunityName(
      de.freshplan.modules.leads.domain.Lead lead, CreateOpportunityFromLeadRequest request) {
    String companyName = lead.companyName;
    String dealType = request.getDealType();

    if (dealType != null && !dealType.isBlank()) {
      return String.format("Vertragschance - %s (%s)", companyName, dealType);
    } else {
      return String.format("Vertragschance - %s", companyName);
    }
  }

  /**
   * Converts a won Opportunity to a Customer.
   *
   * <p>Business Rules:
   *
   * <ul>
   *   <li>Opportunity must be CLOSED_WON stage
   *   <li>Creates Customer with data from Opportunity and Lead
   *   <li>Sets opportunity.customer_id to link back
   *   <li>Sets customer.originalLeadId if opportunity came from lead (V261 field)
   *   <li>Copies pain points and business data from lead
   *   <li>Can optionally create initial Location and Contact
   * </ul>
   *
   * @param opportunityId ID of the opportunity to convert
   * @param request Additional customer data (address, contact options)
   * @return Created customer entity
   * @throws OpportunityNotFoundException if opportunity not found
   * @throws IllegalStateException if opportunity not CLOSED_WON or already has customer
   * @since Sprint 2.1.6.2 Phase 2 (V10026)
   */
  @Transactional
  public Customer convertToCustomer(UUID opportunityId, ConvertToCustomerRequest request) {
    logger.info("Converting opportunity {} to customer", opportunityId);

    // 1. Load Opportunity
    Opportunity opportunity =
        opportunityRepository
            .findByIdOptional(opportunityId)
            .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));

    // 2. Validate Stage (must be CLOSED_WON)
    if (opportunity.getStage() != OpportunityStage.CLOSED_WON) {
      throw new IllegalStateException(
          String.format(
              "Only won opportunities can be converted to customers. Current stage: %s",
              opportunity.getStage()));
    }

    // 3. Check if already has customer
    if (opportunity.getCustomer() != null) {
      throw new IllegalStateException(
          String.format(
              "Opportunity already linked to customer ID: %s",
              opportunity.getCustomer().getId()));
    }

    // 4. Create Customer entity
    Customer customer = new Customer();

    // 5. Set company data (from request, opportunity, or lead)
    String companyName =
        request.getCompanyName() != null ? request.getCompanyName() : opportunity.getName();
    customer.setCompanyName(companyName);

    // 6. Generate customer number
    customer.setCustomerNumber(generateCustomerNumber());

    // 7. Set initial status and lifecycle
    customer.setStatus(de.freshplan.domain.customer.entity.CustomerStatus.AKTIV);
    customer.setLifecycleStage(
        de.freshplan.domain.customer.entity.CustomerLifecycleStage.ONBOARDING);

    // 8. Copy data from Lead (if opportunity came from lead)
    if (opportunity.getLead() != null) {
      de.freshplan.modules.leads.domain.Lead lead = opportunity.getLead();

      // Set originalLeadId (V261 field - enables Lead → Customer traceability)
      customer.setOriginalLeadId(lead.id);
      logger.debug("Linked customer to original lead ID: {}", lead.id);

      // Copy business data
      if (lead.businessType != null) {
        customer.setBusinessType(
            de.freshplan.domain.shared.BusinessType.valueOf(lead.businessType.name()));
      }
      if (lead.estimatedVolume != null) {
        customer.setExpectedAnnualVolume(lead.estimatedVolume);
      }

      // Copy pain points (8 boolean flags + notes)
      if (lead.painStaffShortage != null) {
        customer.setPainStaffShortage(lead.painStaffShortage);
      }
      if (lead.painHighCosts != null) {
        customer.setPainHighCosts(lead.painHighCosts);
      }
      if (lead.painFoodWaste != null) {
        customer.setPainFoodWaste(lead.painFoodWaste);
      }
      if (lead.painQualityInconsistency != null) {
        customer.setPainQualityInconsistency(lead.painQualityInconsistency);
      }
      if (lead.painTimePressure != null) {
        customer.setPainTimePressure(lead.painTimePressure);
      }
      if (lead.painSupplierQuality != null) {
        customer.setPainSupplierQuality(lead.painSupplierQuality);
      }
      if (lead.painUnreliableDelivery != null) {
        customer.setPainUnreliableDelivery(lead.painUnreliableDelivery);
      }
      if (lead.painPoorService != null) {
        customer.setPainPoorService(lead.painPoorService);
      }
      if (lead.painNotes != null) {
        customer.setPainNotes(lead.painNotes);
      }

      logger.debug("Copied pain points and business data from lead {}", lead.id);
    }

    // 9. Override expected volume from opportunity if set
    if (opportunity.getExpectedValue() != null) {
      customer.setExpectedAnnualVolume(opportunity.getExpectedValue());
    }

    // 10. Set audit fields
    User currentUser = getCurrentUser();
    customer.setCreatedBy(currentUser.getUsername());
    customer.setUpdatedBy(currentUser.getUsername());

    // 11. Persist customer
    customerRepository.persist(customer);
    logger.info("Created customer {} with number {}", customer.getId(), customer.getCustomerNumber());

    // 12. Link opportunity to customer
    opportunity.setCustomer(customer);
    opportunityRepository.persist(opportunity);

    // 13. Create activity log
    OpportunityActivity activity =
        new OpportunityActivity(
            opportunity,
            currentUser,
            OpportunityActivity.ActivityType.NOTE,
            "Opportunity zu Kunde konvertiert",
            String.format(
                "Kunde '%s' (Kundennummer: %s) wurde aus dieser Opportunity erstellt. %s",
                customer.getCompanyName(),
                customer.getCustomerNumber(),
                opportunity.getLead() != null
                    ? "Lead-Daten wurden übernommen (Lead ID: " + opportunity.getLead().id + ")"
                    : ""));
    opportunity.addActivity(activity);

    // 14. Audit Log
    try {
      auditService.logAsync(
          AuditContext.builder()
              .eventType(AuditEventType.CUSTOMER_CREATED) // Use CUSTOMER_CREATED for conversion
              .entityType("customer")
              .entityId(customer.getId())
              .newValue(
                  String.format(
                      "Customer created from Opportunity %s (Lead ID: %s)",
                      opportunityId,
                      opportunity.getLead() != null ? opportunity.getLead().id : "none"))
              .changeReason("Opportunity → Customer conversion")
              .build());
    } catch (Exception e) {
      logger.warn("Failed to log audit event for customer creation from opportunity", e);
    }

    logger.info(
        "Converted opportunity {} to customer {} (from lead {})",
        opportunityId,
        customer.getId(),
        opportunity.getLead() != null ? opportunity.getLead().id : "none");

    return customer;
  }

  /**
   * Generates next customer number in format KD-XXXXX.
   *
   * <p>Uses PostgreSQL sequence for atomic, guaranteed-unique customer numbers.
   * This prevents race conditions when multiple concurrent requests create customers.
   *
   * <p>Migration V10028 creates the sequence and initializes it.
   *
   * @return generated customer number (e.g., "KD-00001")
   * @since Sprint 2.1.7 Code Review Fix (Race Condition)
   */
  private String generateCustomerNumber() {
    // Use PostgreSQL sequence for atomic number generation (race condition safe)
    Long nextVal =
        (Long)
            customerRepository
                .getEntityManager()
                .createNativeQuery("SELECT nextval('customer_number_seq')")
                .getSingleResult();
    return String.format("KD-%05d", nextVal);
  }

  /**
   * Creates an Opportunity for an existing Customer (Upsell/Cross-sell/Renewal).
   *
   * <p>Business Rules:
   *
   * <ul>
   *   <li>Customer must exist and be AKTIV status
   *   <li>customer_id is set, lead_id remains NULL
   *   <li>Stage starts at NEEDS_ANALYSIS (not NEW_LEAD - customer is already qualified)
   *   <li>Owner can be assigned or left unassigned
   * </ul>
   *
   * <p>Use Cases:
   *
   * <ul>
   *   <li>Upsell: Existing product line expansion
   *   <li>Cross-sell: New product categories
   *   <li>Renewal: Contract renewal/extension
   * </ul>
   *
   * @param customerId ID of the customer
   * @param request Opportunity data (type, value, timeline)
   * @return Created opportunity response DTO (Sprint 2.1.7 Code Review Fix: Consistency)
   * @throws IllegalArgumentException if customer not found
   * @throws IllegalStateException if customer not AKTIV
   * @since Sprint 2.1.6.2 Phase 2 (V10026)
   */
  @Transactional
  public OpportunityResponse createForCustomer(
      UUID customerId, CreateOpportunityForCustomerRequest request) {
    logger.info("Creating opportunity for customer {}", customerId);

    // 1. Load Customer
    Customer customer =
        customerRepository
            .findByIdOptional(customerId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Customer not found with ID: " + customerId));

    // 2. Validate Customer Status (must be AKTIV)
    if (customer.getStatus() != de.freshplan.domain.customer.entity.CustomerStatus.AKTIV) {
      throw new IllegalStateException(
          String.format(
              "Customer must be active to create opportunity. Current status: %s",
              customer.getStatus()));
    }

    // 3. Determine opportunity name
    String opportunityName =
        request.getName() != null
            ? request.getName()
            : generateCustomerOpportunityName(customer, request);

    // 4. Determine opportunity description
    String opportunityDescription =
        request.getDescription() != null
            ? request.getDescription()
            : generateCustomerOpportunityDescription(customer, request);

    // 5. Determine initial stage (customer opportunities start at NEEDS_ANALYSIS)
    OpportunityStage initialStage =
        request.getStage() != null ? request.getStage() : OpportunityStage.NEEDS_ANALYSIS;

    // 6. Determine assigned user
    User assignedUser = null;
    if (request.getAssignedTo() != null) {
      assignedUser =
          userRepository
              .findByIdOptional(request.getAssignedTo())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "User not found with ID: " + request.getAssignedTo()));
    } else {
      // Use current user as fallback
      assignedUser = getCurrentUser();
    }

    // 7. Create Opportunity entity
    Opportunity opportunity = new Opportunity(opportunityName, initialStage, assignedUser);

    // 8. Set customer FK
    opportunity.setCustomer(customer);

    // 9. Set description (with opportunity type prefix if provided)
    if (request.getOpportunityType() != null) {
      opportunityDescription =
          String.format("[%s] %s", request.getOpportunityType(), opportunityDescription);
    }
    opportunity.setDescription(opportunityDescription);

    // 10. Set business fields
    if (request.getExpectedValue() != null) {
      opportunity.setExpectedValue(request.getExpectedValue());
    }
    if (request.getExpectedCloseDate() != null) {
      opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
    }
    // Probability is automatically set by stage in Opportunity constructor

    // 11. Persist opportunity
    opportunityRepository.persist(opportunity);

    // 12. Create initial activity
    User currentUser = getCurrentUser();
    String activityDescription =
        String.format(
            "Opportunity erstellt für Kunde %s (Kundennummer: %s). Typ: %s",
            customer.getCompanyName(),
            customer.getCustomerNumber(),
            request.getOpportunityType() != null ? request.getOpportunityType() : "Standard");
    OpportunityActivity activity =
        new OpportunityActivity(
            opportunity,
            currentUser,
            OpportunityActivity.ActivityType.NOTE,
            "Opportunity erstellt für bestehenden Kunden",
            activityDescription);
    opportunity.addActivity(activity);

    // 13. Audit Log
    try {
      auditService.logAsync(
          AuditContext.builder()
              .eventType(AuditEventType.OPPORTUNITY_CREATED)
              .entityType("opportunity")
              .entityId(opportunity.getId())
              .newValue(
                  String.format(
                      "Created for Customer %s (%s): %s",
                      customerId, customer.getCustomerNumber(), opportunityName))
              .changeReason(
                  String.format(
                      "Customer → Opportunity (%s)",
                      request.getOpportunityType() != null
                          ? request.getOpportunityType()
                          : "Standard"))
              .build());
    } catch (Exception e) {
      logger.warn("Failed to log audit event for customer opportunity creation", e);
    }

    logger.info(
        "Created opportunity '{}' (ID: {}) for customer {} (Type: {})",
        opportunityName,
        opportunity.getId(),
        customerId,
        request.getOpportunityType());

    // Sprint 2.1.7 Code Review Fix: Return DTO instead of entity for consistency
    return opportunityMapper.toResponse(opportunity);
  }

  /**
   * Generates opportunity name for customer-initiated opportunities.
   *
   * <p>Pattern: "{companyName} - {type} {timeframe}"
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>"ABC Catering GmbH - Upsell Q3 2025"
   *   <li>"XYZ Restaurant - Cross-sell"
   *   <li>"DEF Hotel - Renewal 2026"
   * </ul>
   *
   * @param customer the customer entity
   * @param request the opportunity request
   * @return generated opportunity name
   */
  private String generateCustomerOpportunityName(
      Customer customer, CreateOpportunityForCustomerRequest request) {
    String type = request.getOpportunityType() != null ? request.getOpportunityType() : "Erweiterung";
    String timeframe = request.getTimeframe() != null ? request.getTimeframe() : "";

    return String.format("%s - %s %s", customer.getCompanyName(), type, timeframe).trim();
  }

  /**
   * Generates opportunity description for customer-initiated opportunities.
   *
   * <p>Includes customer context and current annual volume for reference.
   *
   * @param customer the customer entity
   * @param request the opportunity request
   * @return generated opportunity description
   */
  private String generateCustomerOpportunityDescription(
      Customer customer, CreateOpportunityForCustomerRequest request) {
    String type = request.getOpportunityType() != null ? request.getOpportunityType() : "Upsell";

    return String.format(
        "%s-Opportunity für bestehenden Kunden %s (Kundennummer: %s). "
            + "Aktuelles Jahresvolumen: %s EUR",
        type,
        customer.getCompanyName(),
        customer.getCustomerNumber(),
        customer.getExpectedAnnualVolume() != null
            ? customer.getExpectedAnnualVolume()
            : "N/A");
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
