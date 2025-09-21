package de.freshplan.communication.repo;

import de.freshplan.communication.domain.*;
import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class CommunicationRepository {
  @Inject EntityManager em;
  @Inject ScopeContext scope;

  public static class SlaDue {
    public UUID id; public UUID customerId; public String territory;
    SlaDue(UUID id, UUID customerId, String territory) { this.id=id; this.customerId=customerId; this.territory=territory; }
  }

  public Map<String,Object> pageThreads(String q, String customerId, String channel, Boolean unread, String from, String to, int limit, String cursor) {
    StringBuilder sql = new StringBuilder("SELECT id, customer_id, subject, territory, channel, last_message_at, unread_count, version FROM communication_threads WHERE 1=1");
    Map<String,Object> p = new HashMap<>();
    if (!scope.getTerritories().isEmpty()) { sql.append(" AND territory = ANY(:scoped)"); p.put("scoped", scope.getTerritories().toArray(new String[0])); }
    if (q != null && !q.isBlank()) { sql.append(" AND lower(subject) LIKE :q"); p.put("q", "%"+q.toLowerCase()+"%"); }
    if (customerId != null && !customerId.isBlank()) { sql.append(" AND customer_id = :cid"); p.put("cid", java.util.UUID.fromString(customerId)); }
    if (channel != null && !channel.isBlank()) { sql.append(" AND channel = :channel"); p.put("channel", channel); }
    if (unread != null) { sql.append(unread? " AND unread_count > 0" : ""); }
    if (from != null) { sql.append(" AND last_message_at >= :from"); p.put("from", OffsetDateTime.parse(from)); }
    if (to != null) { sql.append(" AND last_message_at <= :to"); p.put("to", OffsetDateTime.parse(to)); }
    if (cursor != null && !cursor.isBlank()) { sql.append(" AND id > :cursor"); p.put("cursor", java.util.UUID.fromString(cursor)); }
    sql.append(" ORDER BY id ASC LIMIT :limit");
    p.put("limit", Math.max(1, Math.min(200, limit)));
    Query qx = em.createNativeQuery(sql.toString());
    p.forEach(qx::setParameter);
    @SuppressWarnings("unchecked") var rows = qx.getResultList();
    List<Map<String,Object>> items = new ArrayList<>();
    String next = null;
    for (Object r : rows) {
      Object[] a = (Object[]) r;
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("id", a[0]); m.put("customerId", a[1]); m.put("subject", a[2]); m.put("territory", a[3]);
      m.put("channel", a[4]); m.put("lastMessageAt", a[5]); m.put("unreadCount", a[6]);
      var version = ((Number)a[7]).intValue(); m.put("etag", ""v"+version+""");
      items.add(m); next = a[0].toString();
    }
    return Map.of("items", items, "nextCursor", rows.size()==(int)p.get("limit")? next : null);
  }

  @Transactional
  public UUID createThread(UUID customerId, String territory, String subject, String channel, List<String> participants) {
    var t = new de.freshplan.communication.domain.Thread();
    t.customerId = customerId; t.territory = territory; t.subject = subject; t.channel = channel;
    if (participants != null && !participants.isEmpty()) {
      var ps = new ParticipantSet(); ps.fromEmail = null;
      ps.toEmails = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(participants).toString();
      em.persist(ps); t.participantSet = ps;
    }
    em.persist(t);
    return t.id;
  }

  @Transactional
  public UUID startConversation(UUID customerId, String territory, String subject, List<String> to, List<String> cc, String bodyText) {
    UUID threadId = createThread(customerId, territory, subject, "EMAIL", to);
    UUID msgId = java.util.UUID.randomUUID();
    em.createNativeQuery("INSERT INTO communication_messages(id, thread_id, territory, direction, status, subject, body_text, recipients) VALUES (:id,:tid,:terr,'OUTBOUND','QUEUED',:subj,:body,:rcpt::jsonb)")
      .setParameter("id", msgId).setParameter("tid", threadId).setParameter("terr", territory)
      .setParameter("subj", subject).setParameter("body", bodyText)
      .setParameter("rcpt", new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(to).toString())
      .executeUpdate();
    enqueueOutbox(msgId, to);
    return threadId;
  }

  @Transactional
  public void enqueueOutbox(UUID messageId, List<String> recipients) {
    String bucket = recipients!=null && !recipients.isEmpty()? recipients.get(0).substring(recipients.get(0).indexOf("@")+1).toLowerCase() : "unknown";
    em.createNativeQuery("INSERT INTO outbox_emails(message_id, rate_bucket, status, next_attempt_at) VALUES (:mid,:bucket,'PENDING', now())")
      .setParameter("mid", messageId).setParameter("bucket", bucket).executeUpdate();
  }

  @Transactional
  public void replyToThread(UUID threadId, String ifMatch, String territory, String bodyText, List<String> cc) {
    var current = (Number) em.createNativeQuery("SELECT version FROM communication_threads WHERE id=:id AND territory=:t")
      .setParameter("id", threadId).setParameter("t", territory).getSingleResult();
    String etag = ""v"+current.intValue()+""";
    if (!etag.equals(ifMatch)) throw new jakarta.ws.rs.WebApplicationException("ETag mismatch", 412);

    UUID msgId = java.util.UUID.randomUUID();
    em.createNativeQuery("INSERT INTO communication_messages(id, thread_id, territory, direction, status, body_text) VALUES (:id,:tid,:terr,'OUTBOUND','QUEUED',:body)")
      .setParameter("id", msgId).setParameter("tid", threadId).setParameter("terr", territory).setParameter("body", bodyText).executeUpdate();
    enqueueOutbox(msgId, java.util.List.of());
    em.createNativeQuery("UPDATE communication_threads SET last_message_at=now(), version=version+1 WHERE id=:id")
      .setParameter("id", threadId).executeUpdate();
  }

  @Transactional
  public void logCall(UUID customerId, String territory, OffsetDateTime at, String summary, String participantsJson) {
    em.createNativeQuery("INSERT INTO comm_activity(id, customer_id, territory, kind, occurred_at, summary, participants) VALUES (gen_random_uuid(), :cid, :terr, 'CALL', :at, :sum, :p::jsonb)")
      .setParameter("cid", customerId).setParameter("terr", territory).setParameter("at", at).setParameter("sum", summary).setParameter("p", participantsJson).executeUpdate();
  }
  @Transactional
  public void logMeeting(UUID customerId, String territory, OffsetDateTime at, String summary, String attendeesJson) {
    em.createNativeQuery("INSERT INTO comm_activity(id, customer_id, territory, kind, occurred_at, summary, participants) VALUES (gen_random_uuid(), :cid, :terr, 'MEETING', :at, :sum, :p::jsonb)")
      .setParameter("cid", customerId).setParameter("terr", territory).setParameter("at", at).setParameter("sum", summary).setParameter("p", attendeesJson).executeUpdate();
  }

  @Transactional
  public void enqueueSlaTask(String customerId, String territory, String rule, java.time.OffsetDateTime due) {
    em.createNativeQuery("INSERT INTO sla_task(id, customer_id, territory, rule_key, due_at) VALUES (gen_random_uuid(), :cid, :terr, :rule, :due)")
      .setParameter("cid", java.util.UUID.fromString(customerId)).setParameter("terr", territory).setParameter("rule", rule).setParameter("due", due).executeUpdate();
  }

  public static class SlaRow { public UUID id; public UUID customerId; public String territory; }
  public java.util.List<SlaRow> findDueSlaTasks(int limit) {
    var q = em.createNativeQuery("SELECT id, customer_id, territory FROM sla_task WHERE status='PENDING' AND due_at<=now() ORDER BY due_at ASC LIMIT :limit").setParameter("limit", limit);
    java.util.List<Object[]> rows = q.getResultList();
    java.util.List<SlaRow> out = new java.util.ArrayList<>();
    for (Object[] r : rows) { var s = new SlaRow(); s.id=(java.util.UUID) r[0]; s.customerId=(java.util.UUID) r[1]; s.territory=(String) r[2]; out.add(s); }
    return out;
  }
  @Transactional public void createFollowUpActivity(java.util.UUID customerId, String territory, String text, java.time.OffsetDateTime at) {
    em.createNativeQuery("INSERT INTO comm_activity(id, customer_id, territory, kind, occurred_at, summary) VALUES (gen_random_uuid(), :cid,:terr,'FOLLOW_UP',:at,:txt)")
      .setParameter("cid", customerId).setParameter("terr", territory).setParameter("at", at).setParameter("txt", text).executeUpdate();
  }
  @Transactional public void markSlaDone(java.util.UUID taskId) {
    em.createNativeQuery("UPDATE sla_task SET status='DONE', updated_at=now() WHERE id=:id").setParameter("id", taskId).executeUpdate();
  }
}
