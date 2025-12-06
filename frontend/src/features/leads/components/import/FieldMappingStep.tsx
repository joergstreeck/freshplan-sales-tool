/**
 * Field Mapping Step - Sprint 2.1.8 Phase 2
 *
 * Schritt 2 des Import-Wizards: Spalten-Zuordnung.
 *
 * Features:
 * - Auto-Mapping Vorschläge
 * - Manuelle Zuordnung per Dropdown
 * - Pflichtfeld-Validierung (companyName)
 * - Visuelle Unterscheidung (gemappt/nicht gemappt)
 *
 * @module FieldMappingStep
 * @since Sprint 2.1.8
 */

import { useState, useMemo, useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Select,
  MenuItem,
  FormControl,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  ArrowForward as ArrowIcon,
  CheckCircle as MappedIcon,
  RadioButtonUnchecked as UnmappedIcon,
  Refresh as ResetIcon,
} from '@mui/icons-material';

// API
import { LEAD_FIELDS } from '../../api/leadImportApi';

// ============================================================================
// Types
// ============================================================================

export interface FieldMappingStepProps {
  uploadId: string;
  columns: string[];
  initialMapping: Record<string, string>;
  onMappingComplete: (mapping: Record<string, string>) => void;
  onBack: () => void;
}

// ============================================================================
// Component
// ============================================================================

export function FieldMappingStep({
  columns,
  initialMapping,
  onMappingComplete,
  onBack,
}: FieldMappingStepProps) {
  // State: Mapping von Quell-Spalte -> Lead-Feld
  const [mapping, setMapping] = useState<Record<string, string>>(initialMapping);

  // Prüfe ob Pflichtfelder gemappt sind
  const requiredFields = useMemo(() => LEAD_FIELDS.filter(f => f.required).map(f => f.key), []);

  const mappedFields = useMemo(() => Object.values(mapping).filter(Boolean), [mapping]);

  const missingRequired = useMemo(
    () => requiredFields.filter(field => !mappedFields.includes(field)),
    [requiredFields, mappedFields]
  );

  const isValid = missingRequired.length === 0;

  // Handle Mapping Change
  const handleMappingChange = useCallback((column: string, field: string) => {
    setMapping(prev => {
      const newMapping = { ...prev };

      // Wenn ein anderes Feld bereits diesen Wert hat, entfernen
      Object.keys(newMapping).forEach(key => {
        if (newMapping[key] === field && key !== column) {
          delete newMapping[key];
        }
      });

      // Neues Mapping setzen oder löschen
      if (field) {
        newMapping[column] = field;
      } else {
        delete newMapping[column];
      }

      return newMapping;
    });
  }, []);

  // Reset Mapping
  const handleReset = useCallback(() => {
    setMapping(initialMapping);
  }, [initialMapping]);

  // Continue
  const handleContinue = useCallback(() => {
    if (isValid) {
      onMappingComplete(mapping);
    }
  }, [isValid, mapping, onMappingComplete]);

  // Get available fields (not yet mapped)
  const getAvailableFields = useCallback(
    (currentColumn: string) => {
      const currentValue = mapping[currentColumn];
      return LEAD_FIELDS.filter(
        field => !mappedFields.includes(field.key) || field.key === currentValue
      );
    },
    [mapping, mappedFields]
  );

  // Statistics
  const stats = useMemo(() => {
    const total = columns.length;
    const mapped = Object.keys(mapping).filter(k => mapping[k]).length;
    return { total, mapped, unmapped: total - mapped };
  }, [columns, mapping]);

  return (
    <Box>
      {/* Header mit Statistik */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h6">Spalten zuordnen</Typography>
          <Typography variant="body2" color="text.secondary">
            Ordnen Sie die Spalten Ihrer Datei den Lead-Feldern zu
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Chip
            label={`${stats.mapped} zugeordnet`}
            color="success"
            size="small"
            variant="outlined"
          />
          <Chip
            label={`${stats.unmapped} offen`}
            color={stats.unmapped > 0 ? 'default' : 'success'}
            size="small"
            variant="outlined"
          />
        </Box>
      </Box>

      {/* Validation Alert */}
      {missingRequired.length > 0 && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          <strong>Pflichtfelder fehlen:</strong>{' '}
          {missingRequired.map(f => LEAD_FIELDS.find(lf => lf.key === f)?.label).join(', ')}
        </Alert>
      )}

      {/* Auto-Mapping Info */}
      {Object.keys(initialMapping).length > 0 && (
        <Alert severity="info" sx={{ mb: 2 }}>
          {Object.keys(initialMapping).length} Spalten wurden automatisch erkannt.
          <Tooltip title="Zurücksetzen auf Auto-Mapping">
            <IconButton size="small" onClick={handleReset} sx={{ ml: 1 }}>
              <ResetIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Alert>
      )}

      {/* Mapping Table */}
      <TableContainer component={Paper} variant="outlined" sx={{ mb: 3 }}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ bgcolor: 'grey.50' }}>
              <TableCell sx={{ fontWeight: 600, width: '40%' }}>Ihre Spalte</TableCell>
              <TableCell sx={{ width: '10%', textAlign: 'center' }}></TableCell>
              <TableCell sx={{ fontWeight: 600, width: '50%' }}>Lead-Feld</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {columns.map(column => {
              const currentMapping = mapping[column] || '';
              const isMapped = Boolean(currentMapping);
              const availableFields = getAvailableFields(column);

              return (
                <TableRow
                  key={column}
                  sx={{
                    bgcolor: isMapped ? 'success.lighter' : 'background.paper',
                    '&:hover': { bgcolor: 'action.hover' },
                  }}
                >
                  {/* Source Column */}
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {isMapped ? (
                        <MappedIcon fontSize="small" color="success" />
                      ) : (
                        <UnmappedIcon fontSize="small" color="disabled" />
                      )}
                      <Typography
                        variant="body2"
                        sx={{
                          fontWeight: 500,
                          letterSpacing: '0.02em',
                        }}
                      >
                        {column}
                      </Typography>
                    </Box>
                  </TableCell>

                  {/* Arrow */}
                  <TableCell sx={{ textAlign: 'center' }}>
                    <ArrowIcon fontSize="small" color={isMapped ? 'success' : 'disabled'} />
                  </TableCell>

                  {/* Target Field */}
                  <TableCell>
                    <FormControl fullWidth size="small">
                      <Select
                        value={currentMapping}
                        onChange={e => handleMappingChange(column, e.target.value)}
                        displayEmpty
                        sx={{
                          bgcolor: 'background.paper',
                          '& .MuiSelect-select': {
                            py: 1,
                          },
                        }}
                      >
                        <MenuItem value="">
                          <Typography color="text.secondary" variant="body2">
                            — Nicht zuordnen —
                          </Typography>
                        </MenuItem>
                        {availableFields.map(field => (
                          <MenuItem key={field.key} value={field.key}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              {field.label}
                              {field.required && (
                                <Chip
                                  label="Pflicht"
                                  size="small"
                                  color="error"
                                  variant="outlined"
                                  sx={{ height: 20, fontSize: '0.7rem' }}
                                />
                              )}
                            </Box>
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Button variant="outlined" onClick={onBack}>
          Zurück
        </Button>
        <Button variant="contained" onClick={handleContinue} disabled={!isValid}>
          Weiter zur Vorschau
        </Button>
      </Box>
    </Box>
  );
}

export default FieldMappingStep;
