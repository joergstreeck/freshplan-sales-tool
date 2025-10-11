package de.freshplan.api.exception.mapper;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Maps validation exceptions to HTTP 400 responses.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  private static final Logger LOG = Logger.getLogger(ValidationExceptionMapper.class.getName());

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    // Log detailed violations SERVER-SIDE for debugging (not exposed to client)
    String violations = exception.getConstraintViolations().stream()
        .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
        .collect(Collectors.joining(", "));

    LOG.severe("Validation failed with violations: " + violations);

    // SECURITY: Return generic message to client (no internal field names/validation logic exposed)
    // Detailed violations are logged server-side for debugging
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(
            new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Bad Request",
                "Validation failed. Please check your input.",
                null,
                null))
        .build();
  }
}
