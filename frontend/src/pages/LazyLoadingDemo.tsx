/**
 * Lazy Loading Demo Page
 *
 * Demonstrates the lazy loading capabilities implemented in PR4.
 * Shows lazy images, components, and data loading with intersection observer.
 *
 * @module LazyLoadingDemo
 * @since FC-005 PR4
 */

import React from 'react';
import { Box, Typography, Paper, Stack, Chip, Alert, Grid } from '@mui/material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { LazyImage } from '../components/common/LazyImage';
import { LazyComponent } from '../components/common/LazyComponent';
import { useLazyData } from '../hooks/useLazyData';

// Mock heavy component
const HeavyComponent: React.FC<{ title: string; content: string }> = ({ title, content }) => {
  // Simulate heavy computation
  const [computed] = React.useState(() => {
    let result = 0;
    for (let i = 0; i < 1000000; i++) {
      result += Math.random();
    }
    return result;
  });

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>
        {title}
      </Typography>
      <Typography variant="body2" color="text.secondary">
        {content}
      </Typography>
      <Typography variant="caption" display="block" sx={{ mt: 1 }}>
        Computed value: {computed.toFixed(2)}
      </Typography>
    </Paper>
  );
};

// Mock data fetcher
const fetchMockData = async (id: number): Promise<{ id: number; data: string }> => {
  await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
  return {
    id,
    data: `Lazy loaded data #${id} - Loaded at ${new Date().toLocaleTimeString()}`,
  };
};

export function LazyLoadingDemo() {
  // Demo lazy data loading
  const lazyData1 = useLazyData(() => fetchMockData(1), { threshold: 0.5 });
  const lazyData2 = useLazyData(() => fetchMockData(2), { threshold: 0.5 });
  const lazyData3 = useLazyData(() => fetchMockData(3), { threshold: 0.5 });

  return (
    <MainLayoutV2>
      <Box sx={{ py: 4 }}>
        <Typography variant="h4" gutterBottom>
          Lazy Loading Demo
        </Typography>

        <Alert severity="info" sx={{ mb: 4 }}>
          Diese Seite demonstriert die Lazy Loading Features aus PR4. Scrollen Sie nach unten, um zu
          sehen, wie Komponenten, Bilder und Daten erst geladen werden, wenn sie in den Viewport
          kommen.
        </Alert>

        <Stack spacing={4}>
          {/* Section 1: Lazy Images */}
          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
              1. Lazy Loading Images
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Bilder werden erst geladen, wenn sie sichtbar werden. Öffnen Sie die Netzwerk-Konsole,
              um den verzögerten Ladevorgang zu sehen.
            </Typography>

            <Grid container spacing={2}>
              {[1, 2, 3, 4, 5, 6].map(i => (
                <Grid key={i} size={{ xs: 12, sm: 6, md: 4 }}>
                  <LazyImage
                    src={`https://picsum.photos/400/300?random=${i}`}
                    alt={`Lazy loaded image ${i}`}
                    height={200}
                    threshold={0.1}
                    rootMargin="50px"
                  />
                  <Typography variant="caption" display="block" sx={{ mt: 1 }}>
                    Bild #{i} - Wird bei Sichtbarkeit geladen
                  </Typography>
                </Grid>
              ))}
            </Grid>
          </Paper>

          {/* Spacer to demonstrate scrolling */}
          <Box
            sx={{ height: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
          >
            <Typography variant="h6" color="text.secondary">
              ⬇️ Scrollen Sie weiter für mehr Demos ⬇️
            </Typography>
          </Box>

          {/* Section 2: Lazy Components */}
          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
              2. Lazy Loading Components
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Schwere Komponenten werden erst gerendert, wenn sie sichtbar werden. Dies verbessert
              die initiale Ladezeit erheblich.
            </Typography>

            <Stack spacing={2}>
              {[1, 2, 3].map(i => (
                <LazyComponent
                  key={i}
                  minHeight={150}
                  threshold={0.1}
                  rootMargin="100px"
                  placeholder={
                    <Paper sx={{ p: 3, bgcolor: 'action.hover' }}>
                      <Typography>Schwere Komponente #{i} - Wartet auf Laden...</Typography>
                    </Paper>
                  }
                >
                  <HeavyComponent
                    title={`Heavy Component #${i}`}
                    content="This component performs expensive calculations when rendered."
                  />
                </LazyComponent>
              ))}
            </Stack>
          </Paper>

          {/* Another spacer */}
          <Box
            sx={{ height: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
          >
            <Typography variant="h6" color="text.secondary">
              ⬇️ Noch mehr Lazy Loading ⬇️
            </Typography>
          </Box>

          {/* Section 3: Lazy Data Loading */}
          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
              3. Lazy Data Loading
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              API-Aufrufe werden erst ausgeführt, wenn die Komponente sichtbar wird. Perfekt für
              schwere Datenabfragen.
            </Typography>

            <Stack spacing={2}>
              <Box ref={lazyData1.ref}>
                <Paper
                  sx={{ p: 2, bgcolor: lazyData1.loading ? 'action.hover' : 'background.paper' }}
                >
                  <Typography variant="subtitle2">Datenblock #1</Typography>
                  {lazyData1.loading && <Typography>Lädt...</Typography>}
                  {lazyData1.error && (
                    <Typography color="error">Error: {lazyData1.error.message}</Typography>
                  )}
                  {lazyData1.data && (
                    <Box>
                      <Typography variant="body2">{lazyData1.data.data}</Typography>
                      <Chip
                        label={lazyData1.inView ? 'Sichtbar' : 'Nicht sichtbar'}
                        size="small"
                        sx={{ mt: 1 }}
                      />
                    </Box>
                  )}
                </Paper>
              </Box>

              <Box ref={lazyData2.ref}>
                <Paper
                  sx={{ p: 2, bgcolor: lazyData2.loading ? 'action.hover' : 'background.paper' }}
                >
                  <Typography variant="subtitle2">Datenblock #2</Typography>
                  {lazyData2.loading && <Typography>Lädt...</Typography>}
                  {lazyData2.error && (
                    <Typography color="error">Error: {lazyData2.error.message}</Typography>
                  )}
                  {lazyData2.data && (
                    <Box>
                      <Typography variant="body2">{lazyData2.data.data}</Typography>
                      <Chip
                        label={lazyData2.inView ? 'Sichtbar' : 'Nicht sichtbar'}
                        size="small"
                        sx={{ mt: 1 }}
                      />
                    </Box>
                  )}
                </Paper>
              </Box>

              <Box ref={lazyData3.ref}>
                <Paper
                  sx={{ p: 2, bgcolor: lazyData3.loading ? 'action.hover' : 'background.paper' }}
                >
                  <Typography variant="subtitle2">Datenblock #3</Typography>
                  {lazyData3.loading && <Typography>Lädt...</Typography>}
                  {lazyData3.error && (
                    <Typography color="error">Error: {lazyData3.error.message}</Typography>
                  )}
                  {lazyData3.data && (
                    <Box>
                      <Typography variant="body2">{lazyData3.data.data}</Typography>
                      <Chip
                        label={lazyData3.inView ? 'Sichtbar' : 'Nicht sichtbar'}
                        size="small"
                        sx={{ mt: 1 }}
                      />
                    </Box>
                  )}
                </Paper>
              </Box>
            </Stack>
          </Paper>

          {/* Performance Benefits */}
          <Paper sx={{ p: 3, bgcolor: 'success.main', color: 'success.contrastText' }}>
            <Typography variant="h5" gutterBottom>
              🚀 Performance Benefits
            </Typography>
            <Stack spacing={1}>
              <Typography>✅ Reduzierte initiale Ladezeit</Typography>
              <Typography>✅ Weniger Memory-Verbrauch</Typography>
              <Typography>✅ Bessere User Experience bei langen Listen</Typography>
              <Typography>✅ Optimierte Netzwerk-Nutzung</Typography>
              <Typography>✅ Smooth Scrolling auch bei vielen Elementen</Typography>
            </Stack>
          </Paper>
        </Stack>
      </Box>
    </MainLayoutV2>
  );
}
