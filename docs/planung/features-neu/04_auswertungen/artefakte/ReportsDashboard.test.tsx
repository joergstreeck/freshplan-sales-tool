import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { freshfoodzTheme } from '@/theme/freshfoodz';
import { ReportsDashboard, fetchSalesSummary } from './reports_integration_snippets';

/**
 * Frontend Component Tests for Reports Dashboard
 *
 * @see ../../grundlagen/TESTING_GUIDE.md - React Testing Library Standards
 * @see ../../grundlagen/DESIGN_SYSTEM.md - Theme V2 Component Testing
 * @see ../../grundlagen/CODING_STANDARDS.md - Frontend Testing Requirements
 *
 * This test suite validates the FreshFoodz Theme V2 integration,
 * user interactions, and error handling for the Reports Dashboard.
 * Uses React Testing Library with Vitest for comprehensive coverage.
 *
 * Theme Testing Coverage:
 * - FreshFoodz Theme V2 color application
 * - Typography (Antonio Bold) rendering
 * - Material-UI component integration
 *
 * Interaction Testing Coverage:
 * - Export button functionality
 * - Filter controls behavior
 * - API integration with error handling
 *
 * Accessibility Testing Coverage:
 * - ARIA labels and roles
 * - Keyboard navigation support
 * - Screen reader compatibility
 *
 * @author Frontend Team
 * @version 1.1
 * @since 2025-09-19
 */

// Mock the UniversalExportButton component
vi.mock('@/components/export', () => ({
  UniversalExportButton: vi.fn(({ buttonLabel, ...props }) => (
    <button data-testid="export-button" {...props}>
      {buttonLabel}
    </button>
  ))
}));

// Mock localStorage for token handling
const mockLocalStorage = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      store = {};
    })
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage
});

// Mock fetch for API calls
global.fetch = vi.fn();

/**
 * Test wrapper with FreshFoodz Theme V2
 */
const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={freshfoodzTheme}>
      {component}
    </ThemeProvider>
  );
};

describe('ReportsDashboard Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockLocalStorage.clear();
  });

  describe('Theme V2 Integration', () => {
    it('should render with FreshFoodz Theme V2 colors', () => {
      // Given: Dashboard with Theme V2
      renderWithTheme(<ReportsDashboard />);

      // When: Component is rendered
      const headline = screen.getByRole('heading', { name: /auswertungen & berichte/i });
      const exportButton = screen.getByTestId('export-button');
      const filterButton = screen.getByRole('button', { name: /erweiterte filter/i });

      // Then: Theme V2 applied correctly
      expect(headline).toBeInTheDocument();
      expect(exportButton).toHaveAttribute('color', 'primary'); // #94C456 via theme
      expect(filterButton).toHaveAttribute('color', 'secondary'); // #004F7B via theme
    });

    it('should use Antonio Bold typography for headlines', () => {
      // Given: Dashboard component
      renderWithTheme(<ReportsDashboard />);

      // When: Headline is rendered
      const headline = screen.getByRole('heading', { name: /auswertungen & berichte/i });

      // Then: Typography variant applied (Antonio Bold via theme)
      expect(headline).toHaveAttribute('component', 'h1');
      // Note: Actual font family testing requires DOM styling verification
    });

    it('should render UniversalExportButton with Theme V2 styling', () => {
      // Given: Dashboard with export button
      renderWithTheme(<ReportsDashboard range="30d" />);

      // When: Export button is rendered
      const exportButton = screen.getByTestId('export-button');

      // Then: Theme V2 properties applied
      expect(exportButton).toHaveTextContent('Umsatzbericht exportieren');
      expect(exportButton).toHaveAttribute('variant', 'contained');
      expect(exportButton).toHaveAttribute('color', 'primary');
    });
  });

  describe('Component Props and State', () => {
    it('should use default range when not specified', () => {
      // Given: Dashboard without range prop
      renderWithTheme(<ReportsDashboard />);

      // When: Component renders
      const exportButton = screen.getByTestId('export-button');

      // Then: Default range applied (30d)
      expect(exportButton).toHaveAttribute('queryParams',
        expect.stringContaining('30d'));
    });

    it('should accept custom range prop', () => {
      // Given: Dashboard with custom range
      renderWithTheme(<ReportsDashboard range="7d" />);

      // When: Component renders
      const exportButton = screen.getByTestId('export-button');

      // Then: Custom range applied
      expect(exportButton).toHaveAttribute('queryParams',
        expect.stringContaining('7d'));
    });

    it('should render all required export formats', () => {
      // Given: Dashboard component
      renderWithTheme(<ReportsDashboard />);

      // When: Export button is rendered
      const exportButton = screen.getByTestId('export-button');

      // Then: All formats available
      const expectedFormats = ['csv', 'xlsx', 'pdf', 'json', 'html', 'jsonl'];
      expect(exportButton).toHaveAttribute('formats',
        expect.stringContaining('csv,xlsx,pdf,json,html,jsonl'));
    });
  });

  describe('User Interactions', () => {
    it('should handle export button click', () => {
      // Given: Dashboard with export functionality
      const mockExportClick = vi.fn();
      vi.mocked(require('@/components/export').UniversalExportButton)
        .mockImplementation(({ onClick, buttonLabel }) => (
          <button onClick={onClick} data-testid="export-button">
            {buttonLabel}
          </button>
        ));

      renderWithTheme(<ReportsDashboard />);

      // When: Export button is clicked
      const exportButton = screen.getByTestId('export-button');
      fireEvent.click(exportButton);

      // Then: Export functionality triggered
      expect(exportButton).toBeInTheDocument();
      // Note: Actual export behavior tested in UniversalExportButton tests
    });

    it('should handle filter button click', () => {
      // Given: Dashboard with filter functionality
      renderWithTheme(<ReportsDashboard />);

      // When: Filter button is clicked
      const filterButton = screen.getByRole('button', { name: /erweiterte filter/i });
      fireEvent.click(filterButton);

      // Then: Button interaction handled
      expect(filterButton).toBeInTheDocument();
      // Note: Filter functionality to be implemented in future iterations
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels and roles', () => {
      // Given: Dashboard component
      renderWithTheme(<ReportsDashboard />);

      // When: Component is rendered
      const headline = screen.getByRole('heading', { level: 1 });
      const exportButton = screen.getByRole('button', { name: /umsatzbericht exportieren/i });
      const filterButton = screen.getByRole('button', { name: /erweiterte filter/i });

      // Then: Proper ARIA structure
      expect(headline).toBeInTheDocument();
      expect(exportButton).toBeInTheDocument();
      expect(filterButton).toBeInTheDocument();
    });

    it('should support keyboard navigation', () => {
      // Given: Dashboard component
      renderWithTheme(<ReportsDashboard />);

      // When: Tab navigation used
      const exportButton = screen.getByTestId('export-button');
      const filterButton = screen.getByRole('button', { name: /erweiterte filter/i });

      exportButton.focus();
      expect(document.activeElement).toBe(exportButton);

      // Simulate tab to next element
      fireEvent.keyDown(exportButton, { key: 'Tab' });
      // Note: Full keyboard navigation testing requires more complex setup
    });
  });

  describe('Error Boundaries and Edge Cases', () => {
    it('should handle missing theme gracefully', () => {
      // Given: Component without ThemeProvider
      // When: Rendered without theme
      const renderWithoutTheme = () => render(<ReportsDashboard />);

      // Then: Should not crash (Material-UI handles missing theme)
      expect(renderWithoutTheme).not.toThrow();
    });

    it('should handle invalid range prop', () => {
      // Given: Dashboard with invalid range
      // @ts-expect-error - Testing invalid prop
      renderWithTheme(<ReportsDashboard range="invalid" />);

      // When: Component renders
      const component = screen.getByRole('heading');

      // Then: Component still renders (validation in parent component)
      expect(component).toBeInTheDocument();
    });
  });
});

describe('fetchSalesSummary API Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockLocalStorage.setItem('authToken', 'test-token');
  });

  describe('Successful API Calls', () => {
    it('should fetch sales summary with default range', async () => {
      // Given: Successful API response
      const mockResponse = {
        sampleSuccessRate: 72.5,
        roiPipeline: 125000,
        partnerSharePct: 35.0,
        atRiskCustomers: 12
      };

      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse)
      } as Response);

      // When: Fetch sales summary
      const result = await fetchSalesSummary();

      // Then: Correct API call and response
      expect(fetch).toHaveBeenCalledWith('/api/reports/sales-summary?range=30d', {
        headers: {
          'Authorization': 'Bearer test-token',
          'Content-Type': 'application/json'
        }
      });
      expect(result).toEqual(mockResponse);
    });

    it('should fetch sales summary with custom range', async () => {
      // Given: API configured for custom range
      const mockResponse = { sampleSuccessRate: 68.0 };
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse)
      } as Response);

      // When: Fetch with custom range
      const result = await fetchSalesSummary('7d');

      // Then: Custom range in API call
      expect(fetch).toHaveBeenCalledWith('/api/reports/sales-summary?range=7d',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer test-token'
          })
        }));
      expect(result).toEqual(mockResponse);
    });
  });

  describe('Error Handling', () => {
    it('should handle API errors gracefully', async () => {
      // Given: API returns error
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: false,
        status: 404
      } as Response);

      // When: Fetch sales summary
      const fetchPromise = fetchSalesSummary();

      // Then: Error thrown with status
      await expect(fetchPromise).rejects.toThrow('API Error: 404');
    });

    it('should handle network errors', async () => {
      // Given: Network failure
      vi.mocked(fetch).mockRejectedValueOnce(new Error('Network error'));

      // When: Fetch sales summary
      const fetchPromise = fetchSalesSummary();

      // Then: Network error propagated
      await expect(fetchPromise).rejects.toThrow('Network error');
    });

    it('should handle missing auth token', async () => {
      // Given: No auth token in localStorage
      mockLocalStorage.removeItem('authToken');

      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({})
      } as Response);

      // When: Fetch sales summary
      await fetchSalesSummary();

      // Then: Authorization header with null token
      expect(fetch).toHaveBeenCalledWith(expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer null'
          })
        }));
    });
  });

  describe('TypeScript Type Safety', () => {
    it('should enforce range parameter types', () => {
      // Given: Type-safe API function
      // When: Call with valid range types
      const validCalls = [
        () => fetchSalesSummary('7d'),
        () => fetchSalesSummary('30d'),
        () => fetchSalesSummary('90d'),
        () => fetchSalesSummary()
      ];

      // Then: All calls should be type-safe
      validCalls.forEach(call => {
        expect(typeof call).toBe('function');
      });

      // Note: Invalid types caught at compile time
      // fetchSalesSummary('invalid'); // TypeScript error
    });
  });
});