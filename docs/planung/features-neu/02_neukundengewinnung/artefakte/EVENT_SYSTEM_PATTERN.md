---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---
# 🔔 Event System Pattern - PostgreSQL LISTEN/NOTIFY

**Status:** ✅ IMPLEMENTED (PR #110)
**Erstellt:** 26.09.2025
**Referenz:** `/backend/src/main/java/de/freshplan/modules/leads/events/`

## 🎯 Pattern Overview

CQRS Light Event-System mit PostgreSQL LISTEN/NOTIFY für Cross-Module-Kommunikation.

## 🏗️ Architecture

### Event Publisher mit AFTER_COMMIT
```java
@ApplicationScoped
public class LeadEventPublisher {

    @Inject DataSource dataSource;
    @Inject ObjectMapper objectMapper;
    @Inject TransactionSynchronizationRegistry txRegistry;

    @Transactional
    public void publishStatusChange(Lead lead, LeadStatus oldStatus,
                                   LeadStatus newStatus, String changedBy) {
        try {
            // Stable timestamp für Idempotency
            LocalDateTime changedAt = lead.updatedAt != null
                ? lead.updatedAt
                : LocalDateTime.now(ZoneOffset.UTC);

            // Deterministische Idempotency-Key
            String keySource = lead.id + "|" + oldStatus + ">" +
                newStatus + "|" +
                changedAt.toInstant(ZoneOffset.UTC).toEpochMilli();
            String idempotencyKey = UUID.nameUUIDFromBytes(
                keySource.getBytes(StandardCharsets.UTF_8)).toString();

            LeadStatusChangeEvent event = new LeadStatusChangeEvent(
                lead.id, lead.companyName, oldStatus, newStatus,
                changedBy, lead.territory?.id, lead.ownerUserId,
                idempotencyKey, changedAt);

            String payload = objectMapper.writeValueAsString(event);

            // AFTER_COMMIT garantiert keine Ghost-Events
            if (txRegistry != null && txRegistry.getTransactionKey() != null) {
                txRegistry.registerInterposedSynchronization(
                    new Synchronization() {
                        @Override
                        public void beforeCompletion() {}

                        @Override
                        public void afterCompletion(int status) {
                            if (status == Status.STATUS_COMMITTED) {
                                notifyChannel("lead_status_changes", payload);
                                publishCrossModuleEvent("LEAD_STATUS_CHANGED", payload);
                            }
                        }
                    });
            } else {
                // Fallback für non-transactional context (Tests)
                notifyChannel("lead_status_changes", payload);
            }

        } catch (JsonProcessingException e) {
            Log.errorf(e, "Failed to serialize event for lead %s", lead.id);
        }
    }

    private void notifyChannel(String channel, String payload) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT pg_notify(?, ?)")) {

            ps.setString(1, channel);
            ps.setString(2, payload);
            ps.execute();

        } catch (Exception e) {
            Log.errorf(e, "CRITICAL: Failed to send NOTIFY on channel %s", channel);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
```

## 🎧 Event Listener mit ManagedExecutor

```java
@ApplicationScoped
public class CrossModuleEventListener {

    @Inject DataSource dataSource;
    @Inject ObjectMapper objectMapper;
    @Inject ManagedExecutor managedExecutor;

    private volatile boolean listening = false;

    void onStart(@Observes StartupEvent ev) {
        listening = true;
        managedExecutor.submit(() -> listenToChannel("lead_status_changes"));
        managedExecutor.submit(() -> listenToChannel("cross_module_events"));
        Log.info("Started listening for PostgreSQL notifications");
    }

    void onStop(@Observes ShutdownEvent ev) {
        stopListening();
    }

    @PreDestroy
    void cleanup() {
        stopListening();
    }

    private void listenToChannel(String channel) {
        while (listening) {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute("LISTEN " + channel);
                Log.infof("Listening on channel: %s", channel);

                PGConnection pgConn = conn.unwrap(PGConnection.class);

                while (listening) {
                    // Blockierendes Polling (10 Sekunden Timeout)
                    PGNotification[] notifications =
                        pgConn.getNotifications(10000);

                    if (notifications != null) {
                        for (PGNotification notification : notifications) {
                            processNotification(
                                notification.getName(),
                                notification.getParameter());
                        }
                    }
                }

            } catch (Exception e) {
                Log.errorf(e, "Error listening to channel %s, retrying...", channel);
                Thread.sleep(5000); // Wait before retry
            }
        }
    }
}
```

## 📦 Event-Typen

### Lead Status Change Event
```java
public class LeadStatusChangeEvent {
    private Long leadId;
    private String companyName;
    private LeadStatus oldStatus;
    private LeadStatus newStatus;
    private String changedBy;
    private LocalDateTime changedAt;
    private String territory;
    private String ownerUserId;
    private String idempotencyKey;

    // Constructor mit stabilen Timestamps
    public LeadStatusChangeEvent(..., LocalDateTime changedAt) {
        // ...
        this.changedAt = changedAt != null
            ? changedAt
            : LocalDateTime.now();
    }
}
```

### Cross-Module Event Wrapper
```json
{
  "eventType": "LEAD_STATUS_CHANGED",
  "module": "leads",
  "payload": {
    // Lead-spezifische Event-Daten
  }
}
```

## 🧪 Testing mit TestEventCollector

```java
@ApplicationScoped
public class TestEventCollector {

    private final BlockingQueue<LeadStatusChangeEvent> events =
        new LinkedBlockingQueue<>();

    void onEvent(@Observes @Priority(APPLICATION - 100)
                 LeadStatusChangeEvent event) {
        events.add(event);
    }

    public LeadStatusChangeEvent pollStatusChangeEvent(
            long timeout, TimeUnit unit) throws InterruptedException {
        return events.poll(timeout, unit);
    }

    public boolean hasReceivedEventWithKey(String idempotencyKey) {
        return events.stream()
            .anyMatch(e -> idempotencyKey.equals(e.getIdempotencyKey()));
    }
}
```

## ⚡ Best Practices

### 1. Idempotency
- Deterministische Keys mit UUID.nameUUIDFromBytes()
- Basiere auf Entity-Timestamps (updatedAt/createdAt)
- Vermeide System.currentTimeMillis()

### 2. Transaction Safety
- IMMER TransactionSynchronizationRegistry nutzen
- AFTER_COMMIT garantiert keine Ghost-Events
- Fallback für Test-Umgebungen

### 3. Resource Management
- ManagedExecutor statt Executors.newFixedThreadPool()
- @PreDestroy für sauberes Shutdown
- Blockierendes getNotifications() statt Polling

### 4. Error Handling
- Kritische Fehler loggen und werfen
- Retry-Logic mit exponential backoff
- Health-Checks für Listener-Status

## 📊 Performance & Monitoring

### Metriken
- Event-Latency: < 100ms (P95)
- Listener-Uptime: > 99.9%
- Event-Loss: 0% (mit Retry)

### Monitoring
```java
// Micrometer Metrics
@Inject MeterRegistry registry;

Counter eventCounter = registry.counter("leads.events.published");
Timer eventLatency = registry.timer("leads.events.latency");

// In publishStatusChange:
Timer.Sample sample = Timer.start(registry);
// ... publish event ...
sample.stop(eventLatency);
eventCounter.increment();
```

## 🔗 Cross-Module Integration

### Event-Routing Map
| Event | Source | Consumers |
|-------|--------|-----------|
| LEAD_STATUS_CHANGED | Leads | Cockpit, Analytics, Communication |
| LEAD_CREATED | Leads | Cockpit, Settings |
| CUSTOMER_CREATED | Customer | Leads (Status-Update) |
| EMAIL_SENT | Communication | Leads (Activity-Log) |
| CAMPAIGN_TRIGGERED | Campaign | Leads, Analytics |

### Integration-Code
```java
// In anderen Modulen
@ApplicationScoped
public class LeadEventConsumer {

    void onLeadStatusChange(@Observes LeadStatusChangeEvent event) {
        // Update Dashboard-Widget
        // Refresh Analytics
        // Trigger Follow-up Actions
    }
}
```

## 📚 Referenzen
- [PR #110 Implementation](https://github.com/joergstreeck/freshplan-sales-tool/pull/110)
- [Gemini Review](https://github.com/joergstreeck/freshplan-sales-tool/pull/110#issuecomment-3339529993)
- [PostgreSQL LISTEN/NOTIFY Docs](https://www.postgresql.org/docs/current/sql-notify.html)
- [Quarkus ManagedExecutor](https://quarkus.io/guides/context-propagation)