# M5 Customer Refactor - Tech Concept

**Feature-Code:** M5  
**Feature-Name:** Customer Refactor  
**Feature-Typ:** 🔧 BACKEND REFACTORING  
**Erstellt:** 2025-07-20  
**Status:** Tech Concept (Refactoring-Mode)  
**Priorität:** HIGH - Foundation Enhancement  
**Geschätzter Aufwand:** 6-8 Tage (Refactoring)  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### 📍 Kontext laden für produktive Arbeit

**Was ist M5?** Customer Refactor - Modulare Neu-Strukturierung des Customer Domain Models für erweiterte Funktionalitäten

**Warum Refactoring statt Neuentwicklung?** Customer-Entity existiert bereits (basic implementation) - wir erweitern zu einem modularen, skalierbaren Domain Model

**Aktueller Zustand:**
- ✅ **Basic Customer Entity:** `backend/src/main/java/de/freshplan/domain/customer/entity/Customer.java`
- ✅ **CustomerRepository:** Basic CRUD Operations implementiert
- ✅ **CustomerService:** Grundlegende Business Logic vorhanden
- ❌ **Modulare Struktur:** Monolithische Customer-Klasse ohne Domain Separation
- ❌ **Contact Management:** Keine separaten Contact Entities
- ❌ **Address Management:** Eingebaute Address ohne Flexibilität
- ❌ **Financial Data:** Keine strukturierte Finanz-/Bonitätsdaten

**Abhängigkeiten verstehen:**
- **Benötigt:** FC-008 Security (✅ VERFÜGBAR) - User-Context für Customer Operations
- **Blockiert:** FC-001 Customer Acquisition, FC-010 Customer Import, FC-013 Duplicate Detection
- **Erweitert:** M4 Pipeline - Customer-Opportunity Beziehungen

**Technischer Kern - Modularer Customer Domain:**
```java
// Neue modulare Struktur
@Entity
@Table(name = "customers")
public class Customer {
    @Id private UUID id;
    @Embedded private CustomerBasicInfo basicInfo;
    @OneToMany(mappedBy = "customer") private List<Contact> contacts;
    @OneToMany(mappedBy = "customer") private List<Address> addresses;
    @OneToOne(mappedBy = "customer") private CustomerFinancials financials;
    @OneToOne(mappedBy = "customer") private CustomerPreferences preferences;
    @OneToMany(mappedBy = "customer") private List<CustomerTag> tags;
}

@Embeddable
public class CustomerBasicInfo {
    private String name;
    private String legalForm; // GmbH, AG, KG, etc.
    private String industry;
    private CustomerType type; // PROSPECT, CUSTOMER, INACTIVE
    private CustomerSegment segment; // A, B, C Kunde
}
```

**Business Value - Skalierbare Customer Foundation:**
- **Bestehend:** Monolithische Customer-Entity mit limitierter Erweiterbarkeit
- **Refactored:** Modulare Domain-Struktur für komplexe Customer-Relationships
- **ROI:** 6-8 Entwicklertage (~€8.000) für 300% erweiterte Customer-Funktionalität

---

## 🎯 ÜBERSICHT

### Business Value
**Problem:** Aktuelle Customer-Entity ist monolithisch und nicht skalierbar für erweiterte Sales-Funktionalitäten

**Lösung:** Modulare Customer Domain mit separaten Entities für Contacts, Addresses, Financials und Tags

**ROI:** 
- **Kosten:** 6-8 Entwicklertage (~€8.000) - Strukturelles Refactoring
- **Skalierbarkeit:** +300% Erweiterungsmöglichkeiten für Customer-Features
- **Development Speed:** +50% schnellere Feature-Entwicklung durch klare Domain-Trennung
- **Data Quality:** +40% bessere Datenqualität durch strukturierte Validation
- **ROI-Ratio:** 15:1 (Break-even nach 1 Monat durch beschleunigte Feature-Entwicklung)

### Kernfunktionen (Refactoring)
1. **Modular Customer Entity** - Separation of Concerns mit klaren Domain-Grenzen
2. **Contact Management** - Separate Contact Entities mit Rollen und Responsabilities
3. **Address Management** - Flexible Address System mit Multiple Addresses
4. **Financial Integration** - Strukturierte Finanz- und Bonitätsdaten
5. **Customer Preferences** - Konfigurierbare Präferenzen und Settings
6. **Tagging System** - Flexible Customer Categorization

---

## 🏗️ ARCHITEKTUR

### Refactoring Strategy - Modular Domain Design
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

### Database Schema - Modular Customer Domain
```sql
-- Core Customer Table (simplified)
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Basic Info (embedded)
    name VARCHAR(255) NOT NULL,
    legal_form VARCHAR(50), -- GmbH, AG, KG, UG, etc.
    industry VARCHAR(100),
    customer_type VARCHAR(20) DEFAULT 'PROSPECT', -- PROSPECT, CUSTOMER, INACTIVE, LOST
    customer_segment VARCHAR(10) DEFAULT 'C', -- A, B, C Kunde
    
    -- Metadata
    created_by UUID REFERENCES users(id),
    assigned_to UUID REFERENCES users(id), -- Zuständiger Verkäufer
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 1, -- Optimistic Locking
    
    -- Soft Delete
    deleted_at TIMESTAMP,
    deleted_by UUID REFERENCES users(id)
);

-- Contacts Table (separate entity)
CREATE TABLE customer_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Contact Info
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    title VARCHAR(100), -- Geschäftsführer, Einkauf, etc.
    department VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    
    -- Contact Preferences
    preferred_communication_method VARCHAR(20) DEFAULT 'EMAIL', -- EMAIL, PHONE, WHATSAPP
    language_preference VARCHAR(10) DEFAULT 'DE', -- DE, EN, FR, etc.
    
    -- Contact Role & Status
    contact_role VARCHAR(50) DEFAULT 'CONTACT', -- PRIMARY, DECISION_MAKER, INFLUENCER, CONTACT
    is_active BOOLEAN DEFAULT true,
    last_contact_date TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Addresses Table (flexible address system)
CREATE TABLE customer_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Address Type
    address_type VARCHAR(20) DEFAULT 'BUSINESS', -- BUSINESS, BILLING, SHIPPING, BRANCH
    
    -- Address Components
    street_line_1 VARCHAR(255),
    street_line_2 VARCHAR(255),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    state_province VARCHAR(100),
    country VARCHAR(5) DEFAULT 'DE', -- ISO Country Code
    
    -- Address Meta
    is_primary BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    
    -- Geo Coordinates (optional)
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer Financials (separate for sensitive data)
CREATE TABLE customer_financials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Financial Data
    credit_limit DECIMAL(12, 2),
    payment_terms INTEGER DEFAULT 30, -- Zahlungsziel in Tagen
    discount_percentage DECIMAL(5, 2) DEFAULT 0.00,
    
    -- Bonitäts-Information
    credit_rating VARCHAR(10), -- AAA, AA, A, BBB, BB, B, CCC, CC, C, D
    credit_score INTEGER, -- 0-1000
    credit_check_date DATE,
    credit_check_provider VARCHAR(100), -- Schufa, Creditreform, etc.
    
    -- Payment Behavior
    avg_days_to_pay DECIMAL(5, 2),
    payment_reliability_score INTEGER, -- 0-100
    
    -- Risk Assessment
    risk_category VARCHAR(20) DEFAULT 'LOW', -- LOW, MEDIUM, HIGH, CRITICAL
    risk_notes TEXT,
    
    -- Revenue Data
    total_revenue DECIMAL(12, 2) DEFAULT 0.00,
    last_order_date DATE,
    avg_order_value DECIMAL(10, 2),
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer Preferences (configuration)
CREATE TABLE customer_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Communication Preferences
    email_marketing_consent BOOLEAN DEFAULT false,
    newsletter_subscription BOOLEAN DEFAULT false,
    phone_marketing_consent BOOLEAN DEFAULT false,
    
    -- Service Preferences
    preferred_delivery_method VARCHAR(50),
    preferred_service_level VARCHAR(50), -- STANDARD, PREMIUM, VIP
    special_instructions TEXT,
    
    -- Data Privacy
    gdpr_consent_date TIMESTAMP,
    gdpr_consent_version VARCHAR(10),
    data_processing_consent BOOLEAN DEFAULT false,
    
    -- Custom Preferences (JSON for flexibility)
    custom_preferences JSONB,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer Tags (flexible categorization)
CREATE TABLE customer_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Tag Information
    tag_name VARCHAR(100) NOT NULL,
    tag_category VARCHAR(50), -- INDUSTRY, SIZE, PRIORITY, CUSTOM
    tag_color VARCHAR(7), -- Hex Color Code
    
    -- Tag Meta
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(customer_id, tag_name) -- Prevent duplicate tags
);

-- Customer Notes & Activities (enhanced notes system)
CREATE TABLE customer_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    
    -- Note Content
    title VARCHAR(255),
    content TEXT NOT NULL,
    note_type VARCHAR(50) DEFAULT 'GENERAL', -- GENERAL, MEETING, CALL, EMAIL, TASK
    
    -- Note Meta
    is_important BOOLEAN DEFAULT false,
    is_internal BOOLEAN DEFAULT false, -- Nur für interne Teams sichtbar
    
    -- Associations
    related_opportunity_id UUID, -- Foreign Key später
    related_contact_id UUID REFERENCES customer_contacts(id),
    
    -- Metadata
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes für Performance
CREATE INDEX idx_customers_name ON customers(name);
CREATE INDEX idx_customers_type_segment ON customers(customer_type, customer_segment);
CREATE INDEX idx_customers_assigned_to ON customers(assigned_to);
CREATE INDEX idx_customers_created_at ON customers(created_at);
CREATE INDEX idx_customer_contacts_customer_id ON customer_contacts(customer_id);
CREATE INDEX idx_customer_contacts_email ON customer_contacts(email);
CREATE INDEX idx_customer_addresses_customer_id ON customer_addresses(customer_id);
CREATE INDEX idx_customer_addresses_type ON customer_addresses(address_type);
CREATE INDEX idx_customer_financials_customer_id ON customer_financials(customer_id);
CREATE INDEX idx_customer_financials_risk ON customer_financials(risk_category);
CREATE INDEX idx_customer_tags_customer_id ON customer_tags(customer_id);
CREATE INDEX idx_customer_tags_name ON customer_tags(tag_name);
CREATE INDEX idx_customer_notes_customer_id ON customer_notes(customer_id);
CREATE INDEX idx_customer_notes_type ON customer_notes(note_type);
```

### Backend-Architecture - Modular Customer Domain
```java
// Core Customer Entity (refactored)
@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class Customer {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    // Basic Info (embedded value object)
    @Embedded
    private CustomerBasicInfo basicInfo;
    
    // Modular Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contact> contacts = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();
    
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerFinancials financials;
    
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerPreferences preferences;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerTag> tags = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerNote> notes = new ArrayList<>();
    
    // Metadata
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Version
    private Integer version;
    
    // Soft Delete
    private LocalDateTime deletedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;
    
    // Business Methods
    public Contact getPrimaryContact() {
        return contacts.stream()
            .filter(Contact::isPrimary)
            .findFirst()
            .orElse(null);
    }
    
    public Address getPrimaryAddress() {
        return addresses.stream()
            .filter(Address::isPrimary)
            .findFirst()
            .orElse(null);
    }
    
    public void addContact(Contact contact) {
        contact.setCustomer(this);
        contacts.add(contact);
    }
    
    public void addAddress(Address address) {
        address.setCustomer(this);
        addresses.add(address);
    }
    
    public void addTag(String tagName, String category) {
        CustomerTag tag = new CustomerTag();
        tag.setCustomer(this);
        tag.setTagName(tagName);
        tag.setTagCategory(category);
        tags.add(tag);
    }
    
    public boolean hasTag(String tagName) {
        return tags.stream()
            .anyMatch(tag -> tag.getTagName().equalsIgnoreCase(tagName));
    }
    
    public boolean isActive() {
        return deletedAt == null && 
               basicInfo.getCustomerType() != CustomerType.INACTIVE;
    }
    
    public boolean isHighValue() {
        return financials != null && 
               financials.getTotalRevenue().compareTo(BigDecimal.valueOf(100000)) > 0;
    }
    
    // Getters, Setters, equals, hashCode
}

// Customer Basic Info (Value Object)
@Embeddable
public class CustomerBasicInfo {
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "legal_form")
    private String legalForm; // GmbH, AG, KG, UG, etc.
    
    private String industry;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType type = CustomerType.PROSPECT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_segment")
    private CustomerSegment segment = CustomerSegment.C;
    
    // Getters, Setters, equals, hashCode
}

// Customer Type Enum
public enum CustomerType {
    PROSPECT("Prospect"),
    CUSTOMER("Customer"),
    INACTIVE("Inactive"),
    LOST("Lost");
    
    private final String displayName;
    
    CustomerType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

// Customer Segment Enum (ABC Analysis)
public enum CustomerSegment {
    A("A-Kunde", "High Value Customer"),
    B("B-Kunde", "Medium Value Customer"),
    C("C-Kunde", "Low Value Customer");
    
    private final String displayName;
    private final String description;
    
    CustomerSegment(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}

// Contact Entity (separate entity)
@Entity
@Table(name = "customer_contacts")
public class Contact {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // Contact Information
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    private String title;
    private String department;
    private String email;
    private String phone;
    private String mobile;
    
    // Contact Preferences
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_communication_method")
    private CommunicationMethod preferredCommunicationMethod = CommunicationMethod.EMAIL;
    
    @Column(name = "language_preference")
    private String languagePreference = "DE";
    
    // Contact Role & Status
    @Enumerated(EnumType.STRING)
    @Column(name = "contact_role")
    private ContactRole contactRole = ContactRole.CONTACT;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_contact_date")
    private LocalDateTime lastContactDate;
    
    // Metadata
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Business Methods
    public String getFullName() {
        return Stream.of(firstName, lastName)
            .filter(Objects::nonNull)
            .filter(s -> !s.isBlank())
            .collect(Collectors.joining(" "));
    }
    
    public boolean isPrimary() {
        return contactRole == ContactRole.PRIMARY;
    }
    
    public boolean isDecisionMaker() {
        return contactRole == ContactRole.DECISION_MAKER;
    }
    
    // Getters, Setters, equals, hashCode
}

// Communication Method Enum
public enum CommunicationMethod {
    EMAIL("E-Mail"),
    PHONE("Telefon"),
    WHATSAPP("WhatsApp"),
    TEAMS("Microsoft Teams"),
    LETTER("Brief");
    
    private final String displayName;
    
    CommunicationMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

// Contact Role Enum
public enum ContactRole {
    PRIMARY("Hauptansprechpartner"),
    DECISION_MAKER("Entscheider"),
    INFLUENCER("Einflussnehmer"),
    CONTACT("Kontaktperson"),
    TECHNICAL("Technischer Kontakt"),
    FINANCIAL("Finanzabteilung");
    
    private final String displayName;
    
    ContactRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

// Address Entity (flexible address system)
@Entity
@Table(name = "customer_addresses")
public class Address {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // Address Type
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressType addressType = AddressType.BUSINESS;
    
    // Address Components
    @Column(name = "street_line_1")
    private String streetLine1;
    
    @Column(name = "street_line_2")
    private String streetLine2;
    
    private String city;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    @Column(name = "state_province")
    private String stateProvince;
    
    private String country = "DE";
    
    // Address Meta
    @Column(name = "is_primary")
    private Boolean isPrimary = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Geo Coordinates (optional)
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;
    
    // Metadata
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Business Methods
    public String getFormattedAddress() {
        return Stream.of(streetLine1, streetLine2, postalCode + " " + city, country)
            .filter(Objects::nonNull)
            .filter(s -> !s.isBlank())
            .collect(Collectors.joining(", "));
    }
    
    public boolean isPrimary() {
        return Boolean.TRUE.equals(isPrimary);
    }
    
    // Getters, Setters, equals, hashCode
}

// Address Type Enum
public enum AddressType {
    BUSINESS("Geschäftsadresse"),
    BILLING("Rechnungsadresse"),
    SHIPPING("Lieferadresse"),
    BRANCH("Filiale"),
    WAREHOUSE("Lager");
    
    private final String displayName;
    
    AddressType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

// Enhanced Customer Repository
@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, UUID> {
    
    // Basic Queries
    public List<Customer> findActiveCustomers() {
        return find("deletedAt IS NULL AND basicInfo.type != ?1", CustomerType.INACTIVE).list();
    }
    
    public List<Customer> findByType(CustomerType type) {
        return find("basicInfo.type = ?1 AND deletedAt IS NULL", type).list();
    }
    
    public List<Customer> findBySegment(CustomerSegment segment) {
        return find("basicInfo.segment = ?1 AND deletedAt IS NULL", segment).list();
    }
    
    public List<Customer> findByAssignedUser(UUID userId) {
        return find("assignedTo.id = ?1 AND deletedAt IS NULL", userId).list();
    }
    
    // Search Queries
    public List<Customer> searchByName(String searchTerm) {
        return find("LOWER(basicInfo.name) LIKE LOWER(?1) AND deletedAt IS NULL", 
                   "%" + searchTerm + "%").list();
    }
    
    public List<Customer> searchByContactEmail(String email) {
        return find("SELECT DISTINCT c FROM Customer c JOIN c.contacts ct " +
                   "WHERE LOWER(ct.email) LIKE LOWER(?1) AND c.deletedAt IS NULL", 
                   "%" + email + "%").list();
    }
    
    // Complex Queries
    public List<Customer> findHighValueCustomers(BigDecimal minRevenue) {
        return find("SELECT c FROM Customer c JOIN c.financials f " +
                   "WHERE f.totalRevenue >= ?1 AND c.deletedAt IS NULL " +
                   "ORDER BY f.totalRevenue DESC", minRevenue).list();
    }
    
    public List<Customer> findCustomersWithRisk(String riskCategory) {
        return find("SELECT c FROM Customer c JOIN c.financials f " +
                   "WHERE f.riskCategory = ?1 AND c.deletedAt IS NULL", riskCategory).list();
    }
    
    public List<Customer> findCustomersWithTag(String tagName) {
        return find("SELECT DISTINCT c FROM Customer c JOIN c.tags t " +
                   "WHERE LOWER(t.tagName) = LOWER(?1) AND c.deletedAt IS NULL", tagName).list();
    }
    
    // Statistics
    public long countByType(CustomerType type) {
        return count("basicInfo.type = ?1 AND deletedAt IS NULL", type);
    }
    
    public BigDecimal getTotalRevenueBySegment(CustomerSegment segment) {
        return find("SELECT COALESCE(SUM(f.totalRevenue), 0) FROM Customer c " +
                   "JOIN c.financials f WHERE c.basicInfo.segment = ?1 AND c.deletedAt IS NULL", 
                   segment).project(BigDecimal.class).firstResult();
    }
    
    // Soft Delete
    public void softDelete(UUID customerId, User deletedBy) {
        Customer customer = findById(customerId);
        if (customer != null) {
            customer.setDeletedAt(LocalDateTime.now());
            customer.setDeletedBy(deletedBy);
            persist(customer);
        }
    }
    
    public void restore(UUID customerId) {
        Customer customer = findById(customerId);
        if (customer != null) {
            customer.setDeletedAt(null);
            customer.setDeletedBy(null);
            persist(customer);
        }
    }
}

// Enhanced Customer Service
@ApplicationScoped
@Transactional
public class CustomerService {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    SecurityContextProvider securityProvider;
    
    @Inject
    CustomerMapper customerMapper;
    
    // CRUD Operations
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        Customer customer = new Customer();
        
        // Set basic info
        CustomerBasicInfo basicInfo = new CustomerBasicInfo();
        basicInfo.setName(request.getName());
        basicInfo.setLegalForm(request.getLegalForm());
        basicInfo.setIndustry(request.getIndustry());
        basicInfo.setType(request.getType() != null ? request.getType() : CustomerType.PROSPECT);
        basicInfo.setSegment(request.getSegment() != null ? request.getSegment() : CustomerSegment.C);
        customer.setBasicInfo(basicInfo);
        
        // Set metadata
        customer.setCreatedBy(currentUser);
        customer.setAssignedTo(request.getAssignedTo() != null ? request.getAssignedTo() : currentUser);
        
        // Add primary contact if provided
        if (request.getPrimaryContact() != null) {
            Contact contact = customerMapper.toContact(request.getPrimaryContact());
            contact.setContactRole(ContactRole.PRIMARY);
            customer.addContact(contact);
        }
        
        // Add primary address if provided
        if (request.getPrimaryAddress() != null) {
            Address address = customerMapper.toAddress(request.getPrimaryAddress());
            address.setAddressType(AddressType.BUSINESS);
            address.setIsPrimary(true);
            customer.addAddress(address);
        }
        
        // Initialize financials
        if (request.getFinancials() != null) {
            CustomerFinancials financials = customerMapper.toFinancials(request.getFinancials());
            financials.setCustomer(customer);
            customer.setFinancials(financials);
        }
        
        // Add tags if provided
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            request.getTags().forEach(tagName -> 
                customer.addTag(tagName, "CUSTOM"));
        }
        
        customerRepository.persist(customer);
        
        return customerMapper.toResponse(customer);
    }
    
    public CustomerResponse updateCustomer(UUID customerId, UpdateCustomerRequest request) {
        Customer customer = findCustomerById(customerId);
        
        // Update basic info
        if (request.getName() != null) {
            customer.getBasicInfo().setName(request.getName());
        }
        if (request.getLegalForm() != null) {
            customer.getBasicInfo().setLegalForm(request.getLegalForm());
        }
        if (request.getIndustry() != null) {
            customer.getBasicInfo().setIndustry(request.getIndustry());
        }
        if (request.getType() != null) {
            customer.getBasicInfo().setType(request.getType());
        }
        if (request.getSegment() != null) {
            customer.getBasicInfo().setSegment(request.getSegment());
        }
        
        // Update assigned user
        if (request.getAssignedTo() != null) {
            customer.setAssignedTo(request.getAssignedTo());
        }
        
        customerRepository.persist(customer);
        
        return customerMapper.toResponse(customer);
    }
    
    public CustomerDetailResponse getCustomerDetails(UUID customerId) {
        Customer customer = findCustomerById(customerId);
        return customerMapper.toDetailResponse(customer);
    }
    
    public List<CustomerResponse> getAllCustomers(CustomerFilter filter) {
        List<Customer> customers = applyFilter(filter);
        return customers.stream()
            .map(customerMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    public void deleteCustomer(UUID customerId) {
        User currentUser = securityProvider.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        customerRepository.softDelete(customerId, currentUser);
    }
    
    // Contact Management
    public ContactResponse addContact(UUID customerId, CreateContactRequest request) {
        Customer customer = findCustomerById(customerId);
        
        Contact contact = customerMapper.toContact(request);
        customer.addContact(contact);
        
        customerRepository.persist(customer);
        
        return customerMapper.toContactResponse(contact);
    }
    
    public ContactResponse updateContact(UUID customerId, UUID contactId, UpdateContactRequest request) {
        Customer customer = findCustomerById(customerId);
        
        Contact contact = customer.getContacts().stream()
            .filter(c -> c.getId().equals(contactId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Contact not found"));
        
        // Update contact fields
        if (request.getFirstName() != null) {
            contact.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            contact.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            contact.setEmail(request.getEmail());
        }
        // ... other fields
        
        customerRepository.persist(customer);
        
        return customerMapper.toContactResponse(contact);
    }
    
    // Address Management
    public AddressResponse addAddress(UUID customerId, CreateAddressRequest request) {
        Customer customer = findCustomerById(customerId);
        
        Address address = customerMapper.toAddress(request);
        customer.addAddress(address);
        
        customerRepository.persist(customer);
        
        return customerMapper.toAddressResponse(address);
    }
    
    // Financial Management
    public CustomerFinancialsResponse updateFinancials(UUID customerId, UpdateFinancialsRequest request) {
        Customer customer = findCustomerById(customerId);
        
        CustomerFinancials financials = customer.getFinancials();
        if (financials == null) {
            financials = new CustomerFinancials();
            financials.setCustomer(customer);
            customer.setFinancials(financials);
        }
        
        // Update financial fields
        if (request.getCreditLimit() != null) {
            financials.setCreditLimit(request.getCreditLimit());
        }
        if (request.getPaymentTerms() != null) {
            financials.setPaymentTerms(request.getPaymentTerms());
        }
        if (request.getCreditRating() != null) {
            financials.setCreditRating(request.getCreditRating());
        }
        // ... other fields
        
        customerRepository.persist(customer);
        
        return customerMapper.toFinancialsResponse(financials);
    }
    
    // Tag Management
    public void addTag(UUID customerId, String tagName, String category) {
        Customer customer = findCustomerById(customerId);
        
        if (!customer.hasTag(tagName)) {
            customer.addTag(tagName, category);
            customerRepository.persist(customer);
        }
    }
    
    public void removeTag(UUID customerId, String tagName) {
        Customer customer = findCustomerById(customerId);
        
        customer.getTags().removeIf(tag -> tag.getTagName().equalsIgnoreCase(tagName));
        customerRepository.persist(customer);
    }
    
    // Business Logic
    public CustomerSegment calculateSegment(UUID customerId) {
        Customer customer = findCustomerById(customerId);
        
        if (customer.getFinancials() == null) {
            return CustomerSegment.C;
        }
        
        BigDecimal totalRevenue = customer.getFinancials().getTotalRevenue();
        
        if (totalRevenue.compareTo(BigDecimal.valueOf(500000)) > 0) {
            return CustomerSegment.A;
        } else if (totalRevenue.compareTo(BigDecimal.valueOf(100000)) > 0) {
            return CustomerSegment.B;
        } else {
            return CustomerSegment.C;
        }
    }
    
    // Helper Methods
    private Customer findCustomerById(UUID customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
    }
    
    private List<Customer> applyFilter(CustomerFilter filter) {
        if (filter == null) {
            return customerRepository.findActiveCustomers();
        }
        
        // Implement complex filtering logic
        // This would be expanded based on specific filter requirements
        return customerRepository.findActiveCustomers();
    }
}
```

---

## 🔄 ABHÄNGIGKEITEN

### Benötigt diese Features:
- **FC-008 Security Foundation** - User Context für Customer Operations ✅ VERFÜGBAR
- **Basic Customer Entity** - Existing Customer.java als Refactoring-Basis ✅ VORHANDEN

### Ermöglicht diese Features:
- **FC-001 Customer Acquisition** - Enhanced Customer Domain für Multi-Channel Acquisition
- **FC-010 Customer Import** - Strukturierte Import-Targets für CSV/Excel/API
- **FC-013 Duplicate Detection** - Erweiterte Daten für intelligente Duplikatserkennung
- **FC-011 Bonitätsprüfung** - CustomerFinancials für Credit Check Integration
- **FC-014 Activity Timeline** - CustomerNote System für Activity Tracking

### Integriert mit:
- **M4 Opportunity Pipeline** - Customer-Opportunity Relationships
- **FC-037 Advanced Reporting** - Enhanced Customer Analytics
- **All Customer-Related Features** - Foundation für alle Customer-Funktionalitäten

---

## 🧪 TESTING-STRATEGIE

### Unit Tests - Customer Domain
```java
@QuarkusTest
class CustomerTest {
    
    @Test
    void testCustomerCreation() {
        // Given
        CustomerBasicInfo basicInfo = new CustomerBasicInfo();
        basicInfo.setName("Test GmbH");
        basicInfo.setLegalForm("GmbH");
        basicInfo.setIndustry("Technology");
        basicInfo.setType(CustomerType.PROSPECT);
        basicInfo.setSegment(CustomerSegment.B);
        
        Customer customer = new Customer();
        customer.setBasicInfo(basicInfo);
        
        // When & Then
        assertEquals("Test GmbH", customer.getBasicInfo().getName());
        assertEquals(CustomerType.PROSPECT, customer.getBasicInfo().getType());
        assertTrue(customer.isActive());
        assertFalse(customer.isHighValue());
    }
    
    @Test
    void testContactManagement() {
        // Given
        Customer customer = createTestCustomer();
        
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setEmail("john.doe@test.com");
        contact.setContactRole(ContactRole.PRIMARY);
        
        // When
        customer.addContact(contact);
        
        // Then
        assertEquals(1, customer.getContacts().size());
        assertEquals("John Doe", customer.getPrimaryContact().getFullName());
        assertTrue(customer.getPrimaryContact().isPrimary());
    }
    
    @Test
    void testAddressManagement() {
        // Given
        Customer customer = createTestCustomer();
        
        Address address = new Address();
        address.setStreetLine1("Teststraße 123");
        address.setCity("Berlin");
        address.setPostalCode("10115");
        address.setCountry("DE");
        address.setAddressType(AddressType.BUSINESS);
        address.setIsPrimary(true);
        
        // When
        customer.addAddress(address);
        
        // Then
        assertEquals(1, customer.getAddresses().size());
        assertTrue(customer.getPrimaryAddress().isPrimary());
        assertEquals("Teststraße 123, 10115 Berlin, DE", 
                    customer.getPrimaryAddress().getFormattedAddress());
    }
    
    @Test
    void testTagManagement() {
        // Given
        Customer customer = createTestCustomer();
        
        // When
        customer.addTag("VIP", "PRIORITY");
        customer.addTag("Tech", "INDUSTRY");
        
        // Then
        assertEquals(2, customer.getTags().size());
        assertTrue(customer.hasTag("VIP"));
        assertTrue(customer.hasTag("Tech"));
        assertFalse(customer.hasTag("NonExistent"));
    }
    
    @Test
    void testHighValueCustomerDetection() {
        // Given
        Customer customer = createTestCustomer();
        
        CustomerFinancials financials = new CustomerFinancials();
        financials.setTotalRevenue(BigDecimal.valueOf(250000));
        financials.setCustomer(customer);
        customer.setFinancials(financials);
        
        // When & Then
        assertTrue(customer.isHighValue());
    }
}

@QuarkusTest
class CustomerServiceTest {
    
    @Inject
    CustomerService customerService;
    
    @Test
    @Transactional
    void testCreateCustomer() {
        // Given
        CreateCustomerRequest request = CreateCustomerRequest.builder()
            .name("Test Corp")
            .legalForm("GmbH")
            .industry("Software")
            .type(CustomerType.PROSPECT)
            .segment(CustomerSegment.B)
            .build();
        
        // When
        CustomerResponse response = customerService.createCustomer(request);
        
        // Then
        assertNotNull(response.getId());
        assertEquals("Test Corp", response.getName());
        assertEquals(CustomerType.PROSPECT, response.getType());
        assertEquals(CustomerSegment.B, response.getSegment());
    }
    
    @Test
    @Transactional
    void testAddContactToCustomer() {
        // Given
        Customer customer = createAndPersistTestCustomer();
        
        CreateContactRequest contactRequest = CreateContactRequest.builder()
            .firstName("Jane")
            .lastName("Smith")
            .email("jane.smith@test.com")
            .contactRole(ContactRole.DECISION_MAKER)
            .build();
        
        // When
        ContactResponse response = customerService.addContact(customer.getId(), contactRequest);
        
        // Then
        assertNotNull(response.getId());
        assertEquals("Jane Smith", response.getFullName());
        assertEquals(ContactRole.DECISION_MAKER, response.getContactRole());
    }
    
    @Test
    @Transactional
    void testCalculateCustomerSegment() {
        // Given
        Customer customer = createAndPersistTestCustomer();
        
        UpdateFinancialsRequest financialsRequest = UpdateFinancialsRequest.builder()
            .totalRevenue(BigDecimal.valueOf(750000))
            .build();
        
        customerService.updateFinancials(customer.getId(), financialsRequest);
        
        // When
        CustomerSegment segment = customerService.calculateSegment(customer.getId());
        
        // Then
        assertEquals(CustomerSegment.A, segment);
    }
}

@QuarkusTest
class CustomerRepositoryTest {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Test
    @Transactional
    void testFindActiveCustomers() {
        // Given
        Customer activeCustomer = createTestCustomer("Active Corp");
        Customer inactiveCustomer = createTestCustomer("Inactive Corp");
        inactiveCustomer.getBasicInfo().setType(CustomerType.INACTIVE);
        
        customerRepository.persist(activeCustomer);
        customerRepository.persist(inactiveCustomer);
        
        // When
        List<Customer> activeCustomers = customerRepository.findActiveCustomers();
        
        // Then
        assertEquals(1, activeCustomers.size());
        assertEquals("Active Corp", activeCustomers.get(0).getBasicInfo().getName());
    }
    
    @Test
    @Transactional
    void testSearchByName() {
        // Given
        Customer customer1 = createTestCustomer("Acme Corporation");
        Customer customer2 = createTestCustomer("Test GmbH");
        Customer customer3 = createTestCustomer("Acme Industries");
        
        customerRepository.persist(customer1);
        customerRepository.persist(customer2);
        customerRepository.persist(customer3);
        
        // When
        List<Customer> results = customerRepository.searchByName("Acme");
        
        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(c -> c.getBasicInfo().getName().contains("Acme")));
    }
    
    @Test
    @Transactional
    void testFindHighValueCustomers() {
        // Given
        Customer customer1 = createTestCustomerWithRevenue("High Value Corp", BigDecimal.valueOf(500000));
        Customer customer2 = createTestCustomerWithRevenue("Low Value Corp", BigDecimal.valueOf(50000));
        
        customerRepository.persist(customer1);
        customerRepository.persist(customer2);
        
        // When
        List<Customer> highValueCustomers = customerRepository.findHighValueCustomers(BigDecimal.valueOf(100000));
        
        // Then
        assertEquals(1, highValueCustomers.size());
        assertEquals("High Value Corp", highValueCustomers.get(0).getBasicInfo().getName());
    }
    
    @Test
    @Transactional
    void testSoftDelete() {
        // Given
        Customer customer = createTestCustomer("To Delete Corp");
        customerRepository.persist(customer);
        
        User user = createTestUser();
        
        // When
        customerRepository.softDelete(customer.getId(), user);
        
        // Then
        Customer deletedCustomer = customerRepository.findById(customer.getId());
        assertNotNull(deletedCustomer.getDeletedAt());
        assertEquals(user, deletedCustomer.getDeletedBy());
        
        List<Customer> activeCustomers = customerRepository.findActiveCustomers();
        assertFalse(activeCustomers.contains(deletedCustomer));
    }
}
```

### Integration Tests - Customer API
```java
@QuarkusTest
@TestSecurity(user = "testuser", roles = {"admin"})
class CustomerResourceTest {
    
    @Test
    void testCreateCustomer() {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
            .name("Integration Test Corp")
            .legalForm("GmbH")
            .industry("Testing")
            .build();
        
        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/api/customers")
        .then()
            .statusCode(201)
            .body("name", equalTo("Integration Test Corp"))
            .body("legalForm", equalTo("GmbH"))
            .body("industry", equalTo("Testing"))
            .body("type", equalTo("PROSPECT"))
            .body("segment", equalTo("C"));
    }
    
    @Test
    void testGetCustomerDetails() {
        // Given - create customer first
        UUID customerId = createTestCustomerViaAPI();
        
        given()
        .when()
            .get("/api/customers/" + customerId)
        .then()
            .statusCode(200)
            .body("id", equalTo(customerId.toString()))
            .body("contacts", notNullValue())
            .body("addresses", notNullValue())
            .body("tags", notNullValue());
    }
    
    @Test
    void testAddContactToCustomer() {
        // Given
        UUID customerId = createTestCustomerViaAPI();
        
        CreateContactRequest contactRequest = CreateContactRequest.builder()
            .firstName("API")
            .lastName("Test")
            .email("api.test@example.com")
            .contactRole(ContactRole.PRIMARY)
            .build();
        
        given()
            .contentType("application/json")
            .body(contactRequest)
        .when()
            .post("/api/customers/" + customerId + "/contacts")
        .then()
            .statusCode(201)
            .body("firstName", equalTo("API"))
            .body("lastName", equalTo("Test"))
            .body("fullName", equalTo("API Test"))
            .body("contactRole", equalTo("PRIMARY"));
    }
    
    @Test
    void testSearchCustomers() {
        // Given - create test customers
        createTestCustomerViaAPI("Search Test 1");
        createTestCustomerViaAPI("Search Test 2");
        createTestCustomerViaAPI("Other Company");
        
        given()
            .queryParam("search", "Search Test")
        .when()
            .get("/api/customers")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("[0].name", containsString("Search Test"))
            .body("[1].name", containsString("Search Test"));
    }
}
```

### E2E Tests - Customer Management Flow
```typescript
describe('Customer Management E2E', () => {
    it('should create customer with contacts and addresses', async () => {
        await page.goto('/customers/new');
        
        // Fill basic customer info
        await page.fill('[data-testid=customer-name]', 'E2E Test Corporation');
        await page.selectOption('[data-testid=legal-form]', 'GmbH');
        await page.fill('[data-testid=industry]', 'Software Development');
        
        // Add primary contact
        await page.click('[data-testid=add-contact-button]');
        await page.fill('[data-testid=contact-first-name]', 'John');
        await page.fill('[data-testid=contact-last-name]', 'Manager');
        await page.fill('[data-testid=contact-email]', 'john.manager@e2etest.com');
        await page.selectOption('[data-testid=contact-role]', 'PRIMARY');
        
        // Add business address
        await page.click('[data-testid=add-address-button]');
        await page.fill('[data-testid=address-street]', 'Tech Street 123');
        await page.fill('[data-testid=address-city]', 'Berlin');
        await page.fill('[data-testid=address-postal-code]', '10115');
        await page.selectOption('[data-testid=address-type]', 'BUSINESS');
        
        // Submit form
        await page.click('[data-testid=save-customer-button]');
        
        // Verify creation
        await expect(page.locator('[data-testid=customer-detail-view]')).toBeVisible();
        await expect(page.locator('text=E2E Test Corporation')).toBeVisible();
        await expect(page.locator('text=John Manager')).toBeVisible();
        await expect(page.locator('text=Tech Street 123')).toBeVisible();
    });
    
    it('should manage customer tags', async () => {
        const customerId = await createTestCustomerViaAPI();
        await page.goto(`/customers/${customerId}`);
        
        // Add tags
        await page.click('[data-testid=add-tag-button]');
        await page.fill('[data-testid=tag-input]', 'VIP');
        await page.press('[data-testid=tag-input]', 'Enter');
        
        await page.fill('[data-testid=tag-input]', 'Tech');
        await page.press('[data-testid=tag-input]', 'Enter');
        
        // Verify tags
        await expect(page.locator('[data-testid=tag-vip]')).toBeVisible();
        await expect(page.locator('[data-testid=tag-tech]')).toBeVisible();
        
        // Remove tag
        await page.click('[data-testid=tag-vip] [data-testid=remove-tag]');
        await expect(page.locator('[data-testid=tag-vip]')).not.toBeVisible();
        await expect(page.locator('[data-testid=tag-tech]')).toBeVisible();
    });
    
    it('should calculate customer segment based on revenue', async () => {
        const customerId = await createTestCustomerViaAPI();
        await page.goto(`/customers/${customerId}/financials`);
        
        // Set high revenue
        await page.fill('[data-testid=total-revenue]', '750000');
        await page.click('[data-testid=save-financials-button]');
        
        // Verify segment calculation
        await expect(page.locator('[data-testid=customer-segment]')).toHaveText('A');
        await expect(page.locator('[data-testid=segment-badge]')).toHaveClass(/.*success.*/);
    });
});
```

---

## 🚀 IMPLEMENTATION GUIDE

### Phase 1: Database Schema & Entities (3 Tage)
1. **Database Migration** - Neue Tabellen für modular customer domain
2. **Entity Classes** - Customer, Contact, Address, CustomerFinancials, etc.
3. **Enum Classes** - CustomerType, ContactRole, AddressType, etc.
4. **Data Migration** - Bestehende Customer-Daten zu neuer Struktur

### Phase 2: Repository & Service Layer (2 Tage)
1. **Enhanced CustomerRepository** - Complex queries und search functionality
2. **Modular CustomerService** - CRUD operations für alle Sub-Entities
3. **Business Logic** - Customer segmentation, validation rules
4. **Mapper Classes** - Entity-DTO Conversion

### Phase 3: API Enhancement (1.5 Tage)
1. **CustomerResource Enhancement** - RESTful APIs für all modules
2. **Request/Response DTOs** - Structured data transfer objects
3. **Validation** - Input validation und business rules
4. **API Documentation** - OpenAPI/Swagger documentation

### Phase 4: Testing & Migration (1.5 Tage)
1. **Unit Tests** - Comprehensive entity und service tests
2. **Integration Tests** - API endpoint testing
3. **Data Migration Testing** - Verify existing data integrity
4. **Performance Testing** - Query optimization und indexing

---

## 📊 SUCCESS CRITERIA

### Funktionale Anforderungen
- ✅ Modulare Customer Domain mit separaten Entities
- ✅ Flexible Contact Management mit Rollen und Responsabilities
- ✅ Multi-Address Support für verschiedene Address-Types
- ✅ Strukturierte Financial Data mit Credit Rating
- ✅ Flexible Tagging System für Customer Categorization
- ✅ Soft Delete mit Audit Trail

### Performance-Anforderungen
- ✅ Customer Search < 200ms P95
- ✅ Customer Detail Load < 300ms P95
- ✅ Complex Queries < 500ms P95
- ✅ Bulk Operations < 2s für 1000 records
- ✅ Database Indexes für alle Query Patterns

### Data Quality-Anforderungen
- ✅ Referential Integrity zwischen allen Entities
- ✅ Validation Rules für all Customer Data
- ✅ Audit Trail für all Customer Changes
- ✅ Data Migration ohne Verlust bestehender Daten
- ✅ Backup/Restore Funktionalität

---

## 🔗 NAVIGATION ZU ALLEN 40 FEATURES

### Core Sales Features
- [FC-001 Customer Acquisition](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) | [FC-002 Smart Customer Insights](/docs/features/PLANNED/02_smart_insights/FC-002_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) | [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) | [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) | [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) | [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) | [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)

### Security & Foundation Features  
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) | [FC-009 Advanced Permissions](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md) | [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md)
- [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md) | [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md) | [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md)

### Mobile & Field Service Features
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) | [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) | [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) | [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) | [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)

### Communication Features
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) | [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) | [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)

### Analytics & Intelligence Features
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md) | [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) | [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) | [FC-037 Advanced Reporting](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md) | [FC-040 Performance Monitoring](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md)

### Integration & Infrastructure Features
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) | [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_KOMPAKT.md) | [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md)

### UI & Productivity Features
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) | [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) | [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) | [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) | [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)

### Advanced Features
- [FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_KOMPAKT.md) | [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_KOMPAKT.md) | [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md) | [M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)

---

**🏗️ M5 Customer Refactor - Ready for Modular Enhancement!**  
**⏱️ Geschätzter Aufwand:** 6-8 Tage | **Refactoring:** Modulare Domain-Struktur + Enhanced Customer Management