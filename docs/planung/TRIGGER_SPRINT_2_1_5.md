---
sprint_id: "2.1.5"
title: "Lead Protection & Progressive Profiling (B2B)"
doc_type: "konzept"
status: "partial_complete"
owner: "team/leads-backend"
date_start: "2025-10-05"
date_end: "2025-10-04"
completion_date: "2025-10-04"
gaps: ["zwei_felder_loesung", "pre_claim_badge", "backend_dto"]
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

## ⚠️ Sprint-Status: PARTIAL COMPLETE (04.10.2025)

**✅ ERLEDIGT:**
- ✅ Backend V255-V257 Migrations (Progress Tracking, Stage, Protection Functions)
- ✅ LeadProtectionService mit 24 Unit Tests
- ✅ LeadWizard (3 Stages: Vormerkung, Registrierung, Qualifizierung)
- ✅ Context-Prop Architecture (CustomersPageV2 mit context='leads')
- ✅ Migration V259 (Removed ui_leads_company_city unique constraint)
- ✅ Dokumentation vollständig (BUSINESS_LOGIC, PRE_CLAIM_LOGIC, FRONTEND_DELTA, SUMMARY)
- ✅ Zwei-Felder-Lösung **dokumentiert** (Notizen + Erstkontakt getrennt)

**❌ GAPS (Code TODO, Doku COMPLETE):**
1. **Zwei-Felder-Lösung** - Separates Notizen-Feld + Checkbox-gesteuerter Erstkontakt-Block (FRONTEND_DELTA.md:52-88)
2. **Pre-Claim Badge** - "⏳ Pre-Claim (X Tage)" Badge in CustomerTable (PRE_CLAIM_LOGIC.md:420-432)
3. **Backend DTO** - registeredAt, protectedUntil, progressDeadline Felder (PRE_CLAIM_LOGIC.md:306-334)

**🔄 VERSCHOBEN AUF SPRINT 2.1.6:**
- Code-Implementierung der 3 Gaps (~4h Aufwand)
- Quick-Action "Erstkontakt nachtragen" (~2h Aufwand)
- Pre-Claim Filter in IntelligentFilterBar (~1h Aufwand)

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
-- V249 wurde zu 10012: Lead Protection Tables
CREATE TABLE lead_protection (
  lead_id, registered_at, protection_until,
  last_progress_at, status, stop_the_clock_reason
);

CREATE TABLE lead_activities (
  lead_id, activity_type, activity_date,
  counts_as_progress, performed_by
);
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
- `LeadWizard.vue` - Progressive form (3 Stufen)
- `LeadProtectionBadge.vue` - Status indicator
- `ActivityTimeline.vue` - Progress tracking
- `ExtensionRequestDialog.vue` - Verlängerungsantrag
- `StopTheClockDialog.vue` - Pausierung mit Grund

## PR-Strategie (Backend/Frontend Split)

**Phase 1: Backend (PR #124)** - Branch: `feature/mod02-sprint-2.1.5-lead-protection`
- ✅ Migrations V255-V257 (Progress Tracking + Stage + Functions/Trigger)
- ✅ Entities: Lead.java (+3), LeadActivity.java (+6)
- ✅ Service: LeadProtectionService (Stage-Validierung, Progress-Deadlines)
- ✅ Tests: 24 Unit Tests (0.845s, Pure Mockito, 100% passed)
- ✅ Dokumentation: ADR-004, DELTA_LOG_2_1_5, CONTRACT_MAPPING, TEST_PLAN, SUMMARY
- **Status:** READY FOR PR

**Phase 2: Frontend (PR #125)** - Branch: `feature/mod02-sprint-2.1.5-frontend-progressive-profiling`
- LeadWizard.vue (3-Stufen Progressive Profiling UI)
- LeadProtectionBadge.vue (Status-Indicator)
- ActivityTimeline.vue (Progress Tracking Display)
- API-Integration: Enhanced POST /api/leads mit Stage-Validierung
- Tests: Integration Tests für Progressive Profiling Flow
- **Status:** PENDING

**Logo-Status (Bereits optimiert ✅):**
- `/cockpit` Route verwendet MainLayoutV2 + HeaderV2 (Logo.tsx 19 KB) ✅
- SalesCockpitV2 hat KEINEN eigenen CockpitHeader ✅
- Alle aktiven Routen verwenden HeaderV2 mit optimiertem Logo ✅

**Verschoben auf Sprint 2.1.6:**
- V258 lead_transfers Tabelle
- PUT /api/leads/{id}/registered-at (Backdating Endpoint)
- Nightly Jobs (Warning/Expiry/Pseudonymisierung)
- Vollständiger Fuzzy-Matching Algorithmus + DuplicateReviewModal.vue

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
- [ ] **Progressive UI (3 Stufen) implementiert**
- [ ] **Activity Tracking UI funktioniert**
- [ ] **Protection Status Badge implementiert**
- [ ] **API-Integration mit Stage-Validierung**
- [ ] **Tests: Integration Tests grün**

## Risiken & Mitigation

- **Datenmigration bestehender Leads**: Batch-Job für Protection-Records
- **Performance bei Fuzzy-Matching**: pg_trgm Indizes, Pagination
- **Stop-the-Clock Missbrauch**: Audit-Log, Manager-Approval

## Abhängigkeiten

- Sprint 2.1.4 muss merged sein (Normalisierung)
- Contract-Dokument für genaue Regeln
- UX-Design für Progressive Forms

## Next Sprint Preview

Sprint 2.1.6: Lead Transfer & Team Management – Übergabe zwischen Partnern, Quotenregelung