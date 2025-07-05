package de.freshplan.domain.customer.service.exception;

import java.util.UUID;

/**
 * Exception thrown when a customer cannot be found.
 * This exception indicates that a requested customer does not exist
 * or is not accessible in the current context.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CustomerNotFoundException extends RuntimeException {
    
    private final UUID customerId;
    
    /**
     * Creates a CustomerNotFoundException with a message.
     * 
     * @param message The exception message
     */
    public CustomerNotFoundException(String message) {
        super(message);
        this.customerId = null;
    }
    
    /**
     * Creates a CustomerNotFoundException for a specific customer ID.
     * 
     * @param customerId The ID of the customer that was not found
     */
    public CustomerNotFoundException(UUID customerId) {
        super("Customer not found with ID: " + customerId);
        this.customerId = customerId;
    }
    
    /**
     * Creates a CustomerNotFoundException with a custom message and customer ID.
     * 
     * @param message The exception message
     * @param customerId The ID of the customer that was not found
     */
    public CustomerNotFoundException(String message, UUID customerId) {
        super(message);
        this.customerId = customerId;
    }
    
    /**
     * Creates a CustomerNotFoundException with a message and cause.
     * 
     * @param message The exception message
     * @param cause The underlying cause
     */
    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.customerId = null;
    }
    
    /**
     * Gets the customer ID that was not found.
     * 
     * @return The customer ID, or null if not specified
     */
    public UUID getCustomerId() {
        return customerId;
    }
}