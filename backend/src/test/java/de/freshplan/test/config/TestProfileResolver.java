package de.freshplan.test.config;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Profile Resolver für CI/Local Umgebungen.
 *
 * Verhindert Testcontainers in CI und nutzt GitHub Services PostgreSQL.
 */
public class TestProfileResolver {

    /**
     * Detektiert ob wir in CI laufen.
     */
    public static boolean isCI() {
        return "true".equals(System.getenv("CI")) ||
               "ci".equals(System.getProperty("quarkus.profile"));
    }

    /**
     * CI Test Profile - nutzt GitHub Services PostgreSQL.
     */
    public static class CITestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            Map<String, String> config = new HashMap<>();

            // Explicitly disable ALL DevServices and Testcontainers
            config.put("quarkus.devservices.enabled", "false");
            config.put("quarkus.datasource.devservices.enabled", "false");

            // Use GitHub Services PostgreSQL
            config.put("quarkus.datasource.jdbc.url", "jdbc:postgresql://localhost:5432/freshplan");
            config.put("quarkus.datasource.username", "freshplan");
            config.put("quarkus.datasource.password", "freshplan");

            // Connection pool for CI
            config.put("quarkus.datasource.jdbc.max-size", "3");
            config.put("quarkus.datasource.jdbc.min-size", "1");

            // Flyway settings
            config.put("quarkus.flyway.migrate-at-start", "true");
            config.put("quarkus.flyway.clean-at-start", "true");
            config.put("quarkus.flyway.clean-disabled", "false");

            // Disable event listeners in CI
            config.put("freshplan.modules.leads.events.enabled", "false");
            config.put("freshplan.modules.cross.events.enabled", "false");

            return config;
        }

        @Override
        public String getConfigProfile() {
            return "ci";
        }
    }

    /**
     * Local Test Profile - kann Testcontainers nutzen.
     */
    public static class LocalTestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            Map<String, String> config = new HashMap<>();

            // Local kann Testcontainers nutzen wenn verfügbar
            config.put("quarkus.datasource.devservices.enabled", "true");

            // Flyway settings
            config.put("quarkus.flyway.migrate-at-start", "true");
            config.put("quarkus.flyway.clean-at-start", "true");
            config.put("quarkus.flyway.clean-disabled", "false");

            return config;
        }

        @Override
        public String getConfigProfile() {
            return "test";
        }
    }
}