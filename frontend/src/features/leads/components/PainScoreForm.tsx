import { useState, useEffect, useRef, useCallback } from 'react';
import {
  Box,
  FormControlLabel,
  Checkbox,
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

  const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved'>('idle');
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);
  const isFirstRenderRef = useRef(true);
  const isSavingRef = useRef(false);

  // Auto-Save Handler with debounce for text fields
  const autoSave = useCallback(
    async (immediate = false) => {
      // Cancel pending debounce
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }

      // Prevent concurrent saves
      if (isSavingRef.current) {
        return;
      }

      const saveFunction = async () => {
        isSavingRef.current = true;
        setSaveStatus('saving');
        try {
          await onUpdate(formData);
          setSaveStatus('saved');
          setTimeout(() => setSaveStatus('idle'), 2000);
        } catch (error) {
          setSaveStatus('idle');
          console.error('Auto-save failed:', error);
        } finally {
          isSavingRef.current = false;
        }
      };

      if (immediate) {
        await saveFunction();
      } else {
        // 2s debounce for text fields (good balance between responsiveness and API load)
        debounceTimerRef.current = setTimeout(saveFunction, 2000);
      }
    },
    [formData, onUpdate]
  );

  // Auto-save on formData changes (skip first render)
  useEffect(() => {
    if (isFirstRenderRef.current) {
      isFirstRenderRef.current = false;
      return;
    }

    // Skip if already saving
    if (isSavingRef.current) {
      return;
    }

    // Check if data actually changed from lead props (prevent save on mount)
    const hasChanges =
      formData.painStaffShortage !== (lead.painStaffShortage || false) ||
      formData.painHighCosts !== (lead.painHighCosts || false) ||
      formData.painFoodWaste !== (lead.painFoodWaste || false) ||
      formData.painQualityInconsistency !== (lead.painQualityInconsistency || false) ||
      formData.painUnreliableDelivery !== (lead.painUnreliableDelivery || false) ||
      formData.painPoorService !== (lead.painPoorService || false) ||
      formData.painSupplierQuality !== (lead.painSupplierQuality || false) ||
      formData.painTimePressure !== (lead.painTimePressure || false) ||
      formData.urgencyLevel !== (lead.urgencyLevel || 'NORMAL') ||
      formData.painNotes !== (lead.painNotes || '');

    // Only save if there are actual changes
    if (hasChanges) {
      // Trigger auto-save with smart debouncing:
      // - Checkboxes/Selects: immediate (better UX)
      // - TextField (painNotes): 2s debounce (prevent spam while typing)
      const isTextFieldChange = formData.painNotes !== lead.painNotes;
      autoSave(!isTextFieldChange); // Immediate if NOT text field
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [formData]); // ONLY formData - autoSave causes infinite loop!

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
          Score: {lead.painScore || 0}/100{' '}
          {lead.painScore && lead.painScore >= 70
            ? '✅'
            : lead.painScore && lead.painScore >= 40
              ? '⚠️'
              : '❌'}
        </Alert>
        {saveStatus === 'saving' && <Chip label="Speichert..." size="small" color="default" />}
        {saveStatus === 'saved' && <Chip label="Gespeichert ✓" size="small" color="success" />}
      </Box>

      <Grid container spacing={3}>
        {/* Section 1: Operational Pains */}
        <Grid size={{ xs: 12 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
            Operational Pains (Strukturelle Betriebsprobleme)
          </Typography>
          <Divider sx={{ mb: 2 }} />
        </Grid>

        <Grid size={{ xs: 12, sm: 6 }}>
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

        <Grid size={{ xs: 12, sm: 6 }}>
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

        <Grid size={{ xs: 12, sm: 6 }}>
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

        <Grid size={{ xs: 12, sm: 6 }}>
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

        <Grid size={{ xs: 12, sm: 6 }}>
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

        {/* Section 2: Lieferanten-Probleme */}
        <Grid size={{ xs: 12 }} sx={{ mt: 2 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
            Lieferanten-Probleme
          </Typography>
          <Divider sx={{ mb: 2 }} />
        </Grid>

        <Grid size={{ xs: 12, sm: 6 }}>
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

        <Grid size={{ xs: 12, sm: 6 }}>
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

        <Grid size={{ xs: 12, sm: 6 }}>
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

        {/* Section 3: Dringlichkeit */}
        <Grid size={{ xs: 12 }} sx={{ mt: 2 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
            Dringlichkeit
          </Typography>
          <Divider sx={{ mb: 2 }} />
        </Grid>

        <Grid size={{ xs: 12, sm: 6 }}>
          <FormControl fullWidth>
            <InputLabel>Dringlichkeitsstufe</InputLabel>
            <Select
              value={formData.urgencyLevel}
              onChange={e => setFormData({ ...formData, urgencyLevel: e.target.value })}
            >
              <MenuItem value="NORMAL">⏸️ Normal (0 Punkte)</MenuItem>
              <MenuItem value="MEDIUM">⏰ Mittel (15 Punkte)</MenuItem>
              <MenuItem value="HIGH">🔥 Hoch (22 Punkte)</MenuItem>
              <MenuItem value="EMERGENCY">🚨 Notfall (30 Punkte)</MenuItem>
            </Select>
          </FormControl>
        </Grid>

        {/* Section 4: Details */}
        <Grid size={{ xs: 12 }} sx={{ mt: 2 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mb: 1 }}>
            Weitere Details
          </Typography>
          <Divider sx={{ mb: 2 }} />
        </Grid>

        <Grid size={{ xs: 12 }}>
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
      </Grid>
    </Box>
  );
}
