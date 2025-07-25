package de.freshplan.domain.audit.annotation;

import de.freshplan.domain.audit.entity.AuditEventType;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Marks a method for automatic audit logging
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @Auditable(eventType = AuditEventType.OPPORTUNITY_UPDATED, entityType = "opportunity")
 * public Opportunity updateOpportunity(UUID id, UpdateRequest request) {
 *     // method implementation
 * }
 * }</pre>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Inherited
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

  /** The audit event type */
  @Nonbinding
  AuditEventType eventType();

  /** The entity type being audited */
  @Nonbinding
  String entityType();

  /** Parameter index containing the entity ID (default: 0) */
  @Nonbinding
  int entityIdParam() default 0;

  /** Parameter index containing the old value (default: -1 means not captured) */
  @Nonbinding
  int oldValueParam() default -1;

  /** Parameter index containing the change reason (default: -1 means not captured) */
  @Nonbinding
  int reasonParam() default -1;

  /** Whether to include the method result as new value (default: true) */
  @Nonbinding
  boolean includeResult() default true;

  /** Whether to log synchronously (default: false for async) */
  @Nonbinding
  boolean sync() default false;
}
