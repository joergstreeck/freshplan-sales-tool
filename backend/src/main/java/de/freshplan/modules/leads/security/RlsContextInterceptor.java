package de.freshplan.modules.leads.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.SecurityContext;
import java.sql.PreparedStatement;
import org.hibernate.Session;
import org.jboss.logging.Logger;

/**
 * Interceptor that sets PostgreSQL GUC variables for RLS on the SAME connection that Hibernate uses
 * for queries. This ensures proper RLS enforcement.
 *
 * <p>Sprint 2.1: Critical fix for connection affinity - GUCs must be set on the same connection
 * that executes the queries.
 */
@Interceptor
@RlsContext
@Priority(Priorities.AUTHORIZATION + 1)
@Dependent // Interceptors must be @Dependent scoped
public class RlsContextInterceptor {

  private static final Logger LOG = Logger.getLogger(RlsContextInterceptor.class);

  @Inject EntityManager em;

  @Inject SecurityContext securityContext;

  @AroundInvoke
  @Transactional // Ensures we work on the same TX/Connection
  public Object setRlsContext(InvocationContext ctx) throws Exception {
    String user =
        securityContext.getUserPrincipal() != null
            ? securityContext.getUserPrincipal().getName()
            : "anonymous";

    String role = determineRole();

    // Work on THE connection of the current Hibernate session
    Session session = em.unwrap(Session.class);
    session.doWork(
        connection -> {
          try (PreparedStatement ps =
              connection.prepareStatement("SELECT set_config(?, ?, true)")) {
            // Set user context
            ps.setString(1, "app.current_user");
            ps.setString(2, user);
            ps.execute();

            // Set role context
            ps.setString(1, "app.current_role");
            ps.setString(2, role);
            ps.execute();

            LOG.debugf(
                "RLS context set on Hibernate connection for user: %s, role: %s", user, role);
          } catch (Exception e) {
            LOG.warnf(e, "Failed to set RLS context for user %s", user);
            // Don't fail the request - RLS policies will default to fail-closed
          }
        });

    // Store in context for debugging
    ctx.getContextData().put("rls.user", user);
    ctx.getContextData().put("rls.role", role);

    try {
      return ctx.proceed();
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debugf("Request completed with RLS context - user: %s, role: %s", user, role);
      }
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
