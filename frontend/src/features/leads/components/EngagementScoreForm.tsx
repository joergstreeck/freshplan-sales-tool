import { useState, useEffect, useRef, useCallback } from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Alert,
  Typography,
  Divider,
  Chip,
} from '@mui/material';
import Grid from '@mui/material/Grid';
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

  const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved'>('idle');
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);
  const isFirstRenderRef = useRef(true);

  // Auto-Save Handler
  const autoSave = useCallback(async (immediate = false) => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    const saveFunction = async () => {
      setSaveStatus('saving');
      try {
        await onUpdate(formData);
        setSaveStatus('saved');
        setTimeout(() => setSaveStatus('idle'), 2000);
      } catch (error) {
        setSaveStatus('idle');
        console.error('Auto-save failed:', error);
      }
    };

    if (immediate) {
      await saveFunction();
    } else {
      debounceTimerRef.current = setTimeout(saveFunction, 1000);
    }
  }, [formData, onUpdate]);

  // Auto-save on formData changes
  useEffect(() => {
    if (isFirstRenderRef.current) {
      isFirstRenderRef.current = false;
      return;
    }

    const hasTextChanged =
      formData.competitorInUse !== lead.competitorInUse ||
      formData.internalChampionName !== lead.internalChampionName;
    autoSave(!hasTextChanged);
  }, [formData, autoSave, lead.competitorInUse, lead.internalChampionName]);

  // Cleanup
  useEffect(() => {
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  return (
    <Box sx={{ p: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Alert severity="info" sx={{ flex: 1, mr: 2 }}>
        Score: {lead.engagementScore || 0}/100{' '}
        {lead.engagementScore && lead.engagementScore >= 70
          ? '✅'
          : lead.engagementScore && lead.engagementScore >= 40
            ? '⚠️'
            : '❌'}
        </Alert>
        {saveStatus === 'saving' && (
          <Chip label="Speichert..." size="small" color="default" />
        )}
        {saveStatus === 'saved' && (
          <Chip label="Gespeichert ✓" size="small" color="success" />
        )}
      </Box>

      <Grid container spacing={3}>
        {/* Section 1: Beziehungsebene */}
        <Grid size={{ xs: 12 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
            Beziehungsebene
          </Typography>
          <Divider sx={{ mb: 2 }} />
        </Grid>

        {/* Relationship Status */}
        <Grid size={{ xs: 12, sm: 6 }}>
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
        <Grid size={{ xs: 12, sm: 6 }}>
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
        <Grid size={{ xs: 12 }} sx={{ mt: 2 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
            Weitere Informationen
          </Typography>
          <Divider sx={{ mb: 2 }} />
        </Grid>

        {/* Internal Champion + Competitor nebeneinander */}
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            label="Fürsprecher im Unternehmen"
            value={formData.internalChampionName}
            onChange={e => setFormData({ ...formData, internalChampionName: e.target.value })}
            fullWidth
            placeholder="Name des Fürsprechers"
            helperText="+30 Punkte wenn vorhanden"
          />
        </Grid>

        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField
            label="Aktueller Wettbewerber"
            value={formData.competitorInUse}
            onChange={e => setFormData({ ...formData, competitorInUse: e.target.value })}
            fullWidth
            placeholder="z.B. Metro, CHEFS CULINAR"
            helperText="Welcher Lieferant wird aktuell genutzt?"
          />
        </Grid>
      </Grid>
    </Box>
  );
}
