package de.freshplan.domain.user.service;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.exception.DuplicateEmailException;
import de.freshplan.domain.user.service.exception.DuplicateUsernameException;
import de.freshplan.domain.user.service.exception.UserAlreadyExistsException;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import de.freshplan.domain.user.service.mapper.UserMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for User management operations.
 * 
 * This service encapsulates the business logic for user management,
 * providing a clean API for user operations while handling validation,
 * error cases, and data transformation.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class UserService {
    
    private static final Logger LOG = Logger.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Inject
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    
    /**
     * Creates a new user.
     * 
     * @param request the user creation request
     * @return the created user response
     * @throws UserAlreadyExistsException if username or email already exists
     */
    public UserResponse createUser(@Valid @NotNull CreateUserRequest request) {
        LOG.debugf("Creating new user with username: %s", request.getUsername());
        
        // Check for existing username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException(request.getUsername());
        }
        
        // Check for existing email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        
        // Create and persist user
        User user = userMapper.toEntity(request);
        userRepository.persist(user);
        
        LOG.infof("User created successfully with ID: %s", user.getId());
        return userMapper.toResponse(user);
    }
    
    /**
     * Updates an existing user.
     * 
     * @param id the user ID
     * @param request the update request
     * @return the updated user response
     * @throws UserNotFoundException if user not found
     * @throws UserAlreadyExistsException if new username/email already exists
     */
    public UserResponse updateUser(@NotNull UUID id, @Valid @NotNull UpdateUserRequest request) {
        LOG.debugf("Updating user with ID: %s", id);
        
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        // Check username uniqueness if changed
        if (!user.getUsername().equals(request.getUsername()) &&
            userRepository.existsByUsernameExcluding(request.getUsername(), id)) {
            throw new DuplicateUsernameException(request.getUsername());
        }
        
        // Check email uniqueness if changed
        if (!user.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmailExcluding(request.getEmail(), id)) {
            throw new DuplicateEmailException(request.getEmail());
        }
        
        // Update user
        userMapper.updateEntity(user, request);
        // User is already managed, no need to persist
        
        LOG.infof("User updated successfully with ID: %s", id);
        return userMapper.toResponse(user);
    }
    
    /**
     * Retrieves a user by ID.
     * 
     * @param id the user ID
     * @return the user response
     * @throws UserNotFoundException if user not found
     */
    public UserResponse getUser(@NotNull UUID id) {
        LOG.debugf("Retrieving user with ID: %s", id);
        
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Retrieves a user by username.
     * 
     * @param username the username
     * @return the user response
     * @throws UserNotFoundException if user not found
     */
    public UserResponse getUserByUsername(@NotNull String username) {
        LOG.debugf("Retrieving user with username: %s", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Lists all users with pagination.
     * 
     * @param pageIndex the page index (0-based)
     * @param pageSize the page size
     * @return list of user responses
     */
    public List<UserResponse> listUsers(int pageIndex, int pageSize) {
        LOG.debugf("Listing users - page: %d, size: %d", pageIndex, pageSize);
        
        Page page = Page.of(pageIndex, pageSize);
        List<User> users = userRepository.findAll()
                .page(page)
                .list();
        
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lists enabled users with pagination.
     * 
     * @param pageIndex the page index (0-based)
     * @param pageSize the page size
     * @return list of enabled user responses
     */
    public List<UserResponse> listEnabledUsers(int pageIndex, int pageSize) {
        LOG.debugf("Listing enabled users - page: %d, size: %d", pageIndex, pageSize);
        
        Page page = Page.of(pageIndex, pageSize);
        List<User> users = userRepository.findEnabledUsers(page);
        
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Searches for users by search term.
     * 
     * @param searchTerm the search term
     * @param pageIndex the page index
     * @param pageSize the page size
     * @return list of matching user responses
     */
    public List<UserResponse> searchUsers(String searchTerm, int pageIndex, int pageSize) {
        LOG.debugf("Searching users with term: %s", searchTerm);
        
        Page page = Page.of(pageIndex, pageSize);
        List<User> users = userRepository.search(searchTerm, page);
        
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Deletes a user.
     * 
     * @param id the user ID
     * @throws UserNotFoundException if user not found
     */
    public void deleteUser(@NotNull UUID id) {
        LOG.debugf("Deleting user with ID: %s", id);
        
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        userRepository.delete(user);
        
        LOG.infof("User deleted successfully with ID: %s", id);
    }
    
    /**
     * Retrieves a user by ID.
     * Alias for getUser for backward compatibility.
     * 
     * @param id the user ID
     * @return the user response
     * @throws UserNotFoundException if user not found
     */
    public UserResponse getUserById(@NotNull UUID id) {
        return getUser(id);
    }
    
    /**
     * Retrieves all users.
     * 
     * @return list of all user responses
     */
    public List<UserResponse> getAllUsers() {
        LOG.debug("Retrieving all users");
        
        List<User> users = userRepository.listAll();
        
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds a user by email.
     * 
     * @param email the email address
     * @return Optional containing the user response if found
     */
    public java.util.Optional<UserResponse> findByEmail(String email) {
        LOG.debugf("Finding user by email: %s", email);
        
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse);
    }
    
    /**
     * Enables a user and returns the updated user.
     * 
     * @param id the user ID
     * @return the updated user response
     * @throws UserNotFoundException if user not found
     */
    public UserResponse enableUser(@NotNull UUID id) {
        LOG.debugf("Enabling user with ID: %s", id);
        
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.enable();
        // User is already managed, no need to persist
        userRepository.flush();
        
        LOG.infof("User enabled successfully with ID: %s", id);
        return userMapper.toResponse(user);
    }
    
    /**
     * Disables a user and returns the updated user.
     * 
     * @param id the user ID
     * @return the updated user response
     * @throws UserNotFoundException if user not found
     */
    public UserResponse disableUser(@NotNull UUID id) {
        LOG.debugf("Disabling user with ID: %s", id);
        
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.disable();
        // User is already managed, no need to persist
        userRepository.flush();
        
        LOG.infof("User disabled successfully with ID: %s", id);
        return userMapper.toResponse(user);
    }
    
    /**
     * Counts total users.
     * 
     * @return total user count
     */
    public long countUsers() {
        return userRepository.count();
    }
    
    /**
     * Counts enabled users.
     * 
     * @return enabled user count
     */
    public long countEnabledUsers() {
        return userRepository.countEnabled();
    }
}