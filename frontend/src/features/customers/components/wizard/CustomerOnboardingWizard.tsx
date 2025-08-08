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
import { Box, Paper, Stepper, Step, StepLabel, Typography, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import { useAutoSave } from '../../hooks/useAutoSave';
import { SaveIndicator } from '../shared/SaveIndicator';
import { LoadingScreen } from '../shared/LoadingScreen';
import { WizardNavigation } from './WizardNavigation';
import { CustomerDataStep } from '../steps/CustomerDataStep';
import { LocationsStep } from '../steps/LocationsStep';
import { Step1BasisFilialstruktur } from '../steps/Step1BasisFilialstruktur';
import { Step2HerausforderungenPotenzialV3 } from '../steps/Step2HerausforderungenPotenzialV3';
import { Step3MultiContactManagement } from '../steps/Step3MultiContactManagement';
import { Step4AngebotServices } from '../steps/Step4AngebotServices';
import { CustomerFieldThemeProvider } from '../../theme';

// DEBUG: Field Theme System
import { debugCustomerFieldTheme } from '../../utils/debugFieldTheme';

/**
 * Wizard step configuration - Verkaufsfokussierte Struktur
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V2.md
 */
const WIZARD_STEPS = [
  {
    id: 'basis-filialstruktur',
    label: 'Basis & Filialstruktur',
    description: 'Unternehmensdaten und Standortverteilung',
    icon: '🏢',
  },
  {
    id: 'herausforderungen-potenzial',
    label: 'Herausforderungen & Potenzial',
    description: 'Unternehmensweite Herausforderungen und Umsatzerwartung',
    icon: '🎯',
  },
  {
    id: 'ansprechpartner',
    label: 'Ansprechpartner',
    description: 'Kontaktpersonen und Kommunikation',
    icon: '👥',
  },
  {
    id: 'angebot-services',
    label: 'Angebot & Leistungen',
    description: 'Services je Filiale',
    icon: '🏢',
  },
];

interface CustomerOnboardingWizardProps {
  onComplete?: (customer: any) => void;
  onCancel?: () => void;
  isModal?: boolean;
}

/**
 * Customer Onboarding Wizard
 *
 * Central component for creating new customers.
 * Manages wizard flow, validation, and draft persistence.
 */
export const CustomerOnboardingWizard: React.FC<CustomerOnboardingWizardProps> = ({
  onComplete,
  onCancel,
  isModal = false,
}) => {
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
    reset,
  } = useCustomerOnboardingStore();

  const { isLoading: loadingFields, error: fieldError } = useFieldDefinitions();
  const { save: manualSave } = useAutoSave({ enabled: true, delay: 2000 });

  // DEBUG: Log field theme on mount
  useEffect(() => {
    if (process.env.NODE_ENV === 'development') {
      debugCustomerFieldTheme();
    }
  }, []);

  // All steps are visible in the new structure - Step 3 is optional but still shown
  const visibleSteps = WIZARD_STEPS;

  /**
   * Handle step navigation
   */
  const handleNext = async () => {
    console.log('handleNext Debug:', {
      currentStep,
      customerData,
      canProgress: canProgressToNextStep(),
    });

    const isValid = await validateCurrentStep();
    console.log('Validation result:', isValid);

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

      const customer = await finalizeCustomer();

      // If modal mode, call onComplete callback
      if (isModal && onComplete) {
        onComplete(customer);
        reset(); // Reset wizard state
      } else {
        // Navigate to success page or customer list
        navigate('/customers', {
          state: { message: 'Kunde erfolgreich angelegt!' },
        });
      }
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
      case 'basis-filialstruktur':
        return <Step1BasisFilialstruktur />;
      case 'herausforderungen-potenzial':
        return <Step2HerausforderungenPotenzialV3 />;
      case 'ansprechpartner':
        return <Step3MultiContactManagement />;
      case 'angebot-services':
        return <Step4AngebotServices />;
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
        <Alert severity="error">Fehler beim Laden der Felddefinitionen: {fieldError.message}</Alert>
      </Box>
    );
  }

  const content = (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      {/* Save Indicator */}
      <SaveIndicator isDirty={isDirty} isSaving={isSaving} lastSaved={lastSaved} />

      {/* Header - only show if not in modal */}
      {!isModal && (
        <Typography variant="h4" component="h1" gutterBottom>
          Neuen Kunden anlegen
        </Typography>
      )}

      {draftId && !customerData.customerNumber && (
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Entwurf-ID: {draftId}
        </Typography>
      )}

      {customerData.customerNumber && (
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Kundennummer: {customerData.customerNumber}
        </Typography>
      )}

      {/* Stepper */}
      <Stepper activeStep={currentStep} sx={{ my: 4 }}>
        {visibleSteps.map((step, index) => (
          <Step key={step.id} completed={index < currentStep}>
            <StepLabel>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                {step.icon && <span>{step.icon}</span>}
                <span>{step.label}</span>
                {step.optional && (
                  <Typography variant="caption" color="text.secondary">
                    (Optional)
                  </Typography>
                )}
              </Box>
            </StepLabel>
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
      <Box sx={{ mb: 4 }}>{renderStep()}</Box>

      {/* Navigation */}
      <WizardNavigation
        currentStep={currentStep}
        totalSteps={visibleSteps.length}
        canProgress={canProgressToNextStep()}
        isSaving={finalizing}
        onBack={handleBack}
        onNext={handleNext}
        onFinish={handleFinish}
        onCancel={isModal ? onCancel : undefined}
      />
    </CustomerFieldThemeProvider>
  );

  // If modal mode, return content without wrapper
  if (isModal) {
    return content;
  }

  // Otherwise, wrap in Paper
  return (
    <Box sx={{ width: '100%', maxWidth: 1200, mx: 'auto', p: 3 }}>
      <Paper elevation={2} sx={{ p: 4, position: 'relative' }}>
        {content}
      </Paper>
    </Box>
  );
};
