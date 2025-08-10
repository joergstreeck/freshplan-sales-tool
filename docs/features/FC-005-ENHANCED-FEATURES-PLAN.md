# 📋 FC-005 Step3 PR4: Enhanced Features Implementation Plan

**Status:** 🔄 In Progress  
**Branch:** feature/fc-005-enhanced-features  
**Erstellt:** 2025-08-10  
**Letztes Update:** 2025-08-10  

## 🎯 Übersicht

Diese Planung umfasst die erweiterten Features für das Customer Management System, insbesondere die intelligente Filterbar mit Kontaktsuche und Export-Funktionalität.

## 📊 Gesamtfortschritt

- [x] IntelligentFilterBar Komponente
- [x] Export Backend-Endpoints
- [x] MiniAuditTimeline
- [x] Filter-Integration in CustomersPageV2
- [x] Kontaktsuche (Universal Search mit Deep-Linking)
- [ ] Virtual Scrolling
- [ ] SmartContactCard mit Audit
- [ ] AuditExportButton

## 🔗 Navigation

**← Zurück:** [FC-005 Xentral Integration](./FC-005-xentral-integration.md)  
**→ Weiter:** [FC-006 Sales Cockpit V2](../FC-006-sales-cockpit-v2.md)  
**↑ Übergeordnet:** [Feature-Übersicht](../README.md)  

## 📦 PR4 Components

### ✅ 1. IntelligentFilterBar
**Status:** Implementiert  
**Datei:** `/frontend/src/features/customers/components/filter/IntelligentFilterBar.tsx`

**Features:**
- Universal Search mit Debouncing
- Quick Filter Chips
- Erweiterte Filter Drawer
- Column Manager mit Drag & Drop
- Export Menu (CSV, Excel, PDF, JSON)
- Saved Filter Sets
- Deutsche Übersetzungen

**Probleme gelöst:**
- RiskLevel Import-Fehler behoben
- SortConfig Type-Mismatch behoben
- Browser-Autocomplete deaktiviert
- Deutsche Labels implementiert

### ✅ 2. Export Backend
**Status:** Implementiert  
**Dateien:**
- `/backend/src/main/java/de/freshplan/api/resources/ExportResource.java`
- `/backend/src/main/java/de/freshplan/domain/export/service/ExportService.java`
- `/backend/src/main/java/de/freshplan/domain/export/service/dto/ExportRequest.java`
- `/backend/src/main/java/de/freshplan/domain/export/service/dto/ExportOptions.java`

**Endpoints:**
- GET `/api/export/audit/csv`
- GET `/api/export/audit/pdf`
- GET `/api/export/audit/excel`
- GET `/api/export/audit/json`
- GET `/api/export/customers/csv`
- GET `/api/export/customers/pdf`
- GET `/api/export/customers/excel`
- GET `/api/export/customers/json`
- POST `/api/export/compliance/pdf`

### ✅ 3. MiniAuditTimeline
**Status:** Implementiert  
**Datei:** `/frontend/src/features/audit/components/MiniAuditTimeline.tsx`

**Features:**
- Kompakte Audit-Historie für Contact Cards
- Accordion-basierte UI
- Role-based Visibility
- Zeigt letzte 5 Änderungen

### 🔄 4. Hybride Kontaktsuche (Universal Search mit Deep-Linking)
**Status:** In Entwicklung  
**Geschätzter Aufwand:** 4-6 Stunden  
**Konzept:** Hybride Lösung mit kategorisierten Ergebnissen und Deep-Linking

#### 4.1 Backend-Erweiterungen (Erweiterte Suche in ALLEN Kontakt-Feldern)

**a) SearchResource.java** ✅ Implementiert
```java
@Path("/api/search")
@ApplicationScoped
public class SearchResource {
    
    @GET
    @Path("/universal")
    public SearchResults universalSearch(
        @QueryParam("query") String query,  // Geändert von "q" auf "query"
        @QueryParam("includeContacts") @DefaultValue("true") boolean includeContacts,
        @QueryParam("includeInactive") @DefaultValue("false") boolean includeInactive,
        @QueryParam("limit") @DefaultValue("15") int limit
    ) {
        // Implementiert in SearchService
    }
}
```

**b) SearchService.java** ✅ Implementiert mit erweiterter Kontakt-Suche
```java
// Erweiterte Query-Analyse für alle Kontaktfelder
public enum QueryType {
    EMAIL,           // Email-Pattern erkannt
    PHONE,           // Telefon/Mobile-Pattern
    CUSTOMER_NUMBER, // Kundennummer-Pattern
    TEXT            // Allgemeiner Text
}

// Erweiterte Kontakt-Suche (ALLE Felder)
private List<SearchResult> searchContacts(String query, QueryType queryType, int limit) {
    List<Contact> contacts;
    
    switch (queryType) {
        case EMAIL:
            // Exakte Email-Suche
            contacts = contactRepository.findByEmail(query, limit);
            break;
            
        case PHONE:
            // Suche in phone UND mobile Feldern
            contacts = contactRepository.findByPhoneOrMobile(query, limit);
            break;
            
        case TEXT:
        default:
            // Volltext-Suche in ALLEN Feldern
            contacts = contactRepository.searchContactsFullText(query, limit);
            break;
    }
    
    // Relevanz-Scoring und DTO-Konvertierung
    return convertToSearchResults(contacts, query, queryType);
}
```

**c) ContactRepository-Erweiterungen** 🔧 Muss erweitert werden
```java
// ContactRepository - Erweiterte Suche in ALLEN Feldern
public List<Contact> searchContactsFullText(String query, int limit) {
    String searchPattern = "%" + query.toLowerCase() + "%";
    return find("""
        isDeleted = false AND (
            lower(firstName) like ?1 OR 
            lower(lastName) like ?1 OR 
            lower(firstName || ' ' || lastName) like ?1 OR
            lower(email) like ?1 OR 
            lower(phone) like ?1 OR 
            lower(mobile) like ?1 OR
            lower(position) like ?1 OR
            lower(department) like ?1 OR
            lower(notes) like ?1
        )
        """, searchPattern)
        .page(0, limit)
        .list();
}

// Suche in Telefon ODER Mobile
public List<Contact> findByPhoneOrMobile(String phone, int limit) {
    String searchPattern = "%" + phone.replaceAll("[^0-9+]", "") + "%";
    return find("""
        isDeleted = false AND (
            REGEXP_REPLACE(phone, '[^0-9+]', '') like ?1 OR
            REGEXP_REPLACE(mobile, '[^0-9+]', '') like ?1
        )
        """, searchPattern)
        .page(0, limit)
        .list();
}

// Exakte Email-Suche (case-insensitive)
public List<Contact> findByEmail(String email, int limit) {
    return find("isDeleted = false AND lower(email) = lower(?1)", email)
        .page(0, limit)
        .list();
}
```

#### 4.2 Frontend-Implementierung (Hybride Lösung mit Deep-Linking)

**a) SearchResultsDropdown.tsx** ✅ Implementiert, muss erweitert werden
```typescript
// Erweiterte Props für Deep-Linking
interface SearchResultsDropdownProps {
  searchQuery: string;
  searchResults: SearchResults | null;
  isLoading: boolean;
  error: Error | null;
  onCustomerClick?: (customerId: string) => void;
  onContactClick?: (customerId: string, contactId: string) => void; // NEU: Deep-Link
  onClose?: () => void;
}

export const SearchResultsDropdown: React.FC<SearchResultsDropdownProps> = ({
  searchQuery,
  searchResults,
  isLoading,
  error,
  onCustomerClick,
  onContactClick,
  onClose,
}) => {
  // Highlight-Funktion für Suchergebnisse
  const highlightMatch = (text: string, query: string) => {
    const parts = text.split(new RegExp(`(${query})`, 'gi'));
    return (
      <span>
        {parts.map((part, i) => 
          part.toLowerCase() === query.toLowerCase() ? 
            <mark key={i} style={{ backgroundColor: '#ffeb3b' }}>{part}</mark> : 
            part
        )}
      </span>
    );
  };

  return (
    <Paper elevation={3} sx={{ 
      position: 'absolute', 
      top: '100%', 
      left: 0, 
      right: 0, 
      zIndex: 1300,
      maxHeight: '60vh',
      overflow: 'auto'
    }}>
      {/* KUNDEN-SEKTION */}
      {customerResults.length > 0 && (
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Typography 
            variant="subtitle2" 
            sx={{ p: 1, bgcolor: 'grey.50', fontWeight: 'bold' }}
          >
            🏢 KUNDEN ({customerResults.length})
          </Typography>
          {customerResults.map(result => (
            <MenuItem 
              key={result.id}
              onClick={() => onCustomerClick?.(result.id)}
            >
              <BusinessIcon sx={{ mr: 2, color: 'primary.main' }} />
              <Box flex={1}>
                <Typography variant="body2">
                  {highlightMatch(result.data.companyName, searchQuery)}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {result.data.customerNumber} • Score: {result.relevanceScore}
                </Typography>
              </Box>
            </MenuItem>
          ))}
        </Box>
      )}

      {/* ANSPRECHPARTNER-SEKTION */}
      {contactResults.length > 0 && (
        <Box>
          <Typography 
            variant="subtitle2" 
            sx={{ p: 1, bgcolor: 'grey.50', fontWeight: 'bold' }}
          >
            👤 ANSPRECHPARTNER ({contactResults.length})
          </Typography>
          {contactResults.map(result => {
            const contact = result.data as ContactSearchDto;
            return (
              <MenuItem 
                key={result.id}
                onClick={() => onContactClick?.(contact.customerId, contact.id)}
              >
                <PersonIcon sx={{ mr: 2, color: 'secondary.main' }} />
                <Box flex={1}>
                  <Typography variant="body2">
                    {highlightMatch(`${contact.firstName} ${contact.lastName}`, searchQuery)}
                    {contact.isPrimary && (
                      <Chip label="Primär" size="small" sx={{ ml: 1 }} />
                    )}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    bei: {contact.customerName}
                  </Typography>
                  <Typography variant="caption" display="block" color="text.secondary">
                    {contact.email && highlightMatch(contact.email, searchQuery)}
                    {contact.phone && ` • ${highlightMatch(contact.phone, searchQuery)}`}
                  </Typography>
                </Box>
              </MenuItem>
            );
          })}
        </Box>
      )}

      {/* Keine Ergebnisse */}
      {!isLoading && searchResults && 
       customerResults.length === 0 && 
       contactResults.length === 0 && (
        <Box p={2} textAlign="center">
          <Typography color="text.secondary">
            Keine Ergebnisse für "{searchQuery}"
          </Typography>
        </Box>
      )}
    </Paper>
  );
};
```

**b) Integration in IntelligentFilterBar** ✅ Implementiert, muss erweitert werden
```typescript
// IntelligentFilterBar.tsx - Deep-Linking Integration
const navigate = useNavigate();

// Universal Search Hook
const {
  searchResults,
  isLoading: isSearching,
  error: searchError,
  search: performUniversalSearch,
  clearResults,
} = useUniversalSearch({
  includeContacts: true,
  includeInactive: false,
  limit: 15,
  debounceMs: 300,
  minQueryLength: 2,
});

// Handler für Deep-Linking
const handleContactClick = (customerId: string, contactId: string) => {
  // Navigate zu Kunde MIT Kontakt-Highlight
  navigate(`/customers/${customerId}?highlightContact=${contactId}`);
  clearResults();
  setSearchTerm('');
};

const handleCustomerClick = (customerId: string) => {
  navigate(`/customers/${customerId}`);
  clearResults();
  setSearchTerm('');
};

// In JSX
<Box position="relative">
  <TextField
    placeholder="Suche nach Firma, Kundennummer oder Ansprechpartner..."
    value={searchTerm}
    onChange={(e) => {
      setSearchTerm(e.target.value);
      performUniversalSearch(e.target.value);
    }}
  />
  
  {searchResults && (searchResults.customers.length > 0 || searchResults.contacts.length > 0) && (
    <SearchResultsDropdown
      searchQuery={searchTerm}
      searchResults={searchResults}
      isLoading={isSearching}
      error={searchError}
      onCustomerClick={handleCustomerClick}
      onContactClick={handleContactClick}
      onClose={clearResults}
    />
  )}
</Box>
```

**c) CustomerDetailPage - Kontakt-Highlighting** 🆕 Neu zu implementieren
```typescript
// CustomerDetailPage.tsx
import { useSearchParams } from 'react-router-dom';

export function CustomerDetailPage() {
  const [searchParams] = useSearchParams();
  const highlightContactId = searchParams.get('highlightContact');
  const [activeTab, setActiveTab] = useState('overview');
  
  // Auto-Switch zu Kontakte-Tab wenn highlightContact vorhanden
  useEffect(() => {
    if (highlightContactId) {
      setActiveTab('contacts');
    }
  }, [highlightContactId]);
  
  // In Kontakte-Tab
  {activeTab === 'contacts' && (
    <ContactsTab 
      customerId={customerId}
      highlightContactId={highlightContactId}
    />
  )}
}

// ContactsTab.tsx
export function ContactsTab({ customerId, highlightContactId }) {
  const contactRef = useRef<HTMLDivElement>(null);
  
  useEffect(() => {
    if (highlightContactId && contactRef.current) {
      // Scroll zu Kontakt
      const element = document.getElementById(`contact-${highlightContactId}`);
      element?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      
      // Highlight-Animation
      element?.classList.add('highlight-animation');
      setTimeout(() => {
        element?.classList.remove('highlight-animation');
      }, 3000);
    }
  }, [highlightContactId]);
  
  return (
    <Box>
      {contacts.map(contact => (
        <Card 
          key={contact.id}
          id={`contact-${contact.id}`}
          sx={{
            ...(contact.id === highlightContactId && {
              animation: 'pulse 1s ease-in-out 3',
              border: '2px solid primary.main'
            })
          }}
        >
          <SmartContactCard contact={contact} />
        </Card>
      ))}
    </Box>
  );
}
```

#### 4.3 Testing

**Backend Tests:**
- SearchServiceTest (Unit Test für Query-Analyse und Scoring)
- SearchResourceIntegrationTest (API-Tests)
- ContactRepositoryTest (Erweiterte Suche in allen Feldern)
- Performance-Test mit 1000+ Einträgen

**Frontend Tests:**
- useUniversalSearch.test.ts (Hook-Tests mit MSW)
- SearchResultsDropdown.test.tsx (Component Tests)
- CustomerDetailPage.test.tsx (Deep-Link Test)
- E2E Test: Suche → Kontakt-Click → Highlight

#### 4.4 Migration für erweiterte Suche
```sql
-- V216__add_extended_search_indexes.sql
-- Erweiterte Indizes für Kontakt-Suche in ALLEN Feldern

-- Basis-Index für Kunden
CREATE INDEX IF NOT EXISTS idx_customers_search 
ON customers(lower(company_name), customer_number, lower(trading_name));

-- Erweiterte Indizes für Kontakte (ALLE durchsuchbaren Felder)
CREATE INDEX IF NOT EXISTS idx_contacts_fulltext_search 
ON customer_contacts(
    lower(first_name), 
    lower(last_name),
    lower(first_name || ' ' || last_name),
    lower(email),
    lower(position),
    lower(department)
);

-- Separater Index für Telefon-Suche (normalisiert)
CREATE INDEX IF NOT EXISTS idx_contacts_phone_search 
ON customer_contacts(
    REGEXP_REPLACE(phone, '[^0-9+]', ''),
    REGEXP_REPLACE(mobile, '[^0-9+]', '')
);

-- Partial Index für Primary Contacts (Performance)
CREATE INDEX IF NOT EXISTS idx_contacts_primary 
ON customer_contacts(customer_id, is_primary) 
WHERE is_primary = true;

-- GIN Index für Notes (wenn pg_trgm verfügbar)
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- CREATE INDEX IF NOT EXISTS idx_contacts_notes_gin 
-- ON customer_contacts USING gin(lower(notes) gin_trgm_ops);
```

### ⏳ 5. Virtual Scrolling
**Status:** Geplant  
**Komponente:** CustomerTable mit react-window

### ⏳ 6. SmartContactCard mit Audit
**Status:** Geplant  
**Integration:** MiniAuditTimeline in ContactCard

### ⏳ 7. AuditExportButton
**Status:** Geplant  
**Komponente:** Standalone Export-Button für Audit-Trail

## 📝 Implementierungs-Reihenfolge (Aktualisiert für Hybride Lösung)

1. **Phase 1: Backend - Erweiterte Kontakt-Suche** ✅ ABGESCHLOSSEN (2h)
   - [x] SearchResource implementiert ✅
   - [x] SearchService mit Query-Analyse ✅
   - [x] ContactRepository erweitert für ALLE Felder ✅
   - [x] Migration V216 für erweiterte Indizes ✅

2. **Phase 2: Frontend - Hybride Search Results** ✅ ABGESCHLOSSEN (2h)
   - [x] useUniversalSearch Hook ✅
   - [x] SearchResultsDropdown Basis ✅
   - [x] SearchResultsDropdown erweitert (Highlighting, bessere Visuals) ✅
   - [x] Deep-Link Handler in IntelligentFilterBar ✅
   - [x] CustomerDetailPage mit highlightContact Parameter ✅

3. **Phase 3: Deep-Linking & Highlighting** ✅ ABGESCHLOSSEN (1h)
   - [x] ContactsTab mit Auto-Scroll ✅
   - [x] Highlight-Animation für Kontakte ✅
   - [x] URL-Parameter Handling ✅
   - [x] Smooth Scrolling Implementation ✅

4. **Phase 4: Testing** (1h)
   - [ ] ContactRepository erweiterte Suche testen
   - [ ] Deep-Link Navigation testen
   - [ ] Performance mit vielen Kontakten
   - [ ] E2E Test: Suche → Click → Highlight

5. **Phase 5: Polish & Optimization** (1h)
   - [ ] Virtual Scrolling bei vielen Ergebnissen
   - [ ] Search Result Caching optimieren
   - [ ] SmartContactCard + Audit Integration
   - [ ] AuditExportButton

## 🚀 Deployment-Checkliste

- [ ] Alle Tests grün
- [ ] Migration V216 erstellt
- [ ] Code Review durchgeführt
- [ ] Performance getestet (< 200ms Response)
- [ ] Dokumentation aktualisiert
- [ ] PR erstellt und beschrieben

## 📊 Metriken

**Ziel-Metriken:**
- Search Response Time: < 200ms
- Search Result Relevance: > 80% accuracy
- Bundle Size Increase: < 10KB
- Test Coverage: > 80%

## 🔗 Verwandte Dokumente

- [API Contract](../technical/API_CONTRACT.md)
- [Frontend Specification](../technical/FRONTEND_BACKEND_SPECIFICATION.md)
- [Test Strategy](../testing/TEST_STRATEGY.md)
- [Performance Guidelines](../guidelines/PERFORMANCE.md)

## 📌 Notizen (Aktualisiert nach Team-Diskussion)

### Konzept-Entscheidung: Hybride Lösung
- **Problem:** Mischen von Kontakten in Kundentabelle ist konzeptionell verwirrend
- **Lösung:** Kategorisierte Dropdown mit Deep-Linking zu Kontakten
- **Vorteil:** Keine Ambiguität, keine zweite Suche nötig, intuitiv

### Technische Details:
- Universal Search durchsucht ALLE Kontakt-Felder (Name, Email, Telefon, Mobile, Position, Department, Notes)
- Relevanz-Scoring basiert auf Übereinstimmungstyp (exakt > prefix > contains)
- Search-Results werden gecacht (30 Sekunden)
- Mindestens 2 Zeichen für Suche erforderlich
- Deep-Link Format: `/customers/{customerId}?highlightContact={contactId}`
- Highlight-Animation: 3 Sekunden Puls-Effekt mit farbigem Rahmen
- Auto-Scroll zu gehighlightetem Kontakt mit smooth behavior

### UX-Verbesserungen:
- Platzhalter-Text: "Suche nach Firma, Kundennummer oder Ansprechpartner..."
- Visuelle Trennung: Kunden (🏢) und Ansprechpartner (👤) Sektionen
- Kontext anzeigen: "bei: [Kundenname]" für jeden Kontakt
- Suchbegriff-Highlighting in Ergebnissen (gelb hinterlegt)
- Primary-Contact Badge für Haupt-Ansprechpartner

---

**Letzte Aktualisierung:** 2025-08-10 02:00 Uhr  
**Autor:** Claude (FC-005 Implementation Team)  
**Review:** Ausstehend