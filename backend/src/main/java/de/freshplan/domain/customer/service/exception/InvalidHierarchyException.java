package de.freshplan.domain.customer.service.exception;

/**
 * Exception thrown when a customer hierarchy operation violates hierarchy integrity constraints.
 *
 * <p>Sprint 2.1.7.7 Multi-Location Management - D1
 *
 * <p>Examples: - Attempting to create FILIALE without HEADQUARTER parent - HEADQUARTER having a
 * parent - FILIALE with non-HEADQUARTER parent
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
public class InvalidHierarchyException extends RuntimeException {

  public InvalidHierarchyException(String message) {
    super(message);
  }

  public InvalidHierarchyException(String message, Throwable cause) {
    super(message, cause);
  }
}
