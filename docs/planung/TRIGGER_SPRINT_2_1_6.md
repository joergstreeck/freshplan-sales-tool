---
sprint_id: "2.1.6"
title: "Lead Transfer & Team Management"
doc_type: "konzept"
status: "planned"
owner: "team/leads-backend"
date_start: "2025-10-12"
date_end: "2025-10-18"
modules: ["02_neukundengewinnung"]
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/shared/adr/ADR-003-rls-leads-row-level-security.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/BUSINESS_LOGIC_LEAD_ERFASSUNG.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/SUMMARY.md"
pr_refs: []
updated: "2025-09-28"
---

# Sprint 2.1.6 – Lead Transfer & Team Management

**📍 Navigation:** Home → Planung → Sprint 2.1.6

> **⚠️ TEST-STRATEGIE BEACHTEN!**
> Tests MÜSSEN Mocks verwenden, NICHT @QuarkusTest mit echter DB!
> Siehe: [`backend/TEST_MIGRATION_PLAN.md`](features-neu/02_neukundengewinnung/backend/TEST_MIGRATION_PLAN.md)
>
> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **ADR-003 RLS Design prüfen** → `shared/adr/ADR-003-rls-leads-row-level-security.md`
> 3. **Backend:** Row-Level-Security Policies implementieren
> 4. **Backend:** Transfer-Flow mit Genehmigung
> 5. **Backend:** Fuzzy-Matching & Scoring (verschoben aus 2.1.5)
> 6. **Frontend:** Transfer-UI & Review-Flow

## Sprint-Ziel

Implementierung von Team-basierter Lead-Sichtbarkeit mit Row-Level-Security, Lead-Transfer-Workflow zwischen Partnern und Fuzzy-Matching für intelligente Duplikat-Erkennung.

## User Stories

### 1. Bestandsleads-Migrations-API (Modul 08 - NEU)
**Akzeptanzkriterien:**
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

**API-Spec:**
```json
POST /api/admin/migration/leads/import
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "dryRun": true,  // PFLICHT für ersten Durchlauf
  "leads": [
    {
      "companyName": "Gasthaus Müller",
      "city": "München",
      "postalCode": "80331",
      "contact": {
        "firstName": "Hans",
        "lastName": "Müller",
        "email": "mueller@gasthaus.de"
      },
      "source": "MIGRATION",
      "ownerUserId": "uuid-partner-123",
      "registeredAt": "2024-08-15T10:30:00Z",  // Historisches Datum
      "activities": [
        {
          "type": "QUALIFIED_CALL",
          "summary": "Erstkontakt Produktinteresse",
          "performedAt": "2024-08-16T14:00:00Z",
          "countsAsProgress": true  // EXPLIZIT!
        }
      ]
    }
  ]
}

Response 200 (Dry-Run):
{
  "dryRun": true,
  "totalLeads": 1,
  "validLeads": 0,
  "invalidLeads": 1,
  "duplicates": 0,
  "errors": [
    {
      "leadIndex": 0,
      "companyName": "Gasthaus Müller",
      "error": "Duplicate detected: email mueller@gasthaus.de exists (Lead-ID: 12345)"
    }
  ]
}

Response 201 (Real Import):
{
  "dryRun": false,
  "totalLeads": 1,
  "importedLeads": 1,
  "skippedLeads": 0,
  "auditLogId": "uuid-audit-789"
}
```

### 2. Lead-Transfer Workflow (verschoben aus 2.1.5)
**Akzeptanzkriterien:**
- V259: lead_transfers Tabelle (V258 = Activity-Type Constraint Fix)
- Transfer-Request mit Begründung
- Genehmigungsprozess (Manager/Admin)
- 48h SLA für Entscheidung
- Automatische Eskalation an Admin
- Audit-Trail für alle Transfers
- Email-Benachrichtigungen

### 2. Backdating Endpoint (verschoben aus 2.1.5)
**Akzeptanzkriterien:**
- PUT /api/leads/{id}/registered-at (Admin/Manager)
- Validierung: nicht in Zukunft; Reason Pflicht
- Audit: `lead_registered_at_backdated`
- Felder bereits vorhanden: `registered_at_override_reason`, etc.
- Recalc Protection-/Activity-Fristen

### 3. Automated Jobs (verschoben aus 2.1.5)
**Akzeptanzkriterien:**
- Nightly Job: Progress Warning Check (Tag 53)
- Nightly Job: Protection Expiry (Tag 70)
- Nightly Job: Pseudonymisierung (60 Tage ohne Progress)
- Email-Benachrichtigungen
- Dashboard-Alerts

### 4. Fuzzy-Matching & Review (verschoben aus 2.1.5)
**Akzeptanzkriterien:**
- Vollständiger Scoring-Algorithmus (Email, Phone, Company, Address)
- Schwellwerte konfigurierbar (hard/soft duplicates)
- 202 Response mit Kandidaten-Liste
- DuplicateReviewModal.vue
- Review-UI: Merge/Reject/Create-New
- Merge-Historie mit Undo-Möglichkeit

### 5. Row-Level-Security (RLS) Implementation (OPTIONAL)
**Akzeptanzkriterien:**
- Owner kann eigene Leads sehen (lead_owner_policy)
- Team-Mitglieder sehen Team-Leads (lead_team_policy)
- Admin hat Vollzugriff (lead_admin_policy)
- Transfer-Empfänger sieht pending Transfers
- Session-Context mit user_id und role

### 6. Team Management (OPTIONAL)
**Akzeptanzkriterien:**
- Team CRUD Operations
- Team-Member Assignment
- Quotenregelung für Teams
- Team-Dashboard mit Metriken
- Territory-Zuordnung (DE/CH)

## Technische Details

### Lead Transfers (aus 2.1.5):
```sql
-- V259: lead_transfers Tabelle (V258 = Activity-Type Constraint Fix in 2.1.5)
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
```

### Backdating (aus 2.1.5):
```java
// PUT /api/leads/{id}/registered-at
@RolesAllowed({"admin", "manager"})
public void updateRegisteredAt(Long id, BackdatingRequest request) {
  // Validate: not in future
  // Update: registered_at + override_reason
  // Recalc: protection_until, progress_deadline
  // Audit: lead_registered_at_backdated
}
```

### Automated Jobs (aus 2.1.5):
```java
@Scheduled(cron = "0 0 1 * * ?")  // 1 AM daily
void checkProgressWarnings() {
  // Find leads: progress_deadline < NOW() + 7 days
  // Set: progress_warning_sent_at
  // Send: Email notification
}
```

### Transfer API:
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

### Matching Configuration:
```yaml
matching:
  thresholds:
    hard_duplicate: 0.95  # 409 Response
    soft_duplicate: 0.75  # 202 Response
    no_match: 0.74        # 201 Response

  weights:
    email_exact: 1.0
    phone_normalized: 0.9
    company_fuzzy: 0.7
    address_distance: 0.5
```

### Frontend Components:
- `TransferRequestDialog.vue` - Transfer initiieren
- `TransferApprovalList.vue` - Pending Transfers
- `DuplicateReviewModal.vue` - Matching Review
- `TeamManagement.vue` - Team CRUD
- `MergeHistoryTimeline.vue` - Merge-Historie

## Definition of Done (Sprint)

- [ ] **RLS Policies aktiv und getestet**
- [ ] **Transfer-Flow End-to-End funktioniert**
- [ ] **Fuzzy-Matching mit konfigurierbaren Schwellen**
- [ ] **Team Management UI fertig**
- [ ] **Performance-Tests für RLS Impact**
- [ ] **Security-Audit für Policies**
- [ ] **ADR-003 als approved markiert**

## Risiken & Mitigation

- **RLS Performance:** Index-Optimierung auf owner_user_id, owner_team_id
- **Policy-Konflikte:** Umfassende Test-Suite für alle Kombinationen
- **Transfer-Deadlocks:** Pessimistic Locking mit Timeout
- **False Positives:** Matching-Schwellen iterativ tunen

## Abhängigkeiten

- Sprint 2.1.4 (Normalisierung) muss abgeschlossen sein
- Sprint 2.1.5 (Protection) sollte parallel laufen können
- PostgreSQL 14+ für RLS Features
- Team-Tabellen müssen existieren

## Test Strategy

```java
@QuarkusTest
@TestTransaction
@WithRLS  // Custom Annotation
class LeadTransferRLSTest {

    @Test
    @AsUser("partner-a")
    void cannotSeeOtherPartnersLeads() {
        // Partner A creates lead
        // Partner B queries: 0 results
    }

    @Test
    @AsUser("team-member")
    void canSeeTeamLeads() {
        // Team lead with visibility='team'
        // Member sees it
    }

    @Test
    @AsAdmin
    void adminSeesEverything() {
        // 10 leads, different owners
        // Admin sees all 10
    }
}
```

## Monitoring & KPIs

- **Transfer Approval Time:** Ziel <24h average
- **RLS Query Performance:** P95 <50ms overhead
- **Matching Accuracy:** >95% precision, >90% recall
- **Team Quota Utilization:** Dashboard-Widget

## Next Sprint Preview

Sprint 2.2: Kundenmanagement - Field-based Customer Architecture mit Contact-Hierarchie