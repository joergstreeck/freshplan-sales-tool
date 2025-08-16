package de.freshplan.test;

import java.util.UUID;

/**
 * Utility for generating unique test data to prevent duplicate key violations.
 * 
 * <p>Uses RUN_SUFFIX from CI environment to ensure uniqueness across parallel runs.
 * Falls back to nanoTime for local testing.
 */
public final class UniqueData {
  
  // Use GitHub run ID from CI or nanoTime for local tests
  private static final String RUN = 
      System.getProperty("RUN_SUFFIX", Long.toString(System.nanoTime()));
  
  private UniqueData() {
    // Utility class
  }
  
  /**
   * Generate unique customer number.
   * Format: KD-{RUN}-{UUID}
   */
  public static String customerNumber() {
    return "KD-" + RUN + "-" + UUID.randomUUID().toString().substring(0, 8);
  }
  
  /**
   * Generate unique permission code.
   * @param prefix Permission type prefix (e.g., "PERM", "ROLE")
   */
  public static String permissionCode(String prefix) {
    return prefix + "-" + RUN + "-" + UUID.randomUUID().toString().substring(0, 6);
  }
  
  /**
   * Generate unique email address for testing.
   * @param prefix User prefix (e.g., "test", "admin")
   */
  public static String email(String prefix) {
    return prefix + "-" + RUN + "-" + UUID.randomUUID().toString().substring(0, 6) + "@test.de";
  }
  
  /**
   * Generate unique username.
   * @param prefix Username prefix
   */
  public static String username(String prefix) {
    return prefix + "-" + RUN + "-" + UUID.randomUUID().toString().substring(0, 6);
  }
  
  /**
   * Generate unique company name.
   * @param base Base company name
   */
  public static String companyName(String base) {
    return base + " " + RUN + "-" + UUID.randomUUID().toString().substring(0, 4);
  }
}