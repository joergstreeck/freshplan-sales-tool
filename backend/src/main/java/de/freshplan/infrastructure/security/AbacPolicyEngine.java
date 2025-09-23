package de.freshplan.infrastructure.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * ABAC (Attribute-Based Access Control) Policy Engine für FreshFoodz CRM.
 * Evaluates complex B2B access rules based on user attributes, resource attributes,
 * and environment context.
 *
 * Core concepts:
 * - Territory-based isolation (DE/CH)
 * - User-based lead ownership
 * - Role-based permissions (CHEF/BUYER)
 * - Time-based access rules
 */
@ApplicationScoped
public class AbacPolicyEngine {

    private static final Logger LOG = Logger.getLogger(AbacPolicyEngine.class);

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    /**
     * Policy evaluation result with detailed reasoning
     */
    public static class PolicyResult {
        private final boolean allowed;
        private final String reason;
        private final Map<String, Object> metadata;

        public PolicyResult(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
            this.metadata = new HashMap<>();
        }

        public PolicyResult(boolean allowed, String reason, Map<String, Object> metadata) {
            this.allowed = allowed;
            this.reason = reason;
            this.metadata = metadata;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }

    /**
     * Resource context for policy evaluation
     */
    public static class ResourceContext {
        private final String resourceType;
        private final UUID resourceId;
        private final Map<String, Object> attributes;

        public ResourceContext(String resourceType, UUID resourceId) {
            this.resourceType = resourceType;
            this.resourceId = resourceId;
            this.attributes = new HashMap<>();
        }

        public ResourceContext withAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public String getResourceType() {
            return resourceType;
        }

        public UUID getResourceId() {
            return resourceId;
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        @SuppressWarnings("unchecked")
        public <T> T getAttribute(String key, Class<T> type) {
            return (T) attributes.get(key);
        }
    }

    /**
     * Evaluates if current user can perform action on resource
     */
    public PolicyResult evaluate(String action, ResourceContext resource) {
        LOG.debugf("Evaluating ABAC policy: action=%s, resource=%s, user=%s",
                action, resource.getResourceType(), getCurrentUserId());

        // Extract user attributes from JWT
        UserAttributes userAttrs = extractUserAttributes();

        // Apply policy rules based on resource type
        return switch (resource.getResourceType()) {
            case "lead" -> evaluateLeadPolicy(action, resource, userAttrs);
            case "contact" -> evaluateContactPolicy(action, resource, userAttrs);
            case "account" -> evaluateAccountPolicy(action, resource, userAttrs);
            case "settings" -> evaluateSettingsPolicy(action, resource, userAttrs);
            default -> new PolicyResult(false, "Unknown resource type: " + resource.getResourceType());
        };
    }

    /**
     * Lead access policy evaluation
     */
    private PolicyResult evaluateLeadPolicy(String action, ResourceContext resource, UserAttributes userAttrs) {
        String territory = resource.getAttribute("territory", String.class);
        UUID ownerId = resource.getAttribute("owner_id", UUID.class);
        String status = resource.getAttribute("status", String.class);

        // Territory isolation check
        if (!userAttrs.territory.equals(territory)) {
            return new PolicyResult(false, "Territory mismatch: user=" + userAttrs.territory + ", lead=" + territory);
        }

        // Check ownership
        boolean isOwner = userAttrs.userId.equals(ownerId);

        // Check collaborator status
        boolean isCollaborator = Optional.ofNullable(resource.getAttribute("collaborators", Set.class))
                .map(collabs -> ((Set<UUID>) collabs).contains(userAttrs.userId))
                .orElse(false);

        // Manager override
        boolean hasOverride = userAttrs.scopes.contains("lead:override");

        return switch (action) {
            case "view" -> {
                if (isOwner || isCollaborator || userAttrs.scopes.contains("lead:read") || hasOverride) {
                    yield new PolicyResult(true, "Access granted: " +
                            (isOwner ? "owner" : isCollaborator ? "collaborator" : "scope"));
                }
                yield new PolicyResult(false, "No view permission for lead");
            }
            case "edit" -> {
                if (isOwner || hasOverride) {
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("audit_required", hasOverride && !isOwner);
                    yield new PolicyResult(true, isOwner ? "Owner can edit" : "Manager override", meta);
                }
                yield new PolicyResult(false, "Only owner can edit lead");
            }
            case "delete" -> {
                if ("CONVERTED".equals(status)) {
                    yield new PolicyResult(false, "Cannot delete converted lead");
                }
                if (isOwner || userAttrs.scopes.contains("lead:delete")) {
                    yield new PolicyResult(true, "Delete permission granted");
                }
                yield new PolicyResult(false, "No delete permission");
            }
            case "assign" -> {
                if (isOwner || userAttrs.scopes.contains("lead:assign")) {
                    yield new PolicyResult(true, "Assignment permission granted");
                }
                yield new PolicyResult(false, "No assignment permission");
            }
            default -> new PolicyResult(false, "Unknown action: " + action);
        };
    }

    /**
     * Contact access policy evaluation
     */
    private PolicyResult evaluateContactPolicy(String action, ResourceContext resource, UserAttributes userAttrs) {
        String contactRole = resource.getAttribute("contact_role", String.class);
        UUID leadId = resource.getAttribute("lead_id", UUID.class);

        // Contact access inherits from lead access
        ResourceContext leadContext = new ResourceContext("lead", leadId);
        // Copy lead attributes from resource context
        leadContext.withAttribute("territory", resource.getAttribute("lead_territory"))
                .withAttribute("owner_id", resource.getAttribute("lead_owner_id"))
                .withAttribute("collaborators", resource.getAttribute("lead_collaborators"));

        PolicyResult leadResult = evaluateLeadPolicy("view", leadContext, userAttrs);
        if (!leadResult.isAllowed()) {
            return new PolicyResult(false, "No access to parent lead");
        }

        // Additional rules for specific contact roles
        if ("CHEF".equals(contactRole) && action.equals("view_sensitive")) {
            if (!userAttrs.roles.contains("manager") && !userAttrs.userId.equals(resource.getAttribute("lead_owner_id"))) {
                return new PolicyResult(false, "Only managers and lead owners can view CHEF sensitive data");
            }
        }

        return new PolicyResult(true, "Contact access granted via lead permission");
    }

    /**
     * Account access policy evaluation
     */
    private PolicyResult evaluateAccountPolicy(String action, ResourceContext resource, UserAttributes userAttrs) {
        String accountTerritory = resource.getAttribute("territory", String.class);

        // Territory check
        if (!userAttrs.territory.equals(accountTerritory)) {
            return new PolicyResult(false, "Account in different territory");
        }

        // Account team membership check
        Set<UUID> teamMembers = resource.getAttribute("team_members", Set.class);
        if (teamMembers != null && teamMembers.contains(userAttrs.userId)) {
            return new PolicyResult(true, "Account team member");
        }

        // Scope-based access
        if (userAttrs.scopes.contains("account:" + action)) {
            return new PolicyResult(true, "Scope-based access: account:" + action);
        }

        return new PolicyResult(false, "No account access");
    }

    /**
     * Settings access policy evaluation
     */
    private PolicyResult evaluateSettingsPolicy(String action, ResourceContext resource, UserAttributes userAttrs) {
        String scope = resource.getAttribute("scope", String.class);

        return switch (scope) {
            case "GLOBAL" -> {
                if (userAttrs.roles.contains("admin")) {
                    yield new PolicyResult(true, "Admin can modify global settings");
                }
                yield new PolicyResult(false, "Only admins can modify global settings");
            }
            case "TERRITORY" -> {
                String settingTerritory = resource.getAttribute("territory", String.class);
                if (userAttrs.territory.equals(settingTerritory) && userAttrs.roles.contains("manager")) {
                    yield new PolicyResult(true, "Territory manager can modify settings");
                }
                yield new PolicyResult(false, "Only territory managers can modify territory settings");
            }
            case "USER" -> {
                UUID settingUserId = resource.getAttribute("user_id", UUID.class);
                if (userAttrs.userId.equals(settingUserId)) {
                    yield new PolicyResult(true, "User can modify own settings");
                }
                yield new PolicyResult(false, "Cannot modify other user's settings");
            }
            default -> new PolicyResult(true, "Settings access granted");
        };
    }

    /**
     * Extract user attributes from JWT and security context
     */
    private UserAttributes extractUserAttributes() {
        UserAttributes attrs = new UserAttributes();

        // User ID from JWT
        attrs.userId = UUID.fromString(jwt.getClaim("sub"));

        // Organization
        attrs.organization = jwt.getClaim("org_id");

        // Territory
        attrs.territory = jwt.getClaim("territory");
        if (attrs.territory == null) {
            attrs.territory = "DE"; // Default to Germany
        }

        // Roles
        attrs.roles = new HashSet<>(jwt.getGroups());

        // Scopes from custom claim
        Object scopesClaim = jwt.getClaim("scopes");
        if (scopesClaim instanceof List) {
            attrs.scopes = new HashSet<>((List<String>) scopesClaim);
        } else if (scopesClaim instanceof String) {
            attrs.scopes = new HashSet<>(Arrays.asList(((String) scopesClaim).split(",")));
        } else {
            attrs.scopes = new HashSet<>();
        }

        // Contact roles (CHEF/BUYER)
        Object contactRolesClaim = jwt.getClaim("contact_roles");
        if (contactRolesClaim instanceof List) {
            attrs.contactRoles = new HashSet<>((List<String>) contactRolesClaim);
        } else {
            attrs.contactRoles = new HashSet<>();
        }

        LOG.debugf("Extracted user attributes: userId=%s, org=%s, territory=%s, roles=%s, scopes=%s",
                attrs.userId, attrs.organization, attrs.territory, attrs.roles, attrs.scopes);

        return attrs;
    }

    /**
     * Get current user ID
     */
    private UUID getCurrentUserId() {
        return UUID.fromString(jwt.getClaim("sub"));
    }

    /**
     * Internal class to hold user attributes
     */
    private static class UserAttributes {
        UUID userId;
        String organization;
        String territory;
        Set<String> roles;
        Set<String> scopes;
        Set<String> contactRoles;
    }

    /**
     * Check if current user has specific scope
     */
    public boolean hasScope(String scope) {
        UserAttributes attrs = extractUserAttributes();
        return attrs.scopes.contains(scope);
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return securityIdentity.hasRole(role);
    }

    /**
     * Get current user's territory
     */
    public String getCurrentTerritory() {
        return extractUserAttributes().territory;
    }
}