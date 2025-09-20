package de.freshplan.admin.ops;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.Map;
import java.util.UUID;

import de.freshplan.security.ScopeContext;

@ApplicationScoped
public class AdminOperationsService {

  @Inject EntityManager em;
  @Inject ScopeContext scope;

  public Map<String,Object> getSmtp(){
    var row = (Object[]) em.createNativeQuery("SELECT host,port,username,tls_mode,truststore_ref FROM admin_smtp_config WHERE tenant_id=:t ORDER BY updated_at DESC LIMIT 1")
      .setParameter("t", scope.getTenantId()).getResultStream().findFirst().orElse(null);
    if (row == null) return java.util.Map.of();
    return java.util.Map.of("host", row[0], "port", row[1], "username", row[2], "tlsMode", row[3], "truststoreRef", row[4]);
    }

  public void updateSmtp(String host, int port, String username, String passwordRef, String tlsMode, String truststoreRef){
    em.createNativeQuery("INSERT INTO admin_smtp_config(tenant_id,host,port,username,password_ref,tls_mode,truststore_ref,updated_by) " +
      "VALUES (:t,:h,:p,:u,:pr,:m,:ts,:by)")
      .setParameter("t", scope.getTenantId()).setParameter("h", host).setParameter("p", port)
      .setParameter("u", username).setParameter("pr", passwordRef).setParameter("m", tlsMode).setParameter("ts", truststoreRef)
      .setParameter("by", scope.getUserId()).executeUpdate();
  }

  public Map<String,Object> getOutbox(){
    var row = (Object[]) em.createNativeQuery("SELECT paused, rate_per_min FROM admin_outbox_limits WHERE tenant_id=:t")
      .setParameter("t", scope.getTenantId()).getSingleResult();
    var backlog = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM event_outbox WHERE tenant_id=:t").setParameter("t", scope.getTenantId()).getSingleResult()).intValue();
    return java.util.Map.of("paused", row[0], "ratePerMin", row[1], "backlog", backlog);
  }

  public void pauseOutbox(){ setPaused(true); }
  public void resumeOutbox(){ setPaused(false); }
  private void setPaused(boolean p){
    em.createNativeQuery("INSERT INTO admin_outbox_limits(tenant_id, rate_per_min, paused, updated_by) " +
      "VALUES (:t, 0, :p, :u) ON CONFLICT (tenant_id) DO UPDATE SET paused=:p, updated_at=now(), updated_by=:u")
      .setParameter("t", scope.getTenantId()).setParameter("p", p).setParameter("u", scope.getUserId()).executeUpdate();
  }

  public void setRate(int rate){
    em.createNativeQuery("INSERT INTO admin_outbox_limits(tenant_id, rate_per_min, paused, updated_by) " +
      "VALUES (:t, :r, false, :u) ON CONFLICT (tenant_id) DO UPDATE SET rate_per_min=:r, updated_at=now(), updated_by=:u")
      .setParameter("t", scope.getTenantId()).setParameter("r", rate).setParameter("u", scope.getUserId()).executeUpdate();
  }

  public void queueDsar(String type, java.util.UUID subjectId, String justification){
    em.createNativeQuery("INSERT INTO admin_dsar_request(tenant_id, subject_id, req_type, status, requested_by, justification) " +
      "VALUES (:t,:s,:ty,'QUEUED',:u,:j)")
      .setParameter("t", scope.getTenantId()).setParameter("s", subjectId).setParameter("ty", type)
      .setParameter("u", scope.getUserId()).setParameter("j", justification).executeUpdate();
  }
}
