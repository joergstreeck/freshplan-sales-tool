package de.freshplan.domain.user.service.query;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.domain.user.service.dto.UserResponse;
import de.freshplan.domain.user.service.exception.UserNotFoundException;
import de.freshplan.domain.user.service.mapper.UserMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Query service for User read operations. Part of CQRS refactoring - handles all read operations
 * for users.
 *
 * <p>This is an EXACT COPY of read methods from UserService to ensure 100% compatibility.
 * IMPORTANT: No @Transactional annotation on class or methods (read-only operations)!
 */
@ApplicationScoped
public class UserQueryService {

  private static final Logger LOG = Logger.getLogger(UserQueryService.class);

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Inject
  public UserQueryService(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  /**
   * Retrieves a user by ID. EXACT COPY from UserService lines 155-169
   *
   * @param id the user ID
   * @return the user response
   * @throws UserNotFoundException if user not found
   */
  public UserResponse getUser(@NotNull UUID id) {
    // Defensive validation - exact copy
    if (id == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    LOG.debugf("Retrieving user with ID: %s", id);

    User user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    return userMapper.toResponse(user);
  }

  /**
   * Retrieves a user by username. EXACT COPY from UserService lines 178-193
   *
   * @param username the username
   * @return the user response
   * @throws UserNotFoundException if user not found
   */
  public UserResponse getUserByUsername(@NotNull String username) {
    // Defensive validation - exact copy
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }

    LOG.debugf("Retrieving user with username: %s", username);

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UserNotFoundException("User not found with username: " + username));

    return userMapper.toResponse(user);
  }

  /**
   * Lists all users with pagination. EXACT COPY from UserService lines 202-209
   *
   * @param pageIndex the page index (0-based)
   * @param pageSize the page size
   * @return list of user responses
   */
  public List<UserResponse> listUsers(int pageIndex, int pageSize) {
    LOG.debugf("Listing users - page: %d, size: %d", pageIndex, pageSize);

    Page page = Page.of(pageIndex, pageSize);
    List<User> users = userRepository.findAll().page(page).list();

    return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Lists enabled users with pagination. EXACT COPY from UserService lines 218-225
   *
   * @param pageIndex the page index (0-based)
   * @param pageSize the page size
   * @return list of enabled user responses
   */
  public List<UserResponse> listEnabledUsers(int pageIndex, int pageSize) {
    LOG.debugf("Listing enabled users - page: %d, size: %d", pageIndex, pageSize);

    Page page = Page.of(pageIndex, pageSize);
    List<User> users = userRepository.findEnabledUsers(page);

    return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Searches for users by search term. EXACT COPY from UserService lines 235-242
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

    return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Retrieves a user by ID. Alias for getUser for backward compatibility. EXACT COPY from
   * UserService lines 275-277
   *
   * @param id the user ID
   * @return the user response
   * @throws UserNotFoundException if user not found
   */
  public UserResponse getUserById(@NotNull UUID id) {
    return getUser(id);
  }

  /**
   * Retrieves all users. EXACT COPY from UserService lines 284-290
   *
   * @return list of all user responses
   */
  public List<UserResponse> getAllUsers() {
    LOG.debug("Retrieving all users");

    List<User> users = userRepository.listAll();

    return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
  }

  /**
   * Finds a user by email. EXACT COPY from UserService lines 298-302
   *
   * @param email the email address
   * @return Optional containing the user response if found
   */
  public Optional<UserResponse> findByEmail(String email) {
    LOG.debugf("Finding user by email: %s", email);

    return userRepository.findByEmail(email).map(userMapper::toResponse);
  }

  /**
   * Counts total users. EXACT COPY from UserService lines 365-367
   *
   * @return total user count
   */
  public long countUsers() {
    return userRepository.count();
  }

  /**
   * Counts enabled users. EXACT COPY from UserService lines 374-376
   *
   * @return enabled user count
   */
  public long countEnabledUsers() {
    return userRepository.countEnabled();
  }
}
