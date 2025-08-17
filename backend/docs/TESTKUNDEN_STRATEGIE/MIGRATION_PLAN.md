# 🔨 MIGRATION PLAN - Der Bauplan zur neuen Test-Daten-Architektur
## Schritt-für-Schritt vom Chaos zur Ordnung

**Version:** 3.0  
**Status:** TEAM-APPROVED & IN PROGRESS (Phase 0 ✅)  
**Datum:** 17.08.2025  
**Autor:** Claude & Jörg (Architektur-Session) + Team-Feedback
**Fortschritt:** Phase 0 ABGESCHLOSSEN ✅

---

## 📋 Inhaltsverzeichnis

1. [Executive Summary](#1-executive-summary)
2. [Ausgangslage](#2-ausgangslage)
3. [Zielarchitektur](#3-zielarchitektur)
4. [Phase 0: Vorbereitung](#4-phase-0-vorbereitung-neu)
5. [Phase 1: Abriss](#5-phase-1-abriss)
6. [Phase 2: Neubau](#6-phase-2-neubau)
7. [Phase 3: Umbau](#7-phase-3-umbau)
8. [Phase 4: Validierung](#8-phase-4-validierung)
9. [Rollback-Plan](#9-rollback-plan)
10. [Zeitplan](#10-zeitplan)
11. [CI-Konfiguration](#11-ci-konfiguration-kritisch)

---

## 1. Executive Summary

### Was machen wir?
Kompletter Umbau des Test-Daten-Managements von 6 chaotischen Systemen zu 1 klaren System.

### Warum?
- CI ist rot (Test-Daten-Konflikte)
- 73+ unkontrollierte Test-Kunden
- Niemand versteht das aktuelle System

### Wie lange?
**5-6 Tage** für komplette Migration

### Risiko?
**Niedrig** - Alle kritischen Komponenten sind im Feature-Branch, nicht in main

---

## 2. Ausgangslage

### 2.1 Aktuelle Komponenten (Stand: 17.08.2025)

```
CHAOS-INVENTAR:
├── 6 Java Initializers
│   ├── CustomerDataInitializer.java (58 Kunden)
│   ├── TestDataContactInitializer.java (11 Kunden)
│   ├── DevDataInitializer.java (Test-User)
│   ├── E2EDataInitializer.java (Test-User)
│   ├── OpportunityDataInitializer.java (Opportunities)
│   └── TestDataResource.java (REST API)
│
├── 4 Migrationen
│   ├── V219__basic_test_customers.sql (5 Kunden)
│   ├── V220__cleanup_test_pollution.sql (Cleanup)
│   ├── V9999__test_seed_data.sql (50 SEED)
│   └── V10000__cleanup_test_data_in_ci.sql (CI-Cleanup)
│
└── 233 Test-Stellen
    └── new Customer() überall verstreut
```

### 2.2 Abhängigkeiten

| Komponente | Verwendet von | Kritikalität |
|------------|---------------|--------------|
| CustomerDataInitializer | Dev-Umgebung, manche Tests | HOCH |
| TestDataContactInitializer | Contact-Tests | MITTEL |
| V9999 | E2E-Tests | HOCH |
| V219/V220 | Keine (Feature-Branch) | NIEDRIG |
| new Customer() | 233 Tests | SEHR HOCH |

---

## 3. Zielarchitektur

### 3.1 Neue Komponenten

```
NEUE ORDNUNG:
├── 1 TestDataBuilder
│   ├── CustomerBuilder
│   ├── ContactBuilder
│   ├── OpportunityBuilder
│   ├── UserBuilder
│   └── ScenarioBuilder
│
├── 2 Migrationen
│   ├── V9999__test_seed_data.sql (20 SEED, optimiert)
│   └── V10000__ci_cleanup.sql (CI-Cleanup, angepasst)
│
└── 0 Initializers (alle gelöscht!)
```

### 3.2 Datenfluss

```
Tests → TestDataBuilder → @TestTransaction → Rollback
E2E → V9999 SEED-Daten → Stabil
```

---

## 4. Phase 0: VORBEREITUNG 🛠️ ✅ ABGESCHLOSSEN (17.08.2025)
**Dauer: 0.5 Tag** *(Tatsächlich: 1 Stunde)*
**KRITISCH für CI-Erfolg!**

### 4.1 CI-Konfiguration anpassen (KRITISCH!)

#### A. JDBC-URL mit ci.build Flag konfigurieren

**Problem:** V10000 prüft `current_setting('ci.build', true)` - das funktioniert NUR wenn der Parameter in der JDBC-URL gesetzt ist!

```yaml
# .github/workflows/backend-ci.yml
- name: Run tests
  run: |
    ./mvnw test \
      -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue
```

```yaml
# .github/workflows/database-growth-check.yml  
- name: Run database growth check
  run: |
    ./mvnw test -Dtest=DatabaseGrowthTrackerTest \
      -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue
```

**Alternative für lokales Testen:**
```sql
-- Einmalig in CI-DB ausführen
ALTER DATABASE freshplan SET ci.build='true';
```

### 4.2 Unique Constraint sicherstellen

```sql
-- Migration V10002__ensure_unique_constraints.sql
ALTER TABLE customers 
  ADD CONSTRAINT IF NOT EXISTS customers_customer_number_uk 
  UNIQUE (customer_number);

ALTER TABLE permissions
  ADD CONSTRAINT IF NOT EXISTS permissions_code_uk 
  UNIQUE (code);
```

### 4.3 V9000 Guard-Logik hinzufügen (KRITISCH!)

```sql
-- Am Anfang von V9000__normalize_fk_actions.sql
DO $$
BEGIN
    -- Guard: Nur in CI/Test ausführen!
    IF current_setting('freshplan.environment', true) NOT IN ('ci', 'test') THEN
        RAISE NOTICE 'V9000 skipped - only for CI/Test environments';
        RETURN;
    END IF;
    
    -- Rest der Migration...
END $$;
```

### 4.4 Backup erstellen

```bash
# Datenbank-Backup
pg_dump freshplan > backup_before_migration_$(date +%Y%m%d).sql

# Code-Backup  
git checkout -b backup/before-test-cleanup
git checkout feature/refactor-large-services
```

---

## 5. Phase 1: ABRISS 🗑️
**Dauer: 1 Tag**

### 5.1 Git-Branch erstellen

```bash
# Neuer Feature-Branch
git checkout main
git pull origin main
git checkout -b refactor/test-data-cleanup

# Backup-Branch für Notfälle
git checkout -b backup/before-test-cleanup
git checkout refactor/test-data-cleanup
```

### 5.2 Löschungen durchführen

#### A. Java Initializers entfernen

```bash
# Alle 6 Initializers löschen
git rm src/main/java/de/freshplan/api/CustomerDataInitializer.java
git rm src/main/java/de/freshplan/api/TestDataContactInitializer.java
git rm src/main/java/de/freshplan/api/DevDataInitializer.java
git rm src/main/java/de/freshplan/api/E2EDataInitializer.java
git rm src/main/java/de/freshplan/api/OpportunityDataInitializer.java

# TestDataResource behalten aber später umbauen!
# git rm src/main/java/de/freshplan/api/dev/TestDataResource.java
```

#### B. Überflüssige Migrationen entfernen

```bash
# V219 und V220 löschen (nicht in main!)
git rm src/main/resources/db/migration/V219__basic_test_customers.sql
git rm src/main/resources/db/migration/V220__cleanup_test_pollution.sql

# V9999 und V10000 behalten für Umbau
```

#### C. Commit Abriss

```bash
git add -A
git commit -m "refactor: Remove old test data initializers and migrations

- Removed 6 Java initializers (CustomerData, TestDataContact, etc.)
- Removed V219 and V220 migrations (not in main)
- Preparing for new TestDataBuilder architecture

Part of test data cleanup initiative"
```

### 5.3 Validierung nach Abriss

```bash
# Tests laufen (werden fehlschlagen - das ist OK!)
./mvnw test 2>&1 | tee abriss-test-results.txt

# Erwartete Fehler dokumentieren
grep -c "CustomerDataInitializer" abriss-test-results.txt
# Sollte 0 sein (keine Referenzen mehr)

# Abhängige Tests identifizieren
grep -r "TEST-00\|TEST_CUST" src/test/java --include="*.java" | cut -d: -f1 | sort -u > affected-tests.txt
wc -l affected-tests.txt
# Ca. 32 betroffene Test-Dateien
```

---

## 6. Phase 2: NEUBAU 🏗️
**Dauer: 1-2 Tage**

### 6.1 TestDataBuilder implementieren (ERWEITERT mit build() vs persist())

#### A. Basis-Struktur erstellen

```java
// src/main/java/de/freshplan/test/TestDataBuilder.java
package de.freshplan.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class TestDataBuilder {
    
    private static final AtomicLong COUNTER = new AtomicLong();
    
    @Inject CustomerRepository customerRepository;
    @Inject ContactRepository contactRepository;
    @Inject OpportunityRepository opportunityRepository;
    @Inject UserRepository userRepository;
    
    public CustomerBuilder customer() {
        return new CustomerBuilder(customerRepository);
    }
    
    public ContactBuilder contact() {
        return new ContactBuilder(contactRepository);
    }
    
    public OpportunityBuilder opportunity() {
        return new OpportunityBuilder(opportunityRepository);
    }
    
    public ScenarioBuilder scenario() {
        return new ScenarioBuilder(this);
    }
    
    // Unique ID Generator
    private static String uniqueId() {
        return System.currentTimeMillis() + "-" + 
               COUNTER.incrementAndGet() + "-" +
               Thread.currentThread().getId();
    }
}
```

#### B. CustomerBuilder implementieren

```java
// Inner class in TestDataBuilder
public static class CustomerBuilder {
    private final CustomerRepository repository;
    private String companyName = "Test Company";
    private CustomerStatus status = CustomerStatus.AKTIV;
    private Industry industry = Industry.SONSTIGE;
    private BigDecimal annualVolume = BigDecimal.ZERO;
    private boolean isTestData = true;
    
    CustomerBuilder(CustomerRepository repository) {
        this.repository = repository;
    }
    
    public CustomerBuilder withCompanyName(String name) {
        this.companyName = name;
        return this;
    }
    
    public CustomerBuilder withStatus(CustomerStatus status) {
        this.status = status;
        return this;
    }
    
    // Vordefinierte Szenarien
    public CustomerBuilder asPremiumCustomer() {
        this.status = CustomerStatus.PREMIUM;
        this.annualVolume = new BigDecimal("1000000");
        return this;
    }
    
    public CustomerBuilder asRiskCustomer() {
        this.status = CustomerStatus.RISIKO;
        return this;
    }
    
    // WICHTIG: Zwei getrennte Pfade!
    
    /**
     * build() - Erstellt Customer OHNE Persistierung
     * Verwendung: Unit-Tests, Service-Tests wo keine DB nötig
     */
    public Customer build() {
        Customer c = new Customer();
        String id = uniqueId();
        c.setCustomerNumber("TEST-" + id);  // IMMER unique!
        c.setCompanyName("[TEST-" + id + "] " + companyName);
        c.setStatus(status);
        c.setIndustry(industry);
        c.setIsTestData(true);  // IMMER true für Test-Daten!
        c.setCreatedBy("test-builder");
        c.setExpectedAnnualVolume(annualVolume);
        // Weitere Required Fields setzen
        c.setPartnerStatus(PartnerStatus.KEIN_PARTNER);
        c.setPaymentTerms(PaymentTerms.NETTO_30);
        c.setPrimaryFinancing(FinancingType.PRIVATE);
        return c;
    }
    
    /**
     * persist() - Erstellt Customer MIT Persistierung
     * Verwendung: Integration-Tests, Tests die DB brauchen
     */
    @Transactional
    public Customer persist() {
        Customer c = build();
        repository.persist(c);
        repository.flush();  // Sofort Constraints prüfen!
        return c;
    }
}
```

### 6.2 PermissionHelper implementieren (NEU - PostgreSQL Variante)

```java
// src/main/java/de/freshplan/test/PermissionHelperPg.java
package de.freshplan.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

@ApplicationScoped
public class PermissionHelperPg {

    @Inject EntityManager em;

    @Transactional(REQUIRES_NEW)
    public Permission findOrCreatePermission(String code, String description) {
        // PostgreSQL ON CONFLICT - atomar und race-safe!
        Long id = ((Number) em.createNativeQuery("""
            INSERT INTO permissions (code, description, is_test_data)
            VALUES (:code, :desc, true)
            ON CONFLICT (code) DO UPDATE
              SET description = COALESCE(permissions.description, EXCLUDED.description)
            RETURNING id
            """)
            .setParameter("code", code)
            .setParameter("desc", description)
            .getSingleResult()).longValue();

        return em.find(Permission.class, id);
    }
}
```

### 6.3 V9999 optimieren

```sql
-- src/test/resources/db/migration/V9999__test_seed_data.sql
-- OPTIMIERT: Nur noch 20 stabile SEED-Daten

DO $$
BEGIN
    -- NUR in Test-DB ausführen
    IF current_database() LIKE '%test%' THEN
        
        -- 1. CLEANUP: Alles löschen
        DELETE FROM audit_trail WHERE is_test_data = true;
        DELETE FROM opportunities WHERE is_test_data = true;
        DELETE FROM customer_contacts WHERE is_test_data = true;
        DELETE FROM customers WHERE is_test_data = true;
        
        -- 2. SEED: 20 stabile Referenz-Daten
        INSERT INTO customers (
            id, customer_number, company_name, 
            customer_type, status, industry,
            expected_annual_volume, risk_score, 
            is_test_data, created_by, created_at
        ) VALUES 
        -- Basis-Kunden für E2E
        (gen_random_uuid(), 'SEED-001', '[SEED] Active Restaurant', 
         'UNTERNEHMEN', 'AKTIV', 'RESTAURANT', 
         100000, 10, true, 'seed', NOW()),
         
        (gen_random_uuid(), 'SEED-002', '[SEED] Risk Hotel', 
         'UNTERNEHMEN', 'RISIKO', 'HOTEL', 
         50000, 85, true, 'seed', NOW()),
         
        (gen_random_uuid(), 'SEED-003', '[SEED] Premium Chain', 
         'UNTERNEHMEN', 'PREMIUM', 'RESTAURANT', 
         1000000, 5, true, 'seed', NOW()),
         
        -- Weitere 17 für verschiedene Szenarien...
        -- (gekürzt für Übersichtlichkeit)
        ;
        
        -- Test-User
        INSERT INTO users (username, email, roles, is_test_data)
        VALUES 
        ('test-admin', 'admin@test.local', '{ADMIN}', true),
        ('test-sales', 'sales@test.local', '{SALES}', true);
        
    END IF;
END $$;
```

### 6.4 V10000 anpassen (MIT CI-FLAG!)

```sql
-- src/main/resources/db/migration/V10000__cleanup_test_data_in_ci.sql
-- CI-spezifisches Cleanup

DO $$
DECLARE
    ci_mode BOOLEAN;
BEGIN
    -- Prüfe ob wir in CI sind
    ci_mode := current_setting('ci.build', true) = 'true';
    
    IF ci_mode THEN
        -- Aggressive Cleanup in CI
        DELETE FROM customers 
        WHERE is_test_data = true 
           OR company_name LIKE '[TEST-%]%'
           OR created_at < NOW() - INTERVAL '1 day';
           
        RAISE NOTICE 'CI Cleanup: Removed old test data';
    END IF;
END $$;
```

### 6.5 V10001 Guard Migration hinzufügen (NEU)

```sql
-- src/main/resources/db/migration/V10001__assert_test_data_contract.sql
-- Sicherheitsnetz während der Migration

DO $$
DECLARE
    test_count INTEGER;
    unmarked_count INTEGER;
BEGIN
    -- Zähle Test-Daten
    SELECT COUNT(*) INTO test_count 
    FROM customers WHERE is_test_data = true;
    
    -- Zähle unmarkierte Test-Daten
    SELECT COUNT(*) INTO unmarked_count
    FROM customers 
    WHERE (company_name LIKE '[TEST%]%' OR company_name LIKE '[SEED]%')
      AND is_test_data = false;
    
    -- Abbruch bei Vertragsverletzung
    IF test_count > 30 THEN
        RAISE EXCEPTION 'Too many test customers: %. Maximum allowed: 30', test_count;
    END IF;
    
    IF unmarked_count > 0 THEN
        RAISE EXCEPTION 'Found % unmarked test customers!', unmarked_count;
    END IF;
    
    RAISE NOTICE 'Test data contract OK: % test customers (max 30)', test_count;
END $$;
```

### 6.6 Commit Neubau

```bash
git add src/main/java/de/freshplan/test/TestDataBuilder.java
git add src/test/resources/db/migration/V9999__test_seed_data.sql
git add src/main/resources/db/migration/V10000__cleanup_test_data_in_ci.sql

git commit -m "feat: Implement new TestDataBuilder architecture

- Added central TestDataBuilder with builders for all entities
- Optimized V9999 to only 20 SEED customers
- Updated V10000 for CI-specific cleanup
- All test data now marked and prefixed

Part of test data cleanup initiative"
```

---

## 7. Phase 3: UMBAU 🔄
**Dauer: 2-3 Tage**

### 7.1 Test-Migration Strategy (VORSICHT bei Mass-Refactoring!)

#### A. Quick-Win Tests (Tag 1)
Tests die bereits @TestTransaction haben:

```bash
# WICHTIG: Zwei unterschiedliche Replacements!
# Für Unit-Tests (ohne DB):
find src/test/java -path "*/unit/*" -name "*.java" -exec sed -i.bak \
  's/new Customer()/testDataBuilder.customer().build()/g' {} \;

# Für Integration-Tests (mit DB):
find src/test/java -path "*/integration/*" -name "*.java" -exec sed -i.bak \
  's/new Customer()/testDataBuilder.customer().persist()/g' {} \;

# Imports hinzufügen
find src/test/java -name "*.java" -exec sed -i.bak \
  '1s/^/import de.freshplan.test.TestDataBuilder;\n/' {} \;
```

#### B. Manuelle Migration (Tag 2-3)

**Beispiel-Migration:**

```java
// ALT: Test mit Initializer-Abhängigkeit
@Test
void testWithExistingCustomer() {
    // Erwartet dass TEST-001 existiert
    Customer customer = customerRepository
        .find("customerNumber", "TEST-001")
        .firstResult();
    
    assertThat(customer).isNotNull();
    // Test-Logik...
}

// NEU: Test mit TestDataBuilder
@Test
@TestTransaction  // Wichtig!
void testWithExistingCustomer() {
    // Erstelle eigenen Test-Kunden
    Customer customer = testDataBuilder.customer()
        .withCompanyName("My Test Customer")
        .persist();
    
    assertThat(customer).isNotNull();
    // Test-Logik...
}
```

### 7.2 Systematische Test-Migration

#### Schritt 1: Affected Tests identifizieren

```bash
# Liste der betroffenen Tests
cat affected-tests.txt

# Pro Test-Datei:
for test in $(cat affected-tests.txt); do
    echo "=== Migrating: $test ==="
    
    # 1. Backup
    cp $test $test.backup
    
    # 2. Inject TestDataBuilder
    # (manuell oder mit Script)
    
    # 3. Test ausführen
    ./mvnw test -Dtest=$(basename $test .java)
    
    # 4. Bei Erfolg: Commit
    if [ $? -eq 0 ]; then
        git add $test
        git commit -m "test: Migrate $(basename $test) to TestDataBuilder"
    fi
done
```

#### Schritt 2: E2E Tests anpassen

```java
// Base-Klasse für E2E
public abstract class BaseE2ETest {
    
    // SEED-Konstanten
    protected static final String SEED_CUSTOMER_ACTIVE = "SEED-001";
    protected static final String SEED_CUSTOMER_RISK = "SEED-002";
    protected static final String SEED_CUSTOMER_PREMIUM = "SEED-003";
    
    @BeforeEach
    void verifySeedData() {
        // Sicherstellen dass SEED-Daten da sind
        given()
            .when().get("/api/customers/" + SEED_CUSTOMER_ACTIVE)
            .then().statusCode(200);
    }
}
```

### 7.3 TestDataResource umbauen

```java
@Path("/api/test-data")
@RolesAllowed("admin")
@IfBuildProfile("dev")  // Nur in dev!
public class TestDataManagementResource {
    
    @Inject TestDataBuilder builder;
    
    @POST
    @Path("/scenario/{type}")
    public Response createScenario(@PathParam("type") String type) {
        return switch(type) {
            case "complete" -> Response.ok(
                builder.scenario().completeBusinessFlow()
            ).build();
            case "premium" -> Response.ok(
                builder.customer().asPremiumCustomer().persist()
            ).build();
            case "risk" -> Response.ok(
                builder.customer().asRiskCustomer().persist()
            ).build();
            default -> Response.status(400).build();
        };
    }
    
    @DELETE
    @Path("/cleanup")
    @Transactional
    public Response cleanup() {
        int deleted = customerRepository
            .delete("isTestData", true);
        return Response.ok(Map.of("deleted", deleted)).build();
    }
    
    @GET
    @Path("/status")
    public Response status() {
        long testData = customerRepository
            .count("isTestData", true);
        return Response.ok(Map.of(
            "testDataCount", testData,
            "status", testData > 100 ? "WARNING" : "OK"
        )).build();
    }
}
```

---

## 8. Phase 4: VALIDIERUNG ✅
**Dauer: 1 Tag**

### 8.1 Test-Suite Validierung

```bash
# Vollständiger Test-Lauf
./mvnw clean test | tee validation-results.txt

# Metriken erfassen
echo "=== TEST METRICS ===" 
grep "Tests run:" validation-results.txt | tail -1

# Fehleranalyse
grep -c "FAILED" validation-results.txt
# Sollte 0 sein!
```

### 8.2 CI-Simulation (MIT CI-FLAG!)

```bash
# CI-Umgebung simulieren mit JDBC-URL Parameter
export CI=true
export GITHUB_ACTIONS=true

# Tests mit CI-Profile UND ci.build Flag
./mvnw test -Dquarkus.profile=ci \
  -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue
```

```bash
# CI-Umgebung simulieren
export CI=true
export GITHUB_ACTIONS=true

# CI-Cleanup testen
psql -c "SET ci.build = 'true';"
./mvnw flyway:migrate

# Tests mit CI-Profile
./mvnw test -Dquarkus.profile=ci
```

### 8.3 Datenbank-Validierung

```sql
-- Test-Daten-Status prüfen
SELECT 
    COUNT(*) as total,
    COUNT(*) FILTER (WHERE is_test_data = true) as marked,
    COUNT(*) FILTER (WHERE company_name LIKE '[TEST%]%') as with_prefix,
    COUNT(*) FILTER (WHERE company_name LIKE '[SEED]%') as seed_data
FROM customers;

-- Erwartung:
-- total: < 30
-- marked: = total
-- with_prefix: = total
-- seed_data: = 20
```

### 8.4 Performance-Vergleich

```bash
# Vorher (aus alten Logs)
grep "Total time" old-ci-logs.txt
# z.B.: Total time: 12:34

# Nachher
time ./mvnw test
# Sollte mindestens 30% schneller sein
```

---

## 9. Rollback-Plan 🔄

Falls etwas schiefgeht:

### 9.1 Sofort-Rollback

```bash
# Zum Backup-Branch
git checkout backup/before-test-cleanup

# Oder Hard Reset
git reset --hard HEAD~10  # Anzahl Commits anpassen
```

### 9.2 Teilweiser Rollback

```bash
# Nur Initializers wiederherstellen
git checkout backup/before-test-cleanup -- src/main/java/de/freshplan/api/*Initializer.java

# Nur Migrationen wiederherstellen  
git checkout backup/before-test-cleanup -- src/main/resources/db/migration/V219*.sql
```

### 9.3 Datenbank-Recovery

```sql
-- Backup vor Migration
pg_dump freshplan > backup_before_migration.sql

-- Restore bei Problemen
psql freshplan < backup_before_migration.sql
```

---

## 10. Zeitplan 📅

### Phase 0: ✅ ERLEDIGT (17.08.2025)
- CI-Konfiguration angepasst
- Migrations vorbereitet
- ArchUnit-Regeln implementiert
- Backup erstellt

### Woche 1: Umbau

| Tag | Phase | Aktivitäten | Milestone |
|-----|-------|-------------|-----------|
| **Mo** | Abriss | Initializers löschen, Migrationen entfernen | Alte Struktur entfernt ✅ |
| **Di** | Neubau | TestDataBuilder implementieren | Builder funktioniert ✅ |
| **Mi** | Neubau | V9999/V10000 optimieren | Migrationen ready ✅ |
| **Do** | Umbau | Quick-Win Tests migrieren | 50% Tests grün ✅ |
| **Fr** | Umbau | Komplexe Tests migrieren | 80% Tests grün ✅ |

### Woche 2: Finalisierung

| Tag | Phase | Aktivitäten | Milestone |
|-----|-------|-------------|-----------|
| **Mo** | Umbau | E2E Tests anpassen | 100% Tests grün ✅ |
| **Di** | Validierung | CI-Tests, Performance | CI grün ✅ |
| **Mi** | Doku | README, Guides updaten | Dokumentation komplett ✅ |
| **Do** | Review | Code Review, Cleanup | PR ready ✅ |
| **Fr** | Deploy | Merge to main | 🎉 **FERTIG!** 🎉 |

---

## 📊 Erfolgs-Metriken

### Vorher → Nachher

| Metrik | Vorher | Nachher | Verbesserung |
|--------|--------|---------|--------------|
| Test-Daten-Systeme | 6 | 1 | -83% |
| Test-Kunden in DB | 73+ | <30 | -59% |
| CI Success Rate | ~60% | >95% | +35% |
| Test-Laufzeit | ~12 min | <8 min | -33% |
| Code-Zeilen | ~3000 | ~800 | -73% |
| Verständlichkeit | 😵 | 😊 | +∞ |

---

## 11. CI-Konfiguration 🚨 (KRITISCH!)

### GitHub Actions anpassen

```yaml
# .github/workflows/backend-ci.yml
jobs:
  test:
    steps:
      - name: Run tests with CI flag
        run: |
          ./mvnw test \
            -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue \
            -Dquarkus.profile=ci
```

### Test-Daten-Dashboard als CI-Gate (mit is_test_data Filter!)

```sql
-- Migration V10003__test_data_dashboard.sql
CREATE OR REPLACE VIEW test_data_dashboard AS
WITH stats AS (
    SELECT 
        COUNT(*) as total_test_data,
        COUNT(*) FILTER (WHERE company_name LIKE '[SEED]%') as seed_data,
        COUNT(*) FILTER (WHERE company_name LIKE '[TEST-%]%') as dynamic_test_data,
        COUNT(*) FILTER (WHERE created_at > NOW() - INTERVAL '1 hour') as created_last_hour,
        COUNT(*) FILTER (WHERE created_at < NOW() - INTERVAL '7 days') as older_than_week
    FROM customers
    WHERE is_test_data = true  -- WICHTIG: Nur Test-Daten zählen!
)
SELECT 
    *,
    CASE 
        WHEN total_test_data > 50 THEN 'CRITICAL'
        WHEN total_test_data > 30 THEN 'WARNING'
        ELSE 'OK'
    END as status
FROM stats;
```

### Database Growth Check - NUR Test-Daten überwachen

```java
// DatabaseGrowthTrackerTest.java
@Test
void checkDatabaseGrowth() {
    var result = entityManager.createNativeQuery(
        "SELECT COUNT(*) FROM customers WHERE is_test_data = true"  // Filter!
    ).getSingleResult();
    
    long testDataCount = ((Number) result).longValue();
    assertThat(testDataCount)
        .as("Test data should not exceed threshold")
        .isLessThanOrEqualTo(50);
}
```

### Concurrency-Test für PermissionHelper

```java
@QuarkusTest
class PermissionHelperConcurrencyIT {

    @Inject PermissionHelperPg helper;
    @Inject PermissionRepository repo;

    @Test
    void concurrent_findOrCreate_is_race_safe() throws Exception {
        String code = "perm:concurrency:test";
        int threads = 16;
        var pool = Executors.newFixedThreadPool(threads);
        
        var tasks = IntStream.range(0, 50)
            .<Callable<Permission>>mapToObj(i ->
                () -> helper.findOrCreatePermission(code, "Test")
            ).toList();

        pool.invokeAll(tasks); 
        pool.shutdown();

        // MUSS genau 1 sein trotz 50 parallelen Versuchen!
        assertThat(repo.list("code", code)).hasSize(1);
    }
}
```

---

### Mockito-Regel maschinell prüfen (CI-Check)

```bash
# .github/workflows/backend-ci.yml - Add before tests
- name: Check for Mockito varargs antipattern
  run: |
    if grep -r "when.*delete.*eq(" src/test/java --include="*.java"; then
      echo "❌ ERROR: Found eq() matcher with varargs method!"
      echo "Use any() instead of eq() for Panache delete methods"
      exit 1
    fi
```

### ArchUnit-Regel: Builder-Nutzung erzwingen

```java
// src/test/java/de/freshplan/arch/TestDataDisciplineRules.java
@AnalyzeClasses(packages = "de.freshplan")
public class TestDataDisciplineRules {

    @ArchTest
    static final ArchRule no_direct_persist_in_tests =
        methods().that().areDeclaredInClassesThat()
            .haveSimpleNameEndingWith("Test")
            .or().haveSimpleNameEndingWith("IT")
        .should().notCallMethodWhere(target -> 
            (target.getOwner().getName().endsWith("CustomerRepository") ||
             target.getOwner().getName().endsWith("Customer")) &&
            (target.getName().equals("persist") || 
             target.getName().equals("persistAndFlush"))
        );
        
    @ArchTest
    static final ArchRule must_use_test_data_builder =
        methods().that().areDeclaredInClassesThat()
            .haveSimpleNameEndingWith("Test")
        .and().haveName("setUp").or().haveName("beforeEach")
        .should().onlyCallMethodsThat()
            .areDeclaredIn(TestDataBuilder.class)
            .orShould().notCallMethodWhere(target ->
                target.getName().equals("new") && 
                target.getOwner().getName().endsWith("Customer")
            );
}
```

## ✅ Definition of Done

### Phase 0 (ABGESCHLOSSEN ✅ - 17.08.2025)
- [x] CI JDBC-URL mit ci.build Flag konfiguriert
- [x] V9000 Guard-Logik für Environment-Check eingebaut
- [x] Database Growth Check filtert auf is_test_data = true
- [x] Mockito CI-Check für varargs antipattern aktiv
- [x] ArchUnit-Regel für Builder-Enforcement implementiert
- [x] V10002 Unique Constraints Migration erstellt
- [x] V10003 Test-Daten-Dashboard View erstellt
- [x] V10000 für CI optimiert (ci.build check)
- [x] Backup-Branch erstellt (backup/before-test-cleanup-20250817)

### Phase 1 (ABGESCHLOSSEN ✅ - 17.08.2025)
- [x] Alle alten Initializers gelöscht ✅
- [x] V219 und V220 Migrationen entfernt ✅

### Phase 2-4 (TODO)
- [ ] TestDataBuilder mit build()/persist() implementiert
- [ ] PermissionHelperPg mit ON CONFLICT implementiert
- [ ] V9999 auf 20 SEED-Daten reduziert
- [ ] V10001 Guard Migration aktiv
- [ ] Concurrency-Test grün
- [ ] Alle Tests migriert und grün
- [ ] CI Pipeline grün
- [ ] Performance verbessert (>30%)
- [ ] Dokumentation aktualisiert
- [ ] Code Review abgeschlossen
- [ ] PR gemerged

---

## 🚀 Let's Go!

```bash
# Start!
git checkout -b refactor/test-data-cleanup
echo "Migration started: $(date)" > migration.log

# Los geht's mit Phase 1!
```

---

*"The best time to plant a tree was 20 years ago. The second best time is now."*

**Ende des Bauplans**