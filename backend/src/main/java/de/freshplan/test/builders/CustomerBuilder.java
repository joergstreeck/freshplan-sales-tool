package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.CreateCustomerRequest;
import de.freshplan.test.utils.TestDataUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for creating test Customer entities. Provides fluent API for setting customer properties
 * and predefined scenarios.
 *
 * <p>This builder is part of the centralized test data architecture, ensuring all test customers
 * are properly marked and can be cleaned up.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CustomerBuilder {

  @Inject CustomerRepository repository;

  // Basic fields
  private String companyName = "Test Company";
  private String tradingName = null;
  private String legalForm = "GmbH";

  // Enums with defaults
  private CustomerType customerType = CustomerType.UNTERNEHMEN;
  private CustomerStatus status = CustomerStatus.LEAD;
  private Industry industry = Industry.SONSTIGE;
  private Classification classification = Classification.C_KUNDE;
  private CustomerHierarchyType hierarchyType = CustomerHierarchyType.STANDALONE;
  private CustomerLifecycleStage lifecycleStage = CustomerLifecycleStage.ACQUISITION;
  private PartnerStatus partnerStatus = PartnerStatus.KEIN_PARTNER;
  private PaymentTerms paymentTerms = PaymentTerms.NETTO_30;
  private DeliveryCondition deliveryCondition = DeliveryCondition.STANDARD;
  private FinancingType primaryFinancing = FinancingType.PRIVATE;

  // Financial fields
  private BigDecimal expectedAnnualVolume = BigDecimal.ZERO;
  private BigDecimal actualAnnualVolume = BigDecimal.ZERO;
  private BigDecimal creditLimit = BigDecimal.ZERO;

  // Risk management
  private Integer riskScore = 0;
  private LocalDateTime lastContactDate = null;
  private LocalDateTime nextFollowUpDate = null;

  // Sprint 2 fields
  private Integer locationsGermany = 0;
  private Integer locationsAustria = 0;
  private Integer locationsSwitzerland = 0;
  private Integer locationsRestEU = 0;
  private String expansionPlanned = null;
  private List<String> painPoints = new ArrayList<>();

  // Test marker - ALWAYS true for test data
  private final boolean isTestData = true;

  /**
   * Resets the builder to default values for creating a new customer.
   *
   * @return this builder instance for chaining
   */
  public CustomerBuilder reset() {
    this.companyName = "Test Company";
    this.tradingName = null;
    this.legalForm = "GmbH";
    this.customerType = CustomerType.UNTERNEHMEN;
    this.status = CustomerStatus.LEAD;
    this.industry = Industry.SONSTIGE;
    this.classification = Classification.C_KUNDE;
    this.hierarchyType = CustomerHierarchyType.STANDALONE;
    this.lifecycleStage = CustomerLifecycleStage.ACQUISITION;
    this.partnerStatus = PartnerStatus.KEIN_PARTNER;
    this.paymentTerms = PaymentTerms.NETTO_30;
    this.deliveryCondition = DeliveryCondition.STANDARD;
    this.primaryFinancing = FinancingType.PRIVATE;
    this.expectedAnnualVolume = BigDecimal.ZERO;
    this.actualAnnualVolume = BigDecimal.ZERO;
    this.creditLimit = BigDecimal.ZERO;
    this.riskScore = 0;
    this.lastContactDate = null;
    this.nextFollowUpDate = null;
    this.locationsGermany = 0;
    this.locationsAustria = 0;
    this.locationsSwitzerland = 0;
    this.locationsRestEU = 0;
    this.expansionPlanned = null;
    this.painPoints = new ArrayList<>();
    return this;
  }

  // Basic setters
  public CustomerBuilder withCompanyName(String name) {
    this.companyName = name;
    return this;
  }

  public CustomerBuilder withTradingName(String name) {
    this.tradingName = name;
    return this;
  }

  public CustomerBuilder withLegalForm(String form) {
    this.legalForm = form;
    return this;
  }

  // Enum setters
  public CustomerBuilder withType(CustomerType type) {
    this.customerType = type;
    return this;
  }

  public CustomerBuilder withStatus(CustomerStatus status) {
    this.status = status;
    return this;
  }

  public CustomerBuilder withIndustry(Industry industry) {
    this.industry = industry;
    return this;
  }

  public CustomerBuilder withClassification(Classification classification) {
    this.classification = classification;
    return this;
  }

  public CustomerBuilder withPartnerStatus(PartnerStatus partnerStatus) {
    this.partnerStatus = partnerStatus;
    return this;
  }

  public CustomerBuilder withPaymentTerms(PaymentTerms terms) {
    this.paymentTerms = terms;
    return this;
  }

  public CustomerBuilder withDeliveryCondition(DeliveryCondition condition) {
    this.deliveryCondition = condition;
    return this;
  }

  public CustomerBuilder withFinancingType(FinancingType financing) {
    this.primaryFinancing = financing;
    return this;
  }

  // Financial setters
  public CustomerBuilder withExpectedAnnualVolume(BigDecimal volume) {
    this.expectedAnnualVolume = volume;
    return this;
  }

  public CustomerBuilder withExpectedAnnualVolume(long volume) {
    this.expectedAnnualVolume = BigDecimal.valueOf(volume);
    return this;
  }

  public CustomerBuilder withCreditLimit(BigDecimal limit) {
    this.creditLimit = limit;
    return this;
  }

  public CustomerBuilder withCreditLimit(long limit) {
    this.creditLimit = BigDecimal.valueOf(limit);
    return this;
  }

  // Risk setters
  public CustomerBuilder withRiskScore(int score) {
    this.riskScore = Math.min(100, Math.max(0, score));
    return this;
  }

  public CustomerBuilder withLastContactDate(LocalDateTime date) {
    this.lastContactDate = date;
    return this;
  }

  public CustomerBuilder withLastContactToday() {
    this.lastContactDate = LocalDateTime.now();
    return this;
  }

  public CustomerBuilder withLastContactDaysAgo(int days) {
    this.lastContactDate = LocalDateTime.now().minusDays(days);
    return this;
  }

  // Location setters (Sprint 2)
  public CustomerBuilder withLocationsGermany(int count) {
    this.locationsGermany = count;
    return this;
  }

  public CustomerBuilder withLocationsAustria(int count) {
    this.locationsAustria = count;
    return this;
  }

  public CustomerBuilder withLocationsSwitzerland(int count) {
    this.locationsSwitzerland = count;
    return this;
  }

  public CustomerBuilder withPainPoints(String... points) {
    this.painPoints = Arrays.asList(points);
    return this;
  }

  // Predefined scenarios
  public CustomerBuilder asPremiumCustomer() {
    this.status = CustomerStatus.AKTIV;
    this.classification = Classification.A_KUNDE;
    this.partnerStatus = PartnerStatus.GOLD_PARTNER;
    this.expectedAnnualVolume = BigDecimal.valueOf(1_000_000);
    this.creditLimit = BigDecimal.valueOf(100_000);
    this.riskScore = 5;
    this.lastContactDate = LocalDateTime.now().minusDays(7);
    return this;
  }

  public CustomerBuilder asRiskCustomer() {
    this.status = CustomerStatus.RISIKO;
    this.classification = Classification.D_KUNDE;
    this.expectedAnnualVolume = BigDecimal.valueOf(50_000);
    this.creditLimit = BigDecimal.ZERO;
    this.riskScore = 85;
    this.lastContactDate = LocalDateTime.now().minusDays(90);
    this.paymentTerms = PaymentTerms.VORKASSE;
    return this;
  }

  public CustomerBuilder asNewLead() {
    this.status = CustomerStatus.LEAD;
    this.lifecycleStage = CustomerLifecycleStage.ACQUISITION;
    this.classification = Classification.C_KUNDE;
    this.expectedAnnualVolume = BigDecimal.valueOf(100_000);
    this.riskScore = 30;
    this.lastContactDate = LocalDateTime.now();
    return this;
  }

  public CustomerBuilder asInactiveCustomer() {
    this.status = CustomerStatus.INAKTIV;
    this.lifecycleStage = CustomerLifecycleStage.RECOVERY;
    this.riskScore = 70;
    this.lastContactDate = LocalDateTime.now().minusDays(180);
    return this;
  }

  public CustomerBuilder asRestaurantCustomer() {
    this.industry = Industry.RESTAURANT;
    this.customerType = CustomerType.UNTERNEHMEN;
    this.deliveryCondition = DeliveryCondition.FREI_HAUS;
    this.paymentTerms = PaymentTerms.NETTO_30;
    return this;
  }

  public CustomerBuilder asHotelCustomer() {
    this.industry = Industry.HOTEL;
    this.customerType = CustomerType.UNTERNEHMEN;
    this.locationsGermany = TestDataUtils.randomInt(1, 5);
    this.expectedAnnualVolume = BigDecimal.valueOf(TestDataUtils.randomInt(200_000, 500_000));
    return this;
  }

  /**
   * Builds a Customer entity WITHOUT persisting to database. Use this for unit tests or when you
   * need an entity without DB interaction.
   *
   * @return a new Customer entity with test data markers
   */
  public Customer build() {
    Customer customer = new Customer();
    String id = TestDataUtils.uniqueId();

    // Set required fields
    customer.setCustomerNumber("TEST-" + id);
    customer.setCompanyName("[TEST-" + id + "] " + companyName);
    customer.setIsTestData(true);
    customer.setIsDeleted(false);

    // Set basic fields
    if (tradingName != null) {
      customer.setTradingName(tradingName);
    }
    customer.setLegalForm(legalForm);

    // Set enums
    customer.setCustomerType(customerType);
    customer.setStatus(status);
    customer.setIndustry(industry);
    customer.setClassification(classification);
    customer.setHierarchyType(hierarchyType);
    customer.setLifecycleStage(lifecycleStage);
    customer.setPartnerStatus(partnerStatus);
    customer.setPaymentTerms(paymentTerms);
    customer.setDeliveryCondition(deliveryCondition);
    customer.setPrimaryFinancing(primaryFinancing);

    // Set financial fields
    customer.setExpectedAnnualVolume(expectedAnnualVolume);
    customer.setActualAnnualVolume(actualAnnualVolume);
    customer.setCreditLimit(creditLimit);

    // Set risk fields
    customer.setRiskScore(riskScore);
    customer.setLastContactDate(lastContactDate);
    customer.setNextFollowUpDate(nextFollowUpDate);

    // Set Sprint 2 fields
    customer.setLocationsGermany(locationsGermany);
    customer.setLocationsAustria(locationsAustria);
    customer.setLocationsSwitzerland(locationsSwitzerland);
    customer.setLocationsRestEU(locationsRestEU);
    customer.setTotalLocationsEU(
        locationsGermany + locationsAustria + locationsSwitzerland + locationsRestEU);
    customer.setExpansionPlanned(expansionPlanned);
    customer.setPainPoints(painPoints);

    // Set audit fields
    customer.setCreatedAt(LocalDateTime.now());
    customer.setCreatedBy("test-builder");

    // Reset builder state after use to prevent state leaking between tests
    // This is critical because the builder is @ApplicationScoped (singleton)
    reset();

    return customer;
  }

  /**
   * Builds and persists a Customer entity to the database. Use this for integration tests that need
   * DB interaction.
   *
   * @return the persisted Customer entity
   */
  @Transactional
  public Customer persist() {
    Customer customer = build(); // build() now automatically resets
    repository.persist(customer);
    repository.flush(); // Force immediate constraint validation
    return customer;
  }

  /**
   * Builds a CreateCustomerRequest DTO for API testing. Use this for REST API integration tests.
   *
   * @return a CreateCustomerRequest with test data markers
   */
  public CreateCustomerRequest buildCreateRequest() {
    String id = TestDataUtils.uniqueId();
    String prefixedName =
        companyName.startsWith("[TEST") ? companyName : "[TEST-" + id + "] " + companyName;

    return new CreateCustomerRequest(
        prefixedName,
        tradingName,
        legalForm,
        customerType,
        industry,
        classification,
        null, // parentCustomerId
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

  // Convenience methods for CustomerResourceIntegrationTest
  public CustomerBuilder asEnterprise() {
    this.customerType = CustomerType.UNTERNEHMEN;
    return this;
  }

  public CustomerBuilder inIndustry(Industry industry) {
    this.industry = industry;
    return this;
  }
}
