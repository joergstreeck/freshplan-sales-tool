// ✅ FIXED VERSION: Theme V2 Integration ohne Hardcoding
// @see ../../grundlagen/DESIGN_SYSTEM.md - FreshFoodz Theme V2 Standards

import type { FC } from 'react';
import { Button, Box, Typography } from '@mui/material';
import { ThemeProvider } from '@mui/material/styles';
import { freshfoodzTheme } from '@/theme/freshfoodz'; // Theme V2
import { UniversalExportButton } from '@/components/export';

/**
 * Reports-Route-Configuration mit Theme V2
 * Ersetzt reports_integration_snippets.tsx
 */

// ✅ ROUTING (providers.tsx) - Theme V2 integriert
export const ReportsRoutes: FC = () => (
  <ThemeProvider theme={freshfoodzTheme}>
    <Routes>
      <Route path="/reports" element={<AuswertungenDashboard />} />
      <Route path="/berichte" element={<Navigate to="/reports" replace />} />
      <Route path="/reports/sales" element={<Reports.Sales />} />
      <Route path="/reports/customers" element={<Reports.Customers />} />
      <Route path="/reports/activities" element={<Reports.Activities />} />
    </Routes>
  </ThemeProvider>
);

/**
 * Data Hooks mit TypeScript Standards
 * @see ../../grundlagen/CODING_STANDARDS.md - import type Requirements
 */
import type { SalesSummaryResponse } from '@/types/reports';

export async function fetchSalesSummary(
  range: '7d' | '30d' | '90d' = '30d'
): Promise<SalesSummaryResponse> {
  const token = localStorage.getItem('authToken');
  const response = await fetch(`/api/reports/sales-summary?range=${range}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`);
  }

  return await response.json();
}

/**
 * Dashboard Integration mit Theme V2 und Error-Boundaries
 * @see ../../grundlagen/DESIGN_SYSTEM.md - Component Standards
 */
interface ReportsDashboardProps {
  range?: '7d' | '30d' | '90d';
}

export const ReportsDashboard: FC<ReportsDashboardProps> = ({
  range = '30d'
}) => {
  return (
    <Box>
      {/* ✅ Theme V2: Typography automatisch Antonio Bold für Headlines */}
      <Typography variant="h1" component="h1">
        Auswertungen & Berichte
      </Typography>

      {/* ✅ Theme V2: Primary Color automatisch #94C456 */}
      <UniversalExportButton
        entity="sales-summary"
        queryParams={{ range }}
        formats={['csv', 'xlsx', 'pdf', 'json', 'html', 'jsonl']}
        buttonLabel="Umsatzbericht exportieren"
        variant="contained"
        color="primary" // ✅ Theme V2: #94C456 automatisch
      />

      {/* ✅ Theme V2: Secondary Color automatisch #004F7B */}
      <Button
        variant="outlined"
        color="secondary"
        sx={{ ml: 2 }}
      >
        Erweiterte Filter
      </Button>
    </Box>
  );
};

/**
 * Error-Boundary-Integration für Production-Readiness
 * @see ../../grundlagen/TESTING_GUIDE.md - Error-Handling-Standards
 */
import { ErrorBoundary } from 'react-error-boundary';

function ReportsErrorFallback({ error }: { error: Error }) {
  return (
    <Box
      sx={{
        p: 3,
        bgcolor: 'error.light',
        color: 'error.contrastText',
        borderRadius: 1
      }}
    >
      <Typography variant="h6" component="h2">
        Fehler beim Laden der Auswertungen
      </Typography>
      <Typography variant="body2">
        {error.message}
      </Typography>
    </Box>
  );
}

export const ReportsWithErrorBoundary: FC = () => (
  <ThemeProvider theme={freshfoodzTheme}>
    <ErrorBoundary FallbackComponent={ReportsErrorFallback}>
      <ReportsDashboard />
    </ErrorBoundary>
  </ThemeProvider>
);

/**
 * Hook für Reports-Data mit Error-Handling
 * @see ../../grundlagen/API_STANDARDS.md - Error-Handling-Pattern
 */
import { useState, useEffect } from 'react';

export function useReportsSummary(range: '7d' | '30d' | '90d' = '30d') {
  const [data, setData] = useState<SalesSummaryResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        setError(null);
        const result = await fetchSalesSummary(range);
        setData(result);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Unknown error'));
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, [range]);

  return { data, loading, error };
}