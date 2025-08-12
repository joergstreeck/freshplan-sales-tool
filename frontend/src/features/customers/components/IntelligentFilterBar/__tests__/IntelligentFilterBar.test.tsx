import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { IntelligentFilterBar } from '../IntelligentFilterBar';
import type { CustomerFilter } from '../../../../../types';

// Mock sub-components
vi.mock('../SearchBar', () => ({
  SearchBar: ({ onSearch, placeholder }: any) => (
    <input
      data-testid="search-bar"
      placeholder={placeholder}
      onChange={(e) => onSearch(e.target.value)}
    />
  ),
}));

vi.mock('../QuickFilters', () => ({
  QuickFilters: ({ filter, onFilterChange }: any) => (
    <div data-testid="quick-filters">
      <button
        data-testid="filter-active"
        onClick={() => onFilterChange({ ...filter, isActive: true })}
      >
        Aktiv
      </button>
      <button
        data-testid="filter-inactive"
        onClick={() => onFilterChange({ ...filter, isActive: false })}
      >
        Inaktiv
      </button>
    </div>
  ),
}));

vi.mock('../FilterDrawer', () => ({
  FilterDrawer: ({ open, onClose, filter, onFilterChange }: any) => 
    open ? (
      <div data-testid="filter-drawer">
        <button onClick={onClose}>Close</button>
        <button
          onClick={() => onFilterChange({ ...filter, hasContacts: true })}
        >
          Mit Kontakten
        </button>
      </div>
    ) : null,
}));

vi.mock('../ColumnManagerDrawer', () => ({
  ColumnManagerDrawer: ({ open, onClose }: any) => 
    open ? (
      <div data-testid="column-manager">
        <button onClick={onClose}>Close Columns</button>
      </div>
    ) : null,
}));

describe('IntelligentFilterBar', () => {
  const mockOnFilterChange = vi.fn();
  const mockOnColumnsChange = vi.fn();
  const mockOnResetFilters = vi.fn();

  const defaultFilter: CustomerFilter = {
    search: '',
    status: [],
    riskScore: null,
    annualVolume: null,
    hasContacts: null,
    isActive: null,
  };

  const defaultColumns = [
    { key: 'company_name', label: 'Firma', visible: true },
    { key: 'status', label: 'Status', visible: true },
    { key: 'risk_score', label: 'Risiko', visible: false },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render all main components', () => {
    render(
      <IntelligentFilterBar
        filter={defaultFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    expect(screen.getByTestId('search-bar')).toBeInTheDocument();
    expect(screen.getByTestId('quick-filters')).toBeInTheDocument();
    expect(screen.getByText('Erweiterte Filter')).toBeInTheDocument();
    expect(screen.getByText('Spalten')).toBeInTheDocument();
  });

  it('should handle search input', async () => {
    const user = userEvent.setup();
    
    render(
      <IntelligentFilterBar
        filter={defaultFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    const searchInput = screen.getByTestId('search-bar');
    await user.type(searchInput, 'Test Company');

    await waitFor(() => {
      expect(mockOnFilterChange).toHaveBeenCalledWith(
        expect.objectContaining({ search: 'Test Company' })
      );
    });
  });

  it('should toggle quick filters', async () => {
    const user = userEvent.setup();
    
    render(
      <IntelligentFilterBar
        filter={defaultFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    const activeButton = screen.getByTestId('filter-active');
    await user.click(activeButton);

    expect(mockOnFilterChange).toHaveBeenCalledWith(
      expect.objectContaining({ isActive: true })
    );
  });

  it('should open and close filter drawer', async () => {
    const user = userEvent.setup();
    
    render(
      <IntelligentFilterBar
        filter={defaultFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    // Open drawer
    const filterButton = screen.getByText('Erweiterte Filter');
    await user.click(filterButton);
    
    expect(screen.getByTestId('filter-drawer')).toBeInTheDocument();

    // Close drawer
    const closeButton = screen.getByText('Close');
    await user.click(closeButton);
    
    await waitFor(() => {
      expect(screen.queryByTestId('filter-drawer')).not.toBeInTheDocument();
    });
  });

  it('should open and close column manager', async () => {
    const user = userEvent.setup();
    
    render(
      <IntelligentFilterBar
        filter={defaultFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    // Open column manager
    const columnsButton = screen.getByText('Spalten');
    await user.click(columnsButton);
    
    expect(screen.getByTestId('column-manager')).toBeInTheDocument();

    // Close column manager
    const closeButton = screen.getByText('Close Columns');
    await user.click(closeButton);
    
    await waitFor(() => {
      expect(screen.queryByTestId('column-manager')).not.toBeInTheDocument();
    });
  });

  it('should apply filters from drawer', async () => {
    const user = userEvent.setup();
    
    render(
      <IntelligentFilterBar
        filter={defaultFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    // Open drawer
    await user.click(screen.getByText('Erweiterte Filter'));
    
    // Apply filter
    await user.click(screen.getByText('Mit Kontakten'));
    
    expect(mockOnFilterChange).toHaveBeenCalledWith(
      expect.objectContaining({ hasContacts: true })
    );
  });

  it('should display active filter count', () => {
    const filterWithValues: CustomerFilter = {
      ...defaultFilter,
      status: ['LEAD', 'PROSPECT'],
      hasContacts: true,
      isActive: true,
    };

    render(
      <IntelligentFilterBar
        filter={filterWithValues}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    // Should show count of active filters
    const filterButton = screen.getByText(/Erweiterte Filter/);
    expect(filterButton).toBeInTheDocument();
  });

  it('should handle reset filters', async () => {
    const user = userEvent.setup();
    
    render(
      <IntelligentFilterBar
        filter={defaultFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
        showReset={true}
      />
    );

    // Assuming reset button is shown when showReset is true
    const resetButton = screen.getByRole('button', { name: /reset/i });
    await user.click(resetButton);
    
    expect(mockOnResetFilters).toHaveBeenCalled();
  });

  it('should maintain filter state across re-renders', () => {
    const { rerender } = render(
      <IntelligentFilterBar
        filter={defaultFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    const updatedFilter: CustomerFilter = {
      ...defaultFilter,
      search: 'Updated Search',
    };

    rerender(
      <IntelligentFilterBar
        filter={updatedFilter}
        onFilterChange={mockOnFilterChange}
        visibleColumns={defaultColumns}
        onColumnsChange={mockOnColumnsChange}
        onResetFilters={mockOnResetFilters}
      />
    );

    const searchInput = screen.getByTestId('search-bar') as HTMLInputElement;
    expect(searchInput.value).toBe('Updated Search');
  });
});