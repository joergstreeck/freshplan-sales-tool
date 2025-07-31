# 🎨 Tag 2: Frontend Foundation

**Datum:** Tag 2 der Step 3 Implementation  
**Fokus:** Types, Store & API Integration  
**Ziel:** Frontend-Basis für Multi-Contact  

## 🧭 Navigation

**← Vorher:** [Backend Contact](./BACKEND_CONTACT.md)  
**↑ Übersicht:** [Step 3 Guide](./README.md)  
**→ Nächster:** [Multi-Contact UI](./FRONTEND_MULTICONTACT.md)  

## 🎯 Tagesziel

Frontend Foundation mit:
- TypeScript Types
- Zustand Store Extension
- API Service
- Field Catalog Erweiterung

## 💻 Implementation

### 1. Contact Types

```typescript
// types/contact.types.ts

export interface Contact {
  id: string;
  customerId: string;
  
  // Basic Info
  salutation?: string;
  title?: string;
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
  
  // Location
  assignedLocationId?: string;
  assignedLocationName?: string; // Denormalized for display
  
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
          assignedLocationId: locationId
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

## 🔗 Nächste Schritte

**Morgen:** [Multi-Contact UI](./FRONTEND_MULTICONTACT.md) - Contact Cards implementieren

---

**Status:** 📋 Bereit zur Implementierung