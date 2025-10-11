package de.freshplan.infrastructure.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Global Exception Mapper for Secure Error Disclosure Prevention.
 *
 * <p>Prevents sensitive information leakage in production error responses:
 *
 * <ul>
 *   <li>Database schema details (SQL error messages)
 *   <li>File paths and stack traces
 *   <li>Internal service names and versions
 * </ul>
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>Production: Generic error messages with correlation ID
 *   <li>Development: Full error details for debugging
 * </ul>
 *
 * <p>Sprint 2.1.6 - Security Hardening Phase 2
 */
@Provider
public class SecureExceptionMapper implements ExceptionMapper<Throwable> {

  private static final Logger LOG = Logger.getLogger(SecureExceptionMapper.class);

  @ConfigProperty(name = "quarkus.profile")
  String profile;

  @Inject SecurityAuditLogger securityAuditLogger;

  @Override
  public Response toResponse(Throwable exception) {
    // Generate unique correlation ID for error tracking
    String correlationId = UUID.randomUUID().toString();

    // Log full error details server-side (for troubleshooting)
    LOG.errorf(
        exception,
        "Unhandled exception (Correlation ID: %s): %s",
        correlationId,
        exception.getMessage());

    // Audit security-relevant exceptions
    if (isSecurityRelevant(exception)) {
      securityAuditLogger.logSecurityException(
          "system", exception.getClass().getSimpleName(), exception.getMessage(), correlationId);
    }

    // Check if exception is already a WebApplicationException (user-facing errors)
    if (exception instanceof WebApplicationException wae) {
      // Return original response (e.g., 404, 403, 400 with custom message)
      return wae.getResponse();
    }

    // Production: Generic error message (prevent information disclosure)
    if ("prod".equals(profile)) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(
              Map.of(
                  "error",
                  "Internal server error",
                  "message",
                  "An unexpected error occurred. Please contact support with correlation ID: "
                      + correlationId,
                  "correlationId",
                  correlationId))
          .build();
    }

    // Development: Full error details for debugging
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(
            Map.of(
                "error",
                "Internal server error",
                "message",
                exception.getMessage() != null ? exception.getMessage() : "Unknown error",
                "type",
                exception.getClass().getSimpleName(),
                "correlationId",
                correlationId,
                "note",
                "Full stack trace available in server logs (dev mode only)"))
        .build();
  }

  /**
   * Check if exception is security-relevant (requires audit logging).
   *
   * @param exception Exception to check
   * @return true if security-relevant
   */
  private boolean isSecurityRelevant(Throwable exception) {
    String exceptionName = exception.getClass().getSimpleName();
    return exceptionName.contains("Security")
        || exceptionName.contains("Authentication")
        || exceptionName.contains("Authorization")
        || exceptionName.contains("Forbidden")
        || exceptionName.contains("Unauthorized");
  }
}
