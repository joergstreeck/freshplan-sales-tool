/**
 * RevenueMetricsWidget Component
 * Sprint 2.1.7.2 - Customer Dashboard - Revenue Metrics Display
 *
 * @description Widget zur Anzeige von Revenue-Metriken (30/90/365 Tage)
 * @since 2025-10-23
 */

import React from 'react';
import { Paper, Typography } from '@mui/material';

interface RevenueMetricsWidgetProps {
  /** Widget-Titel (z.B. "Umsatz 30 Tage") */
  title: string;
  /** Revenue-Wert in Euro */
  value: number;
  /** MUI Theme Color für den Wert */
  color: 'primary' | 'info' | 'success';
}

/**
 * Revenue Metrics Widget
 *
 * Zeigt einen einzelnen Revenue-Wert in einem zentrierten Paper-Card an.
 * Verwendet MUI Theme System (FreshFoodz Design System konform).
 *
 * @example
 * ```tsx
 * <RevenueMetricsWidget
 *   title="Umsatz 30 Tage"
 *   value={25000}
 *   color="success"
 * />
 * ```
 */
export const RevenueMetricsWidget: React.FC<RevenueMetricsWidgetProps> = ({
  title,
  value,
  color,
}) => {
  return (
    <Paper sx={{ p: 2, textAlign: 'center' }}>
      <Typography variant="caption" color="text.secondary">
        {title}
      </Typography>
      <Typography variant="h4" color={color} sx={{ mt: 1 }}>
        {value.toLocaleString('de-DE', {
          style: 'currency',
          currency: 'EUR',
          minimumFractionDigits: 0,
        })}
      </Typography>
    </Paper>
  );
};

export default RevenueMetricsWidget;
