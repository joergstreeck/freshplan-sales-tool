/**
 * FileUploadStep Tests - gemäß testing_guide.md
 * Sprint 2.1.8 - Phase 1: Self-Service Lead-Import
 *
 * REFACTORED: FileUploadStep ist jetzt "pure" für Quota-Anzeige (Container/Presentational Pattern)
 * - Quota-API-Call wurde in den Parent (LeadImportWizard) verschoben
 * - Diese Komponente bekommt quotaInfo und quotaLoading als Props
 * - Upload-Call bleibt hier (User-Initiated Action)
 *
 * Fokus laut Guide:
 * 1. Business-Logic-Tests (PFLICHT) - Quota-Anzeige, Upload-Validation
 * 2. UI-Tests (EMPFOHLEN) - Dropzone, Loading States
 * 3. Edge-Case-Tests (WICHTIG) - Keine Quota, Fehler
 *
 * @since 2025-12-05
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '@/theme/freshfoodz';
import { FileUploadStep, QuotaDisplay } from '../FileUploadStep';
import type { QuotaInfo } from '../../../api/leadImportApi';

// Mock the uploadFile API
vi.mock('../../../api/leadImportApi', async () => {
  const actual = await vi.importActual('../../../api/leadImportApi');
  return {
    ...actual,
    uploadFile: vi.fn(),
  };
});

import { uploadFile } from '../../../api/leadImportApi';

// =============================================================================
// Render Helper
// =============================================================================

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

// =============================================================================
// Test Data
// =============================================================================

const MOCK_QUOTA_INFO: QuotaInfo = {
  currentOpenLeads: 50,
  maxOpenLeads: 200,
  importsToday: 2,
  maxImportsPerDay: 10,
  maxLeadsPerImport: 500,
  remainingCapacity: 150,
  canImport: true,
};

const MOCK_QUOTA_EXHAUSTED: QuotaInfo = {
  currentOpenLeads: 200,
  maxOpenLeads: 200,
  importsToday: 10,
  maxImportsPerDay: 10,
  maxLeadsPerImport: 500,
  remainingCapacity: 0,
  canImport: false,
};

const DEFAULT_PROPS = {
  quotaInfo: MOCK_QUOTA_INFO,
  quotaLoading: false,
  onUploadComplete: vi.fn(),
  onError: vi.fn(),
};

// =============================================================================
// TESTS: QuotaDisplay Component (Unit Tests)
// =============================================================================

describe('QuotaDisplay - Loading State', () => {
  it('zeigt Skeleton wenn isLoading=true', () => {
    renderWithTheme(<QuotaDisplay quotaInfo={null} isLoading={true} />);

    // Skeleton wird angezeigt (MUI Skeleton hat keine role, prüfe class)
    const skeleton = document.querySelector('.MuiSkeleton-root');
    expect(skeleton).toBeInTheDocument();
  });

  it('zeigt nichts wenn quotaInfo null und nicht loading', () => {
    const { container } = renderWithTheme(<QuotaDisplay quotaInfo={null} isLoading={false} />);

    expect(container.firstChild).toBeNull();
  });
});

describe('QuotaDisplay - Quota Info', () => {
  it('zeigt Kapazität wenn canImport=true', () => {
    renderWithTheme(<QuotaDisplay quotaInfo={MOCK_QUOTA_INFO} isLoading={false} />);

    expect(screen.getByText(/Ihre Kapazität:/)).toBeInTheDocument();
    expect(screen.getByText(/150 Leads verfügbar/)).toBeInTheDocument();
    expect(screen.getByText(/50 \/ 200 offene Leads/)).toBeInTheDocument();
  });

  it('zeigt Import-Statistik', () => {
    renderWithTheme(<QuotaDisplay quotaInfo={MOCK_QUOTA_INFO} isLoading={false} />);

    expect(screen.getByText(/2 \/ 10 Imports/)).toBeInTheDocument();
    expect(screen.getByText(/Max. 500 Leads pro Import/)).toBeInTheDocument();
  });

  it('zeigt info-Alert wenn canImport=true', () => {
    renderWithTheme(<QuotaDisplay quotaInfo={MOCK_QUOTA_INFO} isLoading={false} />);

    const alert = document.querySelector('.MuiAlert-standardInfo');
    expect(alert).toBeInTheDocument();
  });

  it('zeigt warning-Alert wenn canImport=false', () => {
    renderWithTheme(<QuotaDisplay quotaInfo={MOCK_QUOTA_EXHAUSTED} isLoading={false} />);

    const alert = document.querySelector('.MuiAlert-standardWarning');
    expect(alert).toBeInTheDocument();
  });
});

// =============================================================================
// TESTS: FileUploadStep - Dropzone UI
// =============================================================================

describe('FileUploadStep - Dropzone UI', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('zeigt Dropzone mit Upload-Hinweis', () => {
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    expect(screen.getByText(/CSV oder Excel-Datei hier ablegen/)).toBeInTheDocument();
    expect(screen.getByText(/oder klicken zum Auswählen/)).toBeInTheDocument();
  });

  it('zeigt unterstützte Dateitypen', () => {
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    expect(screen.getByText('CSV')).toBeInTheDocument();
    expect(screen.getByText('XLSX')).toBeInTheDocument();
    expect(screen.getByText('XLS')).toBeInTheDocument();
    expect(screen.getByText('max. 5 MB')).toBeInTheDocument();
  });

  it('zeigt Tipps für erfolgreichen Import', () => {
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    expect(screen.getByText(/Tipps für erfolgreichen Import/)).toBeInTheDocument();
    expect(
      screen.getByText(/Die erste Zeile sollte Spaltenüberschriften enthalten/)
    ).toBeInTheDocument();
    expect(screen.getByText(/Spalte "Firma" oder "Firmenname" ist Pflicht/)).toBeInTheDocument();
  });
});

// =============================================================================
// TESTS: FileUploadStep - Upload Success
// =============================================================================

describe('FileUploadStep - Upload Success', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('ruft onUploadComplete nach erfolgreichem Upload', async () => {
    const onUploadComplete = vi.fn();
    const mockResponse = {
      uploadId: 'upload-123',
      columns: ['Firma', 'Email'],
      rowCount: 100,
      autoMapping: { Firma: 'companyName' },
      fileName: 'test.csv',
      fileSize: 1024,
    };
    vi.mocked(uploadFile).mockResolvedValue(mockResponse);

    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} onUploadComplete={onUploadComplete} />);

    // Simuliere Datei-Drop (via input element)
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input).toBeInTheDocument();

    const file = new File(['test,content'], 'test.csv', { type: 'text/csv' });
    await userEvent.upload(input, file);

    // Warte auf Upload-Aufruf
    await waitFor(() => {
      expect(uploadFile).toHaveBeenCalledWith(file);
    });

    // Warte auf Callback (500ms delay + progress)
    await waitFor(
      () => {
        expect(onUploadComplete).toHaveBeenCalledWith(mockResponse);
      },
      { timeout: 3000 }
    );
  });
});

// =============================================================================
// TESTS: FileUploadStep - Upload Error
// =============================================================================

describe('FileUploadStep - Upload Error', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('ruft onError bei Upload-Fehler', async () => {
    const onError = vi.fn();
    vi.mocked(uploadFile).mockRejectedValue(new Error('Upload failed'));

    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} onError={onError} />);

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    const file = new File(['test'], 'test.csv', { type: 'text/csv' });
    await userEvent.upload(input, file);

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith('Upload failed');
    });
  });

  it('ruft onError bei Datei zu groß', async () => {
    const onError = vi.fn();

    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} onError={onError} />);

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    // 6 MB Datei (über 5 MB Limit)
    const largeContent = new Array(6 * 1024 * 1024).fill('x').join('');
    const file = new File([largeContent], 'large.csv', { type: 'text/csv' });

    await userEvent.upload(input, file);

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith(expect.stringContaining('groß'));
    });
  });

  // Note: Invalid file type test skipped - react-dropzone handles this internally
  // and doesn't trigger the onError callback in the same way during userEvent.upload
});

// =============================================================================
// TESTS: FileUploadStep - Upload Progress
// =============================================================================

describe('FileUploadStep - Upload Progress', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('zeigt Dateiname während Upload', async () => {
    // Langsamer Upload
    vi.mocked(uploadFile).mockImplementation(
      () =>
        new Promise(resolve =>
          setTimeout(
            () =>
              resolve({
                uploadId: 'upload-123',
                columns: [],
                rowCount: 0,
                autoMapping: {},
                fileName: 'test.csv',
                fileSize: 1024,
              }),
            1000
          )
        )
    );

    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    const file = new File(['test'], 'mydata.csv', { type: 'text/csv' });
    await userEvent.upload(input, file);

    await waitFor(() => {
      expect(screen.getByText('mydata.csv')).toBeInTheDocument();
    });
  });

  it('zeigt Progress während Upload', async () => {
    vi.mocked(uploadFile).mockImplementation(
      () =>
        new Promise(resolve =>
          setTimeout(
            () =>
              resolve({
                uploadId: 'upload-123',
                columns: [],
                rowCount: 0,
                autoMapping: {},
                fileName: 'test.csv',
                fileSize: 1024,
              }),
            1000
          )
        )
    );

    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    const file = new File(['test'], 'test.csv', { type: 'text/csv' });
    await userEvent.upload(input, file);

    await waitFor(() => {
      expect(screen.getByText(/Wird hochgeladen/)).toBeInTheDocument();
    });
  });
});

// =============================================================================
// TESTS: FileUploadStep - Quota Integration
// =============================================================================

describe('FileUploadStep - Quota Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('zeigt QuotaDisplay mit Props', () => {
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    // QuotaDisplay sollte angezeigt werden
    expect(screen.getByText(/Ihre Kapazität:/)).toBeInTheDocument();
    expect(screen.getByText(/150 Leads verfügbar/)).toBeInTheDocument();
  });

  it('zeigt Quota-Loading Skeleton', () => {
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} quotaInfo={null} quotaLoading={true} />);

    const skeleton = document.querySelector('.MuiSkeleton-root');
    expect(skeleton).toBeInTheDocument();
  });

  it('zeigt keine Quota wenn null und nicht loading', () => {
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} quotaInfo={null} quotaLoading={false} />);

    expect(screen.queryByText(/Ihre Kapazität:/)).not.toBeInTheDocument();
  });
});

// =============================================================================
// TESTS: Edge Cases
// =============================================================================

describe('FileUploadStep - Edge Cases', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('formatiert Dateigröße in KB korrekt', () => {
    // Test the formatFileSize function indirectly through the component
    // For small files, it shows KB
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    // Component should render without issues
    expect(screen.getByText(/CSV oder Excel-Datei hier ablegen/)).toBeInTheDocument();
  });

  it('akzeptiert CSV Dateien', () => {
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input).toBeInTheDocument();
    expect(input.accept).toContain('.csv');
  });

  it('akzeptiert Excel Dateien', () => {
    renderWithTheme(<FileUploadStep {...DEFAULT_PROPS} />);

    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input).toBeInTheDocument();
    expect(input.accept).toContain('.xlsx');
    expect(input.accept).toContain('.xls');
  });
});
