# 🎯 SPEC: Customer Detail View - Cockpit Pattern (AKTUELL)

**📅 Erstellt:** 2025-10-26
**📝 Status:** ✅ AKTIV - VERBINDLICH
**👤 Autor:** Jörg Streeck + Strategic Consultant
**Sprint:** 2.1.7.2 - D11 Customer Detail View

---

**📍 Navigation:**
- [🏠 Sprint 2.1.7.2 Hauptdokument](../../TRIGGER_SPRINT_2_1_7_2.md)
- [🔧 Technische Spec](./SPEC_SPRINT_2_1_7_2_TECHNICAL.md) - Backend/Frontend Implementierung

**🔗 D11 Dokumenten-Hierarchie:**
1. [🏗️ Architektur-Konzept](./TRIGGER_SPRINT_2_1_7_2_D11_SERVER_DRIVEN_CARDS.md) - Server-Driven Cards Prinzip
2. [✅ Final Architecture](./SPEC_D11_CUSTOMER_DETAIL_VIEW_ARCHITECTURE.md) - Verbindliche Implementierung
3. **Dieses Dokument** - Funktionale Spec (UX, Features, Design)
4. [❌ DEPRECATED](./SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md) - Verworfene Architektur

**💡 Dieses Dokument erklärt:**
- **WARUM** Cockpit Pattern statt Progressive Disclosure
- **WIE** die UX aussehen soll
- **WAS** der User sieht und tun kann

---

## 📋 EXECUTIVE SUMMARY

**Kunde braucht:** CRM-Kundendetailansicht mit schnellem Zugriff auf Stammdaten, Kommunikationshistorie und Sales-Actions.

**Lösung:** Cockpit-Pattern (wie bestehendes FreshPlan Cockpit) - Separate Detail-Page mit Action Buttons + Tabs.

**Ersetzt:** Progressive Disclosure Pattern (siehe `SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md`)

---

<a name="inhaltsverzeichnis"></a>
## 📑 INHALTSVERZEICHNIS

### 🏗️ Teil 1: Customer Detail View - Cockpit Pattern

1. [Architecture Decision](#architecture-decision)
2. [Navigation Flow](#navigation-flow)
3. [Component Structure](#component-structure)
4. [Routing](#routing)
5. [Files to Create](#files-to-create)
6. [Files to Delete/Modify](#files-to-delete-modify)
7. [Testing Checklist](#testing-checklist)
8. [Design Notes](#design-notes)
9. [Implementation Phases](#implementation-phases)
10. [References](#references-cockpit)
11. [Acceptance Criteria](#acceptance-criteria-cockpit)

### 🐛 Teil 2: Gap Analysis & Fixes

12. [Gap Analysis: Contact API Integration](#gap-analysis-contact-api)
13. [Critical Architecture Issue: Wizard vs Detail-Tabs](#critical-architecture-wizard-vs-tabs)

### 🚀 Teil 3: Lead-Bereich Server-Driven Migration (Sprint 2.1.7.2 D11.2)

14. [Problem: Lead-Komponenten sind hardcoded](#lead-problem)
15. [IST-Architektur Analyse](#lead-ist-architektur)
16. [ZIEL: Full Server-Driven Architecture](#lead-ziel)
17. [Lead→Customer Konversion: Feld-Mapping](#lead-customer-mapping)
18. [Implementation Plan (15 Tasks)](#lead-implementation-plan)
   - [Phase 1: Backend Schema Resources](#lead-phase-1-backend)
   - [Phase 2: Frontend Migration](#lead-phase-2-frontend)
   - [Phase 3: Infrastructure](#lead-phase-3-infrastructure)
19. [Benefits der Migration](#lead-benefits)
20. [Effort Summary](#lead-effort)
21. [Risks & Mitigation](#lead-risks)
22. [Acceptance Criteria](#lead-acceptance-criteria)
23. [References](#lead-references)

---

<a name="architecture-decision"></a>
## 🚨 ARCHITECTURE DECISION

### Warum Cockpit-Pattern statt Progressive Disclosure?

**Verworfene Architektur (Progressive Disclosure):**
```
Table (schmal) │ CompactView (mäßig) │ → Modal (Details)
     60%       │      40%            │
```

**Probleme:**
1. ❌ Table zu schmal → weniger Spalten sichtbar
2. ❌ CompactView zu "meh" - weder Übersicht noch Detail
3. ❌ 3 Disclosure-Levels zu viel (Row → CompactView → Modal)
4. ❌ Auf 13" Laptops eng
5. ❌ Inkonsistent mit bestehendem Cockpit-Pattern

**Neue Architektur (Cockpit-Pattern):**
```
Table (volle Breite) → Detail Page /customers/:id
        100%                    (volle Breite)
```

**Vorteile:**
1. ✅ Konsistent mit bestehendem FreshPlan Cockpit
2. ✅ Table hat volle Breite (mehr Spalten sichtbar)
3. ✅ Detail-Page hat vollen Platz für alle Daten
4. ✅ URL teilbar: `/customers/:id`
5. ✅ Standard-Pattern (Salesforce, HubSpot)
6. ✅ Action Buttons prominent (wie Cockpit Arbeitsbereich)
7. ✅ Weniger Code, einfacher State-Management

---

<a name="navigation-flow"></a>
## 🗺️ NAVIGATION FLOW

```
┌────────────────────────────────────────────────────┐
│ /customers (CustomersPageV2)                       │
│                                                     │
│  ┌──────────────────────────────────────────────┐ │
│  │ CustomerTable (volle Breite)                 │ │
│  │ Kunde  │ Status  │ Risiko  │ Branche  │ ... │ │
│  │ Row 1  │ Aktiv   │ 10%     │ KANTINE  │     │ │
│  │ Row 2  │ Prospect│ 25%     │ HOTEL    │     │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  [+ Neuer Kunde] Button → Wizard (bleibt)          │
└────────────────────────────────────────────────────┘
                    │
                    │ onClick Row → navigate(`/customers/${id}`)
                    ↓
┌────────────────────────────────────────────────────┐
│ /customers/:id (CustomerDetailPage)                │
│                                                     │
│  ┌──────────────────────────────────────────────┐ │
│  │ Header (Breadcrumb + Name + Status)          │ │
│  │ ← Zurück  │  Großhandel Frische Küche GmbH  │ │
│  │                                      [Aktiv]  │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  ┌──────────────────────────────────────────────┐ │
│  │ Action Buttons (wie Cockpit Schnellaktionen) │ │
│  │ [📧 E-Mail] [📞 Anrufen] [💰 Angebot]        │ │
│  │ [📝 Notiz] [📅 Meeting] [✏️ Bearbeiten]      │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  ┌──────────────────────────────────────────────┐ │
│  │ Tabs: Firma │ Geschäft │ Verlauf              │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  ┌──────────────────────────────────────────────┐ │
│  │ Tab Content (volle Breite)                    │ │
│  │                                               │ │
│  │ - Firma: Stammdaten, Standorte, Kontakte    │ │
│  │ - Geschäft: Opportunities, Produkte, Verträge│ │
│  │ - Verlauf: Timeline + Aktivitäten (Cockpit)  │ │
│  │                                               │ │
│  └──────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────┘
```

---

<a name="component-structure"></a>
## 🏗️ COMPONENT STRUCTURE

### 1. CustomersPageV2 (MODIFY)

**File:** `frontend/src/pages/CustomersPageV2.tsx`

**Änderungen:**
```tsx
// ENTFERNEN:
- const [selectedCustomerId, setSelectedCustomerId] = useState<string | null>(null);
- const [detailModalOpen, setDetailModalOpen] = useState(false);
- const selectedCustomer = useMemo(...);
- <CustomerCompactView /> (rechte Spalte)
- <CustomerDetailModal /> (Modal)

// ÄNDERN:
onRowClick={customer => {
  if (context === 'customers') {
    navigate(`/customers/${customer.id}`); // NEU!
  } else if (context === 'leads') {
    navigate(generateLeadUrl(customer.companyName || 'lead', customer.id));
  }
}}

// BEHALTEN:
- Table (volle Breite)
- Filter, Sort, Search
- [+ Neuer Kunde] Button → Wizard
```

**Layout:**
```tsx
<MainLayoutV2>
  <CustomerListHeader />
  <IntelligentFilterBar />
  <CustomerTable
    onRowClick={customer => navigate(`/customers/${customer.id}`)}
  />
</MainLayoutV2>
```

---

### 2. CustomerDetailPage (NEU)

**File:** `frontend/src/pages/CustomerDetailPage.tsx`

**Props:**
```tsx
// Route: /customers/:id
const { id } = useParams();
```

**Structure:**
```tsx
export const CustomerDetailPage = () => {
  const { id } = useParams();
  const { customer, isLoading } = useCustomer(id);
  const [activeTab, setActiveTab] = useState(0);

  return (
    <MainLayoutV2>
      {/* Header mit Breadcrumb */}
      <CustomerDetailHeader customer={customer} />

      {/* Action Buttons (wie Cockpit Schnellaktionen) */}
      <CustomerActionButtons customer={customer} />

      {/* Tabs */}
      <Tabs value={activeTab} onChange={setActiveTab}>
        <Tab label="Firma" />
        <Tab label="Geschäft" />
        <Tab label="Verlauf" />
      </Tabs>

      {/* Tab Panels */}
      <TabPanel value={activeTab} index={0}>
        <CustomerDetailTabFirma customerId={id} />
      </TabPanel>
      <TabPanel value={activeTab} index={1}>
        <CustomerDetailTabGeschaeft customerId={id} />
      </TabPanel>
      <TabPanel value={activeTab} index={2}>
        <CustomerDetailTabVerlauf customerId={id} />
      </TabPanel>
    </MainLayoutV2>
  );
};
```

---

### 3. CustomerDetailHeader (NEU)

**File:** `frontend/src/features/customers/components/detail/CustomerDetailHeader.tsx`

**Content:**
```tsx
export const CustomerDetailHeader = ({ customer }) => {
  const navigate = useNavigate();

  return (
    <Box sx={{ mb: 3 }}>
      {/* Breadcrumb */}
      <Breadcrumbs sx={{ mb: 2 }}>
        <Link
          component="button"
          onClick={() => navigate('/customers')}
          sx={{ display: 'flex', alignItems: 'center' }}
        >
          <ArrowBackIcon sx={{ mr: 0.5 }} />
          Zurück zu Kunden
        </Link>
        <Typography color="text.primary">
          {customer.companyName}
        </Typography>
      </Breadcrumbs>

      {/* Name + Status Badge */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4">
          {customer.companyName}
        </Typography>
        <Chip
          label={customer.status}
          color={getStatusColor(customer.status)}
          size="large"
        />
      </Box>
    </Box>
  );
};
```

---

### 4. CustomerActionButtons (NEU)

**File:** `frontend/src/features/customers/components/detail/CustomerActionButtons.tsx`

**Inspiration:** Cockpit Schnellaktionen (siehe Screenshot)

**Content:**
```tsx
export const CustomerActionButtons = ({ customer }) => {
  return (
    <Box sx={{
      display: 'flex',
      gap: 2,
      mb: 3,
      flexWrap: 'wrap',
      p: 2,
      bgcolor: 'action.hover',
      borderRadius: 1
    }}>
      {/* E-Mail */}
      <Button
        variant="outlined"
        startIcon={<EmailIcon />}
        onClick={() => handleEmail(customer)}
      >
        E-Mail
      </Button>

      {/* Anrufen */}
      <Button
        variant="outlined"
        startIcon={<PhoneIcon />}
        onClick={() => handleCall(customer)}
      >
        Anrufen
      </Button>

      {/* Angebot */}
      <Button
        variant="outlined"
        startIcon={<ReceiptIcon />}
        color="success"
        onClick={() => handleCreateQuote(customer)}
      >
        Angebot
      </Button>

      {/* Notiz */}
      <Button
        variant="outlined"
        startIcon={<NoteIcon />}
        onClick={() => handleAddNote(customer)}
      >
        Notiz
      </Button>

      {/* Meeting */}
      <Button
        variant="outlined"
        startIcon={<EventIcon />}
        onClick={() => handleScheduleMeeting(customer)}
      >
        Meeting
      </Button>

      {/* Bearbeiten */}
      <Button
        variant="contained"
        startIcon={<EditIcon />}
        onClick={() => handleEdit(customer)}
        sx={{ ml: 'auto' }}
      >
        Bearbeiten
      </Button>
    </Box>
  );
};
```

---

### 5. Tab Components (WIEDERVERWENDBAR)

**Bereits vorhanden:**
- `CustomerDetailTabFirma.tsx` - Stammdaten, Standorte, Klassifikation
- `CustomerDetailTabGeschaeft.tsx` - Opportunities, Produkte (TODO)
- `CustomerDetailTabVerlauf.tsx` - Timeline + Kontakte

**Anpassungen:**
- Entferne `Alert` "Sprint 2.1.7.2 D11 - Phase 3" Banners (nicht mehr nötig)
- Layout auf volle Breite anpassen (kein Modal, sondern Page)

---

<a name="routing"></a>
## 🛣️ ROUTING

**File:** `frontend/src/App.tsx`

```tsx
<Routes>
  {/* Existing routes */}
  <Route path="/customers" element={<CustomersPageV2 />} />

  {/* NEU: Customer Detail Route */}
  <Route path="/customers/:id" element={<CustomerDetailPage />} />

  {/* Existing routes */}
  <Route path="/leads" element={<CustomersPageV2 context="leads" />} />
</Routes>
```

---

<a name="files-to-create"></a>
## 📦 FILES TO CREATE

```
frontend/src/pages/
  └── CustomerDetailPage.tsx (NEU)

frontend/src/features/customers/components/detail/
  ├── CustomerDetailHeader.tsx (NEU)
  ├── CustomerActionButtons.tsx (NEU)
  ├── CustomerDetailTabFirma.tsx (BESTEHT - Anpassen)
  ├── CustomerDetailTabGeschaeft.tsx (BESTEHT - Erweitern)
  └── CustomerDetailTabVerlauf.tsx (BESTEHT - Anpassen)
```

---

<a name="files-to-delete-modify"></a>
## 🗑️ FILES TO DELETE/MODIFY

**Löschen (später, nach Fertigstellung):**
- `CustomerCompactView.tsx` - Nicht mehr benötigt
- `CustomerDetailModal.tsx` - Nicht mehr benötigt

**Modifizieren:**
- `CustomersPageV2.tsx` - Split-Panel entfernen, onClick zu navigate ändern

---

<a name="testing-checklist"></a>
## 🧪 TESTING CHECKLIST

### Navigation
- [ ] Click auf Kundenzeile in `/customers` navigiert zu `/customers/:id`
- [ ] Breadcrumb "← Zurück" navigiert zurück zu `/customers`
- [ ] URL `/customers/:id` direkt aufrufbar (Deep Link)
- [ ] Browser Back-Button funktioniert

### Header
- [ ] Kundenname angezeigt
- [ ] Status Badge korrekte Farbe
- [ ] Breadcrumb korrekt

### Action Buttons
- [ ] Alle 6 Buttons sichtbar
- [ ] onClick Handler (zunächst console.log)
- [ ] Responsive: Wrap auf kleineren Screens

### Tabs
- [ ] Alle 3 Tabs anklickbar
- [ ] Tab-Switch funktioniert
- [ ] Tab Content lädt korrekt
- [ ] URL-State optional: `/customers/:id?tab=verlauf`

### Tab: Firma
- [ ] Stammdaten angezeigt
- [ ] Standorte angezeigt
- [ ] Kontakte angezeigt
- [ ] Edit-Funktionen (später)

### Tab: Geschäft
- [ ] Platzhalter vorhanden
- [ ] Sprint 2.2.x Hinweis

### Tab: Verlauf
- [ ] Timeline sichtbar
- [ ] Kontakte CRUD funktioniert ← ❌ **GAP: Contact API Hook fehlt!**
- [ ] ContactEditDialog öffnet ← ⚠️ **Dialog existiert, aber keine Daten**

---

<a name="design-notes"></a>
## 🎨 DESIGN NOTES

**Orientierung:** FreshPlan Cockpit "Arbeitsbereich" (3. Spalte im Cockpit-Screenshot)

**Key Visual Elements:**
1. Action Buttons = wie Cockpit Schnellaktionen (E-Mail, Anrufen, Kalkulation, Angebot)
2. Tabs = wie Cockpit Tabs (Aktivitäten, Details, Dokumente)
3. Timeline = wie Cockpit Timeline (grüne/blaue Icons mit Timestamps)

**Farbschema:**
- FreshFoodz Green: `#94C456`
- Primary Blue: `#004F7B`
- Status Colors: theme.palette.success/warning/error

---

<a name="implementation-phases"></a>
## 🚀 IMPLEMENTATION PHASES

### Phase 1: Routing & Basic Page (30 min)
1. Create `CustomerDetailPage.tsx` (skeleton)
2. Add route in `App.tsx`
3. Modify `CustomersPageV2.tsx` onClick → navigate
4. Test navigation

### Phase 2: Header & Action Buttons (45 min)
1. Create `CustomerDetailHeader.tsx`
2. Create `CustomerActionButtons.tsx`
3. Test rendering

### Phase 3: Tab Integration (30 min)
1. Integrate existing Tab components
2. Remove Modal-specific code
3. Adjust layout for full width
4. Test tab switching

### Phase 4: Polish & Test (30 min)
1. Responsive testing
2. Clean up old components (CompactView, Modal)
3. Remove Phase 3 banners from tabs
4. Final UX testing

**Total:** ~2.5 hours

---

<a name="references-cockpit"></a>
## 📚 REFERENCES

- **Alte Spec:** `SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md` (nicht mehr gültig)
- **Cockpit Screenshot:** User-provided screenshot (Basis für Action Buttons + Timeline)
- **Design System:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md`
- **Router:** React Router v6 (bereits im Projekt)

---

<a name="acceptance-criteria-cockpit"></a>
## ✅ ACCEPTANCE CRITERIA

1. ✅ Click auf Kundenzeile → Navigate zu Detail-Page
2. ✅ Detail-Page zeigt Header (Name, Status, Breadcrumb)
3. ✅ 6 Action Buttons sichtbar (E-Mail, Anrufen, Angebot, Notiz, Meeting, Bearbeiten)
4. ✅ 3 Tabs anklickbar (Firma, Geschäft, Verlauf)
5. ✅ Tab "Firma" zeigt Stammdaten (wie bisher im Modal)
6. ❌ Tab "Verlauf" zeigt Timeline + Kontakte ← **GAP: Kontakte nicht geladen!**
7. ✅ URL teilbar: `/customers/:id`
8. ✅ Breadcrumb zurück funktioniert
9. ✅ Konsistent mit Cockpit-Pattern (Action Buttons + Tabs)
10. ✅ Kein Split-Panel mehr in `/customers`

---

**🎯 ZIEL:** Cockpit-Pattern konsequent durchziehen - konsistent, benutzerfreundlich, mehr Platz für Details!

---

<a name="gap-analysis-contact-api"></a>
## 🚨 GAP ANALYSIS: Sprint 2.1.7.2 D11 - PHASE 4 UNVOLLSTÄNDIG

**📅 Entdeckt:** 2025-10-27
**🚨 Severity:** MEDIUM - Feature unvollständig, User sieht leere Kontaktliste
**✅ Status:** DOCUMENTED - Ready for Implementation

---

### ❌ DAS PROBLEM: CONTACT API INTEGRATION FEHLT

#### IST-Zustand:

**Backend:**
```
✅ ContactResource.java - API existiert!
   GET /api/customers/{id}/contacts
   POST /api/customers/{id}/contacts
   PUT /api/customers/{id}/contacts/{contactId}
   DELETE /api/customers/{id}/contacts/{contactId}

✅ ContactDTO.java - DTO existiert!
✅ ContactService.java - Service existiert!
✅ SEED Data - Julia Hoffmann als Kontakt vorhanden!
```

**Frontend:**
```
✅ CustomerDetailTabVerlauf.tsx - UI Component existiert!
✅ ContactEditDialog.tsx - Dialog existiert!
✅ Contact Type - TypeScript Interface existiert!

❌ useCustomerContacts Hook - FEHLT!
❌ API Integration - NICHT IMPLEMENTIERT!
❌ Hardcoded Empty State - useState<Contact[]>([])
```

**Symptom:**
- User navigiert zu Tab "Verlauf"
- Sieht "Noch keine Kontakte erfasst"
- **OBWOHL** Julia Hoffmann in SEED-Daten existiert!
- Intelligente Suche findet Julia Hoffmann → Navigation funktioniert
- Aber Tab zeigt keine Daten → Kein API Call!

---

### 🔍 ROOT CAUSE

**File:** `frontend/.../CustomerDetailTabVerlauf.tsx` (Zeile 57-64)

```typescript
export const CustomerDetailTabVerlauf: React.FC<CustomerDetailTabVerlaufProps> = ({
  customerId,
}) => {
  // State
  const [contacts, setContacts] = useState<Contact[]>([]); // ❌ HARDCODED EMPTY!
  const [isLoading, setIsLoading] = useState(false);       // ❌ HARDCODED FALSE!

  // TODO: Replace with real API hook in Phase 4  ← DAS IST DAS PROBLEM!
  // const { data: contacts, isLoading, refetch } = useCustomerContacts(customerId);
```

**Was fehlt:**

1. **Hook nicht implementiert:**
   ```typescript
   // FILE: frontend/src/features/customer/hooks/useCustomerContacts.ts
   // ❌ EXISTIERT NICHT!
   ```

2. **Komponente nutzt Platzhalter-State:**
   - `contacts` bleibt immer `[]`
   - `isLoading` bleibt immer `false`
   - Kein API Call wird je gemacht

3. **Backend-DTO ≠ Frontend-Type:**
   - Backend: `ContactDTO` mit ~20 Feldern
   - Frontend: `Contact` mit ~8 Feldern (ContactEditDialog.tsx:38)
   - Type Mapping fehlt!

---

### 📋 IMPLEMENTATION PLAN (Sprint 2.1.7.2 D11.1 - HOTFIX)

#### Task 1: Create useCustomerContacts Hook (10 min)

**File:** `frontend/src/features/customer/hooks/useCustomerContacts.ts` (NEW)

```typescript
import { useQuery } from '@tanstack/react-query';
import { httpClient } from '../../../lib/httpClient';
import type { Contact } from '../../customers/components/detail/ContactEditDialog';

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
```

#### Task 2: Integrate Hook in Component (5 min)

**File:** `CustomerDetailTabVerlauf.tsx` (EDIT - Zeile 57-64)

```typescript
// VORHER:
const [contacts, setContacts] = useState<Contact[]>([]);
const [isLoading, setIsLoading] = useState(false);
// TODO: Replace with real API hook in Phase 4

// NACHHER:
const { data: contacts = [], isLoading } = useCustomerContacts(customerId);
// State für Dialog bleibt:
const [contactDialogOpen, setContactDialogOpen] = useState(false);
const [selectedContact, setSelectedContact] = useState<Contact | null>(null);
```

#### Task 3: Type Mapping prüfen (5 min)

**Backend DTO (ContactDTO.java) → Frontend Type (Contact interface)**

**Vergleich:**
```
Backend (ContactDTO):          Frontend (Contact):
- id: UUID                  →  id?: string ✅
- customerId: UUID          →  (nicht benötigt)
- firstName: String         →  firstName: string ✅
- lastName: String          →  lastName: string ✅
- email: String             →  email?: string ✅
- phone: String             →  phone?: string ✅
- mobile: String            →  mobile?: string ✅
- isPrimary: boolean        →  isPrimary?: boolean ✅
- position: String          →  (fehlt - ignorieren für MVP)
- decisionLevel: String     →  (fehlt - ignorieren für MVP)
- notes: String             →  notes?: string ✅
- ... 10 weitere Felder     →  role: 'CHEF'|'BUYER'|... ← MISMATCH!
```

**Problem:** Backend hat `position`, Frontend erwartet `role`.

**Lösung:**
- Frontend Type erweitern um `position?: string`
- ODER Backend DTO hat `role` Mapping (prüfen!)

#### Task 4: Test mit SEED Data (5 min)

1. Backend läuft: `./mvnw quarkus:dev`
2. Frontend läuft: `npm run dev`
3. Navigate zu Customer "Betriebsgastronomie TechPark Frankfurt GmbH"
4. Tab "Verlauf" öffnen
5. **EXPECT:** Julia Hoffmann wird angezeigt!
6. **EXPECT:** `role` Field wird korrekt gemappt

---

### ✅ ACCEPTANCE CRITERIA (Hotfix D11.1)

1. ✅ Hook `useCustomerContacts.ts` erstellt
2. ✅ `CustomerDetailTabVerlauf.tsx` nutzt Hook (TODO entfernt!)
3. ✅ SEED Customer zeigt Julia Hoffmann im Tab "Verlauf"
4. ✅ Kontaktliste zeigt Name, E-Mail, Telefon
5. ✅ Button "+ Neuer Kontakt" öffnet Dialog (bestehend)
6. ✅ Edit/Delete Buttons funktionieren (bestehende Handler)
7. ✅ Type Mapping Backend ↔ Frontend korrekt

---

### 🎯 WARUM PASSIERTE DAS?

**Phase 4 Plan (SPEC Zeile 456-460):**
```markdown
### Phase 4: Polish & Test (30 min)
1. Responsive testing
2. Clean up old components (CompactView, Modal)
3. Remove Phase 3 banners from tabs
4. Final UX testing
```

**Was fehlt:** Kein expliziter Step "Connect Contact API to Component"!

**Lesson Learned:**
- ✅ UI Struktur wurde in Phase 3 erstellt
- ✅ Backend API existiert seit langem
- ❌ Verknüpfung (Hook) wurde vergessen
- ❌ Phase 4 fokussierte auf "Polish", nicht "Complete"

---

<a name="lead-references"></a>
### 📚 REFERENCES

- **Backend API:** `GET /api/customers/{id}/contacts` (ContactResource.java:23)
- **Frontend Component:** `CustomerDetailTabVerlauf.tsx:57-64`
- **Frontend Type:** `ContactEditDialog.tsx:38`
- **SEED Data:** Betriebsgastronomie TechPark Frankfurt GmbH → Julia Hoffmann

---

**🚨 PRIORITY:** HIGH - User sieht aktuell KEINE Kontakte (obwohl vorhanden!)

---

<a name="critical-architecture-wizard-vs-tabs"></a>
## 🏗️ CRITICAL ARCHITECTURE ISSUE: WIZARD vs. DETAIL-TABS

**📅 Entdeckt:** 2025-10-26 (während Sprint 2.1.7.2 D11)
**🚨 Severity:** HIGH - Technical Debt, Lead-Conversion at Risk
**✅ Status:** ANALYZED - Wartend auf Implementierung

---

### ❌ DAS PROBLEM: ZWEI QUELLEN DER WAHRHEIT

#### IST-Zustand (Inkonsistent):

```
┌─────────────────────────────────────────────────┐
│ WIZARD (Neuanlage Customer)                     │
│ ❌ CLIENT-SIDE Schema                           │
│                                                  │
│ useFieldDefinitions Hook                        │
│   └─> fieldCatalog.json (hart codiert)         │
│       - customerNumber, companyName, ...        │
│       - ~40 Felder                              │
│                                                  │
│ Step1-4: HART CODIERT                          │
└─────────────────────────────────────────────────┘
                    │
                    ↓ schreibt in DB
         ┌──────────────────────┐
         │  Customer Entity     │
         │  (Backend)           │
         └──────────────────────┘
                    │
                    ↓ liest aus DB
┌─────────────────────────────────────────────────┐
│ DETAIL-TABS (Anzeige Customer)                  │
│ ✅ SERVER-DRIVEN Schema                         │
│                                                  │
│ GET /api/customers/schema                       │
│ CustomerSchemaResource (Backend)                │
│   └─> 7 Cards dynamisch generiert              │
│       - company_profile, locations, ...         │
│       - ~50 Felder (mehr als Wizard!)           │
└─────────────────────────────────────────────────┘
```

**→ WIZARD und DETAIL-TABS nutzen VERSCHIEDENE Schemas!**

---

### 🔥 KONKRETE PROBLEME

#### 1. **Lead→Customer Konvertierung kann brechen**

```typescript
// Lead hat Pain Points:
lead.painStaffShortage = "ja"
lead.painHighCosts = "ja"

// Wizard schreibt (basierend auf fieldCatalog.json):
customer.painStaffShortage = "ja"
customer.painHighCosts = "ja"

// Detail-Tabs zeigen (basierend auf Backend Schema):
GET /api/customers/schema → pain_points Card
  → Backend definiert: painStaffShortage, painHighCosts, ...

// Was passiert wenn Backend ein Feld NICHT definiert?
→ Feld verschwindet oder wird nicht angezeigt!
```

#### 2. **Neue Felder müssen an 2 Stellen gepflegt werden**

```
Neues Feld hinzufügen:
1. ❌ fieldCatalog.json (Frontend)
2. ❌ CustomerSchemaResource.java (Backend)

→ Doppelte Arbeit, Fehleranfällig!
```

#### 3. **Field Coverage ist unklar**

- Wizard erfasst 40 Felder
- Detail-Tabs zeigen 50 Felder
- **WO kommen die 10 zusätzlichen Felder her?**
- **Werden sie beim Wizard-Save NULL gesetzt?**

---

### 🔍 IST-ARCHITEKTUR DETAILS

#### Backend: CustomerSchemaResource (✅ KORREKT)

**File:** `backend/.../CustomerSchemaResource.java`

```java
@GET
@Path("/api/customers/schema")
public Response getCustomerSchema() {
  List<CustomerCardSchema> schema = List.of(
    buildCompanyProfileCard(),    // 8 Felder
    buildLocationsCard(),          // 4 Felder + Arrays
    buildClassificationCard(),     // 5 Felder
    buildBusinessDataCard(),       // 5 Felder
    buildContractsCard(),          // 4 Felder
    buildPainPointsCard(),         // 9 Felder (WICHTIG für Lead!)
    buildProductsCard()            // 0 Felder (Platzhalter)
  );
  return Response.ok(schema).build();
}
```

**✅ Vorteile:**
- Single Source of Truth im Backend
- Lead→Customer Mapping konsistent
- Neue Felder: Nur Backend ändern
- Validierung im Backend

#### Frontend: useFieldDefinitions Hook (❌ VERALTET)

**File:** `frontend/.../useFieldDefinitions.ts`

```typescript
import fieldCatalog from '../data/fieldCatalog.json';

export const useFieldDefinitions = () => {
  const catalog = fieldCatalog as FieldCatalog;

  return {
    customerFields: catalog.customer.base || [],  // ❌ HART CODIERT!
    getFieldByKey: (key) => fieldMap.get(key),
  };
};
```

**❌ Probleme:**
- fieldCatalog.json muss manuell gepflegt werden
- Keine Verbindung zum Backend Schema
- Lead→Customer Mapping kann inkonsistent sein

#### Frontend: Wizard Steps (❌ HART CODIERT)

**File:** `frontend/.../Step1BasisFilialstruktur.tsx`

```typescript
const baseFields = useMemo(() => {
  return [
    'customerNumber',
    'companyName',
    'legalForm',
    // ... 20 weitere Felder HART CODIERT!
  ]
    .map(key => getFieldByKey(key))  // ← Aus fieldCatalog.json!
    .filter(isFieldDefinition);
}, [getFieldByKey]);
```

**❌ Probleme:**
- Felder hart codiert im Frontend
- Kein Backend-Abgleich
- Änderungen an 2 Stellen

---

### ✅ SOLL-ARCHITEKTUR: FULL SERVER-DRIVEN

#### Ziel: Single Source of Truth im Backend

```
┌─────────────────────────────────────────────────┐
│ CustomerSchemaResource (Backend)                │
│ ✅ SINGLE SOURCE OF TRUTH                       │
│                                                  │
│ GET /api/customers/schema                       │
│   → 7 Cards für Detail-Tabs                    │
│   → 4 Steps für Wizard (NEU!)                  │
│                                                  │
│ Jedes Feld hat Metadaten:                      │
│ - showInWizard: true/false                     │
│ - wizardStep: 1|2|3|4                          │
│ - showInDetailTab: true/false                  │
│ - cardId: "company_profile"|...                │
└─────────────────────────────────────────────────┘
         ↓                              ↓
    ┌────────┐                    ┌─────────┐
    │ Wizard │                    │ Details │
    │ (NEW)  │                    │ (EXIST) │
    └────────┘                    └─────────┘
     ↓                              ↓
  Step1: Felder mit               Card 1: Felder mit
  wizardStep=1                    cardId="company_profile"
```

---

### 📝 IMPLEMENTATION PLAN

#### Phase 1: Backend erweitern (Sprint 2.1.7.3)

**File:** `CustomerSchemaResource.java`

```java
// NEU: Wizard-Metadaten hinzufügen
@GET
@Path("/schema")
public Response getCustomerSchema() {
  return Response.ok(
    buildCompleteSchema()  // Wizard + Detail-Tabs
  ).build();
}

private FieldSchema buildField(String key, ...) {
  return FieldSchema.builder()
    .fieldKey(key)
    .label("Firmenname")
    .type(FieldType.TEXT)
    // Detail-Tab Metadaten (EXISTING)
    .cardId("company_profile")
    .order(1)
    // Wizard Metadaten (NEU!)
    .showInWizard(true)      // ← NEU!
    .wizardStep(1)           // ← NEU!
    .wizardOrder(2)          // ← NEU!
    .build();
}
```

#### Phase 2: Frontend anpassen (Sprint 2.1.7.3)

**File:** `useCustomerSchema.ts` (NEW Hook)

```typescript
// Ersetzt useFieldDefinitions
export const useCustomerSchema = () => {
  const { data, isLoading } = useQuery({
    queryKey: ['customer-schema'],
    queryFn: () => fetch('/api/customers/schema').then(r => r.json())
  });

  const getWizardFields = (step: number) =>
    data.filter(f => f.showInWizard && f.wizardStep === step);

  const getCardFields = (cardId: string) =>
    data.filter(f => f.cardId === cardId);

  return { getWizardFields, getCardFields, isLoading };
};
```

**File:** `Step1BasisFilialstruktur.tsx` (REFACTOR)

```typescript
// VORHER (hart codiert):
const baseFields = ['customerNumber', 'companyName', ...]
  .map(key => getFieldByKey(key));

// NACHHER (server-driven):
const { getWizardFields } = useCustomerSchema();
const baseFields = getWizardFields(1);  // Step 1 Felder
```

#### Phase 3: fieldCatalog.json deprecaten (Sprint 2.1.7.3)

```
1. Alle Wizard Steps auf useCustomerSchema umstellen
2. useFieldDefinitions Hook als @deprecated markieren
3. fieldCatalog.json löschen (später)
```

---

### ✅ BENEFITS DER LÖSUNG

1. **✅ Single Source of Truth**
   - Backend definiert ALLES
   - Wizard und Detail-Tabs synchron

2. **✅ Lead→Customer Konsistent**
   - Lead Felder → Backend Schema
   - Customer Schema → Lead Felder
   - Keine Diskrepanzen

3. **✅ Wartbarkeit**
   - Neues Feld: Nur Backend ändern
   - Frontend aktualisiert automatisch

4. **✅ Moderner Standard**
   - Salesforce, HubSpot nutzen auch Server-Driven UI
   - Best Practice für SaaS

---

### 🎯 ACCEPTANCE CRITERIA (Architektur-Refactoring)

1. ✅ Backend: `CustomerSchemaResource` hat Wizard-Metadaten
2. ✅ Frontend: Neuer Hook `useCustomerSchema` implementiert
3. ✅ Wizard Step 1-4: Nutzen `useCustomerSchema` statt `useFieldDefinitions`
4. ✅ Detail-Tabs: Nutzen weiterhin `useCustomerSchema` (wie bisher)
5. ✅ fieldCatalog.json: Deprecated oder gelöscht
6. ✅ Test: Lead→Customer Konvertierung funktioniert
7. ✅ Test: Alle Wizard-Felder erscheinen in Detail-Tabs
8. ✅ Dokumentation: ADR geschrieben

---

<a name="lead-references"></a>
### 📚 REFERENCES

- **Backend Schema API:** `GET /api/customers/schema`
- **Bestehender Hook:** `frontend/.../useFieldDefinitions.ts`
- **Wizard Steps:** `frontend/.../components/steps/Step*.tsx`
- **Detail-Tabs:** `frontend/.../components/detail/CustomerDetailTab*.tsx`

---

**🚨 WICHTIG:** Dieser Refactoring MUSS vor Sprint 2.2 (Lead-Konvertierung) erfolgen, sonst werden Daten verloren!

---

<a name="lead-problem"></a>
## 🎯 LEAD-BEREICH: SERVER-DRIVEN MIGRATION PLAN (Sprint 2.1.7.2 D11.2)

**📅 Erstellt:** 2025-10-29
**🚨 Severity:** HIGH - Architectural Inconsistency + Parity Issues
**✅ Status:** DOCUMENTED - Ready for Implementation

---

### ❌ DAS PROBLEM: LEAD-KOMPONENTEN SIND HARDCODED

**Aktuelle Situation:**
```
Lead Feature (12 Komponenten):
✅ 1 von 12 Server-Driven: ContactDialog.tsx
❌ 11 von 12 Hart Codiert:
   - LeadWizard.tsx (Progressive Profiling)
   - LeadEditDialog.tsx (Stammdaten bearbeiten)
   - BusinessPotentialDialog.tsx
   - ActivityDialog.tsx
   - PainScoreForm.tsx
   - RevenueScoreForm.tsx
   - EngagementScoreForm.tsx
   - ... weitere Utility-Dialoge
```

**Kritische Inkonsistenz:**
- **LeadWizard** (Lead erstellen) hat Feld "Quelle" ✅
- **LeadEditDialog** (Lead bearbeiten) hatte Feld "Quelle" NICHT ❌ (Fix: 2025-10-29)
- Parity-Script erkennt diese Diskrepanz NICHT! ❌

**User Feedback:**
> "Das sind unterschiedlichen Karten (Lead-Erfassung und Lead-Bearbeitung). Das muss natürlich die gleichen Felder haben. Das ist nicht sauber. Warum zeigt uns das script das nicht an?"

---

<a name="lead-ist-architektur"></a>
### 🔍 IST-ARCHITEKTUR ANALYSE

#### ✅ Einzige Server-Driven Komponente: ContactDialog

**File:** `frontend/src/features/leads/components/ContactDialog.tsx`

```typescript
// Server-Driven Pattern (KORREKT!)
const { data: schemas, isLoading: isLoadingSchema } = useContactSchema();
const contactSchema = schemas?.[0];

// Dynamische Feld-Generierung
contactSchema.sections.map(section => (
  <Box key={section.sectionId}>
    <Typography variant="h6">{section.title}</Typography>
    <Grid container spacing={2}>
      {section.fields.map(field => renderField(field))}
    </Grid>
  </Box>
))
```

**Schema Quelle:** `GET /api/contacts/schema` (ContactSchemaResource.java)

**Vorteile:**
- ✅ Single Source of Truth (Backend)
- ✅ 3 Sections: basic_info, relationship, social_business
- ✅ Keine hardcoded Felder im Frontend
- ✅ Enum-Loading dynamisch

---

#### ❌ Hardcoded Komponenten (11 von 12)

**1. LeadWizard.tsx (Progressive Profiling)**

**Lines:** 50-100
```typescript
// ❌ HART CODIERT!
const [formData, setFormData] = useState({
  companyName: '',
  source: '' as LeadSource | '',
  website: '',
  phone: '',
  email: '',
  street: '',
  postalCode: '',
  city: '',
  businessType: '' as BusinessType | '',
  employeeCount: '',
  // ... 15 weitere Felder HART CODIERT!
});
```

**Problem:**
- Felder hart codiert
- Keine Schema-Definition im Backend
- Progressive Profiling Stages (0, 1, 2) hart codiert
- Änderungen = Frontend + Backend anpassen

---

**2. LeadEditDialog.tsx (Stammdaten bearbeiten)**

**Lines:** 43-50
```typescript
// ❌ HART CODIERT (bis Fix 2025-10-29)!
const [formData, setFormData] = useState({
  companyName: '',
  website: '',
  street: '',
  postalCode: '',
  city: '',
  source: '' as LeadSource | '', // Fix 2025-10-29
});
```

**Problem:**
- Unvollständige Field-Coverage (source fehlte)
- Create/Edit Dialog Parity-Issue
- Parity-Script erkennt das NICHT!

---

**3. BusinessPotentialDialog.tsx**

**Lines:** 40-60
```typescript
// ❌ HART CODIERT!
const [formData, setFormData] = useState({
  businessType: '',
  estimatedBudget: '',
  decisionTimeframe: '',
  notes: '',
});
```

---

**4. ActivityDialog.tsx**

**Lines:** 30-50
```typescript
// ❌ HART CODIERT!
const [formData, setFormData] = useState({
  activityType: '',
  title: '',
  description: '',
  scheduledDate: '',
  // ... 8 weitere Felder
});
```

---

**5-7. Score-Formulare (Pain, Revenue, Engagement)**

```typescript
// ❌ ALLE HART CODIERT!
// PainScoreForm.tsx
// RevenueScoreForm.tsx
// EngagementScoreForm.tsx
```

---

<a name="lead-ziel"></a>
### 🎯 ZIEL: FULL SERVER-DRIVEN ARCHITECTURE

**Neue Architektur:**
```
┌─────────────────────────────────────────────────┐
│ Backend: LeadSchemaResource (NEW!)              │
│ ✅ SINGLE SOURCE OF TRUTH                       │
│                                                  │
│ GET /api/leads/schema                           │
│   → Progressive Profiling (Stage 0, 1, 2)      │
│   → Detail Cards (Edit Dialog)                  │
│   → Business Potential Schema                   │
│   → Activity Schema                             │
│   → Score Schemas (Pain, Revenue, Engagement)  │
│                                                  │
│ Jedes Feld hat Metadaten:                      │
│ - showInWizard: true/false                     │
│ - wizardStage: 0|1|2 (Progressive Profiling)   │
│ - showInEditDialog: true/false                 │
│ - required: true/false                         │
│ - enumSource: "/api/enums/lead-sources"       │
└─────────────────────────────────────────────────┘
         ↓                              ↓
    ┌────────────┐              ┌──────────────┐
    │ LeadWizard │              │ LeadEditDialog│
    │ (REFACTOR) │              │ (REFACTOR)   │
    └────────────┘              └──────────────┘
         ↓                              ↓
  Stage 0: Felder mit           Felder mit
  wizardStage=0                showInEditDialog=true
```

---

<a name="lead-customer-mapping"></a>
### 📋 LEAD→CUSTOMER KONVERSION: FELD-MAPPING

**Analyse:** ConvertToCustomerDialog.tsx (Lines 59-64)

```typescript
interface ConvertToCustomerRequest {
  companyName: string;           // ✅ Von Lead übernommen
  xentralCustomerId?: string;    // ✅ Neu bei Konvertierung
  hierarchyType?: 'STANDALONE' | 'HEADQUARTER' | 'FILIALE';  // ✅ Neu
  notes?: string;                 // ✅ Optional
}
```

**Vollständige Konvertierung (Backend):**

Backend konvertiert folgende Lead-Felder → Customer:
```
Lead Entity                        → Customer Entity
─────────────────────────────────────────────────────────
companyName                        → companyName ✅
source (MESSE, EMPFEHLUNG, etc.)  → customerSource ✅
website                            → website ✅
phone, email                       → contactInfo ✅
street, postalCode, city           → address (Location) ✅
businessType                       → businessType ✅
employeeCount                      → employeeCount ✅
estimatedBudget                    → estimatedRevenue ✅

// Pain Points (Score)
painStaffShortage, painHighCosts, → painPoints Card ✅
painQualityIssues, etc.

// Contacts (1:N)
Lead.contacts[]                    → Customer.contacts[] ✅

// Activities (1:N)
Lead.activities[]                  → Customer.activities[] ✅

// Scores
painScore, revenueScore,           → Score Aggregates ✅
engagementScore
```

**⚠️ KRITISCH:** Lead-Schemas MÜSSEN alle konvertierungskritischen Felder enthalten!

---

<a name="lead-implementation-plan"></a>
### 🏗️ IMPLEMENTATION PLAN (15 Tasks)

---

<a name="lead-phase-1-backend"></a>
#### **PHASE 1: BACKEND SCHEMA RESOURCES (4-6 Stunden)**

---

**Task 3: LeadSchemaResource erstellen**

**File:** `backend/.../api/LeadSchemaResource.java` (NEW)

**Endpoint:** `GET /api/leads/schema`

**Schema Struktur:**
```java
@Path("/api/leads/schema")
public class LeadSchemaResource {

  @GET
  @PermitAll
  public Response getLeadSchema() {
    return Response.ok(List.of(
      buildProgressiveProfilingSchema(),  // Stage 0, 1, 2
      buildEditDialogSchema(),             // Stammdaten bearbeiten
      buildBusinessPotentialSchema(),      // Business Potential Card
      buildActivitySchema(),               // Activity Dialog
      buildScoreSchemas()                  // Pain, Revenue, Engagement
    )).build();
  }

  // ========== PROGRESSIVE PROFILING ==========

  /**
   * Stage 0: Pre-Claim (MESSE, EMPFEHLUNG, etc.)
   * Minimale Felder: companyName, source, website, phone
   */
  private CardSchema buildProgressiveProfilingSchema() {
    return CardSchema.builder()
      .cardId("lead_progressive_profiling")
      .title("Lead erfassen")
      .sections(List.of(
        buildStage0Section(),  // Pre-Claim (MESSE, EMPFEHLUNG)
        buildStage1Section(),  // Vollschutz (6 Monate)
        buildStage2Section()   // Nurturing (erweitert)
      ))
      .build();
  }

  private CardSection buildStage0Section() {
    return CardSection.builder()
      .sectionId("stage_0_pre_claim")
      .title("Basis-Informationen (Pre-Claim)")
      .subtitle("Pflichtfelder für Lead-Schutz (10 Tage)")
      .fields(List.of(
        FieldDefinition.builder()
          .fieldKey("companyName")
          .label("Firmenname")
          .type(FieldType.TEXT)
          .required(true)
          .wizardStage(0)
          .showInEditDialog(true)
          .gridCols(12)
          .build(),
        FieldDefinition.builder()
          .fieldKey("source")
          .label("Quelle")
          .type(FieldType.ENUM)
          .enumSource("/api/enums/lead-sources")  // MESSE, EMPFEHLUNG, etc.
          .required(true)
          .wizardStage(0)
          .showInEditDialog(true)
          .gridCols(6)
          .build(),
        FieldDefinition.builder()
          .fieldKey("website")
          .label("Website")
          .type(FieldType.TEXT)
          .wizardStage(0)
          .showInEditDialog(true)
          .gridCols(6)
          .placeholder("https://...")
          .build(),
        // ... phone, email, street, postalCode, city
      ))
      .build();
  }

  private CardSection buildStage1Section() {
    return CardSection.builder()
      .sectionId("stage_1_vollschutz")
      .title("Erweiterte Informationen (Vollschutz)")
      .subtitle("Business Type, Budget, Employees")
      .fields(List.of(
        FieldDefinition.builder()
          .fieldKey("businessType")
          .label("Branche")
          .type(FieldType.ENUM)
          .enumSource("/api/enums/business-types")
          .wizardStage(1)
          .showInEditDialog(true)
          .gridCols(6)
          .build(),
        FieldDefinition.builder()
          .fieldKey("employeeCount")
          .label("Mitarbeiteranzahl")
          .type(FieldType.NUMBER)
          .wizardStage(1)
          .showInEditDialog(true)
          .gridCols(6)
          .build(),
        // ... estimatedBudget, decisionTimeframe
      ))
      .build();
  }

  private CardSection buildStage2Section() {
    return CardSection.builder()
      .sectionId("stage_2_nurturing")
      .title("Nurturing & Qualifikation")
      .subtitle("Pain Points, Scores, Activities")
      .fields(List.of(
        // Pain Points (wird in buildScoreSchemas() definiert)
        // Activities (wird in buildActivitySchema() definiert)
      ))
      .build();
  }
}
```

**Effort:** 2-3 Stunden

---

**Task 4: Lead-Schemas definieren (Stage 0, 1, 2)**

**Details:**
- Stage 0: Pre-Claim (5 Felder: companyName, source, website, phone, email)
- Stage 1: Vollschutz (8 Felder: + businessType, employeeCount, estimatedBudget, address)
- Stage 2: Nurturing (15+ Felder: + Pain Points, Activities, Scores)

**Lead Protection Logic:**
```
source = MESSE | EMPFEHLUNG → Pre-Claim (10 Tage)
Stage 1 completed → Vollschutz (6 Monate)
```

**Effort:** Integriert in Task 3

---

**Task 5: BusinessPotentialSchemaResource erstellen**

**File:** `backend/.../api/BusinessPotentialSchemaResource.java` (NEW)

**Endpoint:** `GET /api/business-potentials/schema`

**Schema:**
```java
@Path("/api/business-potentials/schema")
public class BusinessPotentialSchemaResource {

  @GET
  @PermitAll
  public Response getBusinessPotentialSchema() {
    CardSchema schema = CardSchema.builder()
      .cardId("business_potential")
      .title("Geschäftspotenzial")
      .sections(List.of(
        CardSection.builder()
          .sectionId("potential_assessment")
          .title("Potenzial-Bewertung")
          .fields(List.of(
            FieldDefinition.builder()
              .fieldKey("businessType")
              .label("Geschäftsart")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/business-types")
              .required(true)
              .gridCols(6)
              .build(),
            FieldDefinition.builder()
              .fieldKey("estimatedBudget")
              .label("Geschätztes Budget")
              .type(FieldType.CURRENCY)
              .gridCols(6)
              .helpText("Jährliches Budget für Lebensmittel/Getränke")
              .build(),
            FieldDefinition.builder()
              .fieldKey("decisionTimeframe")
              .label("Entscheidungszeitraum")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/decision-timeframes")
              .gridCols(6)
              .build(),
            FieldDefinition.builder()
              .fieldKey("notes")
              .label("Notizen")
              .type(FieldType.TEXTAREA)
              .gridCols(12)
              .placeholder("Weitere Informationen zum Geschäftspotenzial...")
              .build()
          ))
          .build()
      ))
      .build();

    return Response.ok(List.of(schema)).build();
  }
}
```

**Effort:** 1 Stunde

---

**Task 6: ActivitySchemaResource erstellen**

**File:** `backend/.../api/ActivitySchemaResource.java` (NEW)

**Endpoint:** `GET /api/activities/schema`

**Schema:**
```java
@Path("/api/activities/schema")
public class ActivitySchemaResource {

  @GET
  @PermitAll
  public Response getActivitySchema() {
    CardSchema schema = CardSchema.builder()
      .cardId("activity")
      .title("Aktivität")
      .sections(List.of(
        CardSection.builder()
          .sectionId("activity_details")
          .title("Aktivitätsdetails")
          .fields(List.of(
            FieldDefinition.builder()
              .fieldKey("activityType")
              .label("Aktivitätstyp")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/activity-types")  // CALL, EMAIL, MEETING, NOTE
              .required(true)
              .gridCols(6)
              .build(),
            FieldDefinition.builder()
              .fieldKey("title")
              .label("Titel")
              .type(FieldType.TEXT)
              .required(true)
              .gridCols(6)
              .placeholder("z.B. Erstgespräch, Follow-Up Call, ...")
              .build(),
            FieldDefinition.builder()
              .fieldKey("description")
              .label("Beschreibung")
              .type(FieldType.TEXTAREA)
              .gridCols(12)
              .placeholder("Details zur Aktivität...")
              .build(),
            FieldDefinition.builder()
              .fieldKey("scheduledDate")
              .label("Geplantes Datum")
              .type(FieldType.DATE)
              .gridCols(6)
              .build(),
            FieldDefinition.builder()
              .fieldKey("status")
              .label("Status")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/activity-status")  // PLANNED, COMPLETED, CANCELLED
              .gridCols(6)
              .build()
          ))
          .build()
      ))
      .build();

    return Response.ok(List.of(schema)).build();
  }
}
```

**Effort:** 1 Stunde

---

**Task 7: ScoreSchemaResource erstellen**

**File:** `backend/.../api/ScoreSchemaResource.java` (NEW)

**Endpoint:** `GET /api/scores/schema`

**Schema:**
```java
@Path("/api/scores/schema")
public class ScoreSchemaResource {

  @GET
  @PermitAll
  public Response getScoreSchemas() {
    return Response.ok(List.of(
      buildPainScoreSchema(),
      buildRevenueScoreSchema(),
      buildEngagementScoreSchema()
    )).build();
  }

  // ========== PAIN SCORE ==========

  private CardSchema buildPainScoreSchema() {
    return CardSchema.builder()
      .cardId("pain_score")
      .title("Pain Score")
      .subtitle("Schmerzpunkte des Kunden bewerten")
      .sections(List.of(
        CardSection.builder()
          .sectionId("pain_points")
          .title("Schmerzpunkte")
          .fields(List.of(
            FieldDefinition.builder()
              .fieldKey("painStaffShortage")
              .label("Personalmangel")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/pain-intensity")  // NONE, LOW, MEDIUM, HIGH
              .gridCols(6)
              .helpText("Wie stark ist der Personalmangel?")
              .build(),
            FieldDefinition.builder()
              .fieldKey("painHighCosts")
              .label("Hohe Kosten")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/pain-intensity")
              .gridCols(6)
              .build(),
            FieldDefinition.builder()
              .fieldKey("painQualityIssues")
              .label("Qualitätsprobleme")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/pain-intensity")
              .gridCols(6)
              .build(),
            // ... weitere Pain Points (6-9 Felder total)
            FieldDefinition.builder()
              .fieldKey("painNotes")
              .label("Notizen zu Schmerzpunkten")
              .type(FieldType.TEXTAREA)
              .gridCols(12)
              .placeholder("Weitere Schmerzpunkte oder Details...")
              .build()
          ))
          .build()
      ))
      .build();
  }

  // ========== REVENUE SCORE ==========

  private CardSchema buildRevenueScoreSchema() {
    return CardSchema.builder()
      .cardId("revenue_score")
      .title("Revenue Score")
      .subtitle("Umsatzpotenzial bewerten")
      .sections(List.of(
        CardSection.builder()
          .sectionId("revenue_potential")
          .title("Umsatzpotenzial")
          .fields(List.of(
            FieldDefinition.builder()
              .fieldKey("estimatedAnnualRevenue")
              .label("Geschätzter Jahresumsatz")
              .type(FieldType.CURRENCY)
              .gridCols(6)
              .helpText("Potentieller Jahresumsatz mit diesem Kunden")
              .build(),
            FieldDefinition.builder()
              .fieldKey("budgetAvailable")
              .label("Budget verfügbar")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/budget-availability")  // YES, NO, UNKNOWN
              .gridCols(6)
              .build(),
            // ... weitere Revenue-Felder
          ))
          .build()
      ))
      .build();
  }

  // ========== ENGAGEMENT SCORE ==========

  private CardSchema buildEngagementScoreSchema() {
    return CardSchema.builder()
      .cardId("engagement_score")
      .title("Engagement Score")
      .subtitle("Engagement-Level des Kunden bewerten")
      .sections(List.of(
        CardSection.builder()
          .sectionId("engagement_metrics")
          .title("Engagement-Metriken")
          .fields(List.of(
            FieldDefinition.builder()
              .fieldKey("responseRate")
              .label("Antwortrate")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/response-rates")  // FAST, MEDIUM, SLOW, NONE
              .gridCols(6)
              .helpText("Wie schnell reagiert der Kunde auf Anfragen?")
              .build(),
            FieldDefinition.builder()
              .fieldKey("meetingFrequency")
              .label("Meeting-Häufigkeit")
              .type(FieldType.ENUM)
              .enumSource("/api/enums/meeting-frequency")
              .gridCols(6)
              .build(),
            // ... weitere Engagement-Felder
          ))
          .build()
      ))
      .build();
  }
}
```

**Effort:** 1-2 Stunden

---

<a name="lead-phase-2-frontend"></a>
#### **PHASE 2: FRONTEND MIGRATION (3-5 Stunden)**

---

**Task 8: useLeadSchema Hook erstellen**

**File:** `frontend/src/hooks/useLeadSchema.ts` (NEW)

**Pattern:** Analog zu useContactSchema.ts

```typescript
import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders } from '../features/leads/hooks/shared';
import type { CardSchema } from './useContactSchema';  // Reuse types

/**
 * Fetch Lead schema from backend (Progressive Profiling + Edit)
 *
 * GET /api/leads/schema
 * Returns: Array of CardSchema (Progressive Profiling, Edit, Business, Activity, Scores)
 */
async function fetchLeadSchema(): Promise<CardSchema[]> {
  const response = await fetch(`${BASE_URL}/api/leads/schema`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch lead schema: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Hook to fetch Lead schema for dynamic form rendering
 *
 * Usage:
 * ```tsx
 * const { data: schemas, isLoading } = useLeadSchema();
 * const progressiveSchema = schemas?.find(s => s.cardId === 'lead_progressive_profiling');
 * const stage0Fields = progressiveSchema?.sections
 *   .find(s => s.sectionId === 'stage_0_pre_claim')?.fields;
 * ```
 */
export function useLeadSchema() {
  return useQuery({
    queryKey: ['schema', 'leads'],
    queryFn: fetchLeadSchema,
    staleTime: 10 * 60 * 1000,  // 10 minutes
    gcTime: 30 * 60 * 1000,     // 30 minutes
  });
}
```

**Effort:** 20 Minuten

---

**Task 9: LeadWizard zu schema-driven migrieren**

**File:** `frontend/.../LeadWizard.tsx` (REFACTOR)

**VORHER (Lines 50-100):**
```typescript
// ❌ HART CODIERT!
const [formData, setFormData] = useState({
  companyName: '',
  source: '' as LeadSource | '',
  website: '',
  // ... 15 weitere Felder
});

// ❌ Stage-Switch HART CODIERT!
{stage === 0 && <Stage0PreClaim formData={formData} onChange={setFormData} />}
{stage === 1 && <Stage1Vollschutz formData={formData} onChange={setFormData} />}
{stage === 2 && <Stage2Nurturing formData={formData} onChange={setFormData} />}
```

**NACHHER:**
```typescript
import { useLeadSchema } from '../../../hooks/useLeadSchema';
import { renderField } from '../../../utils/schemaRenderer';  // Shared renderer

export function LeadWizard({ open, onClose, onSave }: LeadWizardProps) {
  const { data: schemas, isLoading: isLoadingSchema } = useLeadSchema();
  const progressiveSchema = schemas?.find(s => s.cardId === 'lead_progressive_profiling');

  const [stage, setStage] = useState(0);
  const [formData, setFormData] = useState<Record<string, any>>({});

  // ✅ Stage-Sections aus Schema!
  const stageSection = progressiveSchema?.sections[stage];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {stageSection?.title || 'Lead erfassen'}
        <Typography variant="body2" color="text.secondary">
          {stageSection?.subtitle}
        </Typography>
      </DialogTitle>

      <DialogContent>
        {isLoadingSchema && <CircularProgress />}

        {stageSection && (
          <Grid container spacing={2}>
            {stageSection.fields.map(field => renderField(field, formData, setFormData))}
          </Grid>
        )}
      </DialogContent>

      <DialogActions>
        {stage > 0 && <Button onClick={() => setStage(stage - 1)}>Zurück</Button>}
        <Button onClick={onClose}>Abbrechen</Button>
        {stage < 2 && <Button onClick={() => setStage(stage + 1)}>Weiter</Button>}
        {stage === 2 && <Button onClick={handleSave}>Lead anlegen</Button>}
      </DialogActions>
    </Dialog>
  );
}
```

**Effort:** 1-2 Stunden (+ renderField Util-Funktion)

---

**Task 10: LeadEditDialog zu schema-driven migrieren**

**File:** `frontend/.../LeadEditDialog.tsx` (REFACTOR)

**VORHER (Lines 43-50):**
```typescript
// ❌ HART CODIERT!
const [formData, setFormData] = useState({
  companyName: '',
  website: '',
  street: '',
  postalCode: '',
  city: '',
  source: '' as LeadSource | '',
});
```

**NACHHER:**
```typescript
import { useLeadSchema } from '../../../hooks/useLeadSchema';
import { renderField } from '../../../utils/schemaRenderer';

export function LeadEditDialog({ open, onClose, lead, onSave }: LeadEditDialogProps) {
  const { data: schemas, isLoading: isLoadingSchema } = useLeadSchema();
  const editSchema = schemas?.find(s => s.cardId === 'lead_edit');

  const [formData, setFormData] = useState<Record<string, any>>({});

  // ✅ Initialize from schema fields
  useEffect(() => {
    if (lead && editSchema) {
      const initialData: Record<string, any> = {};
      editSchema.sections.forEach(section => {
        section.fields.forEach(field => {
          initialData[field.fieldKey] = lead[field.fieldKey] || '';
        });
      });
      setFormData(initialData);
    }
  }, [lead, editSchema]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Stammdaten bearbeiten</DialogTitle>

      <DialogContent>
        {isLoadingSchema && <CircularProgress />}

        {editSchema?.sections.map(section => (
          <Box key={section.sectionId} sx={{ mb: 3 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>
              {section.title}
            </Typography>
            <Grid container spacing={2}>
              {section.fields.map(field => renderField(field, formData, setFormData))}
            </Grid>
          </Box>
        ))}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={handleSave} variant="contained">
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
}
```

**Effort:** 30-45 Minuten

---

**Task 11-13: Weitere Dialog-Migrationen**

**BusinessPotentialDialog, ActivityDialog, Score-Forms:**

Analog zu Task 10, jeweils:
1. `useLeadSchema()` Hook nutzen
2. Schema mit `cardId` filtern
3. `renderField()` Util für dynamische Felder
4. Hardcoded State entfernen

**Effort je Dialog:** 30-45 Minuten
**Total:** 1.5-2 Stunden

---

<a name="lead-phase-3-infrastructure"></a>
#### **PHASE 3: INFRASTRUCTURE (1-2 Stunden)**

---

**Task 14: Parity-Script erweitern**

**File:** `scripts/check-server-driven-parity.py` (EDIT)

**Neue Validierung:**
```python
def check_create_edit_dialog_parity():
    """
    Validate that Create and Edit dialogs for the same entity
    have matching field sets.

    Example:
    - LeadWizard (Create) vs LeadEditDialog (Edit)
    - Ensures both have same fields (e.g., "source")
    """

    pairs = [
        ("LeadWizard.tsx", "LeadEditDialog.tsx"),
        ("CustomerWizard.tsx", "CustomerEditDialog.tsx"),
        # Add more pairs as needed
    ]

    for create_file, edit_file in pairs:
        create_fields = extract_fields_from_component(create_file)
        edit_fields = extract_fields_from_component(edit_file)

        missing_in_edit = create_fields - edit_fields
        missing_in_create = edit_fields - create_fields

        if missing_in_edit:
            print(f"❌ PARITY ISSUE: {edit_file} missing fields: {missing_in_edit}")
        if missing_in_create:
            print(f"❌ PARITY ISSUE: {create_file} missing fields: {missing_in_create}")

        if not missing_in_edit and not missing_in_create:
            print(f"✅ PARITY OK: {create_file} ↔ {edit_file}")

def extract_fields_from_component(file_path: str) -> set:
    """
    Extract field keys from component (either hardcoded or schema-driven)

    Handles:
    - Hardcoded: useState({ companyName: '', source: '', ... })
    - Schema-driven: renderField(field) → extract field.fieldKey
    """
    # Implementation using AST parsing or regex
    pass
```

**Effort:** 1-1.5 Stunden

---

**Task 15: Tests durchführen**

**Test Plan:**
```bash
# 1. Backend Compile
cd backend
./mvnw clean compile
# ✅ Erwarte: BUILD SUCCESS

# 2. Frontend Build
cd frontend
npm run build
# ✅ Erwarte: Build successful

# 3. Parity Check
python3 scripts/check-server-driven-parity.py
# ✅ Erwarte: All checks passed

# 4. Integration Test: LeadWizard
# - Open LeadWizard (Progressive Profiling)
# - Stage 0: Expect fields from backend schema
# - Stage 1: Expect fields from backend schema
# - Stage 2: Expect fields from backend schema
# - Create Lead → Success

# 5. Integration Test: LeadEditDialog
# - Open Lead in table
# - Click "Bearbeiten"
# - Expect: SAME fields as LeadWizard Stage 0+1
# - Edit source field → Save → Success

# 6. Integration Test: ContactDialog
# - Already server-driven
# - Expect: No changes needed
# - Create Contact → Success
```

**Effort:** 30-45 Minuten

---

<a name="lead-benefits"></a>
### ✅ BENEFITS DER MIGRATION

1. **✅ Single Source of Truth**
   - Backend definiert ALLE Lead-Schemas
   - Wizard, Edit, Business, Activity, Scores synchron

2. **✅ Parity Guaranteed**
   - Create/Edit Dialoge nutzen GLEICHE Schemas
   - Script erkennt Diskrepanzen sofort

3. **✅ Lead→Customer Konsistent**
   - Lead-Schemas = Customer-Schemas (Felder)
   - Konvertierung verliert keine Daten

4. **✅ Wartbarkeit**
   - Neues Feld: Nur Backend ändern
   - Frontend aktualisiert automatisch
   - Kein fieldCatalog.json mehr!

5. **✅ Progressive Profiling Server-Driven**
   - Stage 0, 1, 2 im Backend definiert
   - Flexibles Lead-Schutz-Modell

6. **✅ Moderner Standard**
   - Salesforce, HubSpot nutzen Server-Driven UI
   - Best Practice für SaaS

---

<a name="lead-effort"></a>
### 📊 EFFORT SUMMARY

**Phase 1 - Backend (4-6 Stunden):**
- Task 3: LeadSchemaResource (2-3h)
- Task 4: Progressive Profiling Stages (integriert)
- Task 5: BusinessPotentialSchemaResource (1h)
- Task 6: ActivitySchemaResource (1h)
- Task 7: ScoreSchemaResource (1-2h)

**Phase 2 - Frontend (3-5 Stunden):**
- Task 8: useLeadSchema Hook (20min)
- Task 9: LeadWizard Refactor (1-2h)
- Task 10: LeadEditDialog Refactor (30-45min)
- Task 11-13: Business/Activity/Score Refactor (1.5-2h)

**Phase 3 - Infrastructure (1-2 Stunden):**
- Task 14: Parity-Script Erweiterung (1-1.5h)
- Task 15: Tests (30-45min)

**TOTAL: 8-13 Stunden** (conservative estimate)

**Recommendation:** Split in 2-3 Sessions à 3-4 Stunden

---

<a name="lead-risks"></a>
### 🚨 RISKS & MITIGATION

**Risk 1: Large Refactor → Context Loss**
- **Mitigation:** Split in 3 Phases, separate sessions
- **Checkpoint:** After each phase (Backend → Frontend → Infrastructure)

**Risk 2: Lead→Customer Conversion Breaks**
- **Mitigation:** Integration Test with real conversion
- **Test Data:** Use SEED Leads → Convert to Customer
- **Validation:** All fields transferred correctly

**Risk 3: Progressive Profiling Logic Complex**
- **Mitigation:** Document Stage 0/1/2 Logic in Backend
- **ADR:** Write ADR for Progressive Profiling Architecture

**Risk 4: Frontend renderField() Util Missing**
- **Mitigation:** Extract from ContactDialog (already implemented)
- **Reusable:** Use for ALL schema-driven components

---

<a name="lead-acceptance-criteria"></a>
### 🎯 ACCEPTANCE CRITERIA

**Backend:**
1. ✅ LeadSchemaResource returns Progressive Profiling Schema (Stage 0, 1, 2)
2. ✅ BusinessPotentialSchemaResource returns Business Potential Schema
3. ✅ ActivitySchemaResource returns Activity Schema
4. ✅ ScoreSchemaResource returns Pain/Revenue/Engagement Schemas
5. ✅ All Enums (lead-sources, business-types, etc.) available via `/api/enums/*`

**Frontend:**
6. ✅ useLeadSchema Hook fetches schemas from backend
7. ✅ LeadWizard uses schema-driven rendering (Stage 0, 1, 2)
8. ✅ LeadEditDialog uses schema-driven rendering
9. ✅ BusinessPotentialDialog uses schema-driven rendering
10. ✅ ActivityDialog uses schema-driven rendering
11. ✅ Score-Forms use schema-driven rendering

**Parity:**
12. ✅ Parity-Script validates Create/Edit Dialog field consistency
13. ✅ LeadWizard ↔ LeadEditDialog have SAME fields (source, etc.)
14. ✅ No hardcoded fields in any Lead component (except ContactDialog - already OK)

**Tests:**
15. ✅ Backend compiles without errors
16. ✅ Frontend builds without errors
17. ✅ Parity-Script passes all checks
18. ✅ Integration Test: LeadWizard → Create Lead → Success
19. ✅ Integration Test: LeadEditDialog → Edit Lead → Success
20. ✅ Integration Test: Lead → Customer Conversion → All fields transferred

---

<a name="lead-references"></a>
### 📚 REFERENCES

**Backend Patterns:**
- `ContactSchemaResource.java` - Reference implementation
- `CustomerSchemaResource.java` - Customer schemas (Lines 701-1014 in SPEC_D11)

**Frontend Patterns:**
- `ContactDialog.tsx` - Schema-driven dialog (already implemented)
- `useContactSchema.ts` - Hook pattern

**Lead→Customer Conversion:**
- `ConvertToCustomerDialog.tsx` - Field mapping
- `Lead.java` Entity - Source fields
- `Customer.java` Entity - Target fields

**Parity Validation:**
- `check-server-driven-parity.py` - Current script
- Need extension: Create/Edit Dialog validation

---

**🚀 READY TO START:** Sprint 2.1.7.2 D11.2 - Server-Driven Lead Migration

**Next Session:** Begin with Phase 1 (Backend) - Fresh Context Required!
