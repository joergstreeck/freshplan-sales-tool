# 🎯 SPEC: Customer Detail View - Cockpit Pattern (AKTUELL)

**📅 Erstellt:** 2025-10-26
**📝 Status:** ✅ AKTIV - VERBINDLICH
**👤 Autor:** Jörg Streeck + Strategic Consultant
**Sprint:** 2.1.7.2 - D11 Customer Detail View

---

## 📋 EXECUTIVE SUMMARY

**Kunde braucht:** CRM-Kundendetailansicht mit schnellem Zugriff auf Stammdaten, Kommunikationshistorie und Sales-Actions.

**Lösung:** Cockpit-Pattern (wie bestehendes FreshPlan Cockpit) - Separate Detail-Page mit Action Buttons + Tabs.

**Ersetzt:** Progressive Disclosure Pattern (siehe `SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md`)

---

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

## 🗑️ FILES TO DELETE/MODIFY

**Löschen (später, nach Fertigstellung):**
- `CustomerCompactView.tsx` - Nicht mehr benötigt
- `CustomerDetailModal.tsx` - Nicht mehr benötigt

**Modifizieren:**
- `CustomersPageV2.tsx` - Split-Panel entfernen, onClick zu navigate ändern

---

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

## 📚 REFERENCES

- **Alte Spec:** `SPEC_D11_DEPRECATED_PROGRESSIVE_DISCLOSURE.md` (nicht mehr gültig)
- **Cockpit Screenshot:** User-provided screenshot (Basis für Action Buttons + Timeline)
- **Design System:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md`
- **Router:** React Router v6 (bereits im Projekt)

---

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

### 📚 REFERENCES

- **Backend API:** `GET /api/customers/{id}/contacts` (ContactResource.java:23)
- **Frontend Component:** `CustomerDetailTabVerlauf.tsx:57-64`
- **Frontend Type:** `ContactEditDialog.tsx:38`
- **SEED Data:** Betriebsgastronomie TechPark Frankfurt GmbH → Julia Hoffmann

---

**🚨 PRIORITY:** HIGH - User sieht aktuell KEINE Kontakte (obwohl vorhanden!)

---

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

### 📚 REFERENCES

- **Backend Schema API:** `GET /api/customers/schema`
- **Bestehender Hook:** `frontend/.../useFieldDefinitions.ts`
- **Wizard Steps:** `frontend/.../components/steps/Step*.tsx`
- **Detail-Tabs:** `frontend/.../components/detail/CustomerDetailTab*.tsx`

---

**🚨 WICHTIG:** Dieser Refactoring MUSS vor Sprint 2.2 (Lead-Konvertierung) erfolgen, sonst werden Daten verloren!
