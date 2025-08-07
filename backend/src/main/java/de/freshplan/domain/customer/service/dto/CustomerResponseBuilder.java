package de.freshplan.domain.customer.service.dto;

import de.freshplan.domain.customer.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for CustomerResponse to avoid long constructor calls. Provides a fluent API for creating
 * CustomerResponse instances.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class CustomerResponseBuilder {
  // Identifier
  private String id;
  private String customerNumber;

  // Basic Information
  private String companyName;
  private String tradingName;
  private String legalForm;

  // Classification
  private CustomerType customerType;
  private Industry industry;
  private Classification classification;

  // Hierarchy
  private String parentCustomerId;
  private CustomerHierarchyType hierarchyType;
  private List<String> childCustomerIds = new ArrayList<>();
  private boolean hasChildren;

  // Status & Lifecycle
  private CustomerStatus status;
  private CustomerLifecycleStage lifecycleStage;
  private PartnerStatus partnerStatus;

  // Financial Information
  private BigDecimal expectedAnnualVolume;
  private BigDecimal actualAnnualVolume;
  private PaymentTerms paymentTerms;
  private BigDecimal creditLimit;
  private DeliveryCondition deliveryCondition;

  // Risk Management
  private Integer riskScore;
  private boolean atRisk;
  private LocalDateTime lastContactDate;
  private LocalDateTime nextFollowUpDate;

  // Chain Structure - NEW for Sprint 2
  private Integer totalLocationsEU;
  private Integer locationsGermany;
  private Integer locationsAustria;
  private Integer locationsSwitzerland;
  private Integer locationsRestEU;
  private String expansionPlanned;

  // Business Model - NEW for Sprint 2
  private FinancingType primaryFinancing;
  private List<String> painPoints = new ArrayList<>();

  // Audit Information
  private LocalDateTime createdAt;
  private String createdBy;
  private LocalDateTime updatedAt;
  private String updatedBy;

  // Soft Delete
  private Boolean isDeleted;
  private LocalDateTime deletedAt;
  private String deletedBy;

  public static CustomerResponseBuilder builder() {
    return new CustomerResponseBuilder();
  }

  // Builder methods
  public CustomerResponseBuilder id(String id) {
    this.id = id;
    return this;
  }

  public CustomerResponseBuilder customerNumber(String customerNumber) {
    this.customerNumber = customerNumber;
    return this;
  }

  public CustomerResponseBuilder companyName(String companyName) {
    this.companyName = companyName;
    return this;
  }

  public CustomerResponseBuilder tradingName(String tradingName) {
    this.tradingName = tradingName;
    return this;
  }

  public CustomerResponseBuilder legalForm(String legalForm) {
    this.legalForm = legalForm;
    return this;
  }

  public CustomerResponseBuilder customerType(CustomerType customerType) {
    this.customerType = customerType;
    return this;
  }

  public CustomerResponseBuilder industry(Industry industry) {
    this.industry = industry;
    return this;
  }

  public CustomerResponseBuilder classification(Classification classification) {
    this.classification = classification;
    return this;
  }

  public CustomerResponseBuilder parentCustomerId(String parentCustomerId) {
    this.parentCustomerId = parentCustomerId;
    return this;
  }

  public CustomerResponseBuilder hierarchyType(CustomerHierarchyType hierarchyType) {
    this.hierarchyType = hierarchyType;
    return this;
  }

  public CustomerResponseBuilder childCustomerIds(List<String> childCustomerIds) {
    this.childCustomerIds = childCustomerIds != null ? childCustomerIds : new ArrayList<>();
    return this;
  }

  public CustomerResponseBuilder hasChildren(boolean hasChildren) {
    this.hasChildren = hasChildren;
    return this;
  }

  public CustomerResponseBuilder status(CustomerStatus status) {
    this.status = status;
    return this;
  }

  public CustomerResponseBuilder lifecycleStage(CustomerLifecycleStage lifecycleStage) {
    this.lifecycleStage = lifecycleStage;
    return this;
  }

  public CustomerResponseBuilder partnerStatus(PartnerStatus partnerStatus) {
    this.partnerStatus = partnerStatus;
    return this;
  }

  public CustomerResponseBuilder expectedAnnualVolume(BigDecimal expectedAnnualVolume) {
    this.expectedAnnualVolume = expectedAnnualVolume;
    return this;
  }

  public CustomerResponseBuilder actualAnnualVolume(BigDecimal actualAnnualVolume) {
    this.actualAnnualVolume = actualAnnualVolume;
    return this;
  }

  public CustomerResponseBuilder paymentTerms(PaymentTerms paymentTerms) {
    this.paymentTerms = paymentTerms;
    return this;
  }

  public CustomerResponseBuilder creditLimit(BigDecimal creditLimit) {
    this.creditLimit = creditLimit;
    return this;
  }

  public CustomerResponseBuilder deliveryCondition(DeliveryCondition deliveryCondition) {
    this.deliveryCondition = deliveryCondition;
    return this;
  }

  public CustomerResponseBuilder riskScore(Integer riskScore) {
    this.riskScore = riskScore;
    return this;
  }

  public CustomerResponseBuilder atRisk(boolean atRisk) {
    this.atRisk = atRisk;
    return this;
  }

  public CustomerResponseBuilder lastContactDate(LocalDateTime lastContactDate) {
    this.lastContactDate = lastContactDate;
    return this;
  }

  public CustomerResponseBuilder nextFollowUpDate(LocalDateTime nextFollowUpDate) {
    this.nextFollowUpDate = nextFollowUpDate;
    return this;
  }

  public CustomerResponseBuilder createdAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public CustomerResponseBuilder createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public CustomerResponseBuilder updatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public CustomerResponseBuilder updatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  public CustomerResponseBuilder isDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
    return this;
  }

  public CustomerResponseBuilder deletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
    return this;
  }

  public CustomerResponseBuilder deletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
    return this;
  }

  // Chain Structure - NEW for Sprint 2
  public CustomerResponseBuilder totalLocationsEU(Integer totalLocationsEU) {
    this.totalLocationsEU = totalLocationsEU;
    return this;
  }

  public CustomerResponseBuilder locationsGermany(Integer locationsGermany) {
    this.locationsGermany = locationsGermany;
    return this;
  }

  public CustomerResponseBuilder locationsAustria(Integer locationsAustria) {
    this.locationsAustria = locationsAustria;
    return this;
  }

  public CustomerResponseBuilder locationsSwitzerland(Integer locationsSwitzerland) {
    this.locationsSwitzerland = locationsSwitzerland;
    return this;
  }

  public CustomerResponseBuilder locationsRestEU(Integer locationsRestEU) {
    this.locationsRestEU = locationsRestEU;
    return this;
  }

  public CustomerResponseBuilder expansionPlanned(String expansionPlanned) {
    this.expansionPlanned = expansionPlanned;
    return this;
  }

  // Business Model - NEW for Sprint 2
  public CustomerResponseBuilder primaryFinancing(FinancingType primaryFinancing) {
    this.primaryFinancing = primaryFinancing;
    return this;
  }

  public CustomerResponseBuilder painPoints(List<String> painPoints) {
    this.painPoints = painPoints;
    return this;
  }

  /**
   * Builds a CustomerResponse from a Customer entity. This is a convenience method that populates
   * all fields from the entity.
   */
  public CustomerResponseBuilder fromEntity(Customer customer) {
    this.id = customer.getId().toString();
    this.customerNumber = customer.getCustomerNumber();
    this.companyName = customer.getCompanyName();
    this.tradingName = customer.getTradingName();
    this.legalForm = customer.getLegalForm();
    this.customerType = customer.getCustomerType();
    this.industry = customer.getIndustry();
    this.classification = customer.getClassification();

    if (customer.getParentCustomer() != null) {
      this.parentCustomerId = customer.getParentCustomer().getId().toString();
    }

    this.hierarchyType = customer.getHierarchyType();

    // Map child customer IDs
    this.childCustomerIds =
        customer.getChildCustomers().stream().map(child -> child.getId().toString()).toList();

    this.hasChildren = customer.hasChildren();
    this.status = customer.getStatus();
    this.lifecycleStage = customer.getLifecycleStage();
    this.partnerStatus = customer.getPartnerStatus();
    this.expectedAnnualVolume = customer.getExpectedAnnualVolume();
    this.actualAnnualVolume = customer.getActualAnnualVolume();
    this.paymentTerms = customer.getPaymentTerms();
    this.creditLimit = customer.getCreditLimit();
    this.deliveryCondition = customer.getDeliveryCondition();
    this.riskScore = customer.getRiskScore();
    this.atRisk = customer.isAtRisk();
    this.lastContactDate = customer.getLastContactDate();
    this.nextFollowUpDate = customer.getNextFollowUpDate();
    this.createdAt = customer.getCreatedAt();
    this.createdBy = customer.getCreatedBy();
    this.updatedAt = customer.getUpdatedAt();
    this.updatedBy = customer.getUpdatedBy();
    this.isDeleted = customer.getIsDeleted();
    this.deletedAt = customer.getDeletedAt();
    this.deletedBy = customer.getDeletedBy();

    // Chain Structure - NEW for Sprint 2
    this.totalLocationsEU = customer.getTotalLocationsEU();
    this.locationsGermany = customer.getLocationsGermany();
    this.locationsAustria = customer.getLocationsAustria();
    this.locationsSwitzerland = customer.getLocationsSwitzerland();
    this.locationsRestEU = customer.getLocationsRestEU();
    this.expansionPlanned = customer.getExpansionPlanned();

    // Business Model - NEW for Sprint 2
    this.primaryFinancing = customer.getPrimaryFinancing();
    this.painPoints = customer.getPainPoints();

    return this;
  }

  public CustomerResponse build() {
    return new CustomerResponse(
        id,
        customerNumber,
        companyName,
        tradingName,
        legalForm,
        customerType,
        industry,
        classification,
        parentCustomerId,
        hierarchyType,
        childCustomerIds,
        hasChildren,
        status,
        lifecycleStage,
        partnerStatus,
        expectedAnnualVolume,
        actualAnnualVolume,
        paymentTerms,
        creditLimit,
        deliveryCondition,
        riskScore,
        atRisk,
        lastContactDate,
        nextFollowUpDate,

        // Chain Structure - NEW for Sprint 2
        totalLocationsEU,
        locationsGermany,
        locationsAustria,
        locationsSwitzerland,
        locationsRestEU,
        expansionPlanned,

        // Business Model - NEW for Sprint 2
        primaryFinancing,
        painPoints,
        createdAt,
        createdBy,
        updatedAt,
        updatedBy,
        isDeleted,
        deletedAt,
        deletedBy);
  }
}
