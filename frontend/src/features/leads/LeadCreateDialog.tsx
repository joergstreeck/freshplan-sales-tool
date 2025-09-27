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
import { useTranslation } from 'react-i18next';
import { createLead } from './api';
import type { Problem } from './types';

interface LeadCreateDialogProps {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export default function LeadCreateDialog({ open, onClose, onCreated }: LeadCreateDialogProps) {
  const { t } = useTranslation('leads');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  // Get field-specific errors from RFC7807 Problem
  const fieldErrors = error?.errors || {};

  // Client-side validation
  const validateForm = () => {
    const errors: Record<string, string[]> = {};

    if (!name.trim()) {
      errors.name = [t('errors.nameRequired')];
    } else if (name.trim().length < 2) {
      errors.name = [t('errors.nameMinLength')];
    }

    if (email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errors.email = [t('errors.emailInvalid')];
    }

    return Object.keys(errors).length > 0 ? { errors } : null;
  };

  const handleSave = async () => {
    const validationError = validateForm();
    if (validationError) {
      setError({ ...validationError, status: 400, title: 'Validation Error' });
      return;
    }

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
      <DialogTitle>{t('create.dialogTitle')}</DialogTitle>
      <DialogContent>
        {error?.status === 409 && (
          <Box mb={2}>
            <Alert severity="warning">
              {t('errors.duplicateEmail')}
            </Alert>
          </Box>
        )}

        {error && error.status !== 409 && (
          <Box mb={2}>
            <Alert severity="error">
              {error.title ?? 'Fehler'}
              {error.detail ? ` – ${error.detail}` : ''}
            </Alert>
          </Box>
        )}

        <TextField
          label={t('form.name')}
          value={name}
          onChange={e => setName(e.target.value)}
          fullWidth
          required
          margin="dense"
          disabled={saving}
          inputProps={{ minLength: 2 }}
          error={!!fieldErrors.name?.length}
          helperText={fieldErrors.name?.[0] || ''}
        />

        <TextField
          label={t('form.email')}
          type="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          fullWidth
          margin="dense"
          disabled={saving}
          error={!!fieldErrors.email?.length}
          helperText={fieldErrors.email?.[0] || ''}
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
          {saving ? 'Speichern...' : t('create.button')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}