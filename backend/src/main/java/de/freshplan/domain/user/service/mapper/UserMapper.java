package de.freshplan.domain.user.service.mapper;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper for converting between User entities and DTOs.
 * 
 * This mapper handles the transformation of data between the domain
 * layer (entities) and the service layer (DTOs). It ensures that only
 * appropriate data is exposed through the API.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class UserMapper {
    
    /**
     * Converts a CreateUserRequest to a User entity.
     * 
     * @param request the creation request
     * @return new User entity
     */
    public User toEntity(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        
        return new User(
            request.getUsername(),
            request.getFirstName(),
            request.getLastName(),
            request.getEmail()
        );
    }
    
    /**
     * Updates an existing User entity with data from UpdateUserRequest.
     * 
     * @param user the user to update
     * @param request the update request
     */
    public void updateEntity(User user, UpdateUserRequest request) {
        if (user == null || request == null) {
            return;
        }
        
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        
        if (request.getEnabled()) {
            user.enable();
        } else {
            user.disable();
        }
    }
    
    /**
     * Converts a User entity to a UserResponse DTO.
     * 
     * @param user the user entity
     * @return user response DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}