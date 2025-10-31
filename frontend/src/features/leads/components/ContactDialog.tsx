/**
 * Contact Dialog Component (Lead Contacts)
 *
 * Sprint 2.1.7.2 D11.1: Server-Driven UI for Contact Forms
 *
 * Schema-driven Dialog zum Anlegen/Bearbeiten von Lead-Kontakten.
 * Fetches schema from ContactSchemaResource (/api/contacts/schema).
 *
 * Features:
 * - Server-Driven UI: Backend controls form structure via schema
 * - 3 Sections: basic_info, relationship, social_business
 * - Dynamic field rendering based on FieldType (TEXT, TEXTAREA, ENUM, DATE, NUMBER)
 * - Enum sources from backend (/api/enums/...)
 * - Validation from schema (required fields)
 * - MUI v7 Grid v2 API
 *
 * Used by: LeadDetailPage (Contact Management)
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  MenuItem,
  Grid,
  Alert,
  CircularProgress,
  Autocomplete,
  Typography,
  Divider,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import { toast } from 'react-hot-toast';
import type { LeadContactDTO } from '../types';
import { createLeadContact, updateLeadContact } from '../api';
import {
  useContactRoles,
  useSalutations,
  useDecisionLevels,
  useTitles,
} from '../../../hooks/useContactEnums';
import { useContactSchema } from '../../../hooks/useContactSchema';
import type { FieldDefinition } from '../../../hooks/useContactSchema';

interface ContactDialogProps {
  open: boolean;
  onClose: () => void;
  leadId: number;
  contact?: LeadContactDTO | null; // Wenn vorhanden = Bearbeiten, sonst = Neu
  onSave: () => void;
}

/**
 * Contact Dialog (Lead Contacts)
 *
 * Schema-driven Dialog für Lead-Kontakte mit dynamischer Feldgenerierung.
 */
export function ContactDialog({
  open,
  onClose,
  leadId: _leadId,
  contact,
  onSave,
}: ContactDialogProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isEdit = !!contact;

  // Load schema from backend (Server-Driven UI)
  const { data: schemas, isLoading: isLoadingSchema } = useContactSchema();
  const contactSchema = schemas?.[0]; // First (and only) card

  // Load enums from backend (for enum fields)
  const { data: contactRoles, isLoading: isLoadingRoles } = useContactRoles();
  const { data: salutations, isLoading: isLoadingSalutations } = useSalutations();
  const { data: decisionLevels, isLoading: isLoadingDecisionLevels } = useDecisionLevels();
  const { data: titles, isLoading: isLoadingTitles } = useTitles();

  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState<Partial<LeadContactDTO>>({
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
    // Section 2: relationship
    birthday: '',
    hobbies: '',
    familyStatus: '',
    childrenCount: undefined,
    personalNotes: '',
    // Section 3: social_business (if available in LeadContactDTO)
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Initialize form data
  useEffect(() => {
    if (contact) {
      setFormData({
        ...contact,
        // Normalize salutation to UPPERCASE (backend may have mixed-case)
        salutation: contact.salutation?.toUpperCase() || '',
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
        // Section 2: relationship
        birthday: '',
        hobbies: '',
        familyStatus: '',
        childrenCount: undefined,
        personalNotes: '',
      });
    }
    setErrors({});
  }, [contact, open]);

  // Validate form
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Required fields (from schema)
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
  const handleSave = async () => {
    if (!validateForm()) {
      return;
    }

    setSaving(true);
    try {
      if (contact) {
        // Update existing contact
        await updateLeadContact(_leadId, contact.id, formData);
        toast.success('Kontakt erfolgreich aktualisiert');
      } else {
        // Create new contact
        await createLeadContact(_leadId, formData);
        toast.success('Kontakt erfolgreich hinzugefügt');
      }

      onSave();
      onClose();
    } catch (error) {
      console.error('Failed to save contact:', error);
      toast.error('Fehler beim Speichern');
      setErrors({
        submit: 'Fehler beim Speichern des Kontakts. Bitte versuchen Sie es erneut.',
      });
    } finally {
      setSaving(false);
    }
  };

  // Handle field change
  const handleFieldChange = (field: keyof LeadContactDTO, value: string | boolean | number) => {
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

  /**
   * Get enum data for a specific enumSource URL
   */
  const getEnumData = (enumSource: string) => {
    if (enumSource === '/api/enums/contact-salutations') return salutations;
    if (enumSource === '/api/enums/contact-titles') return titles;
    if (enumSource === '/api/enums/contact-decision-levels') return decisionLevels;
    if (enumSource === '/api/enums/contact-roles') return contactRoles;
    return [];
  };

  /**
   * Render a single field based on FieldDefinition
   */
  const renderField = (field: FieldDefinition) => {
    const fieldKey = field.fieldKey as keyof LeadContactDTO;
    const value = formData[fieldKey];
    const error = errors[fieldKey];

    // Special case: Position field (Autocomplete with freeSolo)
    if (fieldKey === 'position') {
      return (
        <Grid key={fieldKey} size={{ xs: field.gridCols || 12 }}>
          <Autocomplete
            freeSolo
            options={contactRoles?.map(r => r.label) || []}
            value={contactRoles?.find(r => r.value === value)?.label || (value as string) || ''}
            onChange={(_, newValue) => {
              const matchingRole = contactRoles?.find(r => r.label === newValue);
              handleFieldChange(fieldKey, matchingRole?.value || newValue || '');
            }}
            onInputChange={(_, newInputValue) => {
              if (newInputValue !== value) {
                handleFieldChange(fieldKey, newInputValue);
              }
            }}
            disabled={isLoadingRoles || saving}
            renderInput={params => (
              <TextField
                {...params}
                label={field.label}
                helperText={field.helpText || field.placeholder}
                required={field.required}
                error={!!error}
                fullWidth
              />
            )}
          />
        </Grid>
      );
    }

    // ENUM type
    if (field.type === 'ENUM' && field.enumSource) {
      const enumData = getEnumData(field.enumSource);
      const isLoading =
        (field.enumSource === '/api/enums/contact-salutations' && isLoadingSalutations) ||
        (field.enumSource === '/api/enums/contact-titles' && isLoadingTitles) ||
        (field.enumSource === '/api/enums/contact-decision-levels' && isLoadingDecisionLevels) ||
        (field.enumSource === '/api/enums/contact-roles' && isLoadingRoles);

      return (
        <Grid key={fieldKey} size={{ xs: field.gridCols || 12 }}>
          <TextField
            select
            label={field.label}
            value={value || ''}
            onChange={e => handleFieldChange(fieldKey, e.target.value)}
            fullWidth
            required={field.required}
            error={!!error}
            helperText={error || field.helpText || field.placeholder}
            disabled={isLoading || saving}
          >
            <MenuItem value="">---</MenuItem>
            {enumData?.map(item => (
              <MenuItem key={item.value} value={item.value}>
                {item.label}
              </MenuItem>
            ))}
          </TextField>
        </Grid>
      );
    }

    // TEXTAREA type
    if (field.type === 'TEXTAREA') {
      return (
        <Grid key={fieldKey} size={{ xs: field.gridCols || 12 }}>
          <TextField
            label={field.label}
            value={value || ''}
            onChange={e => handleFieldChange(fieldKey, e.target.value)}
            multiline
            rows={3}
            fullWidth
            required={field.required}
            error={!!error}
            helperText={error || field.helpText || field.placeholder}
            placeholder={field.placeholder}
            disabled={saving}
          />
        </Grid>
      );
    }

    // DATE type
    if (field.type === 'DATE') {
      return (
        <Grid key={fieldKey} size={{ xs: field.gridCols || 12 }}>
          <TextField
            label={field.label}
            type="date"
            value={value || ''}
            onChange={e => handleFieldChange(fieldKey, e.target.value)}
            fullWidth
            required={field.required}
            error={!!error}
            helperText={error || field.helpText || field.placeholder}
            InputLabelProps={{ shrink: true }}
            disabled={saving}
          />
        </Grid>
      );
    }

    // NUMBER type
    if (field.type === 'NUMBER') {
      return (
        <Grid key={fieldKey} size={{ xs: field.gridCols || 12 }}>
          <TextField
            label={field.label}
            type="number"
            value={value || ''}
            onChange={e =>
              handleFieldChange(fieldKey, e.target.value ? parseInt(e.target.value, 10) : 0)
            }
            fullWidth
            required={field.required}
            error={!!error}
            helperText={error || field.helpText || field.placeholder}
            placeholder={field.placeholder}
            disabled={saving}
          />
        </Grid>
      );
    }

    // TEXT type (default)
    return (
      <Grid key={fieldKey} size={{ xs: field.gridCols || 12 }}>
        <TextField
          label={field.label}
          type={
            fieldKey === 'email'
              ? 'email'
              : fieldKey === 'phone' || fieldKey === 'mobile'
                ? 'tel'
                : 'text'
          }
          value={value || ''}
          onChange={e => handleFieldChange(fieldKey, e.target.value)}
          fullWidth
          required={field.required}
          error={!!error}
          helperText={error || field.helpText || field.placeholder}
          placeholder={field.placeholder}
          disabled={saving}
        />
      </Grid>
    );
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth fullScreen={isMobile}>
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
        {isLoadingSchema && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
            <CircularProgress size={24} />
          </Box>
        )}

        {/* Schema not loaded yet */}
        {!isLoadingSchema && !contactSchema && (
          <Alert severity="info" sx={{ my: 2 }}>
            Kontaktformular wird geladen...
          </Alert>
        )}

        {/* Render sections dynamically from schema */}
        {contactSchema &&
          contactSchema.sections.map((section, sectionIndex) => (
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

              {/* Section Fields */}
              <Grid container spacing={2}>
                {section.fields.map(field => renderField(field))}
              </Grid>

              {/* Divider after section (except last) */}
              {sectionIndex < contactSchema.sections.length - 1 && <Divider sx={{ mt: 3 }} />}
            </Box>
          ))}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={saving}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSave}
          variant="contained"
          disabled={saving || !formData.firstName || !formData.lastName}
        >
          {saving ? 'Speichert...' : isEdit ? 'Speichern' : 'Kontakt anlegen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
