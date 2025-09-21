---
Title: Migration Roadmap – LISTEN/NOTIFY → Event Bus
Purpose: Schrittweiser Pfad von Postgres LISTEN/NOTIFY zu einem dedizierten Event-Bus ohne Topic-Bruch
Audience: DevOps/SRE, Backend-Teams
Last Updated: 2025-09-20
Status: Final
---

## 30-Second Summary
Wir behalten Outbox als **Source of Truth**, publizieren heute via **LISTEN/NOTIFY** und migrieren schrittweise auf einen **Event‑Bus** (z. B. Kafka/NATS) per **Connector**, ohne Topic‑/Type‑Umbenennung. Dual‑Publish in Übergangsphase, Consumers migrieren in Wellen. 

---

## Phasen
**Phase 0 – Ist**
- Outbox (immutable), Publisher nutzt LISTEN/NOTIFY.

**Phase 1 – Connector einführen**
- Neuer **Bus‑Publisher** liest dieselbe Outbox (status = READY), publiziert ins Bus‑Topic (`{domain}.{aggregate}`), markiert status = SENT.
- LISTEN/NOTIFY Publisher bleibt aktiv (Dual‑Publish).

**Phase 2 – Consumer Dual‑Read**
- Kritische Consumers lesen bereits vom **Bus**; Legacy lesen weiter über NOTIFY‑Pipeline.
- Lag/Fehler‑Metriken vergleichen; Alerts auf DLQ/lag.

**Phase 3 – Cutover**
- NOTIFY‑Publisher stoppen; Topics unverändert.
- Alle Consumers auf Bus; Outbox bleibt als **Truth + Replay** (Checkpoint).

**Phase 4 – Cleanup**
- NOTIFY‑Artefakte entfernen; Runbooks/Docs aktualisieren.

---

## Technische Leitplanken
- **Idempotenz**: gleicher `event.id` für beide Publisher; Consumers deduplizieren.
- **Ordering**: Partitionierung via `tenantId|orgId|aggregateId`.
- **Schema**: Envelope konstant; `data`‑Schemas versioniert.
- **Observability**: `outbox_backlog`, `publisher_errors`, `consumer_lag`, `dlq_size`.

---

## Rollback
- Bei Bus‑Instabilität Publisher stoppen, NOTIFY‑Publisher aktivieren; Consumers wieder an alte Pipeline.

---
