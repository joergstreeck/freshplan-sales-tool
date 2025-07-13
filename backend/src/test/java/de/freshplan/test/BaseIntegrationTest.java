package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * Base class for integration tests that require security to be disabled. This allows testing API
 * endpoints without dealing with authentication while maintaining security in production.
 */
@QuarkusTest
@TestProfile(SecurityDisabledTestProfile.class)
public abstract class BaseIntegrationTest {

  // Common test setup can be added here if needed
}
