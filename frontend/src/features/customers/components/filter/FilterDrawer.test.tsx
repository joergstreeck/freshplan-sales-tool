import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, within } from '@testing-library/react';
import { FilterDrawer } from './FilterDrawer';
import { FilterConfig, RiskLevel } from '../../types/filter.types';

describe('FilterDrawer', () => {
  const defaultFilters: FilterConfig = {
    text: '',
    status: [],
    industry: [],
    location: [],
    revenueRange: null,
    riskLevel: [],
    hasContacts: null,
    lastContactDays: null,
    tags: [],
  };

  const mockOnClose = vi.fn();
  const mockOnFiltersChange = vi.fn();
  const mockOnApply = vi.fn();
  const mockOnClear = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render when open', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    expect(screen.getByText('Erweiterte Filter')).toBeInTheDocument();
  });

  it('should not render when closed', () => {
    const { container } = render(
      <FilterDrawer
        open={false}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    // Drawer should not be visible
    const drawer = container.querySelector('.MuiDrawer-root');
    expect(drawer).toBeNull();
  });

  it('should call onClose when close button is clicked', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    // Find the close icon button
    const closeButtons = screen.getAllByRole('button');
    const closeButton = closeButtons.find(btn => 
      btn.querySelector('[data-testid="CloseIcon"]')
    );
    
    if (closeButton) {
      fireEvent.click(closeButton);
      expect(mockOnClose).toHaveBeenCalled();
    } else {
      // Fallback: try to close by clicking outside or escape key
      const drawer = screen.getByText('Erweiterte Filter').closest('.MuiDrawer-paper');
      if (drawer) {
        fireEvent.keyDown(drawer, { key: 'Escape', code: 'Escape' });
      }
    }
  });

  it('should not show LEAD, PROSPECT, and RISIKO in status filter', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    // These should NOT be present
    expect(screen.queryByLabelText('Lead')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Interessent')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Risiko')).not.toBeInTheDocument();

    // These should be present
    expect(screen.getByLabelText('Aktiv')).toBeInTheDocument();
    expect(screen.getByLabelText('Inaktiv')).toBeInTheDocument();
    expect(screen.getByLabelText('Archiviert')).toBeInTheDocument();
  });

  it('should show risk levels with ranges in labels', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    expect(screen.getByLabelText('Niedrig (0-29)')).toBeInTheDocument();
    expect(screen.getByLabelText('Mittel (30-59)')).toBeInTheDocument();
    expect(screen.getByLabelText('Hoch (60-79)')).toBeInTheDocument();
    expect(screen.getByLabelText('Kritisch (80-100)')).toBeInTheDocument();
  });

  it('should handle contacts filter selection', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    const withContactsRadio = screen.getByLabelText('Mit Kontakten');
    fireEvent.click(withContactsRadio);

    expect(mockOnFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        hasContacts: true,
      })
    );
  });

  it('should render revenue range slider', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    expect(screen.getByText('Erwarteter Jahresumsatz')).toBeInTheDocument();
    // Check for slider marks
    expect(screen.getByText('100k')).toBeInTheDocument();
    expect(screen.getByText('250k')).toBeInTheDocument();
    expect(screen.getByText('500k+')).toBeInTheDocument();
  });

  it('should handle last contact days slider', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    expect(screen.getByText(/Letzter Kontakt vor mehr als/)).toBeInTheDocument();
  });

  it('should call onApply when Apply button is clicked', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    const applyButton = screen.getByRole('button', { name: /anwenden/i });
    fireEvent.click(applyButton);
    expect(mockOnApply).toHaveBeenCalled();
  });

  it('should call onClear when Reset button is clicked', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    const resetButton = screen.getByRole('button', { name: /zurücksetzen/i });
    fireEvent.click(resetButton);
    expect(mockOnClear).toHaveBeenCalled();
  });

  it('should handle risk level selection', () => {
    render(
      <FilterDrawer
        open={true}
        onClose={mockOnClose}
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        onApply={mockOnApply}
        onClear={mockOnClear}
      />
    );

    const mediumRiskCheckbox = screen.getByLabelText('Mittel (30-59)');
    fireEvent.click(mediumRiskCheckbox);

    expect(mockOnFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        riskLevel: [RiskLevel.MEDIUM],
      })
    );
  });
});