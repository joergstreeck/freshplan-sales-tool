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
> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **Modul-Start (_index.md) → Status prüfen**
> 3. **Backend:** V249 Migration für Lead Protection
> 4. **Frontend:** Progressive Profiling UI (3 Stufen)
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
-- V249: Lead Protection Tables
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

## Definition of Done (Sprint)

- [ ] **V249 Migration deployed & tested**
- [ ] **Progressive UI (3 Stufen) implementiert**
- [ ] **Activity Tracking funktioniert**
- [ ] **60-Tage-Warning automatisiert**
- [ ] **Protection-Endpoints implementiert**
- [ ] **Retention-Jobs konfiguriert**
- [ ] **Compliance-Doku vollständig**
- [ ] **Tests: Unit + Integration grün**
- [ ] **Dokumentation: Contract-Mapping**

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