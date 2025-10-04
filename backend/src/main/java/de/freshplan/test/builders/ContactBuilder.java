package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.CommunicationMethod;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.test.utils.TestDataUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder for creating test CustomerContact entities. Provides fluent API for setting contact
 * properties and predefined roles.
 *
 * <p>This builder ensures all test contacts are properly associated with customers and marked for
 * cleanup after tests.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class ContactBuilder {

  @Inject ContactRepository customerContactRepository;

  @Inject CustomerRepository customerRepository;

  // Required fields
  private Customer customer = null;
  private String firstName = "Test";
  private String lastName = "Contact";

  // Optional fields
  private String title = null;
  private String position = "Mitarbeiter";
  private String department = null;

  // Contact details
  private String email = null;
  private String phone = null;
  private String mobile = null;
  private String fax = null;

  // Contact preferences
  private CommunicationMethod preferredCommunicationMethod = CommunicationMethod.EMAIL;
  private String languagePreference = "DE";

  // Flags
  private Boolean isPrimary = false;
  private Boolean isDecisionMaker = false;
  private Boolean isActive = true;

  // Additional info
  private String notes = null;

  /**
   * Resets the builder to default values for creating a new contact.
   *
   * @return this builder instance for chaining
   */
  public ContactBuilder reset() {
    this.customer = null;
    this.firstName = "Test";
    this.lastName = "Contact";
    this.title = null;
    this.position = "Mitarbeiter";
    this.department = null;
    this.email = null;
    this.phone = null;
    this.mobile = null;
    this.fax = null;
    this.preferredCommunicationMethod = CommunicationMethod.EMAIL;
    this.languagePreference = "DE";
    this.isPrimary = false;
    this.isDecisionMaker = false;
    this.isActive = true;
    this.notes = null;
    return this;
  }

  // Customer association (REQUIRED)
  public ContactBuilder forCustomer(Customer customer) {
    this.customer = customer;
    return this;
  }

  public ContactBuilder forCustomerId(UUID customerId) {
    this.customer = customerRepository.findById(customerId);
    if (this.customer == null) {
      throw new IllegalArgumentException("Customer not found: " + customerId);
    }
    return this;
  }

  // Basic setters
  public ContactBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public ContactBuilder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public ContactBuilder withFullName(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
    return this;
  }

  public ContactBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public ContactBuilder withPosition(String position) {
    this.position = position;
    return this;
  }

  public ContactBuilder withDepartment(String department) {
    this.department = department;
    return this;
  }

  // Contact details setters
  public ContactBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public ContactBuilder withPhone(String phone) {
    this.phone = phone;
    return this;
  }

  public ContactBuilder withMobile(String mobile) {
    this.mobile = mobile;
    return this;
  }

  public ContactBuilder withFax(String fax) {
    this.fax = fax;
    return this;
  }

  // Preference setters
  public ContactBuilder withPreferredCommunication(CommunicationMethod method) {
    this.preferredCommunicationMethod = method;
    return this;
  }

  public ContactBuilder withLanguagePreference(String language) {
    this.languagePreference = language;
    return this;
  }

  // Flag setters
  public ContactBuilder asPrimaryContact() {
    this.isPrimary = true;
    this.isDecisionMaker = true;
    this.isActive = true;
    return this;
  }

  public ContactBuilder asDecisionMaker() {
    this.isDecisionMaker = true;
    return this;
  }

  public ContactBuilder asInactive() {
    this.isActive = false;
    return this;
  }

  // Additional info setters
  public ContactBuilder withNotes(String notes) {
    this.notes = notes;
    return this;
  }

  // Predefined scenarios
  public ContactBuilder asCEO() {
    this.title = "Dr.";
    this.position = "Geschäftsführer";
    this.department = "Geschäftsleitung";
    this.isPrimary = true;
    this.isDecisionMaker = true;
    this.preferredCommunicationMethod = CommunicationMethod.PHONE;
    return this;
  }

  public ContactBuilder asPurchasingManager() {
    this.position = "Einkaufsleiter";
    this.department = "Einkauf";
    this.isDecisionMaker = true;
    this.preferredCommunicationMethod = CommunicationMethod.EMAIL;
    return this;
  }

  public ContactBuilder asSalesContact() {
    this.position = "Vertriebsmitarbeiter";
    this.department = "Vertrieb";
    this.preferredCommunicationMethod = CommunicationMethod.EMAIL;
    return this;
  }

  public ContactBuilder asAccountingContact() {
    this.position = "Buchhalter";
    this.department = "Buchhaltung";
    this.preferredCommunicationMethod = CommunicationMethod.EMAIL;
    return this;
  }

  public ContactBuilder asTechnicalContact() {
    this.position = "Technischer Leiter";
    this.department = "IT";
    this.preferredCommunicationMethod = CommunicationMethod.TEAMS;
    return this;
  }

  /**
   * Builds a CustomerContact entity WITHOUT persisting to database. Use this for unit tests or when
   * you need an entity without DB interaction.
   *
   * @return a new CustomerContact entity with test data markers
   */
  public CustomerContact build() {
    if (customer == null) {
      throw new IllegalStateException(
          "Customer is required for ContactBuilder. Use forCustomer() first.");
    }

    CustomerContact contact = new CustomerContact();
    String id = TestDataUtils.uniqueId();

    // Set required fields
    contact.setCustomer(customer);
    contact.setFirstName(firstName);
    contact.setLastName(lastName);

    // Generate email if not set
    if (email == null) {
      email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test-" + id + ".com";
    }
    contact.setEmail(email);

    // Set optional fields
    contact.setTitle(title);
    contact.setPosition(position);
    contact.setDepartment(department);

    // Set contact details
    contact.setPhone(phone);
    contact.setMobile(mobile);
    contact.setFax(fax);

    // Set preferences
    contact.setPreferredCommunicationMethod(preferredCommunicationMethod);
    contact.setLanguagePreference(languagePreference);

    // Set flags
    contact.setIsPrimary(isPrimary);
    contact.setIsDecisionMaker(isDecisionMaker);
    contact.setIsActive(isActive);

    // Set additional info
    contact.setNotes(notes);

    // Set deletion flag
    contact.setIsDeleted(false);

    // Set audit fields
    contact.setCreatedAt(LocalDateTime.now());
    contact.setCreatedBy("test-builder");

    // Reset builder state after use to prevent state leaking between tests
    // This is critical because the builder is @ApplicationScoped (singleton)
    reset();

    return contact;
  }

  /**
   * Builds and persists a CustomerContact entity to the database. Use this for integration tests
   * that need DB interaction.
   *
   * @return the persisted CustomerContact entity
   */
  @Transactional
  public CustomerContact persist() {
    CustomerContact contact = build(); // build() now automatically resets
    customerContactRepository.persist(contact);
    customerContactRepository.flush(); // Force immediate constraint validation
    return contact;
  }
}
