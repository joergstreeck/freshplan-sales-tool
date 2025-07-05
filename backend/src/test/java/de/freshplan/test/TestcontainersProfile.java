package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.HashMap;
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
        // Return null to not use any profile from application-{profile}.properties
        return null;
    }
    
    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> config = new HashMap<>();
        
        // Override datasource to use PostgreSQL instead of H2
        config.put("quarkus.datasource.db-kind", "postgresql");
        // Force Testcontainers to be used
        config.put("quarkus.datasource.devservices.enabled", "true");
        config.put("quarkus.datasource.devservices.image-name", "postgres:15-alpine");
        // Let Testcontainers manage the username/password
        // Enable Flyway for real migration tests
        config.put("quarkus.flyway.migrate-at-start", "true");
        config.put("quarkus.flyway.baseline-on-migrate", "true");
        // Clean database before migrations to avoid checksum issues
        config.put("quarkus.flyway.clean-at-start", "true");
        // Use validate mode with real PostgreSQL
        config.put("quarkus.hibernate-orm.database.generation", "validate");
        // Disable OIDC for tests
        config.put("quarkus.oidc.enabled", "false");
        config.put("quarkus.http.auth.proactive", "false");
        // Logging
        config.put("quarkus.log.level", "INFO");
        config.put("quarkus.log.category.\"de.freshplan\".level", "DEBUG");
        config.put("quarkus.log.category.\"org.flywaydb\".level", "DEBUG");
        
        return config;
    }
}