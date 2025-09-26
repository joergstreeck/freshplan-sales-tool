package de.freshplan.modules.leads.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.sql.DataSource;

/**
 * Publisher für Lead-Events über PostgreSQL LISTEN/NOTIFY.
 *
 * <p>Implementiert CQRS Light Pattern mit PostgreSQL LISTEN/NOTIFY für Lead-Status-Changes und
 * Cross-Module-Events.
 *
 * <p>Part of FP-236 Security Integration
 */
@ApplicationScoped
public class LeadEventPublisher {

  private static final String LEAD_STATUS_CHANNEL = "lead_status_changes";
  private static final String CROSS_MODULE_CHANNEL = "cross_module_events";

  @Inject DataSource dataSource;

  @Inject ObjectMapper objectMapper;

  @Inject EntityManager entityManager;

  @Inject TransactionSynchronizationRegistry txRegistry;

  /**
   * Publishes a lead status change event via PostgreSQL NOTIFY.
   *
   * @param lead The lead whose status changed
   * @param oldStatus The previous status
   * @param newStatus The new status
   * @param changedBy User who made the change
   */
  @Transactional
  public void publishStatusChange(
      Lead lead, LeadStatus oldStatus, LeadStatus newStatus, String changedBy) {
    try {
      // Use stable timestamp for idempotency
      LocalDateTime changedAt =
          lead.updatedAt != null ? lead.updatedAt : LocalDateTime.now(ZoneOffset.UTC);

      // Generate deterministic idempotency key for reliable deduplication
      String keySource =
          lead.id
              + "|"
              + oldStatus
              + ">"
              + newStatus
              + "|"
              + changedAt.toInstant(ZoneOffset.UTC).toEpochMilli();
      String idempotencyKey =
          UUID.nameUUIDFromBytes(keySource.getBytes(StandardCharsets.UTF_8)).toString();

      // LeadStatusChangeEvent mit UUID statt Long
      UUID leadUuid =
          lead.id != null
              ? UUID.nameUUIDFromBytes(lead.id.toString().getBytes())
              : UUID.randomUUID();

      LeadStatusChangeEvent event =
          new LeadStatusChangeEvent(
              leadUuid,
              lead.companyName,
              oldStatus,
              newStatus,
              changedBy,
              changedAt,
              "Status change: " + oldStatus + " -> " + newStatus);

      String payload = objectMapper.writeValueAsString(event);

      // Register for AFTER_COMMIT to ensure event is only sent if transaction succeeds
      if (txRegistry != null && txRegistry.getTransactionKey() != null) {
        txRegistry.registerInterposedSynchronization(
            new jakarta.transaction.Synchronization() {
              @Override
              public void beforeCompletion() {}

              @Override
              public void afterCompletion(int status) {
                if (status == jakarta.transaction.Status.STATUS_COMMITTED) {
                  // Only publish if transaction committed successfully
                  notifyChannel(LEAD_STATUS_CHANNEL, payload);
                  publishCrossModuleEvent("LEAD_STATUS_CHANGED", payload);
                }
              }
            });
      } else {
        // Fallback for non-transactional context (e.g., tests)
        notifyChannel(LEAD_STATUS_CHANNEL, payload);
        publishCrossModuleEvent("LEAD_STATUS_CHANGED", payload);
      }

      Log.debugf(
          "Scheduled lead status change event: %s -> %s for lead ID %s (after commit)",
          oldStatus, newStatus, lead.id);

    } catch (JsonProcessingException e) {
      Log.errorf(e, "Failed to serialize lead status change event for lead %s", lead.id);
    }
  }

  /**
   * Publishes a cross-module event for activity timeline integration.
   *
   * @param eventType Type of the event
   * @param payload JSON payload
   */
  public void publishCrossModuleEvent(String eventType, String payload) {
    try {
      // Wrap in cross-module envelope
      String crossModulePayload =
          String.format(
              "{\"eventType\":\"%s\",\"module\":\"leads\",\"payload\":%s}", eventType, payload);

      notifyChannel(CROSS_MODULE_CHANNEL, crossModulePayload);

      Log.debugf("Published cross-module event: %s", eventType);
    } catch (Exception e) {
      Log.errorf(e, "Failed to publish cross-module event: %s", eventType);
    }
  }

  /**
   * Sends NOTIFY command to PostgreSQL using pg_notify function. This is safer than building SQL
   * strings manually.
   *
   * @param channel Channel name
   * @param payload Payload to send
   */
  private void notifyChannel(String channel, String payload) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT pg_notify(?, ?)")) {

      ps.setString(1, channel);
      ps.setString(2, payload);
      ps.execute();

    } catch (Exception e) {
      Log.errorf(e, "CRITICAL: Failed to send NOTIFY on channel %s - requires retry", channel);
      throw new RuntimeException("Failed to publish event - manual retry required", e);
    }
  }

  /**
   * Publishes lead creation event.
   *
   * @param lead The newly created lead
   * @param createdBy User who created the lead
   */
  @Transactional
  public void publishLeadCreated(Lead lead, String createdBy) {
    try {
      String payload = objectMapper.writeValueAsString(new LeadCreatedEvent(lead, createdBy));

      notifyChannel("lead_created", payload);
      publishCrossModuleEvent("LEAD_CREATED", payload);

      // Mask PII - only log lead ID, not company name
      Log.debugf("Published lead created event for lead ID %s", lead.id);
    } catch (JsonProcessingException e) {
      Log.errorf(e, "Failed to publish lead created event for %s", lead.id);
    }
  }

  /** Simple event class for lead creation. */
  public static class LeadCreatedEvent {
    public final String leadId;
    public final String companyName;
    public final String territory;
    public final String ownerUserId;
    public final String createdBy;
    public final long timestamp;

    public LeadCreatedEvent(Lead lead, String createdBy) {
      this.leadId = lead.id.toString();
      this.companyName = lead.companyName;
      this.territory = lead.territory != null ? lead.territory.id : null;
      this.ownerUserId = lead.ownerUserId;
      this.createdBy = createdBy;
      // Use lead's createdAt timestamp for consistency
      this.timestamp =
          lead.createdAt != null
              ? lead.createdAt.toInstant(ZoneOffset.UTC).toEpochMilli()
              : System.currentTimeMillis();
    }
  }
}
