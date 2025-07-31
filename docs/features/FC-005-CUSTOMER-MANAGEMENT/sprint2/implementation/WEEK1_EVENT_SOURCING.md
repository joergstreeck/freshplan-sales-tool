# 📅 Woche 1: Event Sourcing Foundation

**Sprint:** Sprint 2 - Contact Management  
**Woche:** 1 (5.-9. August 2025)  
**Fokus:** Event Store Setup + Basic Multi-Contact UI  

## 🎯 Wochenziel

Implementierung der Event Sourcing Foundation und eines funktionsfähigen Multi-Contact UI. Am Ende der Woche haben wir:
- ✅ Event Store mit Versioning
- ✅ Contact Aggregate mit Events
- ✅ Basic Projections (List, Detail)
- ✅ Multi-Contact UI mit Cards

## 📋 Tagesplan

### Montag: Event Store Schema + Base Events

#### Backend Tasks
```sql
-- Event Store Schema (PostgreSQL)
CREATE TABLE events (
  event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aggregate_id UUID NOT NULL,
  aggregate_type VARCHAR(50) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  event_version INTEGER NOT NULL DEFAULT 1,
  sequence_number BIGSERIAL,
  event_data JSONB NOT NULL,
  metadata JSONB NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  
  -- Indices
  INDEX idx_aggregate (aggregate_id, sequence_number),
  INDEX idx_event_type (event_type),
  INDEX idx_created_at (created_at)
);

-- Snapshots Table
CREATE TABLE snapshots (
  snapshot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  aggregate_id UUID NOT NULL,
  aggregate_type VARCHAR(50) NOT NULL,
  version BIGINT NOT NULL,
  data JSONB NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE(aggregate_id, version)
);
```

#### Base Event Implementation
```java
// BaseEvent.java
@MappedSuperclass
public abstract class BaseEvent {
    @Id
    private UUID eventId = UUID.randomUUID();
    
    private UUID aggregateId;
    private String aggregateType;
    private String eventType;
    private Integer eventVersion = 1;
    private Long sequenceNumber;
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private EventMetadata metadata;
    
    private Instant createdAt = Instant.now();
    
    // Getters, Builder...
}

// EventMetadata.java
@Embeddable
public class EventMetadata {
    private String userId;
    private String correlationId;
    private String causationId;
    private String source; // ui, api, import, system
    private String tenantId;
    
    // Additional fields
    private String userAgent;
    private String ipAddress;
}
```

#### Frontend: Store für Contact Array
```typescript
// stores/contactStore.ts
interface ContactState {
  contacts: Contact[];
  primaryContactId?: string;
  loading: boolean;
  error: string | null;
}

export const useContactStore = create<ContactState>((set, get) => ({
  contacts: [],
  primaryContactId: undefined,
  loading: false,
  error: null,
  
  // Actions
  addContact: (contact: Contact) => {
    set(state => ({
      contacts: [...state.contacts, contact],
      // Erster Kontakt wird automatisch primary
      primaryContactId: state.primaryContactId || contact.id
    }));
  },
  
  updateContact: (id: string, updates: Partial<Contact>) => {
    set(state => ({
      contacts: state.contacts.map(c => 
        c.id === id ? { ...c, ...updates } : c
      )
    }));
  },
  
  removeContact: (id: string) => {
    set(state => {
      const newContacts = state.contacts.filter(c => c.id !== id);
      // Wenn primary gelöscht, nächsten als primary setzen
      const newPrimary = state.primaryContactId === id && newContacts.length > 0
        ? newContacts[0].id
        : state.primaryContactId;
      
      return {
        contacts: newContacts,
        primaryContactId: newPrimary
      };
    });
  }
}));
```

### Dienstag: Contact Aggregate + Soft Delete

#### Backend: Contact Aggregate
```java
// ContactAggregate.java
@Entity
@Table(name = "contact_aggregates")
public class ContactAggregate {
    @Id
    private UUID id;
    
    private ContactStatus status = ContactStatus.ACTIVE;
    
    @Transient
    private List<BaseEvent> pendingEvents = new ArrayList<>();
    
    // Business Methods
    public void createContact(CreateContactCommand cmd) {
        // Validation
        if (this.status != null) {
            throw new IllegalStateException("Contact already exists");
        }
        
        // Apply Event
        apply(ContactCreatedEvent.builder()
            .aggregateId(cmd.getContactId())
            .personalData(cmd.getPersonalData())
            .customerId(cmd.getCustomerId())
            .metadata(buildMetadata())
            .build());
    }
    
    public void updateRelationshipData(UpdateRelationshipCommand cmd) {
        ensureActive();
        
        apply(RelationshipDataUpdatedEvent.builder()
            .aggregateId(this.id)
            .relationshipData(cmd.getRelationshipData())
            .metadata(buildMetadata())
            .build());
    }
    
    public void softDelete(String reason) {
        ensureActive();
        
        apply(ContactDeletedEvent.builder()
            .aggregateId(this.id)
            .reason(reason)
            .deletionType("SOFT")
            .metadata(buildMetadata())
            .build());
    }
    
    // Event Handling
    @EventHandler
    private void on(ContactCreatedEvent event) {
        this.id = event.getAggregateId();
        this.status = ContactStatus.ACTIVE;
    }
    
    @EventHandler
    private void on(ContactDeletedEvent event) {
        this.status = ContactStatus.DELETED;
    }
}
```

#### Frontend: ContactCard Component mit Theme
```typescript
// components/ContactCard.tsx
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';

interface ContactCardProps {
  contact: Contact;
  isPrimary: boolean;
  onEdit: (contact: Contact) => void;
  onDelete: (id: string) => void;
  onSetPrimary: (id: string) => void;
}

export const ContactCard: React.FC<ContactCardProps> = ({
  contact,
  isPrimary,
  onEdit,
  onDelete,
  onSetPrimary
}) => {
  return (
    <Card 
      sx={{ 
        mb: 2,
        border: isPrimary ? '2px solid' : '1px solid',
        borderColor: isPrimary ? 'primary.main' : 'divider'
      }}
    >
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="start">
          <Box>
            <Typography variant="h6" component="div">
              {contact.salutation} {contact.title} {contact.firstName} {contact.lastName}
              {isPrimary && (
                <Chip 
                  label="Primär" 
                  size="small" 
                  color="primary" 
                  sx={{ ml: 1 }}
                />
              )}
            </Typography>
            <Typography color="text.secondary">
              {contact.position} | {contact.decisionLevel}
            </Typography>
            <Typography variant="body2" sx={{ mt: 1 }}>
              📧 {contact.email}
              {contact.phone && ` | 📞 ${contact.phone}`}
              {contact.mobile && ` | 📱 ${contact.mobile}`}
            </Typography>
            {contact.assignedLocationId && (
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                📍 Zuständig für: {getLocationName(contact.assignedLocationId)}
              </Typography>
            )}
          </Box>
          
          <Box>
            <IconButton onClick={() => onEdit(contact)}>
              <EditIcon />
            </IconButton>
            <IconButton onClick={() => onDelete(contact.id)}>
              <DeleteIcon />
            </IconButton>
            {!isPrimary && (
              <IconButton onClick={() => onSetPrimary(contact.id)}>
                <StarIcon />
              </IconButton>
            )}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### Mittwoch: Basic Projections

#### Backend: Projection Services
```java
// ContactListProjection.java
@ApplicationScoped
public class ContactListProjection {
    
    @Inject
    EntityManager em;
    
    @EventHandler
    @Transactional
    public void on(ContactCreatedEvent event) {
        ContactListView view = ContactListView.builder()
            .contactId(event.getAggregateId())
            .customerId(event.getCustomerId())
            .name(formatName(event.getPersonalData()))
            .position(event.getPersonalData().getPosition())
            .email(event.getPersonalData().getEmail())
            .isPrimary(event.getIsPrimary())
            .createdAt(event.getCreatedAt())
            .build();
            
        em.persist(view);
    }
    
    @EventHandler
    @Transactional
    public void on(ContactDeletedEvent event) {
        em.createQuery("UPDATE ContactListView SET status = :status WHERE contactId = :id")
            .setParameter("status", "DELETED")
            .setParameter("id", event.getAggregateId())
            .executeUpdate();
    }
}

// ContactDetailProjection.java
@ApplicationScoped
public class ContactDetailProjection {
    
    @EventHandler
    @Transactional
    public void on(ContactCreatedEvent event) {
        ContactDetailView view = mapToDetailView(event);
        em.persist(view);
    }
    
    @EventHandler
    @Transactional
    public void on(RelationshipDataUpdatedEvent event) {
        ContactDetailView view = em.find(ContactDetailView.class, event.getAggregateId());
        view.setRelationshipData(event.getRelationshipData());
        em.merge(view);
    }
}
```

#### Frontend: ContactForm Component
```typescript
// components/ContactForm.tsx
export const ContactForm: React.FC<ContactFormProps> = ({
  contact,
  locations,
  onSave,
  onCancel
}) => {
  const [formData, setFormData] = useState<ContactFormData>(
    contact || getEmptyContact()
  );
  
  return (
    <Dialog open onClose={onCancel} maxWidth="md" fullWidth>
      <DialogTitle>
        {contact ? 'Kontakt bearbeiten' : 'Neuer Kontakt'}
      </DialogTitle>
      
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          {/* Persönliche Daten */}
          <Grid item xs={12} md={6}>
            <TextField
              select
              fullWidth
              label="Anrede"
              value={formData.salutation}
              onChange={(e) => setFormData({...formData, salutation: e.target.value})}
            >
              <MenuItem value="Herr">Herr</MenuItem>
              <MenuItem value="Frau">Frau</MenuItem>
              <MenuItem value="Divers">Divers</MenuItem>
            </TextField>
          </Grid>
          
          <Grid item xs={12} md={6}>
            <TextField
              select
              fullWidth
              label="Titel"
              value={formData.title || ''}
              onChange={(e) => setFormData({...formData, title: e.target.value})}
            >
              <MenuItem value="">Kein Titel</MenuItem>
              <MenuItem value="Dr.">Dr.</MenuItem>
              <MenuItem value="Prof.">Prof.</MenuItem>
            </TextField>
          </Grid>
          
          {/* Weitere Felder... */}
          
          {/* Standort-Zuordnung */}
          <Grid item xs={12}>
            <TextField
              select
              fullWidth
              label="Zuständig für Standort"
              value={formData.assignedLocationId || ''}
              onChange={(e) => setFormData({...formData, assignedLocationId: e.target.value})}
            >
              <MenuItem value="">Hauptadresse</MenuItem>
              {locations.map(loc => (
                <MenuItem key={loc.id} value={loc.id}>
                  {loc.name} ({loc.city})
                </MenuItem>
              ))}
            </TextField>
          </Grid>
        </Grid>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onCancel}>Abbrechen</Button>
        <Button onClick={() => onSave(formData)} variant="contained">
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### Donnerstag: Event Versioning + Migration

#### Backend: Event Migration Service
```java
// EventMigrationService.java
@ApplicationScoped
public class EventMigrationService {
    
    private final Map<String, EventMigrator> migrators = new HashMap<>();
    
    @PostConstruct
    void init() {
        // Register migrators
        migrators.put("ContactCreatedEvent_1_2", new ContactCreatedV1ToV2Migrator());
    }
    
    public BaseEvent migrate(BaseEvent event) {
        String key = event.getEventType() + "_" + event.getEventVersion() + "_" + getCurrentVersion();
        EventMigrator migrator = migrators.get(key);
        
        if (migrator != null) {
            return migrator.migrate(event);
        }
        
        return event;
    }
}

// ContactCreatedV1ToV2Migrator.java
public class ContactCreatedV1ToV2Migrator implements EventMigrator {
    
    @Override
    public BaseEvent migrate(BaseEvent event) {
        ContactCreatedEventV1 v1 = (ContactCreatedEventV1) event;
        
        // Split name field
        String[] nameParts = v1.getName().split(" ", 2);
        
        return ContactCreatedEventV2.builder()
            .eventId(v1.getEventId())
            .aggregateId(v1.getAggregateId())
            .firstName(nameParts[0])
            .lastName(nameParts.length > 1 ? nameParts[1] : "")
            .email(v1.getEmail())
            // New fields with defaults
            .gdprConsent(false)
            .consentDate(null)
            .metadata(v1.getMetadata())
            .build();
    }
}
```

#### Frontend: Multi-Contact Layout mit Theme
```typescript
// components/MultiContactManager.tsx
import { CustomerFieldThemeProvider } from '../../theme';
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';

export const MultiContactManager: React.FC = () => {
  const { contacts, primaryContactId, addContact, updateContact, removeContact, setPrimaryContact } = useContactStore();
  const [showForm, setShowForm] = useState(false);
  const [editingContact, setEditingContact] = useState<Contact | null>(null);
  
  return (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">
          Ansprechpartner ({contacts.length})
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => {
            setEditingContact(null);
            setShowForm(true);
          }}
        >
          Neuer Kontakt
        </Button>
      </Box>
      
      {contacts.length === 0 ? (
        <Alert severity="info">
          Noch keine Ansprechpartner erfasst. Fügen Sie den ersten Kontakt hinzu.
        </Alert>
      ) : (
        <Box>
          {contacts.map(contact => (
            <ContactCard
              key={contact.id}
              contact={contact}
              isPrimary={contact.id === primaryContactId}
              onEdit={(c) => {
                setEditingContact(c);
                setShowForm(true);
              }}
              onDelete={removeContact}
              onSetPrimary={setPrimaryContact}
            />
          ))}
        </Box>
      )}
      
      {showForm && (
        <ContactForm
          contact={editingContact}
          locations={locations}
          onSave={(data) => {
            if (editingContact) {
              updateContact(editingContact.id, data);
            } else {
              addContact({
                ...data,
                id: generateId()
              });
            }
            setShowForm(false);
          }}
          onCancel={() => setShowForm(false)}
        />
      )}
    </Box>
    </CustomerFieldThemeProvider>
  );
};
```

### Freitag: Integration Tests + Review

#### Backend: Integration Tests
```java
// ContactEventStoreIntegrationTest.java
@QuarkusTest
@TestTransaction
class ContactEventStoreIntegrationTest {
    
    @Inject
    EventStore eventStore;
    
    @Inject
    ContactService contactService;
    
    @Test
    void shouldCreateContactAndEmitEvent() {
        // Given
        CreateContactCommand cmd = CreateContactCommand.builder()
            .customerId(UUID.randomUUID())
            .personalData(buildTestPersonalData())
            .build();
        
        // When
        UUID contactId = contactService.createContact(cmd);
        
        // Then
        List<BaseEvent> events = eventStore.getEvents(contactId);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ContactCreatedEvent.class);
        
        ContactCreatedEvent event = (ContactCreatedEvent) events.get(0);
        assertThat(event.getCustomerId()).isEqualTo(cmd.getCustomerId());
    }
    
    @Test
    void shouldHandleEventMigration() {
        // Given - Old event version
        ContactCreatedEventV1 oldEvent = createV1Event();
        eventStore.append(oldEvent);
        
        // When - Read with migration
        List<BaseEvent> events = eventStore.getEvents(oldEvent.getAggregateId());
        
        // Then - Should be migrated to V2
        assertThat(events.get(0)).isInstanceOf(ContactCreatedEventV2.class);
        ContactCreatedEventV2 v2 = (ContactCreatedEventV2) events.get(0);
        assertThat(v2.getFirstName()).isNotEmpty();
    }
}
```

#### Frontend: UI Tests
```typescript
// __tests__/MultiContactManager.test.tsx
describe('MultiContactManager', () => {
  it('should add new contact', async () => {
    render(<MultiContactManager />);
    
    // Click add button
    fireEvent.click(screen.getByText('Neuer Kontakt'));
    
    // Fill form
    fireEvent.change(screen.getByLabelText('Vorname'), {
      target: { value: 'Max' }
    });
    fireEvent.change(screen.getByLabelText('Nachname'), {
      target: { value: 'Mustermann' }
    });
    
    // Save
    fireEvent.click(screen.getByText('Speichern'));
    
    // Verify
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      expect(screen.getByText('Primär')).toBeInTheDocument(); // First is primary
    });
  });
  
  it('should handle primary contact switching', async () => {
    // Setup with 2 contacts
    const { container } = render(<MultiContactManager />);
    // ... add 2 contacts
    
    // Click star on second contact
    const starButtons = container.querySelectorAll('[data-testid="set-primary"]');
    fireEvent.click(starButtons[1]);
    
    // Verify primary switched
    await waitFor(() => {
      const primaryChips = screen.getAllByText('Primär');
      expect(primaryChips).toHaveLength(1);
    });
  });
});
```

## 📊 Wochenergebnis

### Deliverables
- ✅ Event Store Schema implementiert
- ✅ Base Events mit Versioning
- ✅ Contact Aggregate mit Business Logic
- ✅ Soft Delete implementiert
- ✅ List & Detail Projections
- ✅ Multi-Contact UI mit Cards
- ✅ Contact Form mit Validierung
- ✅ Event Migration funktioniert
- ✅ Integration Tests grün

### Metriken
- Test Coverage: 85%
- API Response Time: < 50ms
- UI Rendering: < 100ms
- Events pro Sekunde: > 1000

### Tech Debt
- [ ] Snapshot-Mechanismus (später)
- [ ] Event Archivierung (später)
- [ ] Bulk Operations (Sprint 3)

## 🔗 Nächste Woche

[→ Woche 2: Features + Compliance](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK2_FEATURES_COMPLIANCE.md)

---

**Navigation:**
- [← Sprint 2 Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_MASTER_PLAN.md)
- [→ Woche 2 Details](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/WEEK2_FEATURES_COMPLIANCE.md)