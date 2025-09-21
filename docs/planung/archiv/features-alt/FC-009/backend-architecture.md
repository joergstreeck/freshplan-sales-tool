# FC-009: Backend-Architektur für Contract Renewal

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-009-contract-renewal-management.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-009-contract-renewal-management.md)  
**Fokus:** Technische Backend-Implementierung

## Contract Monitoring Entity

```java
@Entity
@Table(name = "contract_monitoring")
public class ContractMonitoring {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(nullable = false)
    private LocalDate contractStart;
    
    @Column(nullable = false)
    private LocalDate contractEnd;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;
    
    // Preisindex-Tracking
    @Column(precision = 10, scale = 2)
    private BigDecimal baselineIndex;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal currentIndex;
    
    private LocalDate lastIndexCheck;
    
    // Xentral-Sync
    @Column(name = "xentral_customer_id")
    private String xentralCustomerId;
    
    @Enumerated(EnumType.STRING)
    private SyncStatus xentralSyncStatus;
    
    private LocalDateTime lastSyncAttempt;
    
    // Renewal-Tracking
    private LocalDate renewalStartedAt;
    private String renewalInitiatedBy;
    private Integer daysUntilExpiry;
    
    // Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        daysUntilExpiry = calculateDaysUntilExpiry();
    }
}
```

## State Machine

```java
public enum ContractStatus {
    ACTIVE("Aktiv"),
    EXPIRING_SOON("Läuft bald ab"),
    IN_RENEWAL("In Verlängerung"),
    RENEWED("Erneuert"),
    EXPIRED("Abgelaufen"),
    LAPSED_RENEWED("Rückwirkend erneuert");
    
    private final String displayName;
    
    ContractStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public boolean canTransitionTo(ContractStatus newStatus) {
        return switch (this) {
            case ACTIVE -> newStatus == EXPIRING_SOON;
            case EXPIRING_SOON -> newStatus == IN_RENEWAL || newStatus == EXPIRED;
            case IN_RENEWAL -> newStatus == RENEWED || newStatus == EXPIRED;
            case EXPIRED -> newStatus == LAPSED_RENEWED;
            case RENEWED, LAPSED_RENEWED -> false;
        };
    }
}
```

## Scheduled Jobs

```java
@ApplicationScoped
public class ContractRenewalScheduler {
    
    @Inject
    ContractMonitoringRepository contractRepo;
    
    @Inject
    OpportunityService opportunityService;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    XentralApiClient xentralClient;
    
    @Scheduled(every = "24h", identity = "contract-expiry-check")
    void checkExpiringContracts() {
        Log.info("Starting daily contract expiry check");
        
        // 90 Tage vorher → EXPIRING_SOON + Move to RENEWAL
        var expiring90 = contractRepo.findByDaysUntilExpiry(90);
        expiring90.forEach(contract -> {
            contract.setStatus(ContractStatus.EXPIRING_SOON);
            opportunityService.moveToRenewalStage(contract.getCustomer().getId());
            notificationService.sendRenewalReminder(contract, 90);
        });
        
        // 60 Tage → Eskalation an Manager
        var expiring60 = contractRepo.findByDaysUntilExpiry(60);
        expiring60.stream()
            .filter(c -> c.getStatus() == ContractStatus.EXPIRING_SOON)
            .forEach(c -> escalateToManager(c));
        
        // 30 Tage → Eskalation an Geschäftsführung
        var expiring30 = contractRepo.findByDaysUntilExpiry(30);
        expiring30.stream()
            .filter(c -> c.getStatus() == ContractStatus.EXPIRING_SOON)
            .forEach(c -> escalateToExecutive(c));
    }
    
    @Scheduled(every = "1h", identity = "contract-deactivation")
    @Transactional
    void deactivateExpiredContracts() {
        var expired = contractRepo.findNewlyExpired();
        
        expired.forEach(contract -> {
            try {
                // 1. Status update
                contract.setStatus(ContractStatus.EXPIRED);
                
                // 2. Xentral sync
                xentralClient.deactivateCustomerDiscount(
                    contract.getXentralCustomerId(),
                    "contract_expired"
                );
                
                // 3. Event publizieren
                Event.fire(new ContractExpiredEvent(contract));
                
                // 4. Notification
                notificationService.notifyContractExpired(contract);
                
                Log.infof("Contract %s expired and processed", contract.getId());
            } catch (Exception e) {
                Log.errorf(e, "Failed to process expired contract %s", contract.getId());
            }
        });
    }
}
```

## Repository

```java
@ApplicationScoped
public class ContractMonitoringRepository 
    implements PanacheRepositoryBase<ContractMonitoring, UUID> {
    
    public List<ContractMonitoring> findByDaysUntilExpiry(int days) {
        return find("""
            FROM ContractMonitoring c
            WHERE c.contractEnd = :targetDate
            AND c.status NOT IN ('EXPIRED', 'RENEWED', 'LAPSED_RENEWED')
            """,
            Parameters.with("targetDate", LocalDate.now().plusDays(days))
        ).list();
    }
    
    public List<ContractMonitoring> findNewlyExpired() {
        return find("""
            FROM ContractMonitoring c
            WHERE c.contractEnd < :today
            AND c.status NOT IN ('EXPIRED', 'RENEWED', 'LAPSED_RENEWED')
            """,
            Parameters.with("today", LocalDate.now())
        ).list();
    }
    
    public Optional<ContractMonitoring> findByCustomerId(UUID customerId) {
        return find("customer.id", customerId).firstResultOptional();
    }
}
```

## Service Layer

```java
@ApplicationScoped
@Transactional
public class ContractRenewalService {
    
    @Inject
    ContractMonitoringRepository repository;
    
    @Inject
    Event<ContractRenewalStartedEvent> renewalStartedEvent;
    
    public ContractMonitoring startRenewal(UUID customerId, String initiatedBy) {
        var contract = repository.findByCustomerId(customerId)
            .orElseThrow(() -> new ContractNotFoundException(customerId));
            
        if (!contract.getStatus().canTransitionTo(ContractStatus.IN_RENEWAL)) {
            throw new IllegalStateException(
                "Cannot start renewal from status: " + contract.getStatus()
            );
        }
        
        contract.setStatus(ContractStatus.IN_RENEWAL);
        contract.setRenewalStartedAt(LocalDate.now());
        contract.setRenewalInitiatedBy(initiatedBy);
        
        renewalStartedEvent.fire(new ContractRenewalStartedEvent(contract));
        
        return repository.persist(contract);
    }
    
    public ContractMonitoring completeLapsedRenewal(
        UUID customerId, 
        LocalDate newStartDate,
        LocalDate newEndDate
    ) {
        var contract = repository.findByCustomerId(customerId)
            .orElseThrow(() -> new ContractNotFoundException(customerId));
            
        if (contract.getStatus() != ContractStatus.EXPIRED) {
            throw new IllegalStateException("Only expired contracts can be lapsed renewed");
        }
        
        // Neue Vertragsdaten mit Lücke
        contract.setStatus(ContractStatus.LAPSED_RENEWED);
        contract.setContractStart(newStartDate);
        contract.setContractEnd(newEndDate);
        
        // Event für Xentral
        Event.fire(new ContractLapsedRenewalEvent(
            contract,
            contract.getContractEnd().plusDays(1), // Gap start
            newStartDate.minusDays(1)             // Gap end
        ));
        
        return repository.persist(contract);
    }
}
```

## 🚀 Strategische Implementierungsschritte

### Phase 1: Contract-Entity Foundation
1. **Contract-Entity als zentrales Domain-Objekt**
   - Verknüpfung Customer ↔ Contract ↔ Opportunity
   - Vollständige Vertragshistorie (Event Sourcing Pattern)
   - Integration mit FreshPlan-Partnerschaftsvereinbarungen PDF-Speicher

2. **Event-Driven Architecture für Renewals**
   ```java
   // Domain Events
   public sealed interface ContractEvent {
       record ContractCreated(UUID contractId, UUID customerId, LocalDate endDate) implements ContractEvent {}
       record ContractExpiring(UUID contractId, int daysUntilExpiry) implements ContractEvent {}
       record ContractRenewed(UUID contractId, UUID newOpportunityId) implements ContractEvent {}
       record ContractTerminated(UUID contractId, String reason) implements ContractEvent {}
   }
   
   // Event Publisher für automatische Trigger
   @ApplicationScoped
   public class ContractEventPublisher {
       void publishContractExpiring(Contract contract) {
           // 90 Tage vor Ablauf
           // 60 Tage vor Ablauf (Eskalation)
           // 30 Tage vor Ablauf (kritisch)
       }
   }
   ```

3. **Performance-Optimierung für Batch-Operations**
   - Bulk-Creation von RENEWAL Opportunities
   - Optimierte Queries für Kanban-Board mit vielen Renewals
   - Asynchrone Xentral-Synchronisation in Batches

4. **Test-Infrastructure mit Builder Pattern**
   ```java
   // Test Data Builder
   Contract testContract = ContractTestBuilder.aContract()
       .withCustomer(customerBuilder.build())
       .withStartDate(LocalDate.now().minusMonths(9))
       .withEndDate(LocalDate.now().plusMonths(3))
       .withStatus(ContractStatus.ACTIVE)
       .withPartnershipAgreement()
       .build();
   ```

### Integration mit FreshPlan-Partnerschaftsvereinbarungen
- Automatische PDF-Archivierung bei Contract-Creation
- Verknüpfung Contract ↔ Vereinbarungs-PDF
- Audit-Trail für alle Vertragsänderungen
- Integration in Contract-Timeline Events

## 🔗 Verwandte Dokumente

- **Frontend-Integration:** [./frontend-components.md](./frontend-components.md)
- **Xentral Events:** [./xentral-integration.md](./xentral-integration.md)
- **Business Logic:** [./business-workflows.md](./business-workflows.md)