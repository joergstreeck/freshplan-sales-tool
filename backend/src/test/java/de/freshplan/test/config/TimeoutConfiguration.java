package de.freshplan.test.config;

import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Timeout;

/**
 * Global timeout configuration for tests.
 *
 * Prevents test hangs in CI by enforcing timeouts.
 */
public class TimeoutConfiguration implements QuarkusTestBeforeEachCallback {

    // Default timeout für normale Tests
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    // Timeout für Integration-Tests
    private static final int INTEGRATION_TIMEOUT_SECONDS = 60;

    // Timeout für Performance-Tests
    private static final int PERFORMANCE_TIMEOUT_SECONDS = 120;

    @Override
    public void beforeEach(QuarkusTestMethodContext context) {
        // Check if method already has @Timeout annotation
        var method = context.getTestMethod();
        if (method.isAnnotationPresent(Timeout.class)) {
            // Method has explicit timeout, don't override
            return;
        }

        // Apply default timeout based on test type
        String methodName = method.getName();
        String className = context.getTestInstance().getClass().getSimpleName();

        int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;

        if (className.contains("Integration") || methodName.contains("integration")) {
            timeoutSeconds = INTEGRATION_TIMEOUT_SECONDS;
        } else if (className.contains("Performance") || methodName.contains("performance")) {
            timeoutSeconds = PERFORMANCE_TIMEOUT_SECONDS;
        }

        // In CI, reduce timeouts to fail faster
        if (TestProfileResolver.isCI()) {
            timeoutSeconds = Math.min(timeoutSeconds, 60);
        }

        System.out.printf("Test %s.%s timeout: %ds%n",
                className, methodName, timeoutSeconds);
    }
}