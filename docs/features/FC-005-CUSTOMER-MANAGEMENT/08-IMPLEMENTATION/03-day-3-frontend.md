# 📅 TAG 3: FRONTEND FOUNDATION

**Navigation:**
- **Parent:** [Implementation Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)
- **Previous:** [Tag 2: Backend Completion](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/02-day-2-persistence.md)
- **Next:** [Tag 4: Frontend Components](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/04-day-4-integration.md)
- **Related:** [Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)

## 🔲 11. Project Setup (1h)

**Base:** Already on `feature/customer-field-based-ui` branch

- [ ] Folder-Struktur erstellen
```bash
cd frontend/src/features/customers
mkdir -p {components,hooks,stores,services,types,utils,schemas,data}
```

- [ ] Dependencies prüfen
  - [ ] Zustand (v4+)
  - [ ] React Query (v5+)
  - [ ] React Hook Form (v7+)
  - [ ] Zod (v3+)
  - [ ] Material-UI (v5+)

## 🔲 12. Type Definitions (1h)

**Location:** `frontend/src/features/customers/types/`

- [ ] `customer.types.ts`
```typescript
export enum CustomerStatus {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DELETED = 'DELETED'
}

export interface Customer {
  id: string;
  status: CustomerStatus;
  createdAt: string;
  createdBy: string;
  finalizedAt?: string;
  finalizedBy?: string;
}

export interface Location {
  id: string;
  customerId: string;
  locationType: 'LIEFERADRESSE' | 'RECHNUNGSADRESSE';
  name: string;
  // weitere Felder...
}
```

- [ ] `field.types.ts`
```typescript
export interface FieldDefinition {
  key: string;
  label: string;
  type: FieldType;
  required?: boolean;
  options?: SelectOption[];
  validation?: FieldValidation;
  trigger?: string;
  sensitive?: boolean;
}

export interface FieldValue {
  id: string;
  fieldDefinitionId: string;
  entityId: string;
  entityType: EntityType;
  value: any;
  lastUpdated: string;
}
```

- [ ] `api.types.ts`
```typescript
export interface CreateCustomerDraftRequest {
  // leer, nur User aus Context
}

export interface UpdateCustomerDraftRequest {
  fieldValues: Record<string, any>;
}

export interface CustomerDraftResponse {
  id: string;
  status: CustomerStatus;
  fieldValues: Record<string, any>;
  lastSaved?: string;
}
```

## 🔲 13. Field Catalog & Data (1h)

**Location:** `frontend/src/features/customers/data/`

- [ ] `fieldCatalog.json`
```json
{
  "customer": [
    {
      "key": "companyName",
      "label": "Firmenname",
      "type": "text",
      "required": true,
      "validation": {
        "maxLength": 100,
        "pattern": "^[\\w\\s\\-\\.äöüÄÖÜß]+$"
      }
    },
    {
      "key": "industry",
      "label": "Branche",
      "type": "select",
      "required": true,
      "options": [
        { "value": "hotel", "label": "Hotellerie" },
        { "value": "eventCatering", "label": "Event-Catering" }
      ]
    }
    // weitere Felder...
  ]
}
```

- [ ] `industryConfig.ts`
```typescript
export const industryConfig: Record<string, IndustryConfig> = {
  hotel: {
    label: 'Hotellerie',
    icon: 'Hotel',
    specificFields: ['stars', 'roomCount', 'hasRestaurant'],
    defaultValues: {
      serviceType: 'breakfast'
    }
  },
  eventCatering: {
    label: 'Event-Catering',
    icon: 'Event',
    specificFields: ['eventTypes', 'averageGuests'],
    defaultValues: {
      serviceType: 'fullService'
    }
  }
};
```

## 🔲 14. Zustand Store (2h)

**Location:** `frontend/src/features/customers/stores/`

- [ ] `customerOnboardingStore.ts`
```typescript
interface CustomerOnboardingState {
  // Draft data
  draftId: string | null;
  customerId: string | null;
  status: CustomerStatus;
  
  // Form data
  customerData: Record<string, any>;
  locations: Location[];
  detailedLocations: DetailedLocation[];
  
  // UI state
  currentStep: number;
  isDirty: boolean;
  lastSaved: Date | null;
  isSaving: boolean;
  
  // Actions
  createDraft: () => Promise<void>;
  updateField: (key: string, value: any) => void;
  addLocation: (location: Partial<Location>) => void;
  removeLocation: (id: string) => void;
  saveProgress: () => Promise<void>;
  finalizeCustomer: () => Promise<void>;
  reset: () => void;
}

export const useCustomerOnboardingStore = create<CustomerOnboardingState>()(
  persist(
    devtools(
      (set, get) => ({
        // Initial state
        draftId: null,
        customerId: null,
        status: CustomerStatus.DRAFT,
        customerData: {},
        locations: [],
        detailedLocations: [],
        currentStep: 0,
        isDirty: false,
        lastSaved: null,
        isSaving: false,
        
        // Actions implementation...
      })
    ),
    {
      name: 'customer-onboarding',
      partialize: (state) => ({
        draftId: state.draftId,
        customerData: state.customerData,
        locations: state.locations,
        currentStep: state.currentStep
      })
    }
  )
);
```

- [ ] Store Tests schreiben
- [ ] Trigger-Logic implementieren
- [ ] Persistence verifizieren

## 🔲 15. API Services (2h)

**Location:** `frontend/src/features/customers/services/`

- [ ] `customerApi.ts`
```typescript
class CustomerApi {
  async createDraft(): Promise<CustomerDraftResponse> {
    return api.post('/api/customers/drafts');
  }
  
  async updateDraft(id: string, data: UpdateCustomerDraftRequest) {
    return api.put(`/api/customers/drafts/${id}`, data);
  }
  
  async finalizeDraft(id: string): Promise<Customer> {
    return api.post(`/api/customers/drafts/${id}/finalize`);
  }
  
  async searchCustomers(params: SearchParams): Promise<PagedResponse<Customer>> {
    return api.get('/api/customers', { params });
  }
}

export const customerApi = new CustomerApi();
```

- [ ] `fieldDefinitionApi.ts`
```typescript
class FieldDefinitionApi {
  private cache = new Map<string, FieldDefinition[]>();
  
  async getFieldDefinitions(
    entityType: EntityType,
    industry?: string
  ): Promise<FieldDefinition[]> {
    const cacheKey = `${entityType}-${industry || 'all'}`;
    
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)!;
    }
    
    const definitions = await api.get('/api/field-definitions', {
      params: { entityType, industry }
    });
    
    this.cache.set(cacheKey, definitions);
    return definitions;
  }
}
```

- [ ] React Query hooks erstellen
```typescript
export const useCustomerDraft = (draftId: string | null) => {
  return useQuery({
    queryKey: ['customer', 'draft', draftId],
    queryFn: () => customerApi.getDraft(draftId!),
    enabled: !!draftId,
    staleTime: 30000 // 30 seconds
  });
};

export const useSaveCustomerDraft = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateCustomerDraftRequest }) =>
      customerApi.updateDraft(id, data),
    onSuccess: (data, variables) => {
      queryClient.setQueryData(['customer', 'draft', variables.id], data);
    }
  });
};
```

## 🔲 16. Validation Framework (1h)

**Location:** `frontend/src/features/customers/schemas/`

- [ ] `validationSchemas.ts`
```typescript
// German validators
const germanPostalCode = z.string().regex(/^\d{5}$/, 'PLZ muss 5 Ziffern haben');
const germanPhone = z.string().regex(/^[\d\s\-\+\(\)\/]+$/, 'Ungültige Telefonnummer');
const email = z.string().email('Ungültige E-Mail-Adresse');

// Base schemas
export const customerBaseSchema = z.object({
  companyName: z.string().min(1, 'Firmenname ist erforderlich').max(100),
  industry: z.enum(['hotel', 'eventCatering', 'businessCatering', 'socialCatering', 'retail']),
  street: z.string().min(1, 'Straße ist erforderlich'),
  postalCode: germanPostalCode,
  city: z.string().min(1, 'Ort ist erforderlich'),
  email: email.optional(),
  phone: germanPhone.optional()
});
```

- [ ] `fieldValidation.ts`
```typescript
export class FieldValidationService {
  buildSchemaForFields(fields: FieldDefinition[]): z.ZodSchema {
    const shape: Record<string, z.ZodTypeAny> = {};
    
    fields.forEach(field => {
      let schema = this.getBaseSchemaForType(field.type);
      
      if (field.required) {
        schema = schema.min(1, `${field.label} ist erforderlich`);
      }
      
      if (field.validation) {
        schema = this.applyValidationRules(schema, field.validation);
      }
      
      shape[field.key] = field.required ? schema : schema.optional();
    });
    
    return z.object(shape);
  }
}
```

## ✅ Checklist Ende Tag 3

- [ ] Type System vollständig
- [ ] Store funktioniert mit Persistence
- [ ] API Services implementiert
- [ ] Validation Framework ready
- [ ] Erste Integration Tests

---

**Next:** [Tag 4: Frontend Components →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/04-day-4-integration.md)