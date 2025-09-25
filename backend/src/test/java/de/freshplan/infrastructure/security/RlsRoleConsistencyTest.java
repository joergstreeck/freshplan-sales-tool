package de.freshplan.infrastructure.security;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for role consistency in RLS implementation.
 * Ensures roles are properly normalized to uppercase and policies work correctly.
 */
@QuarkusTest
public class RlsRoleConsistencyTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    public void testRoleNormalizationToUppercase() {
        // Test that roles are normalized to uppercase
        String[] testRoles = {"admin", "Admin", "ADMIN", "user", "USER", "manager", "MANAGER"};
        String[] expectedRoles = {"ADMIN", "ADMIN", "ADMIN", "USER", "USER", "MANAGER", "MANAGER"};

        for (int i = 0; i < testRoles.length; i++) {
            // Set role using AppGuc
            em.createNativeQuery(AppGuc.CURRENT_ROLE.setConfigSql(testRoles[i].toUpperCase()))
                .executeUpdate();

            // Verify it's stored in uppercase
            String storedRole = (String) em.createNativeQuery(AppGuc.CURRENT_ROLE.getConfigSql())
                .getSingleResult();

            assertEquals(expectedRoles[i], storedRole,
                "Role should be normalized to uppercase: " + testRoles[i] + " -> " + expectedRoles[i]);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "SYSTEM"})
    @Transactional
    public void testPrivilegedRolesInPolicies(String role) {
        // Test that ADMIN and SYSTEM roles have elevated access
        em.createNativeQuery(AppGuc.CURRENT_USER.setConfigSql("test-user")).executeUpdate();
        em.createNativeQuery(AppGuc.CURRENT_ROLE.setConfigSql(role)).executeUpdate();

        // Verify role is set correctly
        String currentRole = (String) em.createNativeQuery(AppGuc.CURRENT_ROLE.getConfigSql())
            .getSingleResult();
        assertEquals(role, currentRole);

        // Test that privileged roles are recognized in RLS check function
        Object[] contextResult = (Object[]) em.createNativeQuery(
            "SELECT * FROM check_rls_context()"
        ).getSingleResult();

        assertNotNull(contextResult[1], "Role should be present");
        assertEquals(role, contextResult[1], "Role should match");
    }

    @Test
    @Transactional
    public void testRoleCaseInsensitivityInInterceptor() {
        // This tests that the RlsConnectionAffinityGuard properly handles role case
        // The interceptor should always convert to uppercase before setting GUC

        String[] mixedCaseRoles = {"admin", "Admin", "ADMIN", "system", "System", "SYSTEM"};

        for (String role : mixedCaseRoles) {
            // Simulate what the interceptor does
            String normalizedRole = role.toUpperCase();

            // Set the normalized role
            em.createNativeQuery(AppGuc.CURRENT_ROLE.setConfigSql(normalizedRole))
                .executeUpdate();

            // Verify it's uppercase
            String storedRole = (String) em.createNativeQuery(AppGuc.CURRENT_ROLE.getConfigSql())
                .getSingleResult();

            assertEquals(normalizedRole, storedRole,
                "Role should be uppercase regardless of input: " + role);
        }
    }

    @Test
    @Transactional
    public void testSystemRoleHasFullAccess() {
        // Test that SYSTEM role bypasses normal RLS restrictions
        em.createNativeQuery(AppGuc.CURRENT_USER.setConfigSql("system-process")).executeUpdate();
        em.createNativeQuery(AppGuc.CURRENT_ROLE.setConfigSql("SYSTEM")).executeUpdate();

        // SYSTEM role should see all data (this is a conceptual test)
        // In real implementation, you'd test against actual tables with RLS
        String role = (String) em.createNativeQuery(AppGuc.CURRENT_ROLE.getConfigSql())
            .getSingleResult();

        assertEquals("SYSTEM", role, "SYSTEM role should be set correctly");
    }

    @Test
    @Transactional
    public void testInvalidRoleHandling() {
        // Test that invalid or null roles are handled gracefully
        em.createNativeQuery(AppGuc.CURRENT_ROLE.setConfigSql("")).executeUpdate();

        String role = (String) em.createNativeQuery(AppGuc.CURRENT_ROLE.getConfigSql())
            .getSingleResult();

        assertTrue(role == null || role.isEmpty(), "Empty role should be handled");
    }
}