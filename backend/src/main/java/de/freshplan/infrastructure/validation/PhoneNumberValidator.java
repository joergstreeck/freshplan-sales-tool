package de.freshplan.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidPhoneNumber annotation.
 *
 * <p>Validates phone numbers using regex for German/European formats.
 *
 * <p>Sprint 2.1.6 - Security Hardening Phase 2
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

  private boolean allowEmpty;

  // Regex for German/European phone numbers
  // Matches: +49 123 456789, 0049 123 456789, 0123 456789, etc.
  private static final String PHONE_REGEX =
      "^(\\+?\\d{1,3}[\\s.-]?)?((\\(\\d{1,5}\\))|\\d{1,5})[\\s.-]?\\d{1,9}[\\s.-]?\\d{1,9}$";

  @Override
  public void initialize(ValidPhoneNumber constraintAnnotation) {
    this.allowEmpty = constraintAnnotation.allowEmpty();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Allow null/empty if configured
    if (value == null || value.trim().isEmpty()) {
      return allowEmpty;
    }

    // Remove common formatting characters for validation
    String normalized = value.replaceAll("[\\s\\-\\.\\(\\)]", "");

    // Check length (minimum 7 digits, maximum 15 digits for international format)
    if (normalized.length() < 7 || normalized.length() > 15) {
      return false;
    }

    // Validate format using regex
    return value.matches(PHONE_REGEX);
  }
}
