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
import java.sql.Connection;
import java.sql.Statement;
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
      // Generate idempotency key for deduplication
      String idempotencyKey =
          String.format("%d:%s:%s:%d", lead.id, oldStatus, newStatus, System.currentTimeMillis());

      LeadStatusChangeEvent event =
          new LeadStatusChangeEvent(
              lead.id,
              lead.companyName,
              oldStatus,
              newStatus,
              changedBy,
              lead.territory != null ? lead.territory.id : null,
              lead.ownerUserId,
              idempotencyKey);

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
   * Sends NOTIFY command to PostgreSQL.
   *
   * @param channel Channel name
   * @param payload Payload to send
   */
  private void notifyChannel(String channel, String payload) {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {

      // Escape payload for SQL
      String escapedPayload = payload.replace("'", "''");
      String sql = String.format("NOTIFY %s, '%s'", channel, escapedPayload);

      stmt.execute(sql);

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
      this.timestamp = System.currentTimeMillis();
    }
  }
}
