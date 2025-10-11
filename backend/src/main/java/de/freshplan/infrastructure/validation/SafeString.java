package de.freshplan.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a string is safe from injection attacks.
 *
 * <p>Blocks:
 *
 * <ul>
 *   <li>SQL Injection patterns: ', --, ;, DROP, UNION, etc.
 *   <li>XSS patterns: &lt;script&gt;, javascript:, onerror=, etc.
 *   <li>Command Injection: |, &, $(, etc.
 * </ul>
 *
 * <p>Use for: Free-text fields like notes, descriptions, comments
 *
 * <p>Sprint 2.1.6 - Security Hardening Phase 2
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeStringValidator.class)
@Documented
public @interface SafeString {

  String message() default "Input contains potentially dangerous characters";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /**
   * Allow empty/null values (default: true).
   *
   * @return true if empty values are allowed
   */
  boolean allowEmpty() default true;
}
