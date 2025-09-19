package de.freshplan.communication.worker;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EmailOutboxProcessor {

  @Inject EntityManager em;

  private static final int BATCH = Integer.parseInt(System.getProperty("outbox.batch.size","50"));
  private static final int MAX_RETRY = Integer.parseInt(System.getProperty("outbox.max.retry","8"));

  @Scheduled(every="30s", delayed="10s")
  @Transactional
  void process() {
    // Lock pending rows fairly by time, skip locked for parallel pods
    var rows = em.createNativeQuery(
      "SELECT id, message_id, rate_bucket, retry_count FROM outbox_emails " +
      "WHERE status IN ('PENDING','FAILED') AND next_attempt_at<=now() " +
      "ORDER BY next_attempt_at ASC LIMIT :lim FOR UPDATE SKIP LOCKED")
      .setParameter("lim", BATCH).getResultList();

    for (Object r : rows) {
      Object[] a = (Object[]) r;
      var outboxId = (java.util.UUID) a[0];
      var msgId = (java.util.UUID) a[1];
      var bucket = (String) a[2];
      var retry = ((Number) a[3]).intValue();

      try {
        sendEmail(msgId);
        em.createNativeQuery("UPDATE outbox_emails SET status='SENT', updated_at=now() WHERE id=:id").setParameter("id", outboxId).executeUpdate();
        em.createNativeQuery("UPDATE communication_messages SET status='SENT', updated_at=now() WHERE id=:id").setParameter("id", msgId).executeUpdate();
      } catch (Exception ex) {
        retry++;
        String next = retryBackoff(retry);
        em.createNativeQuery("UPDATE outbox_emails SET status='FAILED', retry_count=:r, next_attempt_at=now()+("+next+")::interval, last_error=:e, updated_at=now() WHERE id=:id")
          .setParameter("r", retry).setParameter("e", ex.getMessage()).setParameter("id", outboxId).executeUpdate();
        if (retry >= MAX_RETRY) {
          // give up – mark message bounced soft
          em.createNativeQuery("UPDATE communication_messages SET status='BOUNCED', updated_at=now() WHERE id=:id").setParameter("id", msgId).executeUpdate();
        }
      }
    }
  }

  private void sendEmail(UUID messageId) throws Exception {
    // Integrate SMTP gateway here (DKIM/DMARC handled by infra). For now, simulate success.
  }

  private String retryBackoff(int retry) {
    // exponential backoff in seconds (10, 30, 60, 120, ...)
    int secs = (int) Math.min(600, (Math.pow(2, Math.min(6, retry)) * 10));
    return secs+" seconds";
  }
}
