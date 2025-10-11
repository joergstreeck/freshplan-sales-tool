package de.freshplan.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator implementation for @SafeString annotation.
 *
 * <p>Detects and blocks common injection attack patterns.
 *
 * <p>Sprint 2.1.6 - Security Hardening Phase 2
 */
public class SafeStringValidator implements ConstraintValidator<SafeString, String> {

  private boolean allowEmpty;

  // Dangerous patterns to block (case-insensitive)
  private static final Pattern[] DANGEROUS_PATTERNS =
      new Pattern[] {
        // SQL Injection
        Pattern.compile(
            "('.+--)|(--.+)|(;.*(drop|delete|insert|update|create|alter|truncate))",
            Pattern.CASE_INSENSITIVE),
        Pattern.compile(
            "(union.+select)|(select.+from)|(insert.+into)|(delete.+from)",
            Pattern.CASE_INSENSITIVE),
        // XSS (Script tags)
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on(error|load|click|mouse|key)\\s*=", Pattern.CASE_INSENSITIVE),
        // Command Injection
        Pattern.compile("[|&;`$(){}\\[\\]<>]"),
        // Path Traversal
        Pattern.compile("(\\.\\./)|(\\.\\\\)")
        // LDAP Injection - REMOVED (too broad, blocks legitimate parentheses in notes)
        // This application does not use LDAP, so LDAP injection is NOT a relevant threat
        // If LDAP is added in future, use specific LDAP filter syntax pattern instead:
        // Pattern.compile("\\(\\s*[&|!].*\\)") // Matches LDAP filter syntax like (&(cn=*))
      };

  @Override
  public void initialize(SafeString constraintAnnotation) {
    this.allowEmpty = constraintAnnotation.allowEmpty();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Allow null/empty if configured
    if (value == null || value.trim().isEmpty()) {
      return allowEmpty;
    }

    // Check for dangerous patterns
    for (Pattern pattern : DANGEROUS_PATTERNS) {
      if (pattern.matcher(value).find()) {
        return false; // Dangerous pattern detected
      }
    }

    return true; // Safe string
  }
}
