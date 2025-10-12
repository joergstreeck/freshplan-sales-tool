/**
 * PreClaimBadge - Variante B Pre-Claim Status Indicator
 *
 * Zeigt den Schut status eines Leads basierend auf firstContactDocumentedAt:
 * - Vollschutz (grün): firstContactDocumentedAt !== null
 * - Pre-Claim aktiv (orange): firstContactDocumentedAt === null, Frist läuft noch
 * - Pre-Claim überfällig (rot): firstContactDocumentedAt === null, Frist abgelaufen
 *
 * Variante B Logic (Handelsvertretervertrag §2(8)(a)):
 * - registeredAt IMMER gesetzt (Audit Trail)
 * - firstContactDocumentedAt NULL → Pre-Claim mit 10 Tagen Frist
 * - firstContactDocumentedAt NOT NULL → Vollschutz (6 Monate)
 *
 * FreshFoodz CI: #94C456 (Grün), #FF9800 (Orange), #F44336 (Rot)
 */

import React from 'react';
import { Chip, Tooltip, Box } from '@mui/material';
import {
  Lock as LockIcon,
  Shield as ShieldIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import type { Lead } from '../types';

interface PreClaimBadgeProps {
  lead: Lead;
  variant?: 'default' | 'outlined';
  size?: 'small' | 'medium';
}

const PreClaimBadge: React.FC<PreClaimBadgeProps> = ({
  lead,
  variant = 'default',
  size = 'small',
}) => {
  // Variante B: Pre-Claim Detection
  const hasFullProtection = !!lead.firstContactDocumentedAt;

  if (hasFullProtection) {
    // ✅ Vollschutz aktiv - berechne verbleibende Zeit
    const protectionEnd = lead.protectionUntil ? new Date(lead.protectionUntil) : null;
    const now = new Date();

    let remainingLabel = 'Geschützt';
    let isExpiringSoon = false;

    if (protectionEnd) {
      const daysRemaining = Math.ceil(
        (protectionEnd.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
      );
      const monthsRemaining = Math.floor(daysRemaining / 30);

      // Warnung bei < 30 Tagen
      if (daysRemaining < 30 && daysRemaining > 0) {
        remainingLabel = `Schutz endet in ${daysRemaining} Tagen`;
        isExpiringSoon = true;
      } else if (monthsRemaining > 0) {
        remainingLabel = `Geschützt (noch ${monthsRemaining} ${monthsRemaining === 1 ? 'Monat' : 'Monate'})`;
      }
    }

    return (
      <Tooltip
        title={
          <Box>
            <strong>Vollschutz aktiv</strong>
            <br />
            Erstkontakt dokumentiert am:{' '}
            {new Date(lead.firstContactDocumentedAt!).toLocaleDateString('de-DE', {
              day: '2-digit',
              month: '2-digit',
              year: 'numeric',
            })}
            <br />
            {protectionEnd && (
              <>
                Schutz bis:{' '}
                {protectionEnd.toLocaleDateString('de-DE', {
                  day: '2-digit',
                  month: '2-digit',
                  year: 'numeric',
                })}
              </>
            )}
          </Box>
        }
      >
        <Chip
          icon={isExpiringSoon ? <WarningIcon /> : <LockIcon />}
          label={remainingLabel}
          size={size}
          variant={variant}
          sx={{
            backgroundColor:
              variant === 'default' ? (isExpiringSoon ? '#FF9800' : '#94C456') : undefined,
            borderColor: isExpiringSoon ? '#FF9800' : '#94C456',
            color: variant === 'default' ? '#fff' : isExpiringSoon ? '#FF9800' : '#94C456',
            fontWeight: 600,
            '& .MuiChip-icon': {
              color: variant === 'default' ? '#fff' : isExpiringSoon ? '#FF9800' : '#94C456',
            },
          }}
        />
      </Tooltip>
    );
  }

  // Pre-Claim aktiv - berechne Deadline
  const registeredDate = new Date(lead.registeredAt);
  const deadline = new Date(registeredDate);
  deadline.setDate(deadline.getDate() + 10); // 10 Tage Frist

  const now = new Date();
  const daysRemaining = Math.ceil((deadline.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  const isOverdue = daysRemaining < 0;

  if (isOverdue) {
    // 🔴 Pre-Claim überfällig
    return (
      <Tooltip
        title={
          <Box>
            <strong>Pre-Claim überfällig!</strong>
            <br />
            Erstkontakt-Frist war: {deadline.toLocaleDateString('de-DE')}
            <br />
            {Math.abs(daysRemaining)} Tag{Math.abs(daysRemaining) !== 1 ? 'e' : ''} überfällig
            <br />
            <br />
            <em>Bitte sofort Erstkontakt dokumentieren!</em>
          </Box>
        }
      >
        <Chip
          icon={<WarningIcon />}
          label="Pre-Claim überfällig!"
          size={size}
          variant={variant}
          sx={{
            backgroundColor: variant === 'default' ? '#F44336' : undefined,
            borderColor: '#F44336',
            color: variant === 'default' ? '#fff' : '#F44336',
            fontWeight: 600,
            '& .MuiChip-icon': {
              color: variant === 'default' ? '#fff' : '#F44336',
            },
          }}
        />
      </Tooltip>
    );
  }

  // 🟡 Pre-Claim aktiv (Frist läuft noch)
  return (
    <Tooltip
      title={
        <Box>
          <strong>Vormerkung - Erstkontakt ausstehend</strong>
          <br />
          Registriert am: {registeredDate.toLocaleDateString('de-DE')}
          <br />
          Erstkontakt bis: {deadline.toLocaleDateString('de-DE')}
          <br />
          <br />
          <em>
            Noch {daysRemaining} Tag{daysRemaining !== 1 ? 'e' : ''} Zeit für
            Erstkontakt-Dokumentation
          </em>
        </Box>
      }
    >
      <Chip
        icon={<ShieldIcon />}
        label={`Vormerkung (noch ${daysRemaining} ${daysRemaining === 1 ? 'Tag' : 'Tage'})`}
        size={size}
        variant={variant}
        sx={{
          backgroundColor: variant === 'default' ? '#FF9800' : undefined,
          borderColor: '#FF9800',
          color: variant === 'default' ? '#fff' : '#FF9800',
          fontWeight: 600,
          '& .MuiChip-icon': {
            color: variant === 'default' ? '#fff' : '#FF9800',
          },
        }}
      />
    </Tooltip>
  );
};

export default PreClaimBadge;
