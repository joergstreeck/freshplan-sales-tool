/**
 * Enterprise Toast Notification System
 * 
 * @module ToastProvider
 * @description Zentrales Toast-System für alle Benachrichtigungen im CRM.
 *              Unterstützt verschiedene Severity-Level, Auto-Dismiss und Actions.
 * @since 2.0.0
 * @author FreshPlan Team
 */

import React, { createContext, useContext, useState, useCallback } from 'react';
import {
  Snackbar,
  Alert,
  AlertColor,
  IconButton,
  Box,
  Typography,
  Button
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';

/**
 * Toast notification type
 */
export interface Toast {
  id: string;
  message: string;
  title?: string;
  severity: AlertColor;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
  persistent?: boolean;
}

/**
 * Toast context type
 */
interface ToastContextType {
  addToast: (toast: Omit<Toast, 'id'>) => void;
  removeToast: (id: string) => void;
  clearAll: () => void;
}

/**
 * Toast context
 */
const ToastContext = createContext<ToastContextType | undefined>(undefined);

/**
 * Hook to use toast notifications
 */
export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

/**
 * Enhanced toast hook with predefined methods
 */
export const useEnhancedToast = () => {
  const { addToast, removeToast, clearAll } = useToast();

  const showSuccess = useCallback((message: string, title?: string, action?: Toast['action']) => {
    addToast({
      message,
      title,
      severity: 'success',
      duration: 4000,
      action
    });
  }, [addToast]);

  const showError = useCallback((message: string, title?: string, persistent = false) => {
    addToast({
      message,
      title,
      severity: 'error',
      duration: persistent ? undefined : 6000,
      persistent
    });
  }, [addToast]);

  const showWarning = useCallback((message: string, title?: string, action?: Toast['action']) => {
    addToast({
      message,
      title,
      severity: 'warning',
      duration: 5000,
      action
    });
  }, [addToast]);

  const showInfo = useCallback((message: string, title?: string, action?: Toast['action']) => {
    addToast({
      message,
      title,
      severity: 'info',
      duration: 4000,
      action
    });
  }, [addToast]);

  return {
    showSuccess,
    showError,
    showWarning,
    showInfo,
    removeToast,
    clearAll
  };
};

/**
 * Toast provider component
 */
export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const addToast = useCallback((toastData: Omit<Toast, 'id'>) => {
    const id = Math.random().toString(36).substr(2, 9);
    const toast: Toast = {
      ...toastData,
      id
    };

    setToasts(prev => [...prev, toast]);

    // Auto-dismiss nach duration (falls nicht persistent)
    if (!toast.persistent && toast.duration !== undefined) {
      setTimeout(() => {
        removeToast(id);
      }, toast.duration);
    }
  }, []);

  const removeToast = useCallback((id: string) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  }, []);

  const clearAll = useCallback(() => {
    setToasts([]);
  }, []);

  return (
    <ToastContext.Provider value={{ addToast, removeToast, clearAll }}>
      {children}
      
      {/* Render all toasts */}
      {toasts.map((toast, index) => (
        <Snackbar
          key={toast.id}
          open={true}
          anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
          sx={{
            top: `${80 + index * 70}px !important` // Stack toasts vertically
          }}
        >
          <Alert
            severity={toast.severity}
            variant="filled"
            onClose={() => removeToast(toast.id)}
            action={
              <Box display="flex" alignItems="center" gap={1}>
                {toast.action && (
                  <Button
                    color="inherit"
                    size="small"
                    onClick={toast.action.onClick}
                    sx={{ mr: 1 }}
                  >
                    {toast.action.label}
                  </Button>
                )}
                <IconButton
                  size="small"
                  aria-label="close"
                  color="inherit"
                  onClick={() => removeToast(toast.id)}
                >
                  <CloseIcon fontSize="small" />
                </IconButton>
              </Box>
            }
            sx={{
              minWidth: 320,
              maxWidth: 500,
              boxShadow: 3
            }}
          >
            {toast.title && (
              <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 0.5 }}>
                {toast.title}
              </Typography>
            )}
            <Typography variant="body2">
              {toast.message}
            </Typography>
          </Alert>
        </Snackbar>
      ))}
    </ToastContext.Provider>
  );
};

/**
 * Predefined toast notifications for common CRM actions
 */
export const CrmToasts = {
  /**
   * Stage change notifications
   */
  stageChanged: (stageName: string, opportunityName: string) => ({
    title: 'Stage geändert',
    message: `"${opportunityName}" wurde zu "${stageName}" verschoben.`,
    severity: 'success' as AlertColor,
    duration: 3000
  }),

  stageChangeFailed: (error: string) => ({
    title: 'Stage-Wechsel fehlgeschlagen',
    message: `Fehler beim Verschieben: ${error}`,
    severity: 'error' as AlertColor,
    duration: 5000
  }),

  /**
   * Renewal notifications
   */
  renewalStarted: (customerName: string) => ({
    title: 'Renewal-Prozess gestartet',
    message: `Vertragsverlängerung für "${customerName}" wurde eingeleitet.`,
    severity: 'info' as AlertColor,
    duration: 4000,
    action: {
      label: 'Details',
      onClick: () => {
        // Navigate to renewal details
        console.log('Navigate to renewal details');
      }
    }
  }),

  renewalCompleted: (customerName: string, isSuccessful: boolean) => ({
    title: isSuccessful ? 'Renewal erfolgreich' : 'Renewal nicht erfolgreich',
    message: isSuccessful 
      ? `Vertrag mit "${customerName}" wurde erfolgreich verlängert.`
      : `Renewal mit "${customerName}" konnte nicht abgeschlossen werden.`,
    severity: (isSuccessful ? 'success' : 'warning') as AlertColor,
    duration: 5000
  }),

  /**
   * Deal close notifications
   */
  dealWon: (opportunityName: string, value?: number) => ({
    title: '🎉 Deal gewonnen!',
    message: `"${opportunityName}" wurde erfolgreich abgeschlossen${value ? ` (€${value.toLocaleString()})` : ''}.`,
    severity: 'success' as AlertColor,
    duration: 6000,
    action: {
      label: 'Onboarding starten',
      onClick: () => {
        // Navigate to onboarding
        console.log('Start onboarding process');
      }
    }
  }),

  dealLost: (opportunityName: string, reason?: string) => ({
    title: 'Deal verloren',
    message: `"${opportunityName}" wurde als verloren markiert${reason ? `: ${reason}` : ''}.`,
    severity: 'warning' as AlertColor,
    duration: 4000
  }),

  /**
   * System notifications
   */
  autoSaved: () => ({
    message: 'Änderungen automatisch gespeichert',
    severity: 'info' as AlertColor,
    duration: 2000
  }),

  syncError: (retryAction: () => void) => ({
    title: 'Synchronisation fehlgeschlagen',
    message: 'Verbindung zum Server unterbrochen. Versuchen Sie es erneut.',
    severity: 'error' as AlertColor,
    persistent: true,
    action: {
      label: 'Wiederholen',
      onClick: retryAction
    }
  }),

  /**
   * Contract monitoring notifications
   */
  contractExpiring: (customerName: string, daysLeft: number) => ({
    title: 'Vertrag läuft aus',
    message: `Vertrag mit "${customerName}" läuft in ${daysLeft} Tagen aus.`,
    severity: 'warning' as AlertColor,
    duration: 6000,
    action: {
      label: 'Renewal starten',
      onClick: () => {
        // Start renewal process
        console.log('Start renewal for', customerName);
      }
    }
  })
};

export default ToastProvider;