package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Data Transfer Object for Contact entity. Used for API communication with validation rules. */
public class ContactDTO {

  private UUID id;
  private UUID customerId;

  // Basic Info
  @Size(max = 20)
  private String salutation;

  @Size(max = 50)
  private String title;

  @NotBlank(message = "First name is required")
  @Size(max = 100)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 100)
  private String lastName;

  @Size(max = 100)
  private String position;

  @Size(max = 50)
  private String decisionLevel;

  // Contact Info
  @Email(message = "Invalid email format")
  @Size(max = 255)
  private String email;

  @Size(max = 50)
  private String phone;

  @Size(max = 50)
  private String mobile;

  // Flags
  private boolean isPrimary;
  private boolean isActive = true;

  // Location
  private UUID assignedLocationId;
  private String assignedLocationName; // For display

  // Relationship Data
  private LocalDate birthday;

  @Size(max = 500)
  private String hobbies;

  @Size(max = 50)
  private String familyStatus;

  private Integer childrenCount;

  private String personalNotes;

  // Audit Info (read-only)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  // Computed fields
  private String fullName;
  private String displayName;

  // Constructors
  public ContactDTO() {
    // Default constructor
  }

  // Builder pattern for easier construction
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

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public String getSalutation() {
    return salutation;
  }

  public void setSalutation(String salutation) {
    this.salutation = salutation;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getDecisionLevel() {
    return decisionLevel;
  }

  public void setDecisionLevel(String decisionLevel) {
    this.decisionLevel = decisionLevel;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public boolean isPrimary() {
    return isPrimary;
  }

  public void setPrimary(boolean primary) {
    isPrimary = primary;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public UUID getAssignedLocationId() {
    return assignedLocationId;
  }

  public void setAssignedLocationId(UUID assignedLocationId) {
    this.assignedLocationId = assignedLocationId;
  }

  public String getAssignedLocationName() {
    return assignedLocationName;
  }

  public void setAssignedLocationName(String assignedLocationName) {
    this.assignedLocationName = assignedLocationName;
  }

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public String getHobbies() {
    return hobbies;
  }

  public void setHobbies(String hobbies) {
    this.hobbies = hobbies;
  }

  public String getFamilyStatus() {
    return familyStatus;
  }

  public void setFamilyStatus(String familyStatus) {
    this.familyStatus = familyStatus;
  }

  public Integer getChildrenCount() {
    return childrenCount;
  }

  public void setChildrenCount(Integer childrenCount) {
    this.childrenCount = childrenCount;
  }

  public String getPersonalNotes() {
    return personalNotes;
  }

  public void setPersonalNotes(String personalNotes) {
    this.personalNotes = personalNotes;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  // Builder class
  public static class Builder {
    private final ContactDTO dto = new ContactDTO();

    public Builder id(UUID id) {
      dto.setId(id);
      return this;
    }

    public Builder customerId(UUID customerId) {
      dto.setCustomerId(customerId);
      return this;
    }

    public Builder firstName(String firstName) {
      dto.setFirstName(firstName);
      return this;
    }

    public Builder lastName(String lastName) {
      dto.setLastName(lastName);
      return this;
    }

    public Builder email(String email) {
      dto.setEmail(email);
      return this;
    }

    public Builder position(String position) {
      dto.setPosition(position);
      return this;
    }

    public Builder isPrimary(boolean isPrimary) {
      dto.setPrimary(isPrimary);
      return this;
    }

    public Builder assignedLocationId(UUID locationId) {
      dto.setAssignedLocationId(locationId);
      return this;
    }

    public ContactDTO build() {
      return dto;
    }
  }
}
