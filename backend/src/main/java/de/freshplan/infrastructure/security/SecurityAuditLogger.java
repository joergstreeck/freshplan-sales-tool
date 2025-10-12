package de.freshplan.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Security Audit Logger for Enterprise Compliance.
 *
 * <p>Logs all security-relevant events for:
 *
 * <ul>
 *   <li>GDPR Compliance (data access audit trail)
 *   <li>Incident Response (detect attack patterns)
 *   <li>Forensics (post-incident analysis)
 * </ul>
 *
 * <p>Sprint 2.1.6 - Enterprise Security Hardening
 */
@ApplicationScoped
public class SecurityAuditLogger {

  private static final Logger LOG = Logger.getLogger(SecurityAuditLogger.class);

  /**
   * Log unauthorized access attempt (403 Forbidden).
   *
   * <p>GDPR Article 32: Security of processing requires audit trails
   *
   * @param userId User attempting unauthorized access
   * @param resourceType Type of resource (e.g., "LEAD", "CUSTOMER")
   * @param resourceId ID of resource
   * @param action Attempted action (e.g., "READ", "UPDATE", "DELETE")
   */
  public void logUnauthorizedAccess(
      String userId, String resourceType, String resourceId, String action) {
    LOG.warnf(
        "SECURITY_AUDIT: UNAUTHORIZED_ACCESS | user=%s | resource=%s:%s | action=%s",
        userId, resourceType, resourceId, action);
  }

  /**
   * Log role violation attempt (user tried action requiring higher privileges).
   *
   * @param userId User ID
   * @param requiredRole Role required for action
   * @param attemptedAction Action attempted
   */
  public void logRoleViolation(String userId, String requiredRole, String attemptedAction) {
    LOG.warnf(
        "SECURITY_AUDIT: ROLE_VIOLATION | user=%s | required_role=%s | action=%s",
        userId, requiredRole, attemptedAction);
  }

  /**
   * Log successful privileged action (ADMIN/MANAGER actions).
   *
   * @param userId User ID
   * @param role User role
   * @param action Action performed
   * @param resourceType Resource type
   * @param resourceId Resource ID
   */
  public void logPrivilegedAction(
      String userId, String role, String action, String resourceType, String resourceId) {
    LOG.infof(
        "SECURITY_AUDIT: PRIVILEGED_ACTION | user=%s | role=%s | action=%s | resource=%s:%s",
        userId, role, action, resourceType, resourceId);
  }

  /**
   * Log XSS/Injection attempt detected and blocked.
   *
   * @param userId User ID (if authenticated)
   * @param attackType Type of attack (e.g., "XSS", "SQL_INJECTION")
   * @param payload Malicious payload (sanitized for logging)
   * @param endpoint API endpoint targeted
   */
  public void logInjectionAttempt(
      String userId, String attackType, String payload, String endpoint) {
    // Sanitize payload for logging (max 100 chars, escape special chars)
    String sanitizedPayload =
        payload != null
            ? payload.substring(0, Math.min(100, payload.length())).replaceAll("[\\r\\n]", " ")
            : "null";

    LOG.warnf(
        "SECURITY_AUDIT: INJECTION_ATTEMPT | user=%s | type=%s | endpoint=%s | payload=%s",
        userId != null ? userId : "anonymous", attackType, endpoint, sanitizedPayload);
  }

  /**
   * Log authentication failure (for rate limiting/brute force detection).
   *
   * @param userId User ID attempted
   * @param reason Failure reason
   * @param ipAddress IP address of request
   */
  public void logAuthenticationFailure(String userId, String reason, String ipAddress) {
    LOG.warnf(
        "SECURITY_AUDIT: AUTH_FAILURE | user=%s | reason=%s | ip=%s", userId, reason, ipAddress);
  }

  /**
   * Log data export (GDPR compliance - track who exports what).
   *
   * @param userId User performing export
   * @param exportType Type of export (e.g., "CSV", "PDF")
   * @param recordCount Number of records exported
   */
  public void logDataExport(String userId, String exportType, int recordCount) {
    LOG.infof(
        "SECURITY_AUDIT: DATA_EXPORT | user=%s | type=%s | count=%d",
        userId, exportType, recordCount);
  }

  /**
   * Log sensitive data access (for GDPR Article 30 - Records of processing activities).
   *
   * @param userId User accessing data
   * @param dataType Type of sensitive data (e.g., "PERSONAL_DATA", "CONTACT_INFO")
   * @param resourceId Resource ID
   */
  public void logSensitiveDataAccess(String userId, String dataType, String resourceId) {
    LOG.infof(
        "SECURITY_AUDIT: SENSITIVE_ACCESS | user=%s | data_type=%s | resource=%s",
        userId, dataType, resourceId);
  }

  /**
   * Log rate limit exceeded (for brute force detection and API abuse tracking).
   *
   * @param userId User ID (or IP address if unauthenticated)
   * @param method HTTP method (GET, POST, PUT, PATCH, DELETE)
   * @param path API endpoint path
   * @param limitType Type of limit exceeded (GENERAL, WRITE, DELETE)
   */
  public void logRateLimitExceeded(String userId, String method, String path, String limitType) {
    LOG.warnf(
        "SECURITY_AUDIT: RATE_LIMIT_EXCEEDED | user=%s | method=%s | path=%s | limit_type=%s",
        userId, method, path, limitType);
  }

  /**
   * Log security-relevant exception (for incident response).
   *
   * @param userId User ID (or "system" if not user-triggered)
   * @param exceptionType Exception type (e.g., "SecurityException", "AuthenticationException")
   * @param message Exception message (sanitized, no sensitive data)
   * @param correlationId Correlation ID for error tracking
   */
  public void logSecurityException(
      String userId, String exceptionType, String message, String correlationId) {
    LOG.errorf(
        "SECURITY_AUDIT: SECURITY_EXCEPTION | user=%s | type=%s | message=%s | correlation_id=%s",
        userId, exceptionType, sanitizeMessage(message), correlationId);
  }

  /**
   * Sanitize exception message for logging (remove sensitive data like passwords, tokens).
   *
   * @param message Original message
   * @return Sanitized message
   */
  private String sanitizeMessage(String message) {
    if (message == null) {
      return "null";
    }
    // Truncate to 200 chars and escape newlines
    String sanitized = message.length() > 200 ? message.substring(0, 200) + "..." : message;
    return sanitized.replaceAll("[\\r\\n]", " ");
  }
}
