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

Implementierung von Bestandsleads-Migrations-API (Modul 08), Lead → Kunde Convert Flow, Stop-the-Clock UI und optionalen Features aus Sprint 2.1.5 (Pre-Claim UI-Erweiterungen, Lead Detail-Seite).

**Scope-Änderung:** RLS Team Management + Lead-Transfer verschoben auf Sprint 2.1.7 (Team-Features sind komplex und benötigen eigenen Sprint).

## User Stories

### 0. Lead Stage Enum Refactoring (Issue #125) - **PR #131 PRIORITY**
**Begründung:** Type Safety für Lead Stage - Verhindert Magic Numbers, verbessert Code-Qualität

**Akzeptanzkriterien:**
- ✅ Enum LeadStage erstellt mit 3 Values (VORMERKUNG=0, REGISTRIERUNG=1, QUALIFIZIERT=2)
- ✅ Lead.stage Typ geändert: `Short` → `LeadStage` mit `@Enumerated(EnumType.ORDINAL)`
- ✅ LeadProtectionService.canTransitionStage() nutzt Enum-Methoden (LeadStage.canTransitionTo())
- ✅ Alle Tests grün (24 Unit Tests + Integration Tests)
- ✅ JSON Serialization funktioniert (0/1/2 in API, VORMERKUNG/REGISTRIERUNG/QUALIFIZIERT in UI)
- ✅ KEINE DB-Migration erforderlich (ORDINAL nutzt 0,1,2)

**Aufwand:** 2-3h (Low Complexity - reine Code-Refactoring, keine DB-Änderungen)

**Referenzen:**
- Issue: https://github.com/joergstreeck/freshplan-sales-tool/issues/125
- Code Review: Gemini Comment (Medium Priority)

### 1. Lead-Transfer Workflow (verschoben aus 2.1.5)
**Akzeptanzkriterien:**
- V258: lead_transfers Tabelle
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

### 7. Frontend UI Improvements (verschoben aus 2.1.5)
**Akzeptanzkriterien:**
- **Lead Status-Labels:** REGISTERED → "Vormerkung", ACTIVE → "Aktiv", QUALIFIED → "Qualifiziert", CONVERTED → "Konvertiert", LOST → "Verloren"
- **Lead Action-Buttons:** Löschen/Bearbeiten Buttons in CustomerTable (context-aware)
- **Lead Detail-Seite:** Route `/leads/:id` mit Lead-Details für Navigation bei Klick
- **Context-aware CustomerTable:** Status-Rendering und Actions basierend auf `context` Prop

### 8. MUI Dialog Accessibility Fix (aria-hidden Focus Management)
**Begründung:** MUI Dialog blockiert aria-hidden auf Elementen mit Fokus - WCAG 2.1 Level A Verletzung

**Problem:**
```
Blocked aria-hidden on an element because its descendant retained focus.
The element is displayed on screen with 'display:block' or equivalent styles.
```

**Akzeptanzkriterien:**
- MUI Dialog Focus-Management korrekt implementiert (disableEnforceFocus=false beibehalten)
- aria-hidden Warning in Browser Console eliminiert
- WCAG 2.1 Level A Compliance für Dialog-Focus-Management
- Keine Regression bei Keyboard-Navigation (Tab, Escape, Enter)
- FocusTrap funktioniert weiterhin korrekt

**Betroffene Komponenten:**
- LeadWizard.tsx (MUI Dialog mit Multi-Step-Form)
- Alle anderen Dialogs mit Focus-Management (CustomerEditDialog, StopTheClockDialog, etc.)

**Technische Lösung:**
- MUI `disableEnforceFocus` Option prüfen (nur bei Bedarf aktivieren)
- `disableRestoreFocus` für spezifische Dialogs konfigurieren
- `aria-hidden` korrekt auf Dialog-Overlay und Parent-Elementen setzen
- Focus-Management mit `useRef` + `useEffect` für Custom-Steuerung

**Referenzen:**
- [MUI Dialog API](https://mui.com/material-ui/api/dialog/)
- [WCAG 2.1 Focus Management](https://www.w3.org/WAI/WCAG21/Understanding/focus-visible.html)
- [React Focus Management Best Practices](https://react-spectrum.adobe.com/react-aria/FocusScope.html)

**Aufwand:** 1-2h (Low Complexity - MUI Props-Konfiguration + Testing)

## Technische Details

### Lead Transfers (aus 2.1.5):

**🚨 Migration-Nummer:** Verwende `get-next-migration.sh` statt feste V-Nummern!

```sql
-- Migration: siehe get-next-migration.sh
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

### 2. Lead → Kunde Convert Flow (NEU aus 2.1.5)
**Akzeptanzkriterien:**
- Automatische Übernahme bei Status QUALIFIED → CONVERTED
- Alle Lead-Daten übernehmen (ZERO Doppeleingabe)
- Lead-ID Verknüpfung in customer.original_lead_id
- Historie vollständig erhalten (Activities, Protection-Daten)
- Validation: nur QUALIFIED Leads können konvertiert werden

**API-Spec:**
```json
POST /api/leads/{id}/convert
Authorization: Bearer <token>

Response 200:
{
  "customerId": "uuid-customer-123",
  "leadId": "uuid-lead-456",
  "message": "Lead successfully converted to customer"
}
```

### 3. Stop-the-Clock UI (NEU aus 2.1.5)
**Akzeptanzkriterien:**
- StopTheClockDialog Component (Manager + Admin only)
- Pause/Resume Buttons in LeadProtectionBadge
- Grund-Auswahl: "FreshFoodz Verzögerung", "Kunde im Urlaub", "Andere"
- Audit-Log für jeden Stop/Resume Event
- Maximale Pausendauer konfigurierbar (Default: 30 Tage)

**Frontend Components:**
- `StopTheClockDialog.tsx` - Pause/Resume mit Grund
- `LeadProtectionBadge.tsx` - Pause/Resume Buttons ergänzen

### 4. Pre-Claim UI-Erweiterungen (OPTIONAL aus 2.1.5)
**Akzeptanzkriterien:**
- Quick-Action "Erstkontakt nachtragen" Button für Pre-Claim Leads
- Pre-Claim Filter in IntelligentFilterBar
- Lead Status-Labels Frontend (REGISTERED → "Vormerkung", ACTIVE → "Aktiv")
- Lead Action-Buttons (Löschen/Bearbeiten) in CustomerTable

**Frontend Components:**
- `AddFirstContactDialog.tsx` - Quick-Action für Erstkontakt-Nacherfassung
- `IntelligentFilterBar.tsx` - Pre-Claim Filter ergänzen

### 5. Lead Detail-Seite (OPTIONAL aus 2.1.5)
**Akzeptanzkriterien:**
- Lead Detail-Route `/leads/:id`
- Lead-Informationen anzeigen (Company, Contact, Activities, Protection)
- Navigation von CustomerTable Lead-Klick
- Edit-Modus für Lead-Daten
- Activity-Timeline Integration

**Frontend Components:**
- `LeadDetailPage.tsx` - Detail-Ansicht für Leads
- `LeadEditDialog.tsx` - Edit-Modus

### Automated Jobs (Backend-only, UI in 2.1.7):
```java
@Scheduled(cron = "0 0 1 * * ?")  // 1 AM daily
void checkProgressWarnings() {
  // Find leads: progress_deadline < NOW() + 7 days
  // Set: progress_warning_sent_at
  // Send: Email notification
}
```

### 6. Lead-Transfer & RLS (Verschoben auf Sprint 2.1.7)
**Begründung:** Team-Management und RLS sind komplex und benötigen eigenen Sprint.

**Verschobene Features:**
- Transfer API (POST /leads/{id}/transfer, Genehmigungsprozess)
- Row-Level-Security Policies (owner_policy, team_policy, admin_policy)
- Team Management CRUD
- Fuzzy-Matching & Duplicate Review
- lead_transfers Tabelle (Migration: siehe `get-next-migration.sh`)

**Cross-Module Dependency:**
- **Modul 00 Sicherheit:** ADR-003 RLS Design → Sprint 2.1.7
- **Modul 00 Betrieb:** RLS Performance Monitoring → Sprint 2.1.7

## Definition of Done (Sprint 2.1.6)

**Backend:**
- [ ] **Bestandsleads-Migrations-API funktionsfähig** (Dry-Run + Real-Import)
- [ ] **Lead → Kunde Convert Flow End-to-End** (POST /api/leads/{id}/convert)
- [ ] **Automated Jobs implementiert** (Progress Warning, Expiry, Pseudonymisierung)
- [ ] **Backend Tests ≥80% Coverage**

**Frontend:**
- [ ] **Stop-the-Clock UI funktional** (StopTheClockDialog, RBAC Manager/Admin)
- [ ] **Pre-Claim UI-Erweiterungen** (Quick-Action, Filter - OPTIONAL)
- [ ] **Lead Detail-Seite** (Lead-Route `/leads/:id` - OPTIONAL)
- [ ] **Frontend Tests ≥75% Coverage**

**Dokumentation:**
- [ ] **Migration-API Runbook** (Modul 08, Betrieb)
- [ ] **Convert-Flow dokumentiert** (BUSINESS_LOGIC)
- [ ] **Stop-the-Clock RBAC Policy** (Modul 00 Sicherheit)

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