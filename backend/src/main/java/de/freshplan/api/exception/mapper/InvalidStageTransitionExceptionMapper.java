package de.freshplan.api.exception.mapper;

import de.freshplan.domain.opportunity.service.exception.InvalidStageTransitionException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JAX-RS Exception Mapper for InvalidStageTransitionException. Maps InvalidStageTransitionException
 * to HTTP 400 Bad Request responses with consistent error format.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class InvalidStageTransitionExceptionMapper
    implements ExceptionMapper<InvalidStageTransitionException> {

  @Override
  public Response toResponse(InvalidStageTransitionException exception) {
    // Create structured error response
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "INVALID_STAGE_TRANSITION");
    errorResponse.put("message", exception.getMessage());
    errorResponse.put("timestamp", LocalDateTime.now().toString());
    errorResponse.put("status", 400);

    if (exception.getFromStage() != null && exception.getToStage() != null) {
      errorResponse.put("fromStage", exception.getFromStage().toString());
      errorResponse.put("toStage", exception.getToStage().toString());
    }

    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
  }
}