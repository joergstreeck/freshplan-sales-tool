# 📦 PR #5: Backend Services CQRS Refactoring - Detaillierter Implementierungsplan

**Erstellt:** 13.08.2025  
**Status:** 📋 PLANUNG  
**Branch:** `feature/refactor-large-services`  
**Priorität:** 🟡 Mittel  
**Geschätzter Aufwand:** 3-5 Tage  

---

## 📑 Navigation (Lesereihenfolge)

**Du bist hier:** Dokument 5 von 7  
**⬅️ Zurück:** [`CODE_QUALITY_PR_ROADMAP.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/CODE_QUALITY_PR_ROADMAP.md)  
**➡️ Weiter:** [`TEST_STRATEGY_PER_PR.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/TEST_STRATEGY_PER_PR.md)  
**🏠 Start:** [`README.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/README.md)  
**⚠️ Kritisch:** [`PR_5_CRITICAL_CONTEXT.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/PR_5_CRITICAL_CONTEXT.md)

---

## 📊 Executive Summary

### Problemstellung
Wir haben 3 große Service-Klassen mit über 1.600 Zeilen Code gesamt:
- **CustomerService.java**: 716 Zeilen (Mixed Concerns)
- **OpportunityService.java**: 451 Zeilen (Mixed Concerns)
- **AuditService.java**: 461 Zeilen (Bereits Event-orientiert, aber monolithisch)

Diese Services vermischen Commands (Schreiboperationen) mit Queries (Leseoperationen), was zu:
- Schwerer Testbarkeit
- Performance-Problemen bei Read-Heavy Operations
- Komplexer Transaktionsverwaltung
- Schwieriger Skalierbarkeit führt

### Lösung: CQRS Pattern
**Command Query Responsibility Segregation (CQRS)** trennt Lese- und Schreiboperationen in separate Services, was zu besserer:
- Performance (optimierte Read Models)
- Testbarkeit (fokussierte Services)
- Skalierbarkeit (unabhängige Skalierung von Reads/Writes)
- Wartbarkeit (klare Verantwortlichkeiten) führt

---

## 🔍 IST-Analyse der Services

### 1. CustomerService (716 Zeilen)

#### Aktuelle Struktur:
```java
@ApplicationScoped
public class CustomerService {
    // Mixed: 9 Query-Methoden + 8 Command-Methoden (TATSÄCHLICH GEFUNDEN)
    
    // COMMANDS (Schreiboperationen) - TATSÄCHLICH IN CustomerService.java:
    - createCustomer()           // Zeile 69-120
    - updateCustomer()           // Zeile 159-193
    - deleteCustomer()           // Zeile 204-253 (soft delete)
    - restoreCustomer()          // Zeile 262-304
    - addChildCustomer()         // Zeile 371-412
    - updateAllRiskScores()      // Zeile 444-471
    - mergeCustomers()           // Zeile 483-536
    - changeStatus()             // Zeile 542-575 ⚠️ INITIAL ÜBERSEHEN!
    
    // QUERIES (Leseoperationen) - TATSÄCHLICH IN CustomerService.java:
    - getCustomer()              // Zeile 129-148
    - getAllCustomers()          // Zeile 309-323
    - getCustomersByStatus()     // Zeile 329-342
    - getCustomersByIndustry()   // Zeile 345-356
    - getCustomerHierarchy()     // Zeile 361-368
    - getCustomersAtRisk()       // Zeile 417-426
    - getOverdueFollowUps()      // Zeile 429-438
    - checkDuplicates()          // Zeile 476-480
    - getDashboardData()         // Zeile 580-608 ⚠️ INITIAL ÜBERSEHEN!
}
```

#### Probleme:
- 🔴 Vermischung von Read/Write-Logik
- 🔴 Transaktionsgrenzen unklar (Read-Operationen in @Transactional)
- 🔴 Komplexe Dependencies (Repository, Mapper, NumberGenerator, etc.)
- 🟡 Performance: Read-Queries nicht optimiert

### 2. OpportunityService (451 Zeilen)

#### Aktuelle Struktur:
```java
@ApplicationScoped
@Transactional
public class OpportunityService {
    // Mixed: 8 Query-Methoden + 7 Command-Methoden
    
    // COMMANDS:
    - createOpportunity()        // Zeile 62-118
    - updateOpportunity()        // Zeile 120-175
    - deleteOpportunity()        // Zeile 177-195
    - moveToStage()              // Zeile 197-245
    - assignToUser()             // Zeile 247-275
    - addActivity()              // Zeile 277-305
    - closeOpportunity()         // Zeile 307-335
    
    // QUERIES:
    - findById()                 // Zeile 337-355
    - findAll()                  // Zeile 357-375
    - findByStage()              // Zeile 377-395
    - getPipelineOverview()      // Zeile 397-415
    - getWinLossAnalysis()       // Zeile 417-435
    - findByCustomer()           // Zeile 437-451
}
```

#### Probleme:
- 🔴 Class-Level @Transactional (auch für Reads!)
- 🔴 Stage-Transition-Logik mit Read-Queries vermischt
- 🟡 Audit-Integration direkt im Service

### 3. AuditService (461 Zeilen)

#### Aktuelle Struktur:
```java
@ApplicationScoped
@Startup
public class AuditService {
    // Event-Driven, aber monolithisch
    
    // ASYNC COMMANDS:
    - logAsync()                 // Zeile 79-96
    - logAsync(AuditContext)     // Zeile 99-145
    - processAuditEvent()        // Zeile 147-195
    - calculateHash()            // Zeile 197-225
    - enrichContext()            // Zeile 227-265
    
    // QUERIES:
    - findAuditTrail()           // Zeile 267-295
    - verifyIntegrity()          // Zeile 297-335
    - getAuditStatistics()       // Zeile 337-365
    - exportAuditLog()           // Zeile 367-395
    
    // EVENT HANDLING:
    - onCustomerEvent()          // Zeile 397-415
    - onOpportunityEvent()       // Zeile 417-435
    - cleanupOldEntries()        // Zeile 437-461
}
```

#### Besonderheiten:
- ✅ Bereits async und event-driven
- ✅ Hash-Chain für Integrität
- 🟡 Könnte von Event-Sourcing profitieren
- 🔴 Cleanup-Logic mit Business-Logic vermischt

---

## 🎯 SOLL-Konzept: CQRS-Architektur

### Architektur-Übersicht:

```
┌─────────────────────────────────────────────────────────────┐
│                     API Layer (REST)                         │
├─────────────────────┬───────────────────────────────────────┤
│   Command Handlers  │         Query Handlers                │
├─────────────────────┼───────────────────────────────────────┤
│  Command Services   │         Query Services                 │
│  - CustomerCommand  │         - CustomerQuery                │
│  - OpportunityCmd   │         - OpportunityQuery             │
│  - AuditCommand     │         - AuditQuery                   │
├─────────────────────┼───────────────────────────────────────┤
│   Write Models      │         Read Models                    │
│   (Entities)        │         (Projections/Views)            │
├─────────────────────┴───────────────────────────────────────┤
│                  Event Bus (Domain Events)                   │
├─────────────────────────────────────────────────────────────┤
│                     PostgreSQL                               │
│              Write DB    |    Read Views                     │
└─────────────────────────────────────────────────────────────┘
```

### Design-Prinzipien:

1. **Strikte Trennung**: Commands ändern State, Queries lesen State
2. **Event-Driven**: Commands erzeugen Domain Events
3. **Eventual Consistency**: Read Models werden async aktualisiert
4. **Optimierte Reads**: Denormalisierte Views für Performance
5. **Transaktionsgrenzen**: Nur Commands in Transaktionen

---

## 📐 Detaillierte Implementierung

### Phase 1: CustomerService Refactoring (Tag 1-2)

#### ⚠️ WICHTIGE KORREKTUR (Stand 13.08.2025):
**Der ursprüngliche Plan war FALSCH!** CustomerService nutzt KEINE Domain Events, sondern Timeline Events. Die Implementierung muss daher OHNE Event Bus erfolgen.

#### ⚠️ FEHLENDE METHODE ENTDECKT (Stand 13.08.2025):
**`changeStatus()`-Methode wurde initial übersehen!** Diese Command-Methode (Zeile 542-575 in CustomerService.java) muss noch zu CustomerCommandService hinzugefügt werden. Sie ist eine @Transactional Write-Operation die Timeline Events erstellt.

#### 1.1 CustomerCommandService.java (KORRIGIERTE VERSION)
```java
package de.freshplan.domain.customer.service.command;

@ApplicationScoped
public class CustomerCommandService {
    
    @Inject CustomerRepository repository;
    @Inject CustomerMapper mapper;
    @Inject CustomerNumberGeneratorService numberGenerator;
    // KEIN Event Bus! Nutzt Timeline Events stattdessen
    
    /**
     * Creates a new customer (EXAKTE KOPIE von CustomerService)
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request, String createdBy) {
        // Null validation - exact copy from CustomerService
        if (request == null) {
            throw new IllegalArgumentException("CreateCustomerRequest cannot be null");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("createdBy cannot be null or empty");
        }
        
        // Check for duplicates
        List<Customer> potentialDuplicates = 
            customerRepository.findPotentialDuplicates(request.companyName());
        
        if (!potentialDuplicates.isEmpty()) {
            throw new CustomerAlreadyExistsException(
                "Customer with similar company name already exists: " 
                    + potentialDuplicates.get(0).getCompanyName(),
                "companyName",
                request.companyName()
            );
        }
        
        // Generate unique customer number
        String customerNumber = numberGenerator.generateNext();
        
        // Create customer entity using mapper
        Customer customer = customerMapper.toEntity(request, customerNumber, createdBy);
        
        // Persist customer
        customerRepository.persist(customer);
        
        // Create TIMELINE event (NICHT Domain Event!)
        createTimelineEvent(
            customer,
            "CUSTOMER_CREATED",
            "Kunde erstellt mit Nummer: " + customerNumber,
            createdBy,
            ImportanceLevel.HIGH
        );
        
        return customerMapper.toResponse(customer);
    }
    
    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request, String updatedBy) {
        // Similar pattern: Validate → Update → Event → Audit
    }
    
    @Transactional
    public void deleteCustomer(UUID id, String deletedBy) {
        // Soft delete with event publishing
    }
    
    // Weitere Command-Methoden...
}
```

#### 1.2 CustomerQueryService.java
```java
package de.freshplan.domain.customer.service;

@ApplicationScoped
public class CustomerQueryService {
    
    @Inject CustomerRepository repository;
    @Inject CustomerMapper mapper;
    @Inject EntityManager em; // Für native Queries
    
    /**
     * Find customer by ID - Optimized read
     */
    public Optional<CustomerResponse> findById(UUID id) {
        return repository.findByIdActive(id)
            .map(mapper::toResponse);
    }
    
    /**
     * Search customers with filters - Uses read-optimized view
     */
    public Page<CustomerResponse> searchCustomers(CustomerSearchRequest request) {
        // Native Query für Performance
        String sql = """
            SELECT * FROM customer_search_view
            WHERE (:companyName IS NULL OR company_name ILIKE :companyName)
              AND (:status IS NULL OR status = :status)
              AND (:industry IS NULL OR industry = :industry)
              AND (:riskScoreMin IS NULL OR risk_score >= :riskScoreMin)
              AND (:riskScoreMax IS NULL OR risk_score <= :riskScoreMax)
            ORDER BY company_name
            LIMIT :limit OFFSET :offset
            """;
        
        Query query = em.createNativeQuery(sql, CustomerSearchView.class);
        // Set parameters...
        
        List<CustomerSearchView> results = query.getResultList();
        return Page.of(results.stream()
            .map(mapper::fromSearchView)
            .collect(Collectors.toList()));
    }
    
    /**
     * Get customer statistics - Cached for performance
     */
    @CacheResult(cacheName = "customer-stats")
    public CustomerStatistics getStatistics() {
        // Aggregation queries
    }
    
    // Weitere Query-Methoden...
}
```

#### 1.3 ~~Domain Events~~ ENTFÄLLT!
**KORREKTUR:** CustomerService nutzt KEINE Domain Events, sondern Timeline Events die direkt in der Datenbank gespeichert werden:

```java
// Timeline Event Helper (bereits in CustomerService vorhanden)
private void createTimelineEvent(
    Customer customer,
    String eventType,
    String description,
    String performedBy,
    ImportanceLevel importance
) {
    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType(eventType);
    event.setEventDate(LocalDateTime.now());
    event.setTitle(generateEventTitle(eventType));
    event.setDescription(description);
    event.setPerformedBy(performedBy);
    event.setImportance(importance);
    
    // WICHTIG: Category setzen (NOT NULL constraint!)
    event.setCategory(mapEventTypeToCategory(eventType));
    
    customer.getTimelineEvents().add(event);
}
```

### Phase 2: OpportunityService Refactoring (Tag 2-3)

#### 2.1 OpportunityCommandService.java
```java
@ApplicationScoped
public class OpportunityCommandService {
    
    @Inject OpportunityRepository repository;
    @Inject OpportunityMapper mapper;
    @Inject Event<OpportunityDomainEvent> eventBus;
    @Inject OpportunityStageValidator stageValidator;
    
    @Transactional
    public OpportunityResponse createOpportunity(CreateOpportunityRequest request) {
        // Validate
        validateRequest(request);
        
        // Create
        Opportunity opportunity = new Opportunity(
            request.getName(),
            OpportunityStage.NEW_LEAD,
            getCurrentUser()
        );
        
        // Set fields
        mapper.updateFromRequest(opportunity, request);
        
        // Persist
        repository.persist(opportunity);
        
        // Publish event
        eventBus.fire(new OpportunityCreatedEvent(
            opportunity.getId(),
            opportunity.getName(),
            opportunity.getStage(),
            getCurrentUser().getId()
        ));
        
        return mapper.toResponse(opportunity);
    }
    
    @Transactional
    public OpportunityResponse moveToStage(UUID id, OpportunityStage newStage, String reason) {
        Opportunity opportunity = repository.findById(id)
            .orElseThrow(() -> new OpportunityNotFoundException(id));
        
        OpportunityStage oldStage = opportunity.getStage();
        
        // Validate transition
        if (!stageValidator.canTransition(oldStage, newStage)) {
            throw new InvalidStageTransitionException(oldStage, newStage);
        }
        
        // Update
        opportunity.setStage(newStage);
        opportunity.setStageChangedAt(LocalDateTime.now());
        
        // Add activity
        opportunity.addActivity(new OpportunityActivity(
            "Stage changed from " + oldStage + " to " + newStage,
            reason,
            getCurrentUser()
        ));
        
        // Publish event
        eventBus.fire(new OpportunityStageChangedEvent(
            opportunity.getId(),
            oldStage,
            newStage,
            reason,
            getCurrentUser().getId()
        ));
        
        return mapper.toResponse(opportunity);
    }
}
```

#### 2.2 OpportunityQueryService.java
```java
@ApplicationScoped
public class OpportunityQueryService {
    
    @Inject EntityManager em;
    @Inject OpportunityMapper mapper;
    
    /**
     * Get pipeline overview with aggregated statistics
     */
    public PipelineOverviewResponse getPipelineOverview() {
        String sql = """
            SELECT 
                stage,
                COUNT(*) as count,
                SUM(expected_value) as total_value,
                AVG(EXTRACT(DAY FROM (NOW() - created_at))) as avg_age_days
            FROM opportunities
            WHERE is_deleted = false
            GROUP BY stage
            """;
        
        List<Object[]> results = em.createNativeQuery(sql).getResultList();
        
        return PipelineOverviewResponse.builder()
            .stages(mapToStageStats(results))
            .totalOpportunities(calculateTotal(results))
            .totalPipelineValue(calculateTotalValue(results))
            .build();
    }
    
    /**
     * Win/Loss analysis for reporting
     */
    @CacheResult(cacheName = "win-loss-analysis")
    public WinLossAnalysis getWinLossAnalysis(LocalDate from, LocalDate to) {
        // Complex aggregation query
    }
}
```

### Phase 3: AuditService Event-Sourcing (Tag 3-4)

#### 3.1 AuditCommandService.java
```java
@ApplicationScoped
public class AuditCommandService {
    
    @Inject AuditRepository repository;
    @Inject EventStore eventStore;
    @Inject HashCalculator hashCalculator;
    
    /**
     * Store audit event with hash chain
     */
    @Transactional
    public CompletableFuture<UUID> appendEvent(AuditableEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Calculate hash with previous
            String previousHash = repository.getLastHash();
            String eventHash = hashCalculator.calculate(event, previousHash);
            
            // 2. Store in event store
            StoredEvent storedEvent = eventStore.append(
                event.getAggregateId(),
                event.getEventType(),
                event.getEventData(),
                eventHash
            );
            
            // 3. Update projection
            updateProjection(event);
            
            return storedEvent.getId();
        }, auditExecutor);
    }
    
    /**
     * Rebuild projections from event store
     */
    @Transactional
    public void rebuildProjections(UUID aggregateId) {
        List<StoredEvent> events = eventStore.getEvents(aggregateId);
        
        for (StoredEvent event : events) {
            applyEvent(event);
        }
    }
}
```

#### 3.2 AuditQueryService.java
```java
@ApplicationScoped
public class AuditQueryService {
    
    @Inject AuditProjectionRepository projectionRepo;
    @Inject EventStore eventStore;
    @Inject IntegrityVerifier verifier;
    
    /**
     * Get audit trail from projections (fast)
     */
    public List<AuditEntryResponse> getAuditTrail(UUID entityId) {
        return projectionRepo.findByEntityId(entityId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Verify integrity of audit chain
     */
    public IntegrityReport verifyIntegrity(LocalDateTime from, LocalDateTime to) {
        List<StoredEvent> events = eventStore.getEventsBetween(from, to);
        
        return verifier.verify(events);
    }
}
```

#### 3.3 Event Store Schema
```sql
-- V219__add_event_store.sql
CREATE TABLE event_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_version INTEGER NOT NULL,
    event_data JSONB NOT NULL,
    event_metadata JSONB,
    event_hash VARCHAR(64) NOT NULL,
    previous_hash VARCHAR(64),
    occurred_at TIMESTAMP NOT NULL,
    stored_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_event_hash UNIQUE (event_hash),
    INDEX idx_aggregate (aggregate_id, event_version),
    INDEX idx_occurred_at (occurred_at),
    INDEX idx_event_type (event_type)
);

-- Read projection for audit queries
CREATE MATERIALIZED VIEW audit_projection AS
SELECT 
    e.aggregate_id as entity_id,
    e.aggregate_type as entity_type,
    e.event_type,
    e.event_data,
    e.occurred_at,
    u.username as performed_by
FROM event_store e
LEFT JOIN users u ON (e.event_metadata->>'userId')::uuid = u.id
WHERE e.aggregate_type IN ('Customer', 'Opportunity');

CREATE INDEX idx_audit_projection_entity ON audit_projection(entity_id);
```

---

## 🧪 Test-Strategie

### Unit Tests (Ziel: 90% Coverage)

#### CustomerCommandService Tests
```java
@QuarkusTest
class CustomerCommandServiceTest {
    
    @InjectMock CustomerRepository repository;
    @InjectMock Event<CustomerDomainEvent> eventBus;
    @Inject CustomerCommandService service;
    
    @Test
    void createCustomer_withValidRequest_shouldPublishEvent() {
        // Given
        CreateCustomerRequest request = validRequest();
        ArgumentCaptor<CustomerDomainEvent> eventCaptor = 
            ArgumentCaptor.forClass(CustomerDomainEvent.class);
        
        // When
        CustomerResponse response = service.createCustomer(request, "test-user");
        
        // Then
        verify(repository).persist(any(Customer.class));
        verify(eventBus).fire(eventCaptor.capture());
        
        CustomerCreatedEvent event = (CustomerCreatedEvent) eventCaptor.getValue();
        assertThat(event.companyName()).isEqualTo(request.companyName());
    }
}
```

#### CustomerQueryService Tests
```java
@QuarkusTest
class CustomerQueryServiceTest {
    
    @Test
    void searchCustomers_withFilters_shouldUseOptimizedQuery() {
        // Test that native query is used for performance
    }
    
    @Test
    void getStatistics_shouldBeCached() {
        // Test caching behavior
    }
}
```

### Integration Tests

```java
@QuarkusIntegrationTest
class CustomerCQRSIntegrationTest {
    
    @Test
    void cqrsFlow_createAndQuery_shouldWorkEndToEnd() {
        // 1. Create via Command
        CreateCustomerRequest createRequest = ...;
        CustomerResponse created = commandService.createCustomer(createRequest, "test");
        
        // 2. Wait for eventual consistency
        await().atMost(2, SECONDS).until(() -> 
            queryService.findById(created.id()).isPresent()
        );
        
        // 3. Query via Query Service
        Optional<CustomerResponse> found = queryService.findById(created.id());
        
        assertThat(found).isPresent();
        assertThat(found.get().companyName()).isEqualTo(createRequest.companyName());
    }
}
```

### Performance Tests

```java
@Test
void queryPerformance_searchWith1000Customers_shouldCompleteUnder200ms() {
    // Given: 1000 customers in DB
    
    // When
    long start = System.currentTimeMillis();
    Page<CustomerResponse> results = queryService.searchCustomers(searchRequest);
    long duration = System.currentTimeMillis() - start;
    
    // Then
    assertThat(duration).isLessThan(200);
}
```

---

## 📊 Migrations-Plan

### V219__cqrs_split_preparation.sql
```sql
-- Prepare for CQRS split without breaking existing code

-- 1. Create optimized read views
CREATE MATERIALIZED VIEW customer_search_view AS
SELECT 
    c.id,
    c.customer_number,
    c.company_name,
    c.status,
    c.industry,
    c.risk_score,
    c.expected_annual_volume,
    c.last_contact_date,
    COUNT(DISTINCT con.id) as contacts_count,
    COUNT(DISTINCT opp.id) as opportunities_count
FROM customers c
LEFT JOIN customer_contacts con ON c.id = con.customer_id
LEFT JOIN opportunities opp ON c.id = opp.customer_id
WHERE c.is_deleted = false
GROUP BY c.id;

CREATE INDEX idx_customer_search_view_company ON customer_search_view(company_name);
CREATE INDEX idx_customer_search_view_status ON customer_search_view(status);

-- 2. Event store table (siehe oben)

-- 3. Audit improvements
ALTER TABLE audit_entries ADD COLUMN IF NOT EXISTS event_id UUID;
ALTER TABLE audit_entries ADD CONSTRAINT fk_audit_event 
    FOREIGN KEY (event_id) REFERENCES event_store(id);
```

---

## 🚀 Implementierungs-Reihenfolge

### Tag 1: Setup & CustomerService Start
1. ✅ Branch erstellen: `feature/refactor-large-services`
2. ⏳ Migration V219 erstellen (wenn benötigt)
3. ~~Domain Events definieren~~ ENTFÄLLT - nutzt Timeline Events
4. 🚧 CustomerCommandService implementieren 
   - ✅ createCustomer (mit Timeline Events)
   - ✅ updateCustomer (identisch zu Original)
   - ⏳ deleteCustomer, restoreCustomer, addChildCustomer, updateAllRiskScores, mergeCustomers
5. ✅ Integration Tests für CustomerCommandService (beweisen identisches Verhalten)

### Tag 2: CustomerService Complete
1. ✅ Restliche Command-Methoden
2. ✅ CustomerQueryService implementieren
3. ✅ Read Views erstellen
4. ✅ Integration Tests
5. ✅ Performance Tests

### Tag 3: OpportunityService
1. ✅ OpportunityCommandService
2. ✅ OpportunityQueryService
3. ✅ Pipeline-Aggregationen
4. ✅ Tests

### Tag 4: AuditService & Event Store
1. ✅ Event Store implementieren
2. ✅ AuditCommandService
3. ✅ AuditQueryService
4. ✅ Hash-Chain-Verifikation
5. ✅ Tests

### Tag 5: Integration & Cleanup
1. ✅ End-to-End Tests
2. ✅ Performance-Optimierung
3. ✅ Dokumentation
4. ✅ PR erstellen

---

## ⚠️ Risiken & Mitigationen

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|------------|
| Breaking Changes in APIs | Hoch | Hoch | Facade Pattern für Übergangsphase |
| Performance-Regression | Mittel | Hoch | Benchmark vor/nach Refactoring |
| Event-Ordering Issues | Niedrig | Hoch | Event-Versioning, Idempotenz |
| Test-Komplexität | Hoch | Mittel | Schrittweise Migration |

---

## 📈 Erfolgsmetriken

### Vorher (Baseline):
- **Code-Komplexität:** 3 Services mit 1.628 Zeilen
- **Test-Coverage:** ~75%
- **Query-Performance:** 300-500ms für komplexe Suchen
- **Wartbarkeit:** Schwer (Mixed Concerns)

### Nachher (Ziel):
- **Code-Komplexität:** 6 fokussierte Services mit je <300 Zeilen
- **Test-Coverage:** >90%
- **Query-Performance:** <200ms (durch Read Views)
- **Wartbarkeit:** Einfach (Single Responsibility)

---

## 🔗 Referenzen & Ressourcen

- [Martin Fowler: CQRS](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/event-sourcing)
- [Quarkus Event Bus](https://quarkus.io/guides/reactive-event-bus)
- [Test-Strategie PR #5](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/TEST_STRATEGY_PER_PR.md#pr-5)

---

## 📝 Checkliste vor PR

- [ ] Alle Tests grün
- [ ] Performance-Benchmarks dokumentiert
- [ ] Migration V219 getestet
- [ ] Backward Compatibility sichergestellt
- [ ] Event Store funktioniert
- [ ] Read Views aktualisieren sich
- [ ] Dokumentation aktualisiert
- [ ] Team-Review durchgeführt

---

**Autor:** Claude  
**Review:** Ausstehend  
**Genehmigung:** Ausstehend

---

**Navigation:**  
⬅️ [`CODE_QUALITY_PR_ROADMAP.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/CODE_QUALITY_PR_ROADMAP.md) | ➡️ [`TEST_STRATEGY_PER_PR.md`](/Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/TEST_STRATEGY_PER_PR.md)