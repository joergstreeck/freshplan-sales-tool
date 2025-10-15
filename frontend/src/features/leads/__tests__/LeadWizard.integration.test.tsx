// Sprint 2.1.5 Frontend Phase 2 - LeadWizard Integration Tests (MSW-based)
// Test Coverage: Progressive Flow, DSGVO Consent Validation, Stage-Transition Rules

import { describe, it, expect, vi, beforeEach, afterEach, beforeAll, afterAll } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import { I18nextProvider } from 'react-i18next';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import { server } from '@/mocks/server';
import i18n from '../../../i18n';
import LeadWizard from '../LeadWizard';
import freshfoodzTheme from '../../../theme/freshfoodz';

// Create QueryClient for tests
const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

// Test wrapper with QueryClient, theme and i18n
const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={queryClient}>
    <I18nextProvider i18n={i18n}>
      <ThemeProvider theme={freshfoodzTheme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </I18nextProvider>
  </QueryClientProvider>
);

// Helper function to fill Erstkontakt fields (PFLICHT for MESSE/EMPFEHLUNG/TELEFON)
async function fillErstkontaktFields(user: ReturnType<typeof userEvent.setup>) {
  const dateField = screen.getByLabelText(/zeitpunkt/i);
  await user.type(dateField, '2025-10-04T14:30');

  // Use getAllByLabelText and select the second "Notizen" field (the Erstkontakt one)
  // First one is "Notizen / Quelle (optional)", second one is the Erstkontakt "Notizen"
  const notesFields = screen.getAllByLabelText(/notizen/i);
  const erstkontaktNotesField = notesFields[1]; // Second field is the Erstkontakt notes
  await user.type(erstkontaktNotesField, 'Messestand Berlin - Interesse an Frischekalkulator');
}

beforeAll(() => {
  server.listen({ onUnhandledRequest: 'bypass' });
  // Force German language for tests
  i18n.changeLanguage('de');

  // Setup default MSW handlers for LeadWizard tests
  server.use(
    // Mock backend enum endpoints (Sprint 2.1.6 - Single Source of Truth)
    http.get('http://localhost:8080/api/enums/business-types', () => {
      return HttpResponse.json([
        { value: 'RESTAURANT', label: 'Restaurant / Gaststätte' },
        { value: 'HOTEL', label: 'Hotel / Beherbergung' },
        { value: 'CATERING', label: 'Catering / Event' },
        { value: 'BAKERY', label: 'Bäckerei / Konditorei' },
      ]);
    }),
    http.get('http://localhost:8080/api/enums/lead-sources', () => {
      return HttpResponse.json([
        { value: 'MESSE', label: 'Messe / Event' },
        { value: 'EMPFEHLUNG', label: 'Empfehlung / Partner' },
        { value: 'TELEFON', label: 'Telefon / Kaltakquise' },
        { value: 'WEB_FORMULAR', label: 'Web-Formular' },
      ]);
    }),
    http.get('http://localhost:8080/api/enums/kitchen-sizes', () => {
      return HttpResponse.json([
        { value: 'small', label: 'Klein (< 20 Essen/Tag)' },
        { value: 'medium', label: 'Mittel (20-100 Essen/Tag)' },
        { value: 'large', label: 'Groß (> 100 Essen/Tag)' },
      ]);
    }),
    // Default successful lead creation handler
    http.post('http://localhost:8080/api/leads', async ({ request }) => {
      const payload = (await request.json()) as {
        stage?: number;
        companyName: string;
        source?: string; // Sprint 2.1.5
        contact?: { firstName?: string; lastName?: string; email?: string; phone?: string };
        activities?: Array<{ activityType: string; performedAt: string }>; // Sprint 2.1.5
        estimatedVolume?: number;
        businessType?: string;
      };

      // Validate required fields
      if (!payload.companyName?.trim()) {
        return HttpResponse.json(
          {
            title: 'Validation Failed',
            status: 400,
            detail: 'Company name is required',
            errors: { companyName: ['Firmenname ist Pflicht'] },
          },
          { status: 400 }
        );
      }

      // Check for duplicate email (409 Conflict)
      if (payload.contact?.email === 'duplicate@example.com') {
        return HttpResponse.json(
          {
            title: 'Duplicate Lead',
            status: 409,
            detail: 'Lead mit dieser E-Mail existiert bereits.',
            errors: { email: ['E-Mail ist bereits vergeben'] },
          },
          { status: 409 }
        );
      }

      // Successful creation
      return HttpResponse.json(
        {
          id: 'lead-123',
          stage: payload.stage || 0,
          companyName: payload.companyName,
          source: payload.source,
          contact: payload.contact,
          // Sprint 2.1.5: consentGivenAt wird NICHT gesendet (UI-only)
          status: 'REGISTERED',
          createdAt: new Date().toISOString(),
        },
        { status: 201 }
      );
    })
  );
});

afterEach(() => {
  server.resetHandlers();
  queryClient.clear(); // Clear query cache between tests

  // Re-apply default handlers after reset
  server.use(
    http.get('http://localhost:8080/api/enums/business-types', () => {
      return HttpResponse.json([
        { value: 'RESTAURANT', label: 'Restaurant / Gaststätte' },
        { value: 'HOTEL', label: 'Hotel / Beherbergung' },
        { value: 'CATERING', label: 'Catering / Event' },
        { value: 'BAKERY', label: 'Bäckerei / Konditorei' },
      ]);
    }),
    http.get('http://localhost:8080/api/enums/lead-sources', () => {
      return HttpResponse.json([
        { value: 'MESSE', label: 'Messe / Event' },
        { value: 'EMPFEHLUNG', label: 'Empfehlung / Partner' },
        { value: 'TELEFON', label: 'Telefon / Kaltakquise' },
        { value: 'WEB_FORMULAR', label: 'Web-Formular' },
      ]);
    }),
    http.get('http://localhost:8080/api/enums/kitchen-sizes', () => {
      return HttpResponse.json([
        { value: 'small', label: 'Klein (< 20 Essen/Tag)' },
        { value: 'medium', label: 'Mittel (20-100 Essen/Tag)' },
        { value: 'large', label: 'Groß (> 100 Essen/Tag)' },
      ]);
    }),
    http.post('http://localhost:8080/api/leads', async ({ request }) => {
      const payload = (await request.json()) as {
        stage?: number;
        companyName: string;
        source?: string;
        contact?: { firstName?: string; lastName?: string; email?: string; phone?: string };
        activities?: Array<{ activityType: string; performedAt: string }>;
        estimatedVolume?: number;
        businessType?: string;
      };

      if (!payload.companyName?.trim()) {
        return HttpResponse.json(
          {
            title: 'Validation Failed',
            status: 400,
            detail: 'Company name is required',
            errors: { companyName: ['Firmenname ist Pflicht'] },
          },
          { status: 400 }
        );
      }

      if (payload.contact?.email === 'duplicate@example.com') {
        return HttpResponse.json(
          {
            title: 'Duplicate Lead',
            status: 409,
            detail: 'Lead mit dieser E-Mail existiert bereits.',
            errors: { email: ['E-Mail ist bereits vergeben'] },
          },
          { status: 409 }
        );
      }

      return HttpResponse.json(
        {
          id: 'lead-123',
          stage: payload.stage || 0,
          companyName: payload.companyName,
          source: payload.source,
          contact: payload.contact,
          status: 'REGISTERED',
          createdAt: new Date().toISOString(),
        },
        { status: 201 }
      );
    })
  );
});

afterAll(() => server.close());

describe('LeadWizard - Progressive Profiling Integration Tests', () => {
  const mockOnClose = vi.fn();
  const mockOnCreated = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ==================== TEST 1: Progressive Flow (Stage 0 → 1 → 2) ====================
  describe('Progressive Flow Navigation', () => {
    it('should navigate through all 3 stages (0 → 1 → 2) and display correct labels', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Stage 0: Vormerkung
      expect(screen.getByText('Vormerkung')).toBeInTheDocument();
      expect(screen.getByText('Registrierung')).toBeInTheDocument();
      expect(screen.getByText('Qualifizierung')).toBeInTheDocument();

      expect(screen.getByLabelText(/firmenname/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /weiter/i })).toBeInTheDocument();

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant Berlin');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      // Navigate to Stage 1
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/nachname/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/e.?mail/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/telefon/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /zurück/i })).toBeInTheDocument();
      });

      // Fill Stage 1
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@test-restaurant.de');

      // Navigate to Stage 2
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/küchengröße/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/mitarbeiterzahl/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/website/i)).toBeInTheDocument();
      });

      // Final submit button should be visible (Karte 2: Qualifizierung speichern)
      expect(screen.getByRole('button', { name: /qualifizierung speichern/i })).toBeInTheDocument();
    }, 10000); // Increase timeout to 10s for slow enum loading

    it('should allow navigating back from Stage 2 → 1 → 0 without losing data', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 2
      await waitFor(() => expect(screen.getByLabelText(/mitarbeiterzahl/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/mitarbeiterzahl/i), '25');

      // Navigate back to Stage 1
      await user.click(screen.getByRole('button', { name: /zurück/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/vorname/i)).toHaveValue('Max');
        expect(screen.getByLabelText(/e.?mail/i)).toHaveValue('max@example.com');
      });

      // Navigate back to Stage 0
      await user.click(screen.getByRole('button', { name: /zurück/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/firmenname/i)).toHaveValue('Test Restaurant');
      });
    });
  });

  // ==================== TEST 2: Navigation Tests (Sprint 2.1.5 - Consent-Checkbox entfernt) ====================
  describe('Navigation Tests', () => {
    it('should navigate from Stage 1 to Stage 2 with contact data (Sprint 2.1.5)', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill Stage 0 with source
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1 with contact data
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');

      // Navigate to Stage 2 - SHOULD SUCCEED (no consent checkbox required)
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Should navigate to Stage 2 successfully
      await waitFor(() => {
        expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument();
      });
    });

    it('should require at least email OR phone to proceed from Stage 1', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Try to skip Stage 1 without any contact data
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Should show validation error (Mind. E-Mail oder Telefon erforderlich)
      await waitFor(() => {
        expect(screen.getByRole('alert')).toBeInTheDocument();
      });

      // Should NOT navigate to Stage 2
      expect(screen.queryByLabelText(/geschätztes volumen/i)).not.toBeInTheDocument();
    });

    it('should NOT send consentGivenAt in Sprint 2.1.5 (UI-only, Backend-Feld erst V259)', async () => {
      const user = userEvent.setup();
      let capturedPayload: unknown;

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1 (no consent checkbox exists)
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Submit from Stage 2
      await waitFor(() =>
        expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument()
      );
      await user.click(screen.getByRole('button', { name: /qualifizierung speichern/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        // Sprint 2.1.5: consentGivenAt wird NICHT gesendet (UI-only)
        expect(capturedPayload.consentGivenAt).toBeUndefined();
      });
    });
  });

  // ==================== TEST 3: Stage-Transition Rules ====================
  describe('Stage-Transition Rules', () => {
    it('should correctly determine stage=0 when saving from Stage 0 (Vormerkung)', async () => {
      const user = userEvent.setup();
      let capturedPayload: unknown;

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill Stage 0 only
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      // Save directly from Stage 0 (Vormerkung speichern)
      await user.click(screen.getByRole('button', { name: /vormerkung speichern/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        expect(capturedPayload.stage).toBe(0);
        expect(capturedPayload.contact).toBeUndefined();
      });
    });

    it('should correctly determine stage=1 when saving from Stage 1 (Registrierung)', async () => {
      const user = userEvent.setup();
      let capturedPayload: unknown;

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');

      // Save directly from Stage 1 (Registrierung speichern)
      await user.click(screen.getByRole('button', { name: /registrierung speichern/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        expect(capturedPayload.stage).toBe(1);
        expect(capturedPayload.contact).toBeDefined();
        expect(capturedPayload.contact.email).toBe('max@example.com');
        // Sprint 2.1.5: consentGivenAt wird NICHT gesendet (UI-only)
      });
    });

    it('should correctly determine stage=2 when business data is provided', async () => {
      const user = userEvent.setup();
      let capturedPayload: unknown;

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1 (no consent checkbox)
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 2 with business data
      await waitFor(() => expect(screen.getByLabelText(/mitarbeiterzahl/i)).toBeInTheDocument());
      const employeeCountField = screen.getByLabelText(/mitarbeiterzahl/i);
      await user.clear(employeeCountField);
      await user.type(employeeCountField, '25');

      // Wait for value to be updated
      await waitFor(() => expect(employeeCountField).toHaveValue(25));

      await user.click(screen.getByRole('button', { name: /qualifizierung speichern/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        expect(capturedPayload.stage).toBe(2);
        expect(capturedPayload.employeeCount).toBe(25);
      });
    });

    it('should validate Stage 0 before allowing navigation (companyName required)', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Try to navigate without filling companyName
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Should show validation error
      await waitFor(() => {
        expect(screen.getByText(/firmenname ist pflicht/i)).toBeInTheDocument();
      });

      // Should NOT navigate to Stage 1
      expect(screen.queryByLabelText(/vorname/i)).not.toBeInTheDocument();
    });

    it('should validate email format in Stage 1', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill invalid email
      await waitFor(() => expect(screen.getByLabelText(/e.?mail/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/e.?mail/i), 'invalid-email');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Should show validation error
      await waitFor(() => {
        expect(screen.getByText(/ungültige e.?mail/i)).toBeInTheDocument();
      });
    });
  });

  // ==================== TEST 4: API Integration & Error Handling ====================
  describe('API Integration', () => {
    it('should successfully create lead and call onCreated callback', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill minimal form (Stage 0 only)
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      // Save directly from Stage 0 (Vormerkung speichern)
      await user.click(screen.getByRole('button', { name: /vormerkung speichern/i }));

      // Should call onCreated callback
      await waitFor(() => {
        expect(mockOnCreated).toHaveBeenCalledTimes(1);
      });
    });

    it('should handle 409 Duplicate Email error', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill form with duplicate email
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1 with complete contact data (ADR-007 Option C: firstName + lastName + email required for Contact creation)
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/nachname/i), 'Mustermann');
      await user.type(screen.getByLabelText(/e.?mail/i), 'duplicate@example.com');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() =>
        expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument()
      );
      await user.click(screen.getByRole('button', { name: /qualifizierung speichern/i }));

      // Should show 409 duplicate error
      await waitFor(() => {
        expect(screen.getByRole('alert')).toBeInTheDocument();
      });

      // Verify error message contains duplicate email text
      const alert = screen.getByRole('alert');
      expect(alert).toHaveTextContent(/lead.*e[-\s]?mail.*existiert bereits/i);

      // Should NOT call onCreated
      expect(mockOnCreated).not.toHaveBeenCalled();
    });

    it('should handle generic API errors', async () => {
      const user = userEvent.setup();

      server.use(
        http.post('http://localhost:8080/api/leads', () => {
          return HttpResponse.json(
            {
              title: 'Internal Server Error',
              status: 500,
              detail: 'Database connection failed',
            },
            { status: 500 }
          );
        })
      );

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill minimal form
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      // Save directly from Stage 0 (Vormerkung speichern)
      await user.click(screen.getByRole('button', { name: /vormerkung speichern/i }));

      // Should show generic error
      await waitFor(() => {
        expect(screen.getByText(/internal server error/i)).toBeInTheDocument();
      });
    });

    it('should disable submit button when saving', async () => {
      const user = userEvent.setup();

      // Simulate slow API
      server.use(
        http.post('http://localhost:8080/api/leads', async () => {
          await new Promise(resolve => setTimeout(resolve, 100));
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill minimal form
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      // Save directly from Stage 0 (Vormerkung speichern)
      const submitButton = screen.getByRole('button', { name: /vormerkung speichern/i });

      await user.click(submitButton);

      // Button should be disabled during save
      expect(submitButton).toBeDisabled();

      // Wait for success
      await waitFor(() => {
        expect(mockOnCreated).toHaveBeenCalledTimes(1);
      });
    });
  });

  // ==================== TEST 5: Pre-Claim Tests (Option C) ====================
  describe('Pre-Claim Logic (Option C - Differenzierte Regelung)', () => {
    it('should allow saving WEB_FORMULAR without Erstkontakt (Pre-Claim)', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill company data
      await user.type(screen.getByLabelText(/firmenname/i), 'Web Lead GmbH');

      // Select WEB_FORMULAR (Erstkontakt optional)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /web.formular/i }));

      // Save WITHOUT Erstkontakt (Pre-Claim)
      const submitButton = screen.getByRole('button', { name: /vormerkung speichern/i });
      await user.click(submitButton);

      // Should succeed (no validation error)
      await waitFor(() => {
        expect(mockOnCreated).toHaveBeenCalledTimes(1);
      });
    });

    it('should require Erstkontakt for MESSE source (no Pre-Claim)', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill company data
      await user.type(screen.getByLabelText(/firmenname/i), 'Messe Lead GmbH');

      // Select MESSE (Erstkontakt PFLICHT)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Try to save WITHOUT Erstkontakt
      const submitButton = screen.getByRole('button', { name: /vormerkung speichern/i });
      await user.click(submitButton);

      // Should show validation error
      await waitFor(() => {
        expect(
          screen.getByText(/erstkontakt ist bei dieser quelle erforderlich/i)
        ).toBeInTheDocument();
      });

      // Should NOT call API
      expect(mockOnCreated).not.toHaveBeenCalled();
    });

    it('should allow saving EMPFEHLUNG without Erstkontakt (Pre-Claim)', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      // Fill company data
      await user.type(screen.getByLabelText(/firmenname/i), 'Empfehlungs-Lead GmbH');

      // Select EMPFEHLUNG (Erstkontakt optional seit 2025-10-04)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /empfehlung.*partner/i }));

      // Save WITHOUT Erstkontakt (Pre-Claim)
      const submitButton = screen.getByRole('button', { name: /vormerkung speichern/i });
      await user.click(submitButton);

      // Should succeed (no validation error)
      await waitFor(() => {
        expect(mockOnCreated).toHaveBeenCalledTimes(1);
      });
    });
  });

  // ==================== TEST 6: Dialog Behavior ====================
  describe('Dialog Behavior', () => {
    it('should close dialog when Abbrechen is clicked', async () => {
      const user = userEvent.setup();

      render(<LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />, {
        wrapper: Wrapper,
      });

      await user.click(screen.getByRole('button', { name: /abbrechen/i }));

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should reset form state after successful submission', async () => {
      const user = userEvent.setup();

      const { rerender } = render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill and submit
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');

      // Select source (PFLICHT seit Refactoring)
      const sourceSelect = screen.getByLabelText(/herkunft/i);
      await user.click(sourceSelect);
      await user.click(await screen.findByRole('option', { name: /messe.*event/i }));

      // Fill Erstkontakt fields (PFLICHT for MESSE)
      await fillErstkontaktFields(user);

      // Save directly from Stage 0 (Vormerkung speichern)
      await user.click(screen.getByRole('button', { name: /vormerkung speichern/i }));

      await waitFor(() => {
        expect(mockOnCreated).toHaveBeenCalledTimes(1);
      });

      // Re-open dialog
      rerender(
        <Wrapper>
          <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />
        </Wrapper>
      );

      // Form should be reset to Stage 0
      expect(screen.getByLabelText(/firmenname/i)).toHaveValue('');
    });
  });
});
