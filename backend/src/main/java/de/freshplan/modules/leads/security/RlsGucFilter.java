package de.freshplan.modules.leads.security;

import io.agroal.api.AgroalDataSource;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.SecurityContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.jboss.logging.Logger;

/**
 * DEPRECATED: Replaced by RlsContextInterceptor for proper connection affinity.
 *
 * <p>This filter set GUCs on a separate connection which could lead to RLS policies not being
 * enforced. Use @RlsContext annotation with RlsContextInterceptor instead.
 *
 * @deprecated Use {@link RlsContextInterceptor} with {@link RlsContext} annotation
 */
@Deprecated(since = "Sprint 2.1", forRemoval = true)
// @Provider - DISABLED to prevent double GUC setting
@Priority(Priorities.AUTHENTICATION + 1)
@ApplicationScoped
public class RlsGucFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOG = Logger.getLogger(RlsGucFilter.class);

  @Inject AgroalDataSource dataSource;
  @Inject SecurityContext securityContext;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String user =
        securityContext.getUserPrincipal() != null
            ? securityContext.getUserPrincipal().getName()
            : "anonymous";

    String role = determineRole();

    // Set GUCs for this request's database session using PreparedStatement for security
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT set_config(?, ?, true)")) {

      // Use set_config function with PreparedStatement to prevent SQL injection
      ps.setString(1, "app.current_user");
      ps.setString(2, user);
      ps.execute();

      ps.setString(1, "app.current_role");
      ps.setString(2, role);
      ps.execute();

      LOG.debugf("RLS context set for user: %s, role: %s", user, role);

      // Store connection in request context for cleanup if needed
      requestContext.setProperty("rls.user", user);
      requestContext.setProperty("rls.role", role);

    } catch (Exception e) {
      LOG.warnf(e, "Failed to set RLS context for user %s", user);
      // Don't fail the request, but log the issue
      // RLS policies will default to fail-closed without the GUCs
    }
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    // Optional: Log RLS context used for debugging
    String user = (String) requestContext.getProperty("rls.user");
    String role = (String) requestContext.getProperty("rls.role");

    if (LOG.isDebugEnabled() && user != null) {
      LOG.debugf("Request completed with RLS context - user: %s, role: %s", user, role);
    }
  }

  private String determineRole() {
    if (securityContext.isUserInRole("ADMIN")) {
      return "ADMIN";
    } else if (securityContext.isUserInRole("MANAGER")) {
      return "MANAGER";
    } else if (securityContext.isUserInRole("USER")) {
      return "USER";
    } else {
      return "ANONYMOUS";
    }
  }
}
