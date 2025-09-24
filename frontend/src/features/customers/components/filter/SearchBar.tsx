/**
 * Search Bar Component
 *
 * Universal search input with debouncing and results dropdown
 *
 * @module SearchBar
 * @since FC-005 PR4
 */

import React, { forwardRef } from 'react';
import { TextField, InputAdornment, IconButton } from '@mui/material';
import { Search as SearchIcon, Clear as ClearIcon } from '@mui/icons-material';
import { useTheme } from '@mui/material/styles';

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onClear: () => void;
  onFocus?: () => void;
  placeholder?: string;
  loading?: boolean;
}

export const SearchBar = forwardRef<HTMLInputElement, SearchBarProps>(
  (
    {
      value,
      onChange,
      onClear,
      onFocus,
      placeholder = 'Suche nach Firma, Kundennummer, Kontakten...',
      loading = false,
    },
    ref
  ) => {
    const theme = useTheme();

    return (
      <TextField
        ref={ref}
        fullWidth
        placeholder={placeholder}
        value={value}
        onChange={(e: React.ChangeEvent<HTMLInputElement>) => onChange(e.target.value)}
        onFocus={onFocus}
        autoComplete="off"
        disabled={loading}
        inputProps={{
          autoComplete: 'off',
          'data-form-type': 'other',
          'data-lpignore': 'true',
          autoCorrect: 'off',
          autoCapitalize: 'off',
          spellCheck: 'false',
        }}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon />
            </InputAdornment>
          ),
          endAdornment: value && (
            <InputAdornment position="end">
              <IconButton size="small" onClick={onClear}>
                <ClearIcon />
              </IconButton>
            </InputAdornment>
          ),
        }}
        sx={{
          '& .MuiOutlinedInput-root': {
            backgroundColor: theme.palette.background.paper,
          },
        }}
      />
    );
  }
);

SearchBar.displayName = 'SearchBar';
