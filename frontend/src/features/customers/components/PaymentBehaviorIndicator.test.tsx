/**
 * PaymentBehaviorIndicator Component Tests
 * Sprint 2.1.7.2 - Customer Dashboard - Zahlungsverhalten-Ampel
 *
 * @description Tests für Zahlungsverhalten-Ampel Component (EXCELLENT/GOOD/WARNING/CRITICAL)
 * @since 2025-10-23
 */

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import { PaymentBehaviorIndicator } from './PaymentBehaviorIndicator';

/**
 * Helper function to render with Theme Provider
 */
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

describe('PaymentBehaviorIndicator', () => {
  describe('Payment Behavior Classification', () => {
    it('renders EXCELLENT (≤7 days) with success alert', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="EXCELLENT"
          averageDaysToPay={7}
        />
      );

      expect(screen.getByText(/Zahlungsverhalten: Ausgezeichnet/i)).toBeInTheDocument();
      expect(screen.getByText(/Durchschnittliche Zahlungsdauer: 7 Tage/i)).toBeInTheDocument();
    });

    it('renders GOOD (8-14 days) with info alert', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="GOOD"
          averageDaysToPay={12}
        />
      );

      expect(screen.getByText(/Zahlungsverhalten: Gut/i)).toBeInTheDocument();
      expect(screen.getByText(/Durchschnittliche Zahlungsdauer: 12 Tage/i)).toBeInTheDocument();
    });

    it('renders WARNING (15-30 days) with warning alert', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="WARNING"
          averageDaysToPay={25}
        />
      );

      expect(screen.getByText(/Zahlungsverhalten: Verzögert/i)).toBeInTheDocument();
      expect(screen.getByText(/Durchschnittliche Zahlungsdauer: 25 Tage/i)).toBeInTheDocument();
    });

    it('renders CRITICAL (>30 days) with error alert', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="CRITICAL"
          averageDaysToPay={45}
        />
      );

      expect(screen.getByText(/Zahlungsverhalten: Kritisch/i)).toBeInTheDocument();
      expect(screen.getByText(/Durchschnittliche Zahlungsdauer: 45 Tage/i)).toBeInTheDocument();
    });

    it('renders N_A (no data) with info alert', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="N_A"
          averageDaysToPay={null}
        />
      );

      expect(screen.getByText(/Zahlungsverhalten: Keine Daten/i)).toBeInTheDocument();
      expect(screen.getByText(/Es liegen noch keine Zahlungsdaten vor./i)).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('renders N_A when averageDaysToPay is null', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="GOOD"
          averageDaysToPay={null}
        />
      );

      expect(screen.getByText(/Zahlungsverhalten: Keine Daten/i)).toBeInTheDocument();
      expect(screen.getByText(/Es liegen noch keine Zahlungsdaten vor./i)).toBeInTheDocument();
    });

    it('rounds averageDaysToPay to integer', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="GOOD"
          averageDaysToPay={13.7}
        />
      );

      // toFixed(0) rounds 13.7 to 14
      expect(screen.getByText(/Durchschnittliche Zahlungsdauer: 14 Tage/i)).toBeInTheDocument();
    });

    it('handles zero days correctly', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="EXCELLENT"
          averageDaysToPay={0}
        />
      );

      expect(screen.getByText(/Durchschnittliche Zahlungsdauer: 0 Tage/i)).toBeInTheDocument();
    });
  });

  describe('Localization', () => {
    it('uses German labels', () => {
      renderWithTheme(
        <PaymentBehaviorIndicator
          behavior="EXCELLENT"
          averageDaysToPay={5}
        />
      );

      // Check German labels
      expect(screen.getByText(/Zahlungsverhalten/i)).toBeInTheDocument();
      expect(screen.getByText(/Durchschnittliche Zahlungsdauer/i)).toBeInTheDocument();
      expect(screen.getByText(/Tage/i)).toBeInTheDocument();
    });
  });
});
