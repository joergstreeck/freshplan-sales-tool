/**
 * Dynamic Field Component
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * Renders form fields based on backend FieldDefinition schema.
 * Supports 16 field types with MUI components.
 *
 * ARCHITECTURE (Best Practice):
 * - Detail-View: Read-Only Mode (displayMode="readonly")
 * - Wizard/Edit: Edit Mode (displayMode="edit")
 * - Backend = Single Source of Truth
 *
 * Design System Compliance:
 * - MUI Theme (primary.main, secondary.main)
 * - Deutsche UI-Texte
 * - Grid Layout Support (gridCols)
 * - Validation Support (required, validationRules)
 * - Readonly/Disabled Support
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React from 'react';
import {
  TextField,
  Checkbox,
  FormControlLabel,
  Autocomplete,
  Typography,
  Chip,
  Grid,
  CircularProgress,
  Box,
  Paper,
  Link,
} from '@mui/material';
import type { FieldDefinition } from '../types/customer-schema';
import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from '../features/leads/hooks/shared';

/**
 * Display Mode for DynamicField
 *
 * - "readonly": Read-only display (Detail-View, Typography-based)
 * - "edit": Editable fields (Wizard, TextField-based)
 */
type DisplayMode = 'readonly' | 'edit';

interface DynamicFieldProps {
  field: FieldDefinition;
  value: unknown;
  onChange: (fieldKey: string, value: unknown) => void;
  customerId?: string;
  /**
   * Display mode: "readonly" for Detail-View, "edit" for Wizard
   * Default: "readonly" (Best Practice: Detail-Pages are read-only)
   */
  displayMode?: DisplayMode;
  /**
   * Nested value getter for GROUP/ARRAY types
   */
  getNestedValue?: (fieldKey: string) => unknown;
}

/**
 * Fetch enum options from backend
 *
 * Used for ENUM field type
 * Endpoint defined in field.enumSource (e.g., "/api/enums/business-types")
 */
async function fetchEnumOptions(enumSource: string): Promise<EnumValue[]> {
  const response = await fetch(`${BASE_URL}${enumSource}`, {
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch enum options: ${response.statusText}`);
  }

  return response.json();
}

/**
 * Format currency value for display
 */
function formatCurrency(value: number | null | undefined): string {
  if (value == null) return '-';
  return new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
  }).format(value);
}

/**
 * Parse currency string to number
 */
function parseCurrency(value: string): number | null {
  const cleaned = value.replace(/[^\d,-]/g, '').replace(',', '.');
  const parsed = parseFloat(cleaned);
  return isNaN(parsed) ? null : parsed;
}

/**
 * Format date for display (German format)
 */
function formatDate(value: string | null | undefined): string {
  if (!value) return '-';
  try {
    const date = new Date(value);
    return date.toLocaleDateString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  } catch {
    return value;
  }
}

/**
 * Format datetime for display (German format)
 */
function formatDateTime(value: string | null | undefined): string {
  if (!value) return '-';
  try {
    const date = new Date(value);
    return date.toLocaleString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return value;
  }
}

/**
 * Empty state text for missing values
 */
const EMPTY_VALUE = '-';

/**
 * Dynamic Field Component
 *
 * Renders appropriate MUI component based on field.type and displayMode
 */
export const DynamicField: React.FC<DynamicFieldProps> = ({
  field,
  value,
  onChange,
  displayMode = 'readonly', // Best Practice: Default to read-only
  getNestedValue: _getNestedValue, // Prefix with _ to satisfy ESLint
}) => {
  const { fieldKey, label, type, required, readonly, enumSource, placeholder, helpText, gridCols } =
    field;

  // Grid sizing (default 12 = full width)
  const cols = gridCols ?? 12;

  // Enum field - fetch options (only in edit mode or for label lookup in readonly)
  const { data: enumOptions, isLoading: enumLoading } = useQuery({
    queryKey: ['enums', enumSource],
    queryFn: () => fetchEnumOptions(enumSource!),
    enabled: type === 'ENUM' && !!enumSource,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Handle value change (only relevant in edit mode)
  const handleChange = (newValue: unknown) => {
    onChange(fieldKey, newValue);
  };

  // Get display value for text (handles null/undefined)
  const getDisplayValue = (val: unknown): string => {
    if (val == null || val === '') return EMPTY_VALUE;
    return String(val);
  };

  // Get enum label from value
  const getEnumLabel = (enumValue: unknown): string => {
    if (!enumValue) return EMPTY_VALUE;
    const option = enumOptions?.find(opt => opt.value === enumValue);
    return option?.label ?? String(enumValue);
  };

  // ========== READ-ONLY RENDERING ==========
  const renderReadOnly = () => {
    switch (type) {
      case 'TEXT':
      case 'EMAIL':
      case 'PHONE':
      case 'TEXTAREA':
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            <Typography variant="body1">{getDisplayValue(value)}</Typography>
          </Box>
        );

      case 'URL':
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            {value ? (
              <Link href={String(value)} target="_blank" rel="noopener noreferrer">
                {String(value)}
              </Link>
            ) : (
              <Typography variant="body1">{EMPTY_VALUE}</Typography>
            )}
          </Box>
        );

      case 'NUMBER':
      case 'DECIMAL':
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            <Typography variant="body1">
              {value != null ? new Intl.NumberFormat('de-DE').format(Number(value)) : EMPTY_VALUE}
            </Typography>
          </Box>
        );

      case 'CURRENCY':
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            <Typography variant="body1" fontWeight="medium">
              {formatCurrency(value as number | null | undefined)}
            </Typography>
          </Box>
        );

      case 'BOOLEAN':
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            <Typography variant="body1">{value ? 'Ja' : 'Nein'}</Typography>
          </Box>
        );

      case 'ENUM':
        if (enumLoading) {
          return <CircularProgress size={16} />;
        }
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            <Typography variant="body1">{getEnumLabel(value)}</Typography>
          </Box>
        );

      case 'DATE':
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            <Typography variant="body1">
              {formatDate(value as string | null | undefined)}
            </Typography>
          </Box>
        );

      case 'DATETIME':
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            <Typography variant="body1">
              {formatDateTime(value as string | null | undefined)}
            </Typography>
          </Box>
        );

      case 'LABEL':
        return (
          <Typography variant="body1" sx={{ py: 1 }}>
            <strong>{label}:</strong> {getDisplayValue(value)}
          </Typography>
        );

      case 'CHIP':
        return (
          <Box>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
              {label}
            </Typography>
            {value ? (
              <Chip label={String(value)} color="primary" size="small" />
            ) : (
              <Typography variant="body2" color="text.secondary">
                {EMPTY_VALUE}
              </Typography>
            )}
          </Box>
        );

      case 'GROUP':
        return renderGroupField();

      case 'ARRAY':
        return renderArrayField();

      default:
        return (
          <Typography color="error" variant="body2">
            Unbekannter Feldtyp: {type}
          </Typography>
        );
    }
  };

  // ========== GROUP FIELD RENDERING ==========
  const renderGroupField = () => {
    const nestedFields = field.fields ?? [];
    const groupValue = (value as Record<string, unknown>) ?? {};

    return (
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
          {label}
        </Typography>
        {helpText && (
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
            {helpText}
          </Typography>
        )}
        <Grid container spacing={2}>
          {nestedFields.map(nestedField => (
            <DynamicField
              key={nestedField.fieldKey}
              field={nestedField}
              value={groupValue[nestedField.fieldKey]}
              onChange={(nestedKey, newValue) => {
                // In read-only mode, onChange does nothing
                if (displayMode === 'edit') {
                  const newGroupValue = { ...groupValue, [nestedKey]: newValue };
                  onChange(fieldKey, newGroupValue);
                }
              }}
              displayMode={displayMode}
            />
          ))}
        </Grid>
      </Paper>
    );
  };

  // ========== ARRAY FIELD RENDERING ==========
  const renderArrayField = () => {
    const items = Array.isArray(value) ? value : [];
    const itemSchema = field.itemSchema;

    if (!itemSchema) {
      return (
        <Typography color="error" variant="body2">
          Fehler: itemSchema fehlt für ARRAY-Feld {fieldKey}
        </Typography>
      );
    }

    return (
      <Box>
        <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
          {label}
        </Typography>
        {helpText && (
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
            {helpText}
          </Typography>
        )}

        {items.length === 0 ? (
          <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
            Keine Eintr&auml;ge vorhanden
          </Typography>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {items.map((item, index) => (
              <Paper key={index} variant="outlined" sx={{ p: 2 }}>
                <Typography variant="caption" color="text.secondary" sx={{ mb: 1 }}>
                  Eintrag {index + 1}
                </Typography>
                {itemSchema.type === 'GROUP' && itemSchema.fields ? (
                  <Grid container spacing={2}>
                    {itemSchema.fields.map(nestedField => (
                      <DynamicField
                        key={nestedField.fieldKey}
                        field={nestedField}
                        value={(item as Record<string, unknown>)?.[nestedField.fieldKey]}
                        onChange={() => {
                          // In read-only mode, no changes
                        }}
                        displayMode={displayMode}
                      />
                    ))}
                  </Grid>
                ) : (
                  <DynamicField
                    field={itemSchema}
                    value={item}
                    onChange={() => {}}
                    displayMode={displayMode}
                  />
                )}
              </Paper>
            ))}
          </Box>
        )}
      </Box>
    );
  };

  // ========== EDIT MODE RENDERING ==========
  const renderEdit = () => {
    switch (type) {
      case 'TEXT':
      case 'EMAIL':
      case 'URL':
      case 'PHONE':
        return (
          <TextField
            label={label}
            value={value ?? ''}
            onChange={e => handleChange(e.target.value)}
            required={required}
            disabled={readonly}
            placeholder={placeholder ?? undefined}
            helperText={helpText}
            fullWidth
            type={type === 'EMAIL' ? 'email' : type === 'URL' ? 'url' : 'text'}
          />
        );

      case 'TEXTAREA':
        return (
          <TextField
            label={label}
            value={value ?? ''}
            onChange={e => handleChange(e.target.value)}
            required={required}
            disabled={readonly}
            placeholder={placeholder ?? undefined}
            helperText={helpText}
            fullWidth
            multiline
            rows={4}
          />
        );

      case 'NUMBER':
        return (
          <TextField
            label={label}
            value={value ?? ''}
            onChange={e => handleChange(e.target.value ? Number(e.target.value) : null)}
            required={required}
            disabled={readonly}
            placeholder={placeholder ?? undefined}
            helperText={helpText}
            fullWidth
            type="number"
          />
        );

      case 'DECIMAL':
        return (
          <TextField
            label={label}
            value={value ?? ''}
            onChange={e => handleChange(e.target.value ? parseFloat(e.target.value) : null)}
            required={required}
            disabled={readonly}
            placeholder={placeholder ?? undefined}
            helperText={helpText}
            fullWidth
            type="number"
            inputProps={{ step: '0.01' }}
          />
        );

      case 'CURRENCY':
        return (
          <TextField
            label={label}
            value={value != null ? formatCurrency(value as number) : ''}
            onChange={e => handleChange(parseCurrency(e.target.value))}
            required={required}
            disabled={readonly}
            placeholder={placeholder ?? '0,00 €'}
            helperText={helpText}
            fullWidth
          />
        );

      case 'BOOLEAN':
        return (
          <FormControlLabel
            control={
              <Checkbox
                checked={Boolean(value)}
                onChange={e => handleChange(e.target.checked)}
                disabled={readonly}
              />
            }
            label={label}
          />
        );

      case 'ENUM':
        if (enumLoading) {
          return <CircularProgress size={24} />;
        }

        return (
          <Autocomplete
            options={enumOptions ?? []}
            getOptionLabel={option => option.label}
            value={enumOptions?.find(opt => opt.value === value) ?? null}
            onChange={(_, newValue) => handleChange(newValue?.value ?? null)}
            disabled={readonly}
            renderInput={params => (
              <TextField
                {...params}
                label={label}
                required={required}
                placeholder={placeholder ?? undefined}
                helperText={helpText}
              />
            )}
            fullWidth
          />
        );

      case 'DATE': {
        const dateValue = value ? String(value).split('T')[0] : '';
        return (
          <TextField
            label={label}
            value={dateValue}
            onChange={e => handleChange(e.target.value)}
            required={required}
            disabled={readonly}
            helperText={helpText}
            fullWidth
            type="date"
            InputLabelProps={{ shrink: true }}
          />
        );
      }

      case 'DATETIME':
        return (
          <TextField
            label={label}
            value={value ?? ''}
            onChange={e => handleChange(e.target.value)}
            required={required}
            disabled={readonly}
            helperText={helpText}
            fullWidth
            type="datetime-local"
            InputLabelProps={{ shrink: true }}
          />
        );

      case 'LABEL':
        return (
          <Typography variant="body1" sx={{ py: 1 }}>
            <strong>{label}:</strong> {getDisplayValue(value)}
          </Typography>
        );

      case 'CHIP':
        return (
          <Box>
            <Typography variant="caption" sx={{ display: 'block', mb: 1 }}>
              {label}
            </Typography>
            {value ? (
              <Chip label={String(value)} color="primary" size="small" />
            ) : (
              <Typography variant="body2" color="text.secondary">
                {EMPTY_VALUE}
              </Typography>
            )}
          </Box>
        );

      case 'GROUP':
        return renderGroupField();

      case 'ARRAY':
        return renderArrayField();

      default:
        return (
          <Typography color="error" variant="body2">
            Unbekannter Feldtyp: {type}
          </Typography>
        );
    }
  };

  // Choose rendering based on display mode
  const renderField = () => {
    return displayMode === 'readonly' ? renderReadOnly() : renderEdit();
  };

  return <Grid size={{ xs: 12, sm: cols }}>{renderField()}</Grid>;
};
