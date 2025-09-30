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

### 1. Row-Level-Security (RLS) Implementation
**Akzeptanzkriterien:**
- Owner kann eigene Leads sehen (lead_owner_policy)
- Team-Mitglieder sehen Team-Leads (lead_team_policy)
- Admin hat Vollzugriff (lead_admin_policy)
- Transfer-Empfänger sieht pending Transfers
- Session-Context mit user_id und role
- **RLS-aware Tests: Backdating nur für MANAGER/ADMIN sichtbar & zulässig**

### 2. Lead-Transfer Workflow
**Akzeptanzkriterien:**
- Transfer-Request mit Begründung
- Genehmigungsprozess (Manager/Admin)
- 48h SLA für Entscheidung
- Automatische Eskalation an Admin
- Audit-Trail für alle Transfers
- Email-Benachrichtigungen

### 3. Fuzzy-Matching & Review (aus 2.1.5)
**Akzeptanzkriterien:**
- Scoring-Algorithmus (Email, Phone, Company, Address)
- Schwellwerte konfigurierbar (hard/soft duplicates)
- 202 Response mit Kandidaten-Liste
- Review-UI: Merge/Reject/Create-New
- Merge-Historie mit Undo-Möglichkeit

### 4. Team Management
**Akzeptanzkriterien:**
- Team CRUD Operations
- Team-Member Assignment
- Quotenregelung für Teams
- Team-Dashboard mit Metriken
- Territory-Zuordnung (DE/CH)

## Technische Details

### RLS Policies (PostgreSQL):
```sql
-- Migration V251: RLS Setup
ALTER TABLE leads ADD COLUMN
  owner_user_id UUID,
  owner_team_id UUID,
  visibility VARCHAR(20) DEFAULT 'private';

ALTER TABLE leads ENABLE ROW LEVEL SECURITY;

CREATE POLICY lead_owner_policy ON leads
  FOR ALL TO application_user
  USING (owner_user_id = current_setting('app.current_user_id')::UUID);

CREATE POLICY lead_team_policy ON leads
  FOR SELECT TO application_user
  USING (visibility = 'team' AND owner_team_id IN (
    SELECT team_id FROM team_members
    WHERE user_id = current_setting('app.current_user_id')::UUID
  ));
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