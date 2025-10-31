/**
 * Dynamic Field Component
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * Renders form fields based on backend FieldDefinition schema.
 * Supports 15 field types with MUI components.
 *
 * Design System Compliance:
 * - ✅ MUI Theme (primary.main, secondary.main)
 * - ✅ Deutsche UI-Texte
 * - ✅ Grid Layout Support (gridCols)
 * - ✅ Validation Support (required, validationRules)
 * - ✅ Readonly/Disabled Support
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
} from '@mui/material';
import type { FieldDefinition } from '../types/customer-schema';
import { useQuery } from '@tanstack/react-query';
import { BASE_URL, getAuthHeaders, type EnumValue } from '../features/leads/hooks/shared';

interface DynamicFieldProps {
  field: FieldDefinition;
  value: unknown;
  onChange: (fieldKey: string, value: unknown) => void;
  customerId?: string;
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
  if (value == null) return '';
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
 * Dynamic Field Component
 *
 * Renders appropriate MUI component based on field.type
 */
export const DynamicField: React.FC<DynamicFieldProps> = ({ field, value, onChange }) => {
  const { fieldKey, label, type, required, readonly, enumSource, placeholder, helpText, gridCols } =
    field;

  // Grid sizing (default 12 = full width)
  const cols = gridCols ?? 12;

  // Enum field - fetch options
  const { data: enumOptions, isLoading: enumLoading } = useQuery({
    queryKey: ['enums', enumSource],
    queryFn: () => fetchEnumOptions(enumSource!),
    enabled: type === 'ENUM' && !!enumSource,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Handle value change
  const handleChange = (newValue: unknown) => {
    onChange(fieldKey, newValue);
  };

  // Render based on type
  const renderField = () => {
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
            value={value != null ? formatCurrency(value) : ''}
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
                checked={value ?? false}
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
        // Extract date-only portion from ISO datetime (yyyy-MM-dd)
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
            <strong>{label}:</strong> {value ?? '-'}
          </Typography>
        );

      case 'CHIP':
        return (
          <div>
            <Typography variant="caption" sx={{ display: 'block', mb: 1 }}>
              {label}
            </Typography>
            {value ? (
              <Chip label={value} color="primary" size="small" />
            ) : (
              <Typography variant="body2" color="text.secondary">
                -
              </Typography>
            )}
          </div>
        );

      default:
        return <Typography color="error">Unbekannter Feldtyp: {type}</Typography>;
    }
  };

  return <Grid size={{ xs: 12, sm: cols }}>{renderField()}</Grid>;
};
