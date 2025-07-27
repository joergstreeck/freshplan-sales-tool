# Customer Management API Services

**Stand:** 26.07.2025  
**Status:** ✅ Implementiert  
**Modul:** FC-005 Customer Management - API Integration Layer

## 📋 Übersicht

Diese Service-Schicht bildet die Brücke zwischen Frontend und Backend für das Customer Management System. Sie implementiert:

- Type-safe API Client mit Error Handling
- Customer CRUD Operations mit Draft-System
- Field Definition Management mit Caching
- Location & Detailed Location Management
- React Query Integration für optimales State Management

## 🏗️ Architektur

### Service-Struktur
```
services/
├── api-client.ts        # Basis HTTP Client mit Retry & Error Handling
├── customerApi.ts       # Customer-spezifische API Endpoints
├── fieldDefinitionApi.ts # Field Definition Management mit Cache
├── locationApi.ts       # Location Management API
├── hooks.ts            # React Query Hooks
├── index.ts            # Zentrale Exports
└── README.md           # Diese Datei
```

### API Client Features
- **Automatic Retry**: Bei 5xx Errors mit exponential backoff
- **Request Cancellation**: Abort Controller für alle Requests
- **Timeout Handling**: Konfigurierbares Timeout pro Request
- **Auth Integration**: Automatisches Token Management
- **Error Standardization**: Einheitliche Error-Struktur

## 🔗 Verwendung

### 1. Customer Draft Management

```typescript
import { 
  useCreateCustomerDraft, 
  useUpdateCustomerDraft,
  useFinalizeCustomerDraft 
} from '@/features/customers/services';

// In Component
const { mutate: createDraft } = useCreateCustomerDraft({
  onSuccess: (draft) => {
    console.log('Draft created:', draft.id);
    navigate(`/customers/new/${draft.id}`);
  }
});

// Draft aktualisieren
const { mutate: updateDraft } = useUpdateCustomerDraft();

const handleFieldChange = (fieldValues: Record<string, any>) => {
  updateDraft({
    draftId: currentDraftId,
    data: { fieldValues }
  });
};

// Draft finalisieren
const { mutate: finalizeDraft } = useFinalizeCustomerDraft({
  onSuccess: (customer) => {
    toast.success('Kunde erfolgreich angelegt!');
    navigate(`/customers/${customer.id}`);
  }
});
```

### 2. Customer Search & Display

```typescript
import { useSearchCustomers, useCustomer } from '@/features/customers/services';

// Suche mit Pagination
const { data, isLoading } = useSearchCustomers({
  searchTerm: 'Fresh',
  status: CustomerStatus.ACTIVE,
  page: 0,
  size: 20
});

// Einzelnen Kunden laden
const { data: customer } = useCustomer(customerId);
```

### 3. Field Definitions mit Cache

```typescript
import { useFieldDefinitions } from '@/features/customers/services';

// Felder laden (gecached für 5 Minuten)
const { data: customerFields } = useFieldDefinitions(
  EntityType.CUSTOMER,
  'gastronomy' // Industry
);

// Cache manuell clearen
import { fieldDefinitionApi } from '@/features/customers/services';
fieldDefinitionApi.clearCache();
```

### 4. Location Management

```typescript
import { useCustomerLocations, useCreateLocation } from '@/features/customers/services';

// Standorte laden
const { data: locations } = useCustomerLocations(customerId);

// Neuen Standort anlegen
const { mutate: createLocation } = useCreateLocation({
  onSuccess: () => {
    queryClient.invalidateQueries(['locations', 'customer', customerId]);
  }
});
```

## 🔧 Konfiguration

### Environment Variables
```env
VITE_API_URL=http://localhost:8080  # Backend URL
```

### Request Defaults
```typescript
// Standard Retry: 2 Versuche bei 5xx Errors
// Standard Timeout: 30 Sekunden
// Kann pro Request überschrieben werden:

customerApi.searchCustomers(params, {
  retry: 3,
  timeout: 60000 // 1 Minute
});
```

## 📊 Error Handling

### Standardisierte Error-Struktur
```typescript
interface ApiError {
  status: number;              // HTTP Status Code
  code: string;               // Machine-readable code
  message: string;            // User-friendly message
  fieldErrors?: Record<string, string[]>; // Validation errors
  timestamp: string;
}
```

### Error Handling in Components
```typescript
const { mutate, error } = useUpdateCustomer();

if (error) {
  if (error.status === 422 && error.fieldErrors) {
    // Validation errors anzeigen
    Object.entries(error.fieldErrors).forEach(([field, errors]) => {
      setFieldError(field, errors[0]);
    });
  } else {
    // Generischer Error
    toast.error(error.message);
  }
}
```

## 🚀 Performance Optimierungen

### 1. Request Caching (React Query)
- Customer Details: 1 Minute
- Field Definitions: 5 Minuten
- Search Results: 1 Minute
- Statistics: 5 Minuten

### 2. Optimistic Updates
```typescript
const { mutate } = useUpdateCustomer({
  onMutate: async ({ customerId, fieldValues }) => {
    // Cancel in-flight queries
    await queryClient.cancelQueries(['customers', 'detail', customerId]);
    
    // Snapshot previous value
    const previous = queryClient.getQueryData(['customers', 'detail', customerId]);
    
    // Optimistically update
    queryClient.setQueryData(['customers', 'detail', customerId], old => ({
      ...old,
      fieldValues: { ...old.fieldValues, ...fieldValues }
    }));
    
    return { previous };
  },
  onError: (err, variables, context) => {
    // Rollback on error
    queryClient.setQueryData(
      ['customers', 'detail', variables.customerId],
      context.previous
    );
  }
});
```

### 3. Request Deduplication
React Query dedupliziert automatisch identische Requests innerhalb des staleTime-Fensters.

## 🧪 Testing

### Mock API für Tests
```typescript
import { rest } from 'msw';
import { setupServer } from 'msw/node';

const server = setupServer(
  rest.post('/api/customers/drafts', (req, res, ctx) => {
    return res(ctx.json({
      id: 'draft-123',
      status: 'DRAFT',
      fieldValues: {}
    }));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

## 📚 Weiterführende Dokumentation

- [API Contract Spezifikation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/03-rest-api.md)
- [Frontend Integration Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-api-integration.md)
- [State Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/02-state-management.md)
- [Field System](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/02-field-system.md)

## ✅ Status

- [x] API Client mit Error Handling
- [x] Customer API Service
- [x] Field Definition API mit Cache
- [x] Location API Service
- [x] React Query Hooks
- [x] TypeScript Types
- [x] Auto-Save Integration
- [ ] Unit Tests
- [ ] Integration Tests
- [ ] E2E Tests

**Nächste Schritte:**
1. Unit Tests für alle Services schreiben
2. MSW Mocks für Development einrichten
3. Performance Monitoring einbauen
4. Offline-Support mit React Query Persist