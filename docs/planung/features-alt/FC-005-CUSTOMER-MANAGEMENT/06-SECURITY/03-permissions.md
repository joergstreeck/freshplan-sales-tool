# 🛡️ FC-005 SECURITY - PERMISSIONS & AUDIT

**Navigation:**
- **Parent:** [06-SECURITY](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/README.md)
- **Prev:** [02-encryption.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/02-encryption.md)
- **Next:** [07-PERFORMANCE](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/README.md)

---

## Role-Based Access Control (RBAC)

### Security Policy Implementation

```java
@ApplicationScoped
public class CustomerSecurityPolicy {
    
    @ConfigProperty(name = "security.customer.field-permissions")
    Map<String, Set<String>> fieldPermissionsByRole;
    
    @Inject
    SecurityContext securityContext;
    
    @Inject
    UserCustomerAssignmentRepository assignmentRepo;
    
    public boolean canViewCustomer(UUID customerId) {
        if (securityContext.isUserInRole("admin")) {
            return true;
        }
        
        if (securityContext.isUserInRole("manager")) {
            // Managers can see all customers in their region/team
            return isCustomerInUserScope(customerId);
        }
        
        if (securityContext.isUserInRole("sales")) {
            // Sales can only see assigned customers
            return assignmentRepo.isUserAssignedToCustomer(
                getCurrentUserId(), 
                customerId
            );
        }
        
        return false;
    }
    
    public boolean canEditCustomer(UUID customerId) {
        if (!canViewCustomer(customerId)) {
            return false;
        }
        
        // Additional checks for edit permissions
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) return false;
        
        // Only admin can edit archived customers
        if (customer.getStatus() == CustomerStatus.ARCHIVED) {
            return securityContext.isUserInRole("admin");
        }
        
        // Check field-level permissions
        return hasEditPermission(customerId);
    }
    
    public boolean canDeleteCustomer(UUID customerId) {
        // Only admin can delete
        if (!securityContext.isUserInRole("admin")) {
            return false;
        }
        
        // Additional business rules
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) return false;
        
        // Cannot delete if has active contracts
        return !contractService.hasActiveContracts(customerId);
    }
}
```

### Field-Level Permissions

```java
@ApplicationScoped
public class FieldPermissionService {
    
    private static final Map<String, Set<String>> FIELD_PERMISSIONS = Map.of(
        "admin", Set.of("*"), // All fields
        "manager", Set.of(
            "companyName", "industry", "contactName", "contactEmail",
            "contactPhone", "street", "postalCode", "city",
            "expectedVolume", "paymentMethod", "creditLimit"
        ),
        "sales", Set.of(
            "companyName", "industry", "contactName", "contactEmail",
            "contactPhone", "notes", "customField1", "customField2"
        ),
        "viewer", Set.of(
            "companyName", "industry", "city"
        )
    );
    
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "creditLimit", "paymentTerms", "internalNotes", 
        "bankAccount", "taxId"
    );
    
    public Set<String> getEditableFields(String role, UUID customerId) {
        Set<String> baseFields = FIELD_PERMISSIONS.getOrDefault(role, Set.of());
        
        // Remove sensitive fields for non-admin
        if (!"admin".equals(role)) {
            baseFields = new HashSet<>(baseFields);
            baseFields.removeAll(SENSITIVE_FIELDS);
        }
        
        return baseFields;
    }
    
    public Map<String, Object> filterFieldsByPermission(
        Map<String, Object> fields,
        String role,
        UUID customerId
    ) {
        Set<String> allowedFields = getEditableFields(role, customerId);
        
        return fields.entrySet().stream()
            .filter(entry -> 
                allowedFields.contains("*") || 
                allowedFields.contains(entry.getKey())
            )
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }
}
```

### Frontend Permission Hooks

```typescript
// hooks/useCustomerPermissions.ts
export const useCustomerPermissions = (customerId?: string) => {
  const { user } = useAuth();
  
  const { data: permissions } = useQuery({
    queryKey: ['permissions', 'customer', customerId],
    queryFn: () => customerApi.getPermissions(customerId!),
    enabled: !!customerId,
    staleTime: 5 * 60 * 1000 // 5 minutes
  });
  
  return {
    canView: permissions?.canView ?? false,
    canEdit: permissions?.canEdit ?? false,
    canDelete: permissions?.canDelete ?? false,
    canChangeStatus: permissions?.canChangeStatus ?? false,
    editableFields: permissions?.editableFields ?? [],
    
    // Helper functions
    isFieldEditable: (fieldKey: string) => {
      if (!permissions?.editableFields) return false;
      return permissions.editableFields.includes('*') 
        || permissions.editableFields.includes(fieldKey);
    },
    
    isFieldVisible: (fieldKey: string) => {
      if (!permissions?.visibleFields) return true;
      return permissions.visibleFields.includes('*')
        || permissions.visibleFields.includes(fieldKey);
    },
    
    canPerformAction: (action: CustomerAction) => {
      return permissions?.allowedActions?.includes(action) ?? false;
    }
  };
};

// Usage in components
const CustomerFieldEditor: React.FC<{ 
  fieldKey: string;
  customerId: string;
}> = ({ fieldKey, customerId }) => {
  const { isFieldEditable, isFieldVisible } = useCustomerPermissions(customerId);
  
  if (!isFieldVisible(fieldKey)) {
    return null;
  }
  
  return (
    <TextField
      disabled={!isFieldEditable(fieldKey)}
      // ... other props
    />
  );
};
```

---

## Audit Logging

### DSGVO-konformes Audit System

```java
@Entity
@Table(name = "customer_data_access_log")
@EntityListeners(AuditingEntityListener.class)
public class CustomerDataAccessLog {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private String userName;
    
    @Column(nullable = false)
    private UUID customerId;
    
    @Enumerated(EnumType.STRING)
    private AccessType accessType; // VIEW, EDIT, EXPORT, DELETE
    
    @ElementCollection
    @CollectionTable(name = "accessed_fields")
    private Set<String> accessedFields;
    
    @Column(nullable = false)
    private String purpose; // Business reason
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private String userAgent;
    
    @CreatedDate
    private LocalDateTime accessedAt;
    
    // Legal basis for access
    @Enumerated(EnumType.STRING)
    private LegalBasis legalBasis;
    
    // Session tracking
    private String sessionId;
    
    // Result of access
    @Enumerated(EnumType.STRING)
    private AccessResult result; // SUCCESS, DENIED, ERROR
}

@Component
public class CustomerAccessLogger {
    
    @Inject
    CustomerDataAccessLogRepository repository;
    
    @Inject
    SecurityContext securityContext;
    
    @Inject
    HttpServletRequest request;
    
    public void logDataAccess(
        UUID customerId,
        AccessType type,
        Set<String> fields,
        String purpose,
        AccessResult result
    ) {
        CustomerDataAccessLog log = new CustomerDataAccessLog();
        log.setUserId(getCurrentUserId());
        log.setUserName(getCurrentUserName());
        log.setCustomerId(customerId);
        log.setAccessType(type);
        log.setAccessedFields(filterSensitiveFields(fields));
        log.setPurpose(purpose);
        log.setIpAddress(getClientIpAddress());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setLegalBasis(determineLegalBasis(type));
        log.setSessionId(request.getSession().getId());
        log.setResult(result);
        
        repository.persist(log);
        
        // Alert on suspicious activity
        if (result == AccessResult.DENIED || isSuspiciousAccess(log)) {
            alertSecurityTeam(log);
        }
    }
    
    private Set<String> filterSensitiveFields(Set<String> fields) {
        // Log only that sensitive fields were accessed, not values
        return fields.stream()
            .map(field -> {
                if (isSensitiveField(field)) {
                    return field + "_ACCESSED";
                }
                return field;
            })
            .collect(Collectors.toSet());
    }
    
    private boolean isSuspiciousAccess(CustomerDataAccessLog log) {
        // Check for unusual patterns
        long recentAccesses = repository.countRecentAccesses(
            log.getUserId(), 
            log.getAccessType(),
            LocalDateTime.now().minusMinutes(5)
        );
        
        return recentAccesses > 100; // Threshold for bulk access
    }
}
```

### Audit Trail UI

```typescript
// components/CustomerAuditTrail.tsx
export const CustomerAuditTrail: React.FC<{ customerId: string }> = ({ 
  customerId 
}) => {
  const { data: auditEntries, isLoading } = useQuery({
    queryKey: ['audit', 'customer', customerId],
    queryFn: () => auditApi.getCustomerAuditTrail(customerId)
  });
  
  const groupedEntries = useMemo(() => {
    if (!auditEntries) return {};
    
    return auditEntries.reduce((acc, entry) => {
      const date = format(new Date(entry.accessedAt), 'yyyy-MM-dd');
      if (!acc[date]) acc[date] = [];
      acc[date].push(entry);
      return acc;
    }, {} as Record<string, AuditEntry[]>);
  }, [auditEntries]);
  
  if (isLoading) return <CircularProgress />;
  
  return (
    <Paper>
      <Typography variant="h6">Zugriffsverlauf</Typography>
      
      {Object.entries(groupedEntries).map(([date, entries]) => (
        <Box key={date}>
          <Typography variant="subtitle2">{date}</Typography>
          <Timeline>
            {entries.map(entry => (
              <TimelineItem key={entry.id}>
                <TimelineSeparator>
                  <TimelineDot color={getColorForAccessType(entry.accessType)} />
                  <TimelineConnector />
                </TimelineSeparator>
                <TimelineContent>
                  <Typography variant="body2">
                    {entry.userName} - {entry.accessType}
                  </Typography>
                  <Typography variant="caption">
                    {format(new Date(entry.accessedAt), 'HH:mm:ss')}
                  </Typography>
                  {entry.accessedFields.length > 0 && (
                    <Chip
                      size="small"
                      label={`${entry.accessedFields.length} Felder`}
                    />
                  )}
                </TimelineContent>
              </TimelineItem>
            ))}
          </Timeline>
        </Box>
      ))}
    </Paper>
  );
};
```

---

## Automatische Löschfristen

### Retention Service

```java
@ApplicationScoped
public class DataRetentionService {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    CustomerDeletionService deletionService;
    
    @ConfigProperty(name = "gdpr.retention.draft-days", defaultValue = "30")
    int draftRetentionDays;
    
    @ConfigProperty(name = "gdpr.retention.inactive-years", defaultValue = "6")
    int inactiveRetentionYears;
    
    @ConfigProperty(name = "gdpr.retention.post-contract-years", defaultValue = "10")
    int postContractRetentionYears;
    
    @Scheduled(every = "24h")
    @Transactional
    void enforceRetentionPolicies() {
        Log.info("Starting retention policy enforcement");
        
        // 1. Delete old drafts
        deleteOldDrafts();
        
        // 2. Mark inactive customers for review
        markInactiveCustomers();
        
        // 3. Delete expired customer data
        deleteExpiredCustomerData();
        
        // 4. Clean up audit logs
        cleanupAuditLogs();
        
        Log.info("Retention policy enforcement completed");
    }
    
    private void deleteOldDrafts() {
        LocalDateTime draftCutoff = LocalDateTime.now()
            .minusDays(draftRetentionDays);
        
        List<Customer> oldDrafts = customerRepository
            .find("status = ?1 AND createdAt < ?2", 
                CustomerStatus.DRAFT, 
                draftCutoff)
            .list();
            
        Log.infof("Found %d old drafts to delete", oldDrafts.size());
        
        for (Customer draft : oldDrafts) {
            deletionService.deleteCustomer(
                draft.getId(),
                DeletionRequest.automatic("Draft retention policy")
            );
        }
    }
    
    private void markInactiveCustomers() {
        LocalDateTime inactiveCutoff = LocalDateTime.now()
            .minusYears(inactiveRetentionYears);
            
        List<Customer> inactiveCustomers = customerRepository
            .find("lastActivityDate < ?1 AND status = ?2",
                inactiveCutoff,
                CustomerStatus.ACTIVE)
            .list();
            
        for (Customer customer : inactiveCustomers) {
            customer.setMarkedForDeletion(true);
            customer.setDeletionScheduledFor(
                LocalDateTime.now().plusDays(30)
            );
            
            // Notify account manager
            notificationService.notifyAccountManager(
                customer.getId(),
                "Customer marked for deletion due to inactivity"
            );
        }
    }
    
    private void deleteExpiredCustomerData() {
        List<Customer> expiredCustomers = customerRepository
            .find("markedForDeletion = true AND deletionScheduledFor < ?1",
                LocalDateTime.now())
            .list();
            
        for (Customer customer : expiredCustomers) {
            // Check if deletion can proceed
            if (!hasActiveObligations(customer.getId())) {
                deletionService.deleteCustomer(
                    customer.getId(),
                    DeletionRequest.automatic("Retention period expired")
                );
            }
        }
    }
}
```

### Deletion Configuration

```yaml
# Löschfristen-Konfiguration
gdpr:
  retention:
    # Drafts - kurze Aufbewahrung
    draft-days: 30
    
    # Inaktive Kunden
    inactive-years: 6
    inactive-warning-days: 30
    
    # Nach Vertragsende
    post-contract-years: 10
    
    # Spezielle Datentypen
    marketing-consent-months: 24
    email-history-years: 3
    activity-logs-years: 2
    
  deletion:
    # Soft Delete Phase
    soft-delete-days: 30
    allow-restoration: true
    
    # Anonymisierung
    anonymize-personal-data: true
    keep-statistical-data: true
    anonymization-fields:
      - contactName
      - contactEmail
      - contactPhone
      - internalNotes
    
    # Aufbewahrung für Compliance
    keep-financial-data: true
    financial-retention-years: 10
    
  audit:
    # Audit Log Aufbewahrung
    access-log-retention-days: 365
    deletion-log-retention-years: 10
    security-event-retention-years: 7
```

---

## Security Monitoring

### Real-time Monitoring

```java
@ApplicationScoped
public class SecurityMonitoringService {
    
    @Inject
    Event<SecurityAlert> alertBus;
    
    @ConfigProperty(name = "security.monitoring.thresholds")
    Map<String, Integer> thresholds;
    
    public void monitorAccess(CustomerDataAccessLog log) {
        // Check for bulk exports
        if (log.getAccessType() == AccessType.EXPORT) {
            long exportCount = countRecentExports(log.getUserId());
            if (exportCount > thresholds.get("max.exports.per.hour")) {
                raiseSecurityAlert(
                    SecurityAlert.Level.HIGH,
                    "Bulk export detected",
                    log
                );
            }
        }
        
        // Check for unusual access patterns
        if (isUnusualAccessPattern(log)) {
            raiseSecurityAlert(
                SecurityAlert.Level.MEDIUM,
                "Unusual access pattern detected",
                log
            );
        }
        
        // Check for access outside business hours
        if (isOutsideBusinessHours(log.getAccessedAt())) {
            raiseSecurityAlert(
                SecurityAlert.Level.LOW,
                "Access outside business hours",
                log
            );
        }
    }
    
    private void raiseSecurityAlert(
        SecurityAlert.Level level,
        String message,
        CustomerDataAccessLog log
    ) {
        SecurityAlert alert = new SecurityAlert();
        alert.setLevel(level);
        alert.setMessage(message);
        alert.setUserId(log.getUserId());
        alert.setCustomerId(log.getCustomerId());
        alert.setTimestamp(LocalDateTime.now());
        alert.setDetails(buildAlertDetails(log));
        
        alertBus.fire(alert);
        
        if (level == SecurityAlert.Level.HIGH) {
            // Immediate notification
            notifySecurityTeam(alert);
        }
    }
}
```

---

## Security Checkliste

### Entwicklung
- [ ] Alle Rollen und Berechtigungen definiert
- [ ] Field-Level Permissions implementiert
- [ ] Audit-Logging für alle Zugriffe
- [ ] Automatische Löschfristen konfiguriert
- [ ] Security Monitoring aktiv

### Deployment
- [ ] Least Privilege Principle angewendet
- [ ] Audit Logs extern gesichert
- [ ] Monitoring Alerts konfiguriert
- [ ] Retention Policies aktiv
- [ ] Incident Response Plan bereit

### Compliance
- [ ] DSGVO-Löschfristen eingehalten
- [ ] Audit Trail vollständig
- [ ] Berechtigungen dokumentiert
- [ ] Security Reviews durchgeführt
- [ ] Penetration Tests bestanden