# FC-004 Verkäuferschutz - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** 4-stufiger Kundenschutz mit automatischer Provisions-Sicherung  
**Stack:** Quarkus + PostgreSQL + Event-Driven + React  
**Status:** 🟡 READY TO START - Dependencies vorhanden  
**Dependencies:** FC-008 Security (✅) | FC-009 Permissions (✅) | M4 Pipeline (✅)  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [💰 Commission](#-commission-split) | [🎨 UI](#-ui-components)

**Core Purpose in 1 Line:** `Aktivität → Schutz-Level → Automatische Eskalation → Faire Provisions-Splits`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Protection Domain Model in 5 Minuten

```java
// 1. CustomerProtection.java - Copy-paste ready
@Entity
@Table(name = "customer_protections")
public class CustomerProtection {
    
    @Id @GeneratedValue
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "sales_rep_id", nullable = false)
    private User salesRep;
    
    @Enumerated(EnumType.STRING)
    private ProtectionLevel level = ProtectionLevel.OPEN;
    
    @Column(name = "valid_until")
    private LocalDateTime validUntil;
    
    // The 4 Protection Levels
    public enum ProtectionLevel {
        OPEN(0, null),                           // Frei verfügbar
        FIRST_CONTACT(1, Duration.ofDays(7)),    // 7 Tage Schutz
        IN_NEGOTIATION(2, Duration.ofDays(14)),  // 14 Tage Schutz
        OFFER_CREATED(3, Duration.ofDays(30)),   // 30 Tage Schutz
        DEAL_WON(4, null);                       // Permanent
        
        private final int priority;
        private final Duration defaultDuration;
        
        ProtectionLevel(int priority, Duration defaultDuration) {
            this.priority = priority;
            this.defaultDuration = defaultDuration;
        }
        
        public boolean canEscalateTo(ProtectionLevel target) {
            return target.priority > this.priority;
        }
        
        public LocalDateTime calculateExpiry() {
            return defaultDuration != null 
                ? LocalDateTime.now().plus(defaultDuration)
                : null; // Permanent
        }
    }
    
    // Business Logic
    public boolean isValid() {
        if (level == ProtectionLevel.DEAL_WON) return true;
        return validUntil != null && validUntil.isAfter(LocalDateTime.now());
    }
    
    public void escalateTo(ProtectionLevel newLevel) {
        if (!this.level.canEscalateTo(newLevel)) {
            throw new IllegalStateException(
                "Cannot escalate from " + level + " to " + newLevel
            );
        }
        this.level = newLevel;
        this.validUntil = newLevel.calculateExpiry();
    }
}

// 2. Quick Database Migration
-- V8.0__create_protection_tables.sql
CREATE TABLE customer_protections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    sales_rep_id UUID NOT NULL REFERENCES users(id),
    level VARCHAR(20) NOT NULL,
    valid_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(customer_id), -- Only one active protection per customer
    INDEX idx_protection_expiry (valid_until) WHERE valid_until IS NOT NULL
);
```

### Recipe 2: Protection Service with Auto-Escalation

```java
// CustomerProtectionService.java - Core Business Logic
@ApplicationScoped
@Transactional
public class CustomerProtectionService {
    
    @Inject CustomerProtectionRepository protectionRepo;
    @Inject Event<ProtectionChangedEvent> protectionEvents;
    @Inject SecurityIdentity identity;
    
    // Main Check Method - Use this everywhere!
    public ProtectionStatus checkProtection(UUID customerId) {
        var protection = protectionRepo.findByCustomerId(customerId);
        if (protection.isEmpty() || !protection.get().isValid()) {
            return ProtectionStatus.OPEN;
        }
        
        var p = protection.get();
        var currentUserId = identity.getPrincipal().getName();
        
        if (p.getSalesRep().getId().toString().equals(currentUserId)) {
            return ProtectionStatus.owned(p.getLevel());
        }
        
        return ProtectionStatus.blocked()
            .by(p.getSalesRep().getName())
            .until(p.getValidUntil())
            .level(p.getLevel())
            .build();
    }
    
    // Auto-Escalation on Activity
    @Observes
    void onActivityCreated(@ActivityCreated ActivityCreatedEvent event) {
        var activity = event.getActivity();
        var customerId = activity.getCustomerId();
        var userId = UUID.fromString(identity.getPrincipal().getName());
        
        var status = checkProtection(customerId);
        
        if (status.isOpen()) {
            // Create new FIRST_CONTACT protection
            createProtection(customerId, userId, ProtectionLevel.FIRST_CONTACT);
        } else if (status.isOwnedBy(userId)) {
            // Auto-escalate based on activity type
            escalateIfNeeded(customerId, activity);
        }
    }
    
    private void escalateIfNeeded(UUID customerId, Activity activity) {
        var targetLevel = determineTargetLevel(activity);
        if (targetLevel != null) {
            var protection = protectionRepo.findByCustomerId(customerId).get();
            if (protection.getLevel().canEscalateTo(targetLevel)) {
                protection.escalateTo(targetLevel);
                fireProtectionEvent(protection, "ESCALATED");
            }
        }
    }
    
    private ProtectionLevel determineTargetLevel(Activity activity) {
        return switch (activity.getType()) {
            case QUALIFICATION, MEETING -> ProtectionLevel.IN_NEGOTIATION;
            case OFFER_SENT -> ProtectionLevel.OFFER_CREATED;
            case DEAL_WON -> ProtectionLevel.DEAL_WON;
            default -> null;
        };
    }
    
    // Scheduled Cleanup
    @Scheduled(every = "1h")
    void cleanupExpiredProtections() {
        var expired = protectionRepo.findExpiredProtections();
        expired.forEach(p -> {
            p.setLevel(ProtectionLevel.OPEN);
            p.setValidUntil(null);
            notifyExpiration(p);
        });
    }
}
```

### Recipe 3: Override with Audit Trail

```java
// Protection Override Feature
@Path("/api/protections/{customerId}/override")
@RolesAllowed({"admin", "team_lead"})
public class ProtectionOverrideResource {
    
    @Inject CustomerProtectionService protectionService;
    @Inject AuditService auditService;
    
    @POST
    public Response overrideProtection(
        @PathParam("customerId") UUID customerId,
        OverrideRequest request
    ) {
        // Validate permission
        if (!hasOverridePermission(request.reason)) {
            return Response.status(403)
                .entity("Team leads must provide a reason")
                .build();
        }
        
        // Get current protection
        var current = protectionService.getProtection(customerId);
        
        // Create audit entry FIRST
        auditService.log(AuditEntry.builder()
            .action("PROTECTION_OVERRIDE")
            .customerId(customerId)
            .oldValue(current.getSalesRep().getName())
            .newValue(request.newSalesRepId)
            .reason(request.reason)
            .build()
        );
        
        // Override protection
        protectionService.overrideProtection(
            customerId,
            request.newSalesRepId,
            request.reason
        );
        
        // Notify both sales reps
        notificationService.notifyProtectionOverride(
            current.getSalesRep(),
            request.newSalesRepId,
            customerId,
            request.reason
        );
        
        return Response.ok().build();
    }
    
    private boolean hasOverridePermission(String reason) {
        var user = securityIdentity.getPrincipal();
        if (user.hasRole("admin")) return true;
        if (user.hasRole("team_lead") && reason != null && !reason.isBlank()) {
            return true;
        }
        return false;
    }
}
```

---

## 💰 COMMISSION SPLIT

### Recipe 4: Fair Commission Calculation

```java
// CommissionSplit.java - Domain Model
@Entity
@Table(name = "commission_splits")
public class CommissionSplit {
    
    @Id @GeneratedValue
    private UUID id;
    
    @ManyToOne
    private Customer customer;
    
    @ManyToOne
    private Payment payment;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "split", cascade = CascadeType.ALL)
    private List<CommissionAllocation> allocations = new ArrayList<>();
    
    // The Fair Split: 30/20/30/20
    public enum ContributionType {
        FIRST_CONTACT(30),    // Wer hat den Kunden gefunden?
        QUALIFICATION(20),    // Wer hat qualifiziert?
        OFFER_CREATION(30),   // Wer hat das Angebot erstellt?
        DEAL_CLOSING(20);     // Wer hat abgeschlossen?
        
        private final int percentage;
        
        ContributionType(int percentage) {
            this.percentage = percentage;
        }
    }
}

// CommissionCalculationService.java
@ApplicationScoped
public class CommissionCalculationService {
    
    @Inject ActivityRepository activityRepo;
    @Inject OpportunityRepository dealRepo;
    
    public CommissionSplit calculateSplit(Payment payment) {
        var customer = payment.getCustomer();
        var contributions = analyzeContributions(customer);
        
        var split = new CommissionSplit();
        split.setPayment(payment);
        split.setTotalAmount(payment.getAmount().multiply(COMMISSION_RATE));
        
        // Create allocations based on who did what
        contributions.forEach((type, userId) -> {
            var allocation = new CommissionAllocation();
            allocation.setUserId(userId);
            allocation.setContributionType(type);
            allocation.setPercentage(type.percentage);
            allocation.setAmount(calculateAmount(split.getTotalAmount(), type));
            split.addAllocation(allocation);
        });
        
        return split;
    }
    
    private Map<ContributionType, UUID> analyzeContributions(Customer customer) {
        var contributions = new HashMap<ContributionType, UUID>();
        
        // First Contact
        activityRepo.findFirstByCustomer(customer)
            .ifPresent(a -> contributions.put(FIRST_CONTACT, a.getCreatedBy()));
        
        // Qualification
        activityRepo.findByTypeAndCustomer(QUALIFICATION, customer)
            .stream().findFirst()
            .ifPresent(a -> contributions.put(QUALIFICATION, a.getCreatedBy()));
        
        // Offer Creation
        dealRepo.findByCustomer(customer)
            .stream()
            .filter(d -> d.getOffer() != null)
            .findFirst()
            .ifPresent(d -> contributions.put(OFFER_CREATION, d.getOffer().getCreatedBy()));
        
        // Deal Closing
        dealRepo.findWonByCustomer(customer)
            .ifPresent(d -> contributions.put(DEAL_CLOSING, d.getOwner()));
        
        return contributions;
    }
}
```

---

## 🎨 UI COMPONENTS

### Recipe 5: Protection Status Badge

```typescript
// ProtectionStatus.tsx - Drop into Customer Card
export const ProtectionStatus: React.FC<{customerId: string}> = ({customerId}) => {
    const { data: status } = useProtectionStatus(customerId);
    const { user } = useAuth();
    const [overrideOpen, setOverrideOpen] = useState(false);
    
    if (!status) return null;
    
    const getStatusDisplay = () => {
        switch (status.status) {
            case 'OWNED':
                return {
                    icon: <LockIcon />,
                    color: 'success' as const,
                    label: `Mein Kunde (${status.level})`,
                    variant: 'filled' as const
                };
            case 'BLOCKED':
                return {
                    icon: <BlockIcon />,
                    color: 'error' as const,
                    label: `${status.blockedBy} bis ${formatDate(status.validUntil)}`,
                    variant: 'outlined' as const
                };
            case 'OPEN':
                return {
                    icon: <LockOpenIcon />,
                    color: 'default' as const,
                    label: 'Frei verfügbar',
                    variant: 'outlined' as const
                };
        }
    };
    
    const display = getStatusDisplay();
    const canOverride = user?.roles.some(r => ['admin', 'team_lead'].includes(r));
    
    return (
        <>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Chip
                    icon={display.icon}
                    label={display.label}
                    color={display.color}
                    size="small"
                    variant={display.variant}
                />
                
                {status.status === 'BLOCKED' && canOverride && (
                    <IconButton 
                        size="small" 
                        onClick={() => setOverrideOpen(true)}
                        title="Schutz überschreiben"
                    >
                        <AdminPanelSettingsIcon fontSize="small" />
                    </IconButton>
                )}
            </Box>
            
            <OverrideDialog
                open={overrideOpen}
                onClose={() => setOverrideOpen(false)}
                customerId={customerId}
                currentProtection={status}
            />
        </>
    );
};

// useProtectionStatus hook
export const useProtectionStatus = (customerId: string) => {
    return useQuery({
        queryKey: ['protection', customerId],
        queryFn: () => apiClient.get(`/api/protections/${customerId}/status`)
            .then(res => res.data),
        staleTime: 30 * 1000 // 30 seconds
    });
};
```

### Recipe 6: Commission Split Dashboard

```typescript
// CommissionDashboard.tsx - My Earnings Overview
export const CommissionDashboard: React.FC = () => {
    const { data: splits } = useMyCommissionSplits();
    const [selectedMonth, setSelectedMonth] = useState(new Date());
    
    const monthlyTotal = splits
        ?.filter(s => isSameMonth(s.payment.date, selectedMonth))
        ?.reduce((sum, s) => sum + s.myAmount, 0) || 0;
    
    return (
        <Grid container spacing={3}>
            {/* Monthly Summary */}
            <Grid item xs={12}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6">Meine Provisionen</Typography>
                    <Typography variant="h3" color="primary">
                        {formatCurrency(monthlyTotal)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                        {format(selectedMonth, 'MMMM yyyy', { locale: de })}
                    </Typography>
                </Paper>
            </Grid>
            
            {/* Split Details */}
            <Grid item xs={12}>
                <Paper>
                    <List>
                        {splits?.map(split => (
                            <ListItem key={split.id}>
                                <ListItemText
                                    primary={split.customer.name}
                                    secondary={
                                        <Box>
                                            <Typography variant="caption">
                                                {split.myContribution}: {split.myPercentage}%
                                            </Typography>
                                            <LinearProgress
                                                variant="determinate"
                                                value={split.myPercentage}
                                                sx={{ mt: 0.5 }}
                                            />
                                        </Box>
                                    }
                                />
                                <ListItemSecondaryAction>
                                    <Typography variant="h6" color="success.main">
                                        +{formatCurrency(split.myAmount)}
                                    </Typography>
                                </ListItemSecondaryAction>
                            </ListItem>
                        ))}
                    </List>
                </Paper>
            </Grid>
        </Grid>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Protection Escalation Test
```java
@Test
void testAutomaticEscalation() {
    // Create customer with FIRST_CONTACT
    var customerId = createTestCustomer();
    protectionService.createProtection(customerId, userId, FIRST_CONTACT);
    
    // Create qualification activity
    var activity = Activity.builder()
        .customerId(customerId)
        .type(ActivityType.QUALIFICATION)
        .build();
    
    activityService.create(activity);
    
    // Verify escalation to IN_NEGOTIATION
    var protection = protectionService.getProtection(customerId);
    assertThat(protection.getLevel()).isEqualTo(IN_NEGOTIATION);
    assertThat(protection.getValidUntil()).isAfter(LocalDateTime.now().plusDays(13));
}
```

### Pattern 2: Commission Split Test
```java
@Test
void testFairCommissionSplit() {
    // Setup: Different users contribute
    var customer = createCustomer();
    createActivity(customer, user1, FIRST_CONTACT);
    createActivity(customer, user2, QUALIFICATION);
    createOffer(customer, user3);
    createWonDeal(customer, user4);
    
    // Calculate split
    var payment = createPayment(customer, 10000.00);
    var split = commissionService.calculateSplit(payment);
    
    // Verify fair distribution
    assertThat(split.getAllocations()).hasSize(4);
    assertThat(split.getAllocations())
        .extracting("userId", "percentage", "amount")
        .containsExactlyInAnyOrder(
            tuple(user1, 30, 300.00), // First Contact
            tuple(user2, 20, 200.00), // Qualification
            tuple(user3, 30, 300.00), // Offer
            tuple(user4, 20, 200.00)  // Closing
        );
}
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🔧 Protection Rules Configuration</summary>

### Admin Settings UI
```typescript
// ProtectionRulesConfig.tsx
export const ProtectionRulesConfig = () => {
    const levels = [
        { name: 'FIRST_CONTACT', duration: 7, editable: true },
        { name: 'IN_NEGOTIATION', duration: 14, editable: true },
        { name: 'OFFER_CREATED', duration: 30, editable: true },
        { name: 'DEAL_WON', duration: null, editable: false }
    ];
    
    return (
        <Table>
            <TableHead>
                <TableRow>
                    <TableCell>Schutz-Stufe</TableCell>
                    <TableCell>Dauer (Tage)</TableCell>
                    <TableCell>Aktionen</TableCell>
                </TableRow>
            </TableHead>
            <TableBody>
                {levels.map(level => (
                    <TableRow key={level.name}>
                        <TableCell>{level.name}</TableCell>
                        <TableCell>
                            {level.editable ? (
                                <TextField
                                    type="number"
                                    defaultValue={level.duration}
                                    size="small"
                                />
                            ) : (
                                'Permanent'
                            )}
                        </TableCell>
                        <TableCell>
                            <Button size="small">Speichern</Button>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};
```

</details>

<details>
<summary>📊 Analytics & Reports</summary>

### Protection Analytics Queries
```sql
-- Most productive sales reps
SELECT 
    u.name,
    COUNT(DISTINCT cp.customer_id) as protected_customers,
    COUNT(DISTINCT CASE WHEN cp.level = 'DEAL_WON' THEN cp.customer_id END) as won_deals,
    SUM(cs.amount) as total_commission
FROM customer_protections cp
JOIN users u ON cp.sales_rep_id = u.id
LEFT JOIN commission_splits cs ON cs.user_id = u.id
WHERE cp.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY u.id, u.name
ORDER BY won_deals DESC;

-- Override patterns (for abuse detection)
SELECT 
    overridden_by,
    COUNT(*) as override_count,
    AVG(EXTRACT(epoch FROM (override_at - original_created_at))/3600) as avg_hours_before_override
FROM protection_audit_log
WHERE action = 'OVERRIDE'
AND created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY overridden_by
HAVING COUNT(*) > 5
ORDER BY override_count DESC;
```

</details>

---

**🎯 Nächster Schritt:** Protection Entity implementieren und erste Tests schreiben