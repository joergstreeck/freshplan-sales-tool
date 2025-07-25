/**
 * Enterprise-grade Confirmation Dialog Component
 * 
 * @module ConfirmationDialog
 * @description Wiederverwendbare Bestätigungsdialoge für kritische Aktionen im CRM.
 *              Unterstützt verschiedene Severity-Level und Custom-Aktionen.
 * @since 2.0.0
 * @author FreshPlan Team
 */

import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  Alert,
  Box,
  Typography,
  useTheme,
  Theme
} from '@mui/material';
import {
  Warning as WarningIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  CheckCircle as SuccessIcon
} from '@mui/icons-material';

/**
 * Severity levels for confirmation dialogs
 */
export enum ConfirmationSeverity {
  INFO = 'info',
  WARNING = 'warning',
  ERROR = 'error',
  SUCCESS = 'success'
}

/**
 * Confirmation dialog properties
 */
export interface ConfirmationDialogProps {
  /** Whether the dialog is open */
  open: boolean;
  /** Dialog title */
  title: string;
  /** Main message to display */
  message: string;
  /** Optional detailed description */
  description?: string;
  /** Severity level affecting colors and icons */
  severity?: ConfirmationSeverity;
  /** Text for the confirm button */
  confirmText?: string;
  /** Text for the cancel button */
  cancelText?: string;
  /** Whether the confirm action is loading */
  isLoading?: boolean;
  /** Callback when user confirms */
  onConfirm: () => void;
  /** Callback when user cancels or closes */
  onCancel: () => void;
  /** Optional custom content to display */
  children?: React.ReactNode;
}

/**
 * Get icon and color for severity level
 */
function getSeverityConfig(severity: ConfirmationSeverity, theme: Theme) {
  switch (severity) {
    case ConfirmationSeverity.ERROR:
      return {
        icon: <ErrorIcon />,
        color: theme.palette.error.main,
        buttonColor: 'error' as const
      };
    case ConfirmationSeverity.WARNING:
      return {
        icon: <WarningIcon />,
        color: theme.palette.warning.main,
        buttonColor: 'warning' as const
      };
    case ConfirmationSeverity.SUCCESS:
      return {
        icon: <SuccessIcon />,
        color: theme.palette.success.main,
        buttonColor: 'success' as const
      };
    case ConfirmationSeverity.INFO:
    default:
      return {
        icon: <InfoIcon />,
        color: theme.palette.info.main,
        buttonColor: 'primary' as const
      };
  }
}

/**
 * Enterprise-grade confirmation dialog component
 */
export const ConfirmationDialog: React.FC<ConfirmationDialogProps> = ({
  open,
  title,
  message,
  description,
  severity = ConfirmationSeverity.INFO,
  confirmText = 'Bestätigen',
  cancelText = 'Abbrechen',
  isLoading = false,
  onConfirm,
  onCancel,
  children
}) => {
  const theme = useTheme();
  const { icon, color, buttonColor } = getSeverityConfig(severity, theme);

  return (
    <Dialog
      open={open}
      onClose={onCancel}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: theme.shadows[8]
        }
      }}
    >
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <Box
            sx={{
              color,
              display: 'flex',
              alignItems: 'center'
            }}
          >
            {icon}
          </Box>
          <Typography variant="h6" component="span">
            {title}
          </Typography>
        </Box>
      </DialogTitle>

      <DialogContent>
        <DialogContentText component="div">
          <Typography variant="body1" sx={{ mb: description ? 1 : 0 }}>
            {message}
          </Typography>
          
          {description && (
            <Typography variant="body2" color="text.secondary">
              {description}
            </Typography>
          )}
        </DialogContentText>

        {severity === ConfirmationSeverity.WARNING && (
          <Alert severity="warning" sx={{ mt: 2 }}>
            Diese Aktion kann nicht rückgängig gemacht werden.
          </Alert>
        )}

        {severity === ConfirmationSeverity.ERROR && (
          <Alert severity="error" sx={{ mt: 2 }}>
            Achtung: Diese Aktion ist kritisch und kann Datenverlust verursachen.
          </Alert>
        )}

        {children && (
          <Box sx={{ mt: 2 }}>
            {children}
          </Box>
        )}
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button
          onClick={onCancel}
          variant="outlined"
          disabled={isLoading}
        >
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          variant="contained"
          color={buttonColor}
          disabled={isLoading}
          sx={{
            minWidth: 120,
            ml: 1
          }}
        >
          {isLoading ? 'Wird bearbeitet...' : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

/**
 * Predefined confirmation dialogs for common use cases
 */
export const ConfirmationDialogs = {
  /**
   * Confirm stage change dialog
   */
  stageChange: (
    stageName: string,
    onConfirm: () => void,
    onCancel: () => void,
    isLoading?: boolean
  ) => (
    <ConfirmationDialog
      open={true}
      title="Stage-Wechsel bestätigen"
      message={`Möchten Sie diese Opportunity zu "${stageName}" verschieben?`}
      description="Der Stage-Wechsel wird in der Audit-Historie dokumentiert."
      severity={ConfirmationSeverity.INFO}
      confirmText="Verschieben"
      onConfirm={onConfirm}
      onCancel={onCancel}
      isLoading={isLoading}
    />
  ),

  /**
   * Confirm renewal dialog
   */
  renewalTransition: (
    onConfirm: () => void,
    onCancel: () => void,
    isLoading?: boolean
  ) => (
    <ConfirmationDialog
      open={true}
      title="Vertragsverlängerung starten"
      message="Möchten Sie den Renewal-Prozess für diesen Kunden starten?"
      description="Der bestehende Vertrag wird zur Verlängerung vorgemerkt und alle relevanten Stakeholder werden benachrichtigt."
      severity={ConfirmationSeverity.WARNING}
      confirmText="Renewal starten"
      onConfirm={onConfirm}
      onCancel={onCancel}
      isLoading={isLoading}
    />
  ),

  /**
   * Confirm close deal dialog
   */
  closeDeal: (
    isWon: boolean,
    onConfirm: () => void,
    onCancel: () => void,
    isLoading?: boolean
  ) => (
    <ConfirmationDialog
      open={true}
      title={isWon ? "Deal als gewonnen markieren" : "Deal als verloren markieren"}
      message={
        isWon 
          ? "Möchten Sie diese Opportunity als erfolgreich abgeschlossen markieren?"
          : "Möchten Sie diese Opportunity als verloren markieren?"
      }
      description={
        isWon
          ? "Der Kunde wird automatisch angelegt und das Onboarding wird gestartet."
          : "Bitte dokumentieren Sie den Verlustgrund für zukünftige Analysen."
      }
      severity={isWon ? ConfirmationSeverity.SUCCESS : ConfirmationSeverity.WARNING}
      confirmText={isWon ? "Als gewonnen markieren" : "Als verloren markieren"}
      onConfirm={onConfirm}
      onCancel={onCancel}
      isLoading={isLoading}
    />
  )
};

export default ConfirmationDialog;