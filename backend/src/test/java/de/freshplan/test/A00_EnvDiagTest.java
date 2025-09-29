package de.freshplan.test;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * A00 Smart Environment Diagnostics - sammelt ALLE Abweichungen und erklärt Root-Causes. Statt beim
 * ersten Fehler zu stoppen, sammelt A00 komplette Problemübersicht.
 *
 * <p>WICHTIG: Dieser Test MUSS als ERSTER laufen (daher A00 Präfix)!
 */
@QuarkusTest
@Tag("core")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class A00_EnvDiagTest {

  @Inject AgroalDataSource dataSource;

  private final List<String> problems = new ArrayList<>();

  @Test
  @Order(1)
  void verifyEnvironment() throws Exception {
    var config =
        (io.smallrye.config.SmallRyeConfig)
            org.eclipse.microprofile.config.ConfigProvider.getConfig();

    System.out.println("=== A00 Smart Environment Diagnostics ===");

    // 1) CONFIG + QUELLE - zeigt wo Werte herkommen
    checkFlywayLocations(config);
    checkDevServices(config);
    checkDatabaseGeneration(config);

    // 2) DB FINGERPRINT + STARTZUSTAND
    logEffectiveJdbcUrl(); // Log the actual JDBC URL first
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {

      logDatabaseFingerprint(statement);
      checkCleanStart(statement);
      logNonEmptyTables(statement);
      logFlywayHistory(statement);
      logRunIdPrefixes(statement);
    }

    // 3) CI SUMMARY (falls GitHub Actions)
    writeGitHubSummaryIfPresent();

    // 4) FAIL MIT SAMMELLISTE + LÖSUNGSVORSCHLÄGE
    if (!problems.isEmpty()) {
      String report = renderProblemsReport();
      System.out.println("\n" + report);
      org.junit.jupiter.api.Assertions.fail(
          "A00 Environment Check failed. See diagnostic report above.");
    }

    System.out.println("✅ A00 Environment Check: Alle kritischen Validierungen bestanden");
  }

  private void checkFlywayLocations(io.smallrye.config.SmallRyeConfig config) {
    var flyLoc = config.getConfigValue("quarkus.flyway.locations");
    System.out.printf(
        "DIAG[CFG] flyway.locations=%s (source=%s)%n",
        flyLoc.getValue(), flyLoc.getConfigSourceName());

    if (!"classpath:db/migration".equals(flyLoc.getValue())) {
      problems.add(
          "DIAG[CFG-001] flyway.locations = "
              + flyLoc.getValue()
              + " (source="
              + flyLoc.getConfigSourceName()
              + ") – erwartet classpath:db/migration.\n"
              + "→ Entferne CLI-Override in CI oder setze Profil korrekt (-Pcore-tests).");
    }
  }

  private void checkDevServices(io.smallrye.config.SmallRyeConfig config) {
    var devServices = config.getConfigValue("quarkus.datasource.devservices.enabled");
    System.out.printf(
        "DIAG[CFG] devservices.enabled=%s (source=%s)%n",
        devServices.getValue(), devServices.getConfigSourceName());

    if ("true".equalsIgnoreCase(devServices.getValue())) {
      problems.add(
          "DIAG[DEV-001] DevServices aktiv in CI.\n"
              + "→ quarkus.datasource.devservices.enabled=false setzen.");
    }
  }

  private void checkDatabaseGeneration(io.smallrye.config.SmallRyeConfig config) {
    var dbGen = config.getConfigValue("quarkus.hibernate-orm.database.generation");
    System.out.printf(
        "DIAG[CFG] hibernate-orm.database.generation=%s (source=%s)%n",
        dbGen.getValue(), dbGen.getConfigSourceName());
  }

  private void logEffectiveJdbcUrl() {
    try {
      // Log the actual JDBC URL being used
      String jdbcUrl = dataSource.getConfiguration().connectionPoolConfiguration()
          .connectionFactoryConfiguration().jdbcUrl();
      System.out.println("DIAG[DB] Effective JDBC URL: " + jdbcUrl);

      // Verify it's the expected database
      if (!jdbcUrl.contains("/freshplan")) {
        problems.add("DIAG[DB-URL] JDBC URL does not contain /freshplan: " + jdbcUrl +
                    "\n    FIX: Check application-ci.properties and ensure DevServices is disabled");
      }
    } catch (Exception e) {
      System.out.println("DIAG[DB] Could not determine JDBC URL: " + e.getMessage());
    }
  }

  private void logDatabaseFingerprint(Statement statement) throws SQLException {
    System.out.println("\n=== DATABASE FINGERPRINT ===");

    // Database Metadaten
    try (var rs =
        statement.executeQuery(
            "SELECT current_database(), current_schema(), current_user, version()")) {
      if (rs.next()) {
        System.out.printf(
            "Database: %s | Schema: %s | User: %s%n",
            rs.getString(1), rs.getString(2), rs.getString(3));
        System.out.printf("PostgreSQL: %s%n", rs.getString(4).split(" ")[0]);
      }
    }
  }

  private void checkCleanStart(Statement statement) throws SQLException {
    // Check if customers table exists first
    try (var rs =
        statement.executeQuery(
            "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = 'public' AND table_name = 'customers'")) {
      if (rs.next() && rs.getInt(1) == 0) {
        System.out.printf(
            "DIAG[DB] customers table does not exist yet (OK for initial migration)%n");
        return;
      }
    }

    // Table exists, check count
    long customerCount = scalarLong(statement, "SELECT COUNT(*) FROM customers");
    System.out.printf("DIAG[DB] customers at start: %d%n", customerCount);

    // Only check for clean start in CI environment
    boolean isCI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;

    // Database should be empty at start in CI (no seed data)
    if (isCI && customerCount != 0) {
      problems.add(
          "DIAG[DB-001] Startzustand nicht leer: customers="
              + customerCount
              + ".\n"
              + "→ Schema-Reset im CI fehlt / Rollback deaktiviert / Test ohne Builder.");
    } else if (!isCI && customerCount > 0) {
      System.out.printf(
          "DIAG[DB] Local environment with existing data (OK): %d customers%n", customerCount);
    }
  }

  private void logNonEmptyTables(Statement statement) throws SQLException {
    System.out.println("\n=== NON-EMPTY TABLES (Top 10) ===");

    String[] tables = {"customers", "customer_contacts", "opportunities", "app_user"};
    for (String table : tables) {
      try {
        // Check if table exists first
        try (var rs =
            statement.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_schema = 'public' AND table_name = '"
                    + table
                    + "'")) {
          if (rs.next() && rs.getInt(1) == 0) {
            System.out.printf("? %s: not created yet%n", table);
            continue;
          }
        }

        long count = scalarLong(statement, "SELECT COUNT(*) FROM " + table);
        if (count > 0) {
          System.out.printf("⚠️  %s: %d rows%n", table, count);
        }
      } catch (SQLException e) {
        System.out.printf("? %s: not accessible (%s)%n", table, e.getMessage());
      }
    }
  }

  private void logFlywayHistory(Statement statement) throws SQLException {
    System.out.println("\n=== FLYWAY HISTORY (Top 5) ===");

    try {
      // Check if flyway table exists
      try (var rs =
          statement.executeQuery(
              "SELECT COUNT(*) FROM information_schema.tables "
                  + "WHERE table_schema = 'public' AND table_name = 'flyway_schema_history'")) {
        if (rs.next() && rs.getInt(1) == 0) {
          System.out.println("Flyway history not available yet");
          return;
        }
      }

      try (var rs =
          statement.executeQuery(
              "SELECT version, description, success FROM flyway_schema_history "
                  + "ORDER BY installed_rank DESC LIMIT 5")) {

        while (rs.next()) {
          String status = rs.getBoolean(3) ? "✅" : "❌";
          System.out.printf("%s V%s: %s%n", status, rs.getString(1), rs.getString(2));
        }
      }
    } catch (SQLException e) {
      System.out.println("Flyway history not available: " + e.getMessage());
    }
  }

  private void logRunIdPrefixes(Statement statement) throws SQLException {
    System.out.println("\n=== RUN-ID CORRELATION ===");

    try {
      // Check if customers table exists
      try (var rs =
          statement.executeQuery(
              "SELECT COUNT(*) FROM information_schema.tables "
                  + "WHERE table_schema = 'public' AND table_name = 'customers'")) {
        if (rs.next() && rs.getInt(1) == 0) {
          System.out.println("Customers table not created yet");
          return;
        }
      }

      try (var rs =
          statement.executeQuery(
              "SELECT DISTINCT SUBSTRING(customer_number FROM 'KD-TEST-([^-]+)') as run_id "
                  + "FROM customers WHERE customer_number LIKE 'KD-TEST-%'")) {

        List<String> runIds = new ArrayList<>();
        while (rs.next()) {
          String runId = rs.getString(1);
          if (runId != null) runIds.add(runId);
        }

        if (!runIds.isEmpty()) {
          System.out.printf("Found test data from runs: %s%n", String.join(", ", runIds));

          // Only check for test data in CI environment
          boolean isCI = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null;

          if (isCI) {
            problems.add(
                "DIAG[DATA-001] Test data remains from previous runs: "
                    + String.join(", ", runIds)
                    + "\n→ Schema-Reset failed or Builder missing isTestData=true");
          } else {
            System.out.println("Local environment: Test data present (OK)");
          }
        } else {
          System.out.println("No test data remains found");
        }
      }
    } catch (SQLException e) {
      System.out.println("Run-ID correlation not available: " + e.getMessage());
    }
  }

  private void writeGitHubSummaryIfPresent() {
    String githubSummary = System.getenv("GITHUB_STEP_SUMMARY");
    if (githubSummary != null && !problems.isEmpty()) {
      try (var writer = new java.io.FileWriter(githubSummary, true)) {
        writer.write("\n## 🚨 A00 Environment Diagnostics\n\n");
        for (String problem : problems) {
          writer.write("- " + problem.replace("\n→", "\n  - **Fix:** ") + "\n\n");
        }
      } catch (Exception e) {
        System.out.println("Could not write GitHub summary: " + e.getMessage());
      }
    }
  }

  private String renderProblemsReport() {
    StringBuilder report = new StringBuilder();
    report.append("\n🚨 A00 ENVIRONMENT PROBLEMS FOUND:\n");
    report.append("=".repeat(50)).append("\n");

    for (int i = 0; i < problems.size(); i++) {
      report.append(String.format("%d) %s%n%n", i + 1, problems.get(i)));
    }

    report.append("💡 FIX ALL ISSUES ABOVE BEFORE PROCEEDING WITH TESTS\n");
    return report.toString();
  }

  private long scalarLong(Statement statement, String sql) throws SQLException {
    try (var rs = statement.executeQuery(sql)) {
      return rs.next() ? rs.getLong(1) : 0;
    }
  }
}
