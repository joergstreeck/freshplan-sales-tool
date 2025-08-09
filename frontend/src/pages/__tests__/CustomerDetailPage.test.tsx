import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CustomerDetailPage } from '../CustomerDetailPage';
import { AuthContext } from '../../contexts/AuthContext';
import { ThemeProvider } from '@mui/material';
import freshfoodzTheme from '../../theme/freshfoodz';

// Mock the customer API
vi.mock('../../features/customer/hooks/useCustomerDetails', () => ({
  useCustomerDetails: vi.fn(),
}));

// Mock the EntityAuditTimeline component
vi.mock('../../features/audit/components/EntityAuditTimeline', () => ({
  EntityAuditTimeline: ({ entityType, entityId }: any) => (
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

const createWrapper = (user: any = null) => {
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
  });

  it('should render loading state initially', () => {
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
    useCustomerDetails.mockReturnValue({
      data: null,
      isLoading: true,
      error: null,
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should render error state when customer not found', () => {
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
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
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      expect(screen.getByText(/Berlin/)).toBeInTheDocument();
      expect(screen.getByText(/IT/)).toBeInTheDocument();
    });
  });

  it('should show all tabs for authorized users', async () => {
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
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
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
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
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
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
      expect(screen.getByText(/Die Kontaktverwaltung wird in Sprint 3 implementiert/i)).toBeInTheDocument();
    });

    // Click on Activities tab
    const activitiesTab = screen.getByText('Aktivitäten');
    fireEvent.click(activitiesTab);

    await waitFor(() => {
      expect(screen.getByText(/Das Aktivitäten-Tracking wird in Sprint 4 implementiert/i)).toBeInTheDocument();
    });
  });

  it('should show audit timeline for authorized users', async () => {
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
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
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    await waitFor(() => {
      // Company data
      expect(screen.getByText('Unternehmensdaten')).toBeInTheDocument();
      expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      expect(screen.getByText(/premium/i)).toBeInTheDocument();
      
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
    const { useCustomerDetails } = require('../../features/customer/hooks/useCustomerDetails');
    useCustomerDetails.mockReturnValue({
      data: mockCustomer,
      isLoading: false,
      error: null,
    });

    const mockNavigate = vi.fn();
    vi.mock('react-router-dom', async () => {
      const actual = await vi.importActual('react-router-dom');
      return {
        ...actual,
        useNavigate: () => mockNavigate,
      };
    });

    render(<CustomerDetailPage />, { wrapper: createWrapper() });

    await waitFor(() => {
      const backButton = screen.getByText('Zurück');
      expect(backButton).toBeInTheDocument();
    });
  });
});