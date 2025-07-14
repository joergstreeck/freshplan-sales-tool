package de.freshplan.api.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.freshplan.domain.customer.service.exception.CustomerAlreadyExistsException;
import de.freshplan.domain.customer.service.exception.CustomerHasChildrenException;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.*;
import org.jboss.logging.Logger;

/**
 * Global exception mapper that handles all exceptions and returns standardized error responses.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

  @Context UriInfo uriInfo;

  @Override
  public Response toResponse(Exception exception) {
    // Generate trace ID for correlation
    String traceId = UUID.randomUUID().toString();

    // Log the exception with trace ID
    LOG.errorf(exception, "Exception caught [traceId=%s]", traceId);

    // Handle specific exceptions
    if (exception instanceof CustomerNotFoundException) {
      return handleCustomerNotFound((CustomerNotFoundException) exception);
    }

    if (exception instanceof UserNotFoundException) {
      return handleUserNotFound((UserNotFoundException) exception);
    }

    if (exception instanceof CustomerAlreadyExistsException) {
      return handleCustomerAlreadyExists((CustomerAlreadyExistsException) exception);
    }

    if (exception instanceof CustomerHasChildrenException) {
      return handleCustomerHasChildren((CustomerHasChildrenException) exception);
    }

    if (exception instanceof ConstraintViolationException) {
      return handleValidation((ConstraintViolationException) exception);
    }

    if (exception instanceof IllegalArgumentException) {
      return handleBadRequest((IllegalArgumentException) exception);
    }

    if (exception instanceof SecurityException) {
      return handleSecurity((SecurityException) exception);
    }

    if (exception instanceof JsonProcessingException) {
      return handleJsonProcessing((JsonProcessingException) exception);
    }

    if (exception instanceof jakarta.ws.rs.WebApplicationException) {
      return handleWebApplication((jakarta.ws.rs.WebApplicationException) exception);
    }

    // Default to internal server error
    return handleGeneric(exception, traceId);
  }

  private Response handleCustomerNotFound(CustomerNotFoundException e) {
    String customerId = e.getCustomerId() != null ? e.getCustomerId().toString() : "unknown";
    ErrorResponse error = ErrorResponse.notFound("Customer", customerId);
    if (uriInfo != null) {
      error =
          ErrorResponse.builder()
              .type(error.getType())
              .title(error.getTitle())
              .status(error.getStatus())
              .detail(error.getDetail())
              .instance(uriInfo.getPath())
              .timestamp(error.getTimestamp())
              .build();
    }
    return Response.status(Response.Status.NOT_FOUND).entity(error).build();
  }

  private Response handleUserNotFound(UserNotFoundException e) {
    // Extract user ID from message if possible
    String userId = "unknown";
    if (e.getMessage() != null && e.getMessage().contains("ID:")) {
      int idStart = e.getMessage().indexOf("ID:") + 3;
      userId = e.getMessage().substring(idStart).trim();
    }
    
    ErrorResponse.Builder errorBuilder = ErrorResponse.builder()
        .type("RESOURCE_NOT_FOUND")
        .title("Resource Not Found")
        .status(404)
        .detail(String.format("User with ID %s not found", userId))
        .timestamp(LocalDateTime.now())
        .error("USER_NOT_FOUND") // Set the expected error code
        .message(String.format("User with ID %s not found", userId)); // Set message for backward compatibility
    
    if (uriInfo != null) {
      errorBuilder.instance(uriInfo.getPath());
    }
    
    return Response.status(Response.Status.NOT_FOUND).entity(errorBuilder.build()).build();
  }

  private Response handleCustomerAlreadyExists(CustomerAlreadyExistsException e) {
    String detail;
    if (e.getConflictingField() != null && e.getConflictingValue() != null) {
      detail =
          String.format(
              "Customer with %s '%s' already exists",
              e.getConflictingField(), e.getConflictingValue());
    } else {
      detail = e.getMessage();
    }
    ErrorResponse error = ErrorResponse.conflict(detail);
    if (uriInfo != null) {
      error =
          ErrorResponse.builder()
              .type(error.getType())
              .title(error.getTitle())
              .status(error.getStatus())
              .detail(error.getDetail())
              .instance(uriInfo.getPath())
              .timestamp(error.getTimestamp())
              .build();
    }
    return Response.status(Response.Status.CONFLICT).entity(error).build();
  }

  private Response handleCustomerHasChildren(CustomerHasChildrenException e) {
    ErrorResponse error = ErrorResponse.conflict(e.getMessage());
    if (uriInfo != null) {
      error =
          ErrorResponse.builder()
              .type(error.getType())
              .title(error.getTitle())
              .status(error.getStatus())
              .detail(error.getDetail())
              .instance(uriInfo.getPath())
              .timestamp(error.getTimestamp())
              .build();
    }
    return Response.status(Response.Status.CONFLICT).entity(error).build();
  }

  private Response handleValidation(ConstraintViolationException e) {
    Map<String, List<String>> violations = new HashMap<>();

    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      String propertyPath = violation.getPropertyPath().toString();
      String message = violation.getMessage();

      violations.computeIfAbsent(propertyPath, k -> new ArrayList<>()).add(message);
    }

    ErrorResponse error = ErrorResponse.validation(violations);
    if (uriInfo != null) {
      error =
          ErrorResponse.builder()
              .type(error.getType())
              .title(error.getTitle())
              .status(error.getStatus())
              .detail(error.getDetail())
              .instance(uriInfo.getPath())
              .timestamp(error.getTimestamp())
              .violations(violations)
              .build();
    }
    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }

  private Response handleBadRequest(IllegalArgumentException e) {
    ErrorResponse error = ErrorResponse.badRequest(e.getMessage());
    if (uriInfo != null) {
      error =
          ErrorResponse.builder()
              .type(error.getType())
              .title(error.getTitle())
              .status(error.getStatus())
              .detail(error.getDetail())
              .instance(uriInfo.getPath())
              .timestamp(error.getTimestamp())
              .build();
    }
    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }

  private Response handleSecurity(SecurityException e) {
    // Don't expose security details
    ErrorResponse error = ErrorResponse.forbidden(null);
    if (uriInfo != null) {
      error =
          ErrorResponse.builder()
              .type(error.getType())
              .title(error.getTitle())
              .status(error.getStatus())
              .detail(error.getDetail())
              .instance(uriInfo.getPath())
              .timestamp(error.getTimestamp())
              .build();
    }
    return Response.status(Response.Status.FORBIDDEN).entity(error).build();
  }

  private Response handleJsonProcessing(JsonProcessingException e) {
    String detail;
    if (e instanceof JsonParseException) {
      detail = "Invalid JSON format: " + e.getOriginalMessage();
    } else {
      detail = "JSON processing error: " + e.getMessage();
    }

    ErrorResponse error = ErrorResponse.badRequest(detail);
    if (uriInfo != null) {
      error =
          ErrorResponse.builder()
              .type(error.getType())
              .title(error.getTitle())
              .status(error.getStatus())
              .detail(error.getDetail())
              .instance(uriInfo.getPath())
              .timestamp(error.getTimestamp())
              .build();
    }
    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }

  private Response handleWebApplication(jakarta.ws.rs.WebApplicationException e) {
    Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());

    // Special handling for 415 Unsupported Media Type
    if (status == Response.Status.UNSUPPORTED_MEDIA_TYPE) {
      ErrorResponse error =
          ErrorResponse.builder()
              .type("/errors/unsupported-media-type")
              .title("Unsupported Media Type")
              .status(415)
              .detail("The request media type is not supported. Expected: application/json")
              .instance(uriInfo != null ? uriInfo.getPath() : null)
              .timestamp(java.time.LocalDateTime.now())
              .build();
      return Response.status(status).entity(error).build();
    }

    // Pass through the original status with a proper error response
    ErrorResponse error =
        ErrorResponse.builder()
            .type("/errors/" + status.name().toLowerCase().replace('_', '-'))
            .title(status.getReasonPhrase())
            .status(status.getStatusCode())
            .detail(e.getMessage())
            .instance(uriInfo != null ? uriInfo.getPath() : null)
            .timestamp(java.time.LocalDateTime.now())
            .build();
    return Response.status(status).entity(error).build();
  }

  private Response handleGeneric(Exception e, String traceId) {
    // Don't expose internal error details in production
    String profile = io.quarkus.runtime.LaunchMode.current().getDefaultProfile();
    String detail = "dev".equals(profile) || "test".equals(profile) ? e.getMessage() : null;

    ErrorResponse error = ErrorResponse.internalError(detail, traceId);
    if (uriInfo != null) {
      error =
          ErrorResponse.builder()
              .type(error.getType())
              .title(error.getTitle())
              .status(error.getStatus())
              .detail(error.getDetail())
              .instance(uriInfo.getPath())
              .timestamp(error.getTimestamp())
              .traceId(traceId)
              .build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
  }
}
