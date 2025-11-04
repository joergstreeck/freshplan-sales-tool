package de.freshplan.domain.audit.service.provider;

import de.freshplan.domain.audit.entity.AuditEntry;

/**
 * Audit event for CDI event bus
 *
 * <p>Extracted from AuditService during Sprint 2.1.7.7 Cycle 7 fix to break circular dependency
 * between audit.service and audit.service.command packages. Follows Dependency Inversion Principle
 * (SOLID).
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class AuditEvent {
  private final AuditEntry entry;

  public AuditEvent(AuditEntry entry) {
    this.entry = entry;
  }

  public AuditEntry getEntry() {
    return entry;
  }
}
