package de.freshplan.domain.customer.service.dto;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.shared.BusinessType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a new customer. All validation rules are enforced here.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record CreateCustomerRequest(

    // Basic Information (required)
    @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Company name must not exceed 255 characters")
        String companyName,
    @Size(max = 255, message = "Trading name must not exceed 255 characters") String tradingName,
    @Size(max = 100, message = "Legal form must not exceed 100 characters") String legalForm,

    // Classification
    @NotNull(message = "Customer type is required") CustomerType customerType,
    BusinessType businessType,
    Classification classification,

    // Hierarchy (optional)
    String parentCustomerId, // Will be converted to UUID
    CustomerHierarchyType hierarchyType,

    // Status & Lifecycle
    CustomerStatus status, // Defaults to LEAD if not provided
    CustomerLifecycleStage lifecycleStage, // Defaults to ACQUISITION if not provided

    // Financial Information
    @DecimalMin(value = "0.0", message = "Expected annual volume must be positive")
        @Digits(integer = 10, fraction = 2, message = "Expected annual volume format is invalid")
        BigDecimal expectedAnnualVolume,
    @DecimalMin(value = "0.0", message = "Actual annual volume must be positive")
        @Digits(integer = 10, fraction = 2, message = "Actual annual volume format is invalid")
        BigDecimal actualAnnualVolume,
    PaymentTerms paymentTerms,
    @DecimalMin(value = "0.0", message = "Credit limit must be positive")
        @Digits(integer = 10, fraction = 2, message = "Credit limit format is invalid")
        BigDecimal creditLimit,
    DeliveryCondition deliveryCondition,

    // Contact & Follow-up
    LocalDateTime lastContactDate,
    @Future(message = "Next follow-up date must be in the future") LocalDateTime nextFollowUpDate) {

  /** Constructor with minimal required fields. */
  public CreateCustomerRequest(String companyName, CustomerType customerType) {
    this(
        companyName,
        null, // tradingName
        null, // legalForm
        customerType,
        null, // businessType
        null, // classification
        null, // parentCustomerId
        null, // hierarchyType
        null, // status - will default to LEAD
        null, // lifecycleStage - will default to ACQUISITION
        null, // expectedAnnualVolume
        null, // actualAnnualVolume
        null, // paymentTerms
        null, // creditLimit
        null, // deliveryCondition
        null, // lastContactDate
        null // nextFollowUpDate
        );
  }

  /** Builder-style constructor for testing and complex scenarios. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String companyName;
    private String tradingName;
    private String legalForm;
    private CustomerType customerType;
    private BusinessType businessType;
    private Classification classification;
    private String parentCustomerId;
    private CustomerHierarchyType hierarchyType;
    private CustomerStatus status;
    private CustomerLifecycleStage lifecycleStage;
    private BigDecimal expectedAnnualVolume;
    private BigDecimal actualAnnualVolume;
    private PaymentTerms paymentTerms;
    private BigDecimal creditLimit;
    private DeliveryCondition deliveryCondition;
    private LocalDateTime lastContactDate;
    private LocalDateTime nextFollowUpDate;

    public Builder companyName(String companyName) {
      this.companyName = companyName;
      return this;
    }

    public Builder tradingName(String tradingName) {
      this.tradingName = tradingName;
      return this;
    }

    public Builder legalForm(String legalForm) {
      this.legalForm = legalForm;
      return this;
    }

    public Builder customerType(CustomerType customerType) {
      this.customerType = customerType;
      return this;
    }

    public Builder businessType(BusinessType businessType) {
      this.businessType = businessType;
      return this;
    }

    public Builder classification(Classification classification) {
      this.classification = classification;
      return this;
    }

    public Builder parentCustomerId(String parentCustomerId) {
      this.parentCustomerId = parentCustomerId;
      return this;
    }

    public Builder hierarchyType(CustomerHierarchyType hierarchyType) {
      this.hierarchyType = hierarchyType;
      return this;
    }

    public Builder status(CustomerStatus status) {
      this.status = status;
      return this;
    }

    public Builder lifecycleStage(CustomerLifecycleStage lifecycleStage) {
      this.lifecycleStage = lifecycleStage;
      return this;
    }

    public Builder expectedAnnualVolume(BigDecimal expectedAnnualVolume) {
      this.expectedAnnualVolume = expectedAnnualVolume;
      return this;
    }

    public Builder actualAnnualVolume(BigDecimal actualAnnualVolume) {
      this.actualAnnualVolume = actualAnnualVolume;
      return this;
    }

    public Builder paymentTerms(PaymentTerms paymentTerms) {
      this.paymentTerms = paymentTerms;
      return this;
    }

    public Builder creditLimit(BigDecimal creditLimit) {
      this.creditLimit = creditLimit;
      return this;
    }

    public Builder deliveryCondition(DeliveryCondition deliveryCondition) {
      this.deliveryCondition = deliveryCondition;
      return this;
    }

    public Builder lastContactDate(LocalDateTime lastContactDate) {
      this.lastContactDate = lastContactDate;
      return this;
    }

    public Builder nextFollowUpDate(LocalDateTime nextFollowUpDate) {
      this.nextFollowUpDate = nextFollowUpDate;
      return this;
    }

    public CreateCustomerRequest build() {
      return new CreateCustomerRequest(
          companyName,
          tradingName,
          legalForm,
          customerType,
          businessType,
          classification,
          parentCustomerId,
          hierarchyType,
          status,
          lifecycleStage,
          expectedAnnualVolume,
          actualAnnualVolume,
          paymentTerms,
          creditLimit,
          deliveryCondition,
          lastContactDate,
          nextFollowUpDate);
    }
  }
}
