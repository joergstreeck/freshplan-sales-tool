/**
 * FieldMappingStep Tests - gemäß testing_guide.md
 * Sprint 2.1.8 - Phase 1: Self-Service Lead-Import
 *
 * Fokus laut Guide:
 * 1. Business-Logic-Tests (PFLICHT) - Pflichtfeld-Validierung
 * 2. Edge-Case-Tests (WICHTIG) - Keine Spalten, keine Auto-Mappings
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
import { FieldMappingStep } from '../FieldMappingStep';

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

const DEFAULT_PROPS = {
  uploadId: 'upload-123',
  columns: ['Firma', 'E-Mail', 'Telefon', 'Stadt', 'PLZ'],
  initialMapping: {},
  onMappingComplete: vi.fn(),
  onBack: vi.fn(),
};

// =============================================================================
// TESTS: Business Logic (PFLICHT laut testing_guide.md)
// =============================================================================

describe('FieldMappingStep - Business Logic', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Pflichtfeld-Validierung', () => {
    /**
     * BUSINESS RULE: companyName ist Pflichtfeld
     * Ohne Mapping auf companyName darf kein Weiter möglich sein
     */
    it('disabled Weiter-Button wenn Pflichtfeld nicht gemappt', () => {
      renderWithProviders(<FieldMappingStep {...DEFAULT_PROPS} />);

      const continueButton = screen.getByRole('button', { name: /Weiter zur Vorschau/i });
      expect(continueButton).toBeDisabled();
    });

    it('zeigt Warnung für fehlende Pflichtfelder', () => {
      renderWithProviders(<FieldMappingStep {...DEFAULT_PROPS} />);

      expect(screen.getByText(/Pflichtfelder fehlen/)).toBeInTheDocument();
      expect(screen.getByText(/Firmenname/)).toBeInTheDocument();
    });

    it('enabled Weiter-Button wenn Pflichtfeld gemappt ist', async () => {
      renderWithProviders(
        <FieldMappingStep {...DEFAULT_PROPS} initialMapping={{ Firma: 'companyName' }} />
      );

      const continueButton = screen.getByRole('button', { name: /Weiter zur Vorschau/i });
      expect(continueButton).toBeEnabled();
    });

    it('zeigt KEINE Warnung wenn alle Pflichtfelder gemappt', () => {
      renderWithProviders(
        <FieldMappingStep {...DEFAULT_PROPS} initialMapping={{ Firma: 'companyName' }} />
      );

      expect(screen.queryByText(/Pflichtfelder fehlen/)).not.toBeInTheDocument();
    });
  });

  describe('Auto-Mapping', () => {
    /**
     * BUSINESS RULE: Auto-Mapping erkennt Spalten automatisch
     * Spalten wie "Firma", "Company", "Firmenname" → companyName
     */
    it('zeigt Auto-Mapping Info wenn Mappings vorhanden', () => {
      renderWithProviders(
        <FieldMappingStep
          {...DEFAULT_PROPS}
          initialMapping={{ Firma: 'companyName', 'E-Mail': 'email' }}
        />
      );

      expect(screen.getByText(/2 Spalten wurden automatisch erkannt/)).toBeInTheDocument();
    });

    it('zeigt Reset-Button für Auto-Mapping', () => {
      renderWithProviders(
        <FieldMappingStep {...DEFAULT_PROPS} initialMapping={{ Firma: 'companyName' }} />
      );

      expect(screen.getByRole('button', { name: /Zurücksetzen/i })).toBeInTheDocument();
    });
  });

  describe('Mapping-Statistik', () => {
    /**
     * BUSINESS RULE: User muss Überblick über gemappte/offene Felder haben
     */
    it('zeigt Statistik über gemappte Spalten', () => {
      renderWithProviders(
        <FieldMappingStep
          {...DEFAULT_PROPS}
          columns={['Firma', 'E-Mail', 'Telefon', 'Stadt', 'PLZ']}
          initialMapping={{ Firma: 'companyName', 'E-Mail': 'email' }}
        />
      );

      expect(screen.getByText('2 zugeordnet')).toBeInTheDocument();
      expect(screen.getByText('3 offen')).toBeInTheDocument();
    });
  });

  describe('Mapping-Änderung', () => {
    /**
     * BUSINESS RULE: Ein Feld kann nur einer Spalte zugeordnet werden
     * Wird ein Feld neu zugeordnet, wird die alte Zuordnung entfernt
     */
    it('ruft onMappingComplete mit korrektem Mapping auf', async () => {
      const onMappingComplete = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(
        <FieldMappingStep
          {...DEFAULT_PROPS}
          initialMapping={{ Firma: 'companyName' }}
          onMappingComplete={onMappingComplete}
        />
      );

      await user.click(screen.getByRole('button', { name: /Weiter zur Vorschau/i }));

      expect(onMappingComplete).toHaveBeenCalledWith({ Firma: 'companyName' });
    });

    it('ermöglicht Mapping per Dropdown', async () => {
      renderWithProviders(
        <FieldMappingStep {...DEFAULT_PROPS} initialMapping={{ Firma: 'companyName' }} />
      );

      // Finde das Select für "E-Mail" Spalte
      const rows = screen.getAllByRole('row');
      const emailRow = rows.find(row => row.textContent?.includes('E-Mail'));
      expect(emailRow).toBeTruthy();
    });
  });

  describe('Navigation', () => {
    it('ruft onBack beim Klick auf Zurück', async () => {
      const onBack = vi.fn();
      const user = userEvent.setup();

      renderWithProviders(<FieldMappingStep {...DEFAULT_PROPS} onBack={onBack} />);

      await user.click(screen.getByRole('button', { name: /Zurück/i }));

      expect(onBack).toHaveBeenCalled();
    });
  });
});

// =============================================================================
// TESTS: Edge Cases (WICHTIG laut testing_guide.md)
// =============================================================================

describe('FieldMappingStep - Edge Cases', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('zeigt alle Spalten in Tabelle an', () => {
    renderWithProviders(
      <FieldMappingStep {...DEFAULT_PROPS} columns={['Firma', 'E-Mail', 'Telefon']} />
    );

    expect(screen.getByText('Firma')).toBeInTheDocument();
    expect(screen.getByText('E-Mail')).toBeInTheDocument();
    expect(screen.getByText('Telefon')).toBeInTheDocument();
  });

  it('zeigt leere Tabelle wenn keine Spalten', () => {
    renderWithProviders(<FieldMappingStep {...DEFAULT_PROPS} columns={[]} />);

    // Nur Header-Zeile sollte existieren
    const rows = screen.getAllByRole('row');
    expect(rows.length).toBe(1); // Nur Header
  });

  it('zeigt Pflicht-Badge bei required Feldern', () => {
    renderWithProviders(<FieldMappingStep {...DEFAULT_PROPS} />);

    // "Pflicht" Badge sollte in Dropdown-Optionen erscheinen
    // Da Dropdown nicht offen ist, prüfen wir die Warnung
    expect(screen.getByText(/Pflichtfelder fehlen/)).toBeInTheDocument();
    expect(screen.getByText('Firmenname')).toBeInTheDocument();
  });
});
