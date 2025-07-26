---
Navigation: [⬅️ Previous](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/01-executive-summary.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/03-data-model.md)
Parent: [📁 Tech Concept](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/README.md)
Related: [🔗 ADR Template](/Users/joergstreeck/freshplan-sales-tool/docs/adr/ADR_TEMPLATE.md) | [🔗 Backend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)
---

# 🏛️ FC-005 ARCHITECTURE DECISIONS

**Modul:** FC-005 Customer Management  
**Typ:** Architecture Decision Records (ADRs)  
**Status:** Akzeptiert  

## 📋 ADR-005-1: Hybrid Field System

### Status
**Akzeptiert** - 26.07.2025

### Kontext
Das Customer Management muss verschiedene Branchen (Hotel, Krankenhaus, Restaurant) mit unterschiedlichen Datenanforderungen unterstützen. Legacy-System hatte starre Felder pro Branche.

### Entscheidung
Implementierung eines **Hybrid Field Systems**:
- **Phase 1:** Globaler Field Catalog (vordefinierte Felder)
- **Phase 2:** Custom Fields für Kunden-spezifische Anforderungen

```typescript
interface FieldDefinition {
  id: string
  key: string                    // z.B. "roomCount"
  label: string                  // "Anzahl Zimmer"
  entityType: 'customer' | 'location' | 'detailedLocation'
  industry?: string[]            // ['hotel', 'krankenhaus']
  fieldType: 'text' | 'number' | 'select' | 'date' | 'boolean'
  validation?: string            // Zod Schema als String
  defaultValue?: any
  isCustom: boolean             // false = Catalog, true = Custom
  isPlanned?: boolean           // für zukünftige Felder
}
```

### Konsequenzen
**Positiv:**
- ✅ Neue Branchen ohne Code-Änderung
- ✅ A/B Testing von Feldern möglich
- ✅ Einfache Wartung und Erweiterung

**Negativ:**
- ⚠️ Komplexere Queries (JSONB)
- ⚠️ Keine Compile-Time Type Safety für Fields

## 📋 ADR-005-2: State Management Strategy

### Status
**Akzeptiert** - 26.07.2025

### Kontext
Wizard mit 3 Schritten, Auto-Save, Draft-Funktionalität und komplexen Abhängigkeiten zwischen Feldern.

### Entscheidung
- **Zustand** für lokalen Wizard-State
- **React Query** für Server-State und Caching
- **SessionStorage** für Draft-Persistence

```typescript
// Store Location:
// /frontend/src/features/customers/stores/customerOnboardingStore.ts
interface CustomerOnboardingStore {
  // Wizard State
  currentStep: number
  isDirty: boolean
  lastSaved: Date | null
  
  // Data State
  customerData: Map<string, any>      // fieldKey -> value
  locations: Location[]
  detailedLocations: DetailedLocation[]
  
  // Validation State
  validationErrors: Map<string, string>
  
  // Actions
  setFieldValue: (fieldKey: string, value: any) => void
  saveAsDraft: () => Promise<void>
  loadDraft: (id: string) => Promise<void>
  validateStep: (step: number) => Promise<boolean>
}
```

### Konsequenzen
**Positiv:**
- ✅ Optimale Performance durch lokalen State
- ✅ Auto-Save ohne Server-Roundtrips
- ✅ Einfaches Testing durch klare Trennung

**Negativ:**
- ⚠️ State-Synchronisation erforderlich
- ⚠️ Memory-Footprint bei vielen Fields

## 📋 ADR-005-3: Event-Driven Module Communication

### Status
**Akzeptiert** - 26.07.2025

### Kontext
Customer Module muss mit anderen Modulen (M4 Pipeline, Audit, Notifications) kommunizieren ohne enge Kopplung.

### Entscheidung
Event-basierte Kommunikation über Domain Events:

```java
// Domain Events
public class CustomerCreatedEvent implements DomainEvent {
    private final UUID customerId;
    private final String customerNumber;
    private final String industry;
    private final boolean isChainCustomer;
    private final Instant occurredAt;
}

// Event Publishing
@ApplicationScoped
public class CustomerService {
    @Inject
    Event<CustomerCreatedEvent> customerCreatedEvent;
    
    public Customer createCustomer(CreateCustomerRequest request) {
        Customer customer = // ... create customer
        
        customerCreatedEvent.fire(new CustomerCreatedEvent(
            customer.getId(),
            customer.getCustomerNumber(),
            customer.getIndustry(),
            customer.isChainCustomer()
        ));
        
        return customer;
    }
}
```

### Konsequenzen
**Positiv:**
- ✅ Lose Kopplung zwischen Modulen
- ✅ Einfache Erweiterbarkeit
- ✅ Asynchrone Verarbeitung möglich

**Negativ:**
- ⚠️ Event-Reihenfolge muss beachtet werden
- ⚠️ Debugging wird komplexer

## 📋 ADR-005-4: Draft Persistence Strategy

### Status
**Akzeptiert** - 26.07.2025

### Kontext
User sollen Eingaben nicht verlieren bei Browser-Crash oder Session-Timeout.

### Entscheidung
Zwei-Ebenen Draft-System:
1. **Frontend:** SessionStorage für sofortige Recovery
2. **Backend:** Draft-Status in DB mit Auto-Save alle 30s

```typescript
// Frontend Draft
interface DraftPersistence {
  saveDraft: (data: CustomerData) => void
  loadDraft: () => CustomerData | null
  clearDraft: () => void
}

// Backend Draft
interface CustomerDraftAPI {
  POST   /api/customers/draft        // Create new draft
  PUT    /api/customers/draft/:id    // Update draft
  POST   /api/customers/draft/:id/finalize  // Convert to active
  DELETE /api/customers/draft/:id    // Discard draft
}
```

### Konsequenzen
**Positiv:**
- ✅ Keine Datenverluste
- ✅ Nahtlose User Experience
- ✅ Cross-Device Draft-Fortsetzung

**Negativ:**
- ⚠️ Cleanup-Strategy für alte Drafts nötig
- ⚠️ Storage-Overhead

## 📋 ADR-005-5: Validation Architecture

### Status
**Akzeptiert** - 26.07.2025

### Kontext
Komplexe, feldabhängige Validierungen (Deutsche PLZ, Branchenspezifisch).

### Entscheidung
- **Frontend:** Zod für Schema-Validation
- **Backend:** Bean Validation + Custom Validators
- **Shared:** Validation Rules als JSON

```typescript
// Shared Validation Rules
const validationRules = {
  germanPostalCode: z.string().regex(/^\d{5}$/),
  germanPhone: z.string().regex(/^(\+49|0)[1-9]\d{1,14}$/),
  email: z.string().email(),
  url: z.string().url()
};

// Dynamic Validation
function validateField(
  fieldDef: FieldDefinition, 
  value: any
): ValidationResult {
  if (fieldDef.validation) {
    const validator = validationRules[fieldDef.validation];
    return validator.safeParse(value);
  }
  return { success: true };
}
```

### Konsequenzen
**Positiv:**
- ✅ Konsistente Validierung Frontend/Backend
- ✅ Wiederverwendbare Validation Rules
- ✅ Type-Safe mit Zod

**Negativ:**
- ⚠️ Synchronisation der Rules nötig
- ⚠️ Performance bei vielen Fields

## 📚 Weiterführende Links

- [Data Model →](./03-data-model.md)
- [Frontend State Management →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/02-state-management.md)
- [Event System →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/04-INTEGRATION/02-event-system.md)