package de.freshplan.modules.leads.api;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.domain.shared.LeadSource;
import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadActivity;
import de.freshplan.modules.leads.domain.LeadContact;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.service.LeadProtectionService;
import de.freshplan.modules.leads.service.LeadService;
import de.freshplan.modules.leads.service.UserLeadSettingsService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * REST API for Lead Management. Implements user-based lead protection (NO geographical protection).
 * Territory is only used for business rules (currency/tax).
 *
 * <p>Uses @RlsContext to ensure PostgreSQL GUCs are set on the same connection as Hibernate
 * queries.
 */
@Path("/api/leads")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
@RlsContext // Ensures GUCs are set on correct connection
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeadResource {

  private static final Logger LOG = Logger.getLogger(LeadResource.class);

  @Context SecurityContext securityContext;

  @Inject EntityManager em;

  @Inject LeadService leadService;

  @Inject LeadProtectionService protectionService;

  @Inject UserLeadSettingsService settingsService;

  @Inject de.freshplan.modules.leads.service.LeadBackdatingService backdatingService;

  @Inject de.freshplan.modules.leads.service.LeadConvertService leadConvertService;

  @Inject de.freshplan.modules.leads.service.LeadScoringService leadScoringService;

  @Inject de.freshplan.infrastructure.security.XssSanitizer xssSanitizer;

  @Inject de.freshplan.infrastructure.security.SecurityAuditLogger securityAuditLogger;

  // Sprint 2.1.7 Code Review Fix: Clock injection (Issue #127)
  @Inject Clock clock;

  // Sprint 2.1.7.1: Opportunity Service for Lead→Opportunity traceability
  @Inject de.freshplan.domain.opportunity.service.OpportunityService opportunityService;

  @Context UriInfo uriInfo;

  @Context Request request;

  // Sort field whitelist for security and stability
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of(
          "createdAt",
          "updatedAt",
          "companyName",
          "status",
          "estimatedVolume",
          "city",
          "registeredAt",
          "lastActivityAt");

  @ConfigProperty(name = "app.dev.fallback-user-id", defaultValue = "dev-admin-001")
  String fallbackUserId;

  /**
   * Get current user ID with dev mode fallback. In dev mode, auth is disabled and
   * SecurityContext.getUserPrincipal() returns null.
   */
  private String getCurrentUserId() {
    return securityContext.getUserPrincipal() != null
        ? securityContext.getUserPrincipal().getName()
        : fallbackUserId; // Fallback for dev mode (configurable)
  }

  /**
   * GET /api/leads - List leads with pagination and filtering. Leads are available nationwide, no
   * geographical restrictions.
   */
  @GET
  @Transactional
  public Response listLeads(
      @QueryParam("status") LeadStatus status,
      @QueryParam("territoryId") String territoryId,
      @QueryParam("ownerUserId") String ownerUserId,
      @QueryParam("search") String search,
      @QueryParam("page") @DefaultValue("0") int pageIndex,
      @QueryParam("size") @DefaultValue("20") int pageSize,
      @QueryParam("sort") @DefaultValue("createdAt") String sortField,
      @QueryParam("direction") @DefaultValue("DESC") String sortDirection) {

    String currentUserId = getCurrentUserId();
    LOG.infof(
        "User %s listing leads - status: %s, territory: %s, owner: %s",
        currentUserId, status, territoryId, ownerUserId);

    // Build query filters using helper methods (PMD Complexity Refactoring)
    Map<String, Object> params = new HashMap<>();
    StringBuilder query = new StringBuilder("1=1");
    buildLeadListFilters(query, params, status, territoryId, ownerUserId);
    applySearchFilter(query, params, search, currentUserId);
    applyAccessControl(query, params, currentUserId);

    // Execute query with pagination using safe sort
    Sort sort = safeSort(sortField, sortDirection);
    Page page = Page.of(pageIndex, pageSize);

    // N+1 Prevention: Use HQL with JOIN FETCH to load contacts in ONE query
    // STEP 1: Count query (without JOIN FETCH - not needed for count)
    // ADR-007 Fix: Use HQL with alias "l" to match fetch query (ambiguous email column)
    String countHql = "SELECT COUNT(DISTINCT l) FROM Lead l WHERE " + query.toString();
    jakarta.persistence.TypedQuery<Long> countQuery = em.createQuery(countHql, Long.class);
    for (Map.Entry<String, Object> param : params.entrySet()) {
      countQuery.setParameter(param.getKey(), param.getValue());
    }
    long total = countQuery.getSingleResult();

    // STEP 2: Build HQL with JOIN FETCH + sorting
    String sortClause =
        sort.getColumns().stream()
            .map(
                col ->
                    "l."
                        + col.getName()
                        + " "
                        + (col.getDirection() == Sort.Direction.Descending ? "DESC" : "ASC"))
            .collect(Collectors.joining(", "));
    String hql =
        "SELECT DISTINCT l FROM Lead l LEFT JOIN FETCH l.contacts WHERE "
            + query.toString()
            + " ORDER BY "
            + sortClause;

    // STEP 3: Execute query with parameters
    jakarta.persistence.TypedQuery<Lead> typedQuery = em.createQuery(hql, Lead.class);
    for (Map.Entry<String, Object> param : params.entrySet()) {
      typedQuery.setParameter(param.getKey(), param.getValue());
    }
    typedQuery.setFirstResult(pageIndex * pageSize);
    typedQuery.setMaxResults(pageSize);

    List<Lead> entities = typedQuery.getResultList();

    // Convert to DTOs within the transaction to avoid lazy loading issues
    List<LeadDTO> items = entities.stream().map(LeadDTO::from).collect(Collectors.toList());

    // Build type-safe paginated response with DTOs
    PaginatedResponse<LeadDTO> response =
        PaginatedResponse.<LeadDTO>builder()
            .data(items)
            .page(pageIndex)
            .size(pageSize)
            .total(total)
            .build();

    // Generate weak collection ETag for caching (If-None-Match support)
    int hash = Objects.hash(total, entities.stream().map(l -> l.version).reduce(0L, Long::sum));
    EntityTag collectionTag = ETags.weakList(hash);

    // Check for 304 Not Modified
    Response.ResponseBuilder preconditions = request.evaluatePreconditions(collectionTag);
    if (preconditions != null) {
      return preconditions.tag(collectionTag).build();
    }

    return Response.ok(response).tag(collectionTag).build();
  }

  /**
   * GET /api/leads/{id} - Get lead by ID with ETag support (304 Not Modified). Access control:
   * owner, collaborators, or admin.
   */
  @GET
  @Path("/{id}")
  @Transactional
  public Response getLead(
      @PathParam("id") Long id, @HeaderParam("If-None-Match") String ifNoneMatch) {
    String currentUserId = getCurrentUserId();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // DATA ISOLATION: Non-admin users can only access their own leads or those they collaborate on
    if (!securityContext.isUserInRole("ADMIN")) {
      boolean isOwner = lead.ownerUserId.equals(currentUserId);
      boolean isCollaborator =
          lead.collaboratorUserIds != null && lead.collaboratorUserIds.contains(currentUserId);

      if (!isOwner && !isCollaborator) {
        securityAuditLogger.logUnauthorizedAccess(currentUserId, "LEAD", id.toString(), "READ");
        return Response.status(Response.Status.FORBIDDEN)
            .entity(Map.of("error", "Access denied"))
            .build();
      }
    }

    // Generate strong ETag for preconditions
    EntityTag etag = ETags.strongLead(lead.id, lead.version);

    // evaluatePreconditions handles If-None-Match (returns 304 if match)
    Response.ResponseBuilder preconditions = request.evaluatePreconditions(etag);
    if (preconditions != null) {
      return preconditions.tag(etag).build();
    }

    // Convert to DTO to avoid lazy loading issues
    LeadDTO dto = LeadDTO.from(lead);
    return Response.ok(dto).tag(etag).build();
  }

  /**
   * POST /api/leads - Create new lead. Lead creator becomes automatic owner (user-based
   * protection). PMD Complexity Refactoring: Delegates to helper methods (Issue #146).
   */
  @POST
  @Transactional
  public Response createLead(@Valid LeadCreateRequest request) {
    String currentUserId = getCurrentUserId();
    LOG.infof("User %s creating new lead for company: %s", currentUserId, request.companyName);

    // Check for email duplicate
    Response duplicateError = checkEmailDuplicate(request.email);
    if (duplicateError != null) {
      return duplicateError;
    }

    // Create and initialize lead
    Lead lead = new Lead();
    initializeLeadBasicFields(lead, request);
    initializeLeadOwnership(lead, request, currentUserId);

    // Apply Pre-Claim logic (Variante B - Handelsvertretervertrag §2(8)(a))
    Response preClaimError = applyPreClaimLogic(lead, request);
    if (preClaimError != null) {
      return preClaimError;
    }

    // Persist lead before creating related entities
    lead.persist();

    // Create primary contact (structured or legacy format)
    createPrimaryContact(lead, request, currentUserId);

    // Process activities array (First Contact Documentation)
    processLeadActivities(lead, request, currentUserId);

    // Create initial activity (use LEAD_ASSIGNED instead of CREATED - V258 constraint)
    createAndPersistActivity(lead, currentUserId, ActivityType.LEAD_ASSIGNED, "Lead created");

    LOG.infof("Created lead %s for user %s", lead.id, currentUserId);

    // Return created lead with location header
    URI location = uriInfo.getAbsolutePathBuilder().path(lead.id.toString()).build();

    // Force load of contacts collection before converting to DTO (avoid lazy loading)
    em.flush();
    em.refresh(lead);
    lead.contacts.size();

    LeadDTO dto = LeadDTO.from(lead);
    LOG.infof("Lead %s created with %d contacts", dto.id, dto.contacts.size());

    return Response.created(location).entity(dto).build();
  }

  /**
   * PATCH /api/leads/{id} - Update lead with optimistic locking (If-Match required). Returns 428 if
   * If-Match missing, 412 if version conflict. Supports status transitions and stop-the-clock
   * feature.
   */
  @PATCH
  @Path("/{id}")
  @Transactional
  public Response updateLead(
      @PathParam("id") Long id,
      @HeaderParam("If-Match") String ifMatch,
      LeadUpdateRequest updateRequest) {

    // Require If-Match header for optimistic locking
    if (ifMatch == null || ifMatch.isEmpty()) {
      return Response.status(428) // Precondition Required
          .entity(Map.of("error", "Missing If-Match header for optimistic locking"))
          .build();
    }

    String currentUserId = getCurrentUserId();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // Check permission - only owner or admin can update
    boolean isAdmin = securityContext.isUserInRole("ADMIN");
    boolean isOwner = lead.ownerUserId.equals(currentUserId);

    if (!isOwner && !isAdmin) {
      securityAuditLogger.logUnauthorizedAccess(currentUserId, "LEAD", id.toString(), "UPDATE");
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Only lead owner or admin can update"))
          .build();
    }

    // Check ETag matches current version using strong comparison
    EntityTag currentEtag = ETags.strongLead(lead.id, lead.version);
    Response.ResponseBuilder preconditions = request.evaluatePreconditions(currentEtag);
    if (preconditions != null) {
      return preconditions.tag(currentEtag).build();
    }

    // Apply updates using helper methods to reduce complexity
    Response emailConflict = applyBasicFieldUpdates(lead, updateRequest);
    if (emailConflict != null) {
      return emailConflict;
    }

    Response statusError = applyStatusTransition(lead, updateRequest, currentUserId);
    if (statusError != null) {
      return statusError;
    }

    applyStopClockFeature(lead, updateRequest, currentUserId, isAdmin);
    applyCollaboratorUpdates(lead, updateRequest);
    applyRelationshipFields(lead, updateRequest);
    applyPainDimensionFields(lead, updateRequest);

    lead.updatedBy = currentUserId;

    // Sprint 2.1.6+ Lead Scoring: Recalculate scores BEFORE persist/flush
    leadScoringService.updateLeadScore(lead);

    lead.persist();
    lead.flush();

    LOG.infof("Updated lead %s by user %s", id, currentUserId);
    EntityTag newEtag = ETags.strongLead(lead.id, lead.version);
    LeadDTO dto = LeadDTO.from(lead);
    return Response.ok(dto).tag(newEtag).build();
  }

  /** Apply basic field updates with XSS sanitization. Returns error response if email conflict. */
  private Response applyBasicFieldUpdates(Lead lead, LeadUpdateRequest req) {
    // PMD Complexity Refactoring (Issue #146) - Extracted to helper methods
    applyCompanyAndContactFields(lead, req);
    Response emailConflict = applyEmailField(lead, req);
    if (emailConflict != null) return emailConflict;
    applyAddressFields(lead, req);
    applyBusinessFields(lead, req);
    return null;
  }

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper methods for applyBasicFieldUpdates()
  // ============================================================================

  private void applyCompanyAndContactFields(Lead lead, LeadUpdateRequest req) {
    if (req.companyName != null) {
      lead.companyName = xssSanitizer.sanitizeStrict(req.companyName);
    }
    if (req.contactPerson != null) {
      lead.contactPerson = xssSanitizer.sanitizeStrict(req.contactPerson);
    }
  }

  private Response applyEmailField(Lead lead, LeadUpdateRequest req) {
    if (req.email != null) {
      String newNormalizedEmail = Lead.normalizeEmail(req.email);
      if (newNormalizedEmail != null && !newNormalizedEmail.equals(lead.emailNormalized)) {
        Long duplicateCount =
            Lead.count(
                "emailNormalized = ?1 and id != ?2 and status != ?3",
                newNormalizedEmail,
                lead.id,
                LeadStatus.DELETED);
        if (duplicateCount > 0) {
          return Response.status(Response.Status.CONFLICT)
              .entity(Map.of("error", "Email already exists for another lead"))
              .build();
        }
      }
      lead.email = req.email;
      lead.emailNormalized = newNormalizedEmail;
    }
    return null;
  }

  private void applyAddressFields(Lead lead, LeadUpdateRequest req) {
    if (req.phone != null) lead.phone = xssSanitizer.sanitizeStrict(req.phone);
    if (req.website != null) lead.website = xssSanitizer.sanitizeStrict(req.website);
    if (req.street != null) lead.street = xssSanitizer.sanitizeStrict(req.street);
    if (req.postalCode != null) lead.postalCode = xssSanitizer.sanitizeStrict(req.postalCode);
    if (req.city != null) lead.city = xssSanitizer.sanitizeStrict(req.city);
  }

  private void applyBusinessFields(Lead lead, LeadUpdateRequest req) {
    if (req.businessType != null) lead.businessType = BusinessType.fromString(req.businessType);
    if (req.kitchenSize != null) lead.kitchenSize = KitchenSize.fromString(req.kitchenSize);
    if (req.employeeCount != null) lead.employeeCount = req.employeeCount;
    if (req.estimatedVolume != null) lead.estimatedVolume = req.estimatedVolume;
    if (req.budgetConfirmed != null) lead.budgetConfirmed = req.budgetConfirmed;
    if (req.dealSize != null) {
      lead.dealSize = de.freshplan.domain.shared.DealSize.valueOf(req.dealSize);
    }
  }

  /** Apply status transition with state machine validation. Returns error response if invalid. */
  private Response applyStatusTransition(Lead lead, LeadUpdateRequest req, String currentUserId) {
    if (req.status == null || req.status == lead.status) {
      return null;
    }

    if (!protectionService.canTransitionStatus(lead, req.status, currentUserId)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Invalid status transition"))
          .build();
    }

    LeadStatus oldStatus = lead.status;
    lead.status = req.status;
    lead.lastActivityAt = LocalDateTime.now(clock);

    createAndPersistActivity(
        lead,
        currentUserId,
        ActivityType.STATUS_CHANGE,
        "Status changed from " + oldStatus + " to " + req.status);

    if (req.status == LeadStatus.GRACE_PERIOD) {
      lead.gracePeriodStartAt = LocalDateTime.now(clock);
    } else if (req.status == LeadStatus.EXPIRED) {
      lead.expiredAt = LocalDateTime.now(clock);
    }
    return null;
  }

  /** Apply stop-the-clock feature with cumulative pause duration tracking. */
  private void applyStopClockFeature(
      Lead lead, LeadUpdateRequest req, String currentUserId, boolean isAdmin) {
    if (req.stopClock == null) {
      return;
    }

    var settings = settingsService.getOrCreateForUser(currentUserId);

    if (req.stopClock && (settings.canStopClock || isAdmin)) {
      lead.clockStoppedAt = LocalDateTime.now(clock);
      lead.stopReason = req.stopReason;
      lead.stopApprovedBy = currentUserId;
      createAndPersistActivity(
          lead, currentUserId, ActivityType.CLOCK_STOPPED, "Clock stopped: " + req.stopReason);
    } else if (!req.stopClock && lead.clockStoppedAt != null) {
      var now = LocalDateTime.now(clock);
      var pauseDuration = java.time.Duration.between(lead.clockStoppedAt, now);
      if (lead.progressPauseTotalSeconds == null) lead.progressPauseTotalSeconds = 0L;
      lead.progressPauseTotalSeconds += pauseDuration.toSeconds();

      lead.clockStoppedAt = null;
      lead.stopReason = null;
      lead.stopApprovedBy = null;
      createAndPersistActivity(
          lead,
          currentUserId,
          ActivityType.CLOCK_RESUMED,
          "Clock resumed (paused: " + pauseDuration.toMinutes() + " minutes)");
    }
  }

  /** Apply collaborator additions and removals. */
  private void applyCollaboratorUpdates(Lead lead, LeadUpdateRequest req) {
    if (req.addCollaborators != null) {
      for (String userId : req.addCollaborators) {
        lead.addCollaborator(userId);
      }
    }
    if (req.removeCollaborators != null) {
      for (String userId : req.removeCollaborators) {
        lead.removeCollaborator(userId);
      }
    }
  }

  /** Apply relationship dimension field updates with XSS sanitization. */
  private void applyRelationshipFields(Lead lead, LeadUpdateRequest req) {
    if (req.relationshipStatus != null) {
      lead.relationshipStatus =
          de.freshplan.modules.leads.domain.RelationshipStatus.valueOf(req.relationshipStatus);
    }
    if (req.decisionMakerAccess != null) {
      lead.decisionMakerAccess =
          de.freshplan.modules.leads.domain.DecisionMakerAccess.valueOf(req.decisionMakerAccess);
    }
    if (req.competitorInUse != null) {
      lead.competitorInUse = xssSanitizer.sanitizeStrict(req.competitorInUse);
    }
    if (req.internalChampionName != null) {
      lead.internalChampionName = xssSanitizer.sanitizeStrict(req.internalChampionName);
    }
  }

  /** Apply pain dimension field updates. */
  private void applyPainDimensionFields(Lead lead, LeadUpdateRequest req) {
    // PMD Complexity Refactoring (Issue #146) - Extracted to helper methods
    applyPrimaryPainPoints(lead, req);
    applySecondaryPainPoints(lead, req);
    applyPainMetadata(lead, req);
  }

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper methods for applyPainDimensionFields()
  // ============================================================================

  private void applyPrimaryPainPoints(Lead lead, LeadUpdateRequest req) {
    if (req.painStaffShortage != null) lead.painStaffShortage = req.painStaffShortage;
    if (req.painHighCosts != null) lead.painHighCosts = req.painHighCosts;
    if (req.painFoodWaste != null) lead.painFoodWaste = req.painFoodWaste;
    if (req.painQualityInconsistency != null) {
      lead.painQualityInconsistency = req.painQualityInconsistency;
    }
  }

  private void applySecondaryPainPoints(Lead lead, LeadUpdateRequest req) {
    if (req.painUnreliableDelivery != null)
      lead.painUnreliableDelivery = req.painUnreliableDelivery;
    if (req.painPoorService != null) lead.painPoorService = req.painPoorService;
    if (req.painSupplierQuality != null) lead.painSupplierQuality = req.painSupplierQuality;
    if (req.painTimePressure != null) lead.painTimePressure = req.painTimePressure;
  }

  private void applyPainMetadata(Lead lead, LeadUpdateRequest req) {
    if (req.urgencyLevel != null) {
      lead.urgencyLevel = de.freshplan.modules.leads.domain.UrgencyLevel.valueOf(req.urgencyLevel);
    }
    if (req.multiPainBonus != null) lead.multiPainBonus = req.multiPainBonus;
    if (req.painNotes != null) {
      lead.painNotes = xssSanitizer.sanitizeLenient(req.painNotes);
    }
  }

  /**
   * DELETE /api/leads/{id} - Delete lead (soft delete by setting status to DELETED). Only MANAGER
   * or ADMIN can delete. Requires If-Match header for safe deletion.
   */
  @DELETE
  @Path("/{id}")
  @RolesAllowed({"MANAGER", "ADMIN"})
  @Transactional
  public Response deleteLead(@PathParam("id") Long id, @HeaderParam("If-Match") String ifMatch) {

    // Require If-Match header for safe deletion
    if (ifMatch == null || ifMatch.isEmpty()) {
      return Response.status(428) // Precondition Required
          .entity(Map.of("error", "Missing If-Match header for safe deletion"))
          .build();
    }
    String currentUserId = getCurrentUserId();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // Check permission: MANAGER and ADMIN can delete any lead, USER only their own
    boolean isAdmin = securityContext.isUserInRole("ADMIN");
    boolean isManager = securityContext.isUserInRole("MANAGER");
    boolean isOwner = lead.ownerUserId.equals(currentUserId);

    if (!isManager && !isAdmin && !isOwner) {
      securityAuditLogger.logRoleViolation(currentUserId, "MANAGER", "DELETE_LEAD");
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Only lead owner, manager or admin can delete"))
          .build();
    }

    // Log privileged delete action
    if (isManager || isAdmin) {
      securityAuditLogger.logPrivilegedAction(
          currentUserId, isAdmin ? "ADMIN" : "MANAGER", "DELETE", "LEAD", id.toString());
    }

    // Check ETag for safe deletion
    EntityTag currentEtag = ETags.strongLead(lead.id, lead.version);
    Response.ResponseBuilder preconditions = request.evaluatePreconditions(currentEtag);
    if (preconditions != null) {
      return preconditions.tag(currentEtag).build();
    }

    // Soft delete by setting status
    lead.status = LeadStatus.DELETED;
    lead.updatedBy = currentUserId;
    lead.persist();

    // Log deletion activity (use NOTE instead of DELETED - V258 constraint)
    createAndPersistActivity(lead, currentUserId, ActivityType.NOTE, "Lead deleted");

    LOG.infof("Deleted lead %s by user %s", id, currentUserId);

    // Return new ETag after deletion
    EntityTag newEtag = ETags.strongLead(lead.id, lead.version);
    return Response.noContent().tag(newEtag).build();
  }

  /**
   * POST /api/leads/{id}/activities - Add activity to lead. Updates lastActivityAt for protection
   * system.
   */
  @POST
  @Path("/{id}/activities")
  @Transactional
  public Response addActivity(@PathParam("id") Long id, @Valid ActivityRequest request) {
    String currentUserId = getCurrentUserId();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // DATA ISOLATION: Non-admin users can only add activities to their own leads
    if (!securityContext.isUserInRole("ADMIN")) {
      boolean isOwner = lead.ownerUserId.equals(currentUserId);
      boolean isCollaborator =
          lead.collaboratorUserIds != null && lead.collaboratorUserIds.contains(currentUserId);

      if (!isOwner && !isCollaborator) {
        securityAuditLogger.logUnauthorizedAccess(
            currentUserId, "LEAD_ACTIVITIES", id.toString(), "CREATE");
        return Response.status(Response.Status.FORBIDDEN)
            .entity(Map.of("error", "Access denied"))
            .build();
      }
    }

    // Validate and convert activity type
    ActivityType activityType;
    try {
      activityType = ActivityType.valueOf(request.activityType.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Invalid activity type: " + request.activityType))
          .build();
    }

    // Sprint 2.1.7 Issue #126: Validate and convert outcome if provided
    de.freshplan.modules.leads.domain.ActivityOutcome activityOutcome = null;
    if (request.outcome != null && !request.outcome.isBlank()) {
      try {
        activityOutcome =
            de.freshplan.modules.leads.domain.ActivityOutcome.valueOf(
                request.outcome.toUpperCase());
      } catch (IllegalArgumentException e) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", "Invalid activity outcome: " + request.outcome))
            .build();
      }
    }

    // Create activity using helper method
    LeadActivity activity =
        createAndPersistActivity(lead, currentUserId, activityType, request.description);

    // Set outcome if provided (Sprint 2.1.7 Issue #126)
    if (activityOutcome != null) {
      activity.outcome = activityOutcome;
      // Sprint 2.1.7 Code Review Fix: Remove redundant persist()
      // activity.persist(); // REMOVED: Activity already persisted by createAndPersistActivity()
    }

    // Update lead's last activity timestamp (important for protection system)
    // Sprint 2.1.7 Code Review Fix: Use Clock injection (Issue #127)
    lead.lastActivityAt = LocalDateTime.now(clock);
    lead.persist();

    LOG.infof("Added activity to lead %s by user %s", id, currentUserId);
    // Return DTO to avoid lazy loading issues
    return Response.ok(LeadActivityDTO.from(activity)).build();
  }

  /** GET /api/leads/{id}/activities - Get activities for a lead. */
  @GET
  @Path("/{id}/activities")
  @Transactional
  public Response getActivities(
      @PathParam("id") Long id,
      @QueryParam("page") @DefaultValue("0") int pageIndex,
      @QueryParam("size") @DefaultValue("50") int pageSize) {

    String currentUserId = getCurrentUserId();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // DATA ISOLATION: Non-admin users can only access activities for their own leads
    if (!securityContext.isUserInRole("ADMIN")) {
      boolean isOwner = lead.ownerUserId.equals(currentUserId);
      boolean isCollaborator =
          lead.collaboratorUserIds != null && lead.collaboratorUserIds.contains(currentUserId);

      if (!isOwner && !isCollaborator) {
        securityAuditLogger.logUnauthorizedAccess(
            currentUserId, "LEAD_ACTIVITIES", id.toString(), "READ");
        return Response.status(Response.Status.FORBIDDEN)
            .entity(Map.of("error", "Access denied"))
            .build();
      }
    }

    // Get activities with pagination
    Page page = Page.of(pageIndex, pageSize);
    List<LeadActivity> activities =
        LeadActivity.find("lead", Sort.descending("createdAt"), lead).page(page).list();
    long total = LeadActivity.count("lead", lead);

    // Convert to DTOs to avoid lazy loading issues
    List<LeadActivityDTO> dtos =
        activities.stream().map(LeadActivityDTO::from).collect(Collectors.toList());

    PaginatedResponse<LeadActivityDTO> response =
        PaginatedResponse.<LeadActivityDTO>builder()
            .data(dtos)
            .page(pageIndex)
            .size(pageSize)
            .total(total)
            .build();

    return Response.ok(response).build();
  }

  /**
   * Create safe Sort object with field whitelist validation. Falls back to createdAt DESC if field
   * not allowed.
   */
  private Sort safeSort(String field, String direction) {
    String safeField = ALLOWED_SORT_FIELDS.contains(field) ? field : "createdAt";
    boolean descending = "DESC".equalsIgnoreCase(direction);
    return descending ? Sort.descending(safeField) : Sort.ascending(safeField);
  }

  /**
   * PUT /api/leads/{id}/registered-at - Backdate registeredAt timestamp (Admin/Manager only).
   *
   * <p>Allows setting historical registration dates for Bestandsleads-Migration. Recalculates
   * protection and progress deadlines based on new registeredAt.
   *
   * <p>Sprint 2.1.6 - User Story 4
   */
  @PUT
  @Path("/{id}/registered-at")
  @RolesAllowed({"ADMIN", "MANAGER"})
  @Transactional
  public Response updateRegisteredAt(
      @PathParam("id") Long id,
      @jakarta.validation.Valid
          de.freshplan.modules.leads.api.admin.dto.BackdatingRequest request) {

    String currentUserId = getCurrentUserId();
    LOG.infof(
        "Backdating request for lead %d by user %s: newDate=%s",
        id, currentUserId, request.registeredAt);

    try {
      de.freshplan.modules.leads.api.admin.dto.BackdatingResponse response =
          backdatingService.updateRegisteredAt(id, request, currentUserId);

      return Response.ok(response).build();

    } catch (jakarta.ws.rs.NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Lead not found: " + id))
          .build();
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    }
  }

  /**
   * Convert Lead to Customer.
   *
   * <p>Endpoint: POST /api/leads/{id}/convert
   *
   * <p>Admin/Manager only. Creates a new Customer record from the Lead data and optionally marks
   * the Lead as CONVERTED for audit purposes.
   *
   * <p>Sprint 2.1.6 - User Story 2
   */
  @POST
  @Path("/{id}/convert")
  @RolesAllowed({"ADMIN", "MANAGER"})
  @Transactional
  public Response convertToCustomer(
      @PathParam("id") Long id,
      @jakarta.validation.Valid
          de.freshplan.modules.leads.api.admin.dto.LeadConvertRequest request) {

    String currentUserId = getCurrentUserId();
    LOG.infof("Convert request for lead %d by user %s", id, currentUserId);

    try {
      de.freshplan.modules.leads.api.admin.dto.LeadConvertResponse response =
          leadConvertService.convertToCustomer(id, request, currentUserId);

      return Response.status(Response.Status.CREATED).entity(response).build();

    } catch (jakarta.ws.rs.NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("Lead not found: " + id))
          .build();
    } catch (IllegalStateException | IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse(e.getMessage()))
          .build();
    } catch (Exception e) {
      LOG.errorf(e, "Failed to convert lead %d: %s", id, e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Internal server error during conversion"))
          .build();
    }
  }

  /**
   * Add first contact to a Pre-Claim lead (Sprint 2.1.6 Phase 5).
   *
   * <p>Transitions lead from VORMERKUNG (Stage 0) to REGISTRIERUNG (Stage 1).
   *
   * <p>Endpoint: POST /api/leads/{id}/first-contact
   *
   * <p>Security: All authenticated users (USER, MANAGER, ADMIN)
   *
   * @param id Lead ID
   * @param request First contact details
   * @return Updated lead
   */
  @POST
  @Path("/{id}/first-contact")
  @Transactional
  public Response addFirstContact(
      @PathParam("id") Long id,
      @Valid de.freshplan.modules.leads.api.AddFirstContactRequest request) {

    String currentUserId = getCurrentUserId();

    try {
      Lead updatedLead =
          leadService.addFirstContact(
              id,
              request.contactPerson,
              request.email,
              request.phone,
              request.contactDate,
              request.notes,
              currentUserId);

      return Response.ok(updatedLead).build();

    } catch (WebApplicationException e) {
      // Re-throw WebApplicationException (404, 400) as-is
      throw e;
    } catch (Exception e) {
      LOG.errorf(e, "Failed to add first contact to lead %d: %s", id, e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Internal server error adding first contact"))
          .build();
    }
  }

  /**
   * Helper method to create and persist activity entries. Reduces code duplication and ensures
   * consistent activity logging across all lead operations.
   *
   * @param lead The lead entity
   * @param userId The user performing the action
   * @param type The activity type
   * @param description The activity description
   * @return The persisted activity
   */
  private LeadActivity createAndPersistActivity(
      Lead lead, String userId, ActivityType type, String description) {
    LeadActivity activity = new LeadActivity();
    activity.lead = lead;
    activity.userId = userId;
    activity.activityType = type;
    activity.description = description;
    activity.persist();
    return activity;
  }

  // ===========================
  // Lead Contacts Management - Sprint 2.1.6 Phase 5+
  // ===========================

  /**
   * GET /api/leads/{id}/contacts - Get all contacts for a lead
   *
   * @param leadId Lead ID
   * @return List of contacts
   */
  @GET
  @Path("/{id}/contacts")
  @Transactional
  public Response getLeadContacts(@PathParam("id") Long leadId) {
    try {
      // 1. Verify lead exists
      Lead lead = em.find(Lead.class, leadId);
      if (lead == null) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorResponse("Lead not found"))
            .build();
      }

      // 2. Load all active contacts for this lead
      List<LeadContact> contacts =
          em.createQuery(
                  "SELECT c FROM LeadContact c WHERE c.lead.id = :leadId AND c.isDeleted = false ORDER BY c.isPrimary DESC, c.createdAt DESC",
                  LeadContact.class)
              .setParameter("leadId", leadId)
              .getResultList();

      // 3. Map to DTOs
      List<LeadContactDTO> contactDTOs =
          contacts.stream()
              .map(this::mapContactToDTO)
              .collect(java.util.stream.Collectors.toList());

      return Response.ok(contactDTOs).build();

    } catch (Exception e) {
      LOG.errorf(e, "Failed to get contacts for lead %d: %s", leadId, e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Failed to get contacts"))
          .build();
    }
  }

  /**
   * POST /api/leads/{id}/contacts - Create new contact for lead
   *
   * @param leadId Lead ID
   * @param contactDTO Contact data
   * @return Created contact with HTTP 201
   */
  @POST
  @Path("/{id}/contacts")
  @Transactional
  public Response createLeadContact(
      @PathParam("id") Long leadId, @Valid LeadContactDTO contactDTO) {

    String currentUserId = getCurrentUserId();

    try {
      // 1. Load Lead
      Lead lead = Lead.findById(leadId);
      if (lead == null) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorResponse("Lead not found"))
            .build();
      }

      // 2. Create LeadContact entity
      LeadContact contact = new LeadContact();
      contact.setLead(lead);
      contact.setFirstName(contactDTO.getFirstName());
      contact.setLastName(contactDTO.getLastName());
      contact.setSalutation(contactDTO.getSalutation());
      contact.setTitle(contactDTO.getTitle());
      contact.setPosition(contactDTO.getPosition());
      contact.setDecisionLevel(contactDTO.getDecisionLevel());
      contact.setEmail(contactDTO.getEmail());
      contact.setPhone(contactDTO.getPhone());
      contact.setMobile(contactDTO.getMobile());
      contact.setPrimary(contactDTO.isPrimary());
      contact.setActive(true);
      // Relationship Data - CRM Intelligence
      contact.setBirthday(contactDTO.getBirthday());
      contact.setHobbies(contactDTO.getHobbies());
      contact.setFamilyStatus(contactDTO.getFamilyStatus());
      contact.setChildrenCount(contactDTO.getChildrenCount());
      contact.setPersonalNotes(contactDTO.getPersonalNotes());
      contact.setCreatedBy(currentUserId);
      contact.setUpdatedBy(currentUserId);

      // 3. If this is primary contact, unset other primary contacts
      if (contactDTO.isPrimary()) {
        em.createQuery("UPDATE LeadContact c SET c.isPrimary = false WHERE c.lead.id = :leadId")
            .setParameter("leadId", leadId)
            .executeUpdate();
      }

      // 4. Persist
      contact.persist();

      // Note: No activity log for technical CRUD operations
      // Audit trail is handled via updated_by/updated_at fields

      // 5. Build response DTO
      LeadContactDTO responseDTO = mapContactToDTO(contact);

      return Response.created(URI.create(uriInfo.getPath() + "/" + contact.getId()))
          .entity(responseDTO)
          .build();

    } catch (Exception e) {
      LOG.errorf(e, "Failed to create contact for lead %d: %s", leadId, e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Failed to create contact"))
          .build();
    }
  }

  /**
   * PATCH /api/leads/{id}/contacts/{contactId} - Update existing contact
   *
   * @param leadId Lead ID
   * @param contactId Contact ID (UUID)
   * @param contactDTO Updated contact data
   * @return Updated contact
   */
  @PATCH
  @Path("/{id}/contacts/{contactId}")
  @Transactional
  public Response updateLeadContact(
      @PathParam("id") Long leadId,
      @PathParam("contactId") String contactId,
      @Valid LeadContactDTO contactDTO) {

    String currentUserId = getCurrentUserId();

    try {
      // 1. Load and validate contact
      LeadContact contact = em.find(LeadContact.class, java.util.UUID.fromString(contactId));
      if (contact == null || !contact.getLead().id.equals(leadId)) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorResponse("Contact not found"))
            .build();
      }

      // 2. Apply field updates (PATCH semantics)
      applyContactFieldUpdates(contact, contactDTO, currentUserId);

      // 3. Handle primary flag uniqueness
      if (contactDTO.isPrimary() && !contact.isPrimary()) {
        em.createQuery("UPDATE LeadContact c SET c.isPrimary = false WHERE c.lead.id = :leadId")
            .setParameter("leadId", leadId)
            .executeUpdate();
        contact.setPrimary(true);
      }

      // 4. Persist and return
      em.merge(contact);
      return Response.ok(mapContactToDTO(contact)).build();

    } catch (Exception e) {
      LOG.errorf(
          e, "Failed to update contact %s for lead %d: %s", contactId, leadId, e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Failed to update contact"))
          .build();
    }
  }

  /**
   * DELETE /api/leads/{id}/contacts/{contactId} - Soft delete contact
   *
   * @param leadId Lead ID
   * @param contactId Contact ID (UUID)
   * @return HTTP 204 No Content
   */
  @DELETE
  @Path("/{id}/contacts/{contactId}")
  @Transactional
  public Response deleteLeadContact(
      @PathParam("id") Long leadId, @PathParam("contactId") String contactId) {

    String currentUserId = getCurrentUserId();

    try {
      // 1. Load contact
      LeadContact contact = em.find(LeadContact.class, java.util.UUID.fromString(contactId));
      if (contact == null || !contact.getLead().id.equals(leadId)) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorResponse("Contact not found"))
            .build();
      }

      // 2. Soft delete
      contact.setIsDeleted(true);
      contact.setActive(false);
      contact.setUpdatedBy(currentUserId);
      em.merge(contact);

      // Note: No activity log for technical CRUD operations
      // Audit trail is handled via updated_by/updated_at fields

      return Response.noContent().build();

    } catch (Exception e) {
      LOG.errorf(
          e, "Failed to delete contact %s for lead %d: %s", contactId, leadId, e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Failed to delete contact"))
          .build();
    }
  }

  /**
   * PATCH /api/leads/{id}/contacts/{contactId}/primary - Set contact as primary
   *
   * @param leadId Lead ID
   * @param contactId Contact ID (UUID)
   * @return Updated contact
   */
  @PATCH
  @Path("/{id}/contacts/{contactId}/primary")
  @Transactional
  public Response setContactAsPrimary(
      @PathParam("id") Long leadId, @PathParam("contactId") String contactId) {

    String currentUserId = getCurrentUserId();

    try {
      // 1. Load contact
      LeadContact contact = em.find(LeadContact.class, java.util.UUID.fromString(contactId));
      if (contact == null || !contact.getLead().id.equals(leadId)) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new ErrorResponse("Contact not found"))
            .build();
      }

      // 2. Unset all other primary contacts for this lead
      em.createQuery("UPDATE LeadContact c SET c.isPrimary = false WHERE c.lead.id = :leadId")
          .setParameter("leadId", leadId)
          .executeUpdate();

      // 3. Set this contact as primary
      contact.setPrimary(true);
      contact.setUpdatedBy(currentUserId);
      em.merge(contact);

      // 4. Create activity log
      createAndPersistActivity(
          contact.getLead(),
          currentUserId,
          ActivityType.NOTE,
          "Hauptkontakt gesetzt: " + contact.getFullName());

      // 5. Build response DTO
      LeadContactDTO responseDTO = mapContactToDTO(contact);

      return Response.ok(responseDTO).build();

    } catch (Exception e) {
      LOG.errorf(
          e,
          "Failed to set contact %s as primary for lead %d: %s",
          contactId,
          leadId,
          e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Failed to set primary contact"))
          .build();
    }
  }

  /**
   * Recalculate lead score (manual trigger).
   *
   * <p>Sprint 2.1.6+ Lead Scoring System
   *
   * <p>Use cases:
   *
   * <ul>
   *   <li>After batch updates to lead data
   *   <li>After ICP configuration changes
   *   <li>User-requested recalculation (UI button)
   * </ul>
   *
   * @param id Lead ID (Long, not UUID - this project uses Long for Lead IDs)
   * @return Updated scores
   */
  @POST
  @Path("/{id}/recalculate-score")
  @RolesAllowed({"USER", "ADMIN"})
  @Transactional
  public Response recalculateScore(@PathParam("id") Long id) {
    String currentUserId = getCurrentUserId();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // Recalculate scores
    leadScoringService.updateLeadScore(lead);
    lead.persist();

    LOG.infof("Lead %s score manually recalculated by user %s", id, currentUserId);

    return Response.ok(
            Map.of(
                "leadScore", lead.leadScore != null ? lead.leadScore : 0,
                "painScore", lead.painScore != null ? lead.painScore : 0,
                "revenueScore", lead.revenueScore != null ? lead.revenueScore : 0,
                "fitScore", lead.fitScore != null ? lead.fitScore : 0,
                "engagementScore", lead.engagementScore != null ? lead.engagementScore : 0))
        .build();
  }

  /**
   * Get all opportunities for a specific lead Sprint 2.1.7.1 - Lead → Opportunity Traceability
   *
   * @param id Lead ID
   * @return List of OpportunityResponse DTOs
   */
  @GET
  @Path("/{id}/opportunities")
  @RolesAllowed({"USER", "MANAGER", "ADMIN"})
  @Transactional
  public Response getLeadOpportunities(@PathParam("id") Long id) {
    LOG.infof("Fetching opportunities for lead ID: %d", id);

    // Verify lead exists
    Lead lead = Lead.findById(id);
    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    List<de.freshplan.domain.opportunity.service.dto.OpportunityResponse> opportunities =
        opportunityService.findByLeadId(id);

    LOG.infof("Found %d opportunities for lead ID: %d", opportunities.size(), id);

    return Response.ok(opportunities).build();
  }

  // ============================================================================
  // HELPER METHODS FOR listLeads() - PMD Complexity Refactoring (Issue #146)
  // ============================================================================

  /** Build query filters for lead listing. */
  private void buildLeadListFilters(
      StringBuilder query,
      Map<String, Object> params,
      LeadStatus status,
      String territoryId,
      String ownerUserId) {
    if (status != null) {
      query.append(" and status = :status");
      params.put("status", status);
    }
    if (territoryId != null) {
      query.append(" and territory.id = :territoryId");
      params.put("territoryId", territoryId);
    }
    if (ownerUserId != null) {
      query.append(" and ownerUserId = :ownerUserId");
      params.put("ownerUserId", ownerUserId);
    }
  }

  /** Apply search filter with SQL injection protection. */
  private void applySearchFilter(
      StringBuilder query, Map<String, Object> params, String search, String currentUserId) {
    if (search == null || search.trim().isEmpty()) {
      return;
    }
    String sanitizedSearch = xssSanitizer.sanitizeStrict(search).toLowerCase();

    // Log potential injection attempt
    if (search.contains("'")
        || search.contains(";")
        || search.contains("--")
        || search.contains("DROP")) {
      securityAuditLogger.logInjectionAttempt(currentUserId, "SQL_INJECTION", search, "/api/leads");
    }

    query.append(
        " and (lower(companyName) like :search or lower(contactPerson) like :search"
            + " or lower(l.email) like :search or lower(city) like :search)");
    params.put("search", "%" + sanitizedSearch + "%");
  }

  /** Apply user-based access control for non-admin users. */
  private void applyAccessControl(
      StringBuilder query, Map<String, Object> params, String currentUserId) {
    if (!securityContext.isUserInRole("ADMIN")) {
      query.append(
          " and (ownerUserId = :currentUser or :currentUser in elements(collaboratorUserIds))");
      params.put("currentUser", currentUserId);
    }
  }

  // ============================================================================
  // HELPER METHODS FOR createLead() - PMD Complexity Refactoring (Issue #146)
  // ============================================================================

  /**
   * Check for email duplicate during lead creation.
   *
   * @return Error Response if duplicate found, null otherwise
   */
  private Response checkEmailDuplicate(String email) {
    String normalizedEmail = Lead.normalizeEmail(email);
    if (normalizedEmail != null) {
      Long duplicateCount =
          Lead.count("emailNormalized = ?1 and status != ?2", normalizedEmail, LeadStatus.DELETED);
      if (duplicateCount > 0) {
        return Response.status(Response.Status.CONFLICT)
            .entity(Map.of("error", "Email already exists for another lead"))
            .build();
      }
    }
    return null;
  }

  /** Initialize lead with basic fields from request (XSS sanitized). */
  private void initializeLeadBasicFields(Lead lead, LeadCreateRequest request) {
    lead.companyName = xssSanitizer.sanitizeStrict(request.companyName);
    lead.contactPerson = xssSanitizer.sanitizeStrict(request.contactPerson);
    lead.email = request.email;
    lead.emailNormalized = Lead.normalizeEmail(request.email);
    lead.phone = xssSanitizer.sanitizeStrict(request.phone);
    lead.website = xssSanitizer.sanitizeStrict(request.website);
    lead.street = xssSanitizer.sanitizeStrict(request.street);
    lead.postalCode = xssSanitizer.sanitizeStrict(request.postalCode);
    lead.city = xssSanitizer.sanitizeStrict(request.city);
    lead.countryCode = request.countryCode;

    // Set territory based on country
    Territory territory = Territory.findByCountryCode(lead.countryCode);
    if (territory == null) {
      territory = Territory.findByCountryCode("DE");
    }
    lead.territory = territory;

    // B2B-specific fields
    lead.businessType =
        request.businessType != null ? BusinessType.fromString(request.businessType) : null;
    lead.kitchenSize =
        request.kitchenSize != null ? KitchenSize.fromString(request.kitchenSize) : null;
    lead.employeeCount = request.employeeCount;
    lead.estimatedVolume = request.estimatedVolume;
    lead.industry = request.industry;
  }

  /** Set ownership, source, and protection periods for a new lead. */
  private void initializeLeadOwnership(Lead lead, LeadCreateRequest request, String currentUserId) {
    lead.ownerUserId = currentUserId;
    lead.createdBy = currentUserId;
    lead.source =
        request.source != null ? LeadSource.fromString(request.source) : LeadSource.WEB_FORMULAR;
    lead.sourceCampaign = request.sourceCampaign;

    var settings = settingsService.getOrCreateForUser(currentUserId);
    lead.protectionMonths = settings.leadProtectionMonths;
    lead.protectionDays60 = settings.activityReminderDays;
    lead.protectionDays10 = settings.gracePeriodDays;
  }

  /**
   * Apply Pre-Claim logic based on lead source (Variante B). Returns error Response if validation
   * fails.
   */
  private Response applyPreClaimLogic(Lead lead, LeadCreateRequest request) {
    lead.registeredAt = LocalDateTime.now(clock);

    if (lead.source != null && lead.source.requiresFirstContact()) {
      // MESSE/TELEFON: Requires first contact documentation
      boolean hasFirstContactActivity =
          request.activities != null
              && request.activities.stream()
                  .anyMatch(a -> "FIRST_CONTACT_DOCUMENTED".equals(a.activityType));

      if (!hasFirstContactActivity) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(
                Map.of(
                    "error", "First contact required",
                    "message",
                        "MESSE/TELEFON leads require first contact documentation (date + notes)",
                    "source", lead.source.name()))
            .build();
      }

      lead.status = LeadStatus.REGISTERED;
      lead.stage =
          request.stage != null ? LeadStage.fromValue(request.stage) : LeadStage.REGISTRIERUNG;
      lead.firstContactDocumentedAt = LocalDateTime.now(clock);

      LOG.infof(
          "Lead %s (%s source): Direct REGISTRIERUNG (first contact documented via activities)",
          lead.companyName, lead.source.name());
    } else {
      // Other sources: Pre-Claim allowed
      lead.status = LeadStatus.REGISTERED;
      lead.stage =
          request.stage != null ? LeadStage.fromValue(request.stage) : LeadStage.VORMERKUNG;
      lead.firstContactDocumentedAt = null;

      LOG.infof(
          "Lead %s (%s source): VORMERKUNG with Pre-Claim (10 days to document first contact)",
          lead.companyName, lead.source != null ? lead.source.name() : "UNKNOWN");
    }
    return null;
  }

  /** Create primary contact from structured or legacy contact data. */
  private void createPrimaryContact(Lead lead, LeadCreateRequest request, String currentUserId) {
    if (request.contact != null
        && request.contact.firstName != null
        && !request.contact.firstName.isBlank()) {
      // NEW: Structured contact data
      LeadContact primaryContact = new LeadContact();
      primaryContact.setLead(lead);
      primaryContact.setFirstName(request.contact.firstName);
      primaryContact.setLastName(request.contact.lastName);
      primaryContact.setEmail(request.contact.email);
      primaryContact.setPhone(request.contact.phone);
      primaryContact.setPrimary(true);
      primaryContact.setActive(true);
      primaryContact.setCreatedBy(currentUserId);
      primaryContact.persist();

      LOG.infof(
          "Created primary contact for lead %s: %s %s (email: %s)",
          lead.id, request.contact.firstName, request.contact.lastName, request.contact.email);

    } else if (request.contactPerson != null && !request.contactPerson.isBlank()) {
      // LEGACY: Backward compatibility
      boolean hasContactMethod =
          (request.email != null && !request.email.isBlank())
              || (request.phone != null && !request.phone.isBlank());

      if (hasContactMethod) {
        String[] nameParts = request.contactPerson.trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        LeadContact primaryContact = new LeadContact();
        primaryContact.setLead(lead);
        primaryContact.setFirstName(firstName);
        primaryContact.setLastName(lastName);
        primaryContact.setEmail(request.email);
        primaryContact.setPhone(request.phone);
        primaryContact.setPrimary(true);
        primaryContact.setActive(true);
        primaryContact.setCreatedBy(currentUserId);
        primaryContact.persist();

        LOG.infof(
            "Created primary contact for lead %s from legacy contactPerson: %s (split: %s %s)",
            lead.id, request.contactPerson, firstName, lastName);
      } else {
        LOG.infof("Lead %s created with contactPerson but NO contact methods (Pre-Claim)", lead.id);
      }
    } else {
      LOG.infof("VORMERKUNG lead %s created without contact data (Pre-Claim)", lead.id);
    }
  }

  /** Apply PATCH-style field updates to a LeadContact (only non-null fields are updated). */
  private void applyContactFieldUpdates(LeadContact contact, LeadContactDTO dto, String userId) {
    // PMD Complexity Refactoring (Issue #146) - Extracted to helper methods
    applyContactBasicInfo(contact, dto);
    applyContactDetails(contact, dto);
    applyContactRelationshipData(contact, dto);
    contact.setUpdatedBy(userId);
  }

  // ============================================================================
  // PMD Complexity Refactoring (Issue #146) - Helper methods for applyContactFieldUpdates()
  // ============================================================================

  private void applyContactBasicInfo(LeadContact contact, LeadContactDTO dto) {
    if (dto.getFirstName() != null) contact.setFirstName(dto.getFirstName());
    if (dto.getLastName() != null) contact.setLastName(dto.getLastName());
    if (dto.getSalutation() != null) contact.setSalutation(dto.getSalutation());
    if (dto.getTitle() != null) contact.setTitle(dto.getTitle());
    if (dto.getPosition() != null) contact.setPosition(dto.getPosition());
    if (dto.getDecisionLevel() != null) contact.setDecisionLevel(dto.getDecisionLevel());
  }

  private void applyContactDetails(LeadContact contact, LeadContactDTO dto) {
    if (dto.getEmail() != null) contact.setEmail(dto.getEmail());
    if (dto.getPhone() != null) contact.setPhone(dto.getPhone());
    if (dto.getMobile() != null) contact.setMobile(dto.getMobile());
  }

  private void applyContactRelationshipData(LeadContact contact, LeadContactDTO dto) {
    // Relationship Data - CRM Intelligence
    if (dto.getBirthday() != null) contact.setBirthday(dto.getBirthday());
    if (dto.getHobbies() != null) contact.setHobbies(dto.getHobbies());
    if (dto.getFamilyStatus() != null) contact.setFamilyStatus(dto.getFamilyStatus());
    if (dto.getChildrenCount() != null) contact.setChildrenCount(dto.getChildrenCount());
    if (dto.getPersonalNotes() != null) contact.setPersonalNotes(dto.getPersonalNotes());
  }

  /** Process activities array during lead creation. */
  private void processLeadActivities(Lead lead, LeadCreateRequest request, String currentUserId) {
    if (request.activities == null || request.activities.isEmpty()) {
      return;
    }

    for (var activityData : request.activities) {
      try {
        ActivityType activityType = ActivityType.valueOf(activityData.activityType.toUpperCase());

        LeadActivity activity = new LeadActivity();
        activity.lead = lead;
        activity.userId = currentUserId;
        activity.activityType = activityType;
        activity.description = activityData.summary;

        if (activityData.performedAt != null) {
          activity.activityDate = activityData.performedAt.atStartOfDay();
        }

        if (activityData.countsAsProgress != null) {
          activity.countsAsProgress = activityData.countsAsProgress;
        }

        activity.persist();

        LOG.infof(
            "Created activity %s for lead %s on %s: %s",
            activityType, lead.id, activityData.performedAt, activityData.summary);

      } catch (IllegalArgumentException e) {
        LOG.warnf("Invalid activity type %s in request, skipping", activityData.activityType);
      }
    }
  }

  /**
   * Helper: Map LeadContact entity to LeadContactDTO
   *
   * <p>REFACTORING: Delegates to LeadContactDTO.fromEntity() to centralize mapping logic and
   * eliminate code duplication (Gemini Code Review Fix).
   */
  private LeadContactDTO mapContactToDTO(LeadContact contact) {
    return LeadContactDTO.fromEntity(contact);
  }

  /** Simple error response DTO. */
  private static class ErrorResponse {
    public String error;

    public ErrorResponse(String error) {
      this.error = error;
    }
  }
}
