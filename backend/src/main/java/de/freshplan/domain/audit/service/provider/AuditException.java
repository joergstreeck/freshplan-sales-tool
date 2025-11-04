package de.freshplan.domain.audit.service.provider;

/**
 * Exception for audit failures
 *
 * <p>Extracted from AuditService during Sprint 2.1.7.7 Cycle 7 fix to break circular dependency
 * between audit.service and audit.service.command packages. Follows Dependency Inversion Principle
 * (SOLID).
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class AuditException extends RuntimeException {
  public AuditException(String message, Throwable cause) {
    super(message, cause);
  }
}
