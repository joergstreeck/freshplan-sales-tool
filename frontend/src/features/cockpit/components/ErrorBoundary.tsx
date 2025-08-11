/**
 * Error Boundary for Sales Cockpit components
 */

import { Component, ErrorInfo, ReactNode } from 'react';
// CSS import removed - migrating to MUI sx props

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onRetry?: () => void;
  maxRetries?: number;
}

interface State {
  hasError: boolean;
  error: Error | null;
  retryCount: number;
}

export class CockpitErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
    retryCount: 0,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error, retryCount: 0 };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {

    // TODO: Report to error monitoring service (e.g., Sentry)
    // errorReportingService.captureException(error, { extra: errorInfo });
  }

  private handleRetry = () => {
    const { maxRetries = 3, onRetry } = this.props;
    const { retryCount } = this.state;

    if (retryCount >= maxRetries) {
      return; // Prevent infinite retry loop
    }

    // Execute custom retry logic if provided
    if (onRetry) {
      onRetry();
    }

    // Reset error state and increment retry count
    this.setState(prevState => ({
      hasError: false,
      error: null,
      retryCount: prevState.retryCount + 1,
    }));
  };

  public render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div className="error-boundary-fallback">
          <div className="error-icon">⚠️</div>
          <h2 className="error-title">Etwas ist schief gelaufen</h2>
          <p className="error-message">
            {this.state.error?.message || 'Ein unerwarteter Fehler ist aufgetreten'}
          </p>
          {this.state.retryCount < (this.props.maxRetries || 3) ? (
            <button className="error-retry-btn" onClick={this.handleRetry} type="button">
              Erneut versuchen ({this.state.retryCount + 1}/{this.props.maxRetries || 3})
            </button>
          ) : (
            <div className="error-final">
              <p className="error-final-message">
                Maximale Anzahl von Wiederholungsversuchen erreicht. Bitte laden Sie die Seite neu
                oder kontaktieren Sie den Support.
              </p>
              <button
                className="error-reload-btn"
                onClick={() => window.location.reload()}
                type="button"
              >
                Seite neu laden
              </button>
            </div>
          )}
        </div>
      );
    }

    return this.props.children;
  }
}
