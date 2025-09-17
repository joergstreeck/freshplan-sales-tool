import React from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Button,
  Grid,
  Card,
  CardContent,
} from '@mui/material';
import ConstructionIcon from '@mui/icons-material/Construction';
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch';
import InfoIcon from '@mui/icons-material/Info';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from 'react-router-dom';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

interface PlaceholderPageProps {
  title: string;
  subtitle?: string;
  description?: string;
  expectedDate?: string;
  features?: string[];
  icon?: React.ReactNode;
}

export const PlaceholderPage: React.FC<PlaceholderPageProps> = ({
  title,
  subtitle = 'Diese Seite wird bald verfügbar sein',
  description = 'Wir arbeiten hart daran, diese Funktion für Sie bereitzustellen.',
  expectedDate,
  features = [],
  icon = <ConstructionIcon sx={{ fontSize: 80, color: '#94C456' }} />,
}) => {
  const navigate = useNavigate();

  return (
    <MainLayoutV2>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {/* Back Button */}
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate(-1)}
          sx={{ mb: 3, color: '#004F7B' }}
        >
          Zurück
        </Button>

        {/* Main Content */}
        <Box sx={{ textAlign: 'center', mb: 6 }}>
          {icon}
          <Typography
            variant="h3"
            sx={{ mt: 3, mb: 2, fontFamily: 'Antonio, sans-serif', color: '#004F7B' }}
          >
            {title}
          </Typography>
          <Typography variant="h5" color="text.secondary" sx={{ mb: 1 }}>
            {subtitle}
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 600, mx: 'auto' }}>
            {description}
          </Typography>
        </Box>

        {/* Expected Date */}
        {expectedDate && (
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Paper
              sx={{
                display: 'inline-block',
                p: 2,
                backgroundColor: 'rgba(148, 196, 86, 0.1)',
                border: '1px solid #94C456',
              }}
            >
              <Typography variant="body2" color="text.secondary">
                Voraussichtlich verfügbar:
              </Typography>
              <Typography variant="h6" sx={{ color: '#94C456', fontFamily: 'Antonio, sans-serif' }}>
                {expectedDate}
              </Typography>
            </Paper>
          </Box>
        )}

        {/* Coming Features */}
        {features.length > 0 && (
          <>
            <Typography
              variant="h5"
              sx={{ mb: 3, textAlign: 'center', fontFamily: 'Antonio, sans-serif' }}
            >
              Was Sie erwarten können
            </Typography>
            <Grid container spacing={3} sx={{ mb: 4 }}>
              {features.map((feature, index) => (
                <Grid size={{ xs: 12, md: 4 }} key={index}>
                  <Card sx={{ height: '100%', textAlign: 'center' }}>
                    <CardContent>
                      <RocketLaunchIcon sx={{ fontSize: 40, color: '#94C456', mb: 1 }} />
                      <Typography variant="body1">{feature}</Typography>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </>
        )}

        {/* Info Box */}
        <Paper
          sx={{
            p: 3,
            backgroundColor: 'rgba(0, 79, 123, 0.05)',
            border: '1px solid rgba(0, 79, 123, 0.2)',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
            <InfoIcon sx={{ color: '#004F7B', mt: 0.5 }} />
            <Box>
              <Typography variant="h6" sx={{ mb: 1 }}>
                Möchten Sie benachrichtigt werden?
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Wir informieren Sie gerne, sobald diese Funktion verfügbar ist.
                Wenden Sie sich an Ihren Administrator oder schauen Sie regelmäßig hier vorbei.
              </Typography>
              <Button
                variant="outlined"
                sx={{
                  borderColor: '#004F7B',
                  color: '#004F7B',
                  '&:hover': {
                    borderColor: '#003A5A',
                    backgroundColor: 'rgba(0, 79, 123, 0.04)',
                  },
                }}
              >
                Mehr erfahren
              </Button>
            </Box>
          </Box>
        </Paper>
      </Container>
    </MainLayoutV2>
  );
};