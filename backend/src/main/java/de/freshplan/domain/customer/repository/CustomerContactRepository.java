package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.CustomerContact;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * Repository für CustomerContact Entity.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CustomerContactRepository implements PanacheRepositoryBase<CustomerContact, UUID> {

  /** Findet alle Kontakte eines Kunden. */
  public List<CustomerContact> findByCustomerId(UUID customerId) {
    return find("customerId", customerId).list();
  }

  /** Findet den Primary Contact eines Kunden. */
  public CustomerContact findPrimaryContact(UUID customerId) {
    return find("customerId = ?1 AND isPrimary = true", customerId).firstResult();
  }

  /** Findet Kontakte nach E-Mail. */
  public CustomerContact findByEmail(String email) {
    return find("email", email).firstResult();
  }

  /** Prüft ob E-Mail bereits existiert. */
  public boolean existsByEmail(String email) {
    return count("email", email) > 0;
  }
}
