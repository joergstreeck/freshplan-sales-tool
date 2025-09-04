package de.freshplan.testsupport;

/**
 * Utility class for generating unique test data identifiers that are fork-safe. Prevents duplicate
 * key violations when tests run in parallel forks.
 */
public final class UniqueData {
  private UniqueData() {}

  // surefire.forkNumber ist der Standard; Fallback auf fork.number oder "1"
  public static final String FORK =
      System.getProperty("surefire.forkNumber", System.getProperty("fork.number", "1"));

  /**
   * Generates a fork-safe customer number. Format: PREFIX-XXX-F{fork} Example: CUST-001-F2
   *
   * @param prefix the prefix for the number (e.g., "CUST")
   * @param index the sequential index
   * @return fork-safe identifier
   */
  public static String customerNumber(String prefix, int index) {
    return String.format("%s-%03d-F%s", prefix, index, FORK);
  }

  /**
   * Generates a fork-safe permission code. Format: code-F{fork} Example: customers:read-F2
   *
   * @param code the base permission code
   * @return fork-safe permission code
   */
  public static String permissionCode(String code) {
    return String.format("%s-F%s", code, FORK);
  }

  // Optionales Debug/Monitoring
  static {
    if ("true".equals(System.getProperty("test.debug", "false"))) {
      try {
        long pid = ProcessHandle.current().pid();
        System.out.printf("[UniqueData] FORK=%s, PID=%d%n", FORK, pid);
      } catch (NoClassDefFoundError e) {
        // Java 8 fallback
        System.out.printf("[UniqueData] FORK=%s%n", FORK);
      }
    }
  }
}
