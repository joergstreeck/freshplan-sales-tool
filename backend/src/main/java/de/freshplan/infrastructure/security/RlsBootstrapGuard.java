package de.freshplan.infrastructure.security;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Bootstrap guard that ensures RLS is properly configured on application startup. In production
 * environments, this guard will fail-fast if RLS is not enabled.
 *
 * <p>This is a critical security component that prevents accidental deployment of the application
 * without proper RLS enforcement.
 */
@Startup
@ApplicationScoped
public class RlsBootstrapGuard {

  private static final Logger LOG = Logger.getLogger(RlsBootstrapGuard.class);

  @ConfigProperty(name = "security.rls.interceptor.enabled", defaultValue = "true")
  boolean rlsEnabled;

  @ConfigProperty(name = "security.rls.fail-closed", defaultValue = "true")
  boolean failClosed;

  @ConfigProperty(name = "quarkus.profile")
  String profile;

  void onStart(@Observes StartupEvent event) {
    LOG.infof(
        "RLS Bootstrap Guard starting - Profile: %s, RLS Enabled: %s, Fail-Closed: %s",
        profile, rlsEnabled, failClosed);

    // In production, RLS MUST be enabled
    if ("prod".equals(profile) && !rlsEnabled) {
      String error =
          "CRITICAL SECURITY ERROR: RLS interceptor is disabled in production environment!";
      LOG.fatal(error);
      throw new IllegalStateException(error);
    }

    // In production, fail-closed MUST be enabled
    if ("prod".equals(profile) && !failClosed) {
      String error =
          "CRITICAL SECURITY ERROR: RLS fail-closed mode is disabled in production environment!";
      LOG.fatal(error);
      throw new IllegalStateException(error);
    }

    // Warn if RLS is disabled in non-production
    if (!rlsEnabled && !"test".equals(profile)) {
      LOG.warn("RLS interceptor is DISABLED - this should only happen in test environments!");
    }

    // Log successful startup
    if (rlsEnabled) {
      LOG.info("✅ RLS Bootstrap Guard: Security checks passed");
      LOG.infof("  - Interceptor: ENABLED");
      LOG.infof("  - Fail-Closed: %s", failClosed ? "ENABLED" : "DISABLED");
      LOG.infof("  - Profile: %s", profile);
    }
  }
}
