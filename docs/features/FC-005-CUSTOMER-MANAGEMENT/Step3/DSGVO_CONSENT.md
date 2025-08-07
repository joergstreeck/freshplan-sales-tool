# 🔒 DSGVO Consent Management - Datenschutz-konforme Kontaktverwaltung

**Phase:** 3 - Compliance & Ethics  
**Tag:** 1 der Woche 3  
**Status:** 📋 Specification Ready  

## 🧭 Navigation

**← Zurück:** [Performance Optimization](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/PERFORMANCE_OPTIMIZATION.md)  
**→ Nächster:** [Contact Analytics](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/CONTACT_ANALYTICS.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: Privacy by Design

**DSGVO Consent Management** macht Datenschutz **transparent und benutzerfreundlich**:

> "Vertrauen durch Transparenz - jeder Kontakt behält die Kontrolle über seine Daten"

### 💬 Team-Feedback:
> "Kritisch wichtig und zeitgemäß. Vollständig compliant und wettbewerbsfähig. Audit-Trail und Granularität von Anfang an."

## 📋 Consent Data Model

### Enhanced Contact Entity

```java
// Contact.java - DSGVO-erweiterte Felder
@Entity
@Table(name = "contacts")
public class Contact extends PanacheEntityBase {
    // ... existing fields ...
    
    // DSGVO Consent Fields
    @Column(name = "consent_given")
    private Boolean consentGiven = false;
    
    @Column(name = "consent_date")
    private LocalDateTime consentDate;
    
    @Column(name = "consent_scope")
    @Convert(converter = ConsentScopeConverter.class)
    private Set<ConsentScope> consentScopes = new HashSet<>();
    
    @Column(name = "consent_version")
    private String consentVersion;
    
    @Column(name = "consent_ip_address")
    private String consentIpAddress;
    
    @Column(name = "data_retention_period")
    private Integer dataRetentionPeriodDays = 730; // 2 Jahre default
    
    @Column(name = "deletion_requested")
    private Boolean deletionRequested = false;
    
    @Column(name = "deletion_request_date")
    private LocalDateTime deletionRequestDate;
    
    @Column(name = "anonymized")
    private Boolean anonymized = false;
    
    @Column(name = "last_consent_review")
    private LocalDateTime lastConsentReview;
}
```

### Consent Scope Enum

```java
// ConsentScope.java
public enum ConsentScope {
    BASIC_CONTACT("Grundlegende Kontaktdaten", true),
    MARKETING_EMAIL("Marketing E-Mails", false),
    MARKETING_PHONE("Telefonmarketing", false),
    ANALYTICS("Analyse der Geschäftsbeziehung", false),
    RELATIONSHIP_TRACKING("Beziehungs-Tracking", false),
    LOCATION_DATA("Standortdaten", false),
    PURCHASE_HISTORY("Kaufhistorie", true),
    PERSONAL_NOTES("Persönliche Notizen", false),
    BIRTHDAY_REMINDERS("Geburtstags-Erinnerungen", false),
    THIRD_PARTY_SHARING("Weitergabe an Dritte", false);
    
    private final String description;
    private final boolean requiredForBusiness;
    
    ConsentScope(String description, boolean requiredForBusiness) {
        this.description = description;
        this.requiredForBusiness = requiredForBusiness;
    }
}
```

## 🎨 Frontend Consent Components

### Consent Management Dialog

```typescript
// components/ConsentManagementDialog.tsx
import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Typography,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Button,
  Alert,
  Box,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Chip,
  Link,
  Paper,
  Divider
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Security as SecurityIcon,
  Info as InfoIcon,
  Warning as WarningIcon,
  CheckCircle as CheckIcon
} from '@mui/icons-material';

export const ConsentManagementDialog: React.FC<{
  contact: Contact;
  open: boolean;
  onClose: () => void;
  onUpdate: (consents: ConsentUpdate) => void;
}> = ({ contact, open, onClose, onUpdate }) => {
  const [consents, setConsents] = useState<ConsentState>({});
  const [expanded, setExpanded] = useState<string | false>('basic');
  
  useEffect(() => {
    // Initialize from contact's current consents
    const currentConsents: ConsentState = {};
    contact.consentScopes?.forEach(scope => {
      currentConsents[scope] = true;
    });
    setConsents(currentConsents);
  }, [contact]);
  
  const consentCategories = [
    {
      id: 'basic',
      title: 'Grundlegende Geschäftsdaten',
      icon: <SecurityIcon />,
      required: true,
      scopes: [
        ConsentScope.BASIC_CONTACT,
        ConsentScope.PURCHASE_HISTORY
      ]
    },
    {
      id: 'marketing',
      title: 'Marketing & Kommunikation',
      icon: <EmailIcon />,
      required: false,
      scopes: [
        ConsentScope.MARKETING_EMAIL,
        ConsentScope.MARKETING_PHONE,
        ConsentScope.BIRTHDAY_REMINDERS
      ]
    },
    {
      id: 'analytics',
      title: 'Analyse & Verbesserung',
      icon: <AnalyticsIcon />,
      required: false,
      scopes: [
        ConsentScope.ANALYTICS,
        ConsentScope.RELATIONSHIP_TRACKING,
        ConsentScope.LOCATION_DATA
      ]
    },
    {
      id: 'personal',
      title: 'Persönliche Daten',
      icon: <PersonIcon />,
      required: false,
      scopes: [
        ConsentScope.PERSONAL_NOTES,
        ConsentScope.THIRD_PARTY_SHARING
      ]
    }
  ];
  
  const handleConsentToggle = (scope: ConsentScope) => {
    const scopeInfo = ConsentScopeInfo[scope];
    
    // Prevent disabling required scopes
    if (scopeInfo.requiredForBusiness && consents[scope]) {
      return;
    }
    
    setConsents(prev => ({
      ...prev,
      [scope]: !prev[scope]
    }));
  };
  
  const handleSave = () => {
    const enabledScopes = Object.entries(consents)
      .filter(([_, enabled]) => enabled)
      .map(([scope, _]) => scope as ConsentScope);
    
    onUpdate({
      scopes: enabledScopes,
      version: CURRENT_CONSENT_VERSION,
      timestamp: new Date().toISOString()
    });
  };
  
  const allRequiredConsentsGiven = () => {
    return Object.values(ConsentScope)
      .filter(scope => ConsentScopeInfo[scope].requiredForBusiness)
      .every(scope => consents[scope]);
  };
  
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: { minHeight: '80vh' }
      }}
    >
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <SecurityIcon color="primary" />
          <Typography variant="h6">
            Datenschutz-Einstellungen für {contact.firstName} {contact.lastName}
          </Typography>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {/* Info Banner */}
        <Alert severity="info" sx={{ mb: 3 }}>
          <Typography variant="body2">
            Hier können Sie festlegen, welche Daten wir speichern und wie wir sie verwenden dürfen.
            Einige Daten sind für die Geschäftsbeziehung notwendig und können nicht deaktiviert werden.
          </Typography>
        </Alert>
        
        {/* Consent Categories */}
        {consentCategories.map(category => (
          <Accordion
            key={category.id}
            expanded={expanded === category.id}
            onChange={(_, isExpanded) => setExpanded(isExpanded ? category.id : false)}
            sx={{ mb: 1 }}
          >
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Box display="flex" alignItems="center" gap={2} width="100%">
                {category.icon}
                <Typography sx={{ flexGrow: 1 }}>
                  {category.title}
                </Typography>
                {category.required && (
                  <Chip
                    label="Erforderlich"
                    size="small"
                    color="primary"
                  />
                )}
                {category.scopes.every(s => consents[s]) && (
                  <CheckIcon color="success" />
                )}
              </Box>
            </AccordionSummary>
            
            <AccordionDetails>
              <FormGroup>
                {category.scopes.map(scope => {
                  const scopeInfo = ConsentScopeInfo[scope];
                  const isRequired = scopeInfo.requiredForBusiness;
                  const isEnabled = consents[scope] || false;
                  
                  return (
                    <Box key={scope} mb={2}>
                      <FormControlLabel
                        control={
                          <Checkbox
                            checked={isEnabled}
                            onChange={() => handleConsentToggle(scope)}
                            disabled={isRequired && isEnabled}
                          />
                        }
                        label={
                          <Box>
                            <Typography variant="body2" fontWeight="medium">
                              {scopeInfo.description}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {scopeInfo.details}
                            </Typography>
                          </Box>
                        }
                      />
                      {isRequired && (
                        <Typography variant="caption" color="primary" sx={{ ml: 4 }}>
                          * Für Geschäftsbeziehung erforderlich
                        </Typography>
                      )}
                    </Box>
                  );
                })}
              </FormGroup>
            </AccordionDetails>
          </Accordion>
        ))}
        
        {/* Data Retention Info */}
        <Paper sx={{ p: 2, mt: 3, bgcolor: 'grey.50' }}>
          <Typography variant="subtitle2" gutterBottom>
            <InfoIcon sx={{ fontSize: 16, mr: 1, verticalAlign: 'text-bottom' }} />
            Datenspeicherung & Ihre Rechte
          </Typography>
          <Typography variant="body2" paragraph>
            Ihre Daten werden für {contact.dataRetentionPeriodDays || 730} Tage 
            (ca. {Math.round((contact.dataRetentionPeriodDays || 730) / 365)} Jahre) gespeichert.
          </Typography>
          <Typography variant="body2">
            Sie haben jederzeit das Recht auf:
          </Typography>
          <Box component="ul" sx={{ mt: 1, pl: 3 }}>
            <li>Auskunft über Ihre gespeicherten Daten</li>
            <li>Berichtigung unrichtiger Daten</li>
            <li>Löschung Ihrer Daten (Recht auf Vergessenwerden)</li>
            <li>Einschränkung der Verarbeitung</li>
            <li>Datenübertragbarkeit</li>
          </Box>
          <Link href="/datenschutz" target="_blank" sx={{ mt: 1, display: 'inline-block' }}>
            Vollständige Datenschutzerklärung →
          </Link>
        </Paper>
        
        {/* Last Update Info */}
        {contact.consentDate && (
          <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
            Letzte Aktualisierung: {new Date(contact.consentDate).toLocaleDateString('de-DE')}
            {contact.consentVersion && ` (Version ${contact.consentVersion})`}
          </Typography>
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Abbrechen
        </Button>
        <Button
          variant="contained"
          onClick={handleSave}
          disabled={!allRequiredConsentsGiven()}
          startIcon={<SecurityIcon />}
        >
          Einstellungen speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### Consent Status Indicator

```typescript
// components/ConsentStatusIndicator.tsx
export const ConsentStatusIndicator: React.FC<{
  contact: Contact;
  onManage: () => void;
}> = ({ contact, onManage }) => {
  const getConsentStatus = (): ConsentStatus => {
    if (!contact.consentGiven) return 'none';
    if (contact.deletionRequested) return 'deletion-requested';
    
    const daysSinceConsent = Math.floor(
      (Date.now() - new Date(contact.consentDate).getTime()) / (1000 * 60 * 60 * 24)
    );
    
    if (daysSinceConsent > 365) return 'review-needed';
    if (contact.consentScopes.length < 3) return 'partial';
    return 'complete';
  };
  
  const status = getConsentStatus();
  const statusConfig = {
    'none': {
      color: 'error' as const,
      icon: <WarningIcon />,
      label: 'Keine Einwilligung'
    },
    'partial': {
      color: 'warning' as const,
      icon: <InfoIcon />,
      label: 'Teilweise Einwilligung'
    },
    'complete': {
      color: 'success' as const,
      icon: <CheckIcon />,
      label: 'Vollständige Einwilligung'
    },
    'review-needed': {
      color: 'warning' as const,
      icon: <RefreshIcon />,
      label: 'Überprüfung erforderlich'
    },
    'deletion-requested': {
      color: 'error' as const,
      icon: <DeleteIcon />,
      label: 'Löschung beantragt'
    }
  };
  
  const config = statusConfig[status];
  
  return (
    <Chip
      icon={config.icon}
      label={config.label}
      color={config.color}
      size="small"
      onClick={onManage}
      sx={{ cursor: 'pointer' }}
    />
  );
};
```

## 🔧 Backend DSGVO Services

### Consent Management Service

```java
// ConsentManagementService.java
@ApplicationScoped
@Transactional
public class ConsentManagementService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    ConsentAuditService auditService;
    
    @Inject
    DataAnonymizationService anonymizationService;
    
    @ConfigProperty(name = "dsgvo.consent.version")
    String currentConsentVersion;
    
    /**
     * Update consent for a contact
     */
    public ContactResponse updateConsent(
        UUID contactId,
        ConsentUpdateRequest request,
        String ipAddress
    ) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found"));
        
        // Audit trail
        auditService.logConsentChange(
            contactId,
            contact.getConsentScopes(),
            request.getScopes(),
            ipAddress
        );
        
        // Update consent
        contact.setConsentGiven(!request.getScopes().isEmpty());
        contact.setConsentDate(LocalDateTime.now());
        contact.setConsentScopes(new HashSet<>(request.getScopes()));
        contact.setConsentVersion(currentConsentVersion);
        contact.setConsentIpAddress(ipAddress);
        contact.setLastConsentReview(LocalDateTime.now());
        
        // Reset deletion request if new consent given
        if (!request.getScopes().isEmpty() && contact.getDeletionRequested()) {
            contact.setDeletionRequested(false);
            contact.setDeletionRequestDate(null);
        }
        
        contactRepository.persist(contact);
        
        return ContactMapper.toResponse(contact);
    }
    
    /**
     * Request data deletion (Right to be forgotten)
     */
    public void requestDeletion(UUID contactId, String reason) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found"));
        
        contact.setDeletionRequested(true);
        contact.setDeletionRequestDate(LocalDateTime.now());
        
        // Log deletion request
        auditService.logDeletionRequest(contactId, reason);
        
        // Schedule deletion after grace period
        scheduleDeletion(contactId, 30); // 30 days grace period
    }
    
    /**
     * Export personal data (Right to data portability)
     */
    public PersonalDataExport exportPersonalData(UUID contactId) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found"));
        
        // Collect all personal data
        PersonalDataExport export = new PersonalDataExport();
        export.setExportDate(LocalDateTime.now());
        export.setContactData(ContactMapper.toFullExport(contact));
        
        // Add interaction history
        export.setInteractionHistory(
            interactionRepository.findByContactId(contactId)
                .stream()
                .map(InteractionMapper::toExport)
                .collect(Collectors.toList())
        );
        
        // Add relationship data
        export.setRelationshipData(
            relationshipService.getRelationshipData(contactId)
        );
        
        // Audit the export
        auditService.logDataExport(contactId);
        
        return export;
    }
    
    /**
     * Anonymize contact data after retention period
     */
    @Scheduled(every = "24h")
    void processDataRetention() {
        // Find contacts exceeding retention period
        LocalDateTime retentionThreshold = LocalDateTime.now()
            .minusDays(730); // Default 2 years
        
        List<Contact> contactsToAnonymize = contactRepository
            .find("isActive = true and createdAt < ?1 and anonymized = false", 
                  retentionThreshold)
            .list();
        
        for (Contact contact : contactsToAnonymize) {
            // Check if custom retention period
            int retentionDays = contact.getDataRetentionPeriodDays() != null 
                ? contact.getDataRetentionPeriodDays() 
                : 730;
            
            LocalDateTime customThreshold = LocalDateTime.now()
                .minusDays(retentionDays);
            
            if (contact.getCreatedAt().isBefore(customThreshold)) {
                anonymizeContact(contact);
            }
        }
    }
    
    private void anonymizeContact(Contact contact) {
        // Preserve business-critical data in anonymized form
        contact.setFirstName("ANONYM");
        contact.setLastName("ANONYM-" + contact.getId().toString().substring(0, 8));
        contact.setEmail(null);
        contact.setPhone(null);
        contact.setMobile(null);
        contact.setBirthday(null);
        contact.setHobbies(null);
        contact.setFamilyStatus(null);
        contact.setPersonalNotes(null);
        contact.setAnonymized(true);
        
        // Keep business data for statistics
        // - assignedLocationId (for coverage analysis)
        // - position (for role analysis)
        // - isPrimary (for structure analysis)
        
        contactRepository.persist(contact);
        auditService.logAnonymization(contact.getId());
    }
}
```

### Consent Audit Service

```java
// ConsentAuditService.java
@ApplicationScoped
public class ConsentAuditService {
    
    @Inject
    EntityManager em;
    
    public void logConsentChange(
        UUID contactId,
        Set<ConsentScope> oldScopes,
        Set<ConsentScope> newScopes,
        String ipAddress
    ) {
        ConsentAuditLog log = new ConsentAuditLog();
        log.setContactId(contactId);
        log.setTimestamp(LocalDateTime.now());
        log.setAction(ConsentAction.CONSENT_UPDATED);
        log.setOldScopes(oldScopes);
        log.setNewScopes(newScopes);
        log.setIpAddress(ipAddress);
        log.setUserId(SecurityContext.getCurrentUserId());
        
        // Calculate changes
        Set<ConsentScope> added = new HashSet<>(newScopes);
        added.removeAll(oldScopes);
        log.setAddedScopes(added);
        
        Set<ConsentScope> removed = new HashSet<>(oldScopes);
        removed.removeAll(newScopes);
        log.setRemovedScopes(removed);
        
        em.persist(log);
    }
    
    public List<ConsentAuditLog> getConsentHistory(UUID contactId) {
        return em.createQuery(
            "SELECT log FROM ConsentAuditLog log " +
            "WHERE log.contactId = :contactId " +
            "ORDER BY log.timestamp DESC",
            ConsentAuditLog.class
        )
        .setParameter("contactId", contactId)
        .getResultList();
    }
}
```

## 🧪 Testing DSGVO Compliance

```typescript
// __tests__/dsgvo-compliance.test.ts
describe('DSGVO Compliance', () => {
  it('should require basic consents for business operations', async () => {
    const contact = createMockContact({ consentScopes: [] });
    
    render(<ConsentManagementDialog contact={contact} open={true} />);
    
    // Try to save without required consents
    const saveButton = screen.getByText('Einstellungen speichern');
    expect(saveButton).toBeDisabled();
    
    // Enable required consents
    fireEvent.click(screen.getByLabelText('Grundlegende Kontaktdaten'));
    fireEvent.click(screen.getByLabelText('Kaufhistorie'));
    
    expect(saveButton).toBeEnabled();
  });
  
  it('should handle deletion request correctly', async () => {
    const contact = createMockContact();
    const onDelete = jest.fn();
    
    render(<ContactActions contact={contact} onDelete={onDelete} />);
    
    fireEvent.click(screen.getByText('Daten löschen'));
    fireEvent.click(screen.getByText('Löschung bestätigen'));
    
    expect(onDelete).toHaveBeenCalledWith(
      contact.id,
      expect.objectContaining({
        reason: expect.any(String),
        confirmDeletion: true
      })
    );
  });
  
  it('should export personal data in portable format', async () => {
    const { result } = renderHook(() => usePersonalDataExport());
    
    act(() => {
      result.current.exportData('contact-123');
    });
    
    await waitFor(() => {
      expect(result.current.exportedData).toMatchObject({
        format: 'JSON',
        includesAllPersonalData: true,
        machineReadable: true
      });
    });
  });
});
```

## 🎯 Success Metrics

### Compliance:
- **Consent Rate:** > 90% active consents
- **Review Compliance:** 100% reviewed within 365 days
- **Deletion Processing:** < 30 days
- **Export Requests:** < 24h response time

### User Experience:
- **Consent Dialog Completion:** > 80%
- **Transparency Score:** 5/5 stars
- **Trust Index:** Increased by 25%

## 🤖 Consent-Lifecycle-Automation

### Automatische Consent-Verwaltung

```java
// ConsentAutomationService.java
@ApplicationScoped
public class ConsentAutomationService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    NotificationService notificationService;
    
    /**
     * Automatische Reminder vor Consent-Ablauf
     */
    @Scheduled(every = "24h")
    void checkConsentExpiry() {
        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        
        // Finde Kontakte deren Consent bald abläuft
        List<Contact> expiringConsents = contactRepository.find(
            "consentDate < ?1 and consentDate > ?2 and isActive = true",
            oneYearAgo.plusDays(30), // Consent älter als 335 Tage
            oneYearAgo.minusDays(30)  // aber nicht älter als 395 Tage
        ).list();
        
        for (Contact contact : expiringConsents) {
            // Sende Reminder an Kontakt
            notificationService.sendConsentRenewalReminder(contact);
            
            // Benachrichtige zuständigen Vertrieb
            notificationService.notifySalesAboutExpiringConsent(contact);
        }
    }
    
    /**
     * Auto-Renewal Eligibility Check
     */
    public boolean isEligibleForAutoRenewal(Contact contact) {
        // Prüfe ob alle Bedingungen für Auto-Renewal erfüllt sind
        return contact.getConsentGiven() &&
               !contact.getDeletionRequested() &&
               contact.getInteractionCount() > 5 &&
               contact.getLastInteraction().isAfter(LocalDateTime.now().minusMonths(3)) &&
               contact.getConsentScopes().contains(ConsentScope.MARKETING_EMAIL);
    }
    
    /**
     * Compliance Alert bei Verstößen
     */
    @Observes
    void onContactInteraction(ContactInteractionEvent event) {
        Contact contact = contactRepository.findById(event.getContactId());
        
        // Prüfe ob Interaktion ohne Consent
        if (!contact.getConsentGiven() && event.getInteractionType().requiresConsent()) {
            // Sofort Alert!
            ComplianceAlert alert = ComplianceAlert.builder()
                .severity(Severity.HIGH)
                .contactId(contact.getId())
                .message("Interaktion ohne gültiges Consent!")
                .actionRequired("Consent einholen oder Interaktion stoppen")
                .build();
                
            notificationService.sendComplianceAlert(alert);
            
            // Log für Audit
            auditService.logComplianceViolation(alert);
        }
    }
}
```

### Frontend Auto-Renewal Dialog

```typescript
// components/ConsentAutoRenewalDialog.tsx
export const ConsentAutoRenewalDialog: React.FC<{
  contact: Contact;
  open: boolean;
  onClose: () => void;
}> = ({ contact, open, onClose }) => {
  const [autoRenewalEnabled, setAutoRenewalEnabled] = useState(false);
  
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>
        Automatische Consent-Verlängerung
      </DialogTitle>
      <DialogContent>
        <Alert severity="info" sx={{ mb: 2 }}>
          Ihr Consent läuft in {getDaysUntilExpiry(contact)} Tagen ab.
          Möchten Sie die automatische Verlängerung aktivieren?
        </Alert>
        
        <FormControlLabel
          control={
            <Switch
              checked={autoRenewalEnabled}
              onChange={(e) => setAutoRenewalEnabled(e.target.checked)}
            />
          }
          label="Automatische Verlängerung aktivieren"
        />
        
        {autoRenewalEnabled && (
          <Typography variant="caption" color="text.secondary">
            Ihr Consent wird automatisch verlängert, wenn Sie weiterhin
            aktiv mit uns interagieren.
          </Typography>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Später</Button>
        <Button 
          variant="contained" 
          onClick={() => handleAutoRenewal(contact, autoRenewalEnabled)}
        >
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

---

**Nächster Schritt:** [→ Contact Analytics](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/CONTACT_ANALYTICS.md)

**Privacy First = Trust First! 🔒✨**