/**
 * GdprDeletedBadge - Sprint 2.1.8
 *
 * Badge-Komponente für DSGVO Art. 17 - Recht auf Löschung
 * Zeigt an, dass ein Lead DSGVO-konform anonymisiert wurde.
 */

import React from 'react';
import { Chip, Tooltip } from '@mui/material';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';

interface GdprDeletedBadgeProps {
  gdprDeletedAt?: string;
  gdprDeletedBy?: string;
  gdprDeletionReason?: string;
  compact?: boolean;
}

export function GdprDeletedBadge({
  gdprDeletedAt,
  gdprDeletedBy,
  gdprDeletionReason,
  compact = false,
}: GdprDeletedBadgeProps) {
  const formattedDate = gdprDeletedAt
    ? new Date(gdprDeletedAt).toLocaleDateString('de-DE', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    : 'Unbekannt';

  const tooltipContent = (
    <>
      <strong>DSGVO-anonymisiert (Art. 17)</strong>
      <br />
      Gelöscht am: {formattedDate}
      {gdprDeletedBy && (
        <>
          <br />
          Von: {gdprDeletedBy}
        </>
      )}
      {gdprDeletionReason && (
        <>
          <br />
          Grund: {gdprDeletionReason}
        </>
      )}
    </>
  );

  if (compact) {
    return (
      <Tooltip title={tooltipContent} arrow>
        <DeleteForeverIcon color="disabled" fontSize="small" />
      </Tooltip>
    );
  }

  return (
    <Tooltip title={tooltipContent} arrow>
      <Chip
        icon={<DeleteForeverIcon />}
        label="DSGVO-anonymisiert"
        color="default"
        size="small"
        variant="outlined"
        sx={{ opacity: 0.7 }}
      />
    </Tooltip>
  );
}
