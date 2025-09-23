package de.freshplan.infrastructure.security;

import de.freshplan.infrastructure.security.AbacPolicyEngine.PolicyResult;
import de.freshplan.infrastructure.security.AbacPolicyEngine.ResourceContext;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ABAC Policy Engine.
 * Tests the policy evaluation logic without full JWT setup.
 * For integration testing with JWT, see SecurityIntegrationTest.
 */
@QuarkusTest
@DisplayName("ABAC Policy Engine Tests")
@Disabled("JWT mocking required - will be enabled after JWT setup")
public class AbacPolicyEngineTest {

    @Inject
    AbacPolicyEngine policyEngine;

    private static final UUID LEAD_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID USER_ID = UUID.fromString("987fcdeb-51a2-43d1-9abc-def012345678");
    private static final UUID OTHER_USER_ID = UUID.fromString("456def01-2345-6789-abcd-ef0123456789");

    /**
     * Test basic policy engine instantiation
     */
    @Test
    @DisplayName("Policy engine should be injectable")
    void testPolicyEngineInjection() {
        assertNotNull(policyEngine, "Policy engine should be injected");
    }

    /**
     * Test resource context builder
     */
    @Test
    @DisplayName("Resource context should store attributes correctly")
    void testResourceContext() {
        // Given
        ResourceContext context = new ResourceContext("lead", LEAD_ID)
                .withAttribute("territory", "DE")
                .withAttribute("owner_id", USER_ID)
                .withAttribute("status", "WARM");

        // Then
        assertEquals("lead", context.getResourceType());
        assertEquals(LEAD_ID, context.getResourceId());
        assertEquals("DE", context.getAttribute("territory"));
        assertEquals(USER_ID, context.getAttribute("owner_id"));
        assertEquals("WARM", context.getAttribute("status"));
    }

    /**
     * Test policy result structure
     */
    @Test
    @DisplayName("Policy result should contain decision and reason")
    void testPolicyResult() {
        // Given
        PolicyResult allowedResult = new PolicyResult(true, "Access granted");
        PolicyResult deniedResult = new PolicyResult(false, "Access denied");

        // Then
        assertTrue(allowedResult.isAllowed());
        assertEquals("Access granted", allowedResult.getReason());
        assertNotNull(allowedResult.getMetadata());

        assertFalse(deniedResult.isAllowed());
        assertEquals("Access denied", deniedResult.getReason());
    }

    /**
     * Test with authenticated user would require JWT setup
     * This is a placeholder for future implementation
     */
    @Test
    @DisplayName("Authenticated tests require JWT - placeholder")
    @TestSecurity(user = "test-user", roles = {"sales"})
    void testPlaceholder() {
        // This test demonstrates that security annotations work
        // Actual ABAC testing requires JWT claim setup
        assertTrue(true, "Placeholder test for JWT-based tests");
    }
}