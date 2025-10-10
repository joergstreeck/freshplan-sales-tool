import { useState } from 'react';
import {
  Box,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Alert,
  Typography,
  Divider,
} from '@mui/material';
import type { Lead } from '../types';

interface EngagementScoreFormProps {
  lead: Lead;
  onUpdate: (updates: Partial<Lead>) => Promise<void>;
}

export function EngagementScoreForm({ lead, onUpdate }: EngagementScoreFormProps) {
  const [formData, setFormData] = useState({
    relationshipStatus: lead.relationshipStatus || 'COLD',
    decisionMakerAccess: lead.decisionMakerAccess || 'UNKNOWN',
    competitorInUse: lead.competitorInUse || '',
    internalChampionName: lead.internalChampionName || '',
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
        Score: {lead.engagementScore || 0}/100{' '}
        {lead.engagementScore && lead.engagementScore >= 70
          ? '✅'
          : lead.engagementScore && lead.engagementScore >= 40
            ? '⚠️'
            : '❌'}
      </Alert>

      <Grid container spacing={3}>
        {/* Section 1: Beziehungsebene */}
        <Grid item xs={12}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
            Beziehungsebene
          </Typography>
          <Divider sx={{ mb: 2 }} />
        </Grid>

        {/* Relationship Status */}
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth>
            <InputLabel>Beziehungsqualität</InputLabel>
            <Select
              value={formData.relationshipStatus}
              onChange={e => setFormData({ ...formData, relationshipStatus: e.target.value })}
            >
              <MenuItem value="COLD">❄️ Kein Kontakt (0 Punkte)</MenuItem>
              <MenuItem value="CONTACTED">📞 Erstkontakt erfolgt (5 Punkte)</MenuItem>
              <MenuItem value="ENGAGED_SKEPTICAL">
                🤔 Im Gespräch - skeptisch (8 Punkte)
              </MenuItem>
              <MenuItem value="ENGAGED_POSITIVE">
                😊 Im Gespräch - positiv (12 Punkte)
              </MenuItem>
              <MenuItem value="TRUSTED">🤝 Vertrauensbasis aufgebaut (17 Punkte)</MenuItem>
              <MenuItem value="ADVOCATE">⭐ Fürsprecher / Befürworter (25 Punkte)</MenuItem>
            </Select>
          </FormControl>
        </Grid>

        {/* Decision Maker Access */}
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth>
            <InputLabel>Entscheider-Zugang</InputLabel>
            <Select
              value={formData.decisionMakerAccess}
              onChange={e => setFormData({ ...formData, decisionMakerAccess: e.target.value })}
            >
              <MenuItem value="UNKNOWN">❓ Noch nicht identifiziert (0 Punkte)</MenuItem>
              <MenuItem value="BLOCKED">🚫 Blockiert / Gatekeeper (-3 Punkte)</MenuItem>
              <MenuItem value="INDIRECT">🔄 Indirekter Zugang (10 Punkte)</MenuItem>
              <MenuItem value="DIRECT">✅ Direkter Kontakt (20 Punkte)</MenuItem>
              <MenuItem value="IS_DECISION_MAKER">👔 Ist Entscheider (25 Punkte)</MenuItem>
            </Select>
          </FormControl>
        </Grid>

        {/* Section 2: Weitere Informationen */}
        <Grid item xs={12} sx={{ mt: 2 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
            Weitere Informationen
          </Typography>
          <Divider sx={{ mb: 2 }} />
        </Grid>

        {/* Internal Champion + Competitor nebeneinander */}
        <Grid item xs={12} sm={6}>
          <TextField
            label="Fürsprecher im Unternehmen"
            value={formData.internalChampionName}
            onChange={e => setFormData({ ...formData, internalChampionName: e.target.value })}
            fullWidth
            placeholder="Name des Fürsprechers"
            helperText="+30 Punkte wenn vorhanden"
          />
        </Grid>

        <Grid item xs={12} sm={6}>
          <TextField
            label="Aktueller Wettbewerber"
            value={formData.competitorInUse}
            onChange={e => setFormData({ ...formData, competitorInUse: e.target.value })}
            fullWidth
            placeholder="z.B. Metro, CHEFS CULINAR"
            helperText="Welcher Lieferant wird aktuell genutzt?"
          />
        </Grid>

        {/* Save Button */}
        <Grid item xs={12}>
          <Button variant="contained" onClick={handleSave} disabled={saving} fullWidth>
            {saving ? 'Speichert...' : 'Speichern'}
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
}
