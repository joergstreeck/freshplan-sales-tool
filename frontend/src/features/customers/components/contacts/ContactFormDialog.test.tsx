/**
 * ContactFormDialog Tests
 * Sprint 2.1.7.7: Updated tests for Server-Driven Stepper UI
 *
 * Tests for ContactFormDialog component with 3-step wizard navigation.
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ContactFormDialog } from './ContactFormDialog';
import { CustomerFieldThemeProvider } from '../../theme/CustomerFieldThemeProvider';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';

// Mock useContactSchema hook
vi.mock('../../../../hooks/useContactSchema', () => ({
  useContactSchema: () => ({
    data: [
      {
        cardId: 'contact_form',
        title: 'Kontakt',
        sections: [
          {
            sectionId: 'basic_info',
            title: 'Stammdaten',
            fields: [
              {
                fieldKey: 'salutation',
                label: 'Anrede',
                type: 'ENUM',
                enumSource: '/api/enums/salutations',
              },
              { fieldKey: 'firstName', label: 'Vorname', type: 'TEXT', required: true },
              { fieldKey: 'lastName', label: 'Nachname', type: 'TEXT', required: true },
              { fieldKey: 'email', label: 'E-Mail', type: 'TEXT' },
              { fieldKey: 'phone', label: 'Telefon', type: 'TEXT' },
            ],
          },
          {
            sectionId: 'relationship',
            title: 'Beziehung',
            fields: [
              { fieldKey: 'birthday', label: 'Geburtstag', type: 'DATE' },
              { fieldKey: 'hobbies', label: 'Hobbies', type: 'TEXT' },
            ],
          },
          {
            sectionId: 'social_business',
            title: 'Professionell',
            fields: [
              { fieldKey: 'linkedinUrl', label: 'LinkedIn', type: 'TEXT' },
              { fieldKey: 'xingUrl', label: 'XING', type: 'TEXT' },
            ],
          },
        ],
      },
    ],
    isLoading: false,
    isError: false,
  }),
}));

// Mock fetch for enum endpoints
beforeEach(() => {
  global.fetch = vi.fn((url: string) => {
    if (url.includes('/api/enums/salutations')) {
      return Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve([
            { value: 'HERR', label: 'Herr' },
            { value: 'FRAU', label: 'Frau' },
          ]),
      } as Response);
    }
    return Promise.reject(new Error('Unknown URL'));
  });
});

// Test wrapper with CustomerFieldThemeProvider and QueryClientProvider
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const queryClient = createTestQueryClient();
  return (
    <QueryClientProvider client={queryClient}>
      <CustomerFieldThemeProvider>{children}</CustomerFieldThemeProvider>
    </QueryClientProvider>
  );
};

const renderWithTheme = (ui: React.ReactElement) => render(ui, { wrapper: TestWrapper });

const createMockContact = () => ({
  id: 'contact-1',
  customerId: 'customer-1',
  salutation: 'HERR',
  firstName: 'Max',
  lastName: 'Mustermann',
  email: 'max@example.com',
  phone: '+49 30 12345678',
  isPrimary: true,
  isActive: true,
  responsibilityScope: 'all' as const,
  createdAt: new Date('2025-01-01').toISOString(),
  updatedAt: new Date('2025-01-01').toISOString(),
});

describe('ContactFormDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ========== DIALOG RENDERING ==========

  describe('Dialog Rendering', () => {
    it('renders dialog when open is true', () => {
      renderWithTheme(<ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} />);

      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    it('renders Stepper with 3 steps', () => {
      renderWithTheme(<ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} />);

      // Stepper should show all 3 steps
      expect(screen.getByText('Stammdaten')).toBeInTheDocument();
      expect(screen.getByText('Beziehung')).toBeInTheDocument();
      expect(screen.getByText('Professionell')).toBeInTheDocument();
    });

    it('renders navigation buttons', () => {
      renderWithTheme(<ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} />);

      // Should have at least Abbrechen and a navigation button
      expect(screen.getByRole('button', { name: /abbrechen/i })).toBeInTheDocument();
    });
  });

  // ========== CANCEL/CLOSE ==========

  describe('Cancel/Close Actions', () => {
    it('calls onClose when Abbrechen is clicked', async () => {
      const onClose = vi.fn();
      renderWithTheme(<ContactFormDialog open={true} onClose={onClose} onSubmit={vi.fn()} />);

      const cancelButton = screen.getByRole('button', { name: /abbrechen/i });
      await userEvent.click(cancelButton);

      expect(onClose).toHaveBeenCalled();
    });
  });

  // ========== EDIT MODE ==========

  describe('Edit Mode', () => {
    it('renders with contact data when editing', () => {
      const contact = createMockContact();
      renderWithTheme(
        <ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} contact={contact} />
      );

      // Title should indicate edit mode
      expect(screen.getByText('Kontakt bearbeiten')).toBeInTheDocument();
    });

    it('pre-fills form with existing contact data', () => {
      const contact = createMockContact();
      renderWithTheme(
        <ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} contact={contact} />
      );

      const firstNameInput = screen.getByLabelText(/vorname/i) as HTMLInputElement;
      const lastNameInput = screen.getByLabelText(/nachname/i) as HTMLInputElement;

      expect(firstNameInput.value).toBe('Max');
      expect(lastNameInput.value).toBe('Mustermann');
    });
  });

  // ========== FORM INTERACTIONS ==========

  describe('Form Interactions', () => {
    it('allows entering firstName', async () => {
      renderWithTheme(<ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} />);

      const firstNameInput = screen.getByLabelText(/vorname/i);
      await userEvent.type(firstNameInput, 'Max');

      expect((firstNameInput as HTMLInputElement).value).toBe('Max');
    });

    it('allows entering lastName', async () => {
      renderWithTheme(<ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} />);

      const lastNameInput = screen.getByLabelText(/nachname/i);
      await userEvent.type(lastNameInput, 'Mustermann');

      expect((lastNameInput as HTMLInputElement).value).toBe('Mustermann');
    });

    it('resets form when dialog is reopened', async () => {
      const { rerender } = renderWithTheme(
        <ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} />
      );

      // Enter some data
      const firstNameInput = screen.getByLabelText(/vorname/i);
      await userEvent.type(firstNameInput, 'Test');

      // Close dialog
      rerender(
        <TestWrapper>
          <ContactFormDialog open={false} onClose={vi.fn()} onSubmit={vi.fn()} />
        </TestWrapper>
      );

      // Reopen dialog
      rerender(
        <TestWrapper>
          <ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} />
        </TestWrapper>
      );

      // Form should be reset
      const newFirstNameInput = screen.getByLabelText(/vorname/i) as HTMLInputElement;
      expect(newFirstNameInput.value).toBe('');
    });
  });

  // ========== VALIDATION ==========

  describe('Validation', () => {
    it('has form fields on Step 1', () => {
      renderWithTheme(<ContactFormDialog open={true} onClose={vi.fn()} onSubmit={vi.fn()} />);

      // Should have basic form fields on Step 1
      const firstNameInput = screen.getByLabelText(/vorname/i);
      expect(firstNameInput).toBeInTheDocument();
    });
  });
});
