package de.freshplan.testsupport;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.entity.CustomerLifecycleStage;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test fixture builders for creating test data in a fluent, readable way.
 * All builders create fork-safe test data to prevent collisions in parallel test execution.
 */
public final class TestFixtures {
  private TestFixtures() {}

  private static final int TEST_CUSTOMER_COUNT = Integer.getInteger("test.customer.count", 69);

  public static CustomerBuilder customer() { 
    return new CustomerBuilder(); 
  }

  public static OpportunityBuilder opportunity() {
    return new OpportunityBuilder();
  }

  /**
   * Fluent builder for Customer test data.
   * Creates customers with sensible defaults that can be overridden.
   */
  public static final class CustomerBuilder {
    private final Customer customer = new Customer();
    
    public CustomerBuilder() {
      // Set sensible defaults
      customer.setCustomerNumber(UniqueData.customerNumber("TEST", 
          (int)(Math.random() * 1000)));
      customer.setCompanyName("Test Company " + UUID.randomUUID().toString().substring(0, 8));
      customer.setLegalForm("GmbH");
      customer.setStatus(CustomerStatus.AKTIV);
      customer.setIndustry(Industry.HOTEL);
      customer.setLifecycleStage(CustomerLifecycleStage.GROWTH);
      customer.setCreatedBy("test-user");
      customer.setUpdatedBy("test-user");
      customer.setCreatedAt(LocalDateTime.now());
      customer.setUpdatedAt(LocalDateTime.now());
      customer.setIsDeleted(false);
      customer.setIsTestData(true);  // Mark as test data
      customer.setRiskScore(50);
    }

    public CustomerBuilder withCustomerNumber(String number) { 
      customer.setCustomerNumber(number); 
      return this; 
    }

    public CustomerBuilder withName(String name) { 
      customer.setCompanyName(name); 
      return this; 
    }

    public CustomerBuilder withStatus(CustomerStatus status) { 
      customer.setStatus(status); 
      return this; 
    }

    public CustomerBuilder withIndustry(Industry industry) {
      customer.setIndustry(industry);
      return this;
    }

    public CustomerBuilder withRiskScore(Integer score) {
      customer.setRiskScore(score);
      return this;
    }

    public CustomerBuilder withExpectedAnnualVolume(BigDecimal volume) {
      customer.setExpectedAnnualVolume(volume);
      return this;
    }

    public CustomerBuilder withLifecycleStage(CustomerLifecycleStage stage) {
      customer.setLifecycleStage(stage);
      return this;
    }

    public CustomerBuilder withLastContactDate(LocalDateTime date) {
      customer.setLastContactDate(date);
      return this;
    }

    public Customer build() { 
      return customer; 
    }

    public Customer persist(CustomerRepository repo) { 
      repo.persist(customer);
      return customer;
    }
  }

  /**
   * Fluent builder for Opportunity test data.
   * Creates opportunities with positive expected values to avoid constraint violations.
   */
  public static final class OpportunityBuilder {
    private final Opportunity opportunity = new Opportunity();

    public OpportunityBuilder() {
      // Set sensible defaults with positive values
      opportunity.setName("Test Opportunity " + UUID.randomUUID().toString().substring(0, 8));
      opportunity.setStage(OpportunityStage.QUALIFICATION);
      opportunity.setExpectedValue(BigDecimal.valueOf(2500 + Math.random() * 10000));
      opportunity.setProbability(50);
    }

    public OpportunityBuilder withCustomer(Customer customer) {
      opportunity.setCustomer(customer);
      return this;
    }

    public OpportunityBuilder withName(String name) {
      opportunity.setName(name);
      return this;
    }

    public OpportunityBuilder withStage(OpportunityStage stage) {
      opportunity.setStage(stage);
      opportunity.setProbability(stage.getDefaultProbability());
      return this;
    }

    public OpportunityBuilder withExpectedValue(BigDecimal value) {
      // Ensure positive value
      opportunity.setExpectedValue(value.abs());
      return this;
    }

    public Opportunity build() {
      return opportunity;
    }
  }

  /**
   * Helper method to create a standard test opportunity with guaranteed positive value.
   */
  public static Opportunity newOpportunity(Customer owner) {
    return opportunity()
        .withCustomer(owner)
        .withExpectedValue(BigDecimal.valueOf(2500))
        .build();
  }

  /**
   * Get the configured test customer count.
   */
  public static int getTestCustomerCount() {
    return TEST_CUSTOMER_COUNT;
  }
}