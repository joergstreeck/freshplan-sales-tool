/**
 * ExecuteStep Tests - gemäß testing_guide.md
 * Sprint 2.1.8 - Phase 1: Self-Service Lead-Import
 *
 * Fokus laut Guide:
 * 1. Business-Logic-Tests (PFLICHT) - Duplikat-Behandlung, Ergebnis
 * 2. DTO-Completeness-Tests (EMPFOHLEN) - Import-Ergebnis vollständig
 * 3. Edge-Case-Tests (WICHTIG) - Pending Approval, API-Fehler
 *
 * @since 2025-12-05
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from '@/mocks/server';
import { render } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import freshfoodzTheme from '@/theme/freshfoodz';
import { ExecuteStep } from '../ExecuteStep';
import type { ImportPreviewResponse } from '../../../api/leadImportApi';

// =============================================================================
// Render Helper
// =============================================================================

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0, staleTime: 0 },
      mutations: { retry: false },
    },
  });

  return render(
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>
      </QueryClientProvider>
    </BrowserRouter>
  );
};

// =============================================================================
// Test Data
// =============================================================================

const PREVIEW_DATA: ImportPreviewResponse = {
  uploadId: 'upload-123',
  validation: {
    totalRows: 100,
    validRows: 85,
    errorRows: 5,
    duplicateRows: 10,
  },
  previewRows: [],
  errors: [],
  duplicates: [],
  quotaCheck: {
    approved: true,
    message: '',
    currentOpenLeads: 50,
    maxOpenLeads: 200,
    remainingCapacity: 150,
  },
};

const DEFAULT_PROPS = {
  uploadId: 'upload-123',
  mapping: { Firma: 'companyName', 'E-Mail': 'email' },
  previewData: PREVIEW_DATA,
  source: '',
  duplicateAction: 'SKIP' as const,
  ignoreErrors: false,
  onSettingsChange: vi.fn(),
  onExecuteComplete: vi.fn(),
  onBack: vi.fn(),
  onClose: vi.fn(),
  onError: vi.fn(),
};

const EXECUTE_RESPONSE = {
  success: true,
  importId: 'import-456',
  imported: 85,
  skipped: 10,
  errors: 5,
  status: 'COMPLETED' as const,
  message: '85 Leads erfolgreich importiert',
};

// =============================================================================
// MSW Handlers
// =============================================================================

const setupHandlers = (overrides?: {
  execute?: Partial<typeof EXECUTE_RESPONSE>;
  pendingApproval?: boolean;
  executeError?: boolean;
}) => {
  server.use(
    http.post('http://localhost:8080/api/leads/import/:uploadId/execute', async () => {
      if (overrides?.executeError) {
        return HttpResponse.json(
          { title: 'Import fehlgeschlagen', detail: 'Quota überschritten' },
          { status: 400 }
        );
      }

      if (overrides?.pendingApproval) {
        return HttpResponse.json({
          success: false,
          importId: 'import-pending-789',
          imported: 0,
          skipped: 0,
          errors: 0,
          status: 'PENDING_APPROVAL',
          message: 'Import wartet auf Admin-Freigabe wegen hoher Duplikat-Rate',
        });
      }

      return HttpResponse.json({ ...EXECUTE_RESPONSE, ...overrides?.execute });
    })
  );
};

// =============================================================================
// TESTS: Business Logic (PFLICHT laut testing_guide.md)
// =============================================================================

describe('ExecuteStep - Business Logic', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Import-Zusammenfassung', () => {
    /**
     * BUSINESS RULE: Zusammenfassung zeigt erwartete Import-Zahlen
     */
    it('zeigt Validierungs-Statistik vor Import', () => {
      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} />);

      expect(screen.getByText('Import-Zusammenfassung')).toBeInTheDocument();
      // Use getAllByText since numbers may appear multiple times
      expect(screen.getAllByText('85').length).toBeGreaterThan(0); // validRows
      expect(screen.getAllByText('10').length).toBeGreaterThan(0); // duplicateRows
      expect(screen.getAllByText('5').length).toBeGreaterThan(0); // errorRows
    });

    it('berechnet erwarteten Import bei SKIP korrekt', () => {
      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} duplicateAction="SKIP" />);

      // Bei SKIP: nur validRows = 85
      expect(screen.getByText(/85 Leads importieren/)).toBeInTheDocument();
    });

    it('berechnet erwarteten Import bei CREATE korrekt', () => {
      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} duplicateAction="CREATE" />);

      // Bei CREATE: validRows + duplicateRows = 85 + 10 = 95
      expect(screen.getByText(/95 Leads importieren/)).toBeInTheDocument();
    });
  });

  describe('Duplikat-Behandlung', () => {
    /**
     * BUSINESS RULE: User kann zwischen SKIP und CREATE wählen
     */
    it('zeigt Duplikat-Optionen wenn Duplikate vorhanden', () => {
      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} />);

      expect(screen.getByText('Duplikat-Behandlung')).toBeInTheDocument();
      expect(screen.getByText(/Duplikate überspringen/)).toBeInTheDocument();
      expect(screen.getByText(/Duplikate trotzdem anlegen/)).toBeInTheDocument();
    });

    it('ruft onSettingsChange bei Änderung der Duplikat-Behandlung', async () => {
      const onSettingsChange = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} onSettingsChange={onSettingsChange} />);

      await user.click(screen.getByLabelText(/Duplikate trotzdem anlegen/));

      expect(onSettingsChange).toHaveBeenCalledWith({ duplicateAction: 'CREATE' });
    });

    it('zeigt Warnung bei hoher Duplikat-Rate (>10%)', () => {
      renderWithProviders(
        <ExecuteStep
          {...DEFAULT_PROPS}
          previewData={{
            ...PREVIEW_DATA,
            validation: { totalRows: 100, validRows: 80, errorRows: 5, duplicateRows: 15 },
          }}
        />
      );

      expect(screen.getByText(/Mehr als 10% der Leads sind Duplikate/)).toBeInTheDocument();
    });
  });

  describe('Import-Ausführung', () => {
    /**
     * BUSINESS RULE: Nach erfolgreichem Import Statistik anzeigen
     */
    it('führt Import aus und zeigt Erfolg', async () => {
      setupHandlers();
      const onExecuteComplete = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} onExecuteComplete={onExecuteComplete} />);

      await user.click(screen.getByRole('button', { name: /Leads importieren/i }));

      await waitFor(() => {
        expect(screen.getByText('Import erfolgreich!')).toBeInTheDocument();
      });

      expect(onExecuteComplete).toHaveBeenCalledWith(
        expect.objectContaining({
          success: true,
          imported: 85,
        })
      );
    });

    /**
     * NOTE: Test "zeigt Fortschritts-Indikator während Import" entfernt.
     * Race Condition: MSW-Response ist schneller als React-Render.
     * Loading-State wird durch E2E-Tests abgedeckt.
     */
  });

  describe('Quelle (Source)', () => {
    /**
     * BUSINESS RULE: Optionales Source-Feld für Tracking
     */
    it('zeigt Source-Eingabefeld', () => {
      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} />);

      expect(screen.getByLabelText(/Quelle/)).toBeInTheDocument();
    });

    it('ruft onSettingsChange bei Source-Änderung', async () => {
      const onSettingsChange = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} onSettingsChange={onSettingsChange} />);

      await user.type(screen.getByLabelText(/Quelle/), 'MESSE_2025');

      expect(onSettingsChange).toHaveBeenCalledWith({ source: 'M' }); // Erstes Zeichen
    });
  });
});

// =============================================================================
// TESTS: DTO Completeness (EMPFOHLEN laut testing_guide.md)
// =============================================================================

describe('ExecuteStep - DTO Completeness', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('zeigt alle Ergebnis-Felder nach Import', async () => {
    setupHandlers({
      execute: {
        imported: 77, // Unique numbers to avoid duplicates
        skipped: 18,
        errors: 3,
      },
    });
    const user = userEvent.setup();

    renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} />);

    await user.click(screen.getByRole('button', { name: /Leads importieren/i }));

    await waitFor(() => {
      expect(screen.getByText('Import erfolgreich!')).toBeInTheDocument();
    });

    // Imported
    expect(screen.getByText('77')).toBeInTheDocument();
    expect(screen.getByText('Importiert')).toBeInTheDocument();

    // Skipped
    expect(screen.getByText('18')).toBeInTheDocument();
    expect(screen.getByText('Übersprungen')).toBeInTheDocument();

    // Errors
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('Fehler')).toBeInTheDocument();
  });

  it('zeigt Import-Nachricht', async () => {
    setupHandlers({
      execute: {
        message: '75 Leads wurden erfolgreich importiert',
      },
    });
    const user = userEvent.setup();

    renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} />);

    await user.click(screen.getByRole('button', { name: /Leads importieren/i }));

    await waitFor(() => {
      expect(screen.getByText('75 Leads wurden erfolgreich importiert')).toBeInTheDocument();
    });
  });
});

// =============================================================================
// TESTS: Edge Cases (WICHTIG laut testing_guide.md)
// =============================================================================

describe('ExecuteStep - Edge Cases', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Pending Approval (>10% Duplikate)', () => {
    /**
     * BUSINESS RULE: Bei hoher Duplikat-Rate wird Import zur Freigabe weitergeleitet
     */
    it('zeigt Pending Approval Status', async () => {
      setupHandlers({ pendingApproval: true });
      const user = userEvent.setup();

      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} />);

      await user.click(screen.getByRole('button', { name: /Leads importieren/i }));

      await waitFor(() => {
        expect(screen.getByText('Import wartet auf Freigabe')).toBeInTheDocument();
      });
    });

    it('zeigt Hinweis zur Manager-Freigabe', async () => {
      setupHandlers({ pendingApproval: true });
      const user = userEvent.setup();

      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} />);

      await user.click(screen.getByRole('button', { name: /Leads importieren/i }));

      await waitFor(() => {
        expect(
          screen.getByText(/muss von einem Manager oder Admin freigegeben/)
        ).toBeInTheDocument();
      });
    });
  });

  describe('API-Fehler', () => {
    it('ruft onError bei API-Fehler', async () => {
      setupHandlers({ executeError: true });
      const onError = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} onError={onError} />);

      await user.click(screen.getByRole('button', { name: /Leads importieren/i }));

      await waitFor(() => {
        expect(onError).toHaveBeenCalled();
      });
    });
  });

  describe('Keine Duplikate', () => {
    it('zeigt KEINE Duplikat-Optionen wenn keine Duplikate', () => {
      renderWithProviders(
        <ExecuteStep
          {...DEFAULT_PROPS}
          previewData={{
            ...PREVIEW_DATA,
            validation: { totalRows: 100, validRows: 95, errorRows: 5, duplicateRows: 0 },
          }}
        />
      );

      expect(screen.queryByText('Duplikat-Behandlung')).not.toBeInTheDocument();
    });
  });

  describe('Keine Fehler', () => {
    it('zeigt KEINE Fehler-Optionen wenn keine Fehler', () => {
      renderWithProviders(
        <ExecuteStep
          {...DEFAULT_PROPS}
          previewData={{
            ...PREVIEW_DATA,
            validation: { totalRows: 100, validRows: 90, errorRows: 0, duplicateRows: 10 },
          }}
        />
      );

      expect(screen.queryByText(/Fehlerhafte Zeilen überspringen/)).not.toBeInTheDocument();
    });
  });

  describe('Navigation', () => {
    it('ruft onBack beim Klick auf Zurück', async () => {
      const onBack = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} onBack={onBack} />);

      await user.click(screen.getByRole('button', { name: /Zurück/i }));

      expect(onBack).toHaveBeenCalled();
    });

    it('ruft onClose nach erfolgreichem Import', async () => {
      setupHandlers();
      const onClose = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(<ExecuteStep {...DEFAULT_PROPS} onClose={onClose} />);

      await user.click(screen.getByRole('button', { name: /Leads importieren/i }));

      await waitFor(() => {
        expect(screen.getByText('Import erfolgreich!')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: /Schließen/i }));

      expect(onClose).toHaveBeenCalled();
    });
  });
});
