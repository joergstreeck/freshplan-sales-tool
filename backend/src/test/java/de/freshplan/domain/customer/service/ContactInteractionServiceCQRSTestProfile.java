package de.freshplan.domain.customer.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test profile to enable CQRS for ContactInteractionService integration tests. Enables the CQRS
 * feature flag for testing the new architecture.
 */
public class ContactInteractionServiceCQRSTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("features.cqrs.enabled", "true");
  }
}
