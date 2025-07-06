package de.freshplan.api.exception.mapper;

import de.freshplan.domain.customer.service.exception.CustomerHasChildrenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * JAX-RS Exception Mapper for CustomerHasChildrenException. Maps CustomerHasChildrenException to
 * HTTP 400 Bad Request responses with consistent error format.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class CustomerHasChildrenExceptionMapper
    implements ExceptionMapper<CustomerHasChildrenException> {

  @Override
  public Response toResponse(CustomerHasChildrenException exception) {
    // Create structured error response
    Map<String, Object> errorResponse =
        Map.of(
            "error",
            "CUSTOMER_HAS_CHILDREN",
            "message",
            exception.getMessage(),
            "timestamp",
            LocalDateTime.now().toString(),
            "status",
            400,
            "customerId",
            exception.getCustomerId() != null ? exception.getCustomerId().toString() : null,
            "operation",
            exception.getOperation() != null ? exception.getOperation() : "unknown",
            "childCount",
            exception.getChildIds() != null ? exception.getChildIds().size() : 0);

    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
  }
}
