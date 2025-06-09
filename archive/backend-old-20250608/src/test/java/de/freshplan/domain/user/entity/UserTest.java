package de.freshplan.domain.user.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User entity.
 * 
 * Tests business logic and entity behavior.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
class UserTest {
    
    @Test
    void testConstructor_ShouldCreateEnabledUser() {
        // When
        User user = new User(
            "john.doe",
            "John",
            "Doe",
            "john.doe@freshplan.de"
        );
        
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
        // When
        User user = new User();
        
        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.isEnabled()).isFalse();
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
}