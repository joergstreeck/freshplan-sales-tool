package de.freshplan.infrastructure.security;

import io.quarkus.security.identity.SecurityIdentity;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JAX-RS Filter that sets PostgreSQL session variables for Row-Level Security (RLS).
 * This filter runs on every request and ensures the database connection has the correct
 * security context from the JWT token.
 *
 * Sets the following PostgreSQL session variables:
 * - app.user_id: Current user UUID
 * - app.org_id: Organization/tenant ID
 * - app.territory: User's territory (DE/CH)
 * - app.scopes: Comma-separated list of permissions
 * - app.contact_roles: User's contact roles (CHEF/BUYER)
 */
@Provider
@Priority(2000) // Run after authentication but before other filters
@RequestScoped
public class SessionSettingsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(SessionSettingsFilter.class);

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @Inject
    DataSource dataSource;

    @Context
    HttpServerRequest request;

    private static final String CONNECTION_KEY = "rls.connection";
    private static final String SKIP_RLS_HEADER = "X-Skip-RLS";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip for health checks and metrics
        String path = requestContext.getUriInfo().getPath();
        if (isSystemPath(path)) {
            LOG.trace("Skipping RLS for system path: " + path);
            return;
        }

        // Skip if explicitly requested (for admin operations)
        if ("true".equals(requestContext.getHeaderString(SKIP_RLS_HEADER))) {
            if (!securityIdentity.hasRole("admin")) {
                LOG.warn("Non-admin attempted to skip RLS: " + getCurrentUserId());
            } else {
                LOG.debug("Admin skipping RLS for request");
                return;
            }
        }

        // Only proceed if user is authenticated
        if (securityIdentity.isAnonymous()) {
            LOG.trace("Skipping RLS for anonymous request");
            return;
        }

        try {
            // Get a connection and set session variables
            Connection connection = dataSource.getConnection();
            setSessionVariables(connection);

            // Store connection in request context for later cleanup
            requestContext.setProperty(CONNECTION_KEY, connection);

            LOG.debugf("RLS session configured for user %s, territory %s",
                    getCurrentUserId(), getCurrentTerritory());

        } catch (SQLException e) {
            LOG.error("Failed to set RLS session variables", e);
            // Don't fail the request, but log for monitoring
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Clean up connection if it was created
        Connection connection = (Connection) requestContext.getProperty(CONNECTION_KEY);
        if (connection != null) {
            try {
                // Reset session variables before returning connection to pool
                resetSessionVariables(connection);
                connection.close();
                LOG.trace("RLS connection cleaned up");
            } catch (SQLException e) {
                LOG.error("Failed to clean up RLS connection", e);
            }
        }
    }

    /**
     * Set PostgreSQL session variables from JWT claims
     */
    private void setSessionVariables(Connection connection) throws SQLException {
        // Prepare session variable commands
        List<String> commands = new ArrayList<>();

        // User ID (required)
        String userId = getCurrentUserId();
        if (userId != null) {
            commands.add(String.format("SET LOCAL app.user_id = '%s'", userId));
        }

        // Organization ID
        String orgId = getClaimAsString("org_id");
        if (orgId != null) {
            commands.add(String.format("SET LOCAL app.org_id = '%s'", orgId));
        }

        // Territory
        String territory = getCurrentTerritory();
        if (territory != null) {
            commands.add(String.format("SET LOCAL app.territory = '%s'", territory));
        }

        // Scopes (permissions)
        String scopes = getScopes();
        if (scopes != null && !scopes.isEmpty()) {
            commands.add(String.format("SET LOCAL app.scopes = '%s'", scopes));
        }

        // Contact roles
        String contactRoles = getContactRoles();
        if (contactRoles != null && !contactRoles.isEmpty()) {
            commands.add(String.format("SET LOCAL app.contact_roles = '%s'", contactRoles));
        }

        // Execute all commands in a single batch for performance
        if (!commands.isEmpty()) {
            String sql = String.join("; ", commands);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.execute();
                LOG.tracef("Set %d session variables for RLS", commands.size());
            }
        }
    }

    /**
     * Reset session variables to prevent leakage between requests
     */
    private void resetSessionVariables(Connection connection) throws SQLException {
        String resetSql = """
            RESET app.user_id;
            RESET app.org_id;
            RESET app.territory;
            RESET app.scopes;
            RESET app.contact_roles;
            """;

        try (PreparedStatement stmt = connection.prepareStatement(resetSql)) {
            stmt.execute();
        }
    }

    /**
     * Get current user ID from JWT
     */
    private String getCurrentUserId() {
        try {
            return jwt.getClaim("sub");
        } catch (Exception e) {
            LOG.debug("Could not extract user ID from JWT", e);
            return null;
        }
    }

    /**
     * Get current territory from JWT
     */
    private String getCurrentTerritory() {
        String territory = getClaimAsString("territory");
        if (territory == null) {
            // Default to Germany if not specified
            territory = "DE";
            LOG.trace("No territory in JWT, defaulting to DE");
        }
        return territory;
    }

    /**
     * Get scopes as comma-separated string
     */
    private String getScopes() {
        Object scopesClaim = jwt.getClaim("scopes");

        if (scopesClaim instanceof List) {
            return ((List<?>) scopesClaim).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } else if (scopesClaim instanceof String) {
            return (String) scopesClaim;
        }

        // Derive basic scopes from roles
        Set<String> derivedScopes = new HashSet<>();
        if (securityIdentity.hasRole("admin")) {
            derivedScopes.add("lead:override");
            derivedScopes.add("lead:delete");
            derivedScopes.add("lead:assign");
        }
        if (securityIdentity.hasRole("manager")) {
            derivedScopes.add("lead:read");
            derivedScopes.add("lead:assign");
        }
        if (securityIdentity.hasRole("sales")) {
            derivedScopes.add("lead:create");
            derivedScopes.add("lead:read");
        }

        return String.join(",", derivedScopes);
    }

    /**
     * Get contact roles as comma-separated string
     */
    private String getContactRoles() {
        Object rolesClaim = jwt.getClaim("contact_roles");

        if (rolesClaim instanceof List) {
            return ((List<?>) rolesClaim).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } else if (rolesClaim instanceof String) {
            return (String) rolesClaim;
        }

        return "";
    }

    /**
     * Generic claim extraction
     */
    private String getClaimAsString(String claimName) {
        try {
            Object claim = jwt.getClaim(claimName);
            return claim != null ? claim.toString() : null;
        } catch (Exception e) {
            LOG.debugf("Could not extract claim %s from JWT", claimName);
            return null;
        }
    }

    /**
     * Check if path is a system path that should skip RLS
     */
    private boolean isSystemPath(String path) {
        return path.startsWith("/q/health") ||
               path.startsWith("/q/metrics") ||
               path.startsWith("/q/openapi") ||
               path.startsWith("/q/swagger-ui");
    }
}