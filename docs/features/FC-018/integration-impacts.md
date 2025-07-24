# FC-018: Integration Impacts - Datenschutz & DSGVO-Compliance

**Parent:** [FC-018 Datenschutz System](../2025-07-25_TECH_CONCEPT_FC-018-datenschutz-dsgvo-compliance.md)  
**Datum:** 2025-07-25  
**Status:** Draft  

## 📋 Übersicht

Dieses Dokument analysiert die Integration von FC-018 (Datenschutz & DSGVO-Compliance) in alle bestehenden Features und zeigt die notwendigen Änderungen auf.

## 🎯 Feature Integration Matrix

| Feature | Backend Changes | Frontend Changes | Privacy Impact | Aufwand |
|---------|----------------|------------------|----------------|---------|
| **M4 Opportunity Pipeline** | Privacy Guards für Kundendaten | PrivacyGuard um sensitive Felder | HOCH - Personenbezogene Daten | 1.5 Tage |
| **M5 Customer Management** | Field-Level Encryption | Consent UI, Export-Button | KRITISCH - Kern der PII | 2 Tage |
| **FC-003 E-Mail Integration** | Consent-Check vor E-Mail | Opt-In/Opt-Out Toggles | HOCH - Marketing-Consent | 1 Tag |
| **FC-009 Contract Renewal** | Retention Rules für Verträge | - | MITTEL - Aufbewahrungsfristen | 0.5 Tage |
| **FC-011 Pipeline Cockpit** | Privacy Metrics Widget | - | NIEDRIG - Aggregierte Daten | 0.5 Tage |
| **FC-012 Audit Trail** | Privacy Event Logging | Privacy-Filter im Audit-Viewer | KRITISCH - Compliance | 1 Tag |
| **FC-013 Activity Notes** | Personal Data Annotations | Privacy-Hinweise bei Notizen | MITTEL - User-Generated Content | 0.5 Tage |
| **FC-015 Rights & Roles** | Data Access Permissions | Privacy Role Management | KRITISCH - Zugriffskontrolle | 1.5 Tage |
| **FC-016 KPI-Tracking** | Anonymized Analytics | Privacy-Settings für KPIs | MITTEL - Aggregation ohne PII | 0.5 Tage |
| **FC-017 Error Handling** | Privacy Error Categories | - | NIEDRIG - Error ohne PII | 0.5 Tage |

## 🔧 Feature-spezifische Änderungen

### M4 Opportunity Pipeline

#### Backend Integration

```java
// OpportunityService.java - Privacy Guards hinzufügen
@ApplicationScoped
public class OpportunityService {
    
    @Inject
    PrivacyService privacyService;
    
    public OpportunityResponse getOpportunity(UUID opportunityId, String userId) {
        var opportunity = repository.findById(opportunityId)
            .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));
        
        // Privacy Check für Customer-Daten
        if (opportunity.getCustomerId() != null) {
            if (!privacyService.canAccess(
                userId, 
                opportunity.getCustomerId().toString(),
                DataClassification.PERSONAL,
                ProcessingPurpose.CONTRACT
            )) {
                // Reduzierte Response ohne Kundendaten
                return mapper.toResponseRedacted(opportunity);
            }
        }
        
        return mapper.toResponse(opportunity);
    }
}
```

#### Frontend Integration

```tsx
// OpportunityCard.tsx - Privacy Guards um sensitive Daten
const OpportunityCard: React.FC<{ opportunity: Opportunity }> = ({ opportunity }) => {
    return (
        <Card>
            <CardContent>
                <Typography variant="h6">{opportunity.title}</Typography>
                <Typography>Status: {opportunity.stage}</Typography>
                
                <PrivacyGuard
                    dataClassification={DataClassification.PERSONAL}
                    purpose={ProcessingPurpose.CONTRACT}
                    dataSubjectId={opportunity.customerId}
                >
                    <Typography>
                        Kunde: {opportunity.customerName}
                    </Typography>
                    <Typography variant="body2">
                        Kontakt: {opportunity.customerEmail}
                    </Typography>
                </PrivacyGuard>
                
                <Typography>
                    Wert: {formatCurrency(opportunity.value)}
                </Typography>
            </CardContent>
        </Card>
    );
};
```

### M5 Customer Management

#### Backend Integration - Field-Level Encryption

```java
// Customer.java - Privacy Annotations erweitern
@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    private UUID id;
    
    @PersonalData(classification = DataClassification.PUBLIC)
    @Column(name = "company_name")
    private String companyName;
    
    @PersonalData(
        classification = DataClassification.PERSONAL,
        allowedPurposes = {ProcessingPurpose.CONTRACT, ProcessingPurpose.SUPPORT},
        encryptAtRest = true
    )
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "contact_name")
    private String contactName;
    
    @PersonalData(
        classification = DataClassification.PERSONAL,
        allowedPurposes = {ProcessingPurpose.CONTRACT, ProcessingPurpose.MARKETING},
        encryptAtRest = true
    )
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "email")
    private String email;
    
    @PersonalData(
        classification = DataClassification.SENSITIVE,
        allowedPurposes = {ProcessingPurpose.CONTRACT},
        encryptAtRest = true
    )
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "phone")
    private String phone;
    
    // Datenschutz-Metadata
    @Column(name = "consent_marketing", nullable = false)
    private boolean consentMarketing = false;
    
    @Column(name = "consent_given_at")
    private Instant consentGivenAt;
    
    @Column(name = "anonymized", nullable = false)
    private boolean anonymized = false;
    
    @Column(name = "deletion_requested_at")
    private Instant deletionRequestedAt;
}
```

#### Frontend Integration - Consent & Export

```tsx
// CustomerDetailView.tsx - Privacy-erweitert
const CustomerDetailView: React.FC<{ customerId: string }> = ({ customerId }) => {
    const { data: customer } = useCustomer(customerId);
    const [consentDialogOpen, setConsentDialogOpen] = useState(false);
    
    const exportMutation = useMutation({
        mutationFn: () => privacyApi.exportCustomerData(customerId),
        onSuccess: (data) => {
            // Download-Link generieren
            downloadFile(data, `customer-${customerId}-data.json`);
        }
    });
    
    return (
        <Container>
            <Box display="flex" justifyContent="space-between" mb={2}>
                <Typography variant="h4">
                    {customer?.companyName}
                </Typography>
                
                <Box>
                    <Button
                        startIcon={<DownloadIcon />}
                        onClick={() => exportMutation.mutate()}
                        disabled={exportMutation.isLoading}
                    >
                        Daten exportieren
                    </Button>
                    <Button
                        startIcon={<SecurityIcon />}
                        onClick={() => setConsentDialogOpen(true)}
                    >
                        Datenschutz
                    </Button>
                </Box>
            </Box>
            
            <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardHeader title="Kontaktdaten" />
                        <CardContent>
                            <PrivacyGuard
                                dataClassification={DataClassification.PERSONAL}
                                purpose={ProcessingPurpose.CONTRACT}
                                dataSubjectId={customerId}
                            >
                                <Typography>
                                    <strong>Kontakt:</strong> {customer?.contactName}
                                </Typography>
                                <Typography>
                                    <strong>E-Mail:</strong> {customer?.email}
                                </Typography>
                                <Typography>
                                    <strong>Telefon:</strong> {customer?.phone}
                                </Typography>
                            </PrivacyGuard>
                        </CardContent>
                    </Card>
                </Grid>
                
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardHeader title="Marketing-Einstellungen" />
                        <CardContent>
                            <FormControlLabel
                                control={
                                    <Switch
                                        checked={customer?.consentMarketing || false}
                                        onChange={handleMarketingConsentChange}
                                    />
                                }
                                label="Marketing-E-Mails erlaubt"
                            />
                            {customer?.consentGivenAt && (
                                <Typography variant="caption" display="block">
                                    Einwilligung vom: {formatDate(customer.consentGivenAt)}
                                </Typography>
                            )}
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
            
            <PrivacyConsentDialog
                open={consentDialogOpen}
                onClose={() => setConsentDialogOpen(false)}
                customerId={customerId}
            />
        </Container>
    );
};
```

### FC-003 E-Mail Integration

#### Backend Integration - Consent Validation

```java
// EmailService.java - Consent-Check erweitern
@ApplicationScoped
public class EmailService {
    
    @Inject
    ConsentManager consentManager;
    
    @Inject
    PrivacyService privacyService;
    
    public CompletionStage<EmailSendResult> sendMarketingEmail(
        String recipientId,
        EmailTemplate template
    ) {
        // 1. Prüfe Marketing-Consent
        if (!consentManager.hasValidConsent(recipientId, ProcessingPurpose.MARKETING)) {
            return CompletableFuture.completedFuture(
                EmailSendResult.failed(
                    "No marketing consent",
                    "CONSENT_REQUIRED"
                )
            );
        }
        
        // 2. Prüfe Datenzugriff
        if (!privacyService.canAccess(
            getCurrentUserId(),
            recipientId,
            DataClassification.PERSONAL,
            ProcessingPurpose.MARKETING
        )) {
            return CompletableFuture.completedFuture(
                EmailSendResult.failed(
                    "Access denied",
                    "PRIVACY_VIOLATION"
                )
            );
        }
        
        // 3. Sende E-Mail
        return sendEmailInternal(recipientId, template)
            .thenApply(result -> {
                // 4. Protokolliere für Audit
                auditService.logEmailSent(recipientId, template.getType());
                return result;
            });
    }
}
```

### FC-012 Audit Trail Integration

#### Privacy Event Logging

```java
// PrivacyAuditEventListener.java
@ApplicationScoped
public class PrivacyAuditEventListener {
    
    @Inject
    AuditLogService auditService;
    
    public void onPersonalDataAccessed(@Observes PersonalDataAccessedEvent event) {
        auditService.log(
            AuditEntry.builder()
                .eventType(AuditEventType.PERSONAL_DATA_ACCESSED)
                .userId(event.getUserId())
                .entityType("privacy")
                .entityId(event.getDataSubjectId())
                .details(Map.of(
                    "dataClassification", event.getClassification(),
                    "purpose", event.getPurpose(),
                    "granted", event.isGranted()
                ))
                .severity(event.isGranted() ? AuditSeverity.INFO : AuditSeverity.WARNING)
                .timestamp(event.getTimestamp())
                .build()
        );
    }
    
    public void onConsentChanged(@Observes ConsentEvent event) {
        auditService.log(
            AuditEntry.builder()
                .eventType(event instanceof ConsentGivenEvent ? 
                    AuditEventType.CONSENT_GIVEN : AuditEventType.CONSENT_WITHDRAWN)
                .userId("system")
                .entityType("consent")
                .entityId(event.getConsent().getDataSubjectId())
                .details(Map.of(
                    "purpose", event.getConsent().getPurpose(),
                    "legalBasis", event.getConsent().getLegalBasis(),
                    "ipAddress", event.getConsent().getIpAddress()
                ))
                .severity(AuditSeverity.INFO)
                .timestamp(event.getTimestamp())
                .build()
        );
    }
    
    public void onDataDeleted(@Observes PersonalDataDeletedEvent event) {
        auditService.log(
            AuditEntry.builder()
                .eventType(AuditEventType.PERSONAL_DATA_DELETED)
                .userId("system")
                .entityType("privacy")
                .entityId(event.getDataSubjectId())
                .details(Map.of(
                    "reason", event.getReason(),
                    "method", event.getDeletionMethod(),
                    "recordsAffected", event.getRecordsDeleted()
                ))
                .severity(AuditSeverity.HIGH)
                .timestamp(event.getTimestamp())
                .build()
        );
    }
}
```

### FC-015 Rights & Roles Integration

#### Data Access Permissions

```java
// PrivacyRole.java - Neue Berechtigungen
public enum PrivacyPermission {
    // Datenzugriff
    ACCESS_PUBLIC_DATA("privacy:access:public"),
    ACCESS_PERSONAL_DATA("privacy:access:personal"),
    ACCESS_SENSITIVE_DATA("privacy:access:sensitive"),
    
    // Zweckbindung
    PROCESS_FOR_CONTRACT("privacy:purpose:contract"),
    PROCESS_FOR_MARKETING("privacy:purpose:marketing"),
    PROCESS_FOR_SUPPORT("privacy:purpose:support"),
    PROCESS_FOR_ANALYTICS("privacy:purpose:analytics"),
    
    // Administrative Rechte
    MANAGE_CONSENTS("privacy:manage:consents"),
    PROCESS_DELETION_REQUESTS("privacy:manage:deletions"),
    EXPORT_PERSONAL_DATA("privacy:manage:exports"),
    OVERRIDE_PRIVACY_RESTRICTIONS("privacy:override:all"); // Break-Glass
    
    private final String permission;
}

// Role-Mapping für DSGVO-Compliance
@Entity
@Table(name = "privacy_roles")
public class PrivacyRole {
    
    @Id
    private UUID id;
    
    @Column(name = "role_name", nullable = false)
    private String roleName;
    
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "privacy_role_permissions")
    private Set<PrivacyPermission> permissions = new HashSet<>();
    
    @Column(name = "data_protection_officer", nullable = false)
    private boolean dataProtectionOfficer = false;
    
    @Column(name = "can_override_restrictions", nullable = false)
    private boolean canOverrideRestrictions = false;
}
```

### FC-016 KPI-Tracking Integration

#### Anonymized Analytics

```java
// PrivacyAwareKpiCollector.java
@ApplicationScoped
public class PrivacyAwareKpiCollector {
    
    @Inject
    AnonymizationEngine anonymizer;
    
    public void collectCustomerMetrics() {
        // Nur anonymisierte/aggregierte Daten für KPIs
        var metrics = em.createQuery(
            "SELECT " +
            "COUNT(c.id) as totalCustomers, " +
            "AVG(c.contractValue) as avgValue, " +
            "c.industry as industry " +
            "FROM Customer c " +
            "WHERE c.anonymized = false " +
            "GROUP BY c.industry",
            CustomerMetric.class
        ).getResultList();
        
        // Keine Rückschlüsse auf Einzelpersonen möglich
        kpiService.updateMetrics(metrics);
    }
    
    public void collectRenewalMetrics() {
        // Zeit-basierte Aggregate ohne personenbezogene Daten
        var renewalStats = em.createQuery(
            "SELECT " +
            "DATE_TRUNC('month', r.createdAt) as month, " +
            "COUNT(r.id) as count, " +
            "AVG(r.probability) as avgProbability " +
            "FROM ContractRenewal r " +
            "GROUP BY DATE_TRUNC('month', r.createdAt)",
            RenewalMetric.class
        ).getResultList();
        
        kpiService.updateRenewalMetrics(renewalStats);
    }
}
```

## 🔄 Migration Strategy

### Phase 1: Basis-Integration (2 Tage)
1. **Tag 1**: M5 Customer - Field Encryption & Consent
2. **Tag 2**: M4 Opportunity - Privacy Guards

### Phase 2: Service-Integration (2 Tage)  
1. **Tag 3**: FC-003 E-Mail, FC-012 Audit, FC-015 Roles
2. **Tag 4**: FC-009 Retention, FC-013 Notes, FC-016 KPIs

### Phase 3: Advanced Features (1 Tag)
1. **Tag 5**: FC-011 Cockpit, FC-017 Error Handling, Testing

## 📊 Daten-Migration

### Bestehende Daten verschlüsseln

```sql
-- Migration Script für Customer-Verschlüsselung (SICHERE MULTI-STEP MIGRATION)
-- ACHTUNG: Immer erst Backup erstellen!

-- PHASE 1: Neue verschlüsselte Spalten hinzufügen
CREATE TABLE customers_backup AS SELECT * FROM customers;

ALTER TABLE customers 
ADD COLUMN contact_name_encrypted TEXT,
ADD COLUMN email_encrypted TEXT, 
ADD COLUMN phone_encrypted TEXT,
ADD COLUMN anonymized BOOLEAN DEFAULT FALSE,
ADD COLUMN consent_marketing BOOLEAN DEFAULT FALSE,
ADD COLUMN consent_given_at TIMESTAMP,
ADD COLUMN migration_status VARCHAR(20) DEFAULT 'PENDING'; -- PENDING, MIGRATED, VERIFIED

-- PHASE 2: Batch-Migration (NICHT beim ersten Update!)
-- Wird durch separaten BatchMigrationService durchgeführt
```

**SICHERE MIGRATIONS-STRATEGIE:**

```java
// BatchEncryptionMigrationService.java
@ApplicationScoped
public class BatchEncryptionMigrationService {
    
    @Inject
    EncryptionService encryptionService;
    
    @Scheduled(every = "1m", executionThreshold = 10000)
    @Transactional
    public void migrateBatch() {
        List<Customer> unmigrated = customerRepository
            .find("migration_status = 'PENDING'")
            .page(0, 100) // Kleine Batches
            .list();
            
        for (Customer customer : unmigrated) {
            try {
                // Dual-Write: Verschlüsselt + Klartext parallel
                customer.contactNameEncrypted = encryptionService.encrypt(customer.contactName);
                customer.emailEncrypted = encryptionService.encrypt(customer.email);
                customer.phoneEncrypted = encryptionService.encrypt(customer.phone);
                customer.migrationStatus = "MIGRATED";
                
                // Validation: Decrypt & Compare
                if (encryptionService.decrypt(customer.contactNameEncrypted).equals(customer.contactName)) {
                    customer.migrationStatus = "VERIFIED";
                }
                
                customerRepository.persist(customer);
                Log.info("Migrated customer: " + customer.id);
                
            } catch (Exception e) {
                Log.error("Migration failed for customer: " + customer.id, e);
                customer.migrationStatus = "FAILED";
            }
        }
    }
}

// PHASE 3: Application Code liest aus beiden Spalten
@Convert(converter = EncryptedStringConverter.class)
public String getContactName() {
    // Während Migration: Fallback auf Klartext
    return contactNameEncrypted != null ? contactNameEncrypted : contactName;
}

// PHASE 4: Nach vollständiger Migration (100% VERIFIED)
-- ALTER TABLE customers DROP COLUMN contact_name, email, phone;
-- ALTER TABLE customers RENAME COLUMN contact_name_encrypted TO contact_name;
```

### Consent-Erstellung für bestehende Kunden

```java
// ConsentMigrationService.java
@ApplicationScoped
public class ConsentMigrationService {
    
    /**
     * Erstellt implied Consent für bestehende Kunden basierend auf Geschäftsbeziehung
     */
    @Transactional
    public void createImpliedConsents() {
        var customers = customerRepository.findAllWithoutConsent();
        
        for (var customer : customers) {
            // Contract-based Consent (Art. 6(1)(b) DSGVO)
            var contractConsent = Consent.builder()
                .dataSubjectId(customer.getId().toString())
                .purpose(ProcessingPurpose.CONTRACT)
                .legalBasis(LegalBasis.CONTRACT)
                .givenAt(customer.getCreatedAt()) // Vertragsschluss
                .consentText("Verarbeitung zur Vertragserfüllung")
                .build();
            
            consentRepository.persist(contractConsent);
            
            // Marketing-Consent nur wenn explizit erteilt
            if (customer.hasMarketingHistory()) {
                var marketingConsent = Consent.builder()
                    .dataSubjectId(customer.getId().toString())
                    .purpose(ProcessingPurpose.MARKETING)
                    .legalBasis(LegalBasis.LEGITIMATE_INTEREST)
                    .givenAt(customer.getCreatedAt())
                    .expiresAt(Instant.now().plus(Duration.ofDays(365))) // 1 Jahr
                    .consentText("Verarbeitung aufgrund berechtigten Interesses")
                    .build();
                
                consentRepository.persist(marketingConsent);
            }
        }
    }
}
```

## 🧪 Testing Checklist

### DSGVO-Compliance Tests

```java
@QuarkusTest
class PrivacyComplianceTest {
    
    @Test
    void testPersonalDataAccessControl() {
        // User ohne Berechtigung darf keine PII sehen
        var unauthorizedUser = createUser("sales", Set.of(Role.SALES));
        var customer = createCustomer();
        
        assertFalse(privacyService.canAccess(
            unauthorizedUser.getId(),
            customer.getId().toString(),
            DataClassification.SENSITIVE,
            ProcessingPurpose.MARKETING
        ));
    }
    
    @Test
    void testDataExportCompleteness() {
        var customer = createCustomerWithData();
        var export = exportService.exportAllData(customer.getId().toString());
        
        // Muss alle personenbezogenen Daten enthalten
        assertThat(export.getPersonalData())
            .containsKeys("name", "email", "phone", "address");
        assertThat(export.getProcessingHistory()).isNotEmpty();
        assertThat(export.getConsents()).isNotEmpty();
    }
    
    @Test
    void testRightToBeForgotten() {
        var customer = createCustomer();
        var deletionRequest = new DeletionRequest(
            customer.getId().toString(),
            "User request"
        );
        
        var result = privacyService.deletePersonalData(
            customer.getId().toString(),
            deletionRequest
        ).toCompletableFuture().join();
        
        // Kunde sollte anonymisiert sein
        var updatedCustomer = customerRepository.findById(customer.getId());
        assertTrue(updatedCustomer.get().isAnonymized());
        
        // PII sollte unkenntlich gemacht sein
        assertThat(updatedCustomer.get().getEmail()).contains("***");
    }
}
```

### Performance Tests

```java
@QuarkusTest
class PrivacyPerformanceTest {
    
    @Test
    void testEncryptionPerformance() {
        var customers = createCustomers(1000);
        
        var start = System.currentTimeMillis();
        
        customers.forEach(customer -> {
            customer.setEmail("new-email@test.com"); // Triggers encryption
            customerRepository.persist(customer);
        });
        
        var duration = System.currentTimeMillis() - start;
        
        // Encryption sollte nicht länger als 10ms pro Record dauern
        assertThat(duration).isLessThan(10000);
    }
}
```

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] Database Migration Scripts getestet
- [ ] Encryption Keys generiert und sicher gespeichert
- [ ] Consent-Migration für bestehende Daten durchgeführt
- [ ] Privacy Policies aktualisiert
- [ ] Team-Schulung zu neuen Features abgeschlossen

### Post-Deployment
- [ ] Privacy Center funktional getestet
- [ ] Consent-Management UI funktional
- [ ] Data Export/Delete Workflows getestet
- [ ] Audit Logs enthalten Privacy Events
- [ ] Performance Monitoring aktiviert
- [ ] DPO (Datenschutzbeauftragter) informiert

---

**Zusammenfassung:**

FC-018 integriert sich in **alle Features** mit unterschiedlichen Auswirkungen:
- **Kritisch**: M5 Customer, FC-012 Audit, FC-015 Roles (Kern der Compliance)
- **Hoch**: M4 Opportunity, FC-003 E-Mail (häufige PII-Zugriffe)
- **Mittel**: FC-009 Contracts, FC-013 Notes, FC-016 KPIs
- **Niedrig**: FC-011 Cockpit, FC-017 Error Handling

**Gesamt-Integrationsaufwand**: 8.5 Tage zusätzlich zu den 8-9 Tagen für FC-018 selbst.