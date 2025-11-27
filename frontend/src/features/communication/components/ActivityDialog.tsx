/**
 * Unified Activity Dialog Component
 *
 * Sprint 2.1.7.2: D8 Unified Communication System
 *
 * Code-Reuse:
 * - Basiert auf LeadActivityDialog-Konzept (Sprint 2.1.7.1)
 * - Funktioniert für Lead UND Customer!
 * - Keine Duplikation!
 *
 * Design System Compliance:
 * - ✅ Nur MUI Theme colors
 * - ✅ Typography variants (Poppins)
 * - ✅ Deutsche UI-Sprache
 */

import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Box,
} from '@mui/material';
import { useEnumOptions } from '../../../hooks/useEnumOptions';
import type { Activity } from './ActivityTimeline';

// ============================================================================
// TYPES
// ============================================================================

export interface ActivityDialogProps {
  /** Dialog open state */
  open: boolean;
  /** Dialog close handler */
  onClose: () => void;
  /** Entity type: 'lead' or 'customer' */
  entityType: 'lead' | 'customer';
  /** Entity ID as string (Lead ID or Customer UUID) */
  entityId: string;
  /** Optional activity to edit (if editing existing activity) */
  activity?: Activity | null;
  /** Optional callback after successful save */
  onSaved?: () => void;
}

// ============================================================================
// COMPONENT
// ============================================================================

export const ActivityDialog: React.FC<ActivityDialogProps> = ({
  open,
  onClose,
  entityType,
  entityId,
  activity,
  onSaved,
}) => {
  // ============================================================================
  // SERVER-DRIVEN ENUMS
  // ============================================================================

  // Sprint 2.1.7.7 - Schema-Driven Forms Migration
  const { data: activityTypeOptions } = useEnumOptions('/api/enums/activity-types');
  const { data: outcomeOptions } = useEnumOptions('/api/enums/activity-outcomes');

  // ============================================================================
  // STATE
  // ============================================================================

  const [activityType, setActivityType] = useState<string>('CALL');
  const [outcome, setOutcome] = useState<string>('');
  const [summary, setSummary] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [activityDate, setActivityDate] = useState<string>('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ============================================================================
  // INITIALIZE FORM WITH ACTIVITY DATA (EDIT MODE)
  // ============================================================================

  useEffect(() => {
    if (activity) {
      // Edit mode: populate form
      setActivityType(activity.activityType || 'CALL');
      setOutcome(activity.outcome || '');
      setSummary(activity.summary || '');
      setDescription(activity.description || '');
      // Format date for datetime-local input
      const date = new Date(activity.activityDate);
      const formattedDate = date.toISOString().slice(0, 16);
      setActivityDate(formattedDate);
    } else {
      // Create mode: reset form
      setActivityType('CALL');
      setOutcome('');
      setSummary('');
      setDescription('');
      // Default to current date/time
      const now = new Date();
      const formattedNow = now.toISOString().slice(0, 16);
      setActivityDate(formattedNow);
    }
    setError(null);
  }, [activity, open]);

  // ============================================================================
  // HANDLERS
  // ============================================================================

  const handleSave = async () => {
    try {
      setSaving(true);
      setError(null);

      // Validate
      if (!summary || summary.trim() === '') {
        setError('Bitte geben Sie eine Zusammenfassung ein.');
        setSaving(false);
        return;
      }

      // Prepare payload
      const payload = {
        activityType,
        outcome: outcome || null,
        summary: summary.trim(),
        description: description.trim() || null,
        activityDate: new Date(activityDate).toISOString(),
      };

      // Determine endpoint
      const endpoint = activity
        ? `/api/activities/${activity.id}` // UPDATE
        : `/api/activities/${entityType}/${entityId}`; // CREATE

      const method = activity ? 'PUT' : 'POST';

      // Send request
      const response = await fetch(endpoint, {
        method,
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error('Fehler beim Speichern der Aktivität');
      }

      // Success
      if (onSaved) {
        onSaved();
      }
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setError(null);
    onClose();
  };

  // ============================================================================
  // RENDER
  // ============================================================================

  return (
    <Dialog open={open} onClose={handleCancel} maxWidth="md" fullWidth>
      <DialogTitle>{activity ? 'Aktivität bearbeiten' : 'Neue Aktivität'}</DialogTitle>

      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
          {/* Activity Type */}
          <FormControl fullWidth required>
            <InputLabel id="activity-type-label">Typ</InputLabel>
            <Select
              labelId="activity-type-label"
              id="activity-type"
              value={activityType}
              label="Typ"
              onChange={e => setActivityType(e.target.value)}
            >
              {activityTypeOptions?.map(option => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {/* Activity Date */}
          <TextField
            fullWidth
            required
            type="datetime-local"
            label="Datum/Zeit"
            value={activityDate}
            onChange={e => setActivityDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
          />

          {/* Summary */}
          <TextField
            fullWidth
            required
            label="Zusammenfassung"
            placeholder="z.B. Telefonat Küchenchef - Interesse an Bio-Gemüse-Abo"
            value={summary}
            onChange={e => setSummary(e.target.value)}
            helperText="Kurze Zusammenfassung der Aktivität (max. 500 Zeichen)"
            inputProps={{ maxLength: 500 }}
          />

          {/* Description */}
          <TextField
            fullWidth
            label="Notizen"
            placeholder="Detaillierte Notizen zur Aktivität..."
            value={description}
            onChange={e => setDescription(e.target.value)}
            multiline
            rows={4}
            helperText="Detaillierte Beschreibung der Aktivität"
          />

          {/* Outcome */}
          <FormControl fullWidth>
            <InputLabel id="outcome-label">Ergebnis</InputLabel>
            <Select
              labelId="outcome-label"
              id="outcome"
              value={outcome}
              label="Ergebnis"
              onChange={e => setOutcome(e.target.value)}
            >
              {outcomeOptions?.map(option => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleCancel} disabled={saving}>
          Abbrechen
        </Button>
        <Button variant="contained" onClick={handleSave} disabled={saving || !summary}>
          {saving ? 'Speichern...' : 'Speichern'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
