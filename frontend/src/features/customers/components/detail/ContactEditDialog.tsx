/**
 * Contact Edit Dialog Component
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Phase 3
 *
 * Einfacher Dialog zum Anlegen/Bearbeiten von Kundenkontakten.
 * Leichtgewichtige Alternative zu ContactFormDialog (nur 1 Tab, Basis-Felder).
 *
 * Features:
 * - Create/Edit Kontakte
 * - Felder: Vorname, Nachname, Rolle, E-Mail, Telefon, Mobil, isPrimary, Notizen
 * - Validierung: Mindestens ein Kontaktfeld erforderlich
 * - MUI v7 Grid v2 API
 *
 * Used by: CustomerDetailTabVerlauf.tsx
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Grid,
  Alert,
  useTheme,
  useMediaQuery,
  CircularProgress,
  Autocomplete,
} from '@mui/material';
import {
  useContactRoles,
  useSalutations,
  useDecisionLevels,
  useTitles,
} from '../../../../hooks/useContactEnums';

export interface Contact {
  id?: string;
  // V1 Fields: German Business Etiquette & Sales Strategy
  salutation?: string; // HERR, FRAU, DIVERS
  title?: string; // Dr., Prof., etc. (freetext)
  firstName: string;
  lastName: string;
  position?: string; // Küchenchef, Einkaufsleiter, etc. (backend field - freetext with suggestions)
  decisionLevel?: string; // EXECUTIVE, MANAGER, OPERATIONAL, INFLUENCER
  // Contact Info
  email?: string;
  phone?: string;
  mobile?: string;
  isPrimary?: boolean;
  notes?: string;
  // V2 Fields (Sprint 2.1.7.7)
  birthday?: string; // ISO date string
  assignedLocationId?: string;
  linkedin?: string;
  xing?: string;
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
 * Kompakter Dialog für schnelles Anlegen/Bearbeiten von Kontakten.
 */
export const ContactEditDialog: React.FC<ContactEditDialogProps> = ({
  open,
  onClose,
  customerId: _customerId,
  contact,
  onSubmit,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isEdit = !!contact;

  // Load enums from backend
  const { data: contactRoles, isLoading: isLoadingRoles } = useContactRoles();
  const { data: salutations, isLoading: isLoadingSalutations } = useSalutations();
  const { data: decisionLevels, isLoading: isLoadingDecisionLevels } = useDecisionLevels();
  const { data: titles, isLoading: isLoadingTitles } = useTitles();

  // Form state
  const [formData, setFormData] = useState<Partial<Contact>>({
    salutation: '',
    title: '',
    firstName: '',
    lastName: '',
    position: '',
    decisionLevel: '',
    email: '',
    phone: '',
    mobile: '',
    isPrimary: false,
    notes: '',
    // V2 Fields (Sprint 2.1.7.7)
    birthday: '',
    assignedLocationId: '',
    linkedin: '',
    xing: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Initialize form data
  useEffect(() => {
    if (contact) {
      setFormData({
        ...contact,
        // Normalize salutation to UPPERCASE (backend may have mixed-case like "Frau")
        salutation: contact.salutation?.toUpperCase() || '',
      });
    } else {
      setFormData({
        salutation: '',
        title: '',
        firstName: '',
        lastName: '',
        position: '',
        decisionLevel: '',
        email: '',
        phone: '',
        mobile: '',
        isPrimary: false,
        notes: '',
        // V2 Fields (Sprint 2.1.7.7)
        birthday: '',
        assignedLocationId: '',
        linkedin: '',
        xing: '',
      });
    }
    setErrors({});
  }, [contact, open]);

  // Validate form
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Required fields
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

  // Handle submit
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

  // Handle field change
  const handleFieldChange = (field: keyof Contact, value: string | boolean) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));

    // Clear error for this field
    if (errors[field]) {
      setErrors(prev => {
        const next = { ...prev };
        delete next[field];
        return next;
      });
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      fullScreen={isMobile}
    >
      <DialogTitle>{isEdit ? 'Kontakt bearbeiten' : 'Neuer Kontakt'}</DialogTitle>

      <DialogContent dividers>
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

        {/* Loading State */}
        {(isLoadingSalutations || isLoadingTitles || isLoadingRoles || isLoadingDecisionLevels) && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
            <CircularProgress size={24} />
          </Box>
        )}

        <Grid container spacing={2} sx={{ mt: 1 }}>
          {/* V1: Anrede (Salutation) */}
          <Grid size={{ xs: 4 }}>
            <TextField
              select
              label="Anrede"
              value={formData.salutation || ''}
              onChange={e => handleFieldChange('salutation', e.target.value)}
              fullWidth
              helperText="Herr, Frau, Divers"
              disabled={isLoadingSalutations}
            >
              <MenuItem value="">---</MenuItem>
              {salutations?.map(s => (
                <MenuItem key={s.value} value={s.value}>
                  {s.label}
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          {/* V1: Titel (Title) - Dropdown für deutsche Briefanrede */}
          <Grid size={{ xs: 8 }}>
            <TextField
              select
              label="Titel"
              value={formData.title || ''}
              onChange={e => handleFieldChange('title', e.target.value)}
              fullWidth
              helperText="Akademischer/Berufstitel für Briefanrede"
              disabled={isLoadingTitles}
            >
              <MenuItem value="">---</MenuItem>
              {titles?.map(t => (
                <MenuItem key={t.value} value={t.value}>
                  {t.label}
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          {/* Vorname */}
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Vorname"
              value={formData.firstName || ''}
              onChange={e => handleFieldChange('firstName', e.target.value)}
              required
              fullWidth
              error={!!errors.firstName}
              helperText={errors.firstName}
            />
          </Grid>

          {/* Nachname */}
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Nachname"
              value={formData.lastName || ''}
              onChange={e => handleFieldChange('lastName', e.target.value)}
              required
              fullWidth
              error={!!errors.lastName}
              helperText={errors.lastName}
            />
          </Grid>

          {/* Position (Autocomplete: Dropdown mit Vorschlägen UND Freitext) */}
          <Grid size={{ xs: 12 }}>
            <Autocomplete
              freeSolo
              options={contactRoles?.map(r => r.label) || []}
              value={
                contactRoles?.find(r => r.value === formData.position)?.label ||
                formData.position ||
                ''
              }
              onChange={(_, newValue) => {
                // Find matching enum value or use raw input
                const matchingRole = contactRoles?.find(r => r.label === newValue);
                handleFieldChange('position', matchingRole?.value || newValue || '');
              }}
              onInputChange={(_, newInputValue) => {
                // Allow freetext input
                if (newInputValue !== formData.position) {
                  handleFieldChange('position', newInputValue);
                }
              }}
              disabled={isLoadingRoles}
              renderInput={params => (
                <TextField
                  {...params}
                  label="Position"
                  helperText="Rolle/Funktion im Betrieb (Dropdown oder Freitext)"
                  fullWidth
                />
              )}
            />
          </Grid>

          {/* V1: Entscheidungsebene (Decision Level) */}
          <Grid size={{ xs: 12 }}>
            <TextField
              select
              label="Entscheidungsebene"
              value={formData.decisionLevel || ''}
              onChange={e => handleFieldChange('decisionLevel', e.target.value)}
              fullWidth
              helperText="Wichtig für Sales-Strategie: Wer entscheidet?"
              disabled={isLoadingDecisionLevels}
            >
              <MenuItem value="">---</MenuItem>
              {decisionLevels?.map(level => (
                <MenuItem key={level.value} value={level.value}>
                  {level.label}
                </MenuItem>
              ))}
            </TextField>
          </Grid>

          {/* E-Mail */}
          <Grid size={{ xs: 12 }}>
            <TextField
              label="E-Mail"
              type="email"
              value={formData.email || ''}
              onChange={e => handleFieldChange('email', e.target.value)}
              fullWidth
              error={!!errors.email}
              helperText={errors.email}
            />
          </Grid>

          {/* Telefon */}
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Telefon"
              type="tel"
              value={formData.phone || ''}
              onChange={e => handleFieldChange('phone', e.target.value)}
              fullWidth
            />
          </Grid>

          {/* Mobil */}
          <Grid size={{ xs: 6 }}>
            <TextField
              label="Mobil"
              type="tel"
              value={formData.mobile || ''}
              onChange={e => handleFieldChange('mobile', e.target.value)}
              fullWidth
            />
          </Grid>

          {/* Notizen */}
          <Grid size={{ xs: 12 }}>
            <TextField
              label="Notizen"
              value={formData.notes || ''}
              onChange={e => handleFieldChange('notes', e.target.value)}
              multiline
              rows={3}
              fullWidth
              helperText="Optionale Anmerkungen zu diesem Kontakt"
            />
          </Grid>

          {/* V2 FIELDS - Aktiviert (außer Standort - Sprint 2.1.7.7) */}
          {/* Geburtstag */}
          <Grid size={{ xs: 12 }}>
            <TextField
              label="Geburtstag"
              type="date"
              value={formData.birthday || ''}
              onChange={e => handleFieldChange('birthday', e.target.value)}
              fullWidth
              InputLabelProps={{ shrink: true }}
              helperText="Optional: Geburtstag für persönliche Beziehungspflege"
            />
          </Grid>

          {/* Standort (Location) - BLEIBT DISABLED (Sprint 2.1.7.7) */}
          <Grid size={{ xs: 12 }}>
            <TextField
              label="Zugeordneter Standort"
              value={formData.assignedLocationId || ''}
              onChange={e => handleFieldChange('assignedLocationId', e.target.value)}
              fullWidth
              disabled
              helperText="Feature kommt in Sprint 2.1.7.7 (Standortzuordnung)"
            />
          </Grid>

          {/* LinkedIn */}
          <Grid size={{ xs: 6 }}>
            <TextField
              label="LinkedIn"
              type="url"
              value={formData.linkedin || ''}
              onChange={e => handleFieldChange('linkedin', e.target.value)}
              fullWidth
              placeholder="https://linkedin.com/in/..."
              helperText="LinkedIn-Profil-URL"
            />
          </Grid>

          {/* XING */}
          <Grid size={{ xs: 6 }}>
            <TextField
              label="XING"
              type="url"
              value={formData.xing || ''}
              onChange={e => handleFieldChange('xing', e.target.value)}
              fullWidth
              placeholder="https://xing.com/profile/..."
              helperText="XING-Profil-URL"
            />
          </Grid>

          {/* Hauptansprechpartner */}
          <Grid size={{ xs: 12 }}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.isPrimary || false}
                  onChange={e => handleFieldChange('isPrimary', e.target.checked)}
                />
              }
              label="Als Hauptansprechpartner festlegen"
            />
          </Grid>
        </Grid>
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