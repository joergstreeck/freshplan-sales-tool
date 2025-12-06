/**
 * Execute Step - Sprint 2.1.8 Phase 2
 *
 * Schritt 4 des Import-Wizards: Import ausführen.
 *
 * Features:
 * - Import-Optionen (Duplikat-Behandlung, Quelle)
 * - Zusammenfassung vor dem Import
 * - Import-Fortschritt
 * - Erfolgsmeldung mit Statistik
 *
 * @module ExecuteStep
 * @since Sprint 2.1.8
 */

import { useState, useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  Alert,
  Paper,
  TextField,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Checkbox,
  Divider,
  CircularProgress,
  LinearProgress,
  Grid,
} from '@mui/material';
import {
  CheckCircle as SuccessIcon,
  Error as ErrorIcon,
  HourglassEmpty as PendingIcon,
  Upload as ImportIcon,
  SkipNext as SkipIcon,
  Add as CreateIcon,
} from '@mui/icons-material';

// API
import {
  executeImport,
  type ImportPreviewResponse,
  type ImportExecuteResponse,
} from '../../api/leadImportApi';

// ============================================================================
// Types
// ============================================================================

export interface ExecuteStepProps {
  uploadId: string;
  mapping: Record<string, string>;
  previewData: ImportPreviewResponse;
  source: string;
  duplicateAction: 'SKIP' | 'CREATE';
  ignoreErrors: boolean;
  onSettingsChange: (settings: {
    source?: string;
    duplicateAction?: 'SKIP' | 'CREATE';
    ignoreErrors?: boolean;
  }) => void;
  onExecuteComplete: (result: ImportExecuteResponse) => void;
  onBack: () => void;
  onClose: () => void;
  onError: (error: string) => void;
}

// ============================================================================
// Component
// ============================================================================

export function ExecuteStep({
  uploadId,
  mapping,
  previewData,
  source,
  duplicateAction,
  ignoreErrors,
  onSettingsChange,
  onExecuteComplete,
  onBack,
  onClose,
  onError,
}: ExecuteStepProps) {
  const [isExecuting, setIsExecuting] = useState(false);
  const [result, setResult] = useState<ImportExecuteResponse | null>(null);

  const { validation } = previewData;

  // Calculate expected imports
  const expectedImports =
    duplicateAction === 'SKIP'
      ? validation.validRows
      : validation.validRows + validation.duplicateRows;

  // Execute Import
  const handleExecute = useCallback(async () => {
    setIsExecuting(true);

    try {
      const response = await executeImport(uploadId, {
        mapping,
        duplicateAction,
        source: source || undefined,
        ignoreErrors,
      });

      setResult(response);
      onExecuteComplete(response);
    } catch (err) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : (err as { detail?: string })?.detail || 'Import fehlgeschlagen';
      onError(errorMessage);
      setIsExecuting(false);
    }
  }, [uploadId, mapping, duplicateAction, source, ignoreErrors, onExecuteComplete, onError]);

  // Result View
  if (result) {
    return (
      <Box>
        {/* Status Icon */}
        <Box sx={{ textAlign: 'center', mb: 4 }}>
          {result.success ? (
            <SuccessIcon sx={{ fontSize: 80, color: 'success.main' }} />
          ) : result.status === 'PENDING_APPROVAL' ? (
            <PendingIcon sx={{ fontSize: 80, color: 'warning.main' }} />
          ) : (
            <ErrorIcon sx={{ fontSize: 80, color: 'error.main' }} />
          )}
          <Typography variant="h5" sx={{ mt: 2 }}>
            {result.success
              ? 'Import erfolgreich!'
              : result.status === 'PENDING_APPROVAL'
                ? 'Import wartet auf Freigabe'
                : 'Import fehlgeschlagen'}
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mt: 1 }}>
            {result.message}
          </Typography>
        </Box>

        {/* Result Statistics */}
        {result.success && (
          <Grid container spacing={2} sx={{ mb: 4 }}>
            <Grid size={{ xs: 4 }}>
              <Paper variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                <Typography variant="h4" color="success.main" sx={{ fontWeight: 'bold' }}>
                  {result.imported}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Importiert
                </Typography>
              </Paper>
            </Grid>
            <Grid size={{ xs: 4 }}>
              <Paper variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                <Typography variant="h4" color="warning.main" sx={{ fontWeight: 'bold' }}>
                  {result.skipped}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Übersprungen
                </Typography>
              </Paper>
            </Grid>
            <Grid size={{ xs: 4 }}>
              <Paper variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                <Typography variant="h4" color="error.main" sx={{ fontWeight: 'bold' }}>
                  {result.errors}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Fehler
                </Typography>
              </Paper>
            </Grid>
          </Grid>
        )}

        {/* Pending Approval Info */}
        {result.status === 'PENDING_APPROVAL' && (
          <Alert severity="warning" sx={{ mb: 3 }}>
            Der Import enthält viele Duplikate und muss von einem Manager oder Admin freigegeben
            werden. Sie werden benachrichtigt, sobald der Import bearbeitet wurde.
          </Alert>
        )}

        {/* Actions */}
        <Box sx={{ display: 'flex', justifyContent: 'center' }}>
          <Button variant="contained" onClick={onClose} size="large">
            Schließen
          </Button>
        </Box>
      </Box>
    );
  }

  // Executing View
  if (isExecuting) {
    return (
      <Box sx={{ textAlign: 'center', py: 4 }}>
        <CircularProgress size={60} sx={{ mb: 3 }} />
        <Typography variant="h6" gutterBottom>
          Import wird ausgeführt...
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Bitte warten Sie, während die Leads importiert werden.
        </Typography>
        <LinearProgress sx={{ maxWidth: 400, mx: 'auto' }} />
      </Box>
    );
  }

  // Configuration View
  return (
    <Box>
      {/* Summary */}
      <Paper variant="outlined" sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Import-Zusammenfassung
        </Typography>
        <Box sx={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
          <Box>
            <Typography variant="body2" color="text.secondary">
              Gültige Leads
            </Typography>
            <Typography variant="h5" color="success.main">
              {validation.validRows}
            </Typography>
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary">
              Duplikate
            </Typography>
            <Typography variant="h5" color="warning.main">
              {validation.duplicateRows}
            </Typography>
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary">
              Fehler
            </Typography>
            <Typography variant="h5" color="error.main">
              {validation.errorRows}
            </Typography>
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary">
              Erwarteter Import
            </Typography>
            <Typography variant="h5" color="primary.main">
              {expectedImports}
            </Typography>
          </Box>
        </Box>
      </Paper>

      {/* Options */}
      <Typography variant="h6" gutterBottom>
        Import-Optionen
      </Typography>

      <Grid container spacing={3}>
        {/* Source Field */}
        <Grid size={{ xs: 12 }}>
          <TextField
            label="Quelle (optional)"
            value={source}
            onChange={e => onSettingsChange({ source: e.target.value })}
            placeholder="z.B. MESSE_FRANKFURT_2025, PARTNER_LISTE"
            fullWidth
            helperText="Optionale Kennzeichnung für spätere Auswertungen"
          />
        </Grid>

        {/* Duplicate Action */}
        {validation.duplicateRows > 0 && (
          <Grid size={{ xs: 12 }}>
            <FormControl component="fieldset">
              <FormLabel component="legend">Duplikat-Behandlung</FormLabel>
              <RadioGroup
                value={duplicateAction}
                onChange={e =>
                  onSettingsChange({ duplicateAction: e.target.value as 'SKIP' | 'CREATE' })
                }
              >
                <FormControlLabel
                  value="SKIP"
                  control={<Radio />}
                  label={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <SkipIcon fontSize="small" color="action" />
                      <Box>
                        <Typography>Duplikate überspringen (empfohlen)</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {validation.duplicateRows} Leads werden nicht importiert
                        </Typography>
                      </Box>
                    </Box>
                  }
                />
                <FormControlLabel
                  value="CREATE"
                  control={<Radio />}
                  label={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CreateIcon fontSize="small" color="action" />
                      <Box>
                        <Typography>Duplikate trotzdem anlegen</Typography>
                        <Typography variant="caption" color="text.secondary">
                          Kann zu doppelten Einträgen führen
                        </Typography>
                      </Box>
                    </Box>
                  }
                />
              </RadioGroup>
            </FormControl>
          </Grid>
        )}

        {/* Ignore Errors */}
        {validation.errorRows > 0 && (
          <Grid size={{ xs: 12 }}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={ignoreErrors}
                  onChange={e => onSettingsChange({ ignoreErrors: e.target.checked })}
                />
              }
              label={
                <Box>
                  <Typography>Fehlerhafte Zeilen überspringen</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {validation.errorRows} Zeilen mit Fehlern werden nicht importiert
                  </Typography>
                </Box>
              }
            />
          </Grid>
        )}
      </Grid>

      <Divider sx={{ my: 3 }} />

      {/* Warning if many duplicates */}
      {validation.duplicateRows > 0 && validation.duplicateRows / validation.totalRows > 0.1 && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          <strong>Hinweis:</strong> Mehr als 10% der Leads sind Duplikate.
          {duplicateAction === 'CREATE' &&
            ' Der Import wird möglicherweise zur Freigabe an einen Manager weitergeleitet.'}
        </Alert>
      )}

      {/* Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Button variant="outlined" onClick={onBack}>
          Zurück
        </Button>
        <Button
          variant="contained"
          color="primary"
          onClick={handleExecute}
          startIcon={<ImportIcon />}
          size="large"
        >
          {expectedImports} Leads importieren
        </Button>
      </Box>
    </Box>
  );
}

export default ExecuteStep;
