import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Alert,
  Box,
  Stack,
} from '@mui/material';
import { updateLead } from './api';
import type { Lead, Problem } from './types';

interface LeadEditDialogProps {
  open: boolean;
  lead: Lead | null;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * LeadEdit Dialog
 *
 * Bearbeiten von Lead-Stammdaten (Company, Contact, Address).
 * Backend: PATCH /api/leads/{id} mit If-Match Header (ETag).
 *
 * Editable Fields:
 * - Company: companyName, website
 * - Contact: contactPerson, email, phone
 * - Address: street, postalCode, city
 *
 * Design: FreshFoodz CI, German labels, Grid-Layout (2 Spalten)
 */
export default function LeadEditDialog({
  open,
  lead,
  onClose,
  onSuccess,
}: LeadEditDialogProps) {
  const [formData, setFormData] = useState({
    companyName: '',
    contactPerson: '',
    email: '',
    phone: '',
    website: '',
    street: '',
    postalCode: '',
    city: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  // Initialize form when lead changes
  useEffect(() => {
    if (lead) {
      setFormData({
        companyName: lead.companyName || '',
        contactPerson: lead.contactPerson || '',
        email: lead.email || '',
        phone: lead.phone || '',
        website: lead.website || '',
        street: lead.street || '',
        postalCode: lead.postalCode || '',
        city: lead.city || '',
      });
    }
  }, [lead]);

  const handleChange = (field: keyof typeof formData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [field]: e.target.value }));
  };

  const handleSave = async () => {
    if (!lead) return;

    // Frontend validation
    if (!formData.companyName.trim()) {
      setError({
        status: 400,
        title: 'Validierungsfehler',
        detail: 'Firmenname ist ein Pflichtfeld.',
      });
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await updateLead(lead.id, {
        companyName: formData.companyName.trim(),
        contactPerson: formData.contactPerson.trim() || undefined,
        email: formData.email.trim() || undefined,
        phone: formData.phone.trim() || undefined,
        website: formData.website.trim() || undefined,
        street: formData.street.trim() || undefined,
        postalCode: formData.postalCode.trim() || undefined,
        city: formData.city.trim() || undefined,
      });

      onSuccess();
    } catch (e) {
      setError(e as Problem);
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    if (!saving) {
      setError(null);
      onClose();
    }
  };

  if (!lead) return null;

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>Lead bearbeiten</DialogTitle>

      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error.title ?? 'Fehler'}
            {error.detail ? ` – ${error.detail}` : ''}
          </Alert>
        )}

        <Stack spacing={2} mt={1}>
          {/* Company Section */}
          <TextField
            fullWidth
            required
            label="Firmenname"
            value={formData.companyName}
            onChange={handleChange('companyName')}
            disabled={saving}
          />

          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              fullWidth
              label="Website"
              value={formData.website}
              onChange={handleChange('website')}
              disabled={saving}
              placeholder="https://example.com"
            />
            <TextField
              fullWidth
              label="Ansprechpartner"
              value={formData.contactPerson}
              onChange={handleChange('contactPerson')}
              disabled={saving}
            />
          </Box>

          {/* Contact Section */}
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              fullWidth
              label="E-Mail"
              type="email"
              value={formData.email}
              onChange={handleChange('email')}
              disabled={saving}
            />
            <TextField
              fullWidth
              label="Telefon"
              value={formData.phone}
              onChange={handleChange('phone')}
              disabled={saving}
            />
          </Box>

          {/* Address Section */}
          <TextField
            fullWidth
            label="Straße"
            value={formData.street}
            onChange={handleChange('street')}
            disabled={saving}
          />

          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              label="PLZ"
              value={formData.postalCode}
              onChange={handleChange('postalCode')}
              disabled={saving}
              sx={{ width: '30%' }}
            />
            <TextField
              fullWidth
              label="Stadt"
              value={formData.city}
              onChange={handleChange('city')}
              disabled={saving}
            />
          </Box>
        </Stack>
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
