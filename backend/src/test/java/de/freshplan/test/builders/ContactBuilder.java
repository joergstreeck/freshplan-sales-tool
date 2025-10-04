package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Test data builder for CustomerContact entities. Provides fluent API for creating test contacts.
 * CDI-enabled for Integration-Tests mit echter Database.
 *
 * @author Claude
 * @since Migration Phase 4 - Quick Wins
 * @since Phase 2B - Enhanced CDI integration with persist methods
 */
@ApplicationScoped
public class ContactBuilder {

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository customerContactRepository;

  @Inject EntityManager entityManager;

  // Default values
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
  private String createdBy = "test";
  private LocalDateTime updatedAt = LocalDateTime.now();
  private String updatedBy = "test";

  /**
   * Set the customer for this contact.
   *
   * @param customer The customer entity
   * @return This builder
   */
  public ContactBuilder forCustomer(Customer customer) {
    this.customer = customer;
    return this;
  }

  /**
   * Set the first name.
   *
   * @param firstName First name
   * @return This builder
   */
  public ContactBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * Set the last name.
   *
   * @param lastName Last name
   * @return This builder
   */
  public ContactBuilder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * Set the email address.
   *
   * @param email Email address
   * @return This builder
   */
  public ContactBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  /**
   * Set the phone number.
   *
   * @param phone Phone number
   * @return This builder
   */
  public ContactBuilder withPhone(String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * Set the mobile number.
   *
   * @param mobile Mobile number
   * @return This builder
   */
  public ContactBuilder withMobile(String mobile) {
    this.mobile = mobile;
    return this;
  }

  /**
   * Set the position.
   *
   * @param position Position/title
   * @return This builder
   */
  public ContactBuilder withPosition(String position) {
    this.position = position;
    return this;
  }

  /**
   * Set the department.
   *
   * @param department Department name
   * @return This builder
   */
  public ContactBuilder withDepartment(String department) {
    this.department = department;
    return this;
  }

  /**
   * Mark as primary contact.
   *
   * @return This builder
   */
  public ContactBuilder asPrimary() {
    this.isPrimary = true;
    return this;
  }

  /**
   * Mark as inactive.
   *
   * @return This builder
   */
  public ContactBuilder asInactive() {
    this.isActive = false;
    return this;
  }

  /**
   * Mark as decision maker.
   *
   * @return This builder
   */
  public ContactBuilder asDecisionMaker() {
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
  public ContactBuilder withRole(String role) {
    this.roles.add(role);
    return this;
  }

  /**
   * Set multiple roles.
   *
   * @param roles Set of roles
   * @return This builder
   */
  public ContactBuilder withRoles(Set<String> roles) {
    this.roles = new HashSet<>(roles);
    return this;
  }

  /**
   * Set language preference.
   *
   * @param languagePreference Language code (e.g., "DE", "EN")
   * @return This builder
   */
  public ContactBuilder withLanguagePreference(String languagePreference) {
    this.languagePreference = languagePreference;
    return this;
  }

  /**
   * Set notes.
   *
   * @param notes Notes text
   * @return This builder
   */
  public ContactBuilder withNotes(String notes) {
    this.notes = notes;
    return this;
  }

  /**
   * Set the reporting manager.
   *
   * @param reportsTo The contact this contact reports to
   * @return This builder
   */
  public ContactBuilder withReportsTo(CustomerContact reportsTo) {
    this.reportsTo = reportsTo;
    return this;
  }

  /**
   * Build the contact entity without persisting.
   *
   * @return The built contact entity
   */
  public CustomerContact build() {
    if (customer == null) {
      throw new IllegalStateException("Customer must be set before building contact");
    }

    CustomerContact contact = new CustomerContact();
    contact.setId(UUID.randomUUID());
    contact.setCustomer(customer);
    contact.setFirstName(firstName);
    contact.setLastName(lastName);

    // Generate email if not set
    if (email == null) {
      email = (firstName + "." + lastName + "@test.example.com").toLowerCase();
    }
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

    // Set audit fields
    contact.setCreatedAt(createdAt);
    contact.setCreatedBy(createdBy);
    contact.setUpdatedAt(updatedAt);
    contact.setUpdatedBy(updatedBy);

    // Reset builder for next use
    reset();

    return contact;
  }

  /**
   * Build and persist the contact entity.
   *
   * @return The persisted contact entity
   */
  @Transactional
  public CustomerContact persist() {
    CustomerContact contact = build();
    if (customerContactRepository != null) {
      customerContactRepository.persist(contact);
    } else {
      // Fallback via EntityManager
      entityManager.persist(contact);
    }
    return contact;
  }

  /**
   * Create contact with auto-generated test customer. Für Integration-Tests mit echter Database.
   *
   * @return The persisted contact entity with new customer
   */
  @Transactional
  public CustomerContact createTestContact() {
    // Customer erstellen und persistieren
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("[TEST] Auto-Generated Customer for Contact")
            .build();
    customerRepository.persistAndFlush(customer);

    // Contact erstellen und persistieren
    CustomerContact contact =
        ContactTestDataFactory.builder()
            .forCustomer(customer)
            .withEmail("auto.contact." + System.currentTimeMillis() + "@test.com")
            .build();

    // Contact persistieren
    if (customerContactRepository != null) {
      customerContactRepository.persistAndFlush(contact);
    } else {
      // Fallback via EntityManager
      entityManager.persist(contact);
      entityManager.flush();
    }

    return contact;
  }

  /**
   * Create contact for existing customer. Für Tests die bereits einen Customer haben.
   *
   * @param customer The existing customer entity
   * @return The persisted contact entity
   */
  @Transactional
  public CustomerContact createContactForCustomer(Customer customer) {
    CustomerContact contact =
        ContactTestDataFactory.builder()
            .forCustomer(customer)
            .withEmail(
                "contact."
                    + customer.getCustomerNumber()
                    + "."
                    + System.currentTimeMillis()
                    + "@test.com")
            .build();

    if (customerContactRepository != null) {
      customerContactRepository.persistAndFlush(contact);
    } else {
      // Fallback via EntityManager
      entityManager.persist(contact);
      entityManager.flush();
    }

    return contact;
  }

  /** Reset the builder to default values. */
  private void reset() {
    this.customer = null;
    this.firstName = "Test";
    this.lastName = "Contact";
    this.email = null;
    this.phone = null;
    this.mobile = null;
    this.position = null;
    this.department = null;
    this.isPrimary = false;
    this.isActive = true;
    this.isDecisionMaker = false;
    this.roles = new HashSet<>();
    this.languagePreference = "DE";
    this.notes = null;
    this.reportsTo = null;
    this.createdAt = LocalDateTime.now();
    this.createdBy = "test-system";
    this.updatedAt = LocalDateTime.now();
    this.updatedBy = "test-system";
  }
}
