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
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { toast } from 'react-hot-toast';
import { updateLead } from '../api';
import type { Lead } from '../types';

interface LeadEditDialogProps {
  open: boolean;
  onClose: () => void;
  lead: Lead | null;
  onSave: () => void;
}

/**
 * LeadEditDialog - Stammdaten bearbeiten
 *
 * Sprint 2.1.6 Phase 5+
 * Best Practice: Modal Dialog für strukturierte Eingabe
 */
export function LeadEditDialog({ open, onClose, lead, onSave }: LeadEditDialogProps) {
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    companyName: '',
    website: '',
    street: '',
    postalCode: '',
    city: '',
  });

  useEffect(() => {
    if (lead) {
      setFormData({
        companyName: lead.companyName || '',
        website: lead.website || '',
        street: lead.street || '',
        postalCode: lead.postalCode || '',
        city: lead.city || '',
      });
    }
  }, [lead]);

  const handleSave = async () => {
    if (!lead) return;

    if (!formData.companyName.trim()) {
      toast.error('Firmenname ist erforderlich');
      return;
    }

    setSaving(true);
    try {
      await updateLead(lead.id, {
        companyName: formData.companyName.trim(),
        website: formData.website.trim() || undefined,
        street: formData.street.trim() || undefined,
        postalCode: formData.postalCode.trim() || undefined,
        city: formData.city.trim() || undefined,
      });

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

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        Stammdaten bearbeiten
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
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
            gap: 2,
          }}
        >
          <TextField
            label="Firmenname"
            value={formData.companyName}
            onChange={e => setFormData({ ...formData, companyName: e.target.value })}
            fullWidth
            required
            disabled={saving}
            sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}
          />

          <TextField
            label="Website"
            value={formData.website}
            onChange={e => setFormData({ ...formData, website: e.target.value })}
            fullWidth
            disabled={saving}
            placeholder="https://..."
            sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}
          />

          <TextField
            label="Straße"
            value={formData.street}
            onChange={e => setFormData({ ...formData, street: e.target.value })}
            fullWidth
            disabled={saving}
            sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}
          />

          <TextField
            label="PLZ"
            value={formData.postalCode}
            onChange={e => setFormData({ ...formData, postalCode: e.target.value })}
            fullWidth
            disabled={saving}
          />

          <TextField
            label="Stadt"
            value={formData.city}
            onChange={e => setFormData({ ...formData, city: e.target.value })}
            fullWidth
            disabled={saving}
          />
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={saving}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSave}
          variant="contained"
          disabled={saving || !formData.companyName.trim()}
          sx={{
            bgcolor: '#94C456',
            '&:hover': { bgcolor: '#7FB03E' },
          }}
        >
          {saving ? 'Speichert...' : 'Speichern'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
