package de.freshplan.domain.customer.service.exception;

/**
 * Exception thrown when attempting to create a customer that already exists.
 * This exception indicates that a customer with the same identifying 
 * characteristics (e.g., company name) already exists in the system.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CustomerAlreadyExistsException extends RuntimeException {
    
    private final String conflictingField;
    private final String conflictingValue;
    
    /**
     * Creates a CustomerAlreadyExistsException with a message.
     * 
     * @param message The exception message
     */
    public CustomerAlreadyExistsException(String message) {
        super(message);
        this.conflictingField = null;
        this.conflictingValue = null;
    }
    
    /**
     * Creates a CustomerAlreadyExistsException for a specific field conflict.
     * 
     * @param field The field that has a conflict (e.g., "companyName")
     * @param value The conflicting value
     */
    public CustomerAlreadyExistsException(String field, String value) {
        super("Customer already exists with " + field + ": " + value);
        this.conflictingField = field;
        this.conflictingValue = value;
    }
    
    /**
     * Creates a CustomerAlreadyExistsException with a custom message and field info.
     * 
     * @param message The exception message
     * @param field The field that has a conflict
     * @param value The conflicting value
     */
    public CustomerAlreadyExistsException(String message, String field, String value) {
        super(message);
        this.conflictingField = field;
        this.conflictingValue = value;
    }
    
    /**
     * Creates a CustomerAlreadyExistsException with a message and cause.
     * 
     * @param message The exception message
     * @param cause The underlying cause
     */
    public CustomerAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.conflictingField = null;
        this.conflictingValue = null;
    }
    
    /**
     * Gets the field that has a conflict.
     * 
     * @return The conflicting field name, or null if not specified
     */
    public String getConflictingField() {
        return conflictingField;
    }
    
    /**
     * Gets the value that caused the conflict.
     * 
     * @return The conflicting value, or null if not specified
     */
    public String getConflictingValue() {
        return conflictingValue;
    }
}