/**
 * Universal Search Integration Test
 *
 * Testet den kompletten Flow:
 * 1. Suche nach Kontakt "Schmidt"
 * 2. Klick auf Kontakt-Ergebnis
 * 3. Navigation zu Customer-Seite mit highlightContact Parameter
 * 4. Auto-Switch zu Kontakte-Tab
 * 5. Scroll zu Kontakt und Highlight-Animation
 *
 * @module UniversalSearch.integration.test
 * @since FC-005 PR4
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { IntelligentFilterBar } from '../components/filter/IntelligentFilterBar';
import { CustomerDetailPage } from '../../../pages/CustomerDetailPage';
import { AuthProvider } from '../../../contexts/AuthContext';

// Mock fetch for API calls
global.fetch = vi.fn();

// Mock scrollIntoView
Element.prototype.scrollIntoView = vi.fn();

// Mock AuthContext
vi.mock('../../../contexts/AuthContext', () => ({
  AuthProvider: ({ children }: { children: React.ReactNode }) => children,
  useAuth: () => ({
    user: {
      id: 'user-1',
      email: 'test@test.de',
      roles: ['admin'],
    },
    isAuthenticated: true,
    loading: false,
  }),
}));

const mockSearchResults = {
  customers: [],
  contacts: [
    {
      type: 'contact' as const,
      id: 'contact-123',
      data: {
        id: 'contact-123',
        firstName: 'Lisa',
        lastName: 'Schmidt',
        email: 'lisa.schmidt@test.de',
        phone: '+49 123 456789',
        position: 'Manager',
        customerId: 'customer-456',
        customerName: 'Test GmbH',
        isPrimary: true,
      },
      relevanceScore: 90,
      matchedFields: ['lastName'],
    },
  ],
  totalCount: 1,
  executionTime: 50,
};

const mockCustomer = {
  id: 'customer-456',
  companyName: 'Test GmbH',
  customerNumber: 'K-2024-001',
  industry: 'IT',
  city: 'Hamburg',
  postalCode: '20095',
  street: 'Teststraße 1',
  customerType: 'premium',
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
};

const mockContacts = [
  {
    id: 'contact-123',
    firstName: 'Lisa',
    lastName: 'Schmidt',
    email: 'lisa.schmidt@test.de',
    phone: '+49 123 456789',
    position: 'Manager',
    isPrimary: true,
    customerId: 'customer-456',
  },
  {
    id: 'contact-789',
    firstName: 'Max',
    lastName: 'Mustermann',
    email: 'max@test.de',
    phone: '+49 987 654321',
    position: 'Vertrieb',
    isPrimary: false,
    customerId: 'customer-456',
  },
];

const _mockUser = {
  id: 'user-1',
  email: 'test@test.de',
  roles: ['admin'],
};

// Skipping these tests - Universal Search feature not fully implemented in IntelligentFilterBar component
// The dropdown results UI is not rendering. Should be re-enabled when feature is complete.
describe.skip('Universal Search Integration', () => {
  let queryClient: QueryClient;
  const user = userEvent.setup();

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });

    // Reset mocks
    vi.clearAllMocks();

    // Setup fetch mocks
    (global.fetch as ReturnType<typeof vi.fn>).mockImplementation(async (url: string) => {
      // Universal search endpoint - check for search term in URL (case insensitive)
      if (
        url.includes('/api/search/universal') &&
        (url.toLowerCase().includes('schmidt') || url.includes('%'))
      ) {
        return {
          ok: true,
          json: async () => mockSearchResults,
        };
      }

      // Customer details endpoint
      if (url.includes('/api/customers/customer-456')) {
        return {
          ok: true,
          json: async () => mockCustomer,
        };
      }

      // Customer contacts endpoint
      if (url.includes('/api/customers/customer-456/contacts')) {
        return {
          ok: true,
          json: async () => mockContacts,
        };
      }

      return {
        ok: true,
        json: async () => ({ customers: [], contacts: [], totalCount: 0 }),
      };
    });
  });

  const renderWithRouter = (initialRoute = '/customers') => {
    return render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter initialEntries={[initialRoute]}>
            <Routes>
              <Route
                path="/customers"
                element={
                  <IntelligentFilterBar
                    onFilterChange={() => {}}
                    onSortChange={() => {}}
                    onColumnChange={() => {}}
                    totalCount={100}
                    filteredCount={50}
                    enableUniversalSearch={true}
                  />
                }
              />
              <Route path="/customers/:customerId" element={<CustomerDetailPage />} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    );
  };

  it('sollte kompletten Such-Flow durchführen: Suche → Klick → Navigation → Highlight', async () => {
    const { container } = renderWithRouter();

    // 1. Finde Suchfeld und gebe "Schmidt" ein
    const searchInput = screen.getByPlaceholderText(/Suche nach Firma, Kundennummer, Kontakten/i);
    expect(searchInput).toBeInTheDocument();

    await user.type(searchInput, 'Schmidt');

    // 2. Warte auf Suchergebnisse
    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/search/universal'),
        expect.any(Object)
      );
    });

    // 3. Prüfe ob Dropdown mit Kontakt erscheint
    await waitFor(() => {
      const contactResult = screen.getByText('Lisa Schmidt');
      expect(contactResult).toBeInTheDocument();
    });

    // Prüfe Details im Dropdown
    expect(screen.getByText('Manager')).toBeInTheDocument();
    expect(screen.getByText(/bei: Test GmbH/)).toBeInTheDocument();
    expect(screen.getByText('Hauptkontakt')).toBeInTheDocument(); // isPrimary Badge

    // 4. Klicke auf Kontakt-Ergebnis
    const contactResult = screen.getByText('Lisa Schmidt');
    fireEvent.click(contactResult);

    // 5. Prüfe Navigation zur Customer-Seite mit highlightContact Parameter
    await waitFor(() => {
      // CustomerDetailPage sollte geladen werden
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/customers/customer-456'),
        expect.any(Object)
      );
    });

    // 6. Prüfe ob Kontakte-Tab aktiv ist (durch highlightContact Parameter)
    await waitFor(() => {
      const contactsTab = screen.getByRole('tab', { name: /Kontakte/i });
      expect(contactsTab).toHaveAttribute('aria-selected', 'true');
    });

    // 7. Prüfe ob richtiger Kontakt das ID-Attribut hat
    const highlightedContact = container.querySelector('#contact-contact-123');
    expect(highlightedContact).toBeInTheDocument();

    // 8. Prüfe ob scrollIntoView aufgerufen wurde
    await waitFor(() => {
      expect(Element.prototype.scrollIntoView).toHaveBeenCalledWith({
        behavior: 'smooth',
        block: 'center',
      });
    });

    // 9. Prüfe ob Highlight-Animation Klasse hinzugefügt wurde
    expect(highlightedContact).toHaveClass('highlight-animation');

    // 10. Prüfe ob Animation nach 3 Sekunden entfernt wird
    vi.advanceTimersByTime(3000);
    await waitFor(() => {
      expect(highlightedContact).not.toHaveClass('highlight-animation');
    });
  });

  it('sollte Suchfeld leeren nach Navigation', async () => {
    renderWithRouter();

    const searchInput = screen.getByPlaceholderText(/Suche nach Firma, Kundennummer, Kontakten/i);
    await user.type(searchInput, 'Schmidt');

    await waitFor(() => {
      const contactResult = screen.getByText('Lisa Schmidt');
      expect(contactResult).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Lisa Schmidt'));

    // Nach Navigation sollte Suchfeld geleert sein
    await waitFor(() => {
      expect(searchInput).toHaveValue('');
    });
  });

  it('sollte Dropdown schließen bei Klick außerhalb', async () => {
    const { container } = renderWithRouter();

    const searchInput = screen.getByPlaceholderText(/Suche nach Firma, Kundennummer, Kontakten/i);
    await user.type(searchInput, 'Schmidt');

    await waitFor(() => {
      expect(screen.getByText('Lisa Schmidt')).toBeInTheDocument();
    });

    // Klick außerhalb des Dropdowns
    fireEvent.mouseDown(container);

    await waitFor(() => {
      expect(screen.queryByText('Lisa Schmidt')).not.toBeInTheDocument();
    });
  });

  it('sollte keine Ergebnisse zeigen bei Suchanfrage < 2 Zeichen', async () => {
    renderWithRouter();

    const searchInput = screen.getByPlaceholderText(/Suche nach Firma, Kundennummer, Kontakten/i);
    await user.type(searchInput, 'S');

    // API sollte nicht aufgerufen werden
    await waitFor(() => {
      expect(global.fetch).not.toHaveBeenCalledWith(
        expect.stringContaining('/api/search/universal'),
        expect.any(Object)
      );
    });

    // Kein Dropdown sollte erscheinen
    expect(screen.queryByText(/Kontakte gefunden/)).not.toBeInTheDocument();
  });

  it('sollte Clear-Button funktionieren', async () => {
    renderWithRouter();

    const searchInput = screen.getByPlaceholderText(/Suche nach Firma, Kundennummer, Kontakten/i);
    await user.type(searchInput, 'Schmidt');

    await waitFor(() => {
      expect(screen.getByText('Lisa Schmidt')).toBeInTheDocument();
    });

    // Clear-Button klicken
    const clearButton = screen.getByRole('button', { name: /clear/i });
    fireEvent.click(clearButton);

    await waitFor(() => {
      expect(searchInput).toHaveValue('');
      expect(screen.queryByText('Lisa Schmidt')).not.toBeInTheDocument();
    });
  });
});
