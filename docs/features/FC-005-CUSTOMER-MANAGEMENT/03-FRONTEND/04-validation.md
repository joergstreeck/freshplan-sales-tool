# 🎨 FC-005 FRONTEND - VALIDATION

**Navigation:** [← Field Rendering](03-field-rendering.md) | [Frontend Overview →](README.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Stack:** Zod, React Hook Form  

## 📋 Inhaltsverzeichnis

1. [Validation Framework](#validation-framework)
2. [Zod Schemas](#zod-schemas)
3. [Field-Level Validation](#field-level-validation)
4. [Step Validation](#step-validation)
5. [Custom Validators](#custom-validators)

## Validation Framework

### Zod Integration

```typescript
// schemas/validationSchemas.ts
import { z } from 'zod';

// Base validation schemas
export const germanPostalCodeSchema = z
  .string()
  .regex(/^[0-9]{5}$/, 'PLZ muss genau 5 Ziffern haben');

export const germanPhoneSchema = z
  .string()
  .regex(/^[\d\s\-\+\(\)]+$/, 'Ungültige Telefonnummer')
  .refine(
    (val) => val.replace(/\D/g, '').length >= 10,
    'Telefonnummer muss mindestens 10 Ziffern haben'
  );

export const emailSchema = z
  .string()
  .email('Ungültige E-Mail-Adresse')
  .toLowerCase();

export const urlSchema = z
  .string()
  .url('Ungültige URL')
  .or(z.literal(''));

export const companyNameSchema = z
  .string()
  .min(2, 'Firmenname muss mindestens 2 Zeichen haben')
  .max(100, 'Firmenname darf maximal 100 Zeichen haben')
  .refine(
    (val) => !val.match(/[<>]/),
    'Firmenname darf keine HTML-Zeichen enthalten'
  );
```

### Dynamic Schema Builder

```typescript
// utils/schemaBuilder.ts
import { FieldDefinition } from '../types/field.types';

export const buildFieldSchema = (field: FieldDefinition): z.ZodType<any> => {
  let schema: z.ZodType<any> = z.any();
  
  switch (field.fieldType) {
    case 'text':
      schema = z.string();
      if (field.minLength) {
        schema = schema.min(field.minLength, 
          `Mindestens ${field.minLength} Zeichen erforderlich`
        );
      }
      if (field.maxLength) {
        schema = schema.max(field.maxLength, 
          `Maximal ${field.maxLength} Zeichen erlaubt`
        );
      }
      if (field.pattern) {
        schema = schema.regex(new RegExp(field.pattern), 
          field.patternError || 'Ungültiges Format'
        );
      }
      break;
      
    case 'email':
      schema = emailSchema;
      break;
      
    case 'phone':
      schema = germanPhoneSchema;
      break;
      
    case 'number':
      schema = z.number({
        required_error: 'Bitte geben Sie eine Zahl ein',
        invalid_type_error: 'Muss eine Zahl sein'
      });
      if (field.min !== undefined) {
        schema = schema.min(field.min, 
          `Wert muss mindestens ${field.min} sein`
        );
      }
      if (field.max !== undefined) {
        schema = schema.max(field.max, 
          `Wert darf maximal ${field.max} sein`
        );
      }
      break;
      
    case 'select':
      if (field.options) {
        const values = field.options.map(opt => opt.value);
        schema = z.enum(values as [string, ...string[]]);
      }
      break;
      
    case 'boolean':
      schema = z.boolean();
      break;
      
    case 'date':
      schema = z.string().refine(
        (val) => !isNaN(Date.parse(val)),
        'Ungültiges Datum'
      );
      break;
  }
  
  // Apply custom validation from field definition
  if (field.validation) {
    schema = applyCustomValidation(schema, field.validation);
  }
  
  // Handle required/optional
  if (!field.required) {
    schema = schema.optional().nullable();
  }
  
  return schema;
};
```

## Zod Schemas

### Customer Schema

```typescript
// schemas/customerSchemas.ts
export const customerBaseSchema = z.object({
  companyName: companyNameSchema,
  industry: z.enum(['hotel', 'krankenhaus', 'seniorenresidenz', 
                    'restaurant', 'betriebsrestaurant']),
  chainCustomer: z.enum(['ja', 'nein']),
  
  // Contact information
  email: emailSchema.optional(),
  phone: germanPhoneSchema.optional(),
  website: urlSchema.optional(),
  
  // Address
  street: z.string().max(100).optional(),
  postalCode: germanPostalCodeSchema.optional(),
  city: z.string().max(50).optional(),
});

// Industry-specific schemas
export const hotelFieldsSchema = z.object({
  starRating: z.enum(['1', '2', '3', '4', '5']).optional(),
  roomCount: z.number().min(1).max(9999).optional(),
  restaurantSeats: z.number().min(0).optional(),
});

export const hospitalFieldsSchema = z.object({
  hospitalType: z.enum(['university', 'general', 'specialized']),
  bedCount: z.number().min(1).max(9999),
  departments: z.array(z.string()).optional(),
});

// Dynamic customer schema based on industry
export const getCustomerSchema = (industry?: string) => {
  let schema = customerBaseSchema;
  
  switch (industry) {
    case 'hotel':
      schema = schema.merge(hotelFieldsSchema);
      break;
    case 'krankenhaus':
      schema = schema.merge(hospitalFieldsSchema);
      break;
    // Add other industry schemas...
  }
  
  return schema;
};
```

### Location Schema

```typescript
export const locationSchema = z.object({
  locationType: z.enum(['hauptstandort', 'filiale', 'aussenstelle']),
  name: z.string().min(2).max(100),
  
  // Industry-specific location fields
  roomCount: z.number().optional(),
  bedCount: z.number().optional(),
  kitchenCapacity: z.number().optional(),
});

export const detailedLocationSchema = z.object({
  name: z.string().min(2).max(100),
  street: z.string().max(100).optional(),
  postalCode: germanPostalCodeSchema.optional(),
  city: z.string().max(50).optional(),
  
  // Contact person
  contactPerson: z.string().max(100).optional(),
  contactPhone: germanPhoneSchema.optional(),
  contactEmail: emailSchema.optional(),
});
```

## Field-Level Validation

### Validation Service

```typescript
// utils/fieldValidation.ts
import { FieldDefinition } from '../types/field.types';
import { buildFieldSchema } from './schemaBuilder';

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
          error: this.formatZodError(error)
        };
      }
      throw error;
    }
  }
  
  validateFieldSync(
    fieldDefinition: FieldDefinition,
    value: any
  ): ValidationResult {
    const schema = this.getOrBuildSchema(fieldDefinition);
    
    const result = schema.safeParse(value);
    
    if (result.success) {
      return { isValid: true };
    }
    
    return {
      isValid: false,
      error: this.formatZodError(result.error)
    };
  }
  
  private getOrBuildSchema(field: FieldDefinition): z.ZodType<any> {
    const cacheKey = `${field.key}_${field.version || '1'}`;
    
    if (!this.schemaCache.has(cacheKey)) {
      this.schemaCache.set(cacheKey, buildFieldSchema(field));
    }
    
    return this.schemaCache.get(cacheKey)!;
  }
  
  private formatZodError(error: z.ZodError): string {
    // Return the first error message
    return error.errors[0]?.message || 'Validierungsfehler';
  }
}

export const fieldValidationService = new FieldValidationService();
```

### Real-time Validation Hook

```typescript
// hooks/useFieldValidation.ts
export const useFieldValidation = () => {
  const { validationErrors, setValidationError, clearValidationError } = 
    useCustomerOnboardingStore();
  
  const validateField = useCallback(
    async (fieldKey: string, value: any, fieldDefinition: FieldDefinition) => {
      const result = await fieldValidationService.validateField(
        fieldDefinition,
        value
      );
      
      if (result.isValid) {
        clearValidationError(fieldKey);
      } else {
        setValidationError(fieldKey, result.error!);
      }
      
      return result.isValid;
    },
    [setValidationError, clearValidationError]
  );
  
  const validateFieldDebounced = useMemo(
    () => debounce(validateField, 300),
    [validateField]
  );
  
  return {
    validateField,
    validateFieldDebounced,
    validationErrors
  };
};
```

## Step Validation

### Step Validators

```typescript
// utils/stepValidation.ts
export const stepValidators = {
  // Customer data step
  0: async (state: CustomerOnboardingState): Promise<ValidationErrors> => {
    const errors = new Map<string, string>();
    const { customerData } = state;
    
    // Required fields
    const requiredFields = ['companyName', 'industry', 'chainCustomer'];
    
    for (const field of requiredFields) {
      if (!customerData.has(field) || !customerData.get(field)) {
        errors.set(field, 'Dieses Feld ist erforderlich');
      }
    }
    
    // Validate with schema
    try {
      const industry = customerData.get('industry');
      const schema = getCustomerSchema(industry);
      
      const data = Object.fromEntries(customerData);
      await schema.parseAsync(data);
    } catch (error) {
      if (error instanceof z.ZodError) {
        error.errors.forEach(err => {
          const field = err.path.join('.');
          errors.set(field, err.message);
        });
      }
    }
    
    return errors;
  },
  
  // Locations step
  1: async (state: CustomerOnboardingState): Promise<ValidationErrors> => {
    const errors = new Map<string, string>();
    const { locations } = state;
    
    if (locations.length === 0) {
      errors.set('locations', 'Mindestens ein Standort ist erforderlich');
    }
    
    // Validate each location
    for (let i = 0; i < locations.length; i++) {
      const location = locations[i];
      
      try {
        await locationSchema.parseAsync({
          locationType: location.locationType,
          name: location.fieldValues.get('name'),
          // ... other fields
        });
      } catch (error) {
        if (error instanceof z.ZodError) {
          error.errors.forEach(err => {
            const field = `locations.${i}.${err.path.join('.')}`;
            errors.set(field, err.message);
          });
        }
      }
    }
    
    return errors;
  },
  
  // Detailed locations step
  2: async (state: CustomerOnboardingState): Promise<ValidationErrors> => {
    const errors = new Map<string, string>();
    
    // Validate all detailed locations
    // ...
    
    return errors;
  }
};
```

## Custom Validators

### Business Rule Validators

```typescript
// validators/businessRules.ts
export const businessRuleValidators = {
  // Validate company name uniqueness
  companyNameUnique: async (value: string): Promise<boolean> => {
    const response = await customerApi.checkCompanyName(value);
    return !response.exists;
  },
  
  // Validate postal code exists
  postalCodeExists: async (postalCode: string): Promise<boolean> => {
    // Could integrate with postal code API
    return /^[0-9]{5}$/.test(postalCode);
  },
  
  // Industry-specific validations
  hotelRoomCount: (roomCount: number, starRating: string): boolean => {
    // Business rule: 5-star hotels must have at least 50 rooms
    if (starRating === '5' && roomCount < 50) {
      return false;
    }
    return true;
  }
};

// Async validator wrapper
export const createAsyncValidator = <T>(
  validator: (value: T) => Promise<boolean>,
  errorMessage: string
) => {
  return z.custom<T>(async (val) => {
    const isValid = await validator(val);
    if (!isValid) {
      throw new Error(errorMessage);
    }
    return val;
  });
};
```

### Cross-Field Validation

```typescript
// validators/crossFieldValidation.ts
export const crossFieldValidators = {
  // Validate related fields together
  validateAddressFields: (data: Map<string, any>): ValidationResult => {
    const street = data.get('street');
    const postalCode = data.get('postalCode');
    const city = data.get('city');
    
    // If one address field is filled, all must be filled
    const hasAnyAddress = street || postalCode || city;
    const hasCompleteAddress = street && postalCode && city;
    
    if (hasAnyAddress && !hasCompleteAddress) {
      return {
        isValid: false,
        errors: {
          address: 'Bitte geben Sie eine vollständige Adresse ein'
        }
      };
    }
    
    return { isValid: true };
  },
  
  // Validate industry-specific requirements
  validateIndustryRequirements: (
    industry: string, 
    data: Map<string, any>
  ): ValidationResult => {
    switch (industry) {
      case 'hotel':
        if (!data.get('starRating')) {
          return {
            isValid: false,
            errors: { starRating: 'Sterne-Kategorie ist erforderlich' }
          };
        }
        break;
        
      case 'krankenhaus':
        if (!data.get('bedCount') || data.get('bedCount') < 1) {
          return {
            isValid: false,
            errors: { bedCount: 'Anzahl Betten ist erforderlich' }
          };
        }
        break;
    }
    
    return { isValid: true };
  }
};
```

---

**Parent:** [Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)  
**Related:** [Field Rendering](03-field-rendering.md) | [State Management](02-state-management.md) | [Testing](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)