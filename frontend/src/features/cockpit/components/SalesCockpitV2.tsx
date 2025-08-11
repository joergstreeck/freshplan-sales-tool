/**
 * Sales Cockpit V2 - Optimized for MainLayoutV2
 * Angepasst für das neue Standard-Layout ohne feste Höhenberechnungen
 */

import React, { useState, Suspense } from 'react';
import { Box, Typography, Card, Skeleton, CircularProgress } from '@mui/material';
import { styled } from '@mui/material/styles';
import GroupIcon from '@mui/icons-material/Group';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TaskIcon from '@mui/icons-material/Task';
import ErrorIcon from '@mui/icons-material/Error';
// Feature-Komponenten importieren (MUI Versionen) - mit Lazy Loading
const MyDayColumnMUI = React.lazy(() =>
  import('./MyDayColumnMUI').then(m => ({ default: m.MyDayColumnMUI }))
);
const FocusListColumnMUI = React.lazy(() =>
  import('./FocusListColumnMUI').then(m => ({ default: m.FocusListColumnMUI }))
);
const ActionCenterColumnMUI = React.lazy(() =>
  import('./ActionCenterColumnMUI').then(m => ({ default: m.ActionCenterColumnMUI }))
);
import { ResizablePanels } from './layout/ResizablePanels';
import { useDashboardData } from '../hooks/useSalesCockpit';
import { useAuth } from '../../../hooks/useAuth';

// Loading Skeleton für Lazy Loading der Spalten
const ColumnSkeleton: React.FC<{ title: string }> = ({ title }) => (
  <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', p: 2 }}>
    <Typography variant="h6" sx={{ mb: 2 }}>
      {title}
    </Typography>
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, flex: 1 }}>
      <Skeleton variant="rectangular" height={60} />
      <Skeleton variant="rectangular" height={40} />
      <Skeleton variant="rectangular" height={40} />
      <Skeleton variant="rectangular" height={40} />
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          flex: 1,
        }}
      >
        <CircularProgress size={40} />
      </Box>
    </Box>
  </Card>
);

const StatsCard = styled(Card)(({ theme }) => ({
  padding: theme.spacing(1),
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(1),
  minWidth: 120,
  transition: 'all 0.3s ease',
  '&:hover': {
    transform: 'translateY(-1px)',
    boxShadow: theme.shadows[3],
  },
}));

// Styled components entfernt - jetzt in den einzelnen MUI-Komponenten

export function SalesCockpitV2() {
  // State für ausgewählten Kunden
  const [selectedCustomerId, setSelectedCustomerId] = useState<string | undefined>(undefined);

  // Hole Dashboard-Daten für Header-Statistiken
  const { userId } = useAuth();
  const { data: dashboardData, isLoading, refetch } = useDashboardData(userId);

  // Force refresh on mount für Development (entfernen in Production)
  React.useEffect(() => {
    refetch();
  }, [refetch]);

  const handleCustomerSelect = (customerId: string) => {
    setSelectedCustomerId(customerId);
  };

  return (
    <Box
      component="main"
      role="main"
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        mt: -2, // Negativer margin um näher an den Header zu kommen
      }}
    >
      {/* Dashboard Header */}
      <Box sx={{ mb: 0 }}>
        <Typography variant="h4" component="h1">
          FreshPlan Verkaufszentrale
        </Typography>
      </Box>

      {/* Dashboard Stats - klein und rechtsbündig */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 0.5,
        }}
      >
        {/* Linke Seite leer für Balance */}
        <Box />

        {/* Rechte Seite mit Stats */}
        <Box
          sx={{
            display: 'flex',
            gap: 1.5,
            flexWrap: 'wrap',
            justifyContent: 'flex-end',
          }}
        >
          <StatsCard>
            <GroupIcon sx={{ fontSize: 20, color: 'primary.main' }} />
            <Box>
              <Typography variant="body1" sx={{ lineHeight: 1, fontWeight: 600 }}>
                {isLoading ? (
                  <Skeleton width={30} />
                ) : (
                  dashboardData?.statistics?.totalCustomers || 0
                )}
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ lineHeight: 1, fontSize: '0.7rem' }}
              >
                Kunden gesamt
              </Typography>
            </Box>
          </StatsCard>

          <StatsCard>
            <TrendingUpIcon sx={{ fontSize: 20, color: 'primary.main' }} />
            <Box>
              <Typography variant="body1" sx={{ lineHeight: 1, fontWeight: 600 }}>
                {isLoading ? (
                  <Skeleton width={30} />
                ) : (
                  dashboardData?.statistics?.activeCustomers || 0
                )}
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ lineHeight: 1, fontSize: '0.7rem' }}
              >
                Aktive Kunden
              </Typography>
            </Box>
          </StatsCard>

          <StatsCard>
            <TaskIcon sx={{ fontSize: 20, color: 'secondary.main' }} />
            <Box>
              <Typography variant="body1" sx={{ lineHeight: 1, fontWeight: 600 }}>
                {isLoading ? (
                  <Skeleton width={30} />
                ) : (
                  dashboardData?.statistics?.customersAtRisk || 0
                )}
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ lineHeight: 1, fontSize: '0.7rem' }}
              >
                Risiko-Kunden
              </Typography>
            </Box>
          </StatsCard>

          <StatsCard>
            <ErrorIcon sx={{ fontSize: 20, color: 'secondary.main' }} />
            <Box>
              <Typography variant="body1" sx={{ lineHeight: 1, fontWeight: 600 }}>
                {isLoading ? <Skeleton width={30} /> : dashboardData?.statistics?.overdueItems || 0}
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ lineHeight: 1, fontSize: '0.7rem' }}
              >
                Überfällig
              </Typography>
            </Box>
          </StatsCard>
        </Box>
      </Box>

      {/* 3-Column Layout mit ResizablePanels */}
      <Box sx={{ flexGrow: 1, overflow: 'hidden' }}>
        <ResizablePanels
          storageKey="sales-cockpit-panels"
          defaultSizes={[30, 40, 30]}
          minSizes={[20, 30, 20]}
        >
          {/* Column 1: Mein Tag - mit Lazy Loading */}
          <Suspense fallback={<ColumnSkeleton title="Mein Tag" />}>
            <MyDayColumnMUI />
          </Suspense>

          {/* Column 2: Fokus-Liste - mit Lazy Loading */}
          <Suspense fallback={<ColumnSkeleton title="Arbeitsliste" />}>
            <FocusListColumnMUI onCustomerSelect={handleCustomerSelect} />
          </Suspense>

          {/* Column 3: Aktions-Center - mit Lazy Loading */}
          <Suspense fallback={<ColumnSkeleton title="Aktions-Center" />}>
            <ActionCenterColumnMUI
              selectedCustomerId={selectedCustomerId}
              onClose={() => setSelectedCustomerId(undefined)}
            />
          </Suspense>
        </ResizablePanels>
      </Box>
    </Box>
  );
}
