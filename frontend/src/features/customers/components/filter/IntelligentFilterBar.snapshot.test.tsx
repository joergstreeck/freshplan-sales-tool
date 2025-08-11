/**
 * Snapshot Tests for IntelligentFilterBar Component
 *
 * Tests UI consistency and visual regression for the filter bar.
 *
 * @module IntelligentFilterBar.snapshot.test
 * @since FC-005 PR4
 */

import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import React from 'react';
import { IntelligentFilterBar } from './IntelligentFilterBar';
import type {
  FilterConfig as _FilterConfig,
  SortConfig as _SortConfig,
  ColumnConfig as _ColumnConfig,
} from '../../types/filter.types';

// Mock the hooks
vi.mock('../../hooks/useDebounce', () => ({
  useDebounce: (value: unknown) => value,
}));

vi.mock('../../hooks/useLocalStorage', () => ({
  useLocalStorage: (key: string, defaultValue: unknown) => [defaultValue, vi.fn()],
}));

vi.mock('../../hooks/useUniversalSearch', () => ({
  useUniversalSearch: () => ({
    searchResults: [],
    isSearching: false,
    searchError: null,
    searchTerm: '',
    setSearchTerm: vi.fn(),
    clearResults: vi.fn(),
  }),
}));

vi.mock('../../../customer/store/focusListStore', () => ({
  useFocusListStore: () => ({
    searchCriteria: { searchTerm: '' },
    setSearchCriteria: vi.fn(),
    viewMode: 'list',
    setViewMode: vi.fn(),
    tableColumns: [
      { id: '1', field: 'companyName', label: 'Unternehmen', visible: true, order: 0 },
      { id: '2', field: 'status', label: 'Status', visible: true, order: 1 },
      { id: '3', field: 'riskScore', label: 'Risiko', visible: true, order: 2 },
    ],
    visibleTableColumns: [],
    setVisibleTableColumns: vi.fn(),
    toggleColumnVisibility: vi.fn(),
    setColumnOrder: vi.fn(),
    resetTableColumns: vi.fn(),
  }),
}));

// Test wrapper
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: Infinity },
      mutations: { retry: false },
    },
  });
  const theme = createTheme();

  return ({ children }: { children: React.ReactNode }) => (
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>{children}</ThemeProvider>
      </QueryClientProvider>
    </BrowserRouter>
  );
};

describe('IntelligentFilterBar - Snapshot Tests', () => {
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
    resultCount: 42,
    activeFilters: {},
    searchValue: '',
    quickFilters: [],
    visibleColumns: [],
    enableUniversalSearch: true,
  };

  it('should match snapshot - default state', () => {
    const { container } = render(<IntelligentFilterBar {...defaultProps} />, {
      wrapper: createWrapper(),
    });

    expect(container.firstChild).toMatchSnapshot('default-state');
  });

  it('should match snapshot - with search term', () => {
    const { container } = render(
      <IntelligentFilterBar {...defaultProps} searchValue="Test Company" />,
      { wrapper: createWrapper() }
    );

    expect(container.firstChild).toMatchSnapshot('with-search');
  });

  it('should match snapshot - with active filters', () => {
    const { container } = render(
      <IntelligentFilterBar
        {...defaultProps}
        activeFilters={{
          status: ['ACTIVE', 'INACTIVE'],
          riskLevel: ['HIGH'],
          industry: ['Gastronomie'],
        }}
      />,
      { wrapper: createWrapper() }
    );

    expect(container.firstChild).toMatchSnapshot('with-filters');
  });

  it('should match snapshot - with quick filters', () => {
    const { container } = render(
      <IntelligentFilterBar
        {...defaultProps}
        quickFilters={[
          { id: 'active', label: 'Aktive', active: true },
          { id: 'risky', label: 'Risikoreich', active: false },
          { id: 'recent', label: 'Kürzlich kontaktiert', active: true },
        ]}
      />,
      { wrapper: createWrapper() }
    );

    expect(container.firstChild).toMatchSnapshot('with-quick-filters');
  });

  it('should match snapshot - loading state', () => {
    const { container } = render(<IntelligentFilterBar {...defaultProps} loading={true} />, {
      wrapper: createWrapper(),
    });

    expect(container.firstChild).toMatchSnapshot('loading-state');
  });

  it('should match snapshot - error state', () => {
    const { container } = render(
      <IntelligentFilterBar {...defaultProps} error="Fehler beim Laden der Filter" />,
      { wrapper: createWrapper() }
    );

    expect(container.firstChild).toMatchSnapshot('error-state');
  });

  it('should match snapshot - grid view', () => {
    const { container } = render(<IntelligentFilterBar {...defaultProps} currentView="grid" />, {
      wrapper: createWrapper(),
    });

    expect(container.firstChild).toMatchSnapshot('grid-view');
  });

  it('should match snapshot - with many results', () => {
    const { container } = render(<IntelligentFilterBar {...defaultProps} resultCount={1337} />, {
      wrapper: createWrapper(),
    });

    expect(container.firstChild).toMatchSnapshot('many-results');
  });

  it('should match snapshot - no results', () => {
    const { container } = render(<IntelligentFilterBar {...defaultProps} resultCount={0} />, {
      wrapper: createWrapper(),
    });

    expect(container.firstChild).toMatchSnapshot('no-results');
  });

  it('should match snapshot - with saved filter sets', () => {
    const { container } = render(
      <IntelligentFilterBar
        {...defaultProps}
        savedFilterSets={[
          { id: '1', name: 'Meine Top Kunden', filters: {} },
          { id: '2', name: 'Risikokunden', filters: {} },
        ]}
      />,
      { wrapper: createWrapper() }
    );

    expect(container.firstChild).toMatchSnapshot('with-saved-filters');
  });

  it('should match snapshot - compact mode', () => {
    const { container } = render(<IntelligentFilterBar {...defaultProps} compact={true} />, {
      wrapper: createWrapper(),
    });

    expect(container.firstChild).toMatchSnapshot('compact-mode');
  });

  it('should match snapshot - with all features enabled', () => {
    const { container } = render(
      <IntelligentFilterBar
        {...defaultProps}
        searchValue="Fresh"
        activeFilters={{
          status: ['ACTIVE'],
          riskLevel: ['LOW', 'MEDIUM'],
        }}
        quickFilters={[{ id: 'premium', label: 'Premium', active: true }]}
        resultCount={58}
        savedFilterSets={[{ id: '1', name: 'Favoriten', filters: {} }]}
        enableUniversalSearch={true}
        enableExport={true}
        enableColumnManagement={true}
      />,
      { wrapper: createWrapper() }
    );

    expect(container.firstChild).toMatchSnapshot('all-features');
  });

  it('should match snapshot - mobile view', () => {
    // Mock mobile viewport
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375,
    });

    const { container } = render(<IntelligentFilterBar {...defaultProps} />, {
      wrapper: createWrapper(),
    });

    expect(container.firstChild).toMatchSnapshot('mobile-view');
  });

  it('should match snapshot - dark mode', () => {
    const darkTheme = createTheme({
      palette: {
        mode: 'dark',
      },
    });

    const DarkWrapper = ({ children }: { children: React.ReactNode }) => (
      <BrowserRouter>
        <QueryClientProvider client={new QueryClient()}>
          <ThemeProvider theme={darkTheme}>{children}</ThemeProvider>
        </QueryClientProvider>
      </BrowserRouter>
    );

    const { container } = render(<IntelligentFilterBar {...defaultProps} />, {
      wrapper: DarkWrapper,
    });

    expect(container.firstChild).toMatchSnapshot('dark-mode');
  });
});
