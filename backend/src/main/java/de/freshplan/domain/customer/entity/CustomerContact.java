package de.freshplan.domain.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * Customer contact entity representing individual contacts within a customer organization. Supports
 * role-based access, hierarchy, and comprehensive contact information.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(
    name = "customer_contacts",
    indexes = {
      @Index(name = "idx_contact_customer", columnList = "customer_id"),
      @Index(name = "idx_contact_email", columnList = "email"),
      @Index(name = "idx_contact_primary", columnList = "customer_id, is_primary"),
      @Index(name = "idx_contact_deleted", columnList = "is_deleted")
    })
public class CustomerContact extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  // Basic Contact Information
  @Column(name = "salutation", length = 20)
  private String salutation; // herr, frau, divers

  @Column(name = "title", length = 50)
  private String title; // Dr., Prof., etc.

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "position", length = 150)
  private String position;

  @Column(name = "department", length = 100)
  private String department;

  @Column(name = "decision_level", length = 50)
  private String decisionLevel; // executive, manager, operational, influencer

  // Contact Details
  @Column(name = "email", length = 255)
  private String email;

  @Column(name = "phone", length = 50)
  private String phone;

  @Column(name = "mobile", length = 50)
  private String mobile;

  @Column(name = "fax", length = 50)
  private String fax;

  /**
   * Role System - Temporary implementation using String-based roles. TODO: Will be replaced with
   * proper role management in future iteration.
   *
   * @since 2.0.0
   */
  @Transient private Set<String> roles = new HashSet<>();

  /**
   * Hierarchical relationship - reference to the contact's supervisor. Supports organizational
   * hierarchy mapping.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reports_to_id")
  private CustomerContact reportsTo;

  @OneToMany(mappedBy = "reportsTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CustomerContact> directReports = new ArrayList<>();

  /**
   * Indicates if this is the primary contact for the customer. Only one contact per customer should
   * be marked as primary.
   */
  @Column(name = "is_primary", nullable = false)
  private Boolean isPrimary = false;

  @Column(name = "is_decision_maker", nullable = false)
  private Boolean isDecisionMaker = false;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  /** Preferred communication method for this contact. Defaults to EMAIL if not specified. */
  @Column(name = "preferred_communication_method", length = 20)
  @Enumerated(EnumType.STRING)
  private CommunicationMethod preferredCommunicationMethod = CommunicationMethod.EMAIL;

  @Column(name = "language_preference", length = 10)
  private String languagePreference = "DE";

  // Location Assignment
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_location_id")
  private CustomerLocation assignedLocation;

  // Personal Information (for relationship building)
  @Column(name = "birthday")
  private java.time.LocalDate birthday;

  @Column(name = "hobbies", length = 500)
  private String hobbies; // Comma-separated list

  // V2 Fields (Sprint 2.1.7.2 D11.1 - Contact Management)
  @Column(name = "linkedin", length = 500)
  private String linkedin;

  @Column(name = "xing", length = 500)
  private String xing;

  @Column(name = "family_status", length = 50)
  private String familyStatus; // single, married, divorced, widowed

  @Column(name = "children_count")
  private Integer childrenCount;

  @Column(name = "personal_notes", columnDefinition = "TEXT")
  private String personalNotes;

  // Contact Metadata
  @Column(name = "last_contact_date")
  private LocalDateTime lastContactDate;

  @Column(name = "last_interaction_date")
  private LocalDateTime lastInteractionDate;

  @Column(name = "interaction_count")
  private Integer interactionCount = 0;

  @Column(name = "warmth_score")
  private Integer warmthScore = 50;

  @Column(name = "warmth_confidence")
  private Integer warmthConfidence = 0;

  @Column(name = "data_quality_recommendations", columnDefinition = "TEXT")
  private String dataQualityRecommendations;

  @Column(name = "data_quality_score")
  private Integer dataQualityScore;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  // Soft Delete
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  // Audit Fields
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  // Lifecycle Methods
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now(); // Set updatedAt on creation as well
    if (isDeleted == null) {
      isDeleted = false;
    }
    if (isActive == null) {
      isActive = true;
    }
    if (isPrimary == null) {
      isPrimary = false;
    }
    if (isDecisionMaker == null) {
      isDecisionMaker = false;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();

    // Auto-update decision maker status based on roles
    updateDecisionMakerStatus();
  }

  // Business Methods

  /** Gets the full name of the contact. */
  public String getFullName() {
    if (firstName != null && lastName != null) {
      return firstName + " " + lastName;
    } else if (firstName != null) {
      return firstName;
    } else if (lastName != null) {
      return lastName;
    }
    return "";
  }

  /**
   * Adds a role to this contact. Automatically updates decision maker status if applicable.
   *
   * @param roleName the name of the role to add (case-insensitive)
   */
  public void addRole(String roleName) {
    if (roleName != null && !roleName.isBlank()) {
      this.roles.add(roleName);
      updateDecisionMakerStatus();
    }
  }

  /**
   * Removes a role from this contact. Automatically updates decision maker status after removal.
   *
   * @param roleName the name of the role to remove
   */
  public void removeRole(String roleName) {
    if (roleName != null) {
      this.roles.remove(roleName);
      updateDecisionMakerStatus();
    }
  }

  /**
   * Checks if contact has a specific role. Comparison is case-insensitive.
   *
   * @param roleName the role name to check
   * @return true if the contact has the specified role, false otherwise
   */
  public boolean hasRole(String roleName) {
    return roles.stream().anyMatch(role -> role.equalsIgnoreCase(roleName));
  }

  // Decision maker role constants for better maintainability
  private static final Set<String> DECISION_MAKER_ROLES =
      Set.of("DECISION_MAKER", "GESCHÄFTSFÜHRER", "CEO");

  /**
   * Updates decision maker status based on current roles. Checks for DECISION_MAKER,
   * GESCHÄFTSFÜHRER, or CEO roles. This method is called automatically when roles change.
   */
  private void updateDecisionMakerStatus() {
    this.isDecisionMaker =
        roles.stream().map(String::toUpperCase).anyMatch(DECISION_MAKER_ROLES::contains);
  }

  /**
   * Checks if this contact is a subordinate of another contact. Traverses the hierarchy chain to
   * check both direct and indirect relationships. Prevents infinite loops in case of circular
   * references.
   *
   * @param potentialSuperior the contact to check against
   * @return true if this contact reports to the potential superior (directly or indirectly)
   */
  public boolean isSubordinateOf(CustomerContact potentialSuperior) {
    Set<UUID> visited = new HashSet<>();
    CustomerContact current = this.reportsTo;

    while (current != null) {
      // Prevent infinite loop in case of circular references
      if (!visited.add(current.getId())) {
        return false; // Circular reference detected
      }

      if (current.getId().equals(potentialSuperior.getId())) {
        return true;
      }
      current = current.getReportsTo();
    }
    return false;
  }

  /**
   * Gets all subordinates (direct and indirect) of this contact. Recursively traverses the
   * hierarchy to find all reports. Prevents infinite loops in case of circular references.
   *
   * @return list of all subordinate contacts
   */
  public List<CustomerContact> getAllSubordinates() {
    return getAllSubordinates(new HashSet<>());
  }

  /**
   * Internal helper method to get all subordinates with circular reference protection.
   *
   * @param visited set of already visited contact IDs to prevent infinite recursion
   * @return list of all subordinate contacts
   */
  private List<CustomerContact> getAllSubordinates(Set<UUID> visited) {
    List<CustomerContact> allSubordinates = new ArrayList<>();

    // Add current contact ID to visited set
    if (this.id != null && !visited.add(this.id)) {
      return allSubordinates; // Already visited, prevent circular reference
    }

    for (CustomerContact directReport : directReports) {
      if (!Boolean.TRUE.equals(directReport.getIsDeleted())) {
        allSubordinates.add(directReport);
        allSubordinates.addAll(directReport.getAllSubordinates(visited));
      }
    }
    return allSubordinates;
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

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
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

  public String getFax() {
    return fax;
  }

  public void setFax(String fax) {
    this.fax = fax;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
    updateDecisionMakerStatus();
  }

  public CustomerContact getReportsTo() {
    return reportsTo;
  }

  public void setReportsTo(CustomerContact reportsTo) {
    this.reportsTo = reportsTo;
  }

  public List<CustomerContact> getDirectReports() {
    return directReports;
  }

  public void setDirectReports(List<CustomerContact> directReports) {
    this.directReports = directReports;
  }

  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public void setIsPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  public Boolean getIsDecisionMaker() {
    return isDecisionMaker;
  }

  public void setIsDecisionMaker(Boolean isDecisionMaker) {
    this.isDecisionMaker = isDecisionMaker;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public CommunicationMethod getPreferredCommunicationMethod() {
    return preferredCommunicationMethod;
  }

  public void setPreferredCommunicationMethod(CommunicationMethod preferredCommunicationMethod) {
    this.preferredCommunicationMethod = preferredCommunicationMethod;
  }

  public String getLanguagePreference() {
    return languagePreference;
  }

  public void setLanguagePreference(String languagePreference) {
    this.languagePreference = languagePreference;
  }

  public LocalDateTime getLastContactDate() {
    return lastContactDate;
  }

  public void setLastContactDate(LocalDateTime lastContactDate) {
    this.lastContactDate = lastContactDate;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public String getDeletedBy() {
    return deletedBy;
  }

  public void setDeletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  // New getters and setters for added fields
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

  public String getDecisionLevel() {
    return decisionLevel;
  }

  public void setDecisionLevel(String decisionLevel) {
    this.decisionLevel = decisionLevel;
  }

  public CustomerLocation getAssignedLocation() {
    return assignedLocation;
  }

  public void setAssignedLocation(CustomerLocation assignedLocation) {
    this.assignedLocation = assignedLocation;
  }

  public java.time.LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(java.time.LocalDate birthday) {
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

  public String getLinkedin() {
    return linkedin;
  }

  public void setLinkedin(String linkedin) {
    this.linkedin = linkedin;
  }

  public String getXing() {
    return xing;
  }

  public void setXing(String xing) {
    this.xing = xing;
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

  public String getDataQualityRecommendations() {
    return dataQualityRecommendations;
  }

  public void setDataQualityRecommendations(String dataQualityRecommendations) {
    this.dataQualityRecommendations = dataQualityRecommendations;
  }

  public Integer getDataQualityScore() {
    return dataQualityScore;
  }

  public void setDataQualityScore(Integer dataQualityScore) {
    this.dataQualityScore = dataQualityScore;
  }

  public String getDisplayName() {
    return firstName + " " + lastName;
  }

  @Override
  public String toString() {
    return "CustomerContact{"
        + "id="
        + id
        + ", firstName='"
        + firstName
        + '\''
        + ", lastName='"
        + lastName
        + '\''
        + ", position='"
        + position
        + '\''
        + ", email='"
        + email
        + '\''
        + ", isPrimary="
        + isPrimary
        + ", isDecisionMaker="
        + isDecisionMaker
        + '}';
  }
}
