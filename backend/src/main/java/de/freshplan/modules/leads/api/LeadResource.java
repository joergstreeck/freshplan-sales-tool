package de.freshplan.modules.leads.api;

import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadActivity;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.service.LeadProtectionService;
import de.freshplan.modules.leads.service.LeadService;
import de.freshplan.modules.leads.service.UserLeadSettingsService;
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
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

/**
 * REST API for Lead Management. Implements user-based lead protection (NO geographical
 * protection). Territory is only used for business rules (currency/tax).
 */
@Path("/api/leads")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeadResource {

  private static final Logger LOG = Logger.getLogger(LeadResource.class);

  @Inject JsonWebToken jwt;

  @Inject LeadService leadService;

  @Inject LeadProtectionService protectionService;

  @Inject UserLeadSettingsService settingsService;

  @Context UriInfo uriInfo;

  /**
   * GET /api/leads - List leads with pagination and filtering. Leads are available nationwide, no
   * geographical restrictions.
   */
  @GET
  public Response listLeads(
      @QueryParam("status") LeadStatus status,
      @QueryParam("territoryId") Long territoryId,
      @QueryParam("ownerUserId") String ownerUserId,
      @QueryParam("search") String search,
      @QueryParam("page") @DefaultValue("0") int pageIndex,
      @QueryParam("size") @DefaultValue("20") int pageSize,
      @QueryParam("sort") @DefaultValue("createdAt") String sortField,
      @QueryParam("direction") @DefaultValue("DESC") String sortDirection) {

    String currentUserId = jwt.getSubject();
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
    if (!jwt.getGroups().contains("ADMIN")) {
      query.append(" and (ownerUserId = :currentUser or :currentUser in elements(collaboratorUserIds))");
      params.put("currentUser", currentUserId);
    }

    // Execute query with pagination
    Sort sort = Sort.by(sortField, Sort.Direction.valueOf(sortDirection.toUpperCase()));
    Page page = Page.of(pageIndex, pageSize);

    List<Lead> leads = Lead.find(query.toString(), sort, params).page(page).list();
    long total = Lead.count(query.toString(), params);

    // Build response with pagination metadata
    Map<String, Object> response = new HashMap<>();
    response.put("data", leads);
    response.put("page", pageIndex);
    response.put("size", pageSize);
    response.put("total", total);
    response.put("totalPages", (total + pageSize - 1) / pageSize);

    return Response.ok(response).build();
  }

  /**
   * GET /api/leads/{id} - Get lead by ID. Access control: owner, collaborators, or admin.
   */
  @GET
  @Path("/{id}")
  public Response getLead(@PathParam("id") Long id) {
    String currentUserId = jwt.getSubject();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // Check access permission
    if (!lead.canBeAccessedBy(currentUserId) && !jwt.getGroups().contains("ADMIN")) {
      LOG.warnf("User %s denied access to lead %s", currentUserId, id);
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Access denied to this lead"))
          .build();
    }

    return Response.ok(lead).build();
  }

  /**
   * POST /api/leads - Create new lead. Lead creator becomes automatic owner (user-based
   * protection).
   */
  @POST
  @Transactional
  public Response createLead(@Valid LeadCreateRequest request) {
    String currentUserId = jwt.getSubject();
    LOG.infof("User %s creating new lead for company: %s", currentUserId, request.companyName);

    // Create new lead
    Lead lead = new Lead();
    lead.companyName = request.companyName;
    lead.contactPerson = request.contactPerson;
    lead.email = request.email;
    lead.phone = request.phone;
    lead.website = request.website;
    lead.street = request.street;
    lead.postalCode = request.postalCode;
    lead.city = request.city;
    lead.countryCode = request.countryCode != null ? request.countryCode : "DE";

    // Set territory based on country (for currency/tax rules only)
    Territory territory = Territory.findByCountryCode(lead.countryCode);
    if (territory == null) {
      territory = Territory.findByCountryCode("DE"); // Default to Germany
    }
    lead.territory = territory;

    // B2B-specific fields
    lead.businessType = request.businessType;
    lead.kitchenSize = request.kitchenSize;
    lead.employeeCount = request.employeeCount;
    lead.estimatedVolume = request.estimatedVolume;
    lead.industry = request.industry;

    // Set ownership and protection
    lead.ownerUserId = currentUserId;
    lead.status = LeadStatus.REGISTERED;
    lead.createdBy = currentUserId;
    lead.source = request.source != null ? request.source : "web";
    lead.sourceCampaign = request.sourceCampaign;

    // Apply user's lead settings for protection periods
    var settings = settingsService.getOrCreateForUser(currentUserId);
    lead.protectionMonths = settings.leadProtectionMonths;
    lead.protectionDays60 = settings.activityReminderDays;
    lead.protectionDays10 = settings.gracePeriodDays;

    // Persist lead
    lead.persist();

    // Create initial activity
    LeadActivity activity = new LeadActivity();
    activity.lead = lead;
    activity.userId = currentUserId;
    activity.activityType = ActivityType.CREATED;
    activity.description = "Lead created";
    activity.persist();

    LOG.infof("Created lead %s for user %s", lead.id, currentUserId);

    // Return created lead with location header
    URI location = uriInfo.getAbsolutePathBuilder().path(lead.id.toString()).build();
    return Response.created(location).entity(lead).build();
  }

  /**
   * PATCH /api/leads/{id} - Update lead (partial update). Supports status transitions and
   * stop-the-clock feature.
   */
  @PATCH
  @Path("/{id}")
  @Transactional
  public Response updateLead(@PathParam("id") Long id, LeadUpdateRequest request) {
    String currentUserId = jwt.getSubject();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // Check permission - only owner or admin can update
    boolean isAdmin = jwt.getGroups().contains("ADMIN");
    boolean isOwner = lead.ownerUserId.equals(currentUserId);

    if (!isOwner && !isAdmin) {
      LOG.warnf("User %s denied update access to lead %s", currentUserId, id);
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Only lead owner or admin can update"))
          .build();
    }

    // Update basic fields if provided
    if (request.companyName != null) lead.companyName = request.companyName;
    if (request.contactPerson != null) lead.contactPerson = request.contactPerson;
    if (request.email != null) lead.email = request.email;
    if (request.phone != null) lead.phone = request.phone;
    if (request.website != null) lead.website = request.website;
    if (request.street != null) lead.street = request.street;
    if (request.postalCode != null) lead.postalCode = request.postalCode;
    if (request.city != null) lead.city = request.city;
    if (request.businessType != null) lead.businessType = request.businessType;
    if (request.kitchenSize != null) lead.kitchenSize = request.kitchenSize;
    if (request.employeeCount != null) lead.employeeCount = request.employeeCount;
    if (request.estimatedVolume != null) lead.estimatedVolume = request.estimatedVolume;

    // Handle status change with state machine
    if (request.status != null && request.status != lead.status) {
      if (!protectionService.canTransitionStatus(lead, request.status, currentUserId)) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", "Invalid status transition"))
            .build();
      }

      LeadStatus oldStatus = lead.status;
      lead.status = request.status;
      lead.lastActivityAt = LocalDateTime.now();

      // Log status change activity
      LeadActivity activity = new LeadActivity();
      activity.lead = lead;
      activity.userId = currentUserId;
      activity.activityType = ActivityType.STATUS_CHANGE;
      activity.description = "Status changed from " + oldStatus + " to " + request.status;
      activity.persist();

      // Handle special status transitions
      if (request.status == LeadStatus.GRACE_PERIOD) {
        lead.gracePeriodStartAt = LocalDateTime.now();
      } else if (request.status == LeadStatus.EXPIRED) {
        lead.expiredAt = LocalDateTime.now();
      }
    }

    // Handle Stop-the-Clock feature
    if (request.stopClock != null) {
      var settings = settingsService.getOrCreateForUser(currentUserId);
      if (request.stopClock && (settings.canStopClock || isAdmin)) {
        lead.clockStoppedAt = LocalDateTime.now();
        lead.stopReason = request.stopReason;
        lead.stopApprovedBy = currentUserId;

        // Log clock stop activity
        LeadActivity activity = new LeadActivity();
        activity.lead = lead;
        activity.userId = currentUserId;
        activity.activityType = ActivityType.CLOCK_STOPPED;
        activity.description = "Clock stopped: " + request.stopReason;
        activity.persist();
      } else if (!request.stopClock && lead.clockStoppedAt != null) {
        // Resume clock
        lead.clockStoppedAt = null;
        lead.stopReason = null;
        lead.stopApprovedBy = null;

        LeadActivity activity = new LeadActivity();
        activity.lead = lead;
        activity.userId = currentUserId;
        activity.activityType = ActivityType.CLOCK_RESUMED;
        activity.description = "Clock resumed";
        activity.persist();
      }
    }

    // Handle collaborator management
    if (request.addCollaborators != null) {
      for (String userId : request.addCollaborators) {
        lead.addCollaborator(userId);
      }
    }

    if (request.removeCollaborators != null) {
      for (String userId : request.removeCollaborators) {
        lead.removeCollaborator(userId);
      }
    }

    lead.updatedBy = currentUserId;
    lead.persist();

    LOG.infof("Updated lead %s by user %s", id, currentUserId);
    return Response.ok(lead).build();
  }

  /**
   * DELETE /api/leads/{id} - Delete lead (soft delete by setting status to DELETED). Only admin
   * or owner can delete.
   */
  @DELETE
  @Path("/{id}")
  @Transactional
  public Response deleteLead(@PathParam("id") Long id) {
    String currentUserId = jwt.getSubject();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // Check permission
    boolean isAdmin = jwt.getGroups().contains("ADMIN");
    boolean isOwner = lead.ownerUserId.equals(currentUserId);

    if (!isOwner && !isAdmin) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Only lead owner or admin can delete"))
          .build();
    }

    // Soft delete by setting status
    lead.status = LeadStatus.DELETED;
    lead.updatedBy = currentUserId;
    lead.persist();

    // Log deletion activity
    LeadActivity activity = new LeadActivity();
    activity.lead = lead;
    activity.userId = currentUserId;
    activity.activityType = ActivityType.DELETED;
    activity.description = "Lead deleted";
    activity.persist();

    LOG.infof("Deleted lead %s by user %s", id, currentUserId);
    return Response.noContent().build();
  }

  /**
   * POST /api/leads/{id}/activities - Add activity to lead. Updates lastActivityAt for protection
   * system.
   */
  @POST
  @Path("/{id}/activities")
  @Transactional
  public Response addActivity(@PathParam("id") Long id, @Valid ActivityRequest request) {
    String currentUserId = jwt.getSubject();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // Check access permission
    if (!lead.canBeAccessedBy(currentUserId) && !jwt.getGroups().contains("ADMIN")) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Access denied to this lead"))
          .build();
    }

    // Create activity
    LeadActivity activity = new LeadActivity();
    activity.lead = lead;
    activity.userId = currentUserId;
    activity.activityType = ActivityType.valueOf(request.activityType);
    activity.description = request.description;
    activity.persist();

    // Update lead's last activity timestamp (important for protection system)
    lead.lastActivityAt = LocalDateTime.now();
    lead.persist();

    LOG.infof("Added activity to lead %s by user %s", id, currentUserId);
    return Response.ok(activity).build();
  }

  /**
   * GET /api/leads/{id}/activities - Get activities for a lead.
   */
  @GET
  @Path("/{id}/activities")
  public Response getActivities(
      @PathParam("id") Long id,
      @QueryParam("page") @DefaultValue("0") int pageIndex,
      @QueryParam("size") @DefaultValue("50") int pageSize) {

    String currentUserId = jwt.getSubject();
    Lead lead = Lead.findById(id);

    if (lead == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(Map.of("error", "Lead not found"))
          .build();
    }

    // Check access permission
    if (!lead.canBeAccessedBy(currentUserId) && !jwt.getGroups().contains("ADMIN")) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(Map.of("error", "Access denied to this lead"))
          .build();
    }

    // Get activities with pagination
    Page page = Page.of(pageIndex, pageSize);
    List<LeadActivity> activities =
        LeadActivity.find("lead", Sort.descending("createdAt"), lead).page(page).list();
    long total = LeadActivity.count("lead", lead);

    Map<String, Object> response = new HashMap<>();
    response.put("data", activities);
    response.put("page", pageIndex);
    response.put("size", pageSize);
    response.put("total", total);

    return Response.ok(response).build();
  }
}