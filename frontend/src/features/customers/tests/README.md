# 🧪 FC-005 Customer Management - Test Suite

**Navigation:**

- **Parent:** [FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
- **Related:** [Test Strategy](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TEST-STRATEGY/README.md)

## 📋 Test-Struktur

```
tests/
├── integration/          # Integration Tests
│   ├── api/             # API Service Tests
│   │   ├── customerApi.test.ts
│   │   ├── fieldDefinitionApi.test.ts
│   │   └── locationApi.test.ts
│   ├── store/           # Store Integration Tests
│   │   ├── customerOnboardingStore.test.ts
│   │   └── storeApiIntegration.test.ts
│   └── validation/      # Validation Integration Tests
│       ├── formValidation.test.ts
│       └── schemaBuilder.test.ts
├── e2e/                 # E2E Tests (Playwright)
│   ├── customerOnboarding.spec.ts
│   ├── fieldCatalog.spec.ts
│   └── fixtures/
│       └── testData.ts
├── unit/                # Unit Tests (existierend)
│   ├── components/
│   ├── hooks/
│   └── utils/
└── setup/               # Test Setup & Mocks
    ├── mockServer.ts
    ├── testUtils.tsx
    └── setupTests.ts
```

## 🎯 Test-Strategie

### 1. **Unit Tests** (Vitest + React Testing Library)

- Component Tests (DynamicFieldRenderer, etc.)
- Hook Tests (useAutoSaveApi, etc.)
- Utility Function Tests
- Isolierte Business Logic

### 2. **Integration Tests** (Vitest + MSW)

- API Services mit Mock Service Worker
- Store Integration mit API
- Validation mit echten Schemas
- Component Integration

### 3. **E2E Tests** (Playwright)

- Complete Customer Onboarding Flow
- Field Catalog Funktionalität
- Multi-Step Form Navigation
- Data Persistence

### 4. **Performance Tests**

- Bundle Size Monitoring
- Render Performance
- Memory Leaks
- API Response Times

### 5. **Accessibility Tests** (jest-axe)

- WCAG 2.1 AA Compliance
- Keyboard Navigation
- Screen Reader Support
- Color Contrast

## 🔧 Test-Tools

- **Vitest:** Unit & Integration Tests
- **MSW (Mock Service Worker):** API Mocking
- **React Testing Library:** Component Tests
- **Playwright:** E2E Tests
- **React Query Test Utils:** Query Testing

## 📊 Coverage-Ziele

- **Unit Tests:** > 80% Lines
- **Integration Tests:** Alle kritischen Pfade
- **E2E Tests:** Happy Path + Edge Cases
- **Performance:** < 200ms API, < 50KB Bundle
- **Accessibility:** WCAG 2.1 AA Standard

## 📋 Implementierte Tests

### ✅ Setup & Infrastructure

- `setupTests.ts` - Globale Test-Konfiguration
- `mockServer.ts` - MSW Mock Server für alle APIs
- `testUtils.tsx` - Render Functions & Test Factories

### ✅ Integration Tests

- `customerApi.test.ts` - Customer API Service (Draft, CRUD, Search)
- `fieldDefinitionApi.test.ts` - Field Definition API mit Caching
- `customerOnboardingStore.test.ts` - Zustand Store mit Persistence
- `formValidation.test.ts` - Validation System Integration

### ✅ Unit Tests

- `DynamicFieldRenderer.test.tsx` - Field Rendering Logic
- `useAutoSaveApi.test.ts` - Auto-Save Hook mit Debouncing

### ✅ E2E Tests

- `customerOnboarding.spec.ts` - Complete Wizard Flow
- `fixtures/testData.ts` - Umfangreiche Testdaten

### ✅ Performance Tests

- `bundleSize.test.ts` - Bundle Size Analysis & Limits
- `renderPerformance.test.tsx` - Component Render Speed

### ✅ Accessibility Tests

- `a11y.test.tsx` - WCAG Compliance & Screen Reader Support

## 🚀 Quick Commands

```bash
# Run all tests
npm test

# Run integration tests
npm run test:integration

# Run E2E tests
npm run test:e2e

# Run with coverage
npm run test:coverage

# Run specific test file
npm test customerApi.test.ts

# Run in watch mode
npm run test:watch
```

## 📝 Test-Patterns

### API Service Test Pattern

```typescript
describe('CustomerApi', () => {
  beforeAll(() => server.listen());
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  it('should create draft customer', async () => {
    const result = await customerApi.createDraft();
    expect(result.draftId).toBeDefined();
  });
});
```

### Store Integration Pattern

```typescript
describe('Store API Integration', () => {
  it('should sync with backend', async () => {
    const store = useCustomerOnboardingStore.getState();
    await store.saveDraft();
    expect(store.draftId).toBeDefined();
  });
});
```

### E2E Test Pattern

```typescript
test('complete customer onboarding', async ({ page }) => {
  await page.goto('/customers/new');
  // Step 1: Customer Data
  await page.fill('[name="customerNumber"]', '12345');
  // Continue through wizard...
});
```

---

**Stand:** 26.07.2025  
**Status:** Test Implementation in Progress
