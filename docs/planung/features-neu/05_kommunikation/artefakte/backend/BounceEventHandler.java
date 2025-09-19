package de.freshplan.communication.worker;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/api/comm/bounce/webhook")
public class BounceEventHandler {

  @Inject jakarta.persistence.EntityManager em;

  public static class BouncePayload {
    public String messageId; public String severity; public String smtpCode; public String reason;
  }

  @POST @Transactional
  public Response handle(BouncePayload p) {
    var m = em.createNativeQuery("SELECT id FROM communication_messages WHERE mime_message_id=:mid")
      .setParameter("mid", p.messageId).getResultList();
    if (m.isEmpty()) return Response.ok().build();
    java.util.UUID id = (java.util.UUID) m.get(0);
    em.createNativeQuery("INSERT INTO bounce_events(message_id, severity, smtp_code, reason) VALUES (:mid, :sev, :code, :rea)")
      .setParameter("mid", id).setParameter("sev", p.severity==null? "SOFT": p.severity.toUpperCase())
      .setParameter("code", p.smtpCode).setParameter("rea", p.reason).executeUpdate();
    em.createNativeQuery("UPDATE communication_messages SET status='BOUNCED' WHERE id=:id").setParameter("id", id).executeUpdate();
    // TODO: publish domain event 'email.bounced' into event_outbox if present
    return Response.accepted().build();
  }
}
