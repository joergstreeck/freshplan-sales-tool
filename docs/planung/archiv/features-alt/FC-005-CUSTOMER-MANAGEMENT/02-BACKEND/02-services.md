# 🔧 FC-005 BACKEND - SERVICES

**Navigation:** [← Entities](01-entities.md) | [REST API →](03-rest-api.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Status:** 🔄 In Entwicklung  

## 📋 Inhaltsverzeichnis

1. [CustomerService](#customerservice)
2. [FieldValueService](#fieldvalueservice)
3. [ValidationService](#validationservice)
4. [Event Handling](#event-handling)

## CustomerService

### Haupt-Service für Kundenverwaltung

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
    
    @Transactional
    public void updateFieldValues(
        UUID customerId, 
        List<FieldValueRequest> updates
    ) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
            
        for (FieldValueRequest update : updates) {
            Object oldValue = fieldValueService.getFieldValue(
                customerId, 
                EntityType.CUSTOMER, 
                update.getFieldKey()
            );
            
            fieldValueService.updateFieldValue(
                customerId,
                EntityType.CUSTOMER,
                update
            );
            
            // Audit the change
            auditFieldChange(
                customerId, 
                update.getFieldKey(), 
                oldValue, 
                update.getValue()
            );
        }
    }
}
```

## FieldValueService

### Service für Field Value Management

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
    
    @Transactional
    public void bulkCreateFieldValues(
        UUID entityId,
        EntityType entityType,
        Map<String, Object> fieldValues
    ) {
        for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
            createFieldValue(
                entityId,
                entityType,
                new FieldValueRequest(entry.getKey(), entry.getValue())
            );
        }
    }
}
```

## ValidationService

### Feld-Validierung

```java
@ApplicationScoped
public class ValidationService {
    
    public void validateFieldValue(
        FieldDefinition definition, 
        Object value
    ) {
        // Null check for required fields
        if (definition.isRequired() && value == null) {
            throw new ValidationException(
                String.format("Field '%s' is required", definition.getKey())
            );
        }
        
        // Type validation
        switch (definition.getFieldType()) {
            case "text":
                validateTextValue(definition, value);
                break;
            case "number":
                validateNumberValue(definition, value);
                break;
            case "select":
                validateSelectValue(definition, value);
                break;
            case "email":
                validateEmailValue(value);
                break;
            case "phone":
                validatePhoneValue(value);
                break;
            case "postalCode":
                validatePostalCode(value);
                break;
        }
        
        // Custom validation from JSON schema
        if (definition.getValidation() != null) {
            validateAgainstJsonSchema(value, definition.getValidation());
        }
    }
    
    private void validatePostalCode(Object value) {
        if (value == null) return;
        
        String postalCode = value.toString();
        if (!postalCode.matches("^[0-9]{5}$")) {
            throw new ValidationException(
                "Postal code must be exactly 5 digits"
            );
        }
    }
}
```

## Event Handling

### Domain Events

```java
// Customer Created Event
public class CustomerCreatedEvent {
    private final UUID customerId;
    private final String companyName;
    private final String industry;
    private final LocalDateTime createdAt;
    
    public CustomerCreatedEvent(Customer customer) {
        this.customerId = customer.getId();
        this.createdAt = customer.getCreatedAt();
        
        // Extract field values
        Map<String, Object> fields = customer.getFieldValues().stream()
            .collect(Collectors.toMap(
                FieldValue::getFieldDefinitionId,
                FieldValue::getValue
            ));
            
        this.companyName = (String) fields.get("companyName");
        this.industry = (String) fields.get("industry");
    }
    
    // Getters...
}

// Customer Updated Event
public class CustomerUpdatedEvent {
    private final UUID customerId;
    private final Map<String, Object> changedFields;
    private final LocalDateTime updatedAt;
    
    // Constructor, getters...
}
```

### Event Observers

```java
@ApplicationScoped
public class CustomerEventObserver {
    
    @Inject
    OpportunityService opportunityService;
    
    void onCustomerCreated(@Observes CustomerCreatedEvent event) {
        // Create default opportunity for new customer
        opportunityService.createDefaultOpportunity(
            event.getCustomerId(),
            event.getCompanyName()
        );
    }
    
    void onCustomerUpdated(@Observes CustomerUpdatedEvent event) {
        // Update related data if needed
        if (event.getChangedFields().containsKey("companyName")) {
            opportunityService.updateCustomerName(
                event.getCustomerId(),
                (String) event.getChangedFields().get("companyName")
            );
        }
    }
}
```

---

**Parent:** [Backend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)  
**Related:** [Entities](01-entities.md) | [REST API](03-rest-api.md) | [Database](04-database.md)