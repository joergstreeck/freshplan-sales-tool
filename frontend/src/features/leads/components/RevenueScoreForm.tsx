import { useState } from 'react';
import {
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Button,
  Alert,
  InputAdornment,
  FormHelperText,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import type { Lead } from '../types';

interface RevenueScoreFormProps {
  lead: Lead;
  onUpdate: (updates: Partial<Lead>) => Promise<void>;
}

function calculateDealSize(monthlyVolume: number | null): string {
  if (!monthlyVolume) return '';
  const annual = monthlyVolume * 12;

  if (annual >= 100000) return 'ENTERPRISE';
  if (annual >= 20000) return 'LARGE';
  if (annual >= 5000) return 'MEDIUM';
  return 'SMALL';
}

export function RevenueScoreForm({ lead, onUpdate }: RevenueScoreFormProps) {
  const [formData, setFormData] = useState({
    estimatedVolume: lead.estimatedVolume || null,
    budgetConfirmed: lead.budgetConfirmed || false,
    dealSize: lead.dealSize || '',
  });
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try {
      await onUpdate({
        estimatedVolume: formData.estimatedVolume,
        budgetConfirmed: formData.budgetConfirmed,
        dealSize: formData.dealSize || undefined,
      });
    } finally {
      setSaving(false);
    }
  };

  const autoDealSize = calculateDealSize(formData.estimatedVolume);

  return (
    <Box sx={{ p: 2 }}>
      <Alert severity="info" sx={{ mb: 2 }}>
        Score: {lead.revenueScore || 0}/100{' '}
        {lead.revenueScore && lead.revenueScore >= 70
          ? '✅'
          : lead.revenueScore && lead.revenueScore >= 40
            ? '⚠️'
            : '❌'}
      </Alert>

      <Grid container spacing={2}>
        {/* Estimated Volume */}
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            label="Geschätztes Jahresvolumen"
            type="number"
            value={formData.estimatedVolume || ''}
            onChange={e =>
              setFormData({
                ...formData,
                estimatedVolume: e.target.value ? parseFloat(e.target.value) : null,
              })
            }
            InputProps={{
              startAdornment: <InputAdornment position="start">€</InputAdornment>,
              endAdornment: <InputAdornment position="end">/Monat</InputAdornment>,
            }}
            helperText="Erwartetes monatliches Bestellvolumen"
            fullWidth
          />
        </Grid>

        {/* Deal Size */}
        <Grid size={{ xs: 12, sm: 6 }}>
          <FormControl fullWidth>
            <InputLabel>Deal Size</InputLabel>
            <Select
              value={formData.dealSize || autoDealSize}
              onChange={e => setFormData({ ...formData, dealSize: e.target.value })}
            >
              <MenuItem value="SMALL">Klein (1-5k €/Jahr)</MenuItem>
              <MenuItem value="MEDIUM">Mittel (5-20k €/Jahr)</MenuItem>
              <MenuItem value="LARGE">Groß (20-100k €/Jahr)</MenuItem>
              <MenuItem value="ENTERPRISE">Enterprise (100k+ €/Jahr)</MenuItem>
            </Select>
            <FormHelperText>
              {formData.dealSize
                ? 'Manuell gesetzt'
                : autoDealSize
                  ? `Automatisch: ${autoDealSize}`
                  : 'Nicht berechnet'}
            </FormHelperText>
          </FormControl>
        </Grid>

        {/* Budget Confirmed */}
        <Grid size={{ xs: 12 }}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.budgetConfirmed}
                onChange={e => setFormData({ ...formData, budgetConfirmed: e.target.checked })}
              />
            }
            label="Budget freigegeben / bestätigt"
          />
        </Grid>

        {/* Save Button */}
        <Grid size={{ xs: 12 }}>
          <Button variant="contained" onClick={handleSave} disabled={saving}>
            {saving ? 'Speichert...' : 'Speichern'}
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
}
