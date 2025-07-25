/**
 * Contract Status Badge Component
 * 
 * @module ContractStatusBadge
 * @description Visuelle Indikatoren für Vertragsstatus und Contract Monitoring.
 *              Zeigt auslaufende Verträge, Renewal-Status und kritische Termine.
 * @since 2.0.0
 * @author FreshPlan Team
 */

import React from 'react';
import {
  Chip,
  Box,
  Tooltip,
  Typography,
  alpha,
  useTheme,
  Theme
} from '@mui/material';
import {
  Schedule as ScheduleIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  CheckCircle as CheckCircleIcon,
  Autorenew as AutorenewIcon
} from '@mui/icons-material';

/**
 * Contract status types
 */
export enum ContractStatus {
  ACTIVE = 'active',
  EXPIRING_SOON = 'expiring_soon',    // < 90 Tage
  EXPIRING_CRITICAL = 'expiring_critical', // < 30 Tage
  EXPIRED = 'expired',
  RENEWAL_IN_PROGRESS = 'renewal_in_progress',
  RENEWAL_COMPLETED = 'renewal_completed'
}

/**
 * Contract monitoring data
 */
export interface ContractMonitoringData {
  status: ContractStatus;
  expiryDate?: Date;
  daysUntilExpiry?: number;
  renewalStartDate?: Date;
  contractValue?: number;
  autoRenewal?: boolean;
}

/**
 * Badge props
 */
export interface ContractStatusBadgeProps {
  /** Contract monitoring data */
  data: ContractMonitoringData;
  /** Size variant */
  size?: 'small' | 'medium' | 'large';
  /** Show detailed tooltip */
  showTooltip?: boolean;
  /** Custom click handler */
  onClick?: () => void;
  /** Show as minimal indicator */
  minimal?: boolean;
}

/**
 * Get status configuration
 */
function getStatusConfig(status: ContractStatus, theme: Theme) {
  switch (status) {
    case ContractStatus.ACTIVE:
      return {
        label: 'Aktiv',
        icon: <CheckCircleIcon />,
        color: theme.palette.success.main,
        bgColor: alpha(theme.palette.success.main, 0.1),
        severity: 'success' as const
      };
    
    case ContractStatus.EXPIRING_SOON:
      return {
        label: 'Läuft bald aus',
        icon: <ScheduleIcon />,
        color: theme.palette.warning.main,
        bgColor: alpha(theme.palette.warning.main, 0.1),
        severity: 'warning' as const
      };
    
    case ContractStatus.EXPIRING_CRITICAL:
      return {
        label: 'Kritisch!',
        icon: <ErrorIcon />,
        color: theme.palette.error.main,
        bgColor: alpha(theme.palette.error.main, 0.1),
        severity: 'error' as const
      };
    
    case ContractStatus.EXPIRED:
      return {
        label: 'Abgelaufen',
        icon: <ErrorIcon />,
        color: theme.palette.error.dark,
        bgColor: alpha(theme.palette.error.dark, 0.1),
        severity: 'error' as const
      };
    
    case ContractStatus.RENEWAL_IN_PROGRESS:
      return {
        label: 'Renewal läuft',
        icon: <AutorenewIcon />,
        color: theme.palette.info.main,
        bgColor: alpha(theme.palette.info.main, 0.1),
        severity: 'info' as const
      };
    
    case ContractStatus.RENEWAL_COMPLETED:
      return {
        label: 'Verlängert',
        icon: <CheckCircleIcon />,
        color: theme.palette.success.main,
        bgColor: alpha(theme.palette.success.main, 0.1),
        severity: 'success' as const
      };
    
    default:
      return {
        label: 'Unbekannt',
        icon: <WarningIcon />,
        color: theme.palette.grey[500],
        bgColor: alpha(theme.palette.grey[500], 0.1),
        severity: 'default' as const
      };
  }
}

/**
 * Format days until expiry
 */
function formatDaysUntilExpiry(days: number): string {
  if (days < 0) return 'Abgelaufen';
  if (days === 0) return 'Heute';
  if (days === 1) return '1 Tag';
  if (days < 30) return `${days} Tage`;
  if (days < 90) return `${Math.round(days / 7)} Wochen`;
  return `${Math.round(days / 30)} Monate`;
}

/**
 * Generate tooltip content
 */
function getTooltipContent(data: ContractMonitoringData): string {
  const { status, expiryDate, daysUntilExpiry, contractValue, autoRenewal } = data;
  
  const lines: string[] = [];
  
  if (expiryDate) {
    lines.push(`Vertrag läuft aus: ${expiryDate.toLocaleDateString('de-DE')}`);
  }
  
  if (daysUntilExpiry !== undefined) {
    lines.push(`Verbleibend: ${formatDaysUntilExpiry(daysUntilExpiry)}`);
  }
  
  if (contractValue) {
    lines.push(`Vertragswert: €${contractValue.toLocaleString()}`);
  }
  
  if (autoRenewal !== undefined) {
    lines.push(`Auto-Renewal: ${autoRenewal ? 'Aktiviert' : 'Deaktiviert'}`);
  }
  
  if (status === ContractStatus.RENEWAL_IN_PROGRESS) {
    lines.push('Renewal-Prozess läuft bereits');
  }
  
  return lines.join('\n');
}

/**
 * Contract status badge component
 */
export const ContractStatusBadge: React.FC<ContractStatusBadgeProps> = ({
  data,
  size = 'medium',
  showTooltip = true,
  onClick,
  minimal = false
}) => {
  const theme = useTheme();
  const config = getStatusConfig(data.status, theme);
  
  const sizeConfig = {
    small: { fontSize: '0.75rem', padding: '2px 6px', iconSize: 14 },
    medium: { fontSize: '0.875rem', padding: '4px 8px', iconSize: 16 },
    large: { fontSize: '1rem', padding: '6px 12px', iconSize: 18 }
  }[size];

  const chipContent = (
    <Chip
      label={
        <Box display="flex" alignItems="center" gap={0.5}>
          <Box
            component="span"
            sx={{
              display: 'flex',
              alignItems: 'center',
              fontSize: sizeConfig.iconSize
            }}
          >
            {React.cloneElement(config.icon, { 
              sx: { fontSize: sizeConfig.iconSize } 
            })}
          </Box>
          
          {!minimal && (
            <Typography variant="caption" component="span">
              {config.label}
            </Typography>
          )}
          
          {data.daysUntilExpiry !== undefined && 
           data.status !== ContractStatus.RENEWAL_COMPLETED && (
            <Typography variant="caption" component="span" sx={{ ml: 0.5 }}>
              ({formatDaysUntilExpiry(data.daysUntilExpiry)})
            </Typography>
          )}
        </Box>
      }
      size={size === 'large' ? 'medium' : 'small'}
      variant="filled"
      onClick={onClick}
      sx={{
        backgroundColor: config.bgColor,
        color: config.color,
        border: `1px solid ${alpha(config.color, 0.3)}`,
        fontSize: sizeConfig.fontSize,
        fontWeight: 500,
        cursor: onClick ? 'pointer' : 'default',
        '&:hover': onClick ? {
          backgroundColor: alpha(config.color, 0.15),
          transform: 'scale(1.02)'
        } : {},
        transition: 'all 0.2s ease'
      }}
    />
  );

  if (!showTooltip) {
    return chipContent;
  }

  return (
    <Tooltip
      title={getTooltipContent(data)}
      arrow
      placement="top"
    >
      {chipContent}
    </Tooltip>
  );
};

/**
 * Helper function to determine contract status based on days until expiry
 */
export function getContractStatus(
  daysUntilExpiry: number,
  isRenewalInProgress = false,
  isRenewalCompleted = false
): ContractStatus {
  if (isRenewalCompleted) {
    return ContractStatus.RENEWAL_COMPLETED;
  }
  
  if (isRenewalInProgress) {
    return ContractStatus.RENEWAL_IN_PROGRESS;
  }
  
  if (daysUntilExpiry < 0) {
    return ContractStatus.EXPIRED;
  }
  
  if (daysUntilExpiry <= 30) {
    return ContractStatus.EXPIRING_CRITICAL;
  }
  
  if (daysUntilExpiry <= 90) {
    return ContractStatus.EXPIRING_SOON;
  }
  
  return ContractStatus.ACTIVE;
}

/**
 * Contract monitoring indicator for opportunity cards
 */
export const ContractMonitoringIndicator: React.FC<{
  opportunityName: string;
  expiryDate: Date;
  isRenewal?: boolean;
  contractValue?: number;
}> = ({ expiryDate, isRenewal = false, contractValue }) => {
  const today = new Date();
  const daysUntilExpiry = Math.ceil((expiryDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  
  const status = getContractStatus(
    daysUntilExpiry,
    isRenewal,
    false
  );

  const data: ContractMonitoringData = {
    status,
    expiryDate,
    daysUntilExpiry,
    contractValue
  };

  return (
    <ContractStatusBadge
      data={data}
      size="small"
      minimal={true}
    />
  );
};

export default ContractStatusBadge;