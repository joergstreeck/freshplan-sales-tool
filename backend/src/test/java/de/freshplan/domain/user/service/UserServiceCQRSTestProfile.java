package de.freshplan.domain.user.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test profile to enable CQRS for integration tests. Enables the CQRS feature flag for testing the
 * new architecture.
 */
public class UserServiceCQRSTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("features.cqrs.enabled", "true");
  }
}
