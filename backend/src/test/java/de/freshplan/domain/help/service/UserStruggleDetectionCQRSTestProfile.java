package de.freshplan.domain.help.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile to enable CQRS for UserStruggleDetectionService integration tests.
 *
 * <p>This profile overrides the feature flag to enable CQRS mode, ensuring that tests run against
 * the Command/Query separated implementation. Note: This service uses Event-Driven CQRS with CDI
 * Event Bus.
 *
 * @author Claude
 * @since 15.08.2025
 */
public class UserStruggleDetectionCQRSTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("features.cqrs.enabled", "true");
  }
}
