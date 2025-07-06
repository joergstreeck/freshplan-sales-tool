package de.freshplan.api.exception.mapper;

import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * JAX-RS Exception Mapper for CustomerNotFoundException. Maps CustomerNotFoundException to HTTP 404
 * Not Found responses with consistent error format.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class CustomerNotFoundExceptionMapper implements ExceptionMapper<CustomerNotFoundException> {

  @Override
  public Response toResponse(CustomerNotFoundException exception) {
    // Create structured error response
    Map<String, Object> errorResponse;
    if (exception.getCustomerId() != null) {
      errorResponse =
          Map.of(
              "error", "CUSTOMER_NOT_FOUND",
              "message", exception.getMessage(),
              "timestamp", LocalDateTime.now().toString(),
              "status", 404,
              "customerId", exception.getCustomerId().toString());
    } else {
      errorResponse =
          Map.of(
              "error",
              "CUSTOMER_NOT_FOUND",
              "message",
              exception.getMessage(),
              "timestamp",
              LocalDateTime.now().toString(),
              "status",
              404);
    }

    return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
  }
}
