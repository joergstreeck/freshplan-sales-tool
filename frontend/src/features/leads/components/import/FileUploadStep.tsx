/**
 * File Upload Step - Sprint 2.1.8 Phase 2
 *
 * Schritt 1 des Import-Wizards: Datei-Upload mit Drag & Drop.
 *
 * Features:
 * - Drag & Drop Support
 * - CSV und Excel Unterstützung
 * - Max. 5 MB Dateigröße
 * - Quota-Check vor Upload
 *
 * @module FileUploadStep
 * @since Sprint 2.1.8
 */

import { useState, useCallback, useEffect } from 'react';
import { Box, Typography, Alert, LinearProgress, Chip, Paper } from '@mui/material';
import {
  CloudUpload as UploadIcon,
  Description as FileIcon,
  CheckCircle as SuccessIcon,
} from '@mui/icons-material';
import { useDropzone } from 'react-dropzone';

// API
import {
  uploadFile,
  getQuotaInfo,
  type ImportUploadResponse,
  type QuotaInfo,
} from '../../api/leadImportApi';

// ============================================================================
// Types
// ============================================================================

export interface FileUploadStepProps {
  onUploadComplete: (data: ImportUploadResponse) => void;
  onError: (error: string) => void;
}

// ============================================================================
// Constants
// ============================================================================

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

const ACCEPTED_TYPES = {
  'text/csv': ['.csv'],
  'application/vnd.ms-excel': ['.xls'],
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
};

// ============================================================================
// Component
// ============================================================================

export function FileUploadStep({ onUploadComplete, onError }: FileUploadStepProps) {
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [quotaInfo, setQuotaInfo] = useState<QuotaInfo | null>(null);
  const [quotaLoading, setQuotaLoading] = useState(true);

  // Lade Quota-Info beim Mounten
  useEffect(() => {
    async function loadQuota() {
      try {
        const info = await getQuotaInfo();
        setQuotaInfo(info);
      } catch (err) {
        console.error('Fehler beim Laden der Quota-Info:', err);
        // Nicht als Fehler anzeigen - Wizard funktioniert auch ohne Quota-Anzeige
      } finally {
        setQuotaLoading(false);
      }
    }
    loadQuota();
  }, []);

  // Handle File Upload
  const handleUpload = useCallback(
    async (file: File) => {
      setIsUploading(true);
      setUploadProgress(0);

      try {
        // Simuliere Progress (da fetch kein echtes Progress-Event hat)
        const progressInterval = setInterval(() => {
          setUploadProgress(prev => Math.min(prev + 10, 90));
        }, 200);

        const result = await uploadFile(file);

        clearInterval(progressInterval);
        setUploadProgress(100);

        // Kurze Verzögerung für visuelles Feedback
        setTimeout(() => {
          onUploadComplete(result);
        }, 500);
      } catch (err) {
        const errorMessage =
          err instanceof Error
            ? err.message
            : (err as { detail?: string })?.detail || 'Upload fehlgeschlagen';
        onError(errorMessage);
        setIsUploading(false);
        setUploadProgress(0);
        setSelectedFile(null);
      }
    },
    [onUploadComplete, onError]
  );

  // Dropzone Configuration
  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      if (acceptedFiles.length === 0) return;

      const file = acceptedFiles[0];
      setSelectedFile(file);
      handleUpload(file);
    },
    [handleUpload]
  );

  const { getRootProps, getInputProps, isDragActive, fileRejections } = useDropzone({
    onDrop,
    accept: ACCEPTED_TYPES,
    maxSize: MAX_FILE_SIZE,
    maxFiles: 1,
    disabled: isUploading,
  });

  // Handle File Rejections
  useEffect(() => {
    if (fileRejections.length > 0) {
      const rejection = fileRejections[0];
      const errors = rejection.errors.map(e => {
        if (e.code === 'file-too-large') {
          return 'Datei zu groß (max. 5 MB)';
        }
        if (e.code === 'file-invalid-type') {
          return 'Ungültiger Dateityp (nur CSV, XLS, XLSX)';
        }
        return e.message;
      });
      onError(errors.join(', '));
    }
  }, [fileRejections, onError]);

  // Format File Size
  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <Box>
      {/* Quota Info */}
      {!quotaLoading && quotaInfo && (
        <Alert severity={quotaInfo.canImport ? 'info' : 'warning'} sx={{ mb: 3 }}>
          <Typography variant="body2">
            <strong>Ihre Kapazität:</strong> {quotaInfo.remainingCapacity} Leads verfügbar (Aktuell:{' '}
            {quotaInfo.currentOpenLeads} / {quotaInfo.maxOpenLeads} offene Leads)
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Heute: {quotaInfo.importsToday} / {quotaInfo.maxImportsPerDay} Imports &bull; Max.{' '}
            {quotaInfo.maxLeadsPerImport} Leads pro Import
          </Typography>
        </Alert>
      )}

      {/* Dropzone */}
      <Paper
        {...getRootProps()}
        elevation={0}
        sx={{
          border: '2px dashed',
          borderColor: isDragActive ? 'primary.main' : isUploading ? 'grey.400' : 'grey.300',
          borderRadius: 2,
          p: 6,
          textAlign: 'center',
          cursor: isUploading ? 'default' : 'pointer',
          bgcolor: isDragActive ? 'action.hover' : 'background.paper',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            borderColor: isUploading ? 'grey.400' : 'primary.main',
            bgcolor: isUploading ? 'background.paper' : 'action.hover',
          },
        }}
      >
        <input {...getInputProps()} />

        {isUploading ? (
          // Upload Progress
          <Box>
            <FileIcon sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
            <Typography variant="h6" gutterBottom>
              {selectedFile?.name}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {formatFileSize(selectedFile?.size || 0)}
            </Typography>
            <LinearProgress
              variant="determinate"
              value={uploadProgress}
              sx={{ height: 8, borderRadius: 4, mb: 1 }}
            />
            <Typography variant="caption" color="text.secondary">
              {uploadProgress < 100 ? 'Wird hochgeladen...' : 'Verarbeitung...'}
            </Typography>
          </Box>
        ) : (
          // Dropzone UI
          <Box>
            <UploadIcon
              sx={{
                fontSize: 64,
                color: isDragActive ? 'primary.main' : 'grey.400',
                mb: 2,
              }}
            />
            <Typography variant="h6" gutterBottom>
              {isDragActive ? 'Datei hier ablegen...' : 'CSV oder Excel-Datei hier ablegen'}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              oder klicken zum Auswählen
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center', flexWrap: 'wrap' }}>
              <Chip label="CSV" size="small" variant="outlined" />
              <Chip label="XLSX" size="small" variant="outlined" />
              <Chip label="XLS" size="small" variant="outlined" />
              <Chip label="max. 5 MB" size="small" variant="outlined" color="default" />
            </Box>
          </Box>
        )}
      </Paper>

      {/* Info Box */}
      <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
        <Typography
          variant="subtitle2"
          gutterBottom
          sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
        >
          <SuccessIcon fontSize="small" color="success" />
          Tipps für erfolgreichen Import
        </Typography>
        <Typography variant="body2" color="text.secondary" component="ul" sx={{ m: 0, pl: 2 }}>
          <li>Die erste Zeile sollte Spaltenüberschriften enthalten</li>
          <li>Spalte &quot;Firma&quot; oder &quot;Firmenname&quot; ist Pflicht</li>
          <li>Unterstützte Zeichensätze: UTF-8, Windows-1252, ISO-8859-1</li>
          <li>Duplikate werden automatisch erkannt</li>
        </Typography>
      </Box>
    </Box>
  );
}

export default FileUploadStep;
