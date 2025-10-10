import { useState } from 'react';
import {
  Box,
  Grid,
  FormControlLabel,
  Checkbox,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Alert,
} from '@mui/material';
import type { Lead } from '../types';

interface PainScoreFormProps {
  lead: Lead;
  onUpdate: (updates: Partial<Lead>) => Promise<void>;
}

export function PainScoreForm({ lead, onUpdate }: PainScoreFormProps) {
  const [formData, setFormData] = useState({
    // 8 Pain Points
    painStaffShortage: lead.painStaffShortage || false,
    painHighCosts: lead.painHighCosts || false,
    painFoodWaste: lead.painFoodWaste || false,
    painQualityInconsistency: lead.painQualityInconsistency || false,
    painUnreliableDelivery: lead.painUnreliableDelivery || false,
    painPoorService: lead.painPoorService || false,
    painSupplierQuality: lead.painSupplierQuality || false,
    painTimePressure: lead.painTimePressure || false,

    // Urgency
    urgencyLevel: lead.urgencyLevel || 'NORMAL',

    // Details
    painNotes: lead.painNotes || '',
  });

  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try {
      await onUpdate(formData);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Box sx={{ p: 2 }}>
      <Alert severity="info" sx={{ mb: 2 }}>
        Score: {lead.painScore || 0}/100{' '}
        {lead.painScore && lead.painScore >= 70
          ? '✅'
          : lead.painScore && lead.painScore >= 40
            ? '⚠️'
            : '❌'}
      </Alert>

      <Grid container spacing={2}>
        {/* Pain Points Checkboxes */}
        <Grid item xs={12}>
          <strong>Operational Pains (Strukturelle Betriebsprobleme)</strong>
        </Grid>

        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.painStaffShortage}
                onChange={e => setFormData({ ...formData, painStaffShortage: e.target.checked })}
              />
            }
            label="Personalmangel / Fachkräftemangel"
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.painHighCosts}
                onChange={e => setFormData({ ...formData, painHighCosts: e.target.checked })}
              />
            }
            label="Hoher Kostendruck"
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.painFoodWaste}
                onChange={e => setFormData({ ...formData, painFoodWaste: e.target.checked })}
              />
            }
            label="Food Waste / Überproduktion"
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.painQualityInconsistency}
                onChange={e =>
                  setFormData({ ...formData, painQualityInconsistency: e.target.checked })
                }
              />
            }
            label="Interne Qualitätsinkonsistenz"
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.painTimePressure}
                onChange={e => setFormData({ ...formData, painTimePressure: e.target.checked })}
              />
            }
            label="Zeitdruck / Effizienz"
          />
        </Grid>

        {/* Lieferanten-Probleme Section Header */}
        <Grid item xs={12}>
          <strong>Lieferanten-Probleme</strong>
        </Grid>

        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.painSupplierQuality}
                onChange={e => setFormData({ ...formData, painSupplierQuality: e.target.checked })}
              />
            }
            label="Qualitätsprobleme beim Lieferanten"
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.painUnreliableDelivery}
                onChange={e =>
                  setFormData({ ...formData, painUnreliableDelivery: e.target.checked })
                }
              />
            }
            label="Unzuverlässige Lieferzeiten"
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.painPoorService}
                onChange={e => setFormData({ ...formData, painPoorService: e.target.checked })}
              />
            }
            label="Schlechter Service/Support"
          />
        </Grid>

        {/* Urgency Level */}
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth>
            <InputLabel>Dringlichkeit</InputLabel>
            <Select
              value={formData.urgencyLevel}
              onChange={e => setFormData({ ...formData, urgencyLevel: e.target.value })}
            >
              <MenuItem value="NORMAL">Normal (0 Punkte)</MenuItem>
              <MenuItem value="MEDIUM">Mittel (15 Punkte)</MenuItem>
              <MenuItem value="HIGH">Hoch (22 Punkte)</MenuItem>
              <MenuItem value="EMERGENCY">Notfall (30 Punkte)</MenuItem>
            </Select>
          </FormControl>
        </Grid>

        {/* Pain Details */}
        <Grid item xs={12}>
          <TextField
            label="Weitere Details zu Pain-Faktoren (optional)"
            multiline
            rows={4}
            value={formData.painNotes}
            onChange={e => setFormData({ ...formData, painNotes: e.target.value })}
            fullWidth
            placeholder="Beschreiben Sie konkrete Probleme, Auswirkungen oder besondere Umstände..."
          />
        </Grid>

        {/* Save Button */}
        <Grid item xs={12}>
          <Button variant="contained" onClick={handleSave} disabled={saving}>
            {saving ? 'Speichert...' : 'Speichern'}
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
}
