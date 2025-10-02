import { Chip, Tooltip, Box } from '@mui/material';
import {
  CheckCircle as ProtectedIcon,
  Warning as WarningIcon,
  Error as ExpiredIcon,
} from '@mui/icons-material';
import type { ProtectionStatus, LeadProtectionInfo } from './types';

interface LeadProtectionBadgeProps {
  protectionInfo: LeadProtectionInfo;
  variant?: 'outlined' | 'filled';
  size?: 'small' | 'medium';
}

export default function LeadProtectionBadge({
  protectionInfo,
  variant = 'filled',
  size = 'small',
}: LeadProtectionBadgeProps) {
  const { status, protectionUntil, progressDeadline, daysUntilExpiry, warningMessage } = protectionInfo;

  // Color-Coding based on Protection Status
  const getStatusColor = (): 'success' | 'warning' | 'error' => {
    switch (status) {
      case 'protected':
        return 'success'; // Grün
      case 'warning':
        return 'warning'; // Gelb
      case 'expired':
        return 'error'; // Rot
      default:
        return 'success';
    }
  };

  // Icon based on Protection Status
  const getStatusIcon = () => {
    switch (status) {
      case 'protected':
        return <ProtectedIcon />;
      case 'warning':
        return <WarningIcon />;
      case 'expired':
        return <ExpiredIcon />;
      default:
        return <ProtectedIcon />;
    }
  };

  // Label based on Protection Status
  const getStatusLabel = (): string => {
    switch (status) {
      case 'protected':
        return 'Geschützt';
      case 'warning':
        return `Warnung (${daysUntilExpiry}T)`;
      case 'expired':
        return 'Abgelaufen';
      default:
        return 'Unbekannt';
    }
  };

  // Tooltip Content with detailed information
  const getTooltipContent = () => {
    return (
      <Box>
        <Box sx={{ fontWeight: 'bold', mb: 1 }}>
          {status === 'protected' && '✅ Lead ist geschützt'}
          {status === 'warning' && '⚠️ Lead-Schutz läuft bald ab'}
          {status === 'expired' && '❌ Lead-Schutz abgelaufen'}
        </Box>

        {protectionUntil && (
          <Box sx={{ fontSize: '0.875rem', mb: 0.5 }}>
            <strong>Schutz bis:</strong> {new Date(protectionUntil).toLocaleDateString('de-DE', {
              day: '2-digit',
              month: '2-digit',
              year: 'numeric',
            })}
          </Box>
        )}

        {progressDeadline && (
          <Box sx={{ fontSize: '0.875rem', mb: 0.5 }}>
            <strong>Progress-Deadline:</strong> {new Date(progressDeadline).toLocaleDateString('de-DE', {
              day: '2-digit',
              month: '2-digit',
              year: 'numeric',
            })}
          </Box>
        )}

        {daysUntilExpiry !== undefined && (
          <Box sx={{ fontSize: '0.875rem', mb: 0.5 }}>
            <strong>Verbleibend:</strong> {daysUntilExpiry} Tage
          </Box>
        )}

        {warningMessage && (
          <Box sx={{ fontSize: '0.875rem', mt: 1, fontStyle: 'italic', color: 'warning.light' }}>
            {warningMessage}
          </Box>
        )}

        {status === 'protected' && !warningMessage && (
          <Box sx={{ fontSize: '0.75rem', mt: 1, opacity: 0.8 }}>
            Vertrag §3.2: 6 Monate ab Registrierung + 60-Tage-Aktivitätsstandard
          </Box>
        )}
      </Box>
    );
  };

  return (
    <Tooltip
      title={getTooltipContent()}
      arrow
      placement="top"
      enterDelay={200}
      leaveDelay={200}
    >
      <Chip
        icon={getStatusIcon()}
        label={getStatusLabel()}
        color={getStatusColor()}
        variant={variant}
        size={size}
        aria-label={`Lead-Schutzstatus: ${getStatusLabel()}`}
        sx={{
          fontWeight: 500,
          cursor: 'help',
          '& .MuiChip-icon': {
            fontSize: size === 'small' ? '1rem' : '1.25rem',
          },
        }}
      />
    </Tooltip>
  );
}

// Utility function to calculate Protection Status from Lead data
export function calculateProtectionInfo(lead: {
  protectionUntil?: string;
  progressDeadline?: string;
  progressWarningSentAt?: string;
}): LeadProtectionInfo {
  const now = new Date();

  // Calculate days until expiry (based on progressDeadline)
  let daysUntilExpiry: number | undefined;
  let status: ProtectionStatus = 'protected';
  let warningMessage: string | undefined;

  if (lead.progressDeadline) {
    const deadline = new Date(lead.progressDeadline);
    const diffTime = deadline.getTime() - now.getTime();
    daysUntilExpiry = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    // Status Logic (based on TRIGGER_SPRINT_2_1_5.md)
    if (daysUntilExpiry <= 0) {
      status = 'expired';
      warningMessage = 'Keine Progress-Aktivität seit 60+ Tagen. Lead-Schutz verfallen.';
    } else if (daysUntilExpiry <= 7) {
      status = 'warning';
      warningMessage = `Nur noch ${daysUntilExpiry} Tage bis Progress-Deadline! Bitte Aktivität durchführen.`;
    } else {
      status = 'protected';
    }
  }

  // Check if warning was already sent (Tag 53-60)
  if (lead.progressWarningSentAt && status === 'protected') {
    const warningSent = new Date(lead.progressWarningSentAt);
    if (warningSent.getTime() > now.getTime() - 7 * 24 * 60 * 60 * 1000) {
      // Warning sent within last 7 days
      status = 'warning';
      warningMessage = 'Progress-Warnung versendet. Bitte substanzielle Aktivität durchführen.';
    }
  }

  return {
    status,
    protectionUntil: lead.protectionUntil,
    progressDeadline: lead.progressDeadline,
    daysUntilExpiry,
    warningMessage,
  };
}
