/**
 * Create Branch Dialog Component - Server-Driven UI
 *
 * Sprint 2.1.7.7 D4: CreateBranchDialog - Multi-Location Management
 *
 * Server-Driven Dialog zum Anlegen neuer Filialen (FILIALE) unter einem HEADQUARTER-Kunden.
 * Backend ist Single Source of Truth für Field Definitions.
 *
 * Features:
 * - Server-Driven Schema (useBranchSchema)
 * - Stepper mit 2 Schritten: Basisdaten + Adresse & Kontakt
 * - DynamicFieldRenderer für dynamisches Rendering
 * - Vollständige Validierung
 * - POST /api/customers/{headquarterId}/branches
 * - Design System konform (MUI Theme)
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */

import React, { useState, useEffect, useMemo } from 'react';
import {
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Alert,
  useTheme,
  useMediaQuery,
  Typography,
  CircularProgress,
  Stepper,
  Step,
  StepLabel,
} from '@mui/material';
import { useCreateBranch } from '../../../customer/api/customerQueries';
import { useBranchSchema } from '../../../../hooks/useBranchSchema';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';

interface CreateBranchDialogProps {
  /** Dialog open state */
  open: boolean;
  /** Close dialog callback */
  onClose: () => void;
  /** UUID of the parent HEADQUARTER customer */
  headquarterId: string;
  /** Optional: Name des Headquarters für Anzeige */
  headquarterName?: string;
  /** Optional success callback */
  onSuccess?: () => void;
}

/**
 * Step configuration - maps to backend sections
 */
const STEP_CONFIG = [
  { sectionId: 'basic_info', label: 'Basisdaten' },
  { sectionId: 'address_contact', label: 'Adresse & Kontakt' },
] as const;

/**
 * CreateBranchDialog - Server-Driven Wizard für Filialanlage
 *
 * Zwei Schritte (from backend schema):
 * 1. Basisdaten: Firmenname, BusinessType, Status, Umsatz
 * 2. Adresse & Kontakt: Straße, PLZ, Stadt, Land, Telefon, E-Mail
 */
export const CreateBranchDialog: React.FC<CreateBranchDialogProps> = ({
  open,
  onClose,
  headquarterId,
  headquarterName,
  onSuccess,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  // ========== SERVER-DRIVEN SCHEMA ==========
  const { data: schemas, isLoading: schemaLoading, isError: schemaError } = useBranchSchema();

  // Get sections from schema
  const sections = useMemo(() => {
    if (!schemas || schemas.length === 0) return [];
    return schemas[0]?.sections || [];
  }, [schemas]);

  // Get fields for a specific section
  const getFieldsForSection = (sectionId: string) => {
    const section = sections.find(s => s.sectionId === sectionId);
    return section?.fields || [];
  };

  // ========== STEPPER STATE ==========
  const [activeStep, setActiveStep] = useState(0);

  // ========== FORM STATE ==========
  const [formData, setFormData] = useState<Record<string, unknown>>({
    customerType: 'UNTERNEHMEN',
    status: 'PROSPECT',
    country: 'DE',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  // ========== REACT QUERY MUTATION ==========
  const createBranchMutation = useCreateBranch();

  // ========== EFFECTS ==========

  /**
   * Reset form when dialog opens/closes
   */
  useEffect(() => {
    if (open) {
      setFormData({
        customerType: 'UNTERNEHMEN',
        status: 'PROSPECT',
        country: 'DE',
      });
      setErrors({});
      setActiveStep(0);
    }
  }, [open]);

  // ========== EVENT HANDLERS ==========

  /**
   * Handle field change from DynamicFieldRenderer
   */
  const handleFieldChange = (fieldKey: string, value: unknown) => {
    setFormData(prev => ({
      ...prev,
      [fieldKey]: value,
    }));

    // Clear error for this field
    if (errors[fieldKey]) {
      setErrors(prev => {
        const next = { ...prev };
        delete next[fieldKey];
        return next;
      });
    }
  };

  // ========== STEP VALIDATION ==========

  /**
   * Validate Step 1: Basisdaten
   */
  const validateStep1 = (): boolean => {
    const newErrors: Record<string, string> = {};

    const companyName = formData.companyName as string;
    if (!companyName?.trim()) {
      newErrors.companyName = 'Firmenname ist erforderlich';
    } else if (companyName.trim().length < 2) {
      newErrors.companyName = 'Firmenname muss mindestens 2 Zeichen lang sein';
    }

    if (!formData.businessType) {
      newErrors.businessType = 'Bitte Geschäftsart auswählen';
    }

    // Validate expected annual volume if provided
    const volume = formData.expectedAnnualVolume as string;
    if (volume) {
      const numVolume = parseFloat(
        String(volume)
          .replace(/[^\d.,]/g, '')
          .replace(',', '.')
      );
      if (isNaN(numVolume) || numVolume < 0) {
        newErrors.expectedAnnualVolume = 'Bitte gültigen Betrag eingeben';
      }
    }

    setErrors(prev => ({ ...prev, ...newErrors }));
    return Object.keys(newErrors).length === 0;
  };

  /**
   * Validate Step 2: Adresse & Kontakt
   */
  const validateStep2 = (): boolean => {
    const newErrors: Record<string, string> = {};

    const city = formData.city as string;
    if (!city?.trim()) {
      newErrors.city = 'Stadt ist erforderlich';
    }

    if (!formData.country) {
      newErrors.country = 'Land ist erforderlich';
    }

    // Validate email if provided
    const email = formData.email as string;
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      newErrors.email = 'Bitte gültige E-Mail-Adresse eingeben';
    }

    // Validate postal code for Germany
    const postalCode = formData.postalCode as string;
    if (formData.country === 'DE' && postalCode) {
      if (!/^\d{5}$/.test(postalCode)) {
        newErrors.postalCode = 'Deutsche PLZ muss 5 Ziffern haben';
      }
    }

    setErrors(prev => ({ ...prev, ...newErrors }));
    return Object.keys(newErrors).length === 0;
  };

  /**
   * Validate current step
   */
  const validateCurrentStep = (): boolean => {
    if (activeStep === 0) {
      return validateStep1();
    }
    return validateStep2();
  };

  // ========== NAVIGATION ==========

  const handleNext = () => {
    if (validateCurrentStep()) {
      setActiveStep(prev => prev + 1);
    }
  };

  const handleBack = () => {
    setActiveStep(prev => prev - 1);
  };

  /**
   * Handle form submit
   */
  const handleSubmit = async () => {
    if (!validateStep1() || !validateStep2()) {
      return;
    }

    try {
      // Parse expected annual volume
      let expectedAnnualVolume: number | undefined;
      const volume = formData.expectedAnnualVolume as string;
      if (volume) {
        expectedAnnualVolume = parseFloat(
          String(volume)
            .replace(/[^\d.,]/g, '')
            .replace(',', '.')
        );
      }

      await createBranchMutation.mutateAsync({
        headquarterId,
        branchData: {
          companyName: (formData.companyName as string)?.trim(),
          tradingName: (formData.tradingName as string)?.trim() || undefined,
          businessType: (formData.businessType as string) || undefined,
          customerType: formData.customerType as string,
          status: formData.status as string,
          expectedAnnualVolume,
          address: {
            street: (formData.street as string)?.trim() || undefined,
            postalCode: (formData.postalCode as string)?.trim() || undefined,
            city: (formData.city as string)?.trim() || undefined,
            country: (formData.country as string) || undefined,
          },
          contact: {
            phone: (formData.phone as string)?.trim() || undefined,
            email: (formData.email as string)?.trim() || undefined,
          },
        },
      });

      // Success - trigger callback and close
      onSuccess?.();
      onClose();
    } catch (error) {
      console.error('Error creating branch:', error);

      const errorMessage =
        error instanceof Error
          ? error.message
          : 'Fehler beim Anlegen der Filiale. Bitte versuchen Sie es erneut.';

      setErrors({
        submit: errorMessage,
      });
    }
  };

  /**
   * Handle dialog close
   */
  const handleClose = () => {
    if (!createBranchMutation.isPending) {
      onClose();
    }
  };

  // ========== COMPUTED ==========

  const isLastStep = activeStep === STEP_CONFIG.length - 1;
  const isFirstStep = activeStep === 0;
  const companyName = formData.companyName as string;
  const city = formData.city as string;
  const canProceed =
    activeStep === 0
      ? companyName?.trim()?.length >= 2 && formData.businessType
      : city?.trim()?.length > 0 && formData.country;

  // ========== RENDER ==========

  // Loading state
  if (schemaLoading) {
    return (
      <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
        <DialogContent>
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 4 }}>
            <CircularProgress />
            <Typography sx={{ ml: 2 }}>Schema wird geladen...</Typography>
          </Box>
        </DialogContent>
      </Dialog>
    );
  }

  // Error state
  if (schemaError) {
    return (
      <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
        <DialogContent>
          <Alert severity="error">
            Fehler beim Laden des Branch-Schemas. Bitte versuchen Sie es später erneut.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Schließen</Button>
        </DialogActions>
      </Dialog>
    );
  }

  const currentStep = STEP_CONFIG[activeStep];
  const fields = getFieldsForSection(currentStep.sectionId);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth fullScreen={isMobile}>
      <DialogTitle>
        <Typography variant="h6" component="div">
          Neue Filiale anlegen
        </Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
          {headquarterName
            ? `Filiale für "${headquarterName}"`
            : 'Filiale wird automatisch mit dem Headquarter verknüpft'}
        </Typography>
      </DialogTitle>

      <DialogContent dividers>
        {/* Error Alert */}
        {errors.submit && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {errors.submit}
          </Alert>
        )}

        {/* Stepper Navigation */}
        <Stepper activeStep={activeStep} sx={{ mb: 3 }}>
          {STEP_CONFIG.map(step => {
            const section = sections.find(s => s.sectionId === step.sectionId);
            return (
              <Step key={step.sectionId}>
                <StepLabel>{section?.title || step.label}</StepLabel>
              </Step>
            );
          })}
        </Stepper>

        {/* Step Content - Server-Driven Field Rendering */}
        <Box sx={{ mt: 2, minHeight: 300 }}>
          <DynamicFieldRenderer
            fields={fields}
            values={formData}
            errors={errors}
            onChange={handleFieldChange}
            onBlur={() => {}}
            loading={createBranchMutation.isPending}
          />
        </Box>

        {/* Info Box */}
        {isLastStep && (
          <Alert severity="info" sx={{ mt: 3 }}>
            <Typography variant="body2">
              <strong>Hinweis:</strong> Die Filiale wird automatisch angelegt mit:
            </Typography>
            <Box component="ul" sx={{ mt: 1, mb: 0, pl: 2 }}>
              <li>
                <Typography variant="body2">hierarchyType = FILIALE</Typography>
              </li>
              <li>
                <Typography variant="body2">
                  Verknüpfung zum Headquarter (parentCustomerId)
                </Typography>
              </li>
              <li>
                <Typography variant="body2">
                  Übernahme der xentralCustomerId vom Headquarter
                </Typography>
              </li>
            </Box>
          </Alert>
        )}
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={handleClose} disabled={createBranchMutation.isPending}>
          Abbrechen
        </Button>

        <Box sx={{ flex: 1 }} />

        {!isFirstStep && (
          <Button onClick={handleBack} disabled={createBranchMutation.isPending} sx={{ mr: 1 }}>
            Zurück
          </Button>
        )}

        {isLastStep ? (
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={createBranchMutation.isPending || !canProceed}
          >
            {createBranchMutation.isPending ? 'Speichert...' : 'Filiale anlegen'}
          </Button>
        ) : (
          <Button onClick={handleNext} variant="contained" disabled={!canProceed}>
            Weiter
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

// Explicit re-export for Vite HMR
export default CreateBranchDialog;
