import React, { useState } from 'react';
import { Grid } from '@mui/material';
import {
  Box,
  Paper,
  Typography,
  Tabs,
  Tab,
  Alert,
  Button,
  IconButton,
  Chip,
  Tooltip,
  Badge,
  Skeleton,
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Security as SecurityIcon,
  Assessment as AssessmentIcon,
  Warning as WarningIcon,
  People as PeopleIcon,
  Timeline as TimelineIcon,
  // Download as DownloadIcon,
  Refresh as RefreshIcon,
  FilterList as FilterIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';
import { AuditTrailTable } from '@/features/audit/components/AuditTrailTable';
import { AuditDashboard } from '@/features/audit/admin/AuditDashboard';
import { CompliancePanel } from '@/features/audit/admin/CompliancePanel';
import { UserActivityPanel } from '@/features/audit/admin/UserActivityPanel';
import { auditApi } from '@/features/audit/services/auditApi';
import { UniversalExportButton } from '@/components/export/UniversalExportButton';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <Box
      role="tabpanel"
      hidden={value !== index}
      id={`audit-tabpanel-${index}`}
      aria-labelledby={`audit-tab-${index}`}
    >
      {value === index && <Box>{children}</Box>}
    </Box>
  );
};

export const AuditAdminPage: React.FC = () => {
  const [currentTab, setCurrentTab] = useState(0);
  const [dateRange, setDateRange] = useState({
    from: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    to: new Date(),
  });
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [showFilters, setShowFilters] = useState(false);

  // Fetch dashboard metrics
  const { data: metrics, isLoading: metricsLoading } = useQuery({
    queryKey: ['auditMetrics', dateRange],
    queryFn: () => auditApi.getDashboardMetrics(),
    refetchInterval: autoRefresh ? 30000 : false,
  });

  // Fetch compliance alerts
  const { data: alerts, isLoading: alertsLoading } = useQuery({
    queryKey: ['complianceAlerts'],
    queryFn: () => auditApi.getComplianceAlerts(),
    refetchInterval: autoRefresh ? 60000 : false,
  });

  // Fetch critical events
  const { data: criticalEvents } = useQuery({
    queryKey: ['criticalEvents'],
    queryFn: () => auditApi.getCriticalEvents(5),
  });

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  const getComplianceColor = (score: number) => {
    if (score >= 90) return 'success';
    if (score >= 70) return 'warning';
    return 'error';
  };

  return (
    <MainLayoutV2>
      <Box>
        {/* Header */}
        <Paper sx={{ p: 2, mb: 3 }}>
          <Grid container alignItems="center" spacing={2}>
            <Grid size="grow">
              <Typography
                variant="h4"
                component="h1"
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  fontFamily: 'Antonio, sans-serif',
                  fontWeight: 'bold',
                }}
              >
                <SecurityIcon sx={{ mr: 2, color: '#004F7B', fontSize: 32 }} />
                Audit Dashboard
              </Typography>
              <Typography
                variant="subtitle1"
                color="text.secondary"
                sx={{ fontFamily: 'Poppins, sans-serif' }}
              >
                Compliance Monitoring & Security Analysis
              </Typography>
            </Grid>

            <Grid size="auto">
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                {/* Compliance Score */}
                {metrics && (
                  <Chip
                    label={`Compliance: ${metrics.retentionCompliance}%`}
                    color={getComplianceColor(metrics.retentionCompliance)}
                    sx={{ fontWeight: 'bold' }}
                  />
                )}

                {/* Alert Badge */}
                <Badge badgeContent={alerts?.length || 0} color="error">
                  <IconButton onClick={() => setCurrentTab(3)}>
                    <WarningIcon />
                  </IconButton>
                </Badge>

                {/* Auto Refresh Toggle */}
                <Tooltip title={autoRefresh ? 'Auto-Refresh aktiv' : 'Auto-Refresh inaktiv'}>
                  <IconButton
                    onClick={() => setAutoRefresh(!autoRefresh)}
                    sx={{ color: autoRefresh ? '#94C456' : 'inherit' }}
                  >
                    <RefreshIcon />
                  </IconButton>
                </Tooltip>

                {/* Filter Toggle */}
                <IconButton onClick={() => setShowFilters(!showFilters)}>
                  <FilterIcon />
                </IconButton>

                {/* Export Button using Universal Export Framework */}
                <UniversalExportButton
                  entity="audit"
                  queryParams={{
                    from: dateRange.from.toISOString().split('T')[0],
                    to: dateRange.to.toISOString().split('T')[0],
                  }}
                  buttonLabel="Export"
                  onExportComplete={format => {
                    console.log(`Audit export completed: ${format}`);
                  }}
                />
              </Box>
            </Grid>
          </Grid>
        </Paper>

        {/* Compliance Alert Bar */}
        {metrics && metrics.retentionCompliance < 80 && (
          <Alert
            severity="warning"
            sx={{ mb: 2 }}
            action={
              <Button color="inherit" size="small" onClick={() => setCurrentTab(3)}>
                Details
              </Button>
            }
          >
            ⚠️ Compliance Score unter 80%: {metrics.retentionCompliance}% - Bitte prüfen Sie die
            identifizierten Probleme im Compliance-Tab
          </Alert>
        )}

        {/* Critical Events Alert */}
        {criticalEvents && criticalEvents.length > 0 && (
          <Alert
            severity="error"
            sx={{ mb: 2 }}
            action={
              <Button color="inherit" size="small" onClick={() => setCurrentTab(2)}>
                Anzeigen
              </Button>
            }
          >
            🚨 {criticalEvents.length} kritische Ereignisse in den letzten 24 Stunden erkannt
          </Alert>
        )}

        {/* Main Content */}
        <Paper sx={{ width: '100%' }}>
          <Tabs
            value={currentTab}
            onChange={handleTabChange}
            indicatorColor="primary"
            textColor="primary"
            sx={{
              borderBottom: 1,
              borderColor: 'divider',
              '& .MuiTab-root': {
                fontFamily: 'Poppins, sans-serif',
                fontWeight: 500,
              },
            }}
          >
            <Tab label="Übersicht" icon={<DashboardIcon />} iconPosition="start" />
            <Tab label="Audit Trail" icon={<TimelineIcon />} iconPosition="start" />
            <Tab
              label={
                <Badge badgeContent={criticalEvents?.length || 0} color="error">
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <WarningIcon sx={{ mr: 1 }} />
                    Sicherheit
                  </Box>
                </Badge>
              }
            />
            <Tab label="Compliance" icon={<AssessmentIcon />} iconPosition="start" />
            <Tab label="Benutzer" icon={<PeopleIcon />} iconPosition="start" />
          </Tabs>

          <Box sx={{ p: 3 }}>
            {/* Tab 0: Overview */}
            <TabPanel value={currentTab} index={0}>
              {metricsLoading ? (
                <Grid container spacing={3}>
                  {[1, 2, 3, 4].map(i => (
                    <Grid size={{ xs: 12, sm: 6, md: 3 }} key={i}>
                      <Skeleton variant="rectangular" height={120} />
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <AuditDashboard
                  metrics={metrics}
                  dateRange={dateRange}
                  onDateRangeChange={setDateRange}
                />
              )}
            </TabPanel>

            {/* Tab 1: Audit Trail */}
            <TabPanel value={currentTab} index={1}>
              <AuditTrailTable
                filters={{
                  dateRange: {
                    from: dateRange.from,
                    to: dateRange.to,
                  },
                }}
              />
            </TabPanel>

            {/* Tab 2: Security */}
            <TabPanel value={currentTab} index={2}>
              <Grid container spacing={3}>
                <Grid size={{ xs: 12 }}>
                  <Typography variant="h5" gutterBottom sx={{ fontFamily: 'Antonio, sans-serif' }}>
                    Kritische Ereignisse
                  </Typography>
                  {criticalEvents && criticalEvents.length > 0 ? (
                    <Paper sx={{ p: 2 }}>
                      {criticalEvents.map(event => (
                        <Alert key={event.id} severity="error" sx={{ mb: 1 }}>
                          <Typography variant="subtitle2">
                            {event.userName} - {event.action}
                          </Typography>
                          <Typography variant="caption">
                            {new Date(event.occurredAt).toLocaleString('de-DE')}
                          </Typography>
                        </Alert>
                      ))}
                    </Paper>
                  ) : (
                    <Alert severity="success">
                      Keine kritischen Ereignisse in den letzten 24 Stunden
                    </Alert>
                  )}
                </Grid>
              </Grid>
            </TabPanel>

            {/* Tab 3: Compliance */}
            <TabPanel value={currentTab} index={3}>
              {alertsLoading ? (
                <Skeleton variant="rectangular" height={400} />
              ) : (
                <CompliancePanel alerts={alerts} metrics={metrics} dateRange={dateRange} />
              )}
            </TabPanel>

            {/* Tab 4: User Activity */}
            <TabPanel value={currentTab} index={4}>
              <UserActivityPanel dateRange={dateRange} />
            </TabPanel>
          </Box>
        </Paper>
      </Box>
    </MainLayoutV2>
  );
};
