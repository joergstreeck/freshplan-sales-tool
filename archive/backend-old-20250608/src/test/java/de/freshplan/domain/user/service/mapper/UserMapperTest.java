package de.freshplan.domain.user.service.mapper;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserMapper.
 * 
 * Tests the conversion between User entities and DTOs.
 * Follows the principle of short, readable code lines.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
class UserMapperTest {
    
    private UserMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }
    
    @Test
    void testToEntity_WithValidRequest_ShouldCreateUser() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@freshplan.de")
                .build();
        
        // When
        User user = mapper.toEntity(request);
        
        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john.doe");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@freshplan.de");
        assertThat(user.isEnabled()).isTrue();
    }
    
    @Test
    void testToEntity_WithNullRequest_ShouldReturnNull() {
        // When
        User user = mapper.toEntity(null);
        
        // Then
        assertThat(user).isNull();
    }
    
    @Test
    void testUpdateEntity_WithValidRequest_ShouldUpdateUser() {
        // Given
        User user = createTestUser();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("jane.doe")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@freshplan.de")
                .enabled(false)
                .build();
        
        // When
        mapper.updateEntity(user, request);
        
        // Then
        assertThat(user.getUsername()).isEqualTo("jane.doe");
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getEmail()).isEqualTo("jane.smith@freshplan.de");
        assertThat(user.isEnabled()).isFalse();
    }
    
    @Test
    void testUpdateEntity_WithEnabledTrue_ShouldEnableUser() {
        // Given
        User user = createTestUser();
        user.disable();
        
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .enabled(true)
                .build();
        
        // When
        mapper.updateEntity(user, request);
        
        // Then
        assertThat(user.isEnabled()).isTrue();
    }
    
    @Test
    void testUpdateEntity_WithNullUser_ShouldNotThrow() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("test")
                .firstName("Test")
                .lastName("User")
                .email("test@freshplan.de")
                .enabled(true)
                .build();
        
        // When/Then - should not throw
        mapper.updateEntity(null, request);
    }
    
    @Test
    void testUpdateEntity_WithNullRequest_ShouldNotThrow() {
        // Given
        User user = createTestUser();
        
        // When/Then - should not throw
        mapper.updateEntity(user, null);
    }
    
    @Test
    void testToResponse_WithValidUser_ShouldCreateResponse() {
        // Given
        User user = createTestUserWithId();
        
        // When
        UserResponse response = mapper.toResponse(user);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(response.getLastName()).isEqualTo(user.getLastName());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.isEnabled()).isEqualTo(user.isEnabled());
        assertThat(response.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(response.getUpdatedAt()).isEqualTo(user.getUpdatedAt());
    }
    
    @Test
    void testToResponse_WithNullUser_ShouldReturnNull() {
        // When
        UserResponse response = mapper.toResponse(null);
        
        // Then
        assertThat(response).isNull();
    }
    
    // Helper methods
    
    private User createTestUser() {
        return new User(
            "john.doe",
            "John",
            "Doe",
            "john.doe@freshplan.de"
        );
    }
    
    private User createTestUserWithId() {
        User user = createTestUser();
        
        // Use reflection to set private fields for testing
        setPrivateField(user, "id", UUID.randomUUID());
        setPrivateField(user, "createdAt", Instant.now());
        setPrivateField(user, "updatedAt", Instant.now());
        
        return user;
    }
    
    private void setPrivateField(
            Object target, 
            String fieldName, 
            Object value) {
        try {
            var field = target.getClass()
                    .getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to set field: " + fieldName, 
                e
            );
        }
    }
}