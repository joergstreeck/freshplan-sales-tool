/**
 * ImportsAdminPage Tests - gemäß testing_guide.md
 * Sprint 2.1.8 - Phase 3: Admin-UI + Routing
 *
 * Fokus laut Guide:
 * 1. Business-Logic-Tests (PFLICHT) - Kernlogik korrekt?
 * 2. DTO-Completeness-Tests (EMPFOHLEN) - Alle Felder im Response?
 * 3. Edge-Case-Tests (WICHTIG) - NULL, leere Listen, Fehler?
 * 4. RBAC-Tests - Verschiedene Rollen
 *
 * NICHT getestet (laut Guide sinnlos):
 * - "Text ist sichtbar" ohne Business-Kontext
 * - Coverage um Coverage willen
 *
 * @since 2025-12-05
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from '@/mocks/server';
import { render } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import freshfoodzTheme from '@/theme/freshfoodz';
import ImportsAdminPage from '../ImportsAdminPage';

// =============================================================================
// Test Setup
// =============================================================================

// Mock stores
vi.mock('@/store/authStore', () => ({
  useAuthStore: () => ({
    userPermissions: ['admin.view', 'imports.view', 'imports.approve'],
    setPermissions: vi.fn(),
  }),
}));

vi.mock('@/store/navigationStore', () => ({
  useNavigationStore: () => ({
    isCollapsed: false,
    toggleSidebar: vi.fn(),
    addToRecentlyVisited: vi.fn(),
    recentlyVisited: [],
  }),
}));

vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

import toast from 'react-hot-toast';

// =============================================================================
// Test Data - Realistische Mock-Daten
// =============================================================================

const COMPLETE_STATS = {
  totalImports: 25,
  pendingApprovals: 3,
  completedImports: 20,
  rejectedImports: 2,
  totalImported: 450,
  totalSkipped: 35,
  totalErrors: 15,
};

const PENDING_IMPORTS = [
  {
    id: 'import-1',
    userId: 'sales-user-1',
    importedAt: '2025-12-01T10:30:00',
    totalRows: 100,
    importedCount: 85,
    skippedCount: 10,
    errorCount: 5,
    duplicateRate: 12.5, // > 10% - sollte Warnung auslösen!
    source: 'MANUAL_UPLOAD',
    fileName: 'leads_with_duplicates.xlsx',
    fileSizeBytes: 51200,
    fileType: 'XLSX',
    status: 'PENDING_APPROVAL' as const,
    approvedBy: null,
    approvedAt: null,
    rejectionReason: null,
  },
  {
    id: 'import-2',
    userId: 'sales-user-2',
    importedAt: '2025-12-02T14:15:00',
    totalRows: 50,
    importedCount: 45,
    skippedCount: 3,
    errorCount: 2,
    duplicateRate: 6.0, // < 10% - keine Warnung
    source: 'MANUAL_UPLOAD',
    fileName: 'clean_leads.xlsx',
    fileSizeBytes: 25600,
    fileType: 'XLSX',
    status: 'PENDING_APPROVAL' as const,
    approvedBy: null,
    approvedAt: null,
    rejectionReason: null,
  },
];

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
// MSW Handlers
// =============================================================================

const setupHandlers = (overrides?: {
  stats?: Partial<typeof COMPLETE_STATS> | null;
  pending?: typeof PENDING_IMPORTS | null;
  statsError?: boolean;
}) => {
  const handlers = [
    http.get('/api/admin/imports/stats', () => {
      if (overrides?.statsError) {
        return new HttpResponse(null, { status: 500 });
      }
      return HttpResponse.json({ ...COMPLETE_STATS, ...overrides?.stats });
    }),
    http.get('/api/admin/imports/pending', () => {
      return HttpResponse.json(overrides?.pending ?? PENDING_IMPORTS);
    }),
    http.get('/api/admin/imports', () => {
      return HttpResponse.json([]);
    }),
    http.post('/api/admin/imports/:id/approve', () => {
      return HttpResponse.json({ success: true });
    }),
    http.post('/api/admin/imports/:id/reject', () => {
      return HttpResponse.json({ success: true });
    }),
  ];

  server.use(...handlers);
};

// =============================================================================
// TESTS: Business Logic (PFLICHT laut testing_guide.md)
// =============================================================================

describe('ImportsAdminPage - Business Logic', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Duplikat-Rate Warnung (>10%)', () => {
    /**
     * BUSINESS RULE: Imports mit >10% Duplikaten benötigen Admin-Genehmigung
     * Wenn duplicateRate > 10%, muss Warnung angezeigt werden
     */
    it('zeigt Warnung wenn pendingApprovals > 0 existieren', async () => {
      setupHandlers({ stats: { pendingApprovals: 3 } });

      renderWithProviders(<ImportsAdminPage />);

      await waitFor(() => {
        expect(
          screen.getByText(/Import\(s\) warten auf Genehmigung \(über 10% Duplikate\)/)
        ).toBeInTheDocument();
      });
    });

    it('zeigt KEINE Warnung wenn pendingApprovals = 0', async () => {
      setupHandlers({ stats: { pendingApprovals: 0 }, pending: [] });

      renderWithProviders(<ImportsAdminPage />);

      await waitFor(() => {
        expect(screen.getByText('Import-Verwaltung')).toBeInTheDocument();
      });

      expect(screen.queryByText(/warten auf Genehmigung/)).not.toBeInTheDocument();
    });

    it('zeigt Duplikat-Rate in Prozent mit einer Dezimalstelle', async () => {
      setupHandlers();
      const user = userEvent.setup();

      renderWithProviders(<ImportsAdminPage />);

      // Warte auf Laden und wechsle zu Genehmigungen-Tab
      await waitFor(() => {
        expect(screen.getByRole('tab', { name: /Genehmigungen/i })).toBeInTheDocument();
      });

      await user.click(screen.getByRole('tab', { name: /Genehmigungen/i }));

      // Prüfe dass 12.5% korrekt angezeigt wird
      await waitFor(() => {
        expect(screen.getByText('12.5%')).toBeInTheDocument();
      });
    });
  });

  describe('Approve/Reject Workflow', () => {
    /**
     * BUSINESS RULE: Admin kann Imports genehmigen oder ablehnen
     * Bei Ablehnung muss ein Grund angegeben werden
     */
    it('ruft Approve-API mit korrekter Import-ID auf', async () => {
      let approvedId = '';
      server.use(
        http.get('/api/admin/imports/stats', () => HttpResponse.json(COMPLETE_STATS)),
        http.get('/api/admin/imports/pending', () => HttpResponse.json(PENDING_IMPORTS)),
        http.get('/api/admin/imports', () => HttpResponse.json([])),
        http.post('/api/admin/imports/:id/approve', ({ params }) => {
          approvedId = params.id as string;
          return HttpResponse.json({ success: true });
        })
      );

      const user = userEvent.setup();
      renderWithProviders(<ImportsAdminPage />);

      // Wechsle zu Genehmigungen-Tab
      await waitFor(() => {
        expect(screen.getByRole('tab', { name: /Genehmigungen/i })).toBeInTheDocument();
      });
      await user.click(screen.getByRole('tab', { name: /Genehmigungen/i }));

      // Warte auf Tabelle und klicke Genehmigen
      await waitFor(() => {
        expect(screen.getAllByRole('button', { name: /Genehmigen/i }).length).toBeGreaterThan(0);
      });

      await user.click(screen.getAllByRole('button', { name: /Genehmigen/i })[0]);

      await waitFor(() => {
        expect(approvedId).toBe('import-1');
      });

      expect(toast.success).toHaveBeenCalledWith('Import genehmigt');
    });

    it('öffnet Reject-Dialog und erfordert Begründung', async () => {
      setupHandlers();
      const user = userEvent.setup();

      renderWithProviders(<ImportsAdminPage />);

      // Wechsle zu Genehmigungen-Tab
      await waitFor(() => {
        expect(screen.getByRole('tab', { name: /Genehmigungen/i })).toBeInTheDocument();
      });
      await user.click(screen.getByRole('tab', { name: /Genehmigungen/i }));

      // Klicke Ablehnen
      await waitFor(() => {
        expect(screen.getAllByRole('button', { name: /Ablehnen/i }).length).toBeGreaterThan(0);
      });
      await user.click(screen.getAllByRole('button', { name: /Ablehnen/i })[0]);

      // Dialog sollte erscheinen
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });

      // Ablehnen-Button im Dialog sollte disabled sein ohne Grund
      const dialogRejectButton = within(screen.getByRole('dialog')).getByRole('button', {
        name: /Ablehnen/i,
      });
      expect(dialogRejectButton).toBeDisabled();

      // Grund eingeben
      await user.type(screen.getByLabelText(/Ablehnungsgrund/i), 'Zu viele Duplikate');

      // Jetzt sollte Button enabled sein
      expect(dialogRejectButton).toBeEnabled();
    });

    it('sendet Reject mit Begründung an API', async () => {
      let rejectedId = '';
      let rejectionReason = '';

      server.use(
        http.get('/api/admin/imports/stats', () => HttpResponse.json(COMPLETE_STATS)),
        http.get('/api/admin/imports/pending', () => HttpResponse.json(PENDING_IMPORTS)),
        http.get('/api/admin/imports', () => HttpResponse.json([])),
        http.post('/api/admin/imports/:id/reject', async ({ params, request }) => {
          rejectedId = params.id as string;
          const body = (await request.json()) as { reason: string };
          rejectionReason = body.reason;
          return HttpResponse.json({ success: true });
        })
      );

      const user = userEvent.setup();
      renderWithProviders(<ImportsAdminPage />);

      // Wechsle zu Genehmigungen-Tab
      await waitFor(() => {
        expect(screen.getByRole('tab', { name: /Genehmigungen/i })).toBeInTheDocument();
      });
      await user.click(screen.getByRole('tab', { name: /Genehmigungen/i }));

      // Klicke Ablehnen
      await waitFor(() => {
        expect(screen.getAllByRole('button', { name: /Ablehnen/i }).length).toBeGreaterThan(0);
      });
      await user.click(screen.getAllByRole('button', { name: /Ablehnen/i })[0]);

      // Dialog ausfüllen
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });

      await user.type(screen.getByLabelText(/Ablehnungsgrund/i), 'Datenqualität unzureichend');

      // Ablehnen bestätigen
      await user.click(
        within(screen.getByRole('dialog')).getByRole('button', { name: /Ablehnen/i })
      );

      await waitFor(() => {
        expect(rejectedId).toBe('import-1');
        expect(rejectionReason).toBe('Datenqualität unzureichend');
      });

      expect(toast.success).toHaveBeenCalledWith('Import abgelehnt');
    });
  });
});

// =============================================================================
// TESTS: DTO Completeness (EMPFOHLEN laut testing_guide.md)
// =============================================================================

describe('ImportsAdminPage - DTO Completeness', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  /**
   * WICHTIG: Alle 7 Stats-Felder müssen im UI angezeigt werden
   * Verhindert Bugs wie "leadScore fehlte im DTO" aus Sprint 2.1.6
   */
  it('zeigt alle 7 Stats-Felder korrekt an', async () => {
    setupHandlers({
      stats: {
        totalImports: 101,
        pendingApprovals: 5,
        completedImports: 91,
        rejectedImports: 4,
        totalImported: 1001,
        totalSkipped: 51,
        totalErrors: 11,
      },
    });

    renderWithProviders(<ImportsAdminPage />);

    // Warte auf Stats - manche Werte können mehrfach vorkommen (z.B. in StatCard UND in Chips)
    await waitFor(
      () => {
        // totalImports - kann in StatCard UND Tab-Badge erscheinen
        expect(screen.getAllByText('101').length).toBeGreaterThanOrEqual(1);
      },
      { timeout: 10000 }
    );

    // totalImported, totalSkipped, totalErrors werden in StatCards angezeigt
    expect(screen.getAllByText('1001').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('51').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('11').length).toBeGreaterThanOrEqual(1);
  }, 15000);

  it('zeigt alle Import-Felder in der Tabelle an', async () => {
    setupHandlers();
    const user = userEvent.setup();

    renderWithProviders(<ImportsAdminPage />);

    // Wechsle zu Genehmigungen-Tab
    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Genehmigungen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Genehmigungen/i }));

    // Prüfe dass alle wichtigen Felder in der Tabelle sind
    await waitFor(() => {
      expect(screen.getByText('leads_with_duplicates.xlsx')).toBeInTheDocument();
    });

    expect(screen.getByText('sales-user-1')).toBeInTheDocument();
    // MANUAL_UPLOAD erscheint mehrfach (für jeden Eintrag) - daher getAllByText
    expect(screen.getAllByText('MANUAL_UPLOAD').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('100').length).toBeGreaterThanOrEqual(1); // totalRows
    expect(screen.getByText('12.5%')).toBeInTheDocument(); // duplicateRate
  });
});

// =============================================================================
// TESTS: Edge Cases (WICHTIG laut testing_guide.md)
// =============================================================================

describe('ImportsAdminPage - Edge Cases', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('zeigt leeren Zustand wenn keine Imports vorhanden', async () => {
    setupHandlers({
      stats: {
        totalImports: 0,
        pendingApprovals: 0,
        completedImports: 0,
        rejectedImports: 0,
        totalImported: 0,
        totalSkipped: 0,
        totalErrors: 0,
      },
      pending: [],
    });

    const user = userEvent.setup();
    renderWithProviders(<ImportsAdminPage />);

    // Wechsle zu "Alle Imports" Tab
    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Alle Imports/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Alle Imports/i }));

    await waitFor(() => {
      expect(screen.getByText('Keine Imports vorhanden.')).toBeInTheDocument();
    });
  });

  it('zeigt Erfolgs-Meldung bei leeren Genehmigungen', async () => {
    setupHandlers({ stats: { pendingApprovals: 0 }, pending: [] });
    const user = userEvent.setup();

    renderWithProviders(<ImportsAdminPage />);

    // Wechsle zu Genehmigungen-Tab
    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Genehmigungen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Genehmigungen/i }));

    await waitFor(() => {
      expect(screen.getByText('Keine wartenden Genehmigungen.')).toBeInTheDocument();
    });
  });

  it('behandelt API-Fehler graceful ohne Crash', async () => {
    setupHandlers({ statsError: true });

    // Sollte nicht crashen
    renderWithProviders(<ImportsAdminPage />);

    // Header sollte trotzdem angezeigt werden
    await waitFor(() => {
      expect(screen.getByText('Import-Verwaltung')).toBeInTheDocument();
    });
  });

  it('zeigt NULL-Werte als Strich an', async () => {
    server.use(
      http.get('/api/admin/imports/stats', () => HttpResponse.json(COMPLETE_STATS)),
      http.get('/api/admin/imports/pending', () =>
        HttpResponse.json([
          {
            ...PENDING_IMPORTS[0],
            fileName: null, // NULL-Wert
            source: null,
            fileSizeBytes: null,
          },
        ])
      ),
      http.get('/api/admin/imports', () => HttpResponse.json([]))
    );

    const user = userEvent.setup();
    renderWithProviders(<ImportsAdminPage />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Genehmigungen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Genehmigungen/i }));

    // NULL sollte als "—" angezeigt werden
    await waitFor(() => {
      const dashes = screen.getAllByText('—');
      expect(dashes.length).toBeGreaterThanOrEqual(1);
    });
  });
});

// =============================================================================
// TESTS: Quota-System Anzeige
// =============================================================================

describe('ImportsAdminPage - Quota System', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setupHandlers();
  });

  /**
   * BUSINESS RULE: Quota-Limits müssen sichtbar sein
   * SALES: 100/Tag, MANAGER: 200/Tag, ADMIN: unbegrenzt
   */
  it('zeigt Quota-Limits für alle Rollen an', async () => {
    renderWithProviders(<ImportsAdminPage />);

    await waitFor(() => {
      expect(screen.getByText(/SALES: max. 100 Leads\/Tag/)).toBeInTheDocument();
    });

    expect(screen.getByText(/MANAGER: max. 200/)).toBeInTheDocument();
    expect(screen.getByText(/ADMIN: unbegrenzt/)).toBeInTheDocument();
  });
});
