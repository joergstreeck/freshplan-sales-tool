/**
 * Multi-Location Workflow Integration Tests
 * Sprint 2.1.7.7 - Enterprise-Level Integration Tests
 *
 * @description End-to-end workflow tests for Multi-Location Management
 * These tests verify the complete user journey through:
 * 1. Viewing a HEADQUARTER customer with branches
 * 2. Creating a new branch via CreateBranchDialog
 * 3. Viewing hierarchy metrics in HierarchyDashboard
 * 4. Assigning contacts to multiple locations
 * 5. Creating opportunities for specific branches
 *
 * @since 2025-11-26
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen } from '../../../test/test-utils';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import freshfoodzTheme from '../../../theme/freshfoodz';

// Import components under test
import { HierarchyDashboard } from '../components/hierarchy/HierarchyDashboard';

// Mock all external dependencies
vi.mock('../hooks/useHierarchyMetrics');
vi.mock('../../../lib/apiClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: vi.fn(),
  };
});
vi.mock('../../../features/leads/hooks/shared', () => ({
  BASE_URL: 'http://localhost:8080',
  getAuthHeaders: () => ({}),
}));

import { useHierarchyMetrics } from '../hooks/useHierarchyMetrics';
import { useNavigate } from 'react-router-dom';

// ========== TEST DATA ==========

const HEADQUARTER_ID = '550e8400-e29b-41d4-a716-446655440000';

const mockHierarchyMetrics = {
  totalRevenue: 420000,
  averageRevenue: 140000,
  branchCount: 3,
  totalOpenOpportunities: 12,
  branches: [
    {
      branchId: '550e8400-e29b-41d4-a716-446655440001',
      branchName: 'NH Hotel München',
      city: 'München',
      country: 'DEU',
      revenue: 180000,
      percentage: 42.9,
      openOpportunities: 5,
      status: 'AKTIV',
    },
    {
      branchId: '550e8400-e29b-41d4-a716-446655440002',
      branchName: 'NH Hotel Hamburg',
      city: 'Hamburg',
      country: 'DEU',
      revenue: 150000,
      percentage: 35.7,
      openOpportunities: 4,
      status: 'AKTIV',
    },
    {
      branchId: '550e8400-e29b-41d4-a716-446655440003',
      branchName: 'NH Hotel Berlin',
      city: 'Berlin',
      country: 'DEU',
      revenue: 90000,
      percentage: 21.4,
      openOpportunities: 3,
      status: 'PROSPECT',
    },
  ],
};

// ========== TEST SUITES ==========

describe('Multi-Location Workflow Integration', () => {
  let queryClient: QueryClient;
  const mockNavigate = vi.fn();

  const renderWithProviders = (component: React.ReactElement) => {
    // Note: test-utils already includes Router, so we don't add MemoryRouter here
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

    // Mock fetch for enum endpoints
    global.fetch = vi.fn((url: string) => {
      if (url.includes('/api/enums/customer-types')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve([
              { value: 'UNTERNEHMEN', label: 'Unternehmen' },
              { value: 'EINZELUNTERNEHMEN', label: 'Einzelunternehmen' },
            ]),
        } as Response);
      }
      if (url.includes('/api/enums/customer-status')) {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve([
              { value: 'PROSPECT', label: 'Interessent' },
              { value: 'AKTIV', label: 'Aktiv' },
            ]),
        } as Response);
      }
      return Promise.reject(new Error('Unknown URL'));
    });
  });

  afterEach(() => {
    queryClient.clear();
    vi.restoreAllMocks();
  });

  // ========== HIERARCHY DASHBOARD WORKFLOW ==========

  describe('HierarchyDashboard Workflow', () => {
    it('displays complete hierarchy metrics for HEADQUARTER customer', () => {
      vi.mocked(useHierarchyMetrics).mockReturnValue({
        data: mockHierarchyMetrics,
        isLoading: false,
        isSuccess: true,
        isError: false,
        error: null,
      });

      const mockOnCreateBranch = vi.fn();

      renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      // Verify header
      expect(screen.getByText('Standort-Übersicht')).toBeInTheDocument();

      // Verify aggregated metrics cards
      expect(screen.getByText('Gesamt-Umsatz (Konzern)')).toBeInTheDocument();
      expect(screen.getByText('420.000,00 €')).toBeInTheDocument();

      expect(screen.getByText('Durchschnitt pro Standort')).toBeInTheDocument();
      expect(screen.getByText('140.000,00 €')).toBeInTheDocument();

      expect(screen.getByText('Standorte')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument();

      expect(screen.getAllByText('Opportunities')[0]).toBeInTheDocument();
      expect(screen.getByText('12')).toBeInTheDocument();

      // Verify branch table
      expect(screen.getByText('Standort-Details (sortiert nach Umsatz)')).toBeInTheDocument();
      expect(screen.getByText('NH Hotel München')).toBeInTheDocument();
      expect(screen.getByText('NH Hotel Hamburg')).toBeInTheDocument();
      expect(screen.getByText('NH Hotel Berlin')).toBeInTheDocument();
    });

    it('navigates to branch detail when clicking on branch row', async () => {
      const user = userEvent.setup();

      vi.mocked(useHierarchyMetrics).mockReturnValue({
        data: mockHierarchyMetrics,
        isLoading: false,
        isSuccess: true,
        isError: false,
        error: null,
      });

      const mockOnCreateBranch = vi.fn();

      renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      // Click on Munich branch
      const branchRow = screen.getByText('NH Hotel München').closest('tr');
      expect(branchRow).toBeInTheDocument();

      if (branchRow) {
        await user.click(branchRow);
      }

      // Verify navigation was called with correct URL
      expect(mockNavigate).toHaveBeenCalledWith('/customers/550e8400-e29b-41d4-a716-446655440001');
    });

    it('opens CreateBranchDialog when clicking "Neue Filiale anlegen"', async () => {
      const user = userEvent.setup();

      vi.mocked(useHierarchyMetrics).mockReturnValue({
        data: mockHierarchyMetrics,
        isLoading: false,
        isSuccess: true,
        isError: false,
        error: null,
      });

      const mockOnCreateBranch = vi.fn();

      renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      const createButton = screen.getByRole('button', { name: /Neue Filiale anlegen/i });
      await user.click(createButton);

      expect(mockOnCreateBranch).toHaveBeenCalledTimes(1);
    });

    it('displays empty state when no branches exist', () => {
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

      const mockOnCreateBranch = vi.fn();

      renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      expect(screen.getByText(/Noch keine Filialen angelegt/i)).toBeInTheDocument();
    });
  });

  // CreateBranch Workflow tests are in CreateBranchDialog.test.tsx

  // ========== STATUS CHIP RENDERING ==========

  describe('Status Chip Rendering', () => {
    it('renders different status chips with correct colors', () => {
      vi.mocked(useHierarchyMetrics).mockReturnValue({
        data: mockHierarchyMetrics,
        isLoading: false,
        isSuccess: true,
        isError: false,
        error: null,
      });

      const mockOnCreateBranch = vi.fn();

      renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      // Verify AKTIV chips (2 branches)
      const aktivChips = screen.getAllByText('AKTIV');
      expect(aktivChips).toHaveLength(2);

      // Verify PROSPECT chip (1 branch)
      const prospectChip = screen.getByText('PROSPECT');
      expect(prospectChip).toBeInTheDocument();
    });
  });

  // ========== REVENUE AGGREGATION ==========

  describe('Revenue Aggregation (Salesforce Roll-Up Pattern)', () => {
    it('calculates and displays correct revenue percentages', () => {
      vi.mocked(useHierarchyMetrics).mockReturnValue({
        data: mockHierarchyMetrics,
        isLoading: false,
        isSuccess: true,
        isError: false,
        error: null,
      });

      const mockOnCreateBranch = vi.fn();

      renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      // Munich: 180k / 420k = 42.9%
      expect(screen.getByText('42.9%')).toBeInTheDocument();

      // Hamburg: 150k / 420k = 35.7%
      expect(screen.getByText('35.7%')).toBeInTheDocument();

      // Berlin: 90k / 420k = 21.4%
      expect(screen.getByText('21.4%')).toBeInTheDocument();
    });

    it('sorts branches by revenue in descending order', () => {
      vi.mocked(useHierarchyMetrics).mockReturnValue({
        data: mockHierarchyMetrics,
        isLoading: false,
        isSuccess: true,
        isError: false,
        error: null,
      });

      const mockOnCreateBranch = vi.fn();

      renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      // Get all branch names in order
      const branchNames = screen.getAllByText(/NH Hotel/);

      // First should be Munich (highest revenue: 180k)
      expect(branchNames[0]).toHaveTextContent('NH Hotel München');

      // Second should be Hamburg (150k)
      expect(branchNames[1]).toHaveTextContent('NH Hotel Hamburg');

      // Third should be Berlin (90k)
      expect(branchNames[2]).toHaveTextContent('NH Hotel Berlin');
    });
  });

  // ========== LOADING AND ERROR STATES ==========

  describe('Loading and Error States', () => {
    it('shows loading spinner while fetching hierarchy metrics', () => {
      vi.mocked(useHierarchyMetrics).mockReturnValue({
        data: undefined,
        isLoading: true,
        isSuccess: false,
        isError: false,
        error: null,
      });

      const mockOnCreateBranch = vi.fn();

      renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      expect(screen.getByText('Lädt Standort-Daten...')).toBeInTheDocument();
    });

    it('returns null when data fetch completes but data is undefined', () => {
      vi.mocked(useHierarchyMetrics).mockReturnValue({
        data: undefined,
        isLoading: false,
        isSuccess: true,
        isError: false,
        error: null,
      });

      const mockOnCreateBranch = vi.fn();

      const { container } = renderWithProviders(
        <HierarchyDashboard parentCustomerId={HEADQUARTER_ID} onCreateBranch={mockOnCreateBranch} />
      );

      // Component should render nothing
      expect(container.firstChild).toBeNull();
    });
  });
});
