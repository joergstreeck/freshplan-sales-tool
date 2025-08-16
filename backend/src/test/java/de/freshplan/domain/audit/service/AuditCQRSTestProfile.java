package de.freshplan.domain.audit.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile to enable CQRS for AuditService integration tests.
 *
 * <p>This profile overrides the feature flag to enable CQRS mode, ensuring that tests run against
 * the Command/Query separated implementation.
 *
 * @author Claude
 * @since 15.08.2025
 */
public class AuditCQRSTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of("features.cqrs.enabled", "true");
  }
}
