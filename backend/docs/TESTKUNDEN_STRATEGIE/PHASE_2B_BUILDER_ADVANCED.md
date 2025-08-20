# Phase 2B: Builder Advanced Features

**Aufwand:** 20 Minuten  
**Ziel:** ContactTestDataFactory + OpportunityTestDataFactory + CDI-Integration  
**Ergebnis:** Vollständige Builder-Suite mit Enterprise-Features

---

## 🎯 Überblick

Diese Phase vervollständigt die Builder-Suite:
- **ContactTestDataFactory** - Contact-Management Tests
- **OpportunityTestDataFactory** - Sales-Pipeline Tests  
- **ContactBuilder (CDI)** - Integration-Test Support
- **Persist-Methoden** - Database-Integration

**Resultat:** Enterprise-ready Builder-Suite für alle Test-Szenarien!

**📋 Verweis auf Recherche:** Siehe [RESEARCH_RESULTS.md - Builder/Factory-Infrastruktur](./RESEARCH_RESULTS.md#🏗️-builderfactory-infrastruktur-zu-verbessern-in-phase-2)

---

## 📞 Schritt 1: ContactTestDataFactory verbessern

### 1.1 Status prüfen

**Datei analysieren:**
```bash
# Prüfen aktueller Zustand
grep -A 10 -B 5 "build()" backend/src/test/java/de/freshplan/test/builders/ContactTestDataFactory.java

# Prüfen ob isTestData gesetzt wird
grep -n "isTestData" backend/src/test/java/de/freshplan/test/builders/ContactTestDataFactory.java
```

### 1.2 Contact-Builder erweitern

**Datei: `backend/src/test/java/de/freshplan/test/builders/ContactTestDataFactory.java`**

**Mock Customer-Erstellung verbessern:**
```java
/**
 * Erstellt Mock-Customer für Contact-Tests.
 * Nutzt isTestData=true und eindeutige IDs.
 */
private Customer createMockCustomer() {
    Customer mockCustomer = new Customer();
    mockCustomer.setId(UUID.randomUUID());
    mockCustomer.setCompanyName("[TEST] Mock Customer for Contact");
    
    // Eindeutige Customer Number für Contact-Tests
    String runId = System.getProperty("test.run.id", "LOCAL");
    mockCustomer.setCustomerNumber("TEST-MOCK-" + runId + "-" + UUID.randomUUID().toString().substring(0, 8));
    mockCustomer.setIsTestData(true); // KRITISCH für Cleanup
    mockCustomer.setStatus("ACTIVE");
    
    // Audit-Felder
    mockCustomer.setCreatedAt(LocalDateTime.now());
    mockCustomer.setCreatedBy("test-system");
    
    return mockCustomer;
}
```

**Build-Methode erweitern:**
```java
public CustomerContact build() {
    // Sicherstellen dass Customer gesetzt ist
    if (customer == null) {
        customer = createMockCustomer();
    }
    
    CustomerContact contact = new CustomerContact();
    contact.setFirstName(firstName);
    contact.setLastName(lastName);
    contact.setEmail(email);
    contact.setPhone(phone);
    contact.setPosition(position);
    contact.setCustomer(customer);
    contact.setIsPrimary(isPrimary);
    contact.setIsActive(isActive);
    
    // WICHTIG: Contact inherits test data status from customer
    if (customer.getIsTestData() != null) {
        contact.setIsTestData(customer.getIsTestData());
    }
    
    // Audit-Felder
    contact.setCreatedAt(LocalDateTime.now());
    contact.setCreatedBy("test-system");
    
    return contact;
}
```

**Eindeutige Email-Generierung:**
```java
private static final AtomicLong CONTACT_SEQ = new AtomicLong();

/**
 * Generiert eindeutige Email für Contact-Tests.
 */
private String generateUniqueEmail() {
    String runId = System.getProperty("test.run.id", "LOCAL");
    long seq = CONTACT_SEQ.incrementAndGet();
    return "contact." + runId + "." + seq + "@test.example.com";
}

// In der Builder-Klasse Konstruktor:
public Builder() {
    this.email = generateUniqueEmail(); // Default unique email
    this.firstName = "Test";
    this.lastName = "Contact";
    this.position = "Test Position";
    this.isPrimary = false;
    this.isActive = true;
}
```

---

## 💼 Schritt 2: OpportunityTestDataFactory erweitern

### 2.1 Opportunity.isTestData prüfen

**Prüfen ob Opportunity Entity isTestData-Feld hat:**
```bash
# Suchen ob isTestData existiert
grep -n "isTestData" backend/src/main/java/de/freshplan/domain/opportunity/entity/Opportunity.java

# Falls nicht gefunden - das ist OK, dann skip wir das Feld
```

### 2.2 Opportunity-Builder verbessern

**Datei: `backend/src/test/java/de/freshplan/test/builders/OpportunityTestDataFactory.java`**

**Erweiterte Build-Methode:**
```java
public Opportunity build() {
    Opportunity opportunity = new Opportunity(name, stage, assignedTo);
    
    // Bestehenden Code beibehalten...
    opportunity.setValue(value);
    opportunity.setProbability(probability);
    opportunity.setExpectedCloseDate(expectedCloseDate);
    opportunity.setDescription(description);
    opportunity.setSource(source);
    
    // Test-Data-Flag setzen falls Feld existiert
    setTestDataFlagIfExists(opportunity);
    
    // Audit-Felder für Tests
    opportunity.setCreatedAt(LocalDateTime.now());
    opportunity.setCreatedBy("test-system");
    opportunity.setUpdatedAt(LocalDateTime.now());
    opportunity.setUpdatedBy("test-system");
    
    return opportunity;
}

/**
 * Setzt isTestData=true falls das Feld existiert.
 * Failsafe approach für optionales Feld.
 */
private void setTestDataFlagIfExists(Opportunity opportunity) {
    try {
        java.lang.reflect.Method setter = opportunity.getClass().getMethod("setIsTestData", Boolean.class);
        setter.invoke(opportunity, true);
    } catch (Exception e) {
        // Field doesn't exist - that's ok
        System.out.println("INFO: Opportunity.isTestData field not found - skipping");
    }
}
```

**Eindeutige Opportunity-Namen:**
```java
private static final AtomicLong OPP_SEQ = new AtomicLong();

/**
 * Generiert eindeutige Opportunity-Namen für Tests.
 */
private String generateUniqueName() {
    String runId = System.getProperty("test.run.id", "LOCAL");
    long seq = OPP_SEQ.incrementAndGet();
    return "TEST Opportunity " + runId + "-" + String.format("%03d", seq);
}

// Default-Konstruktor erweitern:
public Builder() {
    this.name = generateUniqueName();
    this.stage = OpportunityStage.NEW_LEAD;
    this.assignedTo = "test-user";
    this.value = 10000.0;
    this.probability = 25;
    this.expectedCloseDate = LocalDate.now().plusDays(30);
    this.source = "TEST";
}
```

---

## ⚙️ Schritt 3: ContactBuilder CDI-Integration stärken

### 3.1 CDI-Builder für Integration-Tests

**Datei: `backend/src/test/java/de/freshplan/test/builders/ContactBuilder.java`**

**Repository-Injection und Persist-Methoden:**
```java
@ApplicationScoped
public class ContactBuilder {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject 
    ContactRepository contactRepository; // Falls existiert
    
    /**
     * Create contact with auto-generated test customer.
     * Für Integration-Tests mit echter Database.
     */
    @Transactional
    public CustomerContact createTestContact() {
        // Customer erstellen und persistieren
        Customer customer = CustomerTestDataFactory.builder()
            .withCompanyName("[TEST] Auto-Generated Customer for Contact")
            .build();
        customerRepository.persistAndFlush(customer);
            
        // Contact erstellen und persistieren
        CustomerContact contact = ContactTestDataFactory.builder()
            .forCustomer(customer)
            .withEmail("auto.contact." + System.currentTimeMillis() + "@test.com")
            .build();
            
        // Falls ContactRepository existiert
        if (contactRepository != null) {
            contactRepository.persistAndFlush(contact);
        } else {
            // Fallback via EntityManager
            customerRepository.getEntityManager().persist(contact);
            customerRepository.getEntityManager().flush();
        }
        
        return contact;
    }
    
    /**
     * Create contact for existing customer.
     * Für Tests die bereits einen Customer haben.
     */
    @Transactional
    public CustomerContact createContactForCustomer(Customer customer) {
        CustomerContact contact = ContactTestDataFactory.builder()
            .forCustomer(customer)
            .withEmail("contact." + customer.getCustomerNumber() + "." + System.currentTimeMillis() + "@test.com")
            .build();
            
        if (contactRepository != null) {
            contactRepository.persistAndFlush(contact);
        } else {
            customerRepository.getEntityManager().persist(contact);
            customerRepository.getEntityManager().flush();
        }
        
        return contact;
    }
}
```

---

## 🔧 Schritt 4: Persist-Methoden für alle Builder

### 4.1 Generic Persist-Helper

**Neuer Helper für alle Builder (Optional):**
```java
// In CustomerTestDataFactory.Builder ergänzen:
/**
 * Build and persist to database. 
 * Für Integration-Tests mit Repository-Injection.
 */
public Customer buildAndPersist(CustomerRepository repository) {
    Customer customer = build();
    repository.persistAndFlush(customer);
    return customer;
}

// In ContactTestDataFactory.Builder ergänzen:
/**
 * Build and persist contact with customer.
 * Erstellt auch Customer falls nötig.
 */
public CustomerContact buildAndPersist(CustomerRepository customerRepo, EntityManager em) {
    CustomerContact contact = build();
    
    // Customer persistieren falls nicht schon gespeichert
    if (contact.getCustomer().getId() == null) {
        customerRepo.persistAndFlush(contact.getCustomer());
    }
    
    // Contact persistieren
    em.persist(contact);
    em.flush();
    
    return contact;
}
```

---

## ✅ Schritt 5: Validierung

### 5.1 Contact-Builder funktioniert
```bash
# Prüfen dass ContactTestDataFactory erweitert wurde
grep -c "setIsTestData" backend/src/test/java/de/freshplan/test/builders/ContactTestDataFactory.java
# SOLLTE ergeben: >= 1 (falls CustomerContact.isTestData existiert)

# Prüfen eindeutige Email-Generierung
grep -c "generateUniqueEmail" backend/src/test/java/de/freshplan/test/builders/ContactTestDataFactory.java
# MUSS ergeben: >= 1
```

### 5.2 Opportunity-Builder erweitert
```bash
# Prüfen dass OpportunityTestDataFactory verbessert wurde
grep -c "generateUniqueName" backend/src/test/java/de/freshplan/test/builders/OpportunityTestDataFactory.java
# MUSS ergeben: >= 1

# Prüfen Audit-Felder
grep -c "setCreatedBy.*test-system" backend/src/test/java/de/freshplan/test/builders/OpportunityTestDataFactory.java
# SOLLTE ergeben: >= 1
```

### 5.3 CDI-Integration funktioniert
```bash
# Prüfen dass ContactBuilder CDI-Annotation hat
grep -c "@ApplicationScoped" backend/src/test/java/de/freshplan/test/builders/ContactBuilder.java
# MUSS ergeben: >= 1

# Prüfen Repository-Injection
grep -c "@Inject" backend/src/test/java/de/freshplan/test/builders/ContactBuilder.java
# SOLLTE ergeben: >= 2 (CustomerRepository + ContactRepository)
```

### 5.4 Integration-Test ausführen
```bash
# Test dass CDI-Builder funktioniert
cd backend
./mvnw test -Dtest=ContactRepositoryTest -DfailIfNoTests=false
# MUSS grün laufen

# Test Contact-Creation
./mvnw test -Dtest=ContactCommandServiceTest -DfailIfNoTests=false  
# SOLLTE grün laufen
```

---

## 🎯 Erfolgskriterien - Definition of Done (messbar)

**Phase 2B ist NUR DANN abgeschlossen, wenn alle Validierungen grün sind:**

### ✅ **Validierung 1: Contact + Opportunity Builder erweitert**
```bash
find backend/src/test -name "*TestDataFactory.java" | wc -l
# MUSS ergeben: >= 4 (Customer, User, Contact, Opportunity)

grep -r "generateUnique" backend/src/test/java/de/freshplan/test/builders/ | wc -l
# SOLLTE ergeben: >= 3 (Customer, Contact, Opportunity haben unique generation)
```

### ✅ **Validierung 2: CDI-Integration funktioniert**
```bash
cd backend && ./mvnw test -Dtest=ContactRepositoryTest -DfailIfNoTests=false
# MUSS grün laufen ohne CDI-Errors

grep -c "@ApplicationScoped" backend/src/test/java/de/freshplan/test/builders/ContactBuilder.java
# MUSS ergeben: >= 1
```

### ✅ **Validierung 3: Persist-Methoden verfügbar**
```bash
grep -r "buildAndPersist" backend/src/test/java/de/freshplan/test/builders/ | wc -l
# SOLLTE ergeben: >= 2 (Customer + Contact haben persist methods)

grep -r "@Transactional" backend/src/test/java/de/freshplan/test/builders/ | wc -l
# SOLLTE ergeben: >= 1 (ContactBuilder hat Transactional methods)
```

### ✅ **Validierung 4: Test-Data-Flags gesetzt wo möglich**
```bash
# Prüfe dass isTestData überall gesetzt wird wo das Feld existiert:
grep -r "setIsTestData(true)" backend/src/test/java/de/freshplan/test/builders/ | wc -l
# SOLLTE ergeben: >= 2 (Customer + User + Contact falls verfügbar)

# Prüfe Reflection-basierte Behandlung für Opportunity:
grep -c "setTestDataFlagIfExists" backend/src/test/java/de/freshplan/test/builders/OpportunityTestDataFactory.java
# SOLLTE ergeben: >= 1
```

**💡 Erst wenn ALLE 4 Validierungen erfolgreich sind → Phase 3A starten!**

---

## ➡️ Nächste Phase

Nach erfolgreichem Abschluss von Phase 2B:
→ **Phase 3A: Core-Tests taggen** (PHASE_3A_CORE_TAGS.md)

**Builder-Suite ist komplett - jetzt können wir Tests systematisch taggen! 🚀**