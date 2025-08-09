/**
 * FC-005 Complete Wizard Flow Integration Tests
 *
 * ZWECK: Testet den vollständigen 3-Schritt Wizard Flow mit API Integration
 * PHILOSOPHIE: Validiert komplexe User-Journeys mit flexibler field-basierter Architektur
 */

import { describe, it, expect, beforeAll, afterAll, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { mockServer, testData } from './mockServer';
// Note: Import would be from actual component when it exists
// import { CustomerOnboardingWizard } from '../../components/wizard/CustomerOnboardingWizard';

// Mock component for testing integration patterns
const CustomerOnboardingWizard = () => {
  return (
    <div>
      <h1>{'Schritt 1 von 3 - Kundendaten'}</h1>
      <form>
        <label htmlFor="companyName">Firmenname</label>
        <input id="companyName" name="companyName" type="text" />

        <label htmlFor="industry">Branche</label>
        <select id="industry" name="industry">
          <option value="">Bitte wählen</option>
          <option value="hotel">Hotel & Gastronomie</option>
          <option value="office">Büro & Verwaltung</option>
        </select>

        <label htmlFor="chainCustomer">Kettenkunde</label>
        <select id="chainCustomer" name="chainCustomer">
          <option value="">Bitte wählen</option>
          <option value="ja">Ja</option>
          <option value="nein">Nein</option>
        </select>

        <button type="button">Weiter</button>
      </form>
    </div>
  );
};

// Mock für React Router (falls verwendet)
vi.mock('react-router-dom', () => ({
  useNavigate: () => vi.fn(),
  useLocation: () => ({ pathname: '/customers/new' }),
}));

// Mock für localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// Mock Server Setup
beforeAll(() => {
  mockServer.listen({ onUnhandledRequest: 'error' });
});

afterEach(() => {
  mockServer.resetHandlers();
  vi.clearAllMocks();
  localStorageMock.getItem.mockClear();
  localStorageMock.setItem.mockClear();
  document.body.innerHTML = '';
});

afterAll(() => {
  mockServer.close();
});

// Helper function to simulate user filling form
async function fillCustomerDataStep(user: unknown, customerData: unknown) {
  // Fill company name
  const companyNameInput = screen.getByLabelText(/firmenname/i);
  await user.clear(companyNameInput);
  await user.type(companyNameInput, customerData.companyName);

  // Select industry
  const industrySelect = screen.getByLabelText(/branche/i);
  await user.selectOptions(industrySelect, customerData.industry);

  // Select chain customer option
  const chainCustomerSelect = screen.getByLabelText(/kettenkunde/i);
  await user.selectOptions(chainCustomerSelect, customerData.chainCustomer);

  // Fill industry-specific fields if present
  if (customerData.hotelStars) {
    const hotelStarsSelect = screen.queryByLabelText(/hotel sterne/i);
    if (hotelStarsSelect) {
      await user.selectOptions(hotelStarsSelect, customerData.hotelStars);
    }
  }
}

describe.skip('FC-005 Complete Wizard Flow Integration Tests', () => {
  describe.skip('Single Customer Flow (chainCustomer = nein)', () => {
    it('should complete single customer onboarding workflow', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Verify Step 1 is visible
      expect(screen.getByText(/schritt 1/i)).toBeInTheDocument();
      expect(screen.getByText(/kundendaten/i)).toBeInTheDocument();

      // Fill customer data for single customer
      const singleCustomerData = {
        ...testData.validCustomerData,
        chainCustomer: 'nein',
      };

      await fillCustomerDataStep(user, singleCustomerData);

      // Verify auto-save functionality
      await waitFor(
        () => {
          expect(localStorageMock.setItem).toHaveBeenCalled();
        },
        { timeout: 3000 }
      );

      // Navigate to next step
      const nextButton = screen.getByText(/weiter/i);
      expect(nextButton).toBeEnabled();
      await user.click(nextButton);

      // For single customer, should skip locations step and go to final step
      await waitFor(() => {
        expect(
          screen.getByText(/schritt 3/i) || screen.getByText(/zusammenfassung/i)
        ).toBeInTheDocument();
      });

      // Complete the wizard
      const submitButton = screen.getByText(/speichern|abschließen/i);
      await user.click(submitButton);

      // Verify API call was made
      await waitFor(() => {
        // Should have called the final customer creation API
        expect(screen.queryByText(/erfolgreich|gespeichert/i)).toBeInTheDocument();
      });
    });
  });

  describe.skip('Chain Customer Flow (chainCustomer = ja)', () => {
    it('should complete chain customer onboarding with multiple locations', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Step 1: Fill customer data for chain customer
      await fillCustomerDataStep(user, testData.validCustomerData);

      // Navigate to Step 2 (Locations)
      const nextButton = screen.getByText(/weiter/i);
      await user.click(nextButton);

      // Verify Step 2 is visible (should show because chainCustomer = 'ja')
      await waitFor(() => {
        expect(screen.getByText(/schritt 2/i)).toBeInTheDocument();
        expect(screen.getByText(/standorte/i)).toBeInTheDocument();
      });

      // Add first location
      const addLocationButton = screen.getByText(/standort hinzufügen/i);
      await user.click(addLocationButton);

      // Fill first location
      const locationNameInput = screen.getByLabelText(/standort.*name/i);
      await user.type(locationNameInput, testData.validLocations[0].name);

      // Add second location
      await user.click(addLocationButton);

      // Fill second location
      const locationInputs = screen.getAllByLabelText(/standort.*name/i);
      await user.type(locationInputs[1], testData.validLocations[1].name);

      // Navigate to Step 3 (Detailed Locations)
      const nextButton2 = screen.getByText(/weiter/i);
      await user.click(nextButton2);

      // Verify Step 3 is visible
      await waitFor(() => {
        expect(screen.getByText(/schritt 3/i)).toBeInTheDocument();
        expect(screen.getByText(/detaillierte.*standorte/i)).toBeInTheDocument();
      });

      // Fill detailed location information
      const addressInput = screen.getByLabelText(/adresse/i);
      await user.type(addressInput, testData.validDetailedLocations[0].address);

      const contactPersonInput = screen.getByLabelText(/ansprechpartner/i);
      await user.type(contactPersonInput, testData.validDetailedLocations[0].contactPerson);

      const phoneInput = screen.getByLabelText(/telefon/i);
      await user.type(phoneInput, testData.validDetailedLocations[0].phone);

      const emailInput = screen.getByLabelText(/e-mail/i);
      await user.type(emailInput, testData.validDetailedLocations[0].email);

      // Complete the wizard
      const submitButton = screen.getByText(/speichern|abschließen/i);
      await user.click(submitButton);

      // Verify successful completion
      await waitFor(() => {
        expect(screen.queryByText(/erfolgreich|gespeichert/i)).toBeInTheDocument();
      });
    });

    it('should show conditional fields based on industry selection', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Initially, hotel-specific fields should not be visible
      expect(screen.queryByLabelText(/hotel sterne/i)).not.toBeInTheDocument();

      // Fill company name first
      const companyNameInput = screen.getByLabelText(/firmenname/i);
      await user.type(companyNameInput, 'Test Hotel');

      // Select hotel industry
      const industrySelect = screen.getByLabelText(/branche/i);
      await user.selectOptions(industrySelect, 'hotel');

      // Hotel-specific fields should now appear
      await waitFor(() => {
        expect(screen.getByLabelText(/hotel sterne/i)).toBeInTheDocument();
      });

      // Select hotel stars
      const hotelStarsSelect = screen.getByLabelText(/hotel sterne/i);
      await user.selectOptions(hotelStarsSelect, '4');

      // Verify the field value is set
      expect((hotelStarsSelect as HTMLSelectElement).value).toBe('4');

      // Change industry to office
      await user.selectOptions(industrySelect, 'office');

      // Hotel-specific fields should disappear
      await waitFor(() => {
        expect(screen.queryByLabelText(/hotel sterne/i)).not.toBeInTheDocument();
      });
    });
  });

  describe.skip('Auto-Save and Draft Recovery', () => {
    it('should auto-save draft during form completion', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Fill some customer data
      const companyNameInput = screen.getByLabelText(/firmenname/i);
      await user.type(companyNameInput, 'Draft Test GmbH');

      // Wait for auto-save (debounced)
      await waitFor(
        () => {
          expect(localStorageMock.setItem).toHaveBeenCalled();
        },
        { timeout: 3000 }
      );

      // Verify auto-save indicator appears
      expect(screen.queryByText(/gespeichert|entwurf/i)).toBeInTheDocument();
    });

    it('should recover from draft when component mounts', async () => {
      // Mock localStorage to return existing draft
      localStorageMock.getItem.mockReturnValue(
        JSON.stringify({
          customerData: {
            companyName: 'Recovered Test GmbH',
            industry: 'hotel',
            chainCustomer: 'ja',
          },
          currentStep: 1,
          lastSaved: new Date().toISOString(),
        })
      );

      render(<CustomerOnboardingWizard />);

      // Verify draft data is loaded
      await waitFor(() => {
        const companyNameInput = screen.getByLabelText(/firmenname/i);
        expect((companyNameInput as HTMLInputElement).value).toBe('Recovered Test GmbH');
      });

      const industrySelect = screen.getByLabelText(/branche/i);
      expect((industrySelect as HTMLSelectElement).value).toBe('hotel');

      const chainCustomerSelect = screen.getByLabelText(/kettenkunde/i);
      expect((chainCustomerSelect as HTMLSelectElement).value).toBe('ja');

      // Verify recovery indicator
      expect(screen.queryByText(/entwurf.*wiederhergestellt/i)).toBeInTheDocument();
    });

    it('should handle draft recovery errors gracefully', async () => {
      // Mock localStorage to return corrupted draft
      localStorageMock.getItem.mockReturnValue('invalid json data');

      render(<CustomerOnboardingWizard />);

      // Should start with fresh form
      const companyNameInput = screen.getByLabelText(/firmenname/i);
      expect((companyNameInput as HTMLInputElement).value).toBe('');

      // Should show error notification about corrupted draft
      await waitFor(() => {
        expect(screen.queryByText(/entwurf.*fehler|korrupt/i)).toBeInTheDocument();
      });
    });
  });

  describe.skip('Validation Integration', () => {
    it('should prevent navigation with invalid data', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Try to navigate without filling required fields
      const nextButton = screen.getByText(/weiter/i);
      await user.click(nextButton);

      // Should still be on step 1
      expect(screen.getByText(/schritt 1/i)).toBeInTheDocument();

      // Should show validation errors
      await waitFor(() => {
        expect(screen.getByText(/firmenname.*erforderlich/i)).toBeInTheDocument();
        expect(screen.getByText(/branche.*erforderlich/i)).toBeInTheDocument();
      });

      // Next button should be disabled or show as invalid
      expect(nextButton).toBeDisabled();
    });

    it('should validate individual fields with real-time feedback', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Test company name validation
      const companyNameInput = screen.getByLabelText(/firmenname/i);

      // Enter too short name
      await user.type(companyNameInput, 'A');
      await user.tab(); // Trigger blur event

      await waitFor(() => {
        expect(screen.getByText(/zu kurz|mindestens.*zeichen/i)).toBeInTheDocument();
      });

      // Clear and enter valid name
      await user.clear(companyNameInput);
      await user.type(companyNameInput, 'Valid Company GmbH');
      await user.tab();

      await waitFor(() => {
        expect(screen.queryByText(/zu kurz|mindestens.*zeichen/i)).not.toBeInTheDocument();
      });
    });

    it('should handle API validation errors during submission', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Fill form with data that will trigger validation error
      await fillCustomerDataStep(user, {
        companyName: 'VALIDATION_ERROR',
        industry: 'hotel',
        chainCustomer: 'nein',
      });

      // Navigate to final step
      const nextButton = screen.getByText(/weiter/i);
      await user.click(nextButton);

      // Submit form
      const submitButton = screen.getByText(/speichern|abschließen/i);
      await user.click(submitButton);

      // Should show API validation errors
      await waitFor(() => {
        expect(screen.getByText(/firmenname.*zu kurz/i)).toBeInTheDocument();
      });

      // Form should remain in editable state
      expect(screen.getByLabelText(/firmenname/i)).toBeEnabled();
    });
  });

  describe.skip('Error Handling and Resilience', () => {
    it('should handle network errors during auto-save', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Fill form with data that triggers network error
      const companyNameInput = screen.getByLabelText(/firmenname/i);
      await user.type(companyNameInput, 'NETWORK_ERROR');

      // Wait for auto-save attempt
      await waitFor(
        () => {
          // Should show error indicator for failed auto-save
          expect(screen.queryByText(/fehler.*speichern|netzwerk.*fehler/i)).toBeInTheDocument();
        },
        { timeout: 4000 }
      );

      // Form should remain functional
      expect(companyNameInput).toBeEnabled();

      // Should offer manual retry option
      expect(screen.queryByText(/erneut.*versuchen/i)).toBeInTheDocument();
    });

    it('should handle server errors during final submission', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Fill valid data
      await fillCustomerDataStep(user, {
        companyName: 'SERVER_ERROR',
        industry: 'hotel',
        chainCustomer: 'nein',
      });

      // Navigate to final step
      const nextButton = screen.getByText(/weiter/i);
      await user.click(nextButton);

      // Submit form
      const submitButton = screen.getByText(/speichern|abschließen/i);
      await user.click(submitButton);

      // Should show server error message
      await waitFor(() => {
        expect(screen.getByText(/server.*fehler|technischer.*fehler/i)).toBeInTheDocument();
      });

      // Should offer retry option
      expect(screen.queryByText(/erneut.*versuchen/i)).toBeInTheDocument();

      // Form data should be preserved
      expect(screen.getByDisplayValue('SERVER_ERROR')).toBeInTheDocument();
    });

    it('should maintain UI responsiveness during long operations', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Fill form
      await fillCustomerDataStep(user, testData.validCustomerData);

      // Simulate slow operation
      const nextButton = screen.getByText(/weiter/i);
      await user.click(nextButton);

      // UI should show loading state but remain responsive
      expect(screen.queryByText(/lädt|verarbeitet/i)).toBeInTheDocument();

      // User should still be able to interact with form
      const companyNameInput = screen.getByLabelText(/firmenname/i);
      expect(companyNameInput).toBeEnabled();
    });
  });

  describe.skip('User Experience Integration', () => {
    it('should provide clear step navigation and progress indication', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Verify step indicator
      expect(screen.getByText(/schritt 1.*3/i)).toBeInTheDocument();

      // Fill required data
      await fillCustomerDataStep(user, testData.validCustomerData);

      // Navigate to step 2
      const nextButton = screen.getByText(/weiter/i);
      await user.click(nextButton);

      // Verify step progression
      await waitFor(() => {
        expect(screen.getByText(/schritt 2.*3/i)).toBeInTheDocument();
      });

      // Should have back button
      expect(screen.getByText(/zurück/i)).toBeInTheDocument();
    });

    it('should allow navigation back and forth between steps', async () => {
      const user = userEvent.setup();

      render(<CustomerOnboardingWizard />);

      // Fill step 1
      await fillCustomerDataStep(user, testData.validCustomerData);

      // Go to step 2
      await user.click(screen.getByText(/weiter/i));

      // Verify on step 2
      await waitFor(() => {
        expect(screen.getByText(/schritt 2/i)).toBeInTheDocument();
      });

      // Go back to step 1
      await user.click(screen.getByText(/zurück/i));

      // Verify back on step 1 with data preserved
      await waitFor(() => {
        expect(screen.getByText(/schritt 1/i)).toBeInTheDocument();
      });

      const companyNameInput = screen.getByLabelText(/firmenname/i);
      expect((companyNameInput as HTMLInputElement).value).toBe(
        testData.validCustomerData.companyName
      );
    });

    it('should provide helpful tooltips and field descriptions', async () => {
      render(<CustomerOnboardingWizard />);

      // Look for help icons or info buttons
      const helpElements = screen.queryAllByRole('button', { name: /hilfe|info/i });
      expect(helpElements.length).toBeGreaterThan(0);

      // Verify tooltips appear on hover (if implemented)
      // This would depend on the actual implementation
    });
  });
});
