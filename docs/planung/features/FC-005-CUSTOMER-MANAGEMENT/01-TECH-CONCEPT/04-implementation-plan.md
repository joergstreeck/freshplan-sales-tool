---
Navigation: [⬅️ Previous](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/03-data-model.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)
Parent: [📁 Tech Concept](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/README.md)
Related: [🔗 Implementation Checklist](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md) | [🔗 Test Strategy](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)
---

# 📅 FC-005 IMPLEMENTATION PLAN

**Modul:** FC-005 Customer Management  
**Dauer:** 5 Tage  
**Team:** 2 Entwickler  
**Start:** TBD  

## 📋 5-Tage Sprint Plan

### 🗓️ Tag 1: Backend Foundation & Field System

**Ziel:** Field System und Customer Entities implementieren

**Backend Developer:**
```bash
# Arbeitsverzeichnis
cd /Users/joergstreeck/freshplan-sales-tool/backend/src/main/java/de/freshplan/domain/customer

# Entities erstellen
├── entity/
│   ├── Customer.java
│   ├── Location.java
│   ├── DetailedLocation.java
│   └── FieldValue.java
├── repository/
│   ├── CustomerRepository.java
│   └── FieldValueRepository.java
└── service/
    └── FieldDefinitionService.java
```

**Frontend Developer:**
```bash
# Field Catalog erstellen
cd /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers

# Struktur aufbauen
├── data/
│   └── fieldCatalog.json          # MVP Field Definitions
├── types/
│   ├── customer.types.ts
│   └── field.types.ts
└── hooks/
    └── useFieldDefinitions.ts
```

**Deliverables Tag 1:**
- ✅ JPA Entities mit Flyway Migration
- ✅ Field Catalog JSON (10 MVP Felder)
- ✅ TypeScript Types
- ✅ Unit Tests für Entities

### 🗓️ Tag 2: Persistence & State Management

**Ziel:** Draft-System und Frontend Store

**Backend Developer:**
```java
// CustomerService.java
@POST @Path("/draft")
public Response createDraft(CreateCustomerDraftRequest request) {
    // Implementation
}

@PUT @Path("/draft/{id}")
public Response updateDraft(@PathParam("id") UUID id, UpdateDraftRequest request) {
    // Auto-save implementation
}

// FieldValueService.java
public void updateFieldValues(UUID entityId, EntityType type, List<FieldValueRequest> values) {
    // Bulk update logic
}
```

**Frontend Developer:**
```typescript
// stores/customerOnboardingStore.ts
export const useCustomerOnboardingStore = create<CustomerOnboardingStore>()(
  persist(
    (set, get) => ({
      // State
      currentStep: 0,
      customerData: new Map(),
      locations: [],
      
      // Actions
      setFieldValue: (key, value) => {
        set(state => ({
          customerData: new Map(state.customerData).set(key, value),
          isDirty: true
        }));
      },
      
      saveAsDraft: async () => {
        const response = await customerApi.saveDraft(get().serialize());
        set({ draftId: response.id, isDirty: false });
      }
    }),
    {
      name: 'customer-onboarding',
      storage: createJSONStorage(() => sessionStorage)
    }
  )
);
```

**Deliverables Tag 2:**
- ✅ Draft REST Endpoints
- ✅ Zustand Store mit Persist
- ✅ Auto-Save Logic (30s interval)
- ✅ Integration Tests

### 🗓️ Tag 3: Frontend Components & UI

**Ziel:** Wizard UI mit Dynamic Field Rendering

**Frontend Developer (Full Day):**
```bash
# Component Struktur
components/
├── CustomerOnboardingWizard.tsx    # Main Container
├── steps/
│   ├── CustomerDataStep.tsx        # Step 1
│   ├── LocationsStep.tsx           # Step 2
│   └── DetailedLocationsStep.tsx   # Step 3
├── fields/
│   ├── DynamicFieldRenderer.tsx    # Field Factory
│   ├── TextField.tsx
│   ├── SelectField.tsx
│   └── NumberField.tsx
└── common/
    ├── WizardNavigation.tsx
    └── SaveIndicator.tsx
```

**Key Components:**
```typescript
// DynamicFieldRenderer.tsx
export const DynamicFieldRenderer: FC<Props> = ({ 
  fields, 
  values, 
  errors, 
  onChange 
}) => {
  return (
    <Grid container spacing={2}>
      {fields.map(field => (
        <Grid item xs={12} sm={field.width || 6} key={field.key}>
          {renderField(field, values.get(field.key), errors.get(field.key))}
        </Grid>
      ))}
    </Grid>
  );
};
```

**Deliverables Tag 3:**
- ✅ Wizard Shell mit Navigation
- ✅ Customer Data Step komplett
- ✅ Dynamic Field Rendering
- ✅ Responsive Design
- ✅ Storybook Stories

### 🗓️ Tag 4: Integration & Advanced Features

**Ziel:** Backend-Frontend Integration, Location Management

**Beide Developer:**

**Morning - API Integration:**
```typescript
// services/customerApi.ts
export const customerApi = {
  getFieldDefinitions: (entityType: EntityType, industry?: string) =>
    api.get(`/field-definitions/${entityType}`, { params: { industry } }),
    
  createDraft: (data: CustomerDraftData) =>
    api.post('/customers/draft', data),
    
  finalizeDraft: (id: string) =>
    api.post(`/customers/draft/${id}/finalize`)
};
```

**Afternoon - Location Features:**
```typescript
// LocationsStep.tsx
const LocationsStep: FC = () => {
  const { industry, locations, addLocation } = useCustomerOnboardingStore();
  const { data: locationFields } = useFieldDefinitions('location', industry);
  
  return (
    <>
      {locations.map((location, index) => (
        <LocationCard 
          key={location.id}
          location={location}
          fields={locationFields}
          onUpdate={(data) => updateLocation(index, data)}
        />
      ))}
      <Button onClick={addLocation}>Standort hinzufügen</Button>
    </>
  );
};
```

**Deliverables Tag 4:**
- ✅ Full API Integration
- ✅ Location Management UI
- ✅ Conditional Field Logic
- ✅ Error Handling
- ✅ Loading States

### 🗓️ Tag 5: Testing & Polish

**Ziel:** Comprehensive Testing & Production Readiness

**Beide Developer:**

**Test Coverage:**
```bash
# Backend Tests
./mvnw test -Dtest="CustomerServiceTest"      # Unit
./mvnw test -Dtest="CustomerResourceIT"       # Integration

# Frontend Tests
npm test -- CustomerOnboardingWizard          # Unit
npm run test:e2e -- customer-onboarding       # E2E mit Playwright
```

**Performance Optimization:**
- Field Definition Caching
- Debounced Auto-Save
- Lazy Loading für Locations
- Bundle Size Check

**Production Checklist:**
- [ ] Security Review (DSGVO compliance)
- [ ] Performance Metrics Dashboard
- [ ] Feature Flags configured
- [ ] Monitoring/Alerts setup
- [ ] Documentation complete

**Deliverables Tag 5:**
- ✅ 80%+ Test Coverage
- ✅ E2E Test Suite
- ✅ Performance < 200ms
- ✅ Production Deployment Guide
- ✅ User Documentation

## 📋 Rollout Strategy

### Phase 1: Internal Testing (1 Woche)
- Deploy to Staging
- Internal Team Testing
- Feedback Collection

### Phase 2: Pilot Users (2 Wochen)
- 3-5 Selected Customers
- Feature Flag Control
- Daily Monitoring

### Phase 3: General Availability
- Progressive Rollout (10% → 50% → 100%)
- Support Team Training
- Migration Guide for existing customers

## 📋 Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Wizard Completion Time | < 3 min | Analytics |
| Draft Recovery Success | 100% | Error Logs |
| Field Validation Errors | < 5% | Form Analytics |
| API Response Time | < 200ms p95 | Monitoring |
| User Satisfaction | > 4.5/5 | Survey |

## 🚨 Risk Mitigation

### Technical Risks
1. **Performance with many fields**
   - Pre-emptive: Pagination, Virtual Scrolling
   
2. **Complex validation rules**
   - Pre-emptive: Server-side validation fallback

### Business Risks
1. **User adoption**
   - Pre-emptive: Video tutorials, tooltips
   
2. **Data quality**
   - Pre-emptive: Import validation, cleaning tools

## 📚 Weiterführende Links

- [Day 1 Backend Tasks →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/01-day-1-backend.md)
- [Frontend Components →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md)
- [Test Strategy →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)