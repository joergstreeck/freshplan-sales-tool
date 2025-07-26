# 📅 TAG 4: FRONTEND COMPONENTS

**Navigation:**
- **Parent:** [Implementation Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)
- **Previous:** [Tag 3: Frontend Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/03-day-3-frontend.md)
- **Next:** [Tag 5: Testing & Completion](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/05-day-5-testing.md)
- **Related:** [Frontend Components](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md)

## ✅ 17. Base Components (3h) - FERTIG

**Location:** `frontend/src/features/customers/components/`

- [x] `wizard/CustomerOnboardingWizard.tsx` ✅ **IMPLEMENTIERT am 26.07.2025**
  - Siehe: [CustomerOnboardingWizard.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/wizard/CustomerOnboardingWizard.tsx)
- [x] `wizard/WizardNavigation.tsx` ✅ **IMPLEMENTIERT**
  - Siehe: [WizardNavigation.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/wizard/WizardNavigation.tsx)
- [x] `steps/CustomerDataStep.tsx` ✅ **IMPLEMENTIERT**
  - Siehe: [CustomerDataStep.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/CustomerDataStep.tsx)
- [x] `steps/LocationsStep.tsx` ✅ **IMPLEMENTIERT**
  - Siehe: [LocationsStep.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/steps/LocationsStep.tsx)
- [x] `shared/SaveIndicator.tsx` ✅ **IMPLEMENTIERT**
  - Siehe: [SaveIndicator.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/shared/SaveIndicator.tsx)
- [x] `shared/LoadingScreen.tsx` ✅ **IMPLEMENTIERT**
  - Siehe: [LoadingScreen.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/shared/LoadingScreen.tsx)
```tsx
export const CustomerOnboardingWizard: React.FC = () => {
  const { currentStep, customerData, locations } = useCustomerOnboardingStore();
  const [activeStep, setActiveStep] = useState(currentStep);
  
  const steps = [
    { label: 'Kundendaten', component: CustomerDataStep },
    { label: 'Standorte', component: LocationsStep, condition: () => customerData.chainCustomer === 'ja' },
    { label: 'Details', component: DetailsStep }
  ];
  
  const visibleSteps = steps.filter(step => !step.condition || step.condition());
  
  return (
    <Box sx={{ width: '100%', maxWidth: 1200, mx: 'auto', p: 3 }}>
      <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
        {visibleSteps.map((step, index) => (
          <Step key={step.label}>
            <StepLabel>{step.label}</StepLabel>
          </Step>
        ))}
      </Stepper>
      
      <Paper elevation={1} sx={{ p: 3 }}>
        <Suspense fallback={<CircularProgress />}>
          {React.createElement(visibleSteps[activeStep].component)}
        </Suspense>
      </Paper>
      
      <WizardNavigation 
        currentStep={activeStep}
        totalSteps={visibleSteps.length}
        onNext={() => setActiveStep(prev => prev + 1)}
        onBack={() => setActiveStep(prev => prev - 1)}
        onFinalize={handleFinalize}
      />
      
      <SaveIndicator />
    </Box>
  );
};
```

- [ ] `wizard/WizardNavigation.tsx`
```tsx
interface WizardNavigationProps {
  currentStep: number;
  totalSteps: number;
  onNext: () => void;
  onBack: () => void;
  onFinalize: () => void;
}

export const WizardNavigation: React.FC<WizardNavigationProps> = ({
  currentStep,
  totalSteps,
  onNext,
  onBack,
  onFinalize
}) => {
  const { isDirty, isSaving, validateCurrentStep } = useCustomerOnboardingStore();
  
  const handleNext = async () => {
    const isValid = await validateCurrentStep();
    if (isValid) {
      await saveProgress();
      onNext();
    }
  };
  
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
      <Button
        onClick={onBack}
        disabled={currentStep === 0}
        startIcon={<ArrowBack />}
      >
        Zurück
      </Button>
      
      <Box sx={{ display: 'flex', gap: 2 }}>
        {currentStep === totalSteps - 1 ? (
          <Button
            variant="contained"
            color="primary"
            onClick={onFinalize}
            disabled={isSaving || isDirty}
            startIcon={<Check />}
          >
            Kunde anlegen
          </Button>
        ) : (
          <Button
            variant="contained"
            onClick={handleNext}
            disabled={isSaving}
            endIcon={<ArrowForward />}
          >
            Weiter
          </Button>
        )}
      </Box>
    </Box>
  );
};
```

- [ ] `shared/SaveIndicator.tsx`
```tsx
export const SaveIndicator: React.FC = () => {
  const { isDirty, isSaving, lastSaved } = useCustomerOnboardingStore();
  
  const getMessage = () => {
    if (isSaving) return 'Speichern...';
    if (isDirty) return 'Ungespeicherte Änderungen';
    if (lastSaved) {
      const timeAgo = formatDistanceToNow(lastSaved, { locale: de });
      return `Zuletzt gespeichert vor ${timeAgo}`;
    }
    return 'Alle Änderungen gespeichert';
  };
  
  return (
    <Box sx={{ 
      position: 'fixed', 
      bottom: 16, 
      right: 16,
      bgcolor: 'background.paper',
      boxShadow: 1,
      borderRadius: 1,
      px: 2,
      py: 1,
      display: 'flex',
      alignItems: 'center',
      gap: 1
    }}>
      {isSaving && <CircularProgress size={16} />}
      {!isSaving && isDirty && <Warning color="warning" fontSize="small" />}
      {!isSaving && !isDirty && <CheckCircle color="success" fontSize="small" />}
      <Typography variant="caption">{getMessage()}</Typography>
    </Box>
  );
};
```

## ✅ 18. Field Components (3h) - FERTIG

**Location:** `frontend/src/features/customers/components/fields/`

- [x] `DynamicFieldRenderer.tsx` ✅ **IMPLEMENTIERT am 26.07.2025**
  - Siehe: [DynamicFieldRenderer.tsx](/Users/joergstreeck/freshplan-sales-tool/frontend/src/features/customers/components/fields/DynamicFieldRenderer.tsx)
  - Dokumentation: [Field Rendering Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/03-field-rendering.md)
```tsx
interface DynamicFieldRendererProps {
  fields: FieldDefinition[];
  values: Record<string, any>;
  errors?: Record<string, string>;
  onChange: (key: string, value: any) => void;
  columns?: number;
}

export const DynamicFieldRenderer: React.FC<DynamicFieldRendererProps> = ({
  fields,
  values,
  errors = {},
  onChange,
  columns = 2
}) => {
  const renderField = (field: FieldDefinition) => {
    const commonProps = {
      value: values[field.key] || '',
      error: errors[field.key],
      onChange: (value: any) => onChange(field.key, value),
      required: field.required,
      disabled: field.disabled
    };
    
    switch (field.type) {
      case 'text':
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
        return null;
    }
  };
  
  return (
    <Grid container spacing={2}>
      {fields.map(field => (
        <Grid 
          key={field.key} 
          item 
          xs={12} 
          md={field.fullWidth ? 12 : 12 / columns}
        >
          <FieldWrapper field={field}>
            {renderField(field)}
          </FieldWrapper>
        </Grid>
      ))}
    </Grid>
  );
};
```

- [ ] `FieldWrapper.tsx`
```tsx
export const FieldWrapper: React.FC<{
  field: FieldDefinition;
  children: React.ReactNode;
}> = ({ field, children }) => {
  return (
    <FormControl fullWidth error={!!field.error}>
      <Box sx={{ mb: 0.5 }}>
        <Typography variant="body2" component="label">
          {field.label}
          {field.required && (
            <Typography component="span" color="error" sx={{ ml: 0.5 }}>
              *
            </Typography>
          )}
        </Typography>
        {field.helpText && (
          <Typography variant="caption" color="text.secondary">
            {field.helpText}
          </Typography>
        )}
      </Box>
      {children}
      {field.error && (
        <FormHelperText>{field.error}</FormHelperText>
      )}
    </FormControl>
  );
};
```

- [ ] Field type components implementieren
```tsx
// TextField.tsx
export const TextField: React.FC<FieldComponentProps> = ({
  field,
  value,
  onChange,
  error,
  required,
  disabled
}) => {
  return (
    <MuiTextField
      fullWidth
      value={value}
      onChange={(e) => onChange(e.target.value)}
      error={!!error}
      required={required}
      disabled={disabled}
      inputProps={{
        maxLength: field.validation?.maxLength
      }}
    />
  );
};

// SelectField.tsx
export const SelectField: React.FC<FieldComponentProps> = ({
  field,
  value,
  onChange,
  error,
  required,
  disabled
}) => {
  return (
    <Select
      fullWidth
      value={value}
      onChange={(e) => onChange(e.target.value)}
      error={!!error}
      required={required}
      disabled={disabled}
    >
      <MenuItem value="">
        <em>Bitte wählen</em>
      </MenuItem>
      {field.options?.map(option => (
        <MenuItem key={option.value} value={option.value}>
          {option.label}
        </MenuItem>
      ))}
    </Select>
  );
};
```

## 🔲 19. Step Components (2h)

**Location:** `frontend/src/features/customers/components/steps/`

- [ ] `CustomerDataStep.tsx`
```tsx
export const CustomerDataStep: React.FC = () => {
  const { 
    customerData, 
    updateField, 
    saveProgress 
  } = useCustomerOnboardingStore();
  
  const { data: fields, isLoading } = useFieldDefinitions('CUSTOMER');
  const [errors, setErrors] = useState<Record<string, string>>({});
  
  // Auto-save
  const debouncedSave = useDebouncedCallback(saveProgress, 2000);
  
  const handleFieldChange = (key: string, value: any) => {
    updateField(key, value);
    
    // Clear error for this field
    setErrors(prev => ({ ...prev, [key]: undefined }));
    
    // Trigger save
    debouncedSave();
    
    // Handle triggers
    if (key === 'chainCustomer' && value === 'ja') {
      // Store will handle showing locations step
    }
  };
  
  const validate = async () => {
    const schema = buildSchemaForFields(fields);
    try {
      await schema.parseAsync(customerData);
      return true;
    } catch (error) {
      if (error instanceof ZodError) {
        setErrors(formatZodErrors(error));
      }
      return false;
    }
  };
  
  if (isLoading) return <CustomerFormSkeleton />;
  
  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Kundendaten
      </Typography>
      
      <DynamicFieldRenderer
        fields={fields}
        values={customerData}
        errors={errors}
        onChange={handleFieldChange}
      />
      
      {customerData.industry && (
        <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
          <Typography variant="body2" color="text.secondary">
            <Info fontSize="small" sx={{ mr: 1, verticalAlign: 'middle' }} />
            Branchenspezifische Felder werden im nächsten Schritt angezeigt.
          </Typography>
        </Box>
      )}
    </Box>
  );
};
```

- [ ] Base für weitere Steps erstellen
```tsx
// Gemeinsame Hook für alle Steps
export const useStepValidation = (stepName: string) => {
  const store = useCustomerOnboardingStore();
  const [errors, setErrors] = useState<Record<string, string>>({});
  
  const validate = async (fields: FieldDefinition[], data: any) => {
    const schema = buildSchemaForFields(fields);
    try {
      await schema.parseAsync(data);
      setErrors({});
      return true;
    } catch (error) {
      if (error instanceof ZodError) {
        setErrors(formatZodErrors(error));
      }
      return false;
    }
  };
  
  useEffect(() => {
    // Register validation function in store
    store.registerStepValidation(stepName, validate);
  }, [stepName]);
  
  return { errors, setErrors };
};
```

## ✅ Checklist Ende Tag 4

- [ ] Wizard Navigation funktioniert
- [ ] Field Rendering dynamisch
- [ ] Auto-Save implementiert
- [ ] Validierung on-the-fly
- [ ] UI responsive

---

**Next:** [Tag 5: Testing & Completion →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/05-day-5-testing.md)