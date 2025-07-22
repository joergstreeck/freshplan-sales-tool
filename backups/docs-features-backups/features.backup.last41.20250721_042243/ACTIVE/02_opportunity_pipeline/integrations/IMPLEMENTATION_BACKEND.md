# FC-011: Bonitätsprüfung - Backend Implementation

**Fokus:** API Integration, Caching, Business Logic

## 🏗️ Service Architecture

### CreditCheckService

```java
@ApplicationScoped
@Transactional
public class CreditCheckService {
    
    @Inject
    CreditCheckApiClient apiClient;
    
    @Inject
    CreditCheckRepository repository;
    
    @Inject
    CreditCheckCache cache;
    
    @ConfigProperty(name = "creditcheck.cache.ttl")
    Duration cacheTtl;
    
    public CreditCheckResult checkCredit(Customer customer) {
        // 1. Check cache first
        Optional<CreditCheckResult> cached = cache.get(customer.getId());
        if (cached.isPresent() && !cached.get().isExpired()) {
            return cached.get();
        }
        
        // 2. Call external API
        CreditCheckRequest request = buildRequest(customer);
        CreditCheckResponse response = apiClient.checkCredit(request);
        
        // 3. Process and store result
        CreditCheckResult result = processResponse(response, customer);
        repository.persist(result);
        cache.put(customer.getId(), result, cacheTtl);
        
        // 4. Trigger alerts if needed
        checkAlerts(result);
        
        return result;
    }
    
    private CreditCheckRequest buildRequest(Customer customer) {
        return CreditCheckRequest.builder()
            .companyName(customer.getCompanyName())
            .registrationNumber(customer.getRegistrationNumber())
            .vatId(customer.getVatId())
            .address(mapAddress(customer.getAddress()))
            .build();
    }
    
    private void checkAlerts(CreditCheckResult result) {
        if (result.getRiskLevel() == RiskLevel.HIGH) {
            alertService.sendHighRiskAlert(result);
        }
        
        if (result.getScore() < 300) {
            alertService.sendLowScoreAlert(result);
        }
    }
}
```

### External API Client

```java
@ApplicationScoped
@RegisterRestClient(configKey = "creditcheck-api")
public interface CreditCheckApiClient {
    
    @POST
    @Path("/check")
    @ClientHeaderParam(name = "X-API-Key", value = "${creditcheck.api.key}")
    CreditCheckResponse checkCredit(CreditCheckRequest request);
    
    @GET
    @Path("/status/{checkId}")
    @ClientHeaderParam(name = "X-API-Key", value = "${creditcheck.api.key}")
    CreditCheckStatus getStatus(@PathParam("checkId") String checkId);
}

// Fallback implementation
@ApplicationScoped
public class CreditCheckFallback {
    
    @Fallback(fallbackMethod = "checkCreditFallback")
    public CreditCheckResponse checkCredit(CreditCheckRequest request) {
        // Main implementation
    }
    
    public CreditCheckResponse checkCreditFallback(CreditCheckRequest request) {
        // Return cached or default low-risk result
        return CreditCheckResponse.builder()
            .status("FALLBACK")
            .score(500)
            .riskLevel(RiskLevel.MEDIUM)
            .message("Service temporarily unavailable")
            .build();
    }
}
```

## 💾 Data Models

### Entities

```java
@Entity
@Table(name = "credit_check_results")
public class CreditCheckResult extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "opportunity_id")
    public Opportunity opportunity;
    
    @Column(name = "check_date", nullable = false)
    public LocalDateTime checkDate;
    
    @Column(name = "provider", nullable = false)
    public String provider;
    
    @Column(name = "score", nullable = false)
    public Integer score;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    public RiskLevel riskLevel;
    
    @Column(name = "credit_limit")
    public BigDecimal creditLimit;
    
    @Column(name = "payment_terms")
    public String paymentTerms;
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    public Map<String, Object> additionalData;
    
    @Column(name = "valid_until")
    public LocalDateTime validUntil;
    
    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
    
    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDateTime.now());
    }
    
    public boolean requiresManualReview() {
        return score < 400 || riskLevel == RiskLevel.HIGH;
    }
}
```

### DTOs

```java
// Request DTO
public record CreditCheckRequestDTO(
    UUID customerId,
    UUID opportunityId,
    Boolean forceRefresh
) {}

// Response DTO
public record CreditCheckResponseDTO(
    UUID checkId,
    Integer score,
    RiskLevel riskLevel,
    BigDecimal creditLimit,
    String paymentTerms,
    LocalDateTime validUntil,
    List<RiskFactorDTO> riskFactors,
    Boolean requiresManualReview
) {
    public static CreditCheckResponseDTO from(CreditCheckResult result) {
        return new CreditCheckResponseDTO(
            result.id,
            result.score,
            result.riskLevel,
            result.creditLimit,
            result.paymentTerms,
            result.validUntil,
            mapRiskFactors(result.additionalData),
            result.requiresManualReview()
        );
    }
}
```

## 🔄 Caching Strategy

### Redis Cache Implementation

```java
@ApplicationScoped
public class CreditCheckCache {
    
    @Inject
    RedisClient redisClient;
    
    private static final String KEY_PREFIX = "creditcheck:";
    
    public Optional<CreditCheckResult> get(UUID customerId) {
        String key = KEY_PREFIX + customerId;
        Response response = redisClient.get(key);
        
        if (response == null) {
            return Optional.empty();
        }
        
        return Optional.of(deserialize(response.toString()));
    }
    
    public void put(UUID customerId, CreditCheckResult result, Duration ttl) {
        String key = KEY_PREFIX + customerId;
        String value = serialize(result);
        
        redisClient.setex(key, ttl.toSeconds(), value);
    }
    
    public void invalidate(UUID customerId) {
        redisClient.del(KEY_PREFIX + customerId);
    }
    
    @ConsumeEvent("customer-updated")
    public void onCustomerUpdated(CustomerUpdatedEvent event) {
        // Invalidate cache when customer data changes
        invalidate(event.getCustomerId());
    }
}
```

## 🚨 Alert System

```java
@ApplicationScoped
public class CreditAlertService {
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    OpportunityService opportunityService;
    
    public void sendHighRiskAlert(CreditCheckResult result) {
        Opportunity opportunity = result.opportunity;
        
        String message = String.format(
            "⚠️ Hohe Risikowarnung für %s: Score %d, Risiko: %s",
            result.customer.getCompanyName(),
            result.score,
            result.riskLevel
        );
        
        // Notify opportunity owner
        notificationService.sendNotification(
            opportunity.getOwner(),
            "Bonitätswarnung",
            message,
            NotificationType.WARNING
        );
        
        // Add activity to opportunity
        opportunityService.addActivity(
            opportunity.getId(),
            ActivityType.CREDIT_CHECK_WARNING,
            message
        );
    }
    
    public void sendLowScoreAlert(CreditCheckResult result) {
        // Alert management about very low scores
        if (result.score < 200) {
            notificationService.sendToRole(
                "CREDIT_MANAGER",
                "Kritische Bonität",
                String.format(
                    "Sehr niedriger Score (%d) für %s",
                    result.score,
                    result.customer.getCompanyName()
                ),
                NotificationType.CRITICAL
            );
        }
    }
}
```

## 📊 REST Endpoints

```java
@Path("/api/credit-check")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreditCheckResource {
    
    @Inject
    CreditCheckService creditCheckService;
    
    @POST
    @RolesAllowed({"sales", "credit_manager", "admin"})
    public Response checkCredit(@Valid CreditCheckRequestDTO request) {
        Customer customer = Customer.findById(request.customerId());
        if (customer == null) {
            return Response.status(404).entity(
                Map.of("error", "Customer not found")
            ).build();
        }
        
        CreditCheckResult result;
        if (request.forceRefresh() != null && request.forceRefresh()) {
            result = creditCheckService.forceCheck(customer);
        } else {
            result = creditCheckService.checkCredit(customer);
        }
        
        return Response.ok(CreditCheckResponseDTO.from(result)).build();
    }
    
    @GET
    @Path("/customer/{customerId}")
    @RolesAllowed({"sales", "credit_manager", "admin"})
    public Response getLatestCheck(@PathParam("customerId") UUID customerId) {
        Optional<CreditCheckResult> latest = creditCheckService
            .getLatestCheck(customerId);
            
        return latest
            .map(result -> Response.ok(CreditCheckResponseDTO.from(result)))
            .orElse(Response.status(404))
            .build();
    }
    
    @GET
    @Path("/history/{customerId}")
    @RolesAllowed({"credit_manager", "admin"})
    public Response getCheckHistory(
        @PathParam("customerId") UUID customerId,
        @QueryParam("limit") @DefaultValue("10") int limit
    ) {
        List<CreditCheckResult> history = creditCheckService
            .getHistory(customerId, limit);
            
        List<CreditCheckResponseDTO> response = history.stream()
            .map(CreditCheckResponseDTO::from)
            .toList();
            
        return Response.ok(response).build();
    }
}
```