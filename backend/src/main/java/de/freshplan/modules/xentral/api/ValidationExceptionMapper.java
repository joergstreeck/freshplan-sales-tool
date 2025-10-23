package de.freshplan.modules.xentral.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global Exception Mapper for Bean Validation Errors.
 *
 * <p>Sprint 2.1.7.2 - D5: Test-Driven Bug Fix
 *
 * <p>Converts ConstraintViolationException to structured JSON error responses.
 *
 * <p><b>Problem:</b> @Valid annotation on REST endpoints throws ConstraintViolationException, but
 * default handler returns generic "Bad Request" without details.
 *
 * <p><b>Solution:</b> This mapper catches validation exceptions and returns structured JSON with
 * detailed error messages.
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    logger.warn("Bean Validation failed: {}", exception.getMessage());

    // Extract first constraint violation message (most relevant for single-field errors)
    String errorMessage =
        exception.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .findFirst()
            .orElse("Validation failed");

    // Build structured JSON error response
    Map<String, String> error = new HashMap<>();
    error.put("error", errorMessage);

    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }
}
