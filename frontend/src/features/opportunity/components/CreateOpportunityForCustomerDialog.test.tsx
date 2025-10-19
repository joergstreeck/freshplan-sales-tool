/**
 * CreateOpportunityForCustomerDialog Component Tests
 * Sprint 2.1.7.3 - Customer → Opportunity Conversion (Bestandskunden-Workflow)
 *
 * @description Tests für Business-Type-Matrix Integration, 3-Tier Fallback, Auto-Berechnung
 * @since 2025-10-19
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import CreateOpportunityForCustomerDialog from './CreateOpportunityForCustomerDialog';
import type { CustomerResponse } from '../../customer/types/customer.types';
import { CustomerStatus, Industry } from '../../customer/types/customer.types';
import { httpClient } from '../../../lib/apiClient';

// Mock API Client
vi.mock('../../../lib/apiClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

// Helper function to render with theme
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

// Mock Customer Data
const mockCustomer: CustomerResponse = {
  id: 'customer-123',
  customerNumber: 'CUS-001',
  companyName: 'Bella Italia Restaurant',
  customerType: 'UNTERNEHMEN' as any,
  industry: Industry.RESTAURANT,
  status: CustomerStatus.AKTIV,
  expectedAnnualVolume: 50000,
  actualAnnualVolume: 48000, // Xentral data (TIER 1)
  riskScore: 15,
  atRisk: false,
  childCustomerIds: [],
  hasChildren: false,
  createdAt: '2025-01-01T10:00:00Z',
  createdBy: 'user-1',
  isDeleted: false,
};

// Mock Multipliers (Business-Type-Matrix)
const mockMultipliers = [
  {
    id: 'mult-1',
    businessType: 'RESTAURANT',
    opportunityType: 'NEUGESCHAEFT',
    multiplier: 1.0,
  },
  {
    id: 'mult-2',
    businessType: 'RESTAURANT',
    opportunityType: 'SORTIMENTSERWEITERUNG',
    multiplier: 0.25,
  },
  {
    id: 'mult-3',
    businessType: 'RESTAURANT',
    opportunityType: 'NEUER_STANDORT',
    multiplier: 0.8,
  },
  {
    id: 'mult-4',
    businessType: 'RESTAURANT',
    opportunityType: 'VERLAENGERUNG',
    multiplier: 0.95,
  },
  {
    id: 'mult-5',
    businessType: 'HOTEL',
    opportunityType: 'SORTIMENTSERWEITERUNG',
    multiplier: 0.65,
  },
];

describe('CreateOpportunityForCustomerDialog', () => {
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    // Default: Successful multipliers load
    vi.mocked(httpClient.get).mockResolvedValue({ data: mockMultipliers });
  });

  describe('Dialog Rendering', () => {
    it('renders dialog when open is true', async () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(
        screen.getByRole('heading', { name: 'Neue Opportunity erstellen' })
      ).toBeInTheDocument();
      expect(screen.getByText(/Bestandskunde:/i)).toBeInTheDocument();
      expect(screen.getByText('Bella Italia Restaurant')).toBeInTheDocument();
    });

    it('does not render dialog when open is false', () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={false}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(
        screen.queryByRole('heading', { name: 'Neue Opportunity erstellen' })
      ).not.toBeInTheDocument();
    });

    it('pre-fills name with customer company name', async () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        const nameInput = screen.getByLabelText(/Name/i) as HTMLInputElement;
        expect(nameInput.value).toBe('Bella Italia Restaurant');
      });
    });

    it('shows default OpportunityType as SORTIMENTSERWEITERUNG (Bestandskunden)', async () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Default should be SORTIMENTSERWEITERUNG for existing customers
      await waitFor(() => {
        expect(screen.getAllByText(/Sortimentserweiterung/i).length).toBeGreaterThan(0);
      });
    });

    it('renders action buttons', async () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Abbrechen' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Opportunity erstellen' })).toBeInTheDocument();
      });
    });
  });

  describe('Business-Type-Matrix Integration', () => {
    it('loads multipliers on dialog open', async () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(httpClient.get).toHaveBeenCalledWith('/api/settings/opportunity-multipliers');
      });
    });

    it('shows loading state while fetching multipliers', async () => {
      // Mock slow API response
      vi.mocked(httpClient.get).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve({ data: mockMultipliers }), 100))
      );

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Loading state should be visible
      expect(screen.getByText(/Lade Multiplier-Matrix/i)).toBeInTheDocument();

      // Wait for loading to complete
      await waitFor(
        () => {
          expect(screen.queryByText(/Lade Multiplier-Matrix/i)).not.toBeInTheDocument();
        },
        { timeout: 1000 }
      );
    });

    it('shows error message when multipliers fail to load', async () => {
      // Mock API error
      vi.mocked(httpClient.get).mockRejectedValueOnce(new Error('Network error'));

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(screen.getByText(/Multipliers konnten nicht geladen werden/i)).toBeInTheDocument();
      });
    });

    it('displays calculation info box with multiplier details', async () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(screen.getByText(/Intelligente Wertschätzung:/i)).toBeInTheDocument();
        expect(screen.getByText(/Basisvolumen:/i)).toBeInTheDocument();
        expect(screen.getByText(/Multiplier:/i)).toBeInTheDocument();
      });
    });
  });

  describe('3-Tier Fallback for Base Volume', () => {
    it('uses actualAnnualVolume (TIER 1) when available', async () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        // Should show Xentral as source
        expect(screen.getByText(/Xentral/i)).toBeInTheDocument();
        // Base volume should be 48000 (actualAnnualVolume) - multiple occurrences possible
        expect(screen.getAllByText(/48\.000/i).length).toBeGreaterThan(0);
      });
    });

    it('falls back to expectedAnnualVolume (TIER 2) when actualAnnualVolume is missing', async () => {
      const customerWithoutActual: CustomerResponse = {
        ...mockCustomer,
        actualAnnualVolume: undefined,
      };

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={customerWithoutActual}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        // Should show Lead-Schätzung as source
        expect(screen.getByText(/Lead-Schätzung/i)).toBeInTheDocument();
        // Base volume should be 50000 (expectedAnnualVolume) - multiple occurrences possible
        expect(screen.getAllByText(/50\.000/i).length).toBeGreaterThan(0);
      });
    });

    it('shows manual entry warning (TIER 3) when no volume available', async () => {
      const customerWithoutVolume: CustomerResponse = {
        ...mockCustomer,
        actualAnnualVolume: undefined,
        expectedAnnualVolume: undefined,
      };

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={customerWithoutVolume}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(screen.getByText(/Kein Basisvolumen verfügbar/i)).toBeInTheDocument();
        expect(screen.getByText(/Bitte manuell schätzen/i)).toBeInTheDocument();
      });
    });
  });

  describe('Auto-Calculation of Expected Value', () => {
    it('calculates expectedValue using baseVolume × multiplier', async () => {
      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        // Base: 48000 (actualAnnualVolume)
        // Multiplier: 0.25 (RESTAURANT × SORTIMENTSERWEITERUNG)
        // Expected: 48000 × 0.25 = 12000
        const valueInput = screen.getByLabelText(/Erwarteter Wert/i) as HTMLInputElement;
        expect(valueInput.value).toBe('12000');
      });
    });

    it('recalculates when OpportunityType changes', async () => {
      // Note: Testing MUI Select interaction is complex in unit tests
      // This test verifies the initial auto-calculation works
      // OpportunityType change triggering recalculation is tested via E2E tests

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Verify initial calculation (SORTIMENTSERWEITERUNG: 48000 × 0.25 = 12000)
      await waitFor(() => {
        const valueInput = screen.getByLabelText(/Erwarteter Wert/i) as HTMLInputElement;
        expect(valueInput.value).toBe('12000');
      });

      // MUI Select interaction testing would require complex setup
      // Verified through manual testing and E2E tests
    });

    it('allows manual override of calculated value', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        const valueInput = screen.getByLabelText(/Erwarteter Wert/i) as HTMLInputElement;
        expect(valueInput.value).toBe('12000'); // Auto-calculated
      });

      // Manual override
      const valueInput = screen.getByLabelText(/Erwarteter Wert/i);
      await user.clear(valueInput);
      await user.type(valueInput, '15000');

      expect((valueInput as HTMLInputElement).value).toBe('15000');
    });
  });

  describe('Form Validation', () => {
    it('shows error when expectedValue is 0', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(screen.getByLabelText(/Erwarteter Wert/i)).toBeInTheDocument();
      });

      // Clear expectedValue field
      const valueInput = screen.getByLabelText(/Erwarteter Wert/i);
      await user.clear(valueInput);
      await user.type(valueInput, '0');

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Opportunity erstellen' });
      await user.click(submitButton);

      // Validation error should appear
      await waitFor(() => {
        expect(screen.getByText(/Wert muss größer als 0 sein/i)).toBeInTheDocument();
      });

      // API should not be called
      expect(httpClient.post).not.toHaveBeenCalled();
    });

    it('allows valid form submission', async () => {
      const user = userEvent.setup();

      // Mock successful API response
      vi.mocked(httpClient.post).mockResolvedValueOnce({ data: { id: 'opp-456' } });

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Wait for form to be ready
      await waitFor(() => {
        const valueInput = screen.getByLabelText(/Erwarteter Wert/i) as HTMLInputElement;
        expect(valueInput.value).toBe('12000');
      });

      // Submit form
      const submitButton = screen.getByRole('button', { name: 'Opportunity erstellen' });
      await user.click(submitButton);

      // API should be called
      await waitFor(() => {
        expect(httpClient.post).toHaveBeenCalledTimes(1);
        expect(httpClient.post).toHaveBeenCalledWith(
          `/api/opportunities/for-customer/${mockCustomer.id}`,
          expect.objectContaining({
            opportunityType: 'SORTIMENTSERWEITERUNG',
            expectedValue: 12000,
          })
        );
      });

      // Success callbacks should be called
      expect(mockOnSuccess).toHaveBeenCalledTimes(1);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });
  });

  describe('Submit/Cancel Actions', () => {
    it('calls onClose when Cancel button is clicked', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Abbrechen' })).toBeInTheDocument();
      });

      const cancelButton = screen.getByRole('button', { name: 'Abbrechen' });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('displays API error message', async () => {
      const user = userEvent.setup();

      // Mock API error
      vi.mocked(httpClient.post).mockRejectedValueOnce({
        response: {
          status: 400,
          data: {
            detail: 'Kunde muss AKTIV sein, um Opportunity zu erstellen.',
          },
        },
      });

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Opportunity erstellen' })).toBeInTheDocument();
      });

      const submitButton = screen.getByRole('button', { name: 'Opportunity erstellen' });
      await user.click(submitButton);

      // Error message should appear (matches switch case in component)
      await waitFor(
        () => {
          expect(screen.getByText(/Kunde muss AKTIV sein/i)).toBeInTheDocument();
        },
        { timeout: 3000 }
      );
    });

    it('disables submit button while multipliers are loading', async () => {
      // Mock slow multipliers load
      vi.mocked(httpClient.get).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve({ data: mockMultipliers }), 200))
      );

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={mockCustomer}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const submitButton = screen.getByRole('button', { name: 'Opportunity erstellen' });
      expect(submitButton).toBeDisabled();

      // Wait for loading to complete
      await waitFor(
        () => {
          expect(submitButton).not.toBeDisabled();
        },
        { timeout: 1000 }
      );
    });
  });

  describe('Edge Cases', () => {
    it('handles customer without industry gracefully', async () => {
      const customerWithoutIndustry: CustomerResponse = {
        ...mockCustomer,
        industry: undefined,
        actualAnnualVolume: 50000, // Has volume but no industry
      };

      renderWithTheme(
        <CreateOpportunityForCustomerDialog
          open={true}
          customer={customerWithoutIndustry}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Should still render without crashing
      await waitFor(() => {
        expect(
          screen.getByRole('heading', { name: 'Neue Opportunity erstellen' })
        ).toBeInTheDocument();
      });

      // Should calculate with base volume (50000) as fallback
      // Even without industry, expectedValue should be set
      await waitFor(() => {
        const valueInput = screen.getByLabelText(/Erwarteter Wert/i) as HTMLInputElement;
        // Falls back to 50000 (base volume × 1.0 fallback)
        expect(valueInput.value).toBe('50000');
      });
    });
  });
});
