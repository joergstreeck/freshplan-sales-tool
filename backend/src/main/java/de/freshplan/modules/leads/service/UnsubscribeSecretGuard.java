package de.freshplan.modules.leads.service;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Security Guard für Unsubscribe Token Secret
 *
 * <p>Stellt sicher, dass in Production ein sicheres Secret konfiguriert ist und verhindert den
 * Start mit Development-Fallback
 */
@Startup
@ApplicationScoped
public class UnsubscribeSecretGuard {

  private static final Logger LOG = Logger.getLogger(UnsubscribeSecretGuard.class);
  private static final String DEV_SECRET = "dev-secret-change-in-production";

  @ConfigProperty(name = "quarkus.profile")
  String profile;

  @ConfigProperty(name = "freshplan.unsubscribe.token.secret")
  Optional<String> tokenSecret;

  @PostConstruct
  void verifySecretConfiguration() {
    boolean isProduction = "prod".equals(profile) || "production".equals(profile);
    String secret = tokenSecret.orElse("");

    // In Production: Erzwinge konfiguriertes Secret
    if (isProduction) {
      if (secret.isEmpty()) {
        throw new IllegalStateException(
            "CRITICAL: freshplan.unsubscribe.token.secret MUST be configured in production!");
      }

      if (secret.equals(DEV_SECRET)) {
        throw new IllegalStateException(
            "CRITICAL: Development token secret detected in production! "
                + "Configure a secure secret via freshplan.unsubscribe.token.secret");
      }

      if (secret.length() < 32) {
        throw new IllegalStateException(
            "CRITICAL: Token secret too short! Minimum 32 characters required for production.");
      }

      LOG.info("✅ Unsubscribe token secret properly configured for production");
    } else {
      // Development/Test: Only warn, don't fail
      if (secret.isEmpty()) {
        LOG.debug("No token secret configured, using development fallback (ok for non-prod)");
      } else if (secret.equals(DEV_SECRET)) {
        LOG.debug("Using development token secret (ok for non-prod)");
      } else if (secret.length() < 32) {
        LOG.warn("Token secret is weak (<32 chars) in non-prod environment");
      }
    }
  }

  /**
   * Liefert das konfigurierte Secret oder Development-Fallback Wird von EmailNotificationService
   * verwendet
   */
  public String getTokenSecret() {
    String secret = tokenSecret.orElse("");
    if (secret.isEmpty()) {
      LOG.debug("No token secret configured, using development fallback");
      return DEV_SECRET;
    }
    return secret;
  }
}
