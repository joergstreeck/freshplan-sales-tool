package de.freshplan.help.api;

import de.freshplan.help.service.HelpService;
import de.freshplan.help.domain.*;
import de.freshplan.security.ScopeContext;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/api/help")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class HelpResource {

  @Inject HelpService service;
  @Inject ScopeContext scope;
  @Inject MeterRegistry meter;

  @GET @Path("/menu") @RolesAllowed({"user","manager","admin"})
  public Response menu(@QueryParam("module") String module,
                       @QueryParam("persona") String persona,
                       @QueryParam("territory") String territory,
                       @QueryParam("limit") @DefaultValue("20") int limit){
    // ABAC enforcement: persona/territory default from JWT if not provided
    String terr = (territory==null || territory.isBlank()) ? scope.getTerritory() : territory;
    String per = (persona==null || persona.isBlank()) ? scope.getPersona() : persona;
    var items = service.menu(module, per, terr, Math.min(Math.max(limit,1),50));
    return Response.ok(java.util.Map.of("items", items)).build();
  }

  @GET @Path("/suggest") @RolesAllowed({"user","manager","admin"})
  public Response suggest(@QueryParam("context") String context,
                          @QueryParam("module") String module,
                          @QueryParam("top") @DefaultValue("3") int top,
                          @QueryParam("sessionMinutes") @DefaultValue("0") int sessionMinutes,
                          @HeaderParam("X-Session-Id") String sessionId){
    if (context==null || context.isBlank()) throw problem(400,"invalid_request","context required");
    // CAR parameters (would be read from Settings; defaults reflect your chosen values)
    double minConfidence = 0.7;
    int base = 2, perHour = 1, max = 5;
    String persona = scope.getPersona();
    String territory = scope.getTerritory();
    var list = service.suggest(context, module, persona, territory, Math.min(Math.max(top,1),10), sessionId, sessionMinutes, minConfidence, base, perHour, max);
    return Response.ok(java.util.Map.of("items", list))
      .header("X-Nudge-Budget-Left", Math.max(0, max - (int)Math.ceil(list.size()))) // indicative
      .build();
  }

  @POST @Path("/feedback") @RolesAllowed({"user","manager","admin"})
  public Response feedback(java.util.Map<String,Object> body){
    // Integration: record feedback event via repository (out of scope for brevity)
    meter.counter("help_feedback_total").increment();
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  private WebApplicationException problem(int status, String type, String detail){
    var body = new java.util.LinkedHashMap<String,Object>();
    body.put("type","https://freshplan.dev/problems/"+type);
    body.put("title",type); body.put("status",status); body.put("detail",detail);
    return new WebApplicationException(Response.status(status).entity(body).build());
  }
}
