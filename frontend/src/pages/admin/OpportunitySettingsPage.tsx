/**
 * OpportunitySettingsPage - Verkaufschancen-Einstellungen
 *
 * Sprint 2.1.7.3 - Admin-UI für Verkaufschancen-Multiplikatoren
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  IconButton,
} from '@mui/material';
import {
  Search as SearchIcon,
  TrendingUp as TrendingUpIcon,
  Close as CloseIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

interface OpportunityMultiplier {
  id: string; // UUID
  businessType: string;
  opportunityType: string;
  multiplier: number;
  createdAt: string;
  updatedAt: string;
}

export const OpportunitySettingsPage = () => {
  const queryClient = useQueryClient();
  const [businessTypeFilter, setBusinessTypeFilter] = useState<string>('ALL');
  const [opportunityTypeFilter, setOpportunityTypeFilter] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [editingMultiplier, setEditingMultiplier] = useState<OpportunityMultiplier | null>(null);
  const [editValue, setEditValue] = useState<number>(0);

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

  // Mutation für Multiplier Updates
  const updateMutation = useMutation({
    mutationFn: async ({ id, multiplier }: { id: string; multiplier: number }) => {
      const response = await fetch(
        `http://localhost:8080/api/settings/opportunity-multipliers/${id}`,
        {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({ multiplier }),
        }
      );
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP ${response.status}`);
      }
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['opportunity-multipliers'] });
      setEditingMultiplier(null);
    },
  });

  const handleEditClick = (multiplier: OpportunityMultiplier) => {
    setEditingMultiplier(multiplier);
    setEditValue(multiplier.multiplier);
  };

  const handleCloseDialog = () => {
    setEditingMultiplier(null);
    setEditValue(0);
  };

  const handleSave = () => {
    if (editingMultiplier) {
      updateMutation.mutate({ id: editingMultiplier.id, multiplier: editValue });
    }
  };

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
              Verkaufschancen-Einstellungen
            </Typography>
          </Box>
          <Typography variant="body1" color="text.secondary">
            Multiplikatoren für intelligente Umsatzschätzung (9 Geschäftstypen × 4
            Verkaufschancen-Typen = 36 Kombinationen)
          </Typography>
        </Box>

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
              <InputLabel>Geschäftstyp</InputLabel>
              <Select
                value={businessTypeFilter}
                onChange={e => setBusinessTypeFilter(e.target.value)}
                label="Geschäftstyp"
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
              <InputLabel>Verkaufschancen-Typ</InputLabel>
              <Select
                value={opportunityTypeFilter}
                onChange={e => setOpportunityTypeFilter(e.target.value)}
                label="Verkaufschancen-Typ"
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
            Fehler beim Laden der Multiplikatoren:{' '}
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
                      Geschäftstyp
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Verkaufschancen-Typ
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Multiplikator
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Aktualisiert
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600, color: 'secondary.main' }}>
                      Aktionen
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredMultipliers.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                        <Typography variant="body2" color="text.secondary">
                          Keine Multiplikatoren gefunden
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    filteredMultipliers.map(m => (
                      <TableRow
                        key={m.id}
                        hover
                        sx={{
                          cursor: 'pointer',
                          '&:hover': {
                            bgcolor: 'action.hover',
                          },
                        }}
                        onClick={() => handleEditClick(m)}
                      >
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
                            {new Date(m.updatedAt).toLocaleDateString('de-DE')}
                            <br />
                            <Typography component="span" variant="caption" color="text.secondary">
                              {new Date(m.updatedAt).toLocaleTimeString('de-DE', {
                                hour: '2-digit',
                                minute: '2-digit',
                              })}
                            </Typography>
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <IconButton size="small" color="primary">
                            <EditIcon fontSize="small" />
                          </IconButton>
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
                {filteredMultipliers.length} von {multipliers?.length || 0} Multiplikatoren angezeigt
              </Typography>
            </Box>
          </>
        )}

        {/* Edit Dialog */}
        <Dialog
          open={editingMultiplier !== null}
          onClose={handleCloseDialog}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <EditIcon color="primary" />
                <Typography variant="h6" color="primary">
                  Multiplikator bearbeiten
                </Typography>
              </Box>
              <IconButton size="small" onClick={handleCloseDialog}>
                <CloseIcon />
              </IconButton>
            </Box>
          </DialogTitle>

          <DialogContent>
            {editingMultiplier && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, pt: 2 }}>
                {/* Read-Only: Geschäftstyp */}
                <Box>
                  <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5 }}>
                    Geschäftstyp
                  </Typography>
                  <Chip
                    label={
                      businessTypeLabels[editingMultiplier.businessType] ||
                      editingMultiplier.businessType
                    }
                    color="secondary"
                    variant="outlined"
                    sx={{ mt: 0.5 }}
                  />
                </Box>

                {/* Read-Only: Verkaufschancen-Typ */}
                <Box>
                  <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5 }}>
                    Verkaufschancen-Typ
                  </Typography>
                  <Chip
                    label={
                      opportunityTypeLabels[editingMultiplier.opportunityType] ||
                      editingMultiplier.opportunityType
                    }
                    color="primary"
                    variant="outlined"
                    sx={{ mt: 0.5 }}
                  />
                </Box>

                {/* Berechnungslogik Erklärung */}
                <Alert severity="info">
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Berechnungslogik:</strong>
                  </Typography>
                  <Typography variant="body2" component="div" sx={{ fontFamily: 'monospace' }}>
                    Erwarteter Umsatz = Basisumsatz × Multiplikator
                  </Typography>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ mt: 1, display: 'block' }}
                  >
                    Basisumsatz kommt aus: Xentral (tatsächliches Jahresvolumen) → Lead-Schätzung
                    (erwartetes Jahresvolumen) → 0 (manuelle Eingabe)
                  </Typography>
                </Alert>

                {/* Editable: Multiplikator */}
                <TextField
                  label="Multiplikator"
                  type="number"
                  value={editValue}
                  onChange={e => setEditValue(parseFloat(e.target.value))}
                  inputProps={{
                    min: 0,
                    max: 2,
                    step: 0.01,
                  }}
                  helperText="Gültig: 0.0 bis 2.0 (z.B. 0.25 = 25% des Basisumsatzes, 1.0 = 100% des Basisumsatzes)"
                  fullWidth
                  required
                  error={editValue < 0 || editValue > 2}
                />

                {/* Mutation Error */}
                {updateMutation.isError && (
                  <Alert severity="error">
                    Fehler beim Speichern:{' '}
                    {updateMutation.error instanceof Error
                      ? updateMutation.error.message
                      : 'Unbekannter Fehler'}
                  </Alert>
                )}
              </Box>
            )}
          </DialogContent>

          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={handleCloseDialog} disabled={updateMutation.isPending}>
              Abbrechen
            </Button>
            <Button
              onClick={handleSave}
              variant="contained"
              color="primary"
              disabled={
                updateMutation.isPending || editValue < 0 || editValue > 2 || !editingMultiplier
              }
            >
              {updateMutation.isPending ? 'Speichern...' : 'Speichern'}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </MainLayoutV2>
  );
};
