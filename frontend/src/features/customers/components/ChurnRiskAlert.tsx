/**
 * ChurnRiskAlert Component
 * Sprint 2.1.7.2 - Customer Dashboard - Churn Risk Detection
 *
 * @description Warnung bei Kunden ohne kürzliche Bestellungen (Churn-Risiko)
 * @since 2025-10-23
 */

import React from 'react';
import { Alert, AlertTitle, Typography } from '@mui/material';
import { formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';

interface ChurnRiskAlertProps {
  /** Datum der letzten Bestellung (ISO 8601 String oder null) */
  lastOrderDate: string | null;
  /** Churn-Schwelle in Tagen (Default: 90 Tage) */
  churnThresholdDays?: number;
}

/**
 * Churn Risk Alert
 *
 * Zeigt eine Warnung an, wenn der Kunde seit X Tagen keine Bestellung mehr aufgegeben hat.
 * Default-Schwelle: 90 Tage (kann pro Kunde konfiguriert werden in Sprint 2.1.7.2 D4).
 *
 * Verwendet MUI Theme System (FreshFoodz Design System konform).
 *
 * @example
 * ```tsx
 * <ChurnRiskAlert
 *   lastOrderDate="2024-06-01T00:00:00Z"
 *   churnThresholdDays={90}
 * />
 * ```
 */
export const ChurnRiskAlert: React.FC<ChurnRiskAlertProps> = ({
  lastOrderDate,
  churnThresholdDays = 90,
}) => {
  // Keine letzte Bestellung → Churn-Risiko
  if (!lastOrderDate) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        <AlertTitle>Churn-Risiko: Keine Bestellungen</AlertTitle>
        <Typography variant="body2">
          Dieser Kunde hat noch keine Bestellungen aufgegeben.
        </Typography>
      </Alert>
    );
  }

  // Berechne Tage seit letzter Bestellung
  const lastOrderDateObj = new Date(lastOrderDate);
  const now = new Date();
  const daysSinceLastOrder = Math.floor(
    (now.getTime() - lastOrderDateObj.getTime()) / (1000 * 60 * 60 * 24)
  );

  // Kein Churn-Risiko → Keine Warnung
  if (daysSinceLastOrder < churnThresholdDays) {
    return null;
  }

  // Churn-Risiko: Schwelle überschritten
  const timeAgo = formatDistanceToNow(lastOrderDateObj, {
    addSuffix: true,
    locale: de,
  });

  return (
    <Alert severity="warning" sx={{ mt: 2 }}>
      <AlertTitle>Churn-Risiko: Inaktiver Kunde</AlertTitle>
      <Typography variant="body2">
        Letzte Bestellung: <strong>{timeAgo}</strong> ({daysSinceLastOrder} Tage)
      </Typography>
      <Typography variant="body2" sx={{ mt: 0.5 }}>
        Empfehlung: Kunde kontaktieren und Re-Engagement-Maßnahmen einleiten.
      </Typography>
    </Alert>
  );
};

export default ChurnRiskAlert;
