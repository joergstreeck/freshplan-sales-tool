/**
 * LeadImportWizard Tests - gemäß testing_guide.md
 * Sprint 2.1.8 - Phase 1: Self-Service Lead-Import
 *
 * Fokus: Wizard-UI Struktur und statische Elemente
 * Hinweis: Quota-API-Tests sind instabil wegen useEffect-Timing
 *
 * @since 2025-12-05
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import freshfoodzTheme from '@/theme/freshfoodz';
import { LeadImportWizard } from '../LeadImportWizard';

// =============================================================================
// Test Setup
// =============================================================================

vi.mock('@/store/authStore', () => ({
  useAuthStore: () => ({
    userPermissions: ['leads.import'],
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

// Mock the API calls to prevent network requests
vi.mock('../../../api/leadImportApi', () => ({
  getQuotaInfo: vi.fn(() =>
    Promise.resolve({
      currentOpenLeads: 50,
      maxOpenLeads: 200,
      importsToday: 2,
      maxImportsPerDay: 10,
      maxLeadsPerImport: 100,
      remainingCapacity: 150,
      canImport: true,
    })
  ),
  uploadFile: vi.fn(),
  createPreview: vi.fn(),
  executeImport: vi.fn(),
  LEAD_FIELDS: [
    { key: 'companyName', label: 'Firmenname', required: true },
    { key: 'email', label: 'E-Mail', required: false },
  ],
}));

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
// TESTS: Wizard UI Structure
// =============================================================================

describe('LeadImportWizard - UI Structure', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Wizard Rendering', () => {
    it('zeigt Dialog mit 4 Schritten an', () => {
      renderWithProviders(<LeadImportWizard open={true} onClose={vi.fn()} />);

      // Prüfe Stepper
      expect(screen.getByText('Datei hochladen')).toBeInTheDocument();
      expect(screen.getByText('Spalten zuordnen')).toBeInTheDocument();
      expect(screen.getByText('Vorschau')).toBeInTheDocument();
      expect(screen.getByText('Importieren')).toBeInTheDocument();
    });

    it('zeigt Lead-Import Titel', () => {
      renderWithProviders(<LeadImportWizard open={true} onClose={vi.fn()} />);

      expect(screen.getByText('Lead-Import')).toBeInTheDocument();
    });

    it('zeigt schließen-Button', () => {
      renderWithProviders(<LeadImportWizard open={true} onClose={vi.fn()} />);

      expect(screen.getByRole('button', { name: /Schließen/i })).toBeInTheDocument();
    });
  });

  describe('Datei-Upload (Schritt 1)', () => {
    /**
     * BUSINESS RULE: Nur CSV/Excel erlaubt, max. 5 MB
     */
    it('zeigt akzeptierte Dateitypen an', () => {
      renderWithProviders(<LeadImportWizard open={true} onClose={vi.fn()} />);

      expect(screen.getByText('CSV')).toBeInTheDocument();
      expect(screen.getByText('XLSX')).toBeInTheDocument();
      expect(screen.getByText('XLS')).toBeInTheDocument();
      expect(screen.getByText('max. 5 MB')).toBeInTheDocument();
    });

    it('zeigt Tipps für erfolgreichen Import', () => {
      renderWithProviders(<LeadImportWizard open={true} onClose={vi.fn()} />);

      expect(screen.getByText(/Firma.*ist Pflicht/)).toBeInTheDocument();
      expect(screen.getByText(/Duplikate werden automatisch erkannt/)).toBeInTheDocument();
    });

    it('zeigt Dropzone-Text', () => {
      renderWithProviders(<LeadImportWizard open={true} onClose={vi.fn()} />);

      expect(screen.getByText(/CSV oder Excel-Datei hier ablegen/)).toBeInTheDocument();
    });
  });

  describe('Dialog Verhalten', () => {
    it('ruft onClose beim Klick auf Schließen', async () => {
      const onClose = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(<LeadImportWizard open={true} onClose={onClose} />);

      await user.click(screen.getByRole('button', { name: /Schließen/i }));

      expect(onClose).toHaveBeenCalled();
    });

    it('zeigt nichts wenn Dialog geschlossen', () => {
      const { container } = renderWithProviders(
        <LeadImportWizard open={false} onClose={vi.fn()} />
      );

      expect(container.querySelector('[role="dialog"]')).not.toBeInTheDocument();
    });

    it('zeigt Schritt-Information im Footer', () => {
      renderWithProviders(<LeadImportWizard open={true} onClose={vi.fn()} />);

      expect(screen.getByText(/Schritt 1 von 4/)).toBeInTheDocument();
    });

    it('zeigt Format-Hinweis im Footer', () => {
      renderWithProviders(<LeadImportWizard open={true} onClose={vi.fn()} />);

      expect(screen.getByText(/Unterstützte Formate: CSV, XLSX/)).toBeInTheDocument();
    });
  });
});
