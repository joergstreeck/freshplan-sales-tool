package de.freshplan.shared.util;

/**
 * Utility class for HTML-related operations.
 *
 * <p>Provides methods for escaping HTML content to prevent XSS attacks.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
public final class HtmlUtils {

  private HtmlUtils() {
    // Utility class
  }

  /**
   * Escapes HTML special characters to prevent XSS attacks.
   *
   * @param input the string to escape
   * @return the escaped string, or empty string if input is null
   */
  public static String escapeHtml(String input) {
    if (input == null) {
      return "";
    }

    return input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")
        .replace("/", "&#x2F;");
  }

  /**
   * Escapes HTML and handles null with custom default.
   *
   * @param input the string to escape
   * @param defaultValue the default value if input is null
   * @return the escaped string or default value
   */
  public static String escapeHtml(String input, String defaultValue) {
    if (input == null || input.trim().isEmpty()) {
      return escapeHtml(defaultValue);
    }
    return escapeHtml(input);
  }
}
