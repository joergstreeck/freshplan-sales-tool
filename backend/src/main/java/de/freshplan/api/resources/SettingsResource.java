package de.freshplan.api.resources;

import de.freshplan.domain.opportunity.service.OpportunityMultiplierService;
import de.freshplan.domain.opportunity.service.dto.OpportunityMultiplierResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * Settings REST API - Sprint 2.1.7.3
 *
 * <p>Business configuration settings (NOT system/technical settings)
 *
 * <p>Currently provides:
 *
 * <ul>
 *   <li>GET /api/settings/opportunity-multipliers - Business-Type-Matrix for opportunity value
 *       estimation
 * </ul>
 *
 * @since Sprint 2.1.7.3
 */
@Path("/api/settings")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Settings", description = "Business configuration settings (read-only)")
public class SettingsResource {

  private static final Logger logger = Logger.getLogger(SettingsResource.class);

  @Inject OpportunityMultiplierService multiplierService;

  @Context SecurityContext securityContext;

  /**
   * Get all opportunity multipliers (Business-Type-Matrix)
   *
   * <p>Returns 36 multiplier combinations (9 BusinessTypes × 4 OpportunityTypes)
   *
   * <p>Use Case: Frontend loads this ONCE when CreateOpportunityForCustomerDialog opens, then
   * calculates expectedValue locally with 3-Tier Fallback:
   *
   * <pre>
   * baseVolume = customer.actualAnnualVolume (Xentral)
   *           OR customer.expectedAnnualVolume (Lead estimate)
   *           OR 0 (manual entry)
   *
   * expectedValue = baseVolume × multiplier[businessType][opportunityType]
   * </pre>
   *
   * <p>Example Response (36 entries):
   *
   * <pre>
   * [
   *   { "businessType": "RESTAURANT", "opportunityType": "SORTIMENTSERWEITERUNG", "multiplier": 0.25 },
   *   { "businessType": "HOTEL", "opportunityType": "SORTIMENTSERWEITERUNG", "multiplier": 0.65 },
   *   ...
   * ]
   * </pre>
   *
   * <p>Cache Recommendation: React Query with 5min stale time (rarely changes)
   *
   * @return List of all multipliers (200 OK)
   */
  @GET
  @Path("/opportunity-multipliers")
  @RolesAllowed({"USER", "MANAGER", "ADMIN"})
  @Operation(
      summary = "Get all opportunity multipliers",
      description =
          "Returns Business-Type-Matrix for intelligent opportunity value estimation (36 entries)")
  @APIResponse(
      responseCode = "200",
      description = "Multipliers loaded successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = OpportunityMultiplierResponse.class)))
  public Response getAllOpportunityMultipliers() {
    logger.debug("GET /api/settings/opportunity-multipliers");

    List<OpportunityMultiplierResponse> multipliers = multiplierService.getAllMultipliers();

    logger.info("Returning " + multipliers.size() + " multipliers");

    return Response.ok(multipliers).build();
  }

  /**
   * Update opportunity multiplier value (Admin-Only)
   *
   * <p>Sprint 2.1.7.3 - Edit-Funktionalität
   */
  @PUT
  @Path("/opportunity-multipliers/{id}")
  @RolesAllowed("ADMIN")
  @Transactional
  public Response updateMultiplier(@PathParam("id") UUID id, UpdateMultiplierRequest request) {
    logger.info(
        "PUT /api/settings/opportunity-multipliers/"
            + id
            + " - multiplier="
            + request.multiplier());

    try {
      // Update multiplier via service
      de.freshplan.domain.opportunity.entity.OpportunityMultiplier updated =
          multiplierService.updateMultiplier(id, request.multiplier());

      // Convert to response DTO
      OpportunityMultiplierResponse response = OpportunityMultiplierResponse.fromEntity(updated);

      logger.info("Successfully updated multiplier " + id);
      return Response.ok(response).build();

    } catch (jakarta.ws.rs.NotFoundException e) {
      logger.warn("Multiplier not found: " + id);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(java.util.Map.of("error", "Multiplier not found"))
          .build();
    } catch (IllegalArgumentException e) {
      logger.warn("Validation failed: " + e.getMessage());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(java.util.Map.of("error", e.getMessage()))
          .build();
    } catch (Exception e) {
      logger.error("Failed to update multiplier " + id, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(java.util.Map.of("error", "Internal server error"))
          .build();
    }
  }

  /** DTO for updating multipliers */
  public record UpdateMultiplierRequest(
      @jakarta.validation.constraints.NotNull(message = "Multiplier is required") @jakarta.validation.constraints.DecimalMin(
              value = "0.0",
              message = "Multiplier must be >= 0.0")
          @jakarta.validation.constraints.DecimalMax(
              value = "2.0",
              message = "Multiplier must be <= 2.0")
          BigDecimal multiplier) {}

  // ============================================================================
  // FUTURE ENDPOINTS (Modul 08 - Extended Admin Features)
  // ============================================================================

  // POST /api/settings/opportunity-multipliers - Create custom multiplier
  // DELETE /api/settings/opportunity-multipliers/{id} - Delete custom multiplier
  // POST /api/settings/opportunity-multipliers/reset - Reset to factory defaults
}
