---
Title: Integration & Event Catalog
Purpose: Einheitlicher Katalog aller domänenrelevanten Events mit Schemas, Garantien und Subscriptions.
Audience: Entwickler, Integrations-Teams, Module Owners
Last Updated: 2025-09-20
Status: Final
---

# 🔗 Integration & Event Catalog

## 30-Second Summary
- Outbox-Pattern, at-least-once, dedup via event.id.
- Kern-Topics sind versioniert, Schemas stabil, Replay unterstützt.

## Topics (Auswahl)

### lead.protection.reminder
- **Purpose:** 60T-Reminder ausgelöst
- **Schema:** `{ id, time, leadId, ownerUserId, kind:"REMINDER_60D" }`

### lead.protection.expired
- **Purpose:** Schutz abgelaufen (Assignment aufgehoben)
- **Schema:** `{ id, time, leadId }`

### credit.checked
- **Purpose:** Ergebnis eines Credit Checks
- **Schema:** `{ id, time, accountId, checkId, result:{score, band, ttl} }`

### order.synced
- **Purpose:** CRM→ERP Bestellung synchronisiert
- **Schema:** `{ id, time, orderId, erpOrderNo, status }`

### activity.created
- **Purpose:** Belegbare Aktivität erfasst
- **Schema:** `{ id, time, leadId, kind, at, createdBy }`

### sample.status.changed
- **Purpose:** Sample/Trial Phase Wechsel
- **Schema:** `{ id, time, phaseId, leadId, stage:"SAMPLE|TRIAL|PRODUCTION_CANDIDATE" }`

## Guarantees
- **Delivery:** at-least-once.
- **Ordering:** per leadId best effort; globale Reihenfolge nicht garantiert.
- **Retention:** 30–90 Tage Events; archivierte Protokolle im Warehouse.

## Retry/Replay
- **DLQ:** je Provider; Replay via checkpoint bis event.time.
- **Idempotenz:** Konsumenten müssen idempotent sein (event.id).

## Publisher/Subscriber Matrix (Beispiele)
- **Publisher:** Reminder-Engine → lead.protection.reminder
- **Publisher:** CreditService → credit.checked
- **Subscriber:** Cockpit KPIs, Auswertungen, Admin-Monitoring

## Integration Points
- **SLOs:** [PERFORMANCE_SLO_CATALOG.md](../../leistung/artefakte/PERFORMANCE_SLO_CATALOG.md)
- **Settings:** [SETTINGS_REGISTRY_COMPLETE.md](../../verwaltung/artefakte/SETTINGS_REGISTRY_COMPLETE.md)
- **Ops:** [OPERATIONS_RUNBOOK.md](../../betrieb/artefakte/OPERATIONS_RUNBOOK.md)

## Troubleshooting

### Doppelte Verarbeitung
Konsument idempotent? Dedup-Speicher aktiv?

### Eventstau
Backpressure, DLQ prüfen, Consumer-Skalierung.