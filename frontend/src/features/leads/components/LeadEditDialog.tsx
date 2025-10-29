import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Alert,
  Typography,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { toast } from 'react-hot-toast';
import { updateLead } from '../api';
import type { Lead } from '../types';
import { useLeadSchema } from '../../../hooks/useLeadSchema';
import { useEnumOptions, type EnumOption } from '../../../hooks/useEnumOptions';
import type { FieldDefinition } from '../../../hooks/useContactSchema';

interface LeadEditDialogProps {
  open: boolean;
  onClose: () => void;
  lead: Lead | null;
  onSave: () => void;
}

/**
 * LeadEditDialog - Schema-Driven Lead Stammdaten bearbeiten
 *
 * Sprint 2.1.7.2 D11: Server-Driven UI Migration
 *
 * This dialog fetches its field definitions from the backend via useLeadSchema.
 * Backend: GET /api/leads/schema (LeadSchemaResource.java)
 *
 * Fields (6, dynamically loaded from backend schema cardId='lead_edit'):
 * - companyName (TEXT, required) - Firmenname
 * - source (ENUM, required) - Quelle (MESSE/TELEFON require first contact documentation)
 * - website (TEXT, optional) - Website
 * - street (TEXT, optional) - Straße
 * - postalCode (TEXT, optional) - PLZ
 * - city (TEXT, optional) - Stadt
 *
 * Architecture:
 * - Backend = Single Source of Truth for field definitions
 * - Frontend = Rendering Layer (no hardcoded field definitions)
 * - Enum options fetched from backend (/api/enums/...)
 */
export function LeadEditDialog({ open, onClose, lead, onSave }: LeadEditDialogProps) {
  // Fetch schema from backend (Server-Driven UI)
  const { data: schemas, isLoading: schemaLoading, error: schemaError } = useLeadSchema();

  // Extract lead_edit schema
  const editSchema = schemas?.find(s => s.cardId === 'lead_edit');

  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState<Record<string, string | number | boolean | null>>({});

  // Load enum options for ENUM fields (lead_edit has 1 enum: source)
  const sourceField = editSchema?.sections.flatMap(s => s.fields).find(f => f.fieldKey === 'source');
  const { data: sourceOptions, isLoading: sourceLoading } = useEnumOptions(sourceField?.enumSource || '');

  // Build enum options map for renderField
  const enumOptionsMap: Record<string, { options: EnumOption[]; loading: boolean }> = {
    source: { options: sourceOptions || [], loading: sourceLoading },
  };

  // Initialize formData from schema fields when lead changes
  useEffect(() => {
    if (lead && editSchema) {
      const initialData: Record<string, string | number | boolean | null> = {};
      editSchema.sections.forEach(section => {
        section.fields.forEach(field => {
          initialData[field.fieldKey] = lead[field.fieldKey as keyof Lead] || '';
        });
      });
      setFormData(initialData);
    }
  }, [lead, editSchema]);

  const handleSave = async () => {
    if (!lead || !editSchema) return;

    // Validate required fields
    const requiredFields = editSchema.sections
      .flatMap(s => s.fields)
      .filter(f => f.required);

    for (const field of requiredFields) {
      const value = formData[field.fieldKey];
      if (!value || (typeof value === 'string' && !value.trim())) {
        toast.error(`${field.label} ist erforderlich`);
        return;
      }
    }

    setSaving(true);
    try {
      // Build payload from formData (trim strings, remove empty values)
      const payload: Record<string, string | number | boolean> = {};
      Object.keys(formData).forEach(key => {
        const value = formData[key];
        if (typeof value === 'string') {
          const trimmed = value.trim();
          if (trimmed) payload[key] = trimmed;
        } else if (value !== null && value !== undefined && value !== '') {
          payload[key] = value;
        }
      });

      await updateLead(lead.id, payload);

      toast.success('Stammdaten erfolgreich aktualisiert');
      onSave();
      onClose();
    } catch (error) {
      console.error('Failed to update lead:', error);
      const errorDetail =
        error && typeof error === 'object' && 'detail' in error ? error.detail : undefined;
      toast.error(errorDetail || 'Fehler beim Speichern');
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    if (!saving) {
      onClose();
    }
  };

  // Helper: Render Field Component (inline helper function)
  const renderField = (field: FieldDefinition) => {
    // Calculate grid span (gridCols from backend)
    const gridSpan = field.gridCols === 12 ? '1 / -1' : '1';

    if (field.type === 'TEXT') {
      return (
        <TextField
          key={field.fieldKey}
          label={field.label}
          value={formData[field.fieldKey] || ''}
          onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.value })}
          required={field.required}
          disabled={saving}
          fullWidth
          placeholder={field.placeholder}
          helperText={field.helpText}
          sx={{ gridColumn: { xs: '1', sm: gridSpan } }}
        />
      );
    }

    if (field.type === 'ENUM') {
      const enumData = enumOptionsMap[field.fieldKey] || { options: [], loading: false };

      return (
        <FormControl key={field.fieldKey} fullWidth sx={{ gridColumn: { xs: '1', sm: gridSpan } }}>
          <InputLabel id={`${field.fieldKey}-label`}>{field.label}</InputLabel>
          <Select
            labelId={`${field.fieldKey}-label`}
            value={formData[field.fieldKey] || ''}
            onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.value })}
            label={field.label}
            disabled={saving || enumData.loading}
          >
            <MenuItem value="">
              <em>Nicht angegeben</em>
            </MenuItem>
            {enumData.options.map(option => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </Select>
          {field.helpText && (
            <Typography variant="caption" sx={{ mt: 0.5, color: 'text.secondary' }}>
              {field.helpText}
            </Typography>
          )}
        </FormControl>
      );
    }

    return null;
  };

  // Loading state
  if (schemaLoading) {
    return (
      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogContent sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 4 }}>
          <CircularProgress />
        </DialogContent>
      </Dialog>
    );
  }

  // Error state
  if (schemaError || !editSchema) {
    return (
      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogContent>
          <Alert severity="error">
            Fehler beim Laden des Schemas. Bitte versuchen Sie es später erneut.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Schließen</Button>
        </DialogActions>
      </Dialog>
    );
  }

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {editSchema.title}
        <IconButton
          aria-label="close"
          onClick={handleClose}
          disabled={saving}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: theme => theme.palette.grey[500],
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers>
        {editSchema.sections.map(section => (
          <Box key={section.sectionId} sx={{ mb: section.sectionId === editSchema.sections[editSchema.sections.length - 1].sectionId ? 0 : 3 }}>
            {section.fields.length > 0 && (
              <>
                <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
                  {section.title}
                </Typography>
                <Box
                  sx={{
                    display: 'grid',
                    gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
                    gap: 2,
                  }}
                >
                  {section.fields.map(field => renderField(field))}
                </Box>
              </>
            )}
          </Box>
        ))}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={saving}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSave}
          variant="contained"
          disabled={saving}
          sx={{
            bgcolor: 'primary.main',
            '&:hover': { bgcolor: 'primary.dark' },
          }}
        >
          {saving ? 'Speichert...' : 'Speichern'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
