package de.freshplan.domain.user.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Collections;
import java.util.List;

/**
 * Request DTO for updating user roles.
 * 
 * This class validates that roles are provided and contain only
 * allowed values: admin, manager, sales, viewer.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class UpdateUserRolesRequest {
    
    @NotNull(message = "Roles list cannot be null")
    @Size(min = 1, message = "At least one role must be specified")
    private final List<String> roles;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public UpdateUserRolesRequest() {
        this.roles = Collections.emptyList();
    }
    
    /**
     * Creates a new request with the given roles.
     * 
     * @param roles the list of roles to assign
     */
    public UpdateUserRolesRequest(List<String> roles) {
        this.roles = roles != null ? List.copyOf(roles) : Collections.emptyList();
    }
    
    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }
    
    /**
     * Builder for UpdateUserRolesRequest.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<String> roles;
        
        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }
        
        public UpdateUserRolesRequest build() {
            return new UpdateUserRolesRequest(roles);
        }
    }
}