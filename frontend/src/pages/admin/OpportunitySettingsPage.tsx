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
import { Search as SearchIcon } from '@mui/icons-material';
import { AdminLayout } from '@/components/layout/AdminLayout';
import { useQuery } from '@tanstack/react-query';

/**
 * OpportunitySettingsPage - Business-Type-Matrix Konfiguration
 *
 * Sprint 2.1.7.3 - Admin-UI für Opportunity Multipliers
 *
 * Features:
 * - Anzeige aller 36 Multipliers (9 BusinessTypes × 4 OpportunityTypes)
 * - Filter nach BusinessType und OpportunityType
 * - Read-Only (Bearbeitung in Modul 08)
 */

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

  // Fetch multipliers from backend
  const { data: multipliers, isLoading, error } = useQuery<OpportunityMultiplier[]>({
    queryKey: ['opportunity-multipliers'],
    queryFn: async () => {
      const response = await fetch('http://localhost:8080/api/settings/opportunity-multipliers');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      return response.json();
    },
  });

  // Filter multipliers
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

  // Extract unique values for filters
  const uniqueBusinessTypes = Array.from(
    new Set(multipliers?.map(m => m.businessType) || [])
  ).sort();
  const uniqueOpportunityTypes = Array.from(
    new Set(multipliers?.map(m => m.opportunityType) || [])
  ).sort();

  // German labels
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

  const getMultiplierColor = (multiplier: number): string => {
    if (multiplier >= 1.5) return '#94C456'; // High potential - green
    if (multiplier >= 1.0) return '#FFB74D'; // Medium potential - orange
    return '#EF5350'; // Low potential - red
  };

  return (
    <AdminLayout>
      <Box sx={{ maxWidth: 1400, mx: 'auto' }}>
        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h4"
            sx={{
              fontFamily: 'Antonio, sans-serif',
              fontWeight: 'bold',
              color: '#004F7B',
              mb: 1,
            }}
          >
            Opportunity Multipliers
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ fontFamily: 'Poppins, sans-serif' }}>
            Business-Type-Matrix für intelligente Umsatzschätzung (9 BusinessTypes × 4 OpportunityTypes = 36 Multipliers)
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
          <Typography
            variant="h6"
            sx={{ mb: 2, fontFamily: 'Poppins, sans-serif', fontWeight: 600 }}
          >
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

        {/* Table */}
        {isLoading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            Fehler beim Laden der Multipliers: {error instanceof Error ? error.message : 'Unbekannter Fehler'}
          </Alert>
        )}

        {!isLoading && !error && filteredMultipliers && (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow sx={{ bgcolor: '#f8f9fa' }}>
                  <TableCell sx={{ fontWeight: 600, fontFamily: 'Poppins, sans-serif' }}>
                    Business Type
                  </TableCell>
                  <TableCell sx={{ fontWeight: 600, fontFamily: 'Poppins, sans-serif' }}>
                    Opportunity Type
                  </TableCell>
                  <TableCell sx={{ fontWeight: 600, fontFamily: 'Poppins, sans-serif' }}>
                    Multiplier
                  </TableCell>
                  <TableCell sx={{ fontWeight: 600, fontFamily: 'Poppins, sans-serif' }}>
                    Erstellt
                  </TableCell>
                  <TableCell sx={{ fontWeight: 600, fontFamily: 'Poppins, sans-serif' }}>
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
                          sx={{ bgcolor: '#E3F2FD', color: '#004F7B', fontWeight: 500 }}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={opportunityTypeLabels[m.opportunityType] || m.opportunityType}
                          size="small"
                          sx={{ bgcolor: '#F3E5F5', color: '#6A1B9A', fontWeight: 500 }}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={`${m.multiplier.toFixed(2)}x`}
                          size="small"
                          sx={{
                            bgcolor: getMultiplierColor(m.multiplier),
                            color: '#fff',
                            fontWeight: 600,
                          }}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontFamily: 'Poppins, sans-serif' }}>
                          {new Date(m.createdAt).toLocaleDateString('de-DE')}
                          <br />
                          <Typography
                            component="span"
                            variant="caption"
                            color="text.secondary"
                          >
                            von {m.createdBy}
                          </Typography>
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontFamily: 'Poppins, sans-serif' }}>
                          {new Date(m.updatedAt).toLocaleDateString('de-DE')}
                          <br />
                          <Typography
                            component="span"
                            variant="caption"
                            color="text.secondary"
                          >
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
        )}

        {/* Stats */}
        {!isLoading && !error && filteredMultipliers && (
          <Box sx={{ mt: 2, display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'Poppins, sans-serif' }}>
              {filteredMultipliers.length} von {multipliers?.length || 0} Multipliers angezeigt
            </Typography>
          </Box>
        )}
      </Box>
    </AdminLayout>
  );
};
