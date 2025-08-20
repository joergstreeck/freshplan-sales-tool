package de.freshplan.test.utils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for test data generation. Provides common helper methods for generating unique IDs,
 * random values, etc.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class TestDataUtils {

  private static final AtomicLong COUNTER = new AtomicLong();

  /**
   * Generates a unique ID for test data. Format: {shortTimestamp}-{counter}
   *
   * <p>This ensures uniqueness across: - Time (last 6 digits of timestamp) - Sequential counter
   * (modulo 10000)
   *
   * <p>Maximum length: 6 + 1 + 4 = 11 characters With "TEST-" prefix: 16 characters (fits in
   * varchar(20))
   *
   * @return a unique identifier string
   */
  public static String uniqueId() {
    // Use last 6 digits of timestamp + counter (max 4 digits)
    long timestamp = System.currentTimeMillis() % 1000000;
    long counter = COUNTER.incrementAndGet() % 10000;
    return timestamp + "-" + counter;
  }

  /**
   * Generates a unique test prefix. Format: TEST-{uniqueId}
   *
   * @return a unique test prefix
   */
  public static String testPrefix() {
    return "TEST-" + uniqueId();
  }

  /**
   * Generates a random integer within a range.
   *
   * @param min minimum value (inclusive)
   * @param max maximum value (exclusive)
   * @return random integer
   */
  public static int randomInt(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max);
  }

  /**
   * Generates a random boolean with 50% probability.
   *
   * @return random boolean
   */
  public static boolean randomBoolean() {
    return ThreadLocalRandom.current().nextBoolean();
  }

  /**
   * Generates a random enum value from an enum class.
   *
   * @param <T> the enum type
   * @param enumClass the enum class
   * @return random enum value
   */
  public static <T extends Enum<?>> T randomEnum(Class<T> enumClass) {
    T[] values = enumClass.getEnumConstants();
    return values[randomInt(0, values.length)];
  }
}
