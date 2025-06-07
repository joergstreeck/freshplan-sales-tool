import { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

/**
 * Error Boundary Component für globale Fehlerbehandlung.
 *
 * Fängt JavaScript-Fehler in der Komponenten-Hierarchie ab,
 * loggt sie und zeigt eine Fallback-UI an.
 *
 * @example
 * ```tsx
 * <ErrorBoundary>
 *   <App />
 * </ErrorBoundary>
 * ```
 */
export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error:', error, errorInfo);

    // Hier könnte ein Error-Reporting-Service integriert werden
    // z.B. Sentry, LogRocket, etc.
  }

  public render() {
    if (this.state.hasError) {
      return (
        this.props.fallback || (
          <div
            style={{
              padding: '50px',
              textAlign: 'center',
              fontFamily: 'system-ui, sans-serif',
            }}
          >
            <h1>Etwas ist schiefgelaufen</h1>
            <p>Ein unerwarteter Fehler ist aufgetreten.</p>
            {import.meta.env.DEV && this.state.error && (
              <details style={{ marginTop: '20px', textAlign: 'left' }}>
                <summary>Fehlerdetails (nur in Entwicklung)</summary>
                <pre
                  style={{
                    background: '#f4f4f4',
                    padding: '10px',
                    overflow: 'auto',
                  }}
                >
                  {this.state.error.toString()}
                  {this.state.error.stack}
                </pre>
              </details>
            )}
            <button
              onClick={() => window.location.reload()}
              style={{
                marginTop: '20px',
                padding: '10px 20px',
                cursor: 'pointer',
              }}
            >
              Seite neu laden
            </button>
          </div>
        )
      );
    }

    return this.props.children;
  }
}
