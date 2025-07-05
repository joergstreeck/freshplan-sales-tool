package de.freshplan.domain.customer.service.exception;

import java.util.List;
import java.util.UUID;

/**
 * Exception thrown when attempting to perform an operation on a customer
 * that has child customers, and the operation is not allowed.
 * This typically occurs during delete or merge operations.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CustomerHasChildrenException extends RuntimeException {
    
    private final UUID customerId;
    private final List<UUID> childIds;
    private final String operation;
    
    /**
     * Creates a CustomerHasChildrenException with a message.
     * 
     * @param message The exception message
     */
    public CustomerHasChildrenException(String message) {
        super(message);
        this.customerId = null;
        this.childIds = null;
        this.operation = null;
    }
    
    /**
     * Creates a CustomerHasChildrenException for a specific customer and operation.
     * 
     * @param customerId The ID of the customer that has children
     * @param operation The operation that was attempted (e.g., "delete", "merge")
     */
    public CustomerHasChildrenException(UUID customerId, String operation) {
        super("Cannot " + operation + " customer " + customerId + 
              " because it has child customers");
        this.customerId = customerId;
        this.childIds = null;
        this.operation = operation;
    }
    
    /**
     * Creates a CustomerHasChildrenException with detailed child information.
     * 
     * @param customerId The ID of the customer that has children
     * @param childIds The IDs of the child customers
     * @param operation The operation that was attempted
     */
    public CustomerHasChildrenException(UUID customerId, List<UUID> childIds, String operation) {
        super("Cannot " + operation + " customer " + customerId + 
              " because it has " + childIds.size() + " child customers: " + childIds);
        this.customerId = customerId;
        this.childIds = childIds;
        this.operation = operation;
    }
    
    /**
     * Creates a CustomerHasChildrenException with a custom message and details.
     * 
     * @param message The exception message
     * @param customerId The ID of the customer that has children
     * @param operation The operation that was attempted
     */
    public CustomerHasChildrenException(String message, UUID customerId, String operation) {
        super(message);
        this.customerId = customerId;
        this.childIds = null;
        this.operation = operation;
    }
    
    /**
     * Creates a CustomerHasChildrenException with a message and cause.
     * 
     * @param message The exception message
     * @param cause The underlying cause
     */
    public CustomerHasChildrenException(String message, Throwable cause) {
        super(message, cause);
        this.customerId = null;
        this.childIds = null;
        this.operation = null;
    }
    
    /**
     * Gets the customer ID that has children.
     * 
     * @return The customer ID, or null if not specified
     */
    public UUID getCustomerId() {
        return customerId;
    }
    
    /**
     * Gets the IDs of the child customers.
     * 
     * @return The list of child customer IDs, or null if not specified
     */
    public List<UUID> getChildIds() {
        return childIds;
    }
    
    /**
     * Gets the operation that was attempted.
     * 
     * @return The operation name, or null if not specified
     */
    public String getOperation() {
        return operation;
    }
}