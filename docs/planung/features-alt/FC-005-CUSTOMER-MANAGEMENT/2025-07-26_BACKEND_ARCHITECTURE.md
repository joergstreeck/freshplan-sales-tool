# 🔧 FC-005 CUSTOMER MANAGEMENT - BACKEND ARCHITECTURE

**Datum:** 26.07.2025  
**Version:** 1.0  
**Status:** 🔄 In Entwicklung  
**Abhängigkeiten:** FC-012 (Audit Trail), Security Foundation  

## 📋 Inhaltsverzeichnis

1. [Entity Design](#entity-design)
2. [Service Layer](#service-layer)
3. [Repository Pattern](#repository-pattern)
4. [REST API Design](#rest-api-design)
5. [Datenbank-Schema](#datenbank-schema)
6. [Migration Strategy](#migration-strategy)
7. [Integration Points](#integration-points)

---

## Entity Design

### Core Entities

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

// Enums
public enum CustomerStatus {
    DRAFT, ACTIVE, INACTIVE, ARCHIVED
}

public enum EntityType {
    CUSTOMER, LOCATION, DETAILED_LOCATION
}
```

### Field Definition (Configuration)

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

---

## Service Layer

### CustomerService

```java
@ApplicationScoped
@Transactional
public class CustomerService {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    FieldValueService fieldValueService;
    
    @Inject
    AuditService auditService;
    
    @Inject
    Event<CustomerCreatedEvent> customerCreatedEvent;
    
    public CustomerDraftResponse createDraft(CreateCustomerDraftRequest request) {
        // 1. Create Customer entity
        Customer customer = new Customer();
        customer.setStatus(CustomerStatus.DRAFT);
        
        // 2. Persist customer first to get ID
        customer = customerRepository.persist(customer);
        
        // 3. Create field values
        for (FieldValueRequest fieldRequest : request.getFieldValues()) {
            fieldValueService.createFieldValue(
                customer.getId(),
                EntityType.CUSTOMER,
                fieldRequest
            );
        }
        
        // 4. Audit log
        auditService.logCreation(
            AuditEntityType.CUSTOMER,
            customer.getId(),
            "Customer draft created"
        );
        
        return mapToResponse(customer);
    }
    
    public CustomerResponse finalizeDraft(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
            
        if (customer.getStatus() != CustomerStatus.DRAFT) {
            throw new InvalidCustomerStateException(
                "Customer is not in DRAFT status"
            );
        }
        
        // Validate all required fields
        validateRequiredFields(customer);
        
        // Update status
        customer.setStatus(CustomerStatus.ACTIVE);
        
        // Fire event for other modules
        customerCreatedEvent.fire(new CustomerCreatedEvent(customer));
        
        return mapToResponse(customer);
    }
    
    private void validateRequiredFields(Customer customer) {
        List<FieldDefinition> requiredFields = 
            fieldDefinitionService.getRequiredFields(EntityType.CUSTOMER);
            
        Map<String, Object> fieldValues = 
            fieldValueService.getFieldValuesAsMap(
                customer.getId(), 
                EntityType.CUSTOMER
            );
            
        for (FieldDefinition field : requiredFields) {
            if (!fieldValues.containsKey(field.getKey()) 
                || fieldValues.get(field.getKey()) == null) {
                throw new MissingRequiredFieldException(field.getKey());
            }
        }
    }
}
```

### FieldValueService

```java
@ApplicationScoped
@Transactional
public class FieldValueService {
    
    @Inject
    FieldValueRepository fieldValueRepository;
    
    @Inject
    FieldDefinitionService fieldDefinitionService;
    
    @Inject
    ValidationService validationService;
    
    public FieldValue createFieldValue(
        UUID entityId, 
        EntityType entityType, 
        FieldValueRequest request
    ) {
        // Get field definition
        FieldDefinition definition = fieldDefinitionService
            .getFieldDefinition(request.getFieldKey())
            .orElseThrow(() -> 
                new FieldDefinitionNotFoundException(request.getFieldKey())
            );
        
        // Validate value against definition
        validationService.validateFieldValue(
            definition, 
            request.getValue()
        );
        
        // Create or update field value
        FieldValue fieldValue = fieldValueRepository
            .findByEntityAndField(entityId, request.getFieldKey())
            .orElse(new FieldValue());
            
        fieldValue.setEntityId(entityId);
        fieldValue.setEntityType(entityType);
        fieldValue.setFieldDefinitionId(definition.getKey());
        fieldValue.setValue(request.getValue());
        
        return fieldValueRepository.persist(fieldValue);
    }
    
    public Map<String, Object> getFieldValuesAsMap(
        UUID entityId, 
        EntityType entityType
    ) {
        List<FieldValue> values = fieldValueRepository
            .findByEntity(entityId, entityType);
            
        return values.stream()
            .collect(Collectors.toMap(
                FieldValue::getFieldDefinitionId,
                FieldValue::getValue
            ));
    }
}
```

---

## Repository Pattern

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
}
```

---

## REST API Design

### Customer Resource

```java
@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class CustomerResource {
    
    @Inject
    CustomerService customerService;
    
    @POST
    @Path("/draft")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response createDraft(
        @Valid CreateCustomerDraftRequest request
    ) {
        CustomerDraftResponse response = 
            customerService.createDraft(request);
        return Response.ok(response).build();
    }
    
    @PUT
    @Path("/draft/{id}")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response updateDraft(
        @PathParam("id") UUID id,
        @Valid UpdateCustomerDraftRequest request
    ) {
        CustomerDraftResponse response = 
            customerService.updateDraft(id, request);
        return Response.ok(response).build();
    }
    
    @POST
    @Path("/draft/{id}/finalize")
    @RolesAllowed({"admin", "manager"})
    public Response finalizeDraft(@PathParam("id") UUID id) {
        CustomerResponse response = 
            customerService.finalizeDraft(id);
        return Response.ok(response).build();
    }
    
    @GET
    @Path("/{id}/field-values")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response getFieldValues(@PathParam("id") UUID id) {
        Map<String, Object> values = 
            fieldValueService.getFieldValuesAsMap(
                id, 
                EntityType.CUSTOMER
            );
        return Response.ok(values).build();
    }
}
```

### Field Definition Resource

```java
@Path("/api/field-definitions")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class FieldDefinitionResource {
    
    @Inject
    FieldDefinitionService service;
    
    @GET
    @Path("/{entityType}")
    @RolesAllowed({"admin", "manager", "sales"})
    public List<FieldDefinitionResponse> getFieldDefinitions(
        @PathParam("entityType") EntityType entityType,
        @QueryParam("industry") String industry,
        @QueryParam("includeCustom") @DefaultValue("false") boolean includeCustom
    ) {
        return service.getFieldDefinitions(
            entityType, 
            industry, 
            includeCustom
        );
    }
    
    @POST
    @RolesAllowed({"admin"})
    public Response createCustomField(
        @Valid CreateFieldDefinitionRequest request
    ) {
        FieldDefinition definition = 
            service.createCustomField(request);
        return Response.ok(definition).build();
    }
}
```

---

## Datenbank-Schema

### Flyway Migration

```sql
-- V1.0.0__create_customer_tables.sql

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    location_type VARCHAR(50) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE field_values (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    field_definition_id VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    value JSONB,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_field_value_entity 
    ON field_values(entity_id, entity_type);
CREATE INDEX idx_field_value_definition 
    ON field_values(field_definition_id);
CREATE INDEX idx_customer_status 
    ON customers(status);
CREATE INDEX idx_location_customer 
    ON locations(customer_id);

-- Field definitions table
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

-- Industries mapping
CREATE TABLE field_definition_industries (
    field_definition_key VARCHAR(100) REFERENCES field_definitions(key),
    industry VARCHAR(50) NOT NULL,
    PRIMARY KEY (field_definition_key, industry)
);
```

---

## Migration Strategy

### Seed Data für Field Definitions

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
        
        // ... weitere Felder
    }
}
```

---

## Integration Points

### 1. Audit Trail Integration

```java
// In CustomerService
@Inject
AuditService auditService;

private void auditFieldChange(
    UUID customerId, 
    String fieldKey, 
    Object oldValue, 
    Object newValue
) {
    auditService.logUpdate(
        AuditEntityType.CUSTOMER,
        customerId,
        String.format("Field '%s' changed", fieldKey),
        Map.of(
            "field", fieldKey,
            "oldValue", oldValue,
            "newValue", newValue
        )
    );
}
```

### 2. Event System

```java
// Events für andere Module
public class CustomerCreatedEvent {
    private final UUID customerId;
    private final String companyName;
    private final String industry;
    // ... constructor, getters
}

public class CustomerUpdatedEvent {
    private final UUID customerId;
    private final Map<String, Object> changedFields;
    // ... constructor, getters
}
```

### 3. Security Integration

```java
@Inject
SecurityContext securityContext;

private void checkCustomerAccess(UUID customerId) {
    if (!securityContext.isUserInRole("admin")) {
        // Check if user has access to this customer
        // Based on assignment, team, etc.
    }
}
```

---

## Performance Considerations

1. **Field Value Caching**
   - Cache field definitions (rarely change)
   - Consider caching field values for read-heavy operations

2. **Batch Operations**
   - Bulk create/update field values
   - Batch load customers with their field values

3. **Query Optimization**
   - Use projections for list views
   - Lazy load detailed locations

---

## Testing Strategy

```java
@QuarkusTest
@TestProfile(CustomerTestProfile.class)
class CustomerServiceTest {
    
    @Test
    @TestSecurity(user = "testuser", roles = "sales")
    void testCreateCustomerDraft() {
        // Test implementation
    }
    
    @Test
    @TestTransaction
    void testFieldValueValidation() {
        // Test implementation
    }
}
```