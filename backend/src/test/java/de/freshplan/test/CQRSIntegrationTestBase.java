package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Base class for CQRS integration tests.
 * Provides common configuration and utilities for testing CQRS implementations.
 */
public abstract class CQRSIntegrationTestBase extends BaseIntegrationTestWithCleanup {

  /**
   * Test profile that enables CQRS and disables test data initialization.
   */
  public static class CQRSEnabledTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          // Enable CQRS for testing
          "features.cqrs.enabled", "true",
          // Disable automatic test data initialization
          "app.init.data", "false",
          // Use test profile for DB
          "quarkus.datasource.db-kind", "postgresql",
          // Clean database before tests
          "quarkus.flyway.clean-at-start", "true"
      );
    }
  }

  /**
   * Test profile that disables CQRS for legacy mode testing.
   */
  public static class CQRSDisabledTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          // Disable CQRS for legacy testing
          "features.cqrs.enabled", "false",
          // Disable automatic test data initialization
          "app.init.data", "false",
          // Use test profile for DB
          "quarkus.datasource.db-kind", "postgresql",
          // Clean database before tests
          "quarkus.flyway.clean-at-start", "true"
      );
    }
  }
}