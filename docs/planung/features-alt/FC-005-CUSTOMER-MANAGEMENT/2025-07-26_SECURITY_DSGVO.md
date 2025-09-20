# 🔒 FC-005 CUSTOMER MANAGEMENT - SECURITY & DSGVO COMPLIANCE

**Datum:** 26.07.2025  
**Version:** 1.0  
**Kritikalität:** HOCH - Personenbezogene Daten  
**Compliance:** DSGVO/GDPR konform  

## 📋 Inhaltsverzeichnis

1. [Datenschutz-Grundlagen](#datenschutz-grundlagen)
2. [Personenbezogene Daten](#personenbezogene-daten)
3. [Technische Schutzmaßnahmen](#technische-schutzmaßnahmen)
4. [Berechtigungskonzept](#berechtigungskonzept)
5. [Audit & Logging](#audit--logging)
6. [Datenminimierung](#datenminimierung)
7. [Betroffenenrechte](#betroffenenrechte)
8. [Datenlöschung](#datenlöschung)

---

## Datenschutz-Grundlagen

### DSGVO-Prinzipien

| Prinzip | Umsetzung im Customer Management |
|---------|----------------------------------|
| **Rechtmäßigkeit** | Verarbeitung nur mit Rechtsgrundlage (Vertrag, berechtigtes Interesse) |
| **Transparenz** | Klare Information über Datenverarbeitung |
| **Zweckbindung** | Daten nur für Kundenverwaltung und Vertrieb |
| **Datenminimierung** | Nur notwendige Felder als Pflichtfelder |
| **Richtigkeit** | Validierung und Aktualisierungsmöglichkeiten |
| **Speicherbegrenzung** | Automatische Löschfristen |
| **Integrität** | Verschlüsselung und Zugriffskontrolle |
| **Rechenschaftspflicht** | Vollständiges Audit-Log |

---

## Personenbezogene Daten

### Datenklassifizierung

```typescript
enum DataSensitivity {
  PUBLIC = 'PUBLIC',           // Firmenname, Branche
  INTERNAL = 'INTERNAL',       // Kundennummer, Status
  CONFIDENTIAL = 'CONFIDENTIAL', // Kontaktdaten, Adressen
  HIGHLY_CONFIDENTIAL = 'HIGHLY_CONFIDENTIAL' // Finanzdaten, Verträge
}

interface FieldDataClassification {
  fieldKey: string;
  sensitivity: DataSensitivity;
  personalData: boolean;
  specialCategory: boolean; // Art. 9 DSGVO
  retentionPeriod: number; // Tage
  encryptionRequired: boolean;
}

// Field Classifications
const FIELD_CLASSIFICATIONS: FieldDataClassification[] = [
  {
    fieldKey: 'companyName',
    sensitivity: DataSensitivity.PUBLIC,
    personalData: false,
    specialCategory: false,
    retentionPeriod: 3650, // 10 Jahre
    encryptionRequired: false
  },
  {
    fieldKey: 'contactName',
    sensitivity: DataSensitivity.CONFIDENTIAL,
    personalData: true,
    specialCategory: false,
    retentionPeriod: 2190, // 6 Jahre nach Vertragsende
    encryptionRequired: true
  },
  {
    fieldKey: 'contactEmail',
    sensitivity: DataSensitivity.CONFIDENTIAL,
    personalData: true,
    specialCategory: false,
    retentionPeriod: 2190,
    encryptionRequired: true
  },
  {
    fieldKey: 'contactPhone',
    sensitivity: DataSensitivity.CONFIDENTIAL,
    personalData: true,
    specialCategory: false,
    retentionPeriod: 2190,
    encryptionRequired: true
  }
];
```

### Rechtsgrundlagen

```java
@Entity
@Table(name = "customer_consent")
public class CustomerConsent {
    @Id
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Enumerated(EnumType.STRING)
    private ConsentType type;
    
    @Enumerated(EnumType.STRING)
    private LegalBasis legalBasis;
    
    private LocalDateTime grantedAt;
    private LocalDateTime revokedAt;
    private String grantedBy; // Person who gave consent
    
    @Column(columnDefinition = "text")
    private String purpose;
}

public enum LegalBasis {
    CONTRACT_FULFILLMENT,    // Art. 6 Abs. 1 lit. b DSGVO
    LEGITIMATE_INTEREST,     // Art. 6 Abs. 1 lit. f DSGVO
    CONSENT,                // Art. 6 Abs. 1 lit. a DSGVO
    LEGAL_OBLIGATION        // Art. 6 Abs. 1 lit. c DSGVO
}
```

---

## Technische Schutzmaßnahmen

### Verschlüsselung

```java
@Component
public class FieldEncryptionService {
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    private final Cipher cipher;
    
    @PostConstruct
    public void init() throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(
            Base64.getDecoder().decode(encryptionKey), 
            "AES"
        );
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
    }
    
    public String encryptFieldValue(String plainText) {
        if (plainText == null) return null;
        
        try {
            byte[] iv = new byte[12];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt field value", e);
        }
    }
    
    public String decryptFieldValue(String encryptedText) {
        // Decryption implementation
    }
}
```

### Database-Level Encryption

```sql
-- PostgreSQL Transparent Data Encryption (TDE)
ALTER TABLE field_values 
    ALTER COLUMN value 
    SET STORAGE EXTERNAL ENCRYPTED;

-- Column-level encryption for sensitive fields
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Encrypt on insert/update
CREATE OR REPLACE FUNCTION encrypt_sensitive_data()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.field_definition_id IN ('contactEmail', 'contactPhone') THEN
        NEW.value = pgp_sym_encrypt(
            NEW.value::text, 
            current_setting('app.encryption_key')
        )::jsonb;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER encrypt_field_values
    BEFORE INSERT OR UPDATE ON field_values
    FOR EACH ROW
    EXECUTE FUNCTION encrypt_sensitive_data();
```

### Transport Security

```yaml
# application.yml
quarkus:
  http:
    ssl:
      protocols: TLSv1.3,TLSv1.2
      cipher-suites:
        - TLS_AES_256_GCM_SHA384
        - TLS_AES_128_GCM_SHA256
        - TLS_CHACHA20_POLY1305_SHA256
  
  # Force HTTPS
  http:
    insecure-requests: REDIRECT
    
  # Security Headers
  http:
    header:
      X-Frame-Options: DENY
      X-Content-Type-Options: nosniff
      X-XSS-Protection: "1; mode=block"
      Strict-Transport-Security: "max-age=31536000; includeSubDomains"
      Content-Security-Policy: "default-src 'self'"
```

---

## Berechtigungskonzept

### Role-Based Access Control (RBAC)

```java
@ApplicationScoped
public class CustomerSecurityPolicy {
    
    @ConfigProperty(name = "security.customer.field-permissions")
    Map<String, Set<String>> fieldPermissionsByRole;
    
    public boolean canAccessField(String role, String fieldKey) {
        // Special handling for sensitive fields
        Set<String> sensitiveFields = Set.of(
            "creditLimit", 
            "paymentTerms", 
            "internalNotes"
        );
        
        if (sensitiveFields.contains(fieldKey)) {
            return role.equals("admin") || role.equals("manager");
        }
        
        return fieldPermissionsByRole
            .getOrDefault(role, Set.of())
            .contains(fieldKey);
    }
    
    public Map<String, Object> filterFieldsByRole(
        Map<String, Object> fields, 
        String role
    ) {
        return fields.entrySet().stream()
            .filter(entry -> canAccessField(role, entry.getKey()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }
}
```

### Frontend Permission Enforcement

```typescript
// hooks/useFieldSecurity.ts
export const useFieldSecurity = () => {
  const { user } = useAuth();
  
  const maskSensitiveField = (
    fieldKey: string, 
    value: any, 
    maskPattern = '***'
  ): any => {
    const sensitiveFields = ['contactEmail', 'contactPhone'];
    
    if (!sensitiveFields.includes(fieldKey)) {
      return value;
    }
    
    // Check permissions
    if (!user.permissions.includes(`customer.field.${fieldKey}.read`)) {
      return maskPattern;
    }
    
    // Partial masking for authorized users
    if (fieldKey === 'contactEmail' && typeof value === 'string') {
      const [local, domain] = value.split('@');
      return `${local.substring(0, 3)}***@${domain}`;
    }
    
    if (fieldKey === 'contactPhone' && typeof value === 'string') {
      return value.replace(/\d(?=\d{4})/g, '*');
    }
    
    return value;
  };
  
  return { maskSensitiveField };
};
```

---

## Audit & Logging

### DSGVO-konformes Audit-Log

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
        String purpose
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
        
        repository.persist(log);
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
}
```

---

## Datenminimierung

### Dynamic Required Fields

```typescript
// utils/fieldRequirements.ts
export const getRequiredFields = (
  customerType: string,
  industry: string,
  purpose: 'create' | 'contract' | 'marketing'
): string[] => {
  const baseRequired = ['companyName', 'industry'];
  
  switch (purpose) {
    case 'create':
      // Minimal für Anlage
      return [...baseRequired, 'contactEmail'];
      
    case 'contract':
      // Erweitert für Vertragsabschluss
      return [
        ...baseRequired,
        'legalForm',
        'street',
        'postalCode',
        'city',
        'contactName',
        'contactEmail',
        'contactPhone'
      ];
      
    case 'marketing':
      // Nur mit expliziter Einwilligung
      return [...baseRequired, 'marketingConsent'];
      
    default:
      return baseRequired;
  }
};
```

### Progressive Data Collection

```java
@ApplicationScoped
public class ProgressiveDataCollectionService {
    
    public ValidationResult validateDataCompleteness(
        Customer customer,
        DataPurpose purpose
    ) {
        Map<String, Object> fieldValues = getFieldValues(customer);
        Set<String> requiredFields = getRequiredFieldsForPurpose(purpose);
        Set<String> missingFields = new HashSet<>();
        
        for (String required : requiredFields) {
            if (!fieldValues.containsKey(required) || 
                fieldValues.get(required) == null) {
                missingFields.add(required);
            }
        }
        
        if (missingFields.isEmpty()) {
            return ValidationResult.success();
        }
        
        return ValidationResult.incomplete(
            "Folgende Felder werden für " + purpose + " benötigt",
            missingFields
        );
    }
}
```

---

## Betroffenenrechte

### Auskunftsrecht (Art. 15 DSGVO)

```java
@Path("/api/gdpr/customer-data")
@Authenticated
public class GDPRCustomerDataResource {
    
    @Inject
    CustomerDataExportService exportService;
    
    @GET
    @Path("/export/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "data-protection-officer"})
    public Response exportCustomerData(
        @PathParam("customerId") UUID customerId,
        @QueryParam("format") @DefaultValue("json") String format
    ) {
        // Verify request legitimacy
        verifyDataExportRequest(customerId);
        
        // Log access for audit
        auditLogger.logDataAccess(
            customerId,
            AccessType.EXPORT,
            Set.of("ALL_FIELDS"),
            "GDPR Art. 15 - Data Subject Request"
        );
        
        CustomerDataExport export = exportService.exportAllData(customerId);
        
        if ("pdf".equals(format)) {
            byte[] pdf = pdfGenerator.generateDataExportPdf(export);
            return Response.ok(pdf)
                .type("application/pdf")
                .header("Content-Disposition", 
                    "attachment; filename=customer-data-export.pdf")
                .build();
        }
        
        return Response.ok(export).build();
    }
}

@ApplicationScoped
public class CustomerDataExportService {
    
    public CustomerDataExport exportAllData(UUID customerId) {
        CustomerDataExport export = new CustomerDataExport();
        
        // Basic customer data
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
        export.setCustomerData(extractCustomerData(customer));
        
        // Field values
        export.setFieldValues(extractFieldValues(customer));
        
        // Locations
        export.setLocations(extractLocationData(customer));
        
        // Related data from other modules
        export.setOpportunities(opportunityService.findByCustomer(customerId));
        export.setContracts(contractService.findByCustomer(customerId));
        export.setActivities(activityService.findByCustomer(customerId));
        export.setEmails(emailService.findByCustomer(customerId));
        
        // Access logs
        export.setAccessLogs(extractAccessLogs(customerId));
        
        // Consents
        export.setConsents(consentService.findByCustomer(customerId));
        
        return export;
    }
}
```

### Berichtigungsrecht (Art. 16 DSGVO)

```typescript
// components/DataCorrectionRequest.tsx
export const DataCorrectionRequest: React.FC<{ customerId: string }> = ({ 
  customerId 
}) => {
  const [corrections, setCorrections] = useState<Map<string, any>>(new Map());
  const [reason, setReason] = useState('');
  
  const handleSubmitCorrection = async () => {
    await gdprApi.submitCorrectionRequest({
      customerId,
      corrections: Array.from(corrections.entries()),
      reason,
      requestedBy: 'data_subject'
    });
  };
  
  return (
    <Paper>
      <Typography variant="h6">Datenberichtigung beantragen</Typography>
      <CorrectionForm 
        fields={editableFields}
        onCorrection={(field, value) => 
          setCorrections(prev => new Map(prev).set(field, value))
        }
      />
      <TextField
        label="Grund der Berichtigung"
        value={reason}
        onChange={(e) => setReason(e.target.value)}
        required
      />
      <Button onClick={handleSubmitCorrection}>
        Berichtigung beantragen
      </Button>
    </Paper>
  );
};
```

### Löschrecht (Art. 17 DSGVO)

```java
@ApplicationScoped
@Transactional
public class CustomerDeletionService {
    
    @Inject
    Event<CustomerDeletionEvent> deletionEvent;
    
    public DeletionResult deleteCustomer(
        UUID customerId,
        DeletionRequest request
    ) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
            
        // Check if deletion is allowed
        DeletionEligibility eligibility = checkDeletionEligibility(customer);
        if (!eligibility.isEligible()) {
            return DeletionResult.blocked(eligibility.getReasons());
        }
        
        // Start deletion process
        DeletionResult result = new DeletionResult();
        
        // 1. Anonymize personal data
        anonymizePersonalData(customer);
        result.addStep("Personal data anonymized");
        
        // 2. Delete field values
        fieldValueRepository.deleteByEntity(customerId, EntityType.CUSTOMER);
        result.addStep("Field values deleted");
        
        // 3. Delete or anonymize related data
        anonymizeRelatedData(customerId);
        result.addStep("Related data anonymized");
        
        // 4. Mark customer as deleted
        customer.setStatus(CustomerStatus.DELETED);
        customer.setDeletedAt(LocalDateTime.now());
        customer.setDeletionReason(request.getReason());
        
        // 5. Fire deletion event
        deletionEvent.fire(new CustomerDeletionEvent(customerId));
        
        // 6. Create deletion certificate
        String certificate = createDeletionCertificate(customerId, result);
        result.setCertificate(certificate);
        
        return result;
    }
    
    private void anonymizePersonalData(Customer customer) {
        // Replace personal data with anonymized values
        Map<String, Object> fieldValues = fieldValueService
            .getFieldValuesAsMap(customer.getId(), EntityType.CUSTOMER);
            
        for (String fieldKey : fieldValues.keySet()) {
            if (isPersonalDataField(fieldKey)) {
                fieldValueService.updateFieldValue(
                    customer.getId(),
                    fieldKey,
                    generateAnonymizedValue(fieldKey)
                );
            }
        }
    }
    
    private String generateAnonymizedValue(String fieldKey) {
        return switch (fieldKey) {
            case "contactName" -> "DELETED_USER_" + UUID.randomUUID().toString().substring(0, 8);
            case "contactEmail" -> "deleted@example.com";
            case "contactPhone" -> "000000000";
            default -> "ANONYMIZED";
        };
    }
}
```

---

## Datenlöschung

### Automatische Löschfristen

```java
@ApplicationScoped
@Scheduled(every = "24h")
public class DataRetentionService {
    
    @Inject
    CustomerRepository customerRepository;
    
    @ConfigProperty(name = "gdpr.retention.draft-days")
    int draftRetentionDays;
    
    @ConfigProperty(name = "gdpr.retention.inactive-years")
    int inactiveRetentionYears;
    
    @Transactional
    void enforceRetentionPolicies() {
        // Delete old drafts
        LocalDateTime draftCutoff = LocalDateTime.now()
            .minusDays(draftRetentionDays);
        
        List<Customer> oldDrafts = customerRepository
            .find("status = ?1 AND createdAt < ?2", 
                CustomerStatus.DRAFT, 
                draftCutoff)
            .list();
            
        for (Customer draft : oldDrafts) {
            deletionService.deleteCustomer(
                draft.getId(),
                DeletionRequest.automatic("Draft retention policy")
            );
        }
        
        // Mark inactive customers for review
        LocalDateTime inactiveCutoff = LocalDateTime.now()
            .minusYears(inactiveRetentionYears);
            
        List<Customer> inactiveCustomers = customerRepository
            .find("lastActivityDate < ?1 AND status = ?2",
                inactiveCutoff,
                CustomerStatus.ACTIVE)
            .list();
            
        for (Customer customer : inactiveCustomers) {
            markForDeletionReview(customer);
        }
    }
}
```

### Löschkonzept

```yaml
# Löschfristen-Konfiguration
gdpr:
  retention:
    # Drafts
    draft-days: 30
    
    # Inaktive Kunden
    inactive-years: 6
    
    # Nach Vertragsende
    post-contract-years: 10
    
    # Besondere Kategorien
    marketing-consent-months: 24
    
  deletion:
    # Soft Delete für 30 Tage (Wiederherstellung möglich)
    soft-delete-days: 30
    
    # Danach Hard Delete
    anonymize-personal-data: true
    keep-statistical-data: true
    
  audit:
    # Audit Logs
    access-log-retention-days: 365
    deletion-log-retention-years: 10
```

---

## Security Checkliste

### Entwicklung
- [ ] Alle personenbezogenen Felder identifiziert
- [ ] Verschlüsselung für sensitive Daten implementiert
- [ ] Zugriffsberechtigungen definiert
- [ ] Audit-Logging implementiert
- [ ] Löschkonzept umgesetzt

### Deployment
- [ ] HTTPS erzwungen
- [ ] Security Headers konfiguriert
- [ ] Backup-Verschlüsselung aktiv
- [ ] Monitoring für Datenzugriffe
- [ ] Incident Response Plan

### Dokumentation
- [ ] Verarbeitungsverzeichnis aktualisiert
- [ ] Datenschutzerklärung angepasst
- [ ] Technische Dokumentation komplett
- [ ] Schulungsunterlagen erstellt
- [ ] Notfallplan dokumentiert