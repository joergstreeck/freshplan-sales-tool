# Mock Replacement Strategy - Infrastructure Plan

**📊 Plan Status:** ✅ Foundation Ready - TestDataBuilder vorhanden
**🎯 Owner:** Development Team + QA Team
**⏱️ Timeline:** Q4 2025 → Q1 2026
**🔧 Effort:** M (koordiniert mit Test Debt Recovery)

## 🎯 Executive Summary (für Claude)

**Mission:** Systematische Elimination aller Mock-Dependencies durch echte TestDataBuilder-Integration
**Problem:** Frontend-Mocks und Test-Mocks fragmentieren Development-Experience und verstecken Integration-Issues
**Solution:** Koordinierte Mock-Replacement mit bereits vorhandenem TestDataBuilder-System
**Timeline:** 8 Wochen parallel zu Test Debt Recovery
**Impact:** Von Mock-abhängiger zu produktionsnaher Development-Experience

**Quick Context:** TestDataBuilder-Infrastructure (6 Builder, zentrale Facade) bereits implementiert und produktiv. Frontend zeigt Mock-KPIs statt echter Daten. Koordinierte Replacement-Strategie verbindet Backend-Test-Modernisierung mit Frontend-Reality.

## 📋 Context & Dependencies

### Current State:
- **✅ TestDataBuilder-System vorhanden:** 6 Builder (Customer, Contact, Opportunity, User, etc.)
- **🔴 Frontend-Mock-Pollution:** Dashboard-KPIs, Triage-Inbox, Lead-Data alle Mock-basiert
- **🔴 Test-Mock-Dependencies:** 97 Tests in "migrate" Status durch SEED/Mock-Abhängigkeiten
- **🔴 API-Mock-Gaps:** Unterschied zwischen Mock-Responses und echten Backend-APIs

### Target State:
- **100% TestDataBuilder-Integration:** Alle Tests nutzen Builder statt Mocks
- **Frontend-Reality:** Dashboards zeigen echte API-Daten statt Mock-KPIs
- **End-to-End-Consistency:** Development bis Production nutzt gleiche Datenstrukturen
- **Mock-Free-Development:** Entwickler arbeiten mit produktionsnahen Daten

### Dependencies:
→ [Test Debt Recovery Plan](./TEST_DEBT_RECOVERY_PLAN.md) - TestDataBuilder-Migration koordiniert
→ [Performance Standards](../grundlagen/PERFORMANCE_STANDARDS.md) - API-Response-Benchmarks
→ [Development Workflow](../grundlagen/DEVELOPMENT_WORKFLOW.md) - CI Integration ohne Mocks

## 🛠️ Implementation Phases

### Phase 1: TestDataBuilder-System Assessment (Woche 1-2)
**Goal:** Vollständige Dokumentation der bereits vorhandenen TestDataBuilder-Capabilities

**Actions:**
- [ ] Audit aller 6 TestDataBuilder (Customer, Contact, Opportunity, User, Timeline, ContactInteraction)
- [ ] Dokumentation der Builder-Patterns und Fluent-APIs
- [ ] Integration-Test zwischen Builder-System und echten Entities
- [ ] Performance-Benchmarking der TestDataBuilder vs. Mock-Creation
- [ ] Create TestDataBuilder-Usage-Guidelines für Frontend-Integration

**Code Analysis:**
```java
// BEREITS VORHANDEN - Assessment dokumentieren:
@ApplicationScoped
public class TestDataBuilder {
    @Inject private CustomerBuilder customerBuilder;
    @Inject private ContactBuilder contactBuilder;
    // ... 6 Builder komplett implementiert

    // Fluent API bereits functional:
    public CustomerBuilder customer() { return customerBuilder.reset(); }
}

// CustomerBuilder Capabilities:
Customer customer = testData.customer()
    .asPremiumCustomer()
    .withCompanyName("FreshFoodz Test GmbH")
    .withIndustry(Industry.RESTAURANT)
    .persist(); // Transactional DB save
```

**Success Criteria:**
- Vollständige Dokumentation aller Builder-Capabilities
- Performance-Baseline für Builder vs. Mock-Creation
- Integration-Guidelines für Frontend-Backend-API-Testing
- Builder-Coverage-Matrix (welche Entities/Scenarios abgedeckt)

**Next:** → [Phase 2](#phase-2)

### Phase 2: Frontend Mock-Replacement (Woche 3-6)
**Goal:** Schrittweise Replacement von Frontend-Mocks durch echte Backend-APIs mit TestDataBuilder

**Actions:**
- [ ] Neukundengewinnung-Dashboard: Mock-KPIs durch echte API-Endpoints ersetzen
- [ ] Triage-Inbox: Mock-TriageItems durch TestDataBuilder-generierte E-Mail-Daten
- [ ] Cockpit-Mock-Data: MyDayColumn echte Tasks von TestDataBuilder-Scenarios
- [ ] Lead-Pipeline: Mock-Lead-Data durch Customer-Entity-basierte Lead-Simulation
- [ ] Campaign-Metrics: TestDataBuilder-Customer für realistische Campaign-Testing

**Frontend-Backend-Integration:**
```typescript
// VORHER (Mock-basiert):
import { mockTriageItems } from '../data/mockData';

// NACHHER (TestDataBuilder-Integration):
import { emailApi } from '../services/emailApi';

// API nutzt TestDataBuilder für Development-Daten:
const { data: emailData } = useQuery({
  queryKey: ['triage-emails'],
  queryFn: () => emailApi.getTriageEmails()
  // Backend generiert mit TestDataBuilder.customer().withEmailHistory()
});
```

**Koordination mit Backend:**
```java
// Neue Development-Endpoints für Frontend:
@Path("/api/dev/triage-emails")
public class DevelopmentTriageResource {

    @Inject TestDataBuilder testData;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<EmailTriageItem> getTriageEmails() {
        // TestDataBuilder für realistische Development-Daten
        return testData.customer()
            .withEmailHistory()
            .generateTriageScenario();
    }
}
```

**Success Criteria:**
- Neukundengewinnung-Dashboard zeigt echte Backend-Daten
- Frontend-Tests laufen gegen echte APIs statt Mocks
- Development-Experience produktionsnah ohne Mock-Fragmentation
- Performance-Parity zwischen Mock und TestDataBuilder-APIs

**Next:** → [Phase 3](#phase-3)

### Phase 3: Test-Mock-Elimination (Woche 7-8)
**Goal:** Parallel zu Test Debt Recovery alle Test-Mocks durch TestDataBuilder ersetzen

**Actions:**
- [ ] Koordination mit Test Debt Recovery Plan für 97 "migrate" Tests
- [ ] SEED-Dependency-Elimination durch TestDataBuilder-Migration
- [ ] Mock-Framework-Cleanup (entferne @Mock annotations wo ersetzbar)
- [ ] TestDataBuilder-based Integration-Test-Suite
- [ ] Create Test-Performance-Benchmarks (Builder vs. Mock vs. SEED)

**Test-Migration-Pattern:**
```java
// VORHER (Mock-abhängig):
@Test
void testCustomerCreation() {
    @Mock Customer mockCustomer;
    when(mockCustomer.getName()).thenReturn("Test Company");
    // Mock-Behavior statt echte Business-Logic
}

// NACHHER (TestDataBuilder-basiert):
@Test
void testCustomerCreation() {
    @Inject TestDataBuilder testData;

    Customer realCustomer = testData.customer()
        .withCompanyName("Test Company")
        .build(); // Echte Entity mit echter Business-Logic

    // Test läuft gegen echte Domain-Logic
}
```

**Success Criteria:**
- 97 "migrate" Tests erfolgreich auf TestDataBuilder migriert
- Zero Mock-Dependencies für Core-Business-Logic-Tests
- Test-Execution-Performance ≥ Mock-Performance
- Test-Reliability durch echte Entity-Behavior verbessert

**Next:** → [Phase 4](#phase-4)

### Phase 4: Mock-Free-Development-Environment (Woche 9-10)
**Goal:** Komplette Mock-Elimination aus Development-Workflow

**Actions:**
- [ ] Create Mock-Detection-Automation (CI-Gate gegen neue Mock-Introduction)
- [ ] Development-Database-Seeding mit TestDataBuilder statt SEED-Files
- [ ] Frontend-Development-Server mit TestDataBuilder-Backend-Integration
- [ ] Documentation: Mock-Free-Development-Guidelines
- [ ] Team-Training: TestDataBuilder-Patterns und Best-Practices

**Mock-Detection-Automation:**
```yaml
# .github/workflows/mock-detection.yml
- name: Detect Mock Dependencies
  run: |
    # Scan für Mock-Imports in Tests
    grep -r "import.*mock" src/test/ && exit 1

    # Scan für Mock-Data in Frontend
    grep -r "mockData" frontend/src/ && exit 1

    # Enforce TestDataBuilder-Usage
    grep -r "TestDataBuilder" src/test/ || exit 1
```

**Development-Environment:**
```java
// Development-Seeding statt SEED-Files:
@ApplicationScoped
public class DevelopmentDataSeeder {

    @Inject TestDataBuilder testData;

    @EventObserver
    void onStartup(@Observes StartupEvent ev) {
        if (isDevMode()) {
            seedDevelopmentData();
        }
    }

    private void seedDevelopmentData() {
        // Realistische Entwicklungsdaten mit TestDataBuilder
        testData.scenario()
            .createCompleteCustomerScenario()
            .withOpportunityPipeline()
            .withEmailHistory()
            .execute();
    }
}
```

**Success Criteria:**
- Zero Mock-Dependencies in Codebase
- CI-Gates verhindern neue Mock-Introduction
- Development-Environment läuft komplett mit TestDataBuilder-Daten
- Team kann Mock-free entwickeln mit produktionsnaher Experience

## ✅ Success Metrics

**Quantitative:**
- **Mock-Elimination-Rate:** 100% Frontend-Mocks entfernt
- **Test-Mock-Reduction:** 97 Tests von Mock zu TestDataBuilder migriert
- **Development-Performance:** TestDataBuilder-APIs ≤110% Mock-Performance
- **CI-Reliability:** Zero Mock-related Test-Failures
- **Code-Simplicity:** -50% Mock-Setup-Code

**Qualitative:**
- Entwickler arbeiten mit produktionsnahen Daten
- Frontend-Backend-Integration-Issues frühzeitig erkannt
- Test-Reliability durch echte Business-Logic verbessert
- Onboarding neuer Entwickler vereinfacht (keine Mock-Komplexität)

**Timeline:**
- Phase 1: Woche 1-2 (TestDataBuilder Assessment)
- Phase 2: Woche 3-6 (Frontend Mock-Replacement)
- Phase 3: Woche 7-8 (Test Mock-Elimination, parallel zu Test Debt Recovery)
- Phase 4: Woche 9-10 (Mock-Free Environment)

## 🔗 Related Documentation

**Foundation Knowledge:**
- **TestDataBuilder-System:** → [/backend/src/main/java/de/freshplan/test/TestDataBuilder.java](../../backend/src/main/java/de/freshplan/test/TestDataBuilder.java)
- **Builder-Patterns:** → [/backend/src/main/java/de/freshplan/test/builders/](../../backend/src/main/java/de/freshplan/test/builders/)
- **Development Guidelines:** → [../grundlagen/DEVELOPMENT_WORKFLOW.md](../grundlagen/DEVELOPMENT_WORKFLOW.md)

**Implementation Details:**
- **CustomerBuilder:** → [CustomerBuilder.java](../../backend/src/main/java/de/freshplan/test/builders/CustomerBuilder.java) (12.5KB, vollständig)
- **TestData Factories:** → [/backend/src/test/java/de/freshplan/test/builders/](../../backend/src/test/java/de/freshplan/test/builders/)
- **Frontend Mock-Locations:** → [/frontend/src/features/cockpit/data/mockData.ts](../../frontend/src/features/cockpit/data/mockData.ts)

**Related Plans:**
- **Dependencies:** → [Test Debt Recovery Plan](./TEST_DEBT_RECOVERY_PLAN.md) (koordinierte TestDataBuilder-Migration)
- **Follow-ups:** → [Performance Optimization](./PERFORMANCE_OPTIMIZATION_PLAN.md) (TestDataBuilder-Performance)
- **Integration:** → [Neukundengewinnung Analysis](../features-neu/02_neukundengewinnung/analyse/2025-09-18_code-analyse-neukundengewinnung.md)

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Mock Replacement Strategy Plan erstellt basierend auf bereits vorhandenem TestDataBuilder-System. 6 Builder (Customer, Contact, Opportunity, etc.) sind produktiv implementiert und ready-to-use.

**Nächster konkreter Schritt:**
Phase 1 starten - Assessment der TestDataBuilder-Capabilities. Dokumentiere vollständige API aller 6 Builder und erstelle Integration-Guidelines für Frontend-Backend-Mock-Replacement.

**Wichtige Dateien für Context:**
- `/backend/src/main/java/de/freshplan/test/TestDataBuilder.java` - Zentrale Facade (bereits implementiert)
- `/backend/src/main/java/de/freshplan/test/builders/CustomerBuilder.java` - Umfassendster Builder (12.5KB)
- `/frontend/src/features/cockpit/data/mockData.ts` - Zu ersetzende Frontend-Mocks

**Offene Entscheidungen:**
- Soll Frontend-Mock-Replacement mit Neukundengewinnung-Implementation koordiniert werden?
- Performance-Benchmarks: Wie viel Overhead ist für TestDataBuilder vs. Mocks akzeptabel?
- Development-Database-Seeding: Automatisch bei Startup oder manuell trigger?

**Kontext-Links:**
- **Grundlagen:** → [Test Debt Recovery für koordinierte Migration](./TEST_DEBT_RECOVERY_PLAN.md)
- **Dependencies:** → [Neukundengewinnung-Analyse zeigt Frontend-Mock-Problematik](../features-neu/02_neukundengewinnung/analyse/2025-09-18_code-analyse-neukundengewinnung.md)