# 🔧 Enterprise Refactoring Guide - Konkrete Lösungen

## 1. Security-Abstraktion mit @CurrentUser

### Problem:
```java
// Überall verstreut:
String createdBy = securityContext.getUsername();
if (createdBy == null) {
    createdBy = "system";
}
```

### Enterprise-Lösung:

#### a) CurrentUser Annotation erstellen:
```java
package de.freshplan.infrastructure.security;

import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface CurrentUser {
}
```

#### b) CurrentUserProducer implementieren:
```java
package de.freshplan.infrastructure.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
public class CurrentUserProducer {
    
    @Inject
    JsonWebToken jwt;
    
    @Produces
    @CurrentUser
    public UserPrincipal getCurrentUser() {
        if (jwt == null || jwt.getName() == null) {
            // Development mode fallback
            return UserPrincipal.system();
        }
        
        return UserPrincipal.builder()
            .username(jwt.getName())
            .email(jwt.getClaim("email"))
            .roles(jwt.getGroups())
            .build();
    }
}
```

#### c) UserPrincipal Value Object:
```java
package de.freshplan.infrastructure.security;

import java.util.Set;

public final class UserPrincipal {
    private final String username;
    private final String email;
    private final Set<String> roles;
    
    // Private constructor - use builder
    
    public static UserPrincipal system() {
        return builder()
            .username("system")
            .email("system@freshplan.de")
            .roles(Set.of("system"))
            .build();
    }
    
    // Builder pattern implementation
}
```

#### d) Verwendung in Resources:
```java
@Path("/api/customers")
public class CustomerResource {
    
    @Inject @CurrentUser
    UserPrincipal currentUser;
    
    @POST
    public Response createCustomer(@Valid CreateCustomerRequest request) {
        // Kein if-null Check mehr nötig!
        CustomerResponse customer = customerService.createCustomer(
            request, 
            currentUser.getUsername()
        );
        return Response.status(CREATED).entity(customer).build();
    }
}
```

## 2. Transaction-Boundary Best Practices

### Problem:
```java
@ApplicationScoped
@Transactional  // ANTI-PATTERN!
public class CustomerService {
```

### Enterprise-Lösung:

```java
@ApplicationScoped
public class CustomerService {
    
    // Read-only operations - KEINE Transaction!
    public CustomerResponse getCustomer(UUID id) {
        return customerRepository.findByIdActive(id)
            .map(customerMapper::toResponse)
            .orElseThrow(() -> new CustomerNotFoundException(id));
    }
    
    // Write operations - Explizite Transaction
    @Transactional
    public CustomerResponse createCustomer(
        CreateCustomerRequest request, 
        String createdBy) {
        // Transaction nur hier
    }
    
    // Alternativ: Command/Query Separation
}
```

## 3. Error Response Standardisierung

### Enterprise Error Response:
```java
package de.freshplan.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorResponse {
    private final String type;        // "VALIDATION_ERROR", "NOT_FOUND", etc.
    private final String title;       // Human-readable title
    private final int status;         // HTTP status code
    private final String detail;      // Detailed error message
    private final String instance;    // URI of request
    private final LocalDateTime timestamp;
    private final Map<String, List<String>> violations; // Field violations
    
    // Builder pattern
    
    public static ErrorResponse notFound(String resource, String id) {
        return builder()
            .type("RESOURCE_NOT_FOUND")
            .title("Resource Not Found")
            .status(404)
            .detail(String.format("%s with ID %s not found", resource, id))
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static ErrorResponse validation(Map<String, List<String>> violations) {
        return builder()
            .type("VALIDATION_ERROR")
            .title("Validation Failed")
            .status(400)
            .detail("The request contains invalid fields")
            .violations(violations)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

### Global Exception Mapper:
```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);
    
    @Context
    UriInfo uriInfo;
    
    @Override
    public Response toResponse(Exception exception) {
        return switch (exception) {
            case CustomerNotFoundException e -> handleNotFound(e);
            case ValidationException e -> handleValidation(e);
            case IllegalArgumentException e -> handleBadRequest(e);
            case SecurityException e -> handleSecurity(e);
            default -> handleGeneric(exception);
        };
    }
    
    private Response handleNotFound(ResourceNotFoundException e) {
        return Response.status(NOT_FOUND)
            .entity(ErrorResponse.notFound(e.getResource(), e.getId()))
            .build();
    }
}
```

## 4. Pagination Standards

### Enterprise Pagination:
```java
package de.freshplan.api.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public final class PaginationRequest {
    
    @Min(0)
    private final int page;
    
    @Min(1) @Max(100)
    private final int size;
    
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    
    public static PaginationRequest of(Integer page, Integer size) {
        return new PaginationRequest(
            page != null ? Math.max(0, page) : DEFAULT_PAGE,
            size != null ? Math.min(Math.max(1, size), MAX_SIZE) : DEFAULT_SIZE
        );
    }
}
```

## 5. Constants für Magic Numbers

```java
package de.freshplan.domain.customer.constants;

public final class CustomerConstants {
    
    // Risk Management
    public static final int DEFAULT_RISK_THRESHOLD = 70;
    public static final int HIGH_RISK_THRESHOLD = 85;
    public static final int CRITICAL_RISK_THRESHOLD = 95;
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Business Rules
    public static final int DAYS_UNTIL_RISK = 90;
    public static final int DAYS_UNTIL_INACTIVE = 180;
    
    // Formats
    public static final String CUSTOMER_NUMBER_FORMAT = "KD-%d-%05d";
    public static final String CUSTOMER_NUMBER_PATTERN = "KD-\\d{4}-\\d{5}";
    
    private CustomerConstants() {
        // Utility class
    }
}
```

## 6. Builder Pattern für CustomerResponse

```java
public final class CustomerResponse {
    // Alle fields final
    
    private CustomerResponse(Builder builder) {
        this.id = builder.id;
        this.customerNumber = builder.customerNumber;
        // ... alle anderen fields
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Convenience Factory Method
    public static CustomerResponse from(Customer entity) {
        return builder()
            .id(entity.getId().toString())
            .customerNumber(entity.getCustomerNumber())
            .companyName(entity.getCompanyName())
            .status(entity.getStatus())
            .riskScore(entity.getRiskScore())
            // ... weitere mappings
            .build();
    }
    
    public static class Builder {
        // Builder implementation
    }
}
```

## 7. Service Layer Refactoring (CQRS-Style)

### Command Service:
```java
@ApplicationScoped
public class CustomerCommandService {
    
    @Inject CustomerRepository repository;
    @Inject CustomerEventPublisher eventPublisher;
    
    @Transactional
    public UUID createCustomer(CreateCustomerCommand command) {
        // Validation
        // Business Logic
        // Persistence
        // Event Publishing
        return customer.getId();
    }
}
```

### Query Service:
```java
@ApplicationScoped
public class CustomerQueryService {
    
    @Inject CustomerReadRepository repository;
    
    // Keine @Transactional!
    public CustomerDetails getCustomerDetails(UUID id) {
        return repository.findDetailView(id)
            .orElseThrow(() -> new CustomerNotFoundException(id));
    }
    
    public Page<CustomerListItem> searchCustomers(CustomerSearchCriteria criteria) {
        // Optimierte Read-Queries
    }
}
```

## 8. Audit Trail mit CDI Events

```java
@ApplicationScoped
public class AuditService {
    
    @Inject Event<AuditEvent> auditEvent;
    
    public void audit(AuditableOperation operation) {
        auditEvent.fire(AuditEvent.builder()
            .timestamp(LocalDateTime.now())
            .user(operation.getUser())
            .action(operation.getAction())
            .resource(operation.getResource())
            .resourceId(operation.getResourceId())
            .changes(operation.getChanges())
            .build()
        );
    }
}

// Observer
@ApplicationScoped
public class AuditLogger {
    
    public void onAuditEvent(@Observes AuditEvent event) {
        // Log to database, file, or external system
    }
}
```

## 9. Performance Optimization

### Lazy Loading Control:
```java
@Entity
@NamedEntityGraph(
    name = "customer-with-children",
    attributeNodes = @NamedAttributeNode("childCustomers")
)
public class Customer {
    // Entity definition
}

// Repository
public Optional<Customer> findByIdWithChildren(UUID id) {
    return find("id = ?1 AND isDeleted = false", id)
        .withHint("jakarta.persistence.loadgraph", 
                  getEntityManager().getEntityGraph("customer-with-children"))
        .firstResultOptional();
}
```

## 10. Clean Code Refactoring

### Extract Method für Timeline Events:
```java
@ApplicationScoped
public class CustomerTimelineService {
    
    public void recordCustomerCreation(Customer customer, String createdBy) {
        recordEvent(customer, 
            EventType.CUSTOMER_CREATED,
            "Kunde erstellt mit Nummer: " + customer.getCustomerNumber(),
            createdBy,
            ImportanceLevel.HIGH
        );
    }
    
    public void recordStatusChange(Customer customer, 
                                 CustomerStatus oldStatus, 
                                 CustomerStatus newStatus,
                                 String changedBy) {
        recordEvent(customer,
            EventType.STATUS_CHANGE,
            String.format("Status geändert von %s zu %s", oldStatus, newStatus),
            changedBy,
            ImportanceLevel.MEDIUM
        );
    }
    
    private void recordEvent(Customer customer, 
                           EventType type,
                           String description,
                           String performedBy,
                           ImportanceLevel importance) {
        // Zentrale Event-Erstellung
    }
}
```

---

**Diese Refactorings bringen den Code auf Enterprise-Level und machen ihn production-ready!**