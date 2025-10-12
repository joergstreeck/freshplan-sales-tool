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
  Typography,
} from '@mui/material';
import { addFirstContact } from './api';
import type { Lead, Problem } from './types';

interface AddFirstContactDialogProps {
  open: boolean;
  lead: Lead | null;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * AddFirstContact Dialog (Sprint 2.1.6 Phase 5)
 *
 * Ermöglicht das Dokumentieren des Erstkontakts mit einem Vormerkung-Lead.
 * Transitions lead from VORMERKUNG (Stage 0) to REGISTRIERUNG (Stage 1).
 *
 * Business Logic:
 * - Lead MUSS in Stage VORMERKUNG sein
 * - contactPerson ist Pflicht (2-255 Zeichen)
 * - email/phone/notes sind optional
 * - registeredAt wird automatisch gesetzt → 6-Monats-Schutz startet
 *
 * Design: FreshFoodz CI (#94C456, #004F7B), German labels
 */
export default function AddFirstContactDialog({
  open,
  lead,
  onClose,
  onSuccess,
}: AddFirstContactDialogProps) {
  const [contactPerson, setContactPerson] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  const handleSave = async () => {
    if (!lead) return;

    // Frontend validation
    if (!contactPerson.trim()) {
      setError({
        status: 400,
        title: 'Validierungsfehler',
        detail: 'Bitte geben Sie einen Ansprechpartner an.',
      });
      return;
    }

    if (contactPerson.trim().length < 2) {
      setError({
        status: 400,
        title: 'Validierungsfehler',
        detail: 'Ansprechpartner muss mindestens 2 Zeichen lang sein.',
      });
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await addFirstContact(lead.id, {
        contactPerson: contactPerson.trim(),
        email: email.trim() || undefined,
        phone: phone.trim() || undefined,
        notes: notes.trim() || undefined,
      });

      // Reset form
      setContactPerson('');
      setEmail('');
      setPhone('');
      setNotes('');

      onSuccess();
    } catch (e) {
      setError(e as Problem);
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    if (!saving) {
      setContactPerson('');
      setEmail('');
      setPhone('');
      setNotes('');
      setError(null);
      onClose();
    }
  };

  if (!lead) return null;

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Erstkontakt dokumentieren</DialogTitle>

      <DialogContent>
        <Box mb={2} mt={1}>
          <Typography variant="body2" color="text.secondary">
            Lead: <strong>{lead.companyName}</strong>
          </Typography>
          <Typography variant="body2" color="text.secondary" mt={0.5}>
            Nach dem Speichern wechselt der Lead von <strong>Vormerkung</strong> zu{' '}
            <strong>Registrierung</strong> und die 6-monatige Schutzfrist startet.
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error.title ?? 'Fehler'}
            {error.detail ? ` – ${error.detail}` : ''}
          </Alert>
        )}

        <TextField
          fullWidth
          required
          label="Ansprechpartner"
          value={contactPerson}
          onChange={e => setContactPerson(e.target.value)}
          disabled={saving}
          placeholder="z.B. Max Mustermann"
          sx={{ mb: 2 }}
          helperText="Pflichtfeld: Name der Kontaktperson (mind. 2 Zeichen)"
        />

        <TextField
          fullWidth
          label="E-Mail"
          type="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          disabled={saving}
          placeholder="z.B. max.mustermann@firma.de"
          sx={{ mb: 2 }}
          helperText="Optional: E-Mail-Adresse der Kontaktperson"
        />

        <TextField
          fullWidth
          label="Telefon"
          value={phone}
          onChange={e => setPhone(e.target.value)}
          disabled={saving}
          placeholder="z.B. +49 123 456789"
          sx={{ mb: 2 }}
          helperText="Optional: Telefonnummer der Kontaktperson"
        />

        <TextField
          fullWidth
          label="Notizen"
          value={notes}
          onChange={e => setNotes(e.target.value)}
          disabled={saving}
          multiline
          rows={3}
          placeholder="z.B. Kontakt über Telefon, sehr interessiert, möchte Angebot in 2 Wochen..."
          helperText="Optional: Zusätzliche Notizen zum Erstkontakt (max. 1000 Zeichen)"
        />
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={saving}>
          Abbrechen
        </Button>
        <Button onClick={handleSave} variant="contained" disabled={saving} color="primary">
          {saving ? 'Speichere...' : 'Speichern'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
