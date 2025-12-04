/**
 * DSGVO-Löschungs-Dialog - Sprint 2.1.8
 *
 * Confirmation Dialog für DSGVO Art. 17 Löschung mit Grund-Auswahl.
 */

import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Alert,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Box,
} from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { GDPR_DELETION_REASONS, type GdprDeletionReasonType } from './types';
import { useGdprDelete } from './useGdprApi';

interface GdprDeleteDialogProps {
  open: boolean;
  onClose: () => void;
  leadId: number;
  companyName: string;
}

export function GdprDeleteDialog({ open, onClose, leadId, companyName }: GdprDeleteDialogProps) {
  const [reason, setReason] = useState<GdprDeletionReasonType | ''>('');
  const [customReason, setCustomReason] = useState('');

  const gdprDelete = useGdprDelete();

  const handleClose = () => {
    setReason('');
    setCustomReason('');
    onClose();
  };

  const handleDelete = async () => {
    const finalReason = reason === 'OTHER' ? customReason : reason;
    if (!finalReason || finalReason.length < 10) return;

    try {
      await gdprDelete.mutateAsync({ leadId, reason: finalReason });
      handleClose();
    } catch (error) {
      // Error wird von React Query gehandled
      console.error('DSGVO-Löschung fehlgeschlagen:', error);
    }
  };

  const isValid = reason && (reason !== 'OTHER' || customReason.length >= 10);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ color: 'error.main', display: 'flex', alignItems: 'center', gap: 1 }}>
        <WarningAmberIcon />
        DSGVO-Löschung (Art. 17)
      </DialogTitle>

      <DialogContent>
        <Alert severity="warning" sx={{ mb: 3 }}>
          Diese Aktion ist <strong>unwiderruflich</strong>. Alle personenbezogenen Daten werden
          anonymisiert. Der Audit-Trail bleibt für Compliance-Zwecke erhalten.
        </Alert>

        <Typography variant="body1" gutterBottom>
          Lead: <strong>{companyName}</strong> (ID: {leadId})
        </Typography>

        <Box sx={{ mt: 3 }}>
          <FormControl fullWidth required>
            <InputLabel id="gdpr-reason-label">Löschgrund</InputLabel>
            <Select
              labelId="gdpr-reason-label"
              value={reason}
              label="Löschgrund"
              onChange={e => setReason(e.target.value as GdprDeletionReasonType)}
            >
              {GDPR_DELETION_REASONS.map(r => (
                <MenuItem key={r.value} value={r.value}>
                  {r.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {reason === 'OTHER' && (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              multiline
              rows={3}
              label="Begründung (min. 10 Zeichen)"
              value={customReason}
              onChange={e => setCustomReason(e.target.value)}
              error={customReason.length > 0 && customReason.length < 10}
              helperText={
                customReason.length > 0 && customReason.length < 10
                  ? `Noch ${10 - customReason.length} Zeichen erforderlich`
                  : ''
              }
            />
          </Box>
        )}

        {gdprDelete.isError && (
          <Alert severity="error" sx={{ mt: 2 }}>
            Fehler: {(gdprDelete.error as Error)?.message || 'Löschung fehlgeschlagen'}
          </Alert>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={gdprDelete.isPending}>
          Abbrechen
        </Button>
        <Button
          color="error"
          variant="contained"
          onClick={handleDelete}
          disabled={!isValid || gdprDelete.isPending}
        >
          {gdprDelete.isPending ? 'Lösche...' : 'Unwiderruflich löschen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
