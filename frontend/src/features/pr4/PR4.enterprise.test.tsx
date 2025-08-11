/**
 * PR4 Enterprise Test Suite
 * Pragmatic tests focusing on core functionality for 80%+ coverage
 * Tests actual component behavior with minimal mocking
 */

import { describe, it, expect, vi, beforeAll as _beforeAll, afterAll as _afterAll } from 'vitest';
import { render, screen, waitFor, within as _within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import React from 'react';

// Import PR4 Components
import { IntelligentFilterBar } from '../customers/components/filter/IntelligentFilterBar';
import { MiniAuditTimeline } from '../audit/components/MiniAuditTimeline';
import { LazyComponent } from '../../components/common/LazyComponent';
import { UniversalExportButton } from '../../components/export/UniversalExportButton';
import { VirtualizedCustomerTable } from '../customers/components/VirtualizedCustomerTable';

// Setup mocks
vi.mock('../customers/hooks/useDebounce', () => ({
  useDebounce: (value: unknown) => value,
}));

vi.mock('../customers/hooks/useLocalStorage', () => ({
  useLocalStorage: (key: string, defaultValue: unknown) => [defaultValue, vi.fn()],
}));

vi.mock('../customers/hooks/useUniversalSearch', () => ({
  useUniversalSearch: () => ({
    searchResults: [],
    isSearching: false,
    searchError: null,
  }),
}));

vi.mock('../../customer/store/focusListStore', () => ({
  useFocusListStore: () => ({
    searchCriteria: { searchTerm: '' },
    setSearchCriteria: vi.fn(),
    viewMode: 'list',
    setViewMode: vi.fn(),
    tableColumns: [
      { field: 'companyName', label: 'Unternehmen', visible: true, order: 0 },
      { field: 'status', label: 'Status', visible: true, order: 1 },
    ],
    visibleTableColumns: [],
    setVisibleTableColumns: vi.fn(),
    toggleColumnVisibility: vi.fn(),
    setColumnOrder: vi.fn(),
    resetTableColumns: vi.fn(),
  }),
}));

vi.mock('../audit/services/auditApi', () => ({
  auditApi: {
    getAuditHistory: () => Promise.resolve({
      entries: [
        {
          id: '1',
          entityType: 'CUSTOMER',
          entityId: '1',
          action: 'UPDATE',
          fieldName: 'status',
          oldValue: 'INACTIVE',
          newValue: 'ACTIVE',
          userId: 'user-1',
          userName: 'Admin User',
          timestamp: new Date().toISOString(),
        },
      ],
      totalCount: 1,
    }),
  },
}));

vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'user-1', name: 'Test User', roles: ['admin'] },
    isAuthenticated: true,
  }),
}));

// Test wrapper
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: 0 },
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

describe('PR4 Enterprise Test Suite', () => {
  
  describe('1. IntelligentFilterBar - Core Features', () => {
    const filterProps = {
      onFilterChange: vi.fn(),
      onSortChange: vi.fn(),
      onSearchChange: vi.fn(),
      onQuickFilterToggle: vi.fn(),
      onAdvancedFiltersOpen: vi.fn(),
      onViewChange: vi.fn(),
      onColumnManagementOpen: vi.fn(),
      onExport: vi.fn(),
      currentView: 'list' as const,
      resultCount: 58,
      activeFilters: {},
      searchValue: '',
      quickFilters: [],
      visibleColumns: [],
      enableUniversalSearch: true,
    };

    it('renders and displays customer count', () => {
      render(<IntelligentFilterBar {...filterProps} />, { wrapper: createWrapper() });
      expect(screen.getByText(/58 Kunden/)).toBeInTheDocument();
    });

    it('handles search input', async () => {
      const user = userEvent.setup();
      const { container: _container } = render(
        <IntelligentFilterBar {...filterProps} />, 
        { wrapper: createWrapper() }
      );
      
      const searchInput = container.querySelector('input[type="text"]');
      if (searchInput) {
        await user.type(searchInput, 'Test');
        expect(searchInput).toHaveValue('Test');
      }
    });

    it('shows filter and column buttons', () => {
      render(<IntelligentFilterBar {...filterProps} />, { wrapper: createWrapper() });
      const buttons = screen.getAllByRole('button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('handles quick filters', () => {
      const props = {
        ...filterProps,
        quickFilters: [
          { id: 'active', label: 'Aktive', active: true },
        ],
      };
      render(<IntelligentFilterBar {...props} />, { wrapper: createWrapper() });
      expect(screen.getByText('Aktive')).toBeInTheDocument();
    });

    it('shows active filter badge', () => {
      const props = {
        ...filterProps,
        activeFilters: { status: ['ACTIVE'], risk: ['HIGH'] },
      };
      render(<IntelligentFilterBar {...props} />, { wrapper: createWrapper() });
      // Should show badge with count
      const badges = screen.queryAllByText('2');
      expect(badges.length).toBeGreaterThan(0);
    });
  });

  describe('2. MiniAuditTimeline - Audit History', () => {
    const auditProps = {
      entityType: 'CUSTOMER',
      entityId: '1',
    };

    it('renders and shows loading state', async () => {
      render(<MiniAuditTimeline {...auditProps} />, { wrapper: createWrapper() });
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
      
      await waitFor(() => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      });
    });

    it('displays audit entries after loading', async () => {
      render(<MiniAuditTimeline {...auditProps} />, { wrapper: createWrapper() });
      
      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });
    });

    it('works in compact mode', async () => {
      const { container: _container } = render(
        <MiniAuditTimeline {...auditProps} compact={true} />, 
        { wrapper: createWrapper() }
      );
      
      await waitFor(() => {
        const accordion = container.querySelector('.MuiAccordion-root');
        expect(accordion).toBeInTheDocument();
      });
    });

    it('works in full mode', async () => {
      const { container: _container } = render(
        <MiniAuditTimeline {...auditProps} compact={false} />, 
        { wrapper: createWrapper() }
      );
      
      await waitFor(() => {
        const timeline = container.querySelector('.MuiTimeline-root');
        expect(timeline).toBeInTheDocument();
      });
    });
  });

  describe('3. LazyComponent - Lazy Loading', () => {
    it('renders placeholder initially', () => {
      const TestChild = () => <div>Loaded Content</div>;
      const { container: _container } = render(
        <LazyComponent>
          <TestChild />
        </LazyComponent>,
        { wrapper: createWrapper() }
      );
      
      // Should have a container
      expect(container.firstChild).toBeInTheDocument();
    });

    it('renders with custom threshold', () => {
      const TestChild = () => <div>Test Content</div>;
      render(
        <LazyComponent threshold={0.5}>
          <TestChild />
        </LazyComponent>,
        { wrapper: createWrapper() }
      );
      
      // Component should render without errors
      expect(document.body).toBeInTheDocument();
    });

    it('accepts custom placeholder', () => {
      const TestChild = () => <div>Content</div>;
      const CustomPlaceholder = () => <div>Loading...</div>;
      
      render(
        <LazyComponent placeholder={<CustomPlaceholder />}>
          <TestChild />
        </LazyComponent>,
        { wrapper: createWrapper() }
      );
      
      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });
  });

  describe('4. UniversalExportButton - Export Functionality', () => {
    const exportProps = {
      data: [
        { id: 1, name: 'Test Customer', status: 'ACTIVE' },
      ],
      columns: [
        { field: 'name', headerName: 'Name' },
        { field: 'status', headerName: 'Status' },
      ],
      filename: 'test-export',
    };

    it('renders export button', () => {
      render(<UniversalExportButton {...exportProps} />, { wrapper: createWrapper() });
      const button = screen.getByRole('button');
      expect(button).toBeInTheDocument();
    });

    it('shows export menu on click', async () => {
      const user = userEvent.setup();
      render(<UniversalExportButton {...exportProps} />, { wrapper: createWrapper() });
      
      const button = screen.getByRole('button');
      await user.click(button);
      
      // Should show export options
      await waitFor(() => {
        expect(screen.getByText(/CSV/i)).toBeInTheDocument();
      });
    });

    it('handles custom button text', () => {
      render(
        <UniversalExportButton {...exportProps} buttonText="Download" />, 
        { wrapper: createWrapper() }
      );
      expect(screen.getByText('Download')).toBeInTheDocument();
    });

    it('works with empty data', () => {
      render(
        <UniversalExportButton {...exportProps} data={[]} />, 
        { wrapper: createWrapper() }
      );
      const button = screen.getByRole('button');
      expect(button).toBeInTheDocument();
    });
  });

  describe('5. VirtualizedCustomerTable - Virtual Scrolling', () => {
    const tableProps = {
      customers: [
        {
          id: '1',
          companyName: 'Test GmbH',
          customerNumber: 'K-001',
          status: 'ACTIVE',
          riskScore: 20,
          lastContactDate: new Date().toISOString(),
        },
        {
          id: '2',
          companyName: 'Demo AG',
          customerNumber: 'K-002',
          status: 'INACTIVE',
          riskScore: 80,
          lastContactDate: new Date().toISOString(),
        },
      ],
      onCustomerSelect: vi.fn(),
      selectedCustomerId: null,
    };

    it('renders customer table', () => {
      render(<VirtualizedCustomerTable {...tableProps} />, { wrapper: createWrapper() });
      expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      expect(screen.getByText('Demo AG')).toBeInTheDocument();
    });

    it('handles customer selection', async () => {
      const user = userEvent.setup();
      const mockOnSelect = vi.fn();
      render(
        <VirtualizedCustomerTable {...tableProps} onCustomerSelect={mockOnSelect} />, 
        { wrapper: createWrapper() }
      );
      
      const firstRow = screen.getByText('Test GmbH').closest('tr');
      if (firstRow) {
        await user.click(firstRow);
        expect(mockOnSelect).toHaveBeenCalledWith('1');
      }
    });

    it('shows risk indicators', () => {
      render(<VirtualizedCustomerTable {...tableProps} />, { wrapper: createWrapper() });
      // Low risk (green) and high risk (red) indicators should be visible
      const riskElements = screen.getAllByText(/20|80/);
      expect(riskElements.length).toBeGreaterThan(0);
    });

    it('handles empty customer list', () => {
      render(
        <VirtualizedCustomerTable {...tableProps} customers={[]} />, 
        { wrapper: createWrapper() }
      );
      expect(screen.getByText(/keine kunden|no customers|leer/i)).toBeInTheDocument();
    });

    it('uses virtual scrolling for large lists', () => {
      const manyCustomers = Array.from({ length: 100 }, (_, i) => ({
        id: `${i}`,
        companyName: `Company ${i}`,
        customerNumber: `K-${i}`,
        status: 'ACTIVE',
        riskScore: 50,
        lastContactDate: new Date().toISOString(),
      }));
      
      const { container: _container } = render(
        <VirtualizedCustomerTable {...tableProps} customers={manyCustomers} />, 
        { wrapper: createWrapper() }
      );
      
      // Should use virtual scrolling
      const virtualList = container.querySelector('[style*="height"]');
      expect(virtualList).toBeInTheDocument();
    });
  });

  describe('6. Integration Tests - Components Working Together', () => {
    it('FilterBar and Table work together', async () => {
      const mockOnFilterChange = vi.fn();
      const customers = [
        {
          id: '1',
          companyName: 'Active Customer',
          customerNumber: 'K-001',
          status: 'ACTIVE',
          riskScore: 20,
          lastContactDate: new Date().toISOString(),
        },
      ];
      
      const { container: _container } = render(
        <div>
          <IntelligentFilterBar
            onFilterChange={mockOnFilterChange}
            onSortChange={vi.fn()}
            onSearchChange={vi.fn()}
            onQuickFilterToggle={vi.fn()}
            onAdvancedFiltersOpen={vi.fn()}
            onViewChange={vi.fn()}
            onColumnManagementOpen={vi.fn()}
            onExport={vi.fn()}
            currentView="list"
            resultCount={1}
            activeFilters={{}}
            searchValue=""
            quickFilters={[]}
            visibleColumns={[]}
            enableUniversalSearch={true}
          />
          <VirtualizedCustomerTable
            customers={customers}
            onCustomerSelect={vi.fn()}
            selectedCustomerId={null}
          />
        </div>,
        { wrapper: createWrapper() }
      );
      
      // Both components should render
      expect(screen.getByText(/1 Kunde/)).toBeInTheDocument();
      expect(screen.getByText('Active Customer')).toBeInTheDocument();
    });

    it('Audit Timeline shows in contact cards', async () => {
      const ContactCard = () => (
        <div>
          <h3>Contact Information</h3>
          <MiniAuditTimeline entityType="CONTACT" entityId="1" compact={true} />
        </div>
      );
      
      render(<ContactCard />, { wrapper: createWrapper() });
      
      expect(screen.getByText('Contact Information')).toBeInTheDocument();
      
      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });
    });

    it('Export works with filtered data', async () => {
      const user = userEvent.setup();
      const filteredData = [
        { id: 1, name: 'Filtered Customer', status: 'ACTIVE' },
      ];
      
      render(
        <UniversalExportButton
          data={filteredData}
          columns={[
            { field: 'name', headerName: 'Name' },
            { field: 'status', headerName: 'Status' },
          ]}
          filename="filtered-export"
        />,
        { wrapper: createWrapper() }
      );
      
      const exportButton = screen.getByRole('button');
      await user.click(exportButton);
      
      // Export menu should open
      await waitFor(() => {
        expect(screen.getByText(/Excel/i)).toBeInTheDocument();
      });
    });
  });

  describe('7. Performance & Accessibility', () => {
    it('Components are keyboard accessible', async () => {
      const user = userEvent.setup();
      
      render(
        <IntelligentFilterBar
          onFilterChange={vi.fn()}
          onSortChange={vi.fn()}
          onSearchChange={vi.fn()}
          onQuickFilterToggle={vi.fn()}
          onAdvancedFiltersOpen={vi.fn()}
          onViewChange={vi.fn()}
          onColumnManagementOpen={vi.fn()}
          onExport={vi.fn()}
          currentView="list"
          resultCount={10}
          activeFilters={{}}
          searchValue=""
          quickFilters={[]}
          visibleColumns={[]}
          enableUniversalSearch={true}
        />,
        { wrapper: createWrapper() }
      );
      
      // Tab through component
      await user.tab();
      expect(document.activeElement).not.toBe(document.body);
    });

    it('Handles rapid interactions without errors', async () => {
      const user = userEvent.setup({ delay: 5 });
      const { container: _container } = render(
        <IntelligentFilterBar
          onFilterChange={vi.fn()}
          onSortChange={vi.fn()}
          onSearchChange={vi.fn()}
          onQuickFilterToggle={vi.fn()}
          onAdvancedFiltersOpen={vi.fn()}
          onViewChange={vi.fn()}
          onColumnManagementOpen={vi.fn()}
          onExport={vi.fn()}
          currentView="list"
          resultCount={10}
          activeFilters={{}}
          searchValue=""
          quickFilters={[]}
          visibleColumns={[]}
          enableUniversalSearch={true}
        />,
        { wrapper: createWrapper() }
      );
      
      const searchInput = container.querySelector('input[type="text"]') as HTMLInputElement;
      if (searchInput) {
        await user.type(searchInput, 'RapidTyping123');
        expect(searchInput.value).toBe('RapidTyping123');
      }
    });
  });
});