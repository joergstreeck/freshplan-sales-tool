import React, { useState, useEffect, useCallback } from 'react';
import { TextField, Box, InputAdornment } from '@mui/material';
import type { TextFieldProps as _TextFieldProps } from '@mui/material';
import {
  formatEUR,
  parseEUR,
  formatEURWhileTyping,
  isValidCurrencyInput,
} from '../../utils/currencyFormatter';
import { SegmentedRevenueCalculator } from '../calculator/SegmentedRevenueCalculator';

interface EURInputProps {
  value: number | null;
  onChange: (value: number) => void;
  onBlur?: () => void;
  label?: string;
  required?: boolean;
  error?: boolean;
  helperText?: string;
  showCalculator?: boolean;
  disabled?: boolean;
  fullWidth?: boolean;
  size?: 'small' | 'medium';
  calculatorHint?: string;
  validationWarning?: number;
}

export const EURInput: React.FC<EURInputProps> = ({
  value,
  onChange,
  onBlur,
  label = 'Betrag',
  required = false,
  error = false,
  helperText,
  showCalculator = false,
  disabled = false,
  fullWidth = true,
  size = 'medium',
  calculatorHint: _calculatorHint,
  validationWarning = 1000000,
}) => {
  const [displayValue, setDisplayValue] = useState('');
  const [isFocused, setIsFocused] = useState(false);
  const [showWarning, setShowWarning] = useState(false);

  // Initialisiere Anzeige-Wert
  useEffect(() => {
    if (!isFocused) {
      setDisplayValue(value ? formatEUR(value) : '');
    }
  }, [value, isFocused]);

  // Handle Input Change
  const handleChange = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      const inputValue = event.target.value;

      // Validiere Eingabe
      if (!isValidCurrencyInput(inputValue)) {
        return;
      }

      // Formatiere während der Eingabe
      const formatted = formatEURWhileTyping(inputValue);
      setDisplayValue(formatted);

      // Parse zu Zahl und update
      const numValue = parseEUR(formatted);
      console.log('EURInput handleChange:', { inputValue, formatted, numValue });
      onChange(numValue);

      // Check für Warnung
      if (validationWarning && numValue > validationWarning) {
        setShowWarning(true);
      } else {
        setShowWarning(false);
      }
    },
    [onChange, validationWarning]
  );

  // Handle Focus
  const handleFocus = useCallback(() => {
    setIsFocused(true);
    // Zeige nur Zahlen beim Fokus
    if (value) {
      setDisplayValue(value.toString());
    }
  }, [value]);

  // Handle Blur
  const handleBlur = useCallback(() => {
    setIsFocused(false);
    // Formatiere mit EUR-Symbol beim Verlassen
    setDisplayValue(value ? formatEUR(value) : '');
    onBlur?.();
  }, [value, onBlur]);

  // Handle Apply from Calculator
  const handleApplyCalculation = useCallback(
    (calculatedValue: number) => {
      console.log('EURInput handleApplyCalculation:', calculatedValue);
      // Update value immediately
      onChange(calculatedValue);
      // Update display immediately
      setDisplayValue(formatEUR(calculatedValue));
      // Check für Warnung
      if (validationWarning && calculatedValue > validationWarning) {
        setShowWarning(true);
      } else {
        setShowWarning(false);
      }
      // Hide calculator by removing focus
      setIsFocused(false);
    },
    [onChange, validationWarning]
  );

  return (
    <Box>
      <TextField
        label={label}
        value={displayValue}
        onChange={handleChange}
        onFocus={handleFocus}
        onBlur={handleBlur}
        required={required}
        error={error || showWarning}
        helperText={showWarning ? 'Hoher Betrag - bitte nochmal prüfen' : helperText}
        disabled={disabled}
        fullWidth={fullWidth}
        size={size}
        InputProps={{
          endAdornment:
            !isFocused && displayValue ? <InputAdornment position="end">€</InputAdornment> : null,
          sx: {
            '& input': {
              fontSize: '18px',
              textAlign: 'right',
              fontWeight: 500,
              minWidth: fullWidth ? 'auto' : '320px',
            },
          },
        }}
        sx={{
          '& .MuiFormHelperText-root': {
            color: showWarning ? 'warning.main' : undefined,
          },
        }}
      />

      {/* Revenue Calculator */}
      {showCalculator && isFocused && (
        <Box
          onMouseDown={e => {
            // Verhindere, dass der Focus verloren geht
            e.preventDefault();
          }}
        >
          <SegmentedRevenueCalculator
            currentValue={value || 0}
            onApplyCalculation={handleApplyCalculation}
          />
        </Box>
      )}
    </Box>
  );
};
