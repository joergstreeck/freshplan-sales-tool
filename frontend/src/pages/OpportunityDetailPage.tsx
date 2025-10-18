/**
 * Opportunity Detail Page
 * Sprint 2.1.7.1 - Lead → Opportunity Conversion
 *
 * @description Temporary placeholder for opportunity detail view
 * @since 2025-10-18
 */

import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Stack,
} from '@mui/material';
import { ArrowBack as ArrowBackIcon } from '@mui/icons-material';
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';
import { useEffect, useState } from 'react';
import { httpClient } from '../lib/apiClient';
import type { Opportunity } from '../features/opportunity/types';

export function OpportunityDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [opportunity, setOpportunity] = useState<Opportunity | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOpportunity = async () => {
      if (!id) return;

      setLoading(true);
      setError(null);

      try {
        const response = await httpClient.get<Opportunity>(`/api/opportunities/${id}`);
        setOpportunity(response.data);
      } catch (err: any) {
        console.error('Failed to fetch opportunity:', err);
        setError(err.response?.data?.message || 'Opportunity nicht gefunden');
      } finally {
        setLoading(false);
      }
    };

    fetchOpportunity();
  }, [id]);

  if (loading) {
    return (
      <MainLayoutV2>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <CircularProgress />
        </Box>
      </MainLayoutV2>
    );
  }

  if (error || !opportunity) {
    return (
      <MainLayoutV2>
        <Box sx={{ maxWidth: 800, mx: 'auto', mt: 4 }}>
          <Alert severity="error" sx={{ mb: 2 }}>
            {error || 'Opportunity nicht gefunden'}
          </Alert>
          <Button
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate(-1)}
          >
            Zurück
          </Button>
        </Box>
      </MainLayoutV2>
    );
  }

  return (
    <MainLayoutV2>
      <Box sx={{ maxWidth: 1200, mx: 'auto', p: 3 }}>
        {/* Header */}
        <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 3 }}>
          <Button
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate(-1)}
          >
            Zurück
          </Button>
          <Typography variant="h4" component="h1">
            {opportunity.name}
          </Typography>
        </Stack>

        {/* Content */}
        <Card>
          <CardContent>
            <Stack spacing={2}>
              <Typography variant="h6">Opportunity Details</Typography>

              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  ID
                </Typography>
                <Typography>{opportunity.id}</Typography>
              </Box>

              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  Name
                </Typography>
                <Typography>{opportunity.name}</Typography>
              </Box>

              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  Stage
                </Typography>
                <Typography>{opportunity.stage}</Typography>
              </Box>

              {opportunity.opportunityType && (
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">
                    Type
                  </Typography>
                  <Typography>{opportunity.opportunityType}</Typography>
                </Box>
              )}

              {opportunity.value && (
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">
                    Wert
                  </Typography>
                  <Typography>
                    {new Intl.NumberFormat('de-DE', {
                      style: 'currency',
                      currency: 'EUR',
                    }).format(opportunity.value)}
                  </Typography>
                </Box>
              )}

              {opportunity.customerName && (
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">
                    Kunde
                  </Typography>
                  <Typography>{opportunity.customerName}</Typography>
                </Box>
              )}

              {opportunity.description && (
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">
                    Beschreibung
                  </Typography>
                  <Typography>{opportunity.description}</Typography>
                </Box>
              )}

              <Alert severity="info" sx={{ mt: 2 }}>
                <Typography variant="body2">
                  <strong>Hinweis:</strong> Dies ist eine vorläufige Detail-Ansicht. Erweiterte
                  Funktionen werden in zukünftigen Sprints ergänzt.
                </Typography>
              </Alert>
            </Stack>
          </CardContent>
        </Card>
      </Box>
    </MainLayoutV2>
  );
}
