/**
 * HierarchyDashboard Component Tests
 * Sprint 2.1.7.7 D5 - Multi-Location Management
 *
 * @description Tests für HierarchyDashboard - Standort-Übersicht mit Metriken
 * @since 2025-11-15
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '../../../../test/test-utils';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import freshfoodzTheme from '../../../../theme/freshfoodz';
import { HierarchyDashboard } from './HierarchyDashboard';
import { useHierarchyMetrics } from '../../hooks/useHierarchyMetrics';
import { useNavigate } from 'react-router-dom';

// Mock dependencies
vi.mock('../../hooks/useHierarchyMetrics');
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: vi.fn(),
  };
});

describe('HierarchyDashboard', () => {
  const mockNavigate = vi.fn();
  const mockOnCreateBranch = vi.fn();
  const mockParentCustomerId = '550e8400-e29b-41d4-a716-446655440000';

  let queryClient: QueryClient;

  const mockMetricsData = {
    totalRevenue: 300000,
    averageRevenue: 100000,
    branchCount: 3,
    totalOpenOpportunities: 8,
    branches: [
      {
        branchId: '550e8400-e29b-41d4-a716-446655440001',
        branchName: 'NH Hotel München',
        city: 'München',
        country: 'DEU',
        revenue: 120000,
        percentage: 40.0,
        openOpportunities: 3,
        status: 'AKTIV',
      },
      {
        branchId: '550e8400-e29b-41d4-a716-446655440002',
        branchName: 'NH Hotel Hamburg',
        city: 'Hamburg',
        country: 'DEU',
        revenue: 100000,
        percentage: 33.3,
        openOpportunities: 3,
        status: 'AKTIV',
      },
      {
        branchId: '550e8400-e29b-41d4-a716-446655440003',
        branchName: 'NH Hotel Berlin',
        city: 'Berlin',
        country: 'DEU',
        revenue: 80000,
        percentage: 26.7,
        openOpportunities: 2,
        status: 'PROSPECT',
      },
    ],
  };

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>
      </QueryClientProvider>
    );
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });

    vi.clearAllMocks();
    vi.mocked(useNavigate).mockReturnValue(mockNavigate);

    // Mock window.matchMedia for MUI
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: (query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {},
        removeListener: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        dispatchEvent: () => {},
      }),
    });
  });

  it('shows loading state initially', () => {
    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: undefined,
      isLoading: true,
      isSuccess: false,
      isError: false,
      error: null,
    });

    renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    expect(screen.getByText('Lädt Standort-Daten...')).toBeInTheDocument();
  });

  it('renders hierarchy metrics with all metric cards', () => {
    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: mockMetricsData,
      isLoading: false,
      isSuccess: true,
      isError: false,
      error: null,
    });

    renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    // Verify header
    expect(screen.getByText('Standort-Übersicht')).toBeInTheDocument();

    // Verify metric cards
    expect(screen.getByText('Gesamt-Umsatz (Konzern)')).toBeInTheDocument();
    expect(screen.getByText('Durchschnitt pro Standort')).toBeInTheDocument();
    expect(screen.getByText('Standorte')).toBeInTheDocument();
    expect(screen.getAllByText('Opportunities')[0]).toBeInTheDocument();

    // Verify metric values
    expect(screen.getByText('300.000,00 €')).toBeInTheDocument();
    expect(screen.getAllByText('100.000,00 €')[0]).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument(); // branch count
    expect(screen.getByText('8')).toBeInTheDocument(); // total opportunities
  });

  it('renders branch table with all branches sorted by revenue descending', () => {
    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: mockMetricsData,
      isLoading: false,
      isSuccess: true,
      isError: false,
      error: null,
    });

    renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    // Verify table header
    expect(screen.getByText('Standort-Details (sortiert nach Umsatz)')).toBeInTheDocument();

    // Verify all branch names are displayed
    expect(screen.getByText('NH Hotel München')).toBeInTheDocument();
    expect(screen.getByText('NH Hotel Hamburg')).toBeInTheDocument();
    expect(screen.getByText('NH Hotel Berlin')).toBeInTheDocument();

    // Verify cities
    expect(screen.getByText('München, DEU')).toBeInTheDocument();
    expect(screen.getByText('Hamburg, DEU')).toBeInTheDocument();
    expect(screen.getByText('Berlin, DEU')).toBeInTheDocument();

    // Verify revenue values
    expect(screen.getByText('120.000,00 €')).toBeInTheDocument();
    expect(screen.getAllByText('100.000,00 €')[0]).toBeInTheDocument();
    expect(screen.getByText('80.000,00 €')).toBeInTheDocument();

    // Verify percentages
    expect(screen.getByText('40%')).toBeInTheDocument();
    expect(screen.getByText('33.3%')).toBeInTheDocument();
    expect(screen.getByText('26.7%')).toBeInTheDocument();
  });

  it('displays status chips with correct labels', () => {
    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: mockMetricsData,
      isLoading: false,
      isSuccess: true,
      isError: false,
      error: null,
    });

    renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    // Status chips: 2x AKTIV, 1x PROSPECT
    const aktivChips = screen.getAllByText('AKTIV');
    expect(aktivChips).toHaveLength(2);

    expect(screen.getByText('PROSPECT')).toBeInTheDocument();
  });

  it('calls onCreateBranch when "Neue Filiale anlegen" button is clicked', async () => {
    const user = userEvent.setup();

    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: mockMetricsData,
      isLoading: false,
      isSuccess: true,
      isError: false,
      error: null,
    });

    renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    const createButton = screen.getByRole('button', { name: /Neue Filiale anlegen/i });
    await user.click(createButton);

    expect(mockOnCreateBranch).toHaveBeenCalledTimes(1);
  });

  it('navigates to branch details when table row is clicked', async () => {
    const user = userEvent.setup();

    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: mockMetricsData,
      isLoading: false,
      isSuccess: true,
      isError: false,
      error: null,
    });

    renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    const branchRow = screen.getByText('NH Hotel München').closest('tr');
    expect(branchRow).toBeInTheDocument();

    if (branchRow) {
      await user.click(branchRow);
      expect(mockNavigate).toHaveBeenCalledWith('/customers/550e8400-e29b-41d4-a716-446655440001');
    }
  });

  it('shows alert when no branches exist', () => {
    const emptyMetrics = {
      totalRevenue: 0,
      averageRevenue: 0,
      branchCount: 0,
      totalOpenOpportunities: 0,
      branches: [],
    };

    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: emptyMetrics,
      isLoading: false,
      isSuccess: true,
      isError: false,
      error: null,
    });

    renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    expect(screen.getByText(/Noch keine Filialen angelegt. Klicken Sie auf/i)).toBeInTheDocument();

    // Table should not be rendered
    expect(screen.queryByText('Standort-Details (sortiert nach Umsatz)')).not.toBeInTheDocument();
  });

  it('displays opportunity chips with correct counts', () => {
    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: mockMetricsData,
      isLoading: false,
      isSuccess: true,
      isError: false,
      error: null,
    });

    renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    // Each branch has opportunity chips
    const opportunityChips = screen.getAllByText(/offen/i);
    expect(opportunityChips.length).toBeGreaterThan(0);
  });

  it('returns null when data is undefined after loading completes', () => {
    vi.mocked(useHierarchyMetrics).mockReturnValue({
      data: undefined,
      isLoading: false,
      isSuccess: true,
      isError: false,
      error: null,
    });

    const { container } = renderWithProviders(
      <HierarchyDashboard
        parentCustomerId={mockParentCustomerId}
        onCreateBranch={mockOnCreateBranch}
      />
    );

    // Component should render nothing (null)
    expect(container.firstChild).toBeNull();
  });
});
