/**
 * EngagementScoreForm - Schema-Driven Engagement Score Form
 * Sprint 2.1.7.2 D11.2 - Server-Driven UI for Lead Scoring
 *
 * @description Dynamic form rendering based on backend schema (ScoreSchemaResource.java)
 * @since 2025-10-29 - Migrated from hardcoded to schema-driven
 */

import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Alert,
  Chip,
  CircularProgress,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import type { Lead } from '../types';
import { useScoreSchema } from '@/hooks/useScoreSchema';
import { useEnumOptions } from '@/hooks/useEnumOptions';
import type { FieldDefinition, FieldType } from '@/hooks/useContactSchema';

interface EngagementScoreFormProps {
  lead: Lead;
  onUpdate: (updates: Partial<Lead>) => Promise<void>;
}

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

export function EngagementScoreForm({ lead, onUpdate }: EngagementScoreFormProps) {
  // ========== SCHEMA LOADING ==========
  const { data: schemas, isLoading: schemaLoading } = useScoreSchema();
  const engagementSchema = useMemo(
    () => schemas?.find(s => s.cardId === 'engagement_score'),
    [schemas]
  );
  const engagementSection = useMemo(() => engagementSchema?.sections?.[0], [engagementSchema]);
  const fields = useMemo(() => engagementSection?.fields || [], [engagementSection]);

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

  // Track last changed field to determine if debouncing should be used
  const lastChangedFieldRef = useRef<{ fieldKey: string; fieldType: FieldType } | null>(null);

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
      const leadDefault =
        leadValue !== undefined
          ? leadValue
          : getDefaultValue(fields.find(f => f.fieldKey === key)?.type || 'TEXT');
      return formValue !== leadDefault;
    });

    if (hasChanges) {
      // Use immediate save for ENUM fields (dropdowns), debounced for TEXT fields
      const lastChanged = lastChangedFieldRef.current;
      const shouldDebounce = lastChanged?.fieldType === 'TEXT' || lastChanged?.fieldType === 'TEXTAREA';
      autoSave(!shouldDebounce); // immediate for ENUM, debounced for TEXT
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
      case 'ENUM':
        return (
          <Grid size={{ xs: 12, sm: field.gridCols || 12 }} key={field.fieldKey}>
            <EnumSelect
              field={field}
              value={String(value || '')}
              onChange={newValue => {
                lastChangedFieldRef.current = { fieldKey: field.fieldKey, fieldType: 'ENUM' };
                setFormData({ ...formData, [field.fieldKey]: newValue });
              }}
            />
          </Grid>
        );

      case 'TEXT':
        return (
          <Grid size={{ xs: 12, sm: field.gridCols || 12 }} key={field.fieldKey}>
            <TextField
              label={field.label}
              value={String(value || '')}
              onChange={e => {
                lastChangedFieldRef.current = { fieldKey: field.fieldKey, fieldType: 'TEXT' };
                setFormData({ ...formData, [field.fieldKey]: e.target.value });
              }}
              fullWidth
              placeholder={field.placeholder}
              helperText={field.helpText}
            />
          </Grid>
        );

      case 'TEXTAREA':
        return (
          <Grid size={{ xs: 12 }} key={field.fieldKey}>
            <TextField
              label={field.label}
              multiline
              rows={4}
              value={String(value || '')}
              onChange={e => {
                lastChangedFieldRef.current = { fieldKey: field.fieldKey, fieldType: 'TEXTAREA' };
                setFormData({ ...formData, [field.fieldKey]: e.target.value });
              }}
              fullWidth
              placeholder={field.placeholder}
              helperText={field.helpText}
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

  if (!engagementSchema || !engagementSection) {
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
          Score: {lead.engagementScore || 0}/100{' '}
          {lead.engagementScore && lead.engagementScore >= 70
            ? '✅'
            : lead.engagementScore && lead.engagementScore >= 40
              ? '⚠️'
              : '❌'}
        </Alert>
        {saveStatus === 'saving' && <Chip label="Speichert..." size="small" color="default" />}
        {saveStatus === 'saved' && <Chip label="Gespeichert ✓" size="small" color="success" />}
      </Box>

      {/* Section Title */}
      {engagementSection.title && (
        <Typography variant="h6" sx={{ mb: 1 }}>
          {engagementSection.title}
        </Typography>
      )}
      {engagementSection.subtitle && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {engagementSection.subtitle}
        </Typography>
      )}

      {/* Dynamic Fields */}
      <Grid container spacing={3}>
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

  // DESIGN_SYSTEM.md: Prevent MUI warnings for out-of-range values
  // Only use value if it exists in loaded options
  const safeValue = options?.some(opt => opt.value === value) ? value : '';

  return (
    <FormControl fullWidth>
      <InputLabel>{field.label}</InputLabel>
      <Select
        value={safeValue}
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
