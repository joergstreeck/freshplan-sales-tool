package de.freshplan.api;

import de.freshplan.infrastructure.security.SecurityContextProvider;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Test resource for verifying security implementation. This is a temporary resource for testing
 * FC-008 Security Foundation.
 */
@Path("/api/security-test")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Security Test", description = "Endpoints for testing security implementation")
public class SecurityTestResource {

  @Inject SecurityContextProvider securityContext;

  @GET
  @Path("/public")
  @Operation(
      summary = "Public endpoint",
      description = "This endpoint should be accessible without authentication")
  @APIResponse(responseCode = "200", description = "Success")
  @PermitAll
  public Response publicEndpoint() {
    return Response.ok(new SecurityTestResponse("This is a public endpoint", null, null)).build();
  }

  @GET
  @Path("/authenticated")
  @Authenticated
  @Operation(
      summary = "Authenticated endpoint",
      description = "This endpoint requires authentication")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Success"),
    @APIResponse(responseCode = "401", description = "Unauthorized")
  })
  public Response authenticatedEndpoint() {
    try {
      String username = securityContext.getUsername();
      String email = securityContext.getEmail();
      return Response.ok(new SecurityTestResponse("You are authenticated", username, email))
          .build();
    } catch (Exception e) {
      // Re-throw as a runtime exception to ensure the test fails and reveals the issue.
      throw new RuntimeException("Error in authenticatedEndpoint while getting security context", e);
    }
  }

  @GET
  @Path("/admin")
  @RolesAllowed("admin")
  @Operation(summary = "Admin only endpoint", description = "This endpoint requires admin role")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Success"),
    @APIResponse(responseCode = "401", description = "Unauthorized"),
    @APIResponse(responseCode = "403", description = "Forbidden")
  })
  public Response adminEndpoint() {
    String username = securityContext.getUsername();
    var roles = securityContext.getRoles();
    return Response.ok(new AdminTestResponse("You have admin access", username, roles)).build();
  }

  @GET
  @Path("/manager")
  @RolesAllowed("manager")
  @Operation(summary = "Manager only endpoint", description = "This endpoint requires manager role")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Success"),
    @APIResponse(responseCode = "401", description = "Unauthorized"),
    @APIResponse(responseCode = "403", description = "Forbidden")
  })
  public Response managerEndpoint() {
    String username = securityContext.getUsername();
    var roles = securityContext.getRoles();
    return Response.ok(new AdminTestResponse("You have manager access", username, roles)).build();
  }

  @GET
  @Path("/sales")
  @RolesAllowed({"admin", "manager", "sales"})
  @Operation(
      summary = "Sales endpoint",
      description = "This endpoint requires admin, manager or sales role")
  @APIResponses({
    @APIResponse(responseCode = "200", description = "Success"),
    @APIResponse(responseCode = "401", description = "Unauthorized"),
    @APIResponse(responseCode = "403", description = "Forbidden")
  })
  public Response salesEndpoint() {
    String username = securityContext.getUsername();
    var roles = securityContext.getRoles();
    return Response.ok(new AdminTestResponse("You have sales access", username, roles)).build();
  }

  // Response DTOs
  public static record SecurityTestResponse(String message, String username, String email) {}

  public static record AdminTestResponse(
      String message, String username, java.util.Set<String> roles) {}
}
