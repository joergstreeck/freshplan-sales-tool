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
  Alert,
  Stack,
  useTheme,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../../components/layout/MainLayoutV2';

// Icons
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import InventoryIcon from '@mui/icons-material/Inventory';
import EmailIcon from '@mui/icons-material/Email';
import PaymentIcon from '@mui/icons-material/Payment';
import WebhookIcon from '@mui/icons-material/Webhook';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningIcon from '@mui/icons-material/Warning';
import ErrorIcon from '@mui/icons-material/Error';
import SyncIcon from '@mui/icons-material/Sync';

interface IntegrationCard {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  path: string;
  status: 'connected' | 'disconnected' | 'warning' | 'syncing';
  provider?: string;
  lastSync?: string;
  dataPoints?: number;
  config?: {
    apiKey?: boolean;
    webhookUrl?: string;
    credentials?: boolean;
  };
}

export function IntegrationsDashboard() {
  const navigate = useNavigate();
  const theme = useTheme();

  const integrations: IntegrationCard[] = [
    {
      id: 'ki',
      title: 'KI-Anbindungen',
      description: 'ChatGPT, Claude und andere KI-Services für intelligente Automatisierung',
      icon: <SmartToyIcon sx={{ fontSize: 48, color: theme.palette.primary.main }} />,
      path: '/admin/integrationen/ki',
      status: 'connected',
      provider: 'OpenAI GPT-4',
      lastSync: 'vor 5 Min',
      dataPoints: 15420,
      config: {
        apiKey: true,
      },
    },
    {
      id: 'xentral',
      title: 'Xentral ERP',
      description: 'Warenwirtschaft, Buchhaltung und Auftragsabwicklung',
      icon: <InventoryIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/admin/integrationen/xentral',
      status: 'connected',
      provider: 'Xentral Cloud',
      lastSync: 'vor 2 Std',
      dataPoints: 8934,
      config: {
        apiKey: true,
        webhookUrl: 'https://api.freshplan.de/webhooks/xentral',
      },
    },
    {
      id: 'email',
      title: 'E-Mail Services',
      description: 'SMTP, Microsoft 365 und Gmail Integration für Benachrichtigungen',
      icon: <EmailIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/admin/integrationen/email',
      status: 'warning',
      provider: 'Microsoft 365',
      lastSync: 'vor 1 Tag',
      config: {
        credentials: true,
      },
    },
    {
      id: 'payment',
      title: 'Payment Provider',
      description: 'Stripe, PayPal und andere Zahlungsdienstleister',
      icon: <PaymentIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/admin/integrationen/payment',
      status: 'disconnected',
      provider: 'Nicht konfiguriert',
    },
    {
      id: 'webhooks',
      title: 'Webhooks',
      description: 'Event-basierte Kommunikation mit externen Systemen',
      icon: <WebhookIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/admin/integrationen/webhooks',
      status: 'syncing',
      provider: 'Webhook Manager',
      lastSync: 'Läuft...',
      dataPoints: 342,
    },
    {
      id: 'new',
      title: 'Neue Integration',
      description: 'Weitere Services und APIs anbinden',
      icon: <AddCircleIcon sx={{ fontSize: 48, color: theme.palette.primary.main }} />,
      path: '/admin/integrationen/neu',
      status: 'disconnected',
    },
  ];

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'connected':
        return <CheckCircleIcon sx={{ fontSize: 20, color: theme.palette.primary.main }} />;
      case 'warning':
        return <WarningIcon sx={{ fontSize: 20, color: theme.palette.warning.main }} />;
      case 'disconnected':
        return <ErrorIcon sx={{ fontSize: 20, color: theme.palette.error.main }} />;
      case 'syncing':
        return (
          <SyncIcon sx={{ fontSize: 20, color: theme.palette.info.main, animation: 'spin 2s linear infinite' }} />
        );
      default:
        return null;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'connected':
        return theme.palette.primary.main;
      case 'warning':
        return theme.palette.warning.main;
      case 'disconnected':
        return theme.palette.error.main;
      case 'syncing':
        return theme.palette.info.main;
      default:
        return theme.palette.grey[600];
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'connected':
        return 'Verbunden';
      case 'warning':
        return 'Warnung';
      case 'disconnected':
        return 'Getrennt';
      case 'syncing':
        return 'Synchronisiert';
      default:
        return status;
    }
  };

  const connectedCount = integrations.filter(i => i.status === 'connected').length;
  const totalCount = integrations.filter(i => i.id !== 'new').length;

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
          <Typography color="text.primary">Integrationen</Typography>
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
                fontFamily: 'Antonio, sans-serif',
                fontWeight: 'bold',
                color: theme.palette.secondary.main,
              }}
            >
              Integrationen Management
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Externe Services, APIs und Schnittstellen verwalten
            </Typography>
          </Box>
        </Box>

        {/* Status Overview */}
        <Paper sx={{ p: 3, mb: 4, backgroundColor: theme.palette.grey[50] }}>
          <Typography
            variant="h6"
            sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: theme.palette.secondary.main }}
          >
            Integrations-Status
          </Typography>
          <Stack direction="row" spacing={4} alignItems="center">
            <Box>
              <Typography variant="caption" color="text.secondary">
                Verbundene Services
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
                <Typography variant="h4" sx={{ color: theme.palette.primary.main }}>
                  {connectedCount}
                </Typography>
                <Typography variant="body1" color="text.secondary">
                  von {totalCount}
                </Typography>
              </Box>
            </Box>
            <Box sx={{ flex: 1 }}>
              <Typography variant="caption" color="text.secondary">
                Verbindungsstatus
              </Typography>
              <Box sx={{ mt: 1 }}>
                <LinearProgress
                  variant="determinate"
                  value={(connectedCount / totalCount) * 100}
                  sx={{
                    height: 8,
                    borderRadius: 1,
                    backgroundColor: theme.palette.grey[300],
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: theme.palette.primary.main,
                    },
                  }}
                />
              </Box>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Letzte Aktivität
              </Typography>
              <Typography variant="h6">vor 5 Min</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Datenvolumen heute
              </Typography>
              <Typography variant="h6">24.7k Events</Typography>
            </Box>
          </Stack>
        </Paper>

        {/* Warning Alert if any */}
        {integrations.some(i => i.status === 'warning') && (
          <Alert severity="warning" sx={{ mb: 3 }}>
            <Typography variant="body2">
              <strong>Achtung:</strong> Eine oder mehrere Integrationen benötigen Ihre
              Aufmerksamkeit.
            </Typography>
          </Alert>
        )}

        {/* Integration Cards */}
        <Grid container spacing={3}>
          {integrations.map(integration => (
            <Grid key={integration.id} size={{ xs: 12, md: 6, lg: 4 }}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'all 0.2s',
                  cursor: 'pointer',
                  border: integration.id === 'new' ? `2px dashed ${theme.palette.primary.main}` : 'none',
                  '&:hover': {
                    boxShadow: 4,
                    transform: 'translateY(-2px)',
                  },
                }}
                onClick={() => navigate(integration.path)}
              >
                <CardContent sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                    {integration.icon}
                    <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
                      {integration.id !== 'new' && (
                        <>
                          {getStatusIcon(integration.status)}
                          <Chip
                            label={getStatusLabel(integration.status)}
                            size="small"
                            sx={{
                              backgroundColor: getStatusColor(integration.status),
                              color: 'white',
                            }}
                          />
                        </>
                      )}
                    </Box>
                  </Box>

                  <Typography
                    variant="h5"
                    sx={{
                      mb: 1,
                      fontFamily: 'Antonio, sans-serif',
                      color: theme.palette.secondary.main,
                    }}
                  >
                    {integration.title}
                  </Typography>

                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2, minHeight: 40 }}>
                    {integration.description}
                  </Typography>

                  <Box sx={{ mt: 'auto' }}>
                    {integration.provider && (
                      <Box sx={{ mb: 2 }}>
                        <Typography variant="caption" color="text.secondary">
                          Provider
                        </Typography>
                        <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                          {integration.provider}
                        </Typography>
                      </Box>
                    )}

                    {integration.lastSync && integration.id !== 'new' && (
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                        <Box>
                          <Typography variant="caption" color="text.secondary">
                            Letzte Sync
                          </Typography>
                          <Typography variant="body2">{integration.lastSync}</Typography>
                        </Box>
                        {integration.dataPoints !== undefined && (
                          <Box>
                            <Typography variant="caption" color="text.secondary">
                              Datenpunkte
                            </Typography>
                            <Typography variant="body2">
                              {integration.dataPoints.toLocaleString('de-DE')}
                            </Typography>
                          </Box>
                        )}
                      </Box>
                    )}

                    {integration.config && (
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        {integration.config.apiKey && (
                          <Chip label="API Key" size="small" variant="outlined" />
                        )}
                        {integration.config.webhookUrl && (
                          <Chip label="Webhook" size="small" variant="outlined" />
                        )}
                        {integration.config.credentials && (
                          <Chip label="OAuth" size="small" variant="outlined" />
                        )}
                      </Box>
                    )}
                  </Box>
                </CardContent>

                <CardActions sx={{ px: 2, pb: 2 }}>
                  <Button
                    fullWidth
                    variant={integration.id === 'new' ? 'outlined' : 'contained'}
                    sx={{
                      backgroundColor: integration.id === 'new' ? 'transparent' : theme.palette.primary.main,
                      borderColor: theme.palette.primary.main,
                      color: integration.id === 'new' ? theme.palette.primary.main : 'white',
                      '&:hover': {
                        backgroundColor:
                          integration.id === 'new' ? 'rgba(148, 196, 86, 0.1)' : theme.palette.primary.dark,
                      },
                    }}
                  >
                    {integration.id === 'new' ? 'Integration hinzufügen' : 'Konfigurieren'}
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Add global keyframes for spin animation */}
        <style>{`
          @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
          }
        `}</style>
      </Box>
    </MainLayoutV2>
  );
}
