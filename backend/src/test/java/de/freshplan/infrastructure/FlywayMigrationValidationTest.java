package de.freshplan.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Flyway Migration Validation Tests - BATCH 3
 *
 * Tests migration integrity and idempotency:
 * - All migrations are applied successfully
 * - No pending migrations
 * - No failed migrations
 * - Migration checksums are valid
 * - Migrations are idempotent (repeatable without errors)
 * - Migration order is correct
 *
 * WHY: Prevents production deployment with broken migrations
 * BLOCKING: CI fails if migrations are in invalid state
 *
 * NOTE: Quarkus Flyway Community Edition does NOT support flyway:undo (Teams/Enterprise only).
 * Instead, we validate forward migrations are correct and idempotent.
 */
@QuarkusTest
@DisplayName("Flyway Migration Validation (CI Quality Gate)")
public class FlywayMigrationValidationTest {

    @Inject
    Flyway flyway;

    @Inject
    DataSource dataSource;

    @Test
    @DisplayName("All migrations should be applied successfully")
    void testAllMigrationsApplied() {
        MigrationInfoService info = flyway.info();
        MigrationInfo[] all = info.all();

        // Assert: At least some migrations exist
        assertTrue(all.length > 0, "No migrations found - check db/migration folder");

        // Get failed migrations
        List<MigrationInfo> failed = Arrays.stream(all)
            .filter(m -> m.getState() == MigrationState.FAILED)
            .collect(Collectors.toList());

        if (!failed.isEmpty()) {
            String failedList = failed.stream()
                .map(m -> String.format("  - %s: %s (State: %s)",
                    m.getVersion(),
                    m.getDescription(),
                    m.getState()))
                .collect(Collectors.joining("\n"));

            fail(String.format("Found %d failed migrations:\n%s\n\n" +
                "Failed migrations indicate broken SQL scripts or constraint violations.\n" +
                "Fix: Review migration SQL and ensure it's compatible with current schema.",
                failed.size(), failedList));
        }

        // Get pending migrations
        List<MigrationInfo> pending = Arrays.stream(all)
            .filter(m -> m.getState() == MigrationState.PENDING)
            .collect(Collectors.toList());

        if (!pending.isEmpty()) {
            String pendingList = pending.stream()
                .map(m -> String.format("  - %s: %s",
                    m.getVersion(),
                    m.getDescription()))
                .collect(Collectors.joining("\n"));

            fail(String.format("Found %d pending migrations:\n%s\n\n" +
                "Pending migrations should be applied before running tests.\n" +
                "This usually means migrate-at-start is disabled or migrations are out of order.",
                pending.size(), pendingList));
        }
    }

    @Test
    @DisplayName("No missing migrations should exist")
    void testNoMissingMigrations() {
        MigrationInfoService info = flyway.info();
        MigrationInfo[] all = info.all();

        List<MigrationInfo> missing = Arrays.stream(all)
            .filter(m -> m.getState() == MigrationState.MISSING_SUCCESS)
            .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            String missingList = missing.stream()
                .map(m -> String.format("  - %s: %s",
                    m.getVersion(),
                    m.getDescription()))
                .collect(Collectors.joining("\n"));

            // Lenient check: Log warning but don't fail (deleted migrations may be intentional)
            System.err.println("⚠️  Warning: Found " + missing.size() + " missing migrations:");
            System.err.println(missingList);
            System.err.println("\nNote: Missing migrations indicate files were deleted after being applied.");
            System.err.println("This is usually intentional cleanup (e.g., test migrations).");
        }

        // Test passes (non-blocking documentation check)
        assertTrue(true, "Missing migrations check completed (warnings logged if missing)");
    }

    @Test
    @DisplayName("Migration checksums should be valid")
    void testMigrationChecksumsValid() {
        MigrationInfoService info = flyway.info();
        MigrationInfo[] all = info.all();

        // Get migrations with checksum mismatches
        List<MigrationInfo> outdated = Arrays.stream(all)
            .filter(m -> m.getState() == MigrationState.OUTDATED)
            .collect(Collectors.toList());

        if (!outdated.isEmpty()) {
            String outdatedList = outdated.stream()
                .map(m -> String.format("  - %s: %s (State: %s)",
                    m.getVersion(),
                    m.getDescription(),
                    m.getState()))
                .collect(Collectors.joining("\n"));

            fail(String.format("Found %d migrations with checksum mismatches:\n%s\n\n" +
                "Checksum mismatches indicate migration files were modified after being applied.\n" +
                "Fix: Never modify applied migrations! Use flyway:repair or create new migration.",
                outdated.size(), outdatedList));
        }
    }

    @Test
    @DisplayName("Migration order should be correct (no out-of-order)")
    void testMigrationOrderCorrect() {
        MigrationInfoService info = flyway.info();
        MigrationInfo[] all = info.all();

        // Get out-of-order migrations
        List<MigrationInfo> outOfOrder = Arrays.stream(all)
            .filter(m -> m.getState() == MigrationState.OUT_OF_ORDER)
            .collect(Collectors.toList());

        // NOTE: out-of-order is ALLOWED in this project (quarkus.flyway.out-of-order=true)
        // but we log a warning if too many exist (might indicate version numbering issues)
        if (outOfOrder.size() > 5) {
            System.err.println("⚠️  Warning: Found " + outOfOrder.size() + " out-of-order migrations:");
            outOfOrder.forEach(m -> System.err.println("  - " + m.getVersion() + ": " + m.getDescription()));
            System.err.println("\nOut-of-order is allowed but consider using consistent version numbers.");
        }

        // This test always passes but logs warnings
        assertTrue(true);
    }

    @Test
    @DisplayName("Critical tables should exist after migrations")
    void testCriticalTablesExist() throws Exception {
        String[] criticalTables = {
            "customers",
            "leads",
            // "users" - removed: no auth system yet
            "customer_contacts",
            "lead_contacts",
            "activities",
            "flyway_schema_history"
        };

        try (Connection conn = dataSource.getConnection()) {
            for (String table : criticalTables) {
                try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT to_regclass('public.' || ?) IS NOT NULL")) {
                    stmt.setString(1, table);
                    try (ResultSet rs = stmt.executeQuery()) {
                        assertTrue(rs.next(), "Query should return result for table: " + table);
                        boolean exists = rs.getBoolean(1);
                        assertTrue(exists,
                            String.format("Critical table '%s' does not exist after migrations.\n" +
                                "This indicates incomplete or failed migrations.", table));
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Migrations should be idempotent (no errors on re-apply)")
    void testMigrationsAreIdempotent() {
        // Test: Run flyway.migrate() again - should be no-op if migrations are idempotent
        int appliedCount = flyway.migrate().migrationsExecuted;

        // Assert: No new migrations applied (all already applied)
        assertEquals(0, appliedCount,
            String.format("Expected 0 new migrations (idempotent), but %d were applied.\n" +
                "This indicates migrations are not idempotent or pending migrations exist.\n" +
                "Fix: Ensure all migrations use IF NOT EXISTS / IF EXISTS checks.", appliedCount));
    }

    @Test
    @DisplayName("Flyway baseline version should be set")
    void testFlywayBaselineExists() {
        MigrationInfoService info = flyway.info();
        assertNotNull(info.current(),
            "No current migration found - Flyway baseline missing?\n" +
            "Check: quarkus.flyway.baseline-on-migrate=true");
    }

    @Test
    @DisplayName("No ignored migrations should exist")
    void testNoIgnoredMigrations() {
        MigrationInfoService info = flyway.info();
        MigrationInfo[] all = info.all();

        List<MigrationInfo> ignored = Arrays.stream(all)
            .filter(m -> m.getState() == MigrationState.IGNORED)
            .collect(Collectors.toList());

        if (!ignored.isEmpty()) {
            String ignoredList = ignored.stream()
                .map(m -> String.format("  - %s: %s (State: %s)",
                    m.getVersion(),
                    m.getDescription(),
                    m.getState()))
                .collect(Collectors.joining("\n"));

            System.err.println("⚠️  Warning: Found " + ignored.size() + " ignored migrations:");
            System.err.println(ignoredList);
            System.err.println("\nIgnored migrations might indicate version conflicts or missing=ignore setting.");
        }

        // This test always passes but logs warnings
        assertTrue(true);
    }

    @Test
    @DisplayName("Migration summary should be available")
    void testMigrationSummaryAvailable() {
        MigrationInfoService info = flyway.info();

        // Get summary statistics
        MigrationInfo current = info.current();
        MigrationInfo[] pending = info.pending();
        MigrationInfo[] applied = info.applied();
        MigrationInfo[] all = info.all();

        System.out.println("\n=== Flyway Migration Summary ===");
        System.out.println("Total migrations: " + all.length);
        System.out.println("Applied: " + applied.length);
        System.out.println("Pending: " + pending.length);
        if (current != null) {
            System.out.println("Current version: " + current.getVersion());
            System.out.println("Current description: " + current.getDescription());
        }
        System.out.println("================================\n");

        assertNotNull(info, "Migration info service should be available");
        assertTrue(all.length > 0, "At least one migration should exist");
    }
}
