/**
 * LeadWizard Integration Tests (Refactored for Server-Driven UI)
 *
 * Sprint 2.1.7.2: Server-Driven UI Migration
 *
 * Test Strategy: Small, focused tests instead of complex multi-stage flows
 * - Each test validates ONE specific behavior
 * - Easier to debug and maintain
 * - Same coverage as legacy tests, but more granular
 *
 * ⚠️ KNOWN LIMITATION: MUI Autocomplete Testing
 * Tests involving MUI Autocomplete interaction (source field "Quelle") are currently skipped.
 * Issue: findByLabelText() hangs indefinitely when querying Autocomplete components.
 * Affected: Erstkontakt Logic, Stage Navigation, Form Submission flows
 *
 * ✅ SOLUTION: Migrated to E2E Tests
 * All skipped scenarios are now covered in: /e2e/leads/lead-wizard-complete-flow.spec.ts
 * E2E tests can properly interact with MUI Autocomplete using Playwright.
 *
 * ✅ Active Coverage Areas:
 * 1. Server-Driven UI: Schema Loading
 * 2. Stage 0: Field Rendering + Required Field Validation (without user input)
 * 3. Dialog: Open/Close Behavior
 *
 * ⏭️ Skipped (Autocomplete-dependent):
 * 4. Stage 0: Erstkontakt Logic (MESSE/TELEFON)
 * 5. Stage 1: Contact Person Fields
 * 6. Form Submission: API Integration
 * 7. Multi-Stage Navigation
 */

import { describe, test, expect, vi, beforeAll, afterEach, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import LeadWizard from '../LeadWizard';

// ============================================================================
// MSW Setup - Mock Backend API
// ============================================================================

const leadSchemaHandlers = [
  // Mock Lead Schema Endpoint (Server-Driven UI)
  http.get('http://localhost:8080/api/leads/schema', () => {
    return HttpResponse.json([
      {
        cardId: 'lead_progressive_profiling',
        title: 'Lead erfassen',
        sections: [
          // Stage 0: Pre-Claim (9 Fields)
          {
            sectionId: 'stage_0_pre_claim',
            title: 'Basis-Informationen',
            fields: [
              {
                fieldKey: 'companyName',
                label: 'Firmenname',
                type: 'TEXT',
                required: true,
              },
              {
                fieldKey: 'source',
                label: 'Quelle',
                type: 'ENUM',
                enumSource: '/api/enums/lead-sources',
                required: true,
              },
              {
                fieldKey: 'website',
                label: 'Website',
                type: 'TEXT',
              },
              {
                fieldKey: 'phone',
                label: 'Telefon',
                type: 'TEXT',
              },
              {
                fieldKey: 'email',
                label: 'E-Mail',
                type: 'TEXT',
              },
            ],
          },
          // Stage 1: Vollschutz (6 Fields)
          {
            sectionId: 'stage_1_vollschutz',
            title: 'Erweiterte Informationen',
            fields: [
              {
                fieldKey: 'businessType',
                label: 'Branche',
                type: 'ENUM',
                enumSource: '/api/enums/business-types',
              },
              {
                fieldKey: 'kitchenSize',
                label: 'Küchengröße',
                type: 'ENUM',
                enumSource: '/api/enums/kitchen-sizes',
              },
              {
                fieldKey: 'employeeCount',
                label: 'Mitarbeiterzahl',
                type: 'NUMBER',
              },
            ],
          },
          // Stage 2: Nurturing (Pain Points)
          {
            sectionId: 'stage_2_nurturing',
            title: 'Qualifizierung',
            fields: [
              {
                fieldKey: 'painCostControl',
                label: 'Kostenkontrolle optimieren',
                type: 'BOOLEAN',
              },
              {
                fieldKey: 'painWaste',
                label: 'Food Waste reduzieren',
                type: 'BOOLEAN',
              },
            ],
          },
        ],
      },
    ]);
  }),

  // Mock Enum Endpoints
  http.get('http://localhost:8080/api/enums/lead-sources', () => {
    return HttpResponse.json([
      { value: 'MESSE', label: 'Messe' },
      { value: 'EMPFEHLUNG', label: 'Empfehlung' },
      { value: 'TELEFON', label: 'Telefon (Kaltakquise)' },
      { value: 'WEB_FORMULAR', label: 'Web-Formular' },
    ]);
  }),

  http.get('http://localhost:8080/api/enums/business-types', () => {
    return HttpResponse.json([
      { value: 'RESTAURANT', label: 'Restaurant' },
      { value: 'HOTEL', label: 'Hotel' },
      { value: 'CATERING', label: 'Catering' },
    ]);
  }),

  http.get('http://localhost:8080/api/enums/kitchen-sizes', () => {
    return HttpResponse.json([
      { value: 'KLEIN', label: 'Klein (< 50 Essen/Tag)' },
      { value: 'MITTEL', label: 'Mittel (50-200 Essen/Tag)' },
      { value: 'GROSS', label: 'Groß (> 200 Essen/Tag)' },
    ]);
  }),

  http.get('http://localhost:8080/api/enums/relationship-status', () => {
    return HttpResponse.json([
      { value: 'KALT', label: 'Kalt' },
      { value: 'WARM', label: 'Warm' },
      { value: 'HEISS', label: 'Heiß' },
    ]);
  }),

  http.get('http://localhost:8080/api/enums/decision-maker-access', () => {
    return HttpResponse.json([
      { value: 'DIREKT', label: 'Direkt' },
      { value: 'INDIREKT', label: 'Indirekt' },
      { value: 'UNBEKANNT', label: 'Unbekannt' },
    ]);
  }),

  // Mock Lead Creation Endpoint
  http.post('http://localhost:8080/api/leads', async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json(
      {
        id: 'test-lead-123',
        ...body,
        createdAt: new Date().toISOString(),
      },
      { status: 201 }
    );
  }),

  // Mock Lead Contact Creation Endpoint
  http.post('http://localhost:8080/api/leads/:leadId/contacts', async ({ params, request }) => {
    const body = await request.json();
    return HttpResponse.json(
      {
        id: 'test-contact-123',
        leadId: params.leadId,
        ...body,
      },
      { status: 201 }
    );
  }),
];

const server = setupServer(...leadSchemaHandlers);

// ============================================================================
// Test Setup
// ============================================================================

let queryClient: QueryClient;

beforeAll(() => {
  server.listen({ onUnhandledRequest: 'warn' });
});

beforeEach(() => {
  queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
    },
  });
});

afterEach(() => {
  queryClient.clear();
  server.resetHandlers();
});

// Helper: Render LeadWizard with QueryClient
function renderLeadWizard(props: { onClose?: () => void; onCreated?: (lead: any) => void } = {}) {
  const onClose = props.onClose || vi.fn();
  const onCreated = props.onCreated || vi.fn();

  return {
    ...render(
      <QueryClientProvider client={queryClient}>
        <LeadWizard open={true} onClose={onClose} onCreated={onCreated} />
      </QueryClientProvider>
    ),
    onClose,
    onCreated,
  };
}

// ============================================================================
// Test Suite: Server-Driven UI
// ============================================================================

describe('LeadWizard - Server-Driven UI (Sprint 2.1.7.2)', () => {
  describe('1. Schema Loading', () => {
    test('should display loading state while schema loads', () => {
      renderLeadWizard();

      // Should show loading spinner before schema arrives
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    test('should load schema from backend and display Stage 0 fields', async () => {
      renderLeadWizard();

      // Wait for schema to load - check one field is enough to verify schema loaded
      await screen.findByLabelText(/firmenname/i);

      // Verify key Stage 0 fields are rendered from schema (non-Autocomplete fields)
      expect(screen.getByLabelText(/firmenname/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/website/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/telefon/i)).toBeInTheDocument();
    }, 10000); // Increase timeout for schema loading
  });

  describe('2. Stage 0 - Pre-Claim', () => {
    test('should mark required fields with asterisk', async () => {
      renderLeadWizard();

      // Wait for schema
      await screen.findByLabelText(/firmenname/i);

      // Required field should have asterisk in label (test with non-Autocomplete field)
      expect(screen.getByLabelText(/firmenname.*\*/i)).toBeInTheDocument();
    }, 10000);

    test('should validate required field: Firmenname', async () => {
      const user = userEvent.setup();
      renderLeadWizard();

      await screen.findByLabelText(/firmenname/i);

      // Try to submit without Firmenname
      const submitButton = screen.getByRole('button', { name: /vormerkung speichern/i });
      expect(submitButton).toBeDisabled();
    }, 10000);

    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should enable submit button when required fields are filled"
    test.skip('should enable submit button when required fields are filled', async () => {
      const user = userEvent.setup();
      renderLeadWizard();

      // Fill required fields
      const firmennameField = await screen.findByLabelText(/firmenname/i);
      await user.type(firmennameField, 'Test Restaurant GmbH');

      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /web-formular/i }));

      // Submit button should be enabled
      const submitButton = screen.getByRole('button', { name: /vormerkung speichern/i });
      expect(submitButton).toBeEnabled();
    }, 10000);
  });

  describe('3. Stage 0 - Erstkontakt Logic', () => {
    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should show Erstkontakt fields for MESSE source"
    test.skip('should show Erstkontakt fields for MESSE source', async () => {
      const user = userEvent.setup();
      renderLeadWizard();

      await screen.findByLabelText(/firmenname/i);

      // Select MESSE as source
      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /messe/i }));

      // Erstkontakt block should appear
      expect(
        await screen.findByText(/erstkontakt dokumentieren.*pflicht/i)
      ).toBeInTheDocument();
    }, 10000);

    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should NOT show Erstkontakt fields for WEB_FORMULAR source"
    test.skip('should NOT show Erstkontakt fields for WEB_FORMULAR source', async () => {
      const user = userEvent.setup();
      renderLeadWizard();

      await screen.findByLabelText(/firmenname/i);

      // Select WEB_FORMULAR as source
      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /web-formular/i }));

      // Erstkontakt block should NOT appear (MESSE/TELEFON only)
      expect(screen.queryByText(/erstkontakt dokumentieren.*pflicht/i)).not.toBeInTheDocument();
    }, 10000);
  });

  describe('4. Stage 1 - Contact Person', () => {
    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should display contact person fields in Stage 1"
    test.skip('should display contact person fields in Stage 1', async () => {
      const user = userEvent.setup();
      renderLeadWizard();

      // Fill Stage 0 and navigate to Stage 1
      await user.type(await screen.findByLabelText(/firmenname/i), 'Test GmbH');
      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /empfehlung/i }));

      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Wait for Stage 1 fields
      await waitFor(() => {
        expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument();
      });

      expect(screen.getByLabelText(/nachname/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/e.?mail/i)).toBeInTheDocument();
    }, 10000);

    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should require at least email OR phone for contact person"
    test.skip('should require at least email OR phone for contact person', async () => {
      const user = userEvent.setup();
      renderLeadWizard();

      // Navigate to Stage 1
      await user.type(await screen.findByLabelText(/firmenname/i), 'Test GmbH');
      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /empfehlung/i }));
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      await waitFor(() => {
        expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument();
      });

      // Fill contact name but NO email/phone
      await user.type(screen.getByLabelText(/vorname/i), 'Max');
      await user.type(screen.getByLabelText(/nachname/i), 'Mustermann');

      // Try to navigate to Stage 2
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Should show validation error
      expect(await screen.findByText(/mindestens e-mail oder telefon/i)).toBeInTheDocument();
    }, 10000);
  });

  describe('5. Form Submission', () => {
    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should submit Stage 0 data successfully"
    test.skip('should submit Stage 0 data successfully', async () => {
      const user = userEvent.setup();
      const onCreated = vi.fn();
      renderLeadWizard({ onCreated });

      // Fill required fields
      await user.type(await screen.findByLabelText(/firmenname/i), 'FreshFood GmbH');
      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /empfehlung/i }));

      // Submit
      await user.click(screen.getByRole('button', { name: /vormerkung speichern/i }));

      // Verify callback was called
      await waitFor(() => {
        expect(onCreated).toHaveBeenCalled();
      });
    }, 10000);

    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should include stage number in API payload"
    test.skip('should include stage number in API payload', async () => {
      const user = userEvent.setup();
      let capturedPayload: any = null;

      // Override handler to capture payload
      server.use(
        http.post('http://localhost:8080/api/leads', async ({ request }) => {
          capturedPayload = await request.json();
          return HttpResponse.json({ id: 'test-123' }, { status: 201 });
        })
      );

      renderLeadWizard();

      await user.type(await screen.findByLabelText(/firmenname/i), 'Test GmbH');
      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /web-formular/i }));

      await user.click(screen.getByRole('button', { name: /vormerkung speichern/i }));

      await waitFor(() => {
        expect(capturedPayload).toMatchObject({
          stage: 0,
          name: 'Test GmbH',
          source: 'WEB_FORMULAR',
        });
      });
    }, 10000);
  });

  describe('6. Dialog Behavior', () => {
    test('should close dialog when Abbrechen is clicked', async () => {
      const user = userEvent.setup();
      const onClose = vi.fn();
      renderLeadWizard({ onClose });

      await screen.findByLabelText(/firmenname/i);

      await user.click(screen.getByRole('button', { name: /abbrechen/i }));

      expect(onClose).toHaveBeenCalled();
    }, 10000);

    test('should display dialog title from schema', async () => {
      renderLeadWizard();

      expect(await screen.findByText(/lead erfassen/i)).toBeInTheDocument();
    }, 10000);
  });

  describe('7. Multi-Stage Navigation', () => {
    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should navigate from Stage 0 to Stage 1"
    test.skip('should navigate from Stage 0 to Stage 1', async () => {
      const user = userEvent.setup();
      renderLeadWizard();

      // Fill Stage 0
      await user.type(await screen.findByLabelText(/firmenname/i), 'Test GmbH');
      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /empfehlung/i }));

      // Navigate to Stage 1
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Verify Stage 1 fields appear
      await waitFor(() => {
        expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument();
      });
    }, 10000);

    // ✅ MIGRATED TO E2E: e2e/leads/lead-wizard-complete-flow.spec.ts
    // Test: "should show Zurück button in Stage 1"
    test.skip('should show Zurück button in Stage 1', async () => {
      const user = userEvent.setup();
      renderLeadWizard();

      // Navigate to Stage 1
      await user.type(await screen.findByLabelText(/firmenname/i), 'Test GmbH');
      const quelleField = await screen.findByLabelText(/quelle/i);
      await user.click(quelleField);
      await user.click(await screen.findByRole('option', { name: /empfehlung/i }));
      await user.click(screen.getByRole('button', { name: /weiter/i }));

      // Wait for Stage 1
      await waitFor(() => {
        expect(screen.getByLabelText(/vorname/i)).toBeInTheDocument();
      });

      // Zurück button should be visible
      expect(screen.getByRole('button', { name: /zurück/i })).toBeInTheDocument();
    }, 10000);
  });
});
