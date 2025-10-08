package de.freshplan.modules.leads.api;

import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.KitchenSize;
import de.freshplan.domain.shared.LeadSource;
import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadActivity;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.service.LeadProtectionService;
import de.freshplan.modules.leads.service.LeadService;
import de.freshplan.modules.leads.service.UserLeadSettingsService;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
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

  @Inject LeadService leadService;

  @Inject LeadProtectionService protectionService;

  @Inject UserLeadSettingsService settingsService;

  @Inject de.freshplan.modules.leads.service.LeadBackdatingService backdatingService;

  @Inject de.freshplan.modules.leads.service.LeadConvertService leadConvertService;

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

    // Build query parameters
    Map<String, Object> params = new HashMap<>();
    StringBuilder query = new StringBuilder("1=1");

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

    if (search != null && !search.trim().isEmpty()) {
      query.append(
          " and (lower(companyName) like :search or lower(contactPerson) like :search"
              + " or lower(email) like :search or lower(city) like :search)");
      params.put("search", "%" + search.toLowerCase() + "%");
    }

    // For non-admin users: show only leads they own or collaborate on
    if (!securityContext.isUserInRole("ADMIN")) {
      query.append(
          " and (ownerUserId = :currentUser or :currentUser in elements(collaboratorUserIds))");
      params.put("currentUser", currentUserId);
    }

    // Execute query with pagination using safe sort
    Sort sort = safeSort(sortField, sortDirection);
    Page page = Page.of(pageIndex, pageSize);

    // Create ONE PanacheQuery and use it for both list() and count()
    PanacheQuery<Lead> pq = Lead.find(query.toString(), sort, params).page(page);

    List<Lead> entities = pq.list();
    long total = pq.count(); // Use the SAME query's count() method

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

    // RLS handles access control - if lead is null, user has no access
    // No additional check needed since RLS policies filter at database level

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
   * protection).
   */
  @POST
  @Transactional
  public Response createLead(@Valid LeadCreateRequest request) {
    String currentUserId = getCurrentUserId();
    LOG.infof("User %s creating new lead for company: %s", currentUserId, request.companyName);

    // Check for email duplicate
    String normalizedEmail = Lead.normalizeEmail(request.email);
    if (normalizedEmail != null) {
      Long duplicateCount =
          Lead.count("emailNormalized = ?1 and status != ?2", normalizedEmail, LeadStatus.DELETED);
      if (duplicateCount > 0) {
        return Response.status(Response.Status.CONFLICT)
            .entity(Map.of("error", "Email already exists for another lead"))
            .build();
      }
    }

    // Create new lead
    Lead lead = new Lead();
    lead.companyName = request.companyName;
    lead.contactPerson = request.contactPerson;
    lead.email = request.email;
    lead.emailNormalized = normalizedEmail;
    lead.phone = request.phone;
    lead.website = request.website;
    lead.street = request.street;
    lead.postalCode = request.postalCode;
    lead.city = request.city;
    lead.countryCode = request.countryCode;

    // Set territory based on country (for currency/tax rules only)
    Territory territory = Territory.findByCountryCode(lead.countryCode);
    if (territory == null) {
      territory = Territory.findByCountryCode("DE"); // Default to Germany
    }
    lead.territory = territory;

    // B2B-specific fields - Sprint 2.1.6 Phase 5: String → Enum conversion
    lead.businessType = request.businessType != null ? BusinessType.fromString(request.businessType) : null;
    lead.kitchenSize = request.kitchenSize != null ? KitchenSize.fromString(request.kitchenSize) : null;
    lead.employeeCount = request.employeeCount;
    lead.estimatedVolume = request.estimatedVolume;
    lead.industry = request.industry;

    // Set ownership and protection
    lead.ownerUserId = currentUserId;
    lead.status = LeadStatus.REGISTERED;
    lead.stage = LeadStage.VORMERKUNG; // Sprint 2.1.6 - Progressive Profiling Stage
    lead.createdBy = currentUserId;
    lead.source = request.source != null ? LeadSource.fromString(request.source) : LeadSource.WEB_FORMULAR;
    lead.sourceCampaign = request.sourceCampaign;

    // Apply user's lead settings for protection periods
    var settings = settingsService.getOrCreateForUser(currentUserId);
    lead.protectionMonths = settings.leadProtectionMonths;
    lead.protectionDays60 = settings.activityReminderDays;
    lead.protectionDays10 = settings.gracePeriodDays;

    // Persist lead
    lead.persist();

    // Create initial activity (use LEAD_ASSIGNED instead of CREATED - V258 constraint)
    createAndPersistActivity(lead, currentUserId, ActivityType.LEAD_ASSIGNED, "Lead created");

    LOG.infof("Created lead %s for user %s", lead.id, currentUserId);

    // Return created lead with location header
    URI location = uriInfo.getAbsolutePathBuilder().path(lead.id.toString()).build();
    // Return DTO to avoid lazy loading issues
    LeadDTO dto = LeadDTO.from(lead);
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
      LOG.warnf("User %s denied update access to lead %s", currentUserId, id);
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Only lead owner or admin can update"))
          .build();
    }

    // Check ETag matches current version using strong comparison
    EntityTag currentEtag = ETags.strongLead(lead.id, lead.version);

    // evaluatePreconditions does strong comparison for If-Match (returns 412 on mismatch)
    Response.ResponseBuilder preconditions = request.evaluatePreconditions(currentEtag);
    if (preconditions != null) {
      return preconditions.tag(currentEtag).build();
    }

    // Update basic fields if provided
    if (updateRequest.companyName != null) lead.companyName = updateRequest.companyName;
    if (updateRequest.contactPerson != null) lead.contactPerson = updateRequest.contactPerson;
    if (updateRequest.email != null) {
      String newNormalizedEmail = Lead.normalizeEmail(updateRequest.email);
      // Check for duplicate if email is changing
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
      lead.email = updateRequest.email;
      lead.emailNormalized = newNormalizedEmail;
    }
    if (updateRequest.phone != null) lead.phone = updateRequest.phone;
    if (updateRequest.website != null) lead.website = updateRequest.website;
    if (updateRequest.street != null) lead.street = updateRequest.street;
    if (updateRequest.postalCode != null) lead.postalCode = updateRequest.postalCode;
    if (updateRequest.city != null) lead.city = updateRequest.city;
    if (updateRequest.businessType != null) lead.businessType = BusinessType.fromString(updateRequest.businessType);
    if (updateRequest.kitchenSize != null) lead.kitchenSize = KitchenSize.fromString(updateRequest.kitchenSize);
    if (updateRequest.employeeCount != null) lead.employeeCount = updateRequest.employeeCount;
    if (updateRequest.estimatedVolume != null) lead.estimatedVolume = updateRequest.estimatedVolume;

    // Handle status change with state machine
    if (updateRequest.status != null && updateRequest.status != lead.status) {
      if (!protectionService.canTransitionStatus(lead, updateRequest.status, currentUserId)) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", "Invalid status transition"))
            .build();
      }

      LeadStatus oldStatus = lead.status;
      lead.status = updateRequest.status;
      lead.lastActivityAt = LocalDateTime.now();

      // Log status change activity
      createAndPersistActivity(
          lead,
          currentUserId,
          ActivityType.STATUS_CHANGE,
          "Status changed from " + oldStatus + " to " + updateRequest.status);

      // Handle special status transitions
      if (updateRequest.status == LeadStatus.GRACE_PERIOD) {
        lead.gracePeriodStartAt = LocalDateTime.now();
      } else if (updateRequest.status == LeadStatus.EXPIRED) {
        lead.expiredAt = LocalDateTime.now();
      }
    }

    // Handle Stop-the-Clock feature
    if (updateRequest.stopClock != null) {
      var settings = settingsService.getOrCreateForUser(currentUserId);
      if (updateRequest.stopClock && (settings.canStopClock || isAdmin)) {
        LocalDateTime now = LocalDateTime.now();
        lead.clockStoppedAt = now;
        lead.stopReason = updateRequest.stopReason;
        lead.stopApprovedBy = currentUserId;

        // Log clock stop activity
        createAndPersistActivity(
            lead,
            currentUserId,
            ActivityType.CLOCK_STOPPED,
            "Clock stopped: " + updateRequest.stopReason);
      } else if (!updateRequest.stopClock && lead.clockStoppedAt != null) {
        // Resume clock: Calculate cumulative pause duration (Sprint 2.1.6 Phase 3 - V262)
        var now = LocalDateTime.now();
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

    // Handle collaborator management
    if (updateRequest.addCollaborators != null) {
      for (String userId : updateRequest.addCollaborators) {
        lead.addCollaborator(userId);
      }
    }

    if (updateRequest.removeCollaborators != null) {
      for (String userId : updateRequest.removeCollaborators) {
        lead.removeCollaborator(userId);
      }
    }

    lead.updatedBy = currentUserId;
    lead.persist();
    lead.flush(); // Force version increment BEFORE creating ETag/DTO

    LOG.infof("Updated lead %s by user %s", id, currentUserId);
    // Return with new strong ETag after version bump
    EntityTag newEtag = ETags.strongLead(lead.id, lead.version);
    // Convert to DTO to avoid lazy loading issues
    LeadDTO dto = LeadDTO.from(lead);
    return Response.ok(dto).tag(newEtag).build();
  }

  /**
   * DELETE /api/leads/{id} - Delete lead (soft delete by setting status to DELETED). Only admin or
   * owner can delete. Requires If-Match header for safe deletion.
   */
  @DELETE
  @Path("/{id}")
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

    // Check permission
    boolean isAdmin = securityContext.isUserInRole("ADMIN");
    boolean isOwner = lead.ownerUserId.equals(currentUserId);

    if (!isOwner && !isAdmin) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Only lead owner or admin can delete"))
          .build();
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

    // RLS handles access control - if lead is null, user has no access
    // No additional check needed since RLS policies filter at database level

    // Validate and convert activity type
    ActivityType activityType;
    try {
      activityType = ActivityType.valueOf(request.activityType.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Invalid activity type: " + request.activityType))
          .build();
    }

    // Create activity using helper method
    LeadActivity activity =
        createAndPersistActivity(lead, currentUserId, activityType, request.description);

    // Update lead's last activity timestamp (important for protection system)
    lead.lastActivityAt = LocalDateTime.now();
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

    // RLS handles access control - if lead is null, user has no access
    // No additional check needed since RLS policies filter at database level

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
      @PathParam("id") Long id, @Valid de.freshplan.modules.leads.api.AddFirstContactRequest request) {

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

  /** Simple error response DTO. */
  private static class ErrorResponse {
    public String error;

    public ErrorResponse(String error) {
      this.error = error;
    }
  }
}
