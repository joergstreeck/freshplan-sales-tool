# 🔧 Technical Debt: Contact API Integration Gap (Sprint 2.1.7.2 Phase 4)

**📅 Erstellt:** 2025-11-02
**🚨 Severity:** MEDIUM
**📋 Sprint:** 2.1.7.7 (Fix für Gap aus Sprint 2.1.7.2)
**⏱️ Effort:** 15-20 Minuten
**✅ Status:** 📋 DOCUMENTED - Ready for Implementation

---

## 📋 EXECUTIVE SUMMARY

**Problem:** Contact Create/Update/Delete API-Calls sind seit Sprint 2.1.7.2 D11 (Customer Detail Cockpit) **NICHT implementiert** - nur Console.log statt echte Backend-Requests.

**Impact:**
- ❌ Lead Contacts: DecisionLevel kann NICHT gespeichert werden (MUI Enum Violation)
- ❌ Customer Contacts: DecisionLevel kann NICHT gespeichert werden
- ❌ Alle Contact-Edits gehen verloren (nur in UI State, nicht in DB)

**Root Cause:** Sprint 2.1.7.2 Phase 4 Plan war **unvollständig** - fokussierte auf "Polish" statt "Complete".

**Solution:** Contact API Hooks implementieren (useCustomerContacts, useLeadContacts).

---

## 🔍 ROOT CAUSE ANALYSIS

### Git History

**Commit:** `e7b489915` - "feat(sprint-2.1.7.2-d11): Customer Detail Cockpit Pattern - Complete"

**File:** `frontend/src/features/customers/components/detail/CustomerDetailTabVerlauf.tsx`

**Lines 98-116:**
```typescript
// Handle submit contact
const handleSubmitContact = async (contactData: Partial<Contact>) => {
  try {
    if (selectedContact) {
      // Update existing
      // TODO: Implement API call in Phase 4  ← DAS IST DAS PROBLEM!
      // await updateContact(customerId, selectedContact.id!, contactData);
      console.log('Update contact:', selectedContact.id, contactData);  // ❌ NUR CONSOLE.LOG!
    } else {
      // Create new
      // TODO: Implement API call in Phase 4
      // await createContact(customerId, contactData);
      console.log('Create contact:', contactData);  // ❌ NUR CONSOLE.LOG!
    }
    // refetch();  // ❌ AUCH AUSKOMMENTIERT!
    setContactDialogOpen(false);
  } catch (error) {
    console.error('Error saving contact:', error);
    throw error;
  }
};
```

### Warum wurde es nicht gemacht?

**Sprint 2.1.7.2 D11 Phase 4 Plan:** (SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md:522-527)

```markdown
### Phase 4: Polish & Test (30 min)
1. Responsive testing
2. Clean up old components (CompactView, Modal)
3. Remove Phase 3 banners from tabs
4. Final UX testing
```

**Gap:** Kein expliziter Step "Connect Contact API to Component"!

**Lesson Learned:**
- ✅ UI Struktur wurde in Phase 3 erstellt
- ✅ Backend API existiert seit Sprint 2.1.7.2
- ❌ Verknüpfung (Hook) wurde vergessen
- ❌ Phase 4 fokussierte auf "Polish", nicht "Complete"

---

## 📦 WHAT EXISTS (Backend)

### Backend Contact API - FUNKTIONIERT BEREITS! ✅

**File:** `backend/.../api/ContactResource.java`

```java
@Path("/api/customers/{customerId}/contacts")
public class ContactResource {

    @GET
    public List<ContactDTO> listContacts(@PathParam("customerId") UUID customerId) {
        // ✅ IMPLEMENTIERT - liefert alle Kontakte
    }

    @POST
    public ContactDTO createContact(
        @PathParam("customerId") UUID customerId,
        @Valid ContactDTO contact
    ) {
        // ✅ IMPLEMENTIERT - erstellt neuen Kontakt
    }

    @PUT
    @Path("/{contactId}")
    public ContactDTO updateContact(
        @PathParam("customerId") UUID customerId,
        @PathParam("contactId") UUID contactId,
        @Valid ContactDTO contact
    ) {
        // ✅ IMPLEMENTIERT - updated Kontakt (inkl. DecisionLevel!)
    }

    @DELETE
    @Path("/{contactId}")
    public void deleteContact(
        @PathParam("customerId") UUID customerId,
        @PathParam("contactId") UUID contactId
    ) {
        // ✅ IMPLEMENTIERT - löscht Kontakt
    }
}
```

**Endpoints:**
- `GET /api/customers/{id}/contacts` ✅
- `POST /api/customers/{id}/contacts` ✅
- `PUT /api/customers/{id}/contacts/{contactId}` ✅
- `DELETE /api/customers/{id}/contacts/{contactId}` ✅

**Backend DecisionLevel Enum:** ✅ 4 Werte (EXECUTIVE, MANAGER, OPERATIONAL, INFLUENCER)

---

## ❌ WHAT'S MISSING (Frontend)

### 1. Customer Contacts - Hook fehlt

**File:** `frontend/src/features/customers/hooks/useCustomerContacts.ts`

**Status:** ❌ EXISTIERT NICHT!

**Needed:**
```typescript
export function useCustomerContacts(customerId: string) {
  return useQuery({
    queryKey: ['customers', customerId, 'contacts'],
    queryFn: async () => {
      const response = await httpClient.get(`/api/customers/${customerId}/contacts`);
      return response.data;
    },
    enabled: !!customerId,
  });
}
```

### 2. Customer Contacts - API Client Functions fehlen

**File:** `frontend/src/features/customers/services/contactApi.ts`

**Status:** ✅ EXISTIERT (aber NICHT genutzt!)

**Existing Functions:**
```typescript
updateContact: async (
  customerId: string,
  contactId: string,
  updates: UpdateContactDTO
): Promise<Contact> => {
  const response = await apiClient.put(`/customers/${customerId}/contacts/${contactId}`, updates);
  return response.data;
}

createContact: async (
  customerId: string,
  contact: CreateContactDTO
): Promise<Contact> => {
  const response = await apiClient.post(`/customers/${customerId}/contacts`, contact);
  return response.data;
}

deleteContact: async (
  customerId: string,
  contactId: string
): Promise<void> => {
  await apiClient.delete(`/customers/${customerId}/contacts/${contactId}`);
}
```

**Problem:** Diese Funktionen werden NIEMALS aufgerufen!

### 3. Lead Contacts - Gleiche Situation

**File:** `frontend/src/features/leads/components/LeadDetailPage.tsx`

**Status:** Lead Contacts haben vermutlich die GLEICHEN TODOs!

---

## ✅ SOLUTION - Implementation Plan

### Task 1: useCustomerContacts Hook (5 min)

**File:** `frontend/src/features/customers/hooks/useCustomerContacts.ts` (NEW)

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { httpClient } from '../../../lib/httpClient';
import type { Contact } from '../components/detail/ContactEditDialog';

export function useCustomerContacts(customerId: string) {
  return useQuery({
    queryKey: ['customers', customerId, 'contacts'],
    queryFn: async () => {
      const response = await httpClient.get<Contact[]>(
        `/api/customers/${customerId}/contacts`
      );
      return response.data;
    },
    enabled: !!customerId,
  });
}

export function useCreateCustomerContact(customerId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (contact: Partial<Contact>) => {
      const response = await httpClient.post(
        `/api/customers/${customerId}/contacts`,
        contact
      );
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers', customerId, 'contacts'] });
    },
  });
}

export function useUpdateCustomerContact(customerId: string, contactId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (updates: Partial<Contact>) => {
      const response = await httpClient.put(
        `/api/customers/${customerId}/contacts/${contactId}`,
        updates
      );
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers', customerId, 'contacts'] });
    },
  });
}

export function useDeleteCustomerContact(customerId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (contactId: string) => {
      await httpClient.delete(
        `/api/customers/${customerId}/contacts/${contactId}`
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers', customerId, 'contacts'] });
    },
  });
}
```

### Task 2: Integrate in CustomerDetailTabVerlauf.tsx (5 min)

**File:** `frontend/src/features/customers/components/detail/CustomerDetailTabVerlauf.tsx`

**BEFORE (Lines 57-64):**
```typescript
// State
const [contacts, setContacts] = useState<Contact[]>([]); // ❌ HARDCODED EMPTY!
const [isLoading, setIsLoading] = useState(false);       // ❌ HARDCODED FALSE!

// TODO: Replace with real API hook in Phase 4  ← LÖSCHEN!
// const { data: contacts, isLoading, refetch } = useCustomerContacts(customerId);
```

**AFTER:**
```typescript
// Hooks
const { data: contacts = [], isLoading } = useCustomerContacts(customerId);
const createContact = useCreateCustomerContact(customerId);
const updateContact = useUpdateCustomerContact(customerId, selectedContact?.id || '');
const deleteContact = useDeleteCustomerContact(customerId);

const [contactDialogOpen, setContactDialogOpen] = useState(false);
const [selectedContact, setSelectedContact] = useState<Contact | null>(null);
```

**Handler Updates (Lines 98-116):**
```typescript
// BEFORE:
const handleSubmitContact = async (contactData: Partial<Contact>) => {
  try {
    if (selectedContact) {
      // TODO: Implement API call in Phase 4
      console.log('Update contact:', selectedContact.id, contactData);
    } else {
      // TODO: Implement API call in Phase 4
      console.log('Create contact:', contactData);
    }
    setContactDialogOpen(false);
  } catch (error) {
    console.error('Error saving contact:', error);
    throw error;
  }
};

// AFTER:
const handleSubmitContact = async (contactData: Partial<Contact>) => {
  try {
    if (selectedContact) {
      await updateContact.mutateAsync(contactData);
    } else {
      await createContact.mutateAsync(contactData);
    }
    setContactDialogOpen(false);
  } catch (error) {
    console.error('Error saving contact:', error);
    throw error;
  }
};

const handleDeleteContact = async (contactId: string) => {
  try {
    await deleteContact.mutateAsync(contactId);
  } catch (error) {
    console.error('Error deleting contact:', error);
    throw error;
  }
};
```

### Task 3: Lead Contacts - Same Pattern (5 min)

**File:** `frontend/src/features/leads/hooks/useLeadContacts.ts` (NEW)

Analog zu useCustomerContacts.ts, aber für Lead Contacts.

### Task 4: Test (5 min)

**Test Plan:**
```bash
# 1. Backend läuft
# Backend ist bereits running

# 2. Frontend läuft
# Frontend ist bereits running

# 3. Test Customer Contact Edit
# - Navigate zu Customer "Betriebsgastronomie TechPark Frankfurt GmbH"
# - Tab "Verlauf" öffnen
# - Contact "Julia Hoffmann" bearbeiten
# - DecisionLevel ändern: EXECUTIVE → MANAGER
# - Speichern
# - ✅ EXPECT: Änderung in DB gespeichert, Page refresh zeigt neue Werte

# 4. Test Lead Contact Edit
# - Navigate zu Lead (Anna Lehmann)
# - Contact bearbeiten
# - DecisionLevel ändern
# - Speichern
# - ✅ EXPECT: Keine MUI Enum Violation mehr!
```

---

## 📊 EFFORT ESTIMATE

**Total:** 15-20 Minuten

- Task 1: useCustomerContacts Hook (5 min)
- Task 2: CustomerDetailTabVerlauf Integration (5 min)
- Task 3: Lead Contacts (5 min)
- Task 4: Testing (5 min)

---

## ✅ ACCEPTANCE CRITERIA

**Customer Contacts:**
1. ✅ Hook `useCustomerContacts.ts` erstellt
2. ✅ `CustomerDetailTabVerlauf.tsx` nutzt Hook (TODOs entfernt!)
3. ✅ Customer zeigt Kontakte (nicht mehr hardcoded empty)
4. ✅ Edit Contact → DecisionLevel gespeichert
5. ✅ Create Contact → funktioniert
6. ✅ Delete Contact → funktioniert

**Lead Contacts:**
7. ✅ Hook `useLeadContacts.ts` erstellt
8. ✅ Lead Contact Editor nutzt Hook
9. ✅ Keine MUI Enum Validation Errors mehr
10. ✅ DecisionLevel wird gespeichert

**Testing:**
11. ✅ TypeScript Type-Check: PASSED
12. ✅ Customer Contact Edit: Funktioniert
13. ✅ Lead Contact Edit: Funktioniert
14. ✅ Keine Console.log mehr (echte API Calls)

---

## 🚨 RISKS & MITIGATION

**Risk 1: Type Mismatch Backend ↔ Frontend**
- **Mitigation:** Backend ContactDTO prüfen, Frontend Contact Type anpassen
- **Known Issue:** Backend hat `position`, Frontend erwartet `role`
- **Solution:** Frontend Type um `position?: string` erweitern

**Risk 2: Lead Contacts API unterschiedlich**
- **Mitigation:** Backend Lead Contact API prüfen (vermutlich identisch)
- **Fallback:** Separate Hook Implementation

**Risk 3: Bestehende Tests brechen**
- **Mitigation:** Tests laufen nach Implementation
- **Expected:** 0 Test-Failures (nur console.log Calls werden ersetzt)

---

## 📚 REFERENCES

**Sprint 2.1.7.2 Dokumentation:**
- `/docs/planung/artefakte/sprint-2.1.7.2/SPEC_D11_CUSTOMER_DETAIL_COCKPIT.md` (Zeile 563-734)
- Gap Analysis: "Phase 4 UNVOLLSTÄNDIG"

**Backend API:**
- `ContactResource.java` - Customer Contact Endpoints
- `LeadContactResource.java` - Lead Contact Endpoints (?)

**Frontend Components:**
- `CustomerDetailTabVerlauf.tsx` - Customer Contacts UI
- `LeadDetailPage.tsx` - Lead Contacts UI (?)
- `ContactEditDialog.tsx` - Contact Edit Dialog

**Git History:**
- Commit `e7b489915` - Sprint 2.1.7.2 D11 Complete (TODOs hinzugefügt)

---

**🚀 READY TO START:** Sprint 2.1.7.7 - Contact API Integration (15-20 min Fix)

**Next Step:** Task 1 - useCustomerContacts Hook erstellen
