import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Grid,
  Chip,
  Paper,
  Divider,
  IconButton,
  Alert,
  Tooltip,
  Skeleton,
} from '@mui/material';
import {
  Close as CloseIcon,
  Security as SecurityIcon,
  Warning as WarningIcon,
  CheckCircle as CheckIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  ContentCopy as CopyIcon,
  Download as DownloadIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';
import { useQuery } from '@tanstack/react-query';
import type { AuditLog } from '../types';
import { auditApi } from '../services/auditApi';
import toast from 'react-hot-toast';

interface AuditDetailModalProps {
  auditId: string | null;
  open: boolean;
  onClose: () => void;
}

export const AuditDetailModal: React.FC<AuditDetailModalProps> = ({ auditId, open, onClose }) => {
  const { data: audit, isLoading } = useQuery({
    queryKey: ['auditDetail', auditId],
    queryFn: () => auditApi.getAuditDetail(auditId!),
    enabled: !!auditId && open,
  });

  const getActionIcon = (action: string) => {
    switch (action) {
      case 'CREATE':
        return <CheckIcon color="success" />;
      case 'UPDATE':
        return <InfoIcon color="info" />;
      case 'DELETE':
        return <ErrorIcon color="error" />;
      case 'LOGIN':
        return <SecurityIcon color="primary" />;
      default:
        return <InfoIcon />;
    }
  };

  const getSeverityColor = (severity?: string): 'error' | 'warning' | 'info' | 'default' => {
    switch (severity) {
      case 'HIGH':
        return 'error';
      case 'MEDIUM':
        return 'warning';
      case 'LOW':
        return 'info';
      default:
        return 'default';
    }
  };

  const copyToClipboard = async (text: string, label: string = 'Text') => {
    try {
      await navigator.clipboard.writeText(text);
      toast.success(`${label} kopiert!`);
    } catch (error) {
      toast.error('Kopieren fehlgeschlagen');
    }
  };

  const exportAuditLog = () => {
    if (!audit) return;

    const data = JSON.stringify(audit, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-log-${audit.id}.json`;
    a.click();
    URL.revokeObjectURL(url);
    toast.success('Audit-Log exportiert');
  };

  if (!audit && !isLoading) return null;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: { minHeight: '70vh' },
      }}
    >
      <DialogTitle sx={{ m: 0, p: 2 }}>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box display="flex" alignItems="center" gap={2}>
            {audit && getActionIcon(audit.action)}
            <Typography
              variant="h6"
              sx={{
                fontFamily: 'Antonio, sans-serif',
                fontWeight: 'bold',
              }}
            >
              Audit Log Details
            </Typography>
          </Box>
          <Box>
            <Tooltip title="Export">
              <IconButton onClick={exportAuditLog} size="small" disabled={!audit}>
                <DownloadIcon />
              </IconButton>
            </Tooltip>
            <IconButton onClick={onClose} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        {isLoading ? (
          <Box>
            <Skeleton variant="rectangular" height={100} sx={{ mb: 2 }} />
            <Skeleton variant="rectangular" height={100} sx={{ mb: 2 }} />
            <Skeleton variant="rectangular" height={100} />
          </Box>
        ) : audit ? (
          <Box>
            {/* Grundinformationen */}
            <Paper sx={{ p: 2, mb: 2, bgcolor: 'background.default' }}>
              <Typography
                variant="subtitle2"
                gutterBottom
                color="text.secondary"
                sx={{ fontFamily: 'Poppins, sans-serif' }}
              >
                Grundinformationen
              </Typography>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    ID
                  </Typography>
                  <Box display="flex" alignItems="center" gap={1}>
                    <Typography variant="body2" fontFamily="monospace" noWrap>
                      {audit.id}
                    </Typography>
                    <IconButton
                      size="small"
                      onClick={() => copyToClipboard(audit.id, 'ID')}
                      sx={{ p: 0.5 }}
                    >
                      <CopyIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    Zeitstempel
                  </Typography>
                  <Typography variant="body2">
                    {format(new Date(audit.timestamp || audit.occurredAt), 'PPpp', { locale: de })}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    Aktion
                  </Typography>
                  <Box sx={{ mt: 0.5 }}>
                    <Chip
                      label={audit.action}
                      size="small"
                      color={audit.action === 'DELETE' ? 'error' : 'primary'}
                      sx={{ fontFamily: 'Poppins, sans-serif' }}
                    />
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    Entity
                  </Typography>
                  <Typography variant="body2">
                    {audit.entityType} ({audit.entityId})
                  </Typography>
                </Grid>
              </Grid>
            </Paper>

            {/* Benutzerinformationen */}
            <Paper sx={{ p: 2, mb: 2, bgcolor: 'background.default' }}>
              <Typography
                variant="subtitle2"
                gutterBottom
                color="text.secondary"
                sx={{ fontFamily: 'Poppins, sans-serif' }}
              >
                Benutzerinformationen
              </Typography>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    Benutzer ID
                  </Typography>
                  <Typography variant="body2">{audit.userId}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    Benutzername
                  </Typography>
                  <Typography variant="body2">{audit.userName || audit.username || '-'}</Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    IP-Adresse
                  </Typography>
                  <Typography variant="body2" fontFamily="monospace">
                    {audit.ipAddress || '-'}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">
                    User Agent
                  </Typography>
                  <Tooltip title={audit.userAgent || ''}>
                    <Typography variant="body2" noWrap>
                      {audit.userAgent || '-'}
                    </Typography>
                  </Tooltip>
                </Grid>
              </Grid>
            </Paper>

            {/* Änderungsdetails */}
            {(audit.changes || audit.oldValues || audit.newValues) && (
              <Paper sx={{ p: 2, mb: 2, bgcolor: 'background.default' }}>
                <Typography
                  variant="subtitle2"
                  gutterBottom
                  color="text.secondary"
                  sx={{ fontFamily: 'Poppins, sans-serif' }}
                >
                  Änderungsdetails
                </Typography>

                {audit.oldValues && (
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="caption" color="text.secondary">
                      Vorherige Werte:
                    </Typography>
                    <Box
                      sx={{
                        maxHeight: 200,
                        overflow: 'auto',
                        bgcolor: 'grey.50',
                        borderRadius: 1,
                        p: 1,
                        mt: 0.5,
                      }}
                    >
                      <pre
                        style={{
                          fontSize: '0.875rem',
                          fontFamily: 'monospace',
                          margin: 0,
                          whiteSpace: 'pre-wrap',
                          wordBreak: 'break-word',
                        }}
                      >
                        {JSON.stringify(audit.oldValues, null, 2)}
                      </pre>
                    </Box>
                  </Box>
                )}

                {audit.newValues && (
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Neue Werte:
                    </Typography>
                    <Box
                      sx={{
                        maxHeight: 200,
                        overflow: 'auto',
                        bgcolor: 'grey.50',
                        borderRadius: 1,
                        p: 1,
                        mt: 0.5,
                      }}
                    >
                      <pre
                        style={{
                          fontSize: '0.875rem',
                          fontFamily: 'monospace',
                          margin: 0,
                          whiteSpace: 'pre-wrap',
                          wordBreak: 'break-word',
                        }}
                      >
                        {JSON.stringify(audit.newValues, null, 2)}
                      </pre>
                    </Box>
                  </Box>
                )}

                {audit.changes && (
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Änderungen:
                    </Typography>
                    <Box
                      sx={{
                        maxHeight: 200,
                        overflow: 'auto',
                        bgcolor: 'grey.50',
                        borderRadius: 1,
                        p: 1,
                        mt: 0.5,
                      }}
                    >
                      <pre
                        style={{
                          fontSize: '0.875rem',
                          fontFamily: 'monospace',
                          margin: 0,
                          whiteSpace: 'pre-wrap',
                          wordBreak: 'break-word',
                        }}
                      >
                        {JSON.stringify(audit.changes, null, 2)}
                      </pre>
                    </Box>
                  </Box>
                )}
              </Paper>
            )}

            {/* Sicherheitsinformationen */}
            <Paper sx={{ p: 2, mb: 2, bgcolor: 'background.default' }}>
              <Typography
                variant="subtitle2"
                gutterBottom
                color="text.secondary"
                sx={{ fontFamily: 'Poppins, sans-serif' }}
              >
                Sicherheit & Integrität
              </Typography>
              <Grid container spacing={2}>
                <Grid size={12}>
                  <Typography variant="caption" color="text.secondary">
                    Hash-Chain
                  </Typography>
                  <Typography
                    variant="body2"
                    fontFamily="monospace"
                    sx={{
                      wordBreak: 'break-all',
                      fontSize: '0.75rem',
                    }}
                  >
                    {audit.hash || '-'}
                  </Typography>
                </Grid>
                {audit.previousHash && (
                  <Grid size={12}>
                    <Typography variant="caption" color="text.secondary">
                      Vorheriger Hash
                    </Typography>
                    <Typography
                      variant="body2"
                      fontFamily="monospace"
                      sx={{
                        wordBreak: 'break-all',
                        fontSize: '0.75rem',
                      }}
                    >
                      {audit.previousHash}
                    </Typography>
                  </Grid>
                )}
                <Grid size={12}>
                  <Box display="flex" alignItems="center" gap={1}>
                    {audit.isValid !== false ? (
                      <>
                        <CheckIcon color="success" fontSize="small" />
                        <Typography variant="body2" color="success.main">
                          Hash-Chain verifiziert
                        </Typography>
                      </>
                    ) : (
                      <>
                        <WarningIcon color="error" fontSize="small" />
                        <Typography variant="body2" color="error.main">
                          Hash-Chain ungültig - mögliche Manipulation!
                        </Typography>
                      </>
                    )}
                  </Box>
                </Grid>
              </Grid>
            </Paper>

            {/* Compliance-Informationen */}
            {(audit.isDsgvoRelevant || audit.retentionPeriod || audit.deletionDate) && (
              <Paper sx={{ p: 2, bgcolor: 'background.default' }}>
                <Typography
                  variant="subtitle2"
                  gutterBottom
                  color="text.secondary"
                  sx={{ fontFamily: 'Poppins, sans-serif' }}
                >
                  Compliance & Retention
                </Typography>
                <Grid container spacing={2}>
                  {audit.isDsgvoRelevant && (
                    <Grid size={12}>
                      <Alert severity="info" sx={{ py: 0.5 }}>
                        <Typography variant="body2">
                          DSGVO-relevant - unterliegt besonderen Datenschutzbestimmungen
                        </Typography>
                      </Alert>
                    </Grid>
                  )}
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Typography variant="caption" color="text.secondary">
                      Aufbewahrungspflicht
                    </Typography>
                    <Typography variant="body2">
                      {audit.retentionPeriod || '10 Jahre (Standard)'}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Typography variant="caption" color="text.secondary">
                      Löschung geplant
                    </Typography>
                    <Typography variant="body2">
                      {audit.deletionDate
                        ? format(new Date(audit.deletionDate), 'PP', { locale: de })
                        : 'Nicht geplant'}
                    </Typography>
                  </Grid>
                  {audit.dsgvoCategories && audit.dsgvoCategories.length > 0 && (
                    <Grid size={12}>
                      <Typography variant="caption" color="text.secondary">
                        DSGVO-Kategorien
                      </Typography>
                      <Box display="flex" gap={0.5} flexWrap="wrap" mt={0.5}>
                        {audit.dsgvoCategories.map((category: string) => (
                          <Chip key={category} label={category} size="small" variant="outlined" />
                        ))}
                      </Box>
                    </Grid>
                  )}
                </Grid>
              </Paper>
            )}

            {/* Warnungen */}
            {audit.warnings && audit.warnings.length > 0 && (
              <Alert severity="warning" sx={{ mt: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Sicherheitswarnungen
                </Typography>
                <ul style={{ margin: 0, paddingLeft: 20 }}>
                  {audit.warnings.map((warning: string, index: number) => (
                    <li key={index}>
                      <Typography variant="body2">{warning}</Typography>
                    </li>
                  ))}
                </ul>
              </Alert>
            )}
          </Box>
        ) : null}
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        <Button
          onClick={onClose}
          variant="contained"
          sx={{
            bgcolor: '#004F7B',
            '&:hover': { bgcolor: '#003d62' },
          }}
        >
          Schließen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
