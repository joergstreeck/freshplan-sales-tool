package de.freshplan.test;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

/**
 * Database Growth Tracker - Zeigt welche Tests Daten hinterlassen
 *
 * <p>Dieser Test läuft zwischen anderen Tests und protokolliert Änderungen
 */
@QuarkusTest
@Tag("quarantine")
@TestTransaction
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseGrowthTrackerTest {

  @Inject CustomerRepository customerRepository;

  private static final Map<String, Long> checkpoints = new HashMap<>();
  private static final List<String> growthLog = new ArrayList<>();

  @Test
  @Order(10)
  void checkpoint_01_initial() {
    captureState("01_INITIAL");
  }

  @Test
  @Order(100)
  void checkpoint_02_afterBasicTests() {
    captureState("02_AFTER_BASIC_TESTS");
  }

  @Test
  @Order(500)
  void checkpoint_03_midway() {
    captureState("03_MIDWAY");
  }

  @Test
  @Order(1000)
  void checkpoint_04_afterCQRSTests() {
    captureState("04_AFTER_CQRS_TESTS");
  }

  @Test
  @Order(9999)
  void checkpoint_99_final() {
    captureState("99_FINAL");

    // Print summary
    System.out.println("\n" + "=".repeat(100));
    System.out.println("=== DATABASE GROWTH SUMMARY ===");
    System.out.println("=".repeat(100));

    Long initial = checkpoints.get("01_INITIAL");
    Long finalCount = checkpoints.get("99_FINAL");

    if (initial != null && finalCount != null) {
      long growth = finalCount - initial;
      System.out.printf(
          "TOTAL GROWTH: %d test customers added during test run (is_test_data=true only)%n",
          growth);

      if (growth > 0) {
        System.out.println("\n### TEST DATA GROWTH TIMELINE ###");
        growthLog.forEach(System.out::println);

        System.out.println("\n### PROBLEM AREAS (Tests not cleaning up) ###");
        checkpoints.forEach(
            (checkpoint, count) -> {
              if (initial != null && count > initial) {
                System.out.printf(
                    "%s: +%d test customers (is_test_data=true)%n", checkpoint, count - initial);
              }
            });

        System.out.println("\n⚠️  Tests should use @TestTransaction for automatic cleanup!");
      }
    }

    System.out.println("=".repeat(100) + "\n");
  }

  private void captureState(String checkpoint) {
    // IMPORTANT: Only count test data (is_test_data = true)
    // This prevents false positives from production seed data
    long count = customerRepository.count("isTestData", true);
    Long previousCount =
        checkpoints.isEmpty()
            ? null
            : checkpoints.values().stream().reduce((first, second) -> second).orElse(null);

    checkpoints.put(checkpoint, count);

    String message =
        String.format("[%s] Test Customers (is_test_data=true): %d", checkpoint, count);
    if (previousCount != null && count != previousCount) {
      long diff = count - previousCount;
      message += String.format(" (CHANGE: %+d)", diff);

      // Log significant changes
      if (Math.abs(diff) > 0) {
        growthLog.add(
            String.format(
                "%s: %d → %d (%+d test customers)", checkpoint, previousCount, count, diff));
      }
    }

    System.out.println(message);
  }
}
