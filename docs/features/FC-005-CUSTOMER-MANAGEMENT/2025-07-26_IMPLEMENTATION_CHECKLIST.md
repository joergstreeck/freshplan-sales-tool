# ✅ FC-005 CUSTOMER MANAGEMENT - IMPLEMENTATION CHECKLIST

**Datum:** 26.07.2025  
**Version:** 1.0  
**Geschätzte Dauer:** 5 Tage (2 Backend, 3 Frontend)  
**Team:** 2 Entwickler optimal  

## 📋 Übersicht

Diese Checkliste führt Schritt für Schritt durch die Implementierung des Customer Management Systems mit Field-basierter Architektur.

**Wichtige Dokumente:**
- [Technisches Konzept](./2025-07-26_TECH_CONCEPT_customer-field-based-architecture.md)
- [Backend Architecture](./2025-07-26_BACKEND_ARCHITECTURE.md)
- [Frontend Architecture](./2025-07-26_FRONTEND_ARCHITECTURE.md)
- [Legacy Field Analysis](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-07-26/2025-07-26_CUSTOMER_COMPLETE_FIELD_ANALYSIS.md)

---

## 📅 Tag 1: Backend Foundation

### 🔲 1. Database Schema (1h)

- [ ] Flyway Migration erstellen: `V2.0.0__create_customer_field_tables.sql`
```sql
-- Location: backend/src/main/resources/db/migration/
-- Siehe: BACKEND_ARCHITECTURE.md -> Database Schema
```

- [ ] Indexes für Performance hinzufügen
- [ ] Test-Migration lokal ausführen
- [ ] Rollback-Script erstellen

### 🔲 2. Entities implementieren (2h)

**Location:** `backend/src/main/java/de/freshplan/domain/customer/entity/`

- [ ] `Customer.java` erweitern
  - [ ] Status enum hinzufügen
  - [ ] Audit fields
  - [ ] Relations zu Location/FieldValue

- [ ] `Location.java` erstellen
  - [ ] Base fields
  - [ ] Relation zu Customer
  - [ ] Relation zu DetailedLocation

- [ ] `DetailedLocation.java` erstellen

- [ ] `FieldValue.java` erstellen
  - [ ] JSONB Converter implementieren
  - [ ] Index annotations

- [ ] `FieldDefinition.java` erstellen
  - [ ] Als Configuration Entity
  - [ ] Caching annotations

### 🔲 3. Repositories (1h)

**Location:** `backend/src/main/java/de/freshplan/domain/customer/repository/`

- [ ] `CustomerRepository.java` erweitern
  - [ ] `findDrafts()` Methode
  - [ ] `cleanupOldDrafts()` Methode
  - [ ] Optimierte Such-Queries

- [ ] `LocationRepository.java` erstellen
  - [ ] Basic CRUD
  - [ ] `findByCustomer()` mit Pagination

- [ ] `FieldValueRepository.java` erstellen
  - [ ] `findByEntity()` Methode
  - [ ] `findByEntityAndField()` Methode
  - [ ] Batch operations

- [ ] `FieldDefinitionRepository.java` erstellen
  - [ ] Cache-aware Methoden
  - [ ] Industry-Filter

### 🔲 4. Service Layer (2h)

**Location:** `backend/src/main/java/de/freshplan/domain/customer/service/`

- [ ] `CustomerService.java` erweitern
  - [ ] `createDraft()` implementieren
  - [ ] `updateDraft()` implementieren  
  - [ ] `finalizeDraft()` mit Validierung
  - [ ] Event publishing

- [ ] `FieldValueService.java` erstellen
  - [ ] Field validation
  - [ ] Batch operations
  - [ ] Value transformation

- [ ] `FieldDefinitionService.java` erstellen
  - [ ] Field catalog loading
  - [ ] Industry-specific filtering
  - [ ] Caching logic

- [ ] `ValidationService.java` erstellen
  - [ ] Zod-like validation
  - [ ] Custom validators
  - [ ] Error messages

### 🔲 5. REST Resources (2h)

**Location:** `backend/src/main/java/de/freshplan/api/resources/`

- [ ] `CustomerResource.java` erweitern
  - [ ] Draft endpoints
  - [ ] Field value endpoints
  - [ ] Search mit Pagination
  - [ ] Swagger annotations

- [ ] `FieldDefinitionResource.java` erstellen
  - [ ] GET endpoints mit Filtering
  - [ ] Admin endpoints (später)
  - [ ] Caching headers

- [ ] DTOs erstellen
  - [ ] `CreateCustomerDraftRequest`
  - [ ] `UpdateCustomerDraftRequest`
  - [ ] `CustomerDraftResponse`
  - [ ] `FieldValueRequest`

### 🔲 6. Tests & Documentation (1h)

- [ ] Unit Tests für Services
- [ ] Integration Tests für Repositories
- [ ] REST Assured Tests für Endpoints
- [ ] Swagger/OpenAPI Dokumentation

---

## 📅 Tag 2: Backend Completion & Integration

### 🔲 7. Field Catalog Seed Data (1h)

**Location:** `backend/src/main/resources/`

- [ ] `field-definitions.json` erstellen mit 10 MVP Feldern
```json
{
  "customer": [
    { "key": "companyName", "label": "Firmenname", ... },
    // Siehe: TECH_CONCEPT -> Initial Field Catalog
  ]
}
```

- [ ] `FieldDefinitionSeeder.java` implementieren
- [ ] Industry-specific fields definieren
- [ ] Test seed data

### 🔲 8. Integration Points (2h)

- [ ] Audit Trail Integration
  - [ ] AuditService injection
  - [ ] Critical field tracking
  - [ ] Change events

- [ ] Security Integration  
  - [ ] Permission checks
  - [ ] Field-level security
  - [ ] Role-based filtering

- [ ] Event System
  - [ ] Domain Events definieren
  - [ ] Event publishers
  - [ ] Test event flow

### 🔲 9. Performance Optimization (2h)

- [ ] Query optimization
  - [ ] N+1 prevention
  - [ ] Batch loading
  - [ ] Projection queries

- [ ] Caching setup
  - [ ] Caffeine configuration
  - [ ] Redis integration (optional)
  - [ ] Cache invalidation

- [ ] Database indexes verifizieren

### 🔲 10. Backend Testing & Polish (3h)

- [ ] Vollständige Test-Suite
  - [ ] 80%+ Coverage
  - [ ] Edge cases
  - [ ] Error scenarios

- [ ] Performance Tests
  - [ ] Load test data generation
  - [ ] JMeter test plan
  - [ ] Baseline measurements

- [ ] Documentation
  - [ ] API documentation
  - [ ] Postman collection
  - [ ] README updates

---

## 📅 Tag 3: Frontend Foundation

### 🔲 11. Project Setup (1h)

**Base:** Already on `feature/customer-field-based-ui` branch

- [ ] Folder-Struktur erstellen
```bash
cd frontend/src/features/customers
mkdir -p {components,hooks,stores,services,types,utils,schemas,data}
```

- [ ] Dependencies prüfen
  - [ ] Zustand
  - [ ] React Query
  - [ ] React Hook Form
  - [ ] Zod
  - [ ] Material-UI

### 🔲 12. Type Definitions (1h)

**Location:** `frontend/src/features/customers/types/`

- [ ] `customer.types.ts`
  - [ ] Customer interfaces
  - [ ] Location types
  - [ ] Status enums

- [ ] `field.types.ts`
  - [ ] FieldDefinition
  - [ ] FieldValue
  - [ ] FieldValidation

- [ ] `api.types.ts`
  - [ ] Request/Response types
  - [ ] Pagination types
  - [ ] Error types

### 🔲 13. Field Catalog & Data (1h)

**Location:** `frontend/src/features/customers/data/`

- [ ] `fieldCatalog.json`
  - [ ] Copy from backend
  - [ ] Add UI-specific properties
  - [ ] Validation rules

- [ ] `industryConfig.ts`
  - [ ] Industry-specific fields
  - [ ] Icons and labels
  - [ ] Default values

### 🔲 14. Zustand Store (2h)

**Location:** `frontend/src/features/customers/stores/`

- [ ] `customerOnboardingStore.ts`
  - [ ] State structure
  - [ ] Actions
  - [ ] Computed values
  - [ ] Persist middleware

- [ ] Store Tests
  - [ ] State updates
  - [ ] Trigger logic
  - [ ] Persistence

### 🔲 15. API Services (2h)

**Location:** `frontend/src/features/customers/services/`

- [ ] `customerApi.ts`
  - [ ] Draft endpoints
  - [ ] Search functionality
  - [ ] Field value updates

- [ ] `fieldDefinitionApi.ts`
  - [ ] Load definitions
  - [ ] Caching strategy

- [ ] React Query hooks
  - [ ] `useCustomerDraft`
  - [ ] `useFieldDefinitions`
  - [ ] `useSaveCustomer`

### 🔲 16. Validation Framework (1h)

**Location:** `frontend/src/features/customers/schemas/`

- [ ] `validationSchemas.ts`
  - [ ] Base schemas
  - [ ] German validators
  - [ ] Dynamic schema builder

- [ ] `fieldValidation.ts`
  - [ ] Validation service
  - [ ] Error formatting
  - [ ] Async validation

---

## 📅 Tag 4: Frontend Components

### 🔲 17. Base Components (3h)

**Location:** `frontend/src/features/customers/components/`

- [ ] `wizard/CustomerOnboardingWizard.tsx`
  - [ ] Stepper integration
  - [ ] Step management
  - [ ] Navigation logic
  - [ ] Save indicator

- [ ] `wizard/WizardNavigation.tsx`
  - [ ] Back/Next buttons
  - [ ] Progress indication
  - [ ] Validation before proceed

- [ ] `shared/SaveIndicator.tsx`
  - [ ] Auto-save status
  - [ ] Last saved time
  - [ ] Sync status

### 🔲 18. Field Components (3h)

**Location:** `frontend/src/features/customers/components/fields/`

- [ ] `DynamicFieldRenderer.tsx`
  - [ ] Field type detection
  - [ ] Layout management
  - [ ] Error display

- [ ] `FieldWrapper.tsx`
  - [ ] Label, help text
  - [ ] Required indicator
  - [ ] Error state

- [ ] Field type components
  - [ ] `TextField.tsx`
  - [ ] `NumberField.tsx`
  - [ ] `SelectField.tsx`
  - [ ] `DateField.tsx`
  - [ ] `BooleanField.tsx`

### 🔲 19. Step Components (2h)

**Location:** `frontend/src/features/customers/components/steps/`

- [ ] `CustomerDataStep.tsx`
  - [ ] Load customer fields
  - [ ] Render form
  - [ ] Handle triggers
  - [ ] Validation

- [ ] Base for other steps
  - [ ] Common structure
  - [ ] Shared hooks
  - [ ] Error handling

---

## 📅 Tag 5: Completion & Testing

### 🔲 20. Additional Steps (3h)

- [ ] `LocationsStep.tsx`
  - [ ] Industry-specific fields
  - [ ] Dynamic sections
  - [ ] Total calculations
  - [ ] Detailed locations toggle

- [ ] `DetailedLocationsStep.tsx`
  - [ ] Location cards
  - [ ] Add/Remove logic
  - [ ] Sync warning
  - [ ] Validation

### 🔲 21. Hooks & Utilities (2h)

**Location:** `frontend/src/features/customers/hooks/`

- [ ] `useCustomerOnboarding.ts`
- [ ] `useFieldDefinitions.ts`
- [ ] `useAutoSave.ts`
- [ ] `useIndustryDefaults.ts`
- [ ] `useFieldSecurity.ts`

**Location:** `frontend/src/features/customers/utils/`

- [ ] `fieldFormatters.ts`
- [ ] `industryHelpers.ts`
- [ ] `validationHelpers.ts`

### 🔲 22. Testing (2h)

- [ ] Component Tests
  - [ ] DynamicFieldRenderer
  - [ ] Wizard flow
  - [ ] Field components

- [ ] Store Tests
  - [ ] State management
  - [ ] Persistence
  - [ ] Computed values

- [ ] Integration Tests
  - [ ] API mocking
  - [ ] Full flow
  - [ ] Error scenarios

### 🔲 23. E2E Test (1h)

- [ ] Playwright test erstellen
  - [ ] Complete flow
  - [ ] Draft recovery
  - [ ] Validation
  - [ ] Chain customer logic

---

## 🚀 Post-Implementation

### 🔲 Performance Verification

- [ ] Lighthouse CI check
- [ ] Bundle size analysis
- [ ] API response times
- [ ] Memory profiling

### 🔲 Security Audit

- [ ] OWASP checklist
- [ ] Permission tests
- [ ] Input validation
- [ ] XSS prevention

### 🔲 Documentation

- [ ] Update API docs
- [ ] User guide
- [ ] Admin guide
- [ ] Troubleshooting

### 🔲 Deployment Preparation

- [ ] Environment variables
- [ ] Feature flags
- [ ] Migration plan
- [ ] Rollback strategy

---

## 📊 Definition of Done

Eine Aufgabe gilt als abgeschlossen wenn:

1. ✅ Code implementiert und funktioniert
2. ✅ Unit Tests geschrieben (>80% Coverage)
3. ✅ Integration Tests vorhanden
4. ✅ Code Review durchgeführt
5. ✅ Dokumentation aktualisiert
6. ✅ Keine Linting/Security Warnings
7. ✅ Performance innerhalb der Ziele
8. ✅ Accessibility geprüft (Frontend)

---

## 🎯 Erfolgs-Metriken

- **API Response Time:** < 200ms P95
- **Frontend Bundle Size:** < 50KB für Customer Module
- **Test Coverage:** > 80% Lines
- **Lighthouse Score:** > 90
- **Zero Security Warnings**

---

## 🚨 Wichtige Hinweise

1. **Field System First:** Nicht mit UI beginnen bevor Field System steht
2. **Test Data:** Früh Testdaten generieren für realistische Tests
3. **Performance:** Von Anfang an auf Indices und Queries achten
4. **Security:** Jedes Feld auf Sensitivity prüfen
5. **Backwards Compatibility:** API versioning beachten

---

## 🤝 Team-Koordination

**Optimal: 2 Entwickler**
- Developer 1: Backend (Tag 1-2)
- Developer 2: Frontend Setup parallel beginnen
- Gemeinsam: Integration & Testing (Tag 4-5)

**Daily Sync Points:**
- 09:00 - Status & Blocker
- 14:00 - API Contract Review
- 17:00 - Progress Check

---

**Bei Fragen:** Technische Konzepte und Architecture-Dokumente konsultieren!