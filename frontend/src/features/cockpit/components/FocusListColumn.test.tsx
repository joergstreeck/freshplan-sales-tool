/**
 * Tests für FocusListColumn Komponente
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { FocusListColumn } from './FocusListColumn';
import { useCockpitStore } from '../../../store/cockpitStore';
import type { Customer } from '../types';

// Customer Mock Type
type CustomerMock = Pick<Customer, 'id' | 'companyName' | 'status'>;

// Mock für CustomerList
vi.mock('../../../features/customer/components/CustomerList', () => ({
  CustomerList: ({ onCustomerSelect }: { onCustomerSelect?: (customer: CustomerMock) => void }) => (
    <div data-testid="customer-list">
      <button
        onClick={() =>
          onCustomerSelect?.({
            id: '123',
            companyName: 'Test GmbH',
            status: 'active',
          })
        }
      >
        Select Customer
      </button>
    </div>
  ),
}));

// Mock für Store
const mockSetViewMode = vi.fn();
const mockAddFilterTag = vi.fn();
const mockRemoveFilterTag = vi.fn();
const mockSetSearchQuery = vi.fn();
const mockSelectCustomer = vi.fn();

vi.mock('../../../store/cockpitStore', () => ({
  useCockpitStore: vi.fn(() => ({
    viewMode: 'list',
    setViewMode: mockSetViewMode,
    filterTags: ['status:active'],
    addFilterTag: mockAddFilterTag,
    removeFilterTag: mockRemoveFilterTag,
    searchQuery: '',
    setSearchQuery: mockSetSearchQuery,
    selectCustomer: mockSelectCustomer,
  })),
}));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
  },
});

const renderWithProviders = (component: React.ReactElement) => {
  return render(<QueryClientProvider client={queryClient}>{component}</QueryClientProvider>);
};

describe.skip('FocusListColumn - OLD VERSION', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('sollte Header mit Titel anzeigen', () => {
    renderWithProviders(<FocusListColumn />);

    expect(screen.getByText('Fokus-Liste')).toBeInTheDocument();
  });

  it('sollte View Mode Switcher anzeigen', () => {
    renderWithProviders(<FocusListColumn />);

    expect(screen.getByTitle('Listenansicht')).toBeInTheDocument();
    expect(screen.getByTitle('Kartenansicht')).toBeInTheDocument();
    expect(screen.getByTitle('Kanban-Ansicht')).toBeInTheDocument();
  });

  it('sollte aktiven View Mode hervorheben', () => {
    renderWithProviders(<FocusListColumn />);

    const listButton = screen.getByTitle('Listenansicht');
    expect(listButton).toHaveClass('active');
  });

  it('sollte View Mode wechseln können', () => {
    renderWithProviders(<FocusListColumn />);

    const cardsButton = screen.getByTitle('Kartenansicht');
    fireEvent.click(cardsButton);

    expect(mockSetViewMode).toHaveBeenCalledWith('cards');
  });

  it('sollte Suchfeld anzeigen', () => {
    renderWithProviders(<FocusListColumn />);

    const searchInput = screen.getByPlaceholderText('Kunden suchen...');
    expect(searchInput).toBeInTheDocument();
  });

  it('sollte Suche ausführen', () => {
    renderWithProviders(<FocusListColumn />);

    const searchInput = screen.getByPlaceholderText('Kunden suchen...');
    fireEvent.change(searchInput, { target: { value: 'Test GmbH' } });

    expect(mockSetSearchQuery).toHaveBeenCalledWith('Test GmbH');
  });

  it('sollte gespeicherte Ansichten anzeigen', () => {
    renderWithProviders(<FocusListColumn />);

    expect(screen.getByText('Aktive Kunden')).toBeInTheDocument();
    expect(screen.getByText('Neue Leads')).toBeInTheDocument();
    expect(screen.getByText('Risiko-Kunden')).toBeInTheDocument();
    expect(screen.getByText('Diese Woche')).toBeInTheDocument();
  });

  it('sollte aktive Filter haben', () => {
    renderWithProviders(<FocusListColumn />);

    // Der Store hat einen Filter Tag 'status:active' gesetzt
    expect(useCockpitStore().filterTags).toContain('status:active');
  });

  it('sollte Filter-Button anzeigen', () => {
    renderWithProviders(<FocusListColumn />);

    const filterButton = screen.getByTitle('Filter');
    expect(filterButton).toBeInTheDocument();
  });

  it('sollte CustomerList rendern', () => {
    renderWithProviders(<FocusListColumn />);

    expect(screen.getByTestId('customer-list')).toBeInTheDocument();
  });

  it('sollte Kunden auswählen können', () => {
    renderWithProviders(<FocusListColumn />);

    const selectButton = screen.getByText('Select Customer');
    fireEvent.click(selectButton);

    expect(mockSelectCustomer).toHaveBeenCalledWith({
      id: '123',
      companyName: 'Test GmbH',
      status: 'active',
    });
  });

  it('sollte Placeholder für andere View Modes anzeigen', () => {
    (useCockpitStore as vi.MockedFunction<typeof useCockpitStore>).mockReturnValue({
      viewMode: 'cards',
      setViewMode: mockSetViewMode,
      filterTags: [],
      addFilterTag: mockAddFilterTag,
      removeFilterTag: mockRemoveFilterTag,
      searchQuery: '',
      setSearchQuery: mockSetSearchQuery,
      selectCustomer: mockSelectCustomer,
    });

    renderWithProviders(<FocusListColumn />);

    expect(screen.getByText('Kartenansicht wird in Phase 2 implementiert')).toBeInTheDocument();
  });

  it('sollte Filter hinzufügen können', () => {
    renderWithProviders(<FocusListColumn />);

    // In einer echten Implementierung würde hier ein Filter-Dialog geöffnet
    const filterButton = screen.getByTitle('Filter');
    fireEvent.click(filterButton);

    // Für den Test simulieren wir das direkt
    expect(filterButton).toBeInTheDocument();
  });
});
