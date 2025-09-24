package de.freshplan.modules.leads.api;

import de.freshplan.modules.leads.domain.Territory;
import de.freshplan.modules.leads.service.TerritoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST API for territory management. Sprint 2.1: Provides access to territory configuration for
 * currency, tax and business rules.
 */
@Path("/api/territories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
    name = "Territories",
    description = "Territory management for currency, tax and business rules")
public class TerritoryResource {

  private static final Logger LOG = Logger.getLogger(TerritoryResource.class);

  @Inject TerritoryService territoryService;

  @GET
  @Operation(summary = "Get all territories", description = "Returns all configured territories")
  public List<Territory> getAllTerritories() {
    LOG.debug("Getting all territories");
    return territoryService.getAllTerritories();
  }

  @GET
  @Path("/{code}")
  @Operation(
      summary = "Get territory by code",
      description = "Returns a specific territory by code")
  public Response getTerritory(@PathParam("code") String code) {
    LOG.debugf("Getting territory: %s", code);
    Territory territory = territoryService.getTerritory(code);
    if (territory == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(territory).build();
  }

  @GET
  @Path("/by-country/{countryCode}")
  @Operation(
      summary = "Get territory by country",
      description = "Determines territory based on country code")
  public Response getTerritoryByCountry(@PathParam("countryCode") String countryCode) {
    LOG.debugf("Determining territory for country: %s", countryCode);
    Territory territory = territoryService.determineTerritory(countryCode);
    return Response.ok(territory).build();
  }
}
