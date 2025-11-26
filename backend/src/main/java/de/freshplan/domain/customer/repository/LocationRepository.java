package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.CustomerLocation;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Repository for CustomerLocation entity. Sprint 2.1.7.7: Multi-Location Management.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class LocationRepository implements PanacheRepositoryBase<CustomerLocation, UUID> {

  /**
   * Find all active locations for a customer.
   *
   * @param customerId the customer ID
   * @return list of active locations sorted by type (headquarters first) and name
   */
  public List<CustomerLocation> findByCustomerId(UUID customerId) {
    return find(
            "customer.id = ?1 and isDeleted = false order by isHeadquarters desc, locationName",
            customerId)
        .list();
  }

  /**
   * Find headquarters location for a customer.
   *
   * @param customerId the customer ID
   * @return headquarters location or null if not found
   */
  public CustomerLocation findHeadquarters(UUID customerId) {
    return find("customer.id = ?1 and isHeadquarters = true and isDeleted = false", customerId)
        .firstResult();
  }

  /**
   * Count active locations for a customer.
   *
   * @param customerId the customer ID
   * @return number of active locations
   */
  public long countByCustomerId(UUID customerId) {
    return count("customer.id = ?1 and isDeleted = false", customerId);
  }

  /**
   * Find locations by IDs.
   *
   * @param locationIds list of location IDs
   * @return list of locations
   */
  public List<CustomerLocation> findByIds(List<UUID> locationIds) {
    if (locationIds == null || locationIds.isEmpty()) {
      return List.of();
    }
    return find("id in ?1 and isDeleted = false", locationIds).list();
  }
}
