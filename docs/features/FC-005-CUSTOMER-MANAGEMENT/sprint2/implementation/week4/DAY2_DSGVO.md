# 📆 Tag 2: FC-018 DSGVO Integration

**Datum:** Dienstag, 27. August 2025  
**Fokus:** DSGVO Modul Integration  
**Ziel:** Datenschutz-Compliance sicherstellen  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 1: Audit](./DAY1_AUDIT.md)  
**↑ Woche 4 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 3: Performance](./DAY3_PERFORMANCE.md)  
**📘 Spec:** [DSGVO Specification](./specs/DSGVO_SPEC.md)  
**🔗 FC-018:** [DSGVO Module](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-018-dsgvo.md)  

## 🎯 Tagesziel

- Backend: DSGVO Integration Service
- Events: Data Subject Request Handler
- Frontend: Privacy Dashboard
- Automation: Request Workflows

## 🔒 DSGVO Architecture

```
User Request → DSGVO Service → Contact Management
      │                              │
      │                     ┌────────┴────────┐
      │                     │ Data Processing │
      │                     └────────┬────────┘
      │                              │
      └─────── Response ←────────────┘
```

## 💻 Backend Implementation

### 1. DSGVO Integration Service

```java
// DSGVOIntegrationService.java
@ApplicationScoped
public class DSGVOIntegrationService {
    
    @Inject
    @Channel("dsgvo-requests")
    Emitter<DataSubjectRequest> requestEmitter;
    
    @Inject
    ContactQueryService contactService;
    
    @Inject
    ConsentService consentService;
    
    // Handle data subject access requests
    public CompletionStage<DataSubjectResponse> handleAccessRequest(UUID contactId) {
        return CompletableFuture.supplyAsync(() -> {
            // Collect all contact data
            ContactProjection contact = contactService.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));
            
            // Get all events
            List<BaseEvent> events = eventStore.getEvents(contactId);
            
            // Get consent history
            List<ConsentEvent> consents = consentService.getConsentHistory(contactId);
            
            // Build comprehensive data package
            return DataSubjectResponse.builder()
                .requestId(UUID.randomUUID())
                .contactId(contactId)
                .personalData(extractPersonalData(contact))
                .communicationHistory(extractCommunications(events))
                .consentHistory(mapConsentHistory(consents))
                .processingActivities(getProcessingActivities(contact))
                .dataSharing(getDataSharingInfo(contact))
                .retentionInfo(getRetentionPolicy(contact))
                .exportFormat(ExportFormat.JSON)
                .generatedAt(Instant.now())
                .build();
        });
    }
    
    // Handle data deletion requests
    public CompletionStage<DeletionResult> handleDeletionRequest(
            UUID contactId, 
            DeletionScope scope) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Verify no legal obligations prevent deletion
            if (hasLegalHold(contactId)) {
                throw new DeletionBlockedException("Legal hold prevents deletion");
            }
            
            switch (scope) {
                case FULL_DELETE -> performFullDeletion(contactId);
                case ANONYMIZE -> performAnonymization(contactId);
                case CRYPTO_SHRED -> performCryptoShredding(contactId);
            }
            
            // Log deletion for compliance
            auditService.logDeletion(contactId, scope);
            
            return DeletionResult.builder()
                .contactId(contactId)
                .scope(scope)
                .deletedAt(Instant.now())
                .certificateId(generateDeletionCertificate())
                .build();
        });
    }
}
```

**Vollständiger Code:** [backend/DSGVOIntegrationService.java](./code/backend/DSGVOIntegrationService.java)

### 2. Data Portability Service

```java
// DataPortabilityService.java
@ApplicationScoped
public class DataPortabilityService {
    
    public byte[] exportContactData(UUID contactId, ExportFormat format) {
        DataSubjectResponse data = dsgvoService.handleAccessRequest(contactId)
            .toCompletableFuture().join();
        
        return switch (format) {
            case JSON -> exportAsJson(data);
            case CSV -> exportAsCsv(data);
            case XML -> exportAsXml(data);
            case PDF -> exportAsPdf(data);
        };
    }
    
    private byte[] exportAsJson(DataSubjectResponse data) {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);
        
        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new ExportException("Failed to export as JSON", e);
        }
    }
    
    private byte[] exportAsPdf(DataSubjectResponse data) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Header
            document.add(new Paragraph("Datenauskunft gemäß Art. 15 DSGVO"));
            document.add(new Paragraph("Erstellt am: " + 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            
            // Personal Data
            document.add(new Chapter("Personenbezogene Daten", 1));
            addPersonalDataSection(document, data.getPersonalData());
            
            // Communication History
            document.add(new Chapter("Kommunikationshistorie", 2));
            addCommunicationSection(document, data.getCommunicationHistory());
            
            // Consent History
            document.add(new Chapter("Einwilligungen", 3));
            addConsentSection(document, data.getConsentHistory());
            
            document.close();
            return baos.toByteArray();
            
        } catch (DocumentException e) {
            throw new ExportException("Failed to export as PDF", e);
        }
    }
}
```

### 3. Retention Policy Engine

```java
// RetentionPolicyEngine.java
@ApplicationScoped
public class RetentionPolicyEngine {
    
    @Scheduled(every = "24h")
    void enforceRetentionPolicies() {
        // Find contacts eligible for deletion
        List<ContactProjection> expiredContacts = contactRepository
            .findByRetentionExpired();
        
        for (ContactProjection contact : expiredContacts) {
            if (canDelete(contact)) {
                // Check for active consents that override retention
                if (!hasActiveConsents(contact.getId())) {
                    dsgvoService.handleDeletionRequest(
                        contact.getId(), 
                        DeletionScope.ANONYMIZE
                    );
                }
            }
        }
    }
    
    private boolean canDelete(ContactProjection contact) {
        // Check business rules
        if (contact.hasActiveContracts()) return false;
        if (contact.hasOpenInvoices()) return false;
        if (contact.isUnderLegalHold()) return false;
        
        // Check retention period
        Duration retentionPeriod = getRetentionPeriod(contact);
        Instant deletionEligible = contact.getLastActivity()
            .plus(retentionPeriod);
        
        return Instant.now().isAfter(deletionEligible);
    }
}
```

## 🎨 Frontend Implementation

### Privacy Dashboard Component

```typescript
// components/privacy/PrivacyDashboard.tsx
export const PrivacyDashboard: React.FC<PrivacyDashboardProps> = ({
  contactId
}) => {
  const [activeTab, setActiveTab] = useState(0);
  const { data: privacyData, loading } = usePrivacyData(contactId);
  
  const handleDataRequest = async (type: RequestType) => {
    const result = await privacyApi.createDataRequest({
      contactId,
      type,
      reason: 'User requested'
    });
    
    if (type === 'ACCESS') {
      // Download the data
      downloadFile(result.downloadUrl, `data-export-${contactId}.json`);
    }
  };
  
  if (loading) return <CircularProgress />;
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h5" gutterBottom>
          <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
          Datenschutz & Privatsphäre
        </Typography>
        
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          <Tab label="Einwilligungen" />
          <Tab label="Datenübersicht" />
          <Tab label="Betroffenenrechte" />
          <Tab label="Aufbewahrung" />
        </Tabs>
        
        {activeTab === 0 && (
          <ConsentManagementTab 
            consents={privacyData.consents}
            onUpdate={handleConsentUpdate}
          />
        )}
        
        {activeTab === 1 && (
          <DataOverviewTab 
            dataCategories={privacyData.dataCategories}
            processingActivities={privacyData.processingActivities}
          />
        )}
        
        {activeTab === 2 && (
          <DataSubjectRightsTab 
            onAccessRequest={() => handleDataRequest('ACCESS')}
            onPortabilityRequest={() => handleDataRequest('PORTABILITY')}
            onDeletionRequest={() => handleDataRequest('DELETION')}
            onRectificationRequest={() => handleDataRequest('RECTIFICATION')}
          />
        )}
        
        {activeTab === 3 && (
          <RetentionPolicyTab 
            retentionInfo={privacyData.retentionInfo}
            deletionSchedule={privacyData.deletionSchedule}
          />
        )}
      </CardContent>
    </Card>
  );
};
```

**Vollständiger Code:** [frontend/PrivacyDashboard.tsx](./code/frontend/privacy/PrivacyDashboard.tsx)

### Data Subject Rights Tab

```typescript
// components/privacy/DataSubjectRightsTab.tsx
export const DataSubjectRightsTab: React.FC<DataSubjectRightsProps> = ({
  onAccessRequest,
  onPortabilityRequest,
  onDeletionRequest,
  onRectificationRequest
}) => {
  const [confirmDialog, setConfirmDialog] = useState<string | null>(null);
  
  const rights = [
    {
      title: 'Auskunftsrecht',
      description: 'Erhalten Sie eine Kopie aller gespeicherten Daten',
      icon: <InfoIcon />,
      action: onAccessRequest,
      article: 'Art. 15 DSGVO'
    },
    {
      title: 'Datenübertragbarkeit',
      description: 'Exportieren Sie Ihre Daten in einem maschinenlesbaren Format',
      icon: <CloudDownloadIcon />,
      action: onPortabilityRequest,
      article: 'Art. 20 DSGVO'
    },
    {
      title: 'Löschung',
      description: 'Fordern Sie die Löschung Ihrer personenbezogenen Daten',
      icon: <DeleteIcon />,
      action: () => setConfirmDialog('deletion'),
      article: 'Art. 17 DSGVO',
      destructive: true
    },
    {
      title: 'Berichtigung',
      description: 'Korrigieren Sie unrichtige Daten',
      icon: <EditIcon />,
      action: onRectificationRequest,
      article: 'Art. 16 DSGVO'
    }
  ];
  
  return (
    <Box sx={{ mt: 2 }}>
      <Grid container spacing={2}>
        {rights.map((right) => (
          <Grid item xs={12} md={6} key={right.title}>
            <Card variant="outlined">
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                  {right.icon}
                  <Typography variant="h6" sx={{ ml: 1 }}>
                    {right.title}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" paragraph>
                  {right.description}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {right.article}
                </Typography>
              </CardContent>
              <CardActions>
                <Button
                  onClick={right.action}
                  color={right.destructive ? 'error' : 'primary'}
                  variant={right.destructive ? 'outlined' : 'contained'}
                >
                  Anfordern
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
      
      <ConfirmationDialog
        open={confirmDialog === 'deletion'}
        onClose={() => setConfirmDialog(null)}
        onConfirm={() => {
          onDeletionRequest();
          setConfirmDialog(null);
        }}
        title="Löschung bestätigen"
        message="Sind Sie sicher, dass Sie alle Ihre Daten löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden."
        confirmText="Ja, alle Daten löschen"
        confirmColor="error"
      />
    </Box>
  );
};
```

## 🧪 Tests

### DSGVO Integration Test

```java
@Test
void shouldExportAllContactData() {
    // Given
    ContactProjection contact = createTestContactWithHistory();
    
    // When
    DataSubjectResponse response = dsgvoService
        .handleAccessRequest(contact.getId())
        .toCompletableFuture().join();
    
    // Then
    assertThat(response.getPersonalData()).isNotEmpty();
    assertThat(response.getCommunicationHistory()).hasSize(5);
    assertThat(response.getConsentHistory()).isNotEmpty();
}

@Test
void shouldAnonymizeContactData() {
    // Given
    ContactProjection contact = createTestContact();
    
    // When
    DeletionResult result = dsgvoService
        .handleDeletionRequest(contact.getId(), DeletionScope.ANONYMIZE)
        .toCompletableFuture().join();
    
    // Then
    ContactProjection anonymized = contactRepository.findById(contact.getId()).get();
    assertThat(anonymized.getFirstName()).isEqualTo("[ANONYMIZED]");
    assertThat(anonymized.getEmail()).startsWith("anon-");
}
```

## 📝 Checkliste

- [ ] DSGVO Integration Service implementiert
- [ ] Data Export in allen Formaten
- [ ] Deletion Workflows erstellt
- [ ] Privacy Dashboard UI
- [ ] Rights Management UI
- [ ] Retention Engine läuft
- [ ] Compliance Tests grün

## 🔗 Weiterführende Links

- **DSGVO Guide:** [GDPR Compliance Guide](./guides/GDPR_COMPLIANCE.md)
- **FC-018 Details:** [DSGVO Module](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-018-dsgvo.md)
- **Nächster Schritt:** [→ Tag 3: Performance Optimization](./DAY3_PERFORMANCE.md)

---

**Status:** 📋 Bereit zur Implementierung