# 📜 FC-005 SECURITY - DSGVO COMPLIANCE

**Navigation:**
- **Parent:** [06-SECURITY](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/README.md)
- **Prev:** [README](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/README.md)
- **Next:** [02-encryption.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/02-encryption.md)

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
  },
  {
    fieldKey: 'creditLimit',
    sensitivity: DataSensitivity.HIGHLY_CONFIDENTIAL,
    personalData: false,
    specialCategory: false,
    retentionPeriod: 3650,
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

public enum ConsentType {
    MARKETING,
    DATA_PROCESSING,
    NEWSLETTER,
    PROFILING,
    THIRD_PARTY_SHARING
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
    
    private Set<String> getRequiredFieldsForPurpose(DataPurpose purpose) {
        return switch (purpose) {
            case BASIC_REGISTRATION -> Set.of("companyName", "contactEmail");
            case CONTRACT_CREATION -> Set.of(
                "companyName", "legalForm", "street", 
                "postalCode", "city", "contactName", 
                "contactEmail", "contactPhone"
            );
            case FINANCIAL_ASSESSMENT -> Set.of(
                "companyName", "legalForm", "creditLimit",
                "paymentTerms", "taxId"
            );
            default -> Set.of("companyName");
        };
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

## DSGVO-Dokumentation

### Verarbeitungsverzeichnis

```yaml
# Verarbeitungstätigkeit: Customer Management
name: "Kundenverwaltung FreshPlan"
responsible: "FreshPlan GmbH"
purpose:
  - "Verwaltung von Kundenbeziehungen"
  - "Vertragsverwaltung"
  - "Kommunikation mit Kunden"
categories_of_data:
  - "Stammdaten (Name, Adresse)"
  - "Kontaktdaten (Email, Telefon)"
  - "Vertragsdaten"
  - "Kommunikationshistorie"
categories_of_persons:
  - "Geschäftskunden"
  - "Ansprechpartner bei Kunden"
recipients:
  - "Interne Mitarbeiter (nach Berechtigung)"
  - "Steuerberater (bei Bedarf)"
third_countries: false
deletion_periods:
  - "Kundendaten: 10 Jahre nach Vertragsende"
  - "Kommunikation: 6 Jahre"
  - "Logs: 1 Jahr"
technical_measures:
  - "Verschlüsselung at-rest und in-transit"
  - "Zugriffsberechtigungen"
  - "Audit-Logging"
  - "Regelmäßige Backups"
```