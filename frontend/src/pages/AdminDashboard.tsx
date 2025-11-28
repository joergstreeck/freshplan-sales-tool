import React from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  Paper,
  Divider,
  useTheme,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

// Icons
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import AssessmentIcon from '@mui/icons-material/Assessment';
import ApiIcon from '@mui/icons-material/Api';
import IntegrationInstructionsIcon from '@mui/icons-material/IntegrationInstructions';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';

interface QuickAccessCard {
  title: string;
  description: string;
  icon: React.ReactNode;
  path: string;
  stats?: string;
  status?: 'active' | 'info' | 'warning';
}

interface CategoryCard {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  itemCount: number;
  items: string[];
}

export function AdminDashboard() {
  const navigate = useNavigate();
  const theme = useTheme();
  // const [expandedCategory, setExpandedCategory] = useState<string | null>(null);

  const quickAccessCards: QuickAccessCard[] = [
    {
      title: 'Audit Dashboard',
      description: 'Überwachen Sie alle Systemaktivitäten und Änderungen',
      icon: <DashboardIcon sx={{ fontSize: 40, color: theme.palette.primary.main }} />,
      path: '/admin/audit',
      stats: '1,247 Events heute',
      status: 'active',
    },
    {
      title: 'Benutzerverwaltung',
      description: 'Verwalten Sie Benutzer, Rollen und Berechtigungen',
      icon: <PeopleIcon sx={{ fontSize: 40, color: theme.palette.secondary.main }} />,
      path: '/admin/users',
      stats: '42 aktive Benutzer',
      status: 'active',
    },
    {
      title: 'Compliance Reports',
      description: 'DSGVO und regulatorische Berichte',
      icon: <AssessmentIcon sx={{ fontSize: 40, color: theme.palette.secondary.main }} />,
      path: '/admin/reports',
      stats: 'Nächster Report in 5 Tagen',
      status: 'info',
    },
  ];

  const categoryCards: CategoryCard[] = [
    {
      id: 'system',
      title: 'System',
      description: 'Technische Systemverwaltung, Monitoring und Wartung',
      icon: <ApiIcon sx={{ fontSize: 32, color: theme.palette.primary.main }} />,
      itemCount: 4,
      items: ['API Status', 'System-Logs', 'Performance', 'Backup & Recovery'],
    },
    {
      id: 'integrations',
      title: 'Integrationen',
      description: 'Externe Services, APIs und Schnittstellen verwalten',
      icon: (
        <IntegrationInstructionsIcon sx={{ fontSize: 32, color: theme.palette.secondary.main }} />
      ),
      itemCount: 6,
      items: ['KI-Anbindungen', 'Xentral', 'E-Mail Services', 'Payment', 'Webhooks', '+ Neue'],
    },
    {
      id: 'help',
      title: 'Hilfe-Konfiguration',
      description: 'Kontextsensitive Hilfe, Tooltips und Onboarding',
      icon: <HelpOutlineIcon sx={{ fontSize: 32, color: theme.palette.secondary.main }} />,
      itemCount: 4,
      items: ['Hilfe-System Demo', 'Tooltips verwalten', 'Touren erstellen', 'Analytics'],
    },
  ];

  const handleCategoryClick = (categoryId: string) => {
    // Navigate to sub-dashboard
    if (categoryId === 'system') {
      navigate('/admin/system');
    } else if (categoryId === 'integrations') {
      navigate('/admin/integrationen');
    } else if (categoryId === 'help') {
      navigate('/admin/help');
    }
  };

  return (
    <MainLayoutV2 maxWidth="full">
      <Box sx={{ py: 4 }}>
        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h3"
            component="h1"
            sx={{
              mb: 1,
              fontWeight: 'bold',
              color: theme.palette.secondary.main,
            }}
          >
            Administration Command Center
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Zentrale Verwaltung aller Systemfunktionen und Konfigurationen
          </Typography>
        </Box>

        {/* Quick Access Section */}
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h5"
            sx={{
              mb: 2,
              color: theme.palette.secondary.main,
            }}
          >
            Schnellzugriff
          </Typography>
          <Grid container spacing={3}>
            {quickAccessCards.map(card => (
              <Grid key={card.title} size={{ xs: 12, md: 4 }}>
                <Card
                  sx={{
                    height: '100%',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    '&:hover': {
                      boxShadow: 4,
                      transform: 'translateY(-2px)',
                    },
                  }}
                  onClick={() => navigate(card.path)}
                >
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                      {card.icon}
                      {card.status === 'active' && (
                        <Chip
                          label="Aktiv"
                          size="small"
                          sx={{
                            ml: 'auto',
                            backgroundColor: theme.palette.primary.main,
                            color: 'white',
                          }}
                        />
                      )}
                    </Box>
                    <Typography
                      variant="h6"
                      sx={{
                        mb: 1,
                      }}
                    >
                      {card.title}
                    </Typography>
                    <Typography
                      variant="body2"
                      color="text.secondary"
                      sx={{ mb: 2, minHeight: 40 }}
                    >
                      {card.description}
                    </Typography>
                    {card.stats && (
                      <Typography
                        variant="caption"
                        sx={{
                          color: theme.palette.primary.main,
                          fontWeight: 'medium',
                        }}
                      >
                        {card.stats}
                      </Typography>
                    )}
                  </CardContent>
                  <CardActions sx={{ px: 2, pb: 2 }}>
                    <Button
                      size="small"
                      endIcon={<ArrowForwardIcon />}
                      sx={{ color: theme.palette.secondary.main }}
                    >
                      Öffnen
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>

        <Divider sx={{ my: 4 }} />

        {/* Categories Section */}
        <Box>
          <Typography
            variant="h5"
            sx={{
              mb: 2,
              color: theme.palette.secondary.main,
            }}
          >
            Kategorien
          </Typography>
          <Grid container spacing={3}>
            {categoryCards.map(category => (
              <Grid key={category.id} size={12}>
                <Paper
                  sx={{
                    p: 3,
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    border: '1px solid',
                    borderColor: 'divider',
                    '&:hover': {
                      borderColor: theme.palette.primary.main,
                      boxShadow: 2,
                    },
                  }}
                  onClick={() => handleCategoryClick(category.id)}
                >
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', flex: 1 }}>
                      {category.icon}
                      <Box sx={{ ml: 2, flex: 1 }}>
                        <Typography
                          variant="h6"
                          sx={{
                            color: theme.palette.secondary.main,
                          }}
                        >
                          {category.title}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {category.description}
                        </Typography>
                        <Box sx={{ mt: 1, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                          {category.items.slice(0, 3).map(item => (
                            <Chip
                              key={item}
                              label={item}
                              size="small"
                              variant="outlined"
                              sx={{ borderColor: theme.palette.primary.main, fontSize: '0.75rem' }}
                            />
                          ))}
                          {category.items.length > 3 && (
                            <Chip
                              label={`+${category.items.length - 3} mehr`}
                              size="small"
                              sx={{
                                backgroundColor: theme.palette.grey[100],
                                fontSize: '0.75rem',
                              }}
                            />
                          )}
                        </Box>
                      </Box>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        {category.itemCount} Tools verfügbar
                      </Typography>
                      <Button
                        variant="contained"
                        endIcon={<ArrowForwardIcon />}
                        sx={{
                          backgroundColor: theme.palette.primary.main,
                          '&:hover': { backgroundColor: theme.palette.primary.dark },
                        }}
                        onClick={e => {
                          e.stopPropagation();
                          handleCategoryClick(category.id);
                        }}
                      >
                        Erkunden
                      </Button>
                    </Box>
                  </Box>
                </Paper>
              </Grid>
            ))}
          </Grid>
        </Box>

        {/* System Status Footer */}
        <Box
          sx={{
            mt: 6,
            p: 2,
            backgroundColor: theme.palette.grey[100],
            borderRadius: 1,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <Typography variant="caption" color="text.secondary">
            System Status: Alle Services operational
          </Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Chip
              label="API: OK"
              size="small"
              sx={{ backgroundColor: theme.palette.primary.main, color: 'white' }}
            />
            <Chip
              label="DB: OK"
              size="small"
              sx={{ backgroundColor: theme.palette.primary.main, color: 'white' }}
            />
            <Chip
              label="Auth: OK"
              size="small"
              sx={{ backgroundColor: theme.palette.primary.main, color: 'white' }}
            />
          </Box>
        </Box>
      </Box>
    </MainLayoutV2>
  );
}
