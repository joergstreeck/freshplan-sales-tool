package de.freshplan.api.exception.mapper;

import de.freshplan.domain.profile.service.exception.ProfileNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * JAX-RS Exception Mapper for ProfileNotFoundException.
 * 
 * @author FreshPlan Team
 * @since 1.0.0
 */
@Provider
public class ProfileNotFoundExceptionMapper 
        implements ExceptionMapper<ProfileNotFoundException> {
    
    @Override
    public Response toResponse(ProfileNotFoundException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(404)
                .error("Profile Not Found")
                .message(exception.getMessage())
                .build();
        
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }
}