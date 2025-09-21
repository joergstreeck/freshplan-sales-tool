---
Title: PostgreSQL LISTEN/NOTIFY Patterns für CQRS Light
Purpose: Event-Patterns für One-Database-Architecture ohne Event-Bus
Audience: Backend-Teams, DevOps
Last Updated: 2025-09-21
Status: CQRS Light Optimized
---

## 30-Second Summary
CQRS Light nutzt PostgreSQL LISTEN/NOTIFY als primäre Event-Lösung für 5-50 interne Benutzer. Keine Event-Bus Migration nötig - LISTEN/NOTIFY ist ausreichend performant und wartungsfreundlich für interne Tools.

---

## CQRS Light Event-Architecture

### Core Pattern: Simple JSON Events via LISTEN/NOTIFY
```sql
-- Trigger für Command-Seite
CREATE OR REPLACE FUNCTION notify_domain_event()
RETURNS TRIGGER AS $$
BEGIN
  PERFORM pg_notify(
    'domain_events',
    json_build_object(
      'event_type', NEW.event_type,
      'aggregate_id', NEW.aggregate_id,
      'payload', NEW.payload,
      'created_at', NEW.created_at,
      'user_id', NEW.user_id
    )::text
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Query-Service lauscht auf Events
LISTEN domain_events;
```

### Event-Typen für Module 01-08
```yaml
Lead Events (Module 02):
  - lead.created
  - lead.assigned
  - lead.protection.expired

Customer Events (Module 03):
  - customer.created
  - customer.updated
  - customer.contact.added

Settings Events (Module 06):
  - settings.updated
  - settings.scope.changed

Help Events (Module 07):
  - help.struggle.detected
  - help.assistance.triggered
```

---

## Performance Characteristics

### CQRS Light Baseline
- **Latency:** <10ms für NOTIFY dispatch
- **Throughput:** 1000+ events/sec (mehr als genug für 5-50 Benutzer)
- **Reliability:** Transactional mit Database-Commits
- **Complexity:** Minimal - keine zusätzliche Infrastructure

### Why LISTEN/NOTIFY für CQRS Light?
1. **Cost-Efficient:** Keine Event-Bus Infrastructure-Kosten
2. **Simple:** One-Database-Architecture
3. **Sufficient:** Performant genug für interne Tools
4. **Reliable:** PostgreSQL-Transactionen garantieren Konsistenz

---

## Implementation Pattern

### 1. Command-Service publiziert Event

**PostgreSQL Publisher Utility (von externer KI empfohlen):**
```sql
-- Channel-Konvention: 'ffz_events' (kompakte JSON <= ~8KB)
CREATE OR REPLACE FUNCTION ffz_notify(
    p_event text,
    p_org text,
    p_territory text,
    p_corr text,
    p_data jsonb
)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE v_payload jsonb;
BEGIN
  v_payload := jsonb_build_object(
    'event', p_event,
    'id', gen_random_uuid(),
    'time', now(),
    'tenant','freshfoodz',
    'org', p_org,
    'territory', p_territory,
    'correlationId', coalesce(p_corr,''),
    'version', 1,
    'data', p_data
  );
  PERFORM pg_notify('ffz_events', v_payload::text);
END $$;
```

**Java Service Implementation:**
```java
@Transactional
public Customer createCustomer(CreateCustomerRequest request) {
    // Business Logic
    Customer customer = customerRepository.save(...);

    // Event via LISTEN/NOTIFY
    em.createNativeQuery("""
        SELECT ffz_notify(
            'customer.created',
            :org,
            :territory,
            :correlationId,
            :data
        )
        """)
        .setParameter("org", customer.getOrgId())
        .setParameter("territory", customer.getTerritory())
        .setParameter("correlationId", MDC.get("correlationId"))
        .setParameter("data", JsonUtils.toJson(customer))
        .getSingleResult();

    return customer;
}
```

### 2. Query-Service konsumiert Event

**Quarkus Reactive Listener (von externer KI empfohlen):**
```java
@ApplicationScoped
public class SimpleEventListener {
    @Inject io.vertx.pgclient.PgPool pgPool;

    @PostConstruct
    void init() {
        pgPool.getConnection(ar -> {
            if (ar.failed()) {
                Log.error("Failed to connect for LISTEN", ar.cause());
                return;
            }
            var conn = ar.result();

            // Setup notification handler
            conn.notificationHandler(notification -> {
                if (!"ffz_events".equals(notification.getChannel())) return;

                var json = new io.vertx.core.json.JsonObject(notification.getPayload());
                handleEvent(json);
            });

            // Start listening
            conn.query("LISTEN ffz_events").execute();
        });
    }

    void handleEvent(io.vertx.core.json.JsonObject evt) {
        var type = evt.getString("event");
        var data = evt.getJsonObject("data");

        switch (type) {
            case "customer.created" -> updateCustomerView(data);
            case "lead.status.changed" -> updateLeadView(data);
            case "sample.delivered" -> createFollowUpTask(data);
            default -> Log.debug("Unhandled event type: " + type);
        }
    }

    @Transactional
    void updateCustomerView(JsonObject data) {
        // Update Read-Model für CQRS Light Query-Service
        CustomerView view = JsonUtils.fromJson(data.toString(), CustomerView.class);
        customerViewRepository.save(view);
    }
}
```

---

## Monitoring

### Essential Metrics für CQRS Light

**PostgreSQL Queue Monitoring (von externer KI empfohlen):**
```sql
-- Event-Backlog (sollte <100 für interne Tools)
SELECT COUNT(*) as backlog
FROM domain_events
WHERE processed_at IS NULL;

-- PostgreSQL NOTIFY Queue Usage
SELECT
    pg_notification_queue_usage() as queue_usage_percent,
    CASE
        WHEN pg_notification_queue_usage() > 0.7 THEN 'WARNING'
        WHEN pg_notification_queue_usage() > 0.9 THEN 'CRITICAL'
        ELSE 'OK'
    END as status;
```

**Prometheus Metrics:**
```yaml
# Application Metrics
- simple_events_published_total{event_type}
- simple_events_handled_total{event_type}
- simple_events_handle_errors_total{event_type}
- simple_events_handle_duration_seconds{event_type}

# PostgreSQL Metrics
- pg_notification_queue_usage # Via postgres_exporter
- outbox_dispatch_lag_seconds # Für E-Mail Outbox
```

---

## Migration Strategy: Nicht nötig!

**CQRS Light Decision:** LISTEN/NOTIFY bleibt die permanente Lösung für FreshFoodz als internes Tool. Keine Event-Bus Migration geplant oder nötig.

### Falls später doch Skalierung nötig (>100 Benutzer):
1. Performance-Monitoring zeigt Bottlenecks
2. Entscheidung basiert auf echten Metriken
3. Migration dann möglich via Outbox-Pattern

**Aber für 5-50 Benutzer: LISTEN/NOTIFY ist optimal!**

---

## Best Practices für CQRS Light

1. **Keep Events Small:** JSON-Payloads <8KB (PostgreSQL NOTIFY Limit)
2. **Process Quickly:** Query-Services sollten Events <100ms verarbeiten
3. **Monitor Queue:** Alert wenn pg_notification_queue_usage >70%
4. **Use Transactions:** Events immer in gleicher Transaction wie Business-Data
5. **Idempotent Consumers:** Doppelte NOTIFYs müssen safe sein
6. **Correlation IDs:** Immer mitführen für Request-Tracing
7. **Territory/Org Scoping:** Events immer mit territory/org für RLS

## WebSocket Bridge für Realtime UI

**Von LISTEN/NOTIFY zu WebSocket (externe KI Empfehlung):**
```java
@ServerEndpoint("/ws/events")
@ApplicationScoped
public class EventWebSocketBridge {
    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @Inject SimpleEventListener eventListener;

    @OnOpen
    public void onOpen(Session session) {
        // Validate user permissions
        String territory = extractTerritory(session);
        session.getUserProperties().put("territory", territory);
        sessions.add(session);
    }

    // Called by SimpleEventListener when events arrive
    public void broadcastEvent(JsonObject event) {
        String eventTerritory = event.getString("territory");

        sessions.parallelStream()
            .filter(s -> eventTerritory.equals(s.getUserProperties().get("territory")))
            .forEach(session -> {
                try {
                    session.getBasicRemote().sendText(event.toString());
                } catch (IOException e) {
                    Log.error("WebSocket send failed", e);
                }
            });
    }
}
```

---

**🎯 CQRS Light mit LISTEN/NOTIFY: Die perfekte Lösung für FreshFoodz als internes Tool!**