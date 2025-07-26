# 📅 TAG 2: BACKEND COMPLETION & INTEGRATION

**Navigation:**
- **Parent:** [Implementation Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)
- **Previous:** [Tag 1: Backend Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/01-day-1-backend.md)
- **Next:** [Tag 3: Frontend Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/03-day-3-frontend.md)
- **Related:** [Integration Points](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/04-INTEGRATION/README.md)

## 🔲 7. Field Catalog Seed Data (1h)

**Location:** `backend/src/main/resources/`

- [ ] `field-definitions.json` erstellen mit 10 MVP Feldern
```json
{
  "customer": [
    { 
      "key": "companyName", 
      "label": "Firmenname", 
      "type": "text",
      "required": true,
      "maxLength": 100
    },
    { 
      "key": "industry", 
      "label": "Branche", 
      "type": "select",
      "required": true,
      "options": ["hotel", "eventCatering", "businessCatering", "socialCatering", "retail"]
    },
    { 
      "key": "chainCustomer", 
      "label": "Filial-/Kettenbetrieb", 
      "type": "boolean",
      "trigger": "locations"
    }
    // Weitere Felder siehe: 01-TECH-CONCEPT/03-data-model.md
  ]
}
```

- [ ] `FieldDefinitionSeeder.java` implementieren
- [ ] Industry-specific fields definieren
- [ ] Test seed data

## 🔲 8. Integration Points (2h)

### Audit Trail Integration
- [ ] AuditService injection
- [ ] Critical field tracking
- [ ] Change events

```java
@Inject
AuditService auditService;

public void updateFieldValue(UUID customerId, String fieldKey, Object value) {
    var oldValue = getFieldValue(customerId, fieldKey);
    
    // Update durchführen
    fieldValueService.update(customerId, fieldKey, value);
    
    // Audit log
    auditService.logFieldChange(
        "CUSTOMER", 
        customerId, 
        fieldKey,
        oldValue,
        value
    );
}
```

### Security Integration  
- [ ] Permission checks
- [ ] Field-level security
- [ ] Role-based filtering

```java
@RolesAllowed({"admin", "manager", "sales"})
public List<FieldDefinition> getFieldDefinitions(
    @Context SecurityContext ctx,
    EntityType entityType,
    String industry
) {
    var userRoles = ctx.getUserPrincipal().getRoles();
    return fieldDefinitionService
        .getFieldsForIndustry(entityType, industry)
        .stream()
        .filter(field -> hasFieldAccess(field, userRoles))
        .collect(Collectors.toList());
}
```

### Event System
- [ ] Domain Events definieren
- [ ] Event publishers
- [ ] Test event flow

```java
// Events definieren
public class CustomerDraftCreated extends DomainEvent { }
public class CustomerFinalized extends DomainEvent { }
public class CustomerFieldUpdated extends DomainEvent { }

// Publisher
@Inject
EventBus eventBus;

// Consumer (in anderen Modulen)
void onCustomerFinalized(@Observes CustomerFinalized event) {
    // Z.B. Contract-Modul informieren
}
```

## 🔲 9. Performance Optimization (2h)

### Query optimization
- [ ] N+1 prevention
- [ ] Batch loading
- [ ] Projection queries

```java
// Optimierte Query mit JOIN FETCH
@Query("""
    SELECT DISTINCT c FROM Customer c
    LEFT JOIN FETCH c.locations l
    LEFT JOIN FETCH l.detailedLocations
    WHERE c.status = :status
    ORDER BY c.createdAt DESC
""")
List<Customer> findActiveWithLocations(@Param("status") CustomerStatus status);
```

### Caching setup
- [ ] Caffeine configuration
- [ ] Redis integration (optional)
- [ ] Cache invalidation

```yaml
# application.yml
quarkus:
  cache:
    caffeine:
      field-definitions:
        expire-after-write: PT5M
        maximum-size: 1000
```

### Database indexes verifizieren
- [ ] EXPLAIN ANALYZE auf wichtige Queries
- [ ] Missing indexes identifizieren
- [ ] Index usage statistics

## 🔲 10. Backend Testing & Polish (3h)

### Vollständige Test-Suite
- [ ] 80%+ Coverage erreichen
- [ ] Edge cases testen
- [ ] Error scenarios

```java
@QuarkusTest
class CustomerServiceTest {
    
    @Test
    void createDraft_shouldCleanupOldDrafts() {
        // Given: User hat bereits 5 Drafts
        createDrafts(TEST_USER, 5);
        
        // When: Neuen Draft erstellen
        var newDraftId = customerService.createDraft(TEST_USER);
        
        // Then: Nur noch 3 Drafts (älteste gelöscht)
        var remainingDrafts = customerRepository.findDrafts(TEST_USER);
        assertThat(remainingDrafts).hasSize(3);
        assertThat(remainingDrafts).noneMatch(d -> d.getCreatedAt().isBefore(cutoffTime));
    }
}
```

### Performance Tests
- [ ] Load test data generation
- [ ] JMeter test plan
- [ ] Baseline measurements

```bash
# Test data generator
./mvnw compile exec:java -Dexec.mainClass="de.freshplan.tools.TestDataGenerator" \
  -Dexec.args="customers=10000 locations=50000"
```

### Documentation
- [ ] API documentation vervollständigen
- [ ] Postman collection exportieren
- [ ] README updates

## ✅ Checklist Ende Tag 2

- [ ] Field Catalog vollständig
- [ ] Alle Integrationen funktionieren
- [ ] Performance innerhalb Targets
- [ ] Test Coverage > 80%
- [ ] Dokumentation aktuell

---

**Next:** [Tag 3: Frontend Foundation →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/03-day-3-frontend.md)