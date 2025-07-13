package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test profile that disables security for integration tests. This profile ensures that all security
 * checks are bypassed during test execution.
 *
 * <p>IMPORTANT: This profile should ONLY be used for testing purposes. Never use this configuration
 * in production!
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class NoSecurityTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
        // Disable all security checks
        "quarkus.security.enabled", "false",

        // Disable OIDC authentication
        "quarkus.oidc.enabled", "false",
        "%test.quarkus.oidc.enabled", "false",

        // Disable HTTP authentication
        "quarkus.http.auth.basic", "false",
        "%test.quarkus.http.auth.basic", "false",

        // Allow all paths without authentication
        "quarkus.http.auth.permission.permit-all.paths", "/*",
        "quarkus.http.auth.permission.permit-all.policy", "permit",
        "%test.quarkus.http.auth.permission.permit-all.paths", "/*",
        "%test.quarkus.http.auth.permission.permit-all.policy", "permit");
  }

  @Override
  public String getConfigProfile() {
    return "test-no-security";
  }
}
