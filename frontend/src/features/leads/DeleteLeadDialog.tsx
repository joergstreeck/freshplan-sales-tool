import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Alert,
  Box,
  Typography,
} from '@mui/material';
import { deleteLead } from './api';
import type { Lead, Problem } from './types';

interface DeleteLeadDialogProps {
  open: boolean;
  lead: Lead | null;
  onClose: () => void;
  onSuccess: () => void;
  context?: 'leads' | 'customers'; // Optional context for dynamic labels
}

/**
 * DeleteLead Confirmation Dialog
 *
 * Soft-Delete eines Leads (setzt status = DELETED).
 * Backend: DELETE /api/leads/{id} mit If-Match Header (ETag).
 *
 * Business Logic:
 * - Nur Owner oder Admin können löschen
 * - Soft Delete: Lead bleibt in DB, status = DELETED
 * - Activity-Eintrag: "Lead deleted"
 *
 * Design: FreshFoodz CI, German labels, kritischer Ton (error color)
 */
export default function DeleteLeadDialog({
  open,
  lead,
  onClose,
  onSuccess,
  context = 'leads', // Default to 'leads' for backwards compatibility
}: DeleteLeadDialogProps) {
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  // Dynamic labels based on context
  const entityLabel = context === 'leads' ? 'Lead' : 'Kunde';
  const entityLabelLowercase = context === 'leads' ? 'Lead' : 'Kunden';

  const handleDelete = async () => {
    if (!lead) return;

    setDeleting(true);
    setError(null);

    try {
      await deleteLead(lead.id);
      onSuccess();
    } catch (e) {
      setError(e as Problem);
    } finally {
      setDeleting(false);
    }
  };

  const handleClose = () => {
    if (!deleting) {
      setError(null);
      onClose();
    }
  };

  if (!lead) return null;

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>{entityLabel} löschen</DialogTitle>

      <DialogContent>
        <Box mb={2} mt={1}>
          <Typography variant="body1" gutterBottom>
            Möchten Sie diesen {entityLabelLowercase} wirklich löschen?
          </Typography>
          <Typography variant="body2" color="text.secondary" mt={1}>
            {entityLabel}: <strong>{lead.companyName}</strong>
          </Typography>
          {lead.contactPerson && (
            <Typography variant="body2" color="text.secondary">
              Kontakt: <strong>{lead.contactPerson}</strong>
            </Typography>
          )}
        </Box>

        <Alert severity="warning" sx={{ mb: 2 }}>
          Der {entityLabel} wird als gelöscht markiert (Soft Delete). Die Daten bleiben in der Datenbank
          erhalten, der Status wird auf "DELETED" gesetzt.
        </Alert>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error.title ?? 'Fehler'}
            {error.detail ? ` – ${error.detail}` : ''}
          </Alert>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={deleting}>
          Abbrechen
        </Button>
        <Button onClick={handleDelete} variant="contained" disabled={deleting} color="error">
          {deleting ? 'Lösche...' : 'Löschen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
