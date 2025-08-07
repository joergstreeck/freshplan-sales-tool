package de.freshplan.api.resources;

import de.freshplan.domain.customer.service.ContactService;
import de.freshplan.domain.customer.service.dto.ContactDTO;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API endpoints for location-based contact queries. Provides endpoints to retrieve contacts
 * assigned to specific locations.
 */
@Path("/api/locations/{locationId}/contacts")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Contacts", description = "Contact management operations")
public class LocationContactResource {

  @Inject ContactService contactService;

  @GET
  @Operation(summary = "Get all contacts assigned to a location")
  @APIResponse(responseCode = "200", description = "List of contacts")
  public List<ContactDTO> getContactsByLocation(
      @Parameter(description = "Location ID", required = true) @PathParam("locationId")
          UUID locationId) {
    return contactService.getContactsByLocationId(locationId);
  }
}
