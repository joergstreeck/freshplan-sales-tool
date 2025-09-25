package de.freshplan.modules.leads.api;

import de.freshplan.modules.leads.domain.LeadStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Request DTO for updating a lead (PATCH operation). All fields are optional. Supports
 * stop-the-clock feature and collaborator management.
 */
public class LeadUpdateRequest {

  @Size(max = 255)
  public String companyName;

  @Size(max = 255)
  public String contactPerson;

  @Email(message = "Invalid email format")
  @Size(max = 255)
  public String email;

  @Size(max = 50)
  public String phone;

  @Size(max = 255)
  public String website;

  @Size(max = 255)
  public String street;

  @Size(max = 20)
  public String postalCode;

  @Size(max = 100)
  public String city;

  // B2B-specific fields
  @Size(max = 100)
  public String businessType;

  @Size(max = 20)
  public String kitchenSize;

  public Integer employeeCount;

  public BigDecimal estimatedVolume;

  // Status management
  public LeadStatus status;

  // Stop-the-clock feature
  public Boolean stopClock;

  @Size(max = 500, message = "Stop reason must not exceed 500 characters")
  public String stopReason;

  // Collaborator management
  public Set<String> addCollaborators;
  public Set<String> removeCollaborators;
}
