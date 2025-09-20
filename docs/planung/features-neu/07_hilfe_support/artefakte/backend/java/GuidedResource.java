package de.freshplan.help.api;

import de.freshplan.help.domain.*;
import de.freshplan.help.service.HelpService;
import de.freshplan.security.ScopeContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/api/help/guided")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class GuidedResource {

  @Inject HelpService service;
  @Inject ScopeContext scope;

  @POST @Path("/follow-up") @RolesAllowed({"user","manager","admin"})
  public Response planFollowUp(@Valid FollowUpPlanRequest req){
    // read defaults from settings (Modul 06) – for brevity, fallback to P3D,P7D
    java.util.List<String> defaults = java.util.List.of("P3D","P7D");
    var res = service.planFollowUp(req, defaults);
    return Response.ok(res).build();
  }

  @POST @Path("/roi-check") @RolesAllowed({"user","manager","admin"})
  public Response roiQuick(@Valid RoiQuickCheckRequest req){
    var res = service.roiQuick(req);
    return Response.ok(res).build();
  }
}
