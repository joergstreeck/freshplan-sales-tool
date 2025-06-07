package de.freshplan.api.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps IllegalArgumentException to HTTP 400 Bad Request responses.
 * 
 * Provides consistent error response format for validation errors
 * thrown by domain services.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class IllegalArgumentExceptionMapper 
        implements ExceptionMapper<IllegalArgumentException> {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(IllegalArgumentExceptionMapper.class);
    
    @Override
    public Response toResponse(IllegalArgumentException exception) {
        logger.warn("Invalid argument exception: {}", exception.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(400)
                .error("INVALID_REQUEST")
                .message(exception.getMessage())
                .build();
        
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}