# 📝 FC-005 TEST EXAMPLES - Mit Flexibilitäts-Philosophie

**Parent:** [Test Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/09-TEST-PLAN/README.md)  
**Philosophie:** [Flexibilitäts-Philosophie](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/09-TEST-PLAN/00-PHILOSOPHIE.md)

---

## 🎯 Test-Beispiele die unsere Philosophie RICHTIG umsetzen

### 1. Store Test mit flexiblen Field Values

```typescript
// ✅ RICHTIG - Respektiert die Flexibilität
describe('CustomerOnboardingStore - Field Management', () => {
  it('should accept any field value type', () => {
    const { result } = renderHook(() => useCustomerOnboardingStore());
    
    // String value
    act(() => {
      result.current.setCustomerField('companyName', 'Test GmbH');
    });
    
    // Number value
    act(() => {
      result.current.setCustomerField('employeeCount', 150);
    });
    
    // Boolean value
    act(() => {
      result.current.setCustomerField('hasParking', true);
    });
    
    // Array value (für Multi-Select)
    act(() => {
      result.current.setCustomerField('services', ['Catering', 'Cleaning', 'Security']);
    });
    
    // Nested object (wenn nötig)
    act(() => {
      result.current.setCustomerField('contactPreferences', {
        email: true,
        phone: false,
        bestTime: 'morning'
      });
    });
    
    // Alle Werte sind erlaubt!
    expect(result.current.customerData.companyName).toBe('Test GmbH');
    expect(result.current.customerData.employeeCount).toBe(150);
    expect(result.current.customerData.hasParking).toBe(true);
    expect(result.current.customerData.services).toEqual(['Catering', 'Cleaning', 'Security']);
    expect(result.current.customerData.contactPreferences).toMatchObject({
      email: true,
      phone: false,
      bestTime: 'morning'
    });
  });
  
  it('should handle dynamic fields that dont exist yet', () => {
    const { result } = renderHook(() => useCustomerOnboardingStore());
    
    // Felder die noch nicht im Field Catalog sind
    act(() => {
      result.current.setCustomerField('futureField1', 'Future Value');
      result.current.setCustomerField('experimentalFeature', { beta: true });
      result.current.setCustomerField('customerId_' + Date.now(), 'dynamic-id');
    });
    
    // Alles funktioniert!
    expect(Object.keys(result.current.customerData).length).toBeGreaterThan(0);
  });
});
```

### 2. API Client Test mit flexiblen Payloads

```typescript
// ✅ RICHTIG - API akzeptiert beliebige Daten
describe('ApiClient - Flexible Payloads', () => {
  it('should send any data structure', async () => {
    const apiClient = new ApiClient({ baseURL: 'http://test.com' });
    
    // Mock fetch
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ success: true })
    });
    
    // Verschiedene Payload-Strukturen
    const payloads = [
      // Einfaches Objekt
      { name: 'Test', value: 123 },
      
      // Verschachteltes Objekt
      {
        customer: {
          fields: {
            dynamic1: 'value1',
            nested: {
              deep: {
                field: true
              }
            }
          }
        }
      },
      
      // Array
      ['item1', 'item2', { complex: true }],
      
      // Primitives (wenn die API das unterstützt)
      'just a string',
      42,
      true
    ];
    
    // Alle sollten funktionieren
    for (const payload of payloads) {
      await apiClient.post('/api/test', payload);
      expect(fetch).toHaveBeenCalled();
    }
  });
});
```

### 3. Component Test mit ungenutzten Imports

```typescript
// ✅ RICHTIG - Imports werden NICHT als Problem behandelt
describe('DetailedLocationsStep', () => {
  it('should render without import warnings', () => {
    // Wir testen NICHT ob alle Imports genutzt werden!
    // Das ist KEIN Test-Kriterium!
    
    const { container } = render(
      <DetailedLocationsStep />
    );
    
    // Testen was wichtig ist: Funktionalität
    expect(container).toBeInTheDocument();
    
    // NICHT testen:
    // - expect(unusedImports).toHaveLength(0) ❌
    // - expect(allImportsUsed).toBe(true) ❌
  });
});
```

### 4. Field Validation mit dynamischen Regeln

```typescript
// ✅ RICHTIG - Validation ist auch flexibel
describe('Field Validation', () => {
  it('should validate based on runtime field definition', () => {
    const { result } = renderHook(() => useCustomerOnboardingStore());
    
    // Setze Field Definitions zur Laufzeit
    const dynamicFields: FieldDefinition[] = [
      {
        key: 'customField1',
        label: 'Custom Field 1',
        fieldType: 'text',
        required: true,
        entityType: 'customer',
        sortOrder: 1,
        // Validation kommt aus Field Definition!
        validation: {
          minLength: 5,
          maxLength: 50
        }
      }
    ];
    
    act(() => {
      result.current.setFieldDefinitions(dynamicFields, []);
    });
    
    // Validiere basierend auf dynamischer Definition
    act(() => {
      result.current.setCustomerField('customField1', 'abc'); // zu kurz
      result.current.validateField('customField1');
    });
    
    // Flexibles System validiert dynamisch
    expect(result.current.validationErrors.customField1).toBeDefined();
  });
});
```

### 5. Integration Test mit Mock Server

```typescript
// ✅ RICHTIG - Mock Server akzeptiert beliebige Strukturen
describe('Customer API Integration', () => {
  it('should handle dynamic field structures from different industries', async () => {
    // Mock verschiedene Branchen-Responses
    server.use(
      rest.get('/api/customers/:id', (req, res, ctx) => {
        const responses: Record<string, any> = {
          'hotel-customer': {
            id: req.params.id,
            fieldValues: {
              companyName: 'Hotel Test',
              industry: 'hotel',
              hotelStars: 5,
              roomCount: 200,
              hasRestaurant: true,
              // Hotel-spezifische Felder
              checkInTime: '14:00',
              checkOutTime: '11:00',
              petFriendly: true
            }
          },
          'krankenhaus-customer': {
            id: req.params.id,
            fieldValues: {
              companyName: 'Klinik Test',
              industry: 'krankenhaus',
              bettenAnzahl: 500,
              notaufnahme: true,
              // Krankenhaus-spezifische Felder
              fachabteilungen: ['Kardiologie', 'Neurologie'],
              intensivbetten: 50
            }
          }
        };
        
        // Flexibler Response basierend auf ID
        const response = responses[req.params.id as string] || {
          id: req.params.id,
          fieldValues: {
            // Unbekannte Struktur ist OK!
            randomField1: 'value',
            nestedStuff: { works: true }
          }
        };
        
        return res(ctx.json(response));
      })
    );
    
    // Teste verschiedene Strukturen
    const hotelCustomer = await apiClient.get('/api/customers/hotel-customer');
    expect(hotelCustomer.fieldValues.hotelStars).toBe(5);
    
    const krankenhausCustomer = await apiClient.get('/api/customers/krankenhaus-customer');
    expect(krankenhausCustomer.fieldValues.bettenAnzahl).toBe(500);
  });
});
```

## 🚨 Anti-Patterns - Was NICHT zu tun ist

### ❌ FALSCH: Strikte Type Enforcement

```typescript
// ❌ FALSCH - Versucht Types zu erzwingen
interface StrictCustomerData {
  companyName: string;
  industry: 'hotel' | 'krankenhaus' | 'restaurant';
  // ... 100+ fields
}

it('should match strict schema', () => {
  // NEIN! Das widerspricht unserer Flexibilität!
  expect(customerData).toMatchObject(strictSchema);
});
```

### ❌ FALSCH: Import Cleanup Tests

```typescript
// ❌ FALSCH - Testet ungenutzte Imports
it('should not have unused imports', () => {
  const unusedImports = analyzeImports(componentFile);
  expect(unusedImports).toHaveLength(0); // NEIN!
});
```

### ❌ FALSCH: Type Guards überall

```typescript
// ❌ FALSCH - Over-Engineering mit Type Guards
function isValidCustomerData(data: any): data is StrictCustomerData {
  return typeof data.companyName === 'string' &&
         ['hotel', 'krankenhaus'].includes(data.industry) &&
         // ... 50 weitere Checks
}

// Nutze stattdessen die Flexibilität!
```

## 🎯 Zusammenfassung

Tests müssen:
1. **Die Flexibilität respektieren**
2. **Mit `any` arbeiten, nicht dagegen**
3. **Dynamische Strukturen akzeptieren**
4. **Funktionalität testen, nicht Types**

---

**Remember:** Unsere Flexibilität ist ein FEATURE, kein Bug!