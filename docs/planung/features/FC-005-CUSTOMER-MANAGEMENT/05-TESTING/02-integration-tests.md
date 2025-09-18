---
Navigation: [⬅️ Previous](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/01-unit-tests.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/03-e2e-tests.md)
Parent: [📁 Testing](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)
Related: [🔗 Backend API](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/03-rest-api.md) | [🔗 Database](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/04-database.md)
---

# 🔌 FC-005 INTEGRATION TESTS

**Fokus:** API & Database Integration  
**Coverage-Ziel:** 70-80%  
**Frameworks:** RestAssured, TestContainers, Quarkus Test  

## 📋 API Integration Tests

### Customer Resource Integration Test

```java
@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
class CustomerResourceIT {
    
    @Test
    @TestSecurity(user = "testuser", roles = "sales")
    void testCustomerDraftWorkflow() {
        // Step 1: Create draft
        CreateCustomerDraftRequest createRequest = buildCreateRequest();
        
        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/customers/draft")
            .then()
            .statusCode(200)
            .extract()
            .response();
            
        String draftId = createResponse.jsonPath().getString("id");
        assertThat(draftId).isNotNull();
        
        // Step 2: Update draft
        UpdateCustomerRequest updateRequest = UpdateCustomerRequest.builder()
            .fieldValues(List.of(
                new FieldValueRequest("email", "test@example.com"),
                new FieldValueRequest("phone", "+49 123 456789")
            ))
            .build();
            
        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/customers/draft/{id}", draftId)
            .then()
            .statusCode(200);
            
        // Step 3: Add location
        CreateLocationRequest locationRequest = buildLocationRequest();
        
        String locationId = given()
            .contentType(ContentType.JSON)
            .body(locationRequest)
            .when()
            .post("/api/customers/{id}/locations", draftId)
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("id");
            
        // Step 4: Finalize draft
        given()
            .when()
            .post("/api/customers/draft/{id}/finalize", draftId)
            .then()
            .statusCode(200)
            .body("status", equalTo("ACTIVE"))
            .body("customerNumber", notNullValue())
            .body("locations", hasSize(1));
    }
    
    @Test
    @TestSecurity(user = "testuser", roles = "admin")
    void testCustomerSearch() {
        // Given: Create test customers
        createTestCustomers();
        
        // When: Search by industry
        given()
            .queryParam("industry", "hotel")
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when()
            .get("/api/customers")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)))
            .body("content[0].fieldValues.industry", equalTo("hotel"));
    }
    
    @Test
    void testUnauthorizedAccess() {
        given()
            .when()
            .get("/api/customers")
            .then()
            .statusCode(401);
    }
}
```

### Field Value Repository Integration Test

```java
@QuarkusTest
@TestProfile(DatabaseTestProfile.class)
class FieldValueRepositoryIT {
    
    @Inject
    FieldValueRepository repository;
    
    @Test
    @TestTransaction
    void testComplexFieldValueQueries() {
        // Given
        UUID customerId = UUID.randomUUID();
        createFieldValues(customerId);
        
        // When: Query by entity and type
        List<FieldValue> values = repository.findByEntityIdAndType(
            customerId, 
            EntityType.CUSTOMER
        );
        
        // Then
        assertThat(values).hasSize(5);
        assertThat(values)
            .extracting(FieldValue::getFieldDefinitionId)
            .containsExactlyInAnyOrder(
                "companyName", "industry", "email", "phone", "chainCustomer"
            );
    }
    
    @Test
    @TestTransaction
    void testFieldValueHistory() {
        // Given
        UUID entityId = UUID.randomUUID();
        String fieldId = "companyName";
        
        // Create initial value
        FieldValue initial = new FieldValue();
        initial.setEntityId(entityId);
        initial.setFieldDefinitionId(fieldId);
        initial.setValue("Old Name");
        repository.persist(initial);
        
        // Update value
        initial.setValue("New Name");
        repository.persist(initial);
        
        // When: Query audit history
        List<AuditEntry> history = repository.getFieldHistory(entityId, fieldId);
        
        // Then
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getOldValue()).isNull();
        assertThat(history.get(0).getNewValue()).isEqualTo("Old Name");
        assertThat(history.get(1).getOldValue()).isEqualTo("Old Name");
        assertThat(history.get(1).getNewValue()).isEqualTo("New Name");
    }
}
```

## 📋 Event System Integration Tests

### Domain Event Publishing Test

```java
@QuarkusTest
@TestProfile(EventTestProfile.class)
class CustomerEventIT {
    
    @Inject
    CustomerService customerService;
    
    @InjectSpy
    Event<CustomerCreatedEvent> customerCreatedEvent;
    
    @InjectSpy
    Event<LocationAddedEvent> locationAddedEvent;
    
    @Test
    @TestTransaction
    void testEventChainOnCustomerCreation() {
        // Given
        CreateCustomerRequest request = buildCompleteCustomerRequest();
        List<CustomerCreatedEvent> capturedEvents = new ArrayList<>();
        
        doAnswer(invocation -> {
            capturedEvents.add(invocation.getArgument(0));
            return null;
        }).when(customerCreatedEvent).fire(any());
        
        // When
        CustomerResponse response = customerService.createCustomer(request);
        
        // Then: Verify event was fired
        assertThat(capturedEvents).hasSize(1);
        CustomerCreatedEvent event = capturedEvents.get(0);
        assertThat(event.getCustomerId()).isEqualTo(response.getId());
        assertThat(event.isChainCustomer()).isTrue();
        assertThat(event.getIndustry()).isEqualTo("hotel");
    }
    
    @Test
    void testCrossBoundaryEventHandling() {
        // Test that other modules receive events
        // This would involve checking audit logs, notifications, etc.
    }
}
```

## 📋 Database Transaction Tests

### Transaction Rollback Test

```java
@QuarkusTest
class TransactionIT {
    
    @Inject
    CustomerService customerService;
    
    @Inject
    LocationService locationService;
    
    @Test
    void testTransactionRollbackOnError() {
        // Given
        Customer customer = createTestCustomer();
        
        // When: Try to add invalid location (should fail)
        Location invalidLocation = new Location();
        // Missing required fields
        
        // Then: Entire transaction should rollback
        assertThrows(TransactionRolledbackException.class, () -> {
            customerService.addLocationToCustomer(
                customer.getId(), 
                invalidLocation
            );
        });
        
        // Verify customer is unchanged
        Customer reloaded = customerService.findById(customer.getId());
        assertThat(reloaded.getLocations()).isEmpty();
    }
}
```

## 📋 Test Data Management

### Test Data Builder

```java
@ApplicationScoped
public class CustomerTestDataBuilder {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    FieldValueService fieldValueService;
    
    @Transactional
    public Customer createCompleteCustomer(String industry) {
        // Create customer
        Customer customer = new Customer();
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setCustomerNumber(generateTestCustomerNumber());
        customerRepository.persist(customer);
        
        // Add field values
        Map<String, Object> fieldValues = Map.of(
            "companyName", "Test " + industry + " GmbH",
            "industry", industry,
            "email", "test@" + industry + ".de",
            "chainCustomer", "ja"
        );
        
        fieldValues.forEach((key, value) -> 
            fieldValueService.createFieldValue(
                customer.getId(),
                EntityType.CUSTOMER,
                new FieldValueRequest(key, value)
            )
        );
        
        return customer;
    }
    
    @Transactional
    public void cleanupTestData() {
        customerRepository.deleteAll();
    }
}
```

### Test Profile Configuration

```java
public class IntegrationTestProfile implements QuarkusTestProfile {
    
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            // Use TestContainers PostgreSQL
            "quarkus.datasource.db-kind", "postgresql",
            "quarkus.datasource.devservices.enabled", "true",
            
            // Disable auth for most tests
            "%test.quarkus.oidc.enabled", "false",
            
            // Enable SQL logging
            "quarkus.hibernate-orm.log.sql", "true",
            
            // Test-specific feature flags
            "feature.customer.draft-mode", "true",
            "feature.customer.auto-numbering", "true"
        );
    }
    
    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return Set.of(MockEmailService.class);
    }
}
```

## 🔧 TestContainers Setup

```java
@QuarkusTest
@QuarkusTestResource(PostgreSQLTestResource.class)
class DatabaseIT {
    // Tests run against real PostgreSQL in container
}

public class PostgreSQLTestResource implements QuarkusTestResourceLifecycleManager {
    
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("freshplan_test")
            .withUsername("test")
            .withPassword("test");
    
    @Override
    public Map<String, String> start() {
        db.start();
        return Map.of(
            "quarkus.datasource.jdbc.url", db.getJdbcUrl(),
            "quarkus.datasource.username", db.getUsername(),
            "quarkus.datasource.password", db.getPassword()
        );
    }
    
    @Override
    public void stop() {
        db.stop();
    }
}
```

## 📚 Weiterführende Links

- [E2E Tests →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/03-e2e-tests.md)
- [Test Profiles](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md#test-umgebungen)
- [CI Integration](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md#ci-monitoring)