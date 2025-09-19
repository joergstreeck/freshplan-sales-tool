# CRM Kundenmanagement – Deploy & Operations Guide

Stand: 2025-09-19

## Umfang
Dieses Paket enthält:
- OpenAPI Spezifikationen (`openapi/`): `samples.yaml`, `activities.yaml`, `fields.yaml`, `common-errors.yaml`
- SQL Schemata & Funktionen (`sql/`): `field_bridge_and_projection.sql`, `samples.sql`, `observability_views.sql`, `retention_policies.sql`
- Postman Collection (`postman/CRM_CustomerManagement.postman_collection.json`)

## Vorbedingungen
- PostgreSQL 14+ mit `pgcrypto`
- Java 17+/Quarkus Backend
- Keycloak/JWT für Bearer Auth
- Truststore-Setup (kein Wildcard-Trust)

## Migrations-Reihenfolge
1. `sql/field_bridge_and_projection.sql`
2. `sql/samples.sql`
3. `sql/observability_views.sql`
4. `sql/retention_policies.sql`

## Feature-Flags
- `features.samples=true`
- `features.activities.b2b_food=true` (V1: PRODUCTTEST_FEEDBACK, ROI_CONSULTATION)
- `features.fields.bridge=true`

## SLOs
- API P95 < 200 ms
- Page Load < 2 s
- Error Budget < 0,5 %

## Monitoring (Empfehlung)
- Panels: `sample_metrics_daily`, `hot_projection_staleness`, API-Latenzen, WS-Disconnects
- Alerts: Staleness > 900 s (15 min), Bounce-Rate > 5 %, Outbox-Fail > 10 %

## Event-Resilienz
- Producer mit `event_outbox` (idempotent)
- Consumer mit Checkpoints (lastEventId)
- Admin: `/events/replay?fromId=...` (siehe API des Event-Services)
- Cockpit: Events + DB-Polling (30 s) mit Stale-Indicator

## Migration (Zero-Downtime)
- Expand → Backfill → Dual-Write → Read-Switch → Contract (Cleanup)
- Backfill-Jobs: Feld-Mapping in `field_values`, `recompute_customer_hot(...)` pro Kunde
- Rollback: Feature-Flags deaktivieren, Read-Switch zurück auf Legacy

## Policies
- **Default-Kit**: Cook&Fresh® Basis 5er als Voreinstellung bei `POST /samples`
- **Bounce-Policy**: HARD Bounce ⇒ `contactability_status=HARD_BOUNCE` + Pflichtaktion „alternativen Kanal hinterlegen“

## Sicherheit
- RBAC: Sales-Rep (Location-Write), Team-Lead (Region), Chain-Manager (Kette)
- Feld-Level-Restriktionen für sensible Felder (Rabatte/Verträge)
