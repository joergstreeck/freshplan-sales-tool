package de.freshplan.user.dto;

import de.freshplan.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for updating an existing user.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class UpdateUserRequest {
    
    private UUID id;
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private Boolean enabled;
    
    private Set<UserRole> roles;
    
    // Constructors
    public UpdateUserRequest() {
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
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Set<UserRole> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }
}