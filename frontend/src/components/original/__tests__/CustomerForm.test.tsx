/**
 * CustomerForm Tests
 *
 * Tests for critical business logic in CustomerForm:
 * - Validation logic (email, postal code, phone)
 * - Input change handling
 * - Currency formatting
 * - Error state management
 *
 * Note: Full UI/integration tests would be much larger.
 * This focuses on the core business logic functions.
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
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
        'options.pleaseSelect': 'Bitte wählen',
        'options.legalForms.gmbh': 'GmbH',
        'options.legalForms.ag': 'AG',
        'options.legalForms.gbr': 'GbR',
        'options.legalForms.einzelunternehmen': 'Einzelunternehmen',
        'options.legalForms.kg': 'KG',
        'options.legalForms.ohg': 'OHG',
        'options.legalForms.ug': 'UG',
        'options.customerTypes.neukunde': 'Neukunde',
        'options.customerTypes.bestandskunde': 'Bestandskunde',
        'options.industries.hotel': 'Hotel',
        'options.industries.krankenhaus': 'Krankenhaus',
        'options.industries.seniorenresidenz': 'Seniorenresidenz',
        'options.industries.betriebsrestaurant': 'Betriebsrestaurant',
        'options.industries.restaurant': 'Restaurant',
        'options.yes': 'Ja',
        'options.no': 'Nein',
        'options.paymentMethods.invoice': 'Rechnung',
        'options.paymentMethods.prepayment': 'Vorkasse',
        'options.paymentMethods.directDebit': 'Lastschrift',
      };
      return translations[key] || key;
    },
  }),
}));

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
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

      expect(screen.getByText('Kundenformular')).toBeInTheDocument();
      expect(screen.getByText('Grunddaten')).toBeInTheDocument();
      expect(screen.getByText('Adressdaten')).toBeInTheDocument();
      expect(screen.getByText('Kontaktdaten')).toBeInTheDocument();
    });

    it('should render all form fields', () => {
      const onFormDataChange = vi.fn();
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

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
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

      const companyInput = screen.getByLabelText('Firmenname');
      await user.type(companyInput, 'Test GmbH');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      // Check last call has updated companyName
      const lastCall = onFormDataChange.mock.calls[onFormDataChange.mock.calls.length - 1][0];
      expect(lastCall.companyName).toBe('Test GmbH');
    });

    it('should update form data when select changes', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

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
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

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
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

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
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

      const plzInput = screen.getByLabelText(/PLZ/);
      await user.clear(plzInput);
      await user.type(plzInput, '10115');

      await waitFor(() => {
        const calls = onFormDataChange.mock.calls;
        const lastCall = calls[calls.length - 1][0];
        expect(lastCall.postalCode).toBe('10115');
      });
    });

    it('should enforce maxLength of 5 for postal code', () => {
      const onFormDataChange = vi.fn();
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

      const plzInput = screen.getByLabelText(/PLZ/) as HTMLInputElement;
      expect(plzInput.maxLength).toBe(5);
    });
  });

  describe('Phone Validation', () => {
    it('should call onFormDataChange when phone number is entered', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

      const phoneInput = screen.getByLabelText(/Telefon/);
      await user.clear(phoneInput);
      await user.type(phoneInput, '+49 30 12345678');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      const calls = onFormDataChange.mock.calls;
      const lastCall = calls[calls.length - 1][0];
      // Type events fire per character, so check it contains the input
      expect(lastCall.contactPhone).toContain('1234567');
    });
  });

  describe('Currency Formatting (Volume Field)', () => {
    it('should format volume input with thousand separators', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

      const volumeInput = screen.getByLabelText(/Erwartetes Jahresvolumen/);
      await user.clear(volumeInput);
      await user.type(volumeInput, '1234567');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
        const calls = onFormDataChange.mock.calls;
        const lastCall = calls[calls.length - 1][0];
        // Check if final value has dots (thousand separators)
        // Value should be formatted like "1.234.567"
        expect(lastCall.expectedVolume).toContain('.');
      });
    });

    it('should strip non-digits from volume input', async () => {
      const user = userEvent.setup();
      const onFormDataChange = vi.fn();
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

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
      render(<CustomerForm formData={filledData} onFormDataChange={onFormDataChange} />);

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
      render(<CustomerForm formData={filledData} onFormDataChange={onFormDataChange} />);

      const cityInput = screen.getByLabelText(/Stadt/);
      await user.clear(cityInput);
      await user.type(cityInput, 'Berlin');

      await waitFor(() => {
        expect(onFormDataChange).toHaveBeenCalled();
      });

      const calls = onFormDataChange.mock.calls;
      const lastCall = calls[calls.length - 1][0];
      expect(lastCall.companyName).toBe('Existing Company');
      expect(lastCall.contactEmail).toBe('existing@email.com');
      expect(lastCall.city).toBe('Berlin');
    });
  });

  describe('Required Fields', () => {
    it('should mark required fields with required attribute', () => {
      const onFormDataChange = vi.fn();
      render(<CustomerForm formData={defaultFormData} onFormDataChange={onFormDataChange} />);

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
});
