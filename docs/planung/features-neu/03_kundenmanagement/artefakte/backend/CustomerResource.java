/**
 * CustomerResource (Module 03, Foundation Standards)
 * - Design System V2: UI konsumiert Theme V2 (kein Hardcoding) – siehe /design-system/
 * - API Standards: /docs/planung/grundlagen/API_STANDARDS.md (Statuscodes, Fehler, ETags)
 * - Security: ABAC Territory-Scoping via JWT Claims – /docs/planung/grundlagen/SECURITY_ABAC.md
 * - SQL Standards: RLS Policies dokumentiert in /artefakte/sql-schemas/*.sql
 */
package de.freshplan.customer;

import de.freshplan.security.ScopeContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

  @Inject CustomerService customers;
  @Inject SampleManagementService samples;

  @GET
  @RolesAllowed({"user","manager"})
  public Map<String,Object> list(@QueryParam("q") String q,
                                 @QueryParam("type") String type,
                                 @QueryParam("territory") String territory,
                                 @QueryParam("limit") @DefaultValue("50") int limit,
                                 @QueryParam("cursor") String cursor) {
    return customers.page(q, type, territory, cursor, limit);
  }

  @POST
  @RolesAllowed({"user","manager"})
  public Response create(@Valid Map<String,Object> req) {
    customers.create(req);
    return Response.status(Response.Status.CREATED).build();
  }

  @GET @Path("/{id}")
  @RolesAllowed({"user","manager"})
  public Map<String,Object> detail(@PathParam("id") String id) {
    return customers.detail(id);
  }

  @GET @Path("/{id}/cookfresh/samples")
  @RolesAllowed({"user","manager"})
  public Map<String,Object> samples(@PathParam("id") String customerId) {
    return samples.listByCustomer(customerId);
  }

  @POST @Path("/{id}/cookfresh/samples")
  @RolesAllowed({"user","manager"})
  public Response requestSample(@PathParam("id") String customerId, Map<String,Object> body) {
    samples.requestSample(customerId, body);
    return Response.status(Response.Status.CREATED).build();
  }
}