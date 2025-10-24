package de.freshplan.domain.communication.api;

import de.freshplan.domain.communication.entity.Activity;
import de.freshplan.domain.communication.entity.EntityType;
import de.freshplan.domain.communication.service.ActivityService;
import de.freshplan.modules.leads.domain.ActivityOutcome;
import de.freshplan.modules.leads.domain.ActivityType;
import io.quarkus.logging.Log;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Activity REST Resource
 *
 * <p>Sprint 2.1.7.2: D8 Unified Communication System - REST API
 *
 * <p>Provides REST endpoints for unified activity tracking across Lead and Customer entities.
 *
 * <p><b>Key Features:</b>
 *
 * <ul>
 *   <li>GET activities for Lead or Customer
 *   <li>POST create new activity
 *   <li>PUT update existing activity
 *   <li>DELETE activity
 *   <li>✨ <b>Unified Timeline:</b> GET /api/activities/customer/{id} includes Lead history!
 * </ul>
 *
 * <p><b>Security:</b> All endpoints require authentication (partner, partner_manager, admin)
 */
@Path("/api/activities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"partner", "partner_manager", "admin"})
public class ActivityResource {

  @Inject ActivityService activityService;

  @Context SecurityContext securityContext;

  // ============================================================================
  // GET ACTIVITIES
  // ============================================================================

  /**
   * Get Activities for Lead
   *
   * @param leadId Lead ID (BIGINT as String)
   * @return List of activities
   */
  @GET
  @Path("/lead/{leadId}")
  public Response getLeadActivities(@PathParam("leadId") Long leadId) {
    Log.infof("GET /api/activities/lead/%d", leadId);

    List<Activity> activities = activityService.getLeadActivities(leadId);

    Log.infof("Found %d activities for Lead %d", activities.size(), leadId);
    return Response.ok(activities).build();
  }

  /**
   * Get Activities for Customer (🎯 THE KILLER FEATURE!)
   *
   * <p><b>Unified Timeline:</b> Includes Lead activities if customer.originalLeadId exists!
   *
   * @param customerId Customer UUID
   * @return Unified Timeline (Lead + Customer activities)
   */
  @GET
  @Path("/customer/{customerId}")
  public Response getCustomerActivities(@PathParam("customerId") UUID customerId) {
    Log.infof("GET /api/activities/customer/%s", customerId);

    // This method automatically includes Lead history!
    List<Activity> activities =
        activityService.getCustomerActivitiesIncludingLeadHistory(customerId);

    Log.infof("Found %d activities for Customer %s (incl. Lead history)", activities.size(), customerId);
    return Response.ok(activities).build();
  }

  /**
   * Get Single Activity by ID
   *
   * @param activityId Activity UUID
   * @return Activity
   */
  @GET
  @Path("/{activityId}")
  public Response getActivity(@PathParam("activityId") UUID activityId) {
    Log.infof("GET /api/activities/%s", activityId);

    Activity activity = Activity.findById(activityId);
    if (activity == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity("Activity not found: " + activityId)
          .build();
    }

    return Response.ok(activity).build();
  }

  // ============================================================================
  // CREATE ACTIVITY
  // ============================================================================

  /**
   * Create Activity for Lead
   *
   * @param leadId Lead ID (BIGINT)
   * @param request Activity creation request
   * @return Created activity
   */
  @POST
  @Path("/lead/{leadId}")
  @Transactional
  public Response createLeadActivity(
      @PathParam("leadId") Long leadId, @Valid CreateActivityRequest request) {
    Log.infof(
        "POST /api/activities/lead/%d (type=%s, summary=%s)",
        leadId, request.activityType, request.summary);

    String userId = securityContext.getUserPrincipal().getName();

    // Create activity using factory method
    Activity activity =
        Activity.forLead(leadId, userId, request.activityType, request.description);

    // Set optional fields
    activity.summary = request.summary;
    activity.outcome = request.outcome;
    activity.activityDate =
        request.activityDate != null ? request.activityDate : LocalDateTime.now();

    // Persist
    Activity created = activityService.createActivity(activity);

    Log.infof("Created activity %s for Lead %d", created.id, leadId);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  /**
   * Create Activity for Customer
   *
   * @param customerId Customer UUID
   * @param request Activity creation request
   * @return Created activity
   */
  @POST
  @Path("/customer/{customerId}")
  @Transactional
  public Response createCustomerActivity(
      @PathParam("customerId") UUID customerId, @Valid CreateActivityRequest request) {
    Log.infof(
        "POST /api/activities/customer/%s (type=%s, summary=%s)",
        customerId, request.activityType, request.summary);

    String userId = securityContext.getUserPrincipal().getName();

    // Create activity using factory method
    Activity activity =
        Activity.forCustomer(customerId, userId, request.activityType, request.description);

    // Set optional fields
    activity.summary = request.summary;
    activity.outcome = request.outcome;
    activity.activityDate =
        request.activityDate != null ? request.activityDate : LocalDateTime.now();

    // Persist
    Activity created = activityService.createActivity(activity);

    Log.infof("Created activity %s for Customer %s", created.id, customerId);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  // ============================================================================
  // UPDATE ACTIVITY
  // ============================================================================

  /**
   * Update Activity
   *
   * @param activityId Activity UUID
   * @param request Update request
   * @return Updated activity
   */
  @PUT
  @Path("/{activityId}")
  @Transactional
  public Response updateActivity(
      @PathParam("activityId") UUID activityId, @Valid UpdateActivityRequest request) {
    Log.infof("PUT /api/activities/%s", activityId);

    Activity updated =
        activityService.updateActivity(
            activityId, request.summary, request.description, request.outcome);

    Log.infof("Updated activity %s", activityId);
    return Response.ok(updated).build();
  }

  // ============================================================================
  // DELETE ACTIVITY
  // ============================================================================

  /**
   * Delete Activity
   *
   * @param activityId Activity UUID
   * @return No content
   */
  @DELETE
  @Path("/{activityId}")
  @Transactional
  public Response deleteActivity(@PathParam("activityId") UUID activityId) {
    Log.infof("DELETE /api/activities/%s", activityId);

    activityService.deleteActivity(activityId);

    Log.infof("Deleted activity %s", activityId);
    return Response.noContent().build();
  }

  // ============================================================================
  // REQUEST/RESPONSE DTOs
  // ============================================================================

  /**
   * Create Activity Request DTO
   *
   * <p>Used for POST /api/activities/{entityType}/{entityId}
   */
  public static class CreateActivityRequest {
    @NotNull(message = "activityType is required")
    public ActivityType activityType;

    @NotBlank(message = "summary is required")
    public String summary;

    public String description;

    public ActivityOutcome outcome;

    public LocalDateTime activityDate;
  }

  /**
   * Update Activity Request DTO
   *
   * <p>Used for PUT /api/activities/{activityId}
   */
  public static class UpdateActivityRequest {
    public String summary;
    public String description;
    public ActivityOutcome outcome;
  }
}
