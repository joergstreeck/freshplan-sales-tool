package de.freshplan.api.exception.mapper;

import de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * JAX-RS Exception Mapper for OpportunityNotFoundException. Maps OpportunityNotFoundException to
 * HTTP 404 Not Found responses with consistent error format.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class OpportunityNotFoundExceptionMapper
    implements ExceptionMapper<OpportunityNotFoundException> {

  @Override
  public Response toResponse(OpportunityNotFoundException exception) {
    // Create structured error response
    Map<String, Object> errorResponse =
        Map.of(
            "error", "OPPORTUNITY_NOT_FOUND",
            "message", exception.getMessage(),
            "timestamp", LocalDateTime.now().toString(),
            "status", 404,
            "opportunityId", exception.getOpportunityId().toString());

    return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
  }
}
