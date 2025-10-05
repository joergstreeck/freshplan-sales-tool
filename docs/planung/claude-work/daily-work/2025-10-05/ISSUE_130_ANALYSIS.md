# 🔴 Issue #130 - TestDataBuilder CDI-Konflikt Analyse

**Erstellt:** 2025-10-05
**Status:** BLOCKER für Sprint 2.1.6
**Aufwand:** 1-2h (Quick Fix)
**Impact:** 12 Tests broken, Worktree CI disabled

---

## 📋 PROBLEM-ZUSAMMENFASSUNG (Nicht-Technisch)

### **Was ist passiert?**

Stell dir vor, du hast in deinem Unternehmen **zwei verschiedene Abteilungen**, die beide "Kundendaten-Vorlagen" erstellen - aber jede macht es ein bisschen anders:

1. **Die alte Abteilung** (`src/main/java`) nutzt ein **veraltetes System** (CustomerContactRepository)
2. **Die neue Abteilung** (`src/test/java`) nutzt ein **modernes System** (ContactRepository)

Das Problem: **Das System kann nicht unterscheiden, welche Abteilung gerade arbeitet** - es lädt beide Vorlagen gleichzeitig und verwechselt dann die Zuständigkeiten.

### **Der konkrete Fehler**

Die Tests versuchen, einen Kunden-Kontakt zu speichern, aber das System sagt:
> "Dieser Kontakt ist schon gespeichert (detached entity)"

**Warum?** Weil zwei verschiedene Builder am selben Kontakt arbeiten - einer denkt, er ist neu, der andere denkt, er ist schon da.

---

## 🔧 TECHNISCHE ANALYSE

### **Root Cause**

**Doppelte TestDataBuilder in `src/main` und `src/test` mit CDI `@ApplicationScoped`:**

```
/src/main/java/de/freshplan/test/builders/
├── ContactBuilder.java          → nutzt CustomerContactRepository (deprecated)
├── CustomerBuilder.java
├── OpportunityBuilder.java
├── TimelineEventBuilder.java
└── TestDataBuilder.java         → CDI Facade (@ApplicationScoped)

/src/test/java/de/freshplan/test/builders/
├── ContactBuilder.java          → nutzt ContactRepository (current) ✅
├── CustomerTestDataFactory.java ✅
├── OpportunityTestDataFactory.java ✅
└── ContactTestDataFactory.java  ✅
```

**CDI-Konflikt:** Quarkus Arc lädt **BEIDE** Builder zur Laufzeit:
- Legacy Builder (src/main) injiziert `CustomerContactRepository`
- Neue Builder (src/test) injizieren `ContactRepository`
- Tests rufen falsche Builder-Version → `EntityExistsException`

### **Broken Tests (12x ContactInteractionServiceIT)**

```java
// Alle 12 Tests scheitern mit gleichem Fehler:
EntityExistsException: detached entity passed to persist:
  de.freshplan.domain.customer.entity.CustomerContact

// Setup-Code (setupTestData:76):
@Inject TestDataBuilder testData;  // ← Legacy Builder wird geladen!

CustomerContact contact = testData.contact()
    .forCustomer(customer)
    .asCEO()
    .persist();  // ← Hier crasht es!
```

**Liste der broken Tests:**
1. `shouldCalculateDataQualityMetricsAccurately`
2. `shouldCalculateEngagementTrends`
3. `shouldCalculateWarmthScoreWithMultipleFactors`
4. `shouldCategorizeInteractionOutcomes`
5. `shouldCreateInteractionAndUpdateContact`
6. `shouldGetInteractionsChronologically`
7. `shouldHandleCQRSModeWhenEnabled`
8. `shouldHandleConcurrentInteractionUpdates`
9. `shouldHandleInvalidContactIdGracefully`
10. `shouldHandleLowDataScenario`
11. `shouldRecordDifferentInteractionTypes`
12. `shouldTrackDataFreshnessCorrectly`

### **Impact**

- ❌ **12 Tests broken** → CI instabil
- ❌ **Worktree CI "Test Suite Expansion" disabled** (`.github/workflows/worktree-ci.yml`)
- ❌ **NoSuchFieldError: customerContactRepository** bei Test-Ausführung
- ❌ **Test-Isolation nicht mehr garantiert** (detached entities)

---

## ✅ LÖSUNG: QUICK FIX (1-2h)

### **Schritt 1: Legacy Builder löschen**

```bash
# LÖSCHEN (Legacy in src/main):
rm -rf /src/main/java/de/freshplan/test/builders/ContactBuilder.java
rm -rf /src/main/java/de/freshplan/test/builders/CustomerBuilder.java
rm -rf /src/main/java/de/freshplan/test/builders/OpportunityBuilder.java
rm -rf /src/main/java/de/freshplan/test/builders/TimelineEventBuilder.java
rm -rf /src/main/java/de/freshplan/test/TestDataBuilder.java
rm -rf /src/main/java/de/freshplan/test/builders/UserBuilder.java
rm -rf /src/main/java/de/freshplan/test/builders/ContactInteractionBuilder.java
```

### **Schritt 2: Tests auf neue Builder migrieren**

**ALT (Legacy Pattern):**
```java
@Inject TestDataBuilder testData;

Customer customer = testData.customer()
    .withCompanyName("Test GmbH")
    .persist();

CustomerContact contact = testData.contact()
    .forCustomer(customer)
    .asCEO()
    .persist();
```

**NEU (TestDataFactory Pattern):**
```java
@Inject CustomerRepository customerRepository;
@Inject ContactRepository contactRepository;

Customer customer = CustomerTestDataFactory.builder()
    .withCompanyName("Test GmbH")
    .buildAndPersist(customerRepository);

CustomerContact contact = ContactTestDataFactory.builder()
    .forCustomer(customer)
    .withPosition("CEO")
    .buildAndPersist(contactRepository);
```

### **Schritt 3: ContactInteractionServiceIT migrieren**

**Datei:** `/backend/src/test/java/de/freshplan/domain/customer/service/ContactInteractionServiceIT.java`

**Änderungen:**
1. `@Inject TestDataBuilder testData` → entfernen
2. `@Inject ContactRepository contactRepository` → hinzufügen
3. `setupTestData()` Methode umschreiben (Zeile 76)
4. Alle 12 Tests validieren

### **Schritt 4: Worktree CI reaktivieren**

**Datei:** `.github/workflows/worktree-ci.yml`

**Änderungen:**
```yaml
# Vorher (disabled):
# - name: Test Suite Expansion
#   run: ./mvnw test -Dtest=ContactInteractionServiceIT

# Nachher (enabled):
- name: Test Suite Expansion
  run: ./mvnw test -Dtest=ContactInteractionServiceIT
```

---

## 🎯 AKZEPTANZKRITERIEN

- [ ] Legacy Builder aus `src/main/java/de/freshplan/test/builders/` gelöscht
- [ ] Alle 12 Tests in ContactInteractionServiceIT grün (keine EntityExistsException)
- [ ] Worktree CI "Test Suite Expansion" Job reaktiviert
- [ ] Keine CDI `NoSuchFieldError` mehr
- [ ] Migration Guide für bestehende Tests dokumentiert (siehe unten)

---

## 📚 MIGRATION GUIDE (für andere Tests)

### **Pattern 1: Customer + Contact Creation**

**ALT:**
```java
@Inject TestDataBuilder testData;

Customer customer = testData.customer()
    .withCompanyName("Test GmbH")
    .persist();
```

**NEU:**
```java
@Inject CustomerRepository customerRepository;

Customer customer = CustomerTestDataFactory.builder()
    .withCompanyName("Test GmbH")
    .buildAndPersist(customerRepository);
```

### **Pattern 2: Contact with Position**

**ALT:**
```java
CustomerContact ceo = testData.contact()
    .forCustomer(customer)
    .asCEO()
    .persist();
```

**NEU:**
```java
@Inject ContactRepository contactRepository;

CustomerContact ceo = ContactTestDataFactory.builder()
    .forCustomer(customer)
    .withPosition("CEO")
    .isPrimary(true)
    .isDecisionMaker(true)
    .buildAndPersist(contactRepository);
```

### **Pattern 3: Opportunity Creation**

**ALT:**
```java
Opportunity opp = testData.opportunity()
    .forCustomer(customer)
    .asQualifiedLead()
    .withExpectedValue(75000)
    .persist();
```

**NEU:**
```java
@Inject OpportunityRepository opportunityRepository;

Opportunity opp = OpportunityTestDataFactory.builder()
    .forCustomer(customer)
    .withStage(OpportunityStage.QUALIFIED)
    .withExpectedValue(BigDecimal.valueOf(75000))
    .buildAndPersist(opportunityRepository);
```

---

## 🔗 REFERENZEN

**Issue:**
- GitHub Issue: https://github.com/joergstreeck/freshplan-sales-tool/issues/130

**Dokumentation:**
- TRIGGER_SPRINT_2_1_6.md: User Story 0.5 (Issue #130 Fix)
- TESTING_GUIDE.md: Zeile 106-152 (Builder Pattern Strategie)
- TestDataBuilder.java (Legacy): Beispiel für alte Facade
- CustomerTestDataFactory.java (Neu): Beispiel für neues Pattern

**CI:**
- Worktree CI Workflow: `.github/workflows/worktree-ci.yml`
- ContactInteractionServiceIT: Broken Tests

---

## 🚀 NEXT STEPS (nach Fix)

### **Sofort:**
1. ✅ Issue #130 Fix (diese Quick-Fix-Strategie)
2. ✅ 12 Tests grün validieren
3. ✅ Worktree CI reaktivieren

### **Sprint 2.1.6 (danach):**
4. ✅ Bestandsleads-Migrations-API
5. ✅ Lead → Kunde Convert Flow
6. ✅ Stop-the-Clock UI
7. ✅ Automated Jobs

### **Sprint 2.1.7 (Test Infrastructure Overhaul):**
8. 🎯 **CRM Szenario-Builder** (komplexe Workflows)
9. 🎯 **Faker-Integration** (realistische Testdaten)
10. 🎯 **Lead-spezifische TestDataFactories**
11. 🎯 **Test-Pattern Library & Documentation**

**Begründung Sprint 2.1.7:** Issue #130 ist nur die "Spitze des Eisbergs" - wir brauchen eine professionelle Testdaten-Strategie für komplexes CRM. Das verdient eigenen Sprint (nicht nur Quick-Fix).

---

**Erstellt:** Claude Code (Sonnet 4.5)
**Datum:** 2025-10-05
**Kontext:** Sprint 2.1.6 Vorbereitung
