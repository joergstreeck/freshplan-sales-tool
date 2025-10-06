package de.freshplan.test.builders;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.user.entity.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Validation tests for Phase 2A - Builder Core improvements. Verifies that builders generate
 * collision-free IDs and proper test data.
 */
@Tag("unit")
class BuilderValidationTest {

  @Test
  void customerBuilderGeneratesProperTestData() {
    // Given/When
    Customer customer =
        CustomerTestDataFactory.builder().withCompanyName("My Test Company").build();

    // Then - Validierung 3a
    assertThat(customer.getIsTestData()).isTrue();
    assertThat(customer.getCustomerNumber()).startsWith("TST-"); // Updated format (was KD-TEST-)
    assertThat(customer.getCompanyName()).isEqualTo("My Test Company");
    assertThat(customer.getStatus()).isNotNull();

    // Verify collision-free generation
    Customer customer2 = CustomerTestDataFactory.builder().build();
    assertThat(customer2.getCustomerNumber()).isNotEqualTo(customer.getCustomerNumber());
    assertThat(customer2.getCompanyName()).matches("\\[TEST.*\\].*");
  }

  @Test
  void userBuilderGeneratesProperTestData() {
    // Given/When
    User user = UserTestDataFactory.builder().withFirstName("John").withLastName("Doe").build();

    // Then - Validierung 3b
    assertThat(user.isTestData()).isTrue();
    assertThat(user.getEmail()).contains("@test.example.com");
    assertThat(user.getFirstName()).isEqualTo("John");
    assertThat(user.getLastName()).isEqualTo("Doe");
    assertThat(user.isEnabled()).isTrue();

    // Verify collision-free generation
    User user2 = UserTestDataFactory.builder().build();
    assertThat(user2.getEmail()).isNotEqualTo(user.getEmail());
    assertThat(user2.getUsername()).isNotEqualTo(user.getUsername());
  }

  @Test
  void customerBuilderWorksWithoutDatabase() {
    // Test dass Builder ohne Dependency Injection funktioniert
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Test GmbH")
            .withCustomerNumber("KD-MANUAL-001")
            .build();

    assertThat(customer).isNotNull();
    assertThat(customer.getIsTestData()).isTrue();
    assertThat(customer.getCustomerNumber()).isEqualTo("KD-MANUAL-001");
  }

  @Test
  void userBuilderWorksWithoutDatabase() {
    // Test dass Builder ohne Dependency Injection funktioniert
    User user =
        UserTestDataFactory.builder()
            .withUsername("manual.user")
            .withEmail("manual@test.com")
            .build();

    assertThat(user).isNotNull();
    assertThat(user.isTestData()).isTrue();
    // Username and email get a suffix added for uniqueness, so we check for prefix
    assertThat(user.getUsername()).startsWith("manual.user");
    assertThat(user.getEmail()).startsWith("manual");
  }
}
