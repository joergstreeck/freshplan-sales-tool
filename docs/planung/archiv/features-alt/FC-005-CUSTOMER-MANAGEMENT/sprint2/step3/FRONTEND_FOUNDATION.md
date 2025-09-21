# 🏗️ Frontend Foundation - Contact Store & API Integration

**Phase:** 1 - Foundation  
**Tag:** 2 der Woche 1  
**Status:** 🎯 Ready for Implementation  

## 🧭 Navigation

**← Zurück:** [Theme Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/THEME_ARCHITECTURE.md)  
**→ Nächster:** [Smart Contact Cards](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/SMART_CONTACT_CARDS.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: Robuste Frontend-Architektur

**Frontend Foundation** schafft die **technische Basis** für alle Contact-Features:

> "Ein solides Fundament für intelligente Kontaktverwaltung"

## 🏗️ Architektur-Übersicht

```
┌─────────────────────────────────────────┐
│        React Components                  │
├─────────────────────────────────────────┤
│    Zustand Contact Store                │
│    - State Management                   │
│    - Optimistic Updates                 │
├─────────────────────────────────────────┤
│      Contact API Service                │
│    - Type-safe API calls               │
│    - Error Handling                     │
├─────────────────────────────────────────┤
│     Validation & Utilities              │
│    - Form Validation                    │
│    - Data Transformation               │
└─────────────────────────────────────────┘
```

## 💻 Implementation

### 1. Contact Types

```typescript
// types/contact.types.ts

export interface Contact {
  id: string;
  customerId: string;
  
  // Basic Info (Strukturiert nach V2)
  salutation: 'Herr' | 'Frau' | 'Divers';
  title?: string; // Dr., Prof., etc.
  firstName: string;
  lastName: string;
  position?: string;
  decisionLevel?: 'executive' | 'manager' | 'operational' | 'influencer';
  
  // Contact Info
  email?: string;
  phone?: string;
  mobile?: string;
  
  // Flags
  isPrimary: boolean;
  isActive: boolean;
  
  // Location & Responsibility (NEU aus V2)
  responsibilityScope: 'all' | 'specific';
  assignedLocationIds?: string[]; // Array bei 'specific'
  assignedLocationNames?: string[]; // Denormalized for display
  
  // Roles (NEU aus V2)
  roles: ContactRole[];
  
  // Relationship Data
  birthday?: string; // ISO date
  hobbies?: string[];
  familyStatus?: 'single' | 'married' | 'divorced' | 'widowed';
  childrenCount?: number;
  personalNotes?: string;
  
  // Audit
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export type ContactRole = 
  | 'decision_maker'
  | 'technical_contact'
  | 'billing_contact'
  | 'operations_contact';

export interface CreateContactRequest {
  firstName: string;
  lastName: string;
  email?: string;
  position?: string;
  // ... andere required fields
}

export interface UpdateContactRequest extends Partial<CreateContactRequest> {
  isPrimary?: boolean;
  assignedLocationId?: string;
}
```

### 2. Store Extension

```typescript
// stores/contactStore.ts

import { create } from 'zustand';
import { immer } from 'zustand/middleware/immer';
import type { Contact } from '../types/contact.types';
import { contactApi } from '../services/contactApi';

interface ContactState {
  // State
  contacts: Contact[];
  loading: boolean;
  error: string | null;
  
  // Actions
  loadContacts: (customerId: string) => Promise<void>;
  addContact: (customerId: string, data: CreateContactRequest) => Promise<void>;
  updateContact: (contactId: string, data: UpdateContactRequest) => Promise<void>;
  deleteContact: (contactId: string) => Promise<void>;
  setPrimaryContact: (customerId: string, contactId: string) => Promise<void>;
  assignToLocation: (contactId: string, locationId: string) => Promise<void>;
}

export const useContactStore = create<ContactState>()(
  immer((set, get) => ({
    contacts: [],
    loading: false,
    error: null,
    
    loadContacts: async (customerId) => {
      set((state) => {
        state.loading = true;
        state.error = null;
      });
      
      try {
        const contacts = await contactApi.getContacts(customerId);
        set((state) => {
          state.contacts = contacts;
          state.loading = false;
        });
      } catch (error) {
        set((state) => {
          state.error = error.message;
          state.loading = false;
        });
      }
    },
    
    addContact: async (customerId, data) => {
      try {
        const newContact = await contactApi.createContact(customerId, data);
        set((state) => {
          state.contacts.push(newContact);
        });
      } catch (error) {
        set((state) => {
          state.error = error.message;
        });
      }
    },
    
    updateContact: async (contactId, data) => {
      try {
        const updated = await contactApi.updateContact(contactId, data);
        set((state) => {
          const index = state.contacts.findIndex(c => c.id === contactId);
          if (index >= 0) {
            state.contacts[index] = updated;
          }
        });
      } catch (error) {
        set((state) => {
          state.error = error.message;
        });
      }
    },
    
    setPrimaryContact: async (customerId, contactId) => {
      try {
        await contactApi.setPrimaryContact(customerId, contactId);
        set((state) => {
          // Update all contacts: unset old primary, set new
          state.contacts.forEach(contact => {
            contact.isPrimary = contact.id === contactId;
          });
        });
      } catch (error) {
        set((state) => {
          state.error = error.message;
        });
      }
    },
    
    deleteContact: async (contactId) => {
      try {
        await contactApi.deleteContact(contactId);
        set((state) => {
          state.contacts = state.contacts.filter(c => c.id !== contactId);
        });
      } catch (error) {
        set((state) => {
          state.error = error.message;
        });
      }
    },
    
    assignToLocation: async (contactId, locationId) => {
      try {
        const updated = await contactApi.updateContact(contactId, {
          assignedLocationIds: [locationId],
          responsibilityScope: 'specific'
        });
        set((state) => {
          const index = state.contacts.findIndex(c => c.id === contactId);
          if (index >= 0) {
            state.contacts[index] = updated;
          }
        });
      } catch (error) {
        set((state) => {
          state.error = error.message;
        });
      }
    },
    
    // NEU aus V2: Responsibility Management
    setContactResponsibility: async (
      contactId: string,
      scope: 'all' | 'specific',
      locationIds?: string[]
    ) => {
      try {
        const updated = await contactApi.updateContact(contactId, {
          responsibilityScope: scope,
          assignedLocationIds: scope === 'specific' ? locationIds : undefined
        });
        set((state) => {
          const index = state.contacts.findIndex(c => c.id === contactId);
          if (index >= 0) {
            state.contacts[index] = updated;
          }
        });
      } catch (error) {
        set((state) => {
          state.error = error.message;
        });
      }
    }
  }))
);
```

### 3. API Service

```typescript
// services/contactApi.ts

import { api } from './api';
import type { 
  Contact, 
  CreateContactRequest, 
  UpdateContactRequest 
} from '../types/contact.types';

export const contactApi = {
  getContacts: async (customerId: string): Promise<Contact[]> => {
    const response = await api.get(`/customers/${customerId}/contacts`);
    return response.data;
  },
  
  createContact: async (
    customerId: string, 
    data: CreateContactRequest
  ): Promise<Contact> => {
    const response = await api.post(`/customers/${customerId}/contacts`, data);
    return response.data;
  },
  
  updateContact: async (
    contactId: string, 
    data: UpdateContactRequest
  ): Promise<Contact> => {
    const response = await api.put(`/contacts/${contactId}`, data);
    return response.data;
  },
  
  deleteContact: async (contactId: string): Promise<void> => {
    await api.delete(`/contacts/${contactId}`);
  },
  
  setPrimaryContact: async (
    customerId: string, 
    contactId: string
  ): Promise<void> => {
    await api.put(`/customers/${customerId}/contacts/${contactId}/set-primary`);
  }
};
```

### 4. Field Catalog Extension

```json
// data/fieldCatalogContacts.json
{
  "contactFields": [
    {
      "key": "contactSalutation",
      "label": "Anrede",
      "entityType": "contact",
      "fieldType": "select",
      "required": false,
      "category": "personal",
      "options": [
        { "value": "herr", "label": "Herr" },
        { "value": "frau", "label": "Frau" },
        { "value": "divers", "label": "Divers" }
      ]
    },
    {
      "key": "contactDecisionLevel",
      "label": "Entscheidungsebene",
      "entityType": "contact",
      "fieldType": "select",
      "required": true,
      "category": "business",
      "options": [
        { "value": "executive", "label": "Geschäftsführung" },
        { "value": "manager", "label": "Abteilungsleitung" },
        { "value": "operational", "label": "Operativ" },
        { "value": "influencer", "label": "Beeinflusser" }
      ],
      "helpText": "Welche Rolle hat der Kontakt im Entscheidungsprozess?"
    },
    {
      "key": "contactBirthday",
      "label": "Geburtstag",
      "entityType": "contact",
      "fieldType": "date",
      "required": false,
      "category": "relationship",
      "helpText": "Für persönliche Glückwünsche"
    },
    {
      "key": "contactHobbies",
      "label": "Hobbys/Interessen",
      "entityType": "contact",
      "fieldType": "multiselect",
      "required": false,
      "category": "relationship",
      "options": [
        { "value": "golf", "label": "Golf" },
        { "value": "tennis", "label": "Tennis" },
        { "value": "sailing", "label": "Segeln" },
        { "value": "wine", "label": "Wein" },
        { "value": "cooking", "label": "Kochen" },
        { "value": "travel", "label": "Reisen" },
        { "value": "art", "label": "Kunst" },
        { "value": "music", "label": "Musik" }
      ]
    },
    {
      "key": "contactTitle",
      "label": "Titel",
      "entityType": "contact",
      "fieldType": "autocomplete",
      "required": false,
      "category": "personal",
      "options": [
        { "value": "dr", "label": "Dr." },
        { "value": "prof", "label": "Prof." },
        { "value": "prof_dr", "label": "Prof. Dr." }
      ],
      "allowCustom": true,
      "helpText": "Akademischer Titel"
    }
  ]
}
```

## 🧪 Store Tests

```typescript
// stores/__tests__/contactStore.test.ts

describe('ContactStore', () => {
  it('should set primary contact correctly', async () => {
    const { addContact, setPrimaryContact, contacts } = useContactStore.getState();
    
    // Add two contacts
    await addContact('customer-1', { 
      firstName: 'Max', 
      lastName: 'Mustermann' 
    });
    await addContact('customer-1', { 
      firstName: 'Maria', 
      lastName: 'Musterfrau' 
    });
    
    // Set second as primary
    const secondContactId = contacts[1].id;
    await setPrimaryContact('customer-1', secondContactId);
    
    // Verify
    const state = useContactStore.getState();
    expect(state.contacts[0].isPrimary).toBe(false);
    expect(state.contacts[1].isPrimary).toBe(true);
  });
});
```

## 📝 Checkliste

- [ ] Contact Types definiert
- [ ] Store erweitert
- [ ] API Service implementiert
- [ ] Field Catalog erweitert
- [ ] Tests geschrieben
- [ ] Integration getestet

### 5. Validation Utilities

```typescript
// utils/contactValidation.ts

export const contactValidation = {
  email: {
    pattern: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
    message: 'Ungültige E-Mail-Adresse'
  },
  
  phone: {
    pattern: /^[\+]?[(]?[0-9]{3}[)]?[-\s\.]?[(]?[0-9]{3}[)]?[-\s\.]?[0-9]{4,6}$/,
    message: 'Ungültige Telefonnummer'
  },
  
  required: {
    firstName: 'Vorname ist erforderlich',
    lastName: 'Nachname ist erforderlich',
    decisionLevel: 'Entscheidungsebene muss ausgewählt werden'
  },
  
  /**
   * Telefonnummern-Normalisierung
   */
  normalizePhoneNumber(phone: string): string {
    if (!phone) return '';
    
    // Entferne alle nicht-numerischen Zeichen außer +
    let normalized = phone.replace(/[^0-9+]/g, '');
    
    // Deutsche Nummern normalisieren
    if (normalized.startsWith('0049')) {
      normalized = '+49' + normalized.substring(4);
    } else if (normalized.startsWith('00')) {
      normalized = '+' + normalized.substring(2);
    } else if (normalized.startsWith('0') && !normalized.startsWith('+')) {
      // Deutsche Nummer ohne Landesvorwahl
      normalized = '+49' + normalized.substring(1);
    }
    
    // Formatierung für bessere Lesbarkeit
    if (normalized.startsWith('+49')) {
      const number = normalized.substring(3);
      const area = number.substring(0, 3);
      const rest = number.substring(3);
      return `+49 ${area} ${rest.match(/.{1,4}/g)?.join(' ') || rest}`;
    }
    
    return normalized;
  },
  
  /**
   * Validierung internationaler Telefonnummern
   */
  validateInternationalPhone(phone: string): boolean {
    // E.164 Format: +[country code][number] max 15 digits
    const e164Regex = /^\+[1-9]\d{1,14}$/;
    const normalized = this.normalizePhoneNumber(phone).replace(/\s/g, '');
    return e164Regex.test(normalized);
  }
};

// Email uniqueness check
export const checkEmailUniqueness = async (
  customerId: string, 
  email: string, 
  excludeContactId?: string
): Promise<boolean> => {
  try {
    const response = await api.post(`/customers/${customerId}/contacts/check-email`, {
      email,
      excludeContactId
    });
    return response.data.isUnique;
  } catch (error) {
    console.error('Email check failed:', error);
    return true; // Assume unique on error
  }
};
```

### 6. Empty State Component

```typescript
// components/shared/EmptyState.tsx

export const EmptyState: React.FC<{
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: {
    label: string;
    onClick: () => void;
    startIcon?: React.ReactNode;
  };
}> = ({ icon, title, description, action }) => {
  return (
    <Box
      sx={{
        textAlign: 'center',
        py: 8,
        px: 3,
        bgcolor: 'background.paper',
        borderRadius: 2,
        border: '2px dashed',
        borderColor: 'divider',
        transition: 'all 0.3s ease',
        '&:hover': {
          borderColor: 'primary.main',
          bgcolor: 'action.hover'
        }
      }}
    >
      {icon && (
        <Box sx={{ mb: 3 }}>
          {icon}
        </Box>
      )}
      
      <Typography variant="h6" gutterBottom>
        {title}
      </Typography>
      
      {description && (
        <Typography variant="body2" color="text.secondary" paragraph>
          {description}
        </Typography>
      )}
      
      {action && (
        <Button
          variant="contained"
          startIcon={action.startIcon}
          onClick={action.onClick}
          size="large"
          sx={{ mt: 2 }}
        >
          {action.label}
        </Button>
      )}
    </Box>
  );
};
```

### 7. Error Handling

```typescript
// hooks/useContactError.ts

export const useContactError = () => {
  const [error, setError] = useState<string | null>(null);
  
  const handleError = useCallback((error: any) => {
    if (error.response?.data?.message) {
      setError(error.response.data.message);
    } else if (error.message) {
      setError(error.message);
    } else {
      setError('Ein unerwarteter Fehler ist aufgetreten');
    }
    
    // Auto-clear nach 5 Sekunden
    setTimeout(() => setError(null), 5000);
  }, []);
  
  return { error, setError, handleError };
};
```

### 8. Loading States

```typescript
// components/shared/ContactSkeleton.tsx

export const ContactSkeleton: React.FC = () => {
  return (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="flex-start" gap={2}>
          <Skeleton variant="circular" width={64} height={64} />
          <Box flex={1}>
            <Skeleton variant="text" width="60%" height={32} />
            <Skeleton variant="text" width="40%" />
            <Box mt={2}>
              <Skeleton variant="text" width="80%" />
              <Skeleton variant="text" width="70%" />
            </Box>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### 9. ResponsibilitySelector Component (aus V2)

```typescript
// components/ResponsibilitySelector.tsx

export const ResponsibilitySelector: React.FC<{
  scope: 'all' | 'specific';
  selectedLocationIds?: string[];
  locations: CustomerLocation[];
  onChange: (scope: 'all' | 'specific', locationIds?: string[]) => void;
  disabled?: boolean;
}> = ({ scope, selectedLocationIds = [], locations, onChange, disabled }) => {
  const isSingleLocation = locations.length === 1;
  
  // Bei Einzelbetrieb automatisch "alle Standorte"
  useEffect(() => {
    if (isSingleLocation && scope !== 'all') {
      onChange('all', [locations[0].id]);
    }
  }, [isSingleLocation, scope, locations, onChange]);
  
  if (isSingleLocation) {
    return (
      <Box sx={{ mt: 2 }}>
        <Typography variant="body2" color="text.secondary">
          📍 Zuständig für: {locations[0].name}
        </Typography>
      </Box>
    );
  }
  
  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="subtitle2" gutterBottom>
        📍 Zuständigkeitsbereich:
      </Typography>
      
      <RadioGroup
        value={scope}
        onChange={(e) => onChange(e.target.value as 'all' | 'specific')}
        disabled={disabled}
      >
        <FormControlLabel
          value="all"
          control={<Radio />}
          label="Für alle Standorte"
        />
        <FormControlLabel
          value="specific"
          control={<Radio />}
          label="Für bestimmte Standorte:"
        />
      </RadioGroup>
      
      {scope === 'specific' && (
        <Box sx={{ ml: 4, mt: 1 }}>
          <LocationCheckboxList
            locations={locations}
            selectedIds={selectedLocationIds}
            onChange={(ids) => onChange('specific', ids)}
            disabled={disabled}
          />
        </Box>
      )}
    </Box>
  );
};

// LocationCheckboxList Component
const LocationCheckboxList: React.FC<{
  locations: CustomerLocation[];
  selectedIds: string[];
  onChange: (ids: string[]) => void;
  disabled?: boolean;
}> = ({ locations, selectedIds, onChange, disabled }) => {
  const handleToggle = (locationId: string) => {
    const newIds = selectedIds.includes(locationId)
      ? selectedIds.filter(id => id !== locationId)
      : [...selectedIds, locationId];
    onChange(newIds);
  };
  
  return (
    <FormGroup>
      {locations.map(location => (
        <FormControlLabel
          key={location.id}
          control={
            <Checkbox
              checked={selectedIds.includes(location.id)}
              onChange={() => handleToggle(location.id)}
              disabled={disabled}
            />
          }
          label={`${location.name} ${location.city ? `(${location.city})` : ''}`}
        />
      ))}
    </FormGroup>
  );
};
```

## 🧪 Comprehensive Testing

```typescript
// stores/__tests__/contactStore.test.ts

describe('ContactStore', () => {
  beforeEach(() => {
    useContactStore.setState({ contacts: [], loading: false, error: null });
  });
  
  it('should handle loading states correctly', async () => {
    const { loadContacts } = useContactStore.getState();
    
    // Start loading
    const promise = loadContacts('customer-1');
    expect(useContactStore.getState().loading).toBe(true);
    
    // Wait for completion
    await promise;
    expect(useContactStore.getState().loading).toBe(false);
  });
  
  it('should handle API errors gracefully', async () => {
    // Mock API error
    jest.spyOn(contactApi, 'getContacts').mockRejectedValue(
      new Error('Network error')
    );
    
    const { loadContacts } = useContactStore.getState();
    await loadContacts('customer-1');
    
    expect(useContactStore.getState().error).toBe('Network error');
  });
  
  it('should validate email uniqueness', async () => {
    const isUnique = await checkEmailUniqueness(
      'customer-1',
      'test@example.com'
    );
    
    expect(isUnique).toBeDefined();
  });
});
```

## 📊 Performance Optimizations

### Optimistic Updates

```typescript
// Store mit optimistischen Updates
addContact: async (customerId, data) => {
  // Optimistic update
  const tempId = `temp-${Date.now()}`;
  const tempContact = { ...data, id: tempId, isPrimary: false };
  
  set((state) => {
    state.contacts.push(tempContact as any);
  });
  
  try {
    const newContact = await contactApi.createContact(customerId, data);
    set((state) => {
      // Replace temp with real
      const index = state.contacts.findIndex(c => c.id === tempId);
      if (index >= 0) {
        state.contacts[index] = newContact;
      }
    });
  } catch (error) {
    // Rollback on error
    set((state) => {
      state.contacts = state.contacts.filter(c => c.id !== tempId);
      state.error = error.message;
    });
  }
}
```

## 🎯 Success Metrics

### Technical:
- **API Response Time:** < 200ms
- **Store Update Time:** < 16ms
- **Type Coverage:** 100%
- **Test Coverage:** > 90%

### Developer Experience:
- **Type Safety:** Full IntelliSense support
- **Error Messages:** Clear and actionable
- **Reusability:** All utilities exportable

## 🔗 Integration Points

### Mit Backend Intelligence:
- Warmth Data Integration
- Interaction Tracking Hooks

### Mit Smart Contact Cards:
- Type-safe Props
- Store Subscriptions

### Mit Mobile Actions:
- API Service Reuse
- Offline Queue Support

---

**Nächster Schritt:** [→ Smart Contact Cards](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/SMART_CONTACT_CARDS.md)

**Foundation = Solide Basis für Innovation! 🏗️✨**