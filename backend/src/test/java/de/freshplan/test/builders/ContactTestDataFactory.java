package de.freshplan.test.builders;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Test data factory for CustomerContact entities. Provides builder pattern for creating test
 * contacts without CDI.
 *
 * @author Claude
 * @since Migration Phase 4 - Quick Wins (Fixed)
 */
public class ContactTestDataFactory {

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
    private String createdBy = "test";
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String updatedBy = "test";

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

      return contact;
    }

    /**
     * Create a mock customer for unit tests that don't need persistence.
     *
     * @return A mock customer entity with test data
     */
    private Customer createMockCustomer() {
      Customer mockCustomer = new Customer();
      mockCustomer.setId(UUID.randomUUID());
      mockCustomer.setCompanyName("[TEST] Mock Customer for Contact");
      mockCustomer.setCustomerNumber("TEST-" + UUID.randomUUID().toString().substring(0, 8));
      mockCustomer.setIsTestData(true);
      mockCustomer.setCreatedAt(LocalDateTime.now());
      mockCustomer.setCreatedBy("test");
      mockCustomer.setUpdatedAt(LocalDateTime.now());
      mockCustomer.setUpdatedBy("test");
      return mockCustomer;
    }
  }
}
