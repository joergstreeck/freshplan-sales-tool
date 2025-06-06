package de.freshplan.domain.user.service.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UpdateUserRolesRequest DTO.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
class UpdateUserRolesRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void validation_withValidRoles_shouldPass() {
        // Given
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of("admin", "manager"));
        
        // When
        Set<ConstraintViolation<UpdateUserRolesRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).isEmpty();
    }
    
    @Test
    void validation_withNullRoles_shouldFail() {
        // Given
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(null);
        
        // When
        Set<ConstraintViolation<UpdateUserRolesRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Roles list cannot be null");
    }
    
    @Test
    void validation_withEmptyRoles_shouldFail() {
        // Given
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(List.of());
        
        // When
        Set<ConstraintViolation<UpdateUserRolesRequest>> violations = validator.validate(request);
        
        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("At least one role must be specified");
    }
    
    @Test
    void builder_shouldCreateValidRequest() {
        // Given
        List<String> roles = List.of("admin", "sales");
        
        // When
        UpdateUserRolesRequest request = UpdateUserRolesRequest.builder()
            .roles(roles)
            .build();
        
        // Then
        assertThat(request.getRoles()).isEqualTo(roles);
    }
    
    @Test
    void constructor_withRoles_shouldSetRoles() {
        // Given
        List<String> roles = List.of("manager");
        
        // When
        UpdateUserRolesRequest request = new UpdateUserRolesRequest(roles);
        
        // Then
        assertThat(request.getRoles()).isEqualTo(roles);
    }
    
    @Test
    void setRoles_shouldUpdateRoles() {
        // Given
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        List<String> roles = List.of("admin", "manager", "sales");
        
        // When
        request.setRoles(roles);
        
        // Then
        assertThat(request.getRoles()).isEqualTo(roles);
    }
}