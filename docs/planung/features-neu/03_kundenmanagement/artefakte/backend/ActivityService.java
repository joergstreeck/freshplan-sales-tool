/** ActivityService – B2B-Convenience-Food Activities (ROI, Tests, Training)
 * Foundation: /docs/planung/grundlagen/API_STANDARDS.md, SECURITY_ABAC.md
 */
package de.freshplan.customer;

import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public class ActivityService {

  @Inject EntityManager em;
  @Inject ScopeContext scope;

  @Transactional
  public void create(Map<String,Object> activity) {
    // Security: Territory-Validation ohne Fallback
    if (scope.getTerritories().isEmpty()) {
      throw new jakarta.ws.rs.ForbiddenException("No authorized territories for user");
    }

    // Input-Validation
    String customerId = (String) activity.get("customerId");
    if (customerId == null || customerId.isBlank()) {
      throw new jakarta.ws.rs.BadRequestException("Customer ID required");
    }
    if (activity.get("kind") == null) {
      throw new jakarta.ws.rs.BadRequestException("Activity kind required");
    }
    if (activity.get("occurredAt") == null) {
      throw new jakarta.ws.rs.BadRequestException("Occurred date required");
    }

    java.util.UUID customerUuid;
    try {
      customerUuid = java.util.UUID.fromString(customerId);
    } catch (IllegalArgumentException e) {
      throw new jakarta.ws.rs.BadRequestException("Invalid customer ID format");
    }

    // Validate activity kind
    String kind = (String) activity.get("kind");
    if (!java.util.Arrays.asList("PRODUCTTEST_FEEDBACK", "ROI_CONSULTATION", "MENU_INTEGRATION",
                                  "DECISION_ALIGNMENT", "TRAINING", "QUALITY_CHECK").contains(kind)) {
      throw new jakarta.ws.rs.BadRequestException("Invalid activity kind: " + kind);
    }

    em.createNativeQuery("INSERT INTO activities(id, customer_id, territory, kind, occurred_at, payload) VALUES (:id,:cid,:territory,:kind,:at,:payload::jsonb)")
      .setParameter("id", java.util.UUID.randomUUID())
      .setParameter("cid", customerUuid)
      .setParameter("territory", scope.getTerritories().get(0))
      .setParameter("kind", kind)
      .setParameter("at", activity.get("occurredAt"))
      .setParameter("payload", activity.getOrDefault("payload","{}"))
      .executeUpdate();
  }
}
