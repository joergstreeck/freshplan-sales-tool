import {
  Box,
  Container,
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

  const systemTools: SystemToolCard[] = [
    {
      title: 'API Status',
      description: 'Service-Health Monitoring und Endpoint-Überwachung in Echtzeit',
      icon: <ApiIcon sx={{ fontSize: 48, color: '#94C456' }} />,
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
      icon: <StorageIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
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
      icon: <SpeedIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
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
      icon: <BackupIcon sx={{ fontSize: 48, color: '#004F7B' }} />,
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
        return <CheckCircleIcon sx={{ fontSize: 20, color: '#94C456' }} />;
      case 'warning':
        return <WarningIcon sx={{ fontSize: 20, color: '#FFA726' }} />;
      default:
        return null;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'online':
        return '#94C456';
      case 'warning':
        return '#FFA726';
      case 'offline':
        return '#EF5350';
      default:
        return '#757575';
    }
  };

  return (
    <MainLayoutV2>
      <Container maxWidth="xl" sx={{ py: 4 }}>
        {/* Breadcrumbs */}
        <Breadcrumbs
          separator={<NavigateNextIcon fontSize="small" />}
          sx={{ mb: 3 }}
        >
          <Link
            component="button"
            variant="body1"
            onClick={() => navigate('/admin')}
            sx={{
              textDecoration: 'none',
              color: '#004F7B',
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
            sx={{ color: '#004F7B' }}
          >
            Zurück
          </Button>
          <Box sx={{ flex: 1 }}>
            <Typography
              variant="h3"
              sx={{
                mb: 1,
                fontFamily: 'Antonio, sans-serif',
                fontWeight: 'bold',
                color: '#004F7B',
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
        <Paper sx={{ p: 3, mb: 4, backgroundColor: '#f8f9fa' }}>
          <Typography
            variant="h6"
            sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
          >
            System Health Overview
          </Typography>
          <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Overall Status
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CheckCircleIcon sx={{ color: '#94C456' }} />
                <Typography variant="h6" sx={{ color: '#94C456' }}>
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
                    backgroundColor: '#e0e0e0',
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: '#94C456',
                    },
                  }}
                />
              </Box>
            </Box>
          </Box>
        </Paper>

        {/* Tool Cards */}
        <Grid container spacing={3}>
          {systemTools.map((tool) => (
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
                        label={tool.status === 'online' ? 'Online' : tool.status === 'warning' ? 'Warning' : 'Offline'}
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
                      fontFamily: 'Antonio, sans-serif',
                      color: '#004F7B',
                    }}
                  >
                    {tool.title}
                  </Typography>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 3, minHeight: 40 }}
                  >
                    {tool.description}
                  </Typography>

                  {tool.metrics && (
                    <Box sx={{ mb: 2 }}>
                      <Grid container spacing={2}>
                        {tool.metrics.map((metric) => (
                          <Grid key={metric.label} size={4}>
                            <Typography
                              variant="caption"
                              color="text.secondary"
                              display="block"
                            >
                              {metric.label}
                            </Typography>
                            <Typography
                              variant="body1"
                              sx={{
                                fontWeight: 'medium',
                                color: metric.trend === 'up' ? '#94C456' : metric.trend === 'down' ? '#EF5350' : 'inherit',
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
                      backgroundColor: '#94C456',
                      '&:hover': { backgroundColor: '#7BA347' },
                    }}
                  >
                    Dashboard öffnen
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>
    </MainLayoutV2>
  );
}