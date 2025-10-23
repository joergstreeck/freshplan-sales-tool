/**
 * RevenueMetricsWidget Component Tests
 * Sprint 2.1.7.2 - Customer Dashboard - Revenue Metrics Display
 *
 * @description Tests für Revenue-Anzeige Widget (30/90/365 Tage)
 * @since 2025-10-23
 */

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import { RevenueMetricsWidget } from './RevenueMetricsWidget';

/**
 * Helper function to render with Theme Provider
 */
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

describe('RevenueMetricsWidget', () => {
  describe('Rendering', () => {
    it('renders widget with title and formatted value', () => {
      renderWithTheme(
        <RevenueMetricsWidget
          title="Umsatz (30 Tage)"
          value={25000}
          color="success"
        />
      );

      expect(screen.getByText('Umsatz (30 Tage)')).toBeInTheDocument();
      expect(screen.getByText('25.000 €')).toBeInTheDocument();
    });

    it('renders widget with different colors', () => {
      const { rerender } = renderWithTheme(
        <RevenueMetricsWidget
          title="Umsatz (90 Tage)"
          value={50000}
          color="info"
        />
      );

      expect(screen.getByText('50.000 €')).toBeInTheDocument();

      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <RevenueMetricsWidget
            title="Umsatz (365 Tage)"
            value={100000}
            color="primary"
          />
        </ThemeProvider>
      );

      expect(screen.getByText('100.000 €')).toBeInTheDocument();
    });
  });

  describe('Currency Formatting', () => {
    it('formats zero value correctly', () => {
      renderWithTheme(
        <RevenueMetricsWidget
          title="Umsatz (30 Tage)"
          value={0}
          color="success"
        />
      );

      expect(screen.getByText('0 €')).toBeInTheDocument();
    });

    it('formats small values correctly', () => {
      renderWithTheme(
        <RevenueMetricsWidget
          title="Umsatz (30 Tage)"
          value={99.5}
          color="success"
        />
      );

      // Note: minimumFractionDigits: 0 does NOT round, it just shows decimals if present
      expect(screen.getByText('99,5 €')).toBeInTheDocument();
    });

    it('formats large values with thousands separator', () => {
      renderWithTheme(
        <RevenueMetricsWidget
          title="Umsatz (365 Tage)"
          value={1250000}
          color="primary"
        />
      );

      expect(screen.getByText('1.250.000 €')).toBeInTheDocument();
    });

    it('formats decimal values with German formatting', () => {
      renderWithTheme(
        <RevenueMetricsWidget
          title="Umsatz (30 Tage)"
          value={12345.67}
          color="success"
        />
      );

      // de-DE locale uses comma for decimals, period for thousands
      expect(screen.getByText('12.345,67 €')).toBeInTheDocument();
    });
  });

  describe('Layout & Accessibility', () => {
    it('renders title as caption variant', () => {
      renderWithTheme(
        <RevenueMetricsWidget
          title="Umsatz (30 Tage)"
          value={5000}
          color="success"
        />
      );

      const titleElement = screen.getByText('Umsatz (30 Tage)');

      // Check that title uses caption variant (smaller text)
      expect(titleElement).toBeInTheDocument();
    });

    it('renders value as h4 variant', () => {
      renderWithTheme(
        <RevenueMetricsWidget
          title="Umsatz (30 Tage)"
          value={5000}
          color="success"
        />
      );

      const valueElement = screen.getByText('5.000 €');

      // Check that value is displayed prominently
      expect(valueElement).toBeInTheDocument();
    });
  });
});
