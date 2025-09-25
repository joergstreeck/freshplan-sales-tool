package de.freshplan.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Health check endpoint for RLS validation. Provides monitoring capabilities to ensure RLS is
 * properly configured.
 */
@Path("/health/rls")
@ApplicationScoped
public class RlsHealthCheckResource {

  private static final Logger LOG = Logger.getLogger(RlsHealthCheckResource.class);

  @Inject EntityManager em;

  @ConfigProperty(name = "security.rls.interceptor.enabled", defaultValue = "true")
  boolean rlsEnabled;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  @RlsContext
  public Response checkRlsHealth() {
    Map<String, Object> health = new HashMap<>();

    try {
      // Check if RLS interceptor is enabled
      health.put("rls_interceptor_enabled", rlsEnabled);

      // Check current GUC context
      Object[] contextResult =
          (Object[]) em.createNativeQuery("SELECT * FROM check_rls_context()").getSingleResult();

      health.put("current_user", contextResult[0]);
      health.put("current_role", contextResult[1]);
      health.put("tenant_id", contextResult[2]);
      health.put("current_territory", contextResult[3]);
      health.put("rls_policies_active", contextResult[4]);

      // Check connection pool stats
      // Note: Standard DB users can only see their own sessions,
      // so we filter by current database for more accurate results
      Long activeConnections =
          (Long)
              em.createNativeQuery(
                      "SELECT count(*) FROM pg_stat_activity WHERE state = 'active' AND datname = current_database()")
                  .getSingleResult();

      Long idleConnections =
          (Long)
              em.createNativeQuery(
                      "SELECT count(*) FROM pg_stat_activity WHERE state = 'idle' AND datname = current_database()")
                  .getSingleResult();

      // TODO: FP-266 - Replace with Agroal datasource metrics for better reliability

      health.put("active_connections", activeConnections);
      health.put("idle_connections", idleConnections);

      // Check if fail-closed is working
      Long rowsWithoutGuc =
          (Long)
              em.createNativeQuery(
                      "SELECT count(*) FROM leads WHERE current_setting('app.current_user', true) IS NULL")
                  .getSingleResult();

      health.put("fail_closed_working", rowsWithoutGuc == 0);

      // Overall status
      boolean healthy =
          rlsEnabled
              && (Boolean) contextResult[4]
              && activeConnections < 80
              && // Connection pool threshold
              rowsWithoutGuc == 0;

      health.put("status", healthy ? "UP" : "DOWN");
      health.put("timestamp", System.currentTimeMillis());

      return Response.ok(health).build();

    } catch (Exception e) {
      LOG.error("RLS health check failed", e);
      health.put("status", "ERROR");
      health.put("error", e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(health).build();
    }
  }

  /**
   * Metrics endpoint for RLS monitoring. TODO: FP-265 - Implement proper metrics collection in
   * Sprint 2.x Currently disabled to avoid shipping stub code to production.
   */
  /*
  @GET
  @Path("/metrics")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response getRlsMetrics() {
      Map<String, Object> metrics = new HashMap<>();

      try {
          // GUC set operations count (would need to be tracked in interceptor)
          metrics.put("guc_set_operations", 0L); // TODO: FP-265 - Implement counter in interceptor

          // Failed RLS checks (would need to be tracked)
          metrics.put("rls_failures", 0L); // TODO: FP-265 - Implement counter

          // Average GUC setup time
          metrics.put("avg_guc_setup_ms", 5L); // TODO: FP-265 - Measure actual time

          // Connection affinity violations
          metrics.put("affinity_violations", 0L); // TODO: FP-265 - Track violations

          return Response.ok(metrics).build();

      } catch (Exception e) {
          LOG.error("Failed to get RLS metrics", e);
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of("error", e.getMessage()))
                        .build();
      }
  }
  */
}
