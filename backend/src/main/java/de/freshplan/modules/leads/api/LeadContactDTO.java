package de.freshplan.modules.leads.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for LeadContact entity - 100% PARITY mit ContactDTO.
 *
 * <p>Used for API communication with validation rules. Supports all fields from LeadContact entity
 * including CRM Intelligence fields (warmth_score, data_quality_score, relationship data).
 *
 * <p><b>Architektur-Entscheidung:</b> ADR-007 Option C (100% Parity with ContactDTO)
 *
 * @see de.freshplan.modules.leads.domain.LeadContact LeadContact Entity
 * @see de.freshplan.domain.customer.service.dto.ContactDTO ContactDTO (Customer Module) - Identical
 *     structure
 */
public class LeadContactDTO {

  private UUID id;
  private Long leadId; // Note: LeadContact uses Long for lead_id (FK to leads.id)

  // ===========================
  // Basic Info (IDENTICAL to ContactDTO lines 16-35)
  // ===========================

  @Size(max = 20)
  private String salutation; // herr, frau, divers

  @Size(max = 50)
  private String title; // Dr., Prof., etc.

  // NOTE: @NotBlank removed - Response DTOs should not validate (Pre-Claim leads have empty
  // contacts)
  // Validation happens in Request DTOs (LeadCreateRequest.ContactData)
  @Size(max = 100)
  private String firstName;

  @Size(max = 100)
  private String lastName;

  @Size(max = 100)
  private String position;

  @Size(max = 50)
  private String decisionLevel; // executive, manager, operational, influencer

  // ===========================
  // Contact Info (IDENTICAL to ContactDTO lines 37-46)
  // ===========================

  @Email(message = "Invalid email format")
  @Size(max = 255)
  private String email;

  @Size(max = 50)
  private String phone;

  @Size(max = 50)
  private String mobile;

  // ===========================
  // Flags (IDENTICAL to ContactDTO lines 48-50)
  // ===========================

  private boolean isPrimary;
  private boolean isActive = true;

  // ===========================
  // Relationship Data - CRM Intelligence (IDENTICAL to ContactDTO lines 56-67)
  // ===========================

  private LocalDate birthday;

  @Size(max = 500)
  private String hobbies; // Comma-separated list

  @Size(max = 50)
  private String familyStatus; // single, married, divorced, widowed

  private Integer childrenCount;

  private String personalNotes;

  // ===========================
  // Intelligence Data - Sales Excellence (NEW - from LeadContact)
  // ===========================

  private Integer warmthScore; // 0-100, default neutral
  private Integer warmthConfidence; // 0-100, confidence in warmth score
  private LocalDateTime lastInteractionDate;
  private Integer interactionCount;

  // ===========================
  // Data Quality & Freshness (NEW - from LeadContact)
  // ===========================

  private Integer dataQualityScore; // 0-100, overall data quality score
  private String dataQualityRecommendations; // Semicolon-separated recommendations

  // ===========================
  // Legacy Compatibility (NEW - from LeadContact)
  // ===========================

  private Boolean isDecisionMaker;
  private Boolean isDeleted;

  // ===========================
  // Audit Info (IDENTICAL to ContactDTO lines 69-73)
  // ===========================

  private LocalDateTime createdAt; // Read-only
  private LocalDateTime updatedAt; // Read-only
  private String createdBy; // Read-only
  private String updatedBy; // Read-only

  // ===========================
  // Computed Fields (IDENTICAL to ContactDTO lines 75-77)
  // ===========================

  private String fullName; // Computed: "Dr. Max Mustermann"
  private String displayName; // Computed: "Herr Dr. Max Mustermann"

  // ===========================
  // Constructors
  // ===========================

  public LeadContactDTO() {
    // Default constructor
  }

  // ===========================
  // Factory Methods
  // ===========================

  /**
   * Create DTO from LeadContact entity.
   *
   * <p>Centralizes mapping logic to avoid duplication in LeadResource.
   *
   * @param contact LeadContact entity
   * @return LeadContactDTO
   */
  public static LeadContactDTO fromEntity(de.freshplan.modules.leads.domain.LeadContact contact) {
    if (contact == null) {
      return null;
    }

    LeadContactDTO dto = new LeadContactDTO();
    dto.setId(contact.getId());
    dto.setLeadId(contact.getLead().id);
    dto.setFirstName(contact.getFirstName());
    dto.setLastName(contact.getLastName());
    dto.setSalutation(contact.getSalutation());
    dto.setTitle(contact.getTitle());
    dto.setPosition(contact.getPosition());
    dto.setDecisionLevel(contact.getDecisionLevel());
    dto.setEmail(contact.getEmail());
    dto.setPhone(contact.getPhone());
    dto.setMobile(contact.getMobile());
    dto.setPrimary(contact.isPrimary());
    dto.setActive(contact.isActive());
    dto.setBirthday(contact.getBirthday());
    dto.setHobbies(contact.getHobbies());
    dto.setFamilyStatus(contact.getFamilyStatus());
    dto.setChildrenCount(contact.getChildrenCount());
    dto.setPersonalNotes(contact.getPersonalNotes());
    dto.setWarmthScore(contact.getWarmthScore());
    dto.setWarmthConfidence(contact.getWarmthConfidence());
    dto.setLastInteractionDate(contact.getLastInteractionDate());
    dto.setInteractionCount(contact.getInteractionCount());
    dto.setDataQualityScore(contact.getDataQualityScore());
    dto.setDataQualityRecommendations(contact.getDataQualityRecommendations());
    dto.setIsDecisionMaker(contact.getIsDecisionMaker());
    dto.setIsDeleted(contact.getIsDeleted());
    dto.setCreatedAt(contact.getCreatedAt());
    dto.setUpdatedAt(contact.getUpdatedAt());
    dto.setCreatedBy(contact.getCreatedBy());
    dto.setUpdatedBy(contact.getUpdatedBy());
    dto.setFullName(contact.getFullName());
    dto.setDisplayName(contact.getDisplayName());
    return dto;
  }

  // ===========================
  // Builder Pattern
  // ===========================

  public static Builder builder() {
    return new Builder();
  }

  // ===========================
  // Getters and Setters
  // ===========================

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Long getLeadId() {
    return leadId;
  }

  public void setLeadId(Long leadId) {
    this.leadId = leadId;
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

  public Integer getWarmthScore() {
    return warmthScore;
  }

  public void setWarmthScore(Integer warmthScore) {
    this.warmthScore = warmthScore;
  }

  public Integer getWarmthConfidence() {
    return warmthConfidence;
  }

  public void setWarmthConfidence(Integer warmthConfidence) {
    this.warmthConfidence = warmthConfidence;
  }

  public LocalDateTime getLastInteractionDate() {
    return lastInteractionDate;
  }

  public void setLastInteractionDate(LocalDateTime lastInteractionDate) {
    this.lastInteractionDate = lastInteractionDate;
  }

  public Integer getInteractionCount() {
    return interactionCount;
  }

  public void setInteractionCount(Integer interactionCount) {
    this.interactionCount = interactionCount;
  }

  public Integer getDataQualityScore() {
    return dataQualityScore;
  }

  public void setDataQualityScore(Integer dataQualityScore) {
    this.dataQualityScore = dataQualityScore;
  }

  public String getDataQualityRecommendations() {
    return dataQualityRecommendations;
  }

  public void setDataQualityRecommendations(String dataQualityRecommendations) {
    this.dataQualityRecommendations = dataQualityRecommendations;
  }

  public Boolean getIsDecisionMaker() {
    return isDecisionMaker;
  }

  public void setIsDecisionMaker(Boolean isDecisionMaker) {
    this.isDecisionMaker = isDecisionMaker;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
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

  // ===========================
  // Builder Class (IDENTICAL pattern to ContactDTO lines 298-345)
  // ===========================

  public static class Builder {
    private final LeadContactDTO dto = new LeadContactDTO();

    public Builder id(UUID id) {
      dto.setId(id);
      return this;
    }

    public Builder leadId(Long leadId) {
      dto.setLeadId(leadId);
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

    public LeadContactDTO build() {
      return dto;
    }
  }
}
