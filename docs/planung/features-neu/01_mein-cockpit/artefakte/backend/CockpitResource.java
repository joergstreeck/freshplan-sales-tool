package de.freshplan.cockpit.api;

import de.freshplan.cockpit.dto.CockpitDTO;
import de.freshplan.cockpit.service.CockpitService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/api/cockpit")
@Produces(MediaType.APPLICATION_JSON)
public class CockpitResource {

  @Inject CockpitService service;

  @GET @Path("/summary")
  @RolesAllowed({"user","manager","admin"})
  public CockpitDTO.Summary summary(@QueryParam("range") @DefaultValue("30d") String range,
                                    @QueryParam("territory") String territory,
                                    @QueryParam("channels") String channels) {
    return service.getSummary(range, territory, channels);
  }

  @GET @Path("/filters")
  @RolesAllowed({"user","manager","admin"})
  public Map<String,Object> filters() {
    return service.filters();
  }
}
