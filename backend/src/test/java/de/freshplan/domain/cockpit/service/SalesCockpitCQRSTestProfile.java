package de.freshplan.domain.cockpit.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile to enable CQRS for SalesCockpitService integration tests.
 *
 * <p>This profile overrides the feature flag to enable CQRS mode, ensuring that tests run against
 * the Query-only implementation. Note: SalesCockpit is a read-only service with only QueryService.
 *
 * @author Claude
 * @since 15.08.2025
 */
public class SalesCockpitCQRSTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("features.cqrs.enabled", "true");
  }
}
