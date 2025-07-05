package de.freshplan.api.exception.mapper;

import de.freshplan.domain.profile.service.exception.DuplicateProfileException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS Exception Mapper for DuplicateProfileException.
 * 
 * @author FreshPlan Team
 * @since 1.0.0
 */
@Provider
public class DuplicateProfileExceptionMapper 
        implements ExceptionMapper<DuplicateProfileException> {
    
    @Override
    public Response toResponse(DuplicateProfileException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(409)
                .error("Duplicate Profile")
                .message(exception.getMessage())
                .build();
        
        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .build();
    }
}