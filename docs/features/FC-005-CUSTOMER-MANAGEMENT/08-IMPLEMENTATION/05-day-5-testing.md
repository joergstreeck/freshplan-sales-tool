# 📅 TAG 5: TESTING & COMPLETION

**Navigation:**
- **Parent:** [Implementation Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)
- **Previous:** [Tag 4: Frontend Components](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/04-day-4-integration.md)
- **Related:** [Testing Strategy](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)

## 🔲 20. Additional Steps (3h)

### LocationsStep.tsx
```tsx
export const LocationsStep: React.FC = () => {
  const { 
    customerData,
    locations, 
    addLocation, 
    removeLocation,
    updateLocation 
  } = useCustomerOnboardingStore();
  
  const industry = customerData.industry;
  const { data: fields } = useFieldDefinitions('LOCATION', industry);
  
  const [expandedLocations, setExpandedLocations] = useState<string[]>([]);
  
  const handleAddLocation = () => {
    const newLocation: Partial<Location> = {
      id: generateTempId(),
      locationType: 'LIEFERADRESSE',
      name: `Standort ${locations.length + 1}`
    };
    addLocation(newLocation);
    setExpandedLocations([...expandedLocations, newLocation.id]);
  };
  
  const calculateTotals = () => {
    return locations.reduce((acc, loc) => ({
      rooms: acc.rooms + (loc.fieldValues?.roomCount || 0),
      seats: acc.seats + (loc.fieldValues?.seatCount || 0),
      revenue: acc.revenue + (loc.fieldValues?.expectedRevenue || 0)
    }), { rooms: 0, seats: 0, revenue: 0 });
  };
  
  const totals = calculateTotals();
  
  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5">Standorte</Typography>
        <Button
          variant="outlined"
          startIcon={<Add />}
          onClick={handleAddLocation}
        >
          Standort hinzufügen
        </Button>
      </Box>
      
      {locations.length === 0 ? (
        <EmptyState 
          message="Noch keine Standorte angelegt"
          action={handleAddLocation}
        />
      ) : (
        <>
          <Grid container spacing={2}>
            {locations.map((location, index) => (
              <Grid item xs={12} key={location.id}>
                <LocationCard
                  location={location}
                  index={index}
                  fields={fields}
                  expanded={expandedLocations.includes(location.id)}
                  onToggle={() => toggleExpanded(location.id)}
                  onUpdate={(updates) => updateLocation(location.id, updates)}
                  onRemove={() => removeLocation(location.id)}
                />
              </Grid>
            ))}
          </Grid>
          
          {industry === 'hotel' && (
            <TotalsSummary totals={totals} />
          )}
        </>
      )}
      
      {customerData.chainCustomer === 'ja' && locations.length > 5 && (
        <Alert severity="info" sx={{ mt: 2 }}>
          <AlertTitle>Detaillierte Standorte</AlertTitle>
          Bei mehr als 5 Standorten empfehlen wir die Nutzung der detaillierten 
          Standort-Verwaltung im nächsten Schritt.
        </Alert>
      )}
    </Box>
  );
};
```

### DetailedLocationsStep.tsx
```tsx
export const DetailedLocationsStep: React.FC = () => {
  const { 
    detailedLocations,
    addDetailedLocation,
    updateDetailedLocation,
    removeDetailedLocation,
    syncFromLocations
  } = useCustomerOnboardingStore();
  
  const [showSyncWarning, setShowSyncWarning] = useState(false);
  
  const handleSync = () => {
    setShowSyncWarning(true);
  };
  
  const confirmSync = () => {
    syncFromLocations();
    setShowSyncWarning(false);
  };
  
  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5">
          Detaillierte Standorte ({detailedLocations.length})
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            variant="outlined"
            startIcon={<Sync />}
            onClick={handleSync}
          >
            Aus Standorten übernehmen
          </Button>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => addDetailedLocation({})}
          >
            Hinzufügen
          </Button>
        </Box>
      </Box>
      
      <DetailedLocationTable
        locations={detailedLocations}
        onUpdate={updateDetailedLocation}
        onRemove={removeDetailedLocation}
      />
      
      <Dialog open={showSyncWarning} onClose={() => setShowSyncWarning(false)}>
        <DialogTitle>Standorte synchronisieren?</DialogTitle>
        <DialogContent>
          <Alert severity="warning">
            Dies überschreibt alle manuell eingegebenen detaillierten Standorte 
            mit den Daten aus dem vorherigen Schritt.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowSyncWarning(false)}>Abbrechen</Button>
          <Button onClick={confirmSync} color="primary" variant="contained">
            Übernehmen
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
```

## 🔲 21. Hooks & Utilities (2h)

**Location:** `frontend/src/features/customers/hooks/`

- [ ] `useCustomerOnboarding.ts`
```typescript
export const useCustomerOnboarding = () => {
  const store = useCustomerOnboardingStore();
  const queryClient = useQueryClient();
  
  // Initialize draft on mount
  useEffect(() => {
    if (!store.draftId) {
      store.createDraft();
    }
  }, []);
  
  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (store.isDirty) {
        store.saveProgress();
      }
    };
  }, []);
  
  // Handle browser close
  useBeforeUnload(
    store.isDirty,
    'Sie haben ungespeicherte Änderungen. Möchten Sie die Seite wirklich verlassen?'
  );
  
  return {
    ...store,
    isReady: !!store.draftId
  };
};
```

- [ ] `useFieldDefinitions.ts`
```typescript
export const useFieldDefinitions = (
  entityType: EntityType,
  industry?: string
) => {
  return useQuery({
    queryKey: ['field-definitions', entityType, industry],
    queryFn: () => fieldDefinitionApi.getFieldDefinitions(entityType, industry),
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000 // 10 minutes
  });
};
```

- [ ] `useAutoSave.ts`
```typescript
export const useAutoSave = (
  data: any,
  saveFn: (data: any) => Promise<void>,
  delay = 2000
) => {
  const [isSaving, setIsSaving] = useState(false);
  const [lastSaved, setLastSaved] = useState<Date | null>(null);
  
  const debouncedSave = useDebouncedCallback(
    async () => {
      setIsSaving(true);
      try {
        await saveFn(data);
        setLastSaved(new Date());
      } catch (error) {
        console.error('Auto-save failed:', error);
        // Show toast notification
      } finally {
        setIsSaving(false);
      }
    },
    delay
  );
  
  useEffect(() => {
    debouncedSave();
  }, [data]);
  
  return { isSaving, lastSaved };
};
```

**Location:** `frontend/src/features/customers/utils/`

- [ ] `fieldFormatters.ts`
```typescript
export const formatFieldValue = (
  value: any,
  fieldType: FieldType
): string => {
  if (value == null) return '';
  
  switch (fieldType) {
    case 'currency':
      return new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR'
      }).format(value);
      
    case 'date':
      return format(new Date(value), 'dd.MM.yyyy');
      
    case 'boolean':
      return value ? 'Ja' : 'Nein';
      
    default:
      return String(value);
  }
};
```

## 🔲 22. Testing (2h)

### Component Tests
```tsx
// DynamicFieldRenderer.test.tsx
describe('DynamicFieldRenderer', () => {
  it('renders all field types correctly', () => {
    const fields: FieldDefinition[] = [
      { key: 'name', type: 'text', label: 'Name' },
      { key: 'age', type: 'number', label: 'Age' },
      { key: 'active', type: 'boolean', label: 'Active' }
    ];
    
    const { getByLabelText } = render(
      <DynamicFieldRenderer
        fields={fields}
        values={{}}
        onChange={jest.fn()}
      />
    );
    
    expect(getByLabelText('Name')).toBeInTheDocument();
    expect(getByLabelText('Age')).toBeInTheDocument();
    expect(getByLabelText('Active')).toBeInTheDocument();
  });
  
  it('handles validation errors', () => {
    const errors = { name: 'Name ist erforderlich' };
    
    const { getByText } = render(
      <DynamicFieldRenderer
        fields={[{ key: 'name', type: 'text', label: 'Name' }]}
        values={{}}
        errors={errors}
        onChange={jest.fn()}
      />
    );
    
    expect(getByText('Name ist erforderlich')).toBeInTheDocument();
  });
});
```

### Store Tests
```typescript
// customerOnboardingStore.test.ts
describe('CustomerOnboardingStore', () => {
  it('creates draft and saves to localStorage', async () => {
    const { result } = renderHook(() => useCustomerOnboardingStore());
    
    await act(async () => {
      await result.current.createDraft();
    });
    
    expect(result.current.draftId).toBeTruthy();
    expect(localStorage.getItem('customer-onboarding')).toBeTruthy();
  });
  
  it('triggers location step when chainCustomer is set', () => {
    const { result } = renderHook(() => useCustomerOnboardingStore());
    
    act(() => {
      result.current.updateField('chainCustomer', 'ja');
    });
    
    expect(result.current.showLocationStep).toBe(true);
  });
});
```

## 🔲 23. E2E Test (1h)

```typescript
// e2e/customer-onboarding.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Customer Onboarding', () => {
  test('complete flow for chain customer', async ({ page }) => {
    // Login
    await page.goto('/login');
    await page.fill('[name="username"]', 'testuser');
    await page.fill('[name="password"]', 'password');
    await page.click('[type="submit"]');
    
    // Navigate to customer creation
    await page.goto('/customers/new');
    
    // Step 1: Customer data
    await page.fill('[name="companyName"]', 'Test Hotel GmbH');
    await page.selectOption('[name="industry"]', 'hotel');
    await page.click('[name="chainCustomer"][value="ja"]');
    await page.fill('[name="email"]', 'test@hotel.de');
    
    await page.click('button:has-text("Weiter")');
    
    // Step 2: Locations (should appear because chainCustomer = ja)
    await expect(page.locator('h5:has-text("Standorte")')).toBeVisible();
    
    await page.click('button:has-text("Standort hinzufügen")');
    await page.fill('[name="locations.0.name"]', 'Hauptstandort');
    await page.fill('[name="locations.0.roomCount"]', '50');
    
    await page.click('button:has-text("Weiter")');
    
    // Step 3: Details
    await page.click('button:has-text("Kunde anlegen")');
    
    // Verify success
    await expect(page).toHaveURL(/\/customers\/\w+/);
    await expect(page.locator('text=Kunde erfolgreich angelegt')).toBeVisible();
  });
  
  test('draft recovery after browser refresh', async ({ page }) => {
    await page.goto('/customers/new');
    
    // Fill some data
    await page.fill('[name="companyName"]', 'Draft Test GmbH');
    
    // Wait for auto-save
    await page.waitForTimeout(3000);
    
    // Refresh page
    await page.reload();
    
    // Check if draft is recovered
    await expect(page.locator('[name="companyName"]')).toHaveValue('Draft Test GmbH');
  });
});
```

## 🚀 Post-Implementation

### Performance Verification
- [ ] Lighthouse CI check
- [ ] Bundle size analysis
- [ ] API response times
- [ ] Memory profiling

### Security Audit
- [ ] OWASP checklist
- [ ] Permission tests
- [ ] Input validation
- [ ] XSS prevention

### Documentation
- [ ] Update API docs
- [ ] User guide
- [ ] Admin guide
- [ ] Troubleshooting

### Deployment Preparation
- [ ] Environment variables
- [ ] Feature flags
- [ ] Migration plan
- [ ] Rollback strategy

## ✅ Final Checklist

- [ ] Alle Features implementiert
- [ ] Test Coverage > 80%
- [ ] E2E Tests grün
- [ ] Performance Targets erreicht
- [ ] Security Audit passed
- [ ] Dokumentation vollständig
- [ ] Code Review abgeschlossen
- [ ] Ready for Production

---

**Herzlichen Glückwunsch! FC-005 Customer Management ist bereit für den Produktivbetrieb! 🎉**

**Stand:** 26.07.2025