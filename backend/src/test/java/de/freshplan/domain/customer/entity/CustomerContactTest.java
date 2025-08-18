package de.freshplan.domain.customer.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.test.builders.ContactTestDataFactory;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CustomerContact entity. Tests role management, hierarchy support, and business
 * logic.
 *
 * <p>Now uses TestDataBuilder pattern for creating test data.
 *
 * @since Migration Phase 4 - Quick Wins
 */
@QuarkusTest
@DisplayName("CustomerContact Entity Tests")
class CustomerContactTest {

  @Inject CustomerBuilder customerBuilder;

  // testRunId für Test-Isolation
  private final String testRunId = UUID.randomUUID().toString().substring(0, 8);

  private CustomerContact contact;
  private Customer customer;

  @BeforeEach
  void setUp() {
    // Verwende Builder statt direkte Konstruktoren
    customer =
        customerBuilder
            .withCompanyName("[TEST] Test Company " + testRunId)
            .build(); // Nur build(), nicht persist() für Unit Tests

    contact =
        ContactTestDataFactory.builder()
            .forCustomer(customer)
            .withFirstName("Max")
            .withLastName("Mustermann")
            .withEmail("max.mustermann@test.com")
            .withGeneratedId()
            .build(); // Nur build() für Entity-Tests
  }

  @Nested
  @DisplayName("Role Management Tests")
  class RoleManagementTests {

    @Test
    @DisplayName("Should add role successfully")
    void shouldAddRole() {
      // Act
      contact.addRole("SALES_MANAGER");

      // Assert
      assertThat(contact.getRoles()).contains("SALES_MANAGER");
      assertThat(contact.hasRole("SALES_MANAGER")).isTrue();
    }

    @Test
    @DisplayName("Should not add null or blank role")
    void shouldNotAddNullOrBlankRole() {
      // Act
      contact.addRole(null);
      contact.addRole("");
      contact.addRole("  ");

      // Assert
      assertThat(contact.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("Should remove role successfully")
    void shouldRemoveRole() {
      // Arrange
      contact.addRole("SALES_MANAGER");
      contact.addRole("DECISION_MAKER");

      // Act
      contact.removeRole("SALES_MANAGER");

      // Assert
      assertThat(contact.getRoles()).doesNotContain("SALES_MANAGER");
      assertThat(contact.getRoles()).contains("DECISION_MAKER");
    }

    @Test
    @DisplayName("Should check role case-insensitively")
    void shouldCheckRoleCaseInsensitively() {
      // Arrange
      contact.addRole("Sales_Manager");

      // Assert
      assertThat(contact.hasRole("SALES_MANAGER")).isTrue();
      assertThat(contact.hasRole("sales_manager")).isTrue();
      assertThat(contact.hasRole("Sales_Manager")).isTrue();
    }

    @Test
    @DisplayName("Should update decision maker status based on roles")
    void shouldUpdateDecisionMakerStatus() {
      // Initially not a decision maker
      assertThat(contact.getIsDecisionMaker()).isFalse();

      // Add decision maker role
      contact.addRole("DECISION_MAKER");
      assertThat(contact.getIsDecisionMaker()).isTrue();

      // Test with German role
      contact.removeRole("DECISION_MAKER");
      contact.addRole("GESCHÄFTSFÜHRER");
      assertThat(contact.getIsDecisionMaker()).isTrue();

      // Test with CEO role
      contact.setRoles(new HashSet<>());
      contact.addRole("CEO");
      assertThat(contact.getIsDecisionMaker()).isTrue();

      // Remove all decision maker roles
      contact.removeRole("CEO");
      assertThat(contact.getIsDecisionMaker()).isFalse();
    }

    @Test
    @DisplayName("Should handle multiple roles")
    void shouldHandleMultipleRoles() {
      // Arrange & Act
      contact.addRole("SALES_MANAGER");
      contact.addRole("TECHNICAL_CONTACT");
      contact.addRole("DECISION_MAKER");

      // Assert
      assertThat(contact.getRoles()).hasSize(3);
      assertThat(contact.hasRole("SALES_MANAGER")).isTrue();
      assertThat(contact.hasRole("TECHNICAL_CONTACT")).isTrue();
      assertThat(contact.hasRole("DECISION_MAKER")).isTrue();
      assertThat(contact.getIsDecisionMaker()).isTrue();
    }
  }

  @Nested
  @DisplayName("Hierarchy Tests")
  class HierarchyTests {

    private CustomerContact supervisor;
    private CustomerContact manager;
    private CustomerContact employee;

    @BeforeEach
    void setUpHierarchy() {
      // Verwende Builder für die Hierarchie-Erstellung
      supervisor =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("Big")
              .withLastName("Boss")
              .withGeneratedId()
              .build();

      manager =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("Middle")
              .withLastName("Manager")
              .withReportsTo(supervisor)
              .withGeneratedId()
              .build();

      employee =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("Regular")
              .withLastName("Employee")
              .withReportsTo(manager)
              .withGeneratedId()
              .build();
    }

    @Test
    @DisplayName("Should identify direct subordinate relationship")
    void shouldIdentifyDirectSubordinate() {
      assertThat(manager.isSubordinateOf(supervisor)).isTrue();
      assertThat(employee.isSubordinateOf(manager)).isTrue();
    }

    @Test
    @DisplayName("Should identify indirect subordinate relationship")
    void shouldIdentifyIndirectSubordinate() {
      assertThat(employee.isSubordinateOf(supervisor)).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-subordinate")
    void shouldReturnFalseForNonSubordinate() {
      assertThat(supervisor.isSubordinateOf(manager)).isFalse();
      assertThat(supervisor.isSubordinateOf(employee)).isFalse();
      assertThat(manager.isSubordinateOf(employee)).isFalse();
    }

    @Test
    @DisplayName("Should handle null reports-to")
    void shouldHandleNullReportsTo() {
      CustomerContact independent =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("Independent")
              .withLastName("Worker")
              .withGeneratedId()
              .build();

      assertThat(independent.isSubordinateOf(supervisor)).isFalse();
    }

    @Test
    @DisplayName("Should prevent circular hierarchy")
    void shouldPreventCircularHierarchy() {
      // This should be prevented at service layer
      // Test that the method doesn't enter infinite loop
      assertThat(supervisor.isSubordinateOf(supervisor)).isFalse();
    }
  }

  @Nested
  @DisplayName("Display Name Tests")
  class DisplayNameTests {

    @Test
    @DisplayName("Should return full name when both names present")
    void shouldReturnFullName() {
      assertThat(contact.getFullName()).isEqualTo("Max Mustermann");
    }

    @Test
    @DisplayName("Should return first name when last name missing")
    void shouldReturnFirstNameWhenLastNameMissing() {
      contact.setLastName(null);
      assertThat(contact.getFullName()).isEqualTo("Max");
    }

    @Test
    @DisplayName("Should return last name when first name missing")
    void shouldReturnLastNameWhenFirstNameMissing() {
      contact.setFirstName(null);
      assertThat(contact.getFullName()).isEqualTo("Mustermann");
    }

    @Test
    @DisplayName("Should return empty string when both names missing")
    void shouldReturnEmptyStringWhenBothNamesMissing() {
      contact.setFirstName(null);
      contact.setLastName(null);
      assertThat(contact.getFullName()).isEqualTo("");
    }
  }

  @Nested
  @DisplayName("Contact Status Tests")
  class ContactStatusTests {

    @Test
    @DisplayName("Should be active by default")
    void shouldBeActiveByDefault() {
      CustomerContact newContact =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("Default")
              .withLastName("Contact")
              .build();
      assertThat(newContact.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should not be primary by default")
    void shouldNotBePrimaryByDefault() {
      CustomerContact newContact =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("Default")
              .withLastName("Contact")
              .build();
      assertThat(newContact.getIsPrimary()).isFalse();
    }

    @Test
    @DisplayName("Should not be decision maker by default")
    void shouldNotBeDecisionMakerByDefault() {
      CustomerContact newContact =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("Default")
              .withLastName("Contact")
              .build();
      assertThat(newContact.getIsDecisionMaker()).isFalse();
    }

    @Test
    @DisplayName("Should update status flags correctly")
    void shouldUpdateStatusFlags() {
      contact.setIsPrimary(true);
      contact.setIsActive(false);

      assertThat(contact.getIsPrimary()).isTrue();
      assertThat(contact.getIsActive()).isFalse();
    }
  }

  @Nested
  @DisplayName("Communication Preference Tests")
  class CommunicationPreferenceTests {

    @Test
    @DisplayName("Should have DE as default language preference")
    void shouldHaveDEAsDefaultLanguagePreference() {
      CustomerContact newContact =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("German")
              .withLastName("Contact")
              .build();
      assertThat(newContact.getLanguagePreference()).isEqualTo("DE");
    }

    @Test
    @DisplayName("Should update language preference")
    void shouldUpdateLanguagePreference() {
      contact.setLanguagePreference("EN");
      assertThat(contact.getLanguagePreference()).isEqualTo("EN");
    }
  }

  @Nested
  @DisplayName("Contact Information Tests")
  class ContactInformationTests {

    @Test
    @DisplayName("Should store all contact details")
    void shouldStoreAllContactDetails() {
      // Arrange
      contact.setPosition("Sales Manager");
      contact.setDepartment("Sales");
      contact.setPhone("+49 30 12345678");
      contact.setMobile("+49 170 1234567");
      contact.setFax("+49 30 12345679");

      // Assert
      assertThat(contact.getPosition()).isEqualTo("Sales Manager");
      assertThat(contact.getDepartment()).isEqualTo("Sales");
      assertThat(contact.getPhone()).isEqualTo("+49 30 12345678");
      assertThat(contact.getMobile()).isEqualTo("+49 170 1234567");
      assertThat(contact.getFax()).isEqualTo("+49 30 12345679");
    }

    @Test
    @DisplayName("Should handle null values in optional fields")
    void shouldHandleNullValuesInOptionalFields() {
      // These fields are optional
      contact.setPosition(null);
      contact.setDepartment(null);
      contact.setPhone(null);
      contact.setMobile(null);
      contact.setFax(null);

      // Should not throw exceptions
      assertThat(contact.getPosition()).isNull();
      assertThat(contact.getDepartment()).isNull();
      assertThat(contact.getPhone()).isNull();
      assertThat(contact.getMobile()).isNull();
      assertThat(contact.getFax()).isNull();
    }
  }

  @Nested
  @DisplayName("Audit Fields Tests")
  class AuditFieldsTests {

    @Test
    @DisplayName("Should track creation and update timestamps")
    void shouldTrackTimestamps() {
      LocalDateTime now = LocalDateTime.now();
      contact.setCreatedAt(now);
      contact.setUpdatedAt(now.plusHours(1));

      assertThat(contact.getCreatedAt()).isEqualTo(now);
      assertThat(contact.getUpdatedAt()).isEqualTo(now.plusHours(1));
    }

    @Test
    @DisplayName("Should track created and updated by users")
    void shouldTrackUsers() {
      contact.setCreatedBy("admin");
      contact.setUpdatedBy("sales_user");

      assertThat(contact.getCreatedBy()).isEqualTo("admin");
      assertThat(contact.getUpdatedBy()).isEqualTo("sales_user");
    }
  }

  @Nested
  @DisplayName("Business Rules Tests")
  class BusinessRulesTests {

    @Test
    @DisplayName("Should require customer association")
    void shouldRequireCustomerAssociation() {
      assertThat(contact.getCustomer()).isNotNull();
      assertThat(contact.getCustomer().getCompanyName()).contains("Test Company");
    }

    @Test
    @DisplayName("Should have unique ID")
    void shouldHaveUniqueId() {
      CustomerContact contact2 =
          ContactTestDataFactory.builder()
              .forCustomer(customer)
              .withFirstName("Another")
              .withLastName("Contact")
              .withGeneratedId()
              .build();

      assertThat(contact.getId()).isNotNull();
      assertThat(contact2.getId()).isNotNull();
      assertThat(contact.getId()).isNotEqualTo(contact2.getId());
    }

    @Test
    @DisplayName("Should support notes field")
    void shouldSupportNotesField() {
      String notes = "Important customer contact. Prefers morning calls.";
      contact.setNotes(notes);

      assertThat(contact.getNotes()).isEqualTo(notes);
    }

    @Test
    @DisplayName("Should handle role sets correctly")
    void shouldHandleRoleSetsCorrectly() {
      Set<String> roles = new HashSet<>();
      roles.add("TECHNICAL_LEAD");
      roles.add("DECISION_MAKER");

      contact.setRoles(roles);

      assertThat(contact.getRoles()).hasSize(2);
      assertThat(contact.getRoles()).contains("TECHNICAL_LEAD", "DECISION_MAKER");
      assertThat(contact.getIsDecisionMaker()).isTrue();
    }
  }
}
