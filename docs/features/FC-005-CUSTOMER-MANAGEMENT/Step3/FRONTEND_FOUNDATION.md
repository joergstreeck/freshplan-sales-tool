# 🏗️ Frontend Foundation - Contact Store & API Integration

**Phase:** 1 - Foundation  
**Tag:** 2 der Woche 1  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BACKEND_INTELLIGENCE.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`

## 🎯 Vision: Robuste Frontend-Architektur

**Frontend Foundation** schafft die **technische Basis** für alle Contact-Features:

> "Ein solides Fundament für intelligente Kontaktverwaltung mit Zustand, Typen und Services"

## ⚠️ VORAUSSETZUNGEN

**BEVOR du beginnst, stelle sicher:**
1. ✅ Backend ContactInteraction Entity ist erstellt
2. ✅ V201 Migration ist ausgeführt
3. ✅ ColdStartDefaults sind implementiert (siehe CRITICAL_SUCCESS_FACTORS)
4. ✅ API Budget-Limits sind konfiguriert

## 🏗️ Architektur-Übersicht

```
┌─────────────────────────────────────────────────┐
│          React Components Layer                  │
│  (Smart Cards, Timeline, Mobile Actions)        │
├─────────────────────────────────────────────────┤
│         useContactStore (Zustand)               │
│  • Contacts State Management                    │
│  • Optimistic Updates                           │
│  • Warmth & Intelligence Data                   │
├─────────────────────────────────────────────────┤
│        Contact API Service Layer                │
│  • Type-safe API calls (TypeScript)            │
│  • Error Handling & Retry Logic                │
│  • Offline Queue Management                     │
├─────────────────────────────────────────────────┤
│       Validation & Transform Layer              │
│  • Zod Schema Validation                        │
│  • Data Transformation                          │
│  • Field Normalization                          │
└─────────────────────────────────────────────────┘
```

## 💻 IMPLEMENTATION

### 1. Contact Types (VOLLSTÄNDIG)

**Pfad:** `frontend/src/features/customers/types/contact.types.ts`

```typescript
// contact.types.ts - Vollständige Type-Definitionen

export interface CustomerContact {
  id: string;
  customerId: string;
  
  // Strukturierte Namensfelder (wie in Step 1 & 2)
  salutation?: 'Herr' | 'Frau' | 'Divers' | '';
  title?: string;
  firstName: string;
  lastName: string;
  
  // Position & Department
  position?: string;
  department?: string;
  
  // Kontaktdaten
  email?: string;
  phone?: string;
  mobile?: string;
  fax?: string;
  
  // Rollen & Hierarchie
  roles: ContactRole[];
  reportsTo?: string; // Contact ID
  directReports?: string[]; // Contact IDs
  
  // Status Flags
  isPrimary: boolean;
  isDecisionMaker: boolean;
  isActive: boolean;
  
  // Kommunikations-Präferenzen
  preferredCommunicationMethod: CommunicationMethod;
  languagePreference: 'DE' | 'EN' | 'FR';
  
  // Location Assignment (Multi-Select)
  responsibilityScope: 'all' | 'specific';
  assignedLocations?: string[]; // Location IDs
  
  // Intelligence Data (NEU)
  lastContactDate?: Date;
  warmthScore?: number; // 0-100
  freshnessLevel?: DataFreshness;
  nextSuggestedAction?: string;
  
  // Soft Delete
  isDeleted: boolean;
  deletedAt?: Date;
  deletedBy?: string;
  
  // Audit
  createdAt: Date;
  createdBy: string;
  updatedAt?: Date;
  updatedBy?: string;
  
  // Notizen
  notes?: string;
}

export interface ContactRole {
  id: string;
  roleName: string;
  description?: string;
  isDecisionMakerRole: boolean;
  permissions?: string[];
}

export enum CommunicationMethod {
  EMAIL = 'EMAIL',
  PHONE = 'PHONE',
  MOBILE = 'MOBILE',
  MEETING = 'MEETING',
  CHAT = 'CHAT'
}

export enum DataFreshness {
  FRESH = 'fresh',        // < 7 Tage
  AGING = 'aging',        // 7-30 Tage
  STALE = 'stale',        // 30-90 Tage
  CRITICAL = 'critical',  // > 90 Tage
  NO_CONTACT = 'no_contact' // Nie kontaktiert
}

// Intelligence Types
export interface ContactIntelligence {
  contactId: string;
  warmthScore: number;
  freshnessLevel: DataFreshness;
  lastInteraction?: ContactInteraction;
  interactionCount30Days: number;
  responseRate: number;
  trendDirection: 'improving' | 'stable' | 'declining';
  suggestions: ActionSuggestion[];
}

export interface ContactInteraction {
  id: string;
  contactId: string;
  customerId: string;
  interactionType: InteractionType;
  interactionTimestamp: Date;
  direction: 'incoming' | 'outgoing' | 'internal';
  subject?: string;
  notes?: string;
  sentiment?: 'very_positive' | 'positive' | 'neutral' | 'negative' | 'very_negative';
  durationMinutes?: number;
  responseTimeHours?: number;
  opportunityId?: string;
  orderId?: string;
  createdBy: string;
  createdAt: Date;
}

export enum InteractionType {
  CALL = 'CALL',
  EMAIL = 'EMAIL',
  MEETING = 'MEETING',
  NOTE = 'NOTE',
  VISIT = 'VISIT',
  CHAT = 'CHAT',
  SOCIAL_MEDIA = 'SOCIAL_MEDIA',
  DOCUMENT_SENT = 'DOCUMENT_SENT',
  SYSTEM_EVENT = 'SYSTEM_EVENT'
}

export interface ActionSuggestion {
  id: string;
  priority: 'high' | 'medium' | 'low';
  type: 'contact' | 'update' | 'opportunity' | 'relationship';
  title: string;
  description: string;
  icon?: string;
  actionUrl?: string;
  dueDate?: Date;
}
```

### 2. Zustand Store (VOLLSTÄNDIG)

**Pfad:** `frontend/src/features/customers/stores/contactStore.ts`

```typescript
// contactStore.ts - Zustand State Management

import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import { CustomerContact, ContactIntelligence, ContactInteraction } from '../types/contact.types';
import { contactApi } from '../services/contactApi';

interface ContactState {
  // State
  contacts: Record<string, CustomerContact>;
  intelligence: Record<string, ContactIntelligence>;
  interactions: Record<string, ContactInteraction[]>;
  selectedContactId: string | null;
  isLoading: boolean;
  error: string | null;
  
  // Filters & Sorting
  filters: {
    customerId?: string;
    isPrimary?: boolean;
    isActive?: boolean;
    freshnessLevel?: string[];
    warmthRange?: [number, number];
    searchTerm?: string;
  };
  sortBy: 'name' | 'warmth' | 'lastContact' | 'position';
  sortDirection: 'asc' | 'desc';
  
  // Actions - CRUD
  fetchContacts: (customerId: string) => Promise<void>;
  fetchContactIntelligence: (contactId: string) => Promise<void>;
  createContact: (contact: Partial<CustomerContact>) => Promise<CustomerContact>;
  updateContact: (id: string, updates: Partial<CustomerContact>) => Promise<void>;
  deleteContact: (id: string, soft?: boolean) => Promise<void>;
  
  // Actions - Intelligence
  refreshWarmthScore: (contactId: string) => Promise<void>;
  logInteraction: (interaction: Partial<ContactInteraction>) => Promise<void>;
  dismissSuggestion: (contactId: string, suggestionId: string) => Promise<void>;
  
  // Actions - Bulk Operations
  bulkUpdateContacts: (ids: string[], updates: Partial<CustomerContact>) => Promise<void>;
  bulkAssignLocations: (contactIds: string[], locationIds: string[]) => Promise<void>;
  
  // Actions - UI State
  selectContact: (contactId: string | null) => void;
  setFilters: (filters: ContactState['filters']) => void;
  setSorting: (sortBy: ContactState['sortBy'], direction?: ContactState['sortDirection']) => void;
  clearError: () => void;
  
  // Computed Getters
  getContact: (id: string) => CustomerContact | undefined;
  getFilteredContacts: () => CustomerContact[];
  getPrimaryContact: (customerId: string) => CustomerContact | undefined;
  getContactsByLocation: (locationId: string) => CustomerContact[];
  getContactsByWarmth: (min: number, max: number) => CustomerContact[];
}

export const useContactStore = create<ContactState>()(
  devtools(
    persist(
      immer((set, get) => ({
        // Initial State
        contacts: {},
        intelligence: {},
        interactions: {},
        selectedContactId: null,
        isLoading: false,
        error: null,
        filters: {},
        sortBy: 'name',
        sortDirection: 'asc',
        
        // Fetch Contacts
        fetchContacts: async (customerId: string) => {
          set((state) => {
            state.isLoading = true;
            state.error = null;
          });
          
          try {
            const contacts = await contactApi.getContactsByCustomer(customerId);
            
            set((state) => {
              contacts.forEach(contact => {
                state.contacts[contact.id] = contact;
              });
              state.isLoading = false;
            });
            
            // Fetch intelligence data in background
            contacts.forEach(contact => {
              get().fetchContactIntelligence(contact.id);
            });
          } catch (error) {
            set((state) => {
              state.error = error instanceof Error ? error.message : 'Failed to fetch contacts';
              state.isLoading = false;
            });
          }
        },
        
        // Fetch Intelligence
        fetchContactIntelligence: async (contactId: string) => {
          try {
            const intelligence = await contactApi.getContactIntelligence(contactId);
            
            set((state) => {
              state.intelligence[contactId] = intelligence;
              
              // Update contact with intelligence data
              if (state.contacts[contactId]) {
                state.contacts[contactId].warmthScore = intelligence.warmthScore;
                state.contacts[contactId].freshnessLevel = intelligence.freshnessLevel;
              }
            });
          } catch (error) {
            console.error('Failed to fetch intelligence:', error);
          }
        },
        
        // Create Contact
        createContact: async (contactData: Partial<CustomerContact>) => {
          set((state) => {
            state.isLoading = true;
            state.error = null;
          });
          
          try {
            // Apply ColdStartDefaults
            const contactWithDefaults = {
              ...contactData,
              warmthScore: contactData.warmthScore ?? 50,
              freshnessLevel: contactData.freshnessLevel ?? 'no_contact',
              isActive: contactData.isActive ?? true,
              isPrimary: contactData.isPrimary ?? false,
              preferredCommunicationMethod: contactData.preferredCommunicationMethod ?? 'EMAIL',
              languagePreference: contactData.languagePreference ?? 'DE',
            };
            
            const newContact = await contactApi.createContact(contactWithDefaults);
            
            set((state) => {
              state.contacts[newContact.id] = newContact;
              state.isLoading = false;
            });
            
            // Log creation as system event
            await get().logInteraction({
              contactId: newContact.id,
              customerId: newContact.customerId,
              interactionType: 'SYSTEM_EVENT',
              subject: 'Kontakt erstellt',
              notes: `${newContact.firstName} ${newContact.lastName} wurde als neuer Kontakt angelegt`,
            });
            
            return newContact;
          } catch (error) {
            set((state) => {
              state.error = error instanceof Error ? error.message : 'Failed to create contact';
              state.isLoading = false;
            });
            throw error;
          }
        },
        
        // Update Contact
        updateContact: async (id: string, updates: Partial<CustomerContact>) => {
          // Optimistic update
          set((state) => {
            if (state.contacts[id]) {
              Object.assign(state.contacts[id], updates);
            }
          });
          
          try {
            const updatedContact = await contactApi.updateContact(id, updates);
            
            set((state) => {
              state.contacts[id] = updatedContact;
              
              // Update lastContactDate if communication happened
              if (updates.lastContactDate) {
                get().refreshWarmthScore(id);
              }
            });
          } catch (error) {
            // Rollback optimistic update
            const originalContact = await contactApi.getContact(id);
            set((state) => {
              state.contacts[id] = originalContact;
              state.error = error instanceof Error ? error.message : 'Failed to update contact';
            });
            throw error;
          }
        },
        
        // Delete Contact (Soft by default)
        deleteContact: async (id: string, soft = true) => {
          if (soft) {
            await get().updateContact(id, {
              isDeleted: true,
              deletedAt: new Date(),
              deletedBy: 'current-user', // TODO: Get from auth context
            });
          } else {
            set((state) => {
              delete state.contacts[id];
              delete state.intelligence[id];
              delete state.interactions[id];
            });
            
            await contactApi.deleteContact(id);
          }
        },
        
        // Refresh Warmth Score
        refreshWarmthScore: async (contactId: string) => {
          try {
            const warmthData = await contactApi.calculateWarmth(contactId);
            
            set((state) => {
              if (state.contacts[contactId]) {
                state.contacts[contactId].warmthScore = warmthData.score;
                state.contacts[contactId].freshnessLevel = warmthData.freshnessLevel;
              }
              
              // Update intelligence
              if (state.intelligence[contactId]) {
                state.intelligence[contactId].warmthScore = warmthData.score;
                state.intelligence[contactId].freshnessLevel = warmthData.freshnessLevel;
                state.intelligence[contactId].suggestions = warmthData.suggestions;
              }
            });
          } catch (error) {
            console.error('Failed to refresh warmth score:', error);
          }
        },
        
        // Log Interaction
        logInteraction: async (interaction: Partial<ContactInteraction>) => {
          try {
            const newInteraction = await contactApi.logInteraction(interaction);
            
            set((state) => {
              const contactId = newInteraction.contactId;
              if (!state.interactions[contactId]) {
                state.interactions[contactId] = [];
              }
              state.interactions[contactId].unshift(newInteraction);
              
              // Update lastContactDate
              if (state.contacts[contactId]) {
                state.contacts[contactId].lastContactDate = newInteraction.interactionTimestamp;
              }
            });
            
            // Trigger warmth recalculation
            await get().refreshWarmthScore(newInteraction.contactId);
          } catch (error) {
            console.error('Failed to log interaction:', error);
            throw error;
          }
        },
        
        // Dismiss Suggestion
        dismissSuggestion: async (contactId: string, suggestionId: string) => {
          set((state) => {
            if (state.intelligence[contactId]) {
              state.intelligence[contactId].suggestions = 
                state.intelligence[contactId].suggestions.filter(s => s.id !== suggestionId);
            }
          });
          
          await contactApi.dismissSuggestion(contactId, suggestionId);
        },
        
        // Bulk Update
        bulkUpdateContacts: async (ids: string[], updates: Partial<CustomerContact>) => {
          set((state) => {
            ids.forEach(id => {
              if (state.contacts[id]) {
                Object.assign(state.contacts[id], updates);
              }
            });
          });
          
          try {
            await contactApi.bulkUpdateContacts(ids, updates);
          } catch (error) {
            // TODO: Rollback
            throw error;
          }
        },
        
        // Bulk Assign Locations
        bulkAssignLocations: async (contactIds: string[], locationIds: string[]) => {
          await get().bulkUpdateContacts(contactIds, {
            responsibilityScope: 'specific',
            assignedLocations: locationIds,
          });
        },
        
        // UI Actions
        selectContact: (contactId: string | null) => {
          set((state) => {
            state.selectedContactId = contactId;
          });
        },
        
        setFilters: (filters: ContactState['filters']) => {
          set((state) => {
            state.filters = filters;
          });
        },
        
        setSorting: (sortBy: ContactState['sortBy'], direction?: ContactState['sortDirection']) => {
          set((state) => {
            state.sortBy = sortBy;
            state.sortDirection = direction ?? state.sortDirection;
          });
        },
        
        clearError: () => {
          set((state) => {
            state.error = null;
          });
        },
        
        // Getters
        getContact: (id: string) => get().contacts[id],
        
        getFilteredContacts: () => {
          const { contacts, filters, sortBy, sortDirection } = get();
          let filtered = Object.values(contacts);
          
          // Apply filters
          if (filters.customerId) {
            filtered = filtered.filter(c => c.customerId === filters.customerId);
          }
          if (filters.isPrimary !== undefined) {
            filtered = filtered.filter(c => c.isPrimary === filters.isPrimary);
          }
          if (filters.isActive !== undefined) {
            filtered = filtered.filter(c => c.isActive === filters.isActive);
          }
          if (filters.freshnessLevel?.length) {
            filtered = filtered.filter(c => 
              c.freshnessLevel && filters.freshnessLevel!.includes(c.freshnessLevel)
            );
          }
          if (filters.warmthRange) {
            const [min, max] = filters.warmthRange;
            filtered = filtered.filter(c => 
              c.warmthScore !== undefined && c.warmthScore >= min && c.warmthScore <= max
            );
          }
          if (filters.searchTerm) {
            const term = filters.searchTerm.toLowerCase();
            filtered = filtered.filter(c => 
              c.firstName.toLowerCase().includes(term) ||
              c.lastName.toLowerCase().includes(term) ||
              c.email?.toLowerCase().includes(term) ||
              c.position?.toLowerCase().includes(term)
            );
          }
          
          // Sort
          filtered.sort((a, b) => {
            let comparison = 0;
            
            switch (sortBy) {
              case 'name':
                comparison = `${a.lastName} ${a.firstName}`.localeCompare(`${b.lastName} ${b.firstName}`);
                break;
              case 'warmth':
                comparison = (a.warmthScore ?? 0) - (b.warmthScore ?? 0);
                break;
              case 'lastContact':
                comparison = (a.lastContactDate?.getTime() ?? 0) - (b.lastContactDate?.getTime() ?? 0);
                break;
              case 'position':
                comparison = (a.position ?? '').localeCompare(b.position ?? '');
                break;
            }
            
            return sortDirection === 'desc' ? -comparison : comparison;
          });
          
          return filtered;
        },
        
        getPrimaryContact: (customerId: string) => {
          return Object.values(get().contacts).find(
            c => c.customerId === customerId && c.isPrimary
          );
        },
        
        getContactsByLocation: (locationId: string) => {
          return Object.values(get().contacts).filter(
            c => c.assignedLocations?.includes(locationId)
          );
        },
        
        getContactsByWarmth: (min: number, max: number) => {
          return Object.values(get().contacts).filter(
            c => c.warmthScore !== undefined && c.warmthScore >= min && c.warmthScore <= max
          );
        },
      })),
      {
        name: 'contact-store',
        partialize: (state) => ({
          // Only persist UI state, not data
          filters: state.filters,
          sortBy: state.sortBy,
          sortDirection: state.sortDirection,
        }),
      }
    )
  )
);
```

### 3. API Service Layer

**Pfad:** `frontend/src/features/customers/services/contactApi.ts`

```typescript
// contactApi.ts - Type-safe API Service

import { httpClient } from '@/services/httpClient';
import { 
  CustomerContact, 
  ContactIntelligence, 
  ContactInteraction 
} from '../types/contact.types';
import { z } from 'zod';

// Validation Schemas
const ContactSchema = z.object({
  firstName: z.string().min(1, 'Vorname ist erforderlich'),
  lastName: z.string().min(1, 'Nachname ist erforderlich'),
  email: z.string().email('Ungültige E-Mail').optional().or(z.literal('')),
  phone: z.string().optional(),
  mobile: z.string().optional(),
  position: z.string().optional(),
  department: z.string().optional(),
});

class ContactApiService {
  private baseUrl = '/api/contacts';
  
  // CRUD Operations
  async getContactsByCustomer(customerId: string): Promise<CustomerContact[]> {
    const response = await httpClient.get<CustomerContact[]>(
      `${this.baseUrl}/customer/${customerId}`
    );
    return response.data;
  }
  
  async getContact(id: string): Promise<CustomerContact> {
    const response = await httpClient.get<CustomerContact>(`${this.baseUrl}/${id}`);
    return response.data;
  }
  
  async createContact(contact: Partial<CustomerContact>): Promise<CustomerContact> {
    // Validate before sending
    const validation = ContactSchema.safeParse(contact);
    if (!validation.success) {
      throw new Error(validation.error.errors[0].message);
    }
    
    const response = await httpClient.post<CustomerContact>(this.baseUrl, contact);
    return response.data;
  }
  
  async updateContact(id: string, updates: Partial<CustomerContact>): Promise<CustomerContact> {
    const response = await httpClient.patch<CustomerContact>(
      `${this.baseUrl}/${id}`,
      updates
    );
    return response.data;
  }
  
  async deleteContact(id: string): Promise<void> {
    await httpClient.delete(`${this.baseUrl}/${id}`);
  }
  
  // Intelligence Operations
  async getContactIntelligence(contactId: string): Promise<ContactIntelligence> {
    const response = await httpClient.get<ContactIntelligence>(
      `${this.baseUrl}/${contactId}/intelligence`
    );
    return response.data;
  }
  
  async calculateWarmth(contactId: string): Promise<{
    score: number;
    freshnessLevel: string;
    suggestions: any[];
  }> {
    const response = await httpClient.post(
      `${this.baseUrl}/${contactId}/calculate-warmth`
    );
    return response.data;
  }
  
  // Interaction Operations
  async logInteraction(interaction: Partial<ContactInteraction>): Promise<ContactInteraction> {
    const response = await httpClient.post<ContactInteraction>(
      '/api/contact-interactions',
      interaction
    );
    return response.data;
  }
  
  async getInteractions(contactId: string): Promise<ContactInteraction[]> {
    const response = await httpClient.get<ContactInteraction[]>(
      `/api/contact-interactions/contact/${contactId}`
    );
    return response.data;
  }
  
  // Bulk Operations
  async bulkUpdateContacts(
    ids: string[], 
    updates: Partial<CustomerContact>
  ): Promise<void> {
    await httpClient.patch(`${this.baseUrl}/bulk`, {
      ids,
      updates,
    });
  }
  
  // Suggestions
  async dismissSuggestion(contactId: string, suggestionId: string): Promise<void> {
    await httpClient.post(
      `${this.baseUrl}/${contactId}/suggestions/${suggestionId}/dismiss`
    );
  }
  
  // Search & Filter
  async searchContacts(params: {
    term?: string;
    customerId?: string;
    isPrimary?: boolean;
    freshnessLevel?: string[];
    warmthRange?: [number, number];
    limit?: number;
    offset?: number;
  }): Promise<{
    contacts: CustomerContact[];
    total: number;
  }> {
    const response = await httpClient.get(`${this.baseUrl}/search`, { params });
    return response.data;
  }
}

export const contactApi = new ContactApiService();
```

### 4. Custom Hooks

**Pfad:** `frontend/src/features/customers/hooks/useContacts.ts`

```typescript
// useContacts.ts - Custom Hooks für Contact Management

import { useEffect, useCallback } from 'react';
import { useContactStore } from '../stores/contactStore';
import { useDebounce } from '@/hooks/useDebounce';

// Main Contact Hook
export function useContacts(customerId?: string) {
  const {
    contacts,
    isLoading,
    error,
    fetchContacts,
    getFilteredContacts,
  } = useContactStore();
  
  useEffect(() => {
    if (customerId) {
      fetchContacts(customerId);
    }
  }, [customerId, fetchContacts]);
  
  const filteredContacts = getFilteredContacts();
  
  return {
    contacts: filteredContacts,
    isLoading,
    error,
    refetch: () => customerId && fetchContacts(customerId),
  };
}

// Primary Contact Hook
export function usePrimaryContact(customerId: string) {
  const { getPrimaryContact, updateContact } = useContactStore();
  
  const primaryContact = getPrimaryContact(customerId);
  
  const setPrimaryContact = useCallback(async (contactId: string) => {
    // Remove primary from current
    if (primaryContact) {
      await updateContact(primaryContact.id, { isPrimary: false });
    }
    
    // Set new primary
    await updateContact(contactId, { isPrimary: true });
  }, [primaryContact, updateContact]);
  
  return {
    primaryContact,
    setPrimaryContact,
  };
}

// Contact Search Hook
export function useContactSearch() {
  const { setFilters, filters } = useContactStore();
  const debouncedSearchTerm = useDebounce(filters.searchTerm, 300);
  
  useEffect(() => {
    if (debouncedSearchTerm !== filters.searchTerm) {
      setFilters({ ...filters, searchTerm: debouncedSearchTerm });
    }
  }, [debouncedSearchTerm]);
  
  const setSearchTerm = useCallback((term: string) => {
    setFilters({ ...filters, searchTerm: term });
  }, [filters, setFilters]);
  
  return {
    searchTerm: filters.searchTerm ?? '',
    setSearchTerm,
  };
}

// Warmth Analytics Hook
export function useWarmthAnalytics(customerId: string) {
  const contacts = useContacts(customerId).contacts;
  
  const analytics = {
    hot: contacts.filter(c => (c.warmthScore ?? 0) >= 75).length,
    warm: contacts.filter(c => {
      const score = c.warmthScore ?? 0;
      return score >= 50 && score < 75;
    }).length,
    cool: contacts.filter(c => {
      const score = c.warmthScore ?? 0;
      return score >= 25 && score < 50;
    }).length,
    cold: contacts.filter(c => (c.warmthScore ?? 0) < 25).length,
    noData: contacts.filter(c => c.warmthScore === undefined).length,
  };
  
  const needsAttention = contacts.filter(c => 
    c.freshnessLevel === 'critical' || 
    c.freshnessLevel === 'stale'
  );
  
  return {
    analytics,
    needsAttention,
    averageWarmth: contacts.length > 0
      ? contacts.reduce((sum, c) => sum + (c.warmthScore ?? 50), 0) / contacts.length
      : 0,
  };
}

// Contact Actions Hook
export function useContactActions(contactId: string) {
  const {
    updateContact,
    deleteContact,
    logInteraction,
    refreshWarmthScore,
  } = useContactStore();
  
  const callContact = useCallback(async () => {
    await logInteraction({
      contactId,
      interactionType: 'CALL',
      direction: 'outgoing',
      subject: 'Ausgehender Anruf',
    });
    
    await updateContact(contactId, {
      lastContactDate: new Date(),
    });
  }, [contactId, logInteraction, updateContact]);
  
  const emailContact = useCallback(async (subject: string) => {
    await logInteraction({
      contactId,
      interactionType: 'EMAIL',
      direction: 'outgoing',
      subject,
    });
    
    await updateContact(contactId, {
      lastContactDate: new Date(),
    });
  }, [contactId, logInteraction, updateContact]);
  
  const scheduleFollowUp = useCallback(async (date: Date, notes: string) => {
    await logInteraction({
      contactId,
      interactionType: 'NOTE',
      subject: 'Follow-up geplant',
      notes,
    });
  }, [contactId, logInteraction]);
  
  return {
    callContact,
    emailContact,
    scheduleFollowUp,
    deleteContact: () => deleteContact(contactId),
    refreshWarmth: () => refreshWarmthScore(contactId),
  };
}
```

## 🧪 TESTING STRATEGY

### Unit Tests

**Pfad:** `frontend/src/features/customers/stores/__tests__/contactStore.test.ts`

```typescript
import { renderHook, act, waitFor } from '@testing-library/react';
import { useContactStore } from '../contactStore';
import { contactApi } from '../../services/contactApi';

jest.mock('../../services/contactApi');

describe('ContactStore', () => {
  beforeEach(() => {
    useContactStore.setState({
      contacts: {},
      intelligence: {},
      interactions: {},
      isLoading: false,
      error: null,
    });
  });
  
  describe('fetchContacts', () => {
    it('should fetch and store contacts', async () => {
      const mockContacts = [
        { id: '1', firstName: 'John', lastName: 'Doe' },
        { id: '2', firstName: 'Jane', lastName: 'Smith' },
      ];
      
      (contactApi.getContactsByCustomer as jest.Mock).mockResolvedValue(mockContacts);
      
      const { result } = renderHook(() => useContactStore());
      
      await act(async () => {
        await result.current.fetchContacts('customer-1');
      });
      
      expect(result.current.contacts['1']).toEqual(mockContacts[0]);
      expect(result.current.contacts['2']).toEqual(mockContacts[1]);
    });
  });
  
  describe('createContact with ColdStartDefaults', () => {
    it('should apply default values for new contacts', async () => {
      const newContact = {
        firstName: 'New',
        lastName: 'Contact',
        customerId: 'customer-1',
      };
      
      const { result } = renderHook(() => useContactStore());
      
      await act(async () => {
        const created = await result.current.createContact(newContact);
        
        expect(created.warmthScore).toBe(50); // ColdStartDefault
        expect(created.freshnessLevel).toBe('no_contact');
        expect(created.isActive).toBe(true);
      });
    });
  });
});
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Sofort zu erledigen:
- [ ] Contact Types erstellen (contact.types.ts)
- [ ] Zustand Store implementieren (contactStore.ts)
- [ ] API Service Layer (contactApi.ts)
- [ ] Custom Hooks (useContacts.ts)
- [ ] Unit Tests schreiben

### Integration:
- [ ] Mit Backend API verbinden
- [ ] Error Boundary hinzufügen
- [ ] Loading States implementieren
- [ ] Offline Queue Manager

### Performance:
- [ ] React.memo für Contact Components
- [ ] Virtual Scrolling für große Listen
- [ ] Debounced Search
- [ ] Optimistic Updates

## 🔗 NÄCHSTE SCHRITTE

Nach erfolgreicher Implementation:
1. **Smart Contact Cards** UI erstellen
2. **Contact Timeline** Component entwickeln
3. **Mobile Actions** implementieren

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Nächstes Dokument:** [→ Smart Contact Cards](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md)