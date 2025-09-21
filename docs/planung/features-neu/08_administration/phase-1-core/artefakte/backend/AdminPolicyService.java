package de.freshplan.admin.policy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.ForbiddenException;
import java.time.OffsetDateTime;
import java.util.UUID;

import de.freshplan.security.ScopeContext;

@ApplicationScoped
public class AdminPolicyService {

  @Inject EntityManager em;
  @Inject ScopeContext scope;

  public void requestChange(String key, String jsonValue, String riskTier, String justification, boolean emergency){
    if (scope.getUserId() == null) throw new ForbiddenException("No user");
    var delayKey = "TIER1".equals(riskTier) ? "admin.approval.tier1.delay" : ("TIER2".equals(riskTier) ? "admin.approval.tier2.delay" : null);
    OffsetDateTime until = null;
    if (delayKey != null && !em.createNativeQuery("SELECT 1").getResultList().isEmpty()) {
      // fallback: 30/10 minutes as default if settings not resolved here
      until = OffsetDateTime.now().plusMinutes("TIER1".equals(riskTier)?30:10);
    }
    em.createNativeQuery("INSERT INTO admin_approval_request(tenant_id, org_id, risk_tier, action, resource_type, resource_id, requested_by, justification, emergency, status, time_delay_until, details) " +
      "VALUES (:t,:o, CAST(:tier AS risk_tier_enum), 'policy.change', 'settings_registry', :key, :rb, :just, :em, :st, :until, CAST(:val AS jsonb))")
      .setParameter("t", scope.getTenantId())
      .setParameter("o", scope.getOrgId())
      .setParameter("tier", riskTier)
      .setParameter("key", key)
      .setParameter("rb", scope.getUserId())
      .setParameter("just", justification)
      .setParameter("em", emergency)
      .setParameter("st", delayKey==null? "APPROVED" : (emergency? "OVERRIDDEN" : "PENDING"))
      .setParameter("until", until)
      .setParameter("val", jsonValue==null?"null":jsonValue)
      .executeUpdate();
  }

  public void approve(UUID id){
    em.createNativeQuery("UPDATE admin_approval_request SET status='APPROVED', approved_by=:u, approved_at=now() WHERE id=:id")
      .setParameter("u", scope.getUserId()).setParameter("id", id).executeUpdate();
    // An outbox/worker would execute the change; omitted for brevity
  }

  public void reject(UUID id){
    em.createNativeQuery("UPDATE admin_approval_request SET status='REJECTED', approved_by=:u, approved_at=now() WHERE id=:id")
      .setParameter("u", scope.getUserId()).setParameter("id", id).executeUpdate();
  }
}
