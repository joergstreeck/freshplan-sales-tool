# 🎨 FC-005 CUSTOMER MANAGEMENT - FRONTEND ARCHITECTURE

**Datum:** 26.07.2025  
**Version:** 1.0  
**Stack:** React 18, TypeScript, Material-UI, Zustand, React Query  
**Abhängigkeiten:** Design System, Auth Context  

## 📋 Inhaltsverzeichnis

1. [Component Architecture](#component-architecture)
2. [State Management](#state-management)
3. [Field Rendering System](#field-rendering-system)
4. [Validation Framework](#validation-framework)
5. [API Integration](#api-integration)
6. [Performance Optimizations](#performance-optimizations)
7. [Testing Strategy](#testing-strategy)

---

## Component Architecture

### Folder Structure

```
frontend/src/features/customers/
├── components/
│   ├── wizard/
│   │   ├── CustomerOnboardingWizard.tsx
│   │   ├── WizardNavigation.tsx
│   │   └── WizardProgress.tsx
│   ├── steps/
│   │   ├── CustomerDataStep.tsx
│   │   ├── LocationsStep.tsx
│   │   └── DetailedLocationsStep.tsx
│   ├── fields/
│   │   ├── DynamicFieldRenderer.tsx
│   │   ├── FieldWrapper.tsx
│   │   └── fieldTypes/
│   │       ├── TextField.tsx
│   │       ├── NumberField.tsx
│   │       ├── SelectField.tsx
│   │       ├── DateField.tsx
│   │       └── BooleanField.tsx
│   ├── cards/
│   │   ├── LocationCard.tsx
│   │   └── DetailedLocationCard.tsx
│   └── shared/
│       ├── ValidationMessage.tsx
│       ├── SaveIndicator.tsx
│       └── BranchIcon.tsx
├── hooks/
│   ├── useCustomerOnboarding.ts
│   ├── useFieldDefinitions.ts
│   ├── useAutoSave.ts
│   └── useIndustryDefaults.ts
├── stores/
│   └── customerOnboardingStore.ts
├── services/
│   ├── customerApi.ts
│   └── fieldDefinitionApi.ts
├── types/
│   ├── customer.types.ts
│   ├── field.types.ts
│   └── api.types.ts
├── utils/
│   ├── fieldValidation.ts
│   ├── fieldFormatters.ts
│   └── industryHelpers.ts
├── schemas/
│   └── validationSchemas.ts
└── data/
    └── fieldCatalog.json
```

### Core Components

#### CustomerOnboardingWizard.tsx

```typescript
import { useState, useEffect } from 'react';
import { Box, Stepper, Step, StepLabel, Paper } from '@mui/material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { useAutoSave } from '../../hooks/useAutoSave';
import { CustomerDataStep } from '../steps/CustomerDataStep';
import { LocationsStep } from '../steps/LocationsStep';
import { DetailedLocationsStep } from '../steps/DetailedLocationsStep';
import { WizardNavigation } from './WizardNavigation';
import { SaveIndicator } from '../shared/SaveIndicator';

const STEPS = [
  { id: 'customer', label: 'Stammdaten' },
  { id: 'locations', label: 'Standorte' },
  { id: 'detailed', label: 'Standortdetails' }
];

export const CustomerOnboardingWizard: React.FC = () => {
  const {
    currentStep,
    customerData,
    isDirty,
    lastSaved,
    canProgressToNextStep,
    setCurrentStep
  } = useCustomerOnboardingStore();
  
  const { isLoading: loadingFields } = useFieldDefinitions();
  const { isSaving } = useAutoSave();
  
  // Dynamic step visibility
  const visibleSteps = STEPS.filter(step => {
    if (step.id === 'locations') {
      return customerData.get('chainCustomer') === 'ja';
    }
    if (step.id === 'detailed') {
      return customerData.get('chainCustomer') === 'ja' 
        && customerData.get('detailedLocations') === true;
    }
    return true;
  });
  
  const renderStep = () => {
    switch (STEPS[currentStep].id) {
      case 'customer':
        return <CustomerDataStep />;
      case 'locations':
        return <LocationsStep />;
      case 'detailed':
        return <DetailedLocationsStep />;
      default:
        return null;
    }
  };
  
  if (loadingFields) {
    return <LoadingScreen />;
  }
  
  return (
    <Box sx={{ width: '100%', maxWidth: 1200, mx: 'auto', p: 3 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Stepper activeStep={currentStep} sx={{ mb: 4 }}>
          {visibleSteps.map((step, index) => (
            <Step key={step.id} completed={index < currentStep}>
              <StepLabel>{step.label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        <SaveIndicator 
          isDirty={isDirty} 
          isSaving={isSaving} 
          lastSaved={lastSaved} 
        />
        
        <Box sx={{ mt: 4, mb: 4 }}>
          {renderStep()}
        </Box>
        
        <WizardNavigation
          currentStep={currentStep}
          totalSteps={visibleSteps.length}
          canProgress={canProgressToNextStep()}
          onBack={() => setCurrentStep(currentStep - 1)}
          onNext={() => setCurrentStep(currentStep + 1)}
          onFinish={() => handleFinish()}
        />
      </Paper>
    </Box>
  );
};
```

#### DynamicFieldRenderer.tsx

```typescript
import React from 'react';
import { Grid } from '@mui/material';
import { FieldDefinition, FieldValue } from '../../types/field.types';
import { FieldWrapper } from './FieldWrapper';
import { TextField } from './fieldTypes/TextField';
import { NumberField } from './fieldTypes/NumberField';
import { SelectField } from './fieldTypes/SelectField';
import { DateField } from './fieldTypes/DateField';
import { BooleanField } from './fieldTypes/BooleanField';

interface Props {
  fields: FieldDefinition[];
  values: Map<string, any>;
  errors: Map<string, string>;
  onChange: (fieldKey: string, value: any) => void;
  onBlur: (fieldKey: string) => void;
}

export const DynamicFieldRenderer: React.FC<Props> = ({
  fields,
  values,
  errors,
  onChange,
  onBlur
}) => {
  const renderField = (field: FieldDefinition) => {
    const value = values.get(field.key) ?? field.defaultValue;
    const error = errors.get(field.key);
    
    const commonProps = {
      value,
      onChange: (newValue: any) => onChange(field.key, newValue),
      onBlur: () => onBlur(field.key),
      error: !!error,
      helperText: error,
      disabled: field.isReadOnly
    };
    
    switch (field.fieldType) {
      case 'text':
      case 'email':
        return <TextField {...commonProps} field={field} />;
        
      case 'number':
        return <NumberField {...commonProps} field={field} />;
        
      case 'select':
        return <SelectField {...commonProps} field={field} />;
        
      case 'date':
        return <DateField {...commonProps} field={field} />;
        
      case 'boolean':
        return <BooleanField {...commonProps} field={field} />;
        
      default:
        console.warn(`Unknown field type: ${field.fieldType}`);
        return null;
    }
  };
  
  return (
    <Grid container spacing={3}>
      {fields.map(field => (
        <Grid 
          item 
          xs={12} 
          sm={field.gridSize?.sm || 6} 
          md={field.gridSize?.md || 4}
          key={field.key}
        >
          <FieldWrapper
            field={field}
            hasError={errors.has(field.key)}
          >
            {renderField(field)}
          </FieldWrapper>
        </Grid>
      ))}
    </Grid>
  );
};
```

---

## State Management

### Zustand Store

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
      
      // Computed
      get isChainCustomer() {
        return get().customerData.get('chainCustomer') === 'ja';
      },
      
      get hasDetailedLocations() {
        return get().customerData.get('detailedLocations') === true;
      },
      
      // Actions
      setFieldValue: (fieldKey, value) => set((state) => {
        state.customerData.set(fieldKey, value);
        state.isDirty = true;
        
        // Clear validation error when field is updated
        state.validationErrors.delete(fieldKey);
        
        // Handle special trigger fields
        if (fieldKey === 'chainCustomer' && value === 'nein') {
          state.locations = [];
        }
      }),
      
      setCurrentStep: (step) => set((state) => {
        state.currentStep = step;
      }),
      
      addLocation: (location) => set((state) => {
        state.locations.push(location);
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
        const errors = await stepValidationService.validateStep(step, get());
        set((state) => {
          state.validationErrors = new Map(errors);
        });
        return errors.size === 0;
      },
      
      saveAsDraft: async () => {
        const { draftId, customerData, locations } = get();
        
        try {
          const response = await customerApi.saveDraft({
            id: draftId,
            customerFieldValues: Array.from(customerData.entries()),
            locations
          });
          
          set((state) => {
            state.draftId = response.id;
            state.isDirty = false;
            state.lastSaved = new Date();
          });
        } catch (error) {
          console.error('Failed to save draft:', error);
          throw error;
        }
      },
      
      loadDraft: async (draftId) => {
        try {
          const draft = await customerApi.loadDraft(draftId);
          
          set((state) => {
            state.draftId = draftId;
            state.customerData = new Map(draft.customerFieldValues);
            state.locations = draft.locations;
            state.isDirty = false;
            state.lastSaved = draft.updatedAt;
          });
        } catch (error) {
          console.error('Failed to load draft:', error);
          throw error;
        }
      },
      
      reset: () => set((state) => {
        state.currentStep = 0;
        state.isDirty = false;
        state.lastSaved = null;
        state.draftId = null;
        state.customerData.clear();
        state.locations = [];
        state.validationErrors.clear();
      })
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

---

## Field Rendering System

### Field Type Components

```typescript
// components/fields/fieldTypes/TextField.tsx
import React from 'react';
import { TextField as MuiTextField } from '@mui/material';
import { FieldComponentProps } from '../../../types/field.types';
import { useFieldFormatting } from '../../../hooks/useFieldFormatting';

export const TextField: React.FC<FieldComponentProps> = ({
  field,
  value,
  onChange,
  onBlur,
  error,
  helperText,
  disabled
}) => {
  const { format, parse } = useFieldFormatting(field);
  
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const parsedValue = parse(event.target.value);
    onChange(parsedValue);
  };
  
  return (
    <MuiTextField
      fullWidth
      label={field.label}
      value={format(value) || ''}
      onChange={handleChange}
      onBlur={onBlur}
      error={error}
      helperText={helperText || field.helpText}
      disabled={disabled}
      required={field.required}
      inputProps={{
        maxLength: field.maxLength,
        pattern: field.pattern
      }}
      type={field.fieldType === 'email' ? 'email' : 'text'}
    />
  );
};
```

### Dynamic Field Loading

```typescript
// hooks/useFieldDefinitions.ts
import { useQuery } from '@tanstack/react-query';
import { fieldDefinitionApi } from '../services/fieldDefinitionApi';
import { useCustomerOnboardingStore } from '../stores/customerOnboardingStore';

export const useFieldDefinitions = (entityType: EntityType) => {
  const industry = useCustomerOnboardingStore(
    state => state.customerData.get('industry')
  );
  
  return useQuery({
    queryKey: ['fieldDefinitions', entityType, industry],
    queryFn: () => fieldDefinitionApi.getFieldDefinitions({
      entityType,
      industry,
      includeCustom: false // MVP ohne Custom Fields
    }),
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000 // 10 minutes
  });
};
```

---

## Validation Framework

### Zod Integration

```typescript
// schemas/validationSchemas.ts
import { z } from 'zod';

// Base schemas
export const germanPostalCodeSchema = z
  .string()
  .regex(/^[0-9]{5}$/, 'PLZ muss 5 Ziffern haben');

export const germanPhoneSchema = z
  .string()
  .regex(/^[\d\s\-\+\(\)]+$/, 'Ungültige Telefonnummer')
  .refine(
    (val) => val.replace(/\D/g, '').length >= 10,
    'Telefonnummer zu kurz'
  );

export const emailSchema = z
  .string()
  .email('Ungültige E-Mail-Adresse');

// Dynamic schema builder
export const buildFieldSchema = (field: FieldDefinition) => {
  let schema: z.ZodType<any> = z.any();
  
  switch (field.fieldType) {
    case 'text':
      schema = z.string();
      if (field.minLength) {
        schema = schema.min(field.minLength);
      }
      if (field.maxLength) {
        schema = schema.max(field.maxLength);
      }
      break;
      
    case 'email':
      schema = emailSchema;
      break;
      
    case 'number':
      schema = z.number();
      if (field.min !== undefined) {
        schema = schema.min(field.min);
      }
      if (field.max !== undefined) {
        schema = schema.max(field.max);
      }
      break;
      
    case 'select':
      if (field.options) {
        schema = z.enum(field.options as [string, ...string[]]);
      }
      break;
  }
  
  // Apply custom validation
  if (field.validation) {
    try {
      const customSchema = JSON.parse(field.validation);
      // Apply custom rules
    } catch (e) {
      console.error('Invalid custom validation:', e);
    }
  }
  
  // Required field
  if (!field.required) {
    schema = schema.optional();
  }
  
  return schema;
};
```

### Field-Level Validation

```typescript
// utils/fieldValidation.ts
import { FieldDefinition } from '../types/field.types';
import { buildFieldSchema } from '../schemas/validationSchemas';

export class FieldValidationService {
  private schemaCache = new Map<string, z.ZodType<any>>();
  
  async validateField(
    fieldDefinition: FieldDefinition,
    value: any
  ): Promise<ValidationResult> {
    const schema = this.getOrBuildSchema(fieldDefinition);
    
    try {
      await schema.parseAsync(value);
      return { isValid: true };
    } catch (error) {
      if (error instanceof z.ZodError) {
        return {
          isValid: false,
          error: error.errors[0].message
        };
      }
      throw error;
    }
  }
  
  private getOrBuildSchema(field: FieldDefinition): z.ZodType<any> {
    const cacheKey = `${field.key}_${field.version}`;
    
    if (!this.schemaCache.has(cacheKey)) {
      this.schemaCache.set(cacheKey, buildFieldSchema(field));
    }
    
    return this.schemaCache.get(cacheKey)!;
  }
}
```

---

## API Integration

### React Query Setup

```typescript
// services/customerApi.ts
import { apiClient } from '@/lib/apiClient';
import { useMutation, useQuery } from '@tanstack/react-query';

export const customerApi = {
  saveDraft: async (draft: CustomerDraft) => {
    const response = await apiClient.post('/api/customers/draft', draft);
    return response.data;
  },
  
  loadDraft: async (draftId: string) => {
    const response = await apiClient.get(`/api/customers/draft/${draftId}`);
    return response.data;
  },
  
  finalizeDraft: async (draftId: string) => {
    const response = await apiClient.post(
      `/api/customers/draft/${draftId}/finalize`
    );
    return response.data;
  }
};

// Hooks
export const useSaveDraft = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: customerApi.saveDraft,
    onSuccess: (data) => {
      queryClient.setQueryData(['draft', data.id], data);
    }
  });
};

export const useLoadDraft = (draftId: string | null) => {
  return useQuery({
    queryKey: ['draft', draftId],
    queryFn: () => customerApi.loadDraft(draftId!),
    enabled: !!draftId
  });
};
```

---

## Performance Optimizations

### 1. Lazy Loading

```typescript
// Lazy load heavy components
const LocationsStep = lazy(() => import('../steps/LocationsStep'));
const DetailedLocationsStep = lazy(() => import('../steps/DetailedLocationsStep'));
```

### 2. Memoization

```typescript
// Memoize expensive computations
const totalLocations = useMemo(() => {
  return locations.reduce((sum, loc) => {
    const count = loc.fieldValues.get('locationCount') || 0;
    return sum + count;
  }, 0);
}, [locations]);
```

### 3. Debounced Auto-Save

```typescript
// hooks/useAutoSave.ts
export const useAutoSave = (delay = 1000) => {
  const { isDirty, saveAsDraft } = useCustomerOnboardingStore();
  const [isSaving, setIsSaving] = useState(false);
  
  const debouncedSave = useMemo(
    () => debounce(async () => {
      setIsSaving(true);
      try {
        await saveAsDraft();
      } finally {
        setIsSaving(false);
      }
    }, delay),
    [saveAsDraft, delay]
  );
  
  useEffect(() => {
    if (isDirty) {
      debouncedSave();
    }
  }, [isDirty, debouncedSave]);
  
  return { isSaving };
};
```

---

## Testing Strategy

### Component Tests

```typescript
// __tests__/DynamicFieldRenderer.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { DynamicFieldRenderer } from '../components/fields/DynamicFieldRenderer';

describe('DynamicFieldRenderer', () => {
  const mockFields: FieldDefinition[] = [
    {
      key: 'companyName',
      label: 'Firmenname',
      fieldType: 'text',
      required: true
    }
  ];
  
  it('renders all fields', () => {
    render(
      <DynamicFieldRenderer
        fields={mockFields}
        values={new Map()}
        errors={new Map()}
        onChange={jest.fn()}
        onBlur={jest.fn()}
      />
    );
    
    expect(screen.getByLabelText('Firmenname')).toBeInTheDocument();
  });
  
  it('displays validation errors', () => {
    const errors = new Map([['companyName', 'Pflichtfeld']]);
    
    render(
      <DynamicFieldRenderer
        fields={mockFields}
        values={new Map()}
        errors={errors}
        onChange={jest.fn()}
        onBlur={jest.fn()}
      />
    );
    
    expect(screen.getByText('Pflichtfeld')).toBeInTheDocument();
  });
});
```

### Store Tests

```typescript
// __tests__/customerOnboardingStore.test.ts
import { act, renderHook } from '@testing-library/react';
import { useCustomerOnboardingStore } from '../stores/customerOnboardingStore';

describe('customerOnboardingStore', () => {
  beforeEach(() => {
    useCustomerOnboardingStore.getState().reset();
  });
  
  it('updates field values', () => {
    const { result } = renderHook(() => useCustomerOnboardingStore());
    
    act(() => {
      result.current.setFieldValue('companyName', 'Test GmbH');
    });
    
    expect(result.current.customerData.get('companyName')).toBe('Test GmbH');
    expect(result.current.isDirty).toBe(true);
  });
  
  it('handles chain customer trigger', () => {
    const { result } = renderHook(() => useCustomerOnboardingStore());
    
    // Add locations
    act(() => {
      result.current.addLocation({ id: '1', /* ... */ });
    });
    
    // Set chainCustomer to 'nein'
    act(() => {
      result.current.setFieldValue('chainCustomer', 'nein');
    });
    
    // Locations should be cleared
    expect(result.current.locations).toHaveLength(0);
  });
});
```