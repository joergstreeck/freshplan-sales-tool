import React from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import PersonIcon from '@mui/icons-material/Person';
import NotificationsIcon from '@mui/icons-material/Notifications';
import PaletteIcon from '@mui/icons-material/Palette';
import SecurityIcon from '@mui/icons-material/Security';
import LanguageIcon from '@mui/icons-material/Language';
import StorageIcon from '@mui/icons-material/Storage';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';

export function EinstellungenDashboard() {
  const navigate = useNavigate();

  const mainSettings = [
    {
      title: 'Mein Profil',
      description: 'Persönliche Informationen und Avatar',
      icon: <PersonIcon sx={{ fontSize: 48, color: '#94C456' }} />,
      path: '/einstellungen/profil',
      items: ['Name & Kontakt', 'Avatar', 'Bio', 'Signatur'],
    },
    {
      title: 'Benachrichtigungen',
      description: 'E-Mail und Push-Benachrichtigungen',
      icon: <NotificationsIcon sx={{ fontSize: 48, color: '#FFA726' }} />,
      path: '/einstellungen/benachrichtigungen',
      items: ['E-Mail Alerts', 'Push', 'In-App', 'Zeitplan'],
    },
    {
      title: 'Darstellung',
      description: 'Theme, Sprache und Layout',
      icon: <PaletteIcon sx={{ fontSize: 48, color: '#29B6F6' }} />,
      path: '/einstellungen/darstellung',
      items: ['Dark Mode', 'Sprache', 'Schriftgröße', 'Layout'],
    },
    {
      title: 'Sicherheit',
      description: 'Passwort, 2FA und Sessions',
      icon: <SecurityIcon sx={{ fontSize: 48, color: '#EF5350' }} />,
      path: '/einstellungen/sicherheit',
      items: ['Passwort', '2FA', 'Sessions', 'API Keys'],
    },
  ];

  const quickSettings = [
    { icon: <LanguageIcon />, label: 'Sprache', value: 'Deutsch' },
    { icon: <AccessTimeIcon />, label: 'Zeitzone', value: 'Europe/Berlin' },
    { icon: <StorageIcon />, label: 'Datenschutz', value: 'Standard' },
  ];

  return (
    <MainLayoutV2>
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h3"
            sx={{ mb: 1, fontFamily: 'Antonio, sans-serif', fontWeight: 'bold', color: '#004F7B' }}
          >
            Einstellungen
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Passen Sie FreshPlan an Ihre Bedürfnisse an
          </Typography>
        </Box>

        <Grid container spacing={3}>
          {/* Main Settings */}
          <Grid size={{ xs: 12, lg: 8 }}>
            <Grid container spacing={3}>
              {mainSettings.map(setting => (
                <Grid key={setting.title} size={{ xs: 12, md: 6 }}>
                  <Card
                    sx={{
                      height: '100%',
                      transition: 'all 0.2s',
                      cursor: 'pointer',
                      '&:hover': { boxShadow: 4, transform: 'translateY(-2px)' },
                    }}
                    onClick={() => navigate(setting.path)}
                  >
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                        {setting.icon}
                        <ArrowForwardIosIcon sx={{ ml: 'auto', color: '#757575' }} />
                      </Box>
                      <Typography
                        variant="h6"
                        sx={{ mb: 1, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
                      >
                        {setting.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        {setting.description}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        {setting.items.map(item => (
                          <Typography
                            key={item}
                            variant="caption"
                            sx={{ px: 1, py: 0.5, backgroundColor: '#f5f5f5', borderRadius: 1 }}
                          >
                            {item}
                          </Typography>
                        ))}
                      </Box>
                    </CardContent>
                    <CardActions>
                      <Button fullWidth sx={{ color: '#004F7B' }}>
                        Konfigurieren
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Grid>

          {/* Quick Settings */}
          <Grid size={{ xs: 12, lg: 4 }}>
            <Card>
              <CardContent>
                <Typography
                  variant="h6"
                  sx={{ mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
                >
                  Schnelleinstellungen
                </Typography>
                <List>
                  {quickSettings.map((item, index) => (
                    <React.Fragment key={item.label}>
                      <ListItem sx={{ px: 0 }}>
                        <ListItemIcon sx={{ minWidth: 40 }}>
                          {React.cloneElement(item.icon, { sx: { color: '#004F7B' } })}
                        </ListItemIcon>
                        <ListItemText primary={item.label} secondary={item.value} />
                        <Button size="small" sx={{ color: '#94C456' }}>
                          Ändern
                        </Button>
                      </ListItem>
                      {index < quickSettings.length - 1 && <Divider />}
                    </React.Fragment>
                  ))}
                </List>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Container>
    </MainLayoutV2>
  );
}
