package de.freshplan.domain.opportunity.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile to enable CQRS for integration tests
 */
public class OpportunityCQRSTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
        "features.cqrs.enabled", "true"
    );
  }
}