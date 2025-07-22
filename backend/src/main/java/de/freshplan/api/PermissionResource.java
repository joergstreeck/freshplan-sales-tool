package de.freshplan.api;

import de.freshplan.domain.permission.annotation.PermissionRequired;
// import de.freshplan.domain.permission.entity.Permission;  // TEMPORARILY DISABLED - NO PERMISSION
// TABLES YET
import de.freshplan.domain.permission.service.PermissionService;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for permission management from FC-009.
 *
 * <p>TEMPORARILY DISABLED - Permission entities and tables not ready yet. Only basic user
 * permission check endpoints active.
 */
@Path("/api/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionResource {

  @Inject PermissionService permissionService;

  @Inject SecurityContextProvider securityProvider;

  /** Get current user's permissions - used by frontend PermissionContext. */
  @GET
  @Path("/me")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response getCurrentUserPermissions() {
    List<String> permissions = permissionService.getCurrentUserPermissions();
    return Response.ok(Map.of("permissions", permissions)).build();
  }

  /**
   * Get all available permissions - for admin UI. STEP 3: Permission entity activated, simple test
   */
  @GET
  @RolesAllowed({"admin"})
  public Response getAllPermissions() {
    try {
      // Simple query test - get all permissions from database
      List<de.freshplan.domain.permission.entity.Permission> permissions =
          de.freshplan.domain.permission.entity.Permission.listAll();

      // Convert to simple response
      List<Map<String, String>> permissionList =
          permissions.stream()
              .map(
                  p ->
                      Map.of(
                          "code", p.getPermissionCode(),
                          "name", p.getName(),
                          "resource", p.getResource(),
                          "action", p.getAction()))
              .collect(java.util.stream.Collectors.toList());

      return Response.ok(Map.of("permissions", permissionList)).build();

    } catch (Exception e) {
      return Response.status(500)
          .entity(Map.of("error", "Failed to load permissions: " + e.getMessage()))
          .build();
    }
  }

  /** Grant permission to user. TEMPORARILY DISABLED - Permission service not ready. */
  @POST
  @Path("/grant")
  @RolesAllowed({"admin"})
  public Response grantPermission(GrantPermissionRequest request) {
    // permissionService.grantUserPermission(request.userId, request.permissionCode,
    // request.reason);  // DISABLED
    return Response.status(Response.Status.NOT_IMPLEMENTED)
        .entity(Map.of("message", "Permission management not yet implemented"))
        .build();
  }

  /** Revoke permission from user. TEMPORARILY DISABLED - Permission service not ready. */
  @POST
  @Path("/revoke")
  @RolesAllowed({"admin"})
  public Response revokePermission(RevokePermissionRequest request) {
    // permissionService.revokeUserPermission(request.userId, request.permissionCode,
    // request.reason);  // DISABLED
    return Response.status(Response.Status.NOT_IMPLEMENTED)
        .entity(Map.of("message", "Permission management not yet implemented"))
        .build();
  }

  /** Check if current user has specific permission. */
  @GET
  @Path("/check/{permissionCode}")
  @RolesAllowed({"admin", "manager", "sales"})
  public Response checkPermission(@PathParam("permissionCode") String permissionCode) {
    boolean hasPermission = permissionService.hasPermission(permissionCode);
    return Response.ok(Map.of("hasPermission", hasPermission)).build();
  }

  /** Get permissions for specific user. */
  @GET
  @Path("/user/{userId}")
  @PermissionRequired("admin:permissions")
  public Response getUserPermissions(@PathParam("userId") UUID userId) {
    // This would require UserService integration - simplified for now
    return Response.ok(Map.of("permissions", List.of())).build();
  }

  // DTOs
  public static class GrantPermissionRequest {
    public UUID userId;
    public String permissionCode;
    public boolean granted = true;
    public String reason;
  }

  public static class RevokePermissionRequest {
    public UUID userId;
    public String permissionCode;
    public String reason;
  }
}
