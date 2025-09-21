---
Title: Foundation-Integration-Guide (Integration Infrastructure)
Purpose: Anleitung zur nahtlosen Integration der Integrationsrichtlinien mit vorhandenen Foundations
Audience: Architektur, DevOps, Modul-Owner
Last Updated: 2025-09-20
Status: Final
---

## 30-Second Summary
Dieses Dokument verankert Integration‑Charter, Gateway‑Policies und Event‑Envelope **in eurer Foundation**:
**Settings‑Registry** steuert Gateway‑Limits/Auth pro Tenant/Org, **EVENT_CATALOG.md** wird um neue Domain‑Events erweitert, **API_STANDARDS.md** erhält Idempotency/ETag/Correlation‑Abschnitte. Migration von **LISTEN/NOTIFY** auf **Event‑Bus** erfolgt ohne Topic‑Bruch.

---

## Settings‑Registry → Gateway Policies
**Keys (Beispiele)** – Scopes: tenant/org
- `gateway.rate.limit.rpm` (integer) – Requests pro Minute
- `gateway.auth.enabled` (boolean)
- `gateway.cors.allowed.origins` (list[string])
- `gateway.idempotency.ttl.hours` (integer, global)
- `gateway.routes.<name>.policy` (object) – per Route Overrides

**Sync‑Mechanismus**
- **Job**: `settings-gateway-sync` liest Effective‑Settings je Tenant/Org und rendert/patcht Gateway‑Config (Kong decK/Envoy xDS). 
- Sync‑Frequenz: default 5 Min; manuell triggerbar (Admin). 
- **Dry‑Run + Diff** vor Apply; Audit‑Log.

---

## EVENT_CATALOG.md Erweiterung
- Ergänze **neue Typen**: `sample.status.changed`, `trial.phase.started/ended`, `product.feedback.recorded`, `credit.prechecked/checked`, `order.submitted/synced`.
- Verweise im Catalog auf **Envelope‑Schema** (`/schemas/cloudevents-event-envelope.v1.json`).
- **Schema‑Ablage**: für jeden Event ein `data`‑Teilschema (JSON Schema) in `/schemas/events/<type>.v1.json`.
- **Publisher/Subscriber Matrix** pro Typ aktualisieren.

---

## API_STANDARDS.md Enhancement
**Neue Kapitel** (copy/paste):
- **Idempotency**: Headerpflicht, 24–48h Scope, dedupe‑Store, 200/409 Semantik.
- **ETag/If‑Match**: Optimistische Sperre, 304 für GET, 412 bei Mismatch.
- **Correlation‑ID**: Pflicht, generiert vom Gateway bei Abwesenheit.
- Beispiele (cURL) und Response‑Beispiele einpflegen.

---

## Compliance/Monitoring Querverweise
- **SLO Catalog** (Normal/Peak) → Verlinke die Schwellen für Gateway/Services/Events.
- **Operations Runbook** → Outbox Backlog, DLQ, Replay.
- **Data Governance** → Keine PII in Events, nur IDs; PII via Sync‑API holen.

