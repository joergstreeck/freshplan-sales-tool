package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.entity.PaymentTerms;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test data factory for Customer entities. Provides builder pattern for creating test customers
 * without CDI.
 *
 * @author Claude
 * @since Migration Phase 4 - Quick Wins
 */
public class CustomerTestDataFactory {

  /** Create a new builder instance. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    // Default values
    private String companyName = "Test Company GmbH";
    private String customerNumber;
    private CustomerStatus status = CustomerStatus.LEAD;
    private Industry industry = Industry.SONSTIGE;
    private String website;
    private String phone;
    private String email;
    private String street;
    private String city = "Berlin";
    private String postalCode = "10115";
    private String country = "Deutschland";
    private String taxId;
    private String commercialRegister;
    private Integer employeeCount;
    private BigDecimal annualRevenue;
    private String source = "Direkt";
    private String notes;
    private LocalDateTime lastContactDate;
    private LocalDateTime nextContactDate;
    private Integer riskScore = 50;
    private BigDecimal expectedMonthlyVolume;
    private BigDecimal expectedAnnualVolume;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String responsibleSales = "testuser";
    private String deliveryTerms;
    private String paymentTerms;
    private Boolean isTestData = true;
    private String createdBy = "test-system";

    /** Set the company name. */
    public Builder withCompanyName(String companyName) {
      this.companyName = companyName;
      return this;
    }

    /** Set the customer number. */
    public Builder withCustomerNumber(String customerNumber) {
      this.customerNumber = customerNumber;
      return this;
    }

    /** Set the customer status. */
    public Builder withStatus(CustomerStatus status) {
      this.status = status;
      return this;
    }

    /** Set the industry. */
    public Builder withIndustry(Industry industry) {
      this.industry = industry;
      return this;
    }

    /** Set the website. */
    public Builder withWebsite(String website) {
      this.website = website;
      return this;
    }

    /** Set the phone. */
    public Builder withPhone(String phone) {
      this.phone = phone;
      return this;
    }

    /** Set the email. */
    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    /** Set the address. */
    public Builder withAddress(String street, String city, String postalCode, String country) {
      this.street = street;
      this.city = city;
      this.postalCode = postalCode;
      this.country = country;
      return this;
    }

    /** Set the street. */
    public Builder withStreet(String street) {
      this.street = street;
      return this;
    }

    /** Set the city. */
    public Builder withCity(String city) {
      this.city = city;
      return this;
    }

    /** Set the postal code. */
    public Builder withPostalCode(String postalCode) {
      this.postalCode = postalCode;
      return this;
    }

    /** Set the country. */
    public Builder withCountry(String country) {
      this.country = country;
      return this;
    }

    /** Set the tax ID. */
    public Builder withTaxId(String taxId) {
      this.taxId = taxId;
      return this;
    }

    /** Set the commercial register. */
    public Builder withCommercialRegister(String commercialRegister) {
      this.commercialRegister = commercialRegister;
      return this;
    }

    /** Set the employee count. */
    public Builder withEmployeeCount(Integer employeeCount) {
      this.employeeCount = employeeCount;
      return this;
    }

    /** Set the annual revenue. */
    public Builder withAnnualRevenue(BigDecimal annualRevenue) {
      this.annualRevenue = annualRevenue;
      return this;
    }

    /** Set the source. */
    public Builder withSource(String source) {
      this.source = source;
      return this;
    }

    /** Set the notes. */
    public Builder withNotes(String notes) {
      this.notes = notes;
      return this;
    }

    /** Set the last contact date. */
    public Builder withLastContactDate(LocalDateTime lastContactDate) {
      this.lastContactDate = lastContactDate;
      return this;
    }

    /** Set the next contact date. */
    public Builder withNextContactDate(LocalDateTime nextContactDate) {
      this.nextContactDate = nextContactDate;
      return this;
    }

    /** Set the risk score. */
    public Builder withRiskScore(Integer riskScore) {
      this.riskScore = riskScore;
      return this;
    }

    /** Set the expected monthly volume. */
    public Builder withExpectedMonthlyVolume(BigDecimal expectedMonthlyVolume) {
      this.expectedMonthlyVolume = expectedMonthlyVolume;
      return this;
    }

    /** Set the expected annual volume. */
    public Builder withExpectedAnnualVolume(BigDecimal expectedAnnualVolume) {
      this.expectedAnnualVolume = expectedAnnualVolume;
      return this;
    }

    /** Set the contract start date. */
    public Builder withContractStartDate(LocalDate contractStartDate) {
      this.contractStartDate = contractStartDate;
      return this;
    }

    /** Set the contract end date. */
    public Builder withContractEndDate(LocalDate contractEndDate) {
      this.contractEndDate = contractEndDate;
      return this;
    }

    /** Set the responsible sales person. */
    public Builder withResponsibleSales(String responsibleSales) {
      this.responsibleSales = responsibleSales;
      return this;
    }

    /** Set the delivery terms. */
    public Builder withDeliveryTerms(String deliveryTerms) {
      this.deliveryTerms = deliveryTerms;
      return this;
    }

    /** Set the payment terms. */
    public Builder withPaymentTerms(String paymentTerms) {
      this.paymentTerms = paymentTerms;
      return this;
    }

    /** Mark as test data. */
    public Builder asTestData(boolean isTestData) {
      this.isTestData = isTestData;
      return this;
    }

    /** Set created by. */
    public Builder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    /**
     * Build the customer entity without persisting.
     *
     * @return The built customer entity
     */
    public Customer build() {
      Customer customer = new Customer();

      // Set all fields
      customer.setCompanyName(companyName);
      customer.setCustomerNumber(customerNumber);
      customer.setStatus(status);
      if (industry != null) {
        customer.setIndustry(industry);
      }
      // Fields that don't exist in Customer entity - commented out
      // customer.setWebsite(website);
      // customer.setPhone(phone);
      // customer.setEmail(email);
      // customer.setStreet(street);
      // customer.setCity(city);
      // customer.setPostalCode(postalCode);
      // customer.setCountry(country);
      // customer.setTaxId(taxId);
      // customer.setCommercialRegister(commercialRegister);
      // customer.setEmployeeCount(employeeCount);
      // customer.setAnnualRevenue(annualRevenue);
      // customer.setSource(source);
      // customer.setNotes(notes);
      customer.setLastContactDate(lastContactDate);
      // customer.setNextContactDate(nextContactDate);
      customer.setRiskScore(riskScore);
      // customer.setExpectedMonthlyVolume(expectedMonthlyVolume);
      customer.setExpectedAnnualVolume(expectedAnnualVolume);
      // customer.setContractStartDate(contractStartDate);
      // customer.setContractEndDate(contractEndDate);
      // customer.setResponsibleSales(responsibleSales);
      // customer.setDeliveryTerms(deliveryTerms);
      if (paymentTerms != null) {
        customer.setPaymentTerms(PaymentTerms.valueOf(paymentTerms));
      }
      customer.setIsTestData(isTestData);
      customer.setCreatedBy(createdBy);

      // Set created at if needed (normally done by JPA)
      customer.setCreatedAt(LocalDateTime.now());

      return customer;
    }

    /** Build a minimal test customer. Convenience method for common test case. */
    public Customer buildMinimal() {
      return withCompanyName("Test Company GmbH").withStatus(CustomerStatus.LEAD).build();
    }
  }
}
