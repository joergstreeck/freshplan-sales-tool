import { useState, useEffect, useRef, useCallback } from 'react';
import {
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Alert,
  InputAdornment,
  FormHelperText,
  Chip,
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
        await onUpdate({
          estimatedVolume: formData.estimatedVolume,
          budgetConfirmed: formData.budgetConfirmed,
          dealSize: formData.dealSize || undefined,
        });
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

    // Immediate save for checkbox/dropdown, debounced for number field
    const hasNumberChanged = formData.estimatedVolume !== lead.estimatedVolume;
    autoSave(!hasNumberChanged);
  }, [formData, autoSave, lead.estimatedVolume]);

  // Cleanup
  useEffect(() => {
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  const autoDealSize = calculateDealSize(formData.estimatedVolume);

  return (
    <Box sx={{ p: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Alert severity="info" sx={{ flex: 1, mr: 2 }}>
          Score: {lead.revenueScore || 0}/100{' '}
          {lead.revenueScore && lead.revenueScore >= 70
            ? '✅'
            : lead.revenueScore && lead.revenueScore >= 40
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
      </Grid>
    </Box>
  );
}
