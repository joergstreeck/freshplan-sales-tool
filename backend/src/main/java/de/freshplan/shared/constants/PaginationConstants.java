package de.freshplan.shared.constants;

/**
 * Constants for pagination across the application.
 * 
 * <p>These constants ensure consistent pagination behavior and limits
 * throughout all REST endpoints.
 */
public final class PaginationConstants {
    
    /**
     * Default page size for paginated endpoints.
     * Used when no explicit size parameter is provided.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * Maximum allowed page size to prevent performance issues.
     * Requests with larger sizes will be rejected with a 400 error.
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * Minimum allowed page size.
     * Ensures at least one item per page.
     */
    public static final int MIN_PAGE_SIZE = 1;
    
    /**
     * Default page number (zero-based).
     * Used when no explicit page parameter is provided.
     */
    public static final int DEFAULT_PAGE_NUMBER = 0;
    
    /**
     * String representation of default page size for JAX-RS @DefaultValue.
     */
    public static final String DEFAULT_PAGE_SIZE_STRING = "20";
    
    /**
     * String representation of default page number for JAX-RS @DefaultValue.
     */
    public static final String DEFAULT_PAGE_NUMBER_STRING = "0";
    
    // Private constructor to prevent instantiation
    private PaginationConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}