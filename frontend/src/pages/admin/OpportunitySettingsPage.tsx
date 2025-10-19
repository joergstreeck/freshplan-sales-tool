/**
 * OpportunitySettingsPage - Business-Type-Matrix Konfiguration
 *
 * Sprint 2.1.7.3 - Admin-UI für Opportunity Multipliers
 *
 * Design System Compliance:
 * - MainLayoutV2 mit maxWidth="full" (Tabellen-Pattern)
 * - MUI Theme STRICT (KEINE hardcoded Farben!)
 * - theme.palette.primary.main / secondary.main
 * - Deutsche Labels
 */

import { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Alert,
  Chip,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { Search as SearchIcon, TrendingUp as TrendingUpIcon } from '@mui/icons-material';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';
import { useQuery } from '@tanstack/react-query';

interface OpportunityMultiplier {
  id: number;
  businessType: string;
  opportunityType: string;
  multiplier: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export const OpportunitySettingsPage = () => {
  const [businessTypeFilter, setBusinessTypeFilter] = useState<string>('ALL');
  const [opportunityTypeFilter, setOpportunityTypeFilter] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState<string>('');

  const {
    data: multipliers,
    isLoading,
    error,
  } = useQuery<OpportunityMultiplier[]>({
    queryKey: ['opportunity-multipliers'],
    queryFn: async () => {
      const response = await fetch('http://localhost:8080/api/settings/opportunity-multipliers');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      return response.json();
    },
  });

  const filteredMultipliers = multipliers?.filter(m => {
    const matchesBusinessType =
      businessTypeFilter === 'ALL' || m.businessType === businessTypeFilter;
    const matchesOpportunityType =
      opportunityTypeFilter === 'ALL' || m.opportunityType === opportunityTypeFilter;
    const matchesSearch =
      searchTerm === '' ||
      m.businessType.toLowerCase().includes(searchTerm.toLowerCase()) ||
      m.opportunityType.toLowerCase().includes(searchTerm.toLowerCase());

    return matchesBusinessType && matchesOpportunityType && matchesSearch;
  });

  const uniqueBusinessTypes = Array.from(
    new Set(multipliers?.map(m => m.businessType) || [])
  ).sort();
  const uniqueOpportunityTypes = Array.from(
    new Set(multipliers?.map(m => m.opportunityType) || [])
  ).sort();

  const businessTypeLabels: Record<string, string> = {
    GASTRONOMIE: 'Gastronomie',
    CATERING: 'Catering',
    EINZELHANDEL: 'Einzelhandel',
    GROSSHANDEL: 'Großhandel',
    BAECKEREI: 'Bäckerei',
    METZGEREI: 'Metzgerei',
    GEMEINSCHAFTSVERPFLEGUNG: 'Gemeinschaftsverpflegung',
    SONSTIGES: 'Sonstiges',
    UNKNOWN: 'Unbekannt',
  };

  const opportunityTypeLabels: Record<string, string> = {
    NEUKUNDE: 'Neukunde',
    SORTIMENTSERWEITERUNG: 'Sortimentserweiterung',
    STANDORTERWEITERUNG: 'Standorterweiterung',
    VERTRAGSVERLAENGERUNG: 'Vertragsverlängerung',
  };

  const getMultiplierColor = (multiplier: number): 'success' | 'warning' | 'error' => {
    if (multiplier >= 1.5) return 'success'; // High potential
    if (multiplier >= 1.0) return 'warning'; // Medium potential
    return 'error'; // Low potential
  };

  return (
    <MainLayoutV2 maxWidth="full">
      <Box sx={{ pb: 4 }}>
        {/* Header */}
        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <TrendingUpIcon color="secondary" sx={{ fontSize: 32 }} />
            <Typography variant="h4" color="secondary">
              Opportunity Multipliers
            </Typography>
          </Box>
          <Typography variant="body1" color="text.secondary">
            Business-Type-Matrix für intelligente Umsatzschätzung (9 BusinessTypes × 4
            OpportunityTypes = 36 Multipliers)
          </Typography>
        </Box>

        {/* Info Alert */}
        <Alert severity="info" sx={{ mb: 3 }}>
          <strong>Sprint 2.1.7.3 - Read-Only Ansicht</strong>
          <br />
          Diese Multipliers werden automatisch bei Customer → Opportunity Flow verwendet.
          Bearbeitung erfolgt in Modul 08 (Admin-UI für Settings).
        </Alert>

        {/* Filters */}
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" color="secondary" sx={{ mb: 2 }}>
            Filter
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <TextField
              placeholder="Suchen..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              sx={{ flex: '1 1 300px' }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
            <FormControl sx={{ flex: '1 1 200px' }}>
              <InputLabel>Business Type</InputLabel>
              <Select
                value={businessTypeFilter}
                onChange={e => setBusinessTypeFilter(e.target.value)}
                label="Business Type"
              >
                <MenuItem value="ALL">Alle</MenuItem>
                {uniqueBusinessTypes.map(bt => (
                  <MenuItem key={bt} value={bt}>
                    {businessTypeLabels[bt] || bt}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl sx={{ flex: '1 1 200px' }}>
              <InputLabel>Opportunity Type</InputLabel>
              <Select
                value={opportunityTypeFilter}
                onChange={e => setOpportunityTypeFilter(e.target.value)}
                label="Opportunity Type"
              >
                <MenuItem value="ALL">Alle</MenuItem>
                {uniqueOpportunityTypes.map(ot => (
                  <MenuItem key={ot} value={ot}>
                    {opportunityTypeLabels[ot] || ot}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </Paper>

        {/* Loading State */}
        {isLoading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress color="primary" />
          </Box>
        )}

        {/* Error State */}
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            Fehler beim Laden der Multipliers:{' '}
            {error instanceof Error ? error.message : 'Unbekannter Fehler'}
          </Alert>
        )}

        {/* Table */}
        {!isLoading && !error && filteredMultipliers && (
          <>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.50' }}>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Business Type
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Opportunity Type
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Multiplier
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Erstellt
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Aktualisiert
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredMultipliers.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                        <Typography variant="body2" color="text.secondary">
                          Keine Multipliers gefunden
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredMultipliers.map(m => (
                      <TableRow key={m.id} hover>
                        <TableCell>
                          <Chip
                            label={businessTypeLabels[m.businessType] || m.businessType}
                            size="small"
                            variant="outlined"
                            color="secondary"
                          />
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={opportunityTypeLabels[m.opportunityType] || m.opportunityType}
                            size="small"
                            variant="outlined"
                            color="primary"
                          />
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={`${m.multiplier.toFixed(2)}x`}
                            size="small"
                            color={getMultiplierColor(m.multiplier)}
                          />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {new Date(m.createdAt).toLocaleDateString('de-DE')}
                            <br />
                            <Typography component="span" variant="caption" color="text.secondary">
                              von {m.createdBy}
                            </Typography>
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">
                            {new Date(m.updatedAt).toLocaleDateString('de-DE')}
                            <br />
                            <Typography component="span" variant="caption" color="text.secondary">
                              von {m.updatedBy}
                            </Typography>
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>

            {/* Stats Footer */}
            <Box sx={{ mt: 2, display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
              <Typography variant="body2" color="text.secondary">
                {filteredMultipliers.length} von {multipliers?.length || 0} Multipliers angezeigt
              </Typography>
            </Box>
          </>
        )}
      </Box>
    </MainLayoutV2>
  );
};
