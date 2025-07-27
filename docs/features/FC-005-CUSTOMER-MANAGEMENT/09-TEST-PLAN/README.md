# 🧪 FC-005 ENTERPRISE TEST PLAN

**Navigation:** [← FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)

---

**Datum:** 27.07.2025  
**Version:** 1.0  
**Status:** Ready for Implementation  
**Zweck:** Detaillierter Test-Plan für Enterprise-Standard Test Coverage

## 🚨 PFLICHTLEKTÜRE VOR START

**[→ FLEXIBILITÄTS-PHILOSOPHIE LESEN](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/09-TEST-PLAN/00-PHILOSOPHIE.md)** ⚠️ KRITISCH!

Diese Philosophie MUSS verstanden werden bevor Tests geschrieben werden!

## 📋 Inhaltsverzeichnis

1. [Übersicht](#übersicht)
2. [Test-Architektur](#test-architektur)
3. [Code-Analyse für Tests](#code-analyse-für-tests)
4. [Test-Kategorien](#test-kategorien)
5. [Implementierungs-Reihenfolge](#implementierungs-reihenfolge)
6. [Test-Dateien Struktur](#test-dateien-struktur)
7. [Kritische Test-Pfade](#kritische-test-pfade)

## Übersicht

Dieser Plan stellt sicher, dass neue Test-Implementierungen:
- ✅ Den **tatsächlichen Code** testen (nicht hypothetischen)
- ✅ Die **field-basierte Architektur** respektieren
- ✅ **Enterprise Standards** erfüllen (>80% Coverage)
- ✅ **Claude-optimiert** sind (absolute Pfade, klare Verlinkungen)

## Test-Architektur

### Bestehende Code-Struktur zum Testen

```
/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/
├── components/
│   ├── fields/
│   │   ├── DynamicFieldRenderer.tsx ✅ (27 Tests vorhanden)
│   │   ├── FieldWrapper.tsx
│   │   └── fieldTypes/
│   │       ├── TextField.tsx
│   │       ├── SelectField.tsx
│   │       ├── MultiSelectField.tsx
│   │       └── [weitere Field Types]
│   ├── wizard/
│   │   └── CustomerOnboardingWizard.tsx
│   ├── steps/
│   │   ├── CustomerDataStep.tsx
│   │   ├── LocationsStep.tsx
│   │   └── DetailedLocationsStep.tsx
│   └── shared/
│       ├── StepProgress.tsx
│       ├── SaveIndicator.tsx
│       └── ErrorDisplay.tsx
├── stores/
│   └── customerOnboardingStore.ts
├── services/
│   ├── api-client.ts
│   └── fieldDefinitions.ts
├── hooks/
│   ├── useAutoSaveApi.ts
│   └── useFieldDefinitions.ts
├── types/
│   ├── customer.types.ts
│   ├── field.types.ts
│   └── location.types.ts
└── utils/
    ├── conditionEvaluator.ts
    ├── fieldCatalogLoader.ts
    └── validation.ts
```

## Code-Analyse für Tests

### 🔑 Kern-Komponenten die getestet werden müssen:

#### 1. **Store: customerOnboardingStore.ts**
- **Pfad:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/stores/customerOnboardingStore.ts`
- **Kritische Funktionen:**
  - `setCustomerField(fieldKey: string, value: any)`
  - `addLocation()`
  - `addDetailedLocation(locationId, detailedLocation)`
  - `validateCurrentStep()`
  - `saveAsDraft()`
  - `reset()`
- **Besonderheiten:** 
  - Nutzt `Record<string, any>` für flexible Fields
  - Persistiert in localStorage
  - Hat komplexe verschachtelte State-Struktur

#### 2. **API Client**
- **Pfad:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/services/api-client.ts`
- **Kritische Funktionen:**
  - `get<T>(endpoint: string)`
  - `post<T>(endpoint: string, data?: any)`
  - Retry-Logik
  - Error Handling
- **Besonderheiten:** 
  - Generischer Client mit `any` für flexible Payloads
  - Token-Management
  - Abort Controller Support

#### 3. **Wizard Component**
- **Pfad:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/wizard/CustomerOnboardingWizard.tsx`
- **Kritische Flows:**
  - Step Navigation
  - Conditional Rendering (chainCustomer)
  - Draft Auto-Save
  - Validation Integration

#### 4. **Field System**
- **DynamicFieldRenderer:** ✅ Bereits getestet
- **Field Types:** Müssen einzeln getestet werden
- **Condition Evaluator:** `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/utils/conditionEvaluator.ts`

## Test-Kategorien

### 1. **Unit Tests** (Vitest + React Testing Library)

#### A. Store Tests
```typescript
// Datei: tests/unit/stores/customerOnboardingStore.test.ts
// Tests für:
- State Initialization
- Field Updates mit any values
- Location Management
- Validation Logic
- Persistence to localStorage
- Reset Functionality
```

#### B. Utils Tests
```typescript
// Datei: tests/unit/utils/conditionEvaluator.test.ts
// Tests für:
- evaluateCondition mit verschiedenen Operatoren
- Edge Cases (null, undefined)
- Complex Conditions

// Datei: tests/unit/utils/validation.test.ts
// Tests für:
- Email Validation
- PLZ Validation (5 Ziffern)
- Required Field Validation
```

#### C. Component Tests
```typescript
// Bereits vorhanden:
// tests/unit/components/DynamicFieldRenderer.test.tsx ✅

// Neu zu erstellen:
// tests/unit/components/wizard/CustomerOnboardingWizard.test.tsx
// tests/unit/components/steps/CustomerDataStep.test.tsx
// tests/unit/components/fields/FieldWrapper.test.tsx
```

### 2. **Integration Tests** (Vitest + MSW)

#### A. Store + API Integration
```typescript
// Datei: tests/integration/storeApiIntegration.test.ts
// Testet:
- saveAsDraft() mit echtem API Call
- loadDraft() mit API Response
- Error Handling bei API Failures
- Retry Mechanismen
```

#### B. Field Definitions Loading
```typescript
// Datei: tests/integration/fieldDefinitionsIntegration.test.ts
// Testet:
- Laden von field-catalog.json
- Filterung nach industry
- Caching Mechanismen
- Fehlerbehandlung
```

#### C. Complete Wizard Flow
```typescript
// Datei: tests/integration/wizardFlowIntegration.test.ts
// Testet:
- Kompletter 3-Step Flow
- Conditional Rendering basierend auf chainCustomer
- Auto-Save während Navigation
- Validation zwischen Steps
```

### 3. **E2E Tests** (Playwright)

#### A. Happy Path
```typescript
// Datei: tests/e2e/customerCreationHappyPath.spec.ts
// Szenario:
1. Neuen Kunden anlegen (Einzelstandort)
2. Alle Pflichtfelder ausfüllen
3. Erfolgreich speichern
```

#### B. Chain Customer Flow
```typescript
// Datei: tests/e2e/chainCustomerFlow.spec.ts
// Szenario:
1. Ketten-Kunde wählen
2. 3 Standorte hinzufügen
3. Detaillierte Standorte für jeden
4. Speichern und verifizieren
```

#### C. Draft Recovery
```typescript
// Datei: tests/e2e/draftRecovery.spec.ts
// Szenario:
1. Kunde teilweise ausfüllen
2. Browser schließen
3. Wieder öffnen → Draft laden
4. Fortsetzen und abschließen
```

## Implementierungs-Reihenfolge

### Phase 1: Store & Utils (2h)
1. `customerOnboardingStore.test.ts` - Kern-State-Management
2. `conditionEvaluator.test.ts` - Field Visibility Logic
3. `validation.test.ts` - Input Validation

### Phase 2: API Integration (2h)
1. Mock Server Setup mit MSW
2. `api-client.test.ts` - Generic Client Tests
3. `storeApiIntegration.test.ts` - Store + API

### Phase 3: Component Integration (2h)
1. `CustomerOnboardingWizard.test.tsx` - Main Component
2. `CustomerDataStep.test.tsx` - Step 1
3. `fieldDefinitionsIntegration.test.ts` - Field Loading

### Phase 4: E2E Tests (2h)
1. Playwright Setup
2. Happy Path Test
3. Chain Customer Test
4. Draft Recovery Test

## Test-Dateien Struktur

```
/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/tests/
├── unit/
│   ├── components/
│   │   ├── DynamicFieldRenderer.test.tsx ✅ (existiert)
│   │   ├── wizard/
│   │   │   └── CustomerOnboardingWizard.test.tsx
│   │   ├── steps/
│   │   │   ├── CustomerDataStep.test.tsx
│   │   │   ├── LocationsStep.test.tsx
│   │   │   └── DetailedLocationsStep.test.tsx
│   │   └── fields/
│   │       └── FieldWrapper.test.tsx
│   ├── stores/
│   │   └── customerOnboardingStore.test.ts
│   ├── utils/
│   │   ├── conditionEvaluator.test.ts
│   │   ├── validation.test.ts
│   │   └── fieldCatalogLoader.test.ts
│   └── hooks/
│       └── useAutoSaveApi.test.ts
├── integration/
│   ├── storeApiIntegration.test.ts
│   ├── fieldDefinitionsIntegration.test.ts
│   ├── wizardFlowIntegration.test.ts
│   └── mockServer.ts
├── e2e/
│   ├── customerCreationHappyPath.spec.ts
│   ├── chainCustomerFlow.spec.ts
│   ├── draftRecovery.spec.ts
│   └── fixtures/
│       └── testData.ts
└── setup/
    ├── setupTests.ts ✅ (existiert)
    └── testUtils.tsx ✅ (existiert)
```

## Kritische Test-Pfade

### 🚨 WICHTIG für Claude bei der Implementierung:

1. **Store Tests MÜSSEN beachten:**
   - `customerData` ist `Record<string, any>` NICHT `Map`
   - `locations` ist Array mit IDs
   - `locationFieldValues` ist separates Object
   - `detailedLocations` ist flaches Array

2. **API Tests MÜSSEN beachten:**
   - Wir haben KEINEN `customerService`
   - API Client ist generisch mit `any` types
   - Endpoints beginnen mit `/api/`

3. **Field Tests MÜSSEN beachten:**
   - Fields nutzen `any` für values (by design!)
   - Field Catalog liegt in `data/field-catalog.json`
   - Conditional Rendering basiert auf `customerData` values

4. **Wizard Tests MÜSSEN beachten:**
   - 3 Steps: Customer → Locations → DetailedLocations
   - Step 2 nur wenn `chainCustomer === 'ja'`
   - Auto-Save mit debounce

## Test Implementation Guidelines

### Für jeden Test:

1. **LESE ZUERST den zu testenden Code:**
   ```typescript
   // Beispiel für Store Test:
   // 1. Lese: /Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/stores/customerOnboardingStore.ts
   // 2. Verstehe die ECHTE API
   // 3. Schreibe Tests die zur ECHTEN API passen
   ```

2. **Verwende die RICHTIGEN Imports:**
   ```typescript
   // RICHTIG:
   import { useCustomerOnboardingStore } from '../../../stores/customerOnboardingStore';
   
   // FALSCH:
   import { CustomerService } from '../../../services/customerService'; // EXISTIERT NICHT!
   ```

3. **Respektiere die Field-basierte Architektur:**
   ```typescript
   // RICHTIG:
   store.setCustomerField('companyName', 'Test GmbH');
   
   // FALSCH:
   store.setCustomer({ companyName: 'Test GmbH' }); // Diese Methode gibt es nicht!
   ```

## Coverage Ziele

- **Unit Tests:** > 80% Line Coverage
- **Integration Tests:** Alle kritischen User Journeys
- **E2E Tests:** Happy Path + wichtigste Edge Cases
- **Gesamt:** > 85% Coverage für Enterprise Standard

## Nächste Schritte

1. Diesen Plan reviewen
2. Mit Phase 1 (Store & Utils Tests) beginnen
3. Schrittweise durch alle Phasen arbeiten
4. Coverage Report nach jeder Phase

---

**Hinweis für Claude:** Dieser Plan basiert auf dem TATSÄCHLICHEN Code in `/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/`. Bitte IMMER den echten Code lesen bevor Tests geschrieben werden!