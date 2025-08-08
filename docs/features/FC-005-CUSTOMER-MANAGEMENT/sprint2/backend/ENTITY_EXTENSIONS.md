# 🗄️ Entity Extensions - Sprint 2

**Sprint:** 2  
**Status:** 🆕 Zu implementieren  
**Package:** `com.freshplan.backend.domain.customer.entity`  

---

## 📍 Navigation
**← API:** [API Endpoints](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/API_ENDPOINTS.md)  
**← Backend:** [Backend Requirements](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/BACKEND_REQUIREMENTS.md)  
**↑ Sprint 2:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  

---

## 🎯 Customer Entity Erweiterung

```java
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    
    // Bestehende Felder...
    
    // NEU: Filialstruktur
    @Column(name = "total_locations_eu")
    private Integer totalLocationsEU;
    
    @Column(name = "locations_germany")
    private Integer locationsGermany;
    
    @Column(name = "locations_austria")
    private Integer locationsAustria;
    
    @Column(name = "locations_switzerland")
    private Integer locationsSwitzerland;
    
    @Column(name = "locations_rest_eu")
    private Integer locationsRestEU;
    
    // NEU: Geschäftsmodell
    @Column(name = "primary_financing")
    @Enumerated(EnumType.STRING)
    private FinancingType primaryFinancing;
    
    // NEU: Pain Points als JSON
    @Column(name = "pain_points", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private List<String> painPoints;
    
    // NEU: Beziehung zu Locations
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Location> locations = new ArrayList<>();
}
```

---

## 🏢 Location Entity (NEU)

```java
@Entity
@Table(name = "locations")
public class Location extends BaseEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "location_name")
    private String locationName;
    
    // Adresse
    private String street;
    
    @Column(name = "house_number")
    private String houseNumber;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    private String city;
    
    @Column(name = "is_main_location")
    private Boolean isMainLocation = false;
    
    // Branchenspezifische Details als JSON
    @Column(name = "service_offerings", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private Map<String, Object> serviceOfferings;
    
    @Column(name = "location_details", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private Map<String, Object> locationDetails;
    
    // Contacts zu diesem Standort
    @OneToMany(mappedBy = "location")
    private List<Contact> contacts = new ArrayList<>();
}
```

---

## 📋 Enums

```java
public enum FinancingType {
    PRIVATE("private"),
    PUBLIC("public"),
    MIXED("mixed");
    
    private final String value;
    
    FinancingType(String value) {
        this.value = value;
    }
}
```

---

## 🔄 Flyway Migration

```sql
-- V2.0.0__add_chain_structure.sql

ALTER TABLE customers 
ADD COLUMN total_locations_eu INTEGER,
ADD COLUMN locations_germany INTEGER,
ADD COLUMN locations_austria INTEGER,
ADD COLUMN locations_switzerland INTEGER,
ADD COLUMN locations_rest_eu INTEGER,
ADD COLUMN primary_financing VARCHAR(20),
ADD COLUMN pain_points JSONB;

-- Create locations table
CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    location_name VARCHAR(100),
    street VARCHAR(200),
    house_number VARCHAR(20),
    postal_code VARCHAR(20),
    city VARCHAR(100),
    is_main_location BOOLEAN DEFAULT false,
    service_offerings JSONB,
    location_details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_locations_customer ON locations(customer_id);
```

---

**← Zurück:** [Backend Requirements](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/BACKEND_REQUIREMENTS.md)