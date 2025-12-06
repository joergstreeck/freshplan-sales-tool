/**
 * Imports Admin Page - Sprint 2.1.8 Phase 3
 *
 * Admin-Dashboard für Lead-Import-Verwaltung:
 * - Übersicht: Import-Statistiken
 * - Alle Imports: Liste aller durchgeführten Imports
 * - Pending Approvals: Imports mit >10% Duplikaten zur Genehmigung
 * - Approve/Reject Workflow
 *
 * @module ImportsAdminPage
 * @since Sprint 2.1.8
 */

import React, { useState } from 'react';
import { Grid, useTheme } from '@mui/material';
import {
  Box,
  Paper,
  Typography,
  Tabs,
  Tab,
  Alert,
  Chip,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Tooltip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Badge,
} from '@mui/material';
import {
  CloudUpload as UploadIcon,
  CheckCircle as ApprovedIcon,
  Cancel as RejectedIcon,
  HourglassEmpty as PendingIcon,
  Refresh as RefreshIcon,
  Check as CheckIcon,
  Close as CloseIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';
import toast from 'react-hot-toast';

// ============================================================================
// Types
// ============================================================================

interface ImportStats {
  totalImports: number;
  pendingApprovals: number;
  completedImports: number;
  rejectedImports: number;
  totalImported: number;
  totalSkipped: number;
  totalErrors: number;
}

interface ImportLog {
  id: string;
  userId: string;
  importedAt: string;
  totalRows: number;
  importedCount: number;
  skippedCount: number;
  errorCount: number;
  duplicateRate: number;
  source: string | null;
  fileName: string | null;
  fileSizeBytes: number | null;
  fileType: string | null;
  status: 'PENDING' | 'COMPLETED' | 'PENDING_APPROVAL' | 'REJECTED';
  approvedBy: string | null;
  approvedAt: string | null;
  rejectionReason: string | null;
}

// ============================================================================
// API Functions
// ============================================================================

const API_BASE = '/api/admin/imports';

async function fetchImportStats(): Promise<ImportStats> {
  const response = await fetch(`${API_BASE}/stats`);
  if (!response.ok) throw new Error('Fehler beim Laden der Import-Statistiken');
  return response.json();
}

async function fetchAllImports(): Promise<ImportLog[]> {
  const response = await fetch(`${API_BASE}?limit=100`);
  if (!response.ok) throw new Error('Fehler beim Laden der Imports');
  return response.json();
}

async function fetchPendingApprovals(): Promise<ImportLog[]> {
  const response = await fetch(`${API_BASE}/pending`);
  if (!response.ok) throw new Error('Fehler beim Laden der Pending Approvals');
  return response.json();
}

async function approveImport(importId: string): Promise<ImportLog> {
  const response = await fetch(`${API_BASE}/${importId}/approve`, { method: 'POST' });
  if (!response.ok) throw new Error('Fehler beim Genehmigen des Imports');
  return response.json();
}

async function rejectImport(importId: string, reason: string): Promise<ImportLog> {
  const response = await fetch(`${API_BASE}/${importId}/reject`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ reason }),
  });
  if (!response.ok) throw new Error('Fehler beim Ablehnen des Imports');
  return response.json();
}

// ============================================================================
// Sub-Components
// ============================================================================

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <Box role="tabpanel" hidden={value !== index} id={`imports-tabpanel-${index}`}>
    {value === index && <Box>{children}</Box>}
  </Box>
);

interface StatCardProps {
  title: string;
  value: number;
  icon: React.ReactNode;
  color: string;
}

const StatCard: React.FC<StatCardProps> = ({ title, value, icon, color }) => (
  <Paper sx={{ p: 2, height: '100%' }}>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
      <Box sx={{ color, fontSize: 40 }}>{icon}</Box>
      <Box>
        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
          {value}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {title}
        </Typography>
      </Box>
    </Box>
  </Paper>
);

const StatusChip: React.FC<{ status: ImportLog['status'] }> = ({ status }) => {
  const config = {
    PENDING: { label: 'Läuft', color: 'default' as const, icon: <PendingIcon /> },
    COMPLETED: { label: 'Abgeschlossen', color: 'success' as const, icon: <ApprovedIcon /> },
    PENDING_APPROVAL: {
      label: 'Wartet auf Genehmigung',
      color: 'warning' as const,
      icon: <PendingIcon />,
    },
    REJECTED: { label: 'Abgelehnt', color: 'error' as const, icon: <RejectedIcon /> },
  };

  const { label, color, icon } = config[status];
  return <Chip label={label} color={color} size="small" icon={icon} />;
};

// ============================================================================
// Main Component
// ============================================================================

export const ImportsAdminPage: React.FC = () => {
  const theme = useTheme();
  const queryClient = useQueryClient();
  const [currentTab, setCurrentTab] = useState(0);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [selectedImportId, setSelectedImportId] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  // Queries
  const {
    data: stats,
    isLoading: statsLoading,
    refetch: refetchStats,
  } = useQuery({
    queryKey: ['importStats'],
    queryFn: fetchImportStats,
  });

  const { data: allImports, isLoading: importsLoading } = useQuery({
    queryKey: ['allImports'],
    queryFn: fetchAllImports,
    enabled: currentTab === 1,
  });

  const { data: pendingApprovals, isLoading: pendingLoading } = useQuery({
    queryKey: ['pendingApprovals'],
    queryFn: fetchPendingApprovals,
    enabled: currentTab === 0 || currentTab === 2,
  });

  // Mutations
  const approveMutation = useMutation({
    mutationFn: approveImport,
    onSuccess: () => {
      toast.success('Import genehmigt');
      queryClient.invalidateQueries({ queryKey: ['importStats'] });
      queryClient.invalidateQueries({ queryKey: ['pendingApprovals'] });
      queryClient.invalidateQueries({ queryKey: ['allImports'] });
    },
    onError: () => toast.error('Fehler beim Genehmigen'),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => rejectImport(id, reason),
    onSuccess: () => {
      toast.success('Import abgelehnt');
      setRejectDialogOpen(false);
      setRejectReason('');
      setSelectedImportId(null);
      queryClient.invalidateQueries({ queryKey: ['importStats'] });
      queryClient.invalidateQueries({ queryKey: ['pendingApprovals'] });
      queryClient.invalidateQueries({ queryKey: ['allImports'] });
    },
    onError: () => toast.error('Fehler beim Ablehnen'),
  });

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  const handleApprove = (importId: string) => {
    approveMutation.mutate(importId);
  };

  const handleRejectClick = (importId: string) => {
    setSelectedImportId(importId);
    setRejectDialogOpen(true);
  };

  const handleRejectConfirm = () => {
    if (selectedImportId && rejectReason.trim()) {
      rejectMutation.mutate({ id: selectedImportId, reason: rejectReason });
    }
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatFileSize = (bytes: number | null) => {
    if (!bytes) return '—';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <MainLayoutV2 maxWidth="full">
      <Box>
        {/* Header */}
        <Paper sx={{ p: 2, mb: 3 }}>
          <Grid container alignItems="center" spacing={2}>
            <Grid size="grow">
              <Typography
                variant="h4"
                component="h1"
                sx={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}
              >
                <UploadIcon sx={{ mr: 2, color: theme.palette.primary.main, fontSize: 32 }} />
                Import-Verwaltung
              </Typography>
              <Typography variant="subtitle1" color="text.secondary">
                Lead-Imports überwachen und genehmigen
              </Typography>
            </Grid>
            <Grid size="auto">
              <Tooltip title="Aktualisieren">
                <IconButton onClick={() => refetchStats()}>
                  <RefreshIcon />
                </IconButton>
              </Tooltip>
            </Grid>
          </Grid>
        </Paper>

        {/* Stats */}
        {statsLoading ? (
          <Grid container spacing={2} sx={{ mb: 3 }}>
            {[1, 2, 3, 4].map(i => (
              <Grid size={{ xs: 12, sm: 6, md: 3 }} key={i}>
                <Skeleton variant="rectangular" height={100} />
              </Grid>
            ))}
          </Grid>
        ) : stats ? (
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <StatCard
                title="Gesamt Imports"
                value={stats.totalImports}
                icon={<UploadIcon />}
                color={theme.palette.info.main}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <StatCard
                title="Importierte Leads"
                value={stats.totalImported}
                icon={<ApprovedIcon />}
                color={theme.palette.success.main}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <StatCard
                title="Übersprungen"
                value={stats.totalSkipped}
                icon={<PendingIcon />}
                color={theme.palette.warning.main}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <StatCard
                title="Fehler"
                value={stats.totalErrors}
                icon={<RejectedIcon />}
                color={theme.palette.error.main}
              />
            </Grid>
          </Grid>
        ) : null}

        {/* Pending Approvals Alert */}
        {stats && stats.pendingApprovals > 0 && (
          <Alert
            severity="warning"
            sx={{ mb: 2 }}
            action={
              <Button color="inherit" size="small" onClick={() => setCurrentTab(2)}>
                Anzeigen
              </Button>
            }
          >
            {stats.pendingApprovals} Import(s) warten auf Genehmigung (über 10% Duplikate)
          </Alert>
        )}

        {/* Main Content */}
        <Paper sx={{ width: '100%' }}>
          <Tabs
            value={currentTab}
            onChange={handleTabChange}
            indicatorColor="primary"
            textColor="primary"
            sx={{ borderBottom: 1, borderColor: 'divider' }}
          >
            <Tab label="Übersicht" />
            <Tab
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  Alle Imports
                  {stats && stats.totalImports > 0 && (
                    <Chip label={stats.totalImports} size="small" />
                  )}
                </Box>
              }
            />
            <Tab
              label={
                <Badge badgeContent={stats?.pendingApprovals || 0} color="warning">
                  Genehmigungen
                </Badge>
              }
            />
          </Tabs>

          <Box sx={{ p: 3 }}>
            {/* Tab 0: Übersicht */}
            <TabPanel value={currentTab} index={0}>
              <Alert severity="info" sx={{ mb: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Quota-System aktiv
                </Typography>
                <Typography variant="body2">
                  SALES: max. 100 Leads/Tag, 3 Imports/Tag | MANAGER: max. 200/5 | ADMIN: unbegrenzt
                </Typography>
              </Alert>

              {pendingApprovals && pendingApprovals.length > 0 && (
                <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
                  <Typography variant="h6" gutterBottom color="warning.main">
                    Wartende Genehmigungen ({pendingApprovals.length})
                  </Typography>
                  <TableContainer>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Datei</TableCell>
                          <TableCell>Benutzer</TableCell>
                          <TableCell>Duplikatrate</TableCell>
                          <TableCell>Zeilen</TableCell>
                          <TableCell>Aktionen</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {pendingApprovals.slice(0, 5).map(imp => (
                          <TableRow key={imp.id}>
                            <TableCell>{imp.fileName || '—'}</TableCell>
                            <TableCell>{imp.userId}</TableCell>
                            <TableCell>
                              <Chip
                                label={`${imp.duplicateRate?.toFixed(1) || 0}%`}
                                size="small"
                                color="warning"
                              />
                            </TableCell>
                            <TableCell>{imp.totalRows}</TableCell>
                            <TableCell>
                              <Tooltip title="Genehmigen">
                                <IconButton
                                  size="small"
                                  color="success"
                                  onClick={() => handleApprove(imp.id)}
                                  disabled={approveMutation.isPending}
                                >
                                  <CheckIcon />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="Ablehnen">
                                <IconButton
                                  size="small"
                                  color="error"
                                  onClick={() => handleRejectClick(imp.id)}
                                >
                                  <CloseIcon />
                                </IconButton>
                              </Tooltip>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Paper>
              )}
            </TabPanel>

            {/* Tab 1: Alle Imports */}
            <TabPanel value={currentTab} index={1}>
              {importsLoading ? (
                <Skeleton variant="rectangular" height={300} />
              ) : allImports && allImports.length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.50' }}>
                        <TableCell sx={{ fontWeight: 600 }}>Datei</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Benutzer</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Datum</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Zeilen</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Importiert</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Übersprungen</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Fehler</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {allImports.map(imp => (
                        <TableRow key={imp.id}>
                          <TableCell>
                            <Box>
                              <Typography variant="body2">{imp.fileName || '—'}</Typography>
                              <Typography variant="caption" color="text.secondary">
                                {formatFileSize(imp.fileSizeBytes)} · {imp.fileType || '—'}
                              </Typography>
                            </Box>
                          </TableCell>
                          <TableCell>{imp.userId}</TableCell>
                          <TableCell>{formatDate(imp.importedAt)}</TableCell>
                          <TableCell>{imp.totalRows}</TableCell>
                          <TableCell>
                            <Chip
                              label={imp.importedCount}
                              size="small"
                              color="success"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={imp.skippedCount}
                              size="small"
                              color="warning"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={imp.errorCount}
                              size="small"
                              color="error"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>
                            <StatusChip status={imp.status} />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Alert severity="info">Keine Imports vorhanden.</Alert>
              )}
            </TabPanel>

            {/* Tab 2: Genehmigungen */}
            <TabPanel value={currentTab} index={2}>
              {pendingLoading ? (
                <Skeleton variant="rectangular" height={300} />
              ) : pendingApprovals && pendingApprovals.length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.50' }}>
                        <TableCell sx={{ fontWeight: 600 }}>Datei</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Benutzer</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Quelle</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Zeilen</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Duplikatrate</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Aktionen</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {pendingApprovals.map(imp => (
                        <TableRow key={imp.id}>
                          <TableCell>
                            <Box>
                              <Typography variant="body2">{imp.fileName || '—'}</Typography>
                              <Typography variant="caption" color="text.secondary">
                                {formatDate(imp.importedAt)}
                              </Typography>
                            </Box>
                          </TableCell>
                          <TableCell>{imp.userId}</TableCell>
                          <TableCell>{imp.source || '—'}</TableCell>
                          <TableCell>{imp.totalRows}</TableCell>
                          <TableCell>
                            <Chip
                              label={`${imp.duplicateRate?.toFixed(1) || 0}%`}
                              size="small"
                              color="warning"
                            />
                          </TableCell>
                          <TableCell>
                            <Button
                              size="small"
                              variant="contained"
                              color="success"
                              startIcon={<CheckIcon />}
                              onClick={() => handleApprove(imp.id)}
                              disabled={approveMutation.isPending}
                              sx={{ mr: 1 }}
                            >
                              Genehmigen
                            </Button>
                            <Button
                              size="small"
                              variant="outlined"
                              color="error"
                              startIcon={<CloseIcon />}
                              onClick={() => handleRejectClick(imp.id)}
                            >
                              Ablehnen
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Alert severity="success">Keine wartenden Genehmigungen.</Alert>
              )}
            </TabPanel>
          </Box>
        </Paper>

        {/* Reject Dialog */}
        <Dialog
          open={rejectDialogOpen}
          onClose={() => setRejectDialogOpen(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>Import ablehnen</DialogTitle>
          <DialogContent>
            <TextField
              autoFocus
              margin="dense"
              label="Ablehnungsgrund"
              fullWidth
              multiline
              rows={3}
              value={rejectReason}
              onChange={e => setRejectReason(e.target.value)}
              placeholder="Bitte geben Sie einen Grund für die Ablehnung an..."
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setRejectDialogOpen(false)}>Abbrechen</Button>
            <Button
              onClick={handleRejectConfirm}
              color="error"
              variant="contained"
              disabled={!rejectReason.trim() || rejectMutation.isPending}
            >
              Ablehnen
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </MainLayoutV2>
  );
};

export default ImportsAdminPage;
