package de.freshplan.domain.user.repository;

import de.freshplan.domain.user.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 *
 * <p>This repository provides data access operations for User entities following the repository
 * pattern. It extends PanacheRepositoryBase to leverage Quarkus Panache's active record pattern
 * benefits while maintaining clean architecture separation.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@Transactional
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

  /**
   * Finds a user by their username.
   *
   * @param username the username to search for
   * @return Optional containing the user if found, empty otherwise
   */
  public Optional<User> findByUsername(String username) {
    if (username == null || username.isBlank()) {
      return Optional.empty();
    }
    return find("username", username).firstResultOptional();
  }

  /**
   * Finds a user by their email address.
   *
   * @param email the email to search for
   * @return Optional containing the user if found, empty otherwise
   */
  public Optional<User> findByEmail(String email) {
    if (email == null || email.isBlank()) {
      return Optional.empty();
    }
    return find("email", email).firstResultOptional();
  }

  /**
   * Finds all enabled users with pagination support.
   *
   * @param page the page information (index and size)
   * @return list of enabled users for the requested page
   */
  public List<User> findEnabledUsers(Page page) {
    return find("enabled = true", Sort.by("lastName", "firstName")).page(page).list();
  }

  /**
   * Counts all enabled users.
   *
   * @return the number of enabled users
   */
  public long countEnabled() {
    return count("enabled = true");
  }

  /**
   * Checks if a username already exists in the database.
   *
   * @param username the username to check
   * @return true if username exists, false otherwise
   */
  public boolean existsByUsername(String username) {
    if (username == null || username.isBlank()) {
      return false;
    }
    return count("username = ?1", username) > 0;
  }

  /**
   * Checks if a username already exists, excluding a specific user. Useful for update operations.
   *
   * @param username the username to check
   * @param excludeUserId the user ID to exclude from the check
   * @return true if username exists for another user, false otherwise
   */
  public boolean existsByUsernameExcluding(String username, UUID excludeUserId) {
    if (username == null || username.isBlank()) {
      return false;
    }
    return count("username = ?1 AND id != ?2", username, excludeUserId) > 0;
  }

  /**
   * Checks if an email already exists in the database.
   *
   * @param email the email to check
   * @return true if email exists, false otherwise
   */
  public boolean existsByEmail(String email) {
    if (email == null || email.isBlank()) {
      return false;
    }
    return count("email = ?1", email) > 0;
  }

  /**
   * Checks if an email already exists, excluding a specific user. Useful for update operations.
   *
   * @param email the email to check
   * @param excludeUserId the user ID to exclude from the check
   * @return true if email exists for another user, false otherwise
   */
  public boolean existsByEmailExcluding(String email, UUID excludeUserId) {
    if (email == null || email.isBlank()) {
      return false;
    }
    return count("email = ?1 AND id != ?2", email, excludeUserId) > 0;
  }

  /**
   * Checks if a username already exists, excluding a specific user. Alias for
   * existsByUsernameExcluding for backward compatibility.
   *
   * @param username the username to check
   * @param excludeUserId the user ID to exclude from the check
   * @return true if username exists for another user, false otherwise
   */
  public boolean existsByUsernameAndIdNot(String username, UUID excludeUserId) {
    return existsByUsernameExcluding(username, excludeUserId);
  }

  /**
   * Checks if an email already exists, excluding a specific user. Alias for existsByEmailExcluding
   * for backward compatibility.
   *
   * @param email the email to check
   * @param excludeUserId the user ID to exclude from the check
   * @return true if email exists for another user, false otherwise
   */
  public boolean existsByEmailAndIdNot(String email, UUID excludeUserId) {
    return existsByEmailExcluding(email, excludeUserId);
  }

  /**
   * Finds all active (enabled) users.
   *
   * @return list of all enabled users
   */
  public List<User> findAllActive() {
    return find("enabled = true", Sort.by("lastName", "firstName")).list();
  }

  /**
   * Searches for users by a search term. Searches in username, first name, last name, and email.
   *
   * @param searchTerm the term to search for
   * @param page the page information
   * @return list of users matching the search term
   */
  public List<User> search(String searchTerm, Page page) {
    if (searchTerm == null || searchTerm.isBlank()) {
      return findAll(Sort.by("lastName", "firstName")).page(page).list();
    }

    String pattern = "%" + searchTerm.toLowerCase() + "%";
    return find(
            "LOWER(username) LIKE ?1 OR LOWER(firstName) LIKE ?1 OR "
                + "LOWER(lastName) LIKE ?1 OR LOWER(email) LIKE ?1",
            pattern)
        .page(page)
        .list();
  }
}
