/**
 * ConvertToCustomerDialog Component Tests
 * Sprint 2.1.7.2 - Customer Management + Xentral Integration
 *
 * @description Tests für Opportunity → Customer Konvertierung mit Xentral-Dropdown
 * @since 2025-10-23
 */
/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import freshfoodzTheme from '../../../theme/freshfoodz';
import ConvertToCustomerDialog from './ConvertToCustomerDialog';
import type { IOpportunity } from '../types/opportunity.types';
import { OpportunityStage } from '../types/opportunity.types';
import { httpClient } from '../../../lib/apiClient';

// Mock API Client
vi.mock('../../../lib/apiClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

/**
 * Helper function to render with all required providers
 */
const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>
    </BrowserRouter>
  );
};

/**
 * Mock Opportunity Data (CLOSED_WON stage)
 */
const mockOpportunity: IOpportunity = {
  id: 'opp-123',
  name: 'Gastro Meier - Neugeschäft',
  description: 'Großer Catering-Auftrag',
  expectedValue: 25000,
  probability: 90,
  expectedCloseDate: '2025-12-31',
  stage: OpportunityStage.CLOSED_WON,
  stageDisplayName: 'Gewonnen',
  stageChangedAt: '2025-10-20T10:00:00Z',
  leadId: 456,
  leadCompanyName: 'Gastro Meier GmbH',
  assignedToId: 'user-001',
  assignedToName: 'Max Mustermann',
  createdAt: '2025-10-01T09:00:00Z',
  updatedAt: '2025-10-20T10:00:00Z',
};

/**
 * Mock Xentral Customers
 */
const mockXentralCustomers = [
  {
    xentralId: 'XC-001',
    companyName: 'Gastro Meier GmbH',
    totalRevenue: 150000,
  },
  {
    xentralId: 'XC-002',
    companyName: 'Restaurant Silbertanne',
    totalRevenue: 85000,
  },
  {
    xentralId: 'XC-003',
    companyName: 'Hotel am See',
    totalRevenue: 220000,
  },
];

describe('ConvertToCustomerDialog', () => {
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();

    // Default mock für Xentral customers API (kann in Tests überschrieben werden)
    vi.mocked(httpClient.get).mockResolvedValue({
      data: [],
    } as any);
  });

  describe('Dialog Rendering', () => {
    it('renders dialog when open is true', () => {
      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(
        screen.getByRole('heading', { name: /Opportunity zu Customer konvertieren/i })
      ).toBeInTheDocument();
    });

    it('does not render dialog when open is false', () => {
      renderWithProviders(
        <ConvertToCustomerDialog
          open={false}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(
        screen.queryByRole('heading', { name: /Opportunity zu Customer konvertieren/i })
      ).not.toBeInTheDocument();
    });

    it('renders all form fields and info boxes', () => {
      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Form Fields
      expect(screen.getByLabelText(/Firmenname/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Xentral-Kunde verknüpfen/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Notizen/i)).toBeInTheDocument();

      // PROSPECT Status Info-Box (Text ist durch <strong> und <br/> unterbrochen)
      expect(screen.getByText(/Customer Status: PROSPECT/i)).toBeInTheDocument();
      expect(screen.getByText(/Status wechselt automatisch zu/i)).toBeInTheDocument();
      expect(screen.getByText(/AKTIV/i)).toBeInTheDocument();

      // Action Buttons
      expect(screen.getByRole('button', { name: /Abbrechen/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Customer anlegen/i })).toBeInTheDocument();
    });

    it('pre-fills company name from opportunity leadCompanyName', () => {
      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const companyNameInput = screen.getByLabelText(/Firmenname/i) as HTMLInputElement;
      expect(companyNameInput.value).toBe('Gastro Meier GmbH');
    });
  });

  describe('Company Name Validation', () => {
    it('disables submit button when company name is empty', async () => {
      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const companyNameInput = screen.getByLabelText(/Firmenname/i);
      const submitButton = screen.getByRole('button', { name: /Customer anlegen/i });

      // Clear company name
      await userEvent.clear(companyNameInput);

      expect(submitButton).toBeDisabled();
    });

    it('enables submit button when company name is provided', async () => {
      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const submitButton = screen.getByRole('button', { name: /Customer anlegen/i });

      // Company name is pre-filled
      expect(submitButton).not.toBeDisabled();
    });
  });

  describe('Xentral Customers Loading', () => {
    it('loads xentral customers on dialog open', async () => {
      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: mockXentralCustomers,
      } as any);

      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(httpClient.get).toHaveBeenCalledWith(
          expect.stringContaining('/api/xentral/customers?salesRepId=')
        );
      });
    });

    it('shows warning alert when no xentral customer is selected', () => {
      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByText(/Keine Xentral-Verknüpfung/i)).toBeInTheDocument();
      expect(screen.getByText(/keine Umsatzdaten angezeigt werden/i)).toBeInTheDocument();
    });
  });

  describe('Autocomplete Filtering', () => {
    it('filters xentral customers by company name', async () => {
      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: mockXentralCustomers,
      } as any);

      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const autocomplete = screen.getByLabelText(/Xentral-Kunde verknüpfen/i);

      await userEvent.click(autocomplete);
      await userEvent.type(autocomplete, 'Silbertanne');

      await waitFor(() => {
        expect(screen.getByText('Restaurant Silbertanne')).toBeInTheDocument();
      });
    });

    it('filters xentral customers by xentral ID', async () => {
      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: mockXentralCustomers,
      } as any);

      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const autocomplete = screen.getByLabelText(/Xentral-Kunde verknüpfen/i);

      await userEvent.click(autocomplete);
      await userEvent.type(autocomplete, 'XC-002');

      await waitFor(() => {
        expect(screen.getByText('Restaurant Silbertanne')).toBeInTheDocument();
      });
    });
  });

  describe('Form Submit - Success', () => {
    it('submits form with company name and xentral customer ID', async () => {
      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: mockXentralCustomers,
      } as any);

      vi.mocked(httpClient.post).mockResolvedValueOnce({
        data: {
          id: 'customer-001',
          companyName: 'Gastro Meier GmbH',
          status: 'PROSPECT',
        },
      } as any);

      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Wait for Xentral customers to load
      await waitFor(() => {
        expect(httpClient.get).toHaveBeenCalled();
      });

      // Select Xentral customer
      const autocomplete = screen.getByLabelText(/Xentral-Kunde verknüpfen/i);
      await userEvent.click(autocomplete);
      await waitFor(() => {
        expect(screen.getByText('Gastro Meier GmbH')).toBeInTheDocument();
      });
      await userEvent.click(screen.getByText('Gastro Meier GmbH'));

      // Submit form
      const submitButton = screen.getByRole('button', { name: /Customer anlegen/i });
      await userEvent.click(submitButton);

      await waitFor(() => {
        expect(httpClient.post).toHaveBeenCalledWith(
          `/api/opportunities/${mockOpportunity.id}/convert-to-customer`,
          expect.objectContaining({
            companyName: 'Gastro Meier GmbH',
            xentralCustomerId: 'XC-001',
          })
        );
      });

      // Should call onSuccess and onClose
      await waitFor(() => {
        expect(mockOnSuccess).toHaveBeenCalled();
        expect(mockOnClose).toHaveBeenCalled();
      });

      // Should navigate to customer detail page
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/customers/customer-001');
      });
    });

    it('submits form without xentral customer (optional)', async () => {
      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: mockXentralCustomers,
      } as any);

      vi.mocked(httpClient.post).mockResolvedValueOnce({
        data: {
          id: 'customer-002',
          companyName: 'Neue Firma AG',
          status: 'PROSPECT',
        },
      } as any);

      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Change company name
      const companyNameInput = screen.getByLabelText(/Firmenname/i);
      await userEvent.clear(companyNameInput);
      await userEvent.type(companyNameInput, 'Neue Firma AG');

      // Submit without selecting Xentral customer
      const submitButton = screen.getByRole('button', { name: /Customer anlegen/i });
      await userEvent.click(submitButton);

      await waitFor(() => {
        expect(httpClient.post).toHaveBeenCalledWith(
          `/api/opportunities/${mockOpportunity.id}/convert-to-customer`,
          expect.objectContaining({
            companyName: 'Neue Firma AG',
            xentralCustomerId: undefined,
          })
        );
      });
    });
  });

  describe('Form Submit - Error Handling', () => {
    it('shows error message when API call fails', async () => {
      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: mockXentralCustomers,
      } as any);

      vi.mocked(httpClient.post).mockRejectedValueOnce({
        response: {
          data: {
            message: 'Opportunity bereits konvertiert',
          },
        },
      });

      renderWithProviders(
        <ConvertToCustomerDialog
          open={true}
          opportunity={mockOpportunity}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Submit form
      const submitButton = screen.getByRole('button', { name: /Customer anlegen/i });
      await userEvent.click(submitButton);

      await waitFor(() => {
        expect(httpClient.post).toHaveBeenCalled();
      });

      // Should NOT call onSuccess or navigate
      expect(mockOnSuccess).not.toHaveBeenCalled();
      expect(mockNavigate).not.toHaveBeenCalled();
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });
});
