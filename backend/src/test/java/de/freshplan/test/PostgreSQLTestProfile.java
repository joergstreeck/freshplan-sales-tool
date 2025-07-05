package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Collections;
import java.util.Map;

/**
 * Test Profile für PostgreSQL Integration Tests mit Testcontainers.
 * 
 * Aktiviert PostgreSQL via Quarkus DevServices (die intern Testcontainers nutzen).
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class PostgreSQLTestProfile implements QuarkusTestProfile {
    
    @Override
    public String getConfigProfile() {
        return "test-postgresql";
    }
    
    @Override
    public Map<String, String> getConfigOverrides() {
        // Return empty map since all config is in application-test-postgresql.properties
        return Collections.emptyMap();
    }
}