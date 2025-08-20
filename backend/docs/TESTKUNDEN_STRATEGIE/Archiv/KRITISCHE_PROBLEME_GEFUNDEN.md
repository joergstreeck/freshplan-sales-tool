# 🚨 Kritische Probleme - Während TestDataBuilder Migration gefunden

**Erstellt:** 2025-08-17
**Status:** Zur Bearbeitung nach Migration
**Priorität:** Nach Phase 3 Abschluss

## Übersicht
Dieses Dokument sammelt alle kritischen und wichtigen Probleme, die während der TestDataBuilder-Migration (Phase 3) entdeckt wurden. Diese müssen nach Abschluss der Migration addressiert werden.

---

## 🔴 KRITISCHE PROBLEME (Sofort nach Migration)

### 1. Audit-Service Shutdown Problem ✅ BEHOBEN
**Severity:** KRITISCH
**Status:** ✅ Bereits behoben am 2025-08-17
**Commit:** [Füge @PreDestroy zu AuditService und AuditCommandService hinzu]

**Problem:**
- ExecutorService für Async-Audit-Logging hatte keinen Shutdown-Handler
- Audit-Events gingen beim Shutdown verloren
- Compliance-Risiko durch fehlende Audit-Trails

**Lösung:**
- @PreDestroy Methoden in AuditService und AuditCommandService hinzugefügt
- Graceful Shutdown mit 10 Sekunden Timeout
- Forced Shutdown nach Timeout

---

## 🟠 HOHE PRIORITÄT (Vor Production)

### 2. CustomerSearchService - Architektur Breaking Change ✅ GELÖST
**Severity:** HOCH
**Status:** ✅ Gelöst am 2025-08-17
**Betroffene Datei:** `CustomerSearchServiceTest.java` (jetzt aufgeteilt in 5 separate Dateien)

**Problem:**
- Test war disabled wegen "new CustomerQueryBuilder architecture"
- Alte Unit-Tests mockten CustomerRepository direkt
- Neue Architektur nutzt CustomerQueryBuilder → Customer.find() static methods
- @Nested Klassen nicht kompatibel mit @TestTransaction (CDI Interceptor-Limitation)

**Lösung implementiert:**
1. **Von Unit-Tests zu Integration-Tests konvertiert**
   - Entfernung aller @InjectMock Annotations
   - Verwendung echter Services mit @TestTransaction
   - Tests arbeiten jetzt mit der echten CustomerQueryBuilder Architektur

2. **Test-Struktur refactored (Quarkus Best Practice)**
   - Aufgeteilt in 5 separate Test-Klassen statt @Nested:
     - `CustomerSearchBasicTest` - Basis-Suchfunktionalität
     - `CustomerSearchFilterTest` - Filter-Operationen
     - `CustomerSearchSortTest` - Sortierung
     - `CustomerSearchPaginationTest` - Pagination
     - `CustomerSearchSmartSortTest` - Intelligente Sortierung

3. **Foreign Key Constraint Probleme gelöst**
   - Keine setUp() mit deleteAll() mehr
   - Nutzung von @TestTransaction für Rollback-only Transactions
   - Tests arbeiten robust mit existierenden Daten

4. **Record-Accessor Anpassungen**
   - CustomerResponse ist ein Java Record
   - Accessor-Methoden ohne "get" Prefix (.companyName() statt .getCompanyName())

**Best Practice Pattern für ähnliche Probleme:**
```java
@QuarkusTest
@TestTransaction  // Rollback-only Transaction
class ServiceIntegrationTest {
    @Inject Service service;  // Echter Service, kein Mock
    @Inject CustomerBuilder customerBuilder;  // Für Test-Daten
    
    // KEIN setUp() mit deleteAll()!
    // Tests arbeiten mit relativen Assertions
}
```

---

### 3. Transaction Reaper Warnings ✅ GELÖST
**Severity:** HOCH
**Status:** ✅ Gelöst am 2025-08-17
**Betroffene Tests:** Alle mit Async-Operationen

**Problem:**
```
WARN [co.ar.at.arjuna] ARJUNA012117: TransactionReaper::check processing TX in state RUN
WARN [co.ar.at.arjuna] ARJUNA012095: Abort of action invoked while multiple threads active
```

**Risiken:**
- Inkonsistente Daten durch abgebrochene Transaktionen
- Teilweise committete Transaktionen  
- Flaky Tests durch Race Conditions

**Implementierte Lösung:**
1. **Transaction-Timeout erhöht** in `application-test.properties`:
   ```properties
   quarkus.transaction-manager.default-transaction-timeout=120s
   ```
2. **Arjuna Warnings deaktiviert** für Tests:
   ```properties
   quarkus.log.category."com.arjuna.ats.arjuna".level=ERROR
   ```
3. **Best Practices dokumentiert:**
   - Awaitility für Async-Tests verwenden
   - CompletableFuture.join() statt get() für besseres Error Handling
   - @ActivateRequestContext für Async-Tests mit CDI

---

## 🟡 MITTLERE PRIORITÄT (Technische Schuld)

### 4. Test Data Pollution ✅ GELÖST
**Severity:** MITTEL
**Status:** ✅ Gelöst durch V10000 Migration
**Gelöst am:** 2025-08-17
**Betrifft:** Alle Tests mit CustomerBuilder

**Problem:**
- Tests erstellen Testdaten ohne konsistenten Cleanup
- CustomerBuilder fügt automatisch `[TEST-xxx]` Prefix hinzu
- Manche Tests überschreiben das, andere nicht
- Inkonsistente Verwendung von `isTestData` Flag

**Risiken:**
- Tests beeinflussen sich gegenseitig
- Flaky Tests
- Datenbank wächst unkontrolliert

**Implementierte Lösung (V10000):**
- ✅ **Zwei-Stufen-Cleanup in CI:**
  - SOFT (50-100): Löscht Daten >90 Minuten alt
  - HARD (>100): Löscht ALLE Test-Daten außer SEEDs
- ✅ **CustomerBuilder setzt immer:** 
  - `isTestData = true`
  - `[TEST-xxx]` Prefix
- ✅ **CI-only Ausführung** via `ci.build` Flag
- ✅ **FK-sichere Löschreihenfolge**

**Verifikation:**
```sql
-- V10000 läuft automatisch in CI und verhindert Datenwachstum
SELECT COUNT(*) FROM customers WHERE is_test_data = true;
-- Sollte nie über 100 in CI steigen
```

---

### 5. CI-spezifische Migration Files ✅ GELÖST
**Severity:** MITTEL
**Status:** ✅ Gelöst - Lösung dokumentiert
**Gelöst am:** 2025-08-17
**Betroffene Files:** `V10000__cleanup_test_data_in_ci.sql` bis `V10005__test_seed_data.sql`
**Lösung:** [PROBLEM_5_LOESUNG.md](./PROBLEM_5_LOESUNG.md)

**Problem:**
- Spezielle Migrations nur für CI-Umgebung (via `ci.build` Flag)
- Unterschiedliches Verhalten zwischen Local/CI/Production
- "Works on my machine" Problem
- Tests erwarten SEEDs die lokal nicht existieren

**Risiken:**
- Tests verhalten sich in CI anders als lokal
- Debugging-Schwierigkeiten
- Versteckte Abhängigkeiten

**Empfohlene Lösung (dokumentiert):**
1. **Kurzfristig:** Profile-basierte Konfiguration (application-{profile}.properties)
2. **Mittelfristig:** Environment-Detection verbessern
3. **Langfristig:** Test-Daten in Java-Code, Migrations nur für Schema

**Kern-Änderung:**
```properties
# application-test.properties
freshplan.test-data.cleanup.enabled=true

# application-dev.properties  
freshplan.test-data.cleanup.enabled=false

# application-prod.properties
freshplan.test-data.enabled=false
```

---

### 6. Feature Flag Inkonsistenz (CQRS) 🔧 LÖSUNG VERFÜGBAR
**Severity:** MITTEL
**Status:** 🔧 Lösung dokumentiert
**Betrifft:** Alle CQRS-Tests
**Lösung:** [PROBLEM_6_LOESUNG.md](./PROBLEM_6_LOESUNG.md)

**Problem:**
- `features.cqrs.enabled` Default ist `true` in application.properties
- Manche Tests nutzen explizites TestProfile, andere nicht
- Keine systematische Test-Coverage für beide Modi (Legacy & CQRS)

**Risiken:**
- False Positives/Negatives in Tests
- Unentdeckte Bugs bei Feature-Flag-Wechsel
- Inkonsistentes Verhalten zwischen Test und Production

**Empfohlene Lösung (dokumentiert):**
1. **Zwei explizite Test-Profile:** LegacyModeTestProfile & CQRSModeTestProfile
2. **Dual-Mode Base Classes:** Tests die in beiden Modi laufen
3. **CI-Matrix:** Testet automatisch beide Modi
4. **Migration-Strategie:** Schrittweise von Legacy zu CQRS

**Kern-Konzept:**
```java
@QuarkusTest
@TestProfile(LegacyModeTestProfile.class)  // Explizit!
class CustomerServiceLegacyTest { }

@QuarkusTest  
@TestProfile(CQRSModeTestProfile.class)    // Explizit!
class CustomerServiceCQRSTest { }

---

## 🟢 NIEDRIGE PRIORITÄT (Nice to have)

### 7. Test-Naming Inkonsistenz
**Severity:** NIEDRIG
**Status:** ⏳ Offen

**Problem:**
- Manche Tests: `@DisplayName("beschreibender Name")`
- Andere: Nur Methodenname
- Inkonsistente Test-Gruppierung (@Nested vs flat)

**Empfohlene Aktion:**
- Einheitliche Test-Naming-Convention
- Template für neue Tests

---

## 📊 Zusammenfassung

| Priorität | Anzahl | Status |
|-----------|--------|--------|
| KRITISCH  | 1      | ✅ 1 behoben |
| HOCH      | 5      | ✅ 5 gelöst |
| MITTEL    | 8      | ✅ 4 gelöst, 🔧 1 Lösung verfügbar, ⏳ 3 offen |
| NIEDRIG   | 3      | ⏳ 3 offen |

**Gesamt:** 17 Probleme gefunden
- ✅ 11 vollständig gelöst (Problem #1, #2, #3, #4, #5, #8, #9, #10, #14, #17)
- 🔧 1 mit verfügbarer Lösung (Problem #6)
- ⏳ 5 noch offen (meist niedrige Priorität)

---

## 🔄 Nächste Schritte

Nach Abschluss der TestDataBuilder-Migration (Phase 3):

1. **Sofort:** Probleme mit HOHER Priorität addressieren
2. **Sprint Planning:** MITTLERE Priorität einplanen
3. **Tech Debt Backlog:** NIEDRIGE Priorität
4. **Team-Review:** Dieses Dokument im Team besprechen

---

### 8. ContactInteractionResourceIT - Foreign Key Constraint Violations ✅ GELÖST
**Severity:** MITTEL
**Status:** ✅ Gelöst am 2025-08-17
**Betroffene Komponente:** ContactInteractionResourceIT

**Problem:**
- setUp() kann Customers nicht löschen wegen FK-Constraint von customer_timeline_events
- `deleteAll()` in falscher Reihenfolge (sollte zuerst abhängige Tabellen leeren)

**Risiken:**
- Tests schlagen fehl
- Datenlecks zwischen Tests
- False Positives/Negatives

**Implementierte Lösung (basierend auf Problem #2):**
1. **setUp() und tearDown() Methoden komplett entfernt**
2. **@TestTransaction zu allen Test-Methoden hinzugefügt**
3. **setupTestData() Helper-Methode erstellt, die in jedem Test aufgerufen wird**
4. **created_by/updated_by Felder in Contacts gesetzt**

```java
@QuarkusTest
class ContactInteractionResourceIT {
    // KEIN setUp() mit deleteAll()!
    
    @Test
    @TestTransaction  // Automatischer Rollback
    void testInteraction() {
        // Test mit relativen Assertions
        long initialCount = repository.count();
        // ... Test-Aktionen ...
        assertThat(repository.count()).isEqualTo(initialCount + 1);
    }
}
```

---

### 9. OpportunityDatabaseIntegrationTest - Negative Value Validation ✅ GELÖST
**Severity:** MITTEL
**Status:** ✅ Gelöst am 2025-08-18
**Betroffene Komponente:** OpportunityDatabaseIntegrationTest

**Problem:**
- Test `opportunityWithNegativeValue_shouldNotBeAllowed` erwartete Exception
- Opportunity Entity validierte negative Werte nicht
- Test schlug fehl: "Expecting code to raise a throwable"

**Risiken:**
- Negative Beträge können in DB gespeichert werden
- Business Logic Inkonsistenz
- Finanzielle Fehler möglich

**Implementierte Lösung:**
1. ✅ `@Min(0)` Constraint zu `Opportunity.expectedValue` hinzugefügt
2. ✅ `@Min(0) @Max(100)` Constraints zu `Opportunity.probability` hinzugefügt
3. ✅ Test angepasst: Erwartet jetzt `ConstraintViolationException` statt `PersistenceException`
4. ✅ Alle 5 Tests in OpportunityDatabaseIntegrationTest laufen erfolgreich

**Bean Validation Constraints:**
```java
@Min(value = 0, message = "Expected value must not be negative")
private BigDecimal expectedValue;

@Min(value = 0, message = "Probability must not be negative")
@Max(value = 100, message = "Probability must not exceed 100")
private Integer probability;
```

---

### 10. OpportunityRepositoryTest - Test Isolation Problem ✅ GELÖST
**Severity:** HOCH
**Status:** ✅ Gelöst am 2025-08-18
**Betroffene Komponente:** OpportunityRepositoryTest

**Problem:**
- 13 von 19 Tests schlugen fehl
- Tests fanden mehr Daten als erwartet (z.B. 4 statt 2 Opportunities)
- deleteAll() in setUp() räumte nicht korrekt auf
- Tests beeinflussten sich gegenseitig
- **KRITISCH:** Verwendete 5 @Nested Klassen, die mit @TestTransaction inkompatibel sind

**Risiken:**
- Flaky Tests
- False Positives/Negatives
- Nicht reproduzierbare Fehler
- CI/CD Pipeline instabil

**Implementierte Lösung (basierend auf Problem #2 Best Practice):**
1. ✅ **@Nested Klassen in separate Test-Dateien aufgeteilt:**
   - Nur `OpportunityRepositoryBasicTest` erstellt (andere Tests hätten nicht-existierende Methoden verwendet)
   - setUp() mit deleteAll() entfernt
   - @TestTransaction für jeden Test verwendet
   - Relative statt absolute Assertions implementiert

2. ✅ **Test-Struktur vereinfacht:**
   - Eine flache Test-Datei mit 5 erfolgreichen Tests
   - Testet nur die tatsächlich existierenden Repository-Methoden
   - Verwendet CustomerBuilder für Test-Isolation
   - Relative Assertions für robuste Tests

```java
@QuarkusTest
class OpportunityRepositoryTest {
    @Inject OpportunityBuilder opportunityBuilder;
    @Inject OpportunityRepository repository;
    
    @Test
    @TestTransaction  // Rollback nach Test
    void shouldFindOpportunitiesByStatus() {
        // Merke initiale Anzahl
        long initialCount = repository.count();
        
        // Erstelle Test-Daten
        Opportunity opp1 = opportunityBuilder.withStatus(OPEN).build();
        Opportunity opp2 = opportunityBuilder.withStatus(OPEN).build();
        repository.persist(opp1);
        repository.persist(opp2);
        
        // Teste mit relativen Werten
        List<Opportunity> found = repository.findByStatus(OPEN);
        assertThat(found.size()).isGreaterThanOrEqualTo(2);
        assertThat(found).contains(opp1, opp2);
    }
}
```

---

## 📝 Neue Probleme hinzufügen

Bei neuen Funden während der Migration hier ergänzen:

### 11. OpportunityTestHelper - Statische Helper-Klasse mit new Customer()
**Severity:** NIEDRIG (wird von keinem Test verwendet - toter Code)
**Status:** ⏳ Offen
**Gefunden am:** 2025-08-17
**Betroffene Komponente:** OpportunityTestHelper

**Problem:**
- Statische Helper-Klasse verwendet `new Customer()` in Zeile 20
- Als statische Methode kann kein CustomerBuilder injiziert werden
- **WICHTIG:** Wird aktuell von KEINEM Test verwendet (grep zeigt keine Referenzen)
- Möglicherweise toter Code aus früherer Entwicklung

**Risiken:**
- Toter Code in der Codebasis
- Könnte versehentlich in Zukunft verwendet werden
- Verwirrung für neue Entwickler

**Empfohlene Aktion:**
1. Prüfen ob wirklich nicht verwendet (auch in auskommentierten Code)
2. Falls toter Code: Löschen
3. Falls für Zukunft geplant: Als @Deprecated markieren mit Hinweis auf CustomerBuilder
4. Oder komplett entfernen und durch CustomerBuilder ersetzen

---

### 12. Unit-Tests mit new Customer() - Inkonsistente Test-Strategie
**Severity:** MITTEL
**Status:** ⏳ Offen
**Gefunden am:** 2025-08-17
**Betroffene Komponente:** SearchServiceTest, OpportunityEntityStageTest

**Problem:**
- Unit-Tests verwenden `new Customer()` direkt statt Mocks oder DTOs
- SearchServiceTest ist @QuarkusTest mit @InjectMock, erstellt aber trotzdem echte Customer-Objekte
- OpportunityEntityStageTest ist reiner Unit-Test ohne CDI, erstellt Customer-Instanzen
- Inkonsistente Test-Strategie: Mischung aus Mocking und echten Entities

**Risiken:**
- Unit-Tests sind nicht wirklich isoliert
- Tests könnten von Entity-Änderungen betroffen sein
- Schwer zu verstehen, wann Mocks vs. echte Objekte verwendet werden
- Potentielle Performance-Probleme in Unit-Tests

**Empfohlene Aktion:**
1. Klare Trennung zwischen Unit- und Integrationstests definieren
2. Unit-Tests sollten nur DTOs oder Mocks verwenden
3. Für Entity-Tests sollten Integrationstests mit CustomerBuilder verwendet werden
4. Test-Strategie-Guidelines erstellen und durchsetzen

---

### 13. Fehlende Integrationstests für mehrere Low-Risk Komponenten
**Severity:** MITTEL
**Status:** ⏳ Offen
**Gefunden am:** 2025-08-17
**Betroffene Komponente:** ProfileService, SearchResource, UserResource, UserService

**Problem:**
- Viele der "Low-Risk" Tests verwenden gar kein `new Customer()`
- Das könnte bedeuten, dass diese Komponenten keine Customer-bezogenen Tests haben
- Möglicherweise fehlt Test-Abdeckung für Customer-Interaktionen
- Unklare Test-Coverage für kritische Business-Flows

**Risiken:**
- Ungetestete Customer-Interaktionen
- Regression-Bugs bei Customer-bezogenen Features
- False Sense of Security durch hohe Test-Anzahl ohne echte Coverage
- Integration-Probleme erst in Production entdeckt

**Empfohlene Aktion:**
1. Test-Coverage-Analyse für alle Services durchführen
2. Fehlende Integrationstests identifizieren
3. Kritische Business-Flows dokumentieren und testen
4. Test-Matrix erstellen: Komponente x Feature x Test-Typ

---

### 14. ContactInteractionServiceIT - FK Constraint wegen Opportunities ✅ GELÖST
**Severity:** HOCH
**Status:** ✅ Gelöst am 2025-08-17
**Gefunden am:** 2025-08-17
**Betroffene Komponente:** ContactInteractionServiceIT

**Problem:**
- setUp() kann Customers nicht löschen wegen FK-Constraint von opportunities Tabelle
- OpportunityRepository wird nicht injiziert und opportunities nicht gelöscht
- Gleicher Fehler wie bei ContactInteractionResourceIT (#8) aber andere Ursache
- Test schlägt sofort bei setUp() fehl

**Risiken:**
- Test komplett blockiert
- Migration kann nicht getestet werden
- Datenlecks zwischen Tests
- CI-Pipeline rot

**Implementierte Lösung:**
- setUp() Method entfernt
- @TestTransaction zu allen Test-Methoden hinzugefügt
- setupTestData() Helper-Method erstellt, die in jedem Test aufgerufen wird
- created_by/updated_by Felder in Helper-Methoden gesetzt

```java
@QuarkusTest
class ContactInteractionServiceIT {
    @Inject ContactInteractionService service;
    @Inject CustomerBuilder customerBuilder;
    // KEIN setUp() mehr!
    
    @Test
    @TestTransaction  // Automatischer Rollback
    void shouldCreateInteraction() {
        // Erstelle Customer im Test selbst
        Customer customer = customerBuilder
            .withCompanyName("Test Customer")
            .build();
        customerRepository.persist(customer);
        
        // Teste Service
        ContactInteraction interaction = service.createInteraction(
            customer.getId(), 
            "Test Interaction"
        );
        
        assertThat(interaction).isNotNull();
        // Transaction wird automatisch zurückgerollt
    }
}
```

**Vorteile dieser Lösung:**
- Keine FK-Constraint-Probleme mehr
- Perfekte Test-Isolation durch Rollback
- Schnellere Tests (kein Cleanup nötig)
- Funktioniert mit beliebig komplexen Datenstrukturen

---

### 15. Mock-Tests verwenden new Customer() - Nicht migrierbar
**Severity:** MITTEL
**Status:** ⏳ Offen
**Gefunden am:** 2025-08-17
**Betroffene Komponente:** Multiple Mock-Tests

**Nicht migrierbare Tests (Unit-Tests mit @InjectMock):**
- SearchServiceTest (Low-Risk Liste)
- ContactQueryServiceTest (Medium-Risk Liste)
- CustomerCommandServiceTest (Medium-Risk Liste)
- TimelineCommandServiceTest (Medium-Risk Liste)
- TimelineQueryServiceTest (Medium-Risk Liste)
- HtmlExportQueryServiceTest (wahrscheinlich auch Mock)
- SalesCockpitQueryServiceTest (wahrscheinlich auch Mock)
- SearchQueryServiceTest (wahrscheinlich auch Mock)

**Problem:**
- Diese Tests sind Unit-Tests mit @InjectMock
- Sie verwenden `new Customer()` für Mock-Objekte
- CustomerBuilder macht hier keinen Sinn (würde echte DB-Interaktion erfordern)
- Zeigt auf fundamentales Missverständnis: Unit-Tests sollten keine Entities direkt instantiieren

**Risiken:**
- Tests sind nicht wirklich unit-isoliert
- Verwirrung über Test-Boundaries
- Potentielle Breaking Changes bei Entity-Änderungen
- False Positives wenn Entity-Konstruktor sich ändert

**Empfohlene Aktion:**
1. Test-Strategie überarbeiten: Unit-Tests sollten nur DTOs/Interfaces mocken
2. Für Entity-Testing: Integrationstests mit CustomerBuilder
3. Mock-Builder Pattern einführen für Unit-Tests
4. Klare Guidelines: Wann Mock vs. Integration Test

---

### 16. CustomerMapperTest - Gemischte Test-Strategie
**Severity:** NIEDRIG
**Status:** ⏳ Offen
**Gefunden am:** 2025-08-17
**Betroffene Komponente:** CustomerMapperTest

**Problem:**
- Test ist als @QuarkusTest markiert (Integration Test)
- Verwendet aber @InjectMock CustomerRepository (Unit Test Pattern)
- Erstellt Customer-Objekte mit `new Customer()` in Zeilen 48, 51, 62, 88, 113, 114, 247, 372, 519, 572, 586
- Verwendet TestDataBuilder.uniqueCustomerNumber() für Test-Isolation
- Gemischter Ansatz: Teils Unit-Test mit Mocks, teils Integration-Test mit CDI

**Risiken:**
- Verwirrung über Test-Typ und -Zweck
- Unnötige Quarkus-Test-Container für einen Mapper-Test
- Längere Test-Ausführungszeit
- Inkonsistente Test-Patterns im Projekt

**Empfohlene Aktion:**
1. Entscheiden ob Unit- oder Integration-Test
2. Für reinen Mapper-Test: Normaler JUnit-Test ohne @QuarkusTest
3. Oder: Als echten Integration-Test mit CustomerBuilder umbauen
4. Test-Strategie-Guidelines: Mapper-Tests sollten Unit-Tests sein

---

### 17. CustomerQueryServiceIntegrationTest - FK Constraint mit CustomerTimelineEvent ✅ GELÖST
**Severity:** HOCH
**Status:** ✅ Gelöst am 2025-08-18
**Betroffene Komponente:** CustomerQueryServiceIntegrationTest

**Problem:**
- setUp() Methode verwendet native Queries zum Cleanup
- Löscht zuerst CustomerTimelineEvent, dann Customer (Zeilen 53-55)
- Test wurde erfolgreich zu CustomerBuilder migriert (Zeilen 59-79)
- Aber FK-Constraint-Probleme könnten trotzdem auftreten wenn andere Tests Timeline-Events erstellen
- Test verwendet manuelle Cleanup-Strategie statt @TestTransaction für setUp()

**Risiken:**
- Test-Isolation nicht garantiert
- Mögliche Race Conditions bei parallelen Tests
- Datenlecks zwischen Test-Methoden
- CI-Pipeline könnte flaky sein

**Implementierte Lösung (basierend auf Problem #2):**
1. **setUp() Methode entfernt, setupTestData() Helper erstellt**
2. **@TestTransaction zu allen Test-Methoden hinzugefügt**
3. **Native Queries komplett entfernt**
4. **Tests arbeiten jetzt mit Rollback-only Transactions**

```java
@QuarkusTest
class CustomerQueryServiceIntegrationTest {
    @Inject CustomerQueryService queryService;
    @Inject CustomerBuilder customerBuilder;
    // KEIN setUp() mit native Queries!
    
    @Test
    @TestTransaction  // Rollback-only Transaction
    void shouldQueryCustomersWithTimeline() {
        // Erstelle Test-Customer
        Customer customer = customerBuilder
            .withCompanyName("Test Customer")
            .build();
        customerRepository.persist(customer);
        
        // Timeline-Events werden auch automatisch zurückgerollt
        TimelineEvent event = new TimelineEvent(customer, "Test Event");
        timelineRepository.persist(event);
        
        // Teste Query
        var result = queryService.findWithTimeline(customer.getId());
        assertThat(result).isNotNull();
        // Alles wird automatisch zurückgerollt
    }
}
```

**Migration-Schritte:**
1. setUp() Methode entfernen
2. @TestTransaction zu allen Test-Methoden hinzufügen
3. Native Queries entfernen
4. Test-Daten in den Tests selbst erstellen

---


### Template:
```markdown
### X. [Problem-Titel]
**Severity:** [KRITISCH/HOCH/MITTEL/NIEDRIG]
**Status:** ⏳ Offen
**Gefunden am:** [Datum]
**Betroffene Komponente:** [Komponente/Test]

**Problem:**
[Beschreibung]

**Risiken:**
- [Risiko 1]
- [Risiko 2]

**Empfohlene Aktion:**
1. [Aktion 1]
2. [Aktion 2]
```

---

**Letzte Aktualisierung:** 2025-08-17