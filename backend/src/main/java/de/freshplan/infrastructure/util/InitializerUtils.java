package de.freshplan.infrastructure.util;

/**
 * Utility class for data initializers. Provides common functionality for test data initialization.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class InitializerUtils {

  private InitializerUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Check if the application is running in test mode. Test data initializers should skip
   * initialization when running tests to avoid interfering with test data setup.
   *
   * @return true if running in test mode, false otherwise
   */
  public static boolean isTestMode() {
    String testProfile = System.getProperty("quarkus.test.profile");
    return "test".equals(testProfile) || Boolean.getBoolean("quarkus.test");
  }

  /**
   * Check if test data initialization should be performed. This is the inverse of isTestMode() for
   * readability.
   *
   * @return true if test data should be initialized, false if in test mode
   */
  public static boolean shouldInitializeTestData() {
    return !isTestMode();
  }
}
