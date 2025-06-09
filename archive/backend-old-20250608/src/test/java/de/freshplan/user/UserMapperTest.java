package de.freshplan.user;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.mapper.UserMapper;
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
    void toEntity_withValidRequest_shouldMapCorrectly() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john.doe");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        
        // When
        User user = mapper.toEntity(request);
        
        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john.doe");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.isEnabled()).isTrue(); // default value
        assertThat(user.getId()).isNotNull(); // auto-generated
    }
    
    @Test
    void toEntity_withNullRequest_shouldReturnNull() {
        // When
        User user = mapper.toEntity(null);
        
        // Then
        assertThat(user).isNull();
    }
    
    @Test
    void updateEntity_withValidData_shouldUpdateAllFields() {
        // Given
        User user = new User("old.username", "Old", "Name", "old@example.com");
        
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("new.username")
                .firstName("New")
                .lastName("Name")
                .email("new@example.com")
                .enabled(false)
                .build();
        
        // When
        mapper.updateEntity(user, request);
        
        // Then
        assertThat(user.getUsername()).isEqualTo("new.username");
        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.isEnabled()).isFalse();
    }
    
    @Test
    void updateEntity_withEnabledTrue_shouldEnableUser() {
        // Given
        User user = new User("username", "First", "Last", "email@example.com");
        user.disable(); // Start with disabled user
        
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("username")
                .firstName("First")
                .lastName("Last")
                .email("email@example.com")
                .enabled(true)
                .build();
        
        // When
        mapper.updateEntity(user, request);
        
        // Then
        assertThat(user.isEnabled()).isTrue();
    }
    
    @Test
    void updateEntity_withEnabledFalse_shouldDisableUser() {
        // Given
        User user = new User("username", "First", "Last", "email@example.com");
        // User is enabled by default
        
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("username")
                .firstName("First")
                .lastName("Last")
                .email("email@example.com")
                .enabled(false)
                .build();
        
        // When
        mapper.updateEntity(user, request);
        
        // Then
        assertThat(user.isEnabled()).isFalse();
    }
    
    @Test
    void updateEntity_withNullUser_shouldNotThrow() {
        // Given
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("username")
                .firstName("First")
                .lastName("Last")
                .email("email@example.com")
                .enabled(true)
                .build();
        
        // When & Then - should not throw
        mapper.updateEntity(null, request);
    }
    
    @Test
    void updateEntity_withNullRequest_shouldNotThrow() {
        // Given
        User user = new User("username", "First", "Last", "email@example.com");
        
        // When & Then - should not throw
        mapper.updateEntity(user, null);
    }
    
    @Test
    void toResponse_withCompleteUser_shouldMapAllFields() {
        // Given
        User user = new User("john.doe", "John", "Doe", "john.doe@example.com");
        UUID userId = user.getId();
        Instant createdAt = user.getCreatedAt();
        Instant updatedAt = user.getUpdatedAt();
        
        // When
        UserResponse response = mapper.toResponse(user);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("john.doe");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.isEnabled()).isTrue();
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }
    
    @Test
    void toResponse_withDisabledUser_shouldMapEnabledAsFalse() {
        // Given
        User user = new User("john.doe", "John", "Doe", "john.doe@example.com");
        user.disable();
        
        // When
        UserResponse response = mapper.toResponse(user);
        
        // Then
        assertThat(response.isEnabled()).isFalse();
    }
    
    @Test
    void toResponse_withNullUser_shouldReturnNull() {
        // When
        UserResponse response = mapper.toResponse(null);
        
        // Then
        assertThat(response).isNull();
    }
}