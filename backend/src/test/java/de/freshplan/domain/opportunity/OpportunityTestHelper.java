package de.freshplan.domain.opportunity;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;

/** Helper class for creating test data in Opportunity tests */
public class OpportunityTestHelper {

  /** Creates a test customer if none exists */
  public static Customer createTestCustomer(CustomerRepository customerRepository) {
    // Try to find existing customer
    Customer customer = customerRepository.findAll().firstResult();
    if (customer != null) {
      return customer;
    }

    // Create new customer using repository
    customer = new Customer();
    customer.setCustomerNumber("TEST-" + System.currentTimeMillis());
    customer.setCompanyName("Test Company GmbH");
    // Set only the fields that exist in Customer entity
    // Street, postal code etc. might be in a separate address entity

    customerRepository.persist(customer);
    return customer;
  }

  /** Creates a test user if none exists */
  public static User createTestUser(UserRepository userRepository) {
    // Try to find specific test user
    User user = userRepository.find("username", "testuser").firstResult();
    if (user != null) {
      return user;
    }

    // Try any admin user
    user = userRepository.find("username", "admin").firstResult();
    if (user != null) {
      return user;
    }

    // Try any sales user
    user = userRepository.find("username", "sales").firstResult();
    if (user != null) {
      return user;
    }

    // Last resort - find any user
    user = userRepository.findAll().firstResult();
    if (user != null) {
      return user;
    }

    // Since User has protected constructor, we need to find an existing one
    // or use a different approach in tests
    throw new UnsupportedOperationException(
        "Cannot create User directly - no test users found in database");
  }
}
