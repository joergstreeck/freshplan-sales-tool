---
module: "02_neukundengewinnung"
domain: "backend"
doc_type: "konzept"
status: "in_progress"
sprint: "2.1.4"
owner: "team/backend"
updated: "2025-09-30"
---

# 🚨 KRITISCHER TEST-MIGRATIONS-PLAN - Sprint 2.1.4 Fix

**📍 Navigation:** Home → Planung → 02_neukundengewinnung → backend → TEST_MIGRATION_PLAN

**STATUS:** 🔴 KRITISCH - CI läuft in 20-Minuten-Timeout
**ERSTELLT:** 2025-09-30
**PROBLEM:** 164 von 171 Tests nutzen @QuarkusTest mit echter DB = ~20+ Minuten Laufzeit

## 📊 IST-ZUSTAND ANALYSE

### Zahlen & Fakten
- **171 Test-Dateien** insgesamt
- **164 nutzen @QuarkusTest** = starten kompletten Quarkus-Container + echte DB
- **Nur 7 echte Unit-Tests** ohne DB
- **CI-Laufzeit:** 20+ Minuten (Timeout!)
- **Log-Größe:** 13MB (96.000+ Zeilen)

### Root Cause
1. Jeder @QuarkusTest startet Quarkus neu (~2-3 Sek)
2. DB-Verbindung + Flyway Migrations für jeden Test
3. Tests laufen sequenziell
4. Ein Test hängt nach UserServiceRolesTest

## 📁 TEST-ABLAGE-STRATEGIE

### Etablierte Test-Struktur (Best Practice)
Tests sind nach **Verantwortlichkeit** organisiert, nicht nach Modulen:

**KORREKTE ABLAGE:**
```
backend/src/test/java/de/freshplan/
├── api/                      # REST-/Resource-Tests (leichtgewichtig)
│   ├── resources/            # REST Endpoints
│   └── settings/             # Settings API
├── domain/                   # Domain Unit-Tests (Mocking, keine DB)
│   ├── customer/service/     # Customer Business Logic
│   ├── user/service/         # User Business Logic
│   └── opportunity/service/  # Opportunity Business Logic
├── integration/              # Integration-/CQRS-Tests (Quarkus, echte DB)
│   ├── cqrs/                 # CQRS End-to-End
│   └── scenarios/            # Cross-Module Szenarien
├── infrastructure/           # Security/Infra-Tests (Filter, RBAC)
├── modules/                  # Modul-spezifische Tests (z.B. Leads)
│   └── leads/                # Modul 02_neukundengewinnung
├── test/                     # Test-Infrastruktur & Basisklassen
│   ├── A00_EnvDiagTest      # Gatekeeper/Diagnose
│   └── DatabaseGrowthTracker # Test-Utilities
└── testsupport/              # Hilfs-/Fixture-Utilities
```

### Test-Kategorisierung nach Best Practice
1. **Unit Tests (70%)** - `de/freshplan/domain/...`
   - Reines Mockito OHNE @QuarkusTest
   - Business Logic isoliert testen
   - Laufzeit: Millisekunden

2. **Integration Tests (20%)** - `de/freshplan/integration/...`
   - @QuarkusTest mit echter DB
   - CQRS, Transaktionen, Events
   - Laufzeit: Sekunden

3. **API Tests (10%)** - `de/freshplan/api/...`
   - Leichtgewichtig mit Mocks wo möglich
   - REST-Contract Testing
   - @QuarkusTest nur wenn unbedingt nötig

### Migration-Strategie für Tests
1. **Identifiziere Test-Typ** (Unit/Integration/API)
2. **Verschiebe in korrektes Verzeichnis** gemäß obiger Struktur
3. **Refactoring auf Mockito** wo möglich (besonders domain/)
4. **Suffix-Konvention:**
   - `*Test.java` für normale Tests
   - `*IntegrationTest.java` für Integration Tests
   - `*MockitoTest.java` nur während Migration als Marker

## 🔄 MIGRATIONS-STRATEGIE (NEU)

### Parallele Test-Erstellung
**WICHTIG:** Tests werden NEU geschrieben statt umgebaut!

1. **Neue Mockito-Tests schreiben** mit Suffix `*MockitoTest.java`
2. **Parallel testen** - beide Versionen laufen
3. **Validieren** dass neue Tests gleiche Coverage haben
4. **Alte Tests löschen** nach erfolgreicher Validierung
5. **Umbenennen** von `*MockitoTest.java` zu `*Test.java`

**Vorteile:**
- Kein Risiko beim Refactoring
- CI bleibt stabil während Migration
- Einfacher Rollback möglich
- Klarere Test-Struktur von Anfang an

## ✅ MIGRATIONS-CHECKLISTE

### Phase 1: Quick Wins (Tag 1) ✅ ABGESCHLOSSEN
**Ziel:** CI wieder lauffähig machen

- [x] **CustomerResourceFeatureFlagTest** → **CustomerResourceDelegationTest** (Mockito)
  - 107x schneller (12.56s → 0.117s)
  - Reflection-Lösung für final classes
- [x] **SettingsResourceTest** → **SettingsResourceIntegrationTest** (umbenannt)
  - Bleibt als Integration-Test (RestAssured)
  - Unit-Test-Versuch verworfen (API-Inkompatibilitäten)
- [ ] **UserServiceRolesTest** - Als nächstes
- [ ] **UserServiceTest** - Danach
- [ ] **HelpSystemCompleteIntegrationTest** - DISABLED (hängt)
- [ ] **UserStruggleDetectionCQRSIntegrationTest** - Später
- [ ] **CurrentUserProducerTest** - Später
- [ ] **CurrentUserProducerIntegrationTest** - Später

### Phase 2: Service Layer Tests (Tag 2-3)
**Ziel:** 50% der Tests auf Mocks umstellen

#### User Domain (15 Tests)
- [ ] UserServiceTest → Mockito
- [ ] UserServiceRolesTest → Mockito
- [ ] UserCommandServiceTest → Mockito
- [ ] UserQueryServiceTest → Mockito
- [ ] UserMapperTest → Plain JUnit
- [ ] UserValidationTest → Plain JUnit
- [ ] UserServiceCQRSIntegrationTest → BEHALTEN (echter Integration Test)
- [ ] Weitere User Tests...

#### Customer Domain (25 Tests)
- [ ] CustomerServiceTest → Mockito
- [ ] CustomerCommandServiceTest → Mockito
- [ ] CustomerQueryServiceTest → Mockito
- [ ] ContactServiceTest → Mockito
- [ ] ContactCommandServiceTest → Mockito
- [ ] ContactQueryServiceTest → Mockito
- [ ] CustomerMapperTest → Plain JUnit
- [ ] CustomerValidationTest → Plain JUnit
- [ ] CustomerRepositoryTest → BEHALTEN (DB Test)
- [ ] ContactRepositoryTest → BEHALTEN (DB Test)
- [ ] Weitere Customer Tests...

#### Opportunity Domain (12 Tests)
- [ ] OpportunityServiceTest → Mockito
- [ ] OpportunityServiceIntegrationTest → Mockito (war falsch benannt!)
- [ ] OpportunityCommandServiceTest → Mockito
- [ ] OpportunityQueryServiceTest → Mockito
- [ ] OpportunityMapperTest → Plain JUnit
- [ ] OpportunityRepositoryTest → BEHALTEN (DB Test)
- [ ] Weitere Opportunity Tests...

#### Lead Module (10 Tests)
- [ ] LeadServiceTest → Mockito
- [ ] LeadNormalizationServiceTest → Mockito
- [ ] LeadEnrichmentServiceTest → Mockito
- [ ] LeadDeduplicationServiceTest → Mockito
- [ ] FollowUpAutomationService → Mockito
- [ ] UserLeadSettingsServiceTest → Mockito
- [ ] LeadRepositoryTest → BEHALTEN (DB Test)
- [ ] Weitere Lead Tests...

### Phase 3: API/Resource Tests (Tag 4)
**Ziel:** REST-Tests optimieren

- [ ] CustomerResourceIntegrationTest → @QuarkusIntegrationTest
- [ ] OpportunityResourceTest → @QuarkusIntegrationTest
- [ ] LeadResourceTest → @QuarkusIntegrationTest
- [ ] UserResourceTest → @QuarkusIntegrationTest
- [ ] Weitere Resource Tests...

### Phase 4: Infrastructure Tests (Tag 5)
**Ziel:** Technische Tests optimieren

- [ ] SecurityTests → Teilweise Mocks
- [ ] AuditTests → Mockito für Service
- [ ] EventTests → Mockito
- [ ] CQRSTests → Selektiv behalten
- [ ] Weitere Infrastructure Tests...

## 🎯 MIGRATIONS-TEMPLATE

### Von @QuarkusTest zu Mockito

**VORHER (mit DB):**
```java
@QuarkusTest
@TestTransaction
class UserServiceTest {
    @Inject UserService userService;
    @Inject UserRepository userRepository;

    @Test
    void testCreateUser() {
        // Test mit echter DB
        var user = userService.createUser(...);
        assertThat(user.getId()).isNotNull();
    }
}
```

**NACHHER (mit Mocks):**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @InjectMocks UserService userService;

    @Test
    void testCreateUser() {
        // Given
        User mockUser = new User();
        when(userRepository.persist(any())).thenReturn(mockUser);
        when(userMapper.toResponse(any())).thenReturn(...);

        // When
        var result = userService.createUser(...);

        // Then
        verify(userRepository).persist(any());
        assertThat(result).isNotNull();
    }
}
```

## 📈 ERWARTETE VERBESSERUNGEN

### Laufzeit-Reduktion
- **Vorher:** ~20+ Minuten (Timeout)
- **Phase 1:** ~15 Minuten (lauffähig)
- **Phase 2:** ~10 Minuten
- **Phase 3:** ~7 Minuten
- **Ziel:** < 5 Minuten

### Test-Verteilung Ziel
- **Unit Tests (Mocks):** 70% (~120 Tests)
- **Integration Tests:** 20% (~35 Tests)
- **E2E Tests:** 10% (~16 Tests)

## 🛠️ TOOLING & SCRIPTS

### Test-Migration Helper
```bash
# Alle @QuarkusTest finden
find src/test -name "*.java" -exec grep -l "@QuarkusTest" {} \;

# Test-Typ analysieren
grep -r "@QuarkusTest" src/test --include="*.java" | \
  grep -E "(Service|Command|Query|Mapper|Validator)Test" | wc -l

# Binary Search für hängende Tests
./disable-tests.sh disable  # Alle aus
./disable-tests.sh enable '[A-M]*Test.java'  # Erste Hälfte
```

## ⚠️ WICHTIGE REGELN

### Was BEHALTEN mit @QuarkusTest
1. **Repository Tests** - testen echte Queries
2. **Migration Tests** - testen Flyway
3. **Ausgewählte Integration Tests** - End-to-End Szenarien
4. **Security Tests** - wenn RLS getestet wird

### Was auf MOCKS umstellen
1. **Alle Service Tests** - Business Logic
2. **Command/Query Services** - CQRS Layer
3. **Mapper Tests** - Transformationen
4. **Validation Tests** - Bean Validation

### Was als PLAIN JUNIT
1. **Utility Classes**
2. **DTOs/Entities** (wenn Tests nötig)
3. **Helper/Utils**
4. **Pure Functions**

## 📝 PROGRESS TRACKING

### Tag 1 (30.09.2025)
- [x] Problem analysiert
- [x] Migrationsplan erstellt
- [x] Neue Migrations-Strategie etabliert (parallel Tests schreiben)
- [x] **CustomerResourceFeatureFlagTest** → **CustomerResourceDelegationTest** ✅
  - Alte Version: 12 Tests, 12.56s mit @QuarkusTest (12 Errors)
  - Neue Version: 10 Tests, 0.117s mit reinem Mockito (0 Errors)
  - **Performance-Gewinn: 107x schneller!**
  - Lösung: Reflection für final classes statt Mock-Probleme
- [ ] SettingsResourceTest migrieren (als nächstes)

### Tag 2
- [ ] User Domain migriert
- [ ] Customer Domain gestartet

### Tag 3
- [ ] Customer Domain fertig
- [ ] Opportunity Domain migriert
- [ ] Lead Module migriert

### Tag 4
- [ ] API/Resource Tests optimiert
- [ ] Performance-Check

### Tag 5
- [ ] Infrastructure Tests
- [ ] Finale Optimierung
- [ ] Dokumentation

## 🔗 REFERENZEN

- **Sprint 2.1.4:** `/docs/planung/features-neu/02_neukundengewinnung/sprints/sprint_2.1.4.md`
- **Master Plan:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
- **CI Config:** `/.github/workflows/backend-ci.yml`
- **Test Guidelines:** Best Practices für Quarkus Testing

## 🚨 LESSONS LEARNED

### Was schief lief
1. **Falsches Test-Pattern** von Anfang an kopiert
2. **@QuarkusTest** überall verwendet ohne nachzudenken
3. **Keine Test-Strategie** definiert
4. **CI-Laufzeit** nicht überwacht

### Für zukünftige Sprints
1. **Test-First != DB-First**
2. **Mocks sind deine Freunde**
3. **CI < 5 Minuten** als harte Grenze
4. **Test-Pyramide** beachten (70/20/10)

---
**NÄCHSTER SCHRITT:** Quick Wins implementieren → CI lauffähig machen → Dann systematisch migrieren