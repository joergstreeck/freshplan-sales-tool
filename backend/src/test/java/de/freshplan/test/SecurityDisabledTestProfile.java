package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test profile that disables security for integration tests. This allows tests to run without
 * authentication while maintaining security in production environments.
 */
public class SecurityDisabledTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
        // Disable OIDC completely for tests
        "quarkus.oidc.enabled", "false",

        // Allow all paths without authentication in tests
        "quarkus.http.auth.permission.permit-all.paths", "/*",
        "quarkus.http.auth.permission.permit-all.policy", "permit");
  }

  @Override
  public String getConfigProfile() {
    return "security-disabled";
  }
}
