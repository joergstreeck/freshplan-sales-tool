package de.freshplan.api.exception.mapper;

import de.freshplan.domain.user.service.exception.InvalidRoleException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS exception mapper for InvalidRoleException.
 * 
 * Converts InvalidRoleException to HTTP 400 Bad Request responses
 * with a standardized error format.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class InvalidRoleExceptionMapper implements ExceptionMapper<InvalidRoleException> {
    
    @Context
    UriInfo uriInfo;
    
    @Override
    public Response toResponse(InvalidRoleException exception) {
        ErrorResponse error = new ErrorResponse(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "INVALID_ROLE",
            exception.getMessage(),
            uriInfo.getPath(),
            null
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .build();
    }
}