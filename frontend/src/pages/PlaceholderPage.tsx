import React from 'react';
import {
  Typography,
  Box,
  Paper,
  Button,
  Grid,
  Card,
  CardContent,
  useTheme,
  alpha,
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
  icon,
}) => {
  const navigate = useNavigate();
  const theme = useTheme();

  // Set default icon with theme color if not provided
  const defaultIcon = <ConstructionIcon sx={{ fontSize: 80, color: theme.palette.primary.main }} />;
  const displayIcon = icon || defaultIcon;

  return (
    <MainLayoutV2>
      <Box sx={{ py: 4 }}>
        {/* Back Button */}
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate(-1)}
          sx={{ mb: 3, color: theme.palette.secondary.main }}
        >
          Zurück
        </Button>

        {/* Main Content */}
        <Box sx={{ textAlign: 'center', mb: 6 }}>
          {displayIcon}
          <Typography variant="h3" sx={{ mt: 3, mb: 2, color: theme.palette.secondary.main }}>
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
                backgroundColor: alpha(theme.palette.success.light, 0.13),
                border: `1px solid ${theme.palette.primary.main}`,
              }}
            >
              <Typography variant="body2" color="text.secondary">
                Voraussichtlich verfügbar:
              </Typography>
              <Typography variant="h6" sx={{ color: theme.palette.primary.main }}>
                {expectedDate}
              </Typography>
            </Paper>
          </Box>
        )}

        {/* Coming Features */}
        {features.length > 0 && (
          <>
            <Typography variant="h5" sx={{ mb: 3, textAlign: 'center' }}>
              Was Sie erwarten können
            </Typography>
            <Grid container spacing={3} sx={{ mb: 4 }}>
              {features.map((feature, index) => (
                <Grid size={{ xs: 12, md: 4 }} key={index}>
                  <Card sx={{ height: '100%', textAlign: 'center' }}>
                    <CardContent>
                      <RocketLaunchIcon
                        sx={{ fontSize: 40, color: theme.palette.primary.main, mb: 1 }}
                      />
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
            backgroundColor: alpha(theme.palette.secondary.main, 0.05),
            border: `1px solid ${alpha(theme.palette.secondary.main, 0.2)}`,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
            <InfoIcon sx={{ color: theme.palette.secondary.main, mt: 0.5 }} />
            <Box>
              <Typography variant="h6" sx={{ mb: 1 }}>
                Möchten Sie benachrichtigt werden?
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Wir informieren Sie gerne, sobald diese Funktion verfügbar ist. Wenden Sie sich an
                Ihren Administrator oder schauen Sie regelmäßig hier vorbei.
              </Typography>
              <Button
                variant="outlined"
                sx={{
                  borderColor: theme.palette.secondary.main,
                  color: theme.palette.secondary.main,
                  '&:hover': {
                    borderColor: theme.palette.secondary.dark,
                    backgroundColor: alpha(theme.palette.secondary.main, 0.04),
                  },
                }}
              >
                Mehr erfahren
              </Button>
            </Box>
          </Box>
        </Paper>
      </Box>
    </MainLayoutV2>
  );
};
