import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { VirtualizedCustomerTable } from './VirtualizedCustomerTable';

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

// Mock react-window-infinite-loader
vi.mock('react-window-infinite-loader', () => ({
  default: vi.fn(({ children }) => {
    // Simply pass through the children function with a mock ref
    return children({ current: {} });
  }),
}));

const mockCustomers = Array.from({ length: 100 }, (_, i) => ({
  id: `customer-${i}`,
  companyName: `Customer ${i}`,
  email: `customer${i}@test.com`,
  status: i % 2 === 0 ? 'active' : 'inactive',
  createdAt: new Date(2025, 0, i + 1).toISOString(),
  revenue: 1000 * (i + 1),
  lastContact: new Date(2025, 7, i + 1).toISOString(),
}));

describe('VirtualizedCustomerTable', () => {
  const mockOnRowClick = vi.fn();
  const mockOnEdit = vi.fn();
  const mockOnDelete = vi.fn();

  const defaultProps = {
    customers: mockCustomers,
    onRowClick: mockOnRowClick,
    onEdit: mockOnEdit,
    onDelete: mockOnDelete,
    loading: false,
  };

  it('should render virtual list for large datasets', () => {
    render(<VirtualizedCustomerTable {...defaultProps} />);
    expect(screen.getByTestId('virtual-list')).toBeInTheDocument();
  });

  it('should render loading state', () => {
    render(<VirtualizedCustomerTable {...defaultProps} loading={true} />);
    // Check if skeleton or loading indicator is present
    expect(screen.getByTestId('virtual-list')).toBeInTheDocument();
  });

  it('should handle empty customer list', () => {
    render(<VirtualizedCustomerTable {...defaultProps} customers={[]} />);
    expect(screen.getByTestId('virtual-list')).toBeInTheDocument();
  });

  it('should handle different heights', () => {
    const { container } = render(<VirtualizedCustomerTable {...defaultProps} height={800} />);
    const list = container.querySelector('[data-testid="virtual-list"]');
    expect(list).toHaveStyle({ height: '800px' });
  });

  it('should render with custom columns', () => {
    const columns = [
      { field: 'companyName', label: 'Company', visible: true },
      { field: 'email', label: 'Email', visible: true },
    ];
    render(<VirtualizedCustomerTable {...defaultProps} columns={columns} />);
    expect(screen.getByTestId('virtual-list')).toBeInTheDocument();
  });

  it('should handle customers with missing data', () => {
    const customersWithMissingData = [
      {
        id: '1',
        companyName: null,
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

    // Should render without crashing
    expect(screen.getByTestId('virtual-list')).toBeInTheDocument();
  });

  it('should handle invalid date formats', () => {
    const customersWithBadDates = [
      {
        id: '1',
        companyName: 'Test Company',
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

    // Should render without crashing
    expect(screen.getByTestId('virtual-list')).toBeInTheDocument();
  });
});