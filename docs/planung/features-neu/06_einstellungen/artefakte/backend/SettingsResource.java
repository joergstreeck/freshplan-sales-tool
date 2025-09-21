package de.freshplan.settings.api;

import de.freshplan.security.ScopeContext;
import de.freshplan.settings.cache.SettingsCache;
import de.freshplan.settings.repo.SettingsRepository;
import de.freshplan.settings.service.SettingsMergeEngine;
import de.freshplan.settings.service.SettingsService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/api/settings")
@Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class SettingsResource {

  @Inject ScopeContext scope;
  @Inject SettingsCache cache;
  @Inject SettingsRepository repo;
  @Inject SettingsMergeEngine engine;
  @Inject SettingsService service;

  private SettingsMergeEngine.Scope resolveScope(String accountId, String contactRole, String contactId){
    return new SettingsMergeEngine.Scope(
      scope.getTenantId(), scope.getTerritory(),
      accountId==null? null: java.util.UUID.fromString(accountId),
      contactRole, contactId==null? null: java.util.UUID.fromString(contactId),
      scope.getUserId()
    );
  }

  @GET @Path("/effective") @RolesAllowed({"user","manager","admin"})
  public Response getEffective(@QueryParam("accountId") String accountId,
                               @QueryParam("contactRole") String contactRole,
                               @QueryParam("contactId") String contactId,
                               @HeaderParam("If-None-Match") String ifNoneMatch){
    var s = resolveScope(accountId, contactRole, contactId);
    var res = cache.getOrCompute(s);
    if (ifNoneMatch != null && ifNoneMatch.equals(res.etag())) return Response.status(Response.Status.NOT_MODIFIED).build();
    return Response.ok(java.util.Map.of("blob", res.blob(), "etag", res.etag(), "computedAt", res.computedAt().toString()))
      .header(HttpHeaders.ETAG, res.etag()).build();
  }

  public static class PatchOp { public String op; public String key; public Scope scope; public Object value; }
  public static class Scope { public String accountId; public String contactRole; public String contactId; public String territory; }

  @PATCH @Path("") @RolesAllowed({"manager","admin"})
  public Response patch(java.util.List<@Valid PatchOp> ops){
    var result = service.applyPatch(ops, scope);
    return Response.ok(java.util.Map.of("blob", result.blob(), "etag", result.etag(), "computedAt", result.computedAt().toString()))
      .header(HttpHeaders.ETAG, result.etag()).build();
  }

  @GET @Path("/keys") @RolesAllowed({"user","manager","admin"})
  public Response keys(){
    var reg = repo.loadRegistry();
    var list = new java.util.ArrayList<java.util.Map<String,Object>>();
    reg.forEach((k,v)-> list.add(java.util.Map.of(
      "key", k, "type", v.type(), "mergeStrategy", v.mergeStrategy(),
      "scope", v.scope(), "description","", "version", v.version()
    )));
    return Response.ok(list).build();
  }
}
