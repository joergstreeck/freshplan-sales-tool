# Phase 2A: Builder Core-Verbesserungen

**Aufwand:** 25 Minuten  
**Ziel:** Stabile Basis für CustomerTestDataFactory + UserTestDataFactory  
**Ergebnis:** Kollisionsfreie IDs, isTestData=true, realistische Test-Daten

---

## 🎯 Überblick

Diese Phase konzentriert sich auf die 2 wichtigsten Builder:
- **CustomerTestDataFactory** - Kern der meisten Tests
- **UserTestDataFactory** - Für User-Management Tests

**Resultat:** Robuste Basis-Builder mit Enterprise-Standards ohne Überengineering!

**📋 Verweis auf Recherche:** Siehe [RESEARCH_RESULTS.md - Builder/Factory-Infrastruktur](./RESEARCH_RESULTS.md#🏗️-builderfactory-infrastruktur-zu-verbessern-in-phase-2)

---

## 🏗️ Schritt 1: CustomerTestDataFactory verbessern

### 1.1 Aktuelle Status prüfen

**Datei analysieren:**
```bash
# Prüfen ob isTestData bereits gesetzt
grep -n "isTestData" backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java

# Prüfen ob eindeutige IDs generiert werden
grep -n "customerNumber" backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java
```

### 1.2 Core-Verbesserungen implementieren

**Datei: `backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java`**

**Kollisionsfreie ID-Generierung hinzufügen:**
```java
// Am Anfang der Builder-Klasse ergänzen:
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public static class Builder {
    // Defaults erweitern:
    private String companyName = generateCompanyName();
    private String customerNumber; // wird automatisch generiert
    private Boolean isTestData = true; // PFLICHT für alle Test-Kunden
    private String status = "ACTIVE";
    
    // KOLLISIONSFREIE ID-GENERIERUNG - Thread-Safe & CI-kompatibel
    private static final AtomicLong SEQ = new AtomicLong();
    
    /**
     * Run-ID für eindeutige Test-Identifikation.
     * Nutzt test.run.id (CI) -> GITHUB_RUN_ID (Fallback) -> "LOCAL"
     */
    private static String runId() { 
        return System.getProperty("test.run.id", 
               System.getenv().getOrDefault("GITHUB_RUN_ID", "LOCAL")); 
    }
    
    /**
     * Generiert eindeutige Kundennummer: KD-TEST-{RUN_ID}-{SEQ}
     * Format erlaubt Spurensuche bei CI-Problemen.
     */
    private static String nextNumber() { 
        return "KD-TEST-" + runId() + "-" + String.format("%05d", SEQ.incrementAndGet()); 
    }
    
    /**
     * Erzeugt automatisch Kundennummer falls nicht explizit gesetzt.
     */
    private void generateUniqueCustomerNumber() {
        if (customerNumber == null) {
            customerNumber = nextNumber();
        }
    }
    
    /**
     * Realistische Firmennamen statt "Test Company".
     * Präfix [TEST] für eindeutige Markierung.
     */
    private static String generateCompanyName() {
        String[] prefixes = {"[TEST]", "[TEST-DATA]"};
        String[] companies = {
            "Müller GmbH", "Schmidt AG", "Weber & Co", "Fischer Solutions", 
            "Becker Industries", "Koch Logistics", "Richter Consulting",
            "Hoffmann Group", "Schulz Systems", "Wagner Analytics"
        };
        
        // Pseudo-Random basierend auf nanoTime für Variety
        String prefix = prefixes[(int)(System.nanoTime() % prefixes.length)];
        String company = companies[(int)(System.nanoTime() % companies.length)];
        return prefix + " " + company;
    }
    
    public Customer build() {
        generateUniqueCustomerNumber();
        
        Customer customer = new Customer();
        customer.setCompanyName(companyName);
        customer.setCustomerNumber(customerNumber);
        customer.setIsTestData(true); // KRITISCH: Immer true für Tests
        customer.setStatus(status);
        
        // Realistische Defaults für Tests
        customer.setExpectedAnnualVolume(50000.0); // 50k€ Default
        customer.setRiskScore(2); // Low-Risk Default
        
        // Audit-Felder für Tests
        customer.setCreatedAt(LocalDateTime.now());
        customer.setCreatedBy("test-system");
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUpdatedBy("test-system");
        
        return customer;
    }
}
```

**Builder-Pattern-Methoden erweitern:**
```java
// Fluent API erweitern:
public Builder withCompanyName(String companyName) {
    this.companyName = companyName;
    return this;
}

public Builder withCustomerNumber(String customerNumber) {
    this.customerNumber = customerNumber;
    return this;
}

public Builder withStatus(String status) {
    this.status = status;
    return this;
}

public Builder withExpectedAnnualVolume(Double volume) {
    this.expectedAnnualVolume = volume;
    return this;
}

public Builder withRiskScore(Integer riskScore) {
    this.riskScore = riskScore;
    return this;
}
```

---

## 👤 Schritt 2: UserTestDataFactory verbessern

### 2.1 Status prüfen

**Datei analysieren:**
```bash
# Prüfen ob isTestData bereits gesetzt
grep -n "isTestData" backend/src/test/java/de/freshplan/test/builders/UserTestDataFactory.java

# Prüfen ob eindeutige Identifier generiert werden
grep -n "email\|username" backend/src/test/java/de/freshplan/test/builders/UserTestDataFactory.java
```

### 2.2 User-Builder-Verbesserungen

**Datei: `backend/src/test/java/de/freshplan/test/builders/UserTestDataFactory.java`**

**Eindeutige User-Identifiers implementieren:**
```java
public static class Builder {
    // Defaults erweitern:
    private String username = "testuser"; // wird unique gemacht
    private String firstName = "Test";
    private String lastName = "User";  
    private String email; // wird automatisch generiert
    private Boolean isTestData = true; // PFLICHT
    private Boolean isActive = true;
    
    /**
     * Generiert eindeutige User-Identifiers basierend auf Run-ID.
     * Verhindert Kollisionen bei parallelen Tests.
     */
    private void generateUniqueIdentifiers() {
        String runId = System.getProperty("test.run.id", 
            String.valueOf(System.currentTimeMillis() % 100000));
            
        // Email immer unique generieren
        if (email == null) {
            email = "test." + runId + "." + System.nanoTime() % 1000 + "@test.example.com";
        }
        
        // Username unique machen falls Default verwendet
        if ("testuser".equals(username)) {
            username = "testuser_" + runId + "_" + (System.nanoTime() % 1000);
        }
    }
    
    public User build() {
        generateUniqueIdentifiers();
        
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setIsTestData(true); // KRITISCH: Immer true
        user.setIsActive(isActive);
        
        // Audit-Felder für Tests
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy("test-system");
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy("test-system");
        
        return user;
    }
}
```

**Fluent API für User erweitern:**
```java
public Builder withUsername(String username) {
    this.username = username;
    return this;
}

public Builder withEmail(String email) {
    this.email = email;
    return this;
}

public Builder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
}

public Builder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
}

public Builder withIsActive(Boolean isActive) {
    this.isActive = isActive;
    return this;
}
```

---

## ✅ Schritt 3: Validierung

### 3.1 isTestData wird überall gesetzt
```bash
# Prüfen dass beide Builder isTestData setzen
grep -A 5 -B 5 "setIsTestData(true)" backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java
grep -A 5 -B 5 "setIsTestData(true)" backend/src/test/java/de/freshplan/test/builders/UserTestDataFactory.java

# Erwartetes Ergebnis: Beide Dateien haben die Zeile
```

### 3.2 Eindeutige IDs werden generiert
```bash
# Test Customer-Builder
cd backend
./mvnw test -Dtest=CustomerRepositoryTest -DfailIfNoTests=false
# Prüfen dass keine Duplicate-Key-Errors

# Test User-Builder  
./mvnw test -Dtest=UserRepositoryTest -DfailIfNoTests=false
# Prüfen dass keine Duplicate-Key-Errors
```

### 3.3 Builder funktionieren ohne Dependencies
```java
// Quick Unit-Test erstellen zum Testen:
@Test
void customerBuilderWorksWithoutDatabase() {
    Customer customer = CustomerTestDataFactory.builder()
        .withCompanyName("My Test Company")
        .build();
        
    assertThat(customer.getIsTestData()).isTrue();
    assertThat(customer.getCustomerNumber()).startsWith("KD-TEST-");
    assertThat(customer.getCompanyName()).isEqualTo("My Test Company");
    assertThat(customer.getStatus()).isEqualTo("ACTIVE");
}

@Test
void userBuilderWorksWithoutDatabase() {
    User user = UserTestDataFactory.builder()
        .withFirstName("John")
        .withLastName("Doe")
        .build();
        
    assertThat(user.getIsTestData()).isTrue();
    assertThat(user.getEmail()).contains("@test.example.com");
    assertThat(user.getFirstName()).isEqualTo("John");
    assertThat(user.getLastName()).isEqualTo("Doe");
    assertThat(user.getIsActive()).isTrue();
}
```

---

## 🎯 Erfolgskriterien - Definition of Done (messbar)

**Phase 2A ist NUR DANN abgeschlossen, wenn alle Validierungen grün sind:**

### ✅ **Validierung 1: Beide Core-Builder setzen isTestData=true**
```bash
grep -c "setIsTestData(true)" backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java
# MUSS ergeben: >= 1

grep -c "setIsTestData(true)" backend/src/test/java/de/freshplan/test/builders/UserTestDataFactory.java  
# MUSS ergeben: >= 1
```

### ✅ **Validierung 2: Kollisionsfreie ID-Generierung funktioniert**
```bash
cd backend && ./mvnw test -Dtest=CustomerRepositoryTest::shouldSaveAndLoad -DfailIfNoTests=false
# MUSS grün laufen ohne Duplicate-Key-Errors

cd backend && ./mvnw test -Dtest=UserRepositoryTest -DfailIfNoTests=false
# MUSS grün laufen ohne Duplicate-Key-Errors
```

### ✅ **Validierung 3: Realistische Test-Daten werden generiert**
```bash
# Test einen Customer erstellen und prüfen:
cd backend && ./mvnw test -Dtest=CustomerTestDataFactory -DfailIfNoTests=false
# Company Name MUSS Präfix [TEST] haben
# Customer Number MUSS Format KD-TEST-{RUN_ID}-{SEQ} haben

# Test einen User erstellen und prüfen:
cd backend && ./mvnw test -Dtest=UserTestDataFactory -DfailIfNoTests=false  
# Email MUSS @test.example.com Domain haben
# Username MUSS bei Default eindeutig sein
```

### ✅ **Validierung 4: Builder-API ist fluent und vollständig**
```bash
# Prüfen dass alle with*-Methoden existieren:
grep -c "public Builder with" backend/src/test/java/de/freshplan/test/builders/CustomerTestDataFactory.java
# SOLLTE ergeben: >= 4 (withCompanyName, withCustomerNumber, withStatus, etc.)

grep -c "public Builder with" backend/src/test/java/de/freshplan/test/builders/UserTestDataFactory.java
# SOLLTE ergeben: >= 4 (withUsername, withEmail, withFirstName, etc.)
```

**💡 Erst wenn ALLE 4 Validierungen erfolgreich sind → Phase 2B starten!**

---

## ➡️ Nächste Phase

Nach erfolgreichem Abschluss von Phase 2A:
→ **Phase 2B: Builder Advanced Features** (PHASE_2B_BUILDER_ADVANCED.md)

**Solide Basis ist gelegt - jetzt erweitern wir die Builder-Suite! 🚀**