package de.freshplan.domain.customer.entity;

import de.freshplan.domain.customer.constants.CustomerConstants;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * Customer entity representing a business customer with full CRM capabilities. Supports soft
 * delete, hierarchies, and comprehensive business data.
 */
@Entity
@Table(
    name = "customers",
    indexes = {
      @Index(name = "idx_customer_number", columnList = "customer_number", unique = true),
      @Index(name = "idx_customer_status", columnList = "status"),
      @Index(name = "idx_customer_deleted", columnList = "is_deleted")
    })
public class Customer extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // Basic Information
  @Column(name = "customer_number", unique = true, nullable = false, length = 20)
  private String customerNumber;

  @Column(name = "company_name", nullable = false, length = 255)
  private String companyName;

  @Column(name = "trading_name", length = 255)
  private String tradingName;

  @Column(name = "legal_form", length = 100)
  private String legalForm;

  // Classification
  @Enumerated(EnumType.STRING)
  @Column(name = "customer_type", nullable = false, length = 20)
  private CustomerType customerType = CustomerType.UNTERNEHMEN;

  @Enumerated(EnumType.STRING)
  @Column(name = "industry", length = 30)
  private Industry industry;

  @Enumerated(EnumType.STRING)
  @Column(name = "classification", length = 10)
  private Classification classification;

  // Hierarchy Support
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_customer_id")
  private Customer parentCustomer;

  @OneToMany(mappedBy = "parentCustomer", cascade = CascadeType.ALL)
  private List<Customer> childCustomers = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(name = "hierarchy_type", length = 20)
  private CustomerHierarchyType hierarchyType = CustomerHierarchyType.STANDALONE;

  // Status & Lifecycle
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private CustomerStatus status = CustomerStatus.LEAD;

  @Enumerated(EnumType.STRING)
  @Column(name = "lifecycle_stage", length = 20)
  private CustomerLifecycleStage lifecycleStage = CustomerLifecycleStage.ACQUISITION;

  // Partner Status (for future partner management)
  @Enumerated(EnumType.STRING)
  @Column(name = "partner_status", length = 20)
  private PartnerStatus partnerStatus = PartnerStatus.KEIN_PARTNER;

  // Financial Information
  @Column(name = "expected_annual_volume", precision = 12, scale = 2)
  private BigDecimal expectedAnnualVolume;

  @Column(name = "actual_annual_volume", precision = 12, scale = 2)
  private BigDecimal actualAnnualVolume;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_terms", length = 20)
  private PaymentTerms paymentTerms = PaymentTerms.NETTO_30;

  @Column(name = "credit_limit", precision = 12, scale = 2)
  private BigDecimal creditLimit;

  @Enumerated(EnumType.STRING)
  @Column(name = "delivery_condition", length = 30)
  private DeliveryCondition deliveryCondition = DeliveryCondition.STANDARD;

  // Risk Management
  @Column(name = "risk_score")
  private Integer riskScore = 0;

  @Column(name = "last_contact_date")
  private LocalDateTime lastContactDate;

  @Column(name = "next_follow_up_date")
  private LocalDateTime nextFollowUpDate;

  /**
   * Flag to identify test data that can be safely cleaned up. Used for controlled test scenarios
   * and data seeding. When true, this customer can be deleted by clean-test-data scripts.
   */
  @Column(name = "is_test_data", nullable = false)
  private Boolean isTestData = false;

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

  @Column(name = "updated_at", nullable = true)
  private LocalDateTime updatedAt;

  @Column(name = "updated_by", length = 100, nullable = true)
  private String updatedBy;

  // Additional audit fields (added by Hibernate automatically)
  @Column(name = "last_modified_at", nullable = true)
  private LocalDateTime lastModifiedAt;

  @Column(name = "last_modified_by", length = 100, nullable = true)
  private String lastModifiedBy;

  // Relationships
  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CustomerContact> contacts = new ArrayList<>();

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CustomerLocation> locations = new ArrayList<>();

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CustomerTimelineEvent> timelineEvents = new ArrayList<>();

  // Lifecycle Methods
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (isDeleted == null) {
      isDeleted = false;
    }
    // Set initial values for last_modified fields
    lastModifiedAt = createdAt;
    if (createdBy != null) {
      lastModifiedBy = createdBy;
    } else {
      lastModifiedBy = "system"; // Fallback for tests
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
    // Update last_modified fields
    lastModifiedAt = updatedAt;
    if (updatedBy != null) {
      lastModifiedBy = updatedBy;
    } else if (lastModifiedBy == null) {
      lastModifiedBy = "system"; // Fallback for tests
    }
  }

  // Business Methods
  public void updateRiskScore() {
    int score = 0;

    // Base score by status
    score +=
        switch (this.status) {
          case LEAD -> CustomerConstants.RISK_SCORE_STATUS_LEAD;
          case PROSPECT -> CustomerConstants.RISK_SCORE_STATUS_PROSPECT;
          case AKTIV -> CustomerConstants.RISK_SCORE_STATUS_AKTIV;
          case RISIKO -> CustomerConstants.RISK_SCORE_STATUS_RISIKO;
          case INAKTIV -> CustomerConstants.RISK_SCORE_STATUS_INAKTIV;
          case ARCHIVIERT -> CustomerConstants.RISK_SCORE_STATUS_ARCHIVIERT;
        };

    // Last contact scoring
    if (lastContactDate != null) {
      long daysSinceContact =
          java.time.temporal.ChronoUnit.DAYS.between(
              lastContactDate.toLocalDate(), java.time.LocalDate.now());
      if (daysSinceContact > CustomerConstants.DAYS_UNTIL_RISK) {
        score += CustomerConstants.RISK_SCORE_LONG_NO_CONTACT_PENALTY;
      }
      if (daysSinceContact > CustomerConstants.DAYS_UNTIL_INACTIVE) {
        score += CustomerConstants.RISK_SCORE_LONG_NO_CONTACT_PENALTY;
      }
    } else {
      score += CustomerConstants.RISK_SCORE_NO_CONTACT_PENALTY; // Never contacted
    }

    // Overdue follow-up
    if (nextFollowUpDate != null && nextFollowUpDate.isBefore(LocalDateTime.now())) {
      score += CustomerConstants.RISK_SCORE_OVERDUE_FOLLOWUP_PENALTY;
    }

    this.riskScore = Math.min(score, CustomerConstants.RISK_SCORE_MAX);
  }

  public boolean isAtRisk() {
    return lastContactDate != null
        && lastContactDate.isBefore(
            LocalDateTime.now().minusDays(CustomerConstants.DAYS_UNTIL_RISK));
  }

  public boolean hasChildren() {
    return childCustomers != null && !childCustomers.isEmpty();
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCustomerNumber() {
    return customerNumber;
  }

  public void setCustomerNumber(String customerNumber) {
    this.customerNumber = customerNumber;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getTradingName() {
    return tradingName;
  }

  public void setTradingName(String tradingName) {
    this.tradingName = tradingName;
  }

  public String getLegalForm() {
    return legalForm;
  }

  public void setLegalForm(String legalForm) {
    this.legalForm = legalForm;
  }

  public CustomerType getCustomerType() {
    return customerType;
  }

  public void setCustomerType(CustomerType customerType) {
    this.customerType = customerType;
  }

  public Industry getIndustry() {
    return industry;
  }

  public void setIndustry(Industry industry) {
    this.industry = industry;
  }

  public Classification getClassification() {
    return classification;
  }

  public void setClassification(Classification classification) {
    this.classification = classification;
  }

  public Customer getParentCustomer() {
    return parentCustomer;
  }

  public void setParentCustomer(Customer parentCustomer) {
    this.parentCustomer = parentCustomer;
  }

  public List<Customer> getChildCustomers() {
    return childCustomers;
  }

  public void setChildCustomers(List<Customer> childCustomers) {
    this.childCustomers = childCustomers;
  }

  public CustomerHierarchyType getHierarchyType() {
    return hierarchyType;
  }

  public void setHierarchyType(CustomerHierarchyType hierarchyType) {
    this.hierarchyType = hierarchyType;
  }

  public CustomerStatus getStatus() {
    return status;
  }

  public void setStatus(CustomerStatus status) {
    this.status = status;
  }

  public CustomerLifecycleStage getLifecycleStage() {
    return lifecycleStage;
  }

  public void setLifecycleStage(CustomerLifecycleStage lifecycleStage) {
    this.lifecycleStage = lifecycleStage;
  }

  public PartnerStatus getPartnerStatus() {
    return partnerStatus;
  }

  public void setPartnerStatus(PartnerStatus partnerStatus) {
    this.partnerStatus = partnerStatus;
  }

  public BigDecimal getExpectedAnnualVolume() {
    return expectedAnnualVolume;
  }

  public void setExpectedAnnualVolume(BigDecimal expectedAnnualVolume) {
    this.expectedAnnualVolume = expectedAnnualVolume;
  }

  public BigDecimal getActualAnnualVolume() {
    return actualAnnualVolume;
  }

  public void setActualAnnualVolume(BigDecimal actualAnnualVolume) {
    this.actualAnnualVolume = actualAnnualVolume;
  }

  public PaymentTerms getPaymentTerms() {
    return paymentTerms;
  }

  public void setPaymentTerms(PaymentTerms paymentTerms) {
    this.paymentTerms = paymentTerms;
  }

  public BigDecimal getCreditLimit() {
    return creditLimit;
  }

  public void setCreditLimit(BigDecimal creditLimit) {
    this.creditLimit = creditLimit;
  }

  public DeliveryCondition getDeliveryCondition() {
    return deliveryCondition;
  }

  public void setDeliveryCondition(DeliveryCondition deliveryCondition) {
    this.deliveryCondition = deliveryCondition;
  }

  public Integer getRiskScore() {
    return riskScore;
  }

  public void setRiskScore(Integer riskScore) {
    this.riskScore = riskScore;
  }

  public LocalDateTime getLastContactDate() {
    return lastContactDate;
  }

  public void setLastContactDate(LocalDateTime lastContactDate) {
    this.lastContactDate = lastContactDate;
  }

  public LocalDateTime getNextFollowUpDate() {
    return nextFollowUpDate;
  }

  public void setNextFollowUpDate(LocalDateTime nextFollowUpDate) {
    this.nextFollowUpDate = nextFollowUpDate;
  }

  public Boolean getIsTestData() {
    return isTestData;
  }

  public void setIsTestData(Boolean isTestData) {
    this.isTestData = isTestData;
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

  public LocalDateTime getLastModifiedAt() {
    return lastModifiedAt;
  }

  public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
    this.lastModifiedAt = lastModifiedAt;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public List<CustomerContact> getContacts() {
    return contacts;
  }

  public void setContacts(List<CustomerContact> contacts) {
    this.contacts = contacts;
  }

  public List<CustomerLocation> getLocations() {
    return locations;
  }

  public void setLocations(List<CustomerLocation> locations) {
    this.locations = locations;
  }

  public List<CustomerTimelineEvent> getTimelineEvents() {
    return timelineEvents;
  }

  public void setTimelineEvents(List<CustomerTimelineEvent> timelineEvents) {
    this.timelineEvents = timelineEvents;
  }

  // Business methods for relationships

  /** Adds a contact to this customer. */
  public void addContact(CustomerContact contact) {
    if (contact != null) {
      contact.setCustomer(this);
      this.contacts.add(contact);
    }
  }

  /** Removes a contact from this customer (soft delete). */
  public void removeContact(CustomerContact contact, String deletedBy) {
    if (contact != null && contacts.contains(contact)) {
      contact.setIsDeleted(true);
      contact.setDeletedAt(java.time.LocalDateTime.now());
      contact.setDeletedBy(deletedBy);
    }
  }

  /** Gets the primary contact for this customer. */
  public java.util.Optional<CustomerContact> getPrimaryContact() {
    return contacts.stream()
        .filter(contact -> !Boolean.TRUE.equals(contact.getIsDeleted()))
        .filter(contact -> Boolean.TRUE.equals(contact.getIsPrimary()))
        .findFirst();
  }

  /** Gets all active contacts. */
  public java.util.List<CustomerContact> getActiveContacts() {
    return contacts.stream()
        .filter(contact -> !Boolean.TRUE.equals(contact.getIsDeleted()))
        .filter(contact -> Boolean.TRUE.equals(contact.getIsActive()))
        .toList();
  }

  /** Adds a location to this customer. */
  public void addLocation(CustomerLocation location) {
    if (location != null) {
      location.setCustomer(this);
      this.locations.add(location);
    }
  }

  /** Removes a location from this customer (soft delete). */
  public void removeLocation(CustomerLocation location, String deletedBy) {
    if (location != null && locations.contains(location)) {
      location.setIsDeleted(true);
      location.setDeletedAt(java.time.LocalDateTime.now());
      location.setDeletedBy(deletedBy);
    }
  }

  /** Gets the main location for this customer. */
  public java.util.Optional<CustomerLocation> getMainLocation() {
    return locations.stream()
        .filter(location -> !Boolean.TRUE.equals(location.getIsDeleted()))
        .filter(location -> Boolean.TRUE.equals(location.getIsMainLocation()))
        .findFirst();
  }

  /** Gets all active locations. */
  public java.util.List<CustomerLocation> getActiveLocations() {
    return locations.stream()
        .filter(location -> !Boolean.TRUE.equals(location.getIsDeleted()))
        .filter(location -> Boolean.TRUE.equals(location.getIsActive()))
        .toList();
  }

  /** Adds a timeline event to this customer. */
  public void addTimelineEvent(CustomerTimelineEvent event) {
    if (event != null) {
      event.setCustomer(this);
      this.timelineEvents.add(event);
    }
  }

  /** Gets recent timeline events (last 30 days). */
  public java.util.List<CustomerTimelineEvent> getRecentTimelineEvents() {
    java.time.LocalDateTime thirtyDaysAgo =
        java.time.LocalDateTime.now().minusDays(CustomerConstants.NEW_CUSTOMER_DAYS);
    return timelineEvents.stream()
        .filter(event -> !Boolean.TRUE.equals(event.getIsDeleted()))
        .filter(event -> event.getEventDate().isAfter(thirtyDaysAgo))
        .sorted((e1, e2) -> e2.getEventDate().compareTo(e1.getEventDate()))
        .toList();
  }

  /** Gets timeline events by category. */
  public java.util.List<CustomerTimelineEvent> getTimelineEventsByCategory(
      de.freshplan.domain.customer.entity.EventCategory category) {
    return timelineEvents.stream()
        .filter(event -> !Boolean.TRUE.equals(event.getIsDeleted()))
        .filter(event -> event.getCategory() == category)
        .sorted((e1, e2) -> e2.getEventDate().compareTo(e1.getEventDate()))
        .toList();
  }
}
