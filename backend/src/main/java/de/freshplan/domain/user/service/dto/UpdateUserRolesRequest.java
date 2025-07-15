package de.freshplan.domain.user.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for updating user roles.
 *
 * <p>This class validates that roles are provided and contain only allowed values: admin, manager,
 * sales.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class UpdateUserRolesRequest {

  @NotNull(message = "Roles list cannot be null") @Size(min = 1, message = "At least one role must be specified")
  private final List<String> roles;

  /**
   * Creates a new request with the given roles. Used by Jackson for JSON deserialization.
   *
   * @param roles the list of roles to assign
   */
  @JsonCreator
  public UpdateUserRolesRequest(@JsonProperty("roles") List<String> roles) {
    this.roles = roles != null ? List.copyOf(roles) : null;
  }

  public List<String> getRoles() {
    return roles;
  }

  /** Builder for UpdateUserRolesRequest. */
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
