package de.freshplan.domain.user.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing user.
 * 
 * This class is immutable and includes all updatable user fields.
 * The ID is not included as it's provided via the REST endpoint path.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class UpdateUserRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", 
             message = "Username can only contain letters, numbers, dots, underscores and hyphens")
    private final String username;
    
    @NotBlank(message = "First name is required")
    @Size(max = 60, message = "First name must not exceed 60 characters")
    private final String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 60, message = "Last name must not exceed 60 characters")
    private final String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private final String email;
    
    @NotNull(message = "Enabled status is required")
    private final Boolean enabled;
    
    /**
     * Creates a new user update request.
     * 
     * @param username the username
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email address
     * @param enabled the enabled status
     */
    @JsonCreator
    public UpdateUserRequest(
            @JsonProperty("username") String username,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String email,
            @JsonProperty("enabled") Boolean enabled) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    /**
     * Builder for UpdateUserRequest.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private Boolean enabled;
        
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
        
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public UpdateUserRequest build() {
            return new UpdateUserRequest(username, firstName, lastName, email, enabled);
        }
    }
}