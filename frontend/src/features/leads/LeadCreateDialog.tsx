import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Alert,
  Box
} from '@mui/material';
import { createLead, Problem } from './api';

interface LeadCreateDialogProps {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export default function LeadCreateDialog({ open, onClose, onCreated }: LeadCreateDialogProps) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  const handleSave = async () => {
    if (!name.trim()) return;

    setSaving(true);
    setError(null);

    try {
      await createLead({
        name: name.trim(),
        email: email.trim() || undefined
      });

      // Reset form
      setName('');
      setEmail('');

      onCreated();
    } catch (e) {
      setError(e);
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    if (!saving) {
      setName('');
      setEmail('');
      setError(null);
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
      <DialogTitle>Lead anlegen</DialogTitle>
      <DialogContent>
        {error && (
          <Box mb={2}>
            <Alert severity="error">
              {error.title ?? 'Fehler'}
              {error.detail ? ` – ${error.detail}` : ''}
              {error.errors && Object.entries(error.errors).map(([field, messages]) => (
                <div key={field}>
                  <strong>{field}:</strong> {messages.join(', ')}
                </div>
              ))}
            </Alert>
          </Box>
        )}

        <TextField
          label="Name"
          value={name}
          onChange={e => setName(e.target.value)}
          fullWidth
          required
          margin="dense"
          disabled={saving}
          error={!name.trim() && name.length > 0}
          helperText={!name.trim() && name.length > 0 ? 'Name ist erforderlich' : ''}
        />

        <TextField
          label="E‑Mail"
          type="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          fullWidth
          margin="dense"
          disabled={saving}
        />
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={saving}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSave}
          variant="contained"
          disabled={saving || !name.trim()}
        >
          {saving ? 'Speichern...' : 'Speichern'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}