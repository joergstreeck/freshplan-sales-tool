package de.freshplan.domain.user.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new user.
 * 
 * This class is immutable and uses Jackson annotations for JSON deserialization.
 * All fields are validated according to business rules.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class CreateUserRequest {
    
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
    
    /**
     * Creates a new user creation request.
     * 
     * @param username the desired username
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     */
    @JsonCreator
    public CreateUserRequest(
            @JsonProperty("username") String username,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String email) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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
    
    /**
     * Builder for CreateUserRequest.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        
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
        
        public CreateUserRequest build() {
            return new CreateUserRequest(username, firstName, lastName, email);
        }
    }
}