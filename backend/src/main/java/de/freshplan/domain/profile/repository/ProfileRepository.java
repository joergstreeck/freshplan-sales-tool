package de.freshplan.domain.profile.repository;

import de.freshplan.domain.profile.entity.Profile;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Profile entity operations. Provides data access layer for customer profiles.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
@ApplicationScoped
public class ProfileRepository implements PanacheRepositoryBase<Profile, UUID> {

  /**
   * Find a profile by customer ID.
   *
   * @param customerId the customer ID to search for
   * @return Optional containing the profile if found
   */
  public Optional<Profile> findByCustomerId(String customerId) {
    return find("customerId", customerId).firstResultOptional();
  }

  /**
   * Check if a profile exists for a given customer ID.
   *
   * @param customerId the customer ID to check
   * @return true if profile exists, false otherwise
   */
  public boolean existsByCustomerId(String customerId) {
    return count("customerId", customerId) > 0;
  }
}
