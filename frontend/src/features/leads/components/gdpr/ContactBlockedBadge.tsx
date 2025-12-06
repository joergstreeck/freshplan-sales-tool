/**
 * ContactBlockedBadge - Sprint 2.1.8
 *
 * Badge-Komponente für DSGVO Art. 7.3 - Widerruf der Einwilligung
 * Zeigt an, dass ein Lead nicht mehr kontaktiert werden darf.
 */

import React from 'react';
import { Chip, Tooltip } from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';

interface ContactBlockedBadgeProps {
  consentRevokedAt?: string;
  consentRevokedBy?: string;
  compact?: boolean;
}

export function ContactBlockedBadge({
  consentRevokedAt,
  consentRevokedBy,
  compact = false,
}: ContactBlockedBadgeProps) {
  const formattedDate = consentRevokedAt
    ? new Date(consentRevokedAt).toLocaleDateString('de-DE', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    : 'Unbekannt';

  const tooltipContent = (
    <>
      <strong>Kontakt gesperrt (DSGVO Art. 7.3)</strong>
      <br />
      Widerrufen am: {formattedDate}
      {consentRevokedBy && (
        <>
          <br />
          Von: {consentRevokedBy}
        </>
      )}
    </>
  );

  if (compact) {
    return (
      <Tooltip title={tooltipContent} arrow>
        <BlockIcon color="error" fontSize="small" />
      </Tooltip>
    );
  }

  return (
    <Tooltip title={tooltipContent} arrow>
      <Chip
        icon={<BlockIcon />}
        label="Kontakt gesperrt"
        color="error"
        size="small"
        variant="outlined"
      />
    </Tooltip>
  );
}
