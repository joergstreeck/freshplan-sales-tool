package de.freshplan.domain.audit.interceptor;

import de.freshplan.domain.audit.annotation.Auditable;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.audit.service.dto.AuditContext;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jboss.logging.Logger;

/**
 * Interceptor for automatic audit logging of annotated methods
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
// @Interceptor // TODO: Re-enable when annotation binding is fixed
// @Auditable // TODO: Fix annotation binding
// @Priority(Interceptor.Priority.APPLICATION + 100)
public class AuditInterceptor {

  private static final Logger log = Logger.getLogger(AuditInterceptor.class);

  @Inject AuditService auditService;

  @AroundInvoke
  public Object auditMethod(InvocationContext context) throws Exception {
    Method method = context.getMethod();
    Auditable annotation = method.getAnnotation(Auditable.class);

    if (annotation == null) {
      // Check class-level annotation
      annotation = context.getTarget().getClass().getAnnotation(Auditable.class);
    }

    if (annotation == null) {
      return context.proceed();
    }

    // Extract audit information
    Object[] params = context.getParameters();
    UUID entityId = extractEntityId(params, annotation.entityIdParam());
    Object oldValue = extractOldValue(params, annotation.oldValueParam());
    String reason = extractReason(params, annotation.reasonParam());

    Object result = null;
    Exception caughtException = null;

    try {
      // Execute the method
      result = context.proceed();

      // Log successful operation
      AuditContext auditContext =
          AuditContext.builder()
              .eventType(annotation.eventType())
              .entityType(annotation.entityType())
              .entityId(entityId)
              .oldValue(oldValue)
              .newValue(annotation.includeResult() ? result : null)
              .changeReason(reason)
              .build();

      if (annotation.sync()) {
        auditService.logSync(auditContext);
      } else {
        CompletableFuture<UUID> future = auditService.logAsync(auditContext);

        // Log any async errors
        future.exceptionally(
            throwable -> {
              log.error("Failed to log audit event asynchronously", throwable);
              return null;
            });
      }

    } catch (Exception e) {
      caughtException = e;

      // Log failed operation
      try {
        AuditContext failureContext =
            AuditContext.builder()
                .eventType(determineFailureEventType(annotation.eventType()))
                .entityType(annotation.entityType())
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(
                    Map.of(
                        "error", e.getClass().getSimpleName(),
                        "message", e.getMessage()))
                .changeReason(reason)
                .build();

        // Always log failures synchronously for immediate recording
        auditService.logSync(failureContext);

      } catch (Exception auditException) {
        log.error("Critical: Failed to audit method failure", auditException);
      }

      throw e;
    }

    return result;
  }

  /** Extract entity ID from method parameters */
  private UUID extractEntityId(Object[] params, int paramIndex) {
    if (paramIndex < 0 || paramIndex >= params.length) {
      return null;
    }

    Object param = params[paramIndex];

    if (param instanceof UUID) {
      return (UUID) param;
    } else if (param instanceof String) {
      try {
        return UUID.fromString((String) param);
      } catch (IllegalArgumentException e) {
        log.warnf("Failed to parse UUID from string: %s", param);
        return null;
      }
    } else if (param != null) {
      // Try to extract ID from entity object
      try {
        Method getId = param.getClass().getMethod("getId");
        Object id = getId.invoke(param);
        if (id instanceof UUID) {
          return (UUID) id;
        } else if (id instanceof String) {
          return UUID.fromString((String) id);
        }
      } catch (Exception e) {
        log.debug("Failed to extract ID from entity object", e);
      }
    }

    return null;
  }

  /** Extract old value from method parameters */
  private Object extractOldValue(Object[] params, int paramIndex) {
    if (paramIndex < 0 || paramIndex >= params.length) {
      return null;
    }
    return params[paramIndex];
  }

  /** Extract change reason from method parameters */
  private String extractReason(Object[] params, int paramIndex) {
    if (paramIndex < 0 || paramIndex >= params.length) {
      return null;
    }

    Object param = params[paramIndex];
    return param != null ? param.toString() : null;
  }

  /** Determine failure event type based on success event type */
  private AuditEventType determineFailureEventType(AuditEventType successType) {
    // Map success types to failure types
    return switch (successType) {
      case LOGIN_SUCCESS -> AuditEventType.LOGIN_FAILURE;
      case XENTRAL_SYNC_SUCCESS -> AuditEventType.XENTRAL_SYNC_FAILURE;
      case DATA_IMPORT_STARTED -> AuditEventType.DATA_IMPORT_FAILED;
      case DATA_EXPORT_STARTED -> AuditEventType.DATA_EXPORT_FAILED;
      case BACKUP_STARTED -> AuditEventType.BACKUP_FAILED;
      default -> AuditEventType.ERROR_OCCURRED;
    };
  }
}
