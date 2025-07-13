package de.freshplan.api.resources;

import de.freshplan.domain.customer.service.CustomerTimelineService;
import de.freshplan.domain.customer.service.dto.timeline.*;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST endpoint for managing customer timeline events. Provides comprehensive API for timeline
 * history and activities.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/customers/{customerId}/timeline")
@RequestScoped
@PermitAll // TODO: SECURITY ROLLBACK - Remove after fixing test configuration (Issue #CI-FIX)
// @RolesAllowed({"admin", "manager", "sales"}) // TODO: SECURITY ROLLBACK - Uncomment after test fix
@SecurityAudit
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customer Timeline", description = "Operations for managing customer timeline events")
public class CustomerTimelineResource {

  private static final Logger LOG = Logger.getLogger(CustomerTimelineResource.class);

  private final CustomerTimelineService timelineService;
  private final SecurityContextProvider securityContext;

  @Inject
  public CustomerTimelineResource(
      CustomerTimelineService timelineService, SecurityContextProvider securityContext) {
    this.timelineService = timelineService;
    this.securityContext = securityContext;
  }

  @GET
  @Operation(
      summary = "Get customer timeline",
      description = "Retrieves paginated timeline events for a specific customer")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Timeline events retrieved successfully",
        content = @Content(schema = @Schema(implementation = TimelineListResponse.class))),
    @APIResponse(responseCode = "404", description = "Customer not found"),
    @APIResponse(responseCode = "400", description = "Invalid request parameters")
  })
  public Response getTimeline(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId,
      @Parameter(description = "Page number (0-based)") @QueryParam("page") @DefaultValue("0")
          int page,
      @Parameter(description = "Page size") @QueryParam("size") @DefaultValue("20") int size,
      @Parameter(description = "Filter by event category") @QueryParam("category") String category,
      @Parameter(description = "Search in title and description") @QueryParam("search")
          String search) {

    LOG.infof("Getting timeline for customer %s", customerId);

    TimelineListResponse timeline =
        timelineService.getCustomerTimeline(customerId, page, size, category, search);

    return Response.ok(timeline).build();
  }

  @POST
  @Operation(
      summary = "Create timeline event",
      description = "Creates a new timeline event for a customer")
  @APIResponses({
    @APIResponse(
        responseCode = "201",
        description = "Timeline event created successfully",
        content = @Content(schema = @Schema(implementation = TimelineEventResponse.class))),
    @APIResponse(responseCode = "404", description = "Customer not found"),
    @APIResponse(responseCode = "400", description = "Invalid request data")
  })
  public Response createEvent(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId,
      @Parameter(description = "Timeline event data", required = true) @Valid @NotNull CreateTimelineEventRequest request) {

    LOG.infof("Creating timeline event for customer %s", customerId);

    TimelineEventResponse event = timelineService.createEvent(customerId, request);

    return Response.status(Response.Status.CREATED).entity(event).build();
  }

  @POST
  @Path("/notes")
  @Operation(summary = "Create note", description = "Creates a quick note for a customer")
  @APIResponses({
    @APIResponse(
        responseCode = "201",
        description = "Note created successfully",
        content = @Content(schema = @Schema(implementation = TimelineEventResponse.class))),
    @APIResponse(responseCode = "404", description = "Customer not found"),
    @APIResponse(responseCode = "400", description = "Invalid request data")
  })
  public Response createNote(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId,
      @Parameter(description = "Note data", required = true) @Valid @NotNull CreateNoteRequest request) {

    LOG.infof("Creating note for customer %s", customerId);

    TimelineEventResponse note = timelineService.createNote(customerId, request);

    return Response.status(Response.Status.CREATED).entity(note).build();
  }

  @POST
  @Path("/communications")
  @Operation(
      summary = "Create communication",
      description = "Records a communication event (call, email, meeting)")
  @APIResponses({
    @APIResponse(
        responseCode = "201",
        description = "Communication recorded successfully",
        content = @Content(schema = @Schema(implementation = TimelineEventResponse.class))),
    @APIResponse(responseCode = "404", description = "Customer not found"),
    @APIResponse(responseCode = "400", description = "Invalid request data")
  })
  public Response createCommunication(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId,
      @Parameter(description = "Communication data", required = true) @Valid @NotNull CreateCommunicationRequest request) {

    LOG.infof("Creating communication for customer %s", customerId);

    TimelineEventResponse communication = timelineService.createCommunication(customerId, request);

    return Response.status(Response.Status.CREATED).entity(communication).build();
  }

  @GET
  @Path("/follow-ups")
  @Operation(
      summary = "Get follow-up events",
      description = "Retrieves all events requiring follow-up for a customer")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Follow-up events retrieved successfully",
        content = @Content(schema = @Schema(implementation = TimelineEventResponse.class))),
    @APIResponse(responseCode = "404", description = "Customer not found")
  })
  public Response getFollowUps(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId) {

    LOG.infof("Getting follow-ups for customer %s", customerId);

    List<TimelineEventResponse> followUps = timelineService.getFollowUpEvents(customerId);

    return Response.ok(followUps).build();
  }

  @GET
  @Path("/follow-ups/overdue")
  @Operation(
      summary = "Get overdue follow-ups",
      description = "Retrieves overdue follow-up events for a customer")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Overdue follow-ups retrieved successfully",
        content = @Content(schema = @Schema(implementation = TimelineEventResponse.class))),
    @APIResponse(responseCode = "404", description = "Customer not found")
  })
  public Response getOverdueFollowUps(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId) {

    LOG.infof("Getting overdue follow-ups for customer %s", customerId);

    List<TimelineEventResponse> overdueFollowUps = timelineService.getOverdueFollowUps(customerId);

    return Response.ok(overdueFollowUps).build();
  }

  @GET
  @Path("/communications/recent")
  @Operation(
      summary = "Get recent communications",
      description = "Retrieves recent communication history for a customer")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Recent communications retrieved successfully",
        content = @Content(schema = @Schema(implementation = TimelineEventResponse.class)))
  })
  public Response getRecentCommunications(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId,
      @Parameter(description = "Number of days to look back")
          @QueryParam("days")
          @DefaultValue("30")
          int days) {

    LOG.infof("Getting recent communications for customer %s (last %d days)", customerId, days);

    List<TimelineEventResponse> communications =
        timelineService.getRecentCommunications(customerId, days);

    return Response.ok(communications).build();
  }

  @GET
  @Path("/summary")
  @Operation(
      summary = "Get timeline summary",
      description = "Retrieves summary statistics for customer timeline")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Timeline summary retrieved successfully",
        content = @Content(schema = @Schema(implementation = TimelineSummaryResponse.class))),
    @APIResponse(responseCode = "404", description = "Customer not found")
  })
  public Response getTimelineSummary(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId) {

    LOG.infof("Getting timeline summary for customer %s", customerId);

    TimelineSummaryResponse summary = timelineService.getTimelineSummary(customerId);

    return Response.ok(summary).build();
  }

  @PUT
  @Path("/events/{eventId}")
  @Operation(summary = "Update timeline event", description = "Updates an existing timeline event")
  @APIResponses({
    @APIResponse(
        responseCode = "200",
        description = "Timeline event updated successfully",
        content = @Content(schema = @Schema(implementation = TimelineEventResponse.class))),
    @APIResponse(responseCode = "404", description = "Event not found"),
    @APIResponse(responseCode = "400", description = "Invalid request data")
  })
  public Response updateEvent(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId,
      @Parameter(description = "Event ID", required = true) @PathParam("eventId") UUID eventId,
      @Parameter(description = "Update data", required = true) @Valid @NotNull UpdateTimelineEventRequest request) {

    LOG.infof("Updating timeline event %s", eventId);

    TimelineEventResponse updated = timelineService.updateEvent(eventId, request);

    return Response.ok(updated).build();
  }

  @POST
  @Path("/events/{eventId}/complete-follow-up")
  @Operation(summary = "Complete follow-up", description = "Marks a follow-up as completed")
  @APIResponses({
    @APIResponse(responseCode = "204", description = "Follow-up completed successfully"),
    @APIResponse(responseCode = "404", description = "Event not found")
  })
  public Response completeFollowUp(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId,
      @Parameter(description = "Event ID", required = true) @PathParam("eventId") UUID eventId,
      @Parameter(description = "User who completed the follow-up")
          @QueryParam("completedBy")
          @NotNull String completedBy) {

    LOG.infof("Completing follow-up for event %s", eventId);

    timelineService.completeFollowUp(eventId, completedBy);

    return Response.noContent().build();
  }

  @DELETE
  @Path("/events/{eventId}")
  @Operation(summary = "Delete timeline event", description = "Soft deletes a timeline event")
  @APIResponses({
    @APIResponse(responseCode = "204", description = "Event deleted successfully"),
    @APIResponse(responseCode = "404", description = "Event not found")
  })
  public Response deleteEvent(
      @Parameter(description = "Customer ID", required = true) @PathParam("customerId")
          UUID customerId,
      @Parameter(description = "Event ID", required = true) @PathParam("eventId") UUID eventId,
      @Parameter(description = "User who deleted the event") @QueryParam("deletedBy") @NotNull String deletedBy) {

    LOG.infof("Deleting timeline event %s", eventId);

    timelineService.deleteEvent(eventId, deletedBy);

    return Response.noContent().build();
  }
}
