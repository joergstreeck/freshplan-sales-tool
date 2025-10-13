---
sprint_id: "2.1.7"
title: "Lead Team Management & Test Infrastructure & Code Quality"
doc_type: "konzept"
status: "planned"
owner: "team/leads-backend"
date_start: "2025-10-19"
date_end: "2025-10-25"
modules: ["02_neukundengewinnung", "00_infrastruktur"]
tracks:
  - track: "Track 1"
    name: "Business Features (verschoben aus 2.1.6)"
    scope: "Lead-Transfer, Fuzzy-Matching, RLS, Team Management"
  - track: "Track 2"
    name: "Test Infrastructure (STRATEGISCH)"
    scope: "CRM Szenario-Builder, Faker-Integration, TestDataFactories"
  - track: "Track 3"
    name: "Code Quality (aus PR #133 Review)"
    scope: "Issue #135 (Name Parsing), EnumResource Refactoring"
    issues: ["#135"]
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/shared/adr/ADR-003-rls-leads-row-level-security.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "planung/grundlagen/testing_guide.md"
pr_refs: ["#133", "#134", "#135"]
updated: "2025-10-08"
prerequisites:
  - "Sprint 2.1.6 Phase 4 COMPLETE (PR #135 - Lead Intelligence Tests + LeadScoringService)"
---

# Sprint 2.1.7 – Lead Team Management & Test Infrastructure Overhaul

**📍 Navigation:** Home → Planung → Sprint 2.1.7

> **🎯 SPRINT-FOKUS: DREI PARALLELE TRACKS**
>
> **Track 1 - Business Features (verschoben aus 2.1.6):**
> - Lead-Transfer Workflow (Team-basiert)
> - Row-Level-Security (RLS) + Team Management
> - Fuzzy-Matching & Duplicate Review
>
> **Track 2 - Test Infrastructure (NEU - STRATEGISCH!):**
> - Professionelle Testdaten-Strategie für komplexes CRM
> - Szenario-Builder für alle CRM-Workflows
> - Lead-spezifische TestDataFactories
> - Customer-Journey Test-Fixtures
>
> **Track 3 - Code Quality (NEU - aus PR #133 Review):**
> - **Issue #135:** Name Parsing Robustness (LeadConvertService)
> - EnumResource Refactoring (LeadSource + KitchenSize als Enums)
> - DTO Kapselung (private fields + getters)
>
> **⚠️ WICHTIG:** Track 2 ist NICHT "nur Tech-Debt" - es ist **Investment in Qualität & Velocity**!
> Ohne professionelle Testdaten können wir Sprint 2.2 (Kundenmanagement) nicht effizient umsetzen.
>
> **⚠️ WICHTIG:** Track 3 adressiert Gemini/Copilot Code Review Feedback (PR #133 - Medium Priority).

## 🔧 GIT WORKFLOW (KRITISCH!)

**PFLICHT-REGELN für alle Sprint-Arbeiten:**

### ✅ ERLAUBT (ohne User-Freigabe):
- `git commit` - Commits erstellen wenn User darum bittet
- `git add` - Dateien stagen
- `git status` / `git diff` - Status prüfen
- Feature-Branches anlegen (`git checkout -b feature/...`)

### 🚫 VERBOTEN (ohne explizite User-Freigabe):
- **`git push`** - NIEMALS ohne User-Erlaubnis pushen!
- **PR-Erstellung** - Nur auf explizite Anforderung
- **PR-Merge** - Nur wenn User explizit zustimmt
- **Branch-Deletion** - Remote-Branches nur mit User-OK löschen

### 📋 Standard-Workflow:
1. **Feature-Branch anlegen:** `git checkout -b feature/modXX-sprint-Y.Z-description`
2. **Arbeiten & Committen:** Code schreiben, Tests validieren, `git commit`
3. **User fragen:** "Branch ist bereit. Soll ich pushen und PR erstellen?"
4. **Erst nach Freigabe:** `git push` + PR-Erstellung

**Referenz:** `/CLAUDE.md` → Sektion "🚫 GIT PUSH POLICY (KRITISCH!)"

---


## Sprint-Ziel

### **Track 0: Sprint Warm-Up (Refactorings aus 2.1.6.1 - OPTIONAL)**
Kleine Refactorings vor Sprint-Start, um technische Schuld zu reduzieren.

**Kern-Deliverables:**
1. **useBusinessTypes Hook verschieben** - Shared Location für Wiederverwendbarkeit (15-30min) ✅ **JETZT**
2. **Parametrized Tests Refactoring** - Gemini-Vorschlag (1-2h) ⏸️ **VERSCHOBEN auf Tech Debt Sprint**

**Gesamt-Effort Track 0:** ~0.5h (nur useBusinessTypes Hook)

**Begründung Track 0:**
- ✅ **useBusinessTypes:** Sehr geringer Aufwand (30min), Sprint 2.1.7 profitiert (Track 2 Test-Komponenten)
- ⏸️ **Parametrized Tests:** Kein Business-Value, Sprint bereits voll → verschieben auf späteren Tech Debt Sprint

---

### **Track 1: Lead Team Management (Business Features)**
Implementierung von Team-basierter Lead-Sichtbarkeit mit Row-Level-Security, Lead-Transfer-Workflow zwischen Partnern, und intelligenter Duplikat-Erkennung mit Fuzzy-Matching.

**Kern-Deliverables:**
1. **Lead-Transfer Workflow** - Vollständiger Transfer zwischen Partnern mit Genehmigung
2. **Fuzzy-Matching & Review** - Intelligente Duplikat-Erkennung mit Scoring
3. **Row-Level-Security (RLS)** - Team-basierte Lead-Sichtbarkeit
4. **Team Management** - CRUD für Teams, Member-Assignment, Quotenregelung

### **Track 2: Test Infrastructure Overhaul (Strategisch)**
Aufbau einer professionellen Testdaten-Architektur für komplexes CRM-System, die alle Szenarien abdeckt und Sprint 2.2+ Velocity erhöht.

**Kern-Deliverables:**
1. **Clock Injection Standard (Issue #127)** - Konsistente Clock über alle Services (4-6h)
2. **ActivityOutcome Enum (Issue #126)** - Type-Safe Validation für outcome Field (2h)
3. **CRM Szenario-Builder** - Komplexe Workflows (Lead → Prospect → Customer → Opportunity → Won) (12-16h)
4. **Lead-Journey Test-Fixtures** - Alle Lead-Stages + Protection-Stati + Activities (6-8h)
5. **Faker-Integration** - Realistische Testdaten (Namen, Adressen, Branchen) (4-6h)
6. **Test-Pattern Library** - Best Practices für alle CRM-Module (4-6h)

**Gesamt-Effort Track 2:** ~32-44h (4-5.5 Tage)

**Begründung Track 2:**
- ✅ **Quality Investment:** Hochwertige Tests = weniger Bugs = schnellere Entwicklung
- ✅ **Sprint 2.2 Readiness:** Kundenmanagement braucht komplexe Customer-Test-Szenarien
- ✅ **Onboarding:** Neue Entwickler verstehen CRM-Workflows durch Testdaten
- ✅ **Regression Prevention:** Alle Edge-Cases als Test-Szenarien dokumentiert

**Verschoben aus Sprint 2.1.6 (Scope-Overflow):**
- Lead-Transfer (User Story 1 → zu komplex für 2.1.6)
- Fuzzy-Matching (User Story 4 → verdient eigenen Sprint)
- RLS + Team Management (User Story 5 & 6 → komplex, gehören zusammen)

---

## User Stories - Track 0: Sprint Warm-Up (Refactorings)

### 0.1 useBusinessTypes Hook zu Shared Location verschieben ✅ JETZT
**Quelle:** Sprint 2.1.6.1 Übergabe (optionales Refactoring)
**Begründung:** Wiederverwendbarkeit für Track 2 Test-Komponenten

**Akzeptanzkriterien:**
- [ ] **Hook verschieben:**
  ```bash
  # Neue Datei erstellen
  frontend/src/hooks/useBusinessTypes.ts

  # Hook-Code aus CustomerForm.tsx extrahieren
  # Import in CustomerForm.tsx anpassen
  ```
- [ ] **Hook-Code:**
  ```typescript
  // frontend/src/hooks/useBusinessTypes.ts
  import { useEffect, useState } from 'react';

  export interface BusinessType {
    value: string;
    label: string;
  }

  export function useBusinessTypes() {
    const [businessTypes, setBusinessTypes] = useState<BusinessType[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
      fetch('/api/enums/business-types', { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
          setBusinessTypes(data);
          setLoading(false);
        })
        .catch(err => {
          setError(err);
          setLoading(false);
        });
    }, []);

    return { businessTypes, loading, error };
  }
  ```
- [ ] **CustomerForm.tsx anpassen:**
  ```typescript
  import { useBusinessTypes } from '../../hooks/useBusinessTypes';

  export function CustomerForm() {
    const { businessTypes, loading, error } = useBusinessTypes();
    // ... rest bleibt gleich
  }
  ```
- [ ] **Tests aktualisieren:**
  - CustomerForm.test.tsx: MSW Mock funktioniert weiterhin (relative URL bereits korrekt)
  - Keine neuen Tests nötig (Hook-Logik unverändert)
- [ ] **Alle Frontend-Tests grün** (18 Tests aus Sprint 2.1.6.1)

**Aufwand:** 15-30min (File move + Import ändern)

**Referenzen:**
- Sprint 2.1.6.1 Übergabe: useBusinessTypes Hook Refactoring (optional)
- CustomerForm.tsx: Aktueller Hook-Code
- ENUM_MIGRATION_STRATEGY.md: BusinessType-Migration Kontext

**Vorteile:**
- ✅ Track 2 kann Hook in Test-Komponenten nutzen
- ✅ Konsistent mit Best Practices (Shared Hooks in /hooks/)
- ✅ Sehr geringer Aufwand (30min)

---

### 0.2 Parametrized Tests Refactoring ⏸️ VERSCHOBEN
**Quelle:** Sprint 2.1.6.1 Gemini Code Review Feedback
**Begründung:** Kein Business-Value, Sprint 2.1.7 bereits voll

**Entscheidung:**
- ⏸️ **VERSCHIEBEN** auf zukünftigen Tech Debt Sprint (nach Sprint 2.2)
- **Grund:** Tests funktionieren (alle grün), Optimierung bringt keinen direkten Nutzen
- **Aufwand gespart:** 1-2h (können in Sprint 2.1.7 Track 2 investiert werden)

**Referenzen:**
- CustomerAutoSyncSetterTest.java: 27 Tests (funktionieren perfekt)
- Gemini Review: Parametrized Tests Vorschlag (NICE-TO-HAVE)

---

## User Stories - Track 1: Business Features

### 1. Lead-Transfer Workflow (verschoben aus 2.1.6)
**Begründung:** Partner-zu-Partner Transfer mit Genehmigung und 48h SLA

**Akzeptanzkriterien:**
- [ ] **Migration V260:** `lead_transfers` Tabelle erstellen
  ```sql
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
- [ ] **POST /api/leads/{id}/transfer** - Transfer-Request initiieren
  - Validierung: Lead gehört zu from_user_id
  - Validierung: Reason Pflichtfeld (min. 20 Zeichen)
  - Audit-Log: `lead_transfer_requested`
  - Email-Benachrichtigung an to_user_id
- [ ] **GET /api/leads/transfers/pending** - Pending Transfers für Manager/Admin
- [ ] **PUT /api/leads/transfers/{id}** - Transfer genehmigen/ablehnen
  - RBAC: Nur Manager/Admin
  - Auto-Approve nach 48h → Admin-Eskalation
  - Audit-Log: `lead_transfer_approved` oder `lead_transfer_rejected`
- [ ] **Transfer-Historie:** Alle Transfers im Lead-Audit-Trail
- [ ] **Backend Tests:** 15+ Tests (Transfer-Logik, RBAC, SLA, Eskalation)

**Technische Details:**
```java
@Path("/leads/{id}/transfer")
@RolesAllowed({"partner", "manager", "admin"})
public class LeadTransferResource {

    @POST
    @Transactional
    public Response requestTransfer(
        @PathParam("id") Long leadId,
        TransferRequest request
    ) {
        // Validate ownership
        // Create transfer record (status=pending)
        // Send notification
        // Schedule 48h escalation job
        return Response.accepted().build();
    }
}
```

**Frontend Components:**
- `TransferRequestDialog.tsx` - Transfer initiieren (Partner)
- `TransferApprovalList.tsx` - Pending Transfers (Manager/Admin)
- `TransferHistoryTimeline.tsx` - Transfer-Historie im Lead-Detail

**Aufwand:** 8-12h (Medium Complexity - DB + API + UI + Tests)

**Referenzen:**
- Original: TRIGGER_SPRINT_2_1_6.md (User Story 1)
- ADR-003: RLS Design (Transfer-Empfänger Visibility)

---

### 2. Fuzzy-Matching & Duplicate Review (verschoben aus 2.1.6)
**Begründung:** Intelligente Duplikat-Erkennung mit konfigurierbaren Schwellwerten

**Akzeptanzkriterien:**
- [ ] **Scoring-Algorithmus** implementieren:
  - Email-Match: Levenshtein-Distance + pg_trgm Similarity (Gewichtung 40%)
  - Phone-Match: E.164 Normalisierung + Prefix-Match (Gewichtung 30%)
  - Company-Match: Fuzzy String + Rechtsform-Normalisierung (Gewichtung 20%)
  - Address-Match: PLZ + Stadt + Straße Fuzzy (Gewichtung 10%)
- [ ] **Schwellwerte konfigurierbar:**
  - Hard Duplicate (BLOCK): Score ≥ 90%
  - Soft Duplicate (WARNING): Score 70-89%
  - No Match: Score < 70%
- [ ] **POST /api/leads** - 202 Response bei Soft Duplicate
  ```json
  {
    "status": 202,
    "duplicateCandidates": [
      {
        "leadId": 123,
        "companyName": "Müller GmbH",
        "matchScore": 85,
        "matchReasons": ["email_similar", "company_fuzzy"]
      }
    ]
  }
  ```
- [ ] **Frontend: DuplicateReviewModal.tsx**
  - Kandidaten-Liste mit Match-Score
  - Actions: "Merge", "Create New", "Cancel"
  - Merge-Preview: Zeigt welche Felder übernommen werden
- [ ] **Merge-Logik:**
  - Lead-Felder: Neueste Werte (last_updated_at)
  - Activities: Alle Activities zusammenführen
  - Audit-Log: `lead_merged` mit source_lead_id + target_lead_id
  - Undo-Möglichkeit: `lead_unmerged` Event (24h)
- [ ] **Backend Tests:** 20+ Tests (Scoring, Schwellwerte, Merge, Undo)

**Technische Details:**
```java
public class LeadDeduplicationService {

    public MatchResult findDuplicates(Lead lead) {
        // pg_trgm für fuzzy matching
        String sql = """
            SELECT id, company_name_normalized,
                   similarity(email_normalized, :email) as email_score,
                   similarity(company_name_normalized, :company) as company_score
            FROM leads
            WHERE status != 'DELETED'
            AND (similarity(email_normalized, :email) > 0.6
                 OR similarity(company_name_normalized, :company) > 0.6)
            """;

        // Calculate weighted score
        // Return candidates with score ≥ 70%
    }
}
```

**Frontend Components:**
- `DuplicateReviewModal.tsx` - Duplicate Review UI
- `DuplicateCandidateCard.tsx` - Kandidaten-Anzeige mit Match-Score
- `MergePreviewDialog.tsx` - Merge-Vorschau
- `MergeHistoryTimeline.tsx` - Merge-Historie + Undo

**Aufwand:** 12-16h (High Complexity - Algorithmus + UI + Tests)

**Referenzen:**
- Original: TRIGGER_SPRINT_2_1_6.md (User Story 4)
- DEDUPE_POLICY.md: Hard/Soft Collision Rules

---

### 3. Row-Level-Security (RLS) Implementation (verschoben aus 2.1.6)
**Begründung:** Team-basierte Lead-Sichtbarkeit mit PostgreSQL RLS Policies

**Akzeptanzkriterien:**
- [ ] **Migration V261:** RLS Policies aktivieren
  ```sql
  -- Enable RLS on leads table
  ALTER TABLE leads ENABLE ROW LEVEL SECURITY;

  -- Policy: Owner sieht eigene Leads
  CREATE POLICY lead_owner_policy ON leads
    FOR SELECT
    USING (owner_user_id = current_setting('app.user_id')::TEXT);

  -- Policy: Team-Mitglieder sehen Team-Leads
  CREATE POLICY lead_team_policy ON leads
    FOR SELECT
    USING (
      owner_team_id IN (
        SELECT team_id FROM team_members
        WHERE user_id = current_setting('app.user_id')::TEXT
      )
    );

  -- Policy: Admin hat Vollzugriff
  CREATE POLICY lead_admin_policy ON leads
    FOR ALL
    USING (current_setting('app.user_role') = 'admin');

  -- Policy: Transfer-Empfänger sieht pending Transfers
  CREATE POLICY lead_transfer_recipient_policy ON leads
    FOR SELECT
    USING (
      id IN (
        SELECT lead_id FROM lead_transfers
        WHERE to_user_id = current_setting('app.user_id')::TEXT
        AND status = 'pending'
      )
    );
  ```
- [ ] **Session-Context:** SET für jede Request
  ```java
  @Transactional
  @PreAuthorize
  public void setSecurityContext(String userId, String role, String teamId) {
      entityManager.createNativeQuery(
          "SET LOCAL app.user_id = :userId; " +
          "SET LOCAL app.user_role = :role; " +
          "SET LOCAL app.user_team_id = :teamId"
      ).setParameter("userId", userId)
       .setParameter("role", role)
       .setParameter("teamId", teamId)
       .executeUpdate();
  }
  ```
- [ ] **Backend Tests:** 25+ Tests (Owner, Team, Admin, Transfer-Recipient, Cross-Team)
- [ ] **Performance:** P95 <50ms RLS-Overhead (Index auf owner_user_id, owner_team_id)

**Technische Details:**
- **Index-Optimierung:**
  ```sql
  CREATE INDEX idx_leads_owner_user_id ON leads(owner_user_id) WHERE status != 'DELETED';
  CREATE INDEX idx_leads_owner_team_id ON leads(owner_team_id) WHERE status != 'DELETED';
  ```
- **Policy Testing Pattern:**
  ```java
  @Test
  @AsUser("partner-a")
  void cannotSeeOtherPartnersLeads() {
      // Partner A creates lead
      Lead lead = leadService.create(...);

      // Switch to Partner B
      setSecurityContext("partner-b", "partner", "team-2");

      // Partner B queries: 0 results
      List<Lead> leads = leadService.findAll();
      assertThat(leads).isEmpty();
  }
  ```

**Aufwand:** 10-14h (High Complexity - RLS + Testing + Performance)

**Referenzen:**
- Original: TRIGGER_SPRINT_2_1_6.md (User Story 5)
- ADR-003: RLS Design

---

### 4. Team Management (verschoben aus 2.1.6)
**Begründung:** Team-basierte Lead-Verwaltung mit Quotenregelung

**Akzeptanzkriterien:**
- [ ] **Migration V262:** Teams-Tabelle
  ```sql
  CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    territory_id BIGINT REFERENCES territories(id),
    lead_quota INTEGER,  -- max. Leads pro Team
    created_at TIMESTAMPTZ DEFAULT NOW()
  );

  CREATE TABLE team_members (
    team_id UUID REFERENCES teams(id),
    user_id VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,  -- member, lead
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (team_id, user_id)
  );
  ```
- [ ] **Team CRUD API:**
  - POST /api/teams - Team erstellen (Admin/Manager)
  - GET /api/teams - Teams auflisten
  - PUT /api/teams/{id} - Team bearbeiten
  - DELETE /api/teams/{id} - Team löschen (nur wenn keine Leads zugeordnet)
- [ ] **Team-Member Assignment:**
  - POST /api/teams/{id}/members - Member hinzufügen
  - DELETE /api/teams/{id}/members/{userId} - Member entfernen
  - PUT /api/teams/{id}/members/{userId}/role - Role ändern (member ↔ lead)
- [ ] **Quotenregelung:**
  - Validierung bei Lead-Erstellung: Team-Lead-Count < team.lead_quota
  - Dashboard-Widget: "Team-Auslastung" (X/Y Leads)
- [ ] **Frontend Components:**
  - `TeamManagementPage.tsx` - Team-Übersicht + CRUD
  - `TeamMemberList.tsx` - Member-Liste mit Role-Management
  - `TeamQuotaIndicator.tsx` - Auslastungs-Anzeige
  - `TeamDashboard.tsx` - Team-Metriken (Lead-Count, Conversion-Rate, etc.)

**Aufwand:** 8-10h (Medium Complexity - CRUD + UI + Tests)

**Referenzen:**
- Original: TRIGGER_SPRINT_2_1_6.md (User Story 6)

---

## User Stories - Track 2: Test Infrastructure Overhaul

### 5. Clock Injection Standard (Issue #127 - REFACTORING)
**Begründung:** Konsistente Clock-Injection über alle Services für deterministische Tests

**Problem-Analyse:**
- ⚠️ **Inkonsistenz:** 2/5 Services haben Clock (LeadMaintenanceService ✅, FollowUpAutomationService ✅)
- ❌ **Fehlende Clock:** 3/5 Services (LeadProtectionService, LeadService, LeadConvertService)
- ✅ **Tests funktionieren** aktuell (großes Zeitfenster), aber nicht 100% deterministisch
- 🎯 **Ziel:** 100% Konsistenz + vollständig deterministische Tests

**Akzeptanzkriterien:**
- [ ] **ClockProvider erstellen:**
  ```java
  @ApplicationScoped
  public class ClockProvider {
      @Produces
      @ApplicationScoped
      public Clock provideClock() {
          return Clock.systemDefaultZone();
      }
  }
  ```
- [ ] **LeadProtectionService refactoren:**
  - 3 Methoden mit `LocalDateTime.now()` → `LocalDateTime.now(clock)`
  - Clock via `@Inject` statt private field
  - Tests auf `Clock.fixed()` umstellen
- [ ] **LeadService refactoren:**
  - 11 Stellen mit `LocalDateTime.now()` → `LocalDateTime.now(clock)`
  - Clock via `@Inject` statt private field
  - Tests auf `Clock.fixed()` umstellen
- [ ] **LeadConvertService refactoren:**
  - 8 Stellen mit `LocalDateTime.now()` → `LocalDateTime.now(clock)`
  - Clock via `@Inject` statt private field
  - Tests auf `Clock.fixed()` umstellen
- [ ] **ADR erstellen:** ADR-007 "Clock Injection Standard"
- [ ] **Alle Tests grün** (Unit + Integration)
- [ ] **100% Konsistenz** zwischen allen Services

**Effort:** ~4-6h (3 Services + ClockProvider + Tests + ADR)

**Referenzen:**
- ✅ **Beispiel-Implementation:** LeadMaintenanceService.java (Sprint 2.1.6 Phase 3)
- 📋 **Analyse:** [ISSUE_127_126_DEEP_ANALYSIS.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/ISSUE_127_126_DEEP_ANALYSIS.md)
- 🔗 **Issue:** [#127](https://github.com/joergstreeck/freshplan-sales-tool/issues/127)

---

### 6. ActivityOutcome Enum + Validation (Issue #126 - DATA INTEGRITY)
**Begründung:** Type-Safe Enum statt String für `outcome` Field (Vorbereitung Follow-Up-Dashboard)

**Problem-Analyse:**
- ❌ **String ohne Validierung:** `outcome VARCHAR(50)` erlaubt beliebige Werte
- ✅ **Comment definiert Werte:** 7 erlaubte Outcomes (V256)
- ⚠️ **Keine Type-Safety:** Typos möglich (z.B. "positve_interest")
- ✅ **Field aktuell ungenutzt** (0 read, 0 write) → kein Production-Impact
- 🎯 **Sprint 2.1.7 braucht Enum** (Follow-Up-Dashboard nutzt outcome)

**Akzeptanzkriterien:**
- [ ] **Enum erstellen:**
  ```java
  public enum ActivityOutcome {
      POSITIVE_INTEREST,
      NEEDS_MORE_INFO,
      NOT_INTERESTED,
      CALLBACK_SCHEDULED,
      DEMO_SCHEDULED,
      CLOSED_WON,
      CLOSED_LOST
  }
  ```
- [ ] **LeadActivity.outcome anpassen:**
  ```java
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome", length = 50)
  public ActivityOutcome outcome;
  ```
- [ ] **Migration V269+ erstellen:**
  ```sql
  ALTER TABLE lead_activities
    ADD CONSTRAINT lead_activities_outcome_chk
    CHECK (outcome IN (
      'POSITIVE_INTEREST',
      'NEEDS_MORE_INFO',
      'NOT_INTERESTED',
      'CALLBACK_SCHEDULED',
      'DEMO_SCHEDULED',
      'CLOSED_WON',
      'CLOSED_LOST'
    ) OR outcome IS NULL);
  ```
- [ ] **Tests schreiben:**
  - Ungültige Outcomes werden rejected (DB-Level)
  - Ungültige Outcomes werden rejected (Entity-Level)
  - JSON Serialization korrekt
- [ ] **Alle Tests grün**

**Effort:** ~2h (Enum + Migration + Tests)

**Referenzen:**
- 📋 **V256 Migration:** Definiert erlaubte Werte im Comment
- 📋 **Analyse:** [ISSUE_127_126_DEEP_ANALYSIS.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_6/ISSUE_127_126_DEEP_ANALYSIS.md)
- 🔗 **Issue:** [#126](https://github.com/joergstreeck/freshplan-sales-tool/issues/126)

---

### 7. CRM Szenario-Builder (NEU - STRATEGISCH!)
**Begründung:** Komplexe CRM-Workflows brauchen professionelle Test-Szenarien

**Problem-Analyse (aus TESTING_GUIDE.md):**
- ❌ **Alte Strategie:** 3-4 SEED-Kunden für alle Tests → ständige Konflikte
- ✅ **Aktuelle Strategie:** Jeder Test erstellt eigene Daten → unabhängig, aber manuell
- 🎯 **Neue Strategie:** Szenario-Builder für Standard-Workflows → konsistent + schnell

**Akzeptanzkriterien:**
- [ ] **ScenarioBuilder erstellen:**
  ```java
  @ApplicationScoped
  public class CRMScenarioBuilder {

      public LeadJourneyScenario aLeadJourney() {
          return new LeadJourneyScenario(this);
      }

      public CustomerJourneyScenario aCustomerJourney() {
          return new CustomerJourneyScenario(this);
      }

      public OpportunityPipelineScenario anOpportunityPipeline() {
          return new OpportunityPipelineScenario(this);
      }
  }

  // Verwendung:
  @Inject CRMScenarioBuilder crm;

  @Test
  void shouldConvertLeadToCustomer() {
      var scenario = crm.aLeadJourney()
          .withStage(LeadStage.QUALIFIZIERT)
          .withActivities(5)  // 5 Activities (automatisch counts_as_progress=true)
          .withProtection(60) // 60 Tage Schutz
          .build();

      // scenario hat: Lead + 5 Activities + Protection-Felder gesetzt
      leadService.convertToCustomer(scenario.lead.id);

      Customer customer = customerRepository.findById(scenario.lead.id);
      assertThat(customer.status()).isEqualTo(CustomerStatus.AKTIV);
  }
  ```
- [ ] **Lead-Journey Scenarios:**
  - `PreClaimLead` - Stage 0, keine registered_at
  - `RegisteredLead` - Stage 1, registered_at gesetzt, <5 Activities
  - `QualifiedLead` - Stage 2, ≥5 Activities (counts_as_progress=true)
  - `ProtectedLead` - Protection aktiv, progress_deadline in Zukunft
  - `ExpiredLead` - Protection abgelaufen, expired_at gesetzt
  - `TransferredLead` - Lead-Transfer Historie
- [ ] **Customer-Journey Scenarios:**
  - `NewCustomer` - Frisch konvertiert von Lead
  - `ActiveCustomer` - 3+ Opportunities, 10+ Activities
  - `RiskCustomer` - Keine Activity seit 90 Tagen
  - `ChurnedCustomer` - Status ARCHIVIERT
- [ ] **Opportunity-Pipeline Scenarios:**
  - `QualifiedOpportunity` - Stage QUALIFIED, 50% Win-Probability
  - `ProposalOpportunity` - Stage PROPOSAL, 75% Win-Probability
  - `NegotiationOpportunity` - Stage NEGOTIATION, 90% Win-Probability
  - `WonOpportunity` - Stage WON, close_date gesetzt
  - `LostOpportunity` - Stage LOST, lost_reason gesetzt

**Aufwand:** 12-16h (High Complexity - viele Szenarien, aber hoher ROI!)

**Referenzen:**
- TESTING_GUIDE.md: Zeile 106-152 (Builder Pattern)
- TestDataBuilder.java: `scenario()` Methode (bereits vorhanden!)

---

### 6. Faker-Integration für realistische Testdaten (NEU)
**Begründung:** Realistische Testdaten verbessern Test-Qualität und Edge-Case-Detection

**Akzeptanzkriterien:**
- [ ] **Java Faker Dependency hinzufügen:**
  ```xml
  <dependency>
      <groupId>net.datafaker</groupId>
      <artifactId>datafaker</artifactId>
      <version>2.0.2</version>
      <scope>test</scope>
  </dependency>
  ```
- [ ] **RealisticDataGenerator erstellen:**
  ```java
  public class RealisticDataGenerator {
      private static final Faker faker = new Faker(Locale.GERMAN);

      public static String germanCompanyName() {
          return faker.company().name() + " " +
                 faker.options().option("GmbH", "AG", "KG", "OHG");
      }

      public static String germanAddress() {
          return faker.address().streetAddress() + ", " +
                 faker.address().zipCode() + " " +
                 faker.address().city();
      }

      public static String germanPhoneNumber() {
          return faker.phoneNumber().phoneNumber(); // E.164 Format
      }

      public static String germanIndustry() {
          return faker.options().option(
              "Gastronomie", "Hotel", "Catering", "Kantinen",
              "Bäckerei", "Konditorei", "Metzgerei"
          );
      }
  }
  ```
- [ ] **Integration in CustomerTestDataFactory:**
  ```java
  public static class Builder {
      private String companyName = RealisticDataGenerator.germanCompanyName();
      private String city = RealisticDataGenerator.germanCity();
      private String phone = RealisticDataGenerator.germanPhoneNumber();
      // ...
  }
  ```
- [ ] **Edge-Case-Daten generieren:**
  - Umlaute: "Müller GmbH", "Bäckerei Schröder"
  - Sonderzeichen: "Café & Restaurant", "O'Reilly's Pub"
  - Lange Namen: 200+ Zeichen Company Names
  - Multi-Word Cities: "Frankfurt am Main", "Neustadt an der Weinstraße"

**Aufwand:** 4-6h (Low Complexity - Dependency + Integration)

**Referenzen:**
- CustomerTestDataFactory.java: Zeile 42-62 (generateCompanyName - zu simpel!)

---

### 7. Lead-spezifische TestDataFactories (NEU)
**Begründung:** Modul 02 (Neukundengewinnung) braucht Lead-spezifische Builder

**Akzeptanzkriterien:**
- [ ] **LeadTestDataFactory erstellen:**
  ```java
  public class LeadTestDataFactory {

      public static Builder builder() {
          return new Builder();
      }

      public static class Builder {
          private String companyName = RealisticDataGenerator.germanCompanyName();
          private LeadStage stage = LeadStage.VORMERKUNG;
          private LeadStatus status = LeadStatus.REGISTERED;
          private String ownerUserId = "test-partner-1";
          private LocalDateTime registeredAt = LocalDateTime.now(); // Variante B: IMMER gesetzt
          private LocalDateTime firstContactDocumentedAt = null; // null = Pre-Claim
          private List<LeadActivity> activities = new ArrayList<>();

          // Lead-spezifische Presets (Variante B)
          public Builder asPreClaimLead() {
              this.stage = LeadStage.VORMERKUNG;
              this.registeredAt = LocalDateTime.now();
              this.firstContactDocumentedAt = null; // ← Pre-Claim!
              return this;
          }

          public Builder asRegisteredLead() {
              this.stage = LeadStage.REGISTRIERUNG;
              this.registeredAt = LocalDateTime.now().minusDays(10);
              this.firstContactDocumentedAt = LocalDateTime.now().minusDays(10); // ← Vollschutz!
              this.activities = generateActivities(3, false); // nicht counts_as_progress
              return this;
          }

          public Builder asQualifiedLead() {
              this.stage = LeadStage.QUALIFIZIERT;
              this.registeredAt = LocalDateTime.now().minusDays(20);
              this.activities = generateActivities(5, true); // counts_as_progress
              return this;
          }

          public Builder withProtection(int days) {
              this.protectionStartAt = LocalDateTime.now();
              this.protectionMonths = 6;
              this.progressDeadline = LocalDateTime.now().plusDays(days);
              return this;
          }

          public Lead build() { /* ... */ }
      }
  }
  ```
- [ ] **LeadActivityTestDataFactory erstellen:**
  ```java
  public class LeadActivityTestDataFactory {

      public static Builder builder() {
          return new Builder();
      }

      public static class Builder {
          private ActivityType type = ActivityType.NOTE;
          private boolean countsAsProgress = false;
          private String summary;
          private LocalDateTime createdAt = LocalDateTime.now();

          public Builder asProgressActivity() {
              this.type = ActivityType.QUALIFIED_CALL;
              this.countsAsProgress = true;
              this.summary = "Qualifizierungsgespräch geführt";
              return this;
          }

          public Builder asNonProgressActivity() {
              this.type = ActivityType.NOTE;
              this.countsAsProgress = false;
              this.summary = "Notiz hinzugefügt";
              return this;
          }

          public LeadActivity build() { /* ... */ }
      }
  }
  ```
- [ ] **Integration in Tests:**
  ```java
  @Test
  void shouldCalculateProgressDeadline() {
      Lead lead = LeadTestDataFactory.builder()
          .asRegisteredLead()
          .withProtection(60)
          .buildAndPersist(leadRepository);

      LeadActivity activity = LeadActivityTestDataFactory.builder()
          .asProgressActivity()
          .forLead(lead)
          .buildAndPersist(activityRepository);

      // Test: progress_deadline wird neu berechnet
      leadProtectionService.recalculateDeadline(lead.id);

      Lead updated = leadRepository.findById(lead.id);
      assertThat(updated.progressDeadline).isAfter(LocalDateTime.now().plusDays(59));
  }
  ```

**Aufwand:** 6-8h (Medium Complexity - neue Factories + Integration)

**Referenzen:**
- ACTIVITY_TYPES_PROGRESS_MAPPING.md: countsAsProgress Mapping
- PRE_CLAIM_LOGIC.md: Pre-Claim Szenarien

---

### 8. Lead Activity Capture UI (NEU - VERVOLLSTÄNDIGUNG)
**Begründung:** Timeline zeigt Aktivitäten, aber KEINE UI zum manuellen Erfassen neuer Aktivitäten existiert

**Problem-Analyse:**
- ✅ **LeadActivityTimeline.tsx:** Zeigt chronologische Historie aller Aktivitäten
- ✅ **Backend-Endpoint:** POST /api/leads/{id}/activities existiert bereits (Sprint 2.1.5/2.1.6)
- ❌ **Fehlende UI:** Kein Dialog zum Erfassen neuer Aktivitäten (E-Mail, Anruf, Termin, Notiz)
- 🎯 **Ziel:** Lightweight Dialog für schnelle Activity-Capture → Integration in Lead-Table Actions

**Akzeptanzkriterien:**
- [ ] **AddLeadActivityDialog.tsx erstellen:**
  ```typescript
  interface AddLeadActivityDialogProps {
    open: boolean;
    leadId: number;
    onClose: () => void;
    onSuccess: () => void;
  }

  export function AddLeadActivityDialog({ open, leadId, onClose, onSuccess }: AddLeadActivityDialogProps) {
    // Form mit:
    // - Activity-Type Dropdown (E-Mail, Anruf, Termin, Notiz, Muster versendet, Bestellung)
    // - Zusammenfassung (Textfeld, Pflicht, min. 10 Zeichen)
    // - Details (Multiline Textfeld, optional)
    // - Outcome Dropdown (optional): POSITIVE_INTEREST, NEEDS_MORE_INFO, etc.
    // - "Meaningful Contact" Checkbox (automatisch checked für E-Mail/Anruf/Termin)
  }
  ```
- [ ] **Action-Button in CustomerTable.tsx:**
  ```typescript
  // Spalte "actions" erweitern (nur für context='leads'):
  <IconButton
    size="small"
    onClick={(e) => {
      e.stopPropagation();
      setSelectedLeadForActivity(lead);
      setActivityDialogOpen(true);
    }}
    title="Aktivität erfassen"
  >
    <AddIcon />
  </IconButton>
  ```
- [ ] **API-Integration:**
  ```typescript
  const handleSaveActivity = async (activity: LeadActivityRequest) => {
    const response = await fetch(`/api/leads/${leadId}/activities`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(activity),
    });

    if (!response.ok) throw new Error('Fehler beim Speichern der Aktivität');

    onSuccess(); // Refresh timeline
    onClose();
  };
  ```
- [ ] **Validation:**
  - Activity-Type: Pflichtfeld
  - Zusammenfassung: min. 10 Zeichen, max. 255 Zeichen
  - Meaningful Contact: Auto-checked für EMAIL/CALL/MEETING, editierbar
- [ ] **UX-Details:**
  - German labels (DESIGN_SYSTEM.md konform): "E-Mail", "Anruf", "Termin", "Notiz", "Muster versendet", "Bestellung"
  - Validation-Feedback: Inline-Errors (Zusammenfassung zu kurz, etc.)
  - Success-Feedback: Snackbar "Aktivität erfasst" (3s Auto-Close)
  - Dialog schließt automatisch nach Success

**Betroffene Komponenten:**
- **Frontend:**
  - AddLeadActivityDialog.tsx (NEW - Activity Capture Dialog)
  - CustomerTable.tsx (Add Action Button + Dialog State)
  - LeadActivityTimeline.tsx (Refresh nach neuer Activity)

**Aufwand:** 4-6h (Medium Complexity - Dialog + Validation + API-Integration + UX)

**Referenzen:**
- POST /api/leads/{id}/activities: LeadResource.java (Sprint 2.1.5)
- DESIGN_SYSTEM.md: UI-Sprachregeln (German labels)
- Activity Types: [ACTIVITY_TYPES_PROGRESS_MAPPING.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md)

---

### 9. Test-Pattern Library & Documentation (NEU)
**Begründung:** Best Practices dokumentieren für Onboarding und Konsistenz

**Akzeptanzkriterien:**
- [ ] **TESTING_PATTERNS.md erstellen:**
  ```markdown
  # CRM Testing Patterns - Best Practices

  ## Pattern 1: Lead-Journey Testing
  ### When to use
  - Testing Lead-Lifecycle (Stage-Transitions, Protection, Conversion)

  ### How to use
  ```java
  @Test
  void shouldConvertQualifiedLeadToCustomer() {
      // ARRANGE: Use Scenario-Builder
      var scenario = crm.aLeadJourney()
          .withStage(LeadStage.QUALIFIZIERT)
          .withActivities(5)
          .build();

      // ACT: Execute business logic
      leadService.convertToCustomer(scenario.lead.id);

      // ASSERT: Verify outcome
      Customer customer = customerRepository.findById(scenario.lead.id);
      assertThat(customer.status()).isEqualTo(CustomerStatus.AKTIV);
  }
  ```

  ## Pattern 2: RBAC Testing with RLS
  ### When to use
  - Testing Row-Level-Security Policies

  ### How to use
  ```java
  @Test
  @AsUser("partner-a")
  void shouldOnlySeeOwnLeads() {
      // ARRANGE: Create lead as Partner A
      Lead lead = LeadTestDataFactory.builder()
          .withOwner("partner-a")
          .buildAndPersist(leadRepository);

      // ACT: Query as different user
      setSecurityContext("partner-b", "partner", "team-2");
      List<Lead> leads = leadService.findAll();

      // ASSERT: Partner B sees 0 results
      assertThat(leads).isEmpty();
  }
  ```

  ## Pattern 3: Activity-Progress Testing
  ### When to use
  - Testing Activity-based Progress-Tracking

  ### How to use
  ```java
  @Test
  void shouldRecalculateProgressDeadline() {
      // ARRANGE: Lead with protection + activities
      Lead lead = LeadTestDataFactory.builder()
          .asRegisteredLead()
          .withProtection(60)
          .buildAndPersist(leadRepository);

      // ACT: Add progress activity
      LeadActivityTestDataFactory.builder()
          .asProgressActivity()
          .forLead(lead)
          .buildAndPersist(activityRepository);

      // ASSERT: progress_deadline updated
      Lead updated = leadRepository.findById(lead.id);
      assertThat(updated.progressDeadline).isAfter(LocalDateTime.now().plusDays(59));
  }
  ```
  ```
- [ ] **TEST_DATA_CHEATSHEET.md erstellen:**
  ```markdown
  # Test Data Cheat Sheet

  ## Quick Reference: Builders & Scenarios

  ### Leads
  - `LeadTestDataFactory.builder().asPreClaimLead()` → Stage 0, keine registered_at
  - `LeadTestDataFactory.builder().asRegisteredLead()` → Stage 1, 3 Activities
  - `LeadTestDataFactory.builder().asQualifiedLead()` → Stage 2, 5 Progress-Activities

  ### Customers
  - `CustomerTestDataFactory.builder().buildMinimal()` → LEAD Status, Berlin
  - `CustomerTestDataFactory.builder().asActiveCustomer()` → AKTIV Status, 3 Opportunities

  ### Activities
  - `LeadActivityTestDataFactory.builder().asProgressActivity()` → countsAsProgress=true
  - `LeadActivityTestDataFactory.builder().asNonProgressActivity()` → countsAsProgress=false

  ### Scenarios
  - `crm.aLeadJourney().withStage(...).build()` → Lead + Activities + Protection
  - `crm.aCustomerJourney().withOpportunities(3).build()` → Customer + Opportunities
  ```
- [ ] **Migration Guide für alte Tests:**
  - 10 Beispiel-Migrationen dokumentieren (alt → neu)
  - Anti-Patterns dokumentieren (was NICHT tun)
  - Performance-Tipps (z.B. @BeforeAll statt @BeforeEach für statische Daten)

**Aufwand:** 4-6h (Low Complexity - Dokumentation)

**Referenzen:**
- TESTING_GUIDE.md: Basis-Dokumentation erweitern

---

## Definition of Done

### Track 1: Business Features
**Backend:**
- [ ] V260-V262 Migrations deployed & tested
- [ ] Lead-Transfer API (Request, Approve, Reject, History)
- [ ] Fuzzy-Matching Service (Scoring, Schwellwerte, Merge)
- [ ] RLS Policies aktiv (Owner, Team, Admin, Transfer-Recipient)
- [ ] Team Management CRUD-API
- [ ] Unit Tests ≥80% Coverage
- [ ] Integration Tests für alle API-Endpoints
- [ ] Performance: RLS P95 <50ms overhead

**Frontend:**
- [ ] TransferRequestDialog.tsx + TransferApprovalList.tsx
- [ ] DuplicateReviewModal.tsx + MergePreviewDialog.tsx
- [ ] TeamManagementPage.tsx + TeamDashboard.tsx
- [ ] Integration Tests ≥80% Coverage

**Dokumentation:**
- [ ] API-Docs aktualisiert (Transfer, Matching, Team)
- [ ] ADR-003 erweitert (RLS Policies)
- [ ] SUMMARY.md finalisiert

### Track 2: Test Infrastructure
**Test Infrastructure:**
- [ ] CRMScenarioBuilder implementiert (Lead-Journey, Customer-Journey, Opportunity-Pipeline)
- [ ] Faker-Integration abgeschlossen (RealisticDataGenerator)
- [ ] LeadTestDataFactory + LeadActivityTestDataFactory erstellt
- [ ] 20+ Szenario-Builder Tests (validieren Test-Szenarien)

**Dokumentation:**
- [ ] TESTING_PATTERNS.md erstellt (5+ Patterns dokumentiert)
- [ ] TEST_DATA_CHEATSHEET.md erstellt (Quick Reference)
- [ ] Migration Guide für alte Tests (10+ Beispiele)
- [ ] TESTING_GUIDE.md erweitert (Szenario-Builder Section)

---

## Risiken & Mitigationen

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|------------|
| **RLS Performance-Problem** | Mittel | Hoch | Index-Optimierung + Performance-Tests (P95 <50ms) |
| **Policy-Konflikte** | Mittel | Hoch | Umfassende Test-Suite für alle Policy-Kombinationen |
| **Transfer-Deadlocks** | Niedrig | Mittel | Pessimistic Locking mit Timeout |
| **False Positives (Matching)** | Hoch | Mittel | Schwellwerte iterativ tunen (A/B Testing) |
| **Track 2 zu umfangreich** | Mittel | Mittel | Optional-Scope: Faker + Patterns sind NICE-TO-HAVE |

---

## Abhängigkeiten

**Voraussetzungen (aus vorherigen Sprints):**
- ✅ Sprint 2.1.4 (Normalisierung) abgeschlossen
- ✅ Sprint 2.1.5 (Protection) abgeschlossen
- ✅ Sprint 2.1.6 (Migration-API, Convert-Flow) abgeschlossen

**Technische Abhängigkeiten:**
- PostgreSQL 14+ für RLS Features
- Team-Tabellen müssen existieren (V262)
- pg_trgm Extension für Fuzzy-Matching

---

## Monitoring & KPIs

**Business Metrics:**
- **Transfer Approval Time:** Ziel <24h average
- **RLS Query Performance:** P95 <50ms overhead
- **Matching Accuracy:** >95% precision, >90% recall
- **Team Quota Utilization:** Dashboard-Widget

**Test Infrastructure Metrics:**
- **Test Execution Time:** Ziel <5min für Unit Tests (Track 2 sollte Tests SCHNELLER machen!)
- **Test Data Creation Time:** <100ms pro Szenario
- **Test Coverage:** ≥85% für alle Module (verbessert durch bessere Testdaten)

---

## Next Sprint Preview

**Sprint 2.2:** Kundenmanagement - Field-based Customer Architecture mit Contact-Hierarchie

**Vorbereitung durch Sprint 2.1.7:**
- ✅ Customer-Journey Scenarios bereits vorhanden (Track 2)
- ✅ Team-basierte Sichtbarkeit etabliert (RLS)
- ✅ Test-Patterns dokumentiert (Onboarding neuer Entwickler)

---

## Referenzen

**Track 1 - Business Features:**
- [TRIGGER_SPRINT_2_1_6.md](TRIGGER_SPRINT_2_1_6.md) - Verschobene User Stories
- [ADR-003-rls-leads-row-level-security.md](features-neu/02_neukundengewinnung/shared/adr/ADR-003-rls-leads-row-level-security.md)
- [DEDUPE_POLICY.md](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md)

**Track 2 - Test Infrastructure:**
- [TESTING_GUIDE.md](grundlagen/TESTING_GUIDE.md) - Zeile 106-152 (Builder Pattern)
- [Issue #130](https://github.com/joergstreeck/freshplan-sales-tool/issues/130) - TestDataBuilder Refactoring (Basis-Problem)
- TestDataBuilder.java: `scenario()` Methode (Vorbild)
- [DEV_SEED_INFRASTRUCTURE_SUMMARY.md](features-neu/00_infrastruktur/migrationen/artefakte/DEV_SEED_INFRASTRUCTURE_SUMMARY.md) - DEV-SEED Strategy (V90001+)
- [DEV-SEED README](../../backend/src/main/resources/db/dev-seed/README.md) - Migration Strategy Documentation

**Track 3 - Code Quality:**
- [PR #133](https://github.com/joergstreeck/freshplan-sales-tool/pull/133) - BusinessType Harmonization & Admin-APIs
- Copilot Code Review (11 Comments) - PR #133 Feedback
- Gemini Code Review (6 Comments, CRITICAL: Idempotenz) - PR #133 Feedback
- **Issue #135:** Name Parsing Robustness (LeadConvertService.java Zeile 129-140)
- EnumResource.java: LeadSource + KitchenSize als Enums (konsistent mit BusinessType)

---

## 📋 TRACK 3: CODE QUALITY (NEU - aus PR #133 Review)

**Quelle:** Copilot/Gemini Code Review Feedback (PR #133)
**Priorität:** Medium (verbessert Datenqualität + Architektur-Konsistenz)
**Aufwand:** 5h (0.6 Tage)

### Issue #135: Name Parsing Robustness

**Problem:**
```java
// LeadConvertService.java (Zeile 134)
// ❌ Aktuell: Zu simpel - versagt bei komplexen Namen
String[] nameParts = lead.contactPerson.trim().split("\\s+", 2);
contact.setFirstName(nameParts[0]); // "Dr." → First Name (falsch!)
contact.setLastName(nameParts.length > 1 ? nameParts[1] : "");
```

**Fehlschlägt bei:**
- "Dr. Max von Mustermann" → firstName="Dr.", lastName="Max von Mustermann"
- "Maria Anna Schmidt" → firstName="Maria", lastName="Anna Schmidt"
- "Prof. Dr. Meyer-Schmidt" → firstName="Prof.", lastName="Dr. Meyer-Schmidt"

**Lösung Option A: Name Parsing Library (EMPFOHLEN)**
```java
import org.apache.commons.lang3.text.HumanNameParser; // oder ähnlich

private void parseContactName(String fullName, CustomerContact contact) {
  HumanName parsed = HumanNameParser.parse(fullName);
  contact.setFirstName(parsed.getGivenName());
  contact.setLastName(parsed.getFamilyName());
  contact.setTitle(parsed.getTitle()); // Dr., Prof., etc.
}
```

**Lösung Option B: Frontend-Lösung (ALTERNATIVE)**
Falls Library zu komplex → Getrennte Felder in LeadWizard.tsx:
```typescript
<TextField label="Titel" name="contact.title" />
<TextField label="Vorname *" name="contact.firstName" required />
<TextField label="Nachname *" name="contact.lastName" required />
```
→ 0 Backend-Aufwand, bessere Datenqualität, bessere UX

**Aufwand:**
- Option A (Library): 3h (Recherche + Implementation + Tests)
- Option B (Frontend): 1h (UI-Änderung in LeadWizard.tsx)

---

### EnumResource Refactoring: LeadSource + KitchenSize als Enums

**Problem:**
```java
// EnumResource.java - Inkonsistent!
// ✅ BusinessType: Dynamic Enum (GOOD)
public List<EnumValue> getBusinessTypes() {
  return Arrays.stream(BusinessType.values())
    .map(type -> new EnumValue(type.name(), type.getDisplayName()))
    .toList();
}

// ❌ LeadSource: Hardcoded List (BAD)
public List<EnumValue> getLeadSources() {
  return List.of(
    new EnumValue("MESSE", "Messe"),
    new EnumValue("EMPFEHLUNG", "Empfehlung"),
    // ... hardcoded!
  );
}
```

**Lösung:**
```java
// 1. LeadSource.java (NEW Enum)
public enum LeadSource {
  MESSE("Messe"),
  EMPFEHLUNG("Empfehlung"),
  TELEFON("Telefon"),
  WEB_FORMULAR("Web-Formular"),
  PARTNER("Partner"),
  SONSTIGE("Sonstige");

  private final String displayName;
  LeadSource(String displayName) { this.displayName = displayName; }
  public String getDisplayName() { return displayName; }
}

// 2. EnumResource.java (REFACTORED)
public List<EnumValue> getLeadSources() {
  return Arrays.stream(LeadSource.values())
    .map(source -> new EnumValue(source.name(), source.getDisplayName()))
    .toList();
}
```

**Benefits:**
- ✅ Konsistent mit BusinessType Pattern
- ✅ Type-Safe (keine Strings in Code)
- ✅ Erweiterbar ohne Code-Änderungen

**Aufwand:** 2h (LeadSource + KitchenSize Enums + Tests)

---

### Track 3 Summary

| Task | Aufwand | Quelle | Priorität |
|------|---------|--------|-----------|
| **Issue #135:** Name Parsing | 3h (Library) oder 1h (Frontend) | Gemini Review (HIGH) | Medium |
| **EnumResource Refactoring** | 2h | Gemini Review (MEDIUM) | Medium |
| **Total** | 5h (Option A) oder 3h (Option B) | PR #133 Feedback | - |

**Entscheidung:**
- ✅ Issue #135: **Option B (Frontend-Lösung)** bevorzugt (1h, bessere UX)
- ✅ EnumResource: Implementieren (2h, Architektur-Konsistenz)
- **Total Track 3: 3h (0.4 Tage)**

---

**Erstellt:** 2025-10-06 (Track 3 hinzugefügt)
**Review:** Product Owner ⏳, Tech Lead ⏳
**Status:** 📅 PLANNED (Start: 19.10.2025)
