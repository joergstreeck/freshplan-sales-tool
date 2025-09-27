---
sprint_id: "2.1.5"
title: "Lead Matching & Review-Flow – Phase 2"
status: "draft"
date_start: "2025-10-07"
date_end: "2025-10-11"
modules: ["02_neukundengewinnung"]
entry_points:
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/frontend/_index.md"
  - "features-neu/02_neukundengewinnung/analyse/api/"
pr_refs: []
updated: "2025-09-27"
---

# Sprint 2.1.5 – Lead Matching & Review-Flow (Phase 2)

> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **Modul-Start (_index.md) → Status prüfen**
> 3. **Backend:** `backend/` für Match-API
> 4. **Frontend:** `frontend/` für Kandidaten-Modal
> 5. **API-Contracts:** `analyse/api/` für OpenAPI-Spec

## Sprint-Ziel

Statt hartem 409-Block bei Duplikaten werden Kandidaten vorgeschlagen. User können bewusst entscheiden: bestehenden Lead verwenden oder trotzdem neu anlegen.

## User Stories

### 1. Backend: `POST /api/leads:match`
**Akzeptanzkriterien:**
- Request: `{ name?, email?, phone?, company? }`
- Response: `candidates[{ leadId, score(0..1), reasons[], preview{...} }]`
- Scoring:
  - E-Mail exakt: 1.0
  - Telefon exakt: 0.98
  - Domain+Name ähnlich: ~0.7
  - Name+Company: ~0.6
- Performance: <200ms für typische Queries

### 2. Frontend: Kandidaten-Modal + Entscheidungsflow
**Akzeptanzkriterien:**
- Beim Speichern: erst `:match` API aufrufen
- Bei Kandidaten: Modal zeigt:
  - Liste der Kandidaten mit Score
  - „Bestehenden Lead öffnen/verwenden" Button
  - „Trotzdem neu anlegen" Button (`?force=true`)
- i18n-Texte vorhanden (de/en)
- Barrierefreiheit: Fokus-Management, ARIA-Labels

### 3. Backend: `force=true` Semantik & Audit
**Akzeptanzkriterien:**
- Server erlaubt bewusstes „trotzdem neu" mit `?force=true`
- Entscheidung wird geloggt (für KPIs)
- Event: `leads_match_resolved_force_new`

### 4. QA/Telemetrie: KPIs & E2E
**Akzeptanzkriterien:**
- Metriken erfasst:
  - Trefferquote Match-API
  - Rate „trotzdem neu"
  - Zeit bis Entscheidung
- E2E-Tests für Standard- und Kandidatenfall

## Definition of Done (Sprint)

- [ ] **Match-API funktionsfähig mit Scoring**
- [ ] **Kandidaten-Modal im Frontend integriert**
- [ ] **Force-Create mit Audit-Log**
- [ ] **i18n für alle neuen UI-Elemente**
- [ ] **Telemetrie-Events implementiert**
- [ ] **E2E-Tests grün**
- [ ] **API-Dokumentation (OpenAPI) aktuell**

## Technische Details

### Match-Algorithmus:
- **Exakte Matches:** E-Mail/Telefon normalisiert vergleichen
- **Fuzzy Matches:**
  - Levenshtein-Distance für Namen
  - Domain-Extraktion aus E-Mail für Company-Match
- **Score-Aggregation:** Gewichteter Durchschnitt der Match-Faktoren

### Frontend-Flow:
1. User füllt Lead-Form aus
2. Klick auf „Speichern"
3. Frontend ruft `:match` API
4. Wenn Kandidaten: Modal öffnen
5. User-Entscheidung:
   - Use existing → Navigation zu Lead
   - Force create → `POST /api/leads?force=true`

## Feature-Flags

- `LEADS_MATCH_ENABLED=true|false` (Frontend & Backend)
- `MATCH_SCORE_THRESHOLD=0.5` (Backend, Kandidaten unter Threshold werden ignoriert)

## Risiken & Mitigation

- **UX-Abbrüche:** Primäre Aktion klar machen (Use existing), Force-Create sekundär
- **Performance:** Match-Query optimieren (Indizes auf normalized fields)
- **False Positives:** Score-Threshold anpassbar per Config

## Abhängigkeiten

- Sprint 2.1.4 muss abgeschlossen sein (Normalisierung als Basis für Matching)
- MSW muss Match-Endpoint mocken können

## Next Sprint Preview

Sprint 2.1.6 (optional): Merge/Unmerge & Historie – Identitätsgraph light für Lead-Zusammenführung

## Out of Scope (dieser Sprint)

- Merge/Unmerge von Leads
- Machine Learning für Match-Scoring
- Bulk-Deduplizierung bestehender Daten