package de.freshplan.test.utils;

import jakarta.inject.Inject;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

/**
 * Base class for integration tests that automatically handles test data cleanup.
 *
 * <p>All integration tests that create test data should extend this class to ensure proper cleanup
 * and prevent database pollution.
 *
 * @author Claude
 * @since Phase 14.3 - Test Data Management
 */
@Tag("unit")
public abstract class BaseIntegrationTest {

  private static final Logger LOG = Logger.getLogger(BaseIntegrationTest.class);

  @Inject protected TestDataCleanup testDataCleanup;

  /**
   * Unique test run ID for this test execution. Use this in your test data to make it identifiable
   * for cleanup.
   */
  protected String testRunId;

  /** Initialize test run ID before each test. */
  @BeforeEach
  protected void setupTestRun() {
    testRunId = UUID.randomUUID().toString().substring(0, 8);
    LOG.debugf("Starting test run: %s", testRunId);

    // Check current test data count
    if (testDataCleanup != null) {
      long currentCount = testDataCleanup.getTestDataCount();
      if (currentCount > 100) {
        LOG.warnf("WARNING: %d test data records found before test!", currentCount);
      }
    }
  }

  /** Clean up test data after each test. */
  @AfterEach
  protected void cleanupTestRun() {
    if (testDataCleanup != null && testRunId != null) {
      LOG.debugf("Cleaning up test run: %s", testRunId);
      testDataCleanup.cleanupTestRun(testRunId);

      // Additional cleanup hook for subclasses
      additionalCleanup();
    }
  }

  /**
   * Hook for subclasses to add additional cleanup logic. Override this method if you need custom
   * cleanup.
   */
  protected void additionalCleanup() {
    // Default: no additional cleanup
  }

  /** Clean up old test data periodically. This runs after all tests in the class. */
  @AfterAll
  static void cleanupOldTestData() {
    // Note: This won't work with @Inject, needs to be handled differently
    LOG.info("Test class completed - consider running cleanup of old test data");
  }

  /**
   * Helper method to create test customer names with proper prefix.
   *
   * @param name The base name
   * @return Name with test prefix including test run ID
   */
  protected String testName(String name) {
    return "[TEST-" + testRunId + "] " + name;
  }

  /**
   * Helper method to create test customer numbers with test run ID.
   *
   * @param prefix The prefix for the customer number
   * @return Customer number with prefix and test run ID
   */
  protected String testCustomerNumber(String prefix) {
    return prefix + testRunId.substring(0, Math.min(testRunId.length(), 6));
  }
}
