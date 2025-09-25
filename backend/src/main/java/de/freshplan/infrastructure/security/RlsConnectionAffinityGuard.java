package de.freshplan.infrastructure.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * CDI Interceptor that ensures RLS context is set on the same database connection used for the
 * actual database operations.
 *
 * <p>This interceptor solves the critical security issue where GUC variables set via
 * ContainerRequestFilter may end up on different connections due to connection pooling.
 *
 * <p>IMPORTANT: Methods annotated with @RlsContext MUST also be annotated with @Transactional to
 * ensure connection affinity.
 */
@RlsContext
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 100) // After auth, before business logic
@Dependent // CDI Interceptors must be @Dependent scoped
public class RlsConnectionAffinityGuard {

  private static final Logger LOG = Logger.getLogger(RlsConnectionAffinityGuard.class);

  @Inject EntityManager em;

  @Inject SecurityIdentity securityIdentity;

  @Inject TransactionSynchronizationRegistry tsr;

  @ConfigProperty(name = "security.rls.interceptor.enabled", defaultValue = "true")
  boolean rlsEnabled;

  @ConfigProperty(name = "security.rls.fail-closed", defaultValue = "true")
  boolean failClosed;

  @AroundInvoke
  public Object setRlsContext(InvocationContext context) throws Exception {
    if (!rlsEnabled) {
      LOG.debug("RLS interceptor disabled, proceeding without context");
      return context.proceed();
    }

    // Verify we have an active transaction (critical for RLS safety)
    if (tsr.getTransactionKey() == null) {
      String error = "No active transaction for RLS context (fail-closed).";
      LOG.error(error);
      if (failClosed) {
        throw new IllegalStateException(error);
      }
    }

    // Check if method is transactional
    if (!isTransactional(context)) {
      String error =
          "Method "
              + context.getMethod().getName()
              + " uses @RlsContext but is not @Transactional. This breaks connection affinity!";
      LOG.error(error);
      if (failClosed) {
        throw new SecurityException(error);
      }
    }

    // Extract user context
    String currentUser = extractCurrentUser();
    String currentRole = extractCurrentRole();
    String tenantId = extractTenantId();
    String territory = extractTerritory();

    // Set GUC variables on the current connection
    setGucVariables(currentUser, currentRole, tenantId, territory);

    try {
      // Execute the actual method
      return context.proceed();
    } finally {
      // GUC variables are automatically cleared at transaction end due to SET LOCAL
      LOG.debugf("RLS context cleared automatically at transaction end");
    }
  }

  private boolean isTransactional(InvocationContext context) {
    // Check method level
    if (context.getMethod().isAnnotationPresent(Transactional.class)) {
      return true;
    }
    // Check class level
    return context.getTarget().getClass().isAnnotationPresent(Transactional.class);
  }

  private String extractCurrentUser() {
    if (!securityIdentity.isAnonymous()) {
      return securityIdentity.getPrincipal().getName();
    }
    return null;
  }

  private String extractCurrentRole() {
    if (!securityIdentity.isAnonymous() && !securityIdentity.getRoles().isEmpty()) {
      // Return the first role in uppercase for consistency with DB policies
      String role = securityIdentity.getRoles().iterator().next();
      return role != null ? role.toUpperCase() : null;
    }
    return null;
  }

  private String extractTenantId() {
    // Extract from JWT claim or request context
    // TODO: FP-263 - Implement multi-tenancy JWT claim extraction (Sprint 2.x)
    // For now, returns null which triggers fail-closed behavior
    return securityIdentity.getAttribute("tenant_id");
  }

  private String extractTerritory() {
    // Extract from JWT claim or user settings
    // TODO: FP-264 - Implement territory extraction from UserSettings or JWT (Sprint 2.x)
    // For now, returns null which triggers fail-closed behavior
    return securityIdentity.getAttribute("territory");
  }

  private void setGucVariables(String user, String role, String tenant, String territory) {
    LOG.debugf(
        "Setting RLS context: user=%s, role=%s, tenant=%s, territory=%s",
        user, role, tenant, territory);

    // Use set_config with parameters for safety (transaction-scoped)
    if (user != null) {
      em.createNativeQuery(AppGuc.CURRENT_USER.setLocalConfigSql())
          .setParameter(1, AppGuc.CURRENT_USER.getKey())
          .setParameter(2, user)
          .getSingleResult();
    }
    if (role != null) {
      // Always uppercase roles for consistency with RLS policies
      em.createNativeQuery(AppGuc.CURRENT_ROLE.setLocalConfigSql())
          .setParameter(1, AppGuc.CURRENT_ROLE.getKey())
          .setParameter(2, role.toUpperCase())
          .getSingleResult();
    }
    if (tenant != null) {
      em.createNativeQuery(AppGuc.TENANT_ID.setLocalConfigSql())
          .setParameter(1, AppGuc.TENANT_ID.getKey())
          .setParameter(2, tenant)
          .getSingleResult();
    }
    if (territory != null) {
      em.createNativeQuery(AppGuc.CURRENT_TERRITORY.setLocalConfigSql())
          .setParameter(1, AppGuc.CURRENT_TERRITORY.getKey())
          .setParameter(2, territory)
          .getSingleResult();
    }

    // Verify GUCs are set (only in debug mode)
    if (LOG.isDebugEnabled()) {
      verifyGucVariables();
    }
  }

  private void verifyGucVariables() {
    try {
      String verifyUser =
          (String) em.createNativeQuery(AppGuc.CURRENT_USER.getConfigSql()).getSingleResult();
      String verifyTerritory =
          (String) em.createNativeQuery(AppGuc.CURRENT_TERRITORY.getConfigSql()).getSingleResult();
      LOG.debugf("GUC verification: user=%s, territory=%s", verifyUser, verifyTerritory);
    } catch (Exception e) {
      LOG.warn("Failed to verify GUC variables", e);
    }
  }
}
