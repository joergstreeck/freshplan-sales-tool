/**
 * Enterprise Error Boundary Component
 *
 * @module ErrorBoundary
 * @description React Error Boundary für graceful error handling mit Logging und Monitoring
 * @since 2.0.0
 */

import React, { Component, ErrorInfo, ReactNode } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  Alert,
  AlertTitle,
  Collapse,
} from '@mui/material';
import {
  Error as ErrorIcon,
  Refresh as RefreshIcon,
  ExpandMore as ExpandMoreIcon,
} from '@mui/icons-material';
import { logger } from '../lib/logger';

/**
 * Error Boundary Props
 * @interface IErrorBoundaryProps
 */
interface IErrorBoundaryProps {
  /** Child components to wrap */
  children: ReactNode;
  /** Fallback UI component (optional) */
  fallback?: ReactNode;
  /** Context name for logging */
  context?: string;
  /** Whether to show error details in development */
  showDetails?: boolean;
  /** Callback when error occurs */
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
}

/**
 * Error Boundary State
 * @interface IErrorBoundaryState
 */
interface IErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
  errorId: string;
  showDetails: boolean;
}

/**
 * Enterprise Error Boundary Implementation
 * @class ErrorBoundary
 * @extends {Component<IErrorBoundaryProps, IErrorBoundaryState>}
 */
export class ErrorBoundary extends Component<IErrorBoundaryProps, IErrorBoundaryState> {
  private loggerInstance = logger.child('ErrorBoundary', {
    context: this.props.context,
  });

  public state: IErrorBoundaryState = {
    hasError: false,
    error: null,
    errorInfo: null,
    errorId: '',
    showDetails: false,
  };

  /**
   * Catch errors in component tree
   * @static
   * @param {Error} error - The error that was thrown
   * @returns {Partial<IErrorBoundaryState>} New state
   */
  public static getDerivedStateFromError(error: Error): Partial<IErrorBoundaryState> {
    const errorId = `ERR-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

    return {
      hasError: true,
      error,
      errorId,
    };
  }

  /**
   * Log error details and report to monitoring
   * @param {Error} error - The error that was caught
   * @param {ErrorInfo} errorInfo - React error info with component stack
   */
  public componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    const { onError, context } = this.props;
    const { errorId } = this.state;

    // Log to console in development
    if (import.meta.env.DEV) {
      console.error('ErrorBoundary caught an error:', error, errorInfo);
    }

    // Log with structured data
    this.loggerInstance.error('Component tree error', {
      errorId,
      errorName: error.name,
      errorMessage: error.message,
      errorStack: error.stack,
      componentStack: errorInfo.componentStack,
      context,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString(),
    });

    // Update state with error info
    this.setState({ errorInfo });

    // Call custom error handler if provided
    if (onError) {
      onError(error, errorInfo);
    }
  }

  /**
   * Reset error state
   */
  private resetError = (): void => {
    this.loggerInstance.info('Error boundary reset', {
      errorId: this.state.errorId,
    });

    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      errorId: '',
      showDetails: false,
    });
  };

  /**
   * Toggle error details visibility
   */
  private toggleDetails = (): void => {
    this.setState(prev => ({ showDetails: !prev.showDetails }));
  };

  public render() {
    const { hasError, error, errorInfo, errorId, showDetails } = this.state;
    const { children, fallback } = this.props;
    const isDevelopment = import.meta.env.DEV;

    if (hasError && error) {
      // Use custom fallback if provided
      if (fallback) {
        return <>{fallback}</>;
      }

      // Default error UI
      return (
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '400px',
            p: 3,
          }}
        >
          <Card sx={{ maxWidth: 600, width: '100%' }}>
            <CardContent>
              <Box sx={{ textAlign: 'center', mb: 3 }}>
                <ErrorIcon
                  sx={{
                    fontSize: 64,
                    color: 'error.main',
                    mb: 2,
                  }}
                />
                <Typography variant="h5" gutterBottom>
                  Oops! Etwas ist schiefgelaufen
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Ein unerwarteter Fehler ist aufgetreten. Das Entwicklungsteam wurde automatisch
                  benachrichtigt.
                </Typography>
                {errorId && (
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ fontFamily: 'monospace' }}
                  >
                    Fehler-ID: {errorId}
                  </Typography>
                )}
              </Box>

              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', mb: 2 }}>
                <Button
                  variant="contained"
                  startIcon={<RefreshIcon />}
                  onClick={this.resetError}
                  sx={{
                    bgcolor: '#94C456',
                    '&:hover': {
                      bgcolor: '#7BA646',
                    },
                  }}
                >
                  Neu laden
                </Button>
                {isDevelopment && (
                  <Button
                    variant="outlined"
                    onClick={this.toggleDetails}
                    endIcon={
                      <ExpandMoreIcon
                        sx={{
                          transform: showDetails ? 'rotate(180deg)' : 'rotate(0deg)',
                          transition: 'transform 0.3s',
                        }}
                      />
                    }
                  >
                    Details
                  </Button>
                )}
              </Box>

              {isDevelopment && errorInfo && (
                <Collapse in={showDetails}>
                  <Alert severity="error" sx={{ mt: 2, textAlign: 'left' }}>
                    <AlertTitle>{error.name}</AlertTitle>
                    <Typography
                      variant="body2"
                      component="pre"
                      sx={{
                        fontFamily: 'monospace',
                        fontSize: '0.875rem',
                        whiteSpace: 'pre-wrap',
                        wordBreak: 'break-word',
                      }}
                    >
                      {error.message}
                    </Typography>
                    <Box sx={{ mt: 2, maxHeight: 300, overflow: 'auto' }}>
                      <Typography
                        variant="caption"
                        component="pre"
                        sx={{
                          fontFamily: 'monospace',
                          fontSize: '0.75rem',
                          opacity: 0.8,
                        }}
                      >
                        {errorInfo.componentStack}
                      </Typography>
                    </Box>
                  </Alert>
                </Collapse>
              )}
            </CardContent>
          </Card>
        </Box>
      );
    }

    return <>{children}</>;
  }
}

/**
 * Error Boundary Hook for functional components
 * @hook useErrorHandler
 * @param {string} context - Context name for logging
 * @returns {(error: Error) => void} Error handler function
 */
export const useErrorHandler = (context?: string) => {
  const loggerInstance = React.useMemo(
    () => logger.child('useErrorHandler', { context }),
    [context]
  );

  return React.useCallback(
    (error: Error, errorInfo?: Record<string, unknown>) => {
      loggerInstance.error('Hook error handler', {
        errorName: error.name,
        errorMessage: error.message,
        errorStack: error.stack,
        ...errorInfo,
      });

      // Re-throw to be caught by nearest ErrorBoundary
      throw error;
    },
    [loggerInstance]
  );
};
