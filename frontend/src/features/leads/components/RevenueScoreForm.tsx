/**
 * RevenueScoreForm - Schema-Driven Revenue Score Form
 * Sprint 2.1.7.2 D11.2 - Server-Driven UI for Lead Scoring
 *
 * @description Dynamic form rendering based on backend schema (ScoreSchemaResource.java)
 * @since 2025-10-29 - Migrated from hardcoded to schema-driven
 */

import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
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
  Chip,
  CircularProgress,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import type { Lead } from '../types';
import { useScoreSchema } from '@/hooks/useScoreSchema';
import { useEnumOptions } from '@/hooks/useEnumOptions';
import type { FieldDefinition, FieldType } from '@/hooks/useContactSchema';

interface RevenueScoreFormProps {
  lead: Lead;
  onUpdate: (updates: Partial<Lead>) => Promise<void>;
}

/**
 * Get default value for a field type
 */
function getDefaultValue(type: FieldType): unknown {
  switch (type) {
    case 'TEXT':
    case 'TEXTAREA':
      return '';
    case 'NUMBER':
    case 'CURRENCY':
      return null;
    case 'BOOLEAN':
      return false;
    case 'ENUM':
      return '';
    case 'DATE':
    case 'DATETIME':
      return null;
    default:
      return null;
  }
}


export function RevenueScoreForm({ lead, onUpdate }: RevenueScoreFormProps) {
  // ========== SCHEMA LOADING ==========
  const { data: schemas, isLoading: schemaLoading } = useScoreSchema();
  const revenueSchema = useMemo(() => schemas?.find(s => s.cardId === 'revenue_score'), [schemas]);
  const revenueSection = useMemo(() => revenueSchema?.sections?.[0], [revenueSchema]);
  const fields = useMemo(() => revenueSection?.fields || [], [revenueSection]);

  // ========== FORM DATA (DYNAMIC) ==========
  const [formData, setFormData] = useState<Record<string, unknown>>(() => {
    const initial: Record<string, unknown> = {};
    return initial;
  });

  // Update formData when schema arrives
  useEffect(() => {
    if (fields.length > 0 && Object.keys(formData).length === 0) {
      const initial: Record<string, unknown> = {};
      fields.forEach(field => {
        const leadValue = lead[field.fieldKey as keyof Lead];
        initial[field.fieldKey] = leadValue !== undefined ? leadValue : getDefaultValue(field.type);
      });
      setFormData(initial);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fields.length]);

  // ========== AUTO-SAVE LOGIC ==========
  const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved'>('idle');
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);
  const isFirstRenderRef = useRef(true);
  const isSavingRef = useRef(false);

  const autoSave = useCallback(
    async (immediate = false) => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }

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
        debounceTimerRef.current = setTimeout(saveFunction, 1000);
      }
    },
    [formData, onUpdate]
  );

  // Auto-save on formData changes
  useEffect(() => {
    if (isFirstRenderRef.current) {
      isFirstRenderRef.current = false;
      return;
    }

    if (isSavingRef.current) {
      return;
    }

    const hasChanges = Object.keys(formData).some(key => {
      const leadValue = lead[key as keyof Lead];
      const formValue = formData[key];
      const leadDefault = leadValue !== undefined ? leadValue : getDefaultValue(
        fields.find(f => f.fieldKey === key)?.type || 'TEXT'
      );
      return formValue !== leadDefault;
    });

    if (hasChanges) {
      autoSave(true); // Always immediate for Revenue form
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [formData]);

  // Cleanup
  useEffect(() => {
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  // ========== FIELD RENDERING ==========
  const renderField = (field: FieldDefinition) => {
    const value = formData[field.fieldKey];

    switch (field.type) {
      case 'CURRENCY':
        return (
          <Grid size={{ xs: 12, sm: field.gridCols || 12 }} key={field.fieldKey}>
            <TextField
              label={field.label}
              type="number"
              value={value || ''}
              onChange={e =>
                setFormData({
                  ...formData,
                  [field.fieldKey]: e.target.value ? parseFloat(e.target.value) : null,
                })
              }
              inputProps={{ step: 1000 }}
              InputProps={{
                startAdornment: <InputAdornment position="start">€</InputAdornment>,
              }}
              fullWidth
              placeholder={field.placeholder}
              helperText={field.helpText}
            />
          </Grid>
        );

      case 'BOOLEAN':
        return (
          <Grid size={{ xs: 12, sm: field.gridCols || 12 }} key={field.fieldKey}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={Boolean(value)}
                  onChange={e =>
                    setFormData({ ...formData, [field.fieldKey]: e.target.checked })
                  }
                />
              }
              label={field.label}
            />
            {field.helpText && (
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block', ml: 4 }}>
                {field.helpText}
              </Typography>
            )}
          </Grid>
        );

      case 'ENUM':
        return (
          <Grid size={{ xs: 12, sm: field.gridCols || 12 }} key={field.fieldKey}>
            <EnumSelect
              field={field}
              value={String(value || '')}
              onChange={newValue =>
                setFormData({ ...formData, [field.fieldKey]: newValue })
              }
            />
          </Grid>
        );

      default:
        return (
          <Grid size={{ xs: 12, sm: field.gridCols || 12 }} key={field.fieldKey}>
            <TextField
              label={field.label}
              value={String(value || '')}
              onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.value })}
              fullWidth
              placeholder={field.placeholder}
              helperText={field.helpText}
            />
          </Grid>
        );
    }
  };

  // ========== LOADING STATE ==========
  if (schemaLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 300 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!revenueSchema || !revenueSection) {
    return (
      <Alert severity="error">
        Schema nicht gefunden. Bitte Backend prüfen (ScoreSchemaResource.java).
      </Alert>
    );
  }

  // ========== RENDER ==========
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
        {saveStatus === 'saving' && <Chip label="Speichert..." size="small" color="default" />}
        {saveStatus === 'saved' && <Chip label="Gespeichert ✓" size="small" color="success" />}
      </Box>

      {/* Section Title */}
      {revenueSection.title && (
        <Typography variant="h6" sx={{ mb: 1 }}>
          {revenueSection.title}
        </Typography>
      )}
      {revenueSection.subtitle && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {revenueSection.subtitle}
        </Typography>
      )}

      {/* Dynamic Fields */}
      <Grid container spacing={2}>
        {fields.map(field => renderField(field))}
      </Grid>
    </Box>
  );
}

/**
 * EnumSelect Component - Loads enum options from backend dynamically
 */
interface EnumSelectProps {
  field: FieldDefinition;
  value: string;
  onChange: (value: string) => void;
}

function EnumSelect({ field, value, onChange }: EnumSelectProps) {
  const { data: options, isLoading } = useEnumOptions(field.enumSource || '');

  return (
    <FormControl fullWidth>
      <InputLabel>{field.label}</InputLabel>
      <Select
        value={value}
        onChange={e => onChange(e.target.value)}
        label={field.label}
        disabled={isLoading}
      >
        {isLoading && <MenuItem disabled>Lädt...</MenuItem>}
        {options?.map(option => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
      {field.helpText && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, ml: 1.75 }}>
          {field.helpText}
        </Typography>
      )}
    </FormControl>
  );
}
