# ADR-0002: PostgreSQL LISTEN/NOTIFY statt Event-Bus

**Status:** Akzeptiert
**Datum:** 18.09.2025
**Autor:** Development Team

## Kontext

Für die Event-basierte Kommunikation zwischen Modulen (insbesondere Settings-Invalidation, Cross-Module-Updates) benötigen wir ein Event-System. Zur Auswahl stehen:
- PostgreSQL LISTEN/NOTIFY
- Dedicated Event-Bus (Kafka, RabbitMQ, Redis)
- CloudEvents + Message Queue

## Entscheidung

Wir implementieren **PostgreSQL LISTEN/NOTIFY** mit:
- JSON-Payload für Event-Daten
- Channel-basierte Event-Kategorien
- Synchrone und asynchrone Event-Verarbeitung
- Keine externen Message-Broker

## Begründung

### Pro PostgreSQL LISTEN/NOTIFY:
- **Minimale Infrastruktur:** Nutzt bereits vorhandene PostgreSQL-DB
- **Transaktionale Garantien:** Events Teil der DB-Transaktion
- **Einfache Implementation:** Native PostgreSQL-Features
- **Bewährte Technologie:** Bereits PostgreSQL-Expertise im Team
- **Cost-Efficient:** Keine zusätzliche Infrastruktur nötig

### Contra Event-Bus:
- **Over-Engineering:** Für 5-50 Nutzer zu komplex
- **Zusätzliche Infrastruktur:** Kafka/RabbitMQ-Cluster erforderlich
- **Höhere Komplexität:** Separates Monitoring, Backup, etc.
- **Budget-Impact:** Zusätzliche Hosting-Kosten

## Konsequenzen

### Positive:
- Sofort verfügbar (keine neue Infrastruktur)
- Transaktionale Konsistenz
- Einfaches Debugging (SQL-Logs)
- Bewährte PostgreSQL-Stabilität

### Negative:
- Event-System limitiert auf Single-DB
- Keine Persistierung der Events
- Bei hohem Event-Volume potentielle DB-Belastung
- Weniger Advanced Features (Replay, Dead Letters)

### Mitigationen:
- Event-Volume monitoring implementieren
- Bei Bedarf später Migration zu dediziertem Event-Bus
- JSON-Schema-Validation für Event-Payloads

## Implementation Details (PR #111 Update)

### PgNotifySender Abstraktion:
```java
// Production Implementation
@ApplicationScoped
public class HibernatePgNotifySender implements PgNotifySender {
    @PersistenceContext EntityManager entityManager;

    public void send(String channel, String payload) {
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("SELECT pg_notify(?, ?)")) {
                ps.setString(1, channel);
                ps.setString(2, payload);
                ps.execute();
            }
        });
    }
}

// Test Alternative
@Alternative @Priority(1) @ApplicationScoped
public class TestPgNotifySender implements PgNotifySender {
    private final List<Send> sent = new CopyOnWriteArrayList<>();
    // Collect notifications for testing without real PostgreSQL
}
```

### AFTER_COMMIT Pattern (nur in Publishern):
```java
// RICHTIG: Publisher mit AFTER_COMMIT
txRegistry.registerInterposedSynchronization(new Synchronization() {
    public void afterCompletion(int status) {
        if (status == Status.STATUS_COMMITTED) {
            pgNotifySender.send(channel, payload);
        }
    }
});

// WICHTIG: Listener brauchen kein AFTER_COMMIT (laufen außerhalb TX)
// Stattdessen: Idempotenz via Caffeine Cache
```

### 8KB NOTIFY Guard mit Truncation:
```java
if (payload.length() > 8000) {
    // Preserve envelope, truncate data field
    envelope.put("data", Map.of(
        "truncated", true,
        "original_size_bytes", originalSize,
        "idempotencyKey", idempotencyKey
    ));
}
```

### Event-Deduplizierung im Listener:
```java
// Caffeine Cache für Idempotenz
Cache<String, Boolean> dedupeCache = Caffeine.newBuilder()
    .maximumSize(500_000)
    .expireAfterWrite(24, TimeUnit.HOURS)
    .recordStats()
    .build();

// Deterministische Keys
String key = UUID.nameUUIDFromBytes(
    (leadId + "|" + type + "|" + timestamp).getBytes()
).toString();
```

### Event-Channels:
```sql
-- Dashboard Updates
NOTIFY dashboard_updates, '{"type": "dashboard.lead_status_changed", ...}';

-- Cross-Module Events
NOTIFY cross_module_events, '{"type": "LEAD_STATUS_CHANGED", ...}';

-- Settings-Invalidation
NOTIFY settings_invalidated, '{"scope": "user", "userId": 123}';
```

### Performance Constraints:
- **Event-Lag SLO:** `listen_notify_lag_ms < 10000`
- **Payload-Size:** Max 8KB per Event (mit Truncation-Handling)
- **Volume-Limit:** <1000 Events/Minute (Monitoring-Alert)
- **Dedupe-Cache:** 500k Entries max, 24h TTL, 90% Capacity-Warning

### Prometheus Metriken:
```yaml
# Counter ohne _total suffix (Micrometer-Standard)
freshplan_events_published{event_type,module,result}
freshplan_events_consumed{event_type,module,result}

# Histogramme für Latency
freshplan_event_latency{event_type,path}

# Gauges für Cache
freshplan_dedupe_cache_entries
freshplan_dedupe_cache_hit_rate
```

## Alternativen

1. **Kafka:** Abgelehnt wegen Komplexität + Kosten
2. **RabbitMQ:** Abgelehnt wegen zusätzlicher Infrastruktur
3. **Redis Pub/Sub:** Abgelehnt wegen fehlender Persistierung
4. **CloudEvents:** Abgelehnt wegen Over-Engineering

## Compliance

- **SLO:** `listen_notify_lag_ms < 10000` erfüllt
- **Schema-Validation:** JSON-Schema für alle Event-Types
- **Monitoring:** Event-Volume + Lag-Time Dashboards