package de.freshplan;

import org.junit.jupiter.api.TestInfo;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.Base64;

/**
 * Test ID generation utility for unique and stable test identifiers.
 *
 * Sprint 2.1.4 - Ensures collision-free test data generation.
 */
public final class TestIds {

    private TestIds() {
        // Utility class
    }

    /**
     * Generates a unique customer number for each test invocation.
     * @return A unique customer number prefixed with "TEST-"
     */
    public static String uniqueCustomerNumber() {
        return "TEST-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a stable customer number within a test run per test method (reproducible).
     * Useful when tests need to reference the same number multiple times.
     * @param info The JUnit TestInfo for the current test
     * @return A stable customer number based on test name
     */
    public static String stableCustomerNumber(TestInfo info) {
        String encoded = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(info.getDisplayName().getBytes(StandardCharsets.UTF_8));
        return "TEST-" + encoded.substring(0, Math.min(8, encoded.length()));
    }

    /**
     * Legacy fixed customer number for backward compatibility.
     * Only use for tests that explicitly require this specific value.
     * @deprecated Use uniqueCustomerNumber() or stableCustomerNumber() instead
     * @return The legacy fixed test customer number
     */
    @Deprecated(forRemoval = false)
    public static String legacyFixedCustomerNumber() {
        return "TEST-17590995";
    }
}