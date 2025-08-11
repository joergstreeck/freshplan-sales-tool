import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { VirtualizedCustomerTable } from './VirtualizedCustomerTable';
import { FixedSizeList } from 'react-window';

// Mock react-window
vi.mock('react-window', () => ({
  FixedSizeList: vi.fn(({ children, itemCount, itemSize, height, width, itemData }) => {
    return (
      <div data-testid="virtual-list" style={{ height, width }}>
        {Array.from({ length: Math.min(itemCount, 10) }).map((_, index) => (
          <div key={index} style={{ height: itemSize }}>
            {children({ index, style: { height: itemSize }, data: itemData })}
          </div>
        ))}
      </div>
    );
  }),
}));

const mockCustomers = Array.from({ length: 100 }, (_, i) => ({
  id: `customer-${i}`,
  name: `Customer ${i}`,
  email: `customer${i}@test.com`,
  status: i % 2 === 0 ? 'active' : 'inactive',
  createdAt: new Date(2025, 0, i + 1).toISOString(),
  revenue: 1000 * (i + 1),
  lastContact: new Date(2025, 7, i + 1).toISOString(),
}));

describe('VirtualizedCustomerTable', () => {
  const mockOnCustomerClick = vi.fn();
  const mockOnSort = vi.fn();

  const defaultProps = {
    customers: mockCustomers,
    onCustomerClick: mockOnCustomerClick,
    onSort: mockOnSort,
    sortField: 'name' as const,
    sortDirection: 'asc' as const,
    loading: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Rendering', () => {
    it('should render virtual list when customers exceed threshold', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      expect(screen.getByTestId('virtual-list')).toBeInTheDocument();
    });

    it('should render regular table when customers are below threshold', () => {
      const fewCustomers = mockCustomers.slice(0, 15);
      render(
        <VirtualizedCustomerTable {...defaultProps} customers={fewCustomers} />
      );
      
      expect(screen.queryByTestId('virtual-list')).not.toBeInTheDocument();
      expect(screen.getByRole('table')).toBeInTheDocument();
    });

    it('should render loading skeleton when loading', () => {
      render(<VirtualizedCustomerTable {...defaultProps} loading={true} />);
      
      expect(screen.getAllByTestId('skeleton-row')).toHaveLength(10);
    });

    it('should render empty state when no customers', () => {
      render(<VirtualizedCustomerTable {...defaultProps} customers={[]} />);
      
      expect(screen.getByText(/keine kunden gefunden/i)).toBeInTheDocument();
    });
  });

  describe('Virtual Scrolling', () => {
    it('should use correct item size', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      const virtualList = screen.getByTestId('virtual-list');
      const firstItem = virtualList.firstElementChild;
      expect(firstItem).toHaveStyle({ height: '60px' });
    });

    it('should render visible items only', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      // Should only render first 10 items (mocked limit)
      expect(screen.getByText('Customer 0')).toBeInTheDocument();
      expect(screen.getByText('Customer 9')).toBeInTheDocument();
      expect(screen.queryByText('Customer 50')).not.toBeInTheDocument();
    });

    it('should handle custom row height', () => {
      render(
        <VirtualizedCustomerTable {...defaultProps} rowHeight={80} />
      );
      
      const virtualList = screen.getByTestId('virtual-list');
      const firstItem = virtualList.firstElementChild;
      expect(firstItem).toHaveStyle({ height: '80px' });
    });

    it('should handle custom visible rows count', () => {
      render(
        <VirtualizedCustomerTable {...defaultProps} visibleRows={5} />
      );
      
      const virtualList = screen.getByTestId('virtual-list');
      // Height should be rowHeight * visibleRows
      expect(virtualList).toHaveStyle({ height: '300px' }); // 60 * 5
    });
  });

  describe('Customer Interactions', () => {
    it('should call onCustomerClick when row is clicked', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      const firstCustomer = screen.getByText('Customer 0').closest('div[role="row"]');
      fireEvent.click(firstCustomer!);
      
      expect(mockOnCustomerClick).toHaveBeenCalledWith(mockCustomers[0]);
    });

    it('should highlight selected customer', () => {
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          selectedCustomerId="customer-0"
        />
      );
      
      const firstCustomer = screen.getByText('Customer 0').closest('div[role="row"]');
      expect(firstCustomer).toHaveClass('selected');
    });

    it('should show hover effect on rows', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      const firstCustomer = screen.getByText('Customer 0').closest('div[role="row"]');
      
      fireEvent.mouseEnter(firstCustomer!);
      expect(firstCustomer).toHaveClass('hover');
      
      fireEvent.mouseLeave(firstCustomer!);
      expect(firstCustomer).not.toHaveClass('hover');
    });
  });

  describe('Sorting', () => {
    it('should show sort indicators', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      const nameHeader = screen.getByText(/name/i).closest('th');
      expect(nameHeader).toContainElement(screen.getByTestId('ArrowUpwardIcon'));
    });

    it('should call onSort when header is clicked', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      const emailHeader = screen.getByText(/email/i);
      fireEvent.click(emailHeader);
      
      expect(mockOnSort).toHaveBeenCalledWith('email');
    });

    it('should show descending arrow when sort direction is desc', () => {
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          sortDirection="desc"
        />
      );
      
      const nameHeader = screen.getByText(/name/i).closest('th');
      expect(nameHeader).toContainElement(screen.getByTestId('ArrowDownwardIcon'));
    });
  });

  describe('Column Display', () => {
    it('should display all default columns', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      expect(screen.getByText(/name/i)).toBeInTheDocument();
      expect(screen.getByText(/email/i)).toBeInTheDocument();
      expect(screen.getByText(/status/i)).toBeInTheDocument();
      expect(screen.getByText(/erstellt/i)).toBeInTheDocument();
      expect(screen.getByText(/umsatz/i)).toBeInTheDocument();
    });

    it('should respect visible columns prop', () => {
      const visibleColumns = ['name', 'email'];
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          visibleColumns={visibleColumns}
        />
      );
      
      expect(screen.getByText(/name/i)).toBeInTheDocument();
      expect(screen.getByText(/email/i)).toBeInTheDocument();
      expect(screen.queryByText(/status/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/umsatz/i)).not.toBeInTheDocument();
    });

    it('should format currency values correctly', () => {
      const fewCustomers = mockCustomers.slice(0, 5);
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          customers={fewCustomers}
        />
      );
      
      expect(screen.getByText('1.000,00 €')).toBeInTheDocument();
    });

    it('should format dates correctly', () => {
      const fewCustomers = mockCustomers.slice(0, 5);
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          customers={fewCustomers}
        />
      );
      
      expect(screen.getByText(/01\.01\.2025/)).toBeInTheDocument();
    });
  });

  describe('Status Display', () => {
    it('should show status badges with correct colors', () => {
      const fewCustomers = mockCustomers.slice(0, 5);
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          customers={fewCustomers}
        />
      );
      
      const activeStatus = screen.getByText('Aktiv');
      expect(activeStatus).toHaveClass('MuiChip-colorSuccess');
      
      const inactiveStatus = screen.getByText('Inaktiv');
      expect(inactiveStatus).toHaveClass('MuiChip-colorDefault');
    });
  });

  describe('Performance', () => {
    it('should memoize row renderer', () => {
      const { rerender } = render(<VirtualizedCustomerTable {...defaultProps} />);
      
      const rowRenderer = vi.fn();
      vi.mocked(FixedSizeList).mockImplementation(({ children }) => {
        rowRenderer(children);
        return <div />;
      });
      
      rerender(<VirtualizedCustomerTable {...defaultProps} />);
      
      // Row renderer should be memoized and not recreated
      expect(rowRenderer).toHaveBeenCalledTimes(1);
    });

    it('should handle large datasets efficiently', () => {
      const largeDataset = Array.from({ length: 10000 }, (_, i) => ({
        id: `customer-${i}`,
        name: `Customer ${i}`,
        email: `customer${i}@test.com`,
        status: 'active',
        createdAt: new Date().toISOString(),
        revenue: 1000,
        lastContact: new Date().toISOString(),
      }));
      
      const { container } = render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          customers={largeDataset}
        />
      );
      
      // Should still render virtual list
      expect(screen.getByTestId('virtual-list')).toBeInTheDocument();
      
      // DOM should not contain all 10000 items
      const rows = container.querySelectorAll('[role="row"]');
      expect(rows.length).toBeLessThan(50);
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA roles', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      expect(screen.getByRole('table')).toBeInTheDocument();
      expect(screen.getAllByRole('row')).toHaveLength(11); // Header + 10 rows
    });

    it('should have proper ARIA labels for sort buttons', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      const nameHeader = screen.getByText(/name/i);
      expect(nameHeader).toHaveAttribute('aria-sort', 'ascending');
    });

    it('should be keyboard navigable', () => {
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      const firstRow = screen.getByText('Customer 0').closest('div[role="row"]');
      
      // Simulate keyboard navigation
      firstRow?.focus();
      expect(document.activeElement).toBe(firstRow);
      
      fireEvent.keyDown(firstRow!, { key: 'Enter' });
      expect(mockOnCustomerClick).toHaveBeenCalledWith(mockCustomers[0]);
    });

    it('should announce row selection to screen readers', () => {
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          selectedCustomerId="customer-0"
        />
      );
      
      const selectedRow = screen.getByText('Customer 0').closest('div[role="row"]');
      expect(selectedRow).toHaveAttribute('aria-selected', 'true');
    });
  });

  describe('Responsive Design', () => {
    it('should hide columns on small screens', () => {
      // Mock small screen
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 400,
      });
      
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      // Should hide some columns on mobile
      expect(screen.queryByText(/erstellt/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/umsatz/i)).not.toBeInTheDocument();
    });

    it('should show all columns on large screens', () => {
      // Mock large screen
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 1920,
      });
      
      render(<VirtualizedCustomerTable {...defaultProps} />);
      
      // Should show all columns
      expect(screen.getByText(/name/i)).toBeInTheDocument();
      expect(screen.getByText(/email/i)).toBeInTheDocument();
      expect(screen.getByText(/status/i)).toBeInTheDocument();
      expect(screen.getByText(/erstellt/i)).toBeInTheDocument();
      expect(screen.getByText(/umsatz/i)).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    it('should handle missing customer data gracefully', () => {
      const customersWithMissingData = [
        {
          id: '1',
          name: null,
          email: undefined,
          status: 'active',
          createdAt: new Date().toISOString(),
          revenue: null,
          lastContact: null,
        },
      ];
      
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          customers={customersWithMissingData as any}
        />
      );
      
      // Should render with fallback values
      expect(screen.getByText('-')).toBeInTheDocument();
    });

    it('should handle invalid date formats', () => {
      const customersWithBadDates = [
        {
          id: '1',
          name: 'Test',
          email: 'test@test.com',
          status: 'active',
          createdAt: 'invalid-date',
          revenue: 1000,
          lastContact: 'also-invalid',
        },
      ];
      
      render(
        <VirtualizedCustomerTable 
          {...defaultProps} 
          customers={customersWithBadDates as any}
        />
      );
      
      // Should show fallback for invalid dates
      expect(screen.getAllByText('-')).toHaveLength(2);
    });
  });
});