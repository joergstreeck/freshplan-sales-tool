/**
 * Enterprise Logging Service
 * 
 * @module Logger
 * @description Zentraler Logging-Service mit strukturierten Logs,
 *              Performance Monitoring und Error Tracking.
 * @since 2.0.0
 */

/**
 * Log levels based on severity
 * @enum {string}
 */
export enum LogLevel {
  DEBUG = 'debug',
  INFO = 'info',
  WARN = 'warn',
  ERROR = 'error',
  FATAL = 'fatal'
}

/**
 * Structured log entry
 * @interface ILogEntry
 */
interface ILogEntry {
  level: LogLevel;
  message: string;
  timestamp: string;
  context?: string;
  userId?: string;
  sessionId?: string;
  traceId?: string;
  data?: Record<string, unknown>;
  error?: Error;
  performance?: {
    duration?: number;
    startTime?: number;
  };
}

/**
 * Logger configuration
 * @interface ILoggerConfig
 */
interface ILoggerConfig {
  minLevel: LogLevel;
  enableConsole: boolean;
  enableRemote: boolean;
  remoteEndpoint?: string;
  performanceThreshold: number; // ms
  contextDefaults?: Record<string, unknown>;
}

/**
 * Enterprise Logger Implementation
 * @class Logger
 */
class Logger {
  private config: ILoggerConfig;
  private buffer: ILogEntry[] = [];
  private flushInterval?: number;

  constructor(config: Partial<ILoggerConfig> = {}) {
    this.config = {
      minLevel: LogLevel.INFO,
      enableConsole: true,
      enableRemote: false,
      performanceThreshold: 1000,
      ...config
    };

    // Start flush interval for remote logging
    if (this.config.enableRemote && this.config.remoteEndpoint) {
      this.startFlushInterval();
    }
  }

  /**
   * Log a debug message
   * @param {string} message - Debug message
   * @param {Record<string, unknown>} data - Additional context data
   */
  debug(message: string, data?: Record<string, unknown>): void {
    this.log(LogLevel.DEBUG, message, data);
  }

  /**
   * Log an info message
   * @param {string} message - Info message
   * @param {Record<string, unknown>} data - Additional context data
   */
  info(message: string, data?: Record<string, unknown>): void {
    this.log(LogLevel.INFO, message, data);
  }

  /**
   * Log a warning
   * @param {string} message - Warning message
   * @param {Record<string, unknown>} data - Additional context data
   */
  warn(message: string, data?: Record<string, unknown>): void {
    this.log(LogLevel.WARN, message, data);
  }

  /**
   * Log an error
   * @param {string} message - Error message
   * @param {Error | Record<string, unknown>} errorOrData - Error object or context data
   */
  error(message: string, errorOrData?: Error | Record<string, unknown>): void {
    const isError = errorOrData instanceof Error;
    this.log(
      LogLevel.ERROR,
      message,
      isError ? { errorMessage: errorOrData.message, stack: errorOrData.stack } : errorOrData,
      isError ? errorOrData : undefined
    );
  }

  /**
   * Log a fatal error
   * @param {string} message - Fatal error message
   * @param {Error} error - Error object
   */
  fatal(message: string, error: Error): void {
    this.log(LogLevel.FATAL, message, {
      errorMessage: error.message,
      stack: error.stack
    }, error);
    
    // Immediately flush on fatal
    this.flush();
  }

  /**
   * Create a child logger with additional context
   * @param {string} context - Context name
   * @param {Record<string, unknown>} defaults - Default data for all logs
   * @returns {Logger} Child logger instance
   */
  child(context: string, defaults?: Record<string, unknown>): Logger {
    return new Logger({
      ...this.config,
      contextDefaults: {
        ...this.config.contextDefaults,
        context,
        ...defaults
      }
    });
  }

  /**
   * Performance timing helper
   * @param {string} operation - Operation name
   * @returns {() => void} End timer function
   */
  time(operation: string): () => void {
    const startTime = performance.now();
    
    return () => {
      const duration = performance.now() - startTime;
      const level = duration > this.config.performanceThreshold ? LogLevel.WARN : LogLevel.INFO;
      
      this.log(level, `Performance: ${operation}`, {
        operation,
        duration: Math.round(duration),
        threshold: this.config.performanceThreshold,
        exceeded: duration > this.config.performanceThreshold
      });
    };
  }

  /**
   * Core logging method
   * @private
   */
  private log(
    level: LogLevel,
    message: string,
    data?: Record<string, unknown>,
    error?: Error
  ): void {
    // Check minimum log level
    if (!this.shouldLog(level)) {
      return;
    }

    const entry: ILogEntry = {
      level,
      message,
      timestamp: new Date().toISOString(),
      data: {
        ...this.config.contextDefaults,
        ...data
      },
      error,
      // Add user context if available
      userId: this.getCurrentUserId(),
      sessionId: this.getSessionId(),
      traceId: this.getTraceId()
    };

    // Console output
    if (this.config.enableConsole) {
      this.consoleOutput(entry);
    }

    // Buffer for remote
    if (this.config.enableRemote) {
      this.buffer.push(entry);
      
      // Auto-flush on error or fatal
      if (level === LogLevel.ERROR || level === LogLevel.FATAL) {
        this.flush();
      }
    }
  }

  /**
   * Console output with proper formatting
   * @private
   */
  private consoleOutput(entry: ILogEntry): void {
    const { level, message, data, error } = entry;
    const prefix = `[${entry.timestamp}] [${level.toUpperCase()}]`;
    
    switch (level) {
      case LogLevel.DEBUG:
        console.debug(prefix, message, data);
        break;
      case LogLevel.INFO:
        console.info(prefix, message, data);
        break;
      case LogLevel.WARN:
        console.warn(prefix, message, data);
        break;
      case LogLevel.ERROR:
      case LogLevel.FATAL:
        console.error(prefix, message, data, error);
        break;
    }
  }

  /**
   * Check if should log based on level
   * @private
   */
  private shouldLog(level: LogLevel): boolean {
    const levels = [LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR, LogLevel.FATAL];
    const minIndex = levels.indexOf(this.config.minLevel);
    const levelIndex = levels.indexOf(level);
    return levelIndex >= minIndex;
  }

  /**
   * Get current user ID from auth context
   * @private
   */
  private getCurrentUserId(): string | undefined {
    // TODO: Implement from auth context
    return undefined;
  }

  /**
   * Get or create session ID
   * @private
   */
  private getSessionId(): string {
    if (!window.sessionStorage.getItem('sessionId')) {
      window.sessionStorage.setItem('sessionId', this.generateId());
    }
    return window.sessionStorage.getItem('sessionId')!;
  }

  /**
   * Get current trace ID for distributed tracing
   * @private
   */
  private getTraceId(): string | undefined {
    // TODO: Implement from request context
    return undefined;
  }

  /**
   * Generate unique ID
   * @private
   */
  private generateId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Start flush interval for remote logging
   * @private
   */
  private startFlushInterval(): void {
    this.flushInterval = window.setInterval(() => {
      if (this.buffer.length > 0) {
        this.flush();
      }
    }, 5000); // Flush every 5 seconds
  }

  /**
   * Flush buffered logs to remote
   * @private
   */
  private async flush(): Promise<void> {
    if (!this.config.enableRemote || !this.config.remoteEndpoint || this.buffer.length === 0) {
      return;
    }

    const entries = [...this.buffer];
    this.buffer = [];

    try {
      await fetch(this.config.remoteEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ entries })
      });
    } catch (error) {
      // Re-add to buffer on failure
      this.buffer.unshift(...entries);
      console.error('Failed to flush logs:', error);
    }
  }

  /**
   * Cleanup on destroy
   */
  destroy(): void {
    if (this.flushInterval) {
      clearInterval(this.flushInterval);
    }
    this.flush();
  }
}

// Export singleton instance with default config
export const logger = new Logger({
  minLevel: import.meta.env.MODE === 'production' ? LogLevel.INFO : LogLevel.DEBUG,
  enableConsole: true,
  enableRemote: import.meta.env.MODE === 'production',
  remoteEndpoint: import.meta.env.VITE_LOG_ENDPOINT,
  performanceThreshold: 1000
});

// Export for testing and custom instances
export { Logger, type ILogEntry, type ILoggerConfig };