# 📆 Tag 1: Consent Management Foundation

**Datum:** Montag, 12. August 2025  
**Fokus:** Consent Entity & Events  
**Ziel:** DSGVO-konforme Einwilligungsverwaltung  

## 🧭 Navigation

**↑ Woche 2 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 2: Crypto-Shredding](./DAY2_CRYPTO.md)  
**📘 Spec:** [Consent Specification](./specs/CONSENT_SPEC.md)  

## 🎯 Tagesziel

- Backend: Consent Aggregate mit Events implementieren
- Frontend: Consent Dialog Component erstellen
- Integration: Event Store Anbindung
- Testing: Unit Tests für Consent Logic

## 💻 Backend Implementation

### 1. Consent Aggregate

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
```

**Vollständiger Code:** [backend/ConsentAggregate.java](./code/backend/ConsentAggregate.java)

### 2. Consent Events

```java
// ConsentGrantedEvent.java
@Value
@Builder
public class ConsentGrantedEvent extends BaseEvent {
    UUID contactId;
    ConsentType consentType;
    ConsentBasis basis;
    Instant expiresAt;
    EventMetadata metadata;
}
```

**Alle Events:** [backend/events/](./code/backend/events/)

## 🎨 Frontend Implementation

### Consent Dialog Component

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
          {/* Weitere Consent-Typen... */}
        </FormGroup>
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

**Vollständiger Code:** [frontend/ConsentDialog.tsx](./code/frontend/ConsentDialog.tsx)

## 🧪 Tests

### Backend Unit Test

```java
@Test
void shouldGrantConsentSuccessfully() {
    // Given
    ConsentAggregate aggregate = new ConsentAggregate(contactId);
    GrantConsentCommand cmd = GrantConsentCommand.builder()
        .consentType(ConsentType.MARKETING)
        .basis(ConsentBasis.EXPLICIT)
        .build();
    
    // When
    aggregate.grantConsent(cmd);
    
    // Then
    assertTrue(aggregate.hasActiveConsent(ConsentType.MARKETING));
}
```

**Alle Tests:** [tests/consent/](./code/tests/consent/)

## 📝 Checkliste

- [ ] ConsentAggregate implementiert
- [ ] Events (Granted, Revoked) definiert
- [ ] Event Handlers registriert
- [ ] Frontend Dialog erstellt
- [ ] API Endpoints implementiert
- [ ] Unit Tests geschrieben
- [ ] Integration Tests laufen

## 🔗 Weiterführende Links

- **DSGVO Grundlagen:** [GDPR Compliance Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/GDPR_COMPLIANCE_ARCHITECTURE.md)
- **Event Patterns:** [Event Sourcing Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/EVENT_SOURCING_FOUNDATION.md)
- **Nächster Schritt:** [→ Tag 2: Crypto-Shredding](./DAY2_CRYPTO.md)

---

**Status:** 📋 Bereit zur Implementierung