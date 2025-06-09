package de.freshplan.user.dto;

import de.freshplan.user.UserRole;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for User data.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class UserResponse {
    
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private Set<UserRole> roles;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Constructors
    public UserResponse() {
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<UserRole> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Builder
    public static class Builder {
        private final UserResponse response = new UserResponse();
        
        public Builder id(UUID id) {
            response.id = id;
            return this;
        }
        
        public Builder username(String username) {
            response.username = username;
            return this;
        }
        
        public Builder email(String email) {
            response.email = email;
            return this;
        }
        
        public Builder firstName(String firstName) {
            response.firstName = firstName;
            return this;
        }
        
        public Builder lastName(String lastName) {
            response.lastName = lastName;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            response.enabled = enabled;
            return this;
        }
        
        public Builder roles(Set<UserRole> roles) {
            response.roles = roles;
            return this;
        }
        
        public Builder createdAt(Instant createdAt) {
            response.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }
        
        public UserResponse build() {
            return response;
        }
    }
}