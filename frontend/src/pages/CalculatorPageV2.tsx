import React from 'react';
import { Container, Typography, Paper, Box, Alert } from '@mui/material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import CalculateIcon from '@mui/icons-material/Calculate';

export function CalculatorPageV2() {
  return (
    <MainLayoutV2>
      <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom sx={{ mb: 4 }}>
          FreshPlan Rabatt-Kalkulator
        </Typography>

        <Alert severity="info" sx={{ mb: 4 }}>
          Diese Seite wird gerade auf die neue Architektur migriert. Die volle Funktionalität wird
          in Kürze verfügbar sein.
        </Alert>

        <Paper sx={{ p: 4 }}>
          <Box sx={{ textAlign: 'center', py: 8 }}>
            <CalculateIcon sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
            <Typography variant="h5" gutterBottom>
              Rabatt-Kalkulator V2
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 2 }}>
              Berechnen Sie Ihren individuellen Rabatt für Lebensmittel-Großhandelsbestellungen.
            </Typography>
            <Typography color="text.secondary">
              Bis zu <strong style={{ color: '#94C456' }}>15% Gesamtrabatt</strong> möglich!
            </Typography>
          </Box>

          {/* Placeholder für die kommenden Komponenten */}
          <Box sx={{ mt: 6, p: 3, border: '2px dashed', borderColor: 'grey.300', borderRadius: 2 }}>
            <Typography variant="body2" color="text.secondary" align="center">
              Calculator-Form und Results-Komponenten werden hier integriert
            </Typography>
          </Box>
        </Paper>

        {/* Info Section als MUI-Komponente */}
        <Paper sx={{ mt: 6, p: 4, backgroundColor: 'grey.50' }}>
          <Typography variant="h6" gutterBottom sx={{ mb: 3 }}>
            So funktioniert die Rabatt-Berechnung
          </Typography>

          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)', lg: 'repeat(4, 1fr)' },
              gap: 3,
            }}
          >
            <Box>
              <Typography variant="subtitle2" color="primary" gutterBottom>
                Mengenrabatt
              </Typography>
              <Typography variant="body2" component="div" color="text.secondary">
                • Ab 5.000€: 3%
                <br />
                • Ab 15.000€: 6%
                <br />
                • Ab 30.000€: 8%
                <br />
                • Ab 50.000€: 9%
                <br />• Ab 75.000€: 10%
              </Typography>
            </Box>

            <Box>
              <Typography variant="subtitle2" color="primary" gutterBottom>
                Frühbucher-Rabatt
              </Typography>
              <Typography variant="body2" component="div" color="text.secondary">
                • Ab 10 Tage: +1%
                <br />
                • Ab 15 Tage: +2%
                <br />• Ab 30 Tage: +3%
              </Typography>
            </Box>

            <Box>
              <Typography variant="subtitle2" color="primary" gutterBottom>
                Abhol-Rabatt
              </Typography>
              <Typography variant="body2" component="div" color="text.secondary">
                • +2% bei Selbstabholung
                <br />
                • Nur ab 5.000€ Bestellwert
                <br />• Abholung am FreshPlan-Standort
              </Typography>
            </Box>

            <Box>
              <Typography variant="subtitle2" color="primary" gutterBottom>
                Besondere Konditionen
              </Typography>
              <Typography variant="body2" component="div" color="text.secondary">
                • Kettenkunden: Individuelle Angebote
                <br />
                • Maximum: 15% Gesamtrabatt
                <br />• Alle Preise netto
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Container>
    </MainLayoutV2>
  );
}
