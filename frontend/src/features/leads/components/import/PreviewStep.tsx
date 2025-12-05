/**
 * Preview Step - Sprint 2.1.8 Phase 2
 *
 * Schritt 3 des Import-Wizards: Vorschau & Validierung.
 *
 * REFACTORED für bessere Testbarkeit:
 * - API-Call wurde in den Parent (LeadImportWizard) verschoben
 * - Diese Komponente ist jetzt "pure" - nur Props rein, UI raus
 * - Siehe testing_guide.md: "Container/Presentational Pattern"
 *
 * Features:
 * - Validierungs-Zusammenfassung (Gültig/Fehler/Duplikate)
 * - Quota-Check Anzeige
 * - Fehler-Liste mit Details
 * - Duplikat-Anzeige
 * - Daten-Vorschau (erste 5 Zeilen)
 *
 * @module PreviewStep
 * @since Sprint 2.1.8
 */

import { useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  Alert,
  Grid,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Chip,
  CircularProgress,
  Skeleton,
} from '@mui/material';
import {
  ExpandMore as ExpandIcon,
  CheckCircle as ValidIcon,
  Error as ErrorIcon,
  Warning as DuplicateIcon,
  Info as InfoIcon,
} from '@mui/icons-material';

// API Types
import type { ImportPreviewResponse } from '../../api/leadImportApi';

// ============================================================================
// Types
// ============================================================================

export interface PreviewStepProps {
  /** Preview-Daten vom Parent (nach API-Call) */
  previewData: ImportPreviewResponse | null;
  /** Loading-State (Parent macht API-Call) */
  isLoading: boolean;
  /** Mapping für Spalten-Anzeige */
  mapping: Record<string, string>;
  /** Callback wenn User auf "Weiter" klickt */
  onContinue: () => void;
  /** Callback für "Zurück" */
  onBack: () => void;
}

// ============================================================================
// Sub-Components
// ============================================================================

interface StatCardProps {
  label: string;
  value: number;
  color: 'success' | 'error' | 'warning' | 'info';
  icon: React.ReactNode;
}

function StatCard({ label, value, color, icon }: StatCardProps) {
  const colorMap = {
    success: 'success.main',
    error: 'error.main',
    warning: 'warning.main',
    info: 'info.main',
  };

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2,
        textAlign: 'center',
        borderColor: colorMap[color],
        borderWidth: 2,
      }}
    >
      <Box sx={{ color: colorMap[color], mb: 1 }}>{icon}</Box>
      <Typography variant="h4" sx={{ fontWeight: 'bold', color: colorMap[color] }}>
        {value}
      </Typography>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
    </Paper>
  );
}

// ============================================================================
// Loading Component (exported for testing)
// ============================================================================

export function PreviewStepLoading() {
  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <CircularProgress size={24} />
        <Typography>Daten werden validiert...</Typography>
      </Box>
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {[1, 2, 3, 4].map(i => (
          <Grid key={i} size={{ xs: 6, sm: 3 }}>
            <Skeleton variant="rectangular" height={100} sx={{ borderRadius: 1 }} />
          </Grid>
        ))}
      </Grid>
      <Skeleton variant="rectangular" height={200} sx={{ borderRadius: 1 }} />
    </Box>
  );
}

// ============================================================================
// Main Component
// ============================================================================

export function PreviewStep({
  previewData,
  isLoading,
  mapping,
  onContinue,
  onBack,
}: PreviewStepProps) {
  // Continue Handler
  const handleContinue = useCallback(() => {
    onContinue();
  }, [onContinue]);

  // Loading State
  if (isLoading) {
    return <PreviewStepLoading />;
  }

  // No Data
  if (!previewData) {
    return (
      <Alert severity="error">Keine Vorschau-Daten verfügbar. Bitte versuchen Sie es erneut.</Alert>
    );
  }

  const { validation, errors, duplicates, quotaCheck, previewRows } = previewData;

  // Check if import is possible
  const canImport = quotaCheck.approved && validation.validRows > 0;

  return (
    <Box>
      {/* Quota Check */}
      <Alert
        severity={quotaCheck.approved ? 'success' : 'error'}
        sx={{ mb: 3 }}
        icon={quotaCheck.approved ? <ValidIcon /> : <ErrorIcon />}
      >
        {quotaCheck.approved ? (
          <>
            <strong>Quota OK:</strong> {quotaCheck.remainingCapacity} Leads verfügbar (
            {quotaCheck.currentOpenLeads} von {quotaCheck.maxOpenLeads} belegt)
          </>
        ) : (
          quotaCheck.message
        )}
      </Alert>

      {/* Validation Summary */}
      <Typography variant="h6" gutterBottom>
        Validierungsergebnis
      </Typography>
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 6, sm: 3 }}>
          <StatCard
            label="Gesamt"
            value={validation.totalRows}
            color="info"
            icon={<InfoIcon fontSize="large" />}
          />
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <StatCard
            label="Gültig"
            value={validation.validRows}
            color="success"
            icon={<ValidIcon fontSize="large" />}
          />
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <StatCard
            label="Duplikate"
            value={validation.duplicateRows}
            color="warning"
            icon={<DuplicateIcon fontSize="large" />}
          />
        </Grid>
        <Grid size={{ xs: 6, sm: 3 }}>
          <StatCard
            label="Fehler"
            value={validation.errorRows}
            color="error"
            icon={<ErrorIcon fontSize="large" />}
          />
        </Grid>
      </Grid>

      {/* Errors Accordion */}
      {errors.length > 0 && (
        <Accordion defaultExpanded={errors.length <= 10} sx={{ mb: 2 }}>
          <AccordionSummary expandIcon={<ExpandIcon />}>
            <Typography color="error" sx={{ fontWeight: 500 }}>
              {errors.length} Fehler gefunden
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.50' }}>
                    <TableCell sx={{ fontWeight: 600 }}>Zeile</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Spalte</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Fehler</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Wert</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {errors.slice(0, 50).map((error, idx) => (
                    <TableRow key={idx}>
                      <TableCell>{error.row}</TableCell>
                      <TableCell>{error.column}</TableCell>
                      <TableCell>{error.message}</TableCell>
                      <TableCell>
                        <code style={{ fontSize: '0.85em' }}>{error.value || '—'}</code>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            {errors.length > 50 && (
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                ... und {errors.length - 50} weitere Fehler
              </Typography>
            )}
          </AccordionDetails>
        </Accordion>
      )}

      {/* Duplicates Accordion */}
      {duplicates.length > 0 && (
        <Accordion sx={{ mb: 2 }}>
          <AccordionSummary expandIcon={<ExpandIcon />}>
            <Typography color="warning.dark" sx={{ fontWeight: 500 }}>
              {duplicates.length} Duplikate gefunden
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Alert severity="info" sx={{ mb: 2 }}>
              Diese Leads existieren möglicherweise bereits im System. Im nächsten Schritt können
              Sie wählen, ob sie übersprungen oder trotzdem angelegt werden.
            </Alert>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ bgcolor: 'grey.50' }}>
                    <TableCell sx={{ fontWeight: 600 }}>Zeile</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Bestehender Lead</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Typ</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Ähnlichkeit</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {duplicates.slice(0, 20).map((dup, idx) => (
                    <TableRow key={idx}>
                      <TableCell>{dup.row}</TableCell>
                      <TableCell>{dup.existingCompanyName}</TableCell>
                      <TableCell>
                        <Chip
                          label={dup.type === 'HARD_COLLISION' ? 'Exakt' : 'Ähnlich'}
                          size="small"
                          color={dup.type === 'HARD_COLLISION' ? 'error' : 'warning'}
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>{Math.round(dup.similarity * 100)}%</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            {duplicates.length > 20 && (
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                ... und {duplicates.length - 20} weitere Duplikate
              </Typography>
            )}
          </AccordionDetails>
        </Accordion>
      )}

      {/* Preview Table */}
      <Accordion defaultExpanded sx={{ mb: 3 }}>
        <AccordionSummary expandIcon={<ExpandIcon />}>
          <Typography sx={{ fontWeight: 500 }}>Datenvorschau (erste 5 Zeilen)</Typography>
        </AccordionSummary>
        <AccordionDetails>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow sx={{ bgcolor: 'grey.50' }}>
                  <TableCell sx={{ fontWeight: 600 }}>Zeile</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                  {Object.values(mapping)
                    .filter(Boolean)
                    .map(field => (
                      <TableCell key={field} sx={{ fontWeight: 600 }}>
                        {field}
                      </TableCell>
                    ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {previewRows.slice(0, 5).map((row, idx) => (
                  <TableRow key={idx}>
                    <TableCell>{row.row}</TableCell>
                    <TableCell>
                      <Chip
                        label={
                          row.status === 'VALID'
                            ? 'Gültig'
                            : row.status === 'DUPLICATE'
                              ? 'Duplikat'
                              : 'Fehler'
                        }
                        size="small"
                        color={
                          row.status === 'VALID'
                            ? 'success'
                            : row.status === 'DUPLICATE'
                              ? 'warning'
                              : 'error'
                        }
                        variant="outlined"
                      />
                    </TableCell>
                    {Object.values(mapping)
                      .filter(Boolean)
                      .map(field => (
                        <TableCell key={field}>{row.data[field] || '—'}</TableCell>
                      ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </AccordionDetails>
      </Accordion>

      {/* Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Button variant="outlined" onClick={onBack}>
          Zurück
        </Button>
        <Button variant="contained" onClick={handleContinue} disabled={!canImport}>
          {canImport ? 'Weiter zum Import' : 'Import nicht möglich'}
        </Button>
      </Box>
    </Box>
  );
}

export default PreviewStep;
