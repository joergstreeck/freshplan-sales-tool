/**
 * AdvancedSearchBar Component
 *
 * Sprint 2.1.8 Phase 4: Advanced Search mit pg_trgm
 *
 * Features:
 * - Fuzzy-Suche mit Debouncing
 * - Autocomplete mit Dropdown
 * - Ähnlichkeitsanzeige
 * - Integration mit LeadList
 *
 * @module AdvancedSearchBar
 * @since Sprint 2.1.8
 */

import { useState, useCallback, useEffect } from 'react';
import {
  TextField,
  InputAdornment,
  IconButton,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Chip,
  Box,
  Typography,
  CircularProgress,
  Popper,
  ClickAwayListener,
  Fade,
} from '@mui/material';
import {
  Search as SearchIcon,
  Clear as ClearIcon,
  TipsAndUpdates as FuzzyIcon,
} from '@mui/icons-material';
import { useTheme } from '@mui/material/styles';
import { fuzzySearchLeads } from '../../api';
import type { Lead } from '../../types';

// Debounce Hook
function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

interface AdvancedSearchBarProps {
  onSelect?: (lead: Lead) => void;
  onSearch?: (query: string) => void;
  placeholder?: string;
  includeInactive?: boolean;
  autoFocus?: boolean;
}

export function AdvancedSearchBar({
  onSelect,
  onSearch,
  placeholder = 'Suche (auch mit Tippfehlern)...',
  includeInactive = false,
  autoFocus = false,
}: AdvancedSearchBarProps) {
  const theme = useTheme();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Lead[]>([]);
  const [loading, setLoading] = useState(false);
  const [fuzzyEnabled, setFuzzyEnabled] = useState(true);
  const [open, setOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);

  const debouncedQuery = useDebounce(query, 300);

  // Suche ausführen wenn debouncedQuery sich ändert
  useEffect(() => {
    if (debouncedQuery.length < 2) {
      setResults([]);
      setOpen(false);
      return;
    }

    const search = async () => {
      setLoading(true);
      try {
        const response = await fuzzySearchLeads(debouncedQuery, 10, includeInactive);
        setResults(response.data);
        setFuzzyEnabled(response.fuzzyEnabled);
        setOpen(response.data.length > 0);
      } catch (error) {
        console.error('Search failed:', error);
        setResults([]);
      } finally {
        setLoading(false);
      }
    };

    search();
  }, [debouncedQuery, includeInactive]);

  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = e.target.value;
      setQuery(value);
      onSearch?.(value);
    },
    [onSearch]
  );

  const handleClear = useCallback(() => {
    setQuery('');
    setResults([]);
    setOpen(false);
    onSearch?.('');
  }, [onSearch]);

  const handleSelect = useCallback(
    (lead: Lead) => {
      setQuery(lead.companyName || '');
      setOpen(false);
      onSelect?.(lead);
    },
    [onSelect]
  );

  const handleClickAway = useCallback(() => {
    setOpen(false);
  }, []);

  const handleFocus = useCallback(
    (e: React.FocusEvent<HTMLInputElement>) => {
      setAnchorEl(e.currentTarget);
      if (results.length > 0) {
        setOpen(true);
      }
    },
    [results.length]
  );

  return (
    <ClickAwayListener onClickAway={handleClickAway}>
      <Box sx={{ position: 'relative', width: '100%' }}>
        <TextField
          fullWidth
          autoFocus={autoFocus}
          placeholder={placeholder}
          value={query}
          onChange={handleInputChange}
          onFocus={handleFocus}
          autoComplete="off"
          inputProps={{
            autoComplete: 'off',
            'data-form-type': 'other',
            'data-lpignore': 'true',
          }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                {loading ? <CircularProgress size={20} /> : <SearchIcon color="action" />}
              </InputAdornment>
            ),
            endAdornment: (
              <InputAdornment position="end">
                {fuzzyEnabled && query.length > 0 && (
                  <FuzzyIcon
                    sx={{ color: theme.palette.success.main, mr: 1 }}
                    titleAccess="Fuzzy-Suche aktiv"
                  />
                )}
                {query && (
                  <IconButton size="small" onClick={handleClear}>
                    <ClearIcon fontSize="small" />
                  </IconButton>
                )}
              </InputAdornment>
            ),
          }}
          sx={{
            '& .MuiOutlinedInput-root': {
              backgroundColor: theme.palette.background.paper,
            },
          }}
        />

        <Popper
          open={open}
          anchorEl={anchorEl}
          placement="bottom-start"
          transition
          style={{ width: anchorEl?.clientWidth, zIndex: theme.zIndex.modal }}
        >
          {({ TransitionProps }) => (
            <Fade {...TransitionProps} timeout={200}>
              <Paper
                elevation={8}
                sx={{
                  mt: 0.5,
                  maxHeight: 400,
                  overflow: 'auto',
                  border: `1px solid ${theme.palette.divider}`,
                }}
              >
                <List dense>
                  {results.map(lead => (
                    <ListItem
                      key={lead.id}
                      onClick={() => handleSelect(lead)}
                      sx={{
                        cursor: 'pointer',
                        '&:hover': {
                          backgroundColor: theme.palette.action.hover,
                        },
                      }}
                    >
                      <ListItemText
                        primary={
                          <Typography variant="body1" fontWeight="medium">
                            {lead.companyName}
                          </Typography>
                        }
                        secondary={
                          <Box component="span" sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
                            {lead.city && (
                              <Typography variant="caption" color="text.secondary">
                                {lead.city}
                              </Typography>
                            )}
                            {lead.email && (
                              <Typography variant="caption" color="text.secondary">
                                {lead.email}
                              </Typography>
                            )}
                          </Box>
                        }
                      />
                      <ListItemSecondaryAction>
                        <Chip
                          label={lead.status || 'NEU'}
                          size="small"
                          color={getStatusColor(lead.status)}
                          variant="outlined"
                        />
                      </ListItemSecondaryAction>
                    </ListItem>
                  ))}
                </List>

                {results.length === 0 && query.length >= 2 && !loading && (
                  <Box p={2} textAlign="center">
                    <Typography variant="body2" color="text.secondary">
                      Keine Ergebnisse gefunden
                    </Typography>
                  </Box>
                )}

                {fuzzyEnabled && results.length > 0 && (
                  <Box
                    p={1}
                    sx={{
                      borderTop: `1px solid ${theme.palette.divider}`,
                      backgroundColor: theme.palette.action.hover,
                    }}
                  >
                    <Typography variant="caption" color="text.secondary">
                      <FuzzyIcon sx={{ fontSize: 14, mr: 0.5, verticalAlign: 'middle' }} />
                      Fuzzy-Suche aktiv - findet auch bei Tippfehlern
                    </Typography>
                  </Box>
                )}
              </Paper>
            </Fade>
          )}
        </Popper>
      </Box>
    </ClickAwayListener>
  );
}

/**
 * Hilfsfunktion für Status-Farben
 */
function getStatusColor(
  status: string | undefined
): 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' {
  switch (status) {
    case 'QUALIFIED':
      return 'success';
    case 'ACTIVE':
      return 'primary';
    case 'REGISTERED':
      return 'info';
    case 'GRACE_PERIOD':
      return 'warning';
    case 'EXPIRED':
    case 'LOST':
      return 'error';
    default:
      return 'default';
  }
}

export default AdvancedSearchBar;
