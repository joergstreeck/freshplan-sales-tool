/**
 * Tests for IntelligentFilterBar Component
 * 
 * @module IntelligentFilterBar.test
 * @since FC-005 PR4
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { IntelligentFilterBar } from '../IntelligentFilterBar';
import type { FilterConfig, SortConfig, ColumnConfig } from '../../../types/filter.types';

// Mock the hello-pangea/dnd module
vi.mock('@hello-pangea/dnd', () => ({
  DragDropContext: ({ children }: any) => children,
  Droppable: ({ children }: any) => children({ 
    draggableProps: {}, 
    dragHandleProps: {}, 
    innerRef: () => {} 
  }),
  Draggable: ({ children }: any) => children({
    draggableProps: {},
    dragHandleProps: {},
    innerRef: () => {},
  }, { isDragging: false }),
}));

const theme = createTheme();

describe('IntelligentFilterBar', () => {
  const mockOnFilterChange = vi.fn();
  const mockOnSortChange = vi.fn();
  const mockOnColumnChange = vi.fn();
  const mockOnExport = vi.fn();

  const defaultProps = {
    onFilterChange: mockOnFilterChange,
    onSortChange: mockOnSortChange,
    onColumnChange: mockOnColumnChange,
    onExport: mockOnExport,
    totalCount: 100,
    filteredCount: 50,
    loading: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    // Mock localStorage
    const localStorageMock: Storage = {
      getItem: vi.fn(),
      setItem: vi.fn(),
      removeItem: vi.fn(),
      clear: vi.fn(),
      length: 0,
      key: vi.fn(),
    };
    Object.defineProperty(window, 'localStorage', {
      value: localStorageMock,
      writable: true,
    });
  });

  const renderComponent = (props = {}) => {
    return render(
      <ThemeProvider theme={theme}>
        <IntelligentFilterBar {...defaultProps} {...props} />
      </ThemeProvider>
    );
  };

  it('should render the search input', () => {
    renderComponent();
    const searchInput = screen.getByPlaceholderText(/Suche nach Kunden/i);
    expect(searchInput).toBeInTheDocument();
  });

  it('should call onFilterChange when searching', async () => {
    const user = userEvent.setup();
    renderComponent();
    
    const searchInput = screen.getByPlaceholderText(/Suche nach Kunden/i);
    await user.type(searchInput, 'test');
    
    // Wait for debounce
    await waitFor(() => {
      expect(mockOnFilterChange).toHaveBeenCalledWith(
        expect.objectContaining({
          text: 'test',
        })
      );
    }, { timeout: 500 });
  });

  it('should clear search when clear button is clicked', async () => {
    const user = userEvent.setup();
    renderComponent();
    
    const searchInput = screen.getByPlaceholderText(/Suche nach Kunden/i) as HTMLInputElement;
    await user.type(searchInput, 'test');
    
    // Wait for clear button to appear
    await waitFor(() => {
      const clearButton = screen.getByRole('button', { name: '' });
      expect(clearButton).toBeInTheDocument();
    });
    
    const clearButton = screen.getByRole('button', { name: '' });
    await user.click(clearButton);
    
    expect(searchInput.value).toBe('');
  });

  it('should show filter count badge when filters are active', () => {
    renderComponent();
    
    // Initially no badge
    const filterButton = screen.getByLabelText(/Erweiterte Filter/i);
    expect(filterButton).toBeInTheDocument();
    
    // TODO: Add test for active filter count after implementing filter drawer interaction
  });

  it('should render quick filter chips', () => {
    renderComponent();
    
    expect(screen.getByText('Aktive Kunden')).toBeInTheDocument();
    expect(screen.getByText('Risiko-Kunden')).toBeInTheDocument();
    expect(screen.getByText('Lange kein Kontakt')).toBeInTheDocument();
    expect(screen.getByText('Top-Kunden')).toBeInTheDocument();
    expect(screen.getByText('Neue Kunden')).toBeInTheDocument();
  });

  it('should apply quick filter when chip is clicked', async () => {
    const user = userEvent.setup();
    renderComponent();
    
    const activeCustomersChip = screen.getByText('Aktive Kunden');
    await user.click(activeCustomersChip);
    
    expect(mockOnFilterChange).toHaveBeenCalledWith(
      expect.objectContaining({
        status: ['ACTIVE'],
      })
    );
  });

  it('should show filter summary when filters are active', async () => {
    const user = userEvent.setup();
    renderComponent();
    
    // Apply a quick filter
    const activeCustomersChip = screen.getByText('Aktive Kunden');
    await user.click(activeCustomersChip);
    
    // Check for filter summary
    await waitFor(() => {
      expect(screen.getByText(/50 von 100 Kunden gefiltert/i)).toBeInTheDocument();
    });
  });

  it('should open export menu when export button is clicked', async () => {
    const user = userEvent.setup();
    renderComponent();
    
    const exportButton = screen.getByLabelText(/Exportieren/i);
    await user.click(exportButton);
    
    // Check if export menu items appear
    await waitFor(() => {
      expect(screen.getByText('CSV Export')).toBeInTheDocument();
      expect(screen.getByText('Excel Export')).toBeInTheDocument();
      expect(screen.getByText('PDF Export')).toBeInTheDocument();
      expect(screen.getByText('JSON Export')).toBeInTheDocument();
    });
  });

  it('should call onExport with correct format when export option is selected', async () => {
    const user = userEvent.setup();
    renderComponent();
    
    const exportButton = screen.getByLabelText(/Exportieren/i);
    await user.click(exportButton);
    
    const csvExport = await screen.findByText('CSV Export');
    await user.click(csvExport);
    
    expect(mockOnExport).toHaveBeenCalledWith('csv');
  });

  it('should disable search input when loading', () => {
    renderComponent({ loading: true });
    
    const searchInput = screen.getByPlaceholderText(/Suche nach Kunden/i) as HTMLInputElement;
    expect(searchInput).toBeDisabled();
  });

  it('should not render export button when onExport is not provided', () => {
    renderComponent({ onExport: undefined });
    
    const exportButton = screen.queryByLabelText(/Exportieren/i);
    expect(exportButton).not.toBeInTheDocument();
  });
});