package de.freshplan.api.dev;

import de.freshplan.domain.testdata.service.TestDataService;
import de.freshplan.infrastructure.security.SecurityAudit;
import de.freshplan.infrastructure.security.SecurityContextProvider;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for managing test data in development environment. All endpoints are under /api/dev
 * to clearly separate from production APIs.
 */
@Path("/api/dev/test-data")
@RolesAllowed("admin")
@SecurityAudit
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Development - Test Data", description = "Test data management for development")
public class TestDataResource {

  @Inject TestDataService testDataService;

  @Inject SecurityContextProvider securityContext;

  @POST
  @Path("/seed")
  @Operation(
      summary = "Seed test data",
      description = "Seeds the database with diverse test customers and related data")
  @APIResponse(responseCode = "200", description = "Test data seeded successfully")
  @APIResponse(responseCode = "500", description = "Error seeding test data")
  public Response seedTestData() {
    // Only allow in development mode
    if (!isDevelopmentMode()) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(new ErrorResponse("Test data operations are only allowed in development mode"))
          .build();
    }

    try {
      var result = testDataService.seedTestData();
      return Response.ok(
              new SeedResponse(
                  "Test data seeded successfully",
                  result.customersCreated(),
                  result.eventsCreated()))
          .build();
    } catch (Exception e) {
      return Response.serverError()
          .entity(new ErrorResponse("Failed to seed test data: " + e.getMessage()))
          .build();
    }
  }

  @DELETE
  @Path("/clean")
  @Operation(summary = "Clean test data", description = "Removes all test data from the database")
  @APIResponse(responseCode = "200", description = "Test data cleaned successfully")
  @APIResponse(responseCode = "500", description = "Error cleaning test data")
  public Response cleanTestData() {
    // Only allow in development mode
    if (!isDevelopmentMode()) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(new ErrorResponse("Test data operations are only allowed in development mode"))
          .build();
    }

    try {
      var result = testDataService.cleanTestData();
      return Response.ok(
              new CleanupResponse(
                  "Test data cleaned successfully",
                  result.customersDeleted(),
                  result.eventsDeleted()))
          .build();
    } catch (Exception e) {
      return Response.serverError()
          .entity(new ErrorResponse("Failed to clean test data: " + e.getMessage()))
          .build();
    }
  }

  @GET
  @Path("/stats")
  @Operation(
      summary = "Get test data statistics",
      description = "Returns current count of test data in the database")
  @APIResponse(responseCode = "200", description = "Test data statistics")
  public Response getTestDataStats() {
    var stats = testDataService.getTestDataStats();
    return Response.ok(new StatsResponse(stats.customerCount(), stats.eventCount())).build();
  }

  @DELETE
  @Path("/clean-old")
  @Operation(
      summary = "Clean old test data",
      description = "One-time cleanup to remove old test data without [TEST] prefix")
  @APIResponse(responseCode = "200", description = "Old test data cleaned successfully")
  public Response cleanOldTestData() {
    // Only allow in development mode
    if (!isDevelopmentMode()) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(new ErrorResponse("Test data operations are only allowed in development mode"))
          .build();
    }

    try {
      var result = testDataService.cleanOldTestData();
      return Response.ok(
              new CleanupResponse(
                  "Old test data cleaned successfully",
                  result.customersDeleted(),
                  result.eventsDeleted()))
          .build();
    } catch (Exception e) {
      return Response.serverError()
          .entity(new ErrorResponse("Failed to clean old test data: " + e.getMessage()))
          .build();
    }
  }

  private boolean isDevelopmentMode() {
    // Check if we're running in dev or test mode
    String profile = io.quarkus.runtime.configuration.ProfileManager.getActiveProfile();
    return "dev".equals(profile) || "test".equals(profile);
  }

  // Response DTOs
  public record SeedResponse(String message, int customersCreated, int eventsCreated) {}

  public record CleanupResponse(String message, long customersDeleted, long eventsDeleted) {}

  public record StatsResponse(long customerCount, long eventCount) {}

  public record ErrorResponse(String error) {}
}
