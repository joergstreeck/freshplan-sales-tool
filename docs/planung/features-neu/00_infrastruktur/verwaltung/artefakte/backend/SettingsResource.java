package de.freshplan.governance.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;
import java.util.stream.Collectors;

@Path("/api/settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class SettingsResource {

  @Inject SettingsService service;
  @Inject JsonWebToken jwt;
  @Inject ObjectMapper mapper;
  @Inject MeterRegistry metrics;

  @GET
  @Path("/effective")
  public Response getEffective(@QueryParam("keys") String keysCsv, @HeaderParam("If-None-Match") String ifNoneMatch){
    SettingsPrincipal p = SettingsPrincipal.fromJwt(jwt);
    Set<String> keys = (keysCsv==null || keysCsv.isBlank())
        ? Collections.emptySet()
        : Arrays.stream(keysCsv.split(",")).map(String::trim).collect(Collectors.toSet());
    SettingsService.EffectiveResult res = service.getEffective(p, keys);
    EntityTag etag = new EntityTag(res.etag());
    if (ifNoneMatch != null && ifNoneMatch.replace(""","").equals(res.etag())){
      return Response.notModified(etag).build();
    }
    return Response.ok(res.payload()).tag(etag).build();
  }

  @PATCH
  public Response patchSetting(SettingsService.PatchRequest req){
    validateScope(req);
    SettingsPrincipal p = SettingsPrincipal.fromJwt(jwt);
    service.patch(p, req);
    return Response.noContent().build();
  }

  private void validateScope(SettingsService.PatchRequest req){
    switch (req.scope()){
      case "global" -> { /* require admin via ABAC in service/DB */ }
      case "tenant" -> { if (req.tenantId().isEmpty()) throw new BadRequestException("tenantId required for tenant scope"); }
      case "org" -> { if (req.tenantId().isEmpty() || req.orgId().isEmpty()) throw new BadRequestException("tenantId + orgId required for org scope"); }
      case "user" -> { if (req.tenantId().isEmpty() || req.orgId().isEmpty() || req.userId().isEmpty()) throw new BadRequestException("tenantId + orgId + userId required for user scope"); }
      default -> throw new BadRequestException("Unknown scope: "+req.scope());
    }
  }
}
