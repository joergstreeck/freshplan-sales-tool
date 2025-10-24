package de.freshplan.api.resources;

import de.freshplan.infrastructure.jobs.SalesRepSyncJob;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Admin Jobs Resource
 *
 * <p>Sprint 2.1.7.2 D6 - Manual Job Triggers for Admin UI
 *
 * <p><b>Endpoints:</b>
 *
 * <ul>
 *   <li>POST /api/admin/jobs/sync-sales-reps - Trigger Sales-Rep Sync Job manually
 * </ul>
 *
 * <p><b>Security:</b> ADMIN role required for all endpoints
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@Path("/api/admin/jobs")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class AdminJobsResource {

  private static final Logger logger = LoggerFactory.getLogger(AdminJobsResource.class);

  @Inject SalesRepSyncJob salesRepSyncJob;

  /**
   * Trigger Sales-Rep Sync Job manually.
   *
   * <p>POST /api/admin/jobs/sync-sales-reps
   *
   * <p><b>Use Cases:</b>
   *
   * <ul>
   *   <li>Initial setup (sync before scheduled 2 AM run)
   *   <li>After adding new users (immediate sync needed)
   *   <li>Testing/debugging sync logic
   * </ul>
   *
   * <p><b>Response:</b>
   *
   * <ul>
   *   <li>200 OK: Sync completed successfully
   *   <li>500 Internal Server Error: Sync failed (check logs)
   * </ul>
   *
   * @return Response with status message
   */
  @POST
  @Path("/sync-sales-reps")
  public Response triggerSalesRepSync() {
    logger.info("🔧 Admin manually triggered Sales-Rep sync");

    try {
      salesRepSyncJob.triggerManualSync();

      return Response.ok()
          .entity(
              new JobTriggerResponse(
                  "success", "Sales-Rep sync completed successfully. Check logs for details."))
          .build();

    } catch (Exception e) {
      logger.error("❌ Manual Sales-Rep sync failed", e);

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(
              new JobTriggerResponse(
                  "error", "Sales-Rep sync failed: " + e.getMessage() + ". Check server logs."))
          .build();
    }
  }

  /**
   * Job Trigger Response DTO.
   *
   * @param status "success" or "error"
   * @param message Human-readable status message
   */
  public record JobTriggerResponse(String status, String message) {}
}
