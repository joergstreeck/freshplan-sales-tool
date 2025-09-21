import React, { useEffect, useRef } from 'react';
import { useSecurityContext } from './SecurityProvider';

/**
 * SecurityAuditLogger für FreshFoodz B2B CRM
 *
 * Ziel: Frontend-seitige Security-Event-Protokollierung für GDPR-Compliance
 * - Automatisches Logging von Security-relevanten User-Actions
 * - Performance-optimiert mit Batch-Sending
 * - Integration mit Backend SecurityAuditInterceptor
 */

interface SecurityEvent {
  timestamp: string;
  userId: string;
  orgId: string;
  territory: string;
  eventType: string;
  eventDetails: any;
  component: string;
  sessionId: string;
}

interface SecurityAuditLoggerProps {
  children: React.ReactNode;
  batchSize?: number;
  flushInterval?: number; // ms
  enableDebugLogging?: boolean;
}

// Event Queue für Batch-Sending
class SecurityEventQueue {
  private events: SecurityEvent[] = [];
  private readonly maxSize: number;
  private readonly flushInterval: number;
  private flushTimer?: NodeJS.Timeout;
  private readonly onFlush: (events: SecurityEvent[]) => Promise<void>;

  constructor(maxSize: number, flushInterval: number, onFlush: (events: SecurityEvent[]) => Promise<void>) {
    this.maxSize = maxSize;
    this.flushInterval = flushInterval;
    this.onFlush = onFlush;
    this.startFlushTimer();
  }

  addEvent(event: SecurityEvent) {
    this.events.push(event);

    if (this.events.length >= this.maxSize) {
      this.flush();
    }
  }

  private startFlushTimer() {
    this.flushTimer = setInterval(() => {
      if (this.events.length > 0) {
        this.flush();
      }
    }, this.flushInterval);
  }

  private async flush() {
    if (this.events.length === 0) return;

    const eventsToSend = [...this.events];
    this.events = [];

    try {
      await this.onFlush(eventsToSend);
    } catch (error) {
      console.error('Failed to send security events:', error);
      // Re-queue events bei Fehler (mit Limit um Endlos-Loop zu vermeiden)
      if (eventsToSend.length < 1000) {
        this.events.unshift(...eventsToSend);
      }
    }
  }

  destroy() {
    if (this.flushTimer) {
      clearInterval(this.flushTimer);
    }
    // Final flush bei Destroy
    this.flush();
  }
}

export const SecurityAuditLogger: React.FC<SecurityAuditLoggerProps> = ({
  children,
  batchSize = 10,
  flushInterval = 30000, // 30 Sekunden
  enableDebugLogging = false
}) => {
  const security = useSecurityContext();
  const eventQueueRef = useRef<SecurityEventQueue | null>(null);
  const sessionIdRef = useRef<string>(generateSessionId());

  // Initialize Event Queue
  useEffect(() => {
    const sendEvents = async (events: SecurityEvent[]) => {
      try {
        const response = await fetch('/api/security/frontend-audit', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${await getAccessToken()}`,
          },
          body: JSON.stringify({ events })
        });

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        if (enableDebugLogging) {
          console.log(`Security audit: sent ${events.length} events`);
        }
      } catch (error) {
        console.error('Security audit logging failed:', error);
        throw error;
      }
    };

    eventQueueRef.current = new SecurityEventQueue(batchSize, flushInterval, sendEvents);

    return () => {
      eventQueueRef.current?.destroy();
    };
  }, [batchSize, flushInterval, enableDebugLogging]);

  // Security Event Logging Function
  const logSecurityEvent = (eventType: string, eventDetails: any, component: string) => {
    if (!security.claims || !eventQueueRef.current) return;

    const event: SecurityEvent = {
      timestamp: new Date().toISOString(),
      userId: security.claims.userId,
      orgId: security.claims.orgId,
      territory: security.claims.territory,
      eventType,
      eventDetails,
      component,
      sessionId: sessionIdRef.current,
    };

    eventQueueRef.current.addEvent(event);

    if (enableDebugLogging) {
      console.log('Security event logged:', event);
    }
  };

  // Global Event Listener für automatische Security-Events
  useEffect(() => {
    const handleSecurityEvent = (event: CustomEvent) => {
      logSecurityEvent(event.detail.type, event.detail.data, event.detail.component);
    };

    // Listen auf Custom Security Events
    window.addEventListener('security-event', handleSecurityEvent as EventListener);

    // Listen auf Navigation Events (Page-Access-Logging)
    const handleRouteChange = () => {
      logSecurityEvent('PAGE_ACCESS', {
        path: window.location.pathname,
        search: window.location.search,
        referrer: document.referrer,
      }, 'Router');
    };

    window.addEventListener('popstate', handleRouteChange);
    // Für SPA: Initial Page Load
    handleRouteChange();

    return () => {
      window.removeEventListener('security-event', handleSecurityEvent as EventListener);
      window.removeEventListener('popstate', handleRouteChange);
    };
  }, [logSecurityEvent]);

  // Provide logging function to children via context
  useEffect(() => {
    // Global Security Logging Function verfügbar machen
    (window as any).logSecurityEvent = logSecurityEvent;

    return () => {
      delete (window as any).logSecurityEvent;
    };
  }, [logSecurityEvent]);

  return <>{children}</>;
};

// Helper Function für Session ID Generation
function generateSessionId(): string {
  return `sess_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

// Helper um Access Token zu erhalten
const getAccessToken = async (): Promise<string> => {
  // In echter Implementation aus useAuth Hook
  return localStorage.getItem('access_token') || '';
};

// Security Event Dispatcher für Components
export const useSecurityEventLogger = () => {
  const logEvent = (type: string, data: any, component: string) => {
    const event = new CustomEvent('security-event', {
      detail: { type, data, component }
    });
    window.dispatchEvent(event);
  };

  return {
    // Lead-specific Events
    logLeadAccess: (leadId: string, accessType: 'VIEW' | 'EDIT' | 'DELETE') =>
      logEvent('LEAD_ACCESS', { leadId, accessType }, 'LeadComponent'),

    logLeadCreation: (leadId: string, leadData: any) =>
      logEvent('LEAD_CREATED', { leadId, leadData }, 'LeadForm'),

    logLeadEdit: (leadId: string, changes: any) =>
      logEvent('LEAD_EDITED', { leadId, changes }, 'LeadEditForm'),

    // Territory Events
    logTerritorySwitch: (fromTerritory: string, toTerritory: string) =>
      logEvent('TERRITORY_SWITCH', { fromTerritory, toTerritory }, 'TerritorySelector'),

    // Note/Activity Events
    logNoteAccess: (noteId: string, category: string, visibility: string) =>
      logEvent('NOTE_ACCESS', { noteId, category, visibility }, 'NoteComponent'),

    logNoteCreation: (noteId: string, category: string, visibility: string) =>
      logEvent('NOTE_CREATED', { noteId, category, visibility }, 'NoteForm'),

    // Security Violation Events
    logAccessDenied: (resource: string, requiredPermission: string, reason: string) =>
      logEvent('ACCESS_DENIED', { resource, requiredPermission, reason }, 'SecurityGuard'),

    logSuspiciousActivity: (activityType: string, details: any) =>
      logEvent('SUSPICIOUS_ACTIVITY', { activityType, details }, 'SecurityMonitor'),

    // Export/Import Events (GDPR-relevant)
    logDataExport: (exportType: string, recordCount: number, filters: any) =>
      logEvent('DATA_EXPORT', { exportType, recordCount, filters }, 'ExportComponent'),

    logDataImport: (importType: string, recordCount: number, source: string) =>
      logEvent('DATA_IMPORT', { importType, recordCount, source }, 'ImportComponent'),

    // Authentication Events
    logLoginAttempt: (success: boolean, method: string) =>
      logEvent('LOGIN_ATTEMPT', { success, method }, 'AuthComponent'),

    logLogout: (reason: string) =>
      logEvent('LOGOUT', { reason }, 'AuthComponent'),

    // Generic Security Event
    logCustomSecurityEvent: (eventType: string, details: any, component: string) =>
      logEvent(eventType, details, component),
  };
};

// React Component Wrapper für automatisches Event-Logging
export const withSecurityLogging = <P extends object>(
  WrappedComponent: React.ComponentType<P>,
  componentName: string
) => {
  return React.forwardRef<any, P>((props, ref) => {
    const logger = useSecurityEventLogger();

    useEffect(() => {
      logger.logCustomSecurityEvent('COMPONENT_MOUNTED', { componentName }, componentName);

      return () => {
        logger.logCustomSecurityEvent('COMPONENT_UNMOUNTED', { componentName }, componentName);
      };
    }, [logger]);

    return <WrappedComponent {...props} ref={ref} />;
  });
};

// HOC für Lead-spezifische Components
export const withLeadSecurityLogging = <P extends { leadId?: string }>(
  WrappedComponent: React.ComponentType<P>,
  componentName: string
) => {
  return React.forwardRef<any, P>((props, ref) => {
    const logger = useSecurityEventLogger();

    useEffect(() => {
      if (props.leadId) {
        logger.logLeadAccess(props.leadId, 'VIEW');
      }
    }, [props.leadId, logger]);

    return <WrappedComponent {...props} ref={ref} />;
  });
};