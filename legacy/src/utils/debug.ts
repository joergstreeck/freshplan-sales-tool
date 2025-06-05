/**
 * Debug Logger for FreshPlan
 * Provides conditional logging based on environment and feature flags
 */

export class DebugLogger {
  private static isEnabled(): boolean {
    // Check multiple sources for debug flag
    return (
      localStorage.getItem('FP_DEBUG_EVENTS') === 'true' ||
      new URLSearchParams(window.location.search).has('debug') ||
      process.env.NODE_ENV === 'development'
    );
  }

  static log(module: string, message: string, data?: any): void {
    if (!this.isEnabled()) return;

    const timestamp = new Date().toISOString();
    const prefix = `[${timestamp}] [${module}]`;

    console.log(`${prefix} ${message}`, data || '');

    // Optional: Send to monitoring service
    if ((window as any).FreshPlan?.monitoring) {
      (window as any).FreshPlan.monitoring.log({ 
        module, 
        message, 
        data, 
        timestamp 
      });
    }
  }

  static warn(module: string, message: string, data?: any): void {
    if (!this.isEnabled()) return;
    
    const timestamp = new Date().toISOString();
    const prefix = `[${timestamp}] [${module}] ⚠️`;
    
    console.warn(`${prefix} ${message}`, data || '');
  }

  static error(module: string, message: string, error?: Error): void {
    // Errors are always logged
    const timestamp = new Date().toISOString();
    const prefix = `[${timestamp}] [${module}] ❌`;
    
    console.error(`${prefix} ${message}`, error || '');
  }

  static group(label: string): void {
    if (this.isEnabled()) console.group(label);
  }

  static groupEnd(): void {
    if (this.isEnabled()) console.groupEnd();
  }

  static time(label: string): void {
    if (this.isEnabled()) console.time(label);
  }

  static timeEnd(label: string): void {
    if (this.isEnabled()) console.timeEnd(label);
  }

  /**
   * Enable debug logging for current session
   */
  static enable(): void {
    localStorage.setItem('FP_DEBUG_EVENTS', 'true');
    console.log('🔧 Debug logging enabled. Refresh to see detailed logs.');
  }

  /**
   * Disable debug logging
   */
  static disable(): void {
    localStorage.removeItem('FP_DEBUG_EVENTS');
    console.log('🔧 Debug logging disabled.');
  }
}

// Expose to window for easy access in console
if (typeof window !== 'undefined') {
  (window as any).FPDebug = DebugLogger;
}