package de.freshplan.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.test.builders.UserTestDataFactory;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for User entity.
 *
 * <p>Tests business logic and entity behavior.
 *
 * <p>Now uses TestDataBuilder pattern for creating test data.
 *
 * @author FreshPlan Team
 * @since 2.0.0 - Updated in Migration Phase 4
 */
class UserTest {

  @Test
  void testConstructor_ShouldCreateEnabledUser() {
    // When
    User user =
        UserTestDataFactory.builder()
            .withUsername("john.doe")
            .withFirstName("John")
            .withLastName("Doe")
            .withEmail("john.doe@freshplan.de")
            .build();

    // Then
    assertThat(user.getUsername()).isEqualTo("john.doe");
    assertThat(user.getFirstName()).isEqualTo("John");
    assertThat(user.getLastName()).isEqualTo("Doe");
    assertThat(user.getEmail()).isEqualTo("john.doe@freshplan.de");
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  void testEnable_ShouldSetEnabledTrue() {
    // Given
    User user = createTestUser();
    user.disable();

    // When
    user.enable();

    // Then
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  void testDisable_ShouldSetEnabledFalse() {
    // Given
    User user = createTestUser();

    // When
    user.disable();

    // Then
    assertThat(user.isEnabled()).isFalse();
  }

  @Test
  void testGetFullName_ShouldCombineNames() {
    // Given
    User user = createTestUser();

    // When
    String fullName = user.getFullName();

    // Then
    assertThat(fullName).isEqualTo("John Doe");
  }

  @Test
  void testSetters_ShouldUpdateFields() {
    // Given
    User user = createTestUser();

    // When
    user.setUsername("jane.smith");
    user.setFirstName("Jane");
    user.setLastName("Smith");
    user.setEmail("jane.smith@freshplan.de");

    // Then
    assertThat(user.getUsername()).isEqualTo("jane.smith");
    assertThat(user.getFirstName()).isEqualTo("Jane");
    assertThat(user.getLastName()).isEqualTo("Smith");
    assertThat(user.getEmail()).isEqualTo("jane.smith@freshplan.de");
  }

  @Test
  void testDefaultConstructor_ShouldCreateEmptyUser() {
    // When - Since User has no accessible default constructor,
    // we test minimal user creation instead
    User user =
        UserTestDataFactory.builder()
            .withUsername("empty")
            .withFirstName("")
            .withLastName("")
            .withEmail("empty@test.de")
            .asDisabled()
            .build();

    // Then
    assertThat(user).isNotNull();
    assertThat(user.getUsername()).isEqualTo("empty");
    assertThat(user.isEnabled()).isFalse();
  }

  // Helper methods

  private User createTestUser() {
    return UserTestDataFactory.builder()
        .withUsername("john.doe")
        .withFirstName("John")
        .withLastName("Doe")
        .withEmail("john.doe@freshplan.de")
        .build();
  }
}
