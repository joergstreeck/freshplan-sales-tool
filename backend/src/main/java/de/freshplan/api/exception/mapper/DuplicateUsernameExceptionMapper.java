package de.freshplan.api.exception.mapper;

import de.freshplan.domain.user.service.exception.DuplicateUsernameException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * JAX-RS exception mapper for DuplicateUsernameException.
 * 
 * Converts DuplicateUsernameException to a 409 Conflict response
 * with a standardized error format.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class DuplicateUsernameExceptionMapper 
        implements ExceptionMapper<DuplicateUsernameException> {
    
    private static final Logger LOG = 
            Logger.getLogger(DuplicateUsernameExceptionMapper.class);
    
    @Context
    UriInfo uriInfo;
    
    @Override
    public Response toResponse(DuplicateUsernameException exception) {
        LOG.debugf("Duplicate username: %s", exception.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(409)
                .error("Conflict")
                .message(exception.getMessage())
                .path(uriInfo.getPath())
                .build();
        
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}