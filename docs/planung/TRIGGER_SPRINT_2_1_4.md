---
sprint_id: "2.1.4"
title: "Lead Deduplication & Data Quality – Phase 1"
doc_type: "konzept"
status: "in_progress"
owner: "team/leads-backend"
date_start: "2025-09-28"
date_end: "2025-10-04"
modules: ["02_neukundengewinnung"]
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/backend/TEST_MIGRATION_PLAN.md"  # KRITISCH: CI-Fix
  - "features-neu/02_neukundengewinnung/analysis/_index.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_4/SUMMARY.md"
pr_refs: []
updated: "2025-09-28"
---

# Sprint 2.1.4 – Lead Deduplication & Data Quality (Phase 1)

**📍 Navigation:** Home → Planung → Sprint 2.1.4

> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **Modul-Start (_index.md) → Status prüfen**
> 3. **Backend-Overlay:** `backend/` für Normalisierung & Unique-Indizes
> 4. **Shared für ADRs:** `shared/adr/` für Architekturentscheidungen
> 5. **Artefakte:** DB-Migrationen in `backend/migrations/`

## 🚨 KRITISCHES CI-PROBLEM ENTDECKT (30.09.2025)

**Problem:** 164 von 171 Tests nutzen @QuarkusTest mit echter DB → 20+ Minuten CI-Timeout!
**Lösung:** Test-Migrationsstrategie läuft parallel → siehe [`backend/TEST_MIGRATION_PLAN.md`](features-neu/02_neukundengewinnung/backend/TEST_MIGRATION_PLAN.md)
**Impact:** CI blockiert alle PRs! Höchste Priorität!

## Sprint-Ziel

Deterministische Duplikatvermeidung durch Normalisierung und Unique-Indizes auf E-Mail/Telefon, plus Idempotenz für resiliente API-Calls.

## User Stories

### 1. Backend: Normalisierung E-Mail/Telefon + Unique-Indizes
**Akzeptanzkriterien:**
- E-Mail wird normalisiert (lowercase, trim; optional „+tag"-Entfernung per Flag)
- Telefon wird in E.164 persistiert (nur wenn valide)
- Partielle Unique-Indizes auf `email_normalized` und `phone_e164` (WHERE NOT NULL)
- 409-Response bei Kollision inkl. RFC7807 mit `errors.email`/`errors.phone`

### 2. Backend: Idempotency-Key bei `POST /api/leads`
**Akzeptanzkriterien:**
- Header `Idempotency-Key` wird akzeptiert
- Innerhalb eines Zeitfensters gleiches Ergebnis (201/200)
- Ohne Key: Standardverhalten bleibt

### 3. Frontend: Idempotency-Key setzen
**Akzeptanzkriterien:**
- FE generiert pro „Speichern" einen `Idempotency-Key` (crypto.randomUUID())
- 409 wird userfreundlich angezeigt (bestehendes Verhalten)

### 4. Dokumentation & Telemetrie
**Akzeptanzkriterien:**
- ADRs zu Normalisierung/Unique/Idempotenz liegen vor
- SQL-Migrationen vorhanden (Up/Down)
- Basis-Events erfasst (Create-Attempt, Conflict)

## Definition of Done (Sprint)

- [ ] **Normalisierte Persistenz + Indizes aktiv**
- [ ] **Idempotenz funktionsfähig**
- [ ] **Telemetrie zeigt Konfliktrate**
- [ ] **Relevante Modul-Docs aktualisiert** (`backend/_index.md`, `shared/adr/`)
- [ ] **Link-Check grün** (keine kaputten Links)
- [ ] **CI/CD grün** (Tests, Linting)

## Technische Details

### Normalisierung (Backend):
- **E-Mail:** `lower(trim(email))`; optional: Local-Part „+tag" entfernen (Flag: `EMAIL_REMOVE_PLUS_TAGS`)
- **Telefon:** libphonenumber nach E.164; bei Fehler: `phone_e164=NULL`

### Feature-Flags:
- `EMAIL_REMOVE_PLUS_TAGS=true|false` (Backend)
- `IDEMPOTENCY_TTL_SECONDS=3600` (Backend)

## Risiken & Mitigation

- **False Positives:** Plus-Tag-Entfernung nur per Flag
- **Idempotency-Store Wuchs:** TTL + periodischer Cleanup
- **Breaking Change:** Neue Unique-Constraints könnten bestehende Duplikate blockieren → Migration muss Duplikate vorher bereinigen

## Abhängigkeiten

- Sprint 2.1.3 muss merged sein (Lead-Basis vorhanden)
- Backend muss Flyway-Migrationen unterstützen

## Next Sprint Preview

Sprint 2.1.5: Lead Matching & Review-Flow (Phase 2) – statt hartem 409 Kandidaten vorschlagen