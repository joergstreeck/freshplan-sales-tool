package de.freshplan.domain.customer.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile to enable CQRS for ContactEventCaptureService integration tests.
 * 
 * This profile overrides the feature flag to enable CQRS mode,
 * ensuring that tests run against the Command-only implementation.
 * Note: ContactEventCaptureService is a write-only service with only CommandService.
 * 
 * @author Claude
 * @since 15.08.2025
 */
public class ContactEventCaptureCQRSTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
        "features.cqrs.enabled", "true"
    );
  }
}