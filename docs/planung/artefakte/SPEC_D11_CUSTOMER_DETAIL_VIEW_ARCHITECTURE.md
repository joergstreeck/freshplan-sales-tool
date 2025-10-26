# 🎯 SPEC: Customer Detail View Architecture (D11)

**📅 Erstellt:** 2025-10-26
**🔄 Aktualisiert:** 2025-10-26 (Architektur-Korrektur)
**🎯 Sprint:** 2.1.7.2 - D11 Server-Driven Customer Cards
**🔄 Status:** ✅ APPROVED (Final Architecture)
**👤 Autor:** Jörg Streeck + Claude Code

---

## 🚨 KRITISCH: KORREKTE ARCHITEKTUR

### ❌ FALSCHE IMPLEMENTIERUNG (wurde verworfen):
```
/customers → Liste
  → Click auf Kunde → Drawer/Popup öffnet sich (FALSCH!)
```

**Probleme:**
- Kein Header, keine Sidebar im Drawer
- Sieht aus wie Popup-Fenster
- Nicht vollständig nutzbar
- Inkonsistent mit Design System V2

### ✅ KORREKTE IMPLEMENTIERUNG (verbindlich):

```
/customers → Kundenliste (CustomersPageV2)
  ├─ Button [+ Neuer Kunde] → Wizard/Dialog öffnet sich
  └─ Click auf Kunde → Navigate to /customers/:id

/customers/:id → Kunden-Detailseite (CustomerDetailPage)
  ├─ MainLayoutV2 (Header + Sidebar + Theme V2)
  ├─ Tab "Firma" (3 Cards: company_profile, locations, classification)
  ├─ Tab "Geschäft" (4 Cards: business_data, contracts, pain_points, products)
  └─ Tab "Verlauf" (disabled - Phase 4)
```

---

## 📋 NAVIGATION & ROUTES

### Route 1: Kundenliste
**URL:** `/customers`
**Component:** `CustomersPageV2.tsx`
**Layout:** `MainLayoutV2` mit `maxWidth="full"`

**Features:**
- ✅ Tabelle mit allen Kunden (VirtualizedCustomerTable)
- ✅ Button [+ Neuer Kunde] → öffnet CustomerOnboardingWizardModal
- ✅ Click auf Tabellenzeile → `navigate('/customers/:id')`
- ✅ Filter, Suche, Sortierung
- ✅ Aktionen: Bearbeiten, Löschen

**WICHTIG:** Diese Seite wird **NICHT verändert**! Sie bleibt exakt wie sie ist.

### Route 2: Kunden-Detailseite (NEU!)
**URL:** `/customers/:id`
**Component:** `CustomerDetailPage.tsx` (NEU zu erstellen)
**Layout:** `MainLayoutV2` mit `maxWidth="xl"`

**Features:**
- ✅ Volle Seite mit Header + Sidebar (Theme V2)
- ✅ Kundenkopfzeile (Name, Status, Jahresumsatz)
- ✅ 3 Tabs: Firma, Geschäft, Verlauf
- ✅ Server-Driven Cards in jedem Tab
- ✅ Grid Layout: 2 Spalten Desktop, 1 Spalte Mobile

---

## 🏗️ KOMPONENTEN-STRUKTUR

### Frontend Files

```
/frontend/src/
├─ pages/
│  ├─ CustomersPageV2.tsx           ✅ BLEIBT UNVERÄNDERT
│  └─ CustomerDetailPage.tsx        🆕 NEU ERSTELLEN
│
├─ features/customers/components/detail/
│  ├─ CustomerDetailTabFirma.tsx    ✅ BEREITS VORHANDEN
│  ├─ CustomerDetailTabGeschaeft.tsx ✅ BEREITS VORHANDEN
│  └─ CustomerDetailView.tsx        ❌ LÖSCHEN (war Drawer - falsch!)
│
└─ components/
   └─ DynamicCustomerCard.tsx       ✅ BLEIBT UNVERÄNDERT
```

### Backend Files

```
/backend/src/main/java/de/freshplan/domain/customer/
├─ api/
│  └─ CustomerSchemaResource.java   🔧 KOMPLETT NEU SCHREIBEN
│
└─ entity/
   └─ Customer.java                 ✅ BLEIBT (hat bereits billingAddress, deliveryAddresses)
```

---

## 📑 TAB-STRUKTUR (Kunden-Detailseite)

### Tab 1: "Firma" (3 Cards)

**Cards (Server-Driven):**
1. `company_profile` - Unternehmensprofil
2. `locations` - Standorte (Multi-Location Details)
3. `classification` - Klassifikation & Größe

**Layout:**
```typescript
<Grid container spacing={2}>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={companyProfileSchema} customerId={id} />
  </Grid>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={locationsSchema} customerId={id} />
  </Grid>
  <Grid size={{ xs: 12, md: 6 }}>
    <DynamicCustomerCard schema={classificationSchema} customerId={id} />
  </Grid>
</Grid>
```

### Tab 2: "Geschäft" (4 Cards)

**Cards (Server-Driven):**
1. `business_data` - Geschäftsdaten & Performance
2. `contracts` - Vertragsbedingungen
3. `pain_points` - Bedürfnisse & Pain Points
4. `products` - Produktportfolio & Services

**Layout:** Gleiche Grid-Struktur wie Tab 1

### Tab 3: "Verlauf" (Phase 4 - SPÄTER)

**Status:** Disabled
**Inhalt:** Kontakte & Timeline (wird in Sprint 2.2.x implementiert)

---

## 🎨 DESIGN SYSTEM V2 COMPLIANCE

### MainLayoutV2 Integration

```typescript
// CustomerDetailPage.tsx
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';

export const CustomerDetailPage = () => {
  const { id } = useParams();
  const { data: customer } = useCustomerDetails(id);
  const [activeTab, setActiveTab] = useState(0);

  return (
    <MainLayoutV2 maxWidth="xl">
      {/* Header Section */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" sx={{ mb: 1 }}>
          {customer?.companyName || 'Kunde'}
        </Typography>
        <Chip label={customer?.status} color="primary" />
        <Typography variant="body2" color="text.secondary">
          Jahresumsatz: {formatCurrency(customer?.expectedAnnualVolume)}
        </Typography>
      </Box>

      {/* Tabs */}
      <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
        <Tab label="Firma" />
        <Tab label="Geschäft" />
        <Tab label="Verlauf" disabled />
      </Tabs>

      {/* Tab Panels */}
      {activeTab === 0 && <CustomerDetailTabFirma customerId={id} />}
      {activeTab === 1 && <CustomerDetailTabGeschaeft customerId={id} />}
    </MainLayoutV2>
  );
};
```

### Theme V2 Colors

**PFLICHT:** Alle Farben aus Theme verwenden!

```typescript
// ✅ RICHTIG
<Chip color="primary" /> // #94C456 (FreshFoodz Green)
<Typography variant="h4" /> // Antonio Bold, #004F7B (FreshFoodz Blue)

// ❌ FALSCH
<Chip sx={{ bgcolor: '#94C456' }} /> // Hardcoded - VERBOTEN!
```

---

## 🔄 LEAD → CUSTOMER KONVERSION (KRITISCH!)

### Welche Felder werden kopiert?

**Quelle:** `OpportunityService.convertToCustomer()` (Lines 445-584)

Wenn ein Kunde aus einem Lead erstellt wird, werden **automatisch 12 Felder** kopiert:

#### 1. Business-Daten (2 Felder)
```java
lead.businessType → customer.businessType         // Enum (z.B. GASTRONOMY, HOTEL, CATERING)
lead.estimatedVolume → customer.expectedAnnualVolume  // EUR (Jahresumsatz-Schätzung)
```

#### 2. Pain Points (9 Felder = 8 Booleans + 1 Text)
```java
lead.painStaffShortage → customer.painStaffShortage           // Boolean
lead.painHighCosts → customer.painHighCosts                   // Boolean
lead.painFoodWaste → customer.painFoodWaste                   // Boolean
lead.painQualityInconsistency → customer.painQualityInconsistency  // Boolean
lead.painTimePressure → customer.painTimePressure             // Boolean
lead.painSupplierQuality → customer.painSupplierQuality       // Boolean
lead.painUnreliableDelivery → customer.painUnreliableDelivery // Boolean
lead.painPoorService → customer.painPoorService               // Boolean
lead.painNotes → customer.painNotes                           // Text (Details)
```

#### 3. Traceability (1 Feld)
```java
lead.id → customer.originalLeadId  // UUID (V261 Field - ermöglicht Lead→Customer Nachverfolgung)
```

### 🎯 Konsequenzen für CustomerSchemaResource.java

**PFLICHT:** Diese Felder MÜSSEN in den entsprechenden Cards enthalten sein!

| Card | Felder aus Lead-Konversion |
|------|----------------------------|
| `business_data` | `expectedAnnualVolume` (von lead.estimatedVolume) |
| `pain_points` | ALLE 9 Pain-Point-Felder (8 Booleans + painNotes) |

**Warum kritisch?**
- Wenn diese Felder fehlen → Daten gehen bei Konversion verloren!
- Lead-Erfassung wäre nutzlos, wenn Pain Points nicht im Kunden ankommen
- expectedAnnualVolume ist Basis für Umsatz-Forecasts

---

## 🔧 BACKEND: CustomerSchemaResource.java

### Aktuelles Problem (KRITISCH!)

**❌ Backend liefert aktuell:**
```json
[
  { "cardId": "company-profile", ... },  // ❌ Bindestrich
  { "cardId": "business-data", ... },
  { "cardId": "needs-solutions", ... },  // ❌ Falscher Name
  { "cardId": "product-portfolio", ... } // ❌ Falscher Name
]
```

**❌ Probleme:**
1. Bindestriche statt Unterstriche
2. Falsche Card-Namen (`needs-solutions` → sollte `pain_points` sein)
3. Mega-Card Struktur (1 Card mit 4 Sections → sollten 3 separate Cards sein)

### ✅ KORREKTE Backend-Struktur

**Backend MUSS liefern:**
```json
[
  { "cardId": "company_profile", "order": 1, ... },
  { "cardId": "locations", "order": 2, ... },
  { "cardId": "classification", "order": 3, ... },
  { "cardId": "business_data", "order": 4, ... },
  { "cardId": "contracts", "order": 5, ... },
  { "cardId": "pain_points", "order": 6, ... },
  { "cardId": "products", "order": 7, ... }
]
```

**Verbindliche Card-Namen:**
| Card ID | Tab | Titel |
|---------|-----|-------|
| `company_profile` | Firma | Unternehmensprofil |
| `locations` | Firma | Standorte |
| `classification` | Firma | Klassifikation |
| `business_data` | Geschäft | Geschäftsdaten & Performance |
| `contracts` | Geschäft | Vertragsbedingungen |
| `pain_points` | Geschäft | Bedürfnisse & Pain Points |
| `products` | Geschäft | Produktportfolio & Services |

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Dokumentation korrigieren ✅
- [x] SPEC_D11 neu schreiben (diese Datei)
- [ ] TRIGGER_D11 aktualisieren (Drawer-Architektur entfernen)

### Phase 2: Backend CustomerSchemaResource.java neu schreiben
**Aufwand:** 2h

**Tasks:**
1. Alle 7 `build*Card()` Methoden neu schreiben
2. Korrekte `cardId` mit Unterstrichen
3. Separate Cards statt Mega-Cards
4. Korrekte Feld-Zuordnung

**Acceptance Criteria:**
- [ ] Backend liefert 7 separate Cards
- [ ] Alle `cardId` mit Unterstrichen
- [ ] Tab "Firma" Filter findet: `company_profile`, `locations`, `classification`
- [ ] Tab "Geschäft" Filter findet: `business_data`, `contracts`, `pain_points`, `products`

### Phase 3: Frontend CustomerDetailPage.tsx erstellen
**Aufwand:** 2h

**Tasks:**
1. `CustomerDetailPage.tsx` erstellen (volle Seite mit MainLayoutV2)
2. Tab-Navigation implementieren (Firma, Geschäft, Verlauf)
3. CustomerDetailTabFirma + CustomerDetailTabGeschaeft integrieren
4. Design System V2 Compliance (Theme, Colors, Grid v7)

**Acceptance Criteria:**
- [ ] Volle Seite mit Header + Sidebar
- [ ] Tab-Navigation funktioniert
- [ ] Cards werden korrekt angezeigt
- [ ] Grid Layout responsive (MUI v7 API)

### Phase 4: CustomersPageV2 Navigation fixen
**Aufwand:** 30min

**Tasks:**
1. Drawer-Code entfernen (Zeilen 60-62, 679-689)
2. Zurück zu `navigate('/customers/:id')` (Zeilen 554-556, etc.)
3. CustomerOnboardingWizard bleibt (für "Neuer Kunde" Button)

**Acceptance Criteria:**
- [ ] Click auf Kunde → Navigate zu `/customers/:id`
- [ ] Button "Neuer Kunde" → öffnet Wizard (bleibt unverändert)
- [ ] Keine Drawer/Modal mehr

### Phase 5: Routing hinzufügen
**Aufwand:** 15min

**Tasks:**
1. Route in `App.tsx` oder `routes.tsx` hinzufügen:
   ```typescript
   <Route path="/customers/:id" element={<CustomerDetailPage />} />
   ```

**Acceptance Criteria:**
- [ ] URL `/customers/c0000000-0123-...` öffnet Kunden-Detailseite
- [ ] Browser-Back funktioniert

### Phase 6: Cleanup
**Aufwand:** 15min

**Tasks:**
1. `CustomerDetailView.tsx` löschen (war Drawer - nicht mehr gebraucht)
2. Imports in allen Dateien korrigieren

---

## ✅ ACCEPTANCE CRITERIA (Gesamt)

### Funktional
- [ ] `/customers` → Kundenliste mit Button "Neuer Kunde"
- [ ] Button "Neuer Kunde" → öffnet Wizard
- [ ] Click auf Kunde → Navigate zu `/customers/:id`
- [ ] `/customers/:id` → Volle Seite mit Header + Sidebar
- [ ] Tab "Firma" zeigt 3 Cards mit Daten
- [ ] Tab "Geschäft" zeigt 4 Cards mit Daten
- [ ] Tab "Verlauf" ist disabled
- [ ] Browser-Back funktioniert korrekt

### Technisch
- [ ] MainLayoutV2 mit `maxWidth="xl"` verwendet
- [ ] Design System V2 compliant (keine hardcoded colors/fonts)
- [ ] MUI Grid v7 API (`size={{ xs, md }}`)
- [ ] Backend liefert korrekte Card-Namen (Unterstriche)
- [ ] Keine Console Warnings
- [ ] Pre-Commit Hooks grün

### Performance
- [ ] Seite lädt < 500ms
- [ ] Tab-Wechsel < 100ms
- [ ] Keine Render-Flickers

---

## 🎯 DONE CRITERIA

**D11 ist COMPLETE wenn:**

1. ✅ Dokumentation korrekt (diese SPEC)
2. ✅ Backend CustomerSchemaResource.java korrekt (7 Cards, Unterstriche)
3. ✅ CustomerDetailPage.tsx volle Seite mit Tabs
4. ✅ Navigation funktioniert (`/customers` → `/customers/:id`)
5. ✅ Design System V2 compliant
6. ✅ Tests ≥80% Coverage
7. ✅ PR merged to main
8. ✅ Browser-Test erfolgreich (Kunde KD-DEV-123)

---

**🤖 Erstellt mit Claude Code - Architektur-Spezifikation V2.0 (FINAL)**
