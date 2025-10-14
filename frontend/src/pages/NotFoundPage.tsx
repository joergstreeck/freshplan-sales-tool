import { Box, Typography, Button, useTheme } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import HomeIcon from '@mui/icons-material/Home';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

export function NotFoundPage() {
  const navigate = useNavigate();
  const theme = useTheme();

  return (
    <MainLayoutV2 maxWidth="sm">
      <Box sx={{ py: 4 }}>
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '60vh',
            textAlign: 'center',
            gap: 3,
          }}
        >
          <ErrorOutlineIcon sx={{ fontSize: 100, color: theme.palette.primary.main }} />

          <Typography
            variant="h1"
            sx={{
              fontSize: '6rem',
              fontFamily: 'Antonio, sans-serif',
              color: theme.palette.secondary.main,
              fontWeight: 'bold',
            }}
          >
            404
          </Typography>

          <Typography
            variant="h4"
            sx={{
              fontFamily: 'Antonio, sans-serif',
              color: theme.palette.secondary.main,
              mb: 2,
            }}
          >
            Seite nicht gefunden
          </Typography>

          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Die angeforderte Seite existiert leider nicht. Möglicherweise wurde sie verschoben oder
            entfernt.
          </Typography>

          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button
              variant="contained"
              startIcon={<HomeIcon />}
              onClick={() => navigate('/')}
              sx={{
                backgroundColor: theme.palette.primary.main,
                '&:hover': { backgroundColor: theme.palette.primary.dark },
              }}
            >
              Zur Startseite
            </Button>

            <Button
              variant="outlined"
              onClick={() => navigate(-1)}
              sx={{
                borderColor: theme.palette.secondary.main,
                color: theme.palette.secondary.main,
                '&:hover': {
                  borderColor: theme.palette.secondary.dark,
                  backgroundColor: 'rgba(0, 79, 123, 0.04)',
                },
              }}
            >
              Zurück
            </Button>
          </Box>
        </Box>
      </Box>
    </MainLayoutV2>
  );
}
