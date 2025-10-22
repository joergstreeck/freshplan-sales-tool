import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useCustomerDetails } from '../../features/customer/hooks/useCustomerDetails';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CustomerDetailPage } from '../CustomerDetailPage';
import { AuthContext } from '../../contexts/AuthContext';
import { ThemeProvider } from '@mui/material';
import freshfoodzTheme from '../../theme/freshfoodz';
import { customerApi } from '../../features/customer/api/customerApi';

// Mock navigate at module scope
const mockNavigate = vi.fn();

// Mock react-router-dom
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock the customer API
vi.mock('../../features/customer/hooks/useCustomerDetails', () => ({
  useCustomerDetails: vi.fn(),
}));

// Mock the customerApi (Sprint 2.1.7.4 - Manual Activation)
vi.mock('../../features/customer/api/customerApi', () => ({
  customerApi: {
    activateCustomer: vi.fn(),
  },
}));

// Mock the EntityAuditTimeline component
vi.mock('../../features/audit/components/EntityAuditTimeline', () => ({
  EntityAuditTimeline: ({ entityType, entityId }: unknown) => (
    <div data-testid="audit-timeline">
      Audit Timeline for {entityType} {entityId}
    </div>
  ),
}));

const mockCustomer = {
  id: 'test-123',
  companyName: 'Test GmbH',
  industry: 'IT',
  city: 'Berlin',
  postalCode: '10115',
  street: 'Teststraße 1',
  customerType: 'premium',
  employeeCount: 50,
  website: 'https://test.de',
  notes: 'Test notes',
  createdAt: '2024-01-01T10:00:00Z',
  updatedAt: '2024-01-02T10:00:00Z',
};

const createWrapper = (user: unknown = null) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={freshfoodzTheme}>
        <AuthContext.Provider
          value={{
            user,
            isAuthenticated: !!user,
            login: vi.fn(),
            logout: vi.fn(),
            isLoading: false,
            token: 'test-token',
          }}
        >
          <MemoryRouter initialEntries={['/customers/test-123']}>
            <Routes>
              <Route path="/customers/:customerId" element={children} />
            </Routes>
          </MemoryRouter>
        </AuthContext.Provider>
      </ThemeProvider>
    </QueryClientProvider>
  );
};

describe('CustomerDetailPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();
    // Mock window.scrollTo
    window.scrollTo = vi.fn();
  });

  it('should render loading state initially', () => {
    useCustomerDetails.mockReturnValue({
      data: null,
      isLoading: true,
      error: null,
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should render error state when customer not found', () => {
    useCustomerDetails.mockReturnValue({
      data: null,
      isLoading: false,
      error: new Error('Customer not found'),
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    expect(screen.getByText(/Kunde konnte nicht geladen werden/i)).toBeInTheDocument();
    expect(screen.getByText(/Zur Kundenliste/i)).toBeInTheDocument();
  });

  it('should render customer details when loaded', async () => {
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    await waitFor(() => {
      // Use getAllByText since company name appears multiple times
      const companyNames = screen.getAllByText('Test GmbH');
      expect(companyNames.length).toBeGreaterThan(0);
      const berlinElements = screen.getAllByText(/Berlin/);
      expect(berlinElements.length).toBeGreaterThan(0);
      const itElements = screen.getAllByText(/IT/);
      expect(itElements.length).toBeGreaterThan(0);
    });
  });

  it('should show all tabs for authorized users', async () => {
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    const adminUser = {
      id: 'admin-1',
      name: 'Admin User',
      email: 'admin@test.de',
      roles: ['admin'],
    };

    render(<CustomerDetailPage />, { wrapper: createWrapper(adminUser) });

    await waitFor(() => {
      expect(screen.getByText('Übersicht')).toBeInTheDocument();
      expect(screen.getByText('Kontakte')).toBeInTheDocument();
      expect(screen.getByText('Aktivitäten')).toBeInTheDocument();
      expect(screen.getByText('Änderungshistorie')).toBeInTheDocument();
    });
  });

  it('should hide audit tab for unauthorized users', async () => {
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    const salesUser = {
      id: 'sales-1',
      name: 'Sales User',
      email: 'sales@test.de',
      roles: ['sales'],
    };

    render(<CustomerDetailPage />, { wrapper: createWrapper(salesUser) });

    await waitFor(() => {
      expect(screen.getByText('Übersicht')).toBeInTheDocument();
      expect(screen.getByText('Kontakte')).toBeInTheDocument();
      expect(screen.getByText('Aktivitäten')).toBeInTheDocument();
      expect(screen.queryByText('Änderungshistorie')).not.toBeInTheDocument();
    });
  });

  it('should switch between tabs', async () => {
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText('Unternehmensdaten')).toBeInTheDocument();
    });

    // Click on Contacts tab
    const contactsTab = screen.getByText('Kontakte');
    fireEvent.click(contactsTab);

    await waitFor(() => {
      // Tab should be selected (aria-selected or similar)
      expect(contactsTab).toHaveAttribute('aria-selected', 'true');
    });

    // Click on Activities tab
    const activitiesTab = screen.getByText('Aktivitäten');
    fireEvent.click(activitiesTab);

    await waitFor(() => {
      expect(activitiesTab).toHaveAttribute('aria-selected', 'true');
    });
  });

  it('should show audit timeline for authorized users', async () => {
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    const managerUser = {
      id: 'manager-1',
      name: 'Manager User',
      email: 'manager@test.de',
      roles: ['manager'],
    };

    render(<CustomerDetailPage />, { wrapper: createWrapper(managerUser) });

    await waitFor(() => {
      expect(screen.getByText('Änderungshistorie')).toBeInTheDocument();
    });

    // Click on Audit tab
    const auditTab = screen.getByText('Änderungshistorie');
    fireEvent.click(auditTab);

    await waitFor(() => {
      expect(screen.getByTestId('audit-timeline')).toBeInTheDocument();
      expect(screen.getByText(/Audit Timeline for customer test-123/i)).toBeInTheDocument();
    });
  });

  it('should display customer overview details correctly', async () => {
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    await waitFor(() => {
      // Company data
      expect(screen.getByText('Unternehmensdaten')).toBeInTheDocument();
      const companyNames = screen.getAllByText('Test GmbH');
      expect(companyNames.length).toBeGreaterThan(0);
      const premiumElements = screen.getAllByText(/premium/i);
      expect(premiumElements.length).toBeGreaterThan(0);

      // Address data
      expect(screen.getByText('Adresse')).toBeInTheDocument();
      expect(screen.getByText('Teststraße 1')).toBeInTheDocument();
      expect(screen.getByText('10115')).toBeInTheDocument();

      // Notes
      expect(screen.getByText('Notizen')).toBeInTheDocument();
      expect(screen.getByText('Test notes')).toBeInTheDocument();
    });
  });

  it('should navigate back to customers list', async () => {
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    await waitFor(() => {
      const backButton = screen.getByText('Zurück');
      expect(backButton).toBeInTheDocument();
    });
  });

  // ========================================================================
  // Sprint 2.1.7.4 - Manual Activation Button Tests
  // ========================================================================

  describe('Manual Activation Button (Sprint 2.1.7.4)', () => {
    const mockProspectCustomer = {
      ...mockCustomer,
      status: 'PROSPECT',
    };

    beforeEach(() => {
      vi.clearAllMocks();
    });

    it('should show PROSPECT alert when customer status is PROSPECT', async () => {
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(screen.getByText(/Kunde wartet auf erste Bestellung/i)).toBeInTheDocument();
        expect(
          screen.getByText(/Erste Bestellung geliefert → AKTIV markieren/i)
        ).toBeInTheDocument();
      });
    });

    it('should NOT show PROSPECT alert when customer status is AKTIV', async () => {
      const aktivCustomer = { ...mockCustomer, status: 'AKTIV' };
      useCustomerDetails.mockReturnValue({
        data: aktivCustomer,
        isLoading: false,
        error: null,
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(screen.queryByText(/Kunde wartet auf erste Bestellung/i)).not.toBeInTheDocument();
        expect(
          screen.queryByText(/Erste Bestellung geliefert → AKTIV markieren/i)
        ).not.toBeInTheDocument();
      });
    });

    it('should open activation dialog when button is clicked', async () => {
      const user = userEvent.setup();
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(
          screen.getByText(/Erste Bestellung geliefert → AKTIV markieren/i)
        ).toBeInTheDocument();
      });

      const activateButton = screen.getByRole('button', {
        name: /Erste Bestellung geliefert → AKTIV markieren/i,
      });
      await user.click(activateButton);

      await waitFor(() => {
        expect(screen.getByText(/Kunde als AKTIV markieren/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Bestellnummer \(optional\)/i)).toBeInTheDocument();
      });
    });

    it('should close activation dialog when cancel is clicked', async () => {
      const user = userEvent.setup();
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      // Open dialog
      await waitFor(() => {
        expect(
          screen.getByText(/Erste Bestellung geliefert → AKTIV markieren/i)
        ).toBeInTheDocument();
      });
      const activateButton = screen.getByRole('button', {
        name: /Erste Bestellung geliefert → AKTIV markieren/i,
      });
      await user.click(activateButton);

      await waitFor(() => {
        expect(screen.getByText(/Kunde als AKTIV markieren/i)).toBeInTheDocument();
      });

      // Click cancel
      const cancelButton = screen.getByRole('button', { name: /Abbrechen/i });
      await user.click(cancelButton);

      await waitFor(() => {
        expect(screen.queryByText(/Kunde als AKTIV markieren/i)).not.toBeInTheDocument();
      });
    });

    it('should allow entering order number in dialog', async () => {
      const user = userEvent.setup();
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      // Open dialog
      const activateButton = screen.getByRole('button', {
        name: /Erste Bestellung geliefert → AKTIV markieren/i,
      });
      await user.click(activateButton);

      await waitFor(() => {
        expect(screen.getByLabelText(/Bestellnummer \(optional\)/i)).toBeInTheDocument();
      });

      // Enter order number
      const orderNumberInput = screen.getByLabelText(/Bestellnummer \(optional\)/i);
      await user.type(orderNumberInput, 'BE-2025-001');

      expect(orderNumberInput).toHaveValue('BE-2025-001');
    });

    it('should successfully activate customer without order number', async () => {
      const user = userEvent.setup();
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      // Mock successful activation
      vi.mocked(customerApi.activateCustomer).mockResolvedValue({
        ...mockProspectCustomer,
        status: 'AKTIV',
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      // Open dialog
      const activateButton = screen.getByRole('button', {
        name: /Erste Bestellung geliefert → AKTIV markieren/i,
      });
      await user.click(activateButton);

      await waitFor(() => {
        expect(screen.getByText(/Kunde als AKTIV markieren/i)).toBeInTheDocument();
      });

      // Submit without order number
      const submitButton = screen.getByRole('button', { name: /Ja, als AKTIV markieren/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(customerApi.activateCustomer).toHaveBeenCalledWith('test-123', undefined);
      });

      // Success message should appear
      await waitFor(() => {
        expect(screen.getByText(/Kunde erfolgreich aktiviert/i)).toBeInTheDocument();
      });
    });

    it('should successfully activate customer with order number', async () => {
      const user = userEvent.setup();
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      // Mock successful activation
      vi.mocked(customerApi.activateCustomer).mockResolvedValue({
        ...mockProspectCustomer,
        status: 'AKTIV',
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      // Open dialog
      const activateButton = screen.getByRole('button', {
        name: /Erste Bestellung geliefert → AKTIV markieren/i,
      });
      await user.click(activateButton);

      // Enter order number
      const orderNumberInput = screen.getByLabelText(/Bestellnummer \(optional\)/i);
      await user.type(orderNumberInput, 'BE-2025-001');

      // Submit with order number
      const submitButton = screen.getByRole('button', { name: /Ja, als AKTIV markieren/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(customerApi.activateCustomer).toHaveBeenCalledWith('test-123', 'BE-2025-001');
      });

      // Success message should appear
      await waitFor(() => {
        expect(screen.getByText(/Kunde erfolgreich aktiviert/i)).toBeInTheDocument();
      });
    });

    it('should display error message when activation fails', async () => {
      const user = userEvent.setup();
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      // Mock API error
      vi.mocked(customerApi.activateCustomer).mockRejectedValue({
        response: {
          data: {
            message: 'Kunde kann nicht aktiviert werden - fehlende Daten',
          },
        },
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      // Open dialog
      const activateButton = screen.getByRole('button', {
        name: /Erste Bestellung geliefert → AKTIV markieren/i,
      });
      await user.click(activateButton);

      // Submit
      const submitButton = screen.getByRole('button', { name: /Ja, als AKTIV markieren/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(
          screen.getByText(/Kunde kann nicht aktiviert werden - fehlende Daten/i)
        ).toBeInTheDocument();
      });
    });

    it('should display generic error message when API error has no message', async () => {
      const user = userEvent.setup();
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      // Mock generic API error
      vi.mocked(customerApi.activateCustomer).mockRejectedValue(new Error('Network error'));

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      // Open dialog
      const activateButton = screen.getByRole('button', {
        name: /Erste Bestellung geliefert → AKTIV markieren/i,
      });
      await user.click(activateButton);

      // Submit
      const submitButton = screen.getByRole('button', { name: /Ja, als AKTIV markieren/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/Fehler beim Aktivieren des Kunden/i)).toBeInTheDocument();
      });
    });

    it('should display info about what happens when activating', async () => {
      const user = userEvent.setup();
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      // Open dialog
      const activateButton = screen.getByRole('button', {
        name: /Erste Bestellung geliefert → AKTIV markieren/i,
      });
      await user.click(activateButton);

      await waitFor(() => {
        expect(screen.getByText(/Was passiert:/i)).toBeInTheDocument();
        expect(screen.getByText(/Status wird von/i)).toBeInTheDocument();
        // Check that PROSPECT and AKTIV appear (multiple times is OK, as long as they appear)
        expect(screen.getAllByText(/PROSPECT/i).length).toBeGreaterThan(0);
        expect(screen.getAllByText(/AKTIV/i).length).toBeGreaterThan(0);
        expect(
          screen.getByText(/Kunde erscheint in Dashboard als "Aktiver Kunde"/i)
        ).toBeInTheDocument();
        expect(screen.getByText(/Aktion wird im Audit-Log protokolliert/i)).toBeInTheDocument();
      });
    });

    it('should show Xentral-Integration note in PROSPECT alert', async () => {
      useCustomerDetails.mockReturnValue({
        data: mockProspectCustomer,
        isLoading: false,
        error: null,
      });

      render(<CustomerDetailPage />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(
          screen.getByText(/Wird automatisch via Xentral-Integration erfolgen - Sprint 2.1.7.2/i)
        ).toBeInTheDocument();
      });
    });
  });
});
