package de.freshplan.infrastructure.cqrs;

import de.freshplan.infrastructure.security.AppGuc;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.security.identity.SecurityIdentity;
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

  @Inject SecurityIdentity securityIdentity;

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
  private volatile long lastConnectionCheck = 0;
  private static final long CONNECTION_CHECK_INTERVAL = 60000; // 1 minute

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
    establishListenerConnection();
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
      // Check connection health periodically
      long now = System.currentTimeMillis();
      if (now - lastConnectionCheck > CONNECTION_CHECK_INTERVAL) {
        ensureConnectionHealthy();
        lastConnectionCheck = now;
      }

      PGConnection pgConn = listenerConnection.unwrap(PGConnection.class);
      PGNotification[] notifications = pgConn.getNotifications();

      if (notifications != null && notifications.length > 0) {
        for (PGNotification notification : notifications) {
          processNotification(notification);
        }
      }
    } catch (Exception e) {
      LOG.error("Error polling notifications, attempting reconnect", e);
      handleConnectionError();
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

  /** Sets RLS context for the listener connection */
  private void setRlsContextForListenerConnection() {
    try (Statement stmt = listenerConnection.createStatement()) {
      // Set session-level context for event listener
      // This connection is long-lived and processes events from all territories
      // IMPORTANT: Using setSessionConfigSql (SET) instead of setConfigSql (SET LOCAL)
      // because this connection has autoCommit=true and lives beyond transactions

      // Use configured system user instead of SecurityIdentity (which may be anonymous in background)
      String systemUser = System.getProperty("security.rls.system-user", "events-bus@freshplan");
      stmt.execute(AppGuc.CURRENT_USER.setSessionConfigSql(systemUser));

      // Set system role for event processing (needs to see all events)
      stmt.execute(AppGuc.CURRENT_ROLE.setSessionConfigSql("SYSTEM"));

      LOG.debugf("RLS context set for event listener: user=%s, role=SYSTEM", systemUser);
    } catch (Exception e) {
      LOG.error("Failed to set RLS context for listener connection", e);
      throw new RuntimeException("Cannot establish secure event listener", e);
    }
  }

  /** Establishes or re-establishes the listener connection */
  private void establishListenerConnection() throws Exception {
    // Close existing connection if any
    if (listenerConnection != null && !listenerConnection.isClosed()) {
      try {
        listenerConnection.close();
      } catch (Exception e) {
        LOG.debug("Error closing old connection", e);
      }
    }

    // Create new connection
    listenerConnection = dataSource.getConnection();
    listenerConnection.setAutoCommit(true);

    // Set RLS context - MUST succeed or we fail
    setRlsContextForListenerConnection();

    // Re-subscribe to all channels
    for (String channel : defaultChannels.split(",")) {
      subscribeToChannel(channel.trim());
    }

    lastConnectionCheck = System.currentTimeMillis();
    LOG.info("Event listener connection established successfully");
  }

  /** Ensures the connection is healthy, reconnects if needed */
  private void ensureConnectionHealthy() {
    try {
      if (listenerConnection == null || listenerConnection.isClosed() || !listenerConnection.isValid(5)) {
        LOG.warn("Listener connection unhealthy, reconnecting...");
        establishListenerConnection();
      }
    } catch (Exception e) {
      LOG.error("Connection health check failed", e);
      handleConnectionError();
    }
  }

  /** Handles connection errors by attempting to reconnect */
  private void handleConnectionError() {
    if (!running) {
      return;
    }

    int retryCount = 0;
    int maxRetries = 3;
    long retryDelay = 1000; // Start with 1 second

    while (running && retryCount < maxRetries) {
      try {
        Thread.sleep(retryDelay);
        LOG.infof("Attempting to reconnect event listener (attempt %d/%d)", retryCount + 1, maxRetries);
        establishListenerConnection();
        LOG.info("Event listener reconnected successfully");
        return;
      } catch (Exception e) {
        retryCount++;
        retryDelay *= 2; // Exponential backoff
        LOG.errorf("Reconnection attempt %d failed: %s", retryCount, e.getMessage());
      }
    }

    LOG.error("Failed to reconnect after " + maxRetries + " attempts. Event listener disabled.");
    running = false;
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
