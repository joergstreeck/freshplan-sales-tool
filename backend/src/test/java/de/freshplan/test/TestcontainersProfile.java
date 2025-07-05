package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test Profile für Integration Tests mit Testcontainers.
 * 
 * Aktiviert PostgreSQL via Testcontainers statt H2 in-memory DB.
 * Verwendet das testcontainers-Profil aus application-testcontainers.properties.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class TestcontainersProfile implements QuarkusTestProfile {
    
    @Override
    public String getConfigProfile() {
        return "testcontainers";
    }
    
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            // Override datasource to use PostgreSQL
            "quarkus.datasource.db-kind", "postgresql",
            // Force Testcontainers to be used
            "quarkus.datasource.devservices.enabled", "true",
            "quarkus.datasource.devservices.image-name", "postgres:15-alpine",
            // Enable Flyway for real migration tests
            "quarkus.flyway.migrate-at-start", "true",
            "quarkus.flyway.repair-at-start", "true",
            // Use update mode temporarily until V11 migration is created
            "quarkus.hibernate-orm.database.generation", "update"
        );
    }
}