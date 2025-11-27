/**
 * ContactFormDialog Component - Server-Driven UI with Stepper Navigation
 *
 * Sprint 2.1.7.7: Vollständig Server-Driven Contact Form
 *
 * Modal dialog for creating and editing contacts.
 * Uses backend schema from GET /api/contacts/schema for dynamic field rendering.
 *
 * Architecture:
 * - Backend: ContactSchemaResource.java (Single Source of Truth)
 * - Frontend: DynamicFieldRenderer (renders what backend defines)
 * - Enums: Loaded from /api/enums/* endpoints
 *
 * Steps (3):
 * 1. Stammdaten (basic_info): Anrede, Name, Position, Kontaktdaten
 * 2. Beziehung (relationship): Geburtstag, Hobbies, Familie
 * 3. Professionell (social_business): LinkedIn, XING, Business Notizen
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */

import React, { useState, useEffect, useMemo } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Divider,
  Alert,
  FormControlLabel,
  Switch,
  Stepper,
  Step,
  StepLabel,
  useTheme,
  useMediaQuery,
  CircularProgress,
} from '@mui/material';

import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import { LocationCheckboxList } from '../shared/LocationCheckboxList';
import { useContactSchema } from '../../../../hooks/useContactSchema';

import type { Contact } from '../../types/contact.types';
import type { Location } from '../../types/location.types';

interface ContactFormDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (contact: Partial<Contact>) => void;
  contact?: Contact | null;
  locations?: Location[];
}

/**
 * Step configuration - maps to backend sections
 */
const STEP_CONFIG = [
  { sectionId: 'basic_info', label: 'Stammdaten' },
  { sectionId: 'relationship', label: 'Beziehung' },
  { sectionId: 'social_business', label: 'Professionell' },
] as const;

/**
 * Contact Form Dialog - Server-Driven with Stepper Navigation
 *
 * Multi-step wizard form using backend schema for field definitions.
 */
export const ContactFormDialog: React.FC<ContactFormDialogProps> = ({
  open,
  onClose,
  onSubmit,
  contact,
  locations = [],
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isEdit = !!contact;

  // ========== SERVER-DRIVEN SCHEMA ==========
  const { data: schemas, isLoading: schemaLoading, isError: schemaError } = useContactSchema();

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

  // ========== FORM STATE ==========
  const [formData, setFormData] = useState<Partial<Contact>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [activeStep, setActiveStep] = useState(0);

  // Initialize form data
  useEffect(() => {
    if (contact) {
      setFormData(contact);
    } else {
      setFormData({
        salutation: undefined,
        firstName: '',
        lastName: '',
        isPrimary: false,
        isActive: true,
        responsibilityScope: 'all',
        assignedLocationIds: [],
      });
    }
    setErrors({});
    setActiveStep(0);
  }, [contact, open]);

  // ========== EVENT HANDLERS ==========

  // Handle field change from DynamicFieldRenderer
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

  // Handle responsibility scope change
  const handleResponsibilityScopeChange = (scope: 'all' | 'specific') => {
    setFormData(prev => ({
      ...prev,
      responsibilityScope: scope,
      assignedLocationIds: scope === 'all' ? [] : prev.assignedLocationIds || [],
    }));
  };

  // Handle location assignment
  const handleLocationChange = (locationIds: string[]) => {
    setFormData(prev => ({
      ...prev,
      assignedLocationIds: locationIds,
    }));
  };

  // ========== STEP VALIDATION ==========

  /**
   * Validate current step fields
   */
  const validateCurrentStep = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (activeStep === 0) {
      // Step 1: Stammdaten - Required fields
      if (!formData.salutation) {
        newErrors.salutation = 'Anrede ist erforderlich';
      }
      if (!formData.firstName?.trim()) {
        newErrors.firstName = 'Vorname ist erforderlich';
      }
      if (!formData.lastName?.trim()) {
        newErrors.lastName = 'Nachname ist erforderlich';
      }
      // At least one contact method
      if (!formData.email && !formData.phone && !formData.mobile) {
        newErrors.email = 'Mindestens eine Kontaktmöglichkeit erforderlich';
      }
      // Email validation if provided
      if (formData.email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.email)) {
          newErrors.email = 'Ungültige E-Mail-Adresse';
        }
      }
      // Location assignment validation
      if (
        formData.responsibilityScope === 'specific' &&
        (!formData.assignedLocationIds || formData.assignedLocationIds.length === 0)
      ) {
        newErrors.assignedLocationIds = 'Bitte wählen Sie mindestens einen Standort';
      }
    }

    // Steps 2 & 3: No required fields, all optional

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * Validate all steps for final submission
   */
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Required fields (Step 1)
    if (!formData.salutation) {
      newErrors.salutation = 'Anrede ist erforderlich';
    }
    if (!formData.firstName?.trim()) {
      newErrors.firstName = 'Vorname ist erforderlich';
    }
    if (!formData.lastName?.trim()) {
      newErrors.lastName = 'Nachname ist erforderlich';
    }

    // At least one contact method
    if (!formData.email && !formData.phone && !formData.mobile) {
      newErrors.email = 'Mindestens eine Kontaktmöglichkeit erforderlich';
    }

    // Email validation
    if (formData.email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        newErrors.email = 'Ungültige E-Mail-Adresse';
      }
    }

    // Location assignment validation
    if (
      formData.responsibilityScope === 'specific' &&
      (!formData.assignedLocationIds || formData.assignedLocationIds.length === 0)
    ) {
      newErrors.assignedLocationIds = 'Bitte wählen Sie mindestens einen Standort';
    }

    setErrors(newErrors);

    // Navigate to step with first error
    if (Object.keys(newErrors).length > 0) {
      setActiveStep(0); // Errors are all in Step 1
    }

    return Object.keys(newErrors).length === 0;
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

  const handleSubmit = () => {
    if (validateForm()) {
      onSubmit(formData);
      onClose();
    }
  };

  // ========== RENDER ==========

  // Loading state
  if (schemaLoading) {
    return (
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
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
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogContent>
          <Alert severity="error">
            Fehler beim Laden des Kontakt-Schemas. Bitte versuchen Sie es später erneut.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Schließen</Button>
        </DialogActions>
      </Dialog>
    );
  }

  const currentStep = STEP_CONFIG[activeStep];
  const fields = getFieldsForSection(currentStep.sectionId);
  const isLastStep = activeStep === STEP_CONFIG.length - 1;
  const isFirstStep = activeStep === 0;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth fullScreen={isMobile}>
      <DialogTitle>{isEdit ? 'Kontakt bearbeiten' : 'Neuen Kontakt anlegen'}</DialogTitle>

      <DialogContent dividers>
        {/* Stepper Navigation */}
        <Stepper activeStep={activeStep} alternativeLabel sx={{ mb: 3 }}>
          {STEP_CONFIG.map(step => {
            const section = sections.find(s => s.sectionId === step.sectionId);
            return (
              <Step key={step.sectionId}>
                <StepLabel>{section?.title || step.label}</StepLabel>
              </Step>
            );
          })}
        </Stepper>

        {/* Step Content */}
        <Box sx={{ minHeight: 300 }}>
          {/* Server-Driven Field Rendering */}
          <DynamicFieldRenderer
            fields={fields}
            values={formData as Record<string, unknown>}
            errors={errors}
            onChange={handleFieldChange}
            onBlur={() => {}}
          />

          {/* Special handling for responsibility on Stammdaten step */}
          {currentStep.sectionId === 'basic_info' && locations.length > 1 && (
            <Box sx={{ mt: 3 }}>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="subtitle2" gutterBottom>
                Zuständigkeitsbereich
              </Typography>

              <Box sx={{ mb: 2 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.responsibilityScope === 'specific'}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                        handleResponsibilityScopeChange(e.target.checked ? 'specific' : 'all')
                      }
                    />
                  }
                  label="Nur für bestimmte Standorte zuständig"
                />
              </Box>

              {formData.responsibilityScope === 'specific' && (
                <>
                  <LocationCheckboxList
                    locations={locations}
                    selectedLocationIds={formData.assignedLocationIds || []}
                    onChange={handleLocationChange}
                  />
                  {errors.assignedLocationIds && (
                    <Alert severity="error" sx={{ mt: 1 }}>
                      {errors.assignedLocationIds}
                    </Alert>
                  )}
                </>
              )}
            </Box>
          )}

          {/* Primary Contact Option - Show on last step */}
          {isLastStep && (
            <Box sx={{ mt: 3 }}>
              <Divider sx={{ mb: 2 }} />
              <FormControlLabel
                control={
                  <Switch
                    checked={formData.isPrimary || false}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                      setFormData(prev => ({
                        ...prev,
                        isPrimary: e.target.checked,
                      }))
                    }
                  />
                }
                label="Als Hauptansprechpartner festlegen"
              />
            </Box>
          )}
        </Box>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={onClose}>Abbrechen</Button>
        <Box sx={{ flex: 1 }} />
        {!isFirstStep && (
          <Button onClick={handleBack} sx={{ mr: 1 }}>
            Zurück
          </Button>
        )}
        {isLastStep ? (
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={!formData.firstName || !formData.lastName}
          >
            {isEdit ? 'Speichern' : 'Kontakt anlegen'}
          </Button>
        ) : (
          <Button onClick={handleNext} variant="contained">
            Weiter
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};
