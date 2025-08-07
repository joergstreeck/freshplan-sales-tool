package de.freshplan.domain.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

// import org.hibernate.envers.Audited; // TODO: Add Hibernate Envers dependency

/**
 * Contact entity representing a person associated with a customer. Supports multiple contacts per
 * customer with primary/secondary designation, location assignment, and relationship data for sales
 * excellence.
 */
@Entity
@Table(
    name = "contacts",
    indexes = {
      @Index(name = "idx_contact_customer", columnList = "customer_id"),
      @Index(name = "idx_contact_location", columnList = "assigned_location_id"),
      @Index(name = "idx_contact_active", columnList = "is_active")
    })
// @Audited // TODO: Aktiviert Hibernate Envers für Audit Trail - dependency fehlt noch
public class Contact extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Relationship
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  // Basic Info
  @Column(name = "salutation", length = 20)
  private String salutation; // herr, frau, divers

  @Column(name = "title", length = 50)
  private String title; // Dr., Prof., etc.

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "position", length = 100)
  private String position;

  @Column(name = "decision_level", length = 50)
  private String decisionLevel; // executive, manager, operational, influencer

  // Contact Info
  @Column(name = "email", length = 255)
  private String email;

  @Column(name = "phone", length = 50)
  private String phone;

  @Column(name = "mobile", length = 50)
  private String mobile;

  // Flags
  @Column(name = "is_primary", nullable = false)
  private boolean isPrimary = false;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  // Location Assignment
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_location_id")
  private CustomerLocation assignedLocation;

  // Relationship Data (Beziehungsebene)
  @Column(name = "birthday")
  private LocalDate birthday;

  @Column(name = "hobbies", length = 500)
  private String hobbies; // Comma-separated list

  @Column(name = "family_status", length = 50)
  private String familyStatus; // single, married, divorced, widowed

  @Column(name = "children_count")
  private Integer childrenCount;

  @Column(name = "personal_notes", columnDefinition = "TEXT")
  private String personalNotes;

  // Intelligence Data (added for Data Strategy Intelligence)
  @Column(name = "warmth_score")
  private Integer warmthScore = 50; // 0-100, default neutral

  @Column(name = "warmth_confidence")
  private Integer warmthConfidence = 0; // 0-100, confidence in warmth score

  @Column(name = "last_interaction_date")
  private LocalDateTime lastInteractionDate;

  @Column(name = "interaction_count")
  private Integer interactionCount = 0;

  // Data Quality & Freshness (added for Data Freshness Tracking)
  @Column(name = "data_quality_score")
  private Integer dataQualityScore; // 0-100, overall data quality score

  @Column(name = "data_quality_recommendations", columnDefinition = "TEXT")
  private String dataQualityRecommendations; // Semicolon-separated recommendations

  // Audit Fields
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "created_by", length = 100)
  private String createdBy;

  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  // Constructors
  public Contact() {
    // Default constructor for JPA
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
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

  public CustomerLocation getAssignedLocation() {
    return assignedLocation;
  }

  public void setAssignedLocation(CustomerLocation assignedLocation) {
    this.assignedLocation = assignedLocation;
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

  // Business Methods
  public String getFullName() {
    StringBuilder sb = new StringBuilder();
    if (title != null && !title.isBlank()) {
      sb.append(title).append(" ");
    }
    sb.append(firstName).append(" ").append(lastName);
    return sb.toString();
  }

  public String getDisplayName() {
    StringBuilder sb = new StringBuilder();
    if (salutation != null && !salutation.isBlank()) {
      sb.append(getSalutationDisplay()).append(" ");
    }
    sb.append(getFullName());
    return sb.toString();
  }

  private String getSalutationDisplay() {
    if (salutation == null) return "";
    return switch (salutation.toLowerCase()) {
      case "herr" -> "Herr";
      case "frau" -> "Frau";
      case "divers" -> "";
      default -> salutation;
    };
  }

  // Builder pattern for easier construction
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final Contact contact = new Contact();

    public Builder customer(Customer customer) {
      contact.setCustomer(customer);
      return this;
    }

    public Builder firstName(String firstName) {
      contact.setFirstName(firstName);
      return this;
    }

    public Builder lastName(String lastName) {
      contact.setLastName(lastName);
      return this;
    }

    public Builder email(String email) {
      contact.setEmail(email);
      return this;
    }

    public Builder position(String position) {
      contact.setPosition(position);
      return this;
    }

    public Builder isPrimary(boolean isPrimary) {
      contact.setPrimary(isPrimary);
      return this;
    }

    public Builder assignedLocation(CustomerLocation location) {
      contact.setAssignedLocation(location);
      return this;
    }

    public Contact build() {
      return contact;
    }
  }
}
