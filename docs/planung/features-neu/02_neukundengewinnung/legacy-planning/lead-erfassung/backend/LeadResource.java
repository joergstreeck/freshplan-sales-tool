/**
 * LeadResource – Foundation Standards V2
 * - API Standards: /docs/planung/grundlagen/API_STANDARDS.md
 * - Security: ABAC via ScopeContext (territory/chain) – JWT Claims
 * - Validation: Bean Validation, RFC7807 Errors
 * - Performance: P95 < 200 ms (measure via Prometheus)
 */
package com.freshplan.leads.api;

import com.freshplan.leads.dto.LeadDTO;
import com.freshplan.leads.service.LeadService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/leads")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeadResource {

  @Inject LeadService leads;

  @GET
  @RolesAllowed({"user","manager"})
  public LeadDTO.Page search(@QueryParam("q") String q,
                             @QueryParam("status") String status,
                             @QueryParam("territory") String territory,
                             @QueryParam("limit") @DefaultValue("50") int limit,
                             @QueryParam("cursor") String cursor) {
    return leads.search(q, status, territory, cursor, limit);
  }

  @POST
  @RolesAllowed({"user","manager"})
  public Response create(@Valid LeadDTO.CreateRequest req) {
    leads.create(req);
    return Response.status(Response.Status.CREATED).build();
  }
}
