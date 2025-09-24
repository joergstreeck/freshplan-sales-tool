import { Box, Typography, Button, Container } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import HomeIcon from '@mui/icons-material/Home';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

export function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <MainLayoutV2>
      <Container maxWidth="sm">
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
          <ErrorOutlineIcon sx={{ fontSize: 100, color: '#94C456' }} />

          <Typography
            variant="h1"
            sx={{
              fontSize: '6rem',
              fontFamily: 'Antonio, sans-serif',
              color: '#004F7B',
              fontWeight: 'bold',
            }}
          >
            404
          </Typography>

          <Typography
            variant="h4"
            sx={{
              fontFamily: 'Antonio, sans-serif',
              color: '#004F7B',
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
                backgroundColor: '#94C456',
                '&:hover': { backgroundColor: '#7BA347' },
              }}
            >
              Zur Startseite
            </Button>

            <Button
              variant="outlined"
              onClick={() => navigate(-1)}
              sx={{
                borderColor: '#004F7B',
                color: '#004F7B',
                '&:hover': {
                  borderColor: '#003A5C',
                  backgroundColor: 'rgba(0, 79, 123, 0.04)',
                },
              }}
            >
              Zurück
            </Button>
          </Box>
        </Box>
      </Container>
    </MainLayoutV2>
  );
}
