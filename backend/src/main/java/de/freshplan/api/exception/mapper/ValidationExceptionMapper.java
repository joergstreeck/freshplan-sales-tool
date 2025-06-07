package de.freshplan.api.exception.mapper;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps validation exceptions to HTTP 400 responses.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class ValidationExceptionMapper 
        implements ExceptionMapper<ConstraintViolationException> {
    
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    "Bad Request",
                    "Validation failed",
                    null,
                    null
                ))
                .build();
    }
}