import React, { useState } from 'react';
import {
  Box, Typography, TextField, Button, MenuItem, Alert, Paper
} from '@mui/material';

type CustomerType = 'HOTEL' | 'RESTAURANT' | 'BETRIEBSGASTRONOMIE' | 'CATERING';

const CUSTOMER_TYPES: { value: CustomerType; label: string }[] = [
  { value: 'HOTEL', label: 'Hotel' },
  { value: 'RESTAURANT', label: 'Restaurant' },
  { value: 'BETRIEBSGASTRONOMIE', label: 'Betriebsgastronomie' },
  { value: 'CATERING', label: 'Catering' }
];

const TERRITORIES = ['BER', 'HAM', 'MUC', 'CGN', 'FRA', 'STU', 'DUS'];

export default function NeuerKunde() {
  const [formData, setFormData] = useState({
    name: '',
    territory: '',
    type: '' as CustomerType | '',
    email: '',
    phone: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Name ist erforderlich';
    } else if (formData.name.length > 200) {
      newErrors.name = 'Name zu lang (max. 200 Zeichen)';
    }

    if (!formData.territory) {
      newErrors.territory = 'Territory ist erforderlich';
    }

    if (!formData.type) {
      newErrors.type = 'Kundentyp ist erforderlich';
    }

    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Ungültige E-Mail-Adresse';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      const response = await fetch('/api/customers', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}` // TODO: Proper auth context
        },
        body: JSON.stringify({
          name: formData.name,
          territory: formData.territory,
          type: formData.type,
          email: formData.email || null,
          phone: formData.phone || null
        })
      });

      if (response.ok) {
        setSubmitSuccess(true);
        setFormData({ name: '', territory: '', type: '', email: '', phone: '' });
        setTimeout(() => setSubmitSuccess(false), 3000);
      } else {
        const error = await response.json();
        setErrors({ submit: error.detail || 'Fehler beim Erstellen des Kunden' });
      }
    } catch (error) {
      setErrors({ submit: 'Netzwerkfehler beim Erstellen des Kunden' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto', p: 3 }}>
      <Typography variant="h2" sx={{ mb: 3 }}>
        Neuer Kunde
      </Typography>

      <Paper sx={{ p: 3, borderRadius: 'var(--radius-md)', boxShadow: 'var(--shadow-sm)' }}>
        {submitSuccess && (
          <Alert severity="success" sx={{ mb: 2 }}>
            Kunde erfolgreich erstellt!
          </Alert>
        )}

        {errors.submit && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {errors.submit}
          </Alert>
        )}

        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Firmenname *"
            value={formData.name}
            onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
            error={!!errors.name}
            helperText={errors.name}
            sx={{ mb: 2 }}
            inputProps={{ maxLength: 200 }}
          />

          <TextField
            fullWidth
            select
            label="Territory *"
            value={formData.territory}
            onChange={(e) => setFormData(prev => ({ ...prev, territory: e.target.value }))}
            error={!!errors.territory}
            helperText={errors.territory}
            sx={{ mb: 2 }}
          >
            {TERRITORIES.map(territory => (
              <MenuItem key={territory} value={territory}>
                {territory}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            fullWidth
            select
            label="Kundentyp *"
            value={formData.type}
            onChange={(e) => setFormData(prev => ({ ...prev, type: e.target.value as CustomerType }))}
            error={!!errors.type}
            helperText={errors.type}
            sx={{ mb: 2 }}
          >
            {CUSTOMER_TYPES.map(type => (
              <MenuItem key={type.value} value={type.value}>
                {type.label}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            fullWidth
            label="E-Mail"
            type="email"
            value={formData.email}
            onChange={(e) => setFormData(prev => ({ ...prev, email: e.target.value }))}
            error={!!errors.email}
            helperText={errors.email}
            sx={{ mb: 2 }}
          />

          <TextField
            fullWidth
            label="Telefon"
            value={formData.phone}
            onChange={(e) => setFormData(prev => ({ ...prev, phone: e.target.value }))}
            sx={{ mb: 3 }}
          />

          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={isSubmitting}
            sx={{
              borderRadius: 'var(--radius-md)',
              boxShadow: 'var(--shadow-sm)',
              minWidth: 150
            }}
          >
            {isSubmitting ? 'Erstelle...' : 'Kunde erstellen'}
          </Button>
        </form>
      </Paper>
    </Box>
  );
}