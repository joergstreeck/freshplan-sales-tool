package de.freshplan.domain.user.service.command;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.CreateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRequest;
import de.freshplan.domain.user.service.dto.UpdateUserRolesRequest;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.exception.DuplicateEmailException;
import de.freshplan.domain.user.service.exception.DuplicateUsernameException;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import de.freshplan.domain.user.service.mapper.UserMapper;
import de.freshplan.domain.user.service.validation.RoleValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.jboss.logging.Logger;

/**
 * Command service for User write operations. Part of CQRS refactoring - handles all write
 * operations for users.
 *
 * <p>This is an EXACT COPY of write methods from UserService to ensure 100% compatibility. All
 * business logic, validation, and even potential bugs are preserved.
 */
@ApplicationScoped
public class UserCommandService {

  private static final Logger LOG = Logger.getLogger(UserCommandService.class);

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Inject
  public UserCommandService(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  /**
   * Creates a new user. EXACT COPY from UserService lines 58-88
   *
   * @param request the user creation request
   * @return the created user response
   * @throws DuplicateUsernameException if username already exists
   * @throws DuplicateEmailException if email already exists
   */
  @Transactional
  public UserResponse createUser(@Valid @NotNull CreateUserRequest request) {
    // Defensive validation - exact copy
    if (request == null) {
      throw new IllegalArgumentException("CreateUserRequest cannot be null");
    }
    if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }
    if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty");
    }

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
   * Updates an existing user. EXACT COPY from UserService lines 99-146
   *
   * @param id the user ID
   * @param request the update request
   * @return the updated user response
   * @throws UserNotFoundException if user not found
   * @throws DuplicateUsernameException if new username already exists
   * @throws DuplicateEmailException if new email already exists
   */
  @Transactional
  public UserResponse updateUser(@NotNull UUID id, @Valid @NotNull UpdateUserRequest request) {
    // Defensive validation - exact copy
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
    if (request == null) {
      throw new IllegalArgumentException("UpdateUserRequest cannot be null");
    }

    LOG.debugf("Updating user with ID: %s", id);

    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    // Check if any data has actually changed - exact copy
    boolean hasChanges =
        !user.getUsername().equals(request.getUsername())
            || !user.getFirstName().equals(request.getFirstName())
            || !user.getLastName().equals(request.getLastName())
            || !user.getEmail().equals(request.getEmail())
            || user.isEnabled() != request.getEnabled();

    if (!hasChanges) {
      LOG.debugf("No changes detected for user ID: %s. Skipping update.", id);
      return userMapper.toResponse(user);
    }

    // Check username uniqueness if changed
    if (!user.getUsername().equals(request.getUsername())
        && userRepository.existsByUsernameExcluding(request.getUsername(), id)) {
      throw new DuplicateUsernameException(request.getUsername());
    }

    // Check email uniqueness if changed
    if (!user.getEmail().equals(request.getEmail())
        && userRepository.existsByEmailExcluding(request.getEmail(), id)) {
      throw new DuplicateEmailException(request.getEmail());
    }

    // Update user
    userMapper.updateEntity(user, request);
    // User is already managed, no need to persist - exact comment preserved

    LOG.infof("User updated successfully with ID: %s", id);
    return userMapper.toResponse(user);
  }

  /**
   * Deletes a user. EXACT COPY from UserService lines 250-266
   *
   * <p>NOTE: This is a HARD DELETE - no soft delete implemented!
   *
   * @param id the user ID
   * @throws UserNotFoundException if user not found
   */
  @Transactional
  public void deleteUser(@NotNull UUID id) {
    // Defensive validation - exact copy
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    LOG.debugf("Deleting user with ID: %s", id);

    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    userRepository.delete(user);

    LOG.infof("User deleted successfully with ID: %s", id);
  }

  /**
   * Enables a user and returns the updated user. EXACT COPY from UserService lines 311-330
   *
   * @param id the user ID
   * @return the updated user response
   * @throws UserNotFoundException if user not found
   */
  @Transactional
  public UserResponse enableUser(@NotNull UUID id) {
    // Defensive validation - exact copy
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    LOG.debugf("Enabling user with ID: %s", id);

    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    user.enable();
    // User is already managed, no need to persist - exact comment preserved
    userRepository.flush();

    LOG.infof("User enabled successfully with ID: %s", id);
    return userMapper.toResponse(user);
  }

  /**
   * Disables a user and returns the updated user. EXACT COPY from UserService lines 339-358
   *
   * @param id the user ID
   * @return the updated user response
   * @throws UserNotFoundException if user not found
   */
  @Transactional
  public UserResponse disableUser(@NotNull UUID id) {
    // Defensive validation - exact copy
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    LOG.debugf("Disabling user with ID: %s", id);

    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    user.disable();
    // User is already managed, no need to persist - exact comment preserved
    userRepository.flush();

    LOG.infof("User disabled successfully with ID: %s", id);
    return userMapper.toResponse(user);
  }

  /**
   * Updates the roles of a user. EXACT COPY from UserService lines 387-414
   *
   * <p>NOTE: Has its own @Transactional annotation in original!
   *
   * @param id the user ID
   * @param request the roles update request
   * @return the updated user response
   * @throws UserNotFoundException if user not found
   * @throws InvalidRoleException if any role is invalid
   */
  @Transactional
  public UserResponse updateUserRoles(
      @NotNull UUID id, @Valid @NotNull UpdateUserRolesRequest request) {
    // Defensive validation - exact copy
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
    if (request == null) {
      throw new IllegalArgumentException("UpdateUserRolesRequest cannot be null");
    }

    LOG.debugf("Updating roles for user with ID: %s", id);

    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    // Normalize and validate all roles in one step - exact copy
    List<String> normalizedRoles = RoleValidator.normalizeAndValidateRoles(request.getRoles());

    // Update user roles
    user.setRoles(normalizedRoles);
    userRepository.flush();

    LOG.infof("User roles updated successfully for ID: %s. New roles: %s", id, normalizedRoles);
    return userMapper.toResponse(user);
  }
}
