/**
 * DsgvoAdminPage Tests - gemäß testing_guide.md
 * Sprint 2.1.8 - Phase 3: Admin-UI + Routing
 *
 * Fokus laut Guide:
 * 1. Business-Logic-Tests (PFLICHT) - Kernlogik korrekt?
 * 2. DTO-Completeness-Tests (EMPFOHLEN) - Alle Felder im Response?
 * 3. Edge-Case-Tests (WICHTIG) - NULL, leere Listen, Fehler?
 *
 * NICHT getestet (laut Guide sinnlos):
 * - "Text ist sichtbar" ohne Business-Kontext
 * - Coverage um Coverage willen
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
import DsgvoAdminPage from '../DsgvoAdminPage';

// =============================================================================
// Test Setup
// =============================================================================

// Mock stores
vi.mock('@/store/authStore', () => ({
  useAuthStore: () => ({
    userPermissions: ['admin.view', 'gdpr.view'],
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

// =============================================================================
// Test Data - Realistische Mock-Daten
// =============================================================================

const COMPLETE_STATS = {
  totalDeletions: 12,
  totalDataRequests: 8,
  pendingRequests: 3,
  deletedLeads: 45,
  blockedContacts: 7,
};

const DELETION_LOGS = [
  {
    id: 1,
    entityType: 'Lead',
    entityId: 1001,
    deletedBy: 'admin@freshfoodz.de',
    deletedAt: '2025-12-01T10:30:00',
    deletionReason: 'Art. 17 DSGVO - Löschantrag',
    originalDataHash: 'sha256:abc123...',
  },
  {
    id: 2,
    entityType: 'Customer',
    entityId: 2002,
    deletedBy: 'manager@freshfoodz.de',
    deletedAt: '2025-12-02T14:15:00',
    deletionReason: 'Widerruf der Einwilligung',
    originalDataHash: 'sha256:def456...',
  },
];

const DATA_REQUESTS = [
  {
    id: 1,
    entityType: 'Lead',
    entityId: 1001,
    requestedBy: 'kunde@example.de',
    requestedAt: '2025-12-01T09:00:00',
    pdfGenerated: true,
    pdfGeneratedAt: '2025-12-01T10:00:00',
  },
  {
    id: 2,
    entityType: 'Customer',
    entityId: 2002,
    requestedBy: 'firma@example.de',
    requestedAt: '2025-12-02T11:00:00',
    pdfGenerated: false,
    pdfGeneratedAt: null,
  },
];

const DELETED_LEADS = [
  {
    id: 1,
    companyName: '[GELÖSCHT-001]',
    gdprDeletedAt: '2025-12-01T10:30:00',
    gdprDeletedBy: 'admin@freshfoodz.de',
    gdprDeletionReason: 'Art. 17 DSGVO - Löschantrag',
  },
  {
    id: 2,
    companyName: '[GELÖSCHT-002]',
    gdprDeletedAt: null,
    gdprDeletedBy: 'system',
    gdprDeletionReason: null,
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
  deletions?: typeof DELETION_LOGS | null;
  requests?: typeof DATA_REQUESTS | null;
  leads?: typeof DELETED_LEADS | null;
  statsError?: boolean;
}) => {
  const handlers = [
    http.get('/api/admin/gdpr/stats', () => {
      if (overrides?.statsError) {
        return new HttpResponse(null, { status: 500 });
      }
      return HttpResponse.json({ ...COMPLETE_STATS, ...overrides?.stats });
    }),
    http.get('/api/admin/gdpr/deletions', () => {
      return HttpResponse.json(overrides?.deletions ?? DELETION_LOGS);
    }),
    http.get('/api/admin/gdpr/data-requests', () => {
      return HttpResponse.json(overrides?.requests ?? DATA_REQUESTS);
    }),
    http.get('/api/admin/gdpr/deleted-leads', () => {
      return HttpResponse.json(overrides?.leads ?? DELETED_LEADS);
    }),
  ];

  server.use(...handlers);
};

// =============================================================================
// TESTS: Business Logic (PFLICHT laut testing_guide.md)
// =============================================================================

describe('DsgvoAdminPage - Business Logic', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Pending Requests Warnung', () => {
    /**
     * BUSINESS RULE: Offene DSGVO-Anfragen müssen hervorgehoben werden
     * Art. 15 DSGVO erfordert Auskunft innerhalb eines Monats
     */
    it('zeigt Warnung wenn pendingRequests > 0', async () => {
      setupHandlers({ stats: { pendingRequests: 5 } });

      renderWithProviders(<DsgvoAdminPage />);

      await waitFor(() => {
        expect(screen.getByText(/5 Datenexport-Anfrage\(n\) noch offen/)).toBeInTheDocument();
      });
    });

    it('zeigt KEINE Warnung wenn pendingRequests = 0', async () => {
      setupHandlers({ stats: { pendingRequests: 0 } });

      renderWithProviders(<DsgvoAdminPage />);

      await waitFor(() => {
        expect(screen.getByText('DSGVO-Verwaltung')).toBeInTheDocument();
      });

      expect(screen.queryByText(/noch offen/)).not.toBeInTheDocument();
    });
  });

  describe('PDF-Export Status (Art. 15)', () => {
    /**
     * BUSINESS RULE: PDF-Status muss klar ersichtlich sein
     * "Generiert" = Anfrage erfüllt, "Ausstehend" = noch zu bearbeiten
     */
    it('zeigt korrekten PDF-Status für jede Anfrage', async () => {
      setupHandlers();
      const user = userEvent.setup();

      renderWithProviders(<DsgvoAdminPage />);

      // Wechsle zu Datenexport-Anfragen Tab
      await waitFor(() => {
        expect(screen.getByRole('tab', { name: /Datenexport-Anfragen/i })).toBeInTheDocument();
      });
      await user.click(screen.getByRole('tab', { name: /Datenexport-Anfragen/i }));

      // Prüfe Status-Chips
      await waitFor(() => {
        // Erste Anfrage: pdfGenerated = true
        expect(screen.getByText(/Generiert/)).toBeInTheDocument();
        // Zweite Anfrage: pdfGenerated = false
        expect(screen.getByText('Ausstehend')).toBeInTheDocument();
      });
    });
  });

  describe('DSGVO-Artikel Referenzen', () => {
    /**
     * BUSINESS RULE: DSGVO-Artikel müssen korrekt referenziert werden
     * Art. 15 (Auskunft), Art. 17 (Löschung), Art. 7.3 (Widerruf)
     */
    it('zeigt alle relevanten DSGVO-Artikel auf Übersichtsseite', async () => {
      setupHandlers();

      renderWithProviders(<DsgvoAdminPage />);

      await waitFor(() => {
        // Header zeigt Artikel-Referenzen
        expect(screen.getByText(/Art\. 15, 17, 7\.3 DSGVO/)).toBeInTheDocument();
      });

      // Übersicht Tab zeigt Details
      expect(screen.getByText(/Art\. 17 - Löschrecht/)).toBeInTheDocument();
      expect(screen.getByText(/Art\. 15 - Auskunftsrecht/)).toBeInTheDocument();
    });
  });
});

// =============================================================================
// TESTS: DTO Completeness (EMPFOHLEN laut testing_guide.md)
// =============================================================================

describe('DsgvoAdminPage - DTO Completeness', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  /**
   * WICHTIG: Alle 5 Stats-Felder müssen im UI angezeigt werden
   * Verhindert Bugs wie "Feld fehlt im Response"
   */
  it('zeigt alle 5 Stats-Felder korrekt an', async () => {
    setupHandlers({
      stats: {
        totalDeletions: 101,
        totalDataRequests: 202,
        pendingRequests: 3,
        deletedLeads: 303,
        blockedContacts: 404,
      },
    });

    renderWithProviders(<DsgvoAdminPage />);

    // Warte auf Stats - Werte können in StatCards und Chips erscheinen
    await waitFor(
      () => {
        expect(screen.getAllByText('101').length).toBeGreaterThanOrEqual(1);
      },
      { timeout: 10000 }
    );

    expect(screen.getAllByText('202').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('303').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('404').length).toBeGreaterThanOrEqual(1);
  }, 15000);

  it('zeigt alle Löschprotokoll-Felder in der Tabelle an', async () => {
    setupHandlers();
    const user = userEvent.setup();

    renderWithProviders(<DsgvoAdminPage />);

    // Wechsle zu Löschprotokolle Tab
    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Löschprotokolle/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Löschprotokolle/i }));

    // Prüfe dass alle wichtigen Felder in der Tabelle sind
    await waitFor(() => {
      expect(screen.getByText('admin@freshfoodz.de')).toBeInTheDocument();
    });

    expect(screen.getByText('Art. 17 DSGVO - Löschantrag')).toBeInTheDocument();
    expect(screen.getByText(/Lead #1001/)).toBeInTheDocument();
  });

  it('zeigt alle Datenexport-Felder in der Tabelle an', async () => {
    setupHandlers();
    const user = userEvent.setup();

    renderWithProviders(<DsgvoAdminPage />);

    // Wechsle zu Datenexport-Anfragen Tab
    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Datenexport-Anfragen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Datenexport-Anfragen/i }));

    // Prüfe Felder
    await waitFor(() => {
      expect(screen.getByText('kunde@example.de')).toBeInTheDocument();
    });

    expect(screen.getByText('firma@example.de')).toBeInTheDocument();
    expect(screen.getByText(/Lead #1001/)).toBeInTheDocument();
  });
});

// =============================================================================
// TESTS: Edge Cases (WICHTIG laut testing_guide.md)
// =============================================================================

describe('DsgvoAdminPage - Edge Cases', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('zeigt leeren Zustand für Löschprotokolle', async () => {
    setupHandlers({ deletions: [] });
    const user = userEvent.setup();

    renderWithProviders(<DsgvoAdminPage />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Löschprotokolle/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Löschprotokolle/i }));

    await waitFor(() => {
      expect(screen.getByText('Keine DSGVO-Löschungen vorhanden.')).toBeInTheDocument();
    });
  });

  it('zeigt leeren Zustand für Datenexport-Anfragen', async () => {
    setupHandlers({ requests: [] });
    const user = userEvent.setup();

    renderWithProviders(<DsgvoAdminPage />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Datenexport-Anfragen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Datenexport-Anfragen/i }));

    await waitFor(() => {
      expect(screen.getByText('Keine Datenexport-Anfragen vorhanden.')).toBeInTheDocument();
    });
  });

  it('zeigt leeren Zustand für gelöschte Leads', async () => {
    setupHandlers({ leads: [] });
    const user = userEvent.setup();

    renderWithProviders(<DsgvoAdminPage />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Gelöschte Leads/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Gelöschte Leads/i }));

    await waitFor(() => {
      expect(screen.getByText('Keine DSGVO-gelöschten Leads vorhanden.')).toBeInTheDocument();
    });
  });

  it('behandelt API-Fehler graceful ohne Crash', async () => {
    setupHandlers({ statsError: true });

    // Sollte nicht crashen
    renderWithProviders(<DsgvoAdminPage />);

    // Header sollte trotzdem angezeigt werden
    await waitFor(() => {
      expect(screen.getByText('DSGVO-Verwaltung')).toBeInTheDocument();
    });
  });

  it('zeigt NULL-Werte als Strich an', async () => {
    setupHandlers({
      leads: [
        {
          id: 99,
          companyName: '[GELÖSCHT-099]',
          gdprDeletedAt: null, // NULL-Wert
          gdprDeletedBy: 'auto-cleanup',
          gdprDeletionReason: null, // NULL-Wert
        },
      ],
    });

    const user = userEvent.setup();
    renderWithProviders(<DsgvoAdminPage />);

    await waitFor(() => {
      expect(screen.getByRole('tab', { name: /Gelöschte Leads/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('tab', { name: /Gelöschte Leads/i }));

    // NULL sollte als "—" angezeigt werden
    await waitFor(() => {
      const dashes = screen.getAllByText('—');
      expect(dashes.length).toBeGreaterThanOrEqual(1);
    });
  });
});

// =============================================================================
// TESTS: Tab-basiertes Lazy Loading
// =============================================================================

describe('DsgvoAdminPage - Lazy Loading', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  /**
   * PERFORMANCE: Tabs laden Daten erst bei Aktivierung
   * Verhindert unnötige API-Calls und verbessert Initial Load
   */
  it('lädt Tab-Daten erst bei Klick', async () => {
    let deletionsRequested = false;
    let requestsRequested = false;

    server.use(
      http.get('/api/admin/gdpr/stats', () => HttpResponse.json(COMPLETE_STATS)),
      http.get('/api/admin/gdpr/deletions', () => {
        deletionsRequested = true;
        return HttpResponse.json(DELETION_LOGS);
      }),
      http.get('/api/admin/gdpr/data-requests', () => {
        requestsRequested = true;
        return HttpResponse.json(DATA_REQUESTS);
      }),
      http.get('/api/admin/gdpr/deleted-leads', () => HttpResponse.json(DELETED_LEADS))
    );

    const user = userEvent.setup();
    renderWithProviders(<DsgvoAdminPage />);

    // Nach initialem Render sollten keine Tab-Daten geladen sein
    await waitFor(() => {
      expect(screen.getByText('DSGVO-Verwaltung')).toBeInTheDocument();
    });

    // Kurz warten, dann prüfen dass keine Requests gemacht wurden
    await new Promise(resolve => setTimeout(resolve, 100));
    expect(deletionsRequested).toBe(false);
    expect(requestsRequested).toBe(false);

    // Klicke auf Löschprotokolle Tab
    await user.click(screen.getByRole('tab', { name: /Löschprotokolle/i }));

    await waitFor(() => {
      expect(deletionsRequested).toBe(true);
    });
    expect(requestsRequested).toBe(false);

    // Klicke auf Datenexport Tab
    await user.click(screen.getByRole('tab', { name: /Datenexport-Anfragen/i }));

    await waitFor(() => {
      expect(requestsRequested).toBe(true);
    });
  });
});
