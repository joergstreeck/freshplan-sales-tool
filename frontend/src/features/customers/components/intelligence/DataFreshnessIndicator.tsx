import React from 'react';
import { Alert, AlertTitle, Button, Chip, Box, Tooltip, CircularProgress } from '@mui/material';
import {
  Warning as WarningIcon,
  Error as ErrorIcon,
  CheckCircle as CheckCircleIcon,
  Info as InfoIcon,
  Update as UpdateIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { contactInteractionApi } from '../../services/contactInteractionApi';
import type { Contact } from '../../types/contact.types';

interface DataFreshnessIndicatorProps {
  contact: Contact;
  variant?: 'alert' | 'chip' | 'inline';
  showUpdateButton?: boolean;
  onUpdateClick?: (contact: Contact) => void;
}

interface DataFreshnessLevel {
  key: string;
  description: string;
  severity: 'success' | 'info' | 'warning' | 'error';
  muiColor: string;
  displayName: string;
}

/**
 * Komponente zur Anzeige der Datenfreshness eines Kontakts.
 *
 * Basiert auf dem Data Strategy Intelligence Konzept für
 * kontinuierliche Datenhygiene und proaktive Update-Kampagnen.
 */
export const DataFreshnessIndicator: React.FC<DataFreshnessIndicatorProps> = ({
  contact,
  variant = 'alert',
  showUpdateButton = true,
  onUpdateClick,
}) => {
  // Lade Freshness Level vom Backend
  const {
    data: freshnessLevel,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['contact-freshness', contact.id],
    queryFn: () => contactInteractionApi.getContactFreshnessLevel(contact.id),
    staleTime: 5 * 60 * 1000, // 5 Minuten
  });

  if (isLoading) {
    return (
      <Box display="flex" alignItems="center" gap={1}>
        <CircularProgress size={16} />
        <span>Prüfe Aktualität...</span>
      </Box>
    );
  }

  if (error || !freshnessLevel) {
    return null; // Bei Fehler nichts anzeigen
  }

  // Wenn Daten fresh sind, nichts anzeigen (wie im Konzept spezifiziert)
  if (freshnessLevel.key === 'fresh') {
    return null;
  }

  const daysSinceUpdate = getDaysSinceLastUpdate(contact);
  const _isCritical = freshnessLevel.key === 'critical';
  const _isStale = freshnessLevel.key === 'stale';

  const handleUpdateClick = () => {
    if (onUpdateClick) {
      onUpdateClick(contact);
    } else {
      // Default: Navigate to contact edit
      window.open(`/customers/edit/${contact.id}`, '_blank');
    }
  };

  // Chip Variant
  if (variant === 'chip') {
    return (
      <Tooltip title={getTooltipText(freshnessLevel, daysSinceUpdate)}>
        <Chip
          icon={getIcon(freshnessLevel.key)}
          label={freshnessLevel.displayName}
          color={freshnessLevel.severity}
          size="small"
          variant="outlined"
        />
      </Tooltip>
    );
  }

  // Inline Variant
  if (variant === 'inline') {
    return (
      <Box display="flex" alignItems="center" gap={1}>
        {getIcon(freshnessLevel.key)}
        <span>{freshnessLevel.displayName}</span>
        {showUpdateButton && (
          <Button
            size="small"
            variant="text"
            startIcon={<UpdateIcon />}
            onClick={handleUpdateClick}
          >
            Aktualisieren
          </Button>
        )}
      </Box>
    );
  }

  // Alert Variant (Default)
  return (
    <Alert
      severity={freshnessLevel.severity}
      action={
        showUpdateButton ? (
          <Button size="small" onClick={handleUpdateClick} startIcon={<UpdateIcon />}>
            Jetzt aktualisieren
          </Button>
        ) : undefined
      }
      sx={{ mb: 2 }}
    >
      <AlertTitle>{getAlertTitle(freshnessLevel.key)}</AlertTitle>
      {getAlertMessage(contact, daysSinceUpdate)}
    </Alert>
  );
};

/**
 * Berechnet Tage seit letztem Update
 */
function getDaysSinceLastUpdate(contact: Contact): number {
  if (!contact.updatedAt) return 999;

  const lastUpdate = new Date(contact.updatedAt);
  const now = new Date();
  const diffTime = Math.abs(now.getTime() - lastUpdate.getTime());
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

/**
 * Gibt das passende Icon für das Freshness Level zurück
 */
function getIcon(level: string) {
  switch (level) {
    case 'fresh':
      return <CheckCircleIcon color="success" fontSize="small" />;
    case 'aging':
      return <InfoIcon color="info" fontSize="small" />;
    case 'stale':
      return <WarningIcon color="warning" fontSize="small" />;
    case 'critical':
      return <ErrorIcon color="error" fontSize="small" />;
    default:
      return <InfoIcon fontSize="small" />;
  }
}

/**
 * Generiert Tooltip-Text
 */
function getTooltipText(freshnessLevel: DataFreshnessLevel, daysSinceUpdate: number): string {
  return `Daten sind ${daysSinceUpdate} Tage alt (${freshnessLevel.displayName})`;
}

/**
 * Generiert Alert-Titel basierend auf Freshness Level
 */
function getAlertTitle(level: string): string {
  switch (level) {
    case 'aging':
      return 'Daten könnten bald veraltet sein';
    case 'stale':
      return 'Daten könnten veraltet sein';
    case 'critical':
      return 'Dringend: Daten sind über ein Jahr alt';
    default:
      return 'Daten prüfen';
  }
}

/**
 * Generiert Alert-Nachricht
 */
function getAlertMessage(contact: Contact, daysSinceUpdate: number): string {
  const contactName = `${contact.firstName} ${contact.lastName}`.trim() || 'Dieser Kontakt';

  if (daysSinceUpdate > 365) {
    return `${contactName} wurde seit über einem Jahr (${daysSinceUpdate} Tage) nicht aktualisiert. Die Daten könnten veraltet oder ungenau sein.`;
  } else if (daysSinceUpdate > 180) {
    return `${contactName} wurde seit ${daysSinceUpdate} Tagen nicht aktualisiert. Bitte überprüfen Sie die Daten.`;
  } else {
    return `${contactName} wurde seit ${daysSinceUpdate} Tagen nicht aktualisiert.`;
  }
}

export default DataFreshnessIndicator;
