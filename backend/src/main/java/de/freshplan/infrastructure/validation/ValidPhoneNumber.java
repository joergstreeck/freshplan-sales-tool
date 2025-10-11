package de.freshplan.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates phone numbers for German/European format.
 *
 * <p>Accepted formats:
 * <ul>
 *   <li>+49 123 456789 (international format)</li>
 *   <li>0049 123 456789 (international format)</li>
 *   <li>0123 456789 (national format)</li>
 *   <li>+41 44 123 45 67 (Swiss format)</li>
 *   <li>+43 1 234 5678 (Austrian format)</li>
 * </ul>
 *
 * <p>Sprint 2.1.6 - Security Hardening Phase 2
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface ValidPhoneNumber {

  String message() default "Invalid phone number format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /**
   * Allow empty/null values (default: true).
   *
   * @return true if empty values are allowed
   */
  boolean allowEmpty() default true;
}
