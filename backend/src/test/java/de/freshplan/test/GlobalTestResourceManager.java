package de.freshplan.test;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * Global test resource manager that ensures all tests use the same database. This prevents the
 * "Two-Container Problem" where migrations run on one database but tests run on another.
 *
 * <p>This manager is applied globally via META-INF/services configuration.
 *
 * <p>Note: ResourceScope is only available in Quarkus 3.x+ For older versions, this resource is
 * automatically shared when registered via services.
 */
public class GlobalTestResourceManager implements QuarkusTestResourceLifecycleManager {

  private static final Logger LOG = Logger.getLogger(GlobalTestResourceManager.class);

  @Override
  public Map<String, String> start() {
    LOG.info("=".repeat(80));
    LOG.info("GLOBAL TEST RESOURCE MANAGER STARTING");
    LOG.info("Ensuring single database container for all tests");
    LOG.info("=".repeat(80));

    // Determine DB connection details
    String ciProfile = System.getenv("QUARKUS_PROFILE");
    boolean isCI = "ci".equals(ciProfile) || "true".equals(System.getenv("CI"));

    String jdbcUrl;
    String username;
    String password;

    if (isCI) {
      LOG.info("CI environment detected - using GitHub Services PostgreSQL");
      jdbcUrl = "jdbc:postgresql://localhost:5432/freshplan";
      username = "freshplan";
      password = "freshplan";
    } else {
      LOG.info("Local environment - using existing PostgreSQL database");
      jdbcUrl = "jdbc:postgresql://localhost:5432/freshplan";
      username = "freshplan";
      password = "freshplan";
    }

    // Execute Flyway clean + migrate programmatically BEFORE Quarkus starts
    // This ensures fresh DB with all constraints/triggers for EVERY test run
    try {
      LOG.info("Executing programmatic Flyway clean + migrate...");
      org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
          .dataSource(jdbcUrl, username, password)
          .locations("classpath:db/migration", "classpath:db/dev-migration")
          .baselineOnMigrate(true)
          .outOfOrder(true)
          .cleanDisabled(false) // Override global cleanDisabled=true
          .load();

      flyway.clean();
      LOG.info("✅ Flyway clean completed");

      flyway.migrate();
      LOG.info("✅ Flyway migrate completed - all constraints/triggers created");
    } catch (Exception e) {
      LOG.error("❌ Failed to clean/migrate database", e);
      throw new RuntimeException("Failed to prepare test database", e);
    }

    // Configure Quarkus to use the same DB but NOT run Flyway again
    Map<String, String> config = new HashMap<>();

    config.put("quarkus.datasource.devservices.enabled", "false");
    config.put("quarkus.devservices.enabled", "false");
    config.put("quarkus.datasource.jdbc.url", jdbcUrl);
    config.put("quarkus.datasource.username", username);
    config.put("quarkus.datasource.password", password);

    // CRITICAL: Disable Flyway in Quarkus - we already did it above
    config.put("quarkus.flyway.migrate-at-start", "false");
    config.put("quarkus.flyway.clean-at-start", "false");
    config.put("quarkus.flyway.repair-at-start", "false");

    // Ensure DevServices are completely disabled
    config.put("%test.quarkus.datasource.devservices.enabled", "false");
    config.put("%ci.quarkus.datasource.devservices.enabled", "false");

    LOG.info("Configuration applied:");
    config.forEach((k, v) -> LOG.infof("  %s = %s", k, v));

    return config;
  }

  @Override
  public void stop() {
    LOG.info("Global test resource manager stopping");
  }

  @Override
  public void inject(TestInjector testInjector) {
    // Optional: inject resources into test classes if needed
  }
}
