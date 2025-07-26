# 🔧 FC-005 BACKEND - DATABASE

**Navigation:** [← REST API](03-rest-api.md) | [Backend Overview →](README.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Status:** 🔄 In Entwicklung  

## 📋 Inhaltsverzeichnis

1. [Datenbank-Schema](#datenbank-schema)
2. [Flyway Migrations](#flyway-migrations)
3. [Seed Data](#seed-data)
4. [Repository Pattern](#repository-pattern)
5. [Performance Optimierung](#performance-optimierung)

## Datenbank-Schema

### Haupt-Tabellen

```sql
-- Customers Table
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Locations Table
CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    location_type VARCHAR(50) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Detailed Locations Table
CREATE TABLE detailed_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    location_id UUID NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    street VARCHAR(255),
    postal_code VARCHAR(10),
    city VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Field Values Table (JSONB Storage)
CREATE TABLE field_values (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    field_definition_id VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    value JSONB,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Field Definition Tables

```sql
-- Field Definitions Table
CREATE TABLE field_definitions (
    key VARCHAR(100) PRIMARY KEY,
    label VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    validation JSONB,
    default_value JSONB,
    is_custom BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Industries Mapping
CREATE TABLE field_definition_industries (
    field_definition_key VARCHAR(100) REFERENCES field_definitions(key),
    industry VARCHAR(50) NOT NULL,
    PRIMARY KEY (field_definition_key, industry)
);
```

### Indexes für Performance

```sql
-- Performance Indexes
CREATE INDEX idx_field_value_entity 
    ON field_values(entity_id, entity_type);
    
CREATE INDEX idx_field_value_definition 
    ON field_values(field_definition_id);
    
CREATE INDEX idx_customer_status 
    ON customers(status);
    
CREATE INDEX idx_location_customer 
    ON locations(customer_id);
    
CREATE INDEX idx_detailed_location 
    ON detailed_locations(location_id);

-- JSONB GIN Index für Field Values
CREATE INDEX idx_field_value_jsonb 
    ON field_values USING gin(value);
```

## Flyway Migrations

### V1.0.0__create_customer_tables.sql

```sql
-- Complete initial migration
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create all tables
CREATE TABLE customers (...);
CREATE TABLE locations (...);
CREATE TABLE detailed_locations (...);
CREATE TABLE field_values (...);
CREATE TABLE field_definitions (...);
CREATE TABLE field_definition_industries (...);

-- Create all indexes
CREATE INDEX ...;

-- Add comments
COMMENT ON TABLE customers IS 'Haupttabelle für Kundendaten';
COMMENT ON TABLE field_values IS 'Dynamische Feldwerte mit JSONB Storage';
```

### V1.0.1__add_audit_triggers.sql

```sql
-- Add updated_at triggers
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_customers_updated_at 
    BEFORE UPDATE ON customers 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

## Seed Data

### Field Definition Seeder

```java
@ApplicationScoped
@Startup
public class FieldDefinitionSeeder {
    
    @Inject
    FieldDefinitionRepository repository;
    
    @PostConstruct
    @Transactional
    public void seedFieldDefinitions() {
        if (repository.count() > 0) {
            return; // Already seeded
        }
        
        // Customer fields
        createFieldDefinition(
            "companyName",
            "Firmenname",
            EntityType.CUSTOMER,
            "text",
            true,
            null
        );
        
        createFieldDefinition(
            "industry",
            "Branche",
            EntityType.CUSTOMER,
            "select",
            true,
            Arrays.asList("hotel", "krankenhaus", "seniorenresidenz", 
                         "restaurant", "betriebsrestaurant")
        );
        
        // Location fields per industry
        createFieldDefinition(
            "roomCount",
            "Anzahl Zimmer",
            EntityType.LOCATION,
            "number",
            false,
            Collections.singletonList("hotel")
        );
        
        createFieldDefinition(
            "bedCount",
            "Anzahl Betten",
            EntityType.LOCATION,
            "number",
            false,
            Arrays.asList("krankenhaus", "seniorenresidenz")
        );
    }
    
    private void createFieldDefinition(
        String key,
        String label,
        EntityType entityType,
        String fieldType,
        boolean required,
        List<String> industries
    ) {
        FieldDefinition def = new FieldDefinition();
        def.setKey(key);
        def.setLabel(label);
        def.setEntityType(entityType);
        def.setFieldType(fieldType);
        
        if (required) {
            def.setValidation(Json.createObjectBuilder()
                .add("required", true)
                .build());
        }
        
        if (industries != null) {
            def.setIndustries(new HashSet<>(industries));
        }
        
        repository.persist(def);
    }
}
```

## Repository Pattern

### CustomerRepository

```java
@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, UUID> {
    
    public Optional<Customer> findByIdWithLocations(UUID id) {
        return find(
            "SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN FETCH c.locations " +
            "WHERE c.id = ?1", 
            id
        ).firstResultOptional();
    }
    
    public List<Customer> findDrafts() {
        return find("status = ?1", CustomerStatus.DRAFT).list();
    }
    
    @Transactional
    public void cleanupOldDrafts(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        delete("status = ?1 AND createdAt < ?2", 
            CustomerStatus.DRAFT, 
            cutoff
        );
    }
}
```

### FieldValueRepository

```java
@ApplicationScoped
public class FieldValueRepository 
    implements PanacheRepositoryBase<FieldValue, UUID> {
    
    public List<FieldValue> findByEntity(UUID entityId, EntityType type) {
        return find(
            "entityId = ?1 AND entityType = ?2", 
            entityId, 
            type
        ).list();
    }
    
    public Optional<FieldValue> findByEntityAndField(
        UUID entityId, 
        String fieldKey
    ) {
        return find(
            "entityId = ?1 AND fieldDefinitionId = ?2", 
            entityId, 
            fieldKey
        ).firstResultOptional();
    }
    
    @Transactional
    public void deleteByEntity(UUID entityId, EntityType type) {
        delete("entityId = ?1 AND entityType = ?2", entityId, type);
    }
}
```

## Performance Optimierung

### Query Optimization

```java
// Optimized query with projections
@Query("""
    SELECT new CustomerListDTO(
        c.id, 
        c.status,
        fv1.value,
        fv2.value,
        COUNT(l)
    )
    FROM Customer c
    LEFT JOIN c.fieldValues fv1 ON fv1.fieldDefinitionId = 'companyName'
    LEFT JOIN c.fieldValues fv2 ON fv2.fieldDefinitionId = 'industry'
    LEFT JOIN c.locations l
    GROUP BY c.id, c.status, fv1.value, fv2.value
""")
List<CustomerListDTO> findCustomersOptimized();
```

### Batch Operations

```java
@Transactional
public void bulkUpdateFieldValues(
    List<FieldValueBatch> batches
) {
    // Use batch insert/update
    String sql = """
        INSERT INTO field_values (id, entity_id, entity_type, field_definition_id, value)
        VALUES (?, ?, ?, ?, ?::jsonb)
        ON CONFLICT (entity_id, field_definition_id) 
        DO UPDATE SET value = EXCLUDED.value, updated_at = CURRENT_TIMESTAMP
    """;
    
    // Execute in batches of 1000
    // ...
}
```

---

**Parent:** [Backend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)  
**Related:** [Entities](01-entities.md) | [Services](02-services.md) | [Performance](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/README.md)