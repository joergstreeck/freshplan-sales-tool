# 🔧 Backend Test Debug Plan - Sprint 2.1.4

**Status:** In Bearbeitung
**Branch:** `feat/mod02-backend-sprint-2.1.4`
**Problem:** 91 fehlende Tests (36 Failures, 55 Errors) nach Sprint 2.1.4 Migration

## 📊 Aktueller Stand (SHA: 6380ff4fe)

### ✅ ERFOLGE
- **ContactsCountConsistencyTest:** ContextNotActive komplett behoben
- **CDI Interceptor Problem:** @TestTransaction von Nested Classes entfernt (SecurityContextProviderTest)
- **🎉 PHASE 1 KOMPLETT ERFOLGREICH:** Dashboard Tests Transaction Collision behoben
  - **4 Dashboard Tests** auf @ActivateRequestContext umgestellt
  - **Alle Transaction Collision Fehler (ARJUNA016051)** verschwunden
  - **13 Dashboard Tests** laufen lokal und in CI durch
  - **CI Run 18130657439:** Bestätigt - keine Dashboard-Fehler mehr

### ⚠️ VERBLEIBENDE PROBLEME (Phase 2-4)

## 🎯 SYSTEMATISCHER DEBUG-PLAN

### 1. **✅ PHASE 1 ERFOLGREICH: Transaction Collision behoben**
**Problem:** Dashboard Tests schlugen mit Transaction Collision fehl
**Grund:** Tests verwendeten UserTransaction + @TestTransaction = Konflikt
**Status:** ✅ **KOMPLETT BEHOBEN** (CI Run 18130657439)

**Betroffene Tests:** ✅ Alle behoben
- ✅ `DashboardRBACTest` + `DashboardRBACAllowedTest`
- ✅ `DashboardAfterCommitTest`
- ✅ `DashboardBatchIdempotencyTest`
- ✅ `DashboardTruncationTest`

**Lösung angewandt:**
```java
// GEÄNDERT VON:
@TestTransaction  // ❌ Verursachte Collision
// ZU:
@ActivateRequestContext  // ✅ Nur RequestContext, keine Transaktion
import jakarta.enterprise.context.control.ActivateRequestContext;
```

**Ergebnis:**
- ✅ **0 Transaction Collision Fehler** in CI
- ✅ **13 Dashboard Tests** laufen durch
- ✅ **ARJUNA016051 Fehler** komplett verschwunden

### 2. **❌ PHASE 2A: FEHLGESCHLAGEN - Viel zu oberflächlich (Run 18131383572)**
**Problem:** Ich fixte nur 2 Tests, aber CI zeigt VIELE weitere ContextNotActive Tests
**Status:** ❌ **GESCHEITERT** - Muss komplett neu angehen

**Root Cause:**
- **Alter Run:** Nur 12 Tests (Debug-Subset) ✅
- **Neuer Run:** Volle CI Test-Suite (der normale Zustand) ❌

**ALLE ContextNotActive Tests gefunden:**
- ❌ `SecurityContextProviderTest` (EdgeCasesTests, AuthenticationDetailsTests, JwtTokenTests)
- ❌ `AuditServiceTest` (5 Methoden: testAuditWithFullContext, testHashChaining, testLogAsync_Success, testLogSync_Success, testSecurityEvent_AlwaysSync)
- ✅ `ContactsCountDebugTest` → bereits gefixt
- ✅ `AuditCQRSIntegrationTest` → bereits gefixt

### 2B. **🔥 PHASE 2A FINALE ERKENNTNIS - BREAKTHROUGH!**
**FINALE ERKENNTNISSE durch lokalen Test:**
1. **❌ @ActivateRequestContext auf Nested Classes** → CDI-Fehler: "INNER class declares an interceptor binding but it must be ignored per CDI rules"
2. **❌ @ActivateRequestContext auf einzelne Test-Methoden IN NESTED CLASSES** → FUNKTIONIERT NICHT! CDI-Problem bestätigt
3. **✅ @TestTransaction für DB-Tests** → **FUNKTIONIERT PERFEKT! AuditServiceTest: 6 passing tests**
4. **✅ NEUE LÖSUNG: Test-Methoden aus Nested Classes herausverschieben** → @ActivateRequestContext nur in Hauptklasse möglich

**🎯 NÄCHSTE SCHRITTE PRIORISIERUNG:**
1. **SOFORT:** SecurityContextProviderTest Refactoring (8 Test-Methoden aus Nested Classes verschieben)
2. **DANN:** Phase 2B UserNotFound Fehler beheben
3. **DANN:** Phase 3 + 4 Mockito Issues

**Konkret identifizierte Test-Methoden (via Debug-Output):**
- `EdgeCasesTests.shouldReturnEmptySetForRolesWhenNotAuthenticated:618` ← **Debug zeigte exakte Zeile!**
- `EdgeCasesTests.shouldHandleMultipleRoleRequirementsCorrectly`
- `EdgeCasesTests.shouldHandleEmptyRoleNameGracefully`
- `AuthenticationDetailsTests.shouldReturnAnonymousDetailsWhenNotAuthenticated`
- `AuthenticationDetailsTests.shouldReturnAuthenticatedDetailsWhenAuthenticated`
- `JwtTokenTests.shouldReturnNullSessionIdWhenJwtNotAvailable`
- `JwtTokenTests.shouldReturnNullJwtWhenInstanceUnsatisfied`
- `JwtTokenTests.shouldReturnNullTokenExpirationWhenJwtNotAvailable`

**NÄCHSTE SCHRITTE (AKTUALISIERT nach lokalem Test):**
1. ✅ Import `@ActivateRequestContext` in SecurityContextProviderTest - IMPLEMENTIERT
2. ❌ @ActivateRequestContext auf 8 Test-Methoden - SCHEITERT an CDI-Problem in Nested Classes
3. **NEUE LÖSUNG:** SecurityContextProviderTest Refactoring (Methoden aus Nested Classes verschieben)
4. ✅ AuditServiceTest: `@TestTransaction` auf Class-Level - BEREITS KORREKT IMPLEMENTIERT

### 2B. **⏳ PHASE 2B: Entity Not Found Fehler (Wartet auf 2A)**
**Problem:** Tests erwarten User/Customer die seit Sprint 2.1.4 nicht mehr existieren

**Betroffene Tests (CI Run 18130657439):**
- `SalesCockpitQueryServiceTest.testAlerts_shouldGenerateOpportunityAlerts` → UserNotFound: `b81ceeed-0e09-4e7f-86a1-4f13bff77ad3`

**Analyse:**
- **ContextNotActive:** Tests fehlt @TestTransaction für RequestContext
- **UserNotFound:** Sprint 2.1.4 entfernte Seed-Daten, Tests erstellen keine eigenen Testdaten

**Lösungsoptionen:**
1. **Testdaten in @BeforeEach erstellen**
2. **Mocks für fehlende Entities verwenden**
3. **Builders/Factories für Testdaten implementieren**

**Kommandos:**
```bash
# Suche nach fehlenden Entity-Erstellungen
grep -r "User not found\|Customer.*not found" ci-logs/
# Prüfe bestehende TestDataFactory
find src/test -name "*TestDataFactory*" -o -name "*Builder*"
```

### 3. **PRIO 3: UnnecessaryStubbing (Mockito)**
**Problem:** Mockito-Stubbings werden definiert aber nicht verwendet

**Betroffene Tests:**
- `TimelineCommandServiceTest.testCreateEvent_withCustomerNotFound_shouldThrowException`
- `TimelineCommandServiceTest.testUpdateEvent_withEventNotFound_shouldThrowException`

**Lösung:**
```java
// OPTION 1: Stubbing entfernen
// when(mockRepository.findById(anyId())).thenReturn(...); // ❌ Entfernen

// OPTION 2: Lenient verwenden
@Mock(lenient = true)
SomeRepository mockRepository;

// OPTION 3: @ExtendWith(MockitoExtension.class) + lenient()
lenient().when(mockRepository.findById(anyId())).thenReturn(...);
```

### 4. **PRIO 4: NullPointer durch fehlende Mocks**
**Problem:** Mocks geben null zurück statt erwartete Objekte

**Betroffene Tests:**
- `TimelineQueryServiceTest.testGetTimelineSummary_shouldReturnSummaryStatistics:312`

**Lösung:**
```java
// Mock korrekt konfigurieren
when(mockService.getTimelineSummary(any())).thenReturn(
    TimelineSummary.builder()
        .totalEvents(5)
        .build()
);
```

## 🔄 WORKFLOW

### Phase 1: Transaction Collision (Sofort)
1. Alle Dashboard Tests auf @ActivateRequestContext umstellen
2. Test lokal: `./mvnw test -Dtest="Dashboard*Test"`
3. Commit + Push
4. CI prüfen

### Phase 2: Entity Not Found (Nach Phase 1)
1. Einen Test analysieren (z.B. SalesCockpitQueryServiceTest)
2. Testdaten-Setup implementieren
3. Pattern auf andere Tests übertragen
4. Commit + Push

### Phase 3: Mockito Issues (Nach Phase 2)
1. UnnecessaryStubbing beheben
2. NullPointer Mocks konfigurieren
3. Commit + Push

### Phase 4: Finale Validierung
1. Alle Tests lokal laufen lassen
2. 19 deaktivierte Tests wieder aktivieren
3. CI grün bekommen

## 📁 WICHTIGE DATEIEN

```
backend/
├── src/test/java/de/freshplan/
│   ├── domain/cockpit/Dashboard*Test.java     # PRIO 1: Transaction Collision
│   ├── domain/customer/service/
│   │   ├── SalesCockpitQueryServiceTest.java  # PRIO 2: UserNotFound
│   │   └── timeline/                          # PRIO 3+4: Mockito Issues
│   └── test/builders/                         # Testdaten-Factories
├── src/test/resources/application.properties  # RLS disabled
└── BACKEND_TEST_DEBUG_PLAN.md                # Dieses Dokument
```

## 🚨 KRITISCHE ERKENNTNISSE

1. **🔥 DEBUG-OUTPUT WAR GOLDWERT:**
   - **Maven Debug mit `-q` + Surefire Reports** zeigten exakte Fehlerursachen
   - **CI-Log-Analyse mit `gh run view --log-failed`** identifizierte konkrete Test-Methoden
   - **Ohne Debug-Outputs** hätten wir das CDI-Problem nicht so schnell erkannt

2. **@TestTransaction vs @ActivateRequestContext:**
   - **DB-Tests (EntityManager):** @TestTransaction auf Class-Level
   - **Service-Tests (RequestScoped Beans):** @ActivateRequestContext auf Test-Methoden
   - **❌ Niemals auf Nested Classes:** CDI-Interceptor-Binding-Fehler

3. **Sprint 2.1.4 Breaking Change:**
   - Seed-Daten entfernt → Tests müssen eigene Daten erstellen
   - RLS Interceptor disabled in Tests (bereits konfiguriert)

4. **Mockito Strict Mode:**
   - Quarkus verwendet strict Mockito → Alle Stubbings müssen verwendet werden
   - lenient() für optionale Stubbings verwenden

## 📈 ERFOLGS-METRIKEN

**Ziel:** 0 Failures, 0 Errors in CI
**Aktuell:** 36 Failures, 55 Errors

**Tracking:**
- Phase 1: -4 Transaction Collision Errors erwartet
- Phase 2: -20 Entity Not Found Errors erwartet
- Phase 3: -10 Mockito Issues erwartet
- Phase 4: Alle Tests grün

## 🔗 REFERENZEN

- **CI Logs:** https://github.com/joergstreeck/freshplan-sales-tool/actions
- **Letzte Analyse:** Run 18129587552 (SHA: e9946cba4)
- **Sprint 2.1.4 Changes:** Seed-Daten entfernt, Self-managed Tests
- **Quarkus Testing Guide:** https://quarkus.io/guides/getting-started-testing