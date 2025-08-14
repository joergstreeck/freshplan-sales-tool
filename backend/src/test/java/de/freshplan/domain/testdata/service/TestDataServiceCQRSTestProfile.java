package de.freshplan.domain.testdata.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test profile for TestDataService CQRS integration tests.
 * Enables the CQRS feature flag for testing the new Command/Query split architecture.
 */
public class TestDataServiceCQRSTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
        "features.cqrs.enabled", "true"
    );
  }
}