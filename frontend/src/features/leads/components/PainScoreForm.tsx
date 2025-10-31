/**
 * PainScoreForm - Schema-Driven Pain Score Form
 * Sprint 2.1.7.2 D11.2 - Server-Driven UI for Lead Scoring
 *
 * @description Dynamic form rendering based on backend schema (ScoreSchemaResource.java)
 * @since 2025-10-29 - Migrated from hardcoded to schema-driven
 */

import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
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
  Chip,
  CircularProgress,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import type { Lead } from '../types';
import { useScoreSchema } from '@/hooks/useScoreSchema';
import { useEnumOptions } from '@/hooks/useEnumOptions';
import type { FieldDefinition, FieldType } from '@/hooks/useContactSchema';

interface PainScoreFormProps {
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

export function PainScoreForm({ lead, onUpdate }: PainScoreFormProps) {
  // ========== SCHEMA LOADING ==========
  const { data: schemas, isLoading: schemaLoading } = useScoreSchema();
  const painSchema = useMemo(() => schemas?.find(s => s.cardId === 'pain_score'), [schemas]);
  const painSection = useMemo(() => painSchema?.sections?.[0], [painSchema]);
  const fields = useMemo(() => painSection?.fields || [], [painSection]);

  // ========== FORM DATA (DYNAMIC) ==========
  const [formData, setFormData] = useState<Record<string, unknown>>(() => {
    const initial: Record<string, unknown> = {};

    // Initialize all fields with lead values or defaults
    // Note: This runs before schema loads, so we initialize with empty object
    // and update in useEffect when schema arrives
    return initial;
  });

  // Update formData when schema arrives (first render)
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
  }, [fields.length]); // Only run when schema arrives

  // ========== AUTO-SAVE LOGIC (UNCHANGED FROM ORIGINAL) ==========
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
    const hasChanges = Object.keys(formData).some(key => {
      const leadValue = lead[key as keyof Lead];
      const formValue = formData[key];

      // Compare with lead value (handle undefined/null/false equivalence)
      const leadDefault =
        leadValue !== undefined
          ? leadValue
          : getDefaultValue(fields.find(f => f.fieldKey === key)?.type || 'TEXT');
      return formValue !== leadDefault;
    });

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

  // ========== FIELD RENDERING ==========
  const renderField = (field: FieldDefinition) => {
    const value = formData[field.fieldKey];

    switch (field.type) {
      case 'BOOLEAN':
        return (
          <Grid size={{ xs: 12, sm: field.gridCols || 12 }} key={field.fieldKey}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={Boolean(value)}
                  onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.checked })}
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
              onChange={newValue => setFormData({ ...formData, [field.fieldKey]: newValue })}
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
              onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.value })}
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

  if (!painSchema || !painSection) {
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

      {/* Section Title (from schema) */}
      {painSection.title && (
        <Typography variant="h6" sx={{ mb: 1 }}>
          {painSection.title}
        </Typography>
      )}
      {painSection.subtitle && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {painSection.subtitle}
        </Typography>
      )}

      {/* Dynamic Fields (from schema) */}
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
