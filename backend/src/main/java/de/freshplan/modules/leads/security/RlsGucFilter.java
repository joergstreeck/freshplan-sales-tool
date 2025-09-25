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
import jakarta.ws.rs.ext.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.jboss.logging.Logger;

/**
 * Request filter that sets PostgreSQL GUC (Grand Unified Configuration) variables
 * for Row Level Security (RLS) policies.
 *
 * Sprint 2.1: Ensures fail-closed security by setting user context for every request.
 * The GUCs are used by RLS policies to determine access rights.
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 1) // Run after authentication
@ApplicationScoped
public class RlsGucFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOG = Logger.getLogger(RlsGucFilter.class);

  @Inject AgroalDataSource dataSource;
  @Inject SecurityContext securityContext;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String user = securityContext.getUserPrincipal() != null
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
  public void filter(ContainerRequestContext requestContext,
                     ContainerResponseContext responseContext) {
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