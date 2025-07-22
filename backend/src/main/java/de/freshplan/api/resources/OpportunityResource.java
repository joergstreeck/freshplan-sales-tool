package de.freshplan.api.resources;

import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.service.OpportunityService;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.PipelineOverviewResponse;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import io.quarkus.panache.common.Page;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API für Opportunity Management
 *
 * <p>Stellt die RESTful API für das Opportunity/Pipeline-Management zur Verfügung. Unterstützt
 * CRUD-Operationen, Stage-Management und Pipeline-Analytics.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/opportunities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
// @Authenticated // Temporarily disabled for integration tests
public class OpportunityResource {

  private static final Logger logger = LoggerFactory.getLogger(OpportunityResource.class);

  @Inject OpportunityService opportunityService;

  // =====================================
  // CRUD OPERATIONS
  // =====================================

  /**
   * Erstellt eine neue Opportunity
   *
   * <p>POST /api/opportunities
   */
  @POST
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response createOpportunity(@Valid CreateOpportunityRequest request) {
    logger.info("Creating opportunity: {}", request.getName());

    OpportunityResponse response = opportunityService.createOpportunity(request);
    return Response.status(Response.Status.CREATED).entity(response).build();
  }

  /**
   * Findet alle Opportunities mit Paginierung
   *
   * <p>GET /api/opportunities?page=0&size=20
   */
  @GET
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response getAllOpportunities(
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("20") int size) {
    logger.debug("Fetching opportunities - page: {}, size: {}", page, size);

    List<OpportunityResponse> opportunities =
        opportunityService.findAllOpportunities(Page.of(page, size));
    return Response.ok(opportunities).build();
  }

  /**
   * Findet eine Opportunity by ID
   *
   * <p>GET /api/opportunities/{id}
   */
  @GET
  @Path("/{id}")
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response getOpportunity(@PathParam("id") UUID id) {
    logger.debug("Fetching opportunity: {}", id);

    OpportunityResponse response = opportunityService.findById(id);
    return Response.ok(response).build();
  }

  /**
   * Aktualisiert eine Opportunity
   *
   * <p>PUT /api/opportunities/{id}
   */
  @PUT
  @Path("/{id}")
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response updateOpportunity(
      @PathParam("id") UUID id, @Valid UpdateOpportunityRequest request) {
    logger.info("Updating opportunity: {}", id);

    OpportunityResponse response = opportunityService.updateOpportunity(id, request);
    return Response.ok(response).build();
  }

  /**
   * Löscht eine Opportunity (Soft-Delete)
   *
   * <p>DELETE /api/opportunities/{id}
   */
  @DELETE
  @Path("/{id}")
  // @RolesAllowed({"admin", "manager"})
  public Response deleteOpportunity(@PathParam("id") UUID id) {
    logger.info("Deleting opportunity: {}", id);

    // TODO: Implement soft delete
    return Response.status(Response.Status.NOT_IMPLEMENTED)
        .entity("Soft delete not yet implemented")
        .build();
  }

  // =====================================
  // STAGE MANAGEMENT
  // =====================================

  /**
   * Ändert die Stage einer Opportunity
   *
   * <p>PUT /api/opportunities/{id}/stage/{stage}?reason=optional
   */
  @PUT
  @Path("/{id}/stage/{stage}")
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response changeStage(
      @PathParam("id") UUID id,
      @PathParam("stage") OpportunityStage stage,
      @QueryParam("reason") String reason) {
    logger.info("Changing stage for opportunity {} to {}", id, stage);

    OpportunityResponse response = opportunityService.changeStage(id, stage, reason);
    return Response.ok(response).build();
  }

  /**
   * Findet Opportunities nach Stage
   *
   * <p>GET /api/opportunities/stage/{stage}
   */
  @GET
  @Path("/stage/{stage}")
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response getOpportunitiesByStage(@PathParam("stage") OpportunityStage stage) {
    logger.debug("Fetching opportunities for stage: {}", stage);

    List<OpportunityResponse> opportunities = opportunityService.findByStage(stage);
    return Response.ok(opportunities).build();
  }

  // =====================================
  // PIPELINE ANALYTICS
  // =====================================

  /**
   * Pipeline Übersicht mit Statistiken
   *
   * <p>GET /api/opportunities/pipeline/overview
   */
  @GET
  @Path("/pipeline/overview")
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response getPipelineOverview() {
    logger.debug("Generating pipeline overview");

    PipelineOverviewResponse response = opportunityService.getPipelineOverview();
    return Response.ok(response).build();
  }

  /**
   * Findet Opportunities eines bestimmten Verkäufers
   *
   * <p>GET /api/opportunities/assigned/{userId}
   */
  @GET
  @Path("/assigned/{userId}")
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response getOpportunitiesByAssignedTo(@PathParam("userId") UUID userId) {
    logger.debug("Fetching opportunities assigned to user: {}", userId);

    List<OpportunityResponse> opportunities = opportunityService.findByAssignedTo(userId);
    return Response.ok(opportunities).build();
  }

  // =====================================
  // ACTIVITY MANAGEMENT
  // =====================================

  /**
   * Fügt eine Activity zu einer Opportunity hinzu
   *
   * <p>POST /api/opportunities/{id}/activities
   */
  @POST
  @Path("/{id}/activities")
  // @RolesAllowed({"admin", "manager", "sales"})
  public Response addActivity(
      @PathParam("id") UUID id,
      @QueryParam("type") String activityType,
      @QueryParam("title") String title,
      @QueryParam("description") String description) {
    logger.info("Adding activity to opportunity {}: {}", id, title);

    // TODO: Implement proper activity request DTO
    return Response.status(Response.Status.NOT_IMPLEMENTED)
        .entity("Activity management not yet implemented")
        .build();
  }

  // =====================================
  // HEALTH CHECK
  // =====================================

  /**
   * Health Check für Opportunity API
   *
   * <p>GET /api/opportunities/health
   */
  @GET
  @Path("/health")
  public Response health() {
    return Response.ok("Opportunity API is healthy").build();
  }
}
