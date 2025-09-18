# 🔧 FC-005 BACKEND - ENTITIES

**Navigation:** [← Backend Overview](README.md) | [Services →](02-services.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Status:** 🔄 In Entwicklung  

## 📋 Inhaltsverzeichnis

1. [Core Entities](#core-entities)
2. [Field System Entities](#field-system-entities)
3. [Enums und Value Objects](#enums-und-value-objects)
4. [Entity Relationships](#entity-relationships)

## Core Entities

### Customer Entity

```java
// Package: de.freshplan.domain.customer.entity

@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class Customer extends BaseEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.DRAFT;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Location> locations = new ArrayList<>();
    
    @OneToMany(mappedBy = "entityId", cascade = CascadeType.ALL)
    @Where(clause = "entity_type = 'CUSTOMER'")
    private List<FieldValue> fieldValues = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Audit Integration
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
```

### Location Entity

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
    
    @Column(name = "location_type", nullable = false)
    private String locationType; // hauptstandort, filiale, aussenstelle
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<DetailedLocation> detailedLocations = new ArrayList<>();
    
    @OneToMany(mappedBy = "entityId", cascade = CascadeType.ALL)
    @Where(clause = "entity_type = 'LOCATION'")
    private List<FieldValue> fieldValues = new ArrayList<>();
}
```

### DetailedLocation Entity

```java
@Entity
@Table(name = "detailed_locations")
public class DetailedLocation extends BaseEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @Column(nullable = false)
    private String name;
    
    private String street;
    
    @Column(name = "postal_code", length = 10)
    private String postalCode;
    
    private String city;
    
    @OneToMany(mappedBy = "entityId", cascade = CascadeType.ALL)
    @Where(clause = "entity_type = 'DETAILED_LOCATION'")
    private List<FieldValue> fieldValues = new ArrayList<>();
}
```

## Field System Entities

### FieldValue Entity

```java
@Entity
@Table(name = "field_values")
@Table(indexes = {
    @Index(name = "idx_field_value_entity", columnList = "entity_id, entity_type"),
    @Index(name = "idx_field_value_definition", columnList = "field_definition_id")
})
public class FieldValue extends BaseEntity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(name = "field_definition_id", nullable = false)
    private String fieldDefinitionId;
    
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;
    
    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Object value;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### FieldDefinition Entity

```java
@Entity
@Table(name = "field_definitions")
@Cacheable
public class FieldDefinition {
    
    @Id
    private String key; // z.B. "companyName"
    
    private String label;
    
    @Enumerated(EnumType.STRING)
    private EntityType entityType;
    
    @ElementCollection
    @CollectionTable(name = "field_definition_industries")
    private Set<String> industries = new HashSet<>();
    
    private String fieldType; // text, number, select, etc.
    
    @Column(columnDefinition = "jsonb")
    private JsonNode validation; // Zod Schema als JSON
    
    @Column(columnDefinition = "jsonb")
    private JsonNode defaultValue;
    
    private boolean isCustom = false;
    
    private boolean isActive = true;
    
    private Integer sortOrder = 0;
}
```

## Enums und Value Objects

### CustomerStatus Enum

```java
public enum CustomerStatus {
    DRAFT,      // Erster Entwurf, noch nicht finalisiert
    ACTIVE,     // Aktiver Kunde
    INACTIVE,   // Temporär inaktiv
    ARCHIVED    // Archiviert (soft delete)
}
```

### EntityType Enum

```java
public enum EntityType {
    CUSTOMER,           // Kunde (Hauptentität)
    LOCATION,          // Standort
    DETAILED_LOCATION  // Detaillierter Standort
}
```

### LocationType Constants

```java
public final class LocationTypes {
    public static final String HAUPTSTANDORT = "hauptstandort";
    public static final String FILIALE = "filiale";
    public static final String AUSSENSTELLE = "aussenstelle";
    public static final String NIEDERLASSUNG = "niederlassung";
    
    private LocationTypes() {} // Utility class
}
```

## Entity Relationships

### Beziehungs-Diagramm

```
┌─────────────┐       ┌──────────────┐       ┌──────────────────────┐
│  Customer   │ 1───n │   Location   │ 1───n │ DetailedLocation     │
├─────────────┤       ├──────────────┤       ├──────────────────────┤
│ id          │       │ id           │       │ id                   │
│ status      │       │ customer_id  │       │ location_id          │
│ createdAt   │       │ locationType │       │ name                 │
│ updatedAt   │       │ sortOrder    │       │ street               │
│ createdBy   │       └──────────────┘       │ postalCode           │
└─────────────┘                              │ city                 │
      │                                      └──────────────────────┘
      │                    │                           │
      └────────────────────┼───────────────────────────┘
                           ▼
                  ┌─────────────────┐
                  │   FieldValue    │
                  ├─────────────────┤
                  │ id              │
                  │ entityId        │
                  │ entityType      │
                  │ fieldDefId      │
                  │ value (JSONB)   │
                  └─────────────────┘
```

### Cascade-Verhalten

- **Customer → Location:** CASCADE ALL (Löschen des Kunden löscht alle Standorte)
- **Location → DetailedLocation:** CASCADE ALL (Löschen des Standorts löscht Details)
- **Entity → FieldValue:** CASCADE ALL (Löschen der Entität löscht alle Feldwerte)

### Fetch-Strategien

- **Standardmäßig LAZY** für alle @ManyToOne Beziehungen
- **Explizite EAGER-Queries** wo benötigt (z.B. Customer mit Locations)

---

**Parent:** [Backend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)  
**Related:** [Services](02-services.md) | [Database Schema](04-database.md)