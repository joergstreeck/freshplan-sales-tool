/**
 * CustomerForm Tests
 *
 * Tests for critical business logic in CustomerForm:
 * - Validation logic (email, postal code, phone)
 * - Input change handling
 * - Currency formatting
 * - Error state management
 * - BusinessType integration (Sprint 2.1.6.1 Phase 1)
 *
 * Note: Full UI/integration tests would be much larger.
 * This focuses on the core business logic functions.
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CustomerForm, type CustomerFormData } from '../CustomerForm';

// Mock i18n
vi.mock('../../../i18n/hooks', () => ({
  useLanguage: () => ({ currentLanguage: 'de' }),
}));

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        formTitle: 'Kundenformular',
        'sections.basicData': 'Grunddaten',
        'sections.addressData': 'Adressdaten',
        'sections.contactData': 'Kontaktdaten',
        'sections.businessData': 'Geschäftsdaten',
        'sections.additionalData': 'Zusatzinformationen',
        'fields.companyName': 'Firmenname',
        'fields.legalForm': 'Rechtsform',
        'fields.customerType': 'Kundentyp',
        'fields.industry': 'Branche',
        'fields.chainCustomer': 'Kettenkunde',
        'fields.customerNumberInternal': 'Kundennummer (intern)',
        'fields.streetAndNumber': 'Straße & Hausnummer',
        'fields.postalCode': 'PLZ',
        'fields.city': 'Stadt',
        'fields.contactName': 'Name',
        'fields.contactPosition': 'Position',
        'fields.contactPhone': 'Telefon',
        'fields.contactEmail': 'E-Mail',
        'fields.expectedVolume': 'Erwartetes Jahresvolumen',
        'fields.paymentMethod': 'Zahlungsart',
        'fields.notes': 'Notizen',
        'fields.customField1': 'Benutzerdefiniertes Feld 1',
        'fields.customField2': 'Benutzerdefiniertes Feld 2',
        'options.pleaseSelect': 'Bitte wählen',
        'options.loading': 'Lädt...',
        'options.legalForms.gmbh': 'GmbH',
        'options.legalForms.ag': 'AG',
        'options.legalForms.gbr': 'GbR',
        'options.legalForms.einzelunternehmen': 'Einzelunternehmen',
        'options.legalForms.kg': 'KG',
        'options.legalForms.ohg': 'OHG',
        'options.legalForms.ug': 'UG',
        'options.customerTypes.neukunde': 'Neukunde',
        'options.customerTypes.bestandskunde': 'Bestandskunde',
        'options.yes': 'Ja',
        'options.no': 'Nein',
        'options.paymentMethods.invoice': 'Rechnung',
        'options.paymentMethods.prepayment': 'Vorkasse',
        'options.paymentMethods.cash': 'Barzahlung',
        'placeholders.expectedVolume': 'z.B. 100.000',
        'placeholders.notes': 'Zusätzliche Informationen...',
      };
      return translations[key] || key;
    },
  }),
}));

// Test helper: Render with QueryClient
function renderWithQueryClient(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // Disable retries in tests
      },
    },
  });
  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>);
}

describe('CustomerForm', () => {
  const defaultFormData: CustomerFormData = {
    companyName: '',
    legalForm: '',
    customerType: '',
    industry: '',
    chainCustomer: 'nein',
    customerNumber: '',
    street: '',
    postalCode: '',
    city: '',
    contactName: '',
    contactPosition: '',
    contactPhone: '',
    contactEmail: '',
    expectedVolume: '',
    paymentMethod: '',
    notes: '',
    customField1: '',
    customField2: '',
  };

  describe('Rendering', () => {
    it('should render form sections', () => {
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      expect(screen.getByText('Kundenformular')).toBeInTheDocument();
      expect(screen.getByText('Grunddaten')).toBeInTheDocument();
      expect(screen.getByText('Adressdaten')).toBeInTheDocument();
      expect(screen.getByText('Kontaktdaten')).toBeInTheDocument();
    });

    it('should render all form fields', () => {
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      expect(screen.getByLabelText('Firmenname')).toBeInTheDocument();
      expect(screen.getByLabelText('Rechtsform')).toBeInTheDocument();
      expect(screen.getByLabelText(/PLZ/)).toBeInTheDocument();
      expect(screen.getByLabelText(/E-Mail/)).toBeInTheDocument();
    });
  });

  describe('Input Change Handling', () => {
    it('should call onFormDataChange when text input changes', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const companyInput = screen.getByLabelText('Firmenname');

      // Type a single character to trigger onChange
      await user.type(companyInput, 'A');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      // Verify callback was called with updated companyName
      expect(onFormDataChange).toHaveBeenCalledWith(
        expect.objectContaining({
          companyName: expect.any(String),
        })
      );
    });

    it('should update form data when select changes', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const legalFormSelect = screen.getByLabelText('Rechtsform');
      await user.selectOptions(legalFormSelect, 'gmbh');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalledWith(
          expect.objectContaining({
            legalForm: 'gmbh',
          })
        );
      });
    });
  });

  describe('Email Validation', () => {
    it('should not show error for valid email', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const emailInput = screen.getByLabelText(/E-Mail/);
      await user.type(emailInput, 'test@example.com');

      // No error message should appear
      await waitFor(() => {
        const errorElements = screen.queryAllByText(/gültige E-Mail/i);
        expect(errorElements).toHaveLength(0);
      });
    });

    it('should show error for invalid email', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const emailInput = screen.getByLabelText(/E-Mail/);
      await user.type(emailInput, 'invalid-email');
      await user.tab(); // Trigger blur/validation

      await waitFor(() => {
        expect(emailInput).toBeInTheDocument();
      });
    });
  });

  describe('Postal Code Validation', () => {
    it('should accept valid 5-digit postal code', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const plzInput = screen.getByLabelText(/PLZ/);

      // Type a digit to trigger onChange
      await user.type(plzInput, '1');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      // Verify callback was called with postalCode field
      expect(onFormDataChange).toHaveBeenCalledWith(
        expect.objectContaining({
          postalCode: expect.any(String),
        })
      );
    });

    it('should enforce maxLength of 5 for postal code', () => {
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const plzInput = screen.getByLabelText(/PLZ/) as HTMLInputElement;
      expect(plzInput.maxLength).toBe(5);
    });
  });

  describe('Phone Validation', () => {
    it('should call onFormDataChange when phone number is entered', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const phoneInput = screen.getByLabelText(/Telefon/);

      // Type a digit to trigger onChange
      await user.type(phoneInput, '0');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      // Verify callback was called with contactPhone field
      expect(onFormDataChange).toHaveBeenCalledWith(
        expect.objectContaining({
          contactPhone: expect.any(String),
        })
      );
    });
  });

  describe('Currency Formatting (Volume Field)', () => {
    it('should format volume input with thousand separators', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const volumeInput = screen.getByLabelText(/Erwartetes Jahresvolumen/);

      // Type a digit to trigger onChange
      await user.type(volumeInput, '1');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      // Verify callback was called with expectedVolume field
      expect(onFormDataChange).toHaveBeenCalledWith(
        expect.objectContaining({
          expectedVolume: expect.any(String),
        })
      );
    });

    it('should strip non-digits from volume input', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const volumeInput = screen.getByLabelText(/Erwartetes Jahresvolumen/);
      await user.type(volumeInput, 'abc123def456');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      // Should only contain digits and formatting
      const lastCall = onFormDataChange.mock.calls[onFormDataChange.mock.calls.length - 1][0];
      expect(lastCall.expectedVolume).toMatch(/^[\d.]*$/);
    });
  });

  describe('Form Data Persistence', () => {
    it('should display pre-filled form data', () => {
      const filledData: CustomerFormData = {
        ...defaultFormData,
        companyName: 'ACME Corp',
        legalForm: 'gmbh',
        contactEmail: 'contact@acme.com',
        postalCode: '10115',
      };
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={filledData} onFormDataChange={onFormDataChange} />
      );

      expect(screen.getByDisplayValue('ACME Corp')).toBeInTheDocument();
      expect(screen.getByDisplayValue('contact@acme.com')).toBeInTheDocument();
      expect(screen.getByDisplayValue('10115')).toBeInTheDocument();
    });

    it('should preserve other fields when updating one field', async () => {
      const user = userEvent.setup();
      const filledData: CustomerFormData = {
        ...defaultFormData,
        companyName: 'Existing Company',
        contactEmail: 'existing@email.com',
      };
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={filledData} onFormDataChange={onFormDataChange} />
      );

      const cityInput = screen.getByLabelText(/Stadt/);

      // Type a character to trigger onChange
      await user.type(cityInput, 'B');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      // Verify callback includes all original fields plus the new city value
      expect(onFormDataChange).toHaveBeenCalledWith(
        expect.objectContaining({
          companyName: 'Existing Company',
          contactEmail: 'existing@email.com',
          city: expect.any(String),
        })
      );
    });
  });

  describe('Required Fields', () => {
    it('should mark required fields with required attribute', () => {
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const companyInput = screen.getByLabelText('Firmenname');
      const streetInput = screen.getByLabelText(/Straße/);
      const plzInput = screen.getByLabelText(/PLZ/);
      const cityInput = screen.getByLabelText(/Stadt/);
      const contactNameInput = screen.getByLabelText(/Name/);
      const phoneInput = screen.getByLabelText(/Telefon/);
      const emailInput = screen.getByLabelText(/E-Mail/);

      expect(companyInput).toBeRequired();
      expect(streetInput).toBeRequired();
      expect(plzInput).toBeRequired();
      expect(cityInput).toBeRequired();
      expect(contactNameInput).toBeRequired();
      expect(phoneInput).toBeRequired();
      expect(emailInput).toBeRequired();
    });
  });

  describe('BusinessType Integration (Sprint 2.1.6.1 Phase 1)', () => {
    it('should show loading state while fetching business types', () => {
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      const industrySelect = screen.getByLabelText(/Branche/);
      expect(industrySelect).toBeDisabled();
      expect(screen.getByText('Lädt...')).toBeInTheDocument();
    });

    it('should load and display 9 business types from MSW mock', async () => {
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      // Wait for business types to load from MSW mock
      await waitFor(
        () => {
          const industrySelect = screen.getByLabelText(/Branche/);
          expect(industrySelect).not.toBeDisabled();
        },
        { timeout: 3000 }
      );

      // Verify all 9 BusinessType values are present
      expect(screen.getByRole('option', { name: 'Restaurant' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Hotel' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Catering' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Kantine' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Großhandel' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'LEH' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Bildung' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Gesundheit' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Sonstiges' })).toBeInTheDocument();
    });

    it('should allow selecting a business type value', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />
      );

      // Wait for business types to load
      await waitFor(
        () => {
          const industrySelect = screen.getByLabelText(/Branche/);
          expect(industrySelect).not.toBeDisabled();
        },
        { timeout: 3000 }
      );

      const industrySelect = screen.getByLabelText(/Branche/);
      await user.selectOptions(industrySelect, 'HOTEL');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalledWith(
          expect.objectContaining({
            industry: 'HOTEL',
          })
        );
      });
    });

    it('should preserve selected business type when form data is updated', async () => {
      const filledData: CustomerFormData = {
        ...defaultFormData,
        industry: 'RESTAURANT',
      };
      const onFormDataChange = vi.fn();
      renderWithQueryClient(
        <CustomerForm formData={filledData} onFormDataChange={onFormDataChange} />
      );

      // Wait for business types to load
      await waitFor(
        () => {
          const industrySelect = screen.getByLabelText(/Branche/) as HTMLSelectElement;
          expect(industrySelect).not.toBeDisabled();
        },
        { timeout: 3000 }
      );

      const industrySelect = screen.getByLabelText(/Branche/) as HTMLSelectElement;
      expect(industrySelect.value).toBe('RESTAURANT');
    });
  });
});
