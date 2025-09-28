package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.Statement;

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

            // First, check and drop the non-concurrent index if it exists
            // (created by V247 in staging/dev environments)
            statement.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM pg_indexes
                        WHERE schemaname = 'public'
                        AND indexname = 'uq_leads_email_canonical'
                    ) THEN
                        DROP INDEX uq_leads_email_canonical;
                    END IF;
                END$$;
                """);

            // Create the index CONCURRENTLY for zero-downtime
            // This allows production traffic to continue uninterrupted
            // NOTE: Single-tenant system, no tenant_id yet
            statement.execute("""
                CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_leads_email_canonical
                ON leads(email_normalized)
                WHERE email_normalized IS NOT NULL
                    AND is_canonical = true
                    AND status != 'DELETED';
                """);

            // Add comment for documentation
            statement.execute("""
                COMMENT ON INDEX uq_leads_email_canonical IS
                'Prevents duplicate canonical leads with same email (created CONCURRENTLY for zero-downtime)';
                """);
        }
    }

    @Override
    public String getDescription() {
        return "Create unique email index CONCURRENTLY for zero-downtime deployment";
    }
}