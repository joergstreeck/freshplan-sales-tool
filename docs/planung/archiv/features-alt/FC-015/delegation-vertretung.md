# FC-015: Delegation & Vertretungsregelungen

**Zurück:** [FC-015 Hauptkonzept](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-015-rights-roles-concept.md)  
**Vorheriges:** [Permission System](./permission-system.md)

## 📋 Übersicht

Flexible Vertretungsregelungen für Urlaub, Krankheit und temporäre Zuständigkeiten.

## 🏗️ Datenmodell

### Delegation Entity
```java
@Entity
@Table(name = "delegations")
public class Delegation extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "from_user_id")
    public User fromUser; // Wer delegiert
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "to_user_id")
    public User toUser; // An wen delegiert wird
    
    @Enumerated(EnumType.STRING)
    public DelegationType type; // FULL, PARTIAL, SPECIFIC
    
    @ElementCollection
    @CollectionTable(name = "delegation_permissions")
    public Set<String> permissions; // Bei PARTIAL/SPECIFIC
    
    @ElementCollection
    @CollectionTable(name = "delegation_resources")
    public Set<DelegationResource> resources; // Spezifische Ressourcen
    
    @Column(nullable = false)
    public LocalDateTime validFrom;
    
    @Column(nullable = false)
    public LocalDateTime validUntil;
    
    @Column(length = 500)
    public String reason; // "Urlaub", "Krankheit", etc.
    
    @Enumerated(EnumType.STRING)
    public DelegationStatus status; // PLANNED, ACTIVE, EXPIRED, CANCELLED
    
    public boolean requiresApproval;
    
    @ManyToOne
    public User approvedBy;
    
    public LocalDateTime approvedAt;
}
```

### Delegation Service
```java
@ApplicationScoped
@Transactional
public class DelegationService {
    @Inject
    DelegationRepository delegationRepo;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    @Channel("delegation-events")
    Emitter<DelegationEvent> eventEmitter;
    
    @Inject
    CurrentUserProducer currentUser;
    
    public Delegation createDelegation(CreateDelegationRequest request) {
        // Validierung
        validateDelegation(request);
        
        var delegation = new Delegation();
        delegation.fromUser = currentUser.get();
        delegation.toUser = User.findById(request.toUserId);
        delegation.type = request.type;
        delegation.permissions = request.permissions;
        delegation.validFrom = request.validFrom;
        delegation.validUntil = request.validUntil;
        delegation.reason = request.reason;
        delegation.status = DelegationStatus.PLANNED;
        
        // Braucht Genehmigung?
        if (requiresApproval(request)) {
            delegation.requiresApproval = true;
            notifyApprovers(delegation);
        } else {
            delegation.status = DelegationStatus.ACTIVE;
            activateDelegation(delegation);
        }
        
        delegationRepo.persist(delegation);
        
        eventEmitter.send(new DelegationCreatedEvent(delegation));
        
        return delegation;
    }
    
    @Scheduled(every = "5m")
    void processDelegations() {
        // Aktiviere geplante Delegationen
        var toActivate = delegationRepo.findPlannedToActivate(LocalDateTime.now());
        toActivate.forEach(this::activateDelegation);
        
        // Deaktiviere abgelaufene
        var toExpire = delegationRepo.findActiveToExpire(LocalDateTime.now());
        toExpire.forEach(this::expireDelegation);
    }
    
    private void activateDelegation(Delegation delegation) {
        delegation.status = DelegationStatus.ACTIVE;
        
        // Benachrichtige beide Parteien
        notificationService.notify(delegation.fromUser, 
            "Ihre Vertretung durch " + delegation.toUser.name + " ist aktiv");
        notificationService.notify(delegation.toUser,
            "Sie vertreten ab sofort " + delegation.fromUser.name);
            
        // Clear Permission Caches
        permissionCache.invalidate(delegation.fromUser.id);
        permissionCache.invalidate(delegation.toUser.id);
        
        eventEmitter.send(new DelegationActivatedEvent(delegation));
    }
}
```

## 🔐 Permission Integration

### Erweiterte Permission Checks
```java
public boolean hasPermissionWithDelegation(UUID userId, String permission) {
    // 1. Eigene Permissions
    if (hasOwnPermission(userId, permission)) return true;
    
    // 2. Aktive Delegationen AN diese Person
    var delegations = delegationRepo.findActiveDelegationsTo(userId, LocalDateTime.now());
    
    for (Delegation d : delegations) {
        switch (d.type) {
            case FULL:
                // Alle Rechte des Delegierenden
                if (hasOwnPermission(d.fromUser.id, permission)) return true;
                break;
                
            case PARTIAL:
                // Nur spezifische Permissions
                if (d.permissions.contains(permission)) return true;
                break;
                
            case SPECIFIC:
                // Permissions + Resource Check
                if (d.permissions.contains(permission) && 
                    matchesResource(d.resources, currentResource)) return true;
                break;
        }
    }
    
    return false;
}
```

## 🎨 Frontend-Komponenten

### Delegation Management
```typescript
interface DelegationFormProps {
  onSubmit: (delegation: CreateDelegationRequest) => void;
  currentUser: User;
}

export const DelegationForm: React.FC<DelegationFormProps> = ({ onSubmit, currentUser }) => {
  const [formData, setFormData] = useState<CreateDelegationRequest>({
    type: 'FULL',
    validFrom: dayjs().add(1, 'day').startOf('day'),
    validUntil: dayjs().add(7, 'day').endOf('day'),
    permissions: [],
    resources: []
  });

  const { data: users } = useQuery(['users'], fetchUsers);
  const { data: myPermissions } = useQuery(['permissions', currentUser.id], 
    () => fetchUserPermissions(currentUser.id));

  return (
    <Box component="form" onSubmit={handleSubmit}>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <UserAutocomplete
            label="Vertretung durch"
            value={formData.toUserId}
            onChange={(userId) => setFormData({...formData, toUserId: userId})}
            exclude={[currentUser.id]}
          />
        </Grid>
        
        <Grid item xs={12}>
          <DelegationTypeSelector
            value={formData.type}
            onChange={(type) => handleTypeChange(type)}
          />
        </Grid>
        
        {formData.type !== 'FULL' && (
          <Grid item xs={12}>
            <PermissionSelector
              selected={formData.permissions}
              available={myPermissions}
              onChange={(perms) => setFormData({...formData, permissions: perms})}
            />
          </Grid>
        )}
        
        <Grid item xs={12} md={6}>
          <DateTimePicker
            label="Gültig von"
            value={formData.validFrom}
            onChange={(date) => setFormData({...formData, validFrom: date})}
            minDateTime={dayjs()}
          />
        </Grid>
        
        <Grid item xs={12} md={6}>
          <DateTimePicker
            label="Gültig bis"
            value={formData.validUntil}
            onChange={(date) => setFormData({...formData, validUntil: date})}
            minDateTime={formData.validFrom}
          />
        </Grid>
      </Grid>
    </Box>
  );
};
```

### Delegation Status Widget
```typescript
export const DelegationStatusWidget: React.FC = () => {
  const { data: activeDelegations } = useQuery(
    ['delegations', 'active'],
    fetchActiveDelegations,
    { refetchInterval: 60000 } // Refresh every minute
  );

  if (!activeDelegations?.length) return null;

  return (
    <Paper sx={{ p: 2, mb: 2, bgcolor: 'warning.light' }}>
      <Typography variant="subtitle2" gutterBottom>
        Aktive Vertretungen
      </Typography>
      {activeDelegations.map(d => (
        <Chip
          key={d.id}
          label={`Vertretung für ${d.fromUser.name}`}
          onDelete={() => handleEndDelegation(d.id)}
          sx={{ m: 0.5 }}
        />
      ))}
    </Paper>
  );
};
```

## 🔄 Automatisierungen

### Outlook-Integration
```java
@ApplicationScoped
public class OutlookDelegationSync {
    @Inject
    @ConfigProperty(name = "outlook.sync.enabled", defaultValue = "false")
    boolean outlookSyncEnabled;
    
    @Inject
    OutlookClient outlookClient;
    
    public void syncFromOutlook(User user) {
        if (!outlookSyncEnabled) return;
        
        var oooStatus = outlookClient.getOutOfOfficeStatus(user.email);
        if (oooStatus.isEnabled()) {
            createAutomaticDelegation(user, oooStatus);
        }
    }
    
    private void createAutomaticDelegation(User user, OutOfOfficeStatus ooo) {
        // Parse Auto-Reply für Vertretung
        var delegate = parseDelegate(ooo.autoReplyMessage);
        if (delegate != null) {
            var request = new CreateDelegationRequest();
            request.toUserId = delegate.id;
            request.type = DelegationType.FULL;
            request.validFrom = ooo.startTime;
            request.validUntil = ooo.endTime;
            request.reason = "Automatisch aus Outlook (Abwesenheit)";
            
            delegationService.createDelegation(request);
        }
    }
}
```

## 📊 Reporting

### Delegation History View
```sql
CREATE VIEW delegation_history AS
SELECT 
    d.id,
    fu.name as from_user_name,
    tu.name as to_user_name,
    d.type,
    d.valid_from,
    d.valid_until,
    d.status,
    d.reason,
    COUNT(DISTINCT dp.permissions) as permission_count,
    d.created_at,
    d.created_by
FROM delegations d
JOIN users fu ON d.from_user_id = fu.id
JOIN users tu ON d.to_user_id = tu.id
LEFT JOIN delegation_permissions dp ON d.id = dp.delegation_id
GROUP BY d.id, fu.name, tu.name;
```

## ⚠️ Sicherheitsaspekte

1. **Delegation Chains verhindern**
   ```java
   if (delegationRepo.hasDelegationFrom(request.toUserId)) {
       throw new ValidationException("Delegation chains are not allowed");
   }
   ```

2. **Kritische Permissions**
   ```java
   Set<String> NON_DELEGATABLE = Set.of(
       "system.manage_users",
       "system.manage_roles",
       "system.view_audit_log"
   );
   ```

3. **Audit Trail**
   - Jede Delegation wird auditiert
   - Jede Nutzung delegierter Rechte wird geloggt

## 🔗 Verknüpfungen

- **Nächstes:** [Approval Workflows](./approval-workflows.md)
- **Integration:** [FC-012 Audit Trail](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md)
- **Frontend:** [Delegation UI Components](./delegation-ui-components.md)