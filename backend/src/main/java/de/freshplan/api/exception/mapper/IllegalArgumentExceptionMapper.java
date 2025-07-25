package de.freshplan.api.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * JAX-RS Exception Mapper for IllegalArgumentException. Maps IllegalArgumentException to HTTP 400
 * Bad Request responses with consistent error format.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

  @Override
  public Response toResponse(IllegalArgumentException exception) {
    // Create structured error response
    Map<String, Object> errorResponse =
        Map.of(
            "error",
            "INVALID_REQUEST",
            "message",
            exception.getMessage(),
            "timestamp",
            LocalDateTime.now().toString(),
            "status",
            400);

    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
  }
}
