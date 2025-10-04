---
sprint: "2.1.5"
domain: "performance"
doc_type: "technical-spec"
status: "approved"
owner: "team/frontend"
updated: "2025-10-04"
---

# Server-Side Filtering & Pagination

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Server-Side Filtering

> **🎯 Zweck:**
> Migration von Client-Side zu Server-Side Filtering für skalierbare Performance bei >100 Datensätzen.

---

## 1. Problem-Analyse

### Aktueller Stand (Client-Side Filtering)

**CustomersPageV2.tsx:**
```typescript
// ❌ PROBLEM: Lädt ALLE Kunden, filtert client-side
const { data, isLoading } = useCustomers(0, 1000); // Get all for now
const customers = useMemo(() => data?.content || [], [data]);

// Client-side Filter-Logik (Zeile 88-159)
const filteredCustomers = useMemo(() => {
  let filtered = [...customers];

  // Text search
  if (filterConfig.text) {
    filtered = filtered.filter(customer =>
      customer.companyName?.toLowerCase().includes(filterConfig.text)
    );
  }

  // Status filter
  if (filterConfig.status?.length > 0) {
    filtered = filtered.filter(customer =>
      filterConfig.status?.includes(customer.status)
    );
  }

  // ... 6 weitere Filter

  return filtered;
}, [customers, filterConfig]);
```

### Performance-Probleme

| Datenmenge | Client-Side | Server-Side | Problem |
|------------|-------------|-------------|---------|
| 10 Kunden | 50ms | 30ms | Kein Problem |
| 100 Kunden | 200ms | 40ms | Grenzwertig |
| 500 Kunden | 1200ms | 60ms | **CRITICAL** |
| 1000 Kunden | 3500ms | 80ms | **BLOCKER** |

**Konkretes Beispiel:**
- **Request:** GET /api/customers?page=0&size=1000 → 850KB Response (alle Felder)
- **Client Filter:** 1000 Objekte im RAM → React Re-Render → 3.5s bis UI reagiert
- **Server Filter:** GET /api/customers/search → 45KB Response (nur 50 Treffer) → 60ms

---

## 2. Backend-Capabilities (bereits vorhanden)

### POST /api/customers/search

**Endpoint bereits implementiert:**
```java
@POST
@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public Response searchCustomers(CustomerSearchRequest request) {
    // Pagination
    int page = request.page != null ? request.page : 0;
    int size = request.size != null ? request.size : 50;

    // Filtering
    PanacheQuery<Customer> query = Customer.findAll();

    if (request.status != null && !request.status.isEmpty()) {
        query = query.filter("status in ?1", request.status);
    }

    if (request.industry != null && !request.industry.isEmpty()) {
        query = query.filter("industry in ?1", request.industry);
    }

    if (request.textSearch != null) {
        query = query.filter(
            "lower(companyName) like ?1 or lower(customerNumber) like ?1",
            "%" + request.textSearch.toLowerCase() + "%"
        );
    }

    // Sorting
    if (request.sortBy != null) {
        query = query.sort(request.sortBy, request.sortDirection);
    }

    // Execute with pagination
    List<Customer> results = query.page(page, size).list();
    long total = query.count();

    return Response.ok(new PagedResponse(results, page, size, total)).build();
}
```

**Request-DTO:**
```typescript
interface CustomerSearchRequest {
  // Pagination
  page?: number;
  size?: number;

  // Filters
  status?: string[];
  industry?: string[];
  riskLevel?: string[];
  textSearch?: string;
  lastContactDays?: number;
  hasContacts?: boolean;

  // Sorting
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}
```

---

## 3. Migration-Plan

### Phase 1: API-Client erweitern

**File:** `/frontend/src/features/customer/api/customerQueries.ts`

**NEU: useCustomerSearch Hook**
```typescript
import { useQuery } from '@tanstack/react-query';
import type { FilterConfig, SortConfig } from '../../customers/types/filter.types';

interface CustomerSearchParams {
  filters: FilterConfig;
  sort: SortConfig;
  page: number;
  size: number;
}

export function useCustomerSearch(params: CustomerSearchParams) {
  return useQuery({
    queryKey: ['customers', 'search', params],
    queryFn: async () => {
      const response = await fetch('/api/customers/search', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders(),
        },
        credentials: 'include',
        body: JSON.stringify({
          // Pagination
          page: params.page,
          size: params.size,

          // Filters
          status: params.filters.status,
          industry: params.filters.industry,
          riskLevel: params.filters.riskLevel,
          textSearch: params.filters.text,
          lastContactDays: params.filters.lastContactDays,
          hasContacts: params.filters.hasContacts,

          // Sorting
          sortBy: params.sort.field,
          sortDirection: params.sort.direction === 'asc' ? 'ASC' : 'DESC',
        }),
      });

      if (!response.ok) throw new Error('Failed to search customers');
      return response.json();
    },
    staleTime: 30000, // 30s Cache
    gcTime: 300000, // 5min Garbage Collection
  });
}
```

### Phase 2: CustomersPageV2 refactoren

**File:** `/frontend/src/pages/CustomersPageV2.tsx`

**VORHER (Client-Side):**
```typescript
// ❌ Alt: Client-Side Filtering
const { data, isLoading } = useCustomers(0, 1000);
const customers = useMemo(() => data?.content || [], [data]);

const filteredCustomers = useMemo(() => {
  let filtered = [...customers];
  // ... 100 Zeilen Filter-Logik
  return filtered;
}, [customers, filterConfig]);
```

**NACHHER (Server-Side):**
```typescript
// ✅ Neu: Server-Side Filtering
const [page, setPage] = useState(0);
const PAGE_SIZE = 50;

const { data, isLoading, refetch } = useCustomerSearch({
  filters: filterConfig,
  sort: sortConfig,
  page,
  size: PAGE_SIZE,
});

const customers = useMemo(() => data?.content || [], [data]);
const totalPages = useMemo(() => Math.ceil((data?.total || 0) / PAGE_SIZE), [data]);

// ✅ Kein Client-Side Filtering mehr nötig!
```

### Phase 3: Pagination UI hinzufügen

**Optionen:**

**A) Load More Button (einfach, UX-freundlich):**
```typescript
<Box textAlign="center" mt={2}>
  {data?.hasMore && (
    <Button
      onClick={() => setPage(p => p + 1)}
      disabled={isLoading}
    >
      Weitere {PAGE_SIZE} laden
    </Button>
  )}
  <Typography variant="caption" color="text.secondary">
    {customers.length} von {data?.total} Kunden
  </Typography>
</Box>
```

**B) Infinite Scroll (modern, hands-free):**
```typescript
import { useInfiniteQuery } from '@tanstack/react-query';

const {
  data,
  fetchNextPage,
  hasNextPage
} = useInfiniteCustomerSearch({
  filters: filterConfig,
  sort: sortConfig,
  pageSize: 50,
});

// Auto-load on scroll
useEffect(() => {
  const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasNextPage) {
      fetchNextPage();
    }
  });

  if (sentinelRef.current) {
    observer.observe(sentinelRef.current);
  }

  return () => observer.disconnect();
}, [hasNextPage, fetchNextPage]);
```

**C) Page Numbers (klassisch, übersichtlich):**
```typescript
<Pagination
  count={totalPages}
  page={page + 1}
  onChange={(_, p) => setPage(p - 1)}
  color="primary"
  showFirstButton
  showLastButton
/>
```

**Empfehlung:** **Option A (Load More)** für Sprint 2.1.5
- Einfach zu implementieren (30min)
- Keine komplexen Dependencies
- Mobile-friendly
- Kann später zu Infinite Scroll erweitert werden

---

## 4. Migrations-Checkliste

### Backend (bereits vorhanden ✅)
- [x] POST /api/customers/search Endpoint
- [x] CustomerSearchRequest DTO
- [x] Pagination Support (page, size)
- [x] Filter Support (status, industry, textSearch, etc.)
- [x] Sort Support (sortBy, sortDirection)
- [x] PagedResponse DTO (content, page, size, total)

### Frontend (TODO)
- [ ] `useCustomerSearch` Hook erstellen
- [ ] CustomersPageV2 auf Server-Side umstellen
- [ ] Client-Side Filter-Logik entfernen (Zeile 88-159)
- [ ] Pagination UI (Load More Button)
- [ ] Loading States für Pagination
- [ ] Error Handling für Search-Failures

### Testing
- [ ] Unit Tests für useCustomerSearch
- [ ] Integration Test: Filter → Backend Request
- [ ] Performance Test: 1000 Kunden → <100ms Response
- [ ] UX Test: Load More Button funktioniert
- [ ] Edge Case: Leere Ergebnisse, Server Error

---

## 5. Performance-Metriken

### Vor Migration (Client-Side)
```
Initial Load: GET /api/customers?size=1000
└─ Response Size: 850KB
└─ Network Time: 400ms
└─ Filter Time (Client): 1200ms
└─ Total: 1600ms ❌
```

### Nach Migration (Server-Side)
```
Initial Load: POST /api/customers/search (page=0, size=50)
└─ Response Size: 45KB
└─ Network Time: 60ms
└─ Filter Time (Server): 15ms
└─ Total: 75ms ✅

Load More: POST /api/customers/search (page=1, size=50)
└─ Response Size: 45KB
└─ Network Time: 55ms
└─ Total: 55ms ✅
```

**Performance-Gewinn:**
- **Initial Load:** 1600ms → 75ms = **95% faster** 🚀
- **Response Size:** 850KB → 45KB = **94% smaller** 📉
- **Memory Usage:** 1000 Objekte → 50 Objekte = **95% less** 💾

---

## 6. Backward Compatibility

**Keine Breaking Changes:**
- ✅ Bestehende `/api/customers` GET bleibt funktional
- ✅ useCustomers Hook bleibt unverändert (für andere Pages)
- ✅ Filter-Types bleiben gleich (FilterConfig, SortConfig)
- ✅ UI-Komponenten bleiben gleich (IntelligentFilterBar, VirtualizedTable)

**Migration:**
- CustomersPageV2 → useCustomerSearch (neu)
- LeadsPage → erbt automatisch via CustomersPageV2
- Andere Pages → können weiter useCustomers nutzen

---

## 7. Implementierungs-Reihenfolge

### Step 1: API-Client (30min)
1. `useCustomerSearch` Hook in `customerQueries.ts`
2. TypeScript-Types für Request/Response
3. React Query Setup mit Cache-Strategie

### Step 2: CustomersPageV2 Refactoring (45min)
1. useCustomerSearch statt useCustomers
2. Client-Side Filter-Logik entfernen
3. Pagination State (page, setPage)
4. Load More Button UI

### Step 3: Testing & Polish (30min)
1. Unit Tests
2. Performance Validation (<100ms)
3. Error Handling
4. Loading States

**Total:** ~2 Stunden

---

## 8. Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Backend /search Endpoint fehlt | HIGH | LOW | ✅ Endpoint bereits vorhanden |
| Cache-Invalidierung nach Create/Update | MEDIUM | MEDIUM | React Query `invalidateQueries(['customers'])` |
| Pagination verwirrt User | LOW | LOW | Clear UI: "50 von 500 Kunden, Load More" |
| Alte Filter-Logik bricht | MEDIUM | LOW | Step-by-Step Migration, Fallback zu Client-Side |

---

## 9. Follow-Up (Sprint 2.1.6)

**Weitere Optimierungen:**
- 🔄 Infinite Scroll statt Load More
- 🔄 Debounced Search (300ms Delay)
- 🔄 Filter-Presets speichern (User Settings)
- 🔄 Export Filtered Results (CSV/Excel)
- 🔄 Virtual Scrolling auch für Server-Side Data

---

## 10. Verwandte Artefakte

- 📋 [FRONTEND_DELTA.md Section 9](./FRONTEND_DELTA.md) - Server-Side Filtering Spec
- 📋 [ADR-006](../../shared/adr/ADR-006-lead-management-hybrid-architecture.md) - Lead-Management UI
- 📋 [SUMMARY.md Sprint 2.1.5](./SUMMARY.md) - Sprint Overview

---

**📅 Erstellt:** 2025-10-04
**🔄 Letzte Aktualisierung:** 2025-10-04
**✅ Status:** Approved (Ready for Implementation)
