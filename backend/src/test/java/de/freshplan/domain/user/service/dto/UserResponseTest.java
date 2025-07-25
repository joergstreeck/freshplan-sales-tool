package de.freshplan.domain.user.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserResponse DTO.
 *
 * <p>Tests immutability, builder pattern, and JSON serialization.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class UserResponseTest {

  @Inject ObjectMapper objectMapper;

  @Test
  void testBuilder_ShouldCreateValidResponse() {
    // Given
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();

    // When
    UserResponse response =
        UserResponse.builder()
            .id(id)
            .username("john.doe")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@freshplan.de")
            .enabled(true)
            .createdAt(now)
            .updatedAt(now)
            .build();

    // Then
    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getUsername()).isEqualTo("john.doe");
    assertThat(response.getFirstName()).isEqualTo("John");
    assertThat(response.getLastName()).isEqualTo("Doe");
    assertThat(response.getEmail()).isEqualTo("john.doe@freshplan.de");
    assertThat(response.isEnabled()).isTrue();
    assertThat(response.getCreatedAt()).isEqualTo(now);
    assertThat(response.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  void testJsonSerialization() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();

    UserResponse response =
        UserResponse.builder()
            .id(id)
            .username("test.user")
            .firstName("Test")
            .lastName("User")
            .email("test@freshplan.de")
            .enabled(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

    // When
    String json = objectMapper.writeValueAsString(response);
    UserResponse deserialized = objectMapper.readValue(json, UserResponse.class);

    // Then
    assertThat(deserialized.getId()).isEqualTo(id);
    assertThat(deserialized.getUsername()).isEqualTo("test.user");
    assertThat(deserialized.getFirstName()).isEqualTo("Test");
    assertThat(deserialized.getLastName()).isEqualTo("User");
    assertThat(deserialized.getEmail()).isEqualTo("test@freshplan.de");
    assertThat(deserialized.isEnabled()).isFalse();

    // Timestamps might lose precision in serialization
    assertThat(deserialized.getCreatedAt())
        .isCloseTo(now, within(1, java.time.temporal.ChronoUnit.SECONDS));
    assertThat(deserialized.getUpdatedAt())
        .isCloseTo(now, within(1, java.time.temporal.ChronoUnit.SECONDS));
  }

  @Test
  void testImmutability() {
    // Given
    UserResponse response =
        UserResponse.builder()
            .id(UUID.randomUUID())
            .username("immutable")
            .firstName("Test")
            .lastName("User")
            .email("immutable@freshplan.de")
            .enabled(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    // Then - verify all fields are final
    // This test ensures the DTO is immutable by design
    assertThat(response.getClass().getDeclaredFields())
        .allMatch(field -> java.lang.reflect.Modifier.isFinal(field.getModifiers()));
  }
}
