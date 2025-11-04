package de.freshplan.domain.audit.service.provider;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Audit Configuration
 *
 * <p>Extracted from AuditService during Sprint 2.1.7.7 Cycle 7 fix to break circular dependency
 * between audit.service and audit.service.command packages. Follows Dependency Inversion Principle
 * (SOLID).
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class AuditConfiguration {
  // These would typically come from application.properties
  public int getAsyncThreadPoolSize() {
    return 5;
  }

  public boolean isEventBusEnabled() {
    return true;
  }
}
