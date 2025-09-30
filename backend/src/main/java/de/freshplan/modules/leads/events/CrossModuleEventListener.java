package de.freshplan.modules.leads.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

/**
 * Listener für Cross-Module Events über PostgreSQL LISTEN/NOTIFY.
 *
 * <p>Empfängt Events von anderen Modulen für Activity-Timeline Integration. Implementiert das CQRS
 * Light Pattern mit PostgreSQL.
 *
 * <p>Part of FP-236 Security Integration
 */
@ApplicationScoped
public class CrossModuleEventListener {

  private static final String CROSS_MODULE_CHANNEL = "cross_module_events";
  private static final String LEAD_STATUS_CHANNEL = "lead_status_changes";

  @Inject DataSource dataSource;

  @Inject ObjectMapper objectMapper;

  @Inject ManagedExecutor managedExecutor;

  @ConfigProperty(name = "freshplan.modules.leads.events.enabled", defaultValue = "true")
  boolean eventsEnabled;

  @ConfigProperty(name = "freshplan.modules.cross.events.enabled", defaultValue = "true")
  boolean crossEventsEnabled;

  private volatile boolean listening = false;

  /** Starts listening to PostgreSQL notifications on application startup. */
  void onStart(@Observes StartupEvent ev) {
    // Check multiple config flags for better control
    if (!eventsEnabled || !crossEventsEnabled) {
      Log.info(
          "CrossModuleEventListener disabled by configuration (eventsEnabled="
              + eventsEnabled
              + ", crossEventsEnabled="
              + crossEventsEnabled
              + ")");
      return;
    }

    // Also check for CI/Test environment
    String profile = System.getProperty("quarkus.profile", "");
    if ("ci".equals(profile) || "test".equals(profile)) {
      Log.info("CrossModuleEventListener disabled in " + profile + " profile");
      return;
    }

    startListening();
  }

  /** Stops listening on application shutdown. */
  void onStop(@Observes ShutdownEvent ev) {
    stopListening();
  }

  /** Cleanup on bean destruction. */
  @PreDestroy
  void cleanup() {
    stopListening();
  }

  /** Starts the LISTEN threads for PostgreSQL notifications. */
  private void startListening() {
    listening = true;

    // Listen for cross-module events
    managedExecutor.submit(() -> listenToChannel(CROSS_MODULE_CHANNEL));

    // Listen for lead status changes
    managedExecutor.submit(() -> listenToChannel(LEAD_STATUS_CHANNEL));

    Log.info("Started listening for PostgreSQL notifications");
  }

  /**
   * Listens to a specific PostgreSQL channel.
   *
   * @param channel The channel to listen to
   */
  private void listenToChannel(String channel) {
    while (listening) {
      try (Connection conn = dataSource.getConnection();
          Statement stmt = conn.createStatement()) {

        // Start listening
        stmt.execute("LISTEN " + channel);
        Log.infof("Listening on channel: %s", channel);

        // Poll for notifications
        org.postgresql.PGConnection pgConn = conn.unwrap(org.postgresql.PGConnection.class);

        while (listening) {
          // Get notifications, blocking for up to 10 seconds
          // This is more efficient than polling with SELECT 1 and sleep
          org.postgresql.PGNotification[] notifications = pgConn.getNotifications(10000);

          if (notifications != null) {
            for (org.postgresql.PGNotification notification : notifications) {
              processNotification(notification.getName(), notification.getParameter());
            }
          }
        }

      } catch (Exception e) {
        Log.errorf(e, "Error listening to channel %s, retrying...", channel);
        try {
          Thread.sleep(5000); // Wait before retry
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
  }

  /**
   * Processes received notifications.
   *
   * @param channel The channel the notification came from
   * @param payload The notification payload
   */
  private void processNotification(String channel, String payload) {
    try {
      Log.debugf("Received notification on %s: %s", channel, payload);

      if (CROSS_MODULE_CHANNEL.equals(channel)) {
        processCrossModuleEvent(payload);
      } else if (LEAD_STATUS_CHANNEL.equals(channel)) {
        processLeadStatusChange(payload);
      }

    } catch (Exception e) {
      Log.errorf(e, "Error processing notification from channel %s", channel);
    }
  }

  /**
   * Processes cross-module events for activity timeline.
   *
   * @param payload The event payload
   */
  private void processCrossModuleEvent(String payload) {
    try {
      JsonNode event = objectMapper.readTree(payload);
      String eventType = event.get("eventType").asText();
      String module = event.get("module").asText();

      // Log for activity timeline
      logActivityTimelineEvent(eventType, module, event.get("payload"));

      // Handle specific cross-module events
      switch (eventType) {
        case "CUSTOMER_CREATED":
          handleCustomerCreated(event.get("payload"));
          break;
        case "EMAIL_SENT":
          handleEmailSent(event.get("payload"));
          break;
        case "CAMPAIGN_TRIGGERED":
          handleCampaignTriggered(event.get("payload"));
          break;
        default:
          Log.debugf("Received event type: %s from module: %s", eventType, module);
      }

    } catch (Exception e) {
      Log.errorf(e, "Failed to process cross-module event");
    }
  }

  /**
   * Processes lead status change events.
   *
   * @param payload The event payload
   */
  private void processLeadStatusChange(String payload) {
    try {
      LeadStatusChangeEvent event = objectMapper.readValue(payload, LeadStatusChangeEvent.class);

      // Log for activity timeline - mask PII (company name)
      Log.infof(
          "Lead status changed: ID %s (%s -> %s) by %s",
          event.leadId(), event.oldStatus(), event.newStatus(), event.userId());

      // Update activity timeline
      updateActivityTimeline(event);

    } catch (Exception e) {
      Log.errorf(e, "Failed to process lead status change");
    }
  }

  /**
   * Logs event to activity timeline.
   *
   * @param eventType Type of event
   * @param module Source module
   * @param payload Event payload
   */
  private void logActivityTimelineEvent(String eventType, String module, JsonNode payload) {
    // This would integrate with the activity timeline service
    // For now, we just log it
    Log.infof("Activity Timeline: [%s] %s from %s", System.currentTimeMillis(), eventType, module);
  }

  /**
   * Updates activity timeline with lead status change.
   *
   * @param event The status change event
   */
  private void updateActivityTimeline(LeadStatusChangeEvent event) {
    // This would update the activity timeline in the database
    // For now, we log the timeline entry
    Log.infof(
        "Timeline Entry: Lead %s status changed from %s to %s at %s",
        event.leadId(), event.oldStatus(), event.newStatus(), event.changedAt());
  }

  // Event handlers for cross-module integration

  private void handleCustomerCreated(JsonNode payload) {
    Log.infof("Customer created from lead: %s", payload);
    // Would update lead status to CONVERTED
  }

  private void handleEmailSent(JsonNode payload) {
    Log.infof("Email sent for lead communication: %s", payload);
    // Would update lead activity log
  }

  private void handleCampaignTriggered(JsonNode payload) {
    Log.infof("Campaign triggered for lead nurturing: %s", payload);
    // Would update lead campaign status
  }

  /** Stops listening to PostgreSQL notifications. */
  public void stopListening() {
    listening = false;
    // ManagedExecutor is managed by Quarkus, no need to shutdown manually
    Log.info("Stopped listening for PostgreSQL notifications");
  }
}
