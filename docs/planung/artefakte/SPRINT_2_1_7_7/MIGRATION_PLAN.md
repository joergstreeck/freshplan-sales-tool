# Migration Plan: CustomersPageV2 → CustomersPage + LeadsPage

**Sprint:** 2.1.7.7
**Datum:** 2025-11-01
**Status:** ✅ Recherche Complete, 🚀 Ready for M1
**Aufwand:** 12h (Tag 1-2)
**Strategie:** Strangler Fig Pattern (Neue Struktur wächst neben alter)

---

## 📑 INHALTSVERZEICHNIS

### **QUICK START**
- [🎯 Warum Migration?](#-warum-migration) - Problem & Lösung
- [🔄 Migration-Phasen (M1-M6) - Übersicht](#-migrations-phasen-m1-m6) - Schnellübersicht aller Phasen

### **RECHERCHE & ANALYSE**
1. [📊 Recherche-Ergebnisse](#-recherche-ergebnisse) - Vollständige IST-Analyse
   - [1️⃣ CustomersPageV2 - Aktuelle Struktur](#1️⃣-customerspagev2---aktuelle-struktur)
   - [2️⃣ Context-Branching - 26 Verzweigungen](#2️⃣-context-branching---26-verzweigungen)
   - [3️⃣ Routing - Aktuelle Konfiguration](#3️⃣-routing---aktuelle-konfiguration)
   - [4️⃣ Shared Components - Was existiert?](#4️⃣-shared-components---was-existiert-bereits)
   - [5️⃣ Dependencies - Was hängt an CustomersPageV2?](#5️⃣-dependencies---was-hängt-an-customerspagev2)
   - [6️⃣ Feature-Parität - Was muss erhalten bleiben?](#6️⃣-feature-parität---was-muss-erhalten-bleiben)
   - [🚨 Kritische Unterschiede](#-kritische-unterschiede-must-preserve)

### **MIGRATIONS-PHASEN (DETAILLIERT)**
2. [🔄 Phase I: M1 - Shared Infrastructure (4h)](#-phase-i-foundation-tag-1-vormittag-4h)
3. [🔄 Phase II: M2 - CustomersPage (4h)](#-phase-ii-new-customers-page-tag-1-nachmittag-4h)
4. [🔄 Phase III: M3 - LeadsPage (3h)](#-phase-iii-new-leads-page-tag-2-vormittag-3h)
5. [🔄 Phase IV: M4 - Routing Switch (1h) 🚨](#-phase-iv-routing-switch-tag-2-nachmittag-1h)
6. [🔄 Phase V: M5 - Testing (2h)](#-phase-v-testing--cleanup-tag-2-abend-2h)
7. [🔄 Phase VI: M6 - Cleanup (30min)](#m6-customerspagev2-deprecaten--löschen-️-30min)

### **REFERENZ & METRIKEN**
8. [📊 Erfolgsmetriken](#-erfolgsmetriken) - Vorher/Nachher Vergleich
9. [🚨 Risiko-Management](#-risiko-management) - Rollback-Strategien
10. [🎯 Integration mit Sprint 2.1.7.7](#-integration-mit-sprint-2177) - Timeline
11. [📋 Fehlende Details](#-fehlende-details-todo-während-implementation) - TODO während Implementation
12. [✅ Go/No-Go Checklist](#-gono-go-checklist) - Entscheidungspunkte

---

### **🚀 QUICK NAVIGATION FÜR NEUE CLAUDE-INSTANZ**

**Wenn du eine neue Claude-Instanz bist und diese Migration fortsetzen sollst:**

1. ✅ **Lies zuerst:** [Warum Migration?](#-warum-migration) - Verstehe das Problem
2. ✅ **Dann:** [Kritische Unterschiede](#-kritische-unterschiede-must-preserve) - **MUST READ!**
3. ✅ **Aktueller Status?** Siehe [Migration-Phasen Übersicht](#-migrations-phasen-m1-m6)
4. ✅ **Nächster Schritt?** Finde aktuelle Phase (M1-M6) und folge dem Checkpoint

**Kritische Punkte (NIEMALS vergessen!):**
- ⚠️ Customers = UUID Navigation (`/customers/uuid`)
- ⚠️ Leads = SLUG Navigation (`/leads/firmenname-123`)
- ⚠️ VORMERKUNG Dialog nur bei Leads!
- ⚠️ After Create: Customers → Detail, Leads → Stay on List

---

## 🎯 WARUM MIGRATION?

### **Problem:**
`CustomersPageV2.tsx` ist eine **monolithische 690-Zeilen Komponente** mit:
- **26 Context-Verzweigungen** (`context === 'leads' ? ... : ...`)
- **Nur 40% Shared Logic** zwischen Customers und Leads
- **60% Context-spezifische Logic** → schlechte Abstraktion!
- **Schwer testbar** (jede Funktion braucht 2x Tests)
- **Schwer wartbar** (jede Änderung = Context-Check nötig)
- **AI-Agent Confusion** (Claude macht Fehler bei Änderungen)

### **Lösung:**
Trennung in **2 separate Pages** mit **Shared Infrastructure**:
```
CustomersPageV2 (690 LOC, 26 Branches)
    ↓
CustomersPage (~200 LOC, 0 Branches) + LeadsPage (~180 LOC, 0 Branches)
    ↓
Shared Components (DataTable, FilterBar, Pagination)
```

---

## 🔄 MIGRATIONS-PHASEN (M1-M6)

**Schnellübersicht:** Was passiert in welcher Phase?

| Phase | Aufwand | Was wird gemacht? | Status | Rollback |
|-------|---------|-------------------|--------|----------|
| **[M1](#-phase-i-foundation-tag-1-vormittag-4h)** | 4h | Shared Infrastructure extrahieren | ✅ COMPLETE | `git stash` |
| **[M2](#-phase-ii-new-customers-page-tag-1-nachmittag-4h)** | 4h | CustomersPage (neu) implementieren | ✅ COMPLETE | Datei löschen |
| **[M4](#-phase-iv-routing-switch-tag-2-nachmittag-1h)** 🚨 | 30min | Routing Switch - Customers LIVE! | ✅ COMPLETE | Route zurück |
| **[M3](#-phase-iii-new-leads-page-tag-2-vormittag-3h)** | 3h | LeadsPage (neu) implementieren | ⏸️ SKIPPED | Später |
| **[M5](#-phase-v-testing--cleanup-tag-2-abend-2h)** | 2h | Tests schreiben | ⏳ PENDING | Skip Tests |
| **[M6](#m6-customerspagev2-deprecaten--löschen-️-30min)** | 30min | CustomersPageV2 deprecaten | ⏳ PENDING | `git revert` |

**Kritische Phase:** M4 (Routing Switch) - Feature Flag für sofortigen Rollback!

**Visual Checkpoints:** Nach JEDER Phase → Jörg testet im Browser!

---

## 📊 RECHERCHE-ERGEBNISSE

### 1️⃣ CustomersPageV2 - Aktuelle Struktur

| Eigenschaft | Wert | Notizen |
|------------|------|---------|
| **Pfad** | `/frontend/src/pages/CustomersPageV2.tsx` | ✅ Gefunden |
| **LOC** | **690 Zeilen** | Sehr groß! |
| **Props** | `CustomersPageV2Props` | Siehe unten |
| **Context-Prop** | `context?: 'customers' \| 'leads'` | Default: `'customers'` |

**Props Interface:**
```tsx
interface CustomersPageV2Props {
  openWizard?: boolean;
  defaultFilter?: FilterConfig;
  title?: string;
  createButtonLabel?: string;
  context?: 'customers' | 'leads'; // ← Kritisch!
}
```

**Haupt-Dependencies:**
- **25 imports** total
- React Hooks: useState, useEffect, useMemo
- MUI: Box, Tabs, Tab, Button
- Wizards: CustomerOnboardingWizardModal, LeadWizard
- Tables: CustomerTable, VirtualizedCustomerTable
- Intelligence: 4 Dashboards (2 für Customers, 2 für Leads)
- Filter: IntelligentFilterBar

---

### 2️⃣ Context-Branching - 26 Verzweigungen

| Kategorie | Count | Zeilen | Beispiel |
|-----------|-------|---------|----------|
| **Data Loading** | 4 | 219, 225, 228, 237 | `context === 'customers' ? serverSideData : leadsData` |
| **Tab Labels** | 3 | 493-495 | `context === 'leads' ? 'Lead-Liste' : 'Kundenliste'` |
| **Empty State** | 3 | 504-513 | `context === 'leads' ? 'Noch keine Leads' : 'Noch keine Kunden'` |
| **Navigation (Table)** | 8 | 550-603 | `context === 'customers' ? navigate('/customers/:id') : generateLeadUrl()` |
| **Load More Button** | 1 | 623 | `context === 'leads' ? 'Weitere Leads' : 'Weitere Kunden'` |
| **Tab Content** | 2 | 633, 636 | `context === 'leads' ? <LeadQualityDashboard> : <DataHygieneDashboard>` |
| **Wizard** | 1 | 640 | `context === 'leads' ? <LeadWizard> : <CustomerOnboardingWizard>` |
| **Toast Labels** | 2 | 442, 682 | `context === 'leads' ? 'Lead' : 'Kunde'` |
| **Navigation (Callback)** | 1 | 465 | `if (context === 'customers') navigate(...)` |
| **Lead-specific Logic** | 1 | 560, 594 | `context === 'leads' && customer.leadStage === 'VORMERKUNG'` |

**Total: 26 Context-Verzweigungen**

---

### 3️⃣ Routing - Aktuelle Konfiguration

**Routing-Datei:** `/frontend/src/providers.tsx`

**Customers Routes:**

| Route | Component | Props | Zeile |
|-------|-----------|-------|-------|
| `/customers` | `<CustomersPageV2 />` | Default (context='customers') | 180 |
| `/customers/new` | `<CustomersPageV2 openWizard={true} />` | Wizard sofort öffnen | 182-184 |
| `/customers/:customerId` | `<CustomerDetailPage />` | Detail-Ansicht | 185 |

**Leads Routes:**

| Route | Component | Props | Zeile | Feature Flag |
|-------|-----------|-------|-------|--------------|
| `/lead-generation/leads` | `<LeadsPage />` | Wrapper um CustomersPageV2 | 267 | `FEAT_LEADGEN` |
| `/lead-generation/leads/:slug` | `<LeadDetailPage />` | Detail mit Slug | 272-274 | `FEAT_LEADGEN` |

**Aktueller LeadsPage.tsx (Wrapper!):**
```tsx
// /pages/LeadsPage.tsx (18 LOC - purer Wrapper!)
import { CustomersPageV2 } from './CustomersPageV2';

export default function LeadsPage() {
  return (
    <CustomersPageV2
      title="Lead-Management"
      createButtonLabel="Lead erfassen"
      context="leads"  // ← Nur dieser Prop!
    />
  );
}
```

---

### 4️⃣ Shared Components - Was existiert bereits?

| Component | Pfad | Bereits Shared? | Generic? |
|-----------|------|----------------|----------|
| **CustomerTable** | `/features/customers/components/CustomerTable.tsx` | ❌ Nein | ⚠️ Teilweise (hat `context` prop) |
| **VirtualizedCustomerTable** | `/features/customers/components/VirtualizedCustomerTable.tsx` | ❌ Nein | ⚠️ Teilweise |
| **IntelligentFilterBar** | `/features/customers/components/filter/IntelligentFilterBar.tsx` | ❌ Nein | ⚠️ Ja (hat `context` prop!) |
| **CustomerListHeader** | `/features/customers/components/CustomerListHeader.tsx` | ❌ Nein | ✅ Ja (generic) |
| **CustomerListSkeleton** | `/features/customers/components/CustomerListSkeleton.tsx` | ❌ Nein | ✅ Ja |
| **EmptyStateHero** | `/components/common/EmptyStateHero.tsx` | ✅ Ja | ✅ Ja |

**Context Config (wichtig!):**

**Datei:** `/features/customers/components/filter/contextConfig.ts`

```tsx
// Separate Spalten-Config für Leads vs Customers!
export const LEADS_TABLE_COLUMNS: TableColumn[] = [
  { field: 'companyName', label: 'Lead', ... },
  { field: 'leadScore', label: 'Score', ... },  // ← Lead-spezifisch!
  ...
];

export const CUSTOMERS_TABLE_COLUMNS: TableColumn[] = [
  { field: 'companyName', label: 'Kunde', ... },
  { field: 'riskScore', label: 'Risiko', ... },  // ← Customer-spezifisch!
  ...
];
```

✅ **Gut:** Config ist bereits separiert!
⚠️ **Problem:** `contextConfig.ts` liegt unter `/customers/` - sollte nach `/shared/`!

---

### 5️⃣ Dependencies - Was hängt an CustomersPageV2?

**Dateien die CustomersPageV2 importieren:**

| Datei | Typ | Verwendung |
|-------|-----|-----------|
| `/providers.tsx` | Routing | Lazy-Import + Routes (Zeile 26, 180, 182) |
| `/pages/LeadsPage.tsx` | Wrapper | Wrapper mit `context="leads"` |
| `/pages/__tests__/CustomersPageV2.test.tsx` | Test | Unit Tests |
| `/features/customers/components/wizard/__tests__/CustomerOnboardingWizardModal.test.tsx` | Test | Integration Test |

**Total: 4 Dateien**

---

### 6️⃣ Feature-Parität - Was muss erhalten bleiben?

**CUSTOMERS Features:**

| Feature | Component/Code | Notes |
|---------|----------------|-------|
| **Tab 1:** Kundenliste | CustomerTable + Filters | Virtualisierung >20 items |
| **Tab 2:** Datenqualität | DataHygieneDashboard | Customer-spezifisch |
| **Tab 3:** Daten-Aktualität | DataFreshnessManager | Customer-spezifisch |
| **Wizard:** Customer Onboarding | CustomerOnboardingWizardModal | Multi-Step |
| **Navigation:** UUID-basiert | `navigate(/customers/${id})` | ⚠️ Kritisch! |
| **After Create:** Navigate Detail | `navigate(/customers/${id})` | ⚠️ Kritisch! |

**LEADS Features:**

| Feature | Component/Code | Notes |
|---------|----------------|-------|
| **Tab 1:** Lead-Liste | CustomerTable + Filters | Gleiche Tabelle! |
| **Tab 2:** Lead-Qualität | LeadQualityDashboard | Lead-spezifisch |
| **Tab 3:** Schutzfristen | LeadProtectionManager | Lead-spezifisch |
| **Wizard:** Lead erfassen | LeadWizard | Einfacher als Customer |
| **Dialog:** Erstkontakt (VORMERKUNG) | AddFirstContactDialog | ⚠️ Lead-Stage spezifisch! |
| **Dialog:** Lead löschen | DeleteLeadDialog | Context-aware labels |
| **Navigation:** Slug-basiert | `generateLeadUrl(name, id)` | ⚠️ Kritisch! |
| **After Create:** Stay on List | Kein navigate | ⚠️ Unterschied! |

**Feature-Matrix:**

| Feature | Customers | Leads | Identisch? |
|---------|-----------|-------|------------|
| **Filters** | Status, Industry, Risk | Status, BusinessType, LeadScore | ❌ 60% unterschiedlich |
| **Sort Options** | Risk, Umsatz, Last Contact | LeadScore, Expected Volume | ❌ 40% unterschiedlich |
| **Table Columns** | CustomerNumber, RiskScore | LeadScore, BusinessType | ❌ 40% unterschiedlich |
| **Navigation** | UUID: `/customers/uuid` | Slug: `/leads/slug` | ❌ Komplett unterschiedlich! |
| **After Create** | Navigate Detail | Stay on List | ❌ Unterschiedlich! |
| **Wizard** | Multi-Step Onboarding | Progressive Profiling | ❌ Unterschiedlich! |
| **Tab 2/3** | Datenqualität/Aktualität | Lead-Qualität/Schutz | ❌ Unterschiedlich! |
| **Pagination** | Server-side (>50) | Client-side (all) | ❌ Unterschiedlich! |

**Ergebnis:** **Nur ~40% ist identisch** zwischen Customers und Leads!

---

### 🚨 KRITISCHE UNTERSCHIEDE (MUST PRESERVE!)

#### **1. Navigation Unterschiede**

```tsx
// CUSTOMERS:
navigate(`/customers/${customer.id}`);  // UUID direkt
// → /customers/a1b2c3d4-e5f6-7890-abcd-ef1234567890

// LEADS:
navigate(generateLeadUrl(customer.companyName || 'lead', customer.id));  // Slug!
// → /leads/baeckerei-mueller-123
```

**Funktion `generateLeadUrl` muss beibehalten werden!**

#### **2. After Create Verhalten**

```tsx
// CUSTOMERS (Zeile 465-467):
if (context === 'customers') {
  navigate(`/customers/${customer.id}`);  // → Detail Page
}
// Bei Leads: Bleiben auf der Liste, neuer Lead wird highlighted
```

#### **3. VORMERKUNG Dialog**

```tsx
// Zeile 560, 594:
if (context === 'leads' && customer.leadStage === 'VORMERKUNG') {
  setFirstContactDialogOpen(true);  // ← Lead-spezifischer Flow!
}
```

#### **4. Pagination Unterschiede**

```tsx
// Zeile 237:
if (context === 'leads') return false; // No pagination for leads yet

// Customers: Server-side pagination (page 0, 1, 2...)
// Leads: Client-side (load all)
```

---

## 🔄 MIGRATIONS-PHASEN (M1-M6)

### **PHASE I: FOUNDATION (Tag 1 Vormittag, 4h)**

#### **M1: Shared Infrastructure extrahieren** ✅ COMPLETE ⏱️ 4h

**Ziel:** Shared Components bereitstellen, OHNE alte Struktur zu ändern

**Neue Ordnerstruktur:**

```
frontend/src/features/shared/
├── components/
│   ├── data-table/
│   │   ├── DataTable.tsx               (Generic Table)
│   │   ├── VirtualizedDataTable.tsx     (Virtualization)
│   │   └── DataTableTypes.ts            (Types)
│   ├── filter/
│   │   ├── FilterBar.tsx                (Generic FilterBar)
│   │   ├── FilterDrawer.tsx             (Filter Drawer)
│   │   └── FilterTypes.ts               (Filter Config Types)
│   └── search/
│       ├── UniversalSearch.tsx          (Search Input)
│       └── SearchTypes.ts               (Search Types)
├── hooks/
│   ├── useAdvancedSearch.ts             (Search Hook)
│   ├── usePagination.ts                 (Pagination Hook)
│   └── useSorting.ts                    (Sorting Hook)
└── utils/
    ├── dataFormatters.ts                (Format Currency, Date, etc.)
    └── tableHelpers.ts                  (Table Helpers)
```

**Extrahierte Components:**
1. **DataTable** (Generic, Config-driven)
2. **FilterBar** (Generic, Config-driven)
3. **Pagination** (Generic Hook)
4. **Sorting** (Generic Hook)
5. **Formatters** (Currency, Date, etc.)

**✅ CHECKPOINT M1: Technical + Visual**

**Technical:**
```bash
npm run typecheck  # Neue Components importierbar?
npm run lint       # Keine Fehler?
npm run dev        # Server startet?
```

**👁️ VISUAL CHECKPOINT (Jörg prüft):**

**Fragen an Jörg:**
1. ✅ **Browser öffnen:** Navigiere zu `/customers`
2. ✅ **CustomersPageV2 läuft noch?** Siehst du die Kundenliste?
3. ✅ **Filter funktionieren?** Kannst du nach Status filtern?
4. ✅ **Tabs funktionieren?** Kannst du zwischen "Kundenliste", "Datenqualität", "Daten-Aktualität" wechseln?
5. ✅ **Wizard öffnet?** Click auf "Neuer Kunde" → Wizard erscheint?

**Expected:** Alles funktioniert EXAKT wie vorher (M1 hat nichts geändert!)

**Screenshot bitte:** Falls etwas komisch aussieht!

**Rollback:** `git stash` (nichts ist geändert, nur neue Dateien!)

---

### **PHASE II: NEW CUSTOMERS PAGE (Tag 1 Nachmittag, 4h)**

#### **M2: CustomersPage (neu) implementieren** ⏱️ 4h

**Ziel:** Neue CustomersPage mit Shared Components

**Neue Dateien:**
- `/pages/CustomersPage.tsx` (~200 LOC)
- `/features/customers/config/customerConfig.ts` (Column & Filter Config)

**Features:**
- Tab 1: Kundenliste (DataTable + FilterBar)
- Tab 2: Datenqualität (DataHygieneDashboard)
- Tab 3: Daten-Aktualität (DataFreshnessManager)
- Wizard: CustomerOnboardingWizardModal
- Navigation: UUID-basiert (`/customers/${id}`)
- After Create: Navigate Detail

**✅ CHECKPOINT M2: Technical + Visual**

**Technical:**
```bash
npm run typecheck
npm run lint
npm run dev  # Server startet?
```

**👁️ VISUAL CHECKPOINT (Jörg prüft):**

**Temporary Route:** `/customers-new` (neue CustomersPage im Parallel-Betrieb!)

**Fragen an Jörg:**
1. ✅ **Navigiere zu `/customers-new`** (neue Page!)
2. ✅ **Kundenliste lädt?** Siehst du deine Kunden?
3. ✅ **Vergleich mit `/customers` (alt):** Sieht die Tabelle IDENTISCH aus?
4. ✅ **Filter funktionieren?** Test: Nach "AKTIV" Status filtern
5. ✅ **Text-Suche funktioniert?** Test: Firmenname eingeben
6. ✅ **Tabs funktionieren?** Wechsel zwischen "Kundenliste" / "Datenqualität" / "Daten-Aktualität"
7. ✅ **Wizard öffnet?** Click "Neuer Kunde" → CustomerOnboardingWizard erscheint?
8. ✅ **Row-Click Navigation?** Click auf einen Kunden → `/customers/:id` öffnet?
9. ✅ **Pagination?** Werden bei >20 Kunden die Pagination-Controls angezeigt?

**Side-by-Side Test:**
- **Links:** `/customers` (CustomersPageV2 - alt)
- **Rechts:** `/customers-new` (CustomersPage - neu)
- **Check:** IDENTISCHES Layout? IDENTISCHES Verhalten?

**Screenshot bitte:** Wenn Unterschiede sichtbar sind!

**Rollback:** Neue Datei löschen, CustomersPageV2 bleibt!

---

### **PHASE III: NEW LEADS PAGE (Tag 2 Vormittag, 3h)**

#### **M3: LeadsPage (neu) implementieren** ⏱️ 3h

**Ziel:** Neue LeadsPage mit Shared Components

**Neue Dateien:**
- `/pages/LeadsPage.tsx` (~180 LOC)
- `/features/leads/config/leadConfig.ts` (Column & Filter Config)

**Features:**
- Tab 1: Lead-Liste (DataTable + FilterBar)
- Tab 2: Lead-Qualität (LeadQualityDashboard)
- Tab 3: Schutzfristen (LeadProtectionManager)
- Wizard: LeadWizard
- Dialog: AddFirstContactDialog (VORMERKUNG)
- Dialog: DeleteLeadDialog
- Navigation: Slug-basiert (`generateLeadUrl()`)
- After Create: Stay on List

**✅ CHECKPOINT M3: Technical + Visual**

**Technical:**
```bash
npm run typecheck
npm run lint
npm run dev  # Server startet?
```

**👁️ VISUAL CHECKPOINT (Jörg prüft):**

**Temporary Route:** `/lead-generation/leads-new` (neue LeadsPage im Parallel-Betrieb!)

**Fragen an Jörg:**
1. ✅ **Navigiere zu `/lead-generation/leads-new`** (neue Page!)
2. ✅ **Lead-Liste lädt?** Siehst du deine Leads?
3. ✅ **Vergleich mit `/lead-generation/leads` (alt):** Sieht die Tabelle IDENTISCH aus?
4. ✅ **Filter funktionieren?** Test: Nach "VORMERKUNG" Lead-Stage filtern
5. ✅ **Lead-Spalten korrekt?** Siehst du "Lead-Score" Spalte (nicht "Risiko")?
6. ✅ **Tabs funktionieren?** Wechsel zwischen "Lead-Liste" / "Lead-Qualität" / "Schutzfristen"
7. ✅ **Wizard öffnet?** Click "Neuer Lead" → LeadWizard (NICHT CustomerOnboarding!) erscheint?
8. ✅ **Row-Click Navigation mit SLUG?** Click auf Lead → `/lead-generation/leads/baeckerei-mueller-123` (mit Slug!)?
9. ✅ **VORMERKUNG Dialog?** Edit-Click auf Lead mit Stage "VORMERKUNG" → AddFirstContactDialog öffnet?
10. ✅ **After Create:** Neuer Lead erstellen → Bleibst du auf der Liste? (NICHT Detail-Page wie bei Customers!)

**Side-by-Side Test:**
- **Links:** `/lead-generation/leads` (CustomersPageV2 wrapper - alt)
- **Rechts:** `/lead-generation/leads-new` (LeadsPage - neu)
- **Check:** IDENTISCHES Layout? IDENTISCHES Verhalten?

**Kritischer Check - Navigation:**
- ✅ **Lead-URL hat SLUG:** `/lead-generation/leads/firmenname-123` (nicht UUID!)
- ✅ **Customer-URL hat UUID:** `/customers/a1b2c3d4-...` (zum Vergleich)

**Screenshot bitte:** Wenn Unterschiede sichtbar sind!

**Rollback:** Neue Datei löschen, CustomersPageV2 bleibt!

---

### **PHASE IV: ROUTING SWITCH (Tag 2 Nachmittag, 1h)**

#### **M4: Routing aktualisieren** ⏱️ 1h

**Ziel:** Routes auf neue Pages umstellen

**Änderungen in `/providers.tsx`:**

```tsx
// BEFORE:
import { CustomersPageV2 } from './pages/CustomersPageV2';
import LeadsPageOld from './pages/LeadsPage'; // War Wrapper!

const routes = [
  { path: '/customers', element: <CustomersPageV2 context="customers" /> },
  { path: '/lead-generation/leads', element: <LeadsPageOld /> },
];

// AFTER:
import { CustomersPage } from './pages/CustomersPage'; // NEU!
import { LeadsPage } from './pages/LeadsPage'; // NEU!
import { CustomersPageV2 } from './pages/CustomersPageV2'; // DEPRECATED (für Rollback)

const routes = [
  { path: '/customers', element: <CustomersPage /> }, // ← NEW!
  { path: '/lead-generation/leads', element: <LeadsPage /> }, // ← NEW!

  // Fallback (deprecated, für Rollback):
  { path: '/customers-old', element: <CustomersPageV2 context="customers" /> },
  { path: '/leads-old', element: <CustomersPageV2 context="leads" /> },
];
```

**✅ CHECKPOINT M4: Technical + Visual (KRITISCHER PUNKT!)**

**Technical:**
```bash
npm run typecheck
npm run lint
npm run dev  # Server startet?
```

**🚨 KRITISCHER VISUAL CHECKPOINT (Jörg prüft SEHR GENAU!):**

**Was passiert:** Routes werden LIVE umgeschaltet! `/customers` zeigt jetzt die NEUE Page!

**⚠️ WICHTIG: Rollback-Route vorbereitet:**
- Alt: `/customers-old` (CustomersPageV2)
- Alt: `/lead-generation/leads-old` (CustomersPageV2 wrapper)

**Fragen an Jörg (in dieser Reihenfolge!):**

**1. Customers Check:**
1. ✅ **Navigiere zu `/customers`** (sollte NEUE CustomersPage zeigen!)
2. ✅ **Kundenliste lädt?** Alle Kunden sichtbar?
3. ✅ **Filter funktionieren?** Test mehrere Filter
4. ✅ **Tabs funktionieren?** Alle 3 Tabs durchklicken
5. ✅ **Wizard funktioniert?** Neuer Kunde erstellen
6. ✅ **Navigation funktioniert?** Click auf Kunde → Detail-Page öffnet?
7. ✅ **URL korrekt?** Detail-URL ist `/customers/UUID` (nicht Slug!)

**2. Leads Check:**
1. ✅ **Navigiere zu `/lead-generation/leads`** (sollte NEUE LeadsPage zeigen!)
2. ✅ **Lead-Liste lädt?** Alle Leads sichtbar?
3. ✅ **Filter funktionieren?** Test Lead-Stage Filter
4. ✅ **Lead-Score Spalte?** Zeigt "Lead-Score" (nicht "Risiko")?
5. ✅ **Wizard funktioniert?** Neuer Lead erstellen
6. ✅ **Navigation funktioniert?** Click auf Lead → Detail-Page öffnet?
7. ✅ **URL korrekt mit SLUG?** Detail-URL ist `/lead-generation/leads/firmenname-123` (mit Slug!)
8. ✅ **After Create?** Neuer Lead bleibt auf Liste (nicht Detail-Page!)

**3. Rollback Check (WICHTIG!):**
1. ✅ **Navigiere zu `/customers-old`** → CustomersPageV2 funktioniert noch?
2. ✅ **Navigiere zu `/lead-generation/leads-old`** → CustomersPageV2 wrapper funktioniert noch?

**🚨 STOP-Kriterium:**
Falls IRGENDETWAS nicht funktioniert:
1. **SOFORT STOPP!**
2. **Screenshot machen!**
3. **Rollback auf `-old` Routes** (Feature Flag auf `false`)
4. **Debug-Analyse**

**Screenshot bitte:** Von BEIDEN Pages (/customers UND /lead-generation/leads)!

**Rollback-Strategie:**
```tsx
// Feature Flag in providers.tsx:
const USE_NEW_PAGES = false; // ← Bei Fehler auf false setzen!

const routes = [
  {
    path: '/customers',
    element: USE_NEW_PAGES ? <CustomersPage /> : <CustomersPageV2 context="customers" />
  },
  {
    path: '/lead-generation/leads',
    element: USE_NEW_PAGES ? <LeadsPage /> : <LeadsPageWrapper />
  },
];
```

---

### **PHASE V: TESTING & CLEANUP (Tag 2 Abend, 2h)**

#### **M5: Tests schreiben** ⏱️ 2h

**Neue Test-Dateien:**
- `/pages/__tests__/CustomersPage.test.tsx`
- `/pages/__tests__/LeadsPage.test.tsx`
- `/features/shared/components/data-table/__tests__/DataTable.test.tsx`
- `/features/shared/components/filter/__tests__/FilterBar.test.tsx`

**Test-Coverage Ziele:**
- CustomersPage: >80%
- LeadsPage: >80%
- DataTable: >90%
- FilterBar: >80%

**✅ CHECKPOINT M5:**
```bash
npm run test -- --coverage

# CI Check:
npm run ci
```

---

#### **M6: CustomersPageV2 deprecaten + löschen** ⏱️ 30min

**Schritt 1: Deprecation Warning (optional)**

```tsx
/**
 * @deprecated Use CustomersPage or LeadsPage instead
 * This component will be removed in Sprint 2.1.7.8
 */
export function CustomersPageV2(props: CustomersPageV2Props) {
  console.warn('CustomersPageV2 is deprecated.');
  // ... existing code
}
```

**Schritt 2: Löschen (nach erfolgreichen Tests!)**

```bash
# Backup erstellen
git add .
git commit -m "chore: Backup before deleting CustomersPageV2"

# Löschen
rm frontend/src/pages/CustomersPageV2.tsx
rm frontend/src/pages/LeadsPage.tsx  # Alter Wrapper!

# Tests laufen noch?
npm run test
npm run dev
```

**✅ FINAL CHECKPOINT M6: Technical + Visual (ABSCHLUSS-PRÜFUNG!)**

**Technical:**
```bash
# CI grün?
npm run ci

# Bundle Size check:
npm run build
# → Bundle sollte KLEINER sein (690 LOC weg!)
```

**🎉 FINAL VISUAL CHECKPOINT (Jörg macht komplette QA!):**

**Vollständiger Workflow-Test (End-to-End):**

**1. Customers Workflow:**
1. ✅ **Navigiere zu `/customers`**
2. ✅ **Kundenliste lädt?** Alle Kunden sichtbar?
3. ✅ **Filter Test:** Nach "AKTIV" filtern → funktioniert?
4. ✅ **Text-Suche Test:** Firmenname eingeben → findet Kunde?
5. ✅ **Sortierung Test:** Nach "Umsatz" sortieren → funktioniert?
6. ✅ **Neuen Kunden anlegen:**
   - Click "Neuer Kunde"
   - Wizard durchlaufen (alle Steps)
   - Kunde speichern
   - **Navigate zu Detail-Page?** (automatisch nach Create!)
7. ✅ **Detail-Page Test:**
   - Customer Detail lädt?
   - Tabs funktionieren?
   - Kontakte sichtbar?
   - Opportunities sichtbar?
8. ✅ **Zurück zur Liste:** Navigate `/customers` → neuer Kunde ist in der Liste?

**2. Leads Workflow:**
1. ✅ **Navigiere zu `/lead-generation/leads`**
2. ✅ **Lead-Liste lädt?** Alle Leads sichtbar?
3. ✅ **Filter Test:** Nach "VORMERKUNG" Stage filtern → funktioniert?
4. ✅ **Lead-Score Spalte sichtbar?** (nicht "Risiko"!)
5. ✅ **Neuen Lead anlegen:**
   - Click "Neuer Lead"
   - LeadWizard (NICHT CustomerOnboarding!) durchlaufen
   - Lead speichern
   - **Bleibst auf der Liste?** (NICHT Detail-Page wie Customers!)
6. ✅ **Row-Click Navigation:**
   - Click auf Lead → Detail-Page öffnet?
   - URL hat SLUG? `/lead-generation/leads/firmenname-123`
7. ✅ **VORMERKUNG Dialog Test:**
   - Edit-Click auf Lead mit Stage "VORMERKUNG"
   - AddFirstContactDialog öffnet? (Lead-spezifisch!)
8. ✅ **Lead Quality Tab:** Tab "Lead-Qualität" funktioniert?

**3. Navigation Unterschiede Check (KRITISCH!):**
1. ✅ **Customer Detail URL:** `/customers/UUID` (z.B. `/customers/a1b2c3d4-...`)
2. ✅ **Lead Detail URL:** `/lead-generation/leads/SLUG` (z.B. `/lead-generation/leads/baeckerei-mueller-123`)
3. ✅ **Unterschied bestätigt?** Customer = UUID, Lead = Slug!

**4. Performance Check:**
1. ✅ **Kundenliste mit >20 Einträgen:** Virtualisierung funktioniert?
2. ✅ **Pagination bei >50 Einträgen:** "Weitere Kunden laden" Button erscheint?
3. ✅ **Filter-Performance:** Filter anwenden → lädt schnell?

**5. Rollback-Routes entfernt?**
1. ✅ **`/customers-old` existiert NICHT mehr?** (404 Error erwartet!)
2. ✅ **`/lead-generation/leads-old` existiert NICHT mehr?** (404 Error erwartet!)
3. ✅ **CustomersPageV2.tsx gelöscht?** (Datei existiert nicht mehr!)

**6. Bundle Size Check:**
```bash
npm run build
# Vorher: ~XXX KB
# Nachher: ~YYY KB (erwartet: -100 KB / -15%)
```

**Screenshot bitte:**
- `/customers` (vollständige Page)
- `/lead-generation/leads` (vollständige Page)
- Bundle Size Output (Terminal)

**🎉 SUCCESS-Kriterium:**
Wenn ALLE Checks ✅ sind → **Migration erfolgreich!** 🚀

**Falls IRGENDEIN Check ❌:**
1. **STOPP!**
2. **Screenshot machen**
3. **Issue dokumentieren**
4. **Rollback erwägen** (`git revert`)

---

## 📊 ERFOLGSMETRIKEN

**Nach Migration (M6 complete):**

| Metrik | Vorher | Nachher | Verbesserung |
|--------|--------|---------|--------------|
| **LOC** | 690 Zeilen | ~380 Zeilen (200+180) | -45% |
| **Context-Branches** | 26 | 0 | -100% |
| **Shared Logic** | ~40% | 100% (in /shared/) | +150% |
| **Test-Coverage** | Mixed | >80% pro Page | +40% |
| **Bundle Size** | Baseline | -100 KB erwartet | -15% |
| **Maintenance** | Schwer | Einfach | ✅ |

---

## 🚨 RISIKO-MANAGEMENT

### **Rollback-Strategie (pro Phase):**

| Phase | Rollback | Zeitverlust |
|-------|----------|-------------|
| **M1** | `git stash` | 0 min |
| **M2** | Neue Datei löschen | 5 min |
| **M3** | Neue Datei löschen | 5 min |
| **M4** | Routes auf -old umschalten | 2 min |
| **M5** | Tests skippen (temporär) | 0 min |
| **M6** | `git revert` | 10 min |

**Critical Path:** M4 (Routing Switch)
- **Sicherung:** Feature Flag `USE_NEW_PAGES` (sofortiges Zurückschalten!)
- **Testing:** Manueller QA-Durchlauf PFLICHT vor M6!

---

## 🎯 INTEGRATION MIT SPRINT 2.1.7.7

**Optimierte Sprint-Reihenfolge:**

**Tag 1 (8h):**
- 08:00-12:00: **M1-M2** (Shared Infra + CustomersPage)
- 13:00-17:00: **M3-M4** (LeadsPage + Routing)

**Tag 2 (8h):**
- 08:00-10:00: **M5** (Tests)
- 10:00-12:00: **M6** (Cleanup) + **D9** (UX Polish)
- 13:00-17:00: **D0-D1** (FILIALE aktivieren + CreateBranchDialog)

**Tag 3 (8h):**
- 08:00-12:00: **D2-D3** (XentralAddressMatcher + HierarchyMetricsService)
- 13:00-17:00: **D4-D6** (HierarchyDashboard + TreeView + Integration)

**Tag 4 (4h):**
- 08:00-12:00: **E2E Tests** + **MP5 Update**

**Total: 28h = 3,5 Tage**

---

## 📋 FEHLENDE DETAILS (TODO während Implementation)

Diese Details werden während der Implementation geklärt:

- [ ] **Test-Coverage analysieren** (CustomersPageV2.test.tsx) - wird in M5 sowieso geschrieben
- [ ] **generateLeadUrl() Location finden** - wird beim Implementieren der LeadsPage sowieso importiert
- [ ] **Types dokumentieren** (Customer vs Lead) - werden beim Implementieren sowieso sichtbar

**Begründung:** Diese Details sind für M1 nicht blockierend und werden während der Implementation automatisch geklärt.

---

## ✅ GO/NO-GO CHECKLIST

**Vor M1 Start:**
- ✅ Recherche vollständig
- ✅ Kritische Unterschiede dokumentiert (Navigation, VORMERKUNG, Pagination)
- ✅ Rollback-Strategie definiert
- ✅ Checkpoints definiert
- ✅ TRIGGER aktualisiert

**Vor M4 (Routing Switch):**
- ✅ M1-M3 erfolgreich (alle Checkpoints grün)
- ✅ Manuelle Tests erfolgreich
- ✅ Feature Flag vorbereitet

**Vor M6 (Löschen):**
- ✅ M4 erfolgreich (Routes funktionieren)
- ✅ M5 erfolgreich (Tests >80%)
- ✅ Manuelle QA durchgeführt
- ✅ CI grün

---

## 🔍 STATUS TRACKING FÜR NEUE CLAUDE-INSTANZ

**Wenn du eine neue Claude-Instanz bist und nicht weißt, wo wir stehen:**

### **1. Aktuellen Status finden:**

```bash
# Prüfe TRIGGER-Dokument:
cat docs/planung/TRIGGER_SPRINT_2_1_7_7.md | grep "Status:"

# Prüfe TODO-Liste:
cat docs/planung/artefakte/SPRINT_2_1_7_7/MIGRATION_PLAN.md | grep "Status:"

# Prüfe letzte Git-Commits:
git log --oneline --all | grep -i "migration\|m1\|m2\|m3\|m4\|m5\|m6" | head -10
```

### **2. Finde die aktuelle Phase:**

**Prüfe welche Dateien existieren:**

```bash
# M1 abgeschlossen? → Shared Components existieren:
ls -la frontend/src/features/shared/components/

# M2 abgeschlossen? → CustomersPage existiert:
ls -la frontend/src/pages/CustomersPage.tsx

# M3 abgeschlossen? → LeadsPage existiert:
ls -la frontend/src/pages/LeadsPage.tsx

# M4 abgeschlossen? → Routing umgestellt:
grep -n "CustomersPage" frontend/src/providers.tsx

# M6 abgeschlossen? → CustomersPageV2 gelöscht:
ls -la frontend/src/pages/CustomersPageV2.tsx  # → "No such file" = M6 done!
```

### **3. Aktualisiere die Status-Tabelle:**

Wenn Phase X abgeschlossen, update die Tabelle in [Migrations-Phasen](#-migrations-phasen-m1-m6):

```markdown
| **M1** | 4h | Shared Infrastructure extrahieren | ✅ COMPLETE | `git stash` |
```

### **4. Frage Jörg nach Visual Confirmation:**

```
"Hey Jörg, ich bin eine neue Claude-Instanz und setze die Migration fort.

Ich sehe, dass:
- M1: [✅ / ⏳ / ❌]
- M2: [✅ / ⏳ / ❌]
- M3: [✅ / ⏳ / ❌]

Bitte bestätige:
1. Welche Phase ist abgeschlossen?
2. Wo sind wir stehengeblieben?
3. Gab es Probleme?

Dann starte ich an der richtigen Stelle weiter!"
```

---

## 📝 ÄNDERUNGSPROTOKOLL

| Datum | Phase | Änderung | Von |
|-------|-------|----------|-----|
| 2025-11-01 | Recherche | Initial Research complete | Claude Code |
| 2025-11-01 | M1 | Status: PENDING → IN_PROGRESS | Claude Code |
| ... | ... | ... | ... |

**Nächste Claude-Instanz:** Füge hier neue Einträge hinzu, wenn du eine Phase abschließt!

---

**🚀 Ready for M1: Shared Infrastructure extrahieren**

---

## 📌 WICHTIGE HINWEISE FÜR NEUE CLAUDE-INSTANZ

### **Kritische Dateien - NICHT ANFASSEN bis Phase:**

| Datei | Anfassen ab Phase | Warum? |
|-------|-------------------|--------|
| `CustomersPageV2.tsx` | M6 | Wird erst ganz am Ende gelöscht! |
| `providers.tsx` | M4 | Routing Switch erst nach M3! |
| Bestehende Tests | M5 | Tests erst nach neuen Pages! |

### **Kritische Befehle - NUR mit Jörg-Freigabe:**

```bash
# ❌ NIEMALS ohne Fragen:
git push  # → Nur nach expliziter Freigabe!
npm run build --production  # → Nur nach M6!
rm frontend/src/pages/CustomersPageV2.tsx  # → Nur in M6!

# ✅ IMMER sicher:
git status
npm run dev
npm run typecheck
```

### **Bei Unsicherheit:**

1. **STOPP!**
2. **Frage Jörg:** "Ich bin unsicher bei [X]. Soll ich [Y] machen?"
3. **Warte auf Antwort**
4. **Dokumentiere Entscheidung** im Änderungsprotokoll

**Motto:** Lieber 1x zu viel fragen als 1x zu wenig!
