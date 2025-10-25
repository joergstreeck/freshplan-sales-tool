/**
 * ChurnRiskAlert Component Tests
 * Sprint 2.1.7.2 - Customer Dashboard - Churn Risk Detection
 *
 * @description Tests für Churn-Risiko Warnung Component
 * @since 2025-10-23
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import { ChurnRiskAlert } from './ChurnRiskAlert';

/**
 * Helper function to render with Theme Provider
 */
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

describe('ChurnRiskAlert', () => {
  beforeEach(() => {
    // Reset Date.now() mock before each test
    vi.restoreAllMocks();
  });

  describe('Churn Risk Detection', () => {
    it('shows no alert when lastOrderDate is recent (< 90 days)', () => {
      const recentDate = new Date();
      recentDate.setDate(recentDate.getDate() - 30); // 30 days ago

      const { container } = renderWithTheme(
        <ChurnRiskAlert lastOrderDate={recentDate.toISOString()} churnThresholdDays={90} />
      );

      // No alert should be rendered
      expect(container.firstChild).toBeNull();
    });

    it('shows warning alert when lastOrderDate exceeds threshold (≥ 90 days)', () => {
      const oldDate = new Date();
      oldDate.setDate(oldDate.getDate() - 100); // 100 days ago

      renderWithTheme(
        <ChurnRiskAlert lastOrderDate={oldDate.toISOString()} churnThresholdDays={90} />
      );

      expect(screen.getByText(/Churn-Risiko: Inaktiver Kunde/i)).toBeInTheDocument();
      expect(screen.getByText(/Letzte Bestellung:/i)).toBeInTheDocument();
      expect(screen.getByText(/100 Tage/i)).toBeInTheDocument();
      expect(screen.getByText(/Kunde kontaktieren/i)).toBeInTheDocument();
    });

    it('shows error alert when lastOrderDate is null (no orders)', () => {
      renderWithTheme(<ChurnRiskAlert lastOrderDate={null} churnThresholdDays={90} />);

      expect(screen.getByText(/Churn-Risiko: Keine Bestellungen/i)).toBeInTheDocument();
      expect(screen.getByText(/hat noch keine Bestellungen aufgegeben/i)).toBeInTheDocument();
    });
  });

  describe('Custom Threshold', () => {
    it('uses custom churnThresholdDays (14 days)', () => {
      const oldDate = new Date();
      oldDate.setDate(oldDate.getDate() - 20); // 20 days ago

      renderWithTheme(
        <ChurnRiskAlert lastOrderDate={oldDate.toISOString()} churnThresholdDays={14} />
      );

      // 20 days >= 14 days threshold → should show alert
      expect(screen.getByText(/Churn-Risiko: Inaktiver Kunde/i)).toBeInTheDocument();
      // Should show "20 Tage" in parentheses
      expect(screen.getByText(/\(20 Tage\)/i)).toBeInTheDocument();
    });

    it('uses custom churnThresholdDays (365 days)', () => {
      const recentDate = new Date();
      recentDate.setDate(recentDate.getDate() - 200); // 200 days ago

      const { container } = renderWithTheme(
        <ChurnRiskAlert lastOrderDate={recentDate.toISOString()} churnThresholdDays={365} />
      );

      // 200 days < 365 days threshold → no alert
      expect(container.firstChild).toBeNull();
    });

    it('uses default threshold (90 days) when not provided', () => {
      const oldDate = new Date();
      oldDate.setDate(oldDate.getDate() - 95); // 95 days ago

      renderWithTheme(
        <ChurnRiskAlert
          lastOrderDate={oldDate.toISOString()}
          // No churnThresholdDays → should default to 90
        />
      );

      // 95 days > 90 days (default) → should show alert
      expect(screen.getByText(/Churn-Risiko: Inaktiver Kunde/i)).toBeInTheDocument();
      expect(screen.getByText(/95 Tage/i)).toBeInTheDocument();
    });
  });

  describe('Date Formatting', () => {
    it('formats lastOrderDate with German locale (formatDistanceToNow)', () => {
      const oldDate = new Date();
      oldDate.setDate(oldDate.getDate() - 120); // 120 days ago

      renderWithTheme(
        <ChurnRiskAlert lastOrderDate={oldDate.toISOString()} churnThresholdDays={90} />
      );

      // Should show German time ago text (e.g., "vor 4 Monaten")
      expect(screen.getByText(/Letzte Bestellung:/i)).toBeInTheDocument();
      expect(screen.getByText(/vor/i)).toBeInTheDocument(); // German "ago" = "vor"
    });

    it('displays days count in parentheses', () => {
      const oldDate = new Date();
      oldDate.setDate(oldDate.getDate() - 150); // 150 days ago

      renderWithTheme(
        <ChurnRiskAlert lastOrderDate={oldDate.toISOString()} churnThresholdDays={90} />
      );

      // Should show "(150 Tage)"
      expect(screen.getByText(/\(150 Tage\)/i)).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('shows alert when lastOrderDate exactly matches threshold', () => {
      const exactDate = new Date();
      exactDate.setDate(exactDate.getDate() - 90); // Exactly 90 days ago

      renderWithTheme(
        <ChurnRiskAlert lastOrderDate={exactDate.toISOString()} churnThresholdDays={90} />
      );

      // Exactly at threshold (90 days) → SHOW alert (>= threshold)
      // Implementation uses >=, so 90 days = churn risk
      expect(screen.getByText(/Churn-Risiko: Inaktiver Kunde/i)).toBeInTheDocument();
    });

    it('shows alert when lastOrderDate is 1 day over threshold', () => {
      const oneDayOver = new Date();
      oneDayOver.setDate(oneDayOver.getDate() - 91); // 91 days ago

      renderWithTheme(
        <ChurnRiskAlert lastOrderDate={oneDayOver.toISOString()} churnThresholdDays={90} />
      );

      // 91 days > 90 days threshold → should show alert
      expect(screen.getByText(/Churn-Risiko: Inaktiver Kunde/i)).toBeInTheDocument();
    });

    it('handles very old dates (> 1 year)', () => {
      const veryOldDate = new Date();
      veryOldDate.setDate(veryOldDate.getDate() - 500); // 500 days ago

      renderWithTheme(
        <ChurnRiskAlert lastOrderDate={veryOldDate.toISOString()} churnThresholdDays={90} />
      );

      expect(screen.getByText(/Churn-Risiko: Inaktiver Kunde/i)).toBeInTheDocument();
      expect(screen.getByText(/500 Tage/i)).toBeInTheDocument();
    });
  });

  describe('Localization', () => {
    it('uses German labels', () => {
      renderWithTheme(<ChurnRiskAlert lastOrderDate={null} churnThresholdDays={90} />);

      // Check German labels - use more specific text matchers
      expect(screen.getByText(/Churn-Risiko: Keine Bestellungen/i)).toBeInTheDocument();
      expect(
        screen.getByText(/Dieser Kunde hat noch keine Bestellungen aufgegeben/i)
      ).toBeInTheDocument();
    });
  });
});
