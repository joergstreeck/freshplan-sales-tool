# 🏗️ M5 Customer Refactor - CLAUDE TECH ⚡

**Feature-Typ:** 🔀 FULLSTACK  
**Komplexität:** MITTEL | **Aufwand:** 6-8 Tage | **Priorität:** HIGH  
**Status:** Ready | **Version:** 2.0 | **Letzte Aktualisierung:** 21.07.2025

## ⚡ QUICK-LOAD (30 Sekunden zur Produktivität)

**Was:** Modulare Customer Domain mit separaten Entities für Contacts, Addresses, Financials und Tags
**Warum:** Refactoring von monolithischem Customer Entity zu modularer Domain-Struktur für bessere Wartbarkeit
**Für wen:** Entwickler die Customer Management erweitern und bestehende Architektur verbessern wollen

### 🎯 Sofort loslegen:
```bash
# 1. Schema Migration
cat > backend/src/main/resources/db/migration/V10__customer_refactor.sql
./mvnw flyway:migrate

# 2. Core Entity
cp CustomerRefactorTemplates/Customer.java backend/src/main/java/de/freshplan/domain/customer/entity/

# 3. Service Layer
cp CustomerRefactorTemplates/CustomerService.java backend/src/main/java/de/freshplan/domain/customer/service/
```

### 📋 Copy-Paste Ready Recipes:

#### Neue Customer Entity (Modular):
```java
@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class Customer {
    @Id @GeneratedValue private UUID id;
    
    @Embedded private CustomerBasicInfo basicInfo;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contact> contacts = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();
    
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerFinancials financials;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerTag> tags = new ArrayList<>();
    
    // Business Methods
    public Contact getPrimaryContact() {
        return contacts.stream().filter(Contact::isPrimary).findFirst().orElse(null);
    }
    
    public Address getPrimaryAddress() {
        return addresses.stream().filter(Address::isPrimary).findFirst().orElse(null);
    }
    
    public boolean hasTag(String tagName) {
        return tags.stream().anyMatch(tag -> tag.getTagName().equalsIgnoreCase(tagName));
    }
}
```

#### Contact Entity (Separate):
```java
@Entity
@Table(name = "customer_contacts")
public class Contact {
    @Id @GeneratedValue private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    private String firstName, lastName, title, department, email, phone, mobile;
    
    @Enumerated(EnumType.STRING)
    private ContactRole contactRole = ContactRole.CONTACT;
    
    @Enumerated(EnumType.STRING)
    private CommunicationMethod preferredCommunicationMethod = CommunicationMethod.EMAIL;
    
    private Boolean isActive = true;
    private LocalDateTime lastContactDate;
    
    public String getFullName() {
        return Stream.of(firstName, lastName)
            .filter(Objects::nonNull)
            .filter(s -> !s.isBlank())
            .collect(Collectors.joining(" "));
    }
    
    public boolean isPrimary() { return contactRole == ContactRole.PRIMARY; }
}

enum ContactRole {
    PRIMARY("Hauptansprechpartner"), DECISION_MAKER("Entscheider"), 
    INFLUENCER("Einflussnehmer"), CONTACT("Kontaktperson");
    
    private final String displayName;
    ContactRole(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
```

#### Enhanced Repository:
```java
@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, UUID> {
    
    public List<Customer> findActiveCustomers() {
        return find("deletedAt IS NULL AND basicInfo.type != ?1", CustomerType.INACTIVE).list();
    }
    
    public List<Customer> searchByName(String searchTerm) {
        return find("LOWER(basicInfo.name) LIKE LOWER(?1) AND deletedAt IS NULL", 
                   "%" + searchTerm + "%").list();
    }
    
    public List<Customer> findHighValueCustomers(BigDecimal minRevenue) {
        return find("SELECT c FROM Customer c JOIN c.financials f " +
                   "WHERE f.totalRevenue >= ?1 AND c.deletedAt IS NULL " +
                   "ORDER BY f.totalRevenue DESC", minRevenue).list();
    }
    
    public List<Customer> findCustomersWithTag(String tagName) {
        return find("SELECT DISTINCT c FROM Customer c JOIN c.tags t " +
                   "WHERE LOWER(t.tagName) = LOWER(?1) AND c.deletedAt IS NULL", tagName).list();
    }
    
    public void softDelete(UUID customerId, User deletedBy) {
        Customer customer = findById(customerId);
        if (customer != null) {
            customer.setDeletedAt(LocalDateTime.now());
            customer.setDeletedBy(deletedBy);
            persist(customer);
        }
    }
}
```

#### Service Layer (Enhanced):
```java
@ApplicationScoped
@Transactional
public class CustomerService {
    
    @Inject CustomerRepository customerRepository;
    @Inject SecurityContextProvider securityProvider;
    @Inject CustomerMapper customerMapper;
    
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        Customer customer = new Customer();
        
        // Set basic info
        CustomerBasicInfo basicInfo = new CustomerBasicInfo();
        basicInfo.setName(request.getName());
        basicInfo.setLegalForm(request.getLegalForm());
        basicInfo.setType(request.getType() != null ? request.getType() : CustomerType.PROSPECT);
        customer.setBasicInfo(basicInfo);
        
        customer.setCreatedBy(currentUser);
        customer.setAssignedTo(request.getAssignedTo() != null ? request.getAssignedTo() : currentUser);
        
        // Add primary contact if provided
        if (request.getPrimaryContact() != null) {
            Contact contact = customerMapper.toContact(request.getPrimaryContact());
            contact.setContactRole(ContactRole.PRIMARY);
            customer.addContact(contact);
        }
        
        customerRepository.persist(customer);
        return customerMapper.toResponse(customer);
    }
    
    public ContactResponse addContact(UUID customerId, CreateContactRequest request) {
        Customer customer = findCustomerById(customerId);
        Contact contact = customerMapper.toContact(request);
        customer.addContact(contact);
        customerRepository.persist(customer);
        return customerMapper.toContactResponse(contact);
    }
    
    public void addTag(UUID customerId, String tagName, String category) {
        Customer customer = findCustomerById(customerId);
        if (!customer.hasTag(tagName)) {
            customer.addTag(tagName, category);
            customerRepository.persist(customer);
        }
    }
    
    public CustomerSegment calculateSegment(UUID customerId) {
        Customer customer = findCustomerById(customerId);
        if (customer.getFinancials() == null) return CustomerSegment.C;
        
        BigDecimal totalRevenue = customer.getFinancials().getTotalRevenue();
        if (totalRevenue.compareTo(BigDecimal.valueOf(500000)) > 0) return CustomerSegment.A;
        else if (totalRevenue.compareTo(BigDecimal.valueOf(100000)) > 0) return CustomerSegment.B;
        else return CustomerSegment.C;
    }
}
```

#### Database Migration:
```sql
-- Core Customer Table (refactored)
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    legal_form VARCHAR(50), -- GmbH, AG, KG, UG
    industry VARCHAR(100),
    customer_type VARCHAR(20) DEFAULT 'PROSPECT',
    customer_segment VARCHAR(10) DEFAULT 'C',
    created_by UUID REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 1,
    deleted_at TIMESTAMP,
    deleted_by UUID REFERENCES users(id)
);

-- Contacts Table (separate entity)
CREATE TABLE customer_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100), last_name VARCHAR(100),
    title VARCHAR(100), department VARCHAR(100),
    email VARCHAR(255), phone VARCHAR(50), mobile VARCHAR(50),
    preferred_communication_method VARCHAR(20) DEFAULT 'EMAIL',
    language_preference VARCHAR(10) DEFAULT 'DE',
    contact_role VARCHAR(50) DEFAULT 'CONTACT',
    is_active BOOLEAN DEFAULT true,
    last_contact_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer Financials (sensitive data separate)
CREATE TABLE customer_financials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    credit_limit DECIMAL(12, 2),
    payment_terms INTEGER DEFAULT 30,
    credit_rating VARCHAR(10), -- AAA, AA, A, BBB, BB, B, CCC
    credit_score INTEGER, -- 0-1000
    total_revenue DECIMAL(12, 2) DEFAULT 0.00,
    avg_order_value DECIMAL(10, 2),
    risk_category VARCHAR(20) DEFAULT 'LOW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer Tags (flexible categorization)
CREATE TABLE customer_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    tag_name VARCHAR(100) NOT NULL,
    tag_category VARCHAR(50), -- INDUSTRY, SIZE, PRIORITY, CUSTOM
    tag_color VARCHAR(7), -- Hex Color
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(customer_id, tag_name)
);

-- Performance Indexes
CREATE INDEX idx_customers_name ON customers(name);
CREATE INDEX idx_customers_type_segment ON customers(customer_type, customer_segment);
CREATE INDEX idx_customer_contacts_customer_id ON customer_contacts(customer_id);
CREATE INDEX idx_customer_contacts_email ON customer_contacts(email);
CREATE INDEX idx_customer_financials_customer_id ON customer_financials(customer_id);
CREATE INDEX idx_customer_tags_customer_id ON customer_tags(customer_id);
```

## 🏗️ ARCHITEKTUR ÜBERBLICK

### Refactoring Strategy:
```
BESTEHEND (Monolithisch):            →    REFACTORED (Modular):
Customer.java                             Customer.java (Core Entity)
├── name, email, phone                    ├── CustomerBasicInfo (Embedded)
├── address (embedded)                    ├── List<Contact> contacts
├── notes                                 ├── List<Address> addresses  
└── Basic CRUD                            ├── CustomerFinancials financials
                                          ├── CustomerPreferences preferences
                                          └── List<CustomerTag> tags
```

### Domain-Driven Design Approach:
- **Customer Aggregate Root** - Core entity managing all related data
- **Contact Value Objects** - Separate entity for multi-contact management
- **Address Value Objects** - Flexible address system with types
- **Financial Sub-Domain** - Sensitive financial data separated
- **Tag System** - Flexible categorization with colors and categories

## 📊 BUSINESS VALUE

### Performance Verbesserungen:
- **Lazy Loading** für große Customer-Datensätze
- **Indexing Strategy** für häufige Query-Patterns
- **Optimized Queries** für Search und Filtering
- **Caching** für frequently accessed Customer data

### Data Quality:
- **Referential Integrity** zwischen allen Entities
- **Validation Rules** für alle Customer-Daten
- **Audit Trail** für Customer-Änderungen
- **Soft Delete** mit Wiederherstellungs-Funktionalität

### Erweiterbarkeit:
- **Modulare Struktur** ermöglicht einfache Feature-Erweiterung
- **Tag System** für flexible Kategorisierung
- **Contact Roles** für komplexe Organisationsstrukturen
- **Financial Separation** für sensitive Daten-Handling

## 🔄 ABHÄNGIGKEITEN

### Benötigt:
- **FC-008 Security Foundation** - User Context für Customer Operations ✅
- **Basic Customer Entity** - Existing Customer.java als Refactoring-Basis ✅

### Ermöglicht:
- **FC-001 Customer Acquisition** - Enhanced Customer Domain
- **FC-010 Customer Import** - Strukturierte Import-Targets
- **FC-013 Duplicate Detection** - Erweiterte Daten für Duplikat-Erkennung
- **FC-011 Bonitätsprüfung** - CustomerFinancials für Credit Checks
- **FC-014 Activity Timeline** - CustomerNote System für Activities

### Integriert mit:
- **M4 Opportunity Pipeline** - Customer-Opportunity Relationships
- **FC-037 Advanced Reporting** - Enhanced Customer Analytics
- **All Customer Features** - Foundation für Customer-Funktionalitäten

## 🧪 TESTING STRATEGY

### Unit Tests:
```java
@QuarkusTest
class CustomerTest {
    @Test void testCustomerCreation() {
        CustomerBasicInfo basicInfo = new CustomerBasicInfo();
        basicInfo.setName("Test GmbH");
        basicInfo.setType(CustomerType.PROSPECT);
        
        Customer customer = new Customer();
        customer.setBasicInfo(basicInfo);
        
        assertEquals("Test GmbH", customer.getBasicInfo().getName());
        assertTrue(customer.isActive());
    }
    
    @Test void testContactManagement() {
        Customer customer = createTestCustomer();
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setContactRole(ContactRole.PRIMARY);
        
        customer.addContact(contact);
        
        assertEquals(1, customer.getContacts().size());
        assertEquals("John Doe", customer.getPrimaryContact().getFullName());
    }
}
```

### Integration Tests:
```java
@QuarkusTest
@TestSecurity(user = "testuser", roles = {"admin"})
class CustomerResourceTest {
    @Test void testCreateCustomer() {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
            .name("Integration Test Corp")
            .legalForm("GmbH")
            .build();
        
        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/api/customers")
        .then()
            .statusCode(201)
            .body("name", equalTo("Integration Test Corp"));
    }
}
```

## 🚀 IMPLEMENTATION PLAN

### Phase 1: Database Schema & Entities (3 Tage)
- Database Migration für neue Tabellen
- Entity Classes mit JPA Mappings
- Enum Classes für alle Domain Types
- Data Migration von bestehenden Customer-Daten

### Phase 2: Repository & Service Layer (2 Tage)
- Enhanced CustomerRepository mit Complex Queries
- Modular CustomerService für alle Sub-Entities
- Business Logic für Customer Segmentation
- Mapper Classes für Entity-DTO Conversion

### Phase 3: API Enhancement (1.5 Tage)
- CustomerResource Enhancement für alle Module
- Request/Response DTOs für strukturierte Data Transfer
- Validation für Input und Business Rules
- API Documentation mit OpenAPI

### Phase 4: Testing & Migration (1.5 Tage)
- Unit Tests für alle Entities und Services
- Integration Tests für API Endpoints
- Data Migration Testing für bestehende Daten
- Performance Testing und Query Optimization

## 📈 SUCCESS CRITERIA

### Funktionale Anforderungen:
- ✅ Modulare Customer Domain mit separaten Entities
- ✅ Flexible Contact Management mit Rollen
- ✅ Multi-Address Support für verschiedene Types
- ✅ Strukturierte Financial Data mit Credit Rating
- ✅ Flexible Tagging System für Categorization
- ✅ Soft Delete mit Audit Trail

### Performance Anforderungen:
- ✅ Customer Search < 200ms P95
- ✅ Customer Detail Load < 300ms P95
- ✅ Complex Queries < 500ms P95
- ✅ Database Indexes für alle Query Patterns

### Data Quality:
- ✅ Referential Integrity zwischen allen Entities
- ✅ Validation Rules für alle Customer Data
- ✅ Audit Trail für alle Customer Changes
- ✅ Data Migration ohne Datenverlust

---
**🏗️ M5 Customer Refactor - Ready for Modular Enhancement!**  
**Optimiert für:** 6-8 Tage | **Reduktion:** Monolithische → Modulare Architektur | **Performance:** < 200ms Customer Search