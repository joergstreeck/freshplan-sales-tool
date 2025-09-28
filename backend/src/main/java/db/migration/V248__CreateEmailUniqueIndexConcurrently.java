package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Creates unique index CONCURRENTLY for zero-downtime deployment.
 * This migration runs outside of a transaction to allow CONCURRENTLY.
 *
 * Sprint 2.1.4: Lead Deduplication & Data Quality
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
            ResultSet rs = statement.executeQuery("""
                SELECT 1 FROM pg_indexes
                WHERE schemaname = 'public'
                AND indexname = 'uq_leads_email_canonical_v2'
                """);

            boolean indexExists = rs.next();
            rs.close();

            if (!indexExists) {
                // Create index CONCURRENTLY (only works outside transaction)
                statement.execute("""
                    CREATE UNIQUE INDEX CONCURRENTLY uq_leads_email_canonical_v2
                    ON leads(email_normalized)
                    WHERE email_normalized IS NOT NULL
                        AND is_canonical = true
                        AND status != 'DELETED'
                    """);

                // Add comment for documentation
                statement.execute("""
                    COMMENT ON INDEX uq_leads_email_canonical_v2 IS
                    'Prevents duplicate canonical leads with same email (v2, created CONCURRENTLY for zero-downtime)';
                    """);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Create unique email index CONCURRENTLY for zero-downtime deployment";
    }
}