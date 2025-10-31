# 🔍 Lead-Suche Implementierung

**Datum:** 2025-10-29
**Problem:** Leadliste kann nur Kunden suchen, keine Leads
**Lösung:** Lead-Suche nach Blaupause der funktionierenden Kundensuche implementieren

---

**📍 Navigation:**
- [🏠 Sprint 2.1.7.2 Hauptdokument](../../TRIGGER_SPRINT_2_1_7_2.md)
- [🔧 Technische Spec](./SPEC_SPRINT_2_1_7_2_TECHNICAL.md) - D2.1 Details in technischer Spezifikation
- [📋 Commit-Zusammenfassung](./sprint-2.1.7.2-COMMIT-SUMMARY.md) - Implementierungs-Historie

**🔗 Verwandte Themen:**
- SearchService Architektur - siehe Technische Spec
- CQRS Pattern - Lead vs Customer Suche

---

## 🎯 ANFORDERUNG

**Zwei separate Suchen:**
1. **Kundensuche** (funktioniert bereits ✅) - sucht in `customers` + `customer_contacts`
2. **Leadsuche** (neu implementieren ❌) - soll in `leads` + `lead_contacts` suchen

**Keine Vermischung!** Context-Parameter entscheidet, welche Tabellen durchsucht werden.

---

## ✅ FUNKTIONIERENDE KUNDENSUCHE (Blaupause)

### 1. DTOs (Customer)

**CustomerSearchDto.java**
```java
public class CustomerSearchDto {
  private String id;              // UUID als String
  private String companyName;
  private String customerNumber;
  private String status;
  private String contactEmail;
  private String contactPhone;
  private Integer contactCount;
}
```

**ContactSearchDto.java**
```java
public class ContactSearchDto {
  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String position;
  private String customerId;
  private String customerName;
  private Boolean isPrimary;
}
```

---

### 2. SearchService (Customer) - Zeile 180-245

**searchCustomers()**
```java
private List<SearchResult> searchCustomers(
    String query, QueryType queryType, boolean includeInactive, int limit) {

  List<Customer> customers;
  List<String> matchedFields = new ArrayList<>();

  switch (queryType) {
    case EMAIL:
      customers = customerRepository.findByContactEmail(query, limit);
      matchedFields.add("contactEmail");
      break;

    case PHONE:
      customers = customerRepository.findByPhone(query, limit);
      matchedFields.add("phone");
      break;

    case CUSTOMER_NUMBER:
      customers = customerRepository.findByCustomerNumberLike(query + "%", limit);
      matchedFields.add("customerNumber");
      break;

    case TEXT:
    default:
      customers = customerRepository.searchFullText(query, limit);
      matchedFields.addAll(List.of("companyName", "tradingName", "customerNumber"));
      break;
  }

  // Filter inactive
  if (!includeInactive) {
    customers = customers.stream()
        .filter(c -> c.getStatus() != CustomerStatus.INAKTIV)
        .collect(Collectors.toList());
  }

  // Convert to SearchResults
  return customers.stream()
      .map(customer -> {
        int score = calculateRelevanceScore(customer, query, queryType);
        CustomerSearchDto dto = mapToCustomerDto(customer);
        return SearchResult.builder()
            .type("customer")
            .id(customer.getId().toString())
            .data(dto)
            .relevanceScore(score)
            .matchedFields(matchedFields)
            .build();
      })
      .sorted(Comparator.comparingInt(SearchResult::getRelevanceScore).reversed())
      .limit(limit)
      .collect(Collectors.toList());
}
```

**searchContacts()** - Zeile 248-312
```java
private List<SearchResult> searchContacts(
    String query, QueryType queryType, int limit) {

  List<CustomerContact> contacts;
  List<String> matchedFields = new ArrayList<>();

  switch (queryType) {
    case EMAIL:
      contacts = contactRepository.findByEmail(query, limit);
      matchedFields.add("email");
      break;

    case PHONE:
      contacts = contactRepository.findByPhoneOrMobile(query, limit);
      matchedFields.addAll(List.of("phone", "mobile"));
      break;

    case TEXT:
    default:
      contacts = contactRepository.searchContactsFullText(query, limit);
      matchedFields.addAll(List.of("firstName", "lastName", "email", "phone", "position"));
      break;
  }

  // Convert to SearchResults
  return contacts.stream()
      .map(contact -> {
        int score = calculateContactRelevanceScore(contact, query, queryType);
        ContactSearchDto dto = mapToContactDto(contact);
        return SearchResult.builder()
            .type("contact")
            .id(contact.getId().toString())
            .data(dto)
            .relevanceScore(score)
            .matchedFields(matchedFields)
            .build();
      })
      .sorted(Comparator.comparingInt(SearchResult::getRelevanceScore).reversed())
      .limit(limit)
      .collect(Collectors.toList());
}
```

---

## ❌ NEU: LEAD-SUCHE (nach Blaupause)

### 1. DTOs erstellen (Lead)

**LeadSearchDto.java** (neu)
```java
package de.freshplan.domain.search.service.dto;

public class LeadSearchDto {
  private String id;              // Long als String
  private String companyName;
  private String contactPerson;   // Statt customerNumber
  private String status;          // LeadStatus
  private String stage;           // LeadStage (VORMERKUNG/REGISTRIERUNG/QUALIFIZIERT)
  private String email;           // Lead hat direktes email-Feld!
  private String phone;           // Lead hat direktes phone-Feld!
  private Integer contactCount;

  // Getters + Setters + Builder
}
```

**LeadContactSearchDto.java** (neu)
```java
package de.freshplan.domain.search.service.dto;

public class LeadContactSearchDto {
  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String position;
  private String leadId;          // Statt customerId
  private String leadName;        // Statt customerName
  private Boolean isPrimary;

  // Getters + Setters + Builder
}
```

---

### 2. SearchService erweitern (Lead)

**searchLeads()** (neu - Panache-Queries!)
```java
private List<SearchResult> searchLeads(
    String query, QueryType queryType, boolean includeInactive, int limit) {

  List<Lead> leads;
  List<String> matchedFields = new ArrayList<>();

  switch (queryType) {
    case EMAIL:
      // Lead hat direktes email-Feld!
      leads = Lead.list("lower(email) = lower(?1) and isCanonical = true", query);
      matchedFields.add("email");
      break;

    case PHONE:
      // Lead hat direktes phone-Feld!
      String phonePattern = "%" + query + "%";
      leads = Lead.list("phone like ?1 and isCanonical = true", phonePattern);
      matchedFields.add("phone");
      break;

    case TEXT:
    default:
      // Fulltext-Suche in companyName, contactPerson
      String searchPattern = "%" + query.toLowerCase() + "%";
      leads = Lead.list(
        "isCanonical = true AND (lower(companyName) like ?1 OR lower(contactPerson) like ?1)",
        searchPattern
      );
      matchedFields.addAll(List.of("companyName", "contactPerson"));
      break;
  }

  // Filter inactive leads
  if (!includeInactive) {
    leads = leads.stream()
        .filter(l -> l.status != LeadStatus.EXPIRED && l.status != LeadStatus.ARCHIVED)
        .collect(Collectors.toList());
  }

  // Limit results
  leads = leads.stream().limit(limit).collect(Collectors.toList());

  // Convert to SearchResults
  return leads.stream()
      .map(lead -> {
        int score = calculateLeadRelevanceScore(lead, query, queryType);
        LeadSearchDto dto = mapToLeadDto(lead);
        return SearchResult.builder()
            .type("lead")
            .id(lead.id.toString())
            .data(dto)
            .relevanceScore(score)
            .matchedFields(matchedFields)
            .build();
      })
      .sorted(Comparator.comparingInt(SearchResult::getRelevanceScore).reversed())
      .collect(Collectors.toList());
}

private LeadSearchDto mapToLeadDto(Lead lead) {
  LeadSearchDto dto = new LeadSearchDto();
  dto.setId(lead.id.toString());
  dto.setCompanyName(lead.companyName);
  dto.setContactPerson(lead.contactPerson);
  dto.setStatus(lead.status != null ? lead.status.toString() : null);
  dto.setStage(lead.stage != null ? lead.stage.toString() : null);
  dto.setEmail(lead.email);
  dto.setPhone(lead.phone);
  dto.setContactCount(lead.contacts != null ? lead.contacts.size() : 0);
  return dto;
}

private int calculateLeadRelevanceScore(Lead lead, String query, QueryType queryType) {
  int score = 0;
  String lowerQuery = query.toLowerCase();

  // Email exact match
  if (queryType == QueryType.EMAIL && lead.email != null && lead.email.equalsIgnoreCase(query)) {
    score += 100;
  }

  // Company name match
  if (lead.companyName != null) {
    String lowerName = lead.companyName.toLowerCase();
    if (lowerName.equals(lowerQuery)) {
      score += 90;
    } else if (lowerName.startsWith(lowerQuery)) {
      score += 70;
    } else if (lowerName.contains(lowerQuery)) {
      score += 50;
    }
  }

  // Active lead bonus
  if (lead.status == LeadStatus.REGISTERED || lead.status == LeadStatus.IN_CONTACT) {
    score += 20;
  }

  return score;
}
```

**searchLeadContacts()** (neu - Panache-Queries!)
```java
private List<SearchResult> searchLeadContacts(
    String query, QueryType queryType, int limit) {

  List<LeadContact> contacts;
  List<String> matchedFields = new ArrayList<>();

  switch (queryType) {
    case EMAIL:
      contacts = LeadContact.list("isActive = true AND lower(email) = lower(?1)", query);
      matchedFields.add("email");
      break;

    case PHONE:
      String phonePattern = "%" + query + "%";
      contacts = LeadContact.list(
        "isActive = true AND (phone like ?1 OR mobile like ?1)",
        phonePattern
      );
      matchedFields.addAll(List.of("phone", "mobile"));
      break;

    case TEXT:
    default:
      String searchPattern = "%" + query.toLowerCase() + "%";
      contacts = LeadContact.list(
        """
        isActive = true AND (
          lower(firstName) like ?1 OR
          lower(lastName) like ?1 OR
          lower(firstName || ' ' || lastName) like ?1 OR
          lower(email) like ?1 OR
          lower(position) like ?1
        )
        """,
        searchPattern
      );
      matchedFields.addAll(List.of("firstName", "lastName", "email", "position"));
      break;
  }

  // Limit results
  contacts = contacts.stream().limit(limit).collect(Collectors.toList());

  // Convert to SearchResults
  return contacts.stream()
      .map(contact -> {
        int score = calculateLeadContactRelevanceScore(contact, query, queryType);
        LeadContactSearchDto dto = mapToLeadContactDto(contact);
        return SearchResult.builder()
            .type("leadContact")
            .id(contact.getId().toString())
            .data(dto)
            .relevanceScore(score)
            .matchedFields(matchedFields)
            .build();
      })
      .sorted(Comparator.comparingInt(SearchResult::getRelevanceScore).reversed())
      .collect(Collectors.toList());
}

private LeadContactSearchDto mapToLeadContactDto(LeadContact contact) {
  LeadContactSearchDto dto = new LeadContactSearchDto();
  dto.setId(contact.getId().toString());
  dto.setFirstName(contact.getFirstName());
  dto.setLastName(contact.getLastName());
  dto.setEmail(contact.getEmail());
  dto.setPhone(contact.getPhone());
  dto.setPosition(contact.getPosition());
  dto.setLeadId(contact.getLead().id.toString());
  dto.setLeadName(contact.getLead().companyName);
  dto.setIsPrimary(contact.isPrimary());
  return dto;
}

private int calculateLeadContactRelevanceScore(LeadContact contact, String query, QueryType queryType) {
  int score = 0;
  String lowerQuery = query.toLowerCase();

  // Email exact match
  if (queryType == QueryType.EMAIL && contact.getEmail() != null && contact.getEmail().equalsIgnoreCase(query)) {
    score += 100;
  }

  // Name matching
  String fullName = (contact.getFirstName() + " " + contact.getLastName()).toLowerCase();
  if (fullName.equals(lowerQuery)) {
    score += 90;
  } else if (fullName.startsWith(lowerQuery)) {
    score += 70;
  } else if (fullName.contains(lowerQuery)) {
    score += 50;
  }

  // Primary contact bonus
  if (contact.isPrimary()) {
    score += 30;
  }

  return score;
}
```

---

### 3. Context-Routing hinzufügen

**universalSearch() erweitern**
```java
public SearchResults universalSearch(
    String query,
    boolean includeContacts,
    boolean includeInactive,
    int limit,
    String context) {  // NEU: context Parameter

  if (cqrsEnabled) {
    return queryService.universalSearch(query, includeContacts, includeInactive, limit, context);
  }

  return legacyUniversalSearch(query, includeContacts, includeInactive, limit, context);
}
```

**legacyUniversalSearch() erweitern**
```java
private SearchResults legacyUniversalSearch(
    String query,
    boolean includeContacts,
    boolean includeInactive,
    int limit,
    String context) {  // NEU: context Parameter

  long startTime = System.currentTimeMillis();
  QueryType queryType = detectQueryType(query);

  List<SearchResult> entityResults = new ArrayList<>();
  List<SearchResult> contactResults = new ArrayList<>();

  // Context-basiertes Routing
  if ("leads".equals(context)) {
    // ❌ NEU: Suche in Leads
    entityResults = searchLeads(query, queryType, includeInactive, limit);

    if (includeContacts) {
      contactResults = searchLeadContacts(query, queryType, limit);
    }

  } else {
    // ✅ BESTEHT: Suche in Customers (default)
    entityResults = searchCustomers(query, queryType, includeInactive, limit);

    if (includeContacts) {
      contactResults = searchContacts(query, queryType, limit);
    }
  }

  long executionTime = System.currentTimeMillis() - startTime;

  return SearchResults.builder()
      .customers(entityResults)  // Feldname bleibt generisch
      .contacts(contactResults)
      .totalCount(entityResults.size() + contactResults.size())
      .executionTime(executionTime)
      .build();
}
```

---

### 4. SearchResource erweitern

**context Parameter hinzufügen**
```java
@GET
@Path("/universal")
public Response universalSearch(
    @QueryParam("query") @NotBlank @Size(min = 2, max = 100) String query,
    @QueryParam("includeContacts") @DefaultValue("true") boolean includeContacts,
    @QueryParam("includeInactive") @DefaultValue("false") boolean includeInactive,
    @QueryParam("limit") @DefaultValue("20") @Min(1) @Max(100) int limit,
    @QueryParam("context") @DefaultValue("customers") String context) {  // NEU!

  LOG.infof(
    "Search request: query='%s', context='%s', includeContacts=%s, limit=%d",
    query, context, includeContacts, limit);

  SearchResults results =
      searchService.universalSearch(query, includeContacts, includeInactive, limit, context);

  return Response.ok(results).build();
}
```

---

### 5. Frontend anpassen

#### ⚠️ WICHTIG: V2Page-Architektur
**CustomersPageV2 ist wiederverwendbar für beide Kontexte!**

```typescript
// CustomersPageV2.tsx - Zeile 42-50
interface CustomersPageV2Props {
  context?: 'customers' | 'leads'; // Lifecycle Context
}

// Zeile 526-540: IntelligentFilterBar wird context übergeben
<IntelligentFilterBar
  onFilterChange={setFilterConfig}
  onSortChange={(config: SortConfig) => { ... }}
  onColumnChange={setActiveColumns}
  totalCount={customers.length}
  filteredCount={filteredCustomers.length}
  loading={isLoading}
  initialFilters={filterConfig}
  context={context}  // ✅ Context wird bereits übergeben!
/>
```

**Erkenntnisse:**
- ✅ Es gibt KEIN separates LeadsPageV2 - CustomersPageV2 wird für beide verwendet
- ✅ CustomersPageV2 empfängt `context` als Prop
- ✅ CustomersPageV2 gibt `context` an IntelligentFilterBar weiter
- ✅ Frontend-Architektur ist bereits vorbereitet für context-basierte Trennung

**Was fehlt noch:**
- ❌ useUniversalSearch sendet `context` noch nicht an Backend-API

#### useUniversalSearch.ts erweitern

```typescript
// Interface erweitern
interface UseUniversalSearchOptions {
  includeContacts?: boolean;
  includeInactive?: boolean;
  limit?: number;
  debounceMs?: number;
  minQueryLength?: number;
  context?: 'leads' | 'customers';  // NEU!
}

// Parameter-Bau anpassen (Zeile ~164-169)
const params = new URLSearchParams({
  query,
  includeContacts: includeContacts.toString(),
  includeInactive: includeInactive.toString(),
  limit: limit.toString(),
  context: context || 'customers',  // NEU!
});
```

---

## 🔑 WICHTIGE UNTERSCHIEDE: Customer vs Lead

| Aspekt | Customer | Lead |
|--------|----------|------|
| **Pattern** | Repository | Panache Active Record |
| **Queries** | `customerRepository.searchFullText()` | `Lead.list("query", params)` |
| **ID-Typ** | UUID | Long |
| **Fields** | private + getters | public |
| **Email/Phone** | Nur in Contacts | Direkt in Lead Entity! |
| **Nummer-Feld** | customerNumber | ❌ Kein Nummer-Feld |
| **Status-Filter** | `!= CustomerStatus.INAKTIV` | `!= LeadStatus.EXPIRED && != ARCHIVED` |

---

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: DTOs
- [ ] LeadSearchDto.java erstellen
- [ ] LeadContactSearchDto.java erstellen

### Phase 2: SearchService
- [ ] searchLeads() Methode hinzufügen
- [ ] searchLeadContacts() Methode hinzufügen
- [ ] mapToLeadDto() Helper
- [ ] mapToLeadContactDto() Helper
- [ ] calculateLeadRelevanceScore() Helper
- [ ] calculateLeadContactRelevanceScore() Helper

### Phase 3: Context-Routing
- [ ] universalSearch() - context Parameter hinzufügen
- [ ] legacyUniversalSearch() - context Parameter hinzufügen
- [ ] Context-basiertes if/else Routing implementieren

### Phase 4: API
- [ ] SearchResource - context Parameter hinzufügen

### Phase 5: Frontend
- [ ] useUniversalSearch - context Option hinzufügen
- [ ] useUniversalSearch - context zu API-Call hinzufügen

### Phase 6: Testing
- [ ] Test: Leadsuche findet Leads
- [ ] Test: Leadsuche findet KEINE Kunden
- [ ] Test: Kundensuche findet KEINE Leads (Regression)

---

## ⚠️ WICHTIG: Keine Änderungen an Kundensuche!

Die funktionierende Kundensuche bleibt **vollständig unverändert**:
- ✅ searchCustomers() - KEINE Änderung
- ✅ searchContacts() - KEINE Änderung
- ✅ CustomerRepository - KEINE Änderung
- ✅ ContactRepository - KEINE Änderung

**Nur Ergänzungen für Lead-Suche!**

---

**Geschätzter Aufwand:** 3-4h Backend + 1h Frontend + 1h Testing = **5-6h total**
