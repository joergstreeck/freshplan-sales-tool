package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Bootstrap test to ensure database migrations run before actual tests.
 * 
 * This test does nothing except start Quarkus, which triggers Flyway migrations.
 * Used in CI to ensure the database schema is ready before running the test suite.
 * 
 * @author FreshPlan Team
 * @since 2025-08-16
 */
@QuarkusTest
class MigrationBootstrapIT {
    
    /**
     * Empty test that just verifies Quarkus context starts successfully.
     * This triggers Flyway migrations to run.
     */
    @Test
    void contextStarts() {
        // Intentionally empty - just triggers Flyway migrations
        // The test passes if Quarkus starts successfully
    }
}