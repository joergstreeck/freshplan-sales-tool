package de.freshplan.domain.customer.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile to enable CQRS for Customer integration tests. Sets features.cqrs.enabled=true for
 * testing CQRS implementation.
 *
 * @author FreshPlan Team
 * @since Phase 14 - Integration Tests
 */
public class CustomerCQRSTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("features.cqrs.enabled", "true");
  }
}
