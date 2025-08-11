/**
 * Wizard Navigation Component
 *
 * Navigation controls for the customer onboarding wizard.
 * Handles step progression, validation, and finalization.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md
 */

import React from 'react';
import { Box, Button, CircularProgress, Typography } from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  ArrowForward as ArrowForwardIcon,
  Check as CheckIcon,
} from '@mui/icons-material';

interface WizardNavigationProps {
  /** Current step index */
  currentStep: number;
  /** Total number of steps */
  totalSteps: number;
  /** Can progress to next step */
  canProgress: boolean;
  /** Is currently saving */
  isSaving?: boolean;
  /** Back button handler */
  onBack: () => void;
  /** Next button handler */
  onNext: () => void;
  /** Finish button handler */
  onFinish: () => void;
  /** Cancel button handler (optional for modal mode) */
  onCancel?: () => void;
}

/**
 * Wizard Navigation
 *
 * Step navigation with validation support.
 * Shows different actions based on current step.
 */
export const WizardNavigation: React.FC<WizardNavigationProps> = ({
  currentStep,
  totalSteps,
  canProgress,
  isSaving = false,
  onBack,
  onNext,
  onFinish,
  onCancel: _onCancel,
}) => {
  const isFirstStep = currentStep === 0;
  const isLastStep = currentStep === totalSteps - 1;

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        pt: 3,
        borderTop: 1,
        borderColor: 'divider',
      }}
    >
      {/* Back Button */}
      <Button
        variant="outlined"
        onClick={onBack}
        disabled={isFirstStep || isSaving}
        startIcon={<ArrowBackIcon />}
        sx={{ minWidth: 120 }}
      >
        Zurück
      </Button>

      {/* Step Indicator */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <Typography variant="body2" color="text.secondary">
          Schritt {currentStep + 1} von {totalSteps}
        </Typography>
      </Box>

      {/* Next/Finish Button */}
      {isLastStep ? (
        <Button
          variant="contained"
          color="primary"
          onClick={onFinish}
          disabled={!canProgress || isSaving}
          startIcon={isSaving ? <CircularProgress size={20} /> : <CheckIcon />}
          sx={{ minWidth: 140 }}
        >
          {isSaving ? 'Wird gespeichert...' : 'Abschließen'}
        </Button>
      ) : (
        <Button
          variant="contained"
          color="primary"
          onClick={onNext}
          disabled={!canProgress || isSaving}
          endIcon={<ArrowForwardIcon />}
          sx={{ minWidth: 120 }}
        >
          Weiter
        </Button>
      )}
    </Box>
  );
};
