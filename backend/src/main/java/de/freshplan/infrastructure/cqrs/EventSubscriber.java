package de.freshplan.infrastructure.cqrs;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

/**
 * CQRS Light Event Subscriber using PostgreSQL LISTEN/NOTIFY Handles incoming events from
 * PostgreSQL channels Optimized for 5-50 internal users with <200ms processing
 */
@ApplicationScoped
public class EventSubscriber {

  private static final Logger LOG = Logger.getLogger(EventSubscriber.class);

  @Inject DataSource dataSource;

  @Inject Event<EventNotification> eventBus;

  @ConfigProperty(name = "cqrs.subscriber.enabled", defaultValue = "true")
  boolean subscriberEnabled;

  @ConfigProperty(name = "cqrs.subscriber.poll-interval-ms", defaultValue = "100")
  int pollInterval;

  @ConfigProperty(name = "cqrs.subscriber.channels", defaultValue = "cqrs_all_events")
  String defaultChannels;

  private final Map<String, Consumer<JsonObject>> handlers = new ConcurrentHashMap<>();
  private ScheduledExecutorService executor;
  private Connection listenerConnection;
  private volatile boolean running = false;

  /** Starts the event subscriber on application startup */
  void onStart(@Observes StartupEvent ev) {
    if (!subscriberEnabled) {
      LOG.info("CQRS Event Subscriber is disabled");
      return;
    }

    try {
      startListening();
      LOG.info("CQRS Event Subscriber started successfully");
    } catch (Exception e) {
      LOG.error("Failed to start CQRS Event Subscriber", e);
    }
  }

  /** Stops the event subscriber on application shutdown */
  void onStop(@Observes ShutdownEvent ev) {
    stopListening();
    LOG.info("CQRS Event Subscriber stopped");
  }

  /** Registers a handler for a specific event type */
  public void registerHandler(String eventType, Consumer<JsonObject> handler) {
    handlers.put(eventType, handler);
    LOG.infof("Registered handler for event type: %s", eventType);
  }

  /** Subscribes to a PostgreSQL channel */
  public void subscribeToChannel(String channel) {
    if (listenerConnection == null) {
      LOG.warn("Cannot subscribe to channel - listener not started");
      return;
    }

    try (Statement stmt = listenerConnection.createStatement()) {
      stmt.execute("LISTEN " + channel);
      LOG.infof("Subscribed to channel: %s", channel);
    } catch (Exception e) {
      LOG.error("Failed to subscribe to channel: " + channel, e);
    }
  }

  /** Starts listening for PostgreSQL notifications */
  private void startListening() throws Exception {
    // Create dedicated connection for LISTEN/NOTIFY
    listenerConnection = dataSource.getConnection();
    listenerConnection.setAutoCommit(true);

    // Subscribe to all configured channels
    for (String channel : defaultChannels.split(",")) {
      subscribeToChannel(channel.trim());
    }

    running = true;

    // Start polling for notifications
    executor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "cqrs-event-listener");
              t.setDaemon(true);
              return t;
            });

    executor.scheduleWithFixedDelay(
        this::pollNotifications, 0, pollInterval, TimeUnit.MILLISECONDS);
  }

  /** Stops listening for PostgreSQL notifications */
  private void stopListening() {
    running = false;

    if (executor != null) {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }

    if (listenerConnection != null) {
      try {
        listenerConnection.close();
      } catch (Exception e) {
        LOG.error("Error closing listener connection", e);
      }
    }
  }

  /** Polls for PostgreSQL notifications */
  private void pollNotifications() {
    if (!running) {
      return;
    }

    try {
      PGConnection pgConn = listenerConnection.unwrap(PGConnection.class);
      PGNotification[] notifications = pgConn.getNotifications();

      if (notifications != null && notifications.length > 0) {
        for (PGNotification notification : notifications) {
          processNotification(notification);
        }
      }
    } catch (Exception e) {
      LOG.error("Error polling notifications", e);
    }
  }

  /** Processes a single notification */
  private void processNotification(PGNotification notification) {
    try {
      String channel = notification.getName();
      String payload = notification.getParameter();

      LOG.debugf(
          "Received notification on channel %s: %s",
          channel, payload.substring(0, Math.min(payload.length(), 100)));

      JsonObject event = new JsonObject(payload);
      String eventType = event.getString("event_type");

      // Fire CDI event for internal processing
      EventNotification eventNotification = new EventNotification(channel, eventType, event);
      eventBus.fire(eventNotification);

      // Call registered handler if exists
      Consumer<JsonObject> handler = handlers.get(eventType);
      if (handler != null) {
        handler.accept(event);
      }

      // Update metrics
      recordEventProcessed(eventType);

    } catch (Exception e) {
      LOG.error("Error processing notification", e);
    }
  }

  /** Records event processing metrics */
  private void recordEventProcessed(String eventType) {
    // TODO: Integrate with monitoring system
    LOG.tracef("Event processed: %s", eventType);
  }

  /** Event notification for CDI event bus */
  public static class EventNotification {
    private final String channel;
    private final String eventType;
    private final JsonObject payload;

    public EventNotification(String channel, String eventType, JsonObject payload) {
      this.channel = channel;
      this.eventType = eventType;
      this.payload = payload;
    }

    public String getChannel() {
      return channel;
    }

    public String getEventType() {
      return eventType;
    }

    public JsonObject getPayload() {
      return payload;
    }

    @Override
    public String toString() {
      return String.format("EventNotification[channel=%s, type=%s]", channel, eventType);
    }
  }
}
