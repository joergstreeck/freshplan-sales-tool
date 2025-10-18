package de.freshplan.api.resources;

import de.freshplan.domain.opportunity.service.OpportunityMultiplierService;
import de.freshplan.domain.opportunity.service.dto.OpportunityMultiplierResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
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

  // ============================================================================
  // FUTURE ENDPOINTS (Modul 08 - Admin-Dashboard)
  // ============================================================================

  // PUT /api/settings/opportunity-multipliers/{id} - Update multiplier (Admin-UI)
  // POST /api/settings/opportunity-multipliers - Create custom multiplier
  // DELETE /api/settings/opportunity-multipliers/{id} - Delete custom multiplier
  // POST /api/settings/opportunity-multipliers/reset - Reset to factory defaults
}
