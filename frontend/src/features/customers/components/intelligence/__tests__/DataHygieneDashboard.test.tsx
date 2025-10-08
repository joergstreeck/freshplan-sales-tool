import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { DataHygieneDashboard } from '../DataHygieneDashboard';
import { contactInteractionApi } from '../../../services/contactInteractionApi';
import type { DataQualityMetricsDTO } from '../../../types/intelligence.types';

// Mock the API
import { vi } from 'vitest';
vi.mock('../../../services/contactInteractionApi');
const mockedApi = contactInteractionApi as vi.Mocked<typeof contactInteractionApi>;

// Mock recharts components
vi.mock('recharts', () => ({
  PieChart: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="pie-chart">{children}</div>
  ),
  Pie: () => <div data-testid="pie" />,
  Cell: () => <div data-testid="cell" />,
  ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="responsive-container">{children}</div>
  ),
  Tooltip: () => <div data-testid="tooltip" />,
  Legend: () => <div data-testid="legend" />,
}));

const theme = createTheme();

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </QueryClientProvider>
  );
};

describe('DataHygieneDashboard Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Empty State (Cold Start)', () => {
    it('should display empty state metrics correctly', async () => {
      // Arrange
      const emptyMetrics: DataQualityMetricsDTO = {
        totalContacts: 0,
        contactsWithInteractions: 0,
        averageInteractionsPerContact: 0,
        dataCompletenessScore: 0,
        contactsWithWarmthScore: 0,
        freshContacts: 0,
        agingContacts: 0,
        staleContacts: 0,
        criticalContacts: 0,
        showDataCollectionHints: true,
        criticalDataGaps: [
          'Über 50% der Kontakte haben keine Interaktionen',
          'Datenqualität unter 60%',
        ],
        improvementSuggestions: [
          'Import von E-Mail-Historie durchführen',
          'Datenpflege-Kampagne starten',
        ],
        overallDataQuality: 'CRITICAL',
        interactionCoverage: 0,
      };

      mockedApi.getDataQualityMetrics.mockResolvedValue(emptyMetrics);

      // Act
      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      // Assert
      await waitFor(() => {
        expect(screen.getByText('Datenqualität-Übersicht')).toBeInTheDocument();
      });

      // Check key metrics display
      expect(screen.getAllByText('0%')).toHaveLength(2); // Data quality and interaction coverage
      expect(screen.getByText('Kritisch')).toBeInTheDocument();
      expect(screen.getByText('0 von 0')).toBeInTheDocument(); // Interactions coverage
      expect(screen.getByText('0')).toBeInTheDocument(); // Warmth score coverage

      // Check recommendations section
      expect(screen.getByText('Empfehlungen zur Datenqualität')).toBeInTheDocument();
      expect(
        screen.getByText('Über 50% der Kontakte haben keine Interaktionen')
      ).toBeInTheDocument();
      expect(screen.getByText('Datenqualität unter 60%')).toBeInTheDocument();

      // Check improvement suggestions
      expect(screen.getByText('Import von E-Mail-Historie durchführen')).toBeInTheDocument();
      expect(screen.getByText('Datenpflege-Kampagne starten')).toBeInTheDocument();
    });

    it('should show progressive enhancement phases with correct current phase', async () => {
      const emptyMetrics: DataQualityMetricsDTO = {
        totalContacts: 0,
        contactsWithInteractions: 0,
        averageInteractionsPerContact: 0,
        dataCompletenessScore: 0,
        contactsWithWarmthScore: 0,
        freshContacts: 0,
        agingContacts: 0,
        staleContacts: 0,
        criticalContacts: 0,
        showDataCollectionHints: true,
        criticalDataGaps: [],
        improvementSuggestions: [],
        overallDataQuality: 'CRITICAL',
        interactionCoverage: 0,
      };

      mockedApi.getDataQualityMetrics.mockResolvedValue(emptyMetrics);

      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(screen.getByText('Datenqualitäts-Fortschritt')).toBeInTheDocument();
      });

      // Check all three phases are displayed
      expect(screen.getByText('Bootstrap Phase (0-7 Tage)')).toBeInTheDocument();
      expect(screen.getByText('Learning Phase (7-30 Tage)')).toBeInTheDocument();
      expect(screen.getByText('Intelligent Phase (30+ Tage)')).toBeInTheDocument();

      // Check descriptions
      expect(screen.getByText('Basic CRUD, Manuelle Notizen')).toBeInTheDocument();
      expect(screen.getByText('Erste Warmth Trends, Simple Vorschläge')).toBeInTheDocument();
      expect(screen.getByText('Predictive Analytics, Smart Recommendations')).toBeInTheDocument();

      // Current phase should be marked
      expect(screen.getByText('Aktuell')).toBeInTheDocument();
    });
  });

  describe('Populated State', () => {
    it('should display populated metrics correctly', async () => {
      // Arrange
      const populatedMetrics: DataQualityMetricsDTO = {
        totalContacts: 50,
        contactsWithInteractions: 35,
        averageInteractionsPerContact: 4.2,
        dataCompletenessScore: 75,
        contactsWithWarmthScore: 30,
        freshContacts: 25,
        agingContacts: 15,
        staleContacts: 8,
        criticalContacts: 2,
        showDataCollectionHints: true,
        criticalDataGaps: ['2 Kontakte über 1 Jahr nicht aktualisiert'],
        improvementSuggestions: ['Sofortige Überprüfung kritischer Kontakte'],
        overallDataQuality: 'GOOD',
        interactionCoverage: 70,
      };

      mockedApi.getDataQualityMetrics.mockResolvedValue(populatedMetrics);

      // Act
      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      // Assert
      await waitFor(() => {
        expect(screen.getByText('75%')).toBeInTheDocument(); // Data quality
      });

      expect(screen.getByText('Gut')).toBeInTheDocument();
      expect(screen.getByText('35 von 50')).toBeInTheDocument(); // Interactions coverage
      expect(screen.getByText('4.2')).toBeInTheDocument(); // Average interactions
      expect(screen.getByText('30')).toBeInTheDocument(); // Warmth score coverage

      // Check interaction coverage percentage - may have multiple 70% occurrences
      expect(screen.getAllByText('70%').length).toBeGreaterThanOrEqual(1);
    });

    it('should display data freshness chart when data is available', async () => {
      const metricsWithFreshness: DataQualityMetricsDTO = {
        totalContacts: 100,
        contactsWithInteractions: 80,
        averageInteractionsPerContact: 3.5,
        dataCompletenessScore: 80,
        contactsWithWarmthScore: 60,
        freshContacts: 40,
        agingContacts: 30,
        staleContacts: 20,
        criticalContacts: 10,
        showDataCollectionHints: false,
        criticalDataGaps: [],
        improvementSuggestions: [],
        overallDataQuality: 'EXCELLENT',
        interactionCoverage: 80,
      };

      mockedApi.getDataQualityMetrics.mockResolvedValue(metricsWithFreshness);

      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(screen.getByText('Kontakte nach Aktualität')).toBeInTheDocument();
      });

      // Check that chart components are rendered
      expect(screen.getByTestId('pie-chart')).toBeInTheDocument();
      expect(screen.getByTestId('responsive-container')).toBeInTheDocument();
    });

    it('should show critical contact warning when present', async () => {
      const metricsWithCritical: DataQualityMetricsDTO = {
        totalContacts: 20,
        contactsWithInteractions: 15,
        averageInteractionsPerContact: 2.1,
        dataCompletenessScore: 60,
        contactsWithWarmthScore: 10,
        freshContacts: 10,
        agingContacts: 5,
        staleContacts: 3,
        criticalContacts: 2,
        showDataCollectionHints: true,
        criticalDataGaps: ['2 Kontakte über 1 Jahr nicht aktualisiert'],
        improvementSuggestions: ['Sofortige Überprüfung kritischer Kontakte'],
        overallDataQuality: 'FAIR',
        interactionCoverage: 75,
      };

      mockedApi.getDataQualityMetrics.mockResolvedValue(metricsWithCritical);

      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(
          screen.getByText('2 Kontakte wurden über 1 Jahr nicht aktualisiert!')
        ).toBeInTheDocument();
      });

      expect(screen.getByText('Kritische Kontakte anzeigen')).toBeInTheDocument();
    });

    it('should show excellent state when data quality is high', async () => {
      const excellentMetrics: DataQualityMetricsDTO = {
        totalContacts: 100,
        contactsWithInteractions: 95,
        averageInteractionsPerContact: 8.5,
        dataCompletenessScore: 95,
        contactsWithWarmthScore: 90,
        freshContacts: 85,
        agingContacts: 10,
        staleContacts: 3,
        criticalContacts: 0,
        showDataCollectionHints: false,
        criticalDataGaps: [],
        improvementSuggestions: [],
        overallDataQuality: 'EXCELLENT',
        interactionCoverage: 95,
      };

      mockedApi.getDataQualityMetrics.mockResolvedValue(excellentMetrics);

      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(screen.getAllByText('95%').length).toBeGreaterThanOrEqual(1);
      });

      expect(screen.getByText('Exzellent')).toBeInTheDocument();
      expect(
        screen.getByText('Ihre Datenqualität ist ausgezeichnet! Weiter so!')
      ).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    it('should display error message when API fails', async () => {
      // Arrange
      mockedApi.getDataQualityMetrics.mockRejectedValue(new Error('API Error'));

      // Act
      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      // Assert
      await waitFor(() => {
        expect(
          screen.getByText('Fehler beim Laden der Datenqualitäts-Metriken')
        ).toBeInTheDocument();
      });
    });

    it('should show loading state initially', () => {
      // Arrange
      mockedApi.getDataQualityMetrics.mockImplementation(
        () => new Promise(resolve => setTimeout(resolve, 1000))
      );

      // Act
      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      // Assert
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Interactive Elements', () => {
    it('should render refresh and download buttons', async () => {
      const mockMetrics: DataQualityMetricsDTO = {
        totalContacts: 10,
        contactsWithInteractions: 5,
        averageInteractionsPerContact: 2.0,
        dataCompletenessScore: 50,
        contactsWithWarmthScore: 3,
        freshContacts: 5,
        agingContacts: 3,
        staleContacts: 2,
        criticalContacts: 0,
        showDataCollectionHints: true,
        criticalDataGaps: [],
        improvementSuggestions: [],
        overallDataQuality: 'FAIR',
        interactionCoverage: 50,
      };

      mockedApi.getDataQualityMetrics.mockResolvedValue(mockMetrics);

      render(<DataHygieneDashboard />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(screen.getByText('Datenqualität-Übersicht')).toBeInTheDocument();
      });

      // Check for interactive buttons
      expect(screen.getByLabelText('Daten aktualisieren')).toBeInTheDocument();
      expect(screen.getByLabelText('Bericht exportieren')).toBeInTheDocument();
    });
  });

  describe('Data Quality Color Coding', () => {
    const testCases = [
      { score: 95, quality: 'EXCELLENT', label: 'Exzellent' },
      { score: 75, quality: 'GOOD', label: 'Gut' },
      { score: 55, quality: 'FAIR', label: 'Befriedigend' },
      { score: 35, quality: 'POOR', label: 'Mangelhaft' },
      { score: 15, quality: 'CRITICAL', label: 'Kritisch' },
    ];

    testCases.forEach(({ score, quality, label }) => {
      it(`should display correct label for ${quality} quality (${score}%)`, async () => {
        const metrics: DataQualityMetricsDTO = {
          totalContacts: 10,
          contactsWithInteractions: 5,
          averageInteractionsPerContact: 2.0,
          dataCompletenessScore: score,
          contactsWithWarmthScore: 3,
          freshContacts: 5,
          agingContacts: 3,
          staleContacts: 2,
          criticalContacts: 0,
          showDataCollectionHints: true,
          criticalDataGaps: [],
          improvementSuggestions: [],
          overallDataQuality: quality as unknown,
          interactionCoverage: 50,
        };

        mockedApi.getDataQualityMetrics.mockResolvedValue(metrics);

        render(<DataHygieneDashboard />, { wrapper: createWrapper() });

        await waitFor(() => {
          expect(screen.getByText(label)).toBeInTheDocument();
        });
      });
    });
  });
});
