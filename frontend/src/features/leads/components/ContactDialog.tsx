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
  MenuItem,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { toast } from 'react-hot-toast';
import type { LeadContactDTO } from '../types';
import { createLeadContact, updateLeadContact } from '../api';

interface ContactDialogProps {
  open: boolean;
  onClose: () => void;
  leadId: number;
  contact?: LeadContactDTO | null; // Wenn vorhanden = Bearbeiten, sonst = Neu
  onSave: () => void;
}

/**
 * ContactDialog - Kontakt hinzufügen/bearbeiten
 *
 * Sprint 2.1.6 Phase 5+
 * ADR-007: 100% Parity mit customer_contacts (26 Felder)
 *
 * TODO: Backend API implementieren
 */
export function ContactDialog({
  open,
  onClose,
  leadId: _leadId,
  contact,
  onSave,
}: ContactDialogProps) {
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    salutation: '',
    title: '',
    firstName: '',
    lastName: '',
    position: '',
    decisionLevel: '',
    email: '',
    phone: '',
    mobile: '',
  });

  useEffect(() => {
    if (contact) {
      setFormData({
        salutation: contact.salutation || '',
        title: contact.title || '',
        firstName: contact.firstName || '',
        lastName: contact.lastName || '',
        position: contact.position || '',
        decisionLevel: contact.decisionLevel || '',
        email: contact.email || '',
        phone: contact.phone || '',
        mobile: contact.mobile || '',
      });
    } else {
      // Reset für Neuanlage
      setFormData({
        salutation: '',
        title: '',
        firstName: '',
        lastName: '',
        position: '',
        decisionLevel: '',
        email: '',
        phone: '',
        mobile: '',
      });
    }
  }, [contact, open]);

  const handleSave = async () => {
    if (!formData.firstName.trim() || !formData.lastName.trim()) {
      toast.error('Vor- und Nachname sind erforderlich');
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
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {contact ? 'Kontakt bearbeiten' : 'Neuer Kontakt'}
        <IconButton
          aria-label="close"
          onClick={onClose}
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
            gridTemplateColumns: { xs: '1fr', sm: 'auto 1fr 1fr' },
            gap: 2,
          }}
        >
          {/* Anrede + Titel + Name */}
          <TextField
            select
            label="Anrede"
            value={formData.salutation}
            onChange={e => setFormData({ ...formData, salutation: e.target.value })}
            disabled={saving}
            sx={{ gridColumn: { xs: '1', sm: 'auto' } }}
          >
            <MenuItem value="">—</MenuItem>
            <MenuItem value="Herr">Herr</MenuItem>
            <MenuItem value="Frau">Frau</MenuItem>
            <MenuItem value="Divers">Divers</MenuItem>
          </TextField>

          <TextField
            label="Titel"
            value={formData.title}
            onChange={e => setFormData({ ...formData, title: e.target.value })}
            disabled={saving}
            placeholder="Dr., Prof., ..."
          />

          <Box
            sx={{
              gridColumn: { xs: '1', sm: 'span 2' },
              display: 'grid',
              gridTemplateColumns: '1fr 1fr',
              gap: 2,
            }}
          >
            <TextField
              label="Vorname"
              value={formData.firstName}
              onChange={e => setFormData({ ...formData, firstName: e.target.value })}
              disabled={saving}
              required
            />

            <TextField
              label="Nachname"
              value={formData.lastName}
              onChange={e => setFormData({ ...formData, lastName: e.target.value })}
              disabled={saving}
              required
            />
          </Box>

          {/* Position + Entscheidungsebene */}
          <TextField
            label="Position"
            value={formData.position}
            onChange={e => setFormData({ ...formData, position: e.target.value })}
            disabled={saving}
            placeholder="Küchenchef, Inhaber, ..."
            sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}
          />

          <TextField
            select
            label="Entscheidungsebene"
            value={formData.decisionLevel}
            onChange={e => setFormData({ ...formData, decisionLevel: e.target.value })}
            disabled={saving}
            sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}
          >
            <MenuItem value="">—</MenuItem>
            <MenuItem value="Entscheider">Entscheider</MenuItem>
            <MenuItem value="Beeinflusser">Beeinflusser</MenuItem>
            <MenuItem value="Umsetzer">Umsetzer</MenuItem>
          </TextField>

          {/* Kontaktdaten */}
          <TextField
            label="E-Mail"
            type="email"
            value={formData.email}
            onChange={e => setFormData({ ...formData, email: e.target.value })}
            disabled={saving}
            sx={{ gridColumn: { xs: '1', sm: '1 / -1' } }}
          />

          <TextField
            label="Telefon"
            value={formData.phone}
            onChange={e => setFormData({ ...formData, phone: e.target.value })}
            disabled={saving}
            placeholder="+49 ..."
            sx={{ gridColumn: { xs: '1', sm: 'span 1' } }}
          />

          <TextField
            label="Mobil"
            value={formData.mobile}
            onChange={e => setFormData({ ...formData, mobile: e.target.value })}
            disabled={saving}
            placeholder="+49 ..."
            sx={{ gridColumn: { xs: '1', sm: 'span 1' } }}
          />
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={saving}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSave}
          variant="contained"
          disabled={saving || !formData.firstName.trim() || !formData.lastName.trim()}
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
