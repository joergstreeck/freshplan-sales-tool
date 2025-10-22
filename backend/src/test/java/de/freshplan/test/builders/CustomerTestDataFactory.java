package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.entity.PaymentTerms;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data factory for Customer entities. Provides builder pattern for creating test customers
 * without CDI.
 *
 * <p>Track 2C - Enhanced with RealisticDataGenerator for German test data
 *
 * @author Claude
 * @since Migration Phase 4 - Quick Wins
 * @since Phase 2A - Enhanced with collision-free ID generation
 * @since Track 2C - Integrated with RealisticDataGenerator
 */
public class CustomerTestDataFactory {

  // Thread-local RealisticDataGenerator for realistic defaults
  private static final ThreadLocal<RealisticDataGenerator> GENERATOR =
      ThreadLocal.withInitial(() -> new RealisticDataGenerator());

  // KOLLISIONSFREIE ID-GENERIERUNG - Thread-Safe & CI-kompatibel
  private static final AtomicLong SEQ = new AtomicLong();

  /**
   * Run-ID für eindeutige Test-Identifikation. Nutzt test.run.id (CI) -> GITHUB_RUN_ID (Fallback)
   * -> "LOCAL"
   */
  private static String runId() {
    return System.getProperty(
        "test.run.id", System.getenv().getOrDefault("GITHUB_RUN_ID", "LOCAL"));
  }

  /**
   * Generiert eindeutige Kundennummer: TST-{HASH}-{SEQ} Max 20 Zeichen für VARCHAR(20) Constraint.
   * Format: TST-<8-char-hash>-<5-digit-seq> = max 18 chars
   */
  private static String nextNumber() {
    String runId = runId();
    // Hash RUN_ID to 8 chars (consistent per run, collision-resistant)
    int hash = Math.abs(runId.hashCode());
    String hashStr = String.format("%08X", hash).substring(0, 8);
    return "TST-" + hashStr + "-" + String.format("%05d", SEQ.incrementAndGet());
  }

  /**
   * Create a seeded generator for deterministic tests.
   *
   * @param seed Seed value for repeatable randomization
   * @return Builder with seeded generator
   */
  public static Builder builder(long seed) {
    Builder builder = new Builder();
    builder.generator = new RealisticDataGenerator(seed);
    return builder;
  }

  /** Create a new builder instance. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    // RealisticDataGenerator instance (thread-local or seeded)
    private RealisticDataGenerator generator = GENERATOR.get();

    // Default values - now with realistic defaults from RealisticDataGenerator
    // Note: companyName is lazy-initialized in build() to use correct generator instance
    private String companyName;
    private String customerNumber; // wird automatisch generiert
    private CustomerStatus status = CustomerStatus.PROSPECT; // Sprint 2.1.7.4: LEAD removed from lifecycle
    private Industry industry = Industry.SONSTIGE;
    private String website;
    private String phone;
    private String email;
    private String street;
    private String city; // wird durch generator befüllt falls null
    private String postalCode; // wird durch generator befüllt falls null
    private String country = "Deutschland";
    private String taxId;
    private String commercialRegister;
    private Integer employeeCount;
    private BigDecimal annualRevenue;
    private String source = "Direkt";
    private String notes;
    private LocalDateTime lastContactDate;
    private LocalDateTime nextContactDate;
    private Integer riskScore; // Default 2 in build() wenn null
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
     * <p>Track 2C - Enhanced with RealisticDataGenerator defaults
     *
     * @return The built customer entity
     */
    public Customer build() {
      // Automatische customerNumber-Generierung falls nicht explizit gesetzt
      if (customerNumber == null) {
        customerNumber = nextNumber();
      }

      // Realistische Defaults aus RealisticDataGenerator
      if (companyName == null) {
        companyName = "[TEST] " + generator.germanCompanyName();
      }
      if (city == null) {
        city = generator.germanCity();
      }
      if (postalCode == null) {
        postalCode = generator.germanPostalCode();
      }

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

      // Realistische Defaults für Tests
      customer.setRiskScore(riskScore != null ? riskScore : 2); // Low-Risk Default
      // customer.setExpectedMonthlyVolume(expectedMonthlyVolume);
      customer.setExpectedAnnualVolume(
          expectedAnnualVolume != null
              ? expectedAnnualVolume
              : BigDecimal.valueOf(50000)); // 50k€ Default
      // customer.setContractStartDate(contractStartDate);
      // customer.setContractEndDate(contractEndDate);
      // customer.setResponsibleSales(responsibleSales);
      // customer.setDeliveryTerms(deliveryTerms);
      if (paymentTerms != null) {
        customer.setPaymentTerms(PaymentTerms.valueOf(paymentTerms));
      }
      // KRITISCH: Immer true für Tests - überschreibt auch explizite false-Werte
      customer.setIsTestData(true);
      customer.setCreatedBy(createdBy);

      // Set created at if needed (normally done by JPA)
      customer.setCreatedAt(LocalDateTime.now());

      return customer;
    }

    /** Build a minimal test customer. Convenience method for common test case. */
    public Customer buildMinimal() {
      return withCompanyName("Test Company GmbH").withStatus(CustomerStatus.PROSPECT).build(); // Sprint 2.1.7.4: LEAD removed
    }

    /**
     * Build and persist to database. Für Integration-Tests mit Repository-Injection.
     *
     * @param repository The CustomerRepository to use for persistence
     * @return The persisted customer entity
     */
    public Customer buildAndPersist(
        de.freshplan.domain.customer.repository.CustomerRepository repository) {
      Customer customer = build();
      repository.persistAndFlush(customer);
      return customer;
    }
  }
}
