/**
 * LeadQualityDashboard Tests
 *
 * Sprint 2.1.6 Phase 4 - Frontend-only mit Mock-Daten
 * Pattern: Analog zu DataHygieneDashboard.test.tsx
 */

import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { LeadQualityDashboard } from '../LeadQualityDashboard';
import { vi } from 'vitest';

// Mock recharts components
vi.mock('recharts', () => ({
  PieChart: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="pie-chart">{children}</div>
  ),
  Pie: () => <div data-testid="pie" />,
  Cell: () => <div data-testid="cell" />,
  ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="responsive-container">{children}</div>
  ),
  Tooltip: () => <div data-testid="tooltip" />,
  Legend: () => <div data-testid="legend" />,
  BarChart: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="bar-chart">{children}</div>
  ),
  Bar: () => <div data-testid="bar" />,
  XAxis: () => <div data-testid="x-axis" />,
  YAxis: () => <div data-testid="y-axis" />,
  CartesianGrid: () => <div data-testid="cartesian-grid" />,
}));

const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('LeadQualityDashboard', () => {
  describe('Rendering', () => {
    it('should render dashboard title', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('Lead-Qualität Übersicht')).toBeInTheDocument();
    });

    it('should render all KPI metric cards', () => {
      renderWithTheme(<LeadQualityDashboard />);

      // KPI Cards
      expect(screen.getByText('Durchschnittlicher Lead-Score')).toBeInTheDocument();
      expect(screen.getByText('Top-Leads')).toBeInTheDocument();
      expect(screen.getAllByText('Vollständig qualifiziert').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('Nachqualifizierung nötig')).toBeInTheDocument();
    });

    it('should display mock data values correctly', () => {
      renderWithTheme(<LeadQualityDashboard />);

      // Mock data from MOCK_LEAD_QUALITY_DATA
      expect(screen.getByText('62/100')).toBeInTheDocument(); // Average score
      expect(screen.getAllByText('5').length).toBeGreaterThanOrEqual(1); // Top leads (appears in multiple places)
      expect(screen.getAllByText('47%').length).toBeGreaterThanOrEqual(1); // Complete qualification (18/38)
      expect(screen.getAllByText('8').length).toBeGreaterThanOrEqual(1); // Minimal qualification
    });
  });

  describe('Charts', () => {
    it('should render lead score distribution pie chart', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('Lead-Score-Verteilung')).toBeInTheDocument();
      expect(screen.getAllByTestId('pie-chart').length).toBeGreaterThanOrEqual(1);
    });

    it('should render qualification level bar chart', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('Qualifizierungs-Level')).toBeInTheDocument();
      expect(screen.getByTestId('bar-chart')).toBeInTheDocument();
    });
  });

  describe('Critical Data Gaps', () => {
    it('should display critical data gaps alert', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('⚠️ Kritische Datenlücken')).toBeInTheDocument();
    });

    it('should show missing contact person gap', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('6 Leads ohne Ansprechpartner')).toBeInTheDocument();
      expect(screen.getByText('Kontaktperson fehlt für Direktansprache')).toBeInTheDocument();
    });

    it('should show missing email gap', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('11 Leads ohne E-Mail-Adresse')).toBeInTheDocument();
      expect(screen.getByText('E-Mail-Marketing nicht möglich')).toBeInTheDocument();
    });

    it('should show missing phone gap', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('4 Leads ohne Telefonnummer')).toBeInTheDocument();
      expect(screen.getByText('Telefonische Kontaktaufnahme nicht möglich')).toBeInTheDocument();
    });

    it('should show missing budget info gap', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('15 Leads ohne Budget-Information')).toBeInTheDocument();
      expect(screen.getByText('Umsatzpotenzial unklar')).toBeInTheDocument();
    });
  });

  describe('Recommendations', () => {
    it('should display recommendations alert', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('💡 Empfehlungen zur Qualitätsverbesserung')).toBeInTheDocument();
    });

    it('should show follow-up recommendation', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('Nachqualifizierung starten')).toBeInTheDocument();
      expect(
        screen.getByText('8 Leads mit Minimal-Daten gezielt nachbearbeiten')
      ).toBeInTheDocument();
    });

    it('should show top leads prioritization recommendation', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('Top-Leads priorisieren')).toBeInTheDocument();
      expect(
        screen.getByText('5 Top-Leads (Score >80) sollten bevorzugt bearbeitet werden')
      ).toBeInTheDocument();
    });

    it('should show data completeness recommendation', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('Datenvollständigkeit erhöhen')).toBeInTheDocument();
      expect(
        screen.getByText(
          'Fehlende Pflichtfelder bei Erstkontakt erfassen (E-Mail, Telefon, Ansprechpartner)'
        )
      ).toBeInTheDocument();
    });
  });

  describe('Qualification Progress', () => {
    it('should display qualification progress section', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getByText('Qualifizierungs-Fortschritt')).toBeInTheDocument();
    });

    it('should show progress bars for all qualification levels', () => {
      renderWithTheme(<LeadQualityDashboard />);

      expect(screen.getAllByText('Vollständig qualifiziert').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('Teilweise qualifiziert')).toBeInTheDocument();
      expect(screen.getByText('Minimal-Daten')).toBeInTheDocument();
    });

    it('should display percentage values', () => {
      renderWithTheme(<LeadQualityDashboard />);

      // Mock data: complete=18/38=47%, partial=12/38=32%, minimal=8/38=21%
      expect(screen.getAllByText('47%').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('32%')).toBeInTheDocument();
      expect(screen.getByText('21%')).toBeInTheDocument();
    });
  });

  describe('FreshFoodz CI Compliance', () => {
    it('should use FreshFoodz brand color for title', () => {
      renderWithTheme(<LeadQualityDashboard />);

      const title = screen.getByText('Lead-Qualität Übersicht');
      expect(title).toHaveStyle({ color: '#004F7B' });
    });
  });

  describe('German Language (DESIGN_SYSTEM.md)', () => {
    it('should use German labels for all UI elements', () => {
      renderWithTheme(<LeadQualityDashboard />);

      // German labels present (avoid false positives from compound words)
      expect(screen.getByText('Lead-Score-Verteilung')).toBeInTheDocument();
      expect(screen.getByText('Qualifizierungs-Level')).toBeInTheDocument();
      expect(screen.getAllByText(/Kritische Datenlücken/).length).toBeGreaterThanOrEqual(1);
      expect(
        screen.getAllByText(/Empfehlungen zur Qualitätsverbesserung/).length
      ).toBeGreaterThanOrEqual(1);
    });
  });
});
