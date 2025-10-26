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
} from '@mui/material';

export interface Contact {
  id?: string;
  firstName: string;
  lastName: string;
  role: 'CHEF' | 'BUYER' | 'MANAGER' | 'OTHER';
  email?: string;
  phone?: string;
  mobile?: string;
  isPrimary?: boolean;
  notes?: string;
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
  customerId,
  contact,
  onSubmit,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isEdit = !!contact;

  // Form state
  const [formData, setFormData] = useState<Partial<Contact>>({
    firstName: '',
    lastName: '',
    role: 'CHEF',
    email: '',
    phone: '',
    mobile: '',
    isPrimary: false,
    notes: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Initialize form data
  useEffect(() => {
    if (contact) {
      setFormData(contact);
    } else {
      setFormData({
        firstName: '',
        lastName: '',
        role: 'CHEF',
        email: '',
        phone: '',
        mobile: '',
        isPrimary: false,
        notes: '',
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

        <Grid container spacing={2} sx={{ mt: 1 }}>
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

          {/* Rolle */}
          <Grid size={{ xs: 12 }}>
            <TextField
              select
              label="Rolle"
              value={formData.role || 'CHEF'}
              onChange={e => handleFieldChange('role', e.target.value)}
              fullWidth
              helperText="Welche Funktion hat diese Person?"
            >
              <MenuItem value="CHEF">Küchenchef (CHEF)</MenuItem>
              <MenuItem value="BUYER">Einkäufer (BUYER)</MenuItem>
              <MenuItem value="MANAGER">Manager</MenuItem>
              <MenuItem value="OTHER">Sonstiges</MenuItem>
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