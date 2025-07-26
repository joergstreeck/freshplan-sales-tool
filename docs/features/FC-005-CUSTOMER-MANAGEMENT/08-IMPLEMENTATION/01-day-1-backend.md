# 📅 TAG 1: BACKEND FOUNDATION

**Navigation:**
- **Parent:** [Implementation Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)
- **Next:** [Tag 2: Backend Completion](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/02-day-2-persistence.md)
- **Related:** [Backend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)

## 🔲 1. Database Schema (1h)

- [ ] Flyway Migration erstellen: `V2.0.0__create_customer_field_tables.sql`
```sql
-- Location: backend/src/main/resources/db/migration/
-- Siehe: 02-BACKEND/04-database.md für vollständiges Schema
```

- [ ] Indexes für Performance hinzufügen
- [ ] Test-Migration lokal ausführen
- [ ] Rollback-Script erstellen

## 🔲 2. Entities implementieren (2h)

**Location:** `backend/src/main/java/de/freshplan/domain/customer/entity/`

- [ ] `Customer.java` erweitern
  - [ ] Status enum hinzufügen
  - [ ] Audit fields
  - [ ] Relations zu Location/FieldValue

- [ ] `Location.java` erstellen
  - [ ] Base fields
  - [ ] Relation zu Customer
  - [ ] Relation zu DetailedLocation

- [ ] `DetailedLocation.java` erstellen

- [ ] `FieldValue.java` erstellen
  - [ ] JSONB Converter implementieren
  - [ ] Index annotations

- [ ] `FieldDefinition.java` erstellen
  - [ ] Als Configuration Entity
  - [ ] Caching annotations

## 🔲 3. Repositories (1h)

**Location:** `backend/src/main/java/de/freshplan/domain/customer/repository/`

- [ ] `CustomerRepository.java` erweitern
  - [ ] `findDrafts()` Methode
  - [ ] `cleanupOldDrafts()` Methode
  - [ ] Optimierte Such-Queries

- [ ] `LocationRepository.java` erstellen
  - [ ] Basic CRUD
  - [ ] `findByCustomer()` mit Pagination

- [ ] `FieldValueRepository.java` erstellen
  - [ ] `findByEntity()` Methode
  - [ ] `findByEntityAndField()` Methode
  - [ ] Batch operations

- [ ] `FieldDefinitionRepository.java` erstellen
  - [ ] Cache-aware Methoden
  - [ ] Industry-Filter

## 🔲 4. Service Layer (2h)

**Location:** `backend/src/main/java/de/freshplan/domain/customer/service/`

- [ ] `CustomerService.java` erweitern
  - [ ] `createDraft()` implementieren
  - [ ] `updateDraft()` implementieren  
  - [ ] `finalizeDraft()` mit Validierung
  - [ ] Event publishing

- [ ] `FieldValueService.java` erstellen
  - [ ] Field validation
  - [ ] Batch operations
  - [ ] Value transformation

- [ ] `FieldDefinitionService.java` erstellen
  - [ ] Field catalog loading
  - [ ] Industry-specific filtering
  - [ ] Caching logic

- [ ] `ValidationService.java` erstellen
  - [ ] Zod-like validation
  - [ ] Custom validators
  - [ ] Error messages

## 🔲 5. REST Resources (2h)

**Location:** `backend/src/main/java/de/freshplan/api/resources/`

- [ ] `CustomerResource.java` erweitern
  - [ ] Draft endpoints
  - [ ] Field value endpoints
  - [ ] Search mit Pagination
  - [ ] Swagger annotations

- [ ] `FieldDefinitionResource.java` erstellen
  - [ ] GET endpoints mit Filtering
  - [ ] Admin endpoints (später)
  - [ ] Caching headers

- [ ] DTOs erstellen
  - [ ] `CreateCustomerDraftRequest`
  - [ ] `UpdateCustomerDraftRequest`
  - [ ] `CustomerDraftResponse`
  - [ ] `FieldValueRequest`

## 🔲 6. Tests & Documentation (1h)

- [ ] Unit Tests für Services
- [ ] Integration Tests für Repositories
- [ ] REST Assured Tests für Endpoints
- [ ] Swagger/OpenAPI Dokumentation

## 📝 Code-Beispiele

### Entity Beispiel
```java
@Entity
@Table(name = "customers")
public class Customer extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.DRAFT;
    
    // ... weitere Felder
}
```

### Service Beispiel
```java
@ApplicationScoped
@Transactional
public class CustomerService {
    
    public UUID createDraft(String userId) {
        var customer = new Customer();
        customer.setStatus(CustomerStatus.DRAFT);
        customer.setCreatedBy(userId);
        customer.persist();
        
        eventBus.publish(new CustomerDraftCreated(customer.getId()));
        return customer.getId();
    }
}
```

## ✅ Checklist Ende Tag 1

- [ ] Alle Entities kompilieren
- [ ] Flyway Migration läuft durch
- [ ] Basic CRUD funktioniert
- [ ] Erste Tests grün
- [ ] API dokumentiert

---

**Next:** [Tag 2: Backend Completion →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/02-day-2-persistence.md)