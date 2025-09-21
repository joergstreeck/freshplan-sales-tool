# FC-012: Backend Implementation für Audit Trail System

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md)  
**Fokus:** Java/Quarkus Implementation Details

## 🏗️ Audit Annotations

### Custom Annotations für deklaratives Auditing

```java
// Markiert Methoden für Audit
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface Auditable {
    AuditEventType eventType();
    String entityType() default "";
    boolean includeResult() default true;
    boolean includeParameters() default true;
}

// Markiert Parameter die als Reason geloggt werden
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditReason {
}

// Markiert sensible Daten die maskiert werden sollen
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditSensitive {
    String mask() default "***";
}
```

### Usage Examples

```java
@ApplicationScoped
@Transactional
public class OpportunityService {
    
    @Auditable(eventType = AuditEventType.OPPORTUNITY_STAGE_CHANGED)
    public Opportunity changeStage(
        UUID opportunityId, 
        OpportunityStage newStage,
        @AuditReason String reason
    ) {
        Opportunity opp = opportunityRepository.findById(opportunityId);
        OpportunityStage oldStage = opp.getStage();
        
        opp.setStage(newStage);
        opp.setLastModified(Instant.now());
        
        // Audit wird automatisch durch Interceptor erstellt
        return opportunityRepository.persist(opp);
    }
    
    @Auditable(eventType = AuditEventType.OPPORTUNITY_WON)
    public Opportunity markAsWon(
        UUID opportunityId,
        @AuditReason String closeReason,
        @AuditSensitive String internalNotes
    ) {
        // Implementation
    }
}
```

## 📊 Audit Repository & Entities

### Audit Entity

```java
@Entity
@Table(name = "audit_trail")
@Immutable // Audit-Einträge dürfen nie geändert werden
public class AuditEntry {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false, updatable = false)
    private Instant timestamp;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditEventType eventType;
    
    @Column(nullable = false, length = 50)
    private String entityType;
    
    @Column(nullable = false)
    private UUID entityId;
    
    // User Information
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private String userName;
    
    @Column(nullable = false, length = 50)
    private String userRole;
    
    // Change Data as JSONB
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private AuditChangeData changeData;
    
    // Context
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "session_id")
    private UUID sessionId;
    
    // Source Information
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditSource source;
    
    @Column(name = "api_endpoint")
    private String apiEndpoint;
    
    @Column(name = "request_id")
    private UUID requestId;
    
    // Integrity
    @Column(nullable = false, length = 64)
    private String dataHash;
    
    @Column(length = 64)
    private String previousHash;
    
    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
        this.dataHash = calculateHash();
        this.previousHash = AuditHashChain.getLastHash();
    }
    
    private String calculateHash() {
        var data = String.format("%s|%s|%s|%s|%s|%s",
            timestamp, eventType, entityId, userId, 
            JsonUtils.toJson(changeData), previousHash
        );
        return DigestUtils.sha256Hex(data);
    }
}
```

### Change Data Structure

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditChangeData {
    private Object oldValue;
    private Object newValue;
    private String changeReason;
    private String userComment;
    private Map<String, Object> metadata;
    
    // Getters/Setters with proper JSON serialization
}
```

### Repository with Query Methods

```java
@ApplicationScoped
public class AuditRepository implements PanacheRepositoryBase<AuditEntry, UUID> {
    
    public List<AuditEntry> findByEntity(String entityType, UUID entityId) {
        return find("entityType = ?1 and entityId = ?2 order by timestamp desc",
                   entityType, entityId).list();
    }
    
    public List<AuditEntry> findByUser(UUID userId, LocalDateTime from, LocalDateTime to) {
        return find("userId = ?1 and timestamp between ?2 and ?3 order by timestamp desc",
                   userId, from, to).list();
    }
    
    public Stream<AuditEntry> streamByFilters(AuditFilterCriteria criteria) {
        var query = new StringBuilder("1=1");
        var params = new HashMap<String, Object>();
        
        if (criteria.getEventTypes() != null) {
            query.append(" and eventType in :eventTypes");
            params.put("eventTypes", criteria.getEventTypes());
        }
        
        if (criteria.getFrom() != null) {
            query.append(" and timestamp >= :from");
            params.put("from", criteria.getFrom());
        }
        
        // ... more filters
        
        return find(query.toString(), params).stream();
    }
    
    // Spezielle Methode für Compliance Reports
    public ComplianceReport generateComplianceReport(
        LocalDate from, 
        LocalDate to,
        List<AuditEventType> relevantEvents
    ) {
        var entries = find(
            "timestamp between ?1 and ?2 and eventType in ?3",
            from.atStartOfDay(), to.plusDays(1).atStartOfDay(), relevantEvents
        ).list();
        
        return ComplianceReport.builder()
            .period(from, to)
            .totalEvents(entries.size())
            .eventsByType(groupByEventType(entries))
            .userActivity(groupByUser(entries))
            .criticalChanges(filterCritical(entries))
            .build();
    }
}
```

## 🔒 Security & Integrity

### Hash Chain Implementation

```java
@Singleton
@Startup
public class AuditHashChain {
    
    private static final AtomicReference<String> lastHash = 
        new AtomicReference<>("GENESIS");
    
    @Inject
    AuditRepository auditRepository;
    
    @PostConstruct
    void init() {
        // Load last hash from database on startup
        auditRepository.findLastEntry()
            .map(AuditEntry::getDataHash)
            .ifPresent(lastHash::set);
    }
    
    public static String getLastHash() {
        return lastHash.get();
    }
    
    public static void updateLastHash(String newHash) {
        lastHash.set(newHash);
    }
    
    // Verify integrity of audit chain
    public ValidationResult verifyIntegrity(LocalDate from, LocalDate to) {
        var entries = auditRepository.streamByDateRange(from, to)
            .collect(Collectors.toList());
            
        String previousHash = "GENESIS";
        var errors = new ArrayList<IntegrityError>();
        
        for (var entry : entries) {
            // Verify hash
            var calculatedHash = recalculateHash(entry, previousHash);
            if (!calculatedHash.equals(entry.getDataHash())) {
                errors.add(new IntegrityError(entry.getId(), "Hash mismatch"));
            }
            
            // Verify chain
            if (!entry.getPreviousHash().equals(previousHash)) {
                errors.add(new IntegrityError(entry.getId(), "Chain broken"));
            }
            
            previousHash = entry.getDataHash();
        }
        
        return new ValidationResult(entries.size(), errors);
    }
}
```

### Request Context Capture

```java
@RequestScoped
public class AuditContext {
    
    @Context
    HttpServletRequest request;
    
    @Inject
    SecurityIdentity identity;
    
    private UUID sessionId = UUID.randomUUID();
    private UUID requestId = UUID.randomUUID();
    
    public AuditContextData capture() {
        return AuditContextData.builder()
            .userId(identity.getPrincipal().getName())
            .userRole(identity.getRoles().stream().findFirst().orElse("unknown"))
            .ipAddress(getClientIp())
            .userAgent(request.getHeader("User-Agent"))
            .sessionId(sessionId)
            .requestId(requestId)
            .source(determineSource())
            .apiEndpoint(request.getRequestURI())
            .build();
    }
    
    private String getClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private AuditSource determineSource() {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/")) {
            return AuditSource.API;
        } else if (uri.startsWith("/webhook/")) {
            return AuditSource.WEBHOOK;
        }
        return AuditSource.UI;
    }
}
```

## 🚀 Performance Optimizations

### Async Audit Logging

```java
@ApplicationScoped
public class AsyncAuditService {
    
    @Inject
    ManagedExecutor executor;
    
    @Inject
    AuditService auditService;
    
    @Inject
    @Channel("audit-events")
    Emitter<AuditEvent> auditEmitter;
    
    public CompletableFuture<Void> logAsync(AuditEvent event) {
        return CompletableFuture
            .runAsync(() -> {
                try {
                    // Send to Kafka for async processing
                    auditEmitter.send(event);
                } catch (Exception e) {
                    // Fallback to direct DB write
                    Log.warn("Kafka unavailable, falling back to sync audit", e);
                    auditService.logEvent(event);
                }
            }, executor)
            .exceptionally(throwable -> {
                Log.error("Failed to log audit event", throwable);
                // Could implement retry logic here
                return null;
            });
    }
}
```

### Batch Insert for High Volume

```java
@ApplicationScoped
public class AuditBatchProcessor {
    
    private final BlockingQueue<AuditEntry> queue = 
        new LinkedBlockingQueue<>(10000);
    
    @Inject
    AuditRepository repository;
    
    @Scheduled(every = "5s")
    void processBatch() {
        var batch = new ArrayList<AuditEntry>(1000);
        queue.drainTo(batch, 1000);
        
        if (!batch.isEmpty()) {
            try {
                repository.persist(batch);
                Log.debugf("Persisted %d audit entries", batch.size());
            } catch (Exception e) {
                Log.error("Failed to persist audit batch", e);
                // Re-queue failed entries
                batch.forEach(queue::offer);
            }
        }
    }
}
```

## 📈 Monitoring & Alerts

```java
@ApplicationScoped
public class AuditMonitoring {
    
    @Inject
    MeterRegistry meterRegistry;
    
    @Inject
    AuditRepository repository;
    
    @PostConstruct
    void init() {
        // Track audit events per type
        for (AuditEventType type : AuditEventType.values()) {
            meterRegistry.counter("audit.events", "type", type.name());
        }
        
        // Track performance
        meterRegistry.timer("audit.write.duration");
        meterRegistry.gauge("audit.queue.size", queue, Queue::size);
    }
    
    @Scheduled(every = "1h")
    void checkAnomalies() {
        // Check for suspicious patterns
        var lastHour = Instant.now().minus(1, ChronoUnit.HOURS);
        
        // Too many failed logins
        var failedLogins = repository.countByTypeAndTime(
            AuditEventType.LOGIN_FAILED, lastHour
        );
        if (failedLogins > 100) {
            alertService.send("High number of failed logins: " + failedLogins);
        }
        
        // Bulk operations
        var bulkOps = repository.countByTypeAndTime(
            AuditEventType.BULK_ACTION_EXECUTED, lastHour
        );
        if (bulkOps > 10) {
            alertService.send("Unusual bulk operation activity: " + bulkOps);
        }
    }
}
```