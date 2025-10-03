---
sprint_id: "2.1.5"
title: "Lead Protection & Progressive Profiling (B2B)"
doc_type: "konzept"
status: "in_progress"
owner: "team/leads-backend"
date_start: "2025-10-05"
date_end: "2025-10-11"
modules: ["02_neukundengewinnung"]
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/frontend/_index.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/SUMMARY.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DSGVO_CONSENT_SPECIFICATION.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/FRONTEND_DELTA.md"
  - "frontend/FRONTEND_ACCESSIBILITY.md"
pr_refs: []
updated: "2025-10-04"
---

# Sprint 2.1.5 – Lead Protection & Progressive Profiling (B2B)

**📍 Navigation:** Home → Planung → Sprint 2.1.5

> **⚠️ TEST-STRATEGIE BEACHTEN!**
> Tests MÜSSEN Mocks verwenden, NICHT @QuarkusTest mit echter DB!
> Siehe: [`backend/TEST_MIGRATION_PLAN.md`](features-neu/02_neukundengewinnung/backend/TEST_MIGRATION_PLAN.md)
>
> **⚠️ DATENBANK-MIGRATIONEN BEACHTEN!**
> Vor Migration-Arbeit IMMER [`MIGRATIONS.md`](MIGRATIONS.md) lesen!
> - Alle Migrations V1-V257 dokumentiert (inkl. V255-V257 für diesen Sprint)
> - V10xxx Test/Dev-Range erklärt (Production Skip)
> - CONCURRENTLY-Regeln für Production
> - Nächste Nummer: `./scripts/get-next-migration.sh`
>
> **🎨 FRONTEND DESIGN SYSTEM BEACHTEN!**
> Vor Frontend-Arbeit IMMER [`/docs/planung/grundlagen/DESIGN_SYSTEM.md`](grundlagen/DESIGN_SYSTEM.md) lesen!
> - **FreshFoodz CI:** #94C456 (Green), #004F7B (Blue), Antonio Bold, Poppins
> - **UI-Framework:** MUI v7 + Emotion (KEIN Tailwind!)
> - **SmartLayout:** Intelligente Content-Breiten (Tables 100%, Forms 800px)
> - **Accessibility:** WCAG 2.1 AA/AAA Standards
> - **Sprache:** Deutsch (Dashboard → Übersicht, Save → Speichern)
> - **AI-Kontext:** [`CRM_AI_CONTEXT_SCHNELL.md`](CRM_AI_CONTEXT_SCHNELL.md) für kompakten Überblick
>
> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **Modul-Start (_index.md) → Status prüfen**
> 3. **Backend:** V255-V257 Migrations für Lead Protection (siehe MIGRATIONS.md)
> 4. **Frontend:** Progressive Profiling UI (3 Stufen) - **Design System beachten!**
> 5. **Shared:** Vertragliche Anforderungen dokumentieren
> 6. **Compliance:** Data-Retention-Plan umsetzen

## Sprint-Ziel

Implementierung der vertraglichen Lead-Schutz-Mechanismen (6 Monate, 60-Tage-Regel) und Progressive Profiling für B2B-Lead-Erfassung.

## User Stories

### 1. Lead Protection Backend
**Akzeptanzkriterien:**
- Lead-Protection-Tabelle mit 6-Monats-Schutz
- Activity-Tracking für 60-Tage-Progress
- Stop-the-Clock Mechanismus
- Automatische Status-Updates (protected → warning → expired)

### 2. Progressive Profiling (3 Stufen) + Pre-Claim
**Stufe 0: Vormerkung (Pre-Claim möglich!)**
- **Pflicht:** Firma, Stadt, Quelle (Dropdown), Zugewiesen an (Partner)
- **Optional:** Branche
- **Keine** personenbezogenen Daten erforderlich
- **PRE-CLAIM MECHANIK:**
  - **WENN** Kontakt vorhanden **ODER** Erstkontakt dokumentiert → ✅ Schutz startet (registered_at = now)
  - **SONST** → ❌ Pre-Claim (registered_at = NULL, kein Schutz, 10 Tage Frist)
  - **Ausnahme:** Bestandsleads bei Migration → sofortiger Schutz
- **Erstkontakt-Pflichtblock (wenn kein Kontakt):**
  - UI zeigt Block "Erstkontakt dokumentieren" mit 3 Pflichtfeldern:
    - **Kanal** (Dropdown): Telefon, Email, Messe-Stand, Persönlich
    - **Datum/Uhrzeit** (DateTimePicker): Wann fand Erstkontakt statt
    - **Kurznotiz** (Textarea, min. 10 Zeichen): Was wurde besprochen
  - Erzeugt Activity: `FIRST_CONTACT_DOCUMENTED` (countsAsProgress=false, startet Schutz)
- **Quellenspezifische Pflichtfelder:**
  - `MESSE`: Event-Name (Pflicht) → in Activity.summary/metadata
  - `EMPFEHLUNG`: Referrer-Name/Firma (Pflicht) → in Activity.summary/metadata
  - `TELEFON`: Kanal + Notiz (Pflicht wenn kein Kontakt) → Erstkontakt-Block

**Stufe 1: Lead-Registrierung**
- **Pflicht:** Vorname, Nachname, Email ODER Telefon
- **Consent-Logic:**
  - `source = WEB_FORMULAR` → Consent-Checkbox PFLICHT
    - **⚠️ WICHTIG:** Feld `consent_given_at` kommt erst in V259 (Sprint 2.1.6 Web-Intake)
    - Frontend in 2.1.5: UI vorbereitet, Backend-Feld NICHT vorhanden
    - Validierung erfolgt nur Frontend-seitig, keine Backend-Persistierung
  - `source != WEB_FORMULAR` → Info-Text "berechtigtes Interesse (Art. 6 Abs. 1 lit. f)"
- Source Tracking erweitert: MESSE, EMPFEHLUNG, TELEFON, WEB_FORMULAR
- Owner Assignment via Geo+Workload (Sprint 2.1.6: Verfügbarkeit)

**Stufe 2: Qualifiziert**
- **Optional:** VAT ID, Expected Volume, Key Account Flags
- EDL/Chain Affiliation
- Auto-Next-Actions (z.B. "Sample senden" → Activity + Reminder)

### 3. Activity Tracking UI
**Akzeptanzkriterien:**
- Activity Log (Call/Email/Meeting/Demo)
- Progress-Indicator (Days since last activity)
- Warning-Badge bei < 7 Tage bis Deadline
- Stop-the-Clock Dialog mit Grund

### Pre-Claim UI-Komponenten (Sprint 2.1.5 Frontend Phase 2)

**Listen-Filter:**
- **"Alle Pre-Claim Leads"**: `WHERE registered_at IS NULL`
- **"Pre-Claim (läuft ab ≤3 Tage)"**: `WHERE registered_at IS NULL AND created_at < NOW() - INTERVAL '7 days'`
- **"Pre-Claim (läuft ab ≤10 Tage)"**: `WHERE registered_at IS NULL AND created_at >= NOW() - INTERVAL '10 days'`
- **"Pre-Claim (abgelaufen)"**: `WHERE registered_at IS NULL AND created_at < NOW() - INTERVAL '10 days'`

**Performance & Index-Strategie:**
- **Partieller Index** (empfohlen für Production):
  ```sql
  -- V259 (Sprint 2.1.6 - Performance-Optimierung)
  CREATE INDEX CONCURRENTLY idx_leads_preclaim_created
    ON leads (created_at)
    WHERE registered_at IS NULL;
  ```
- **Rationale:** Pre-Claim Queries filtern IMMER `registered_at IS NULL` + `created_at` Range
- **Query-Planner Nutzung:** Partial Index wird automatisch gewählt (bessere Selectivity)
- **Monitoring:** P95 Query-Zeit sollte <50ms bleiben (Grafana Alert bei >100ms)

**Badge im LeadHeader:**
- **Text:** "Pre-Claim"
- **Color:** Orange (#FFA500)
- **Tooltip:** "Kein Schutz aktiv – Vervollständigen bis [created_at + 10 Tage]"
- **Icon:** ⏳ (Sanduhr)

**Lead-Liste Spalte:**
- Neue Spalte "Status" zeigt Pre-Claim Badge prominent
- Sortierung: Pre-Claim Leads zuerst (ASC created_at)

### 4. Protection Endpoints & Compliance
**Akzeptanzkriterien:**
- POST /lead-protection/{id}/reminder - Erinnerung + 10-Tage-Nachfrist
- POST /lead-protection/{id}/extend - Verlängerung auf Antrag
- POST/DELETE /lead-protection/{id}/stop-clock - Stop-the-Clock
- DELETE /lead-protection/{id}/personal-data - DSGVO-Löschung
- Retention-Jobs für automatische Pseudonymisierung
- **PUT /api/leads/{id}/registered-at** (Admin/Manager)
  - Validierung: nicht in Zukunft; Reason Pflicht
  - Audit: `lead_registered_at_backdated`
  - Folge: Recalc Schutz-/Aktivitätsfristen (falls Felder vorhanden)

### 5. Dedupe Policy (Sprint 2.1.5: Hard Collisions Only)
**Akzeptanzkriterien:**
- **Harte Kollisionen (BLOCK + Manager-Override):**
  - Email exakt (normalisiert) ODER
  - Telefon exakt (E.164) ODER
  - Firma + PLZ exakt (normalisiert)
  - → **409 Conflict** (RFC 7807 Problem+JSON)
  - → Override nur Manager/Admin + `overrideReason` (Pflicht, min. 10 Zeichen)
  - → Audit-Log: `lead_duplicate_override`
- **Weiche Kollisionen (WARN + Fortfahren):**
  - Gleiche Email-Domain UND gleiche Stadt/PLZ ODER
  - Gleiche Firma (exakt) UND gleiche Stadt
  - → **409 Conflict** mit `severity: "WARNING"`
  - → Fortfahren erlaubt: Jeder Nutzer + `reason` (Pflicht, min. 10 Zeichen)
- **KEIN Fuzzy-Matching** (pg_trgm) in 2.1.5 → Sprint 2.1.6
- UI: DuplicateLeadDialog (Hard) + SimilarLeadDialog (Soft)
  - Button: "Existierenden Lead öffnen" + "Trotzdem anlegen"

**Dedupe Resubmit-Flow (einheitlich 409):**
1. **Hard Collision** (kein `severity` Feld):
   - Resubmit mit `overrideReason` (Query-Param, min. 10 Zeichen)
   - Benötigt MANAGER oder ADMIN Role
   - Beispiel: `POST /api/leads?overrideReason=Unterschiedliche%20Standorte`
   - Audit-Log: `lead_duplicate_override`
2. **Soft Collision** (`severity: "WARNING"`):
   - Resubmit mit `reason` (Query-Param, min. 10 Zeichen)
   - Beliebige Role (kein Manager-Override)
   - Beispiel: `POST /api/leads?reason=Neue%20Niederlassung`
   - Kein Audit-Log
**Frontend:** Beide Dialoge zeigen Reason-Textarea PFLICHT vor Resubmit

**RFC 7807 – Problem+JSON (Erweiterung):**
```json
{
  "type": "https://freshplan/errors/duplicate-lead",
  "title": "Duplicate lead detected",
  "status": 409,
  "detail": "Email bereits registriert: max@example.com",
  "extensions": {
    "severity": "WARNING",
    "duplicates": [
      {
        "leadId": "uuid-123",
        "companyName": "Beispiel GmbH",
        "city": "München",
        "postalCode": "80331",
        "ownerUserId": "uuid-owner"
      }
    ]
  }
}
```
- **`extensions.severity`**: `"WARNING"` bei Soft Collision, fehlt bei Hard Collision
- **`extensions.duplicates[]`**: Liste potenzieller Duplikate mit `leadId`, `companyName`, optional `city`/`postalCode`/`ownerUserId`

## Technische Details

### Backend Changes:
```sql
-- V255-V258: Sprint 2.1.5 Migrations (Inline-First Architecture, ADR-004)
-- Separate lead_protection Table NICHT implementiert (siehe ADR-004)
-- Nächste Migration: V259 (für Sprint 2.1.6 Lead-Transfers + consent_given_at)

-- V255: Protection Felder in leads Table (Inline)
ALTER TABLE leads ADD COLUMN progress_warning_sent_at TIMESTAMPTZ;
ALTER TABLE leads ADD COLUMN progress_deadline TIMESTAMPTZ;
ALTER TABLE leads ADD COLUMN stage SMALLINT NOT NULL DEFAULT 0;

-- V256: lead_activities Augmentation
ALTER TABLE lead_activities ADD COLUMN counts_as_progress BOOLEAN DEFAULT FALSE;
ALTER TABLE lead_activities ADD COLUMN summary VARCHAR(500);
ALTER TABLE lead_activities ADD COLUMN outcome VARCHAR(50);
ALTER TABLE lead_activities ADD COLUMN performed_by VARCHAR(50);

-- V257: Helper Functions + Triggers
CREATE FUNCTION calculate_protection_until(...);
CREATE FUNCTION calculate_progress_deadline(...);
CREATE TRIGGER update_progress_on_activity ...;
```

### API Extensions:
```json
POST /api/leads (Enhanced):
{
  "stage": 0|1|2,
  "companyName": "required",
  "city": "required",
  "contact": {...},  // optional
  "source": "partner",
  "ownerUserId": "UUID"
}

Responses:
- 201: Created (no duplicates)
- 409: Hard duplicate exists (email/phone normalized)
```

### Protection Endpoints:
```json
POST /lead-protection/{leadId}/reminder
POST /lead-protection/{leadId}/extend
  Body: {"reason": "string", "duration": "P30D"}
POST /lead-protection/{leadId}/stop-clock
  Body: {"reason": "waiting_for_freshfoodz"}
DELETE /lead-protection/{leadId}/stop-clock
DELETE /lead-protection/{leadId}/personal-data
```

### Frontend Components:
- `LeadWizard.tsx` - Progressive form (3 Stufen), Full-Page Component
- `LeadProtectionBadge.tsx` - Status indicator mit Tooltip/ARIA
- `ActivityTimeline.tsx` - Progress tracking display
- **NICHT in 2.1.5:** `ExtensionRequestDialog` - verschoben auf 2.1.6
- **NICHT in 2.1.5:** `StopTheClockDialog` - verschoben auf 2.1.6 (Manager-only UI)

### Live-Dedupe-Hints (UX-Optimierung - OPTIONAL)

**⚠️ HINWEIS:** Gute UX, aber zusätzlicher Scope - kann später nachgezogen werden.

**Verhalten:**
- Throttle: 300ms nach letzter Eingabe in Email/Phone/Company/PostalCode
- API-Endpoint: `GET /api/leads/check-duplicate?email=...&phone=...&company=...&postalCode=...`
- Response: `{ "isDuplicate": boolean, "duplicateLeads": [...], "severity": "HARD"|"SOFT" }`

**UI-Feedback:**
- Email-Feld: Inline-Warnung "Ähnlicher Lead existiert" (orange) bei Soft
- Email-Feld: Inline-Error "Lead existiert bereits" (rot) bei Hard
- Tooltip zeigt Duplikat-Details (Firma, Stadt, Owner)
- Kein Submit-Block, nur Info (Block erst bei POST 409)

**Performance:**
- Debounce 300ms (zu schnell → API-Overload, zu langsam → schlechte UX)
- Cache 60s (gleiche Query wiederholt → kein API-Call)
- Max. 3 Kandidaten in Tooltip (mehr → "... und X weitere")

**Backend:**
- Nutzt gleiche DuplicateDetectionService wie POST /api/leads
- Keine Audit-Logs (nur bei echtem Submit)
- Rate-Limit: 10 req/min pro User

### DSGVO & Compliance (KRITISCH):
**Consent-Management (Source-abhängig):**
- **Stage 0 (Vormerkung)**: Keine personenbezogenen Daten → Kein Consent nötig
- **Stage 1 (Lead-Registrierung)**:
  - **Consent NUR bei `source = WEB_FORMULAR`** (Lead registriert sich selbst)
    - Checkbox PFLICHT: "Ich stimme zu, dass meine Kontaktdaten gespeichert werden (Widerruf jederzeit möglich)"
    - **⚠️ Backend-Feld `lead.consent_given_at` erst in Sprint 2.1.6 (V259)**
    - Sprint 2.1.5: Frontend-Validierung only, kein Backend-Persist
    - Validierung: Ohne Consent KEIN Submit möglich
  - **KEIN Consent bei Partner-Erfassung** (Messe, Telefon, Empfehlung)
    - Info-Text: "Daten werden auf Basis berechtigten Interesses gespeichert (Art. 6 Abs. 1 lit. f DSGVO)"
    - Rechtliche Grundlage: B2B-Geschäftsanbahnung (berechtigtes Interesse)
- **Rechtsgrundlage**:
  - Web-Formular: Consent (Art. 6 Abs. 1 lit. a DSGVO)
  - Partner-Erfassung: Berechtigtes Interesse (Art. 6 Abs. 1 lit. f DSGVO)

### Activity-Types Progress-Mapping:
**Vollständige Mapping:** Siehe [ACTIVITY_TYPES_PROGRESS_MAPPING.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md)

**Kurzübersicht (13 Types):**
- ✅ **countsAsProgress=true (5):** QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT
- ❌ **countsAsProgress=false (8):** NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK + 3 System-Activities

**V257 Trigger-Behavior:**
- Trigger `update_progress_on_activity` feuert NUR bei `counts_as_progress = TRUE`
- System-Activities (FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED) → KEIN Trigger-Fire

**⚠️ KRITISCH: V258 Migration ERFORDERLICH!**
- **DB-Constraint gefunden:** V238 hat `CHECK (activity_type IN (...))`
- **7 Activity-Types fehlen** in V238 Constraint!
- **V258 Fix:** Constraint erweitern mit allen 13 Types aus ACTIVITY_TYPES_PROGRESS_MAPPING.md

### Stop-the-Clock Rules (Backend-only in 2.1.5):
**RBAC-Policy:**
- **Pausieren/Resume**: Nur MANAGER + ADMIN Role
- **UI-Button**: NICHT in Phase 2 (verschoben auf 2.1.6)
- **Erlaubte Gründe**:
  - "FreshFoodz Verzögerung" (vertraglicher Grace-Period-Trigger)
  - "Kunde im Urlaub" (temporäre Pausierung)
  - "Andere" (mit Freitext-Begründung)
- **Audit-Log**: PFLICHT für jeden Stop/Resume Event
- **Max. Pausendauer**: TBD (Business-Regel in 2.1.6)

## PR-Strategie (Backend/Frontend Split)

**Phase 1: Backend (PR #124)** - Branch: `feature/mod02-sprint-2.1.5-lead-protection`
- ✅ Migrations V255-V257 (Progress Tracking + Stage + Functions/Trigger)
- ✅ Entities: Lead.java (+3), LeadActivity.java (+6)
- ✅ Service: LeadProtectionService (Stage-Validierung, Progress-Deadlines)
- ✅ Tests: 24 Unit Tests (0.845s, Pure Mockito, 100% passed)
- ✅ Dokumentation: ADR-004, DELTA_LOG_2_1_5, CONTRACT_MAPPING, TEST_PLAN, SUMMARY
- **Status:** READY FOR PR

**Phase 2: Frontend (PR #125)** - Branch: `feature/mod02-sprint-2.1.5-frontend-progressive-profiling`
- ⏸️ LeadWizard.tsx (3-Stufen Progressive Profiling UI, Full-Page Component)
- ⏸️ DSGVO Consent-Checkbox (Stage 1, UI-only - Backend-Feld erst Sprint 2.1.6)
- ⏸️ LeadProtectionBadge.tsx (Status-Indicator mit Tooltip/Responsive/ARIA)
- ⏸️ ActivityTimeline.tsx (Progress Tracking Display mit countsAsProgress Filter)
- ⏸️ API-Integration: Enhanced POST /api/leads mit Stage-Validierung + Consent
- ✅ Integration Tests für Progressive Profiling Flow
- ✅ FRONTEND_ACCESSIBILITY.md Dokumentation
- ✅ LeadWizard ist Standard (Feature-Flag entfernt)
- **Status:** COMPLETE (03.10.2025)

**Logo-Status (Bereits optimiert ✅):**
- `/cockpit` Route verwendet MainLayoutV2 + HeaderV2 (Logo.tsx 19 KB) ✅
- SalesCockpitV2 hat KEINEN eigenen CockpitHeader ✅
- Alle aktiven Routen verwenden HeaderV2 mit optimiertem Logo ✅

**PFLICHT für Sprint 2.1.5 Backend Phase 2:**
- **V258:** Activity-Type Constraint erweitern (13 Types aus ACTIVITY_TYPES_PROGRESS_MAPPING.md)
  - QUALIFIED_CALL, DEMO, ROI_PRESENTATION, SAMPLE_FEEDBACK (bereits im Enum, fehlen in V238!)
  - FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED (NEU)

**Verschoben auf Sprint 2.1.6:**
- **V259:** lead_transfers Tabelle (Lead-Transfer zwischen Partnern)
- PUT /api/leads/{id}/registered-at (Backdating Endpoint für Bestandsleads)
- POST /api/admin/migration/leads/import (Bestandsleads-Migrations-API, Modul 08)
- Lead → Kunde Convert Flow (automatische Übernahme bei QUALIFIED → CONVERTED)
- StopTheClockDialog UI (Manager-only, mit Approval-Workflow)
- ExtensionRequestDialog UI (Schutzfrist-Verlängerung auf Antrag)
- Nightly Jobs (Warning/Expiry/Pseudonymisierung - Scheduled Tasks)
- Vollständiger Fuzzy-Matching Algorithmus (Levenshtein-Distance, pg_trgm)
- DuplicateReviewModal (Merge/Unmerge UI mit Identitätsgraph)

**Begründung für Split:**
- Konsistent mit Sprint 2.1.2/2.1.3 Pattern (Frontend/Backend getrennt)
- Kleinere, fokussierte PRs (easier Review)
- Backend kann schneller merged werden
- Frontend kann parallel entwickelt werden

## Definition of Done (Sprint 2.1.5 - Gesamtpaket)

**Basierend auf ChatGPT/Claude Implementierungsplan validiert 2025-10-03:**

### Backend (Phase 1 - COMPLETE 01.10.2025):
- [x] **V255-V257 Migrations deployed & tested**
- [x] **Entity Updates (Lead.java, LeadActivity.java)**
- [x] **Service Extensions (LeadProtectionService)**
- [x] **Unit Tests grün (24 Tests, 100% passed)**
- [x] **Dokumentation: ADR-004, DELTA_LOG, CONTRACT_MAPPING, TEST_PLAN**

### Backend (Phase 2 - COMPLETE 04.10.2025):
- [x] **V258 Migration deployed & tested (13 Activity-Types)**
- [x] **ActivityType.java Enum erweitert (3 neue System-Types)**
- [x] **Unit Tests grün (7 V258 Tests, 100% passed)**
- [x] **Dokumentation: MIGRATIONS.md, ACTIVITY_TYPES_PROGRESS_MAPPING.md**

### Frontend (Phase 2 - IN PROGRESS):
- [ ] **Pre-Claim Logic implementiert:**
  - [ ] `registered_at = NULL` → Pre-Claim (kein Schutz, 10 Tage Frist)
  - [ ] Erstkontakt-Pflichtblock UI (Kanal, Datum, Notiz) wenn kein Kontakt
  - [ ] Schutzstart bei Kontakt ODER dokumentiertem Erstkontakt
  - [ ] Migration-Ausnahme: Bestandsleads → sofortiger Schutz
- [ ] **Quellenspezifische Pflichtfelder:**
  - [ ] MESSE: Event-Name (Pflicht)
  - [ ] EMPFEHLUNG: Referrer-Name/Firma (Pflicht)
  - [ ] TELEFON: Kanal + Notiz (Pflicht wenn kein Kontakt)
- [ ] **DSGVO Consent Source-abhängig:**
  - [ ] `source = WEB_FORMULAR` → Consent-Checkbox PFLICHT
  - [ ] `source != WEB_FORMULAR` → Info-Text "berechtigtes Interesse"
- [ ] **Dedupe Hard/Soft Collisions:**
  - [ ] 409 Conflict bei Email/Phone/Firma+PLZ exakt (Hard)
  - [ ] 409 Conflict mit `severity: "WARNING"` bei Domain+Stadt ODER Firma+Stadt (Soft)
  - [ ] `Problem.extensions` Typ mit `severity` + `duplicates[]` (types.ts)
  - [ ] Manager-Override mit `overrideReason` (Hard, min. 10 Zeichen)
  - [ ] User-Resubmit mit `reason` (Soft, min. 10 Zeichen)
  - [ ] DuplicateLeadDialog UI (Hard) + SimilarLeadDialog UI (Soft)
- [ ] **Activity-Types erweitert (13 Types):**
  - [ ] FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED zu Enum hinzugefügt (types.ts)
  - [ ] `ACTIVITY_PROGRESS_MAP: Record<ActivityType, boolean>` implementiert (5 true, 8 false)
  - [ ] `createLead()` Query-Params Support (`reason`, `overrideReason`)
  - [ ] KEIN hardcoded `source: 'manual'` (api.ts) - `payload.source` verwenden
  - [ ] Erstkontakt-Block als `activities[]` senden (NICHT `firstContact` Feld)
- [ ] **Assignment Service (Interface-Vorbereitung):**
  - [ ] AssignmentService.assign(Lead) Interface definiert
  - [ ] Geo → Segment → Workload Logik implementiert
  - [ ] Activity LEAD_ASSIGNED wird persistiert mit Metadata:
    - `method`: "GEO_WORKLOAD" | "MANUAL" | "WEB_INTAKE_AUTO"
    - `metadata.previousOwner`: UUID (falls Re-Assign, sonst NULL)
    - `metadata.assignmentReason`: String (z.B. "Geo-Zone München, Workload 3/10")
    - `metadata.geoZone`: String (z.B. "DE-BY-München")
    - `metadata.workloadScore`: Integer (aktueller Workload 0-100)
- [ ] **Tests:**
  - [ ] Unit Tests ≥80% Coverage (Mock-first)
  - [ ] Gezielte IT für V257 Trigger (nur Progress-Activities)
  - [ ] Gezielte IT für Dedupe-Repo-Queries
- [ ] **Dokumentation:**
  - [ ] CONTRACT_MAPPING.md aktualisiert
  - [ ] SUMMARY.md aktualisiert
  - [ ] API-Docs POST /api/leads (RFC7807 409)
  - [ ] FRONTEND_ACCESSIBILITY.md aktualisiert

### Feature-Flags & Rollout:
- [ ] **VITE_FEATURE_LEADGEN** bleibt aktiv (bereits vorhanden)
- [ ] **VITE_FEATURE_WEB_INTAKE** vorbereitet (vorerst OFF, Sprint 2.1.6)

### Optional (Nice-to-Have):
- [ ] **Live-Dedupe-Hints (UX-Optimierung):**
  - [ ] Throttle 300ms nach Eingabe in Email/Phone/Company/PostalCode
  - [ ] GET /api/leads/check-duplicate Endpoint
  - [ ] Inline-Feedback im LeadWizard (orange=SOFT, rot=HARD)
  - [ ] Rate-Limit: 10 req/min pro User
  - [ ] Cache 60s (gleiche Query → kein API-Call)

## Observability & Metriken

### Pre-Claim Metriken:
```java
// Micrometer Gauges
@Gauge(name = "leads_preclaim_open_total", description = "Anzahl Pre-Claim Leads (registered_at IS NULL)")
public long getPreClaimOpenCount() { ... }

@Gauge(name = "leads_preclaim_expiring_3d", description = "Pre-Claim Leads ablaufend ≤3 Tage")
public long getPreClaimExpiring3Days() { ... }

// Micrometer Counters
@Counter(name = "leads_preclaim_expired_total", description = "Abgelaufene Pre-Claim Leads")
public void incrementPreClaimExpired() { ... }

// Micrometer Histograms
@Timer(name = "lead_first_contact_to_protection_ms", description = "Zeit von Erstellung bis Schutzbeginn")
public void recordFirstContactToProtection(Duration duration) { ... }
```

### Dedupe Metriken:
```java
@Counter(name = "dedupe_block_total", description = "Harte Kollisionen geblockt")
public void incrementDedupeBlock() { ... }

@Counter(name = "dedupe_warn_total", description = "Weiche Kollisionen gewarnt")
public void incrementDedupeWarn() { ... }

@Counter(name = "duplicate_overrides_total", description = "Manager-Overrides")
public void incrementDuplicateOverride() { ... }
```

### Assignment Metriken:
```java
@Timer(name = "lead_assignment_duration_ms", description = "Dauer Lead-Zuweisung")
public void recordAssignmentDuration(Duration duration) { ... }

@Counter(name = "lead_assignments_total", description = "Anzahl Zuweisungen", tags = ["method"])
public void incrementAssignment(String method) { ... }
```

### Dashboard-Queries (Prometheus/Grafana):
```promql
# Pre-Claim Leads ablaufend in 3 Tagen
leads_preclaim_expiring_3d

# Dedupe Block-Rate
rate(dedupe_block_total[5m])

# Manager-Override-Rate
rate(duplicate_overrides_total[5m]) / rate(dedupe_block_total[5m])

# P95 Assignment-Dauer
histogram_quantile(0.95, rate(lead_assignment_duration_ms_bucket[5m]))
```

## Risiken & Mitigation

- **Datenmigration bestehender Leads**: Dedizierte Backend-API (Modul 08, Sprint 2.1.6), keine manuellen Datumsfelder im Frontend
- **DSGVO Consent-Verweigerung**: Fallback auf "Legitimate Interest" nur bei existierenden Geschäftsbeziehungen, sonst KEIN Lead-Create
- **Performance bei Fuzzy-Matching**: pg_trgm Indizes, Pagination, verschoben auf 2.1.6
- **Stop-the-Clock Missbrauch**: Audit-Log, Manager-Approval, RBAC-Policy (UI erst in 2.1.6)
- **Activity-Type Progress-Mapping unklar**: Verbindliche Liste in TRIGGER dokumentiert, Backend-Enum mit Default-Werten

## Abhängigkeiten

- Sprint 2.1.4 muss merged sein (Normalisierung)
- Contract-Dokument für genaue Regeln
- UX-Design für Progressive Forms

## Next Sprint Preview

Sprint 2.1.6: Lead Transfer & Team Management – Übergabe zwischen Partnern, Quotenregelung