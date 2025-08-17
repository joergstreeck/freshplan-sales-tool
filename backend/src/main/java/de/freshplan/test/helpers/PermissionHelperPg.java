package de.freshplan.test.helpers;

import de.freshplan.domain.permission.entity.Permission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.UUID;

/**
 * Race-condition-safe helper for Permission creation in tests.
 * 
 * Uses PostgreSQL ON CONFLICT clause to ensure atomic "INSERT OR SELECT" operations,
 * preventing race conditions when multiple tests try to create the same permission
 * simultaneously (e.g., in parallel CI execution).
 * 
 * <h3>Why this approach?</h3>
 * <ul>
 *   <li>No exception flows - always returns a valid Permission</li>
 *   <li>100% thread-safe at database level</li>
 *   <li>Atomic operation - no separate INSERT and SELECT</li>
 *   <li>Works perfectly with parallel test execution</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * <pre>{@code
 * @Inject
 * PermissionHelperPg permissionHelper;
 * 
 * // Safe to call from multiple threads/tests simultaneously
 * Permission readPerm = permissionHelper.findOrCreatePermission(
 *     "customers:read", 
 *     "Read customer data"
 * );
 * }</pre>
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class PermissionHelperPg {
    
    @Inject
    EntityManager em;
    
    /**
     * Find or create a permission atomically using PostgreSQL ON CONFLICT.
     * 
     * This method is completely race-condition-safe. If multiple threads/tests
     * call this method with the same permission code simultaneously, all will
     * receive the same Permission instance without any errors.
     * 
     * The description is prefixed with [TEST] to identify test-created permissions.
     * 
     * @param code The permission code (e.g., "customers:read")
     * @param description The permission description
     * @return The existing or newly created Permission
     * @throws IllegalArgumentException if code doesn't follow "resource:action" format
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Permission findOrCreatePermission(String code, String description) {
        // Validate permission code format
        if (code == null || !code.contains(":")) {
            throw new IllegalArgumentException(
                "Permission code must follow format 'resource:action', got: " + code
            );
        }
        
        String[] parts = code.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Permission code must have exactly one ':', got: " + code
            );
        }
        
        String resource = parts[0];
        String action = parts[1];
        
        // Mark test permissions in description
        String testDescription = description != null 
            ? "[TEST] " + description 
            : "[TEST] Auto-created permission";
        
        // Derive name from code if not in description
        String name = description != null && !description.isEmpty() 
            ? description 
            : code.replace(":", " ").replace("_", " ");
        
        // Use PostgreSQL ON CONFLICT for atomic upsert
        // This returns the ID whether the row was inserted or already existed
        UUID permissionId = (UUID) em.createNativeQuery("""
            INSERT INTO permissions (
                id, 
                permission_code, 
                name, 
                description, 
                resource, 
                action, 
                created_at
            )
            VALUES (
                gen_random_uuid(), 
                :code, 
                :name, 
                :description, 
                :resource, 
                :action, 
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (permission_code) DO UPDATE
                SET description = COALESCE(permissions.description, EXCLUDED.description)
            RETURNING id
            """)
            .setParameter("code", code)
            .setParameter("name", name)
            .setParameter("description", testDescription)
            .setParameter("resource", resource)
            .setParameter("action", action)
            .getSingleResult();
        
        // Return the Permission entity
        return em.find(Permission.class, permissionId);
    }
    
    /**
     * Find or create a permission with a default description.
     * 
     * @param code The permission code (e.g., "customers:read")
     * @return The existing or newly created Permission
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Permission findOrCreatePermission(String code) {
        return findOrCreatePermission(code, "Test permission: " + code);
    }
    
    /**
     * Create multiple permissions atomically.
     * Useful for setting up test scenarios that need multiple permissions.
     * 
     * @param codes Array of permission codes
     * @return Array of Permission entities in the same order
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Permission[] findOrCreatePermissions(String... codes) {
        Permission[] permissions = new Permission[codes.length];
        for (int i = 0; i < codes.length; i++) {
            permissions[i] = findOrCreatePermission(codes[i]);
        }
        return permissions;
    }
    
    /**
     * Delete test permissions created by this helper.
     * Only deletes permissions with [TEST] prefix in description.
     * 
     * This is useful for cleanup in integration tests that don't use @TestTransaction.
     * 
     * @return Number of permissions deleted
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int deleteTestPermissions() {
        return em.createNativeQuery("""
            DELETE FROM permissions 
            WHERE description LIKE '[TEST]%'
            """)
            .executeUpdate();
    }
}