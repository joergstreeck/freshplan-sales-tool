/**
 * PreviewStep Tests - gemäß testing_guide.md
 * Sprint 2.1.8 - Phase 1: Self-Service Lead-Import
 *
 * REFACTORED: PreviewStep ist jetzt "pure" (Container/Presentational Pattern)
 * - Keine API-Calls mehr in der Komponente
 * - Alle Daten kommen als Props
 * - Dadurch einfach testbar ohne MSW/Mocking
 *
 * Fokus laut Guide:
 * 1. Business-Logic-Tests (PFLICHT) - Quota-Check, Validierung, Duplikate
 * 2. DTO-Completeness-Tests (EMPFOHLEN) - Alle Felder werden angezeigt
 * 3. Edge-Case-Tests (WICHTIG) - Keine Daten, Loading State
 *
 * @since 2025-12-05
 */

import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '@/theme/freshfoodz';
import { PreviewStep, PreviewStepLoading } from '../PreviewStep';
import type { ImportPreviewResponse } from '../../../api/leadImportApi';

// =============================================================================
// Render Helper
// =============================================================================

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

// =============================================================================
// Test Data
// =============================================================================

const MOCK_PREVIEW_DATA: ImportPreviewResponse = {
  uploadId: 'upload-123',
  validation: {
    totalRows: 100,
    validRows: 85,
    errorRows: 5,
    duplicateRows: 10,
  },
  previewRows: [
    { row: 1, status: 'VALID', data: { companyName: 'Test GmbH', email: 'test@example.com' } },
    { row: 2, status: 'DUPLICATE', data: { companyName: 'Dup GmbH', email: 'dup@example.com' } },
    { row: 3, status: 'ERROR', data: { companyName: '', email: 'invalid' } },
  ],
  errors: [
    { row: 3, column: 'companyName', message: 'Pflichtfeld fehlt', value: '' },
    { row: 3, column: 'email', message: 'Ungültiges E-Mail-Format', value: 'invalid' },
  ],
  duplicates: [
    {
      row: 2,
      existingLeadId: 123,
      existingCompanyName: 'Existing GmbH',
      type: 'HARD_COLLISION',
      similarity: 1.0,
    },
    {
      row: 5,
      existingLeadId: 456,
      existingCompanyName: 'Similar AG',
      type: 'SOFT_COLLISION',
      similarity: 0.85,
    },
  ],
  quotaCheck: {
    approved: true,
    message: '',
    currentOpenLeads: 50,
    maxOpenLeads: 200,
    remainingCapacity: 150,
  },
};

const DEFAULT_PROPS = {
  previewData: MOCK_PREVIEW_DATA,
  isLoading: false,
  mapping: { Firma: 'companyName', 'E-Mail': 'email' },
  onContinue: vi.fn(),
  onBack: vi.fn(),
};

// =============================================================================
// TESTS: Loading State
// =============================================================================

describe('PreviewStep - Loading State', () => {
  it('zeigt Lade-Indikator wenn isLoading=true', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} isLoading={true} previewData={null} />);

    expect(screen.getByText(/Daten werden validiert/)).toBeInTheDocument();
  });

  it('zeigt Loading-Komponente separat', () => {
    renderWithTheme(<PreviewStepLoading />);

    expect(screen.getByText(/Daten werden validiert/)).toBeInTheDocument();
  });
});

// =============================================================================
// TESTS: Quota Check (PFLICHT laut testing_guide.md)
// =============================================================================

describe('PreviewStep - Quota Check', () => {
  /**
   * BUSINESS RULE: Quota-Check muss User informieren ob Import möglich
   */
  it('zeigt Quota OK wenn approved=true', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText(/Quota OK/)).toBeInTheDocument();
    expect(screen.getByText(/150 Leads verfügbar/)).toBeInTheDocument();
  });

  it('zeigt Quota-Warnung wenn approved=false', () => {
    const dataWithQuotaError = {
      ...MOCK_PREVIEW_DATA,
      quotaCheck: {
        approved: false,
        message: 'Lead-Kontingent erschöpft. Maximal 200 offene Leads erlaubt.',
        currentOpenLeads: 200,
        maxOpenLeads: 200,
        remainingCapacity: 0,
      },
    };

    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} previewData={dataWithQuotaError} />);

    expect(screen.getByText(/Lead-Kontingent erschöpft/)).toBeInTheDocument();
  });

  it('disabled Weiter-Button wenn Quota nicht approved', () => {
    const dataWithQuotaError = {
      ...MOCK_PREVIEW_DATA,
      quotaCheck: {
        ...MOCK_PREVIEW_DATA.quotaCheck,
        approved: false,
      },
    };

    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} previewData={dataWithQuotaError} />);

    expect(screen.getByRole('button', { name: /Import nicht möglich/ })).toBeDisabled();
  });
});

// =============================================================================
// TESTS: Validation Summary (PFLICHT laut testing_guide.md)
// =============================================================================

describe('PreviewStep - Validation Summary', () => {
  /**
   * BUSINESS RULE: User muss Überblick über Validierung bekommen
   */
  it('zeigt Validierungsergebnis Überschrift', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('Validierungsergebnis')).toBeInTheDocument();
  });

  it('zeigt alle Statistik-Karten', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    // Labels in StatCards - einige Werte erscheinen mehrfach
    expect(screen.getByText('Gesamt')).toBeInTheDocument();
    expect(screen.getAllByText('100').length).toBeGreaterThanOrEqual(1);

    expect(screen.getAllByText('Gültig').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('85').length).toBeGreaterThanOrEqual(1);

    expect(screen.getByText('Duplikate')).toBeInTheDocument();
    expect(screen.getAllByText('10').length).toBeGreaterThanOrEqual(1);

    expect(screen.getAllByText('Fehler').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('5').length).toBeGreaterThanOrEqual(1);
  });
});

// =============================================================================
// TESTS: Error Display (PFLICHT laut testing_guide.md)
// =============================================================================

describe('PreviewStep - Error Display', () => {
  /**
   * BUSINESS RULE: Fehler müssen mit Details angezeigt werden
   */
  it('zeigt Fehler-Accordion wenn Fehler vorhanden', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('2 Fehler gefunden')).toBeInTheDocument();
  });

  it('zeigt Fehler-Details in Tabelle', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('Pflichtfeld fehlt')).toBeInTheDocument();
    expect(screen.getByText('Ungültiges E-Mail-Format')).toBeInTheDocument();
  });

  it('zeigt KEINE Fehler-Accordion wenn keine Fehler', () => {
    const dataWithoutErrors = {
      ...MOCK_PREVIEW_DATA,
      errors: [],
      validation: { ...MOCK_PREVIEW_DATA.validation, errorRows: 0 },
    };

    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} previewData={dataWithoutErrors} />);

    expect(screen.queryByText(/Fehler gefunden/)).not.toBeInTheDocument();
  });
});

// =============================================================================
// TESTS: Duplicate Display (PFLICHT laut testing_guide.md)
// =============================================================================

describe('PreviewStep - Duplicate Display', () => {
  /**
   * BUSINESS RULE: Duplikate werden mit Typ (HARD/SOFT) angezeigt
   */
  it('zeigt Duplikate-Accordion wenn Duplikate vorhanden', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('2 Duplikate gefunden')).toBeInTheDocument();
  });

  it('zeigt HARD_COLLISION als "Exakt"', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('Exakt')).toBeInTheDocument();
  });

  it('zeigt SOFT_COLLISION als "Ähnlich"', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('Ähnlich')).toBeInTheDocument();
  });

  it('zeigt Ähnlichkeit in Prozent', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('100%')).toBeInTheDocument();
    expect(screen.getByText('85%')).toBeInTheDocument();
  });

  it('zeigt KEINE Duplikate-Accordion wenn keine Duplikate', () => {
    const dataWithoutDuplicates = {
      ...MOCK_PREVIEW_DATA,
      duplicates: [],
      validation: { ...MOCK_PREVIEW_DATA.validation, duplicateRows: 0 },
    };

    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} previewData={dataWithoutDuplicates} />);

    expect(screen.queryByText(/Duplikate gefunden/)).not.toBeInTheDocument();
  });
});

// =============================================================================
// TESTS: Preview Table (EMPFOHLEN laut testing_guide.md)
// =============================================================================

describe('PreviewStep - Preview Table', () => {
  it('zeigt Datenvorschau Accordion', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('Datenvorschau (erste 5 Zeilen)')).toBeInTheDocument();
  });

  it('zeigt Status-Chips korrekt', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} />);

    // "Gültig" erscheint auch als StatCard Label, also getAllByText
    expect(screen.getAllByText('Gültig').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Duplikat')).toBeInTheDocument();
  });
});

// =============================================================================
// TESTS: Navigation (WICHTIG laut testing_guide.md)
// =============================================================================

describe('PreviewStep - Navigation', () => {
  it('ruft onBack beim Klick auf Zurück', async () => {
    const onBack = vi.fn();
    const user = userEvent.setup();

    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} onBack={onBack} />);

    await user.click(screen.getByRole('button', { name: /Zurück/i }));

    expect(onBack).toHaveBeenCalled();
  });

  it('ruft onContinue beim Klick auf Weiter', async () => {
    const onContinue = vi.fn();
    const user = userEvent.setup();

    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} onContinue={onContinue} />);

    await user.click(screen.getByRole('button', { name: /Weiter zum Import/i }));

    expect(onContinue).toHaveBeenCalled();
  });

  it('disabled Weiter-Button wenn keine gültigen Zeilen', () => {
    const dataWithNoValidRows = {
      ...MOCK_PREVIEW_DATA,
      validation: { ...MOCK_PREVIEW_DATA.validation, validRows: 0 },
    };

    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} previewData={dataWithNoValidRows} />);

    expect(screen.getByRole('button', { name: /Import nicht möglich/ })).toBeDisabled();
  });
});

// =============================================================================
// TESTS: Edge Cases (WICHTIG laut testing_guide.md)
// =============================================================================

describe('PreviewStep - Edge Cases', () => {
  it('zeigt Fehler-Alert wenn keine Daten und nicht Loading', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} previewData={null} isLoading={false} />);

    expect(screen.getByText(/Keine Vorschau-Daten verfügbar/)).toBeInTheDocument();
  });

  it('zeigt nicht Fehler-Alert während Loading', () => {
    renderWithTheme(<PreviewStep {...DEFAULT_PROPS} previewData={null} isLoading={true} />);

    expect(screen.queryByText(/Keine Vorschau-Daten verfügbar/)).not.toBeInTheDocument();
  });
});
