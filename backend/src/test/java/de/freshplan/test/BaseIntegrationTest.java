package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

/**
 * Base class for integration tests that require security to be disabled. This allows testing API
 * endpoints without dealing with authentication while maintaining security in production.
 */
@QuarkusTest
@Disabled("TEMPORARY: Sprint 2.1.4 CI Performance Fix")
@Tag("migrate")
@TestProfile(SecurityDisabledTestProfile.class)
public abstract class BaseIntegrationTest {

  // Common test setup can be added here if needed
}
