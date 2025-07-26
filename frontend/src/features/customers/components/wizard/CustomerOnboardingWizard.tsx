/**
 * Customer Onboarding Wizard Component
 * 
 * Main container for the customer onboarding process.
 * Implements the 3-step wizard flow with dynamic step visibility.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/README.md
 */

import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Paper, 
  Stepper, 
  Step, 
  StepLabel,
  Typography,
  Alert
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { useAutoSave } from '../../hooks/useAutoSave';
import { SaveIndicator } from '../shared/SaveIndicator';
import { LoadingScreen } from '../shared/LoadingScreen';
import { WizardNavigation } from './WizardNavigation';
import { CustomerDataStep } from '../steps/CustomerDataStep';
import { LocationsStep } from '../steps/LocationsStep';

/**
 * Wizard step configuration
 */
const WIZARD_STEPS = [
  { 
    id: 'customer', 
    label: 'Kundenstammdaten',
    description: 'Grundlegende Informationen zum Kunden'
  },
  { 
    id: 'locations', 
    label: 'Standorte',
    description: 'Standorte für Filialunternehmen',
    condition: (data: Record<string, any>) => data.chainCustomer === 'ja'
  },
  { 
    id: 'details', 
    label: 'Details',
    description: 'Zusätzliche Details und Ausgabestellen',
    condition: (data: Record<string, any>) => data.chainCustomer === 'ja' && data.hasDetailedLocations
  }
];

/**
 * Customer Onboarding Wizard
 * 
 * Central component for creating new customers.
 * Manages wizard flow, validation, and draft persistence.
 */
export const CustomerOnboardingWizard: React.FC = () => {
  const navigate = useNavigate();
  const [finalizing, setFinalizing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const {
    currentStep,
    customerData,
    isDirty,
    lastSaved,
    draftId,
    isLoading,
    isSaving,
    setCurrentStep,
    validateCurrentStep,
    canProgressToNextStep,
    finalizeCustomer,
    reset
  } = useCustomerOnboardingStore();
  
  const { isLoading: loadingFields, error: fieldError } = useFieldDefinitions();
  const { save: manualSave } = useAutoSave({ enabled: true, delay: 2000 });
  
  // Filter visible steps based on conditions
  const visibleSteps = WIZARD_STEPS.filter(step => 
    !step.condition || step.condition(customerData)
  );
  
  // Adjust current step if conditions change
  useEffect(() => {
    if (currentStep >= visibleSteps.length && visibleSteps.length > 0) {
      setCurrentStep(visibleSteps.length - 1);
    }
  }, [currentStep, visibleSteps.length, setCurrentStep]);
  
  /**
   * Handle step navigation
   */
  const handleNext = async () => {
    const isValid = validateCurrentStep();
    if (isValid) {
      await manualSave();
      setCurrentStep(currentStep + 1);
    }
  };
  
  const handleBack = () => {
    setCurrentStep(currentStep - 1);
  };
  
  /**
   * Handle wizard completion
   */
  const handleFinish = async () => {
    try {
      setFinalizing(true);
      setError(null);
      
      const isValid = validateCurrentStep();
      if (!isValid) {
        setError('Bitte füllen Sie alle Pflichtfelder aus.');
        return;
      }
      
      await finalizeCustomer();
      
      // Navigate to success page or customer list
      navigate('/customers', { 
        state: { message: 'Kunde erfolgreich angelegt!' }
      });
    } catch (err) {
      setError('Fehler beim Speichern des Kunden. Bitte versuchen Sie es erneut.');
      console.error('Failed to finalize customer:', err);
    } finally {
      setFinalizing(false);
    }
  };
  
  /**
   * Render current step component
   */
  const renderStep = () => {
    const stepConfig = visibleSteps[currentStep];
    if (!stepConfig) return null;
    
    switch (stepConfig.id) {
      case 'customer':
        return <CustomerDataStep />;
      case 'locations':
        return <LocationsStep />;
      case 'details':
        // DetailedLocationsStep would be implemented here
        return (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h6" color="text.secondary">
              Details-Step (Coming Soon)
            </Typography>
          </Box>
        );
      default:
        return null;
    }
  };
  
  // Loading state
  if (isLoading || loadingFields) {
    return <LoadingScreen message="Formular wird geladen..." />;
  }
  
  // Error state
  if (fieldError) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          Fehler beim Laden der Felddefinitionen: {fieldError.message}
        </Alert>
      </Box>
    );
  }
  
  return (
    <Box sx={{ width: '100%', maxWidth: 1200, mx: 'auto', p: 3 }}>
      <Paper elevation={2} sx={{ p: 4, position: 'relative' }}>
        {/* Save Indicator */}
        <SaveIndicator 
          isDirty={isDirty}
          isSaving={isSaving}
          lastSaved={lastSaved}
        />
        
        {/* Header */}
        <Typography variant="h4" component="h1" gutterBottom>
          Neuen Kunden anlegen
        </Typography>
        
        {draftId && (
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Entwurf-ID: {draftId}
          </Typography>
        )}
        
        {/* Stepper */}
        <Stepper activeStep={currentStep} sx={{ my: 4 }}>
          {visibleSteps.map((step, index) => (
            <Step key={step.id} completed={index < currentStep}>
              <StepLabel>{step.label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        {/* Error Display */}
        {error && (
          <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}
        
        {/* Step Content */}
        <Box sx={{ mb: 4 }}>
          {renderStep()}
        </Box>
        
        {/* Navigation */}
        <WizardNavigation
          currentStep={currentStep}
          totalSteps={visibleSteps.length}
          canProgress={canProgressToNextStep()}
          isSaving={finalizing}
          onBack={handleBack}
          onNext={handleNext}
          onFinish={handleFinish}
        />
      </Paper>
    </Box>
  );
};