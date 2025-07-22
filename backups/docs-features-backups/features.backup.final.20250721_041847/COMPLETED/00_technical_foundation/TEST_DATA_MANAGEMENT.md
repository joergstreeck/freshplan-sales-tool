# Test Data Management - Dev Environment Setup ⚡

**Feature Code:** TF-003  
**Feature-Typ:** 🔧 BACKEND  
**Status:** ✅ IMPLEMENTED  
**Komponenten:** CustomerDataInitializer, DevDataInitializer  

---

## 🎯 ZWECK

**Problem:** Leere Datenbank beim Start = nichts zu testen  
**Lösung:** Automatische Test-Daten beim Backend-Start  
**Impact:** Sofort loslegen mit realistischen Daten  

---

## 📊 IMPLEMENTIERTE KOMPONENTEN

### CustomerDataInitializer
```java
@ApplicationScoped
@Startup
public class CustomerDataInitializer {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    ContactRepository contactRepository;
    
    @ConfigProperty(name = "app.init.test-data", defaultValue = "true")
    boolean initTestData;
    
    @ConfigProperty(name = "app.init.customer-count", defaultValue = "50")
    int customerCount;
    
    void onStart(@Observes StartupEvent ev) {
        if (!initTestData || customerRepository.count() > 0) {
            return;
        }
        
        Log.info("Initializing test customers...");
        
        for (int i = 1; i <= customerCount; i++) {
            createTestCustomer(i);
        }
        
        Log.info(f"Created {customerCount} test customers");
    }
    
    private void createTestCustomer(int index) {
        var customer = new Customer();
        customer.name = CUSTOMER_NAMES[index % CUSTOMER_NAMES.length] + " GmbH";
        customer.customerNumber = String.format("K%05d", index);
        customer.industry = INDUSTRIES[index % INDUSTRIES.length];
        customer.revenue = BigDecimal.valueOf(100000 + (index * 10000));
        customer.employees = 10 + (index * 5);
        customer.createdAt = LocalDateTime.now().minusDays(index);
        
        customerRepository.persist(customer);
        
        // Create 1-3 contacts per customer
        int contactCount = 1 + (index % 3);
        for (int j = 0; j < contactCount; j++) {
            createTestContact(customer, j);
        }
    }
}
```

### DevDataInitializer
```java
@ApplicationScoped
@Startup
public class DevDataInitializer {
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    SecurityService securityService;
    
    void onStart(@Observes StartupEvent ev) {
        if (!"dev".equals(ProfileManager.getActiveProfile())) {
            return;
        }
        
        createTestUsers();
        createTestActivities();
        createTestOpportunities();
    }
    
    private void createTestUsers() {
        if (userRepository.count() == 0) {
            // Admin user
            createUser("admin", "Admin", "User", Role.ADMIN);
            
            // Sales manager
            createUser("manager", "Sales", "Manager", Role.MANAGER);
            
            // Sales reps
            createUser("thomas", "Thomas", "Müller", Role.SALES);
            createUser("sarah", "Sarah", "Schmidt", Role.SALES);
            createUser("michael", "Michael", "Weber", Role.SALES);
            
            Log.info("Created 5 test users");
        }
    }
}
```

---

## 🔧 KONFIGURATION

### application.properties
```properties
# Test Data Configuration
%dev.app.init.test-data=true
%dev.app.init.customer-count=50
%dev.app.init.activity-count=200
%dev.app.init.opportunity-count=30

# Disable in production
%prod.app.init.test-data=false

# Test-specific settings
%test.app.init.test-data=true
%test.app.init.customer-count=10
```

### Realistische Test-Daten
```java
private static final String[] CUSTOMER_NAMES = {
    "Müller", "Schmidt", "Schneider", "Fischer", "Weber",
    "Meyer", "Wagner", "Becker", "Schulz", "Hoffmann"
};

private static final String[] INDUSTRIES = {
    "Gastronomie", "Einzelhandel", "Großhandel", 
    "Produktion", "Dienstleistung", "IT"
};

private static final String[] CITIES = {
    "Berlin", "Hamburg", "München", "Köln", "Frankfurt",
    "Stuttgart", "Düsseldorf", "Leipzig", "Dortmund"
};
```

---

## 🚀 VERWENDUNG

### Automatischer Start
```bash
# Backend starten - Daten werden automatisch erstellt
cd backend && ./mvnw quarkus:dev

# Logs zeigen:
INFO  [CustomerDataInitializer] Created 50 test customers
INFO  [DevDataInitializer] Created 5 test users
```

### Daten zurücksetzen
```bash
# Datenbank leeren und neu initialisieren
cd backend
./mvnw flyway:clean flyway:migrate
./mvnw quarkus:dev
```

### Custom Test-Daten
```bash
# Mit Environment Variables
INIT_CUSTOMER_COUNT=100 ./mvnw quarkus:dev

# Oder in application.properties
%dev.app.init.customer-count=100
```

---

## ✅ VORTEILE

1. **Sofort startbereit** - Keine manuelle Dateneingabe
2. **Realistische Daten** - Deutsche Namen/Städte
3. **Reproduzierbar** - Immer gleiche Testbasis
4. **Konfigurierbar** - Anzahl anpassbar
5. **Umgebungs-spezifisch** - Nur in Dev/Test

---

## 📊 GENERIERTE DATEN

**Bei Standard-Konfiguration:**
- 50 Kunden mit je 1-3 Kontakten
- 5 Test-User (admin, manager, 3x sales)
- 200 Activities (Anrufe, Meetings, E-Mails)
- 30 Opportunities in verschiedenen Stages
- Realistische Zeitstempel (letzte 90 Tage)

---

**Erweiterung:** [TEST_DATA_SCENARIOS.md](/docs/features/ACTIVE/01_security/TEST_DATA_SCENARIOS.md)  
**API Testing:** [TEST_API_COLLECTION.md](/docs/features/ACTIVE/01_security/TEST_API_COLLECTION.md)