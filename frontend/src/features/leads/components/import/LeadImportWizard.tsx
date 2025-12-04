/**
 * Lead Import Wizard - Sprint 2.1.8 Phase 2
 *
 * 4-Schritt Import-Wizard für Self-Service Lead-Import:
 * 1. Datei hochladen (CSV/Excel)
 * 2. Spalten zuordnen (Auto-Mapping + manuell)
 * 3. Vorschau & Validierung
 * 4. Import ausführen
 *
 * @module LeadImportWizard
 * @since Sprint 2.1.8
 */

import { useState, useCallback } from 'react';
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

// API Types
import type {
  ImportUploadResponse,
  ImportPreviewResponse,
  ImportExecuteResponse,
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
  executeResult: ImportExecuteResponse | null;
  source: string;
  duplicateAction: 'SKIP' | 'CREATE';
  ignoreErrors: boolean;
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
    executeResult: null,
    source: '',
    duplicateAction: 'SKIP',
    ignoreErrors: false,
  });

  // Reset Wizard
  const resetWizard = useCallback(() => {
    setActiveStep(0);
    setError(null);
    setState({
      uploadData: null,
      mapping: {},
      previewData: null,
      executeResult: null,
      source: '',
      duplicateAction: 'SKIP',
      ignoreErrors: false,
    });
  }, []);

  // Handle Close
  const handleClose = useCallback(() => {
    resetWizard();
    onClose();
  }, [resetWizard, onClose]);

  // Step 1: Upload Complete
  const handleUploadComplete = useCallback((data: ImportUploadResponse) => {
    setState(prev => ({
      ...prev,
      uploadData: data,
      mapping: data.autoMapping || {},
    }));
    setActiveStep(1);
    setError(null);
  }, []);

  // Step 2: Mapping Complete
  const handleMappingComplete = useCallback((mapping: Record<string, string>) => {
    setState(prev => ({ ...prev, mapping }));
    setActiveStep(2);
    setError(null);
  }, []);

  // Step 3: Preview Complete (Navigate to Execute)
  const handlePreviewComplete = useCallback((previewData: ImportPreviewResponse) => {
    setState(prev => ({ ...prev, previewData }));
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
        return <FileUploadStep onUploadComplete={handleUploadComplete} onError={setError} />;

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
        return state.uploadData ? (
          <PreviewStep
            uploadId={state.uploadData.uploadId}
            mapping={state.mapping}
            onPreviewComplete={handlePreviewComplete}
            onBack={handleBack}
            onError={setError}
          />
        ) : null;

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
        <IconButton onClick={handleClose} size="small" aria-label="Schließen">
          <CloseIcon />
        </IconButton>
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
