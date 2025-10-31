package de.freshplan.api.exception.mapper;

import de.freshplan.api.exception.ErrorResponse;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Maps ResteasyReactive validation exceptions to HTTP 400 responses.
 *
 * <p>This mapper handles Bean Validation violations thrown by Quarkus when request payloads fail
 * validation constraints (e.g., @NotBlank, @Email, @Size).
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class ResteasyReactiveViolationExceptionMapper
    implements ExceptionMapper<ResteasyReactiveViolationException> {

  private static final Logger LOG =
      Logger.getLogger(ResteasyReactiveViolationExceptionMapper.class.getName());

  @Override
  public Response toResponse(ResteasyReactiveViolationException exception) {
    // Log detailed violations SERVER-SIDE for debugging (not exposed to client)
    String violations =
        exception.getConstraintViolations().stream()
            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
            .collect(Collectors.joining(", "));

    LOG.severe("Bean Validation failed with violations: " + violations);

    // SECURITY: Return generic message to client (no internal field names/validation logic
    // exposed)
    // Detailed violations are logged server-side for debugging
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .type("VALIDATION_ERROR")
            .title("Bad Request")
            .status(400)
            .detail("Validation failed")
            .error("Bad Request")
            .message("Validation failed")
            .build();

    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
  }
}
