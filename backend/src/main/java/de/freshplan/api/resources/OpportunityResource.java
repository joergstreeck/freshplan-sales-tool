package de.freshplan.api.resources;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.service.OpportunityService;
import de.freshplan.domain.opportunity.service.dto.ConvertToCustomerRequest;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityForCustomerRequest;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityFromLeadRequest;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityRequest;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.domain.opportunity.service.dto.PipelineOverviewResponse;
import de.freshplan.domain.opportunity.service.dto.UpdateOpportunityRequest;
import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import de.freshplan.infrastructure.security.SecurityAudit;
import io.quarkus.panache.common.Page;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
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
@SecurityAudit
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @RolesAllowed({"admin", "manager"})
  public Response deleteOpportunity(@PathParam("id") UUID id) {
    logger.info("Deleting opportunity: {}", id);

    // TODO: Implement soft delete
    return Response.status(Response.Status.NOT_IMPLEMENTED)
        .entity("Soft delete not yet implemented")
        .build();
  }

  // =====================================
  // LEAD → OPPORTUNITY → CUSTOMER WORKFLOW
  // =====================================

  /**
   * Creates an Opportunity from a qualified Lead.
   *
   * <p>POST /api/opportunities/from-lead/{leadId}
   *
   * <p>Converts a qualified lead into an opportunity. The lead must be in QUALIFIED or ACTIVE
   * status. This creates the lead_id FK link enabling full Lead → Opportunity → Customer
   * traceability.
   *
   * @param leadId ID of the lead to convert
   * @param request Opportunity creation parameters (deal type, value, timeline)
   * @return 201 Created with opportunity data
   * @since Sprint 2.1.6.2 Phase 2 (V10026)
   */
  @POST
  @Path("/from-lead/{leadId}")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response createFromLead(
      @PathParam("leadId") Long leadId, @Valid CreateOpportunityFromLeadRequest request) {
    logger.info("Creating opportunity from lead ID: {}", leadId);

    try {
      OpportunityResponse opportunity = opportunityService.createFromLead(leadId, request);

      logger.info("Successfully created opportunity {} from lead {}", opportunity.getId(), leadId);

      return Response.status(Response.Status.CREATED).entity(opportunity).build();

    } catch (IllegalArgumentException e) {
      // Lead not found or validation failed
      logger.warn("Failed to create opportunity from lead {}: {}", leadId, e.getMessage());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(java.util.Map.of("error", e.getMessage()))
          .build();

    } catch (Exception e) {
      logger.error("Unexpected error creating opportunity from lead " + leadId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(java.util.Map.of("error", "Internal server error"))
          .build();
    }
  }

  /**
   * Converts a won Opportunity to a Customer.
   *
   * <p>POST /api/opportunities/{id}/convert-to-customer
   *
   * <p>Converts a CLOSED_WON opportunity into a customer. This sets the originalLeadId field (V261)
   * if the opportunity originated from a lead, enabling full Lead → Opportunity → Customer
   * traceability. All pain points and business data are transferred.
   *
   * @param opportunityId ID of the opportunity to convert
   * @param request Customer conversion data (company name, address, contact options)
   * @return 200 OK with customer data and Location header
   * @since Sprint 2.1.6.2 Phase 2 (V10026)
   */
  @POST
  @Path("/{id}/convert-to-customer")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response convertToCustomer(
      @PathParam("id") UUID opportunityId, @Valid ConvertToCustomerRequest request) {
    logger.info("Converting opportunity {} to customer", opportunityId);

    try {
      Customer customer = opportunityService.convertToCustomer(opportunityId, request);

      logger.info(
          "Successfully converted opportunity {} to customer {} ({})",
          opportunityId,
          customer.getId(),
          customer.getCustomerNumber());

      return Response.ok(customer).header("Location", "/api/customers/" + customer.getId()).build();

    } catch (OpportunityNotFoundException e) {
      logger.warn("Opportunity not found: {}", opportunityId);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(java.util.Map.of("error", "Opportunity not found: " + opportunityId))
          .build();

    } catch (IllegalStateException e) {
      // Stage validation or duplicate customer check failed
      logger.warn("Failed to convert opportunity {}: {}", opportunityId, e.getMessage());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(java.util.Map.of("error", e.getMessage()))
          .build();

    } catch (Exception e) {
      logger.error("Unexpected error converting opportunity " + opportunityId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(java.util.Map.of("error", "Internal server error"))
          .build();
    }
  }

  /**
   * Creates an Opportunity for an existing Customer (Upsell/Cross-sell/Renewal).
   *
   * <p>POST /api/opportunities/for-customer/{customerId}
   *
   * <p>Creates an opportunity for an existing AKTIV customer. Use cases include Upsell (expanding
   * existing product lines), Cross-sell (new product categories), and Renewal (contract
   * extensions). Opportunities start at NEEDS_ANALYSIS stage (customer is already qualified).
   *
   * @param customerId ID of the customer
   * @param request Opportunity creation parameters (type, value, timeline)
   * @return 201 Created with opportunity data
   * @since Sprint 2.1.6.2 Phase 2 (V10026)
   */
  @POST
  @Path("/for-customer/{customerId}")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response createForCustomer(
      @PathParam("customerId") UUID customerId,
      @Valid CreateOpportunityForCustomerRequest request) {
    logger.info("Creating opportunity for customer ID: {}", customerId);

    try {
      // Sprint 2.1.7 Code Review Fix: Service now returns OpportunityResponse DTO
      OpportunityResponse opportunity = opportunityService.createForCustomer(customerId, request);

      logger.info(
          "Successfully created opportunity {} for customer {} (Type: {})",
          opportunity.getId(),
          customerId,
          request.getOpportunityType());

      // No need to fetch again - service already returns OpportunityResponse
      return Response.status(Response.Status.CREATED).entity(opportunity).build();

    } catch (IllegalArgumentException e) {
      // Customer not found
      logger.warn("Failed to create opportunity for customer {}: {}", customerId, e.getMessage());
      return Response.status(Response.Status.NOT_FOUND)
          .entity(java.util.Map.of("error", e.getMessage()))
          .build();

    } catch (IllegalStateException e) {
      // Customer status validation failed
      logger.warn("Failed to create opportunity for customer {}: {}", customerId, e.getMessage());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(java.util.Map.of("error", e.getMessage()))
          .build();

    } catch (Exception e) {
      logger.error("Unexpected error creating opportunity for customer " + customerId, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(java.util.Map.of("error", "Internal server error"))
          .build();
    }
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @RolesAllowed({"admin", "manager", "sales"})
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
  @PermitAll
  public Response health() {
    return Response.ok("Opportunity API is healthy").build();
  }
}
