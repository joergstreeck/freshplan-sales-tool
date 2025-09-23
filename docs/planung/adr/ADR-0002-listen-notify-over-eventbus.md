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

## Implementation Details

### Event-Channels:
```sql
-- Settings-Invalidation
NOTIFY settings_invalidated, '{"scope": "user", "userId": 123}';

-- Cross-Module Events
NOTIFY lead_updated, '{"leadId": 456, "status": "qualified"}';

-- System Events
NOTIFY system_event, '{"type": "maintenance", "message": "Backup started"}';
```

### Performance Constraints:
- **Event-Lag SLO:** `listen_notify_lag_ms < 10000`
- **Payload-Size:** Max 8KB per Event (PostgreSQL-Limit)
- **Volume-Limit:** <1000 Events/Minute (Monitoring-Alert)

## Alternativen

1. **Kafka:** Abgelehnt wegen Komplexität + Kosten
2. **RabbitMQ:** Abgelehnt wegen zusätzlicher Infrastruktur
3. **Redis Pub/Sub:** Abgelehnt wegen fehlender Persistierung
4. **CloudEvents:** Abgelehnt wegen Over-Engineering

## Compliance

- **SLO:** `listen_notify_lag_ms < 10000` erfüllt
- **Schema-Validation:** JSON-Schema für alle Event-Types
- **Monitoring:** Event-Volume + Lag-Time Dashboards