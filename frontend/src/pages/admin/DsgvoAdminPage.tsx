/**
 * DSGVO Admin Page - Sprint 2.1.8 Phase 3
 *
 * Admin-Dashboard für DSGVO-Compliance:
 * - Übersicht: Statistiken (Löschungen, Anfragen, gesperrte Kontakte)
 * - Löschprotokolle: Art. 17 DSGVO-Löschungen
 * - Datenexport-Anfragen: Art. 15 Auskunftsrecht
 * - Gelöschte Leads: Anonymisierte Lead-Daten
 *
 * @module DsgvoAdminPage
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
} from '@mui/material';
import {
  Security as SecurityIcon,
  Delete as DeleteIcon,
  Description as DescriptionIcon,
  Block as BlockIcon,
  Refresh as RefreshIcon,
  Download as DownloadIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';

// ============================================================================
// Types
// ============================================================================

interface GdprStats {
  totalDeletions: number;
  totalDataRequests: number;
  pendingRequests: number;
  deletedLeads: number;
  blockedContacts: number;
}

interface GdprDeletionLog {
  id: number;
  entityType: string;
  entityId: number;
  deletedBy: string;
  deletedAt: string;
  deletionReason: string;
  originalDataHash: string;
}

interface GdprDataRequest {
  id: number;
  entityType: string;
  entityId: number;
  requestedBy: string;
  requestedAt: string;
  pdfGenerated: boolean;
  pdfGeneratedAt: string | null;
}

interface DeletedLead {
  id: number;
  companyName: string;
  gdprDeletedAt: string;
  gdprDeletedBy: string;
  gdprDeletionReason: string;
}

// ============================================================================
// API Functions
// ============================================================================

const API_BASE = '/api/admin/gdpr';

async function fetchGdprStats(): Promise<GdprStats> {
  const response = await fetch(`${API_BASE}/stats`);
  if (!response.ok) throw new Error('Fehler beim Laden der DSGVO-Statistiken');
  return response.json();
}

async function fetchDeletionLogs(): Promise<GdprDeletionLog[]> {
  const response = await fetch(`${API_BASE}/deletions?limit=50`);
  if (!response.ok) throw new Error('Fehler beim Laden der Löschprotokolle');
  return response.json();
}

async function fetchDataRequests(): Promise<GdprDataRequest[]> {
  const response = await fetch(`${API_BASE}/data-requests?limit=50`);
  if (!response.ok) throw new Error('Fehler beim Laden der Datenexport-Anfragen');
  return response.json();
}

async function fetchDeletedLeads(): Promise<DeletedLead[]> {
  const response = await fetch(`${API_BASE}/deleted-leads?limit=50`);
  if (!response.ok) throw new Error('Fehler beim Laden der gelöschten Leads');
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
  <Box role="tabpanel" hidden={value !== index} id={`gdpr-tabpanel-${index}`}>
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

// ============================================================================
// Main Component
// ============================================================================

export const DsgvoAdminPage: React.FC = () => {
  const theme = useTheme();
  const [currentTab, setCurrentTab] = useState(0);

  // Queries
  const {
    data: stats,
    isLoading: statsLoading,
    refetch: refetchStats,
  } = useQuery({
    queryKey: ['gdprStats'],
    queryFn: fetchGdprStats,
  });

  const { data: deletionLogs, isLoading: deletionsLoading } = useQuery({
    queryKey: ['gdprDeletions'],
    queryFn: fetchDeletionLogs,
    enabled: currentTab === 1,
  });

  const { data: dataRequests, isLoading: requestsLoading } = useQuery({
    queryKey: ['gdprDataRequests'],
    queryFn: fetchDataRequests,
    enabled: currentTab === 2,
  });

  const { data: deletedLeads, isLoading: leadsLoading } = useQuery({
    queryKey: ['gdprDeletedLeads'],
    queryFn: fetchDeletedLeads,
    enabled: currentTab === 3,
  });

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
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
                <SecurityIcon sx={{ mr: 2, color: theme.palette.primary.main, fontSize: 32 }} />
                DSGVO-Verwaltung
              </Typography>
              <Typography variant="subtitle1" color="text.secondary">
                Datenschutz-Compliance nach Art. 15, 17, 7.3 DSGVO
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
                title="DSGVO-Löschungen"
                value={stats.totalDeletions}
                icon={<DeleteIcon />}
                color={theme.palette.error.main}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <StatCard
                title="Datenexport-Anfragen"
                value={stats.totalDataRequests}
                icon={<DescriptionIcon />}
                color={theme.palette.info.main}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <StatCard
                title="Gesperrte Kontakte"
                value={stats.blockedContacts}
                icon={<BlockIcon />}
                color={theme.palette.warning.main}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <StatCard
                title="Gelöschte Leads"
                value={stats.deletedLeads}
                icon={<SecurityIcon />}
                color={theme.palette.secondary.main}
              />
            </Grid>
          </Grid>
        ) : null}

        {/* Pending Requests Alert */}
        {stats && stats.pendingRequests > 0 && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            {stats.pendingRequests} Datenexport-Anfrage(n) noch offen
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
                  Löschprotokolle
                  {stats && stats.totalDeletions > 0 && (
                    <Chip label={stats.totalDeletions} size="small" color="error" />
                  )}
                </Box>
              }
            />
            <Tab
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  Datenexport-Anfragen
                  {stats && stats.totalDataRequests > 0 && (
                    <Chip label={stats.totalDataRequests} size="small" color="info" />
                  )}
                </Box>
              }
            />
            <Tab label="Gelöschte Leads" />
          </Tabs>

          <Box sx={{ p: 3 }}>
            {/* Tab 0: Übersicht */}
            <TabPanel value={currentTab} index={0}>
              <Alert severity="info" sx={{ mb: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  DSGVO-Compliance Status
                </Typography>
                <Typography variant="body2">
                  Dieses Dashboard zeigt alle datenschutzrelevanten Aktivitäten. Art. 15 (Auskunft),
                  Art. 17 (Löschung) und Art. 7.3 (Einwilligungswiderruf) sind vollständig
                  implementiert.
                </Typography>
              </Alert>

              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <Paper variant="outlined" sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      Art. 17 - Löschrecht
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Betroffene können die Löschung ihrer Daten verlangen. Die Löschung erfolgt als
                      Soft-Delete mit PII-Anonymisierung.
                    </Typography>
                  </Paper>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <Paper variant="outlined" sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      Art. 15 - Auskunftsrecht
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Betroffene können Auskunft über ihre gespeicherten Daten verlangen. Der Export
                      erfolgt als PDF.
                    </Typography>
                  </Paper>
                </Grid>
              </Grid>
            </TabPanel>

            {/* Tab 1: Löschprotokolle */}
            <TabPanel value={currentTab} index={1}>
              {deletionsLoading ? (
                <Skeleton variant="rectangular" height={300} />
              ) : deletionLogs && deletionLogs.length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.50' }}>
                        <TableCell sx={{ fontWeight: 600 }}>ID</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Entity</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Gelöscht von</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Gelöscht am</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Grund</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {deletionLogs.map(log => (
                        <TableRow key={log.id}>
                          <TableCell>{log.id}</TableCell>
                          <TableCell>
                            <Chip
                              label={`${log.entityType} #${log.entityId}`}
                              size="small"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>{log.deletedBy}</TableCell>
                          <TableCell>{formatDate(log.deletedAt)}</TableCell>
                          <TableCell
                            sx={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis' }}
                          >
                            {log.deletionReason}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Alert severity="success">Keine DSGVO-Löschungen vorhanden.</Alert>
              )}
            </TabPanel>

            {/* Tab 2: Datenexport-Anfragen */}
            <TabPanel value={currentTab} index={2}>
              {requestsLoading ? (
                <Skeleton variant="rectangular" height={300} />
              ) : dataRequests && dataRequests.length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.50' }}>
                        <TableCell sx={{ fontWeight: 600 }}>ID</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Entity</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Angefragt von</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Angefragt am</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>PDF Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {dataRequests.map(req => (
                        <TableRow key={req.id}>
                          <TableCell>{req.id}</TableCell>
                          <TableCell>
                            <Chip
                              label={`${req.entityType} #${req.entityId}`}
                              size="small"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>{req.requestedBy}</TableCell>
                          <TableCell>{formatDate(req.requestedAt)}</TableCell>
                          <TableCell>
                            {req.pdfGenerated ? (
                              <Chip
                                label={`Generiert ${req.pdfGeneratedAt ? formatDate(req.pdfGeneratedAt) : ''}`}
                                size="small"
                                color="success"
                                icon={<DownloadIcon />}
                              />
                            ) : (
                              <Chip label="Ausstehend" size="small" color="warning" />
                            )}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Alert severity="info">Keine Datenexport-Anfragen vorhanden.</Alert>
              )}
            </TabPanel>

            {/* Tab 3: Gelöschte Leads */}
            <TabPanel value={currentTab} index={3}>
              {leadsLoading ? (
                <Skeleton variant="rectangular" height={300} />
              ) : deletedLeads && deletedLeads.length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'grey.50' }}>
                        <TableCell sx={{ fontWeight: 600 }}>ID</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Anonymisierter Name</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Gelöscht von</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Gelöscht am</TableCell>
                        <TableCell sx={{ fontWeight: 600 }}>Grund</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {deletedLeads.map(lead => (
                        <TableRow key={lead.id}>
                          <TableCell>{lead.id}</TableCell>
                          <TableCell>
                            <Chip
                              label={lead.companyName}
                              size="small"
                              color="error"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>{lead.gdprDeletedBy}</TableCell>
                          <TableCell>
                            {lead.gdprDeletedAt ? formatDate(lead.gdprDeletedAt) : '—'}
                          </TableCell>
                          <TableCell
                            sx={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis' }}
                          >
                            {lead.gdprDeletionReason || '—'}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Alert severity="success">Keine DSGVO-gelöschten Leads vorhanden.</Alert>
              )}
            </TabPanel>
          </Box>
        </Paper>
      </Box>
    </MainLayoutV2>
  );
};

export default DsgvoAdminPage;
