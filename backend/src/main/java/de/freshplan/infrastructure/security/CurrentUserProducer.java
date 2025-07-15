package de.freshplan.infrastructure.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.Set;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

/**
 * CDI producer for the current user principal. This creates a UserPrincipal instance from the JWT
 * token or provides a system user for development mode.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@RequestScoped
public class CurrentUserProducer {

  private static final Logger LOG = Logger.getLogger(CurrentUserProducer.class);

  @Inject Instance<JsonWebToken> jwtInstance;

  @Inject SecurityContextProvider securityContext;

  @Produces
  @CurrentUser
  @RequestScoped
  public UserPrincipal getCurrentUser() {
    // First try JWT token
    if (!jwtInstance.isUnsatisfied() && !jwtInstance.isAmbiguous()) {
      JsonWebToken token = jwtInstance.get();
      if (token.getName() != null) {
        return UserPrincipal.builder()
            .username(token.getName())
            .email(token.getClaim("email"))
            .roles(token.getGroups())
            .authenticated(true)
            .build();
      }
    }

    // Fallback to SecurityContextProvider
    if (securityContext.isAuthenticated() && securityContext.getUsername() != null) {
      Set<String> roles = securityContext.getRoles();

      return UserPrincipal.builder()
          .username(securityContext.getUsername())
          .email(securityContext.getUsername() + "@freshplan.de")
          .roles(roles)
          .authenticated(true)
          .build();
    }

    // Development mode fallback
    String profile = io.quarkus.runtime.LaunchMode.current().getDefaultProfile();
    if ("dev".equals(profile) || "test".equals(profile)) {
      LOG.debug("No authentication found, using system user for development");
      return UserPrincipal.system();
    }

    // Production mode - return anonymous user
    LOG.warn("No authentication found in production mode");
    return UserPrincipal.anonymous();
  }
}
