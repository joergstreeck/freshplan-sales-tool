// Sprint 2.1.5 Frontend Phase 2 - LeadWizard Integration Tests (MSW-based)
// Test Coverage: Progressive Flow, DSGVO Consent Validation, Stage-Transition Rules

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import { I18nextProvider } from 'react-i18next';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import i18n from '../../../i18n';
import LeadWizard from '../LeadWizard';
import freshfoodzTheme from '../../../theme/freshfoodz';

// Test wrapper with theme and i18n
const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <I18nextProvider i18n={i18n}>
    <ThemeProvider theme={freshfoodzTheme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  </I18nextProvider>
);

// MSW Server Setup
const server = setupServer(
  // Default successful lead creation handler
  http.post('http://localhost:8080/api/leads', async ({ request }) => {
    const payload = (await request.json()) as {
      stage?: number;
      companyName: string;
      contact?: { firstName?: string; lastName?: string; email?: string; phone?: string };
      consentGivenAt?: string;
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
        contact: payload.contact,
        consentGivenAt: payload.consentGivenAt,
        status: 'REGISTERED',
        createdAt: new Date().toISOString(),
      },
      { status: 201 }
    );
  })
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
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

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Stage 0: Vormerkung
      expect(screen.getByText('Vormerkung')).toBeInTheDocument();
      expect(screen.getByText('Registrierung')).toBeInTheDocument();
      expect(screen.getByText('Qualifizierung')).toBeInTheDocument();

      expect(screen.getByLabelText(/firmenname/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /weiter/i })).toBeInTheDocument();

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant Berlin');

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

      // DSGVO Consent Checkbox should be visible and NOT pre-filled
      const consentCheckbox = screen.getByRole('checkbox', { name: /ich stimme zu/i });
      expect(consentCheckbox).toBeInTheDocument();
      expect(consentCheckbox).not.toBeChecked(); // NOT pre-filled (DSGVO requirement)

      // Check consent checkbox
      await user.click(consentCheckbox);
      expect(consentCheckbox).toBeChecked();

      // Navigate to Stage 2
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/küchengröße/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/mitarbeiterzahl/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/website/i)).toBeInTheDocument();
      });

      // Final submit button should be visible
      expect(screen.getByRole('button', { name: /lead erstellen/i })).toBeInTheDocument();
    });

    it('should allow navigating back from Stage 2 → 1 → 0 without losing data', async () => {
      const user = userEvent.setup();

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');
      await user.click(screen.getByRole('checkbox', { name: /ich stimme zu/i }));
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 2
      await waitFor(() => expect(screen.getByLabelText(/mitarbeiterzahl/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/mitarbeiterzahl/i), '25');

      // Navigate back to Stage 1
      await user.click(screen.getByRole('button', { name: /zurück/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/vorname/i)).toHaveValue('Max');
        expect(screen.getByLabelText(/e.?mail/i)).toHaveValue('max@example.com');
        expect(screen.getByRole('checkbox', { name: /ich stimme zu/i })).toBeChecked();
      });

      // Navigate back to Stage 0
      await user.click(screen.getByRole('button', { name: /zurück/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/firmenname/i)).toHaveValue('Test Restaurant');
      });
    });
  });

  // ==================== TEST 2: DSGVO Consent Validation ====================
  describe('DSGVO Consent Validation', () => {
    it('should require consent when contact data is provided', async () => {
      const user = userEvent.setup();

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1 without consent
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');

      // Try to navigate to Stage 2 without consent
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Should show validation error (specific error message, not the hint)
      await waitFor(() => {
        expect(screen.getByText('Einwilligung erforderlich für Kontaktdaten')).toBeInTheDocument();
      });

      // Should NOT navigate to Stage 2
      expect(screen.queryByLabelText(/geschätztes volumen/i)).not.toBeInTheDocument();
      expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument(); // Still on Stage 1
    });

    it('should NOT require consent when no contact data is provided', async () => {
      const user = userEvent.setup();

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Skip Stage 1 (no contact data, no consent)
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Should navigate to Stage 2 successfully
      await waitFor(() => {
        expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument();
      });
    });

    it('should send consentGivenAt timestamp in API payload when consent is given', async () => {
      const user = userEvent.setup();
      let capturedPayload: unknown;

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1 with consent
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');
      await user.click(screen.getByRole('checkbox', { name: /ich stimme zu/i }));
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Submit from Stage 2
      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        expect(capturedPayload.consentGivenAt).toBeDefined();
        expect(capturedPayload.consentGivenAt).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/); // ISO 8601
      });
    });

    it('should NOT send consentGivenAt when consent is not given', async () => {
      const user = userEvent.setup();
      let capturedPayload: unknown;

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0 only
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Skip Stage 1
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Submit from Stage 2
      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        expect(capturedPayload.consentGivenAt).toBeUndefined();
      });
    });
  });

  // ==================== TEST 3: Stage-Transition Rules ====================
  describe('Stage-Transition Rules', () => {
    it('should correctly determine stage=0 when only company data is provided', async () => {
      const user = userEvent.setup();
      let capturedPayload: unknown;

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0 only
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Skip Stage 1
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Submit from Stage 2
      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        expect(capturedPayload.stage).toBe(0);
        expect(capturedPayload.contact).toBeUndefined();
      });
    });

    it('should correctly determine stage=1 when contact data + consent is provided', async () => {
      const user = userEvent.setup();
      let capturedPayload: unknown;

      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1 with consent
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');
      await user.click(screen.getByRole('checkbox', { name: /ich stimme zu/i }));
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Submit from Stage 2 (no business data)
      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        expect(capturedPayload.stage).toBe(1);
        expect(capturedPayload.contact).toBeDefined();
        expect(capturedPayload.consentGivenAt).toBeDefined();
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

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 1 with consent
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/e.?mail/i), 'max@example.com');
      await user.click(screen.getByRole('checkbox', { name: /ich stimme zu/i }));
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Fill Stage 2 with business data
      await waitFor(() => expect(screen.getByLabelText(/mitarbeiterzahl/i)).toBeInTheDocument());
      const employeeCountField = screen.getByLabelText(/mitarbeiterzahl/i);
      await user.clear(employeeCountField);
      await user.type(employeeCountField, '25');

      // Wait for value to be updated
      await waitFor(() => expect(employeeCountField).toHaveValue(25));

      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

      await waitFor(() => {
        expect(capturedPayload).toBeDefined();
        expect(capturedPayload.stage).toBe(2);
        expect(capturedPayload.employeeCount).toBe(25);
      });
    });

    it('should validate Stage 0 before allowing navigation (companyName required)', async () => {
      const user = userEvent.setup();

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

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

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill Stage 0
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
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

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill minimal form (Stage 0 only)
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Skip Stage 1
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Submit
      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

      // Should call onCreated callback
      await waitFor(() => {
        expect(mockOnCreated).toHaveBeenCalledTimes(1);
      });
    });

    it('should handle 409 Duplicate Email error', async () => {
      const user = userEvent.setup();

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill form with duplicate email
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => expect(screen.getByLabelText(/e.?mail/i)).toBeInTheDocument());
      await user.type(screen.getByLabelText(/e.?mail/i), 'duplicate@example.com');
      await user.click(screen.getByRole('checkbox', { name: /ich stimme zu/i }));
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

      // Should show 409 duplicate error
      await waitFor(() => {
        expect(screen.getByText(/lead mit dieser e.?mail existiert bereits/i)).toBeInTheDocument();
      });

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

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill minimal form
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

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
          await new Promise((resolve) => setTimeout(resolve, 100));
          return HttpResponse.json({ id: 'lead-123', status: 'REGISTERED' }, { status: 201 });
        })
      );

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

      // Fill minimal form
      await user.type(screen.getByLabelText(/firmenname/i), 'Test Restaurant');
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      const submitButton = screen.getByRole('button', { name: /lead erstellen/i });

      await user.click(submitButton);

      // Button should be disabled during save
      expect(submitButton).toBeDisabled();

      // Wait for success
      await waitFor(() => {
        expect(mockOnCreated).toHaveBeenCalledTimes(1);
      });
    });
  });

  // ==================== TEST 5: Dialog Behavior ====================
  describe('Dialog Behavior', () => {
    it('should close dialog when Abbrechen is clicked', async () => {
      const user = userEvent.setup();

      render(
        <LeadWizard open={true} onClose={mockOnClose} onCreated={mockOnCreated} />,
        { wrapper: Wrapper }
      );

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
      await user.click(screen.getByRole('button', { name: /weiter/i }));
      await waitFor(() => expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /weiter/i }));
      await waitFor(() => expect(screen.getByLabelText(/geschätztes volumen/i)).toBeInTheDocument());
      await user.click(screen.getByRole('button', { name: /lead erstellen/i }));

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
