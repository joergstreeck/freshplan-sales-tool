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
pr_refs: []
updated: "2025-09-28"
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

### 2. Progressive Profiling (3 Stufen)
**Stufe 0: Vormerkung (Minimal)**
- Firma + Stadt (Pflicht)
- Branche (Optional)
- Keine personenbezogenen Daten

**Stufe 1: Lead-Registrierung**
- Company Details + Optional Contact
- Source Tracking (manual/partner/marketing)
- Owner Assignment

**Stufe 2: Qualifiziert**
- VAT ID, Expected Volume
- Key Account Flags
- EDL/Chain Affiliation

### 3. Activity Tracking UI
**Akzeptanzkriterien:**
- Activity Log (Call/Email/Meeting/Demo)
- Progress-Indicator (Days since last activity)
- Warning-Badge bei < 7 Tage bis Deadline
- Stop-the-Clock Dialog mit Grund

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

## Technische Details

### Backend Changes:
```sql
-- V255-V257: Inline-First Architecture (ADR-004)
-- Separate lead_protection Table NICHT implementiert (siehe ADR-004)
-- V249 als Materiallager verfügbar für Sprint 2.1.6+ falls benötigt

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

### DSGVO & Compliance (KRITISCH):
**Consent-Management (Pflicht ab Phase 2):**
- **Stage 0**: Keine personenbezogenen Daten → Kein Consent nötig
- **Stage 1**: Consent-Checkbox PFLICHT (nicht vorausgefüllt)
  - Text: "Ich stimme zu, dass meine Kontaktdaten gespeichert werden (Widerruf jederzeit möglich)"
  - Backend: `lead.consent_given_at TIMESTAMPTZ` speichern
  - Validierung: Ohne Consent KEIN Submit möglich
- **Rechtsgrundlage**: Consent (sauberste Lösung für B2B-Neu-Erfassung)
- **Widerrufsrecht**: Link zu Datenschutzrichtlinie, einfacher Widerruf-Prozess

### Activity-Types Progress-Mapping:
**countsAsProgress = true:**
- `QUALIFIED_CALL` - Echtes Gespräch mit Entscheider
- `MEETING` - Physisches Treffen
- `DEMO` - Produktdemonstration
- `ROI_PRESENTATION` - Business-Value-Präsentation
- `SAMPLE_SENT` - Sample-Box versendet

**countsAsProgress = false:**
- `NOTE` - Nur interne Notiz
- `FOLLOW_UP` - Automatisches Follow-up
- `EMAIL` - Zu low-touch
- `CALL` - Nur wenn nicht QUALIFIED_CALL
- `SAMPLE_FEEDBACK` - Passives Feedback-Logging

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
- ⏸️ DSGVO Consent-Checkbox (Stage 1, lead.consent_given_at Feld)
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

**Verschoben auf Sprint 2.1.6:**
- V258 lead_transfers Tabelle (Lead-Transfer zwischen Partnern)
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

## Definition of Done (Sprint)

**Phase 1 (Backend - PR #124):**
- [x] **V255-V257 Migrations deployed & tested**
- [x] **Entity Updates (Lead.java, LeadActivity.java)**
- [x] **Service Extensions (LeadProtectionService)**
- [x] **Unit Tests grün (24 Tests, 100% passed)**
- [x] **Dokumentation: ADR-004, DELTA_LOG, CONTRACT_MAPPING, TEST_PLAN**

**Phase 2 (Frontend - PR #125):**
- [ ] **LeadWizard.tsx (3 Stufen) implementiert (Full-Page Component)**
- [ ] **DSGVO Consent-Checkbox (Stage 1) mit lead.consent_given_at**
- [ ] **LeadProtectionBadge.tsx implementiert (Tooltip/Responsive/ARIA)**
- [ ] **ActivityTimeline.tsx implementiert (countsAsProgress Filter)**
- [ ] **API-Integration mit Stage + Consent-Validierung**
- [x] **Integration Tests grün (MSW-basiert)**
- [x] **FRONTEND_ACCESSIBILITY.md Dokumentation**
- [x] **LeadWizard ist Standard (Feature-Flag entfernt)**

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