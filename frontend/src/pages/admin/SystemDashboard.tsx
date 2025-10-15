import {
  Box,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Paper,
  Breadcrumbs,
  Link,
  Chip,
  LinearProgress,
  useTheme,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../../components/layout/MainLayoutV2';

// Icons
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ApiIcon from '@mui/icons-material/Api';
import StorageIcon from '@mui/icons-material/Storage';
import SpeedIcon from '@mui/icons-material/Speed';
import BackupIcon from '@mui/icons-material/Backup';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningIcon from '@mui/icons-material/Warning';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';

interface SystemToolCard {
  title: string;
  description: string;
  icon: React.ReactNode;
  path: string;
  status: 'online' | 'warning' | 'offline';
  metrics?: {
    label: string;
    value: string;
    trend?: 'up' | 'down' | 'stable';
  }[];
  lastUpdated?: string;
}

export function SystemDashboard() {
  const navigate = useNavigate();
  const theme = useTheme();

  const systemTools: SystemToolCard[] = [
    {
      title: 'API Status',
      description: 'Service-Health Monitoring und Endpoint-Überwachung in Echtzeit',
      icon: <ApiIcon sx={{ fontSize: 48, color: theme.palette.primary.main }} />,
      path: '/admin/system/api-test',
      status: 'online',
      metrics: [
        { label: 'Uptime', value: '99.98%', trend: 'stable' },
        { label: 'Response Time', value: '142ms', trend: 'down' },
        { label: 'Requests/Min', value: '1,247', trend: 'up' },
      ],
      lastUpdated: 'Live',
    },
    {
      title: 'System-Logs',
      description: 'Echtzeit-Überwachung aller Systemereignisse und Fehler',
      icon: <StorageIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/admin/system/logs',
      status: 'online',
      metrics: [
        { label: 'Events heute', value: '12,456' },
        { label: 'Errors', value: '3', trend: 'down' },
        { label: 'Warnings', value: '27', trend: 'stable' },
      ],
      lastUpdated: 'vor 2 Min',
    },
    {
      title: 'Performance',
      description: 'Detaillierte Performance-Metriken und Optimierungsvorschläge',
      icon: <SpeedIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/admin/system/performance',
      status: 'warning',
      metrics: [
        { label: 'CPU', value: '67%', trend: 'up' },
        { label: 'Memory', value: '4.2GB / 8GB' },
        { label: 'Disk I/O', value: '124 MB/s' },
      ],
      lastUpdated: 'vor 5 Min',
    },
    {
      title: 'Backup & Recovery',
      description: 'Datensicherung, Snapshots und Disaster Recovery Management',
      icon: <BackupIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/admin/system/backup',
      status: 'online',
      metrics: [
        { label: 'Letztes Backup', value: 'vor 2h' },
        { label: 'Backup-Größe', value: '3.7 GB' },
        { label: 'Recovery Time', value: '< 15 Min' },
      ],
      lastUpdated: 'vor 2 Std',
    },
  ];

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'online':
        return <CheckCircleIcon sx={{ fontSize: 20, color: theme.palette.primary.main }} />;
      case 'warning':
        return <WarningIcon sx={{ fontSize: 20, color: theme.palette.warning.main }} />;
      default:
        return null;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'online':
        return theme.palette.primary.main;
      case 'warning':
        return theme.palette.warning.main;
      case 'offline':
        return theme.palette.error.main;
      default:
        return theme.palette.grey[600];
    }
  };

  return (
    <MainLayoutV2>
      <Box sx={{ py: 4 }}>
        {/* Breadcrumbs */}
        <Breadcrumbs separator={<NavigateNextIcon fontSize="small" />} sx={{ mb: 3 }}>
          <Link
            component="button"
            variant="body1"
            onClick={() => navigate('/admin')}
            sx={{
              textDecoration: 'none',
              color: theme.palette.secondary.main,
              '&:hover': { textDecoration: 'underline' },
            }}
          >
            Administration
          </Link>
          <Typography color="text.primary">System</Typography>
        </Breadcrumbs>

        {/* Header */}
        <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate('/admin')}
            sx={{ color: theme.palette.secondary.main }}
          >
            Zurück
          </Button>
          <Box sx={{ flex: 1 }}>
            <Typography
              variant="h3"
              sx={{
                mb: 1,
                color: theme.palette.secondary.main,
              }}
            >
              System Management
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Technische Systemverwaltung, Monitoring und Wartung
            </Typography>
          </Box>
        </Box>

        {/* System Health Overview */}
        <Paper sx={{ p: 3, mb: 4, backgroundColor: theme.palette.grey[50] }}>
          <Typography variant="h6" sx={{ mb: 2, color: theme.palette.secondary.main }}>
            System Health Overview
          </Typography>
          <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Overall Status
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CheckCircleIcon sx={{ color: theme.palette.primary.main }} />
                <Typography variant="h6" sx={{ color: theme.palette.primary.main }}>
                  Operational
                </Typography>
              </Box>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">
                System Uptime
              </Typography>
              <Typography variant="h6">15 Tage, 7:23:45</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Load Average
              </Typography>
              <Typography variant="h6">2.34, 2.12, 1.98</Typography>
            </Box>
            <Box sx={{ flex: 1, minWidth: 200 }}>
              <Typography variant="caption" color="text.secondary">
                Resource Usage
              </Typography>
              <Box sx={{ mt: 1 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                  <Typography variant="caption">CPU</Typography>
                  <Typography variant="caption">67%</Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={67}
                  sx={{
                    height: 6,
                    borderRadius: 1,
                    backgroundColor: theme.palette.grey[300],
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: theme.palette.primary.main,
                    },
                  }}
                />
              </Box>
            </Box>
          </Box>
        </Paper>

        {/* Tool Cards */}
        <Grid container spacing={3}>
          {systemTools.map(tool => (
            <Grid key={tool.title} size={{ xs: 12, md: 6 }}>
              <Card
                sx={{
                  height: '100%',
                  transition: 'all 0.2s',
                  cursor: 'pointer',
                  '&:hover': {
                    boxShadow: 4,
                    transform: 'translateY(-2px)',
                  },
                }}
                onClick={() => navigate(tool.path)}
              >
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                    {tool.icon}
                    <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
                      {getStatusIcon(tool.status)}
                      <Chip
                        label={
                          tool.status === 'online'
                            ? 'Online'
                            : tool.status === 'warning'
                              ? 'Warnung'
                              : 'Offline'
                        }
                        size="small"
                        sx={{
                          backgroundColor: getStatusColor(tool.status),
                          color: 'white',
                        }}
                      />
                    </Box>
                  </Box>

                  <Typography
                    variant="h5"
                    sx={{
                      mb: 1,
                      color: theme.palette.secondary.main,
                    }}
                  >
                    {tool.title}
                  </Typography>

                  <Typography variant="body2" color="text.secondary" sx={{ mb: 3, minHeight: 40 }}>
                    {tool.description}
                  </Typography>

                  {tool.metrics && (
                    <Box sx={{ mb: 2 }}>
                      <Grid container spacing={2}>
                        {tool.metrics.map(metric => (
                          <Grid key={metric.label} size={4}>
                            <Typography variant="caption" color="text.secondary" display="block">
                              {metric.label}
                            </Typography>
                            <Typography
                              variant="body1"
                              sx={{
                                fontWeight: 'medium',
                                color:
                                  metric.trend === 'up'
                                    ? theme.palette.primary.main
                                    : metric.trend === 'down'
                                      ? theme.palette.error.main
                                      : 'inherit',
                              }}
                            >
                              {metric.value}
                            </Typography>
                          </Grid>
                        ))}
                      </Grid>
                    </Box>
                  )}

                  <Typography variant="caption" color="text.secondary">
                    Aktualisiert: {tool.lastUpdated}
                  </Typography>
                </CardContent>

                <CardActions sx={{ px: 2, pb: 2 }}>
                  <Button
                    fullWidth
                    variant="contained"
                    sx={{
                      backgroundColor: theme.palette.primary.main,
                      '&:hover': { backgroundColor: theme.palette.primary.dark },
                    }}
                  >
                    Dashboard öffnen
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
    </MainLayoutV2>
  );
}
