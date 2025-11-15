/**
 * MetricCard - Sprint 2.1.7.7 Multi-Location Management
 *
 * Reusable metric card component for displaying KPIs and statistics.
 * Design System compliant: MUI Theme colors, no hardcoded values.
 *
 * @module MetricCard
 * @since Sprint 2.1.7.7
 */

import React from 'react';
import { Card, CardContent, Typography, Stack } from '@mui/material';

interface MetricCardProps {
  /** Card title (e.g., "Gesamt-Umsatz") */
  title: string;

  /** Main value (e.g., "125.000,00 €") */
  value: string | number;

  /** Subtitle/additional info (e.g., "12 Standorte") */
  subtitle?: string;

  /** Color from MUI theme palette */
  color?: 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error';
}

/**
 * MetricCard Component
 *
 * Displays a KPI metric with title, value, and optional subtitle.
 * Uses MUI theme colors for consistent styling.
 *
 * @example
 * <MetricCard
 *   title="Gesamt-Umsatz"
 *   value="125.000,00 €"
 *   subtitle="12 Standorte"
 *   color="primary"
 * />
 */
export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  color = 'primary',
}) => {
  return (
    <Card
      sx={{
        height: '100%',
        borderTop: 3,
        borderColor: `${color}.main`,
      }}
    >
      <CardContent>
        <Stack spacing={1}>
          {/* Title */}
          <Typography variant="body2" color="text.secondary">
            {title}
          </Typography>

          {/* Main Value */}
          <Typography variant="h4" component="div" color={`${color}.main`}>
            {value}
          </Typography>

          {/* Subtitle */}
          {subtitle && (
            <Typography variant="caption" color="text.secondary">
              {subtitle}
            </Typography>
          )}
        </Stack>
      </CardContent>
    </Card>
  );
};
