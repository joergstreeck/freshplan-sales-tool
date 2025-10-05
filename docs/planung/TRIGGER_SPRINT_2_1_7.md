---
sprint_id: "2.1.7"
title: "Lead Team Management & Test Infrastructure Overhaul"
doc_type: "konzept"
status: "planned"
owner: "team/leads-backend"
date_start: "2025-10-19"
date_end: "2025-10-25"
modules: ["02_neukundengewinnung", "00_infrastruktur"]
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/shared/adr/ADR-003-rls-leads-row-level-security.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "grundlagen/TESTING_GUIDE.md"
pr_refs: []
updated: "2025-10-05"
---

# Sprint 2.1.7 – Lead Team Management & Test Infrastructure Overhaul

**📍 Navigation:** Home → Planung → Sprint 2.1.7

> **🎯 SPRINT-FOKUS: ZWEI PARALLELE TRACKS**
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
> **⚠️ WICHTIG:** Track 2 ist NICHT "nur Tech-Debt" - es ist **Investment in Qualität & Velocity**!
> Ohne professionelle Testdaten können wir Sprint 2.2 (Kundenmanagement) nicht effizient umsetzen.

## Sprint-Ziel

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
1. **CRM Szenario-Builder** - Komplexe Workflows (Lead → Prospect → Customer → Opportunity → Won)
2. **Lead-Journey Test-Fixtures** - Alle Lead-Stages + Protection-Stati + Activities
3. **Faker-Integration** - Realistische Testdaten (Namen, Adressen, Branchen)
4. **Test-Pattern Library** - Best Practices für alle CRM-Module

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

### 5. CRM Szenario-Builder (NEU - STRATEGISCH!)
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
          private LocalDateTime registeredAt = null; // null = Pre-Claim
          private List<LeadActivity> activities = new ArrayList<>();

          // Lead-spezifische Presets
          public Builder asPreClaimLead() {
              this.stage = LeadStage.VORMERKUNG;
              this.registeredAt = null;
              return this;
          }

          public Builder asRegisteredLead() {
              this.stage = LeadStage.REGISTRIERUNG;
              this.registeredAt = LocalDateTime.now().minusDays(10);
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

### 8. Test-Pattern Library & Documentation (NEU)
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

---

**Erstellt:** 2025-10-05
**Review:** Product Owner ⏳, Tech Lead ⏳
**Status:** 📅 PLANNED (Start: 19.10.2025)
