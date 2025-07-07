package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.CustomerTimelineEvent;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Repository for CustomerTimelineEvent entities. Provides data access operations for customer
 * timeline events.
 */
@ApplicationScoped
public class CustomerTimelineEventRepository
    implements PanacheRepositoryBase<CustomerTimelineEvent, UUID> {
  // Panache provides all basic CRUD operations
  // Add custom query methods here as needed
}
