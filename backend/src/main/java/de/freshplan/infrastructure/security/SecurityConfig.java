package de.freshplan.infrastructure.security;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Security configuration for the application. Provides centralized security settings and
 * initialization.
 */
@ApplicationScoped
public class SecurityConfig {

  private static final Logger LOG = Logger.getLogger(SecurityConfig.class);

  @ConfigProperty(name = "quarkus.oidc.enabled", defaultValue = "true")
  boolean oidcEnabled;

  @ConfigProperty(name = "quarkus.oidc.auth-server-url", defaultValue = "")
  String authServerUrl;

  @ConfigProperty(name = "quarkus.oidc.client-id", defaultValue = "")
  String clientId;

  /** Log security configuration on startup. */
  void onStart(@Observes StartupEvent ev) {
    LOG.info("=== Security Configuration ===");
    LOG.infof("OIDC Enabled: %s", oidcEnabled);
    LOG.infof("Auth Server URL: %s", authServerUrl);
    LOG.infof("Client ID: %s", clientId);

    if (!oidcEnabled) {
      LOG.warn("⚠️  Security is DISABLED! This should only be used in development.");
    } else {
      LOG.info(
          "✅ Security is ENABLED. All endpoints will require authentication unless marked with @PermitAll.");
    }

    LOG.info("=== Available Security Roles ===");
    LOG.info("- admin: Full system access");
    LOG.info("- manager: Management functions");
    LOG.info("- sales: Sales operations");
  }

  /** Security role constants for consistent usage across the application. */
  public static class Roles {
    public static final String ADMIN = "admin";
    public static final String MANAGER = "manager";
    public static final String SALES = "sales";

    private Roles() {
      // Utility class
    }
  }

  /** Check if security is enabled. */
  public boolean isSecurityEnabled() {
    return oidcEnabled;
  }
}
