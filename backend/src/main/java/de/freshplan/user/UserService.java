package de.freshplan.user;

import de.freshplan.user.dto.CreateUserRequest;
import de.freshplan.user.dto.UpdateUserRequest;
import de.freshplan.user.dto.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for User management operations.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class UserService {
    
    @Inject
    UserRepository repository;
    
    /**
     * Get all users.
     */
    public List<UserResponse> getAllUsers() {
        return repository.listAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get user by ID.
     */
    public UserResponse getUserById(UUID id) {
        User user = repository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
        return toResponse(user);
    }
    
    /**
     * Create new user.
     */
    public UserResponse createUser(CreateUserRequest request) {
        // Check for duplicate username
        if (repository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        
        // Check for duplicate email
        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        if (request.getRoles() != null) {
            user.setRoles(request.getRoles());
        }
        
        repository.persist(user);
        return toResponse(user);
    }
    
    /**
     * Update existing user.
     */
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = repository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
        
        // Update username if provided and check for duplicates
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (repository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        
        // Update email if provided and check for duplicates
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (repository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        
        // Update other fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getRoles() != null) {
            user.setRoles(request.getRoles());
        }
        
        repository.persist(user);
        return toResponse(user);
    }
    
    /**
     * Delete user.
     */
    public void deleteUser(UUID id) {
        if (!repository.deleteById(id)) {
            throw new NotFoundException("User not found with ID: " + id);
        }
    }
    
    /**
     * Enable user.
     */
    public void enableUser(UUID id) {
        User user = repository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
        user.setEnabled(true);
        repository.persist(user);
    }
    
    /**
     * Disable user.
     */
    public void disableUser(UUID id) {
        User user = repository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
        user.setEnabled(false);
        repository.persist(user);
    }
    
    /**
     * Convert User entity to UserResponse DTO.
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .enabled(user.isEnabled())
            .roles(user.getRoles())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}