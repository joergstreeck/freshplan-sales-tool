package de.freshplan.api;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * Integration test for PingResource.
 *
 * <p>This test uses Quarkus DevServices to automatically start: - PostgreSQL container - Keycloak
 * container (if enabled)
 *
 * <p>All tests from PingResourceTest are inherited and run against the full application stack.
 */
@QuarkusIntegrationTest
class PingIT extends PingResourceTest {
  // All tests inherited from PingResourceTest
  // They will run with real database and containers
}
