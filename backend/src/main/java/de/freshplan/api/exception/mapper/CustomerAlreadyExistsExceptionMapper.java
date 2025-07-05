package de.freshplan.api.exception.mapper;

import de.freshplan.domain.customer.service.exception.CustomerAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * JAX-RS Exception Mapper for CustomerAlreadyExistsException.
 * Maps CustomerAlreadyExistsException to HTTP 409 Conflict responses
 * with consistent error format.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class CustomerAlreadyExistsExceptionMapper implements ExceptionMapper<CustomerAlreadyExistsException> {
    
    @Override
    public Response toResponse(CustomerAlreadyExistsException exception) {
        // Create structured error response
        Map<String, Object> errorResponse = Map.of(
            "error", "CUSTOMER_ALREADY_EXISTS",
            "message", exception.getMessage(),
            "timestamp", LocalDateTime.now().toString(),
            "status", 409,
            "conflictingField", exception.getConflictingField() != null ? 
                exception.getConflictingField() : "unknown",
            "conflictingValue", exception.getConflictingValue() != null ? 
                exception.getConflictingValue() : "unknown"
        );
        
        return Response
            .status(Response.Status.CONFLICT)
            .entity(errorResponse)
            .build();
    }
}