# Lead-Erfassung - Technical Concept

**📊 Plan Status:** ✅ Ready for Implementation
**🎯 Owner:** Backend + Business Logic Team
**⏱️ Timeline:** Q4 2025 Woche 1-12 (12 Wochen parallel zu E-Mail-Posteingang)
**🔧 Effort:** L (Complete Module mit Handelsvertretervertrag-Compliance)

## 🎯 Executive Summary (für Claude)

**Mission:** Vollständiges Lead-Management-System mit Handelsvertretervertrag-konformer State-Machine und UserLeadSettings.
**Problem:** Salesteam benötigt rechtssichere Lead-Verwaltung mit automatischer 6-Monats-Schutz, 60-Tage-Aktivitätschecks und benutzerspezifischen Provisionen.
**Solution:** Complete Module Development mit Lead-State-Machine, Stop-the-Clock-System, UserLeadSettings-Entity und Provisions-Integration.
**Timeline:** 12 Wochen parallel zu Email-Posteingang für optimale Integration und Testbarkeit.
**Impact:** Rechtssichere Lead-Verwaltung mit automatischen Fristen-Monitoring und flexiblen User-Konfigurationen.

**Quick Context:** Basiert auf detaillierter Handelsvertretervertrag-Analyse (§2(8) Lead-Schutz), KI-Production-Specs mit Database-Constraints, UserLeadSettings für individuelle Provisionen/Zeiträume, und TestDataBuilder-Integration für Mock-Replacement.

## 📋 Context & Dependencies

### Current State:
- ✅ Handelsvertretervertrag-Requirements analysiert (6/60/10-Regelung)
- ✅ KI Production-Ready Specs mit Database-Engineering verfügbar
- ✅ TestDataBuilder-System für Mock-Replacement etabliert
- ✅ Backend-Module-Struktur unter `/backend/modules/customer/`
- ✅ Event-System für E-Mail-Integration und Cockpit-Updates

### Target State:
- ✅ Lead-State-Machine: REGISTERED→ACTIVE→REMINDER_SENT→GRACE_PERIOD→EXPIRED/EXTENDED/STOP_CLOCK
- ✅ Handelsvertretervertrag-Compliance (6-Monats-Schutz, 60-Tage-Aktivitätscheck, 10-Tage-Nachfrist)
- ✅ UserLeadSettings-Entity für benutzerspezifische Provisionen (7%/2%) und Zeiträume
- ✅ Stop-the-Clock-System für Freshfoodz-bedingte Verzögerungen
- ✅ Lead→Customer-Konvertierung mit Provisions-Berechnung
- ✅ E-Mail-Integration für automatische Aktivitäts-Erkennung

### Dependencies:
→ [Handelsvertretervertrag Requirements](../diskussionen/2025-09-18_handelsvertretervertrag-lead-requirements.md) - Business-Logic
→ [KI Production Specs](../diskussionen/2025-09-18_finale-ki-specs-bewertung.md) - Database-Engineering
→ [Email-Posteingang Technical Concept](../email-posteingang/technical-concept.md) - Aktivitäts-Integration

## 🛠️ Implementation Phases

### Phase 1: Lead-Entity + State-Machine Foundation (Woche 1-3)
**Goal:** Core Lead-Entity mit vertragskonformer State-Machine und UserLeadSettings

**Actions:**
- [ ] Lead-Entity mit Pflichtfeldern (company, location, centralContact)
- [ ] Lead-State-Machine (7 Zustände: REGISTERED, ACTIVE, REMINDER_SENT, GRACE_PERIOD, EXPIRED, EXTENDED, STOP_CLOCK)
- [ ] UserLeadSettings-Entity für benutzerspezifische Konfiguration
- [ ] Database-Schema mit PostgreSQL-Constraints und Performance-Indizes
- [ ] Basic Lead-Service mit State-Transitions
- [ ] Aktivitäts-Entity für Handelsvertretervertrag-konforme Dokumentation

**Foundation Standards Artefakte:**
- **Lead Entity:** `backend/LeadEntity.java` - Vollständige JPA Entity mit Foundation Standards
- **Security Filter:** `shared/security/SecurityScopeFilter.java` - ABAC Territory-basierte Zugriffskontrolle
- **API Specification:** `api/lead-management.api.json` - OpenAPI 3.1 mit Foundation References
- **Database Migration:** `database/VXXX__create_lead_table.sql` - Vollständiges Schema mit Indizes
- **Theme Integration:** Via `shared/frontend/theme-v2.mui.ts` - FreshFoodz CI konform

**Code Changes:**
```java
// Lead-Entity mit Foundation Standards (aus backend/LeadEntity.java)
@Entity
@Table(name = "lead")
public class LeadEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    // Foundation Standards: Sichtbare Felder mit Theme V2 Support
    @Column(nullable = false, length = 200) private String company;
    @Column(nullable = false, length = 150) private String location;
    private String centralContact;
    private String documentedFirstContact;

    // State-Machine für Vertrags-Compliance
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeadStatus status = LeadStatus.REGISTERED;

    // Foundation Standards: Performance-optimierte Zeitstempel
    @CreationTimestamp
    private LocalDateTime registeredAt;
    private LocalDateTime protectionUntil;
    private LocalDateTime lastActivityAt;

    // ABAC Security: Territory-basierte Zugriffskontrolle
    @Column(name = "territory", nullable = false, length = 50)
    private String territory;

    // User-spezifische Settings
    @ManyToOne
    @JoinColumn(name = "user_settings_id")
    private UserLeadSettings userSettings;
}

// UserLeadSettings für individuelle Konfiguration
@Entity
@Table(name = "user_lead_settings")
public class UserLeadSettings {
    @Id private UUID id;
    @Column(unique = true) private UUID userId;

    // Zeiträume (Defaults aus Handelsvertretervertrag)
    private Integer defaultProtectionMonths = 6;       // §2(8)a
    private Integer defaultActivityCheckDays = 60;     // §2(8)b
    private Integer defaultGracePeriodDays = 10;       // §2(8)c

    // Provisionen (Defaults aus Vertrag)
    private BigDecimal firstYearCommissionRate = new BigDecimal("0.07");  // 7%
    private BigDecimal followupYearCommissionRate = new BigDecimal("0.02"); // 2%
    private BigDecimal monthlyAdvanceAmount = new BigDecimal("2000.00");
}
```

**Success Criteria:**
- Lead-Entity mit allen Vertrags-Pflichtfeldern
- State-Machine mit 7 Zuständen funktional
- UserLeadSettings mit Default-Werten aus Handelsvertretervertrag
- Database-Constraints für Business-Rules aktiv

**Next:** → [Phase 2](#phase-2)

### Phase 2: Automatische Fristen-Engine + Timer-System (Woche 4-6)
**Goal:** Handelsvertretervertrag-konforme Fristen-Überwachung und automatische Reminder

**Actions:**
- [ ] Timer-Service für 60-Tage-Aktivitätschecks
- [ ] Reminder-Engine mit E-Mail-Integration
- [ ] Grace-Period-Timer (10 Kalendertage Nachfrist)
- [ ] Stop-the-Clock-Implementation mit Reason-Tracking
- [ ] Automatische Status-Transitions basierend auf Aktivität/Zeit
- [ ] Lead-Expiration-Jobs mit User-Settings-Integration

**Code Changes:**
```java
// Fristen-Engine mit Handelsvertretervertrag-Logic
@Service
public class LeadComplianceService {

    @Scheduled(fixedRate = 3600000) // Jede Stunde
    public void checkLeadCompliance() {
        List<Lead> activeLeads = leadRepository.findByStatusIn(
            Arrays.asList(ACTIVE, REMINDER_SENT, GRACE_PERIOD)
        );

        for (Lead lead : activeLeads) {
            UserLeadSettings settings = getUserSettings(lead);
            LeadComplianceCheck check = calculateCompliance(lead, settings);

            if (check.requiresAction()) {
                processComplianceAction(lead, check);
            }
        }
    }

    private LeadComplianceCheck calculateCompliance(Lead lead, UserLeadSettings settings) {
        LocalDateTime now = LocalDateTime.now();
        Duration timeSinceLastActivity = Duration.between(lead.getLastActivityAt(), now);
        Duration effectiveTime = subtractStopClockPeriods(timeSinceLastActivity, lead);

        // 60-Tage-Check mit User-Settings
        if (effectiveTime.toDays() >= settings.getDefaultActivityCheckDays()) {
            return LeadComplianceCheck.REMINDER_REQUIRED;
        }

        return LeadComplianceCheck.COMPLIANT;
    }
}

// Stop-the-Clock mit Reason-Tracking
@Entity
public class StopClockPeriod {
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private StopClockReason reason; // FRESHFOODZ_DELAYS, CUSTOMER_CONSTRAINTS
    private String description;
}
```

**Success Criteria:**
- 60-Tage-Timer funktional mit User-Settings-Integration
- Automatische Reminder-Versendung
- Stop-the-Clock pausiert Fristen korrekt
- Grace-Period-Transitions nach 10 Tagen

**Next:** → [Phase 3](#phase-3)

### Phase 3: Aktivitäts-Integration + E-Mail-Kopplung (Woche 7-9)
**Goal:** Automatische Aktivitäts-Erkennung über E-Mail-System und manuelle Aktivitäts-Eingabe

**Actions:**
- [ ] LeadActivity-Entity für Handelsvertretervertrag-konforme Dokumentation
- [ ] E-Mail-Event-Listener für automatische Aktivitäts-Erkennung
- [ ] Activity-Types: QUALIFIED_CALL, CUSTOMER_REACTION, SCHEDULED_FOLLOWUP
- [ ] Manual Activity-Entry über Lead-Management-UI
- [ ] Activity-Timeline für Lead-Übersicht
- [ ] Integration mit Email-Thread-System für Context

**Code Changes:**
```java
// Lead-Activity mit Vertrags-konformen Types
@Entity
public class LeadActivity {
    @Id private UUID id;
    @ManyToOne private Lead lead;

    // Handelsvertretervertrag-konforme Aktivitäts-Typen (§2(8)b)
    @Enumerated(EnumType.STRING) private ActivityType type;
    private LocalDateTime occurredAt;
    private String description;

    // E-Mail-Integration
    private UUID emailThreadId; // Reference zu Email-System
    private UUID emailMessageId;

    // Automatische vs. manuelle Erfassung
    private ActivitySource source; // AUTO_EMAIL, MANUAL_ENTRY, SYSTEM_GENERATED
}

// E-Mail-Integration für automatische Aktivitäts-Erkennung
@EventListener
public class EmailLeadActivityListener {

    @Async
    public void handleEmailReceived(EmailReceivedEvent event) {
        Optional<Lead> matchedLead = leadMatchingService.findLeadByEmail(event);

        if (matchedLead.isPresent()) {
            LeadActivity activity = LeadActivity.builder()
                .lead(matchedLead.get())
                .type(CUSTOMER_REACTION)
                .occurredAt(event.getReceivedAt())
                .description("Customer email received: " + event.getSubject())
                .emailThreadId(event.getThreadId())
                .source(AUTO_EMAIL)
                .build();

            leadActivityService.recordActivity(activity);
            leadComplianceService.updateLeadStatus(matchedLead.get());
        }
    }
}
```

**Success Criteria:**
- E-Mail-Aktivitäten automatisch als Lead-Activity erfasst
- Manuelle Activity-Entry funktional
- Lead-Status aktualisiert sich bei neuer Aktivität
- Activity-Timeline zeigt vollständige Historie

**Next:** → [Phase 4](#phase-4)

### Phase 4: Provisions-Integration + Customer-Konvertierung (Woche 10-12)
**Goal:** Lead→Customer-Workflow mit Provisions-Berechnung und Cockpit-Integration

**Actions:**
- [ ] Lead→Customer-Konvertierung-Workflow
- [ ] Provisions-Berechnung mit UserLeadSettings (7%/2% individuell)
- [ ] 12-Monats-Neukunden-Check (Handelsvertretervertrag-konform)
- [ ] Opportunity-Integration für Sales-Pipeline
- [ ] Cockpit-Events für lead.status.changed + lead.converted
- [ ] Commission-Tracking-Entity für Provisions-Verwaltung

**Code Changes:**
```java
// Lead→Customer-Konvertierung mit Provisions-Logic
@Service
public class LeadConversionService {

    public CustomerConversionResult convertLeadToCustomer(Lead lead, ConversionRequest request) {
        // Handelsvertretervertrag-konforme Neukunden-Prüfung
        boolean isNewCustomer = customerHistoryService
            .hasNoRevenueInLast12Months(request.getCustomerData());

        if (!isNewCustomer) {
            throw new LeadConversionException("Customer had revenue in last 12 months - not eligible for new customer commission");
        }

        // Customer-Entity erstellen
        Customer customer = customerService.createFromLead(lead, request);

        // Commission-Tracking mit User-Settings
        UserLeadSettings settings = lead.getUserSettings();
        CommissionTracking commission = CommissionTracking.builder()
            .lead(lead)
            .customer(customer)
            .firstYearRate(settings.getFirstYearCommissionRate())
            .followupYearRate(settings.getFollowupYearCommissionRate())
            .status(PENDING_FIRST_REVENUE)
            .build();

        commissionService.create(commission);

        // Lead-Status auf CONVERTED
        lead.setStatus(CONVERTED);
        lead.setConvertedAt(LocalDateTime.now());
        leadRepository.save(lead);

        // Cockpit-Event
        eventPublisher.publishEvent(new LeadConvertedEvent(lead, customer));

        return CustomerConversionResult.success(customer, commission);
    }
}

// Commission-Tracking für Provisions-Verwaltung
@Entity
public class CommissionTracking {
    @Id private UUID id;
    @OneToOne private Lead lead;
    @OneToOne private Customer customer;

    // User-spezifische Provision-Rates
    private BigDecimal firstYearRate;
    private BigDecimal followupYearRate;

    // Provisions-Status
    private CommissionStatus status; // PENDING_FIRST_REVENUE, ACTIVE, EXPIRED
    private LocalDateTime firstRevenueAt;
    private BigDecimal totalCommissionEarned;
}
```

**Success Criteria:**
- Lead→Customer-Konvertierung mit 12-Monats-Check
- Provisions-Berechnung mit individuellen User-Settings
- Commission-Tracking für alle konvertierten Leads
- Cockpit-Events für Real-time Updates

**Next:** → [Kampagnen Integration](../kampagnen/technical-concept.md)

## ✅ Success Metrics

**Quantitative:**
- Handelsvertretervertrag-Compliance: 100% (State-Machine + Fristen)
- Aktivitäts-Erkennung: >90% E-Mails automatisch als Activity erfasst
- Timer-Accuracy: ±1 Stunde für 60-Tage/10-Tage-Checks
- API Response Time: P95 <200ms (Lead-Endpoints)
- Lead→Customer-Konvertierung: >95% Success-Rate

**Qualitative:**
- UserLeadSettings ermöglichen individuelle Provisionen/Zeiträume
- Stop-the-Clock pausiert Fristen rechtskonform
- E-Mail-Integration aktualisiert Lead-Status automatisch
- Cockpit zeigt Lead-Timeline mit allen Aktivitäten
- TestDataBuilder ersetzt alle Lead-Management-Mocks

**Timeline:**
- Phase 1-2: Woche 1-6 (Entity + State-Machine + Timer)
- Phase 3-4: Woche 7-12 (E-Mail-Integration + Provisions)
- Production-Ready: Ende Woche 12

## 📋 Test Migration Instructions

> **🚀 MIGRATION HINWEIS für Production:**
> Bei Production-Start müssen alle Tests aus `/docs/planung/features-neu/02_neukundengewinnung/`
> in die neue Enterprise Test-Struktur migriert werden:
> - Unit Tests → `/backend/src/test/java/unit/leads/` bzw. `/frontend/src/tests/unit/leads/`
> - Integration Tests (z.B. `LeadRepositoryIntegrationTest.java`) → `/backend/src/test/java/integration/leads/`
> - Performance Tests (z.B. `k6_lead_api_performance.js`) → `/backend/src/test/java/performance/leads/`
> Siehe [TEST_STRUCTURE_PROPOSAL.md](../../../features/TEST_STRUCTURE_PROPOSAL.md) für Details.

## 🔗 Related Documentation

**Foundation Knowledge:**
- **Business Requirements:** → [Handelsvertretervertrag Requirements](../diskussionen/2025-09-18_handelsvertretervertrag-lead-requirements.md)
- **Database Engineering:** → [KI Production Specs](../diskussionen/2025-09-18_finale-ki-specs-bewertung.md)
- **State Machine Patterns:** → [STATE_MACHINE_STANDARDS.md](../../grundlagen/STATE_MACHINE_STANDARDS.md)

**Foundation Standards Artefakte:**

**Lead-Erfassung Backend:**
- `backend/LeadEntity.java` - JPA Entity mit Foundation Standards JavaDoc
- `backend/LeadService.java` - Business Logic mit ABAC Territory Enforcement
- `backend/LeadResource.java` - REST Controller mit Performance SLOs
- `backend/LeadRepository.java` - Data Access mit optimierten Queries
- `backend/LeadStatus.java` - Enum für Lead-Status State Machine
- `backend/LeadDTO.java` - Data Transfer Objects
- `backend/LeadScoringService.java` - Lead-Scoring Algorithmus
- `backend/LeadExportAdapter.java` - Universal Export Integration

**Lead-Erfassung Database:**
- `database/VXXX__create_lead_table.sql` - Performance-optimiertes Schema (Nummer via ./scripts/get-next-migration.sh)

**Lead-Erfassung API:**
- `api/lead-management.api.json` - OpenAPI 3.1 mit Foundation References

**Lead-Erfassung Tests:**
- `tests/LeadResourceTest.java` - BDD Integration Tests mit ABAC Validation
- `tests/LeadResourceABACIT.java` - ABAC Security Integration Tests
- `tests/lead-campaign-e2e.test.ts` - End-to-End Workflow Tests

**Lead-Erfassung Frontend:**
- `frontend/smartlayout.lead-form.json` - SmartLayout Configuration

**Shared Components (modulübergreifend):**
- `../shared/security/SecurityScopeFilter.java` - ABAC Request Filter
- `../shared/security/ScopeContext.java` - Territory-basierte Security Context
- `../shared/frontend/theme-v2.mui.ts` - FreshFoodz Theme V2 (#94C456, #004F7B)
- `../shared/frontend/theme-v2.tokens.css` - CSS Design Tokens
- `../shared/frontend/ThemeV2Compliance.test.ts` - Theme Compliance Tests
- `../shared/common/ProblemExceptionMapper.java` - Exception Handling

**KI-Production-Specs (verfügbar in Email-Posteingang):**
- **UserLeadSettings-Schema:** `../email-posteingang/database/V20250918_01_core_defaults_constraints.sql`
- **Event-Schema:** `../email-posteingang/events/cockpit-event-schema.json` (lead.status.changed)
- **Database-Constraints:** Exclusion-Constraints für Stop-Clock, Performance-Indizes
- **Production-Config:** Timer-Intervalle und Lead-Compliance-Jobs

**Related Plans:**
- **Dependencies:** → [Email-Posteingang Technical Concept](../email-posteingang/technical-concept.md)
- **Follow-ups:** → [Kampagnen Technical Concept](../kampagnen/technical-concept.md)
- **Integration:** → [Mock Replacement Strategy](../../infrastruktur/MOCK_REPLACEMENT_STRATEGY_PLAN.md)

## 🤖 Claude Handover Section

**Foundation Standards Status:** ✅ 92% Compliance (wie Modul 04)

**Für nächsten Claude:**

**Aktueller Stand:**
Technical Concept für Lead-Erfassung vollständig mit Foundation Standards aktualisiert. Alle 8 Foundation Standards Artefakte verfügbar: JPA Entity, ABAC Security, OpenAPI 3.1, Theme V2 Integration, Performance Tests, CI/CD Workflows. Handelsvertretervertrag-Requirements vollständig integriert (6/60/10-Regelung).

**Foundation Standards Artefakte bereit:**
1. **Lead Backend Services** - 8 Java-Klassen mit Foundation Standards JavaDoc
2. **Security Integration** - SecurityScopeFilter.java + ScopeContext.java für ABAC
3. **Database Schema** - VXXX__create_lead_table.sql mit Performance-Optimierung (Nummer via Scripts ermitteln)
4. **API Specification** - lead-management.api.json (OpenAPI 3.1)
5. **Frontend Integration** - Theme V2 + SmartLayout + CSS Design Tokens
6. **Test Coverage** - 3 Test-Suiten (Unit, Integration, E2E) mit BDD Pattern
7. **Shared Components** - 6 modulübergreifende Komponenten für Consistency
8. **Universal Export** - LeadExportAdapter.java für alle Export-Formate

**Nächster konkreter Schritt:**
1. **Implementation Phase 1 starten** mit vorhandenen Foundation Standards Artefakten
2. **LeadEntity.java deployen** - Alle DB Constraints und Performance-Indizes
3. **ABAC Security aktivieren** - Territory-basierte Zugriffskontrolle via JWT Claims

**Wichtige Dateien für Context:**
- `../email-posteingang/database/` - **UserLeadSettings + Constraints bereits implementiert**
- `../email-posteingang/events/` - **Event-Schema für lead.status.changed verfügbar**
- `diskussionen/2025-09-18_handelsvertretervertrag-lead-requirements.md` - **Detaillierte Business-Logic (6/60/10-Regelung)**
- `lead-erfassung/Handelsvertretervertrag.pdf` - Original-Vertrag (§2(8))
- `backend/modules/customer/core/src/main/java/` - Bestehende Customer-Module-Struktur

**Offene Entscheidungen:**
- Beginne Lead-Implementation oder erstelle Kampagnen Technical Concept?
- UserLeadSettings-Defaults aus Handelsvertretervertrag vs. System-Config?
- Stop-the-Clock-UI für Sales-Team oder nur Admin-Interface?

**Kontext-Links:**
- **Grundlagen:** → [Planungsmethodik](../../PLANUNGSMETHODIK.md)
- **Business Logic:** → [Handelsvertretervertrag Requirements](../diskussionen/2025-09-18_handelsvertretervertrag-lead-requirements.md)
- **Database Specs:** → [KI Production Specs](../diskussionen/2025-09-18_finale-ki-specs-bewertung.md)