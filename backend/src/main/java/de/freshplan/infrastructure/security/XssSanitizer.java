package de.freshplan.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * XSS Protection Service using OWASP HTML Sanitizer.
 *
 * <p>Sanitizes user input to prevent XSS attacks by removing or escaping HTML tags and scripts.
 *
 * <p>Sprint 2.1.6 - Security Enhancement
 */
@ApplicationScoped
public class XssSanitizer {

  // Strict policy: Allow NO HTML tags (plain text only)
  private static final PolicyFactory STRICT_POLICY = new HtmlPolicyBuilder().toFactory();

  // Lenient policy: Allow basic formatting (bold, italic, links) - for rich text fields
  private static final PolicyFactory LENIENT_POLICY =
      new HtmlPolicyBuilder()
          .allowElements("b", "i", "em", "strong", "a", "p", "br")
          .allowAttributes("href")
          .onElements("a")
          .requireRelNofollowOnLinks()
          .toFactory();

  /**
   * Sanitize input with STRICT policy (no HTML allowed).
   *
   * <p>Use for: Company names, email addresses, phone numbers, addresses
   *
   * @param input User input (may contain malicious HTML/scripts)
   * @return Sanitized text (HTML tags removed/escaped)
   */
  public String sanitizeStrict(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    return STRICT_POLICY.sanitize(input);
  }

  /**
   * Sanitize input with LENIENT policy (basic HTML allowed).
   *
   * <p>Use for: Notes, descriptions, comments (where basic formatting is acceptable)
   *
   * @param input User input (may contain malicious HTML/scripts)
   * @return Sanitized HTML (dangerous tags removed, safe tags preserved)
   */
  public String sanitizeLenient(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    return LENIENT_POLICY.sanitize(input);
  }

  /**
   * Batch sanitize array of strings (strict).
   *
   * @param inputs Array of user inputs
   * @return Array of sanitized strings
   */
  public String[] sanitizeAllStrict(String... inputs) {
    if (inputs == null) {
      return null;
    }
    String[] sanitized = new String[inputs.length];
    for (int i = 0; i < inputs.length; i++) {
      sanitized[i] = sanitizeStrict(inputs[i]);
    }
    return sanitized;
  }
}
