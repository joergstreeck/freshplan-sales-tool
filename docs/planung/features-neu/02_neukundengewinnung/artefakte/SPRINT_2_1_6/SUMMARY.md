---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "planned"
sprint: "2.1.6"
owner: "team/leads-backend"
updated: "2025-10-03"
---

# Sprint 2.1.6 – Artefakte Summary

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.6

## Übersicht

Sprint 2.1.6 implementiert **Lead-Transfer-Workflow**, **Bestandsleads-Migration (Modul 08)**, **Lead → Kunde Convert Flow**, **Stop-the-Clock UI**, und **Lead-Management UI Phase 2** (ADR-006 Lead-spezifische Erweiterungen).

## Sprint-Ziel

Implementierung von Team-basierter Lead-Sichtbarkeit mit Row-Level-Security, Lead-Transfer-Workflow zwischen Partnern, Bestandsleads-Migration und Fuzzy-Matching für intelligente Duplikat-Erkennung.

## Geplante Features (aus Sprint 2.1.5 verschoben + NEU)

### 1. Bestandsleads-Migrations-API (Modul 08 - NEU)
- POST /api/admin/migration/leads/import (Admin-only, Dry-Run Mode PFLICHT)
- Batch-Import: max. 1000 Leads/Batch
- Historische Datumsfelder korrekt übernehmen (registeredAt, activities)
- `countsAsProgress` explizit setzen (NICHT automatisch berechnen!)
- Duplikaten-Check + Warning-Report (gleiche Logik wie manuelle Erfassung)
- Audit-Log: `leads_batch_imported` (mit User, Timestamp, Lead-Count)
- Re-Import-Fähigkeit bei Fehlern (Idempotenz via Request-Hash)
- **Migration-Ausnahme:** Bestandsleads → sofortiger Schutz (registered_at != NULL)
- Validierung: registered_at nicht in Zukunft, Activity-Dates chronologisch
- Response: Erfolg/Fehler pro Lead + Gesamt-Statistik

### 2. Lead-Transfer Workflow
- V259: lead_transfers Tabelle (V258 = Activity-Type Constraint Fix in 2.1.5)
- Transfer-Request mit Begründung
- Genehmigungsprozess (Manager/Admin)
- 48h SLA für Entscheidung
- Automatische Eskalation an Admin
- Audit-Trail für alle Transfers
- Email-Benachrichtigungen

### 3. Backdating Endpoint
- PUT /api/leads/{id}/registered-at (Admin/Manager)
- Validierung: nicht in Zukunft; Reason Pflicht
- Audit: `lead_registered_at_backdated`
- Felder bereits vorhanden: `registered_at_override_reason`, etc.
- Recalc Protection-/Activity-Fristen

### 4. Automated Jobs
- Nightly Job: Progress Warning Check (Tag 53)
- Nightly Job: Protection Expiry (Tag 70)
- Nightly Job: Pseudonymisierung (60 Tage ohne Progress)
- Email-Benachrichtigungen
- Dashboard-Alerts

### 5. Fuzzy-Matching & Review (verschoben aus 2.1.5)
- Vollständiger Scoring-Algorithmus (Email, Phone, Company, Address)
- Schwellwerte konfigurierbar (hard/soft duplicates)
- 202 Response mit Kandidaten-Liste
- DuplicateReviewModal.vue
- Review-UI: Merge/Reject/Create-New
- Merge-Historie mit Undo-Möglichkeit

### 6. Stop-the-Clock UI (verschoben aus 2.1.5)
- StopTheClockDialog UI (Manager-only)
- Approval-Workflow
- Max. Pausendauer-Validierung
- Audit-Log Integration

### 7. Lead → Kunde Convert Flow (NEU)
- Automatische Übernahme bei QUALIFIED → CONVERTED
- Activity-Migration (Lead-Activities → Customer-Activities)
- Protection-Status Übernahme
- Audit-Trail

### 8. Lead-Management UI Phase 2 (ADR-006) - NEU
**📋 Siehe:** [ADR-006-lead-management-hybrid-architecture.md](../../shared/adr/ADR-006-lead-management-hybrid-architecture.md)

**Architektur-Entscheidung:**
- ✅ Sprint 2.1.5 (Phase 1): Leads via CustomersPageV2 (Customer-Status-Reuse)
- 🔄 Sprint 2.1.6 (Phase 2): Lead-spezifische Erweiterungen

**Phase 2 Features:**
1. **Lead-Scoring-System (0-100 Punkte)**
   - Umsatzpotenzial (25%)
   - Engagement (25%)
   - Fit (25%)
   - Dringlichkeit (25%)
   - UI: LeadScoreIndicator mit Progress Bar
   - Backend: `ALTER TABLE leads ADD COLUMN lead_score INTEGER`

2. **Lead-Status-Workflows**
   - Status-Übergänge: LEAD → PROSPECT → AKTIV
   - UI: LeadStatusWorkflow, LeadQualificationForm
   - Tracking: Lead-Konversions-Metriken

3. **Lead-Activity-Timeline**
   - Activity-Log: Chronologische Interaktions-Historie
   - Activity-Types: EMAIL_SENT, CALL_MADE, MEETING_SCHEDULED, QUOTE_SENT
   - UI: LeadActivityTimeline mit Icons

4. **Lead-Protection aktivieren**
   - Backend: `GET /api/leads?assignedTo={userId}`
   - Index: `CREATE INDEX idx_leads_assigned_to ON leads(assigned_to) WHERE status IN ('LEAD', 'PROSPECT')`
   - Frontend: Quick Filter "Meine Leads" (assignedTo = currentUser)

### 9. Row-Level-Security (RLS) Implementation (OPTIONAL)
- Owner kann eigene Leads sehen (lead_owner_policy)
- Team-Mitglieder sehen Team-Leads (lead_team_policy)
- Admin hat Vollzugriff (lead_admin_policy)
- Transfer-Empfänger sieht pending Transfers
- Session-Context mit user_id und role

## Migrations

### V259: lead_transfers + consent_given_at
```sql
-- lead_transfers Tabelle
CREATE TABLE lead_transfers (
  id BIGSERIAL PRIMARY KEY,
  lead_id BIGINT REFERENCES leads(id),
  from_user_id VARCHAR(50) NOT NULL,
  to_user_id VARCHAR(50) NOT NULL,
  reason TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,  -- pending, approved, rejected
  approved_by VARCHAR(50),
  approved_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- DSGVO Consent-Feld (Web-Intake)
ALTER TABLE leads ADD COLUMN consent_given_at TIMESTAMPTZ NULL;
COMMENT ON COLUMN leads.consent_given_at IS
  'DSGVO Art. 6 Abs. 1 lit. a: Zeitpunkt der Einwilligung (nur bei source=WEB_FORMULAR)';

-- Consent-Text finalisiert: siehe DSGVO_CONSENT_SPECIFICATION.md (Sprint 2.1.5)
-- Frontend: Checkbox UI bereits in LeadWizard.tsx implementiert (Frontend Phase 2)
```

### V260: Activity-Templates (OPTIONAL - Sprint 2.1.7)
```sql
CREATE TABLE activity_templates (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  activity_type VARCHAR(50) NOT NULL,
  summary_template TEXT,
  counts_as_progress BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
```

## Frontend Components

**Lead-Management UI Phase 2 (ADR-006):**
- `LeadScoreIndicator.tsx` - Lead-Score mit Progress Bar (0-100)
- `LeadStatusWorkflow.tsx` - Status-Übergänge (LEAD → PROSPECT → AKTIV)
- `LeadActivityTimeline.tsx` - Activity-Historie
- `LeadQualificationForm.tsx` - Qualifizierungs-Formular
- `hooks/useLeadScore.ts` - Lead-Score-Berechnung
- `hooks/useLeadConversion.ts` - Status-Conversion-Logik

**Lead-Transfer & Stop-the-Clock:**
- `TransferRequestDialog.vue` - Transfer initiieren
- `TransferApprovalList.vue` - Pending Transfers
- `DuplicateReviewModal.vue` - Matching Review
- `StopTheClockDialog.vue` - Stop-the-Clock (Manager-only)
- `ExtensionRequestDialog.vue` - Schutzfrist-Verlängerung
- `MergeHistoryTimeline.vue` - Merge-Historie

## API-Endpoints (NEU)

### Lead-Transfer
```json
POST /leads/{id}/transfer
{
  "targetUserId": "UUID",
  "reason": "string",
  "notifyTarget": true
}

PUT /leads/{id}/transfer/{requestId}
{
  "decision": "approve|reject",
  "comment": "string"
}

GET /leads/{id}/transfer/history
```

### Bestandsleads-Migration
```json
POST /api/admin/migration/leads/import
{
  "dryRun": true,
  "leads": [
    {
      "companyName": "Gasthaus Müller",
      "city": "München",
      "contact": {...},
      "registeredAt": "2024-08-15T10:30:00Z",
      "activities": [...]
    }
  ]
}
```

### Backdating
```json
PUT /api/leads/{id}/registered-at
{
  "registeredAt": "2024-06-03T10:15:00Z",
  "reason": "Import Altbestand",
  "evidenceUrl": "https://..."
}
```

## Definition of Done

### Backend:
- [ ] V259 Migration deployed & tested
- [ ] lead_transfers CRUD-API
- [ ] Bestandsleads-Migration-API (Dry-Run + Real Import)
- [ ] Backdating Endpoint
- [ ] Nightly Jobs (Warning/Expiry/Pseudonymisierung)
- [ ] Fuzzy-Matching Service
- [ ] Unit Tests ≥80% Coverage
- [ ] Integration Tests für alle API-Endpoints

### Frontend:
- [ ] Lead-Management UI Phase 2 (ADR-006):
  - [ ] LeadScoreIndicator.tsx
  - [ ] LeadStatusWorkflow.tsx
  - [ ] LeadActivityTimeline.tsx
  - [ ] useLeadScore.ts Hook
  - [ ] useLeadConversion.ts Hook
- [ ] Lead-Transfer & Stop-the-Clock:
  - [ ] TransferRequestDialog.vue
  - [ ] TransferApprovalList.vue
  - [ ] StopTheClockDialog.vue (Manager-only)
  - [ ] ExtensionRequestDialog.vue
  - [ ] DuplicateReviewModal.vue (Merge/Reject/Create-New)
- [ ] Integration Tests ≥80% Coverage

### Dokumentation:
- [ ] API-Docs aktualisiert (Transfer, Migration, Backdating)
- [ ] SUMMARY.md finalisiert
- [ ] CONTRACT_MAPPING.md aktualisiert (§2(8)(c), §2(8)(e), §2(8)(i))
- [ ] RELEASE_NOTES.md

## Risiken & Mitigationen

| Risiko | Mitigation | Status |
|--------|------------|--------|
| RLS Performance | Index-Optimierung auf owner_user_id, owner_team_id | PLANNED |
| Policy-Konflikte | Umfassende Test-Suite für alle Kombinationen | PLANNED |
| Transfer-Deadlocks | Pessimistic Locking mit Timeout | PLANNED |
| False Positives | Matching-Schwellen iterativ tunen | PLANNED |

## Abhängigkeiten

- Sprint 2.1.4 (Normalisierung) muss abgeschlossen sein ✅
- Sprint 2.1.5 (Protection) sollte parallel laufen können ✅
- PostgreSQL 14+ für RLS Features
- Team-Tabellen müssen existieren (falls RLS implementiert wird)

## Monitoring & KPIs

- **Transfer Approval Time:** Ziel <24h average
- **RLS Query Performance:** P95 <50ms overhead
- **Matching Accuracy:** >95% precision, >90% recall
- **Migration Success Rate:** >99% bei Dry-Run validation

## Vertragliche Basis

Direkte Umsetzung aus Handelsvertretervertrag:
- **§2(8)(c) Erinnerung + 10 Tage Nachfrist**: Nightly Job für Warning/Expiry
- **§2(8)(e) Verlängerung auf Antrag**: ExtensionRequestDialog
- **§2(8)(i) Löschung/Pseudonymisierung**: Nightly Job + Endpoint

## Next Sprint Preview

Sprint 2.1.7: Lead-Scoring Algorithmus, Activity-Templates, Mobile-First UI, Offline-Fähigkeit, QR-Code-Scanner

## Links

- [TRIGGER_SPRINT_2_1_6](../../../../TRIGGER_SPRINT_2_1_6.md)
- [SPRINT_MAP](../../SPRINT_MAP.md)
- [ADR-003-rls-leads-row-level-security](../../shared/adr/ADR-003-rls-leads-row-level-security.md)
