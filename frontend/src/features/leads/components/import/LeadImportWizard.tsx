/**
 * Lead Import Wizard - Sprint 2.1.8 Phase 2
 *
 * 4-Schritt Import-Wizard für Self-Service Lead-Import:
 * 1. Datei hochladen (CSV/Excel)
 * 2. Spalten zuordnen (Auto-Mapping + manuell)
 * 3. Vorschau & Validierung
 * 4. Import ausführen
 *
 * REFACTORED für bessere Testbarkeit:
 * - API-Calls für Preview werden hier gemacht, nicht in PreviewStep
 * - Siehe testing_guide.md: "Container/Presentational Pattern"
 *
 * @module LeadImportWizard
 * @since Sprint 2.1.8
 */

import { useState, useCallback, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stepper,
  Step,
  StepLabel,
  Box,
  Typography,
  IconButton,
  Alert,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';

// Import Steps
import { FileUploadStep } from './FileUploadStep';
import { FieldMappingStep } from './FieldMappingStep';
import { PreviewStep } from './PreviewStep';
import { ExecuteStep } from './ExecuteStep';

// Central Help System
import { HelpTooltip } from '@/features/help';

// API
import {
  createPreview,
  getQuotaInfo,
  type ImportUploadResponse,
  type ImportPreviewResponse,
  type ImportExecuteResponse,
  type QuotaInfo,
} from '../../api/leadImportApi';

// ============================================================================
// Types
// ============================================================================

export interface LeadImportWizardProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: (result: ImportExecuteResponse) => void;
}

interface WizardState {
  uploadData: ImportUploadResponse | null;
  mapping: Record<string, string>;
  previewData: ImportPreviewResponse | null;
  previewLoading: boolean;
  executeResult: ImportExecuteResponse | null;
  source: string;
  duplicateAction: 'SKIP' | 'CREATE';
  ignoreErrors: boolean;
  // Quota State (für FileUploadStep)
  quotaInfo: QuotaInfo | null;
  quotaLoading: boolean;
}

// ============================================================================
// Constants
// ============================================================================

const STEPS = [
  { label: 'Datei hochladen', description: 'CSV oder Excel-Datei auswählen' },
  { label: 'Spalten zuordnen', description: 'Felder den Lead-Eigenschaften zuweisen' },
  { label: 'Vorschau', description: 'Daten prüfen und validieren' },
  { label: 'Importieren', description: 'Import starten und Ergebnis ansehen' },
];

// ============================================================================
// Component
// ============================================================================

export function LeadImportWizard({ open, onClose, onSuccess }: LeadImportWizardProps) {
  // Wizard State
  const [activeStep, setActiveStep] = useState(0);
  const [error, setError] = useState<string | null>(null);

  // Data State
  const [state, setState] = useState<WizardState>({
    uploadData: null,
    mapping: {},
    previewData: null,
    previewLoading: false,
    executeResult: null,
    source: '',
    duplicateAction: 'SKIP',
    ignoreErrors: false,
    quotaInfo: null,
    quotaLoading: true, // Starten mit Loading
  });

  // Reset Wizard
  const resetWizard = useCallback(() => {
    setActiveStep(0);
    setError(null);
    setState({
      uploadData: null,
      mapping: {},
      previewData: null,
      previewLoading: false,
      executeResult: null,
      source: '',
      duplicateAction: 'SKIP',
      ignoreErrors: false,
      quotaInfo: null,
      quotaLoading: true,
    });
  }, []);

  // Load Quota on Dialog Open
  useEffect(() => {
    async function loadQuota() {
      if (!open) return;

      setState(prev => ({ ...prev, quotaLoading: true }));
      try {
        const quota = await getQuotaInfo();
        setState(prev => ({ ...prev, quotaInfo: quota, quotaLoading: false }));
      } catch (err) {
        console.error('Failed to load quota info:', err);
        // Quota-Fehler nicht als Block behandeln, nur nicht anzeigen
        setState(prev => ({ ...prev, quotaInfo: null, quotaLoading: false }));
      }
    }

    loadQuota();
  }, [open]);

  // Handle Close
  const handleClose = useCallback(() => {
    resetWizard();
    onClose();
  }, [resetWizard, onClose]);

  // Step 1: Upload Complete
  const handleUploadComplete = useCallback((data: ImportUploadResponse) => {
    // Backend API standardized on 'suggestedMapping' (Sprint 2.1.8).
    // Legacy fallback to 'autoMapping' preserved for backwards compatibility during migration.
    // TODO: Remove 'autoMapping' fallback after confirming all API clients are updated.
    const autoMapping = data.suggestedMapping || data.autoMapping || {};
    setState(prev => ({
      ...prev,
      uploadData: data,
      mapping: autoMapping,
    }));
    setActiveStep(1);
    setError(null);
  }, []);

  // Step 2: Mapping Complete - triggers Preview API call
  const handleMappingComplete = useCallback((mapping: Record<string, string>) => {
    setState(prev => ({ ...prev, mapping, previewData: null, previewLoading: true }));
    setActiveStep(2);
    setError(null);
  }, []);

  // Load Preview Data when entering Step 2
  useEffect(() => {
    async function loadPreview() {
      if (activeStep !== 2 || !state.uploadData || !state.previewLoading) {
        return;
      }

      try {
        const data = await createPreview(state.uploadData.uploadId, state.mapping);
        setState(prev => ({ ...prev, previewData: data, previewLoading: false }));
      } catch (err) {
        const errorMessage =
          err instanceof Error
            ? err.message
            : (err as { detail?: string })?.detail || 'Vorschau konnte nicht geladen werden';
        setError(errorMessage);
        setState(prev => ({ ...prev, previewLoading: false }));
      }
    }

    loadPreview();
  }, [activeStep, state.uploadData, state.mapping, state.previewLoading]);

  // Step 3: Preview Continue (Navigate to Execute)
  const handlePreviewContinue = useCallback(() => {
    setActiveStep(3);
    setError(null);
  }, []);

  // Step 4: Execute Complete
  const handleExecuteComplete = useCallback(
    (result: ImportExecuteResponse) => {
      setState(prev => ({ ...prev, executeResult: result }));
      if (result.success && onSuccess) {
        onSuccess(result);
      }
    },
    [onSuccess]
  );

  // Update Settings (Source, DuplicateAction, IgnoreErrors)
  const handleSettingsChange = useCallback(
    (settings: {
      source?: string;
      duplicateAction?: 'SKIP' | 'CREATE';
      ignoreErrors?: boolean;
    }) => {
      setState(prev => ({
        ...prev,
        ...(settings.source !== undefined && { source: settings.source }),
        ...(settings.duplicateAction !== undefined && {
          duplicateAction: settings.duplicateAction,
        }),
        ...(settings.ignoreErrors !== undefined && { ignoreErrors: settings.ignoreErrors }),
      }));
    },
    []
  );

  // Navigation
  const handleBack = useCallback(() => {
    setActiveStep(prev => Math.max(0, prev - 1));
    setError(null);
  }, []);

  // Render current step content
  const renderStepContent = () => {
    switch (activeStep) {
      case 0:
        return (
          <FileUploadStep
            quotaInfo={state.quotaInfo}
            quotaLoading={state.quotaLoading}
            onUploadComplete={handleUploadComplete}
            onError={setError}
          />
        );

      case 1:
        return state.uploadData ? (
          <FieldMappingStep
            uploadId={state.uploadData.uploadId}
            columns={state.uploadData.columns}
            initialMapping={state.mapping}
            onMappingComplete={handleMappingComplete}
            onBack={handleBack}
          />
        ) : null;

      case 2:
        return (
          <PreviewStep
            previewData={state.previewData}
            isLoading={state.previewLoading}
            mapping={state.mapping}
            onContinue={handlePreviewContinue}
            onBack={handleBack}
          />
        );

      case 3:
        return state.uploadData && state.previewData ? (
          <ExecuteStep
            uploadId={state.uploadData.uploadId}
            mapping={state.mapping}
            previewData={state.previewData}
            source={state.source}
            duplicateAction={state.duplicateAction}
            ignoreErrors={state.ignoreErrors}
            onSettingsChange={handleSettingsChange}
            onExecuteComplete={handleExecuteComplete}
            onBack={handleBack}
            onClose={handleClose}
            onError={setError}
          />
        ) : null;

      default:
        return null;
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          minHeight: '70vh',
          maxHeight: '90vh',
        },
      }}
    >
      {/* Header */}
      <DialogTitle
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: 1,
          borderColor: 'divider',
          pb: 2,
        }}
      >
        <Box>
          <Typography variant="h5" component="span">
            Lead-Import
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {STEPS[activeStep]?.description}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <HelpTooltip feature="lead-import" placement="bottom" />
          <IconButton onClick={handleClose} size="small" aria-label="Schließen">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      {/* Stepper */}
      <Box sx={{ px: 3, pt: 3 }}>
        <Stepper activeStep={activeStep} alternativeLabel>
          {STEPS.map((step, index) => (
            <Step key={step.label} completed={index < activeStep}>
              <StepLabel>{step.label}</StepLabel>
            </Step>
          ))}
        </Stepper>
      </Box>

      {/* Content */}
      <DialogContent sx={{ px: 3, py: 3, overflow: 'auto' }}>
        {/* Error Alert */}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* Step Content */}
        {renderStepContent()}
      </DialogContent>

      {/* Footer Info */}
      {activeStep < 3 && (
        <DialogActions sx={{ px: 3, pb: 2, justifyContent: 'space-between' }}>
          <Typography variant="caption" color="text.secondary">
            Schritt {activeStep + 1} von {STEPS.length}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Unterstützte Formate: CSV, XLSX (max. 5 MB)
          </Typography>
        </DialogActions>
      )}
    </Dialog>
  );
}

export default LeadImportWizard;
