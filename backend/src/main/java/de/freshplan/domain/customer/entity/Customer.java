package de.freshplan.domain.customer.entity;

import de.freshplan.domain.customer.constants.CustomerConstants;
import de.freshplan.domain.shared.BusinessType;
import de.freshplan.domain.shared.KitchenSize;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

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

  /**
   * Business type classification (unified with Lead entity).
   *
   * <p>Sprint 2.1.6 Phase 2: Single Source of Truth for business classification. This field
   * replaces the legacy {@link #industry} field.
   *
   * @since 2.1.6
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "business_type", length = 30)
  private BusinessType businessType;

  /**
   * Legacy industry classification.
   *
   * @deprecated Use {@link #businessType} instead. This field is kept for backward compatibility
   *     and will be removed in a future migration (V265+). The businessType field is the Single
   *     Source of Truth for business classification.
   * @since 1.0
   */
  @Deprecated(since = "2.1.6", forRemoval = true)
  @Enumerated(EnumType.STRING)
  @Column(name = "industry", length = 30)
  private Industry industry;

  @Enumerated(EnumType.STRING)
  @Column(name = "classification", length = 10)
  private Classification classification;

  // Lead Parity Fields (Sprint 2.1.7.2 - V10032)
  // These fields exist in Lead entity and are needed for seamless Lead→Customer conversion

  /**
   * Kitchen size classification (100% Lead parity).
   *
   * @since 2.1.7.2
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "kitchen_size", length = 20)
  private KitchenSize kitchenSize;

  /**
   * Number of employees (100% Lead parity).
   *
   * @since 2.1.7.2
   */
  @Column(name = "employee_count")
  private Integer employeeCount;

  /**
   * Number of branches/locations (100% Lead parity). Note: Redundant with totalLocationsEU but kept
   * for Lead conversion compatibility.
   *
   * @since 2.1.7.2
   */
  @Column(name = "branch_count")
  private Integer branchCount = 1;

  /**
   * Chain/franchise flag (100% Lead parity). Note: Redundant with totalLocationsEU &gt; 1 check but
   * kept for Lead conversion compatibility.
   *
   * @since 2.1.7.2
   */
  @Column(name = "is_chain")
  private Boolean isChain = false;

  /**
   * Estimated purchase volume (100% Lead parity). Note: Complements expectedAnnualVolume - Lead has
   * estimatedVolume, Customer had expectedAnnualVolume.
   *
   * @since 2.1.7.2
   */
  @Column(name = "estimated_volume", precision = 12, scale = 2)
  private BigDecimal estimatedVolume;

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

  // Chain Structure - NEW for Sprint 2
  @Column(name = "total_locations_eu")
  private Integer totalLocationsEU = 0;

  @Column(name = "locations_germany")
  private Integer locationsGermany = 0;

  @Column(name = "locations_austria")
  private Integer locationsAustria = 0;

  @Column(name = "locations_switzerland")
  private Integer locationsSwitzerland = 0;

  @Column(name = "locations_rest_eu")
  private Integer locationsRestEU = 0;

  @Column(name = "expansion_planned", length = 10)
  private String expansionPlanned;

  // Business Model - NEW for Sprint 2
  @Enumerated(EnumType.STRING)
  @Column(name = "primary_financing", length = 20)
  private FinancingType primaryFinancing;

  // Pain Points as JSON - NEW for Sprint 2
  /**
   * @deprecated Use Boolean pain_* fields instead (V279+). This JSONB field is kept for backward
   *     compatibility only.
   */
  @Deprecated(since = "2.1.6", forRemoval = true)
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "pain_points", columnDefinition = "jsonb")
  private List<String> painPoints = new ArrayList<>();

  // Pain Scoring System V3 (Sprint 2.1.6 Phase 5+ - V279)
  // 100% Harmonized with Lead Entity (8 Boolean fields)

  // OPERATIONAL PAINS (5 fields)
  @Column(name = "pain_staff_shortage")
  private Boolean painStaffShortage = false; // Personalmangel (+10)

  @Column(name = "pain_high_costs")
  private Boolean painHighCosts = false; // Hoher Kostendruck (+7)

  @Column(name = "pain_food_waste")
  private Boolean painFoodWaste = false; // Food Waste/Überproduktion (+7)

  @Column(name = "pain_quality_inconsistency")
  private Boolean painQualityInconsistency = false; // Interne Qualitätsinkonsistenz (+6)

  @Column(name = "pain_time_pressure")
  private Boolean painTimePressure = false; // Zeitdruck/Effizienz (+5)

  // SWITCHING PAINS (3 fields)
  @Column(name = "pain_supplier_quality")
  private Boolean painSupplierQuality = false; // Qualitätsprobleme beim Lieferanten (+10)

  @Column(name = "pain_unreliable_delivery")
  private Boolean painUnreliableDelivery = false; // Unzuverlässige Lieferzeiten (+8)

  @Column(name = "pain_poor_service")
  private Boolean painPoorService = false; // Schlechter Service/Support (+3)

  // Notes (Freitext)
  @Column(name = "pain_notes", columnDefinition = "TEXT")
  private String painNotes;

  // Seasonal Business Support (Sprint 2.1.7.4 - V10033/V10034)
  // Allows marking customers with seasonal business patterns
  // to exclude them from churn detection during off-season

  @Column(name = "is_seasonal_business")
  private Boolean isSeasonalBusiness = false;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "seasonal_months", columnDefinition = "jsonb")
  private List<Integer> seasonalMonths = new ArrayList<>(); // Months 1-12

  @Column(name = "seasonal_pattern", length = 50)
  private String seasonalPattern; // e.g. 'SUMMER', 'WINTER', 'CUSTOM'

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

  // Lead Conversion Tracking (Sprint 2.1.6)
  /**
   * ID of the Lead that was converted to this Customer. NULL if Customer was created directly (not
   * from Lead conversion).
   *
   * <p>Sprint 2.1.6 - User Story 2: Lead → Kunde Convert Flow
   */
  @Column(name = "original_lead_id")
  private Long originalLeadId;

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
    // Initialize Sprint 2 fields
    if (painPoints == null) {
      painPoints = new ArrayList<>();
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

  public BusinessType getBusinessType() {
    return businessType;
  }

  public void setBusinessType(BusinessType businessType) {
    this.businessType = businessType;
    // Auto-sync to legacy industry field for backward compatibility
    if (businessType != null) {
      this.industry = businessType.toLegacyIndustry();
    }
  }

  /**
   * @deprecated Use {@link #getBusinessType()} instead.
   */
  @Deprecated(since = "2.1.6", forRemoval = true)
  public Industry getIndustry() {
    return industry;
  }

  /**
   * @deprecated Use {@link #setBusinessType(BusinessType)} instead.
   */
  @Deprecated(since = "2.1.6", forRemoval = true)
  public void setIndustry(Industry industry) {
    this.industry = industry;
    // Auto-sync to new businessType field
    if (industry != null) {
      this.businessType = BusinessType.fromLegacyIndustry(industry);
    }
  }

  public Classification getClassification() {
    return classification;
  }

  public void setClassification(Classification classification) {
    this.classification = classification;
  }

  // Lead Parity Fields Getters/Setters (Sprint 2.1.7.2 - V10032)

  public KitchenSize getKitchenSize() {
    return kitchenSize;
  }

  public void setKitchenSize(KitchenSize kitchenSize) {
    this.kitchenSize = kitchenSize;
  }

  public Integer getEmployeeCount() {
    return employeeCount;
  }

  public void setEmployeeCount(Integer employeeCount) {
    this.employeeCount = employeeCount;
  }

  public Integer getBranchCount() {
    return branchCount;
  }

  public void setBranchCount(Integer branchCount) {
    this.branchCount = branchCount;
  }

  public Boolean getIsChain() {
    return isChain;
  }

  public void setIsChain(Boolean isChain) {
    this.isChain = isChain;
  }

  public BigDecimal getEstimatedVolume() {
    return estimatedVolume;
  }

  public void setEstimatedVolume(BigDecimal estimatedVolume) {
    this.estimatedVolume = estimatedVolume;
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

  // Chain Structure Getters/Setters - NEW for Sprint 2
  public Integer getTotalLocationsEU() {
    return totalLocationsEU;
  }

  public void setTotalLocationsEU(Integer totalLocationsEU) {
    this.totalLocationsEU = totalLocationsEU;
  }

  public Integer getLocationsGermany() {
    return locationsGermany;
  }

  public void setLocationsGermany(Integer locationsGermany) {
    this.locationsGermany = locationsGermany;
  }

  public Integer getLocationsAustria() {
    return locationsAustria;
  }

  public void setLocationsAustria(Integer locationsAustria) {
    this.locationsAustria = locationsAustria;
  }

  public Integer getLocationsSwitzerland() {
    return locationsSwitzerland;
  }

  public void setLocationsSwitzerland(Integer locationsSwitzerland) {
    this.locationsSwitzerland = locationsSwitzerland;
  }

  public Integer getLocationsRestEU() {
    return locationsRestEU;
  }

  public void setLocationsRestEU(Integer locationsRestEU) {
    this.locationsRestEU = locationsRestEU;
  }

  public String getExpansionPlanned() {
    return expansionPlanned;
  }

  public void setExpansionPlanned(String expansionPlanned) {
    this.expansionPlanned = expansionPlanned;
  }

  public FinancingType getPrimaryFinancing() {
    return primaryFinancing;
  }

  public void setPrimaryFinancing(FinancingType primaryFinancing) {
    this.primaryFinancing = primaryFinancing;
  }

  /**
   * @deprecated Use Boolean pain_* getters instead (V279+)
   */
  @Deprecated(since = "2.1.6", forRemoval = true)
  public List<String> getPainPoints() {
    return painPoints;
  }

  /**
   * @deprecated Use Boolean pain_* setters instead (V279+)
   */
  @Deprecated(since = "2.1.6", forRemoval = true)
  public void setPainPoints(List<String> painPoints) {
    this.painPoints = painPoints;
  }

  // Pain Scoring V3 Getters/Setters (Sprint 2.1.6 Phase 5+ - V279)

  public Boolean getPainStaffShortage() {
    return painStaffShortage;
  }

  public void setPainStaffShortage(Boolean painStaffShortage) {
    this.painStaffShortage = painStaffShortage;
  }

  public Boolean getPainHighCosts() {
    return painHighCosts;
  }

  public void setPainHighCosts(Boolean painHighCosts) {
    this.painHighCosts = painHighCosts;
  }

  public Boolean getPainFoodWaste() {
    return painFoodWaste;
  }

  public void setPainFoodWaste(Boolean painFoodWaste) {
    this.painFoodWaste = painFoodWaste;
  }

  public Boolean getPainQualityInconsistency() {
    return painQualityInconsistency;
  }

  public void setPainQualityInconsistency(Boolean painQualityInconsistency) {
    this.painQualityInconsistency = painQualityInconsistency;
  }

  public Boolean getPainTimePressure() {
    return painTimePressure;
  }

  public void setPainTimePressure(Boolean painTimePressure) {
    this.painTimePressure = painTimePressure;
  }

  public Boolean getPainSupplierQuality() {
    return painSupplierQuality;
  }

  public void setPainSupplierQuality(Boolean painSupplierQuality) {
    this.painSupplierQuality = painSupplierQuality;
  }

  public Boolean getPainUnreliableDelivery() {
    return painUnreliableDelivery;
  }

  public void setPainUnreliableDelivery(Boolean painUnreliableDelivery) {
    this.painUnreliableDelivery = painUnreliableDelivery;
  }

  public Boolean getPainPoorService() {
    return painPoorService;
  }

  public void setPainPoorService(Boolean painPoorService) {
    this.painPoorService = painPoorService;
  }

  public String getPainNotes() {
    return painNotes;
  }

  public void setPainNotes(String painNotes) {
    this.painNotes = painNotes;
  }

  // Seasonal Business Support (Sprint 2.1.7.4)

  public Boolean getIsSeasonalBusiness() {
    return isSeasonalBusiness;
  }

  public void setIsSeasonalBusiness(Boolean isSeasonalBusiness) {
    this.isSeasonalBusiness = isSeasonalBusiness;
  }

  public List<Integer> getSeasonalMonths() {
    return seasonalMonths;
  }

  public void setSeasonalMonths(List<Integer> seasonalMonths) {
    this.seasonalMonths = seasonalMonths;
  }

  public String getSeasonalPattern() {
    return seasonalPattern;
  }

  public void setSeasonalPattern(String seasonalPattern) {
    this.seasonalPattern = seasonalPattern;
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

  /** Gets the count of active contacts for this customer. */
  public int getActiveContactsCount() {
    java.util.List<CustomerContact> activeContacts = getActiveContacts();
    return activeContacts != null ? activeContacts.size() : 0;
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

  // Sprint 2.1.6: Lead Conversion Tracking Getter/Setter
  public Long getOriginalLeadId() {
    return originalLeadId;
  }

  public void setOriginalLeadId(Long originalLeadId) {
    this.originalLeadId = originalLeadId;
  }
}
