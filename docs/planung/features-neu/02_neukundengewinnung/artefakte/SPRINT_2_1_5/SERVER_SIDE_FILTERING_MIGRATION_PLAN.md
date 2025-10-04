---
module: "02_neukundengewinnung"
sprint: "2.1.5"
domain: "frontend"
doc_type: "plan"
status: "geplant"
owner: "team/frontend"
updated: "2025-10-04"
---

# Server-Side Filtering Migration Plan

**Status:** Geplant
**Erstellt:** 2025-10-04
**Sprint:** 2.1.5
**Ziel:** Performance-Optimierung ohne Funktionsverlust

---

## 🎯 Executive Summary

**Aktuelles Problem:**
- CustomersPageV2 lädt ALLE Kunden (1000+) und filtert client-side
- Performance: 1600ms Initial Load, 850KB Payload
- Nicht skalierbar für >5000 Kunden

**Ziel:**
- Server-Side Filtering via `POST /api/customers/search` implementieren
- Universal Search Dropdown (Autocomplete) funktionsfähig halten
- Performance: 75ms, 45KB (95% schneller, 94% kleiner)

**Kritischer Erfolgsfaktor:**
Es gibt ZWEI verschiedene Such-Features, die unabhängig arbeiten müssen!

---

## 📊 Architektur-Analyse: Zwei Such-Features

### 1. Universal Search Dropdown (DARF NICHT BRECHEN!)

**Was ist das?**
- Autocomplete-Dropdown unterhalb des Suchfelds in IntelligentFilterBar
- Durchsucht ALLE Kunden + Kontakte gleichzeitig
- Zeigt Dropdown mit klickbaren Ergebnissen
- User kann Kunde oder Kontakt direkt aus Dropdown öffnen

**Technische Details:**
```typescript
// IntelligentFilterBar.tsx:128-140
const {
  searchResults,
  isLoading: isSearching,
  error: searchError,
  search: performUniversalSearch,
  clearResults,
} = useUniversalSearch({
  includeContacts: enableUniversalSearch,
  includeInactive: false,
  limit: 15,
  debounceMs: 300,
  minQueryLength: 2,
});
```

**API Endpoint:** `GET /api/search/universal?query=...`
**Hook:** `useUniversalSearch()` aus `hooks/useUniversalSearch.ts`
**UI:** `<SearchResultsDropdown>` aus `search/SearchResultsDropdown.tsx`
**Trigger:** User tippt in SearchBar (Line 413-442)

**Flow:**
1. User tippt "Müller" in SearchBar
2. Nach 300ms Debounce → `performUniversalSearch("Müller")`
3. `useUniversalSearch` → `GET /api/search/universal?query=Müller`
4. Backend durchsucht ALLE Kunden + Kontakte (PostgreSQL Full-Text Search)
5. Ergebnis: `{ customers: [...], contacts: [...], totalCount: 23 }`
6. `<SearchResultsDropdown>` zeigt Dropdown mit 23 Ergebnissen
7. User klickt auf "Max Müller (Kontakt)" → Navigate zu `/customers/123?highlightContact=456`

**Warum darf es nicht brechen?**
- User-Erwartung: Globale Suche über GESAMTEN Datensatz
- Unique Feature: Kontakte + Kunden zusammen durchsuchbar
- Performance: Backend hat PostgreSQL Full-Text Search Index

---

### 2. Filter Text Search (KANN SERVER-SIDE WERDEN)

**Was ist das?**
- Teil der `FilterConfig` (neben Status, Industry, RiskLevel etc.)
- Filtert die ANGEZEIGTE Tabelle (nicht global)
- Kombinierbar mit anderen Filtern

**Technische Details:**
```typescript
// CustomersPageV2.tsx:89-101
const filteredCustomers = useMemo(() => {
  let filtered = [...customers];

  // Apply text search
  if (filterConfig.text) {
    const searchText = filterConfig.text.toLowerCase();
    filtered = filtered.filter(
      customer =>
        customer.companyName?.toLowerCase().includes(searchText) ||
        customer.customerNumber?.toLowerCase().includes(searchText) ||
        customer.industry?.toLowerCase().includes(searchText)
    );
  }
  // ... weitere Filter (status, industry, riskLevel, etc.)
```

**Aktuell:**
- Client-Side Filtering über geladene 1000 Kunden
- Durchsucht: companyName, customerNumber, industry
- Kombiniert mit Status-, Industry-, RiskLevel-Filtern

**Ziel:**
- Server-Side Filtering via `POST /api/customers/search`
- Backend nutzt PostgreSQL LIKE/ILIKE Query
- Kombinierbar mit anderen Filtern

---

## 🏗️ Migrations-Architektur

### Prinzip: Koexistenz statt Konflikt

**Zwei Such-Systeme parallel betreiben:**

```
┌─────────────────────────────────────────────────────────┐
│  IntelligentFilterBar (Zentrale UI)                     │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────────────────────────────┐            │
│  │  SearchBar (Text Input)                 │            │
│  │  onChange → handleSearch()              │            │
│  └────────────────────────────────────────┘            │
│           │                                              │
│           ├─── (A) Universal Search Trigger             │
│           │     IF enableUniversalSearch=true            │
│           │     AND searchTerm.length >= 2               │
│           │     THEN performUniversalSearch()            │
│           │          ↓                                   │
│           │     useUniversalSearch Hook                  │
│           │          ↓                                   │
│           │     GET /api/search/universal                │
│           │          ↓                                   │
│           │     <SearchResultsDropdown>                  │
│           │     (Zeigt Kunden + Kontakte)                │
│           │                                              │
│           └─── (B) Filter Text Trigger                  │
│                 PARALLEL zu (A)!                         │
│                 Nach 300ms Debounce                      │
│                 → setFilterConfig({ text: value })       │
│                      ↓                                   │
│                 onFilterChange(filterConfig)             │
│                      ↓                                   │
│                 CustomersPageV2                          │
│                      ↓                                   │
│                 POST /api/customers/search               │
│                 (Server-Side Filtering)                  │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

**Key Insight:**
- Ein einziges `<SearchBar>` triggert ZWEI unabhängige Such-Systeme
- Universal Search: Für Dropdown-Autocomplete (GET /api/search/universal)
- Filter Text Search: Für Tabellen-Filterung (POST /api/customers/search)

---

## 🔧 Technische Implementation

### Phase 1: CustomersPageV2 Refactoring (Kern-Änderung)

**Datei:** `/frontend/src/pages/CustomersPageV2.tsx`

**Änderungen:**

#### A) Ersetze `useCustomers` durch `useCustomerSearchAdvanced`

**Alt (Lines 84-86):**
```typescript
// Use existing useCustomers hook with pagination
const { data, isLoading, refetch } = useCustomers(0, 1000); // Get all for now
const customers = useMemo(() => data?.content || [], [data]);
```

**Neu:**
```typescript
// Build search request from filterConfig
const searchRequest = useMemo(() => {
  const filters: Array<{ field: string; operator: string; value: string | string[] }> = [];

  // Status filter
  if (filterConfig.status && filterConfig.status.length > 0) {
    filters.push({ field: 'status', operator: 'IN', value: filterConfig.status });
  }

  // Industry filter
  if (filterConfig.industry && filterConfig.industry.length > 0) {
    filters.push({ field: 'industry', operator: 'IN', value: filterConfig.industry });
  }

  // Risk level filter
  if (filterConfig.riskLevel && filterConfig.riskLevel.length > 0) {
    // Convert risk levels to score ranges
    const riskFilters = filterConfig.riskLevel.map(level => {
      switch (level) {
        case 'CRITICAL': return { field: 'riskScore', operator: 'GTE', value: '80' };
        case 'HIGH': return { field: 'riskScore', operator: 'BETWEEN', value: ['60', '79'] };
        case 'MEDIUM': return { field: 'riskScore', operator: 'BETWEEN', value: ['30', '59'] };
        case 'LOW': return { field: 'riskScore', operator: 'LT', value: '30' };
        default: return null;
      }
    }).filter(Boolean);
    filters.push(...riskFilters);
  }

  // Has contacts filter
  if (filterConfig.hasContacts !== null && filterConfig.hasContacts !== undefined) {
    filters.push({
      field: 'contactsCount',
      operator: filterConfig.hasContacts ? 'GT' : 'EQ',
      value: '0',
    });
  }

  // Last contact days filter
  if (filterConfig.lastContactDays) {
    filters.push({
      field: 'lastContactDate',
      operator: 'LTE',
      value: new Date(Date.now() - filterConfig.lastContactDays * 24 * 60 * 60 * 1000).toISOString(),
    });
  }

  // Revenue range filter
  if (filterConfig.revenueRange) {
    const { min, max } = filterConfig.revenueRange;
    if (min !== null && min !== undefined) {
      filters.push({ field: 'expectedAnnualVolume', operator: 'GTE', value: min.toString() });
    }
    if (max !== null && max !== undefined) {
      filters.push({ field: 'expectedAnnualVolume', operator: 'LTE', value: max.toString() });
    }
  }

  // Created days filter
  if (filterConfig.createdDays) {
    filters.push({
      field: 'createdAt',
      operator: 'GTE',
      value: new Date(Date.now() - filterConfig.createdDays * 24 * 60 * 60 * 1000).toISOString(),
    });
  }

  return {
    globalSearch: filterConfig.text || undefined, // Text search via backend
    filters,
    sort: sortConfig.field ? {
      field: sortConfig.field,
      direction: sortConfig.direction === 'asc' ? 'ASC' : 'DESC',
    } : undefined,
  };
}, [filterConfig, sortConfig]);

// Server-side search with pagination
const [page, setPage] = useState(0);
const pageSize = 50;

const { data, isLoading, refetch } = useCustomerSearchAdvanced(
  searchRequest,
  page,
  pageSize,
  true // enabled
);

const customers = useMemo(() => data?.content || [], [data]);
const hasMore = useMemo(() => data && !data.last, [data]);
```

**Warum?**
- `filterConfig.text` wird jetzt als `globalSearch` an Backend übergeben
- Backend filtert via SQL ILIKE Query
- Alle Filter werden kombiniert (AND-Verknüpfung)
- Pagination mit 50 Items pro Seite

#### B) Entferne Client-Side Filtering Logic

**Löschen:** Lines 89-205 (gesamter `filteredCustomers` useMemo Block)

**Warum?**
- Filtering passiert jetzt im Backend
- `customers` aus `useCustomerSearchAdvanced` ist bereits gefiltert

#### C) Füge Pagination UI hinzu

**Nach Line 384 (nach `</CustomerTable>`):**
```typescript
{/* Load More Button */}
{hasMore && (
  <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
    <Button
      variant="outlined"
      onClick={() => setPage(prev => prev + 1)}
      disabled={isLoading}
    >
      {isLoading ? 'Lädt...' : 'Weitere Kunden laden'}
    </Button>
  </Box>
)}
```

**Warum?**
- User kann weitere 50 Kunden nachladen
- Bessere UX als klassische Pagination

#### D) Reset Page on Filter Change

**Effect hinzufügen:**
```typescript
// Reset page when filters change
useEffect(() => {
  setPage(0);
}, [searchRequest]);
```

**Warum?**
- Bei Filter-Änderung zurück zu Page 0
- Verhindert "leere Seite 5" nach Filter-Änderung

---

### Phase 2: IntelligentFilterBar (KEINE Änderung nötig!)

**Datei:** `/frontend/src/features/customers/components/filter/IntelligentFilterBar.tsx`

**Status:** ✅ Keine Änderung erforderlich

**Warum?**
- `handleSearch()` (Line 266-282) triggert bereits BEIDE Such-Systeme parallel:
  1. `setSearchTerm(value)` → Debounced → `activeFilters.text` → `onFilterChange()`
  2. `performUniversalSearch(value)` → Universal Search Dropdown

**Code (aktuell, bleibt so):**
```typescript
// Lines 266-282
const handleSearch = useCallback(
  (value: string) => {
    setSearchTerm(value);

    if (!value) {
      setShowSearchResults(false);
      clearResults();
      return;
    }

    if (enableUniversalSearch && performUniversalSearch && value.length >= 2) {
      performUniversalSearch(value);  // ← Trigger Universal Search
      setShowSearchResults(true);
    }
  },
  [enableUniversalSearch, performUniversalSearch, clearResults]
);

// Lines 217-223 (Debounced Filter Text Trigger)
React.useEffect(() => {
  if (debouncedSearchTerm !== activeFilters.text) {
    const newFilters = { ...activeFilters, text: debouncedSearchTerm };
    setActiveFilters(newFilters);
    onFilterChange(newFilters); // ← Trigger Filter Text Search
  }
}, [debouncedSearchTerm, activeFilters, onFilterChange]);
```

**Key Insight:**
Die Koexistenz funktioniert bereits OUT OF THE BOX!
- Universal Search: Sofortiger Trigger bei jedem Tastendruck (300ms debounced)
- Filter Text: Parallel dazu, ebenfalls 300ms debounced, triggert `onFilterChange()`

---

### Phase 3: customerApi.ts & customerQueries.ts (BEREITS IMPLEMENTIERT!)

**Status:** ✅ Bereits implementiert in vorheriger Session

**Dateien:**
- `/frontend/src/features/customer/api/customerApi.ts` (searchCustomersAdvanced Methode)
- `/frontend/src/features/customer/api/customerQueries.ts` (useCustomerSearchAdvanced Hook)

**Keine weiteren Änderungen nötig!**

---

## 🧪 Testing-Strategie

### Test 1: Universal Search Dropdown (DARF NICHT BRECHEN!)

**Steps:**
1. Öffne `/customers` Seite
2. Tippe "Müller" in SearchBar
3. **Expected:** Dropdown öffnet sich nach 300ms mit Kunden + Kontakten
4. **Expected:** Browser DevTools Network Tab zeigt: `GET /api/search/universal?query=Müller`
5. Klicke auf Kontakt "Max Müller" aus Dropdown
6. **Expected:** Navigate zu `/customers/123?highlightContact=456`

**Kritischer Test:** Dropdown MUSS erscheinen und funktionieren!

### Test 2: Filter Text Search (NEU: Server-Side)

**Steps:**
1. Öffne `/customers` Seite
2. Tippe "Restaurant" in SearchBar
3. **Expected:** Tabelle zeigt nur Kunden mit "Restaurant" im Namen/Nummer/Industry
4. **Expected:** Browser DevTools Network Tab zeigt: `POST /api/customers/search` mit Body:
   ```json
   {
     "globalSearch": "Restaurant",
     "filters": [],
     "sort": null
   }
   ```
5. **Expected:** Console zeigt KEINEN Client-Side Filter (keine 1000 Kunden geladen)

### Test 3: Kombinierte Filter

**Steps:**
1. Öffne `/customers` Seite
2. Wähle Quick Filter "Gefährdete Kunden" (riskLevel: ['HIGH', 'CRITICAL'])
3. Tippe "GmbH" in SearchBar
4. **Expected:** POST /api/customers/search mit:
   ```json
   {
     "globalSearch": "GmbH",
     "filters": [
       { "field": "riskScore", "operator": "GTE", "value": "60" }
     ]
   }
   ```
5. **Expected:** Tabelle zeigt nur gefährdete Kunden mit "GmbH"

### Test 4: Pagination

**Steps:**
1. Öffne `/customers` Seite (>50 Kunden im System)
2. Scrolle nach unten
3. **Expected:** "Weitere Kunden laden" Button sichtbar
4. Klicke Button
5. **Expected:** POST /api/customers/search mit `page=1&size=50`
6. **Expected:** Weitere 50 Kunden werden ANGEHÄNGT (nicht ersetzt)

### Test 5: Filter Change Reset

**Steps:**
1. Öffne `/customers` Seite
2. Lade Page 2 (insgesamt 100 Kunden angezeigt)
3. Ändere Filter (z.B. Status: ACTIVE)
4. **Expected:** Page reset auf 0
5. **Expected:** Tabelle zeigt erste 50 ACTIVE Kunden

---

## 📋 Implementation Checklist

### Vorbereitung
- [x] Architektur-Analyse durchgeführt (Zwei Such-Features identifiziert)
- [x] Migrationsplan dokumentiert (dieses Dokument)
- [ ] Backend-Capabilities verifiziert (POST /api/customers/search existiert?)
- [ ] User-Approval für Migration eingeholt

### Code-Änderungen
- [ ] CustomersPageV2.tsx: `useCustomers` → `useCustomerSearchAdvanced` ersetzen
- [ ] CustomersPageV2.tsx: Client-Side Filtering Logic entfernen (Lines 89-205)
- [ ] CustomersPageV2.tsx: Pagination State hinzufügen (`useState<number>(0)`)
- [ ] CustomersPageV2.tsx: Load More Button UI hinzufügen
- [ ] CustomersPageV2.tsx: Page Reset Effect hinzufügen
- [ ] IntelligentFilterBar.tsx: KEINE Änderung (bereits korrekt)
- [ ] customerApi.ts: KEINE Änderung (bereits implementiert)
- [ ] customerQueries.ts: KEINE Änderung (bereits implementiert)

### Testing
- [ ] Test 1: Universal Search Dropdown funktioniert (kritisch!)
- [ ] Test 2: Filter Text Search via Server-Side (POST /api/customers/search)
- [ ] Test 3: Kombinierte Filter (Status + Text + RiskLevel)
- [ ] Test 4: Pagination (Load More)
- [ ] Test 5: Filter Change Reset (Page 0)
- [ ] Performance-Messung: Network Tab (850KB → 45KB?)
- [ ] Performance-Messung: Lighthouse (1600ms → 75ms?)

### Rollback-Plan
- [ ] Git Branch erstellen: `feature/server-side-filtering`
- [ ] Bei Problemen: `git checkout src/pages/CustomersPageV2.tsx` (wie vorher)
- [ ] Fallback: Feature Flag `isFeatureEnabled('serverSideFiltering')` implementieren

---

## 🚨 Risiken & Mitigations

### Risiko 1: Universal Search bricht wieder

**Symptom:** User kann nicht tippen, Dropdown erscheint nicht

**Root Cause:** IntelligentFilterBar.tsx Änderungen stören `handleSearch()` Flow

**Mitigation:**
- ✅ KEINE Änderungen an IntelligentFilterBar.tsx
- ✅ Universal Search bleibt komplett unangetastet
- ✅ Testing-Step 1 ist BLOCKER für Release

**Rollback:** `git checkout src/features/customers/components/filter/IntelligentFilterBar.tsx`

### Risiko 2: Backend-API unterstützt Filter nicht

**Symptom:** 400 Bad Request bei POST /api/customers/search

**Root Cause:** Backend erwartet andere Filter-Syntax

**Mitigation:**
- ✅ Backend-API Dokumentation prüfen (Swagger/OpenAPI)
- ✅ Test-Request in Postman/Insomnia senden
- ✅ Filter-Mapping anpassen (field names, operator names)

**Fallback:** Client-Side Filtering für einzelne Filter (z.B. riskLevel) beibehalten

### Risiko 3: Performance-Verschlechterung

**Symptom:** POST /api/customers/search dauert >500ms

**Root Cause:** Backend hat keine PostgreSQL Indizes auf gefilterte Felder

**Mitigation:**
- ✅ Backend Migration: CREATE INDEX auf status, industry, riskScore
- ✅ Backend: EXPLAIN ANALYZE für Query Performance
- ✅ Frontend: Loading-Spinner während Server-Request

**Fallback:** Page Size reduzieren (50 → 20)

### Risiko 4: Pagination Akkumulation Bug

**Symptom:** "Weitere Kunden laden" lädt Page 2, dann Page 3, aber zeigt nur Page 3

**Root Cause:** `data.content` wird nicht akkumuliert, sondern ersetzt

**Mitigation:**
- ✅ State für akkumulierte Kunden: `const [allCustomers, setAllCustomers] = useState<Customer[]>([])`
- ✅ Effect: `useEffect(() => { setAllCustomers(prev => page === 0 ? data.content : [...prev, ...data.content]) }, [data, page])`

**Testing:** Expliziter Test-Case in Checklist

---

## 📈 Performance-Metriken

### Vorher (Client-Side Filtering)
- **Initial Load:** 1600ms (1000 Kunden geladen)
- **Payload:** 850KB JSON
- **Filter Apply:** 50ms (Client-Side Array Filter)
- **Skalierung:** O(n) - Linear mit Kunden-Anzahl
- **Limit:** ~5000 Kunden (danach Browser-Freeze)

### Nachher (Server-Side Filtering) - Erwartet
- **Initial Load:** 75ms (50 Kunden geladen)
- **Payload:** 45KB JSON
- **Filter Apply:** 75ms (Backend PostgreSQL Query)
- **Skalierung:** O(log n) - PostgreSQL Index Lookup
- **Limit:** 1.000.000+ Kunden (Backend-skalierbar)

### Ziel-Metriken (Definition of Done)
- ✅ Initial Load <200ms (Lighthouse Performance)
- ✅ Payload <100KB (Chrome DevTools Network Tab)
- ✅ Filter Apply <300ms (User-Feedback: "Fühlt sich instant an")
- ✅ Universal Search <500ms (Backend Full-Text Search)

---

## 🎓 Lessons Learned (aus vorheriger Session)

### Was ging schief?
1. **Zwei Such-Features verwechselt:** Universal Search vs Filter Text Search
2. **Zu schnell refactored:** CustomersPageV2 geändert ohne Architektur-Analyse
3. **Kein Testing:** User musste Bug finden ("ich kann nicht tippen")

### Was machen wir diesmal besser?
1. ✅ **Architektur-Analyse FIRST:** Zwei Such-Features klar getrennt
2. ✅ **Migrations-Plan BEFORE Code:** Dieses Dokument
3. ✅ **Testing-Checklist:** Universal Search Test ist BLOCKER
4. ✅ **Rollback-Plan:** Git Branch + Fallback-Strategie
5. ✅ **User-Approval:** Plan vorlegen, Freigabe abwarten

---

## 🚀 Next Steps

1. **User-Review:** Diesen Migrationsplan mit User besprechen
2. **Backend-Verification:** POST /api/customers/search testen (Postman)
3. **Git Branch:** `feature/server-side-filtering` erstellen
4. **Implementation:** CustomersPageV2.tsx ändern (siehe Phase 1)
5. **Testing:** Checklist abarbeiten (Universal Search FIRST!)
6. **Performance-Messung:** Lighthouse + Network Tab Vergleich
7. **User-Testing:** User testet Universal Search + Filter
8. **Merge:** PR erstellen, Review, Merge zu main

---

## 📚 Referenzen

- **ADR-006:** Hybrid-Ansatz Lead Management (begründet Server-Side Performance-Fokus + Context-Prop)
- **SERVER_SIDE_FILTERING.md:** Technische Spezifikation (Performance-Metriken)
- **FRONTEND_DELTA.md Section 8:** Lead-Management UI mit Lifecycle-Context
- **FRONTEND_DELTA.md Section 9:** Server-Side Filtering Dokumentation
- **useUniversalSearch.ts:** Universal Search Hook (DARF NICHT BRECHEN!)
- **IntelligentFilterBar.tsx:** Zentrale UI mit Context-Prop
- **CustomersPageV2.tsx:** Kern der Migration mit Hybrid-Ansatz

---

## 🔄 Update: Lifecycle-Context-Architektur (2025-10-04)

**Neue Erkenntnis:** IntelligentFilterBar benötigt `context`-Prop für kontext-sensitive Status-Filter

### Context-Prop Implementation

**Interface Update:**
```typescript
interface IntelligentFilterBarProps {
  context?: 'customers' | 'leads';  // Lifecycle Context
  // ... existing props
}
```

**Verwendung:**
```typescript
// CustomersPageV2
<IntelligentFilterBar context="customers" ... />
// → Zeigt Status: AKTIV, INAKTIV, ARCHIVIERT

// LeadsPage (CustomersPageV2 wrapper)
<IntelligentFilterBar context="leads" ... />
// → Zeigt Status: LEAD, PROSPECT
```

**FilterDrawer Logic:**
```typescript
{Object.values(CustomerStatus).filter(status => {
  if (context === 'leads') {
    return status === 'LEAD' || status === 'PROSPECT';
  }
  return status !== 'LEAD' && status !== 'PROSPECT' && status !== 'RISIKO';
}).map(status => ...)}
```

### Rationale

**Lifecycle-basiertes Denken:**
- Lead ist eine **Lifecycle-Phase**, kein separates Entity
- Alle Filter (Risiko, Umsatz, Kontakte) gelten für **beide Phasen**
- Nur Status-Filter ist kontext-sensitiv ("Baby" vs "Erwachsen")

**Best Practice:**
- Salesforce: Lead → Opportunity → Customer (gleiche Datenbasis)
- HubSpot: Contact Lifecycle Stage (Lead → Customer)
- Pipedrive: Deal Stage (gleiche Pipeline)

**Vorteile:**
- ✅ Eine FilterBar für beide Kontexte (keine Code-Duplikation)
- ✅ Konsistente UX (gleiche Bedienung)
- ✅ Einfache Erweiterung (ein Prop statt zwei Komponenten)

---

**🤖 Ende Migrationsplan | Version 1.1 | 2025-10-04**
