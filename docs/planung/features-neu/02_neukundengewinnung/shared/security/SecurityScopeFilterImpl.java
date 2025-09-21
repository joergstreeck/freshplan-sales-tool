package de.freshplan.security;

import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Production-ready ABAC Security Filter with JWT Claims Processing
 *
 * Implements territory and chain-based access control via JWT claims
 * for multi-tenant B2B Food distribution system.
 *
 * @see ../../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security Implementation
 * @see ../../../grundlagen/API_STANDARDS.md - Jakarta EE REST Standards
 * @see ../../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms Requirements
 *
 * JWT Claim Structure Expected:
 * {
 *   "sub": "user123",
 *   "territories": ["DE_NORTH", "DE_SOUTH"],
 *   "chain_id": "EDEKA",
 *   "roles": ["GENUSSBERATER", "ADMIN"],
 *   "tenant": "freshfoodz"
 * }
 *
 * @author Security Team
 * @version 2.0
 * @since 2025-09-19
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
@ApplicationScoped
public class SecurityScopeFilterImpl implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityScopeFilterImpl.class);

    @Inject
    ScopeContext scopeContext;

    @Inject
    JsonWebToken jwt;

    // Configurable claim names
    private static final String CLAIM_TERRITORIES = "territories";
    private static final String CLAIM_CHAIN_ID = "chain_id";
    private static final String CLAIM_TENANT = "tenant";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_USER_ID = "sub";

    // Expected tenant for validation
    private static final String EXPECTED_TENANT = "freshfoodz";

    // Header-based scopes for development/testing only
    private static final boolean ALLOW_DEV_HEADERS =
        "development".equals(System.getProperty("quarkus.profile", "prod"));

    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {
            // Extract and validate JWT claims
            if (jwt != null && jwt.getClaimNames() != null && !jwt.getClaimNames().isEmpty()) {
                processJwtClaims(requestContext);
            } else if (ALLOW_DEV_HEADERS) {
                // Fallback to headers in development mode only
                processDevHeaders(requestContext);
            } else {
                // No valid authentication in production
                unauthorizedResponse(requestContext, "Missing or invalid JWT token");
                return;
            }

            // Validate that we have required scopes
            if (scopeContext.getTerritories().isEmpty()) {
                unauthorizedResponse(requestContext, "No territories assigned to user");
                return;
            }

            log.debug("Security context established: territories={}, chainId={}, userId={}",
                     scopeContext.getTerritories(),
                     scopeContext.getChainId(),
                     scopeContext.getUserId());

        } catch (Exception e) {
            log.error("Security filter error", e);
            unauthorizedResponse(requestContext, "Authentication processing failed");
        }
    }

    /**
     * Process JWT claims and populate security context
     */
    private void processJwtClaims(ContainerRequestContext requestContext) {
        // Extract user ID
        String userId = jwt.getSubject();
        if (userId == null || userId.isBlank()) {
            unauthorizedResponse(requestContext, "Invalid JWT: missing subject");
            return;
        }
        scopeContext.setUserId(userId);

        // Validate tenant (multi-tenant isolation)
        String tenant = extractStringClaim(CLAIM_TENANT);
        if (!EXPECTED_TENANT.equals(tenant)) {
            log.warn("Tenant mismatch: expected={}, actual={}", EXPECTED_TENANT, tenant);
            unauthorizedResponse(requestContext, "Invalid tenant");
            return;
        }
        scopeContext.setTenant(tenant);

        // Extract territories
        List<String> territories = extractTerritories();
        if (territories.isEmpty()) {
            log.warn("No territories found for user: {}", userId);
        }
        scopeContext.setTerritories(territories);

        // Extract chain ID (optional)
        String chainId = extractStringClaim(CLAIM_CHAIN_ID);
        scopeContext.setChainId(chainId);

        // Extract roles
        List<String> roles = extractRoles();
        scopeContext.setRoles(roles);

        // Log successful authentication
        log.info("JWT authenticated: user={}, territories={}, chain={}, roles={}",
                userId, territories.size(), chainId, roles);
    }

    /**
     * Extract territories from JWT claim
     * Handles both array and comma-separated string formats
     */
    private List<String> extractTerritories() {
        List<String> territories = new ArrayList<>();

        try {
            Object claim = jwt.getClaim(CLAIM_TERRITORIES);

            if (claim instanceof JsonArray) {
                JsonArray array = (JsonArray) claim;
                for (JsonValue value : array) {
                    if (value instanceof JsonString) {
                        String territory = ((JsonString) value).getString();
                        if (!territory.isBlank()) {
                            territories.add(territory.trim().toUpperCase());
                        }
                    }
                }
            } else if (claim instanceof List<?>) {
                // Direct List from some JWT implementations
                for (Object item : (List<?>) claim) {
                    String territory = String.valueOf(item);
                    if (!territory.isBlank()) {
                        territories.add(territory.trim().toUpperCase());
                    }
                }
            } else if (claim instanceof String) {
                // Comma-separated fallback
                String terrStr = (String) claim;
                for (String territory : terrStr.split(",")) {
                    if (!territory.isBlank()) {
                        territories.add(territory.trim().toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract territories from JWT", e);
        }

        return territories;
    }

    /**
     * Extract roles from JWT claim
     */
    private List<String> extractRoles() {
        List<String> roles = new ArrayList<>();

        try {
            Object claim = jwt.getClaim(CLAIM_ROLES);

            if (claim instanceof JsonArray) {
                JsonArray array = (JsonArray) claim;
                for (JsonValue value : array) {
                    if (value instanceof JsonString) {
                        roles.add(((JsonString) value).getString());
                    }
                }
            } else if (claim instanceof List<?>) {
                for (Object item : (List<?>) claim) {
                    roles.add(String.valueOf(item));
                }
            } else if (claim instanceof String) {
                // Comma-separated fallback
                String rolesStr = (String) claim;
                roles.addAll(Arrays.asList(rolesStr.split(",")));
            }
        } catch (Exception e) {
            log.error("Failed to extract roles from JWT", e);
        }

        return roles;
    }

    /**
     * Extract a string claim from JWT
     */
    private String extractStringClaim(String claimName) {
        try {
            Object claim = jwt.getClaim(claimName);
            if (claim != null) {
                if (claim instanceof JsonString) {
                    return ((JsonString) claim).getString();
                } else {
                    return String.valueOf(claim);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract claim: {}", claimName, e);
        }
        return null;
    }

    /**
     * Process development headers (only in dev mode)
     */
    private void processDevHeaders(ContainerRequestContext requestContext) {
        log.warn("Using development header authentication - NOT FOR PRODUCTION");

        String userId = requestContext.getHeaderString("X-User-Id");
        if (userId == null) {
            userId = "dev-user";
        }
        scopeContext.setUserId(userId);

        String territoriesHeader = requestContext.getHeaderString("X-Territories");
        if (territoriesHeader != null && !territoriesHeader.isBlank()) {
            List<String> territories = Arrays.asList(territoriesHeader.split(","));
            scopeContext.setTerritories(territories);
        } else {
            // Default development territory
            scopeContext.setTerritories(Arrays.asList("DEV_TERRITORY"));
        }

        String chainId = requestContext.getHeaderString("X-Chain-Id");
        scopeContext.setChainId(chainId);

        String roles = requestContext.getHeaderString("X-Roles");
        if (roles != null) {
            scopeContext.setRoles(Arrays.asList(roles.split(",")));
        } else {
            scopeContext.setRoles(Arrays.asList("DEV_USER"));
        }

        scopeContext.setTenant(EXPECTED_TENANT);
    }

    /**
     * Send unauthorized response
     */
    private void unauthorizedResponse(ContainerRequestContext requestContext, String message) {
        log.warn("Unauthorized access attempt: {}", message);

        Map<String, Object> error = new HashMap<>();
        error.put("error", "unauthorized");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());

        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                   .entity(error)
                   .build()
        );
    }
}