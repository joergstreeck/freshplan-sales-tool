/**
 * AdaptiveField - Intelligentes Feld mit dynamischer Größenanpassung
 *
 * Felder wachsen basierend auf Inhalt bis zu einem definierten Maximum.
 * Rendert NUR das TextField - Label und Container werden von FieldWrapper übernommen.
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_IMPLEMENTATION_GUIDE.md
 */

import React, { useState, useRef, useEffect } from 'react';
import { styled } from '@mui/material/styles';
import { TextField } from '@mui/material';
import type { FieldDefinition } from '../../../../types/field.types';
import { useCustomerFieldTheme } from '../../theme';
import { getFieldSize } from '../../utils/fieldSizeCalculator';
import { calculateFieldWidth } from '../../utils/adaptiveFieldCalculator';

interface AdaptiveFieldProps {
  field: FieldDefinition;
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  error?: string;
  disabled?: boolean;
  readOnly?: boolean;
}

const MeasureSpan = styled('span')({
  position: 'absolute',
  visibility: 'hidden',
  whiteSpace: 'pre',
  fontSize: '14px',
  fontFamily: 'var(--kunde-text-schrift, Poppins, sans-serif)',
  padding: '0 14px',
  pointerEvents: 'none',
  zIndex: -1,
});

const StyledTextField = styled(TextField)(() => ({
  '& .MuiInputBase-root': {
    transition: `width var(--kunde-übergang, 0.15s ease-out)`,
    minHeight: '44px', // Touch-friendly Mindesthöhe
    fontFamily: 'var(--kunde-text-schrift, Poppins, sans-serif)',
  },
  '& .MuiInputBase-input': {
    padding: '12px 16px', // Optimales Padding für Lesbarkeit
  },
  '& .MuiOutlinedInput-root': {
    '&:hover fieldset': {
      borderColor: 'var(--kunde-primär, #94C456)',
    },
    '&.Mui-focused fieldset': {
      borderColor: 'var(--kunde-primär, #94C456)',
    },
  },
}));

/**
 * Adaptive Field Component
 *
 * Features:
 * - Dynamisches Wachstum basierend auf Inhalt
 * - Sanfte Animationen
 * - Nutzt fieldSizeCalculator für korrekte Größen
 * - Mobile-optimiert
 */
export const AdaptiveField: React.FC<AdaptiveFieldProps> = ({
  field,
  value,
  onChange,
  onBlur,
  error,
  disabled = false,
  readOnly = false,
}) => {
  const { theme } = useCustomerFieldTheme();
  const [dynamicWidth, setDynamicWidth] = useState<number>();
  const measureRef = useRef<HTMLSpanElement>(null);

  // Helper-Funktion für Size-Kategorie Mapping
  const getSizeCategoryFromGrid = (gridSize: number): string => {
    if (gridSize <= 2) return 'kompakt';
    if (gridSize <= 3) return 'klein';
    if (gridSize <= 6) return 'mittel';
    if (gridSize <= 10) return 'groß';
    return 'voll';
  };

  // Dynamische Breiten-Berechnung
  useEffect(() => {
    if (measureRef.current && theme.darstellung === 'anpassungsfähig') {
      const textWidth = measureRef.current.offsetWidth;

      // Nutze calculateFieldWidth für die intelligente Berechnung
      const calculatedWidth = calculateFieldWidth(field, textWidth);

      // Zusätzlich: Respektiere Theme-Grenzen basierend auf fieldSizeCalculator
      const sizeInfo = getFieldSize(field);
      const sizeCategory = getSizeCategoryFromGrid(sizeInfo.md || 6);
      const themeMinWidth = parseInt(theme.feldgrößen[sizeCategory].minBreite);
      const themeMaxWidth = parseInt(theme.feldgrößen[sizeCategory].maxBreite);

      // Kombiniere beide Systeme: calculateFieldWidth mit Theme-Grenzen
      const finalWidth = Math.min(Math.max(calculatedWidth, themeMinWidth), themeMaxWidth);
      setDynamicWidth(finalWidth);
    }
  }, [value, field, theme]);

  return (
    <>
      {/* Unsichtbarer Mess-Span */}
      <MeasureSpan ref={measureRef}>{value || field.placeholder || field.label}</MeasureSpan>

      {/* NUR TextField, OHNE Label (wird von FieldWrapper übernommen) */}
      <StyledTextField
        id={field.key}
        name={field.key}
        type={
          field.fieldType === 'email' ? 'email' : field.fieldType === 'number' ? 'number' : 'text'
        }
        value={value}
        onChange={e => onChange(e.target.value)}
        onBlur={onBlur}
        placeholder={field.placeholder}
        disabled={disabled}
        error={!!error}
        helperText={error}
        size="small"
        variant="outlined"
        inputProps={{
          readOnly,
          maxLength: field.maxLength,
          min: field.fieldType === 'number' ? field.min : undefined,
          max: field.fieldType === 'number' ? field.max : undefined,
          step: field.fieldType === 'number' ? field.step : undefined,
          'aria-label': field.label,
          'aria-required': field.required,
          'aria-invalid': !!error,
        }}
        style={{
          width: dynamicWidth ? `${dynamicWidth}px` : '100%',
        }}
        sx={{
          '& .MuiInputBase-root': {
            backgroundColor: readOnly ? 'action.disabledBackground' : 'background.paper',
          },
        }}
      />
    </>
  );
};
