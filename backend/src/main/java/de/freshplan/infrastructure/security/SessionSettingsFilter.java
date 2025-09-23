package de.freshplan.infrastructure.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Minimaler Security Session Settings Filter.
 *
 * Setzt PostgreSQL Session-Variablen (GUCs) aus dem Security Context,
 * ohne Annahmen über Business-Tabellen zu treffen.
 *
 * Diese Foundation wird später von Business-Modulen genutzt,
 * wenn diese ihre eigenen RLS-Policies definieren.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@RequestScoped
public class SessionSettingsFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(SessionSettingsFilter.class);

    @Inject
    SecurityIdentity identity;

    @Inject
    Instance<JsonWebToken> jwt;

    @Inject
    DataSource dataSource;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip für anonyme Requests und System-Endpoints
        if (identity.isAnonymous() || isSystemPath(requestContext.getUriInfo().getPath())) {
            LOG.trace("Skipping session settings for anonymous/system request");
            return;
        }

        // Extract user information from security context
        String userId = extractUserId();
        String orgId = extractClaim("org_id", "freshfoodz");
        String territory = extractClaim("territory", "DE");
        String roles = String.join(",", identity.getRoles());

        LOG.debugf("Setting app context: user=%s, org=%s, territory=%s, roles=%s",
                userId, orgId, territory, roles);

        // Set PostgreSQL session variables
        try (Connection conn = dataSource.getConnection()) {
            setAppContext(conn, userId, orgId, territory, roles);
        } catch (SQLException e) {
            // Defensive: Log but don't fail the request
            LOG.warn("Failed to set app context in database", e);
            // Don't throw - let request continue without session context
        }
    }

    private void setAppContext(Connection conn, String userId, String orgId,
                              String territory, String roles) throws SQLException {
        String sql = "SELECT set_app_context(?::uuid, ?, ?, string_to_array(?, ','))";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // User ID can be null for some system operations
            if (userId != null && !userId.isEmpty()) {
                ps.setObject(1, UUID.fromString(userId));
            } else {
                ps.setNull(1, java.sql.Types.OTHER);
            }

            ps.setString(2, orgId);
            ps.setString(3, territory);
            ps.setString(4, roles);

            ps.execute();
            LOG.trace("App context set successfully");
        }
    }

    private String extractUserId() {
        try {
            // Try JWT subject first
            if (!jwt.isUnsatisfied() && jwt.get() != null && jwt.get().getSubject() != null) {
                return jwt.get().getSubject();
            }
            // Fallback to principal name
            if (identity.getPrincipal() != null) {
                return identity.getPrincipal().getName();
            }
        } catch (Exception e) {
            LOG.debug("Could not extract user ID", e);
        }
        return null;
    }

    private String extractClaim(String claimName, String defaultValue) {
        try {
            if (!jwt.isUnsatisfied() && jwt.get() != null) {
                Object claim = jwt.get().getClaim(claimName);
                if (claim != null) {
                    return claim.toString();
                }
            }
            // Try security identity attributes
            if (identity.getAttributes().containsKey(claimName)) {
                return identity.getAttribute(claimName);
            }
        } catch (Exception e) {
            LOG.debugf("Could not extract claim %s", claimName);
        }
        return defaultValue;
    }

    private boolean isSystemPath(String path) {
        return path.startsWith("/q/health") ||
               path.startsWith("/q/metrics") ||
               path.startsWith("/q/dev") ||
               path.startsWith("/q/swagger-ui") ||
               path.startsWith("/q/openapi");
    }
}