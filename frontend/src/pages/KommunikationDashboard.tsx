import {
  Box,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  Badge,
  useTheme,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import ChatIcon from '@mui/icons-material/Chat';
import AnnouncementIcon from '@mui/icons-material/Announcement';
import NoteIcon from '@mui/icons-material/Note';
import MailIcon from '@mui/icons-material/Mail';

export function KommunikationDashboard() {
  const navigate = useNavigate();
  const theme = useTheme();

  const tools = [
    {
      title: 'Team-Chat',
      description: 'Echtzeit-Kommunikation mit dem Team',
      icon: <ChatIcon sx={{ fontSize: 48, color: theme.palette.primary.main }} />,
      path: '/kommunikation/chat',
      badge: 12,
      status: 'online',
    },
    {
      title: 'Ankündigungen',
      description: 'Wichtige Mitteilungen und Updates',
      icon: <AnnouncementIcon sx={{ fontSize: 48, color: theme.palette.warning.main }} />,
      path: '/kommunikation/ankuendigungen',
      badge: 3,
      status: 'neu',
    },
    {
      title: 'Notizen',
      description: 'Persönliche und geteilte Notizen',
      icon: <NoteIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/kommunikation/notizen',
      stats: '234 Notizen',
    },
    {
      title: 'Interne Nachrichten',
      description: 'Private Nachrichten zwischen Kollegen',
      icon: <MailIcon sx={{ fontSize: 48, color: theme.palette.secondary.main }} />,
      path: '/kommunikation/nachrichten',
      badge: 5,
    },
  ];

  return (
    <MainLayoutV2 maxWidth="full">
      <Box sx={{ py: 4 }}>
        <Box sx={{ mb: 4 }}>
          <Typography
            variant="h3"
            sx={{ mb: 1, fontFamily: 'Antonio, sans-serif', fontWeight: 'bold', color: theme.palette.secondary.main }}
          >
            Kommunikation & Zusammenarbeit
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Bleiben Sie mit Ihrem Team in Verbindung und teilen Sie wichtige Informationen
          </Typography>
        </Box>

        <Grid container spacing={3}>
          {tools.map(tool => (
            <Grid key={tool.title} size={{ xs: 12, sm: 6, md: 3 }}>
              <Card
                sx={{
                  height: '100%',
                  transition: 'all 0.2s',
                  cursor: 'pointer',
                  '&:hover': { boxShadow: 4, transform: 'translateY(-2px)' },
                }}
                onClick={() => navigate(tool.path)}
              >
                <CardContent sx={{ textAlign: 'center' }}>
                  <Box sx={{ position: 'relative', display: 'inline-block' }}>
                    {tool.badge ? (
                      <Badge badgeContent={tool.badge} color="error">
                        {tool.icon}
                      </Badge>
                    ) : (
                      tool.icon
                    )}
                  </Box>
                  <Typography
                    variant="h6"
                    sx={{ mt: 2, mb: 1, fontFamily: 'Antonio, sans-serif', color: theme.palette.secondary.main }}
                  >
                    {tool.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    {tool.description}
                  </Typography>
                  {tool.status && (
                    <Chip
                      label={tool.status === 'online' ? 'Online' : 'Neu'}
                      size="small"
                      sx={{
                        backgroundColor: tool.status === 'online' ? theme.palette.primary.main : theme.palette.warning.main,
                        color: 'white',
                      }}
                    />
                  )}
                  {tool.stats && (
                    <Typography variant="body2" sx={{ color: theme.palette.secondary.main, fontWeight: 'medium' }}>
                      {tool.stats}
                    </Typography>
                  )}
                </CardContent>
                <CardActions>
                  <Button fullWidth sx={{ color: theme.palette.secondary.main }}>
                    Öffnen
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
