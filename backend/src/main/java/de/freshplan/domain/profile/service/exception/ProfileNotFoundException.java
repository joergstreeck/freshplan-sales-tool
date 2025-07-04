package de.freshplan.domain.profile.service.exception;

import java.util.UUID;

/**
 * Exception thrown when a profile is not found.
 * 
 * @author FreshPlan Team
 * @since 1.0.0
 */
public class ProfileNotFoundException extends RuntimeException {
    
    public ProfileNotFoundException(UUID id) {
        super("Profile not found with ID: " + id);
    }
    
    public ProfileNotFoundException(String customerId) {
        super("Profile not found for customer ID: " + customerId);
    }
}