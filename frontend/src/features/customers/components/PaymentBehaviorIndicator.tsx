/**
 * PaymentBehaviorIndicator Component
 * Sprint 2.1.7.2 - Customer Dashboard - Zahlungsverhalten-Ampel
 *
 * @description Ampel-System zur Anzeige des Zahlungsverhaltens (EXCELLENT/GOOD/WARNING/CRITICAL)
 * @since 2025-10-23
 */

import React from 'react';
import { Alert, AlertTitle, Typography } from '@mui/material';

interface PaymentBehaviorIndicatorProps {
  /** Payment Behavior Enum (EXCELLENT, GOOD, WARNING, CRITICAL, N_A) */
  behavior: string;
  /** Durchschnittliche Zahlungsdauer in Tagen */
  averageDaysToPay: number | null;
}

/**
 * Payment Behavior Indicator (Ampel-System)
 *
 * Zeigt das Zahlungsverhalten als farbige Alert-Box an:
 * - EXCELLENT (≤7 Tage): Grün
 * - GOOD (8-14 Tage): Blau
 * - WARNING (15-30 Tage): Orange
 * - CRITICAL (>30 Tage): Rot
 * - N_A: Grau
 *
 * Verwendet MUI Theme System (FreshFoodz Design System konform).
 *
 * @example
 * ```tsx
 * <PaymentBehaviorIndicator
 *   behavior="GOOD"
 *   averageDaysToPay={12}
 * />
 * ```
 */
export const PaymentBehaviorIndicator: React.FC<PaymentBehaviorIndicatorProps> = ({
  behavior,
  averageDaysToPay,
}) => {
  const getColor = (): 'success' | 'info' | 'warning' | 'error' => {
    switch (behavior) {
      case 'EXCELLENT':
        return 'success';
      case 'GOOD':
        return 'info';
      case 'WARNING':
        return 'warning';
      case 'CRITICAL':
        return 'error';
      default:
        return 'info';
    }
  };

  const getLabel = (): string => {
    switch (behavior) {
      case 'EXCELLENT':
        return 'Ausgezeichnet';
      case 'GOOD':
        return 'Gut';
      case 'WARNING':
        return 'Verzögert';
      case 'CRITICAL':
        return 'Kritisch';
      case 'N_A':
        return 'Keine Daten';
      default:
        return 'Unbekannt';
    }
  };

  // N_A: Keine Daten → Info-Box
  if (behavior === 'N_A' || averageDaysToPay === null) {
    return (
      <Alert severity="info">
        <AlertTitle>Zahlungsverhalten: Keine Daten</AlertTitle>
        <Typography variant="body2">
          Es liegen noch keine Zahlungsdaten vor.
        </Typography>
      </Alert>
    );
  }

  return (
    <Alert severity={getColor()}>
      <AlertTitle>Zahlungsverhalten: {getLabel()}</AlertTitle>
      <Typography variant="body2">
        Durchschnittliche Zahlungsdauer: {averageDaysToPay.toFixed(0)} Tage
      </Typography>
    </Alert>
  );
};

export default PaymentBehaviorIndicator;
