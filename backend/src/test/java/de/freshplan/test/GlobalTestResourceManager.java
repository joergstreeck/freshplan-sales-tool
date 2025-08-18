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
    
    // Include test data migrations AND callbacks for SEED data
    config.put("quarkus.flyway.locations", 
               "classpath:db/migration,classpath:db/testdata,classpath:db/ci-migrations,classpath:db/callbacks");
    
    // Ensure DevServices are completely disabled
    config.put("%test.quarkus.datasource.devservices.enabled", "false");
    config.put("%ci.quarkus.datasource.devservices.enabled", "false");
    
    LOG.info("Configuration applied:");
    config.forEach((k, v) -> LOG.infof("  %s = %s", k, v));
    
    // In CI, also ensure SEED data programmatically since callbacks may not run
    // if all migrations are already up-to-date
    if (isCI) {
      ensureSeedDataProgrammatically();
    }
    
    return config;
  }
  
  private void ensureSeedDataProgrammatically() {
    LOG.info("Ensuring SEED data programmatically for CI...");
    
    // Wait a bit for migrations to complete
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    
    String jdbcUrl = "jdbc:postgresql://localhost:5432/freshplan";
    String user = "freshplan";
    String pass = "freshplan";
    
    try (var conn = java.sql.DriverManager.getConnection(jdbcUrl, user, pass);
         var stmt = conn.createStatement()) {
      
      // Check if SEED data exists
      var rs = stmt.executeQuery(
        "SELECT COUNT(*) FROM customers WHERE customer_number LIKE 'SEED-%'");
      rs.next();
      int seedCount = rs.getInt(1);
      
      if (seedCount == 20) {
        LOG.info("All 20 SEED customers already exist");
        return;
      }
      
      LOG.infof("Only %d SEED customers found, ensuring all 20...", seedCount);
      
      // Execute the same logic as the afterMigrate callback
      stmt.execute("""
        DO $$
        BEGIN
          DELETE FROM customers WHERE customer_number LIKE 'SEED-%';
          
          CREATE EXTENSION IF NOT EXISTS pgcrypto;
          
          INSERT INTO customers (
              id, customer_number, company_name, customer_type, status,
              expected_annual_volume, risk_score, is_test_data, is_deleted, 
              created_by, created_at
          ) VALUES 
              (gen_random_uuid(), 'SEED-001', '[SEED] Restaurant München', 'UNTERNEHMEN', 'AKTIV', 500000, 5, false, false, 'resource-mgr', NOW() - INTERVAL '2 years'),
              (gen_random_uuid(), 'SEED-002', '[SEED] Hotel Berlin', 'UNTERNEHMEN', 'AKTIV', 300000, 10, false, false, 'resource-mgr', NOW() - INTERVAL '18 months'),
              (gen_random_uuid(), 'SEED-003', '[SEED] Catering Frankfurt', 'UNTERNEHMEN', 'AKTIV', 200000, 15, false, false, 'resource-mgr', NOW() - INTERVAL '1 year'),
              (gen_random_uuid(), 'SEED-004', '[SEED] Kantine Hamburg', 'UNTERNEHMEN', 'AKTIV', 150000, 20, false, false, 'resource-mgr', NOW() - INTERVAL '6 months'),
              (gen_random_uuid(), 'SEED-005', '[SEED] Bio-Markt Dresden', 'UNTERNEHMEN', 'AKTIV', 100000, 25, false, false, 'resource-mgr', NOW() - INTERVAL '3 months'),
              (gen_random_uuid(), 'SEED-006', '[SEED] Bäckerei Stuttgart', 'UNTERNEHMEN', 'RISIKO', 50000, 70, false, false, 'resource-mgr', NOW() - INTERVAL '2 years'),
              (gen_random_uuid(), 'SEED-007', '[SEED] Metzgerei Köln', 'UNTERNEHMEN', 'RISIKO', 40000, 80, false, false, 'resource-mgr', NOW() - INTERVAL '1 year'),
              (gen_random_uuid(), 'SEED-008', '[SEED] Café Leipzig', 'UNTERNEHMEN', 'INAKTIV', 0, 90, false, false, 'resource-mgr', NOW() - INTERVAL '3 years'),
              (gen_random_uuid(), 'SEED-009', '[SEED] Bar Düsseldorf', 'UNTERNEHMEN', 'INAKTIV', 0, 95, false, false, 'resource-mgr', NOW() - INTERVAL '4 years'),
              (gen_random_uuid(), 'SEED-010', '[SEED] Club Essen', 'UNTERNEHMEN', 'ARCHIVIERT', 0, 100, false, false, 'resource-mgr', NOW() - INTERVAL '5 years'),
              (gen_random_uuid(), 'SEED-011', '[SEED] New Lead Restaurant', 'NEUKUNDE', 'LEAD', 75000, 30, false, false, 'resource-mgr', NOW()),
              (gen_random_uuid(), 'SEED-012', '[SEED] Qualified Lead Hotel', 'NEUKUNDE', 'LEAD', 100000, 35, false, false, 'resource-mgr', NOW() - INTERVAL '1 week'),
              (gen_random_uuid(), 'SEED-013', '[SEED] Angebot Catering', 'NEUKUNDE', 'PROSPECT', 125000, 40, false, false, 'resource-mgr', NOW() - INTERVAL '2 weeks'),
              (gen_random_uuid(), 'SEED-014', '[SEED] Negotiation Kantine', 'NEUKUNDE', 'PROSPECT', 150000, 45, false, false, 'resource-mgr', NOW() - INTERVAL '3 weeks'),
              (gen_random_uuid(), 'SEED-015', '[SEED] Won Deal Bäckerei', 'UNTERNEHMEN', 'AKTIV', 175000, 50, false, false, 'resource-mgr', NOW() - INTERVAL '1 month'),
              (gen_random_uuid(), 'SEED-016', '[SEED] Großkunde AG', 'UNTERNEHMEN', 'AKTIV', 1000000, 5, false, false, 'resource-mgr', NOW()),
              (gen_random_uuid(), 'SEED-017', '[SEED] Privatkunde Klein', 'PRIVAT', 'AKTIV', 5000, 55, false, false, 'resource-mgr', NOW()),
              (gen_random_uuid(), 'SEED-018', '[SEED] Verein e.V.', 'VEREIN', 'AKTIV', 50000, 60, false, false, 'resource-mgr', NOW()),
              (gen_random_uuid(), 'SEED-019', '[SEED] Institution GmbH', 'INSTITUTION', 'LEAD', 0, 65, false, false, 'resource-mgr', NOW()),
              (gen_random_uuid(), 'SEED-020', '[SEED] Sonstige Corp', 'SONSTIGE', 'RISIKO', 10000, 100, false, false, 'resource-mgr', NOW())
          ON CONFLICT (customer_number) DO UPDATE
            SET is_test_data = FALSE,
                company_name = EXCLUDED.company_name,
                customer_type = EXCLUDED.customer_type,
                status = EXCLUDED.status;
        END $$
        """);
      
      LOG.info("SEED data ensured programmatically");
      
    } catch (Exception e) {
      LOG.error("Failed to ensure SEED data programmatically", e);
    }
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