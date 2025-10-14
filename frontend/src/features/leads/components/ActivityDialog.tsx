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
import { useActivityOutcomes } from '../../../hooks/useActivityOutcomes';

interface ActivityDialogProps {
  open: boolean;
  onClose: () => void;
  leadId: number;
  onSave: () => void;
}

/**
 * ActivityDialog - Lead-Aktivitäten protokollieren
 *
 * Sprint 2.1.7 - Issue #126: ActivityOutcome Integration
 *
 * Features:
 * - Activity Type selection (CALL, EMAIL, MEETING, NOTE, etc.)
 * - Description field (required)
 * - ActivityOutcome dropdown (optional) - tracks success/follow-up needs
 * - Single Source of Truth: Outcome values from backend API
 */
export function ActivityDialog({ open, onClose, leadId, onSave }: ActivityDialogProps) {
  const [activityType, setActivityType] = useState<string>('CALL');
  const [description, setDescription] = useState<string>('');
  const [outcome, setOutcome] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch activity outcomes from backend (Single Source of Truth)
  const { data: outcomes, isLoading: outcomesLoading } = useActivityOutcomes();

  const activityTypes = [
    { value: 'CALL', label: 'Anruf' },
    { value: 'EMAIL', label: 'E-Mail' },
    { value: 'MEETING', label: 'Meeting/Termin' },
    { value: 'NOTE', label: 'Notiz' },
    { value: 'SAMPLE_SENT', label: 'Muster versendet' },
    { value: 'PROPOSAL_SENT', label: 'Angebot versendet' },
  ];

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

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        Neue Aktivität erfassen
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
          {/* Activity Type */}
          <TextField
            select
            label="Aktivitätstyp"
            value={activityType}
            onChange={e => setActivityType(e.target.value)}
            disabled={isSubmitting}
            required
            fullWidth
          >
            {activityTypes.map(type => (
              <MenuItem key={type.value} value={type.value}>
                {type.label}
              </MenuItem>
            ))}
          </TextField>

          {/* Description */}
          <TextField
            label="Beschreibung"
            value={description}
            onChange={e => setDescription(e.target.value)}
            disabled={isSubmitting}
            required
            multiline
            rows={4}
            fullWidth
            placeholder="Was wurde besprochen? Welche Ergebnisse wurden erzielt?"
            helperText={`${description.length}/1000 Zeichen`}
            inputProps={{ maxLength: 1000 }}
          />

          {/* Activity Outcome (Sprint 2.1.7 Issue #126) */}
          <TextField
            select
            label="Ergebnis (optional)"
            value={outcome}
            onChange={e => setOutcome(e.target.value)}
            disabled={isSubmitting || outcomesLoading}
            fullWidth
            helperText="Hilft beim Tracking von Erfolg und Follow-up-Bedarf"
          >
            <MenuItem value="">
              <em>Kein Ergebnis ausgewählt</em>
            </MenuItem>
            {outcomesLoading ? (
              <MenuItem disabled>
                <CircularProgress size={20} sx={{ mr: 1 }} />
                Lade...
              </MenuItem>
            ) : (
              outcomes?.map(outcome => (
                <MenuItem key={outcome.value} value={outcome.value}>
                  {outcome.label}
                </MenuItem>
              ))
            )}
          </TextField>
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
