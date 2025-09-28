package db.migration;

import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Creates unique index CONCURRENTLY for zero-downtime deployment. This migration runs outside of a
 * transaction to allow CONCURRENTLY.
 *
 * <p>Sprint 2.1.4: Lead Deduplication & Data Quality
 */
public class V248__CreateEmailUniqueIndexConcurrently extends BaseJavaMigration {

  @Override
  public boolean canExecuteInTransaction() {
    // Must be false to allow CREATE INDEX CONCURRENTLY
    return false;
  }

  @Override
  public void migrate(Context context) throws Exception {
    try (Statement statement = context.getConnection().createStatement()) {

      // Check if index already exists
      ResultSet rs =
          statement.executeQuery(
              """
                SELECT 1 FROM pg_indexes
                WHERE schemaname = 'public'
                AND indexname = 'uq_leads_email_canonical_v2'
                """);

      boolean indexExists = rs.next();
      rs.close();

      if (!indexExists) {
        // Simplified test detection - check application_name
        boolean isTestMode = isTestEnvironment(statement);

        if (isTestMode) {
          // In test mode: create index without CONCURRENTLY
          statement.execute(
              """
                        CREATE UNIQUE INDEX IF NOT EXISTS uq_leads_email_canonical_v2
                        ON leads(email_normalized)
                        WHERE email_normalized IS NOT NULL
                            AND is_canonical = true
                            AND status != 'DELETED'
                        """);
        } else {
          // In production: create index CONCURRENTLY
          statement.execute(
              """
                        CREATE UNIQUE INDEX CONCURRENTLY uq_leads_email_canonical_v2
                        ON leads(email_normalized)
                        WHERE email_normalized IS NOT NULL
                            AND is_canonical = true
                            AND status != 'DELETED'
                        """);
        }

        // Add comment for documentation
        statement.execute(
            """
                    COMMENT ON INDEX uq_leads_email_canonical_v2 IS
                    'Prevents duplicate canonical leads with same email (v2, created CONCURRENTLY for zero-downtime in production)';
                    """);
      }
    }
  }

  @Override
  public String getDescription() {
    return "Create unique email index CONCURRENTLY for zero-downtime deployment";
  }

  /**
   * Simplified test environment detection. Returns true if running in test context (Maven Surefire,
   * JUnit, etc.)
   */
  private boolean isTestEnvironment(Statement statement) {
    try {
      ResultSet rs = statement.executeQuery("SELECT current_setting('application_name', true)");
      if (rs.next()) {
        String appName = rs.getString(1);
        rs.close();
        if (appName != null && !appName.isEmpty()) {
          String lower = appName.toLowerCase();
          return lower.contains("test") || lower.contains("surefire") || lower.contains("junit");
        }
      }
      rs.close();
    } catch (Exception e) {
      // If we can't determine, assume production for safety
    }
    return false;
  }
}
