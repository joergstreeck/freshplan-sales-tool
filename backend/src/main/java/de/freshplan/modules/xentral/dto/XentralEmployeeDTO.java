package de.freshplan.modules.xentral.dto;

/**
 * Xentral Employee DTO
 *
 * <p>Sprint 2.1.7.2: Xentral Integration (Sales Rep Mapping)
 *
 * <p>Represents an employee (sales rep) from Xentral ERP system (Neue Xentral API v25.39+)
 *
 * <p>Mapped from JSON:API response: GET /api/employees?filter[role]=sales
 *
 * @param employeeId Employee ID in Xentral
 * @param firstName First name
 * @param lastName Last name
 * @param email Email address (used for matching with FreshPlan users)
 * @param role Employee role (e.g., "sales", "admin")
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record XentralEmployeeDTO(
    String employeeId, String firstName, String lastName, String email, String role) {

  /**
   * Validates the DTO
   *
   * @throws IllegalArgumentException if required fields are missing
   */
  public void validate() {
    if (employeeId == null || employeeId.isBlank()) {
      throw new IllegalArgumentException("employeeId cannot be null or blank");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email cannot be null or blank");
    }
  }

  /**
   * Gets full name
   *
   * @return first name + last name
   */
  public String getFullName() {
    return firstName + " " + lastName;
  }
}
