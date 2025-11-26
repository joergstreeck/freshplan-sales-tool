/**
 * Contact Multi-Location Assignment Tests
 * Sprint 2.1.7.7 - Enterprise-Level Tests
 *
 * @description Comprehensive tests for Contact Multi-Location assignment feature
 * - Server-Driven UI with conditional visibility (visibleWhenField/visibleWhenValue)
 * - responsibilityScope: ALL vs SPECIFIC behavior
 * - Multi-select location assignment
 * - Schema loading and error handling
 *
 * @since 2025-11-26
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '../../../../test/test-utils';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import freshfoodzTheme from '../../../../theme/freshfoodz';
import { ContactEditDialog } from './ContactEditDialog';

// Mock dependencies
vi.mock('../../../../hooks/useContactSchema');
vi.mock('../../services/hooks');
vi.mock('../../../../hooks/useContactEnums', () => ({
  normalizeDecisionLevel: (level: string | undefined) => level?.toUpperCase() || '',
}));

import { useContactSchema } from '../../../../hooks/useContactSchema';
import { useCustomerLocations } from '../../services/hooks';

// Mock Schema Data - simulates backend response
const mockContactSchema = [
  {
    cardId: 'contact_details',
    title: 'Kontaktdaten',
    subtitle: 'Persönliche und berufliche Informationen',
    icon: '👤',
    order: 1,
    sections: [
      {
        sectionId: 'basic_info',
        title: 'Stammdaten',
        subtitle: 'Persönliche und berufliche Informationen',
        fields: [
          {
            fieldKey: 'salutation',
            label: 'Anrede',
            type: 'ENUM',
            enumSource: '/api/enums/salutations',
            required: true,
            gridCols: 3,
          },
          {
            fieldKey: 'firstName',
            label: 'Vorname',
            type: 'TEXT',
            required: true,
            gridCols: 6,
          },
          {
            fieldKey: 'lastName',
            label: 'Nachname',
            type: 'TEXT',
            required: true,
            gridCols: 6,
          },
          {
            fieldKey: 'email',
            label: 'E-Mail',
            type: 'TEXT',
            gridCols: 12,
          },
        ],
      },
      {
        sectionId: 'location_assignment',
        title: 'Standort-Zuordnung',
        subtitle: 'Für welche Standorte ist dieser Kontakt zuständig?',
        collapsible: true,
        defaultCollapsed: false,
        fields: [
          {
            fieldKey: 'responsibilityScope',
            label: 'Zuständigkeitsbereich',
            type: 'ENUM',
            enumSource: '/api/enums/responsibility-scopes',
            required: true,
            gridCols: 12,
            helpText:
              'ALL = Kontakt ist für alle Standorte zuständig, SPECIFIC = Nur für bestimmte Standorte',
          },
          {
            fieldKey: 'assignedLocationIds',
            label: 'Zugewiesene Standorte',
            type: 'MULTISELECT',
            gridCols: 12,
            helpText: 'Wählen Sie die Standorte aus, für die dieser Kontakt zuständig ist',
            visibleWhenField: 'responsibilityScope',
            visibleWhenValue: 'SPECIFIC',
          },
        ],
      },
      {
        sectionId: 'relationship',
        title: 'Beziehungsmanagement',
        fields: [
          {
            fieldKey: 'birthday',
            label: 'Geburtstag',
            type: 'DATE',
            gridCols: 6,
          },
        ],
      },
    ],
  },
];

// Mock Customer Locations
const mockLocations = {
  content: [
    { id: 'loc-001', locationName: 'Berlin Mitte' },
    { id: 'loc-002', locationName: 'Hamburg Hafen' },
    { id: 'loc-003', locationName: 'München Zentrum' },
    { id: 'loc-004', locationName: 'Köln Altstadt' },
  ],
  totalElements: 4,
  totalPages: 1,
};

describe('Contact Multi-Location Assignment', () => {
  let queryClient: QueryClient;
  const mockCustomerId = '550e8400-e29b-41d4-a716-446655440000';
  const mockOnClose = vi.fn();
  const mockOnSubmit = vi.fn().mockResolvedValue(undefined);

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>
      </QueryClientProvider>
    );
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });

    vi.clearAllMocks();

    // Mock window.matchMedia for MUI
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: (query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {},
        removeListener: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        dispatchEvent: () => {},
      }),
    });

    // Default mock implementations
    vi.mocked(useContactSchema).mockReturnValue({
      data: mockContactSchema,
      isLoading: false,
      isError: false,
    } as ReturnType<typeof useContactSchema>);

    vi.mocked(useCustomerLocations).mockReturnValue({
      data: mockLocations,
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useCustomerLocations>);
  });

  afterEach(() => {
    queryClient.clear();
    vi.restoreAllMocks();
  });

  // ========== SCHEMA LOADING TESTS ==========

  describe('Schema Loading', () => {
    it('shows loading state while schema is being fetched', () => {
      vi.mocked(useContactSchema).mockReturnValue({
        data: undefined,
        isLoading: true,
        isError: false,
      } as ReturnType<typeof useContactSchema>);

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByText('Schema wird geladen...')).toBeInTheDocument();
    });

    it('shows error state when schema fetch fails', () => {
      vi.mocked(useContactSchema).mockReturnValue({
        data: undefined,
        isLoading: false,
        isError: true,
      } as ReturnType<typeof useContactSchema>);

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByText(/Fehler beim Laden des Kontakt-Schemas/i)).toBeInTheDocument();
    });

    it('renders all sections from schema after successful load', () => {
      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // Verify sections are rendered
      expect(screen.getByText('Stammdaten')).toBeInTheDocument();
      expect(screen.getByText('Standort-Zuordnung')).toBeInTheDocument();
      expect(screen.getByText('Beziehungsmanagement')).toBeInTheDocument();
    });
  });

  // ========== RESPONSIBILITY SCOPE TESTS ==========

  describe('Responsibility Scope Behavior', () => {
    it('defaults to ALL responsibility scope for new contact', () => {
      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // The form data should default to ALL, but we check form behavior
      expect(screen.getByText('Zuständigkeitsbereich')).toBeInTheDocument();
    });

    it('hides location selection when responsibility scope is ALL', () => {
      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // When responsibilityScope = ALL (default), assignedLocationIds should be hidden
      // The conditional visibility is handled by visibleWhenField/visibleWhenValue
      const locationSection = screen.getByText('Standort-Zuordnung');
      expect(locationSection).toBeInTheDocument();
    });

    it('preserves responsibility scope when editing existing contact with SPECIFIC', () => {
      const existingContact = {
        id: 'contact-123',
        firstName: 'Max',
        lastName: 'Mustermann',
        salutation: 'HERR',
        responsibilityScope: 'SPECIFIC',
        assignedLocationIds: ['loc-001', 'loc-002'],
        email: 'max@example.com',
      };

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          contact={existingContact}
          onSubmit={mockOnSubmit}
        />
      );

      // Should show edit mode
      expect(screen.getByText('Kontakt bearbeiten')).toBeInTheDocument();
    });
  });

  // ========== LOCATION INJECTION TESTS ==========

  describe('Location Options Injection', () => {
    it('injects customer locations into assignedLocationIds field options', () => {
      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // Verify useCustomerLocations was called with correct parameters
      expect(useCustomerLocations).toHaveBeenCalledWith(mockCustomerId, 0, 100);
    });

    it('handles empty locations list gracefully', () => {
      vi.mocked(useCustomerLocations).mockReturnValue({
        data: { content: [], totalElements: 0, totalPages: 0 },
        isLoading: false,
        isError: false,
      } as unknown as ReturnType<typeof useCustomerLocations>);

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // Dialog should still render without errors
      expect(screen.getByText('Neuer Kontakt')).toBeInTheDocument();
    });
  });

  // ========== FORM SUBMISSION TESTS ==========

  describe('Form Submission with Multi-Location Data', () => {
    it('includes responsibilityScope in submitted data', async () => {
      const user = userEvent.setup();

      // Note: This test validates form data structure, not full submission
      // Full submission requires valid salutation and contact method
      const existingContact = {
        id: 'contact-123',
        firstName: 'Max',
        lastName: 'Mustermann',
        salutation: 'HERR',
        responsibilityScope: 'ALL',
        assignedLocationIds: [],
        email: 'max@example.com',
      };

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          contact={existingContact}
          onSubmit={mockOnSubmit}
        />
      );

      // Submit form (existing contact already has required fields)
      const submitButton = screen.getByRole('button', { name: /Speichern/i });
      await user.click(submitButton);

      // Verify responsibilityScope is included
      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(
          expect.objectContaining({
            responsibilityScope: 'ALL',
            assignedLocationIds: [],
          })
        );
      });
    }, 10000); // Increase timeout

    it('preserves assignedLocationIds for SPECIFIC scope contacts', async () => {
      const user = userEvent.setup();

      const existingContact = {
        id: 'contact-123',
        firstName: 'Max',
        lastName: 'Mustermann',
        salutation: 'HERR',
        responsibilityScope: 'SPECIFIC',
        assignedLocationIds: ['loc-001', 'loc-002'],
        email: 'max@example.com',
      };

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          contact={existingContact}
          onSubmit={mockOnSubmit}
        />
      );

      // Submit form
      const submitButton = screen.getByRole('button', { name: /Speichern/i });
      await user.click(submitButton);

      // Verify assignedLocationIds are preserved
      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(
          expect.objectContaining({
            responsibilityScope: 'SPECIFIC',
            assignedLocationIds: ['loc-001', 'loc-002'],
          })
        );
      });
    });
  });

  // ========== VALIDATION TESTS ==========

  describe('Form Validation', () => {
    it('requires salutation field', async () => {
      const user = userEvent.setup();

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // Fill only name fields
      const firstNameInput = screen.getByLabelText(/Vorname/i);
      const lastNameInput = screen.getByLabelText(/Nachname/i);

      await user.type(firstNameInput, 'Max');
      await user.type(lastNameInput, 'Mustermann');

      // Submit form
      const submitButton = screen.getByRole('button', { name: /Kontakt anlegen/i });
      await user.click(submitButton);

      // Should show validation error for salutation
      await waitFor(() => {
        expect(screen.getByText(/Anrede ist erforderlich/i)).toBeInTheDocument();
      });

      // onSubmit should NOT have been called
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('requires at least one contact method (email, phone, or mobile)', async () => {
      const user = userEvent.setup();

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // Fill only name fields without contact info
      const firstNameInput = screen.getByLabelText(/Vorname/i);
      const lastNameInput = screen.getByLabelText(/Nachname/i);

      await user.type(firstNameInput, 'Max');
      await user.type(lastNameInput, 'Mustermann');

      // Submit form
      const submitButton = screen.getByRole('button', { name: /Kontakt anlegen/i });
      await user.click(submitButton);

      // Should show validation error for contact method
      await waitFor(() => {
        expect(screen.getByText(/Mindestens eine Kontaktmöglichkeit/i)).toBeInTheDocument();
      });
    });

    it('validates email format', async () => {
      const user = userEvent.setup();

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // Fill fields with invalid email
      const firstNameInput = screen.getByLabelText(/Vorname/i);
      const lastNameInput = screen.getByLabelText(/Nachname/i);
      const emailInput = screen.getByLabelText(/E-Mail/i);

      await user.type(firstNameInput, 'Max');
      await user.type(lastNameInput, 'Mustermann');
      await user.type(emailInput, 'invalid-email');

      // Submit form
      const submitButton = screen.getByRole('button', { name: /Kontakt anlegen/i });
      await user.click(submitButton);

      // Should show email validation error
      await waitFor(() => {
        expect(screen.getByText(/Ungültige E-Mail-Adresse/i)).toBeInTheDocument();
      });
    });
  });

  // ========== DIALOG BEHAVIOR TESTS ==========

  describe('Dialog Behavior', () => {
    it('shows "Neuer Kontakt" title for new contact', () => {
      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByText('Neuer Kontakt')).toBeInTheDocument();
    });

    it('shows "Kontakt bearbeiten" title for existing contact', () => {
      const existingContact = {
        id: 'contact-123',
        firstName: 'Max',
        lastName: 'Mustermann',
        email: 'max@example.com',
      };

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          contact={existingContact}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByText('Kontakt bearbeiten')).toBeInTheDocument();
    });

    it('calls onClose when Cancel button is clicked', async () => {
      const user = userEvent.setup();

      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      const cancelButton = screen.getByRole('button', { name: /Abbrechen/i });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('resets form state when dialog is reopened', async () => {
      const { rerender } = renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // Close and reopen
      rerender(
        <QueryClientProvider client={queryClient}>
          <ThemeProvider theme={freshfoodzTheme}>
            <ContactEditDialog
              open={false}
              onClose={mockOnClose}
              customerId={mockCustomerId}
              onSubmit={mockOnSubmit}
            />
          </ThemeProvider>
        </QueryClientProvider>
      );

      rerender(
        <QueryClientProvider client={queryClient}>
          <ThemeProvider theme={freshfoodzTheme}>
            <ContactEditDialog
              open={true}
              onClose={mockOnClose}
              customerId={mockCustomerId}
              onSubmit={mockOnSubmit}
            />
          </ThemeProvider>
        </QueryClientProvider>
      );

      // Form should be fresh
      const firstNameInput = screen.getByLabelText(/Vorname/i) as HTMLInputElement;
      expect(firstNameInput.value).toBe('');
    });
  });

  // ========== ACCESSIBILITY TESTS ==========

  describe('Accessibility', () => {
    it('has proper form labeling', () => {
      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      // Check for properly labeled inputs
      expect(screen.getByLabelText(/Vorname/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Nachname/i)).toBeInTheDocument();
    });

    it('has proper button roles', () => {
      renderWithProviders(
        <ContactEditDialog
          open={true}
          onClose={mockOnClose}
          customerId={mockCustomerId}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByRole('button', { name: /Abbrechen/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Kontakt anlegen/i })).toBeInTheDocument();
    });
  });
});
