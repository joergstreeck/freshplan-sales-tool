package de.freshplan.test;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import java.util.HashMap;
import org.jboss.logging.Logger;

/**
 * Global test resource manager that ensures all tests use the same database.
 * This prevents the "Two-Container Problem" where migrations run on one database
 * but tests run on another.
 * 
 * This manager is applied globally via META-INF/services configuration.
 * 
 * Note: ResourceScope is only available in Quarkus 3.x+
 * For older versions, this resource is automatically shared when registered via services.
 */
public class GlobalTestResourceManager implements QuarkusTestResourceLifecycleManager {
  
  private static final Logger LOG = Logger.getLogger(GlobalTestResourceManager.class);
  
  @Override
  public Map<String, String> start() {
    LOG.info("=".repeat(80));
    LOG.info("GLOBAL TEST RESOURCE MANAGER STARTING");
    LOG.info("Ensuring single database container for all tests");
    LOG.info("=".repeat(80));
    
    Map<String, String> config = new HashMap<>();
    
    // Detect if we're in CI
    String ciProfile = System.getenv("QUARKUS_PROFILE");
    boolean isCI = "ci".equals(ciProfile) || "true".equals(System.getenv("CI"));
    
    if (isCI) {
      LOG.info("CI environment detected - using GitHub Services PostgreSQL");
      // In CI, use the GitHub Services PostgreSQL
      config.put("quarkus.datasource.devservices.enabled", "false");
      config.put("quarkus.devservices.enabled", "false");
      config.put("quarkus.datasource.jdbc.url", "jdbc:postgresql://localhost:5432/freshplan");
      config.put("quarkus.datasource.username", "freshplan");
      config.put("quarkus.datasource.password", "freshplan");
    } else {
      LOG.info("Local environment - allowing DevServices if not already configured");
      // Local development can use DevServices or manual config
    }
    
    // Ensure Flyway runs migrations ONCE at start
    config.put("quarkus.flyway.migrate-at-start", "true");
    config.put("quarkus.flyway.clean-at-start", "false");
    config.put("quarkus.flyway.repair-at-start", "false");
    config.put("quarkus.flyway.out-of-order", "true");
    
    // Include test data migrations
    config.put("quarkus.flyway.locations", 
               "classpath:db/migration,classpath:db/testdata,classpath:db/ci-migrations");
    
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