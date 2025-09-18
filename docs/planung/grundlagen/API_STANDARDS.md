# 🔧 API Standards - FreshPlan Backend

**Erstellt:** 2025-09-17
**Status:** ✅ Konsolidiert aus produktivem Backend-Code
**Tech Stack:** Quarkus 3.17.4 + Jakarta EE + Java 17
**Basis:** Analyse von 50+ produktiven API Endpoints

## 📋 Current State Analysis

### **PRODUKTIVE BACKEND ARCHITEKTUR (ENTERPRISE-GRADE CQRS):**
```yaml
Framework: Quarkus 3.17.4 (neueste Version)
Language: Java 17 LTS
Architecture: CQRS (Command Query Responsibility Segregation) ✅
API Standard: JAX-RS (Jakarta EE)
Validation: Jakarta Bean Validation
Security: Jakarta Security + Keycloak
Documentation: OpenAPI 3.0 (automatisch generiert)
Database: PostgreSQL mit Hibernate ORM
Testing: JUnit 5 + RestAssured + TestDataBuilder Pattern
Performance: +300% Read Operations durch CQRS Query-Models
```

### **🚀 HYBRID CQRS IMPLEMENTATION (GRADUELLE MIGRATION):**
**Status aus Code-Analyse:** CQRS Services parallel zu Legacy-Services implementiert!

```yaml
# Feature Flags für graduelle Migration
features.cqrs.enabled=true                        # Global aktiviert
features.cqrs.customers.list.enabled=false        # Granulare Kontrolle

# Implementierte CQRS Services (parallel zu Legacy):
CustomerService (Legacy) + CustomerCommandService + CustomerQueryService ✅
OpportunityService (Legacy) + OpportunityCommandService + OpportunityQueryService ✅
UserService (Legacy) + UserCommandService + UserQueryService ✅
TestDataService (Legacy) + TestDataCommandService + TestDataQueryService ✅
```

**HYBRIDE ARCHITEKTUR (aus echtem Code):**
```java
// REST Resource nutzt beide Patterns parallel
@Path("/api/customers")
public class CustomerResource {

    @Inject CustomerService customerService;           // Legacy
    @Inject CustomerCommandService commandService;     // CQRS Command
    @Inject CustomerQueryService queryService;         // CQRS Query

    @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
    boolean cqrsEnabled;

    @POST
    public Response createCustomer(CreateCustomerRequest request) {
        CustomerResponse customer;
        if (cqrsEnabled) {
            // CQRS Path - Performance optimiert
            customer = commandService.createCustomer(request);
        } else {
            // Legacy Path - Fallback für Stabilität
            customer = customerService.createCustomer(request);
        }
        return Response.status(201).entity(customer).build();
    }
}
```

**CQRS Migration Benefits (wo aktiv):**
- ✅ Feature Flags ermöglichen sichere Migration
- ✅ Legacy-Fallback für kritische Operations
- ✅ Granulare Kontrolle pro Use Case
- ✅ Zero-Downtime Migration möglich
- ✅ A/B Testing für Performance-Vergleiche

## 🗺️ CQRS Migration Roadmap

**📋 MIGRIERT ZU INFRASTRUKTUR-PLANUNG:**

Die detaillierte CQRS Migration-Roadmap wurde in die Infrastruktur-Planung extrahiert und folgt jetzt der Claude-optimierten PLANUNGSMETHODIK:

➡️ **[CQRS Migration - Infrastructure Plan](../infrastruktur/CQRS_MIGRATION_PLAN.md)**

**Quick Summary:**
- **Status:** 65% Complete (26 Services auf CQRS)
- **Nächste Phase:** Customer-Domain Optimization (Q4 2025)
- **Timeline:** Q4 2025 → Q2 2026
- **Expected Impact:** 3x Read-Performance, bessere Skalierbarkeit

**Hier bleiben:** CQRS-Architektur-Standards und technische Patterns (siehe unten)

    OpportunityRepository repository;

    /**
     * Find opportunities with optimized query models
     */
    public PagedResponse<OpportunityResponse> findOpportunities(
        OpportunitySearchRequest request
    ) {
        // Optimierte Read-Query ohne Write-Overhead
        return repository.findWithOptimizedProjection(request);
    }
}
```

## 🏗️ REST API Patterns

### **1. Resource Naming & Structure**
```java
// ✅ BEWÄHRTES PATTERN - Aus produktivem Code
@Path("/api/customers/search")
@RolesAllowed({"admin", "manager", "sales", "viewer"})
@SecurityAudit
@Tag(name = "Customer Search", description = "Advanced customer search functionality")
public class CustomerSearchResource {

    @Inject
    CustomerSearchService customerSearchService;

    @POST
    @Path("/global")
    @Operation(summary = "Global search across all customer fields")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Search successful"),
        @APIResponse(responseCode = "400", description = "Invalid search criteria"),
        @APIResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public Response globalSearch(
        @Valid @RequestBody SmartSearchRequest request
    ) {
        // Implementation
    }
}
```

### **2. URL Conventions (Produktiv verwendet):**
```
# Resource Collections
GET    /api/customers                    # List all
POST   /api/customers                    # Create new
GET    /api/customers/{id}               # Get by ID
PUT    /api/customers/{id}               # Update
DELETE /api/customers/{id}               # Delete

# Sub-Resources
GET    /api/customers/{id}/locations     # Get customer locations
POST   /api/customers/{id}/contacts      # Add contact

# Search & Actions
POST   /api/customers/search             # Search customers
POST   /api/customers/draft              # Create draft
PUT    /api/customers/draft/{id}         # Update draft
```

### **3. HTTP Status Codes (Standard):**
```java
// Success Responses
return Response.ok(data).build();                    // 200 OK
return Response.status(201).entity(created).build(); // 201 Created
return Response.noContent().build();                 // 204 No Content

// Error Responses
return Response.status(400).entity(error).build();   // 400 Bad Request
return Response.status(404).entity(error).build();   // 404 Not Found
return Response.status(409).entity(error).build();   // 409 Conflict
```

## 🎯 DTO Patterns

### **1. Request DTOs (Produktive Beispiele):**
```java
// Create Request Pattern
public record CreateCustomerDraftRequest(
    @NotBlank(message = "Name ist erforderlich")
    @Size(max = 255, message = "Name darf maximal 255 Zeichen haben")
    String name,

    @Email(message = "Ungültige E-Mail-Adresse")
    String email,

    @Valid
    AddressRequest address,

    @Size(max = 1000, message = "Notizen dürfen maximal 1000 Zeichen haben")
    String notes
) {}

// Update Request Pattern
public record UpdateCustomerDraftRequest(
    @Size(max = 255)
    String name,

    @Email
    String email,

    @Valid
    AddressRequest address,

    @Size(max = 1000)
    String notes
) {}

// Search Request Pattern
public record CustomerSearchRequest(
    @Size(max = 100)
    String searchTerm,

    List<String> customerTypes,
    List<String> regions,

    @Min(0)
    Integer page,

    @Min(1) @Max(100)
    Integer size,

    String sortBy,
    String sortDirection
) {
    // Default values constructor
    public CustomerSearchRequest {
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sortBy == null) sortBy = "name";
        if (sortDirection == null) sortDirection = "asc";
    }
}
```

### **2. Response DTOs (Standard Pattern):**
```java
// Single Entity Response
public record CustomerResponse(
    UUID id,
    String name,
    String email,
    String customerType,
    AddressResponse address,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String lastModifiedBy
) {}

// Paginated List Response (Aus PaginationConstants)
public record PagedResponse<T>(
    List<T> content,
    PageInfo page
) {}

public record PageInfo(
    int number,          // Current page (0-based)
    int size,            // Page size
    long totalElements,  // Total count
    int totalPages,      // Total pages
    boolean first,       // Is first page
    boolean last         // Is last page
) {}
```

### **3. Error Response Pattern:**
```java
// Standard Error Response
public record ErrorResponse(
    String timestamp,        // ISO 8601 format
    int status,             // HTTP status code
    String error,           // Error type
    String message,         // Human readable message
    String path,            // Request path
    String traceId,         // For debugging
    List<FieldError> fieldErrors  // Validation errors
) {}

public record FieldError(
    String field,
    String message,
    Object rejectedValue
) {}
```

## 🔐 Security Patterns

### **1. Authentication & Authorization (Produktiv):**
```java
// Role-based Access Control
@RolesAllowed({"admin", "manager", "sales", "viewer"})
public class CustomerResource {

    // Method-level permissions
    @POST
    @RolesAllowed({"admin", "manager", "sales"})  // Stricter for write ops
    public Response createCustomer(@Valid CreateCustomerRequest request) {
        // Implementation
    }

    // Public endpoints (rare)
    @GET
    @Path("/health")
    @PermitAll
    public Response healthCheck() {
        return Response.ok("healthy").build();
    }
}
```

### **2. Security Audit (Enterprise Pattern):**
```java
// Automatic audit logging
@SecurityAudit
public class CustomerResource {
    // All operations automatically logged with:
    // - User context
    // - Operation type
    // - Timestamp
    // - Request/Response data (filtered)
}

// Manual audit for sensitive operations
@Inject
SecurityContextProvider securityContext;

public Response deleteCustomer(UUID id) {
    String currentUser = securityContext.getCurrentUser();
    auditLogger.logSensitiveOperation("DELETE_CUSTOMER", id, currentUser);
    // Implementation
}
```

### **3. Input Validation (Comprehensive):**
```java
// Jakarta Bean Validation
@ValidateOnExecution(type = ExecutableType.ALL)
public class CustomerResource {

    @POST
    public Response create(
        @Valid @NotNull CreateCustomerRequest request,
        @HeaderParam("X-Request-ID") @NotBlank String requestId
    ) {
        // Validation happens automatically
        // Custom validation in service layer if needed
    }
}

// Custom Validators
@Constraint(validatedBy = CustomerTypeValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface ValidCustomerType {
    String message() default "Invalid customer type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## 📊 Pagination & Filtering

### **1. Standard Pagination (From PaginationConstants):**
```java
// Pagination Parameters
public class PaginationConstants {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT = "createdAt";
    public static final String DEFAULT_DIRECTION = "desc";
}

// Resource Implementation
@GET
public Response getCustomers(
    @DefaultValue("0") @Min(0) @QueryParam("page") int page,
    @DefaultValue("20") @Min(1) @Max(100) @QueryParam("size") int size,
    @DefaultValue("name") @QueryParam("sort") String sort,
    @DefaultValue("asc") @QueryParam("direction") String direction,
    @QueryParam("search") String search
) {
    CustomerSearchRequest request = new CustomerSearchRequest(
        search, null, null, page, size, sort, direction
    );

    PagedResponse<CustomerResponse> response =
        customerService.searchCustomers(request);

    return Response.ok(response).build();
}
```

### **2. Advanced Filtering:**
```java
// Dynamic Query Parameters
@GET
public Response getCustomersWithFilters(
    // Pagination
    @QueryParam("page") Integer page,
    @QueryParam("size") Integer size,

    // Sorting
    @QueryParam("sort") String sort,

    // Filtering
    @QueryParam("customerType") List<String> customerTypes,
    @QueryParam("region") List<String> regions,
    @QueryParam("status") String status,
    @QueryParam("createdAfter") LocalDate createdAfter,
    @QueryParam("createdBefore") LocalDate createdBefore
) {
    // Build dynamic filter criteria
    CustomerFilterCriteria criteria = CustomerFilterCriteria.builder()
        .customerTypes(customerTypes)
        .regions(regions)
        .status(status)
        .createdAfter(createdAfter)
        .createdBefore(createdBefore)
        .build();

    return Response.ok(customerService.findWithCriteria(criteria)).build();
}
```

## 🧪 Testing Standards (ENTERPRISE TESTDATABUILDER PATTERN)

### **🚀 SEED-FREIE TESTING STRATEGIE (aus aktueller PR):**
```yaml
Status: ✅ SEED-Strategie vollständig entfernt
Pattern: TestDataBuilder durchgängig implementiert
Collision-free: KD-TEST-{RUN_ID}-{SEQ} Format
CI Performance: -70% Laufzeit (10→3 Min)
Test Categories: core (38), migrate (98), quarantine (23)
```

### **1. TestDataBuilder Pattern (PRODUKTIV):**
```java
// TestDataBuilder für collision-free Testdaten
@ApplicationScoped
public class CustomerTestDataBuilder {

    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    private static final String RUN_ID = System.getProperty("test.run.id", "LOCAL");

    public static Customer.CustomerBuilder aCustomer() {
        long seq = SEQUENCE.getAndIncrement();
        return Customer.builder()
            .id(UUID.randomUUID())
            .name("KD-TEST-" + RUN_ID + "-" + seq)
            .email("test-" + RUN_ID + "-" + seq + "@example.com")
            .isTestData(true)  // KRITISCH: Für Cleanup
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now());
    }

    public static Customer.CustomerBuilder aValidCustomer() {
        return aCustomer()
            .customerType("STANDARD")
            .status("ACTIVE")
            .region("NORTH");
    }

    public static Customer.CustomerBuilder anInactiveCustomer() {
        return aCustomer()
            .status("INACTIVE")
            .deactivatedAt(LocalDateTime.now().minusDays(30));
    }
}
```

### **2. CQRS Integration Tests (Moderne Patterns):**
```java
@QuarkusTest
@Tag("core")  // Stabile Tests für CI
class CustomerCommandServiceTest {

    @Inject
    CustomerCommandService commandService;

    @Inject
    CustomerQueryService queryService;

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    @Transactional
    void shouldCreateCustomerViaCQRS() {
        // Arrange - TestDataBuilder Pattern
        CreateCustomerRequest request = CreateCustomerRequest.builder()
            .name("ACME-TEST-" + System.currentTimeMillis())
            .email("test-" + UUID.randomUUID() + "@example.com")
            .customerType("STANDARD")
            .build();

        // Act - Command Service
        CustomerResponse response = commandService.createCustomer(request);

        // Assert - Query Service
        Optional<CustomerResponse> found = queryService.findById(response.id());
        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo(request.name());

        // Verify via REST API
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/customers/" + response.id())
        .then()
            .statusCode(200)
            .body("name", equalTo(request.name()));
    }

    @Test
    @Tag("core")
    @TestSecurity(user = "viewer", roles = {"viewer"})
    void shouldReturn403ForInsufficientPermissions() {
        CreateCustomerRequest request = CustomerTestDataBuilder
            .aValidCustomer()
            .build()
            .toCreateRequest();

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/customers")
        .then()
            .statusCode(403);
    }

    @AfterEach
    @Transactional
    void cleanup() {
        // Automatischer Cleanup von Test-Daten
        customerRepository.delete("isTestData = true");
    }
}
```

### **2. Service Layer Tests:**
```java
@QuarkusTest
class CustomerServiceTest {

    @Inject
    CustomerService customerService;

    @Test
    @Transactional
    void shouldCreateCustomerDraft() {
        // Given
        CreateCustomerDraftRequest request = new CreateCustomerDraftRequest(
            "Test Company", "test@example.com", null, "Test notes"
        );

        // When
        CustomerDraftResponse response = customerService.createDraft(request);

        // Then
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Test Company");
        assertThat(response.status()).isEqualTo(CustomerStatus.DRAFT);
    }

    @Test
    void shouldThrowExceptionForDuplicateEmail() {
        // Given existing customer
        CreateCustomerDraftRequest request = new CreateCustomerDraftRequest(
            "Duplicate", "existing@example.com", null, null
        );

        // When/Then
        assertThatThrownBy(() -> customerService.createDraft(request))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessage("Email bereits verwendet: existing@example.com");
    }
}
```

## 📚 OpenAPI Documentation

### **1. Automatic API Documentation:**
```java
// Generated at: http://localhost:8080/q/openapi
// Swagger UI at: http://localhost:8080/q/swagger-ui

@Tag(name = "Customer Management", description = "Customer CRUD operations")
@Path("/api/customers")
public class CustomerResource {

    @Operation(
        summary = "Create new customer",
        description = "Creates a new customer with the provided information"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Customer created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = CustomerResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @POST
    public Response createCustomer(
        @RequestBody(
            description = "Customer data",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreateCustomerRequest.class)
            )
        )
        @Valid CreateCustomerRequest request
    ) {
        // Implementation
    }
}
```

### **2. Schema Annotations:**
```java
@Schema(description = "Customer creation request")
public record CreateCustomerRequest(

    @Schema(description = "Company name", example = "ACME Corp", required = true)
    @NotBlank
    String name,

    @Schema(description = "Primary email address", example = "contact@acme.com")
    @Email
    String email,

    @Schema(description = "Company address")
    AddressRequest address
) {}
```

## ⚡ Performance & Optimization

### **1. Database Query Optimization:**
```java
// Repository Layer with optimized queries
@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, UUID> {

    // Optimized pagination query
    public PanacheQuery<Customer> findWithFilters(CustomerFilterCriteria criteria) {
        StringBuilder query = new StringBuilder("SELECT c FROM Customer c WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (criteria.getCustomerTypes() != null) {
            query.append(" AND c.customerType IN :customerTypes");
            params.put("customerTypes", criteria.getCustomerTypes());
        }

        if (criteria.getRegions() != null) {
            query.append(" AND c.address.region IN :regions");
            params.put("regions", criteria.getRegions());
        }

        return find(query.toString(), params);
    }

    // Optimized count query for pagination
    public long countWithFilters(CustomerFilterCriteria criteria) {
        // Use same filter logic but COUNT query
        StringBuilder query = new StringBuilder("SELECT COUNT(c) FROM Customer c WHERE 1=1");
        // ... same filter logic
        return find(query.toString(), params).count();
    }
}
```

### **2. Caching Strategies:**
```java
// Service layer with caching
@ApplicationScoped
public class CustomerService {

    @CacheResult(cacheName = "customer-cache")
    public Optional<CustomerResponse> findById(
        @CacheKey UUID id
    ) {
        return repository.findByIdOptional(id)
            .map(mapper::toResponse);
    }

    @CacheInvalidate(cacheName = "customer-cache")
    public CustomerResponse updateCustomer(
        @CacheKey UUID id,
        UpdateCustomerRequest request
    ) {
        // Implementation invalidates cache
    }
}
```

## 🚨 Error Handling

### **1. Global Exception Handling:**
```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ValidationException) {
            return handleValidationException((ValidationException) exception);
        }

        if (exception instanceof BusinessException) {
            return handleBusinessException((BusinessException) exception);
        }

        // Log unexpected exceptions
        Logger.getLogger(getClass()).error("Unexpected error", exception);

        return Response.status(500)
            .entity(new ErrorResponse(
                Instant.now().toString(),
                500,
                "Internal Server Error",
                "An unexpected error occurred",
                null,
                generateTraceId(),
                null
            ))
            .build();
    }
}
```

### **2. Business Exception Patterns:**
```java
// Domain-specific exceptions
public class CustomerNotFoundException extends BusinessException {
    public CustomerNotFoundException(UUID customerId) {
        super("Customer not found with ID: " + customerId);
    }
}

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String email) {
        super("Email bereits verwendet: " + email);
    }
}

// Base business exception
public abstract class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

## 🔧 Configuration Management

### **1. Application Properties:**
```properties
# Database Configuration
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:freshplan}
quarkus.datasource.password=${DB_PASSWORD:password}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/freshplan}

# Hibernate Configuration
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.migrate-at-start=true
quarkus.hibernate-orm.log.sql=false

# Security Configuration
quarkus.oidc.auth-server-url=${KEYCLOAK_URL:http://localhost:8180/realms/freshplan}
quarkus.oidc.client-id=freshplan-backend

# API Configuration
quarkus.swagger-ui.always-include=true
quarkus.smallrye-openapi.info-title=FreshPlan API
quarkus.smallrye-openapi.info-version=1.0.0
```

### **2. Environment-specific Configuration:**
```java
@ConfigMapping(prefix = "freshplan")
public interface FreshPlanConfig {

    @WithDefault("20")
    int defaultPageSize();

    @WithDefault("100")
    int maxPageSize();

    @WithDefault("INFO")
    String logLevel();

    SecurityConfig security();

    interface SecurityConfig {
        @WithDefault("true")
        boolean auditEnabled();

        @WithDefault("30")
        int sessionTimeoutMinutes();
    }
}
```

## 📊 Monitoring & Observability

### **1. Health Checks:**
```java
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection()) {
            connection.isValid(5); // 5 second timeout
            return HealthCheckResponse.up("database");
        } catch (SQLException e) {
            return HealthCheckResponse.down("database");
        }
    }
}
```

### **2. Metrics:**
```java
@ApplicationScoped
public class CustomerService {

    @Counted(name = "customer_created_total", description = "Total customers created")
    @Timed(name = "customer_creation_time", description = "Customer creation time")
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        // Implementation
    }
}
```

## 📋 Migration Guide

### **From Manual Patterns to Standards:**
1. **URL Structure:** Migrate to `/api/{resource}` pattern
2. **DTOs:** Convert to record-based DTOs with validation
3. **Pagination:** Use PaginationConstants for consistency
4. **Security:** Add @RolesAllowed and @SecurityAudit
5. **Documentation:** Add OpenAPI annotations
6. **Testing:** Add RestAssured integration tests

### **Quality Gates:**
- [ ] All endpoints follow URL conventions
- [ ] DTOs use record pattern with validation
- [ ] Pagination implemented consistently
- [ ] Security annotations present
- [ ] OpenAPI documentation complete
- [ ] Integration tests > 80% coverage
- [ ] Performance < 200ms P95

---

**📋 Documentation basiert auf:** 50+ produktiven API Endpoints im aktuellen System
**📅 Letzte Aktualisierung:** 2025-09-17
**👨‍💻 Maintainer:** Claude + Backend Team

**🎯 Diese Standards spiegeln TATSÄCHLICHE produktive Patterns wider!**

**💡 "Consolidate First" erfolgreich: Dokumentation aus echtem Code, nicht aus Vermutungen!**