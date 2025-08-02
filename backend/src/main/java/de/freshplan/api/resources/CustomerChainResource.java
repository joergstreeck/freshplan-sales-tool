package de.freshplan.api.resources;

import de.freshplan.domain.customer.service.CustomerChainService;
import de.freshplan.domain.customer.service.dto.BusinessModelDto;
import de.freshplan.domain.customer.service.dto.ChainStructureDto;
import de.freshplan.domain.customer.service.dto.PotentialCalculationRequest;
import de.freshplan.domain.customer.service.dto.PotentialCalculationResponse;
import de.freshplan.infrastructure.security.CurrentUser;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import de.freshplan.infrastructure.security.UserPrincipal;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

/**
 * REST API for Customer Chain Structure and Business Model Management. Part of Sprint 2 -
 * Sales-focused customer management.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "manager", "sales"})
@SecurityAudit
public class CustomerChainResource {

  @Inject CustomerChainService chainService;

  @Inject SecurityContextProvider securityContext;

  @Inject @CurrentUser UserPrincipal currentUser;

  /**
   * Updates the chain structure information for a customer.
   *
   * @param customerId The customer ID
   * @param chainStructure The chain structure data
   * @return 200 OK with updated chain structure
   */
  @PATCH
  @Path("/{id}/chain-structure")
  @RolesAllowed({"admin", "manager"})
  public Response updateChainStructure(
      @PathParam("id") UUID customerId, @Valid ChainStructureDto chainStructure) {

    securityContext.requireAnyRole("admin", "manager");

    ChainStructureDto updated =
        chainService.updateChainStructure(customerId, chainStructure, currentUser.getUsername());

    return Response.ok(updated).build();
  }

  /**
   * Updates the business model and pain points for a customer.
   *
   * @param customerId The customer ID
   * @param businessModel The business model data
   * @return 200 OK with updated business model
   */
  @PATCH
  @Path("/{id}/business-model")
  @RolesAllowed({"admin", "manager"})
  public Response updateBusinessModel(
      @PathParam("id") UUID customerId, @Valid BusinessModelDto businessModel) {

    securityContext.requireAnyRole("admin", "manager");

    BusinessModelDto updated =
        chainService.updateBusinessModel(customerId, businessModel, currentUser.getUsername());

    return Response.ok(updated).build();
  }

  /**
   * Calculates the sales potential for a customer based on their industry and service offerings.
   *
   * @param customerId The customer ID
   * @param request The calculation parameters
   * @return 200 OK with potential calculation results
   */
  @POST
  @Path("/{id}/calculate-potential")
  public Response calculatePotential(
      @PathParam("id") UUID customerId, @Valid PotentialCalculationRequest request) {

    PotentialCalculationResponse response = chainService.calculatePotential(customerId, request);

    return Response.ok(response).build();
  }
}
