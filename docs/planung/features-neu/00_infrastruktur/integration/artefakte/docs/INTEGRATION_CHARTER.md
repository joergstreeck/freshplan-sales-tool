---
Title: Integration-Charter
Purpose: Single Source of Truth für Integrationsrichtlinien (APIs, Events, Gateway, SLO/Alerts)
Audience: Backend/Frontend Entwickler, DevOps/SRE, Architecture
Last Updated: 2025-09-20
Status: Final
---

## 30-Second Summary
Hybrid-Integration: **API-Gateway** als zentraler Entry-Point (Auth, RateLimits, Idempotency, ETag), **Domain-Events** (Outbox) für Backend-Entkopplung. 
**Strong consistency** für UX-kritische Kommandos/Reads, **eventual consistency** für Aggregationen/Sync. 
**Settings-Registry** steuert Gateway-Policies **pro Tenant/Org**. **Contracts/Schemas** sind CI-Gates. 

---

## Standard-Header (alle Services)
**Write-APIs (POST/PUT/PATCH/DELETE)**
- `Idempotency-Key: <uuid>` – *required* für write-heavy, vom Server 24–48h persistiert (Route+Actor-basiert). Duplizierte Requests → gleiche Response (200/409 je nach Semantik).
- `If-Match: "<etag>"` – *required* bei Updates (Lost-Update-Schutz).

**Alle APIs**
- `X-Correlation-Id: <uuid>` – End-to-End Tracing (Gateway setzt wenn nicht vorhanden).
- `X-Tenant-Id: <uuid>` / `X-Org-Id: <uuid>` – Mandanten-/Org-Kontext; im Backend via ABAC/RLS durchgesetzt.
- `Accept: application/json` / `Content-Type: application/json`

**Caching/Conditional**
- `ETag` im Response; Client sendet `If-None-Match` bei GET → `304 Not Modified`.

---

## Idempotency-Regeln
- **Scope**: per `(Idempotency-Key, Route, Actor)` eindeutig.
- **Lebensdauer**: 24–48h (konfigurierbar per Settings-Registry: `credit.idempotency.ttl`).
- **Side effects**: Server speichert Ergebnis (HTTP/Body/ETag/Location) und gibt bei Retries identisch zurück.
- **Nicht-idempotente Aktionen** (z. B. unique Transitionen) → 409 mit RFC7807, falls Re-Submit mit anderem Payload.

---

## ETag-Handling
- **Read**: GET liefert `ETag`; `If-None-Match` → 304.
- **Write**: PUT/PATCH verlangt `If-Match` (optimistische Sperre). Mismatch → `412 Precondition Failed`.
- **Compute**: SHA-256 über Response-JSON (stabile Feldreihenfolge), im Backend/Projection gehalten.

---

## Event-Envelope (CloudEvents 1.0)
- Pflichtfelder: `id`, `type`, `source`, `specversion:"1.0"`, `time`, `datacontenttype:"application/json"`, `data`.
- Erweiterungen (Extensions): `correlationId`, `tenantId`, `orgId` (alle als Strings; UUID/ULID empfohlen).
- **At-least-once** Delivery; **idempotente** Konsumenten (dedupe-store per `id`). 
- **Schema**: JSON-Schema in `/schemas/cloudevents-event-envelope.v1.json`.

**Naming & Versionierung**
- `type`: `<domain>.<aggregate>.<event>` (z. B. `sample.status.changed`). 
- **Additive Evolution**: nur optionale Felder ergänzen. Breaking Change ⇒ neuer `type`/`/v2` Route.

---

## Retry/Backoff Policies
**HTTP Clients (Sync)**
- Exponential Backoff mit Jitter: initial 100ms, Faktor 2.0, max 2s, max 5 Versuche (nur für idempotente GET/PUT).
- For non-idempotent POST: 1 Retry nur bei `502/503/504` *und* `Idempotency-Key` vorhanden.

**Event Publisher**
- Outbox→Publisher Retries mit Backoff/Jitter; DLQ ab N Fehlversuchen (z. B. N=10). Replay über Admin-Tool.

**Consumers**
- Retry bei Transienten; Circuit-Breaker öffnen ab 5× Fehler p95> Schwelle; Dead-letter ab wiederholtem Failure.

---

## SLOs & Alerts (Normal vs. Peak)
**Sync APIs**
- Normal: Cockpit/Reads p95 < 200ms; Credit-PreCheck p95 < 300ms.
- Peak (5×): Reads p95 < 350ms; Credit-PreCheck p95 < 500ms; Error-Rate < 2%.
**Event Processing**
- Normal: Ingest→Consume p95 < 1s.
- Peak: p95 < 3–5s; Backlog < 10k Events; DLQ = 0 (Alert > 0).

**Alerts (Prometheus)**
- `http_request_duration_seconds{route}` p95 über Schwellwert > 5 min → WARN/CRIT.
- `outbox_backlog` / `consumer_lag` über Target → WARN/CRIT.
- `dlq_size > 0` sofort CRIT.

---

## Versionierungs-Guidelines
- **Additive Only**: Neue optionale Felder erlaubt; keine Pflichtfeld-Entfernung.
- **Deprecation**: Markiere als „deprecated“ im OpenAPI/Event-Schema; Sunset in Settings-Registry (`feature.sunset.*`).
- **Breaking**: Neuer Pfad (`/v2`) oder neuer `type`-Name.

---

## Integration mit Foundation
- **Settings-Registry**: Gateway-Policies (RateLimits/Auth) konfigurierbar pro Tenant/Org (Sync-Job → Gateway).
- **EVENT_CATALOG.md**: Domain-Events konsolidiert; Schema-Referenz auf Envelope v1.
- **API_STANDARDS.md**: Dieses Charter-Dokument ergänzt **Idempotency/ETag/Correlation**-Abschnitte.

---

## Troubleshooting
- Fehlende `Idempotency-Key` → 400 (Gateway) bzw. 428 (Backend) je nach Policy.
- Hohe `outbox_backlog` → Publisher/Consumer skaliert? DLQ prüfen, Replay starten.
- ETag-Mismatches gehäuft → Client-Synchronisation prüfen, If-Match/If-None-Match korrekt?

