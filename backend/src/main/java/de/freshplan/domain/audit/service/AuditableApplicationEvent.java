package de.freshplan.domain.audit.service;

import de.freshplan.domain.audit.service.dto.AuditContext;

/**
 * Interface for application events that should be audited
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public interface AuditableApplicationEvent {

  /** Convert this event to an audit context for logging */
  AuditContext toAuditContext();

  /** Check if this event should be audited Default: true */
  default boolean shouldAudit() {
    return true;
  }
}
