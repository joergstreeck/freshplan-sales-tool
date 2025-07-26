# 🎨 FC-005 FRONTEND - STATE MANAGEMENT

**Navigation:** [← Components](01-components.md) | [Field Rendering →](03-field-rendering.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Stack:** Zustand, React Query, Immer  

## 📋 Inhaltsverzeichnis

1. [Zustand Store](#zustand-store)
2. [State Structure](#state-structure)
3. [Actions & Mutations](#actions--mutations)
4. [Persistence & Hydration](#persistence--hydration)
5. [React Query Integration](#react-query-integration)

## Zustand Store

### Main Store Definition

```typescript
// stores/customerOnboardingStore.ts
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import { FieldValue, ValidationError } from '../types';

interface Location {
  id: string;
  locationType: string;
  fieldValues: Map<string, any>;
  detailedLocations: DetailedLocation[];
}

interface DetailedLocation {
  id: string;
  name: string;
  street?: string;
  postalCode?: string;
  city?: string;
  fieldValues: Map<string, any>;
}

interface CustomerOnboardingState {
  // Wizard state
  currentStep: number;
  isDirty: boolean;
  lastSaved: Date | null;
  draftId: string | null;
  
  // Data state
  customerData: Map<string, any>;
  locations: Location[];
  validationErrors: Map<string, string>;
  
  // Computed state
  isChainCustomer: boolean;
  hasDetailedLocations: boolean;
  
  // Actions
  setFieldValue: (fieldKey: string, value: any) => void;
  setCurrentStep: (step: number) => void;
  addLocation: (location: Location) => void;
  removeLocation: (locationId: string) => void;
  updateLocation: (locationId: string, updates: Partial<Location>) => void;
  validateField: (fieldKey: string) => Promise<void>;
  validateStep: (step: number) => Promise<boolean>;
  saveAsDraft: () => Promise<void>;
  loadDraft: (draftId: string) => Promise<void>;
  reset: () => void;
}

const STORAGE_KEY = 'customer-onboarding-draft';

export const useCustomerOnboardingStore = create<CustomerOnboardingState>()(
  persist(
    immer((set, get) => ({
      // Initial state
      currentStep: 0,
      isDirty: false,
      lastSaved: null,
      draftId: null,
      customerData: new Map(),
      locations: [],
      validationErrors: new Map(),
      
      // Computed getters
      get isChainCustomer() {
        return get().customerData.get('chainCustomer') === 'ja';
      },
      
      get hasDetailedLocations() {
        return get().customerData.get('detailedLocations') === true;
      },
      
      // Actions implementation follows...
    })),
    {
      name: STORAGE_KEY,
      storage: createJSONStorage(() => sessionStorage),
      partialize: (state) => ({
        draftId: state.draftId,
        customerData: Array.from(state.customerData.entries()),
        locations: state.locations,
        currentStep: state.currentStep,
        lastSaved: state.lastSaved
      })
    }
  )
);
```

## State Structure

### Data Organization

```typescript
// Customer Data Structure
interface CustomerData {
  // Base fields
  companyName: string;
  industry: string;
  chainCustomer: 'ja' | 'nein';
  detailedLocations?: boolean;
  
  // Dynamic fields based on industry
  [key: string]: any;
}

// State Shape
interface StoreState {
  // Navigation
  currentStep: 0 | 1 | 2;
  
  // Form state
  isDirty: boolean;
  lastSaved: Date | null;
  draftId: string | null;
  
  // Data collections
  customerData: Map<string, any>;
  locations: Location[];
  
  // Validation
  validationErrors: Map<string, string>;
  fieldTouched: Map<string, boolean>;
}
```

### Selectors

```typescript
// Computed selectors for derived state
export const selectors = {
  // Get customer display name
  getCustomerName: (state: CustomerOnboardingState) => {
    return state.customerData.get('companyName') || 'Neuer Kunde';
  },
  
  // Check if current step is valid
  isStepValid: (state: CustomerOnboardingState, step: number) => {
    switch (step) {
      case 0: // Customer data
        return state.validationErrors.size === 0 
          && state.customerData.has('companyName')
          && state.customerData.has('industry');
      
      case 1: // Locations
        return state.locations.length > 0;
      
      case 2: // Detailed locations
        return state.locations.every(
          loc => loc.detailedLocations.length > 0
        );
      
      default:
        return false;
    }
  },
  
  // Get total location count
  getTotalLocationCount: (state: CustomerOnboardingState) => {
    return state.locations.reduce((sum, loc) => {
      return sum + loc.detailedLocations.length;
    }, 0);
  }
};
```

## Actions & Mutations

### Field Management

```typescript
// Inside the store definition
setFieldValue: (fieldKey, value) => set((state) => {
  state.customerData.set(fieldKey, value);
  state.isDirty = true;
  
  // Clear validation error when field is updated
  state.validationErrors.delete(fieldKey);
  
  // Handle special trigger fields
  if (fieldKey === 'chainCustomer' && value === 'nein') {
    // Clear locations if not a chain customer
    state.locations = [];
  }
  
  if (fieldKey === 'industry') {
    // Reset industry-specific fields when industry changes
    const industryFields = getIndustrySpecificFields(value);
    // Clear non-relevant fields
    state.customerData.forEach((val, key) => {
      if (!industryFields.includes(key) && !isBaseField(key)) {
        state.customerData.delete(key);
      }
    });
  }
}),
```

### Location Management

```typescript
addLocation: (location) => set((state) => {
  // Generate unique ID if not provided
  const newLocation = {
    ...location,
    id: location.id || generateId(),
    fieldValues: new Map(location.fieldValues || []),
    detailedLocations: []
  };
  
  state.locations.push(newLocation);
  state.isDirty = true;
}),

removeLocation: (locationId) => set((state) => {
  state.locations = state.locations.filter(l => l.id !== locationId);
  state.isDirty = true;
}),

updateLocation: (locationId, updates) => set((state) => {
  const location = state.locations.find(l => l.id === locationId);
  if (location) {
    Object.assign(location, updates);
    state.isDirty = true;
  }
}),

addDetailedLocation: (locationId, detailedLocation) => set((state) => {
  const location = state.locations.find(l => l.id === locationId);
  if (location) {
    location.detailedLocations.push({
      ...detailedLocation,
      id: detailedLocation.id || generateId()
    });
    state.isDirty = true;
  }
}),
```

### Validation

```typescript
validateField: async (fieldKey) => {
  const { customerData } = get();
  const value = customerData.get(fieldKey);
  
  try {
    await fieldValidationService.validateField(fieldKey, value);
    set((state) => {
      state.validationErrors.delete(fieldKey);
    });
  } catch (error) {
    set((state) => {
      state.validationErrors.set(fieldKey, error.message);
    });
  }
},

validateStep: async (step) => {
  const state = get();
  const errors = new Map<string, string>();
  
  switch (step) {
    case 0: // Customer data validation
      const requiredFields = ['companyName', 'industry', 'chainCustomer'];
      for (const field of requiredFields) {
        if (!state.customerData.has(field)) {
          errors.set(field, 'Pflichtfeld');
        }
      }
      break;
      
    case 1: // Location validation
      if (state.locations.length === 0) {
        errors.set('locations', 'Mindestens ein Standort erforderlich');
      }
      break;
  }
  
  set((state) => {
    state.validationErrors = errors;
  });
  
  return errors.size === 0;
},
```

## Persistence & Hydration

### Storage Configuration

```typescript
// Persistence middleware configuration
const persistConfig = {
  name: STORAGE_KEY,
  storage: createJSONStorage(() => sessionStorage),
  
  // Only persist essential data
  partialize: (state) => ({
    draftId: state.draftId,
    customerData: Array.from(state.customerData.entries()),
    locations: serializeLocations(state.locations),
    currentStep: state.currentStep,
    lastSaved: state.lastSaved
  }),
  
  // Rehydrate Maps and complex objects
  onRehydrateStorage: () => (state) => {
    if (state) {
      state.customerData = new Map(state.customerData);
      state.locations = deserializeLocations(state.locations);
      state.validationErrors = new Map();
    }
  }
};
```

### Auto-Save Implementation

```typescript
// hooks/useAutoSave.ts
export const useAutoSave = (delay = 1000) => {
  const { isDirty, saveAsDraft } = useCustomerOnboardingStore();
  const [isSaving, setIsSaving] = useState(false);
  const saveMutation = useSaveDraft();
  
  const debouncedSave = useMemo(
    () => debounce(async () => {
      setIsSaving(true);
      try {
        await saveMutation.mutateAsync();
      } finally {
        setIsSaving(false);
      }
    }, delay),
    [saveMutation, delay]
  );
  
  useEffect(() => {
    if (isDirty) {
      debouncedSave();
    }
    
    return () => {
      debouncedSave.cancel();
    };
  }, [isDirty, debouncedSave]);
  
  return { isSaving };
};
```

## React Query Integration

### Draft Management

```typescript
// services/customerQueries.ts
export const customerQueries = {
  draft: (draftId: string) => ({
    queryKey: ['customer', 'draft', draftId],
    queryFn: () => customerApi.getDraft(draftId),
    staleTime: 5 * 60 * 1000, // 5 minutes
  }),
  
  fieldDefinitions: (entityType: string, industry?: string) => ({
    queryKey: ['fieldDefinitions', entityType, industry],
    queryFn: () => fieldDefinitionApi.getDefinitions({
      entityType,
      industry
    }),
    staleTime: 30 * 60 * 1000, // 30 minutes
  })
};

// Mutations
export const useSaveDraft = () => {
  const queryClient = useQueryClient();
  const store = useCustomerOnboardingStore();
  
  return useMutation({
    mutationFn: async () => {
      const { draftId, customerData, locations } = store.getState();
      
      return customerApi.saveDraft({
        id: draftId,
        customerFieldValues: Array.from(customerData.entries()),
        locations: serializeLocations(locations)
      });
    },
    
    onSuccess: (data) => {
      store.setState({
        draftId: data.id,
        isDirty: false,
        lastSaved: new Date()
      });
      
      queryClient.setQueryData(
        ['customer', 'draft', data.id], 
        data
      );
    }
  });
};
```

### Optimistic Updates

```typescript
export const useUpdateFieldValue = () => {
  const queryClient = useQueryClient();
  const { draftId } = useCustomerOnboardingStore();
  
  return useMutation({
    mutationFn: async ({ fieldKey, value }) => {
      return customerApi.updateFieldValue(draftId!, fieldKey, value);
    },
    
    // Optimistic update
    onMutate: async ({ fieldKey, value }) => {
      await queryClient.cancelQueries(['customer', 'draft', draftId]);
      
      const previousDraft = queryClient.getQueryData(['customer', 'draft', draftId]);
      
      queryClient.setQueryData(['customer', 'draft', draftId], old => ({
        ...old,
        customerFieldValues: {
          ...old.customerFieldValues,
          [fieldKey]: value
        }
      }));
      
      return { previousDraft };
    },
    
    // Rollback on error
    onError: (err, variables, context) => {
      queryClient.setQueryData(
        ['customer', 'draft', draftId], 
        context.previousDraft
      );
    }
  });
};
```

---

**Parent:** [Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)  
**Related:** [Components](01-components.md) | [Field Rendering](03-field-rendering.md) | [Validation](04-validation.md)