# 📅 Woche 2: Features + Compliance

**Sprint:** Sprint 2 - Contact Management  
**Woche:** 2 (12.-16. August 2025)  
**Fokus:** DSGVO Compliance + Mobile Features  

## 🎯 Wochenziel

Implementierung der DSGVO-Compliance-Features und mobiler Funktionalitäten. Am Ende der Woche haben wir:
- ✅ Consent Management implementiert
- ✅ Crypto-Shredding für Datenschutz
- ✅ Mobile Quick Actions
- ✅ Offline Queue Basis
- ✅ GDPR Export Funktionalität

## 📋 Tagesplan

### Montag: Consent Management Foundation

#### Backend: Consent Entity & Events
```java
// ConsentAggregate.java
@Entity
@Table(name = "consent_aggregates")
public class ConsentAggregate {
    @Id
    private UUID contactId;
    
    @ElementCollection
    @CollectionTable(name = "consents")
    private List<Consent> consents = new ArrayList<>();
    
    // Business Methods
    public void grantConsent(GrantConsentCommand cmd) {
        // Check if already granted
        if (hasActiveConsent(cmd.getConsentType())) {
            throw new ConsentAlreadyGrantedException(cmd.getConsentType());
        }
        
        apply(ConsentGrantedEvent.builder()
            .contactId(this.contactId)
            .consentType(cmd.getConsentType())
            .basis(cmd.getBasis())
            .expiresAt(calculateExpiry(cmd.getConsentType()))
            .metadata(buildMetadata())
            .build());
    }
    
    public void revokeConsent(RevokeConsentCommand cmd) {
        if (!hasActiveConsent(cmd.getConsentType())) {
            throw new ConsentNotFoundException(cmd.getConsentType());
        }
        
        apply(ConsentRevokedEvent.builder()
            .contactId(this.contactId)
            .consentType(cmd.getConsentType())
            .reason(cmd.getReason())
            .metadata(buildMetadata())
            .build());
    }
}

// Consent.java
@Embeddable
public class Consent {
    private ConsentType type;
    private ConsentBasis basis;
    private ConsentStatus status;
    private Instant grantedAt;
    private Instant revokedAt;
    private Instant expiresAt;
}
```

#### Frontend: Consent Dialog Component
```typescript
// components/ConsentDialog.tsx
export const ConsentDialog: React.FC<ConsentDialogProps> = ({
  contact,
  open,
  onClose,
  onSave
}) => {
  const [consents, setConsents] = useState<ConsentState>({
    marketing: false,
    personalData: false,
    communication: false
  });
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Datenschutz-Einwilligungen</DialogTitle>
      
      <DialogContent>
        <Alert severity="info" sx={{ mb: 2 }}>
          DSGVO-konforme Einwilligungsverwaltung für {contact.name}
        </Alert>
        
        <FormGroup>
          <FormControlLabel
            control={
              <Checkbox
                checked={consents.marketing}
                onChange={(e) => setConsents({...consents, marketing: e.target.checked})}
              />
            }
            label={
              <Box>
                <Typography>Marketing-Kommunikation</Typography>
                <Typography variant="caption" color="text.secondary">
                  Newsletter, Angebote, Produktinformationen
                </Typography>
              </Box>
            }
          />
          
          <FormControlLabel
            control={
              <Checkbox
                checked={consents.personalData}
                onChange={(e) => setConsents({...consents, personalData: e.target.checked})}
              />
            }
            label={
              <Box>
                <Typography>Erweiterte Datenverarbeitung</Typography>
                <Typography variant="caption" color="text.secondary">
                  Beziehungsdaten, Präferenzen, Analysen
                </Typography>
              </Box>
            }
          />
        </FormGroup>
        
        <Typography variant="caption" sx={{ mt: 2, display: 'block' }}>
          Einwilligungen können jederzeit widerrufen werden.
        </Typography>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={() => onSave(consents)} variant="contained">
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### Dienstag: Crypto-Shredding Implementation

#### Backend: Encryption Service
```java
// CryptoShredService.java
@ApplicationScoped
public class CryptoShredService {
    
    @Inject
    KeyManagementService keyService;
    
    // Encrypt personal data
    public EncryptedData encrypt(PersonalData data) {
        String keyId = keyService.generateKey();
        SecretKey key = keyService.getKey(keyId);
        
        String encrypted = AESUtil.encrypt(JsonUtil.toJson(data), key);
        
        return EncryptedData.builder()
            .keyId(keyId)
            .encryptedPayload(encrypted)
            .algorithm("AES-256-GCM")
            .build();
    }
    
    // Decrypt personal data
    public PersonalData decrypt(EncryptedData encrypted) {
        SecretKey key = keyService.getKey(encrypted.getKeyId());
        if (key == null) {
            // Key was shredded - data is permanently gone
            throw new DataShreddedException("Personal data has been deleted");
        }
        
        String json = AESUtil.decrypt(encrypted.getEncryptedPayload(), key);
        return JsonUtil.fromJson(json, PersonalData.class);
    }
    
    // GDPR deletion - just delete the key
    public void shredData(String keyId) {
        keyService.deleteKey(keyId);
        
        // Audit log the deletion
        auditService.log(DataShreddedEvent.builder()
            .keyId(keyId)
            .timestamp(Instant.now())
            .reason("GDPR deletion request")
            .build());
    }
}
```

#### Key Storage Schema
```sql
-- Encryption keys table
CREATE TABLE encryption_keys (
  key_id UUID PRIMARY KEY,
  encrypted_key TEXT NOT NULL, -- Key encrypted with master key
  algorithm VARCHAR(50) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP WITH TIME ZONE,
  status VARCHAR(20) DEFAULT 'active'
);

-- Audit table for key operations
CREATE TABLE key_audit_log (
  audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  key_id UUID NOT NULL,
  operation VARCHAR(50) NOT NULL, -- created, accessed, deleted
  user_id UUID NOT NULL,
  timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  reason TEXT
);
```

### Mittwoch: Mobile Quick Actions

#### Frontend: Quick Action Menu
```typescript
// components/mobile/QuickActionMenu.tsx
export const QuickActionMenu: React.FC<QuickActionMenuProps> = ({
  contact,
  onAction
}) => {
  const [expanded, setExpanded] = useState(false);
  const isMobile = useMediaQuery('(max-width:600px)');
  
  const primaryAction = useMemo(() => {
    // Intelligente Haupt-Aktion basierend auf letzter Interaktion
    const lastInteraction = contact.lastInteraction;
    if (!lastInteraction) return 'call';
    
    const daysSince = daysBetween(lastInteraction, new Date());
    if (daysSince > 30) return 'email'; // Lange nicht gesprochen
    if (daysSince > 7) return 'call';   // Wöchentlicher Check
    return 'note';                       // Kürzlich gesprochen
  }, [contact]);
  
  return (
    <SpeedDial
      ariaLabel="Quick Actions"
      sx={{ position: 'fixed', bottom: 16, right: 16 }}
      icon={<SpeedDialIcon />}
      onClose={() => setExpanded(false)}
      onOpen={() => setExpanded(true)}
      open={expanded}
    >
      <SpeedDialAction
        key="call"
        icon={<PhoneIcon />}
        tooltipTitle="Anrufen"
        onClick={() => {
          onAction('call', contact);
          setExpanded(false);
        }}
        sx={{
          backgroundColor: primaryAction === 'call' ? 'primary.main' : undefined
        }}
      />
      
      <SpeedDialAction
        key="email"
        icon={<EmailIcon />}
        tooltipTitle="E-Mail senden"
        onClick={() => {
          onAction('email', contact);
          setExpanded(false);
        }}
      />
      
      <SpeedDialAction
        key="whatsapp"
        icon={<WhatsAppIcon />}
        tooltipTitle="WhatsApp"
        onClick={() => {
          onAction('whatsapp', contact);
          setExpanded(false);
        }}
      />
      
      <SpeedDialAction
        key="note"
        icon={<NoteIcon />}
        tooltipTitle="Notiz hinzufügen"
        onClick={() => {
          onAction('note', contact);
          setExpanded(false);
        }}
      />
    </SpeedDial>
  );
};
```

#### Swipe Actions für Mobile
```typescript
// hooks/useSwipeActions.ts
export const useSwipeActions = () => {
  const handleSwipe = useCallback((direction: 'left' | 'right', contact: Contact) => {
    const actions = {
      left: () => archiveContact(contact),
      right: () => initiateCall(contact)
    };
    
    actions[direction]();
    
    // Haptic feedback on mobile
    if ('vibrate' in navigator) {
      navigator.vibrate(50);
    }
  }, []);
  
  return { handleSwipe };
};
```

### Donnerstag: Offline Queue Implementation

#### Frontend: Offline Action Queue
```typescript
// services/offlineQueue.ts
interface QueuedAction {
  id: string;
  type: ActionType;
  payload: any;
  timestamp: Date;
  retryCount: number;
  conflictMarkers?: string[];
}

export class OfflineActionQueue {
  private queue: QueuedAction[] = [];
  private syncInProgress = false;
  
  constructor(
    private storage: Storage = localStorage,
    private api: ApiService
  ) {
    this.loadQueue();
    this.setupEventListeners();
  }
  
  // Queue action when offline
  async queueAction(action: ContactAction): Promise<void> {
    const queuedAction: QueuedAction = {
      id: generateId(),
      type: action.type,
      payload: action.payload,
      timestamp: new Date(),
      retryCount: 0,
      conflictMarkers: this.detectConflictMarkers(action)
    };
    
    this.queue.push(queuedAction);
    this.persistQueue();
    
    // Try immediate sync if online
    if (navigator.onLine) {
      await this.syncQueue();
    }
  }
  
  // Sync when coming online
  private setupEventListeners(): void {
    window.addEventListener('online', () => {
      console.log('Back online - syncing queue');
      this.syncQueue();
    });
    
    // Periodic sync attempt
    setInterval(() => {
      if (navigator.onLine && this.queue.length > 0) {
        this.syncQueue();
      }
    }, 30000); // Every 30 seconds
  }
  
  // Sync queue with conflict resolution
  private async syncQueue(): Promise<void> {
    if (this.syncInProgress || this.queue.length === 0) return;
    
    this.syncInProgress = true;
    const failedActions: QueuedAction[] = [];
    
    for (const action of this.queue) {
      try {
        // Check for conflicts
        if (action.conflictMarkers?.length) {
          const conflicts = await this.checkConflicts(action);
          if (conflicts.length > 0) {
            // Handle conflict
            const resolution = await this.resolveConflict(action, conflicts);
            if (resolution === 'skip') {
              continue;
            }
          }
        }
        
        // Execute action
        await this.executeAction(action);
        
      } catch (error) {
        action.retryCount++;
        if (action.retryCount < 3) {
          failedActions.push(action);
        } else {
          // Log failed action for manual resolution
          console.error('Action failed after 3 retries:', action);
        }
      }
    }
    
    this.queue = failedActions;
    this.persistQueue();
    this.syncInProgress = false;
  }
  
  // Conflict detection
  private detectConflictMarkers(action: ContactAction): string[] {
    const markers: string[] = [];
    
    if (action.type === 'updateContact') {
      // Fields that might conflict
      markers.push('email', 'phone', 'position');
    }
    
    return markers;
  }
}
```

#### Service Worker für Offline
```typescript
// sw.js
self.addEventListener('fetch', (event) => {
  // Cache API responses for offline
  if (event.request.url.includes('/api/contacts')) {
    event.respondWith(
      caches.match(event.request)
        .then(response => {
          if (response) {
            // Return cached version
            return response;
          }
          
          // Fetch from network
          return fetch(event.request)
            .then(response => {
              // Cache successful responses
              if (response.status === 200) {
                const responseClone = response.clone();
                caches.open('contact-cache-v1')
                  .then(cache => {
                    cache.put(event.request, responseClone);
                  });
              }
              return response;
            });
        })
        .catch(() => {
          // Return offline fallback
          return new Response(
            JSON.stringify({ offline: true, cached: false }),
            { headers: { 'Content-Type': 'application/json' } }
          );
        })
    );
  }
});
```

### Freitag: GDPR Export & Testing

#### Backend: GDPR Export Service
```java
// GDPRExportService.java
@ApplicationScoped
public class GDPRExportService {
    
    @Inject
    EventStore eventStore;
    
    @Inject
    CryptoShredService cryptoService;
    
    public ExportResult exportContactData(UUID contactId, ExportFormat format) {
        // Collect all data
        ContactExportData data = ContactExportData.builder()
            .personalData(getPersonalData(contactId))
            .consentHistory(getConsentHistory(contactId))
            .communicationHistory(getCommunicationHistory(contactId))
            .eventHistory(getEventHistory(contactId))
            .build();
        
        // Format export
        return switch (format) {
            case JSON -> exportAsJson(data);
            case PDF -> exportAsPdf(data);
            case CSV -> exportAsCsv(data);
        };
    }
    
    private List<EventSummary> getEventHistory(UUID contactId) {
        return eventStore.getEvents(contactId).stream()
            .map(event -> EventSummary.builder()
                .eventType(event.getEventType())
                .timestamp(event.getCreatedAt())
                .userId(event.getMetadata().getUserId())
                // Personal data is encrypted - only show metadata
                .build())
            .collect(Collectors.toList());
    }
    
    // Scheduled cleanup of old exports
    @Scheduled(every = "24h")
    void cleanupOldExports() {
        exportRepository.deleteExportsOlderThan(Duration.ofDays(7));
    }
}
```

#### Frontend: Export Dialog
```typescript
// components/GDPRExportDialog.tsx
export const GDPRExportDialog: React.FC<GDPRExportDialogProps> = ({
  contact,
  open,
  onClose
}) => {
  const [format, setFormat] = useState<ExportFormat>('pdf');
  const [loading, setLoading] = useState(false);
  
  const handleExport = async () => {
    setLoading(true);
    try {
      const blob = await contactApi.exportGDPRData(contact.id, format);
      
      // Download file
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `dsgvo-export-${contact.name}-${new Date().toISOString()}.${format}`;
      a.click();
      
      // Audit log
      await auditApi.logExport({
        contactId: contact.id,
        format,
        timestamp: new Date()
      });
      
      onClose();
    } catch (error) {
      console.error('Export failed:', error);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>DSGVO Datenexport</DialogTitle>
      
      <DialogContent>
        <Alert severity="info" sx={{ mb: 2 }}>
          Exportieren Sie alle gespeicherten Daten gemäß DSGVO Art. 20
        </Alert>
        
        <FormControl fullWidth>
          <InputLabel>Export-Format</InputLabel>
          <Select
            value={format}
            onChange={(e) => setFormat(e.target.value as ExportFormat)}
          >
            <MenuItem value="pdf">PDF (Lesbar)</MenuItem>
            <MenuItem value="json">JSON (Maschinenlesbar)</MenuItem>
            <MenuItem value="csv">CSV (Tabellenformat)</MenuItem>
          </Select>
        </FormControl>
        
        <Box sx={{ mt: 2 }}>
          <Typography variant="caption">
            Der Export enthält:
          </Typography>
          <ul>
            <li>Persönliche Daten</li>
            <li>Einwilligungshistorie</li>
            <li>Kommunikationsverlauf</li>
            <li>Aktivitätsprotokoll</li>
          </ul>
        </Box>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button 
          onClick={handleExport} 
          variant="contained"
          disabled={loading}
        >
          {loading ? <CircularProgress size={20} /> : 'Exportieren'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

#### Integration Tests
```typescript
// __tests__/gdpr.integration.test.ts
describe('GDPR Compliance', () => {
  it('should handle consent lifecycle', async () => {
    // Create contact
    const contact = await createTestContact();
    
    // Grant consent
    await contactApi.grantConsent(contact.id, {
      type: 'marketing',
      basis: 'explicit'
    });
    
    // Verify consent active
    const consents = await contactApi.getConsents(contact.id);
    expect(consents.marketing).toBe(true);
    
    // Revoke consent
    await contactApi.revokeConsent(contact.id, 'marketing');
    
    // Verify consent revoked
    const updatedConsents = await contactApi.getConsents(contact.id);
    expect(updatedConsents.marketing).toBe(false);
  });
  
  it('should export all personal data', async () => {
    const contact = await createTestContact();
    
    // Request export
    const exportData = await contactApi.exportGDPRData(contact.id, 'json');
    const parsed = JSON.parse(exportData);
    
    // Verify completeness
    expect(parsed).toHaveProperty('personalData');
    expect(parsed).toHaveProperty('consentHistory');
    expect(parsed).toHaveProperty('eventHistory');
    expect(parsed.personalData.email).toBe(contact.email);
  });
  
  it('should handle crypto-shredding', async () => {
    const contact = await createTestContact();
    
    // Delete contact (shred keys)
    await contactApi.deleteContact(contact.id, { permanent: true });
    
    // Try to access - should fail
    await expect(contactApi.getContact(contact.id))
      .rejects.toThrow('DataShreddedException');
  });
});
```

## 📊 Wochenergebnis

### Deliverables
- ✅ Consent Management mit UI
- ✅ Crypto-Shredding implementiert
- ✅ Mobile Quick Actions
- ✅ Offline Queue funktioniert
- ✅ GDPR Export (JSON, PDF, CSV)
- ✅ Service Worker für Offline
- ✅ Alle Tests grün

### Metriken
- GDPR Compliance: 100%
- Mobile Performance: < 2s Initial Load
- Offline Coverage: 80% der Features
- Test Coverage: 88%

### Tech Debt
- [ ] Consent Auto-Renewal (später)
- [ ] Advanced Conflict Resolution (Sprint 3)
- [ ] Push Notifications (später)

## 🔗 Nächste Woche

[→ Woche 3: Relationship + Analytics](./WEEK3_RELATIONSHIP_ANALYTICS.md)

---

**Navigation:**
- [← Woche 1: Event Sourcing](./WEEK1_EVENT_SOURCING.md)
- [→ Woche 3: Relationship](./WEEK3_RELATIONSHIP_ANALYTICS.md)