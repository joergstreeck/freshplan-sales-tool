package de.freshplan.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for User entity.
 * Used for API responses to avoid exposing entity internals.
 */
public class UserDTO {
    public UUID id;
    public String username;
    public String firstName;
    public String lastName;
    public String email;
    public boolean enabled;
    public Instant createdAt;
    public Instant updatedAt;
    
    // Default constructor for JSON serialization
    public UserDTO() {}
    
    // Constructor for entity conversion
    public UserDTO(UUID id, String username, String firstName, String lastName, 
                   String email, boolean enabled, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}