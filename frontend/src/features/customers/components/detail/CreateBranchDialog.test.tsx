/**
 * CreateBranchDialog Component Tests
 * Sprint 2.1.7.7 D4 - Multi-Location Management
 *
 * @description Tests für CreateBranchDialog - Rendering, Form-Validation, Server-Driven Enums, Submit/Cancel
 * @since 2025-11-14
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '../../../../test/test-utils';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../../theme/freshfoodz';
import { CreateBranchDialog } from './CreateBranchDialog';
import { httpClient } from '../../../../lib/apiClient';

// Mock API Client
vi.mock('../../../../lib/apiClient', () => ({
  httpClient: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

// Mock BASE_URL for useEnumOptions
vi.mock('../../../../features/leads/hooks/shared', () => ({
  BASE_URL: 'http://localhost:8080',
  getAuthHeaders: () => ({}),
}));

// Helper function to render with theme
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

// Mock Enum Responses
const mockCustomerTypeOptions = [
  { value: 'UNTERNEHMEN', label: 'Unternehmen' },
  { value: 'EINZELUNTERNEHMEN', label: 'Einzelunternehmen' },
  { value: 'PRIVAT', label: 'Privat' },
];

const mockStatusOptions = [
  { value: 'PROSPECT', label: 'Interessent' },
  { value: 'AKTIV', label: 'Aktiv' },
  { value: 'INAKTIV', label: 'Inaktiv' },
];

describe('CreateBranchDialog', () => {
  const mockHeadquarterId = '550e8400-e29b-41d4-a716-446655440000';
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    // Mock window.matchMedia for MUI useMediaQuery
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation(query => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });

    // Mock fetch for useEnumOptions
    global.fetch = vi.fn((url: string) => {
      if (url.includes('/api/enums/customer-types')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockCustomerTypeOptions),
        } as Response);
      }
      if (url.includes('/api/enums/customer-status')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockStatusOptions),
        } as Response);
      }
      return Promise.reject(new Error('Unknown URL'));
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // ========== DIALOG RENDERING TESTS ==========

  describe('Dialog Rendering', () => {
    it('renders dialog when open is true', async () => {
      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByText('Neue Filiale anlegen')).toBeInTheDocument();
      expect(
        screen.getByText('Filiale wird automatisch mit dem Headquarter verknüpft')
      ).toBeInTheDocument();
    });

    it('does not render dialog when open is false', () => {
      renderWithTheme(
        <CreateBranchDialog
          open={false}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      expect(
        screen.queryByRole('heading', { name: 'Neue Filiale anlegen' })
      ).not.toBeInTheDocument();
    });

    it('renders all form fields', async () => {
      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Check for form fields
      expect(screen.getByLabelText(/Firmenname/i)).toBeInTheDocument();

      // Wait for enum options to load
      await waitFor(() => {
        expect(screen.getByLabelText(/Kundentyp/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Status/i)).toBeInTheDocument();
      });
    });

    it('renders action buttons', () => {
      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByRole('button', { name: 'Abbrechen' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Filiale anlegen' })).toBeInTheDocument();
    });

    it('shows info alert about automatic field assignment', () => {
      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByText(/Hinweis:/i)).toBeInTheDocument();
      expect(screen.getByText(/hierarchyType = FILIALE/i)).toBeInTheDocument();
      expect(screen.getByText(/Verknüpfung zum Headquarter/i)).toBeInTheDocument();
      expect(screen.getByText(/Übernahme der xentralCustomerId/i)).toBeInTheDocument();
    });
  });

  // ========== SERVER-DRIVEN ENUM TESTS ==========

  describe('Server-Driven Enum Options', () => {
    it('loads customerType options from backend', async () => {
      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Wait for enum options to load
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          expect.stringContaining('/api/enums/customer-types'),
          expect.any(Object)
        );
      });
    });

    it('loads status options from backend', async () => {
      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Wait for enum options to load
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          expect.stringContaining('/api/enums/customer-status'),
          expect.any(Object)
        );
      });
    });
  });

  // ========== FORM VALIDATION TESTS ==========

  describe('Form Validation', () => {
    it('disables submit button when companyName is empty', async () => {
      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Submit button should be disabled when companyName is empty
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      expect(submitButton).toBeDisabled();

      // API should not be called
      expect(vi.mocked(httpClient.post)).not.toHaveBeenCalled();
    });

    it('shows error when companyName is less than 2 characters', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter only 1 character
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, 'A');

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      await user.click(submitButton);

      // Validation error should appear
      await waitFor(() => {
        expect(
          screen.getByText(/Firmenname muss mindestens 2 Zeichen lang sein/i)
        ).toBeInTheDocument();
      });

      // API should not be called
      expect(vi.mocked(httpClient.post)).not.toHaveBeenCalled();
    });

    it('submit button is disabled when companyName is empty', () => {
      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      expect(submitButton).toBeDisabled();
    });

    it('enables submit button when valid name is entered', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Submit button should be disabled initially
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      expect(submitButton).toBeDisabled();

      // Start typing valid name
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, 'Test Filiale');

      // Submit button should now be enabled
      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      });
    });
  });

  // ========== SUBMIT/CANCEL ACTIONS ==========

  describe('Submit/Cancel Actions', () => {
    it('calls onClose when Cancel button is clicked', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      const cancelButton = screen.getByRole('button', { name: 'Abbrechen' });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('submits valid form data and calls onSuccess', async () => {
      const user = userEvent.setup();

      // Mock successful API response
      vi.mocked(httpClient.post).mockResolvedValueOnce({
        data: {
          id: '123e4567-e89b-12d3-a456-426614174000',
          companyName: 'Test Filiale GmbH',
          hierarchyType: 'FILIALE',
          parentCustomerId: mockHeadquarterId,
          status: 'PROSPECT',
          customerType: 'UNTERNEHMEN',
        },
      });

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter valid company name
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, 'Test Filiale GmbH');

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      await user.click(submitButton);

      // API should be called with correct data
      await waitFor(() => {
        expect(vi.mocked(httpClient.post)).toHaveBeenCalledTimes(1);
        expect(vi.mocked(httpClient.post)).toHaveBeenCalledWith(
          `/api/customers/${mockHeadquarterId}/branches`,
          {
            companyName: 'Test Filiale GmbH',
            status: 'PROSPECT',
            customerType: 'UNTERNEHMEN',
          }
        );
      });

      // Success callbacks should be called
      expect(mockOnSuccess).toHaveBeenCalledTimes(1);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('shows loading state during submission', async () => {
      const user = userEvent.setup();

      // Mock slow API response
      vi.mocked(httpClient.post).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve({ data: {} }), 200))
      );

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter valid company name
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, 'Test Filiale GmbH');

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      await user.click(submitButton);

      // Loading text should appear
      await waitFor(
        () => {
          expect(screen.getByRole('button', { name: 'Speichert...' })).toBeInTheDocument();
        },
        { timeout: 1000 }
      );
    });

    it('disables buttons during submission', async () => {
      const user = userEvent.setup();

      // Mock slow API response
      vi.mocked(httpClient.post).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve({ data: {} }), 100))
      );

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter valid company name
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, 'Test Filiale GmbH');

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      await user.click(submitButton);

      // Buttons should be disabled during submission
      await waitFor(() => {
        expect(submitButton).toBeDisabled();
        expect(screen.getByRole('button', { name: 'Abbrechen' })).toBeDisabled();
      });
    });

    it('displays generic error message when error has no message', async () => {
      const user = userEvent.setup();

      // Mock API error without message
      vi.mocked(httpClient.post).mockRejectedValueOnce({});

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter valid company name
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, 'Test Filiale GmbH');

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      await user.click(submitButton);

      // Generic error message should appear
      await waitFor(
        () => {
          expect(
            screen.getByText(/Fehler beim Anlegen der Filiale. Bitte versuchen Sie es erneut./i)
          ).toBeInTheDocument();
        },
        { timeout: 3000 }
      );
    });

    it('resets form state when dialog re-opens', async () => {
      const user = userEvent.setup();

      const { rerender } = renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter company name
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, 'Modified Name');

      // Close dialog
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <CreateBranchDialog
            open={false}
            onClose={mockOnClose}
            headquarterId={mockHeadquarterId}
            onSuccess={mockOnSuccess}
          />
        </ThemeProvider>
      );

      // Re-open dialog
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <CreateBranchDialog
            open={true}
            onClose={mockOnClose}
            headquarterId={mockHeadquarterId}
            onSuccess={mockOnSuccess}
          />
        </ThemeProvider>
      );

      // Form should be reset to default values
      await waitFor(() => {
        const nameInputAfterReopen = screen.getByLabelText(/Firmenname/i) as HTMLInputElement;
        expect(nameInputAfterReopen.value).toBe('');
      });
    });

    it('prevents dialog close during submission', async () => {
      const user = userEvent.setup();

      // Mock slow API response
      vi.mocked(httpClient.post).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve({ data: {} }), 200))
      );

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter valid company name
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, 'Test Filiale GmbH');

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      await user.click(submitButton);

      // Try to close dialog during submission (via Cancel button - should be disabled)
      await waitFor(() => {
        const cancelButton = screen.getByRole('button', { name: 'Abbrechen' });
        expect(cancelButton).toBeDisabled();
      });
    });
  });

  // ========== EDGE CASES ==========

  describe('Edge Cases', () => {
    it('trims whitespace from companyName before submission', async () => {
      const user = userEvent.setup();

      vi.mocked(httpClient.post).mockResolvedValueOnce({ data: { id: '123' } });

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter company name with leading/trailing whitespace
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, '  Test Filiale GmbH  ');

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      await user.click(submitButton);

      // API should be called with trimmed value
      await waitFor(() => {
        expect(vi.mocked(httpClient.post)).toHaveBeenCalledWith(
          expect.any(String),
          expect.objectContaining({
            companyName: 'Test Filiale GmbH',
          })
        );
      });
    });

    it('handles special characters in companyName', async () => {
      const user = userEvent.setup();

      vi.mocked(httpClient.post).mockResolvedValueOnce({ data: { id: '123' } });

      renderWithTheme(
        <CreateBranchDialog
          open={true}
          onClose={mockOnClose}
          headquarterId={mockHeadquarterId}
          onSuccess={mockOnSuccess}
        />
      );

      // Enter company name with special characters
      const specialName = 'Test & Partner GmbH & Co. KG';
      const nameInput = screen.getByLabelText(/Firmenname/i);
      await user.type(nameInput, specialName);

      // Click submit
      const submitButton = screen.getByRole('button', { name: 'Filiale anlegen' });
      await user.click(submitButton);

      // API should be called with special characters preserved
      await waitFor(() => {
        expect(vi.mocked(httpClient.post)).toHaveBeenCalledWith(
          expect.any(String),
          expect.objectContaining({
            companyName: specialName,
          })
        );
      });
    });
  });
});
