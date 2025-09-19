/** SampleManagementService – Cook&Fresh® Sample-Box Tracking
 * Foundation References:
 * - API: /docs/planung/grundlagen/API_STANDARDS.md
 * - SQL: /docs/planung/grundlagen/SQL_STANDARDS.md (Indexes, RLS)
 * - Security: /docs/planung/grundlagen/SECURITY_ABAC.md
 */
package de.freshplan.customer;

import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public class SampleManagementService {

  @Inject EntityManager em;
  @Inject ScopeContext scope;

  public Map<String,Object> listByCustomer(String customerId) {
    // Security: Territory-Validation ohne Fallback
    if (scope.getTerritories().isEmpty()) {
      throw new jakarta.ws.rs.ForbiddenException("No authorized territories for user");
    }

    // Input-Validation
    if (customerId == null || customerId.isBlank()) {
      throw new jakarta.ws.rs.BadRequestException("Customer ID required");
    }

    java.util.UUID customerUuid;
    try {
      customerUuid = java.util.UUID.fromString(customerId);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException("Invalid customer ID format");
    }

    var q = em.createNativeQuery("SELECT id, status, delivery_date, updated_at FROM sample_request WHERE customer_id=:cid AND territory = ANY(:scoped) ORDER BY updated_at DESC")
              .setParameter("cid", customerUuid)
              .setParameter("scoped", scope.getTerritories().toArray(new String[0]));
    @SuppressWarnings("unchecked")
    var rows = q.getResultList();
    var items = new java.util.ArrayList<java.util.Map<String,Object>>();
    for (Object r : rows) {
      Object[] a = (Object[]) r;
      items.add(java.util.Map.of("id", a[0], "status", a[1].toString(), "deliveryDate", a[2], "updatedAt", a[3]));
    }
    return java.util.Map.of("items", items);
  }

  @Transactional
  public void requestSample(String customerId, Map<String,Object> body) {
    // Security: Territory-Validation ohne Fallback
    if (scope.getTerritories().isEmpty()) {
      throw new jakarta.ws.rs.ForbiddenException("No authorized territories for user");
    }

    // Input-Validation
    if (customerId == null || customerId.isBlank()) {
      throw new jakarta.ws.rs.BadRequestException("Customer ID required");
    }
    if (body.get("deliveryDate") == null) {
      throw new jakarta.ws.rs.BadRequestException("Delivery date required");
    }

    java.util.UUID customerUuid;
    try {
      customerUuid = java.util.UUID.fromString(customerId);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException("Invalid customer ID format");
    }

    em.createNativeQuery("INSERT INTO sample_request(id, customer_id, territory, status, delivery_date, notes) VALUES (:id,:cid,:territory,'REQUESTED',:date,:notes)")
      .setParameter("id", java.util.UUID.randomUUID())
      .setParameter("cid", customerUuid)
      .setParameter("territory", scope.getTerritories().get(0))
      .setParameter("date", body.get("deliveryDate"))
      .setParameter("notes", body.getOrDefault("notes", ""))
      .executeUpdate();
  }
}
