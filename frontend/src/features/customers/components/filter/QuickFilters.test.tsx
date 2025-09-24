import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QuickFilters, QUICK_FILTERS } from './QuickFilters';
import { CustomerStatus } from '../../../customer/types/customer.types';

describe('QuickFilters', () => {
  const mockOnToggleQuickFilter = vi.fn();

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render all quick filter chips', () => {
    render(<QuickFilters activeQuickFilters={[]} onToggleQuickFilter={mockOnToggleQuickFilter} />);

    expect(screen.getByText('Aktive Kunden')).toBeInTheDocument();
    expect(screen.getByText('Inaktive Kunden')).toBeInTheDocument();
  });

  it('should only show Active and Inactive filters', () => {
    render(<QuickFilters activeQuickFilters={[]} onToggleQuickFilter={mockOnToggleQuickFilter} />);

    // Should only have 2 filters
    const chips = screen.getAllByRole('button');
    expect(chips).toHaveLength(2);

    // Should not have these filters anymore
    expect(screen.queryByText('Risiko-Kunden')).not.toBeInTheDocument();
    expect(screen.queryByText('Neue Kunden')).not.toBeInTheDocument();
    expect(screen.queryByText('Top-Kunden')).not.toBeInTheDocument();
    expect(screen.queryByText('Lange kein Kontakt')).not.toBeInTheDocument();
  });

  it('should highlight active filters', () => {
    render(
      <QuickFilters activeQuickFilters={['active']} onToggleQuickFilter={mockOnToggleQuickFilter} />
    );

    const activeChip = screen.getByText('Aktive Kunden').closest('div');
    const inactiveChip = screen.getByText('Inaktive Kunden').closest('div');

    // Active chip should have primary color
    expect(activeChip?.className).toContain('MuiChip-filled');
    expect(activeChip?.className).toContain('MuiChip-colorPrimary');

    // Inactive chip should be outlined
    expect(inactiveChip?.className).toContain('MuiChip-outlined');
  });

  it('should call onToggleQuickFilter when chip is clicked', () => {
    render(<QuickFilters activeQuickFilters={[]} onToggleQuickFilter={mockOnToggleQuickFilter} />);

    const activeCustomersChip = screen.getByText('Aktive Kunden');
    fireEvent.click(activeCustomersChip);

    expect(mockOnToggleQuickFilter).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 'active',
        label: 'Aktive Kunden',
        filter: { status: [CustomerStatus.AKTIV] },
      })
    );
  });

  it('should toggle multiple filters independently', () => {
    const { rerender } = render(
      <QuickFilters activeQuickFilters={[]} onToggleQuickFilter={mockOnToggleQuickFilter} />
    );

    // Click active filter
    fireEvent.click(screen.getByText('Aktive Kunden'));

    // Rerender with active filter selected
    rerender(
      <QuickFilters activeQuickFilters={['active']} onToggleQuickFilter={mockOnToggleQuickFilter} />
    );

    // Click inactive filter
    fireEvent.click(screen.getByText('Inaktive Kunden'));

    expect(mockOnToggleQuickFilter).toHaveBeenCalledTimes(2);
    expect(mockOnToggleQuickFilter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        id: 'inactive',
        label: 'Inaktive Kunden',
        filter: { status: [CustomerStatus.INAKTIV] },
      })
    );
  });

  it('should use correct CustomerStatus enum values', () => {
    // Verify that QUICK_FILTERS uses the correct enum values
    expect(QUICK_FILTERS).toHaveLength(2);

    expect(QUICK_FILTERS[0]).toEqual({
      id: 'active',
      label: 'Aktive Kunden',
      icon: expect.anything(),
      filter: { status: [CustomerStatus.AKTIV] },
    });

    expect(QUICK_FILTERS[1]).toEqual({
      id: 'inactive',
      label: 'Inaktive Kunden',
      icon: expect.anything(),
      filter: { status: [CustomerStatus.INAKTIV] },
    });
  });
});
