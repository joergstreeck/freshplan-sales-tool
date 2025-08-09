# 🎨 FC-005 FRONTEND - COMPONENTS

**Navigation:** [← Frontend Overview](README.md) | [State Management →](02-state-management.md)

---

**Datum:** 26.07.2025  
**Version:** 1.1  
**Stack:** React 18, TypeScript, Material-UI v7

## ⚠️ WICHTIG: MUI Grid v2 Syntax

**Seit MUI v7 verwenden wir ausschließlich Grid v2 Syntax:**
```typescript
// ✅ RICHTIG - Grid v2 Syntax (MUI v7)
import { Grid } from '@mui/material';

<Grid container spacing={3}>
  <Grid size={{ xs: 12, sm: 6, md: 4 }}>
    <Component />
  </Grid>
</Grid>

// ❌ FALSCH - Alte Grid v1 Syntax
<Grid item xs={12} sm={6} md={4}>  // item prop existiert nicht mehr!
```

**Migration Guide:** [DEBUG_COOKBOOK.md#mui-grid-v2](/Users/joergstreeck/freshplan-sales-tool/docs/guides/DEBUG_COOKBOOK.md#mui-grid-v2)  

## 📋 Inhaltsverzeichnis

1. [Component Architecture](#component-architecture)
2. [Core Components](#core-components)
3. [Step Components](#step-components)
4. [Shared Components](#shared-components)

## Component Architecture

### Folder Structure

```
frontend/src/features/customers/components/
├── wizard/
│   ├── CustomerOnboardingWizard.tsx    # Main wizard container
│   ├── WizardNavigation.tsx           # Navigation controls
│   └── WizardProgress.tsx             # Progress indicator
├── steps/
│   ├── CustomerDataStep.tsx           # Step 1: Stammdaten
│   ├── LocationsStep.tsx              # Step 2: Standorte
│   └── DetailedLocationsStep.tsx      # Step 3: Details
├── fields/
│   ├── DynamicFieldRenderer.tsx       # Field rendering engine
│   ├── FieldWrapper.tsx               # Field container
│   └── fieldTypes/                    # Specific field components
├── cards/
│   ├── LocationCard.tsx               # Location display card
│   └── DetailedLocationCard.tsx       # Detailed location card
└── shared/
    ├── ValidationMessage.tsx          # Error display
    ├── SaveIndicator.tsx              # Save status
    └── BranchIcon.tsx                 # Industry icons
```

## Core Components

### CustomerOnboardingWizard

Der Haupt-Container für den gesamten Onboarding-Prozess.

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
  
  // Dynamic step visibility based on customer data
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

### WizardNavigation

Navigation controls für den Wizard.

```typescript
interface WizardNavigationProps {
  currentStep: number;
  totalSteps: number;
  canProgress: boolean;
  onBack: () => void;
  onNext: () => void;
  onFinish: () => void;
}

export const WizardNavigation: React.FC<WizardNavigationProps> = ({
  currentStep,
  totalSteps,
  canProgress,
  onBack,
  onNext,
  onFinish
}) => {
  const isFirstStep = currentStep === 0;
  const isLastStep = currentStep === totalSteps - 1;
  
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
      <Button
        onClick={onBack}
        disabled={isFirstStep}
        startIcon={<ArrowBackIcon />}
      >
        Zurück
      </Button>
      
      <Box sx={{ flex: 1 }} />
      
      {!isLastStep ? (
        <Button
          variant="contained"
          onClick={onNext}
          disabled={!canProgress}
          endIcon={<ArrowForwardIcon />}
          sx={{ 
            bgcolor: '#94C456',
            '&:hover': { bgcolor: '#7AA342' }
          }}
        >
          Weiter
        </Button>
      ) : (
        <Button
          variant="contained"
          onClick={onFinish}
          disabled={!canProgress}
          endIcon={<CheckIcon />}
          sx={{ 
            bgcolor: '#004F7B',
            '&:hover': { bgcolor: '#003A5C' }
          }}
        >
          Abschließen
        </Button>
      )}
    </Box>
  );
};
```

## Step Components

### CustomerDataStep

Erster Schritt für Kundenstammdaten.

```typescript
export const CustomerDataStep: React.FC = () => {
  const { customerData, setFieldValue, validationErrors } = 
    useCustomerOnboardingStore();
  const { data: fields } = useFieldDefinitions('CUSTOMER');
  
  const handleFieldChange = (fieldKey: string, value: any) => {
    setFieldValue(fieldKey, value);
  };
  
  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>
        Kundenstammdaten
      </Typography>
      
      <DynamicFieldRenderer
        fields={fields || []}
        values={customerData}
        errors={validationErrors}
        onChange={handleFieldChange}
        onBlur={(fieldKey) => validateField(fieldKey)}
      />
      
      {/* Special handling for chain customer */}
      {customerData.get('chainCustomer') === 'ja' && (
        <Alert severity="info" sx={{ mt: 2 }}>
          <AlertTitle>Filialunternehmen</AlertTitle>
          Im nächsten Schritt können Sie die Standorte erfassen.
        </Alert>
      )}
    </Box>
  );
};
```

### LocationsStep

Zweiter Schritt für Standortverwaltung.

```typescript
export const LocationsStep: React.FC = () => {
  const { locations, addLocation, removeLocation } = 
    useCustomerOnboardingStore();
  const [showAddDialog, setShowAddDialog] = useState(false);
  
  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5">
          Standorte ({locations.length})
        </Typography>
        
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => setShowAddDialog(true)}
          sx={{ borderColor: '#94C456', color: '#94C456' }}
        >
          Standort hinzufügen
        </Button>
      </Box>
      
      <Grid container spacing={2}>
        {locations.map((location, index) => (
          <Grid item xs={12} md={6} key={location.id}>
            <LocationCard
              location={location}
              index={index + 1}
              onEdit={() => handleEditLocation(location.id)}
              onDelete={() => removeLocation(location.id)}
            />
          </Grid>
        ))}
      </Grid>
      
      {locations.length === 0 && (
        <EmptyState
          icon={<BusinessIcon />}
          title="Keine Standorte vorhanden"
          description="Fügen Sie mindestens einen Standort hinzu"
        />
      )}
      
      <AddLocationDialog
        open={showAddDialog}
        onClose={() => setShowAddDialog(false)}
        onAdd={addLocation}
      />
    </Box>
  );
};
```

## Shared Components

### SaveIndicator

Zeigt den Speicherstatus an.

```typescript
interface SaveIndicatorProps {
  isDirty: boolean;
  isSaving: boolean;
  lastSaved: Date | null;
}

export const SaveIndicator: React.FC<SaveIndicatorProps> = ({
  isDirty,
  isSaving,
  lastSaved
}) => {
  const getStatusIcon = () => {
    if (isSaving) return <CircularProgress size={16} />;
    if (isDirty) return <EditIcon fontSize="small" />;
    return <CheckCircleIcon fontSize="small" color="success" />;
  };
  
  const getStatusText = () => {
    if (isSaving) return 'Wird gespeichert...';
    if (isDirty) return 'Ungespeicherte Änderungen';
    if (lastSaved) {
      const timeAgo = formatDistanceToNow(lastSaved, { locale: de });
      return `Zuletzt gespeichert vor ${timeAgo}`;
    }
    return 'Alle Änderungen gespeichert';
  };
  
  return (
    <Box sx={{ 
      display: 'flex', 
      alignItems: 'center', 
      gap: 1,
      color: isDirty ? 'warning.main' : 'success.main'
    }}>
      {getStatusIcon()}
      <Typography variant="caption">
        {getStatusText()}
      </Typography>
    </Box>
  );
};
```

### BranchIcon

Industry-spezifische Icons.

```typescript
interface BranchIconProps {
  industry: string;
  size?: 'small' | 'medium' | 'large';
}

export const BranchIcon: React.FC<BranchIconProps> = ({ 
  industry, 
  size = 'medium' 
}) => {
  const iconSize = {
    small: 20,
    medium: 24,
    large: 32
  }[size];
  
  const getIcon = () => {
    switch (industry) {
      case 'hotel':
        return <HotelIcon />;
      case 'krankenhaus':
        return <LocalHospitalIcon />;
      case 'seniorenresidenz':
        return <ElderlyIcon />;
      case 'restaurant':
        return <RestaurantIcon />;
      case 'betriebsrestaurant':
        return <BusinessCenterIcon />;
      default:
        return <BusinessIcon />;
    }
  };
  
  return React.cloneElement(getIcon(), { 
    sx: { fontSize: iconSize } 
  });
};
```

---

**Parent:** [Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)  
**Related:** [State Management](02-state-management.md) | [Field Rendering](03-field-rendering.md) | [Validation](04-validation.md)