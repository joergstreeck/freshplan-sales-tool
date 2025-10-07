import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Alert,
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
} from '@mui/material';
import { updateLead } from './api';
import type { Lead, Problem } from './types';

interface StopTheClockDialogProps {
  open: boolean;
  lead: Lead | null;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * Stop-the-Clock Dialog (Sprint 2.1.6 Phase 4)
 *
 * Ermöglicht Admin/Manager das Pausieren und Fortsetzen der Lead-Schutzfrist.
 * Kumulative Pause-Zeit wird im Backend berechnet (progressPauseTotalSeconds).
 *
 * Design: FreshFoodz CI (#94C456, #004F7B), German labels, Sprache des Vertriebsmitarbeiters
 */
export default function StopTheClockDialog({
  open,
  lead,
  onClose,
  onSuccess,
}: StopTheClockDialogProps) {
  const [reason, setReason] = useState('');
  const [customReason, setCustomReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  const isStopped = !!lead?.clockStoppedAt;

  // Predefined stop reasons (German, business-oriented language)
  const stopReasons = [
    'Kunde ist im Urlaub',
    'Kunde hat technische Probleme',
    'Interner Abstimmungsbedarf',
    'Wartet auf Budget-Freigabe',
    'Sonstiges (bitte angeben)',
  ];

  const handleSave = async () => {
    if (!lead) return;

    // Validation for STOP action
    if (!isStopped) {
      if (!reason) {
        setError({
          status: 400,
          title: 'Validierungsfehler',
          detail: 'Bitte wählen Sie einen Grund aus.',
        });
        return;
      }

      if (reason === 'Sonstiges (bitte angeben)' && !customReason.trim()) {
        setError({
          status: 400,
          title: 'Validierungsfehler',
          detail: 'Bitte geben Sie einen benutzerdefinierten Grund an.',
        });
        return;
      }
    }

    setSaving(true);
    setError(null);

    try {
      const finalReason =
        reason === 'Sonstiges (bitte angeben)' ? customReason.trim() : reason;

      await updateLead(lead.id, {
        stopClock: !isStopped,
        stopReason: isStopped ? undefined : finalReason,
      });

      // Reset form
      setReason('');
      setCustomReason('');

      onSuccess();
    } catch (e) {
      setError(e as Problem);
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    if (!saving) {
      setReason('');
      setCustomReason('');
      setError(null);
      onClose();
    }
  };

  if (!lead) return null;

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {isStopped ? 'Schutzfrist fortsetzen' : 'Schutzfrist pausieren'}
      </DialogTitle>

      <DialogContent>
        <Box mb={2}>
          <Typography variant="body2" color="text.secondary">
            Lead: <strong>{lead.companyName}</strong>
          </Typography>
          {isStopped && lead.stopReason && (
            <Typography variant="body2" color="text.secondary" mt={1}>
              Aktueller Grund: <strong>{lead.stopReason}</strong>
            </Typography>
          )}
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error.title ?? 'Fehler'}
            {error.detail ? ` – ${error.detail}` : ''}
          </Alert>
        )}

        {isStopped ? (
          <Alert severity="info">
            Die Schutzfrist wurde pausiert. Durch Fortsetzen wird die
            verstrichene Zeit zur kumulativen Pause hinzugefügt.
          </Alert>
        ) : (
          <>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel id="stop-reason-label">Grund für Pause</InputLabel>
              <Select
                labelId="stop-reason-label"
                id="stop-reason"
                value={reason}
                label="Grund für Pause"
                onChange={(e) => setReason(e.target.value)}
                disabled={saving}
              >
                {stopReasons.map((r) => (
                  <MenuItem key={r} value={r}>
                    {r}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {reason === 'Sonstiges (bitte angeben)' && (
              <TextField
                fullWidth
                label="Benutzerdefinierter Grund"
                value={customReason}
                onChange={(e) => setCustomReason(e.target.value)}
                disabled={saving}
                multiline
                rows={3}
                placeholder="Bitte geben Sie einen detaillierten Grund an..."
              />
            )}
          </>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={saving}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSave}
          variant="contained"
          disabled={saving}
          color={isStopped ? 'primary' : 'warning'}
        >
          {isStopped ? 'Fortsetzen' : 'Pausieren'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
