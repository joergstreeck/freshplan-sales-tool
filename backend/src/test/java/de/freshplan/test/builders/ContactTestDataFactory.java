package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data factory for CustomerContact entities. Provides builder pattern for creating test
 * contacts without CDI.
 *
 * @author Claude
 * @since Migration Phase 4 - Quick Wins (Fixed)
 * @since Phase 2B - Enhanced with collision-free email generation
 */
public class ContactTestDataFactory {

  // KOLLISIONSFREIE ID-GENERIERUNG - Thread-Safe & CI-kompatibel
  private static final AtomicLong CONTACT_SEQ = new AtomicLong();

  /** Create a new builder instance. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    // Default values
    private UUID id;
    private Customer customer;
    private String firstName = "Test";
    private String lastName = "Contact";
    private String email;
    private String phone;
    private String mobile;
    private String position;
    private String department;
    private Boolean isPrimary = false;
    private Boolean isActive = true;
    private Boolean isDecisionMaker = false;
    private Set<String> roles = new HashSet<>();
    private String languagePreference = "DE";
    private String notes;
    private CustomerContact reportsTo;

    // Audit fields
    private LocalDateTime createdAt = LocalDateTime.now();
    private String createdBy = "test-system";
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String updatedBy = "test-system";

    /**
     * Generiert eindeutige Email für Contact-Tests.
     * Format: contact.{RUN_ID}.{SEQ}@test.example.com
     */
    private static String generateUniqueEmail() {
      String runId = System.getProperty("test.run.id", 
          System.getenv().getOrDefault("GITHUB_RUN_ID", "LOCAL"));
      long seq = CONTACT_SEQ.incrementAndGet();
      return "contact." + runId + "." + seq + "@test.example.com";
    }

    /**
     * Builder-Konstruktor mit Default-Werten
     */
    public Builder() {
      // Setze Default unique email bei Erstellung
      this.email = generateUniqueEmail();
    }

    /**
     * Set the customer for this contact.
     *
     * @param customer The customer entity
     * @return This builder
     */
    public Builder forCustomer(Customer customer) {
      this.customer = customer;
      return this;
    }

    /**
     * Set the first name.
     *
     * @param firstName First name
     * @return This builder
     */
    public Builder withFirstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    /**
     * Set the last name.
     *
     * @param lastName Last name
     * @return This builder
     */
    public Builder withLastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    /**
     * Set the email address.
     *
     * @param email Email address
     * @return This builder
     */
    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    /**
     * Set the phone number.
     *
     * @param phone Phone number
     * @return This builder
     */
    public Builder withPhone(String phone) {
      this.phone = phone;
      return this;
    }

    /**
     * Set the mobile number.
     *
     * @param mobile Mobile number
     * @return This builder
     */
    public Builder withMobile(String mobile) {
      this.mobile = mobile;
      return this;
    }

    /**
     * Set the position.
     *
     * @param position Position/title
     * @return This builder
     */
    public Builder withPosition(String position) {
      this.position = position;
      return this;
    }

    /**
     * Set the department.
     *
     * @param department Department name
     * @return This builder
     */
    public Builder withDepartment(String department) {
      this.department = department;
      return this;
    }

    /**
     * Mark as primary contact.
     *
     * @return This builder
     */
    public Builder asPrimary() {
      this.isPrimary = true;
      return this;
    }

    /**
     * Set whether this is the primary contact.
     *
     * @param isPrimary True if primary contact
     * @return This builder
     */
    public Builder withIsPrimary(boolean isPrimary) {
      this.isPrimary = isPrimary;
      return this;
    }

    /**
     * Mark as inactive.
     *
     * @return This builder
     */
    public Builder asInactive() {
      this.isActive = false;
      return this;
    }

    /**
     * Mark as decision maker.
     *
     * @return This builder
     */
    public Builder asDecisionMaker() {
      this.isDecisionMaker = true;
      this.roles.add("DECISION_MAKER");
      return this;
    }

    /**
     * Add a role.
     *
     * @param role Role to add
     * @return This builder
     */
    public Builder withRole(String role) {
      this.roles.add(role);
      return this;
    }

    /**
     * Set multiple roles.
     *
     * @param roles Set of roles
     * @return This builder
     */
    public Builder withRoles(Set<String> roles) {
      this.roles = new HashSet<>(roles);
      return this;
    }

    /**
     * Set language preference.
     *
     * @param languagePreference Language code (e.g., "DE", "EN")
     * @return This builder
     */
    public Builder withLanguagePreference(String languagePreference) {
      this.languagePreference = languagePreference;
      return this;
    }

    /**
     * Set notes.
     *
     * @param notes Notes text
     * @return This builder
     */
    public Builder withNotes(String notes) {
      this.notes = notes;
      return this;
    }

    /**
     * Set the reporting manager.
     *
     * @param reportsTo The contact this contact reports to
     * @return This builder
     */
    public Builder withReportsTo(CustomerContact reportsTo) {
      this.reportsTo = reportsTo;
      return this;
    }

    /**
     * Set a test ID (for unit tests only).
     *
     * @param id The UUID to use as ID
     * @return This builder
     */
    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    /**
     * Generate a random test ID (for unit tests only).
     *
     * @return This builder
     */
    public Builder withGeneratedId() {
      this.id = UUID.randomUUID();
      return this;
    }

    /**
     * Build the contact entity without persisting.
     *
     * @return The built contact entity
     */
    public CustomerContact build() {
      // For unit tests that don't need a real customer, create a mock one
      if (customer == null) {
        customer = createMockCustomer();
      }

      CustomerContact contact = new CustomerContact();
      // Set ID if provided (for unit tests)
      if (id != null) {
        contact.setId(id);
      }
      contact.setCustomer(customer);
      contact.setFirstName(firstName);
      contact.setLastName(lastName);

      // Email ist bereits im Konstruktor gesetzt worden (unique)
      // Nur überschreiben wenn explizit NULL gesetzt wurde
      contact.setEmail(email);

      contact.setPhone(phone);
      contact.setMobile(mobile);
      contact.setPosition(position);
      contact.setDepartment(department);
      contact.setIsPrimary(isPrimary);
      contact.setIsActive(isActive);
      contact.setIsDecisionMaker(isDecisionMaker);
      contact.setRoles(roles);
      contact.setLanguagePreference(languagePreference);
      contact.setNotes(notes);
      contact.setReportsTo(reportsTo);

      // WICHTIG: Contact inherits test data status from customer wenn möglich
      setTestDataFlagIfExists(contact, customer);

      // Set audit fields
      contact.setCreatedAt(createdAt);
      contact.setCreatedBy(createdBy);
      contact.setUpdatedAt(updatedAt);
      contact.setUpdatedBy(updatedBy);

      return contact;
    }

    /**
     * Setzt isTestData auf Contact falls das Feld existiert.
     * Contact erbt den Test-Status vom Customer.
     */
    private void setTestDataFlagIfExists(CustomerContact contact, Customer customer) {
      try {
        // Prüfe ob CustomerContact.setIsTestData existiert
        java.lang.reflect.Method setter = contact.getClass().getMethod("setIsTestData", Boolean.class);
        // Übernehme isTestData vom Customer falls vorhanden
        if (customer != null && customer.getIsTestData() != null) {
          setter.invoke(contact, customer.getIsTestData());
        } else {
          // Fallback: Setze auf true für Tests
          setter.invoke(contact, true);
        }
      } catch (Exception e) {
        // Field doesn't exist - that's ok, CustomerContact hat kein isTestData
        // Kein Logging nötig, da erwartetes Verhalten
      }
    }

    /**
     * Create a mock customer for unit tests that don't need persistence.
     * Nutzt eindeutige IDs und isTestData=true.
     *
     * @return A mock customer entity with test data
     */
    private Customer createMockCustomer() {
      Customer mockCustomer = new Customer();
      mockCustomer.setId(UUID.randomUUID());
      mockCustomer.setCompanyName("[TEST] Mock Customer for Contact");
      
      // Eindeutige Customer Number für Contact-Tests
      String runId = System.getProperty("test.run.id", 
          System.getenv().getOrDefault("GITHUB_RUN_ID", "LOCAL"));
      mockCustomer.setCustomerNumber("TEST-MOCK-" + runId + "-" + UUID.randomUUID().toString().substring(0, 8));
      mockCustomer.setIsTestData(true); // KRITISCH für Cleanup
      
      // Audit-Felder
      mockCustomer.setCreatedAt(LocalDateTime.now());
      mockCustomer.setCreatedBy("test-system");
      mockCustomer.setUpdatedAt(LocalDateTime.now());
      mockCustomer.setUpdatedBy("test-system");
      
      return mockCustomer;
    }

    /**
     * Build and persist contact with customer.
     * Erstellt auch Customer falls nötig.
     * 
     * @param customerRepo The CustomerRepository for customer persistence
     * @param em The EntityManager for contact persistence
     * @return The persisted contact entity
     */
    public CustomerContact buildAndPersist(
        de.freshplan.domain.customer.repository.CustomerRepository customerRepo,
        jakarta.persistence.EntityManager em) {
      CustomerContact contact = build();
      
      // Customer persistieren falls nicht schon gespeichert
      if (contact.getCustomer() != null && contact.getCustomer().getId() == null) {
        customerRepo.persistAndFlush(contact.getCustomer());
      }
      
      // Contact persistieren
      em.persist(contact);
      em.flush();
      
      return contact;
    }
  }
}
