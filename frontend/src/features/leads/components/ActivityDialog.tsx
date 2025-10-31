import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
  Box,
  IconButton,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { useActivitySchema } from '../../../hooks/useActivitySchema';
import { useEnumOptions } from '../../../hooks/useEnumOptions';

interface ActivityDialogProps {
  open: boolean;
  onClose: () => void;
  leadId: number;
  onSave: () => void;
}

/**
 * ActivityDialog - Schema-Driven Activity Logging
 *
 * Sprint 2.1.7.2 D11: Server-Driven UI Migration
 *
 * This dialog fetches its field definitions from the backend via useActivitySchema.
 * Backend: GET /api/activities/schema (ActivitySchemaResource.java)
 *
 * Fields (3, dynamically loaded):
 * - activityType (ENUM, required) - Aktivitätstyp (CALL, EMAIL, MEETING, etc.)
 * - description (TEXTAREA, required) - Beschreibung der Aktivität
 * - outcome (ENUM, optional) - Ergebnis (SUCCESSFUL, UNSUCCESSFUL, etc.)
 *
 * Architecture:
 * - Backend = Single Source of Truth for field definitions
 * - Frontend = Rendering Layer (no hardcoded field definitions)
 * - Enum options fetched from backend (/api/enums/...)
 */
export function ActivityDialog({ open, onClose, leadId, onSave }: ActivityDialogProps) {
  const [activityType, setActivityType] = useState<string>('CALL');
  const [description, setDescription] = useState<string>('');
  const [outcome, setOutcome] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch schema from backend (Server-Driven UI)
  const { data: schemas, isLoading: schemaLoading, error: schemaError } = useActivitySchema();

  // Extract activity schema
  const activitySchema = schemas?.find(s => s.cardId === 'activity');
  const detailsSection = activitySchema?.sections?.find(s => s.sectionId === 'activity_details');
  const fields = detailsSection?.fields || [];

  // Fetch enum options for ENUM fields
  const activityTypeField = fields.find(f => f.fieldKey === 'activityType');
  const outcomeField = fields.find(f => f.fieldKey === 'outcome');

  const { data: activityTypeOptions } = useEnumOptions(activityTypeField?.enumSource || '');
  const { data: outcomeOptions } = useEnumOptions(outcomeField?.enumSource || '');

  const handleSubmit = async () => {
    if (!description.trim()) {
      setError('Beschreibung ist erforderlich');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      // Build request payload
      const payload: {
        activityType: string;
        description: string;
        outcome?: string;
      } = {
        activityType,
        description: description.trim(),
      };

      // Add outcome if selected
      if (outcome) {
        payload.outcome = outcome;
      }

      // Call backend API
      const response = await fetch(`http://localhost:8080/api/leads/${leadId}/activities`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ error: 'Unknown error' }));
        throw new Error(errorData.error || `HTTP ${response.status}`);
      }

      // Success: Reset form and close dialog
      setActivityType('CALL');
      setDescription('');
      setOutcome('');
      onSave(); // Trigger parent refresh
      onClose();
    } catch (err) {
      console.error('Failed to save activity:', err);
      setError(err instanceof Error ? err.message : 'Fehler beim Speichern der Aktivität');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setActivityType('CALL');
      setDescription('');
      setOutcome('');
      setError(null);
      onClose();
    }
  };

  // Loading state
  if (schemaLoading) {
    return (
      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogContent
          sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 4 }}
        >
          <CircularProgress />
        </DialogContent>
      </Dialog>
    );
  }

  // Error state
  if (schemaError || !activitySchema) {
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

  // Extract field definitions from schema
  const activityTypeFieldDef = fields.find(f => f.fieldKey === 'activityType');
  const descriptionFieldDef = fields.find(f => f.fieldKey === 'description');
  const outcomeFieldDef = fields.find(f => f.fieldKey === 'outcome');

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {activitySchema.title}
        <IconButton
          aria-label="close"
          onClick={handleClose}
          disabled={isSubmitting}
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
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {/* Activity Type (ENUM) - Schema-driven */}
          {activityTypeFieldDef && (
            <TextField
              select
              label={activityTypeFieldDef.label}
              value={activityType}
              onChange={e => setActivityType(e.target.value)}
              disabled={isSubmitting}
              required={activityTypeFieldDef.required}
              fullWidth
              helperText={activityTypeFieldDef.helpText}
            >
              {activityTypeOptions?.map(option => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </TextField>
          )}

          {/* Description (TEXTAREA) - Schema-driven */}
          {descriptionFieldDef && (
            <TextField
              label={descriptionFieldDef.label}
              value={description}
              onChange={e => setDescription(e.target.value)}
              disabled={isSubmitting}
              required={descriptionFieldDef.required}
              multiline
              rows={4}
              fullWidth
              placeholder={descriptionFieldDef.placeholder}
              helperText={`${description.length}/1000 Zeichen - ${descriptionFieldDef.helpText}`}
              inputProps={{ maxLength: 1000 }}
            />
          )}

          {/* Outcome (ENUM, optional) - Schema-driven */}
          {outcomeFieldDef && (
            <TextField
              select
              label={outcomeFieldDef.label}
              value={outcome}
              onChange={e => setOutcome(e.target.value)}
              disabled={isSubmitting}
              fullWidth
              helperText={outcomeFieldDef.helpText}
            >
              <MenuItem value="">
                <em>Kein Ergebnis ausgewählt</em>
              </MenuItem>
              {outcomeOptions?.map(option => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </TextField>
          )}
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={isSubmitting}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={isSubmitting || !description.trim()}
        >
          {isSubmitting ? <CircularProgress size={20} /> : 'Speichern'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
