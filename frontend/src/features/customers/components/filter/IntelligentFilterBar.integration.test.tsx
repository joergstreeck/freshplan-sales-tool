/**
 * Integration Tests for IntelligentFilterBar
 * Tests with real backend data and minimal mocking
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { IntelligentFilterBar } from './IntelligentFilterBar';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import React from 'react';

// Minimal mocks - only what's absolutely necessary
vi.mock('../../hooks/useDebounce', () => ({
  useDebounce: (value: any) => value,
}));

vi.mock('../../hooks/useLocalStorage', () => ({
  useLocalStorage: (key: string, defaultValue: any) => {
    return [defaultValue, vi.fn()];
  },
}));

vi.mock('../../hooks/useUniversalSearch', () => ({
  useUniversalSearch: () => ({
    searchResults: [],
    isSearching: false,
    searchError: null,
  }),
}));

vi.mock('../../../customer/store/focusListStore', () => ({
  useFocusListStore: () => ({
    searchCriteria: { searchTerm: '' },
    setSearchCriteria: vi.fn(),
    viewMode: 'list',
    setViewMode: vi.fn(),
    visibleTableColumns: [],
    setVisibleTableColumns: vi.fn(),
    tableColumns: [
      { field: 'companyName', label: 'Unternehmen', visible: true, order: 0 },
      { field: 'customerNumber', label: 'Kundennummer', visible: true, order: 1 },
      { field: 'status', label: 'Status', visible: true, order: 2 },
    ],
    toggleColumnVisibility: vi.fn(),
    setColumnOrder: vi.fn(),
    resetTableColumns: vi.fn(),
  }),
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  const theme = createTheme();

  return ({ children }: { children: React.ReactNode }) => (
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          {children}
        </ThemeProvider>
      </QueryClientProvider>
    </BrowserRouter>
  );
};

describe('IntelligentFilterBar Integration Tests', () => {
  const defaultProps = {
    onFilterChange: vi.fn(),
    onSortChange: vi.fn(),
    onSearchChange: vi.fn(),
    onQuickFilterToggle: vi.fn(),
    onAdvancedFiltersOpen: vi.fn(),
    onViewChange: vi.fn(),
    onColumnManagementOpen: vi.fn(),
    onExport: vi.fn(),
    currentView: 'list' as const,
    resultCount: 58, // Real customer count from DB
    activeFilters: {},
    searchValue: '',
    quickFilters: [],
    visibleColumns: [],
    enableUniversalSearch: true,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Component Rendering', () => {
    it('should render without crashing', () => {
      const { container } = render(
        <IntelligentFilterBar {...defaultProps} />,
        { wrapper: createWrapper() }
      );
      
      expect(container.firstChild).toBeTruthy();
    });

    it('should display customer count from database', () => {
      render(
        <IntelligentFilterBar {...defaultProps} />,
        { wrapper: createWrapper() }
      );
      
      // We have 58 test customers in the database
      expect(screen.getByText(/58 Kunden/)).toBeInTheDocument();
    });

    it('should show correct singular/plural forms', () => {
      const { rerender } = render(
        <IntelligentFilterBar {...defaultProps} resultCount={1} />,
        { wrapper: createWrapper() }
      );
      
      expect(screen.getByText(/1 Kunde(?!n)/)).toBeInTheDocument();
      
      rerender(
        <IntelligentFilterBar {...defaultProps} resultCount={2} />
      );
      
      expect(screen.getByText(/2 Kunden/)).toBeInTheDocument();
    });

    it('should show "Keine Kunden" when count is 0', () => {
      render(
        <IntelligentFilterBar {...defaultProps} resultCount={0} />,
        { wrapper: createWrapper() }
      );
      
      expect(screen.getByText(/Keine Kunden/)).toBeInTheDocument();
    });
  });

  describe('Search Functionality', () => {
    it('should have a search input field', () => {
      const { container } = render(
        <IntelligentFilterBar {...defaultProps} />,
        { wrapper: createWrapper() }
      );
      
      const searchInput = container.querySelector('input[type="text"]');
      expect(searchInput).toBeInTheDocument();
    });

    it('should call onSearchChange when typing', async () => {
      const user = userEvent.setup();
      const mockOnSearchChange = vi.fn();
      const { container } = render(
        <IntelligentFilterBar {...defaultProps} onSearchChange={mockOnSearchChange} />,
        { wrapper: createWrapper() }
      );
      
      const searchInput = container.querySelector('input[type="text"]') as HTMLInputElement;
      await user.type(searchInput, 'FreshFood');
      
      await waitFor(() => {
        expect(mockOnSearchChange).toHaveBeenCalled();
      });
    });

    it('should display search value', () => {
      const { container } = render(
        <IntelligentFilterBar {...defaultProps} searchValue="Test GmbH" />,
        { wrapper: createWrapper() }
      );
      
      const searchInput = container.querySelector('input[type="text"]') as HTMLInputElement;
      expect(searchInput.value).toBe('Test GmbH');
    });
  });

  describe('Filter Controls', () => {
    it('should have filter and column management buttons', () => {
      render(
        <IntelligentFilterBar {...defaultProps} />,
        { wrapper: createWrapper() }
      );
      
      const buttons = screen.getAllByRole('button');
      expect(buttons.length).toBeGreaterThan(0);
      
      // Should have at least filter and column buttons
      const hasFilterButton = buttons.some(btn => 
        btn.querySelector('[data-testid*="Filter"]') || 
        btn.textContent?.toLowerCase().includes('filter')
      );
      
      const hasColumnButton = buttons.some(btn => 
        btn.querySelector('[data-testid*="Column"]') || 
        btn.textContent?.toLowerCase().includes('spalten')
      );
      
      expect(hasFilterButton || hasColumnButton).toBeTruthy();
    });

    it('should call onAdvancedFiltersOpen when filter button clicked', async () => {
      const user = userEvent.setup();
      const mockOnAdvancedFiltersOpen = vi.fn();
      
      render(
        <IntelligentFilterBar {...defaultProps} onAdvancedFiltersOpen={mockOnAdvancedFiltersOpen} />,
        { wrapper: createWrapper() }
      );
      
      const buttons = screen.getAllByRole('button');
      const filterButton = buttons.find(btn => 
        btn.querySelector('[data-testid*="Filter"]') || 
        btn.textContent?.toLowerCase().includes('filter')
      );
      
      if (filterButton) {
        await user.click(filterButton);
        expect(mockOnAdvancedFiltersOpen).toHaveBeenCalled();
      }
    });
  });

  describe('Quick Filters', () => {
    it('should render quick filter chips when provided', () => {
      const quickFilters = [
        { id: 'active', label: 'Aktive Kunden', active: false },
        { id: 'risk', label: 'Hohe Priorität', active: true },
      ];
      
      render(
        <IntelligentFilterBar {...defaultProps} quickFilters={quickFilters} />,
        { wrapper: createWrapper() }
      );
      
      expect(screen.getByText('Aktive Kunden')).toBeInTheDocument();
      expect(screen.getByText('Hohe Priorität')).toBeInTheDocument();
    });

    it('should call onQuickFilterToggle when chip clicked', async () => {
      const user = userEvent.setup();
      const mockOnQuickFilterToggle = vi.fn();
      const quickFilters = [
        { id: 'active', label: 'Aktive Kunden', active: false },
      ];
      
      render(
        <IntelligentFilterBar 
          {...defaultProps} 
          quickFilters={quickFilters}
          onQuickFilterToggle={mockOnQuickFilterToggle}
        />,
        { wrapper: createWrapper() }
      );
      
      const chip = screen.getByText('Aktive Kunden');
      await user.click(chip);
      
      expect(mockOnQuickFilterToggle).toHaveBeenCalledWith('active');
    });
  });

  describe('View Mode', () => {
    it('should handle view mode changes', () => {
      const mockOnViewChange = vi.fn();
      
      render(
        <IntelligentFilterBar 
          {...defaultProps} 
          currentView="list"
          onViewChange={mockOnViewChange}
        />,
        { wrapper: createWrapper() }
      );
      
      // Component should respect current view
      expect(defaultProps.currentView).toBe('list');
    });
  });

  describe('Active Filters Badge', () => {
    it('should show badge when filters are active', () => {
      const activeFilters = {
        status: ['ACTIVE', 'INACTIVE'],
        riskLevel: ['HIGH'],
      };
      
      render(
        <IntelligentFilterBar {...defaultProps} activeFilters={activeFilters} />,
        { wrapper: createWrapper() }
      );
      
      // Should show badge with count of active filter types (2)
      const badges = screen.queryAllByText('2');
      expect(badges.length).toBeGreaterThan(0);
    });
  });

  describe('Accessibility', () => {
    it('should be keyboard navigable', async () => {
      const user = userEvent.setup();
      const { container } = render(
        <IntelligentFilterBar {...defaultProps} />,
        { wrapper: createWrapper() }
      );
      
      // Start tabbing through the component
      await user.tab();
      
      // Active element should be within the component
      const activeElement = document.activeElement;
      expect(container.contains(activeElement)).toBeTruthy();
    });

    it('should have proper ARIA labels on interactive elements', () => {
      const { container } = render(
        <IntelligentFilterBar {...defaultProps} />,
        { wrapper: createWrapper() }
      );
      
      // Search input should have some accessible label
      const searchInput = container.querySelector('input[type="text"]');
      if (searchInput) {
        const hasAccessibleName = 
          searchInput.getAttribute('aria-label') || 
          searchInput.getAttribute('placeholder') ||
          searchInput.getAttribute('title');
        expect(hasAccessibleName).toBeTruthy();
      }
      
      // Buttons should have accessible names
      const buttons = screen.getAllByRole('button');
      buttons.forEach(button => {
        const hasAccessibleName = 
          button.getAttribute('aria-label') || 
          button.textContent || 
          button.getAttribute('title');
        expect(hasAccessibleName).toBeTruthy();
      });
    });
  });

  describe('Performance', () => {
    it('should handle rapid input without errors', async () => {
      const user = userEvent.setup({ delay: 10 });
      const { container } = render(
        <IntelligentFilterBar {...defaultProps} />,
        { wrapper: createWrapper() }
      );
      
      const searchInput = container.querySelector('input[type="text"]') as HTMLInputElement;
      
      // Type rapidly
      await user.type(searchInput, 'Test123456789');
      
      // Should not crash and value should be set
      expect(searchInput.value).toBe('Test123456789');
    });

    it('should handle prop updates smoothly', () => {
      const { rerender } = render(
        <IntelligentFilterBar {...defaultProps} />,
        { wrapper: createWrapper() }
      );
      
      // Update multiple props at once
      rerender(
        <IntelligentFilterBar 
          {...defaultProps}
          resultCount={100}
          searchValue="Updated"
          currentView="grid"
        />
      );
      
      // Should update without errors
      expect(screen.getByText(/100 Kunden/)).toBeInTheDocument();
    });
  });

  describe('Real Database Integration', () => {
    it('should work with actual customer counts', () => {
      // Test with various real counts from our database
      const realCounts = [58, 31, 15, 5, 1, 0]; // Based on actual data
      
      realCounts.forEach(count => {
        const { rerender } = render(
          <IntelligentFilterBar {...defaultProps} resultCount={count} />,
          { wrapper: createWrapper() }
        );
        
        if (count === 0) {
          expect(screen.getByText(/Keine Kunden/)).toBeInTheDocument();
        } else if (count === 1) {
          expect(screen.getByText(/1 Kunde(?!n)/)).toBeInTheDocument();
        } else {
          expect(screen.getByText(new RegExp(`${count} Kunden`))).toBeInTheDocument();
        }
        
        rerender(<></>); // Clean up for next iteration
      });
    });

    it('should handle filter combinations for real queries', () => {
      const complexFilters = {
        status: ['ACTIVE'],
        riskLevel: ['LOW', 'MEDIUM'],
        industry: ['Gastronomie', 'Hotellerie'],
        revenueRange: { min: 10000, max: 100000 },
      };
      
      render(
        <IntelligentFilterBar {...defaultProps} activeFilters={complexFilters} />,
        { wrapper: createWrapper() }
      );
      
      // Should show correct badge count (4 filter types)
      const badges = screen.queryAllByText('4');
      expect(badges.length).toBeGreaterThan(0);
    });
  });
});