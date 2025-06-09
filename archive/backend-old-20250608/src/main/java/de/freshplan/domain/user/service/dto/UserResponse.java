package de.freshplan.domain.user.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for user data.
 * 
 * This immutable class represents user information sent to clients.
 * Sensitive information should not be included in this DTO.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class UserResponse {
    
    private final UUID id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean enabled;
    private final List<String> roles;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant updatedAt;
    
    /**
     * Creates a new user response.
     * 
     * @param id the user ID
     * @param username the username
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email address
     * @param enabled the enabled status
     * @param roles the user's roles
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     */
    public UserResponse(
            UUID id,
            String username,
            String firstName,
            String lastName,
            String email,
            boolean enabled,
            List<String> roles,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    @JsonProperty("id")
    public UUID getId() {
        return id;
    }
    
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }
    
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }
    
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }
    
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }
    
    @JsonProperty("enabled")
    public boolean isEnabled() {
        return enabled;
    }
    
    @JsonProperty("fullName")
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @JsonProperty("roles")
    public List<String> getRoles() {
        return roles;
    }
    
    @JsonProperty("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    @JsonProperty("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Builder for UserResponse.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private boolean enabled;
        private List<String> roles;
        private Instant createdAt;
        private Instant updatedAt;
        
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }
        
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public UserResponse build() {
            return new UserResponse(id, username, firstName, lastName, email, 
                                  enabled, roles, createdAt, updatedAt);
        }
    }
}