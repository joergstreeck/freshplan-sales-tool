/**
 * Contact Edit Dialog Component
 *
 * Sprint 2.1.7.7: Refactored to use DynamicFieldRenderer (Server-Driven UI)
 *
 * Schema-driven Dialog zum Anlegen/Bearbeiten von Kundenkontakten.
 * Fetches schema from ContactSchemaResource (/api/contacts/schema).
 *
 * Features:
 * - Server-Driven UI: Backend controls form structure via schema
 * - DynamicFieldRenderer: Generic field rendering (no hardcoded enum mappings)
 * - 3 Sections: basic_info, relationship, social_business
 * - Validation from schema (required fields)
 * - Async submit with loading state
 *
 * Used by: CustomerDetailTabVerlauf.tsx
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React, { useState, useEffect, useMemo } from 'react';
import {
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControlLabel,
  Checkbox,
  Alert,
  useTheme,
  useMediaQuery,
  CircularProgress,
  Typography,
  Divider,
} from '@mui/material';
import { useContactSchema } from '../../../../hooks/useContactSchema';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import { normalizeDecisionLevel } from '../../../../hooks/useContactEnums';
import { useCustomerLocations } from '../../services/hooks';

export interface Contact {
  id?: string;
  // Basic Info (Section 1: basic_info)
  salutation?: string; // HERR, FRAU, DIVERS
  title?: string; // Dr., Prof., etc.
  firstName: string;
  lastName: string;
  position?: string; // Küchenchef, Einkaufsleiter, etc.
  decisionLevel?: string; // EXECUTIVE, MANAGER, OPERATIONAL, INFLUENCER
  email?: string;
  phone?: string;
  mobile?: string;
  // Location Assignment (Section 2: location_assignment - Sprint 2.1.7.7)
  responsibilityScope?: string; // ALL or SPECIFIC
  assignedLocationIds?: string[]; // UUIDs der zugewiesenen Standorte
  // Relationship (Section 3: relationship)
  birthday?: string; // ISO date string
  hobbies?: string; // V2: Hobbies & Interessen
  familyStatus?: string; // V2: Familienstand
  childrenCount?: number; // V2: Anzahl Kinder
  personalNotes?: string; // V2: Persönliche Notizen (Beziehung)
  // Social & Business (Section 4: social_business)
  linkedin?: string; // V2: LinkedIn Profile URL
  xing?: string; // V2: XING Profile URL
  notes?: string; // V2: Business Notizen (geschäftlich)
  // Extra fields (not in schema)
  isPrimary?: boolean; // UI-only flag
  assignedLocationId?: string; // DEPRECATED - use assignedLocationIds
}

interface ContactEditDialogProps {
  open: boolean;
  onClose: () => void;
  customerId: string;
  contact?: Contact | null;
  onSubmit: (contact: Partial<Contact>) => Promise<void>;
}

/**
 * Contact Edit Dialog
 *
 * Server-Driven Dialog für Kontakte mit DynamicFieldRenderer.
 * Zeigt alle Sections auf einer Seite (kein Stepper).
 */
export const ContactEditDialog: React.FC<ContactEditDialogProps> = ({
  open,
  onClose,
  customerId,
  contact,
  onSubmit,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isEdit = !!contact;

  // ========== SERVER-DRIVEN SCHEMA ==========
  const { data: schemas, isLoading: schemaLoading, isError: schemaError } = useContactSchema();

  // ========== CUSTOMER LOCATIONS (Sprint 2.1.7.7) ==========
  const { data: locationsData } = useCustomerLocations(customerId, 0, 100);

  // Transform locations to options for MULTISELECT field
  const locationOptions = useMemo(() => {
    const locations = locationsData?.content || [];
    return locations.map(loc => ({
      value: loc.id,
      label: loc.locationName || `Standort ${loc.id.slice(0, 8)}`,
    }));
  }, [locationsData]);

  // Get sections from schema and inject location options
  const sections = useMemo(() => {
    if (!schemas || schemas.length === 0) return [];
    const baseSections = schemas[0]?.sections || [];

    // Inject location options into the assignedLocationIds field
    return baseSections.map(section => {
      if (section.sectionId !== 'location_assignment') return section;

      return {
        ...section,
        fields: section.fields.map(field => {
          if (field.fieldKey === 'assignedLocationIds') {
            return {
              ...field,
              options: locationOptions,
            };
          }
          return field;
        }),
      };
    });
  }, [schemas, locationOptions]);

  // ========== FORM STATE ==========
  const [formData, setFormData] = useState<Partial<Contact>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Initialize form data
  useEffect(() => {
    if (contact) {
      setFormData({
        ...contact,
        // Sprint 2.1.7.7: Normalize legacy lowercase enum values to UPPERCASE
        salutation: contact.salutation || '',
        decisionLevel: normalizeDecisionLevel(contact.decisionLevel) || '',
        // Sprint 2.1.7.7: Multi-Location defaults
        responsibilityScope: contact.responsibilityScope || 'ALL',
        assignedLocationIds: contact.assignedLocationIds || [],
      });
    } else {
      setFormData({
        // Section 1: basic_info
        salutation: '',
        title: '',
        firstName: '',
        lastName: '',
        position: '',
        decisionLevel: '',
        email: '',
        phone: '',
        mobile: '',
        // Section 2: location_assignment (Sprint 2.1.7.7)
        responsibilityScope: 'ALL',
        assignedLocationIds: [],
        // Section 3: relationship
        birthday: '',
        hobbies: '',
        familyStatus: '',
        childrenCount: undefined,
        personalNotes: '',
        // Section 4: social_business
        linkedin: '',
        xing: '',
        notes: '',
        // Extra fields
        isPrimary: false,
      });
    }
    setErrors({});
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

  // ========== VALIDATION ==========

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Required fields (from schema)
    if (!formData.salutation) {
      newErrors.salutation = 'Anrede ist erforderlich';
    }
    if (!formData.firstName?.trim()) {
      newErrors.firstName = 'Vorname ist erforderlich';
    }
    if (!formData.lastName?.trim()) {
      newErrors.lastName = 'Nachname ist erforderlich';
    }

    // Email validation
    if (formData.email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        newErrors.email = 'Ungültige E-Mail-Adresse';
      }
    }

    // At least one contact method
    if (!formData.email && !formData.phone && !formData.mobile) {
      newErrors.contactMethod =
        'Mindestens eine Kontaktmöglichkeit (E-Mail, Telefon oder Mobil) erforderlich';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // ========== SUBMIT ==========

  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(formData);
      onClose();
    } catch (error) {
      console.error('Error saving contact:', error);
      setErrors({
        submit: 'Fehler beim Speichern des Kontakts. Bitte versuchen Sie es erneut.',
      });
    } finally {
      setIsSubmitting(false);
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

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth fullScreen={isMobile}>
      <DialogTitle>{isEdit ? 'Kontakt bearbeiten' : 'Neuer Kontakt'}</DialogTitle>

      <DialogContent dividers>
        {/* Error Alerts */}
        {errors.submit && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {errors.submit}
          </Alert>
        )}

        {errors.contactMethod && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            {errors.contactMethod}
          </Alert>
        )}

        {/* Render sections dynamically from schema using DynamicFieldRenderer */}
        {sections.map((section, sectionIndex) => (
          <Box key={section.sectionId} sx={{ mb: 3 }}>
            {/* Section Header */}
            <Typography
              variant="h6"
              sx={{
                mb: 1,
                mt: sectionIndex > 0 ? 2 : 0,
                fontSize: '1rem',
                fontWeight: 600,
                color: 'text.secondary',
              }}
            >
              {section.title}
            </Typography>
            {section.subtitle && (
              <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
                {section.subtitle}
              </Typography>
            )}

            {/* Server-Driven Field Rendering */}
            <DynamicFieldRenderer
              fields={section.fields}
              values={formData as Record<string, unknown>}
              errors={errors}
              onChange={handleFieldChange}
              onBlur={() => {}}
              useAdaptiveLayout={false}
            />

            {/* Divider after section (except last) */}
            {sectionIndex < sections.length - 1 && <Divider sx={{ mt: 3 }} />}
          </Box>
        ))}

        {/* Extra field: isPrimary (not in schema) */}
        {sections.length > 0 && (
          <Box sx={{ mt: 3 }}>
            <Divider sx={{ mb: 2 }} />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.isPrimary || false}
                  onChange={e =>
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
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={isSubmitting}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={isSubmitting || !formData.firstName || !formData.lastName}
        >
          {isSubmitting ? 'Speichert...' : isEdit ? 'Speichern' : 'Kontakt anlegen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Explicit re-export for Vite HMR
export type { Contact };
