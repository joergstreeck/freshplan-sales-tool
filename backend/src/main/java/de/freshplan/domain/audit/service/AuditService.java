package de.freshplan.domain.audit.service;

import de.freshplan.domain.user.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Audit service for logging permission checks and other security events. */
@ApplicationScoped
@Transactional
public class AuditService {

  private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

  @Inject EntityManager entityManager;

  /** Logs a permission check for audit purposes. */
  public void logPermissionCheck(
      User user, String permissionCode, String checkType, boolean result) {
    try {
      entityManager
          .createNativeQuery(
              "INSERT INTO permission_audit (id, user_id, permission_code, check_type, result, checked_at) "
                  + "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?)")
          .setParameter(1, user.getId())
          .setParameter(2, permissionCode)
          .setParameter(3, checkType)
          .setParameter(4, result)
          .setParameter(5, Instant.now())
          .executeUpdate();

      logger.debug(
          "Logged permission check: user={}, permission={}, type={}, result={}",
          user.getUsername(),
          permissionCode,
          checkType,
          result);

    } catch (Exception e) {
      // Don't let audit failures break the main operation
      logger.error("Failed to log permission check: {}", e.getMessage(), e);
    }
  }

  /** Logs a general security event. */
  public void logSecurityEvent(UUID userId, String eventType, String description, boolean success) {
    try {
      // For now, log to application logs
      logger.info(
          "Security event: user={}, event={}, description={}, success={}",
          userId,
          eventType,
          description,
          success);
    } catch (Exception e) {
      logger.error("Failed to log security event: {}", e.getMessage(), e);
    }
  }
}
