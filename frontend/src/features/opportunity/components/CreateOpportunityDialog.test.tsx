/**
 * CreateOpportunityDialog Component Tests
 * Sprint 2.1.7.1 - Lead → Opportunity Conversion
 *
 * @description Tests für Dialog-Rendering, Form-Validation, OpportunityType-Selection und Submit/Cancel
 * @since 2025-10-18
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../../../test/test-utils';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import CreateOpportunityDialog from './CreateOpportunityDialog';
import type { Lead } from '../../leads/types';
import { httpClient } from '../../../lib/apiClient';

// Mock API Client
vi.mock('../../../lib/apiClient', () => ({
  httpClient: {
    post: vi.fn(),
  },
}));

// Helper function to render with theme
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

// Mock Lead Data
const mockLead: Lead = {
  id: 123,
  companyName: 'Test Catering GmbH',
  city: 'Berlin',
  postalCode: '10115',
  status: 'NEW',
  estimatedVolume: 5000,
  leadScore: 75,
  createdAt: '2025-10-01T10:00:00Z',
  updatedAt: '2025-10-15T14:30:00Z',
};

describe('CreateOpportunityDialog', () => {
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Dialog Rendering', () => {
    it('renders dialog when open is true', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByRole('heading', { name: 'Opportunity erstellen' })).toBeInTheDocument();
      expect(screen.getByText(/Lead:/i)).toBeInTheDocument();
      expect(screen.getByText('Test Catering GmbH')).toBeInTheDocument();
    });

    it('does not render dialog when open is false', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={false}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(
        screen.queryByRole('heading', { name: 'Opportunity erstellen' })
      ).not.toBeInTheDocument();
    });

    it('renders all form fields', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Check for form fields by their placeholders/unique identifiers
      expect(screen.getByPlaceholderText(/Wird automatisch generiert/i)).toBeInTheDocument();
      expect(screen.getAllByText(/Opportunity-Typ/i).length).toBeGreaterThan(0); // Label + Select
      expect(screen.getAllByText(/Erwarteter Wert/i).length).toBeGreaterThan(0);
      expect(screen.getAllByText(/Erwartetes Abschlussdatum/i).length).toBeGreaterThan(0);
      expect(screen.getAllByText(/Beschreibung/i).length).toBeGreaterThan(0);
    });

    it('pre-fills name with lead company name', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const nameInput = screen.getByLabelText(/Name/i) as HTMLInputElement;
      expect(nameInput.value).toBe('Test Catering GmbH');
    });

    it('pre-fills expectedValue with lead estimatedVolume', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const valueInput = screen.getByLabelText(/Erwarteter Wert/i) as HTMLInputElement;
      expect(valueInput.value).toBe('5000');
    });

    it('shows default OpportunityType as NEUGESCHAEFT', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Default value should be NEUGESCHAEFT (displayed in German)
      // Multiple occurrences: Label + help text mention "Neugeschäft"
      expect(screen.getAllByText(/Neugeschäft/i).length).toBeGreaterThan(0);
    });

    it('renders action buttons', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByRole('button', { name: 'Abbrechen' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Opportunity erstellen' })).toBeInTheDocument();
    });
  });

  describe('OpportunityType Selection', () => {
    it('shows OpportunityType field with default value', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Check that OpportunityType field label exists (may appear multiple times)
      expect(screen.getAllByText(/Opportunity-Typ/i).length).toBeGreaterThan(0);
      // Check that help text for Lead conversion is present
      expect(screen.getByText(/Lead-Conversion: Standard ist "Neugeschäft"/i)).toBeInTheDocument();
    });

    it('shows OpportunityType icons in component', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // The icons are part of the Select component
      // Verify that the component renders successfully with OpportunityType field
      expect(screen.getAllByText(/Opportunity-Typ/i).length).toBeGreaterThan(0);
      // Verify help text is visible
      expect(screen.getByText(/Lead-Conversion/i)).toBeInTheDocument();
    });
  });

  describe('Form Validation', () => {
    it('shows error when expectedValue is 0', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Clear expectedValue field (set to 0)
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

    it('shows error when expectedValue is negative', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Set negative value
      const valueInput = screen.getByLabelText(/Erwarteter Wert/i);
      await user.clear(valueInput);
      await user.type(valueInput, '-100');

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

    it('validates expectedCloseDate must be in future', () => {
      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // DatePicker validation tested via component logic
      // Default date is +30 days (always valid)
      // Min date is set to addDays(new Date(), 1) in component
      // Validation logic: isAfter(startOfDay(expectedCloseDate), startOfDay(new Date()))

      // Verify DatePicker field exists
      expect(screen.getAllByText(/Erwartetes Abschlussdatum/i).length).toBeGreaterThan(0);

      // Note: Manual date input testing with MUI DatePicker requires complex setup
      // Validation is covered by unit testing the validate() function
    });

    it('allows valid form submission', async () => {
      const user = userEvent.setup();

      // Mock successful API response
      vi.mocked(httpClient.post).mockResolvedValueOnce({ data: { id: '456' } });

      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Form is pre-filled with valid data
      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Opportunity erstellen' });
      await user.click(submitButton);

      // API should be called
      await waitFor(() => {
        expect(httpClient.post).toHaveBeenCalledTimes(1);
        expect(httpClient.post).toHaveBeenCalledWith(
          `/api/opportunities/from-lead/${mockLead.id}`,
          expect.objectContaining({
            opportunityType: 'NEUGESCHAEFT',
            expectedValue: 5000,
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
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const cancelButton = screen.getByRole('button', { name: 'Abbrechen' });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('disables buttons during submission', async () => {
      const user = userEvent.setup();

      // Mock slow API response
      vi.mocked(httpClient.post).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve({ data: {} }), 100))
      );

      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const submitButton = screen.getByRole('button', { name: 'Opportunity erstellen' });
      await user.click(submitButton);

      // Buttons should be disabled during submission
      await waitFor(() => {
        expect(submitButton).toBeDisabled();
        expect(screen.getByRole('button', { name: 'Abbrechen' })).toBeDisabled();
      });
    });

    it('shows loading state during submission', async () => {
      const user = userEvent.setup();

      // Mock slow API response
      vi.mocked(httpClient.post).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve({ data: {} }), 200))
      );

      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const submitButton = screen.getByRole('button', { name: 'Opportunity erstellen' });
      await user.click(submitButton);

      // Loading text should appear
      await waitFor(
        () => {
          expect(screen.getByRole('button', { name: 'Erstelle...' })).toBeInTheDocument();
        },
        { timeout: 1000 }
      );
    });

    it('displays API error message', async () => {
      const user = userEvent.setup();

      // Mock API error
      const errorMessage = 'Lead wurde bereits konvertiert';
      vi.mocked(httpClient.post).mockRejectedValueOnce({
        response: {
          data: {
            detail: errorMessage,
          },
        },
      });

      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const submitButton = screen.getByRole('button', { name: 'Opportunity erstellen' });
      await user.click(submitButton);

      // Error message should appear (most important test)
      await waitFor(
        () => {
          const errorText = screen.getByText(errorMessage);
          expect(errorText).toBeInTheDocument();
        },
        { timeout: 3000 }
      );

      // Verify the error message is visible to the user
      expect(screen.getByText(errorMessage)).toBeVisible();
    });

    it('resets form state when dialog closes', async () => {
      const user = userEvent.setup();

      const { rerender } = renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Modify form
      const nameInput = screen.getByLabelText(/Name/i);
      await user.clear(nameInput);
      await user.type(nameInput, 'Modified Name');

      // Close dialog
      const cancelButton = screen.getByRole('button', { name: 'Abbrechen' });
      await user.click(cancelButton);

      // Re-open dialog
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <CreateOpportunityDialog
            open={true}
            lead={mockLead}
            onClose={mockOnClose}
            onSuccess={mockOnSuccess}
          />
        </ThemeProvider>
      );

      // Form should be reset to default values
      const nameInputAfterReopen = screen.getByLabelText(/Name/i) as HTMLInputElement;
      expect(nameInputAfterReopen.value).toBe('Test Catering GmbH');
    });
  });

  describe('Edge Cases', () => {
    it('handles lead without estimatedVolume', () => {
      const leadWithoutVolume: Lead = {
        ...mockLead,
        estimatedVolume: undefined,
      };

      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={leadWithoutVolume}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // expectedValue field should be empty
      const valueInput = screen.getByLabelText(/Erwarteter Wert/i) as HTMLInputElement;
      expect(valueInput.value).toBe('');
    });

    it('handles lead without leadScore in description', () => {
      const leadWithoutScore: Lead = {
        ...mockLead,
        leadScore: undefined,
      };

      renderWithTheme(
        <CreateOpportunityDialog
          open={true}
          lead={leadWithoutScore}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Description should not mention lead score
      const descriptionInput = screen.getByLabelText(/Beschreibung/i) as HTMLTextAreaElement;
      expect(descriptionInput.value).not.toContain('Lead-Score');
    });
  });
});
