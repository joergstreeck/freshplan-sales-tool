# FC-017: Backend Services - Error Handling System

**Parent:** [FC-017 Error Handling System](../2025-07-25_TECH_CONCEPT_FC-017-error-handling-system.md)  
**Datum:** 2025-07-25  
**Status:** Draft  

## 📋 Übersicht

Dieses Dokument beschreibt die Backend-Services für das Error Handling System mit Fokus auf:
- Circuit Breaker Pattern für externe Services
- Fallback-Mechanismen
- Error Tracking & Monitoring
- Recovery & Retry Strategies

## 🏗️ Service-Architektur

```
ErrorHandlingServices/
├── Core/
│   ├── GlobalExceptionMapper.java
│   ├── ErrorResponse.java
│   └── ErrorTrackingService.java
├── CircuitBreaker/
│   ├── CircuitBreakerRegistry.java
│   ├── CircuitBreakerConfig.java
│   └── ServiceCircuitBreaker.java
├── Fallback/
│   ├── FallbackService.java
│   ├── CacheManager.java
│   └── OfflineQueueService.java
├── Health/
│   ├── ServiceHealthChecker.java
│   ├── HealthResource.java
│   └── HealthMetrics.java
└── Recovery/
    ├── RetryManager.java
    ├── DeadLetterQueueService.java
    └── RecoveryScheduler.java
```

## 🔧 Core Services

### GlobalExceptionMapper

```java
@Provider
@Priority(Priorities.USER)
@ApplicationScoped
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    
    @Inject
    ErrorTrackingService errorTracker;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    SecurityContext securityContext;
    
    @Override
    public Response toResponse(Throwable exception) {
        String errorId = UUID.randomUUID().toString();
        ErrorResponse errorResponse = buildErrorResponse(exception, errorId);
        
        // Track error asynchronously
        errorTracker.trackAsync(errorResponse, exception);
        
        // Notify affected users if necessary
        if (shouldNotifyUsers(exception)) {
            notificationService.notifyAffectedUsersAsync(
                errorResponse,
                securityContext.getUserPrincipal()
            );
        }
        
        // Log with context
        Log.error("Error [{}]: {}", errorId, exception.getMessage(), exception);
        
        return Response
            .status(errorResponse.getStatus())
            .entity(errorResponse)
            .header("X-Error-Id", errorId)
            .build();
    }
    
    private ErrorResponse buildErrorResponse(Throwable exception, String errorId) {
        if (exception instanceof BusinessException) {
            return handleBusinessException((BusinessException) exception, errorId);
        } else if (exception instanceof IntegrationException) {
            return handleIntegrationException((IntegrationException) exception, errorId);
        } else if (exception instanceof ValidationException) {
            return handleValidationException((ValidationException) exception, errorId);
        } else if (exception instanceof SecurityException) {
            // Don't leak security details
            return ErrorResponse.builder()
                .errorId(errorId)
                .category(ErrorCategory.AUTHORIZATION_ERROR)
                .message("Zugriff verweigert")
                .userAction("Bitte wenden Sie sich an Ihren Administrator")
                .timestamp(Instant.now())
                .build();
        }
        
        // Default internal error
        return ErrorResponse.builder()
            .errorId(errorId)
            .category(ErrorCategory.INTERNAL_ERROR)
            .message("Ein unerwarteter Fehler ist aufgetreten")
            .userAction("Bitte versuchen Sie es später erneut oder kontaktieren Sie den Support mit der Fehler-ID: " + errorId)
            .timestamp(Instant.now())
            .build();
    }
}
```

### ErrorResponse

```java
@JsonbPropertyOrder({"errorId", "category", "message", "userAction", "details", "timestamp"})
public class ErrorResponse {
    
    private String errorId;
    private ErrorCategory category;
    private String message;
    private String userAction;
    private Map<String, Object> details;
    private Instant timestamp;
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ErrorResponse response = new ErrorResponse();
        
        public Builder errorId(String errorId) {
            response.errorId = errorId;
            return this;
        }
        
        public Builder category(ErrorCategory category) {
            response.category = category;
            return this;
        }
        
        public Builder message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder userAction(String userAction) {
            response.userAction = userAction;
            return this;
        }
        
        public Builder addDetail(String key, Object value) {
            if (response.details == null) {
                response.details = new HashMap<>();
            }
            response.details.put(key, value);
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            response.timestamp = timestamp;
            return this;
        }
        
        public ErrorResponse build() {
            return response;
        }
    }
}
```

## 🔌 Circuit Breaker Implementation

### ServiceCircuitBreaker

```java
@ApplicationScoped
public class ServiceCircuitBreaker {
    
    private final Map<String, CircuitBreakerState> breakers = new ConcurrentHashMap<>();
    
    @Inject
    CircuitBreakerConfig config;
    
    @Inject
    Event<ServiceStatusChangedEvent> statusEvent;
    
    public <T> CompletionStage<T> executeAsync(
        String serviceName,
        Supplier<CompletionStage<T>> operation,
        Function<Throwable, T> fallback
    ) {
        CircuitBreakerState state = breakers.computeIfAbsent(
            serviceName,
            k -> new CircuitBreakerState(config.getConfigFor(serviceName))
        );
        
        if (state.isOpen()) {
            if (state.shouldAttemptReset()) {
                state.transitionToHalfOpen();
            } else {
                // Circuit is open, use fallback
                return CompletableFuture.completedFuture(
                    fallback.apply(new CircuitBreakerOpenException(serviceName))
                );
            }
        }
        
        return operation.get()
            .handle((result, throwable) -> {
                if (throwable != null) {
                    state.recordFailure();
                    if (state.shouldOpen()) {
                        state.transitionToOpen();
                        fireStatusEvent(serviceName, ServiceStatus.DOWN);
                    }
                    return fallback.apply(throwable);
                } else {
                    state.recordSuccess();
                    if (state.shouldClose()) {
                        state.transitionToClosed();
                        fireStatusEvent(serviceName, ServiceStatus.UP);
                    }
                    return result;
                }
            });
    }
    
    private void fireStatusEvent(String serviceName, ServiceStatus status) {
        statusEvent.fire(new ServiceStatusChangedEvent(serviceName, status));
    }
}
```

### Circuit Breaker für Xentral

```java
@ApplicationScoped
public class XentralServiceClient {
    
    @Inject
    @RestClient
    XentralApi xentralApi;
    
    @Inject
    ServiceCircuitBreaker circuitBreaker;
    
    @Inject
    FallbackService fallbackService;
    
    @Inject
    @ConfigProperty(name = "xentral.timeout", defaultValue = "30s")
    Duration timeout;
    
    public CompletionStage<CustomerData> getCustomerData(String customerId) {
        return circuitBreaker.executeAsync(
            "xentral",
            () -> executeWithTimeout(
                xentralApi.getCustomer(customerId)
                    .thenApply(this::mapToCustomerData)
            ),
            throwable -> fallbackService.getCachedCustomerData(customerId)
                .orElseThrow(() -> new ServiceUnavailableException(
                    "Xentral ist momentan nicht erreichbar und keine zwischengespeicherten Daten verfügbar",
                    "xentral_unavailable_no_cache"
                ))
        );
    }
    
    public CompletionStage<ContractData> updateContract(String contractId, ContractUpdate update) {
        return circuitBreaker.executeAsync(
            "xentral",
            () -> executeWithTimeout(
                xentralApi.updateContract(contractId, update)
                    .thenApply(this::mapToContractData)
            ),
            throwable -> {
                // Queue for later processing
                fallbackService.queueContractUpdate(contractId, update);
                // Return optimistic response
                return ContractData.fromUpdate(contractId, update)
                    .withSyncStatus(SyncStatus.PENDING);
            }
        );
    }
    
    private <T> CompletionStage<T> executeWithTimeout(CompletionStage<T> stage) {
        return stage.toCompletableFuture()
            .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
            .exceptionally(throwable -> {
                if (throwable instanceof TimeoutException) {
                    throw new IntegrationTimeoutException("xentral", timeout);
                }
                throw new CompletionException(throwable);
            });
    }
}
```

## 📦 Fallback Service

### FallbackService

```java
@ApplicationScoped
public class FallbackService {
    
    @Inject
    CacheManager cache;
    
    @Inject
    LocalDataRepository localRepo;
    
    @Inject
    OfflineQueueService offlineQueue;
    
    @Inject
    NotificationService notificationService;
    
    @ConfigProperty(name = "fallback.cache.ttl", defaultValue = "PT24H")
    Duration cacheTtl;
    
    public Optional<CustomerData> getCachedCustomerData(String customerId) {
        // 1. Try memory cache first (fastest)
        var cached = cache.get(cacheKey("customer", customerId), CustomerData.class);
        if (cached != null) {
            Log.debug("Found customer {} in memory cache", customerId);
            return Optional.of(cached);
        }
        
        // 2. Try local database snapshot
        var snapshot = localRepo.findLastKnownState(Customer.class, customerId);
        if (snapshot.isPresent()) {
            Log.debug("Found customer {} in local snapshot", customerId);
            var customerData = mapToCustomerData(snapshot.get());
            // Populate cache for next time
            cache.put(cacheKey("customer", customerId), customerData, cacheTtl);
            return Optional.of(customerData);
        }
        
        Log.warn("No fallback data available for customer {}", customerId);
        return Optional.empty();
    }
    
    public void queueContractUpdate(String contractId, ContractUpdate update) {
        OfflineOperation operation = OfflineOperation.builder()
            .id(UUID.randomUUID())
            .type(OperationType.CONTRACT_UPDATE)
            .entityId(contractId)
            .payload(Json.createObjectBuilder()
                .add("contractId", contractId)
                .add("update", toJson(update))
                .build())
            .userId(getCurrentUserId())
            .createdAt(Instant.now())
            .build();
        
        offlineQueue.enqueue(operation);
        
        // Notify user
        notificationService.notify(
            getCurrentUserId(),
            NotificationType.INFO,
            "Vertragsänderung wird ausgeführt sobald die Verbindung wiederhergestellt ist",
            Map.of("contractId", contractId, "operationId", operation.getId())
        );
    }
    
    @Scheduled(every = "5m")
    void processOfflineQueue() {
        if (!isAnyServiceAvailable()) {
            Log.debug("Skipping offline queue processing - no services available");
            return;
        }
        
        var pendingOperations = offlineQueue.getPendingOperations(100);
        Log.info("Processing {} offline operations", pendingOperations.size());
        
        for (var operation : pendingOperations) {
            try {
                processOfflineOperation(operation);
                offlineQueue.markAsProcessed(operation.getId());
            } catch (Exception e) {
                Log.error("Failed to process offline operation {}: {}", 
                    operation.getId(), e.getMessage());
                offlineQueue.incrementRetryCount(operation.getId());
                
                if (operation.getRetryCount() >= 3) {
                    // Move to dead letter queue
                    deadLetterQueue.add(operation, e);
                    offlineQueue.remove(operation.getId());
                }
            }
        }
    }
}
```

### OfflineQueueService

```java
@ApplicationScoped
@Transactional
public class OfflineQueueService {
    
    @Inject
    EntityManager em;
    
    public void enqueue(OfflineOperation operation) {
        em.persist(operation);
        em.flush();
        Log.info("Queued offline operation {} of type {}", 
            operation.getId(), operation.getType());
    }
    
    public List<OfflineOperation> getPendingOperations(int limit) {
        return em.createQuery(
            "SELECT o FROM OfflineOperation o " +
            "WHERE o.status = :status " +
            "AND o.retryCount < :maxRetries " +
            "ORDER BY o.createdAt ASC",
            OfflineOperation.class)
            .setParameter("status", OperationStatus.PENDING)
            .setParameter("maxRetries", 3)
            .setMaxResults(limit)
            .getResultList();
    }
    
    public void markAsProcessed(UUID operationId) {
        em.createQuery(
            "UPDATE OfflineOperation o " +
            "SET o.status = :status, o.processedAt = :now " +
            "WHERE o.id = :id")
            .setParameter("status", OperationStatus.PROCESSED)
            .setParameter("now", Instant.now())
            .setParameter("id", operationId)
            .executeUpdate();
    }
}
```

## 🏥 Health Check System

### ServiceHealthChecker

```java
@ApplicationScoped
public class ServiceHealthChecker {
    
    @Inject
    @RestClient
    XentralApi xentralApi;
    
    @Inject
    @RestClient
    EmailProviderApi emailApi;
    
    @Inject
    KeycloakService keycloakService;
    
    @Inject
    EntityManager em;
    
    @Inject
    Event<ServiceHealthStatusEvent> healthEvent;
    
    private final Map<String, ServiceHealth> healthCache = new ConcurrentHashMap<>();
    
    @Scheduled(every = "30s")
    void performHealthChecks() {
        var results = Map.of(
            "xentral", checkXentral(),
            "email", checkEmailProvider(),
            "keycloak", checkKeycloak(),
            "database", checkDatabase()
        );
        
        // Update cache and fire events for changes
        results.forEach((service, health) -> {
            var previous = healthCache.put(service, health);
            if (previous == null || previous.getStatus() != health.getStatus()) {
                healthEvent.fire(new ServiceHealthStatusEvent(service, health));
                Log.info("Service {} status changed: {} -> {}", 
                    service, 
                    previous != null ? previous.getStatus() : "UNKNOWN",
                    health.getStatus());
            }
        });
    }
    
    public ServiceHealth checkXentral() {
        try {
            var start = System.currentTimeMillis();
            var response = xentralApi.healthCheck()
                .toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
            var duration = System.currentTimeMillis() - start;
            
            return ServiceHealth.builder()
                .service("xentral")
                .status(response.isHealthy() ? ServiceStatus.UP : ServiceStatus.DEGRADED)
                .responseTime(duration)
                .details(Map.of(
                    "version", response.getVersion(),
                    "modules", response.getActiveModules()
                ))
                .lastCheck(Instant.now())
                .build();
        } catch (Exception e) {
            return ServiceHealth.builder()
                .service("xentral")
                .status(ServiceStatus.DOWN)
                .error(e.getMessage())
                .lastCheck(Instant.now())
                .build();
        }
    }
    
    public ServiceHealthStatus getAllServicesStatus() {
        return ServiceHealthStatus.builder()
            .services(new HashMap<>(healthCache))
            .overallStatus(calculateOverallStatus(healthCache.values()))
            .lastCheck(Instant.now())
            .build();
    }
}
```

### HealthResource

```java
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {
    
    @Inject
    ServiceHealthChecker healthChecker;
    
    @GET
    @Path("/services")
    @RolesAllowed({"admin", "monitoring"})
    public ServiceHealthStatus getServiceHealth() {
        return healthChecker.getAllServicesStatus();
    }
    
    @GET
    @Path("/ready")
    public Response readinessCheck() {
        var status = healthChecker.getAllServicesStatus();
        
        if (status.getOverallStatus() == ServiceStatus.UP) {
            return Response.ok(Map.of("status", "ready")).build();
        }
        
        if (status.hasCriticalServiceDown()) {
            return Response.status(503)
                .entity(Map.of(
                    "status", "not_ready",
                    "reason", "critical_services_down",
                    "services", status.getDownServices()
                ))
                .build();
        }
        
        // Degraded mode - some non-critical services down
        return Response.ok(Map.of(
            "status", "ready_degraded",
            "degraded_services", status.getDegradedServices()
        )).build();
    }
    
    @GET
    @Path("/live")
    public Response livenessCheck() {
        // Simple check - is the application responsive?
        return Response.ok(Map.of("status", "alive")).build();
    }
}
```

## 🔄 Recovery & Retry

### RetryManager

```java
@ApplicationScoped
public class RetryManager {
    
    @Inject
    DeadLetterQueueService dlq;
    
    @ConfigProperty(name = "retry.max-attempts", defaultValue = "3")
    int maxAttempts;
    
    @ConfigProperty(name = "retry.backoff.multiplier", defaultValue = "2")
    double backoffMultiplier;
    
    @ConfigProperty(name = "retry.initial-delay", defaultValue = "PT1S")
    Duration initialDelay;
    
    public <T> CompletionStage<T> executeWithRetry(
        Supplier<CompletionStage<T>> operation,
        RetryPolicy policy,
        String operationName
    ) {
        return executeWithRetryInternal(operation, policy, operationName, 0);
    }
    
    private <T> CompletionStage<T> executeWithRetryInternal(
        Supplier<CompletionStage<T>> operation,
        RetryPolicy policy,
        String operationName,
        int attempt
    ) {
        return operation.get()
            .handle((result, throwable) -> {
                if (throwable != null) {
                    if (attempt >= policy.getMaxAttempts()) {
                        // Max retries exceeded - send to DLQ
                        dlq.send(new FailedOperation(
                            operationName,
                            throwable,
                            attempt,
                            Instant.now()
                        ));
                        throw new RetryExhaustedException(
                            "Operation failed after " + attempt + " attempts",
                            throwable
                        );
                    }
                    
                    if (policy.shouldRetry(throwable)) {
                        Duration delay = calculateDelay(attempt, policy);
                        Log.warn("Operation {} failed (attempt {}), retrying in {}",
                            operationName, attempt + 1, delay);
                        
                        return Uni.createFrom().item(() -> null)
                            .onItem().delayIt().by(delay)
                            .onItem().transformToUni(__ -> 
                                executeWithRetryInternal(
                                    operation, 
                                    policy, 
                                    operationName, 
                                    attempt + 1
                                )
                            );
                    }
                    
                    throw new CompletionException(throwable);
                }
                return result;
            })
            .toCompletableFuture()
            .thenCompose(Function.identity());
    }
    
    private Duration calculateDelay(int attempt, RetryPolicy policy) {
        long delayMillis = (long) (
            policy.getInitialDelay().toMillis() * 
            Math.pow(policy.getBackoffMultiplier(), attempt)
        );
        return Duration.ofMillis(Math.min(delayMillis, policy.getMaxDelay().toMillis()));
    }
}
```

### DeadLetterQueueService

```java
@ApplicationScoped
@Transactional
public class DeadLetterQueueService {
    
    @Inject
    EntityManager em;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    Event<DeadLetterEvent> dlqEvent;
    
    public void send(FailedOperation operation) {
        var dlqEntry = DeadLetterQueueEntry.builder()
            .id(UUID.randomUUID())
            .operationType(operation.getType())
            .operationName(operation.getName())
            .payload(operation.getPayload())
            .errorMessage(operation.getError().getMessage())
            .stackTrace(getStackTrace(operation.getError()))
            .attemptCount(operation.getAttemptCount())
            .createdAt(Instant.now())
            .build();
        
        em.persist(dlqEntry);
        
        // Fire event for monitoring
        dlqEvent.fire(new DeadLetterEvent(dlqEntry));
        
        // Notify administrators
        notificationService.notifyAdmins(
            "Operation failed permanently",
            String.format("Operation %s failed after %d attempts and was moved to DLQ",
                operation.getName(), operation.getAttemptCount())
        );
        
        Log.error("Operation {} moved to DLQ after {} attempts",
            operation.getName(), operation.getAttemptCount());
    }
    
    public Page<DeadLetterQueueEntry> getEntries(int page, int size) {
        var query = em.createQuery(
            "SELECT d FROM DeadLetterQueueEntry d ORDER BY d.createdAt DESC",
            DeadLetterQueueEntry.class
        );
        
        var totalCount = em.createQuery(
            "SELECT COUNT(d) FROM DeadLetterQueueEntry d",
            Long.class
        ).getSingleResult();
        
        var entries = query
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
        
        return new Page<>(entries, page, size, totalCount);
    }
    
    @RolesAllowed("admin")
    public void reprocess(UUID entryId) {
        var entry = em.find(DeadLetterQueueEntry.class, entryId);
        if (entry == null) {
            throw new NotFoundException("DLQ entry not found: " + entryId);
        }
        
        // Create new operation from DLQ entry
        var operation = OfflineOperation.fromDlqEntry(entry);
        offlineQueue.enqueue(operation);
        
        // Mark as reprocessed
        entry.setReprocessedAt(Instant.now());
        entry.setStatus(DlqStatus.REPROCESSED);
        
        Log.info("DLQ entry {} requeued for processing", entryId);
    }
}
```

## 📊 Monitoring & Metrics

### ErrorMetricsCollector

```java
@ApplicationScoped
public class ErrorMetricsCollector {
    
    @Inject
    MeterRegistry registry;
    
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> recoveryTimers = new ConcurrentHashMap<>();
    
    public void recordError(ErrorCategory category, String service) {
        errorCounters.computeIfAbsent(
            "errors." + category.name().toLowerCase() + "." + service,
            name -> Counter.builder(name)
                .description("Error count by category and service")
                .tag("category", category.name())
                .tag("service", service)
                .register(registry)
        ).increment();
    }
    
    public void recordRecoveryTime(String service, Duration duration) {
        recoveryTimers.computeIfAbsent(
            "recovery.time." + service,
            name -> Timer.builder(name)
                .description("Time to recover from service failure")
                .tag("service", service)
                .register(registry)
        ).record(duration);
    }
    
    @Scheduled(every = "1m")
    void publishMetrics() {
        // Circuit breaker states
        circuitBreakerRegistry.getAllBreakers().forEach(breaker -> {
            Gauge.builder("circuit.breaker.state", breaker, b -> {
                switch (b.getState()) {
                    case CLOSED: return 0.0;
                    case OPEN: return 1.0;
                    case HALF_OPEN: return 2.0;
                    default: return -1.0;
                }
            })
            .tag("service", breaker.getName())
            .register(registry);
        });
        
        // Offline queue size
        Gauge.builder("offline.queue.size", offlineQueue, 
                OfflineQueueService::getPendingCount)
            .register(registry);
        
        // DLQ size
        Gauge.builder("dlq.size", dlq, 
                DeadLetterQueueService::getEntryCount)
            .register(registry);
    }
}
```

## 🧪 Testing Support

### TestErrorSimulator

```java
@ApplicationScoped
@Profile("test")
public class TestErrorSimulator {
    
    private final Map<String, ErrorSimulation> simulations = new ConcurrentHashMap<>();
    
    public void simulateServiceDown(String serviceName, Duration duration) {
        simulations.put(serviceName, new ErrorSimulation(
            ErrorType.SERVICE_DOWN,
            Instant.now().plus(duration)
        ));
    }
    
    public void simulateSlowResponse(String serviceName, Duration delay) {
        simulations.put(serviceName, new ErrorSimulation(
            ErrorType.SLOW_RESPONSE,
            delay
        ));
    }
    
    public void clearSimulation(String serviceName) {
        simulations.remove(serviceName);
    }
    
    public boolean shouldSimulateError(String serviceName) {
        var simulation = simulations.get(serviceName);
        if (simulation == null) return false;
        
        if (simulation.getType() == ErrorType.SERVICE_DOWN) {
            return Instant.now().isBefore(simulation.getUntil());
        }
        
        return true;
    }
}
```

---

**Nächste Schritte:**
- [ ] Circuit Breaker Konfiguration optimieren
- [ ] Monitoring Dashboard aufsetzen
- [ ] Performance Tests für Fallback-Szenarien
- [ ] Alerting Rules definieren