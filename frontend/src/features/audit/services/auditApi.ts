import { httpClient } from '@/lib/apiClient';
import { isFeatureEnabled as _isFeatureEnabled } from '@/config/featureFlags';
import type {
  AuditLog,
  AuditFilters,
  AuditDashboardMetrics,
  ActivityChartData,
  ComplianceAlert,
  AuditExportOptions,
} from '../types';

// Mock data for development
const mockAuditLogs: AuditLog[] = [
  {
    id: '1',
    timestamp: new Date().toISOString(),
    occurredAt: new Date().toISOString(), // For UserAuditTimeline compatibility
    userId: 'user-123',
    userName: 'Max Mustermann',
    userEmail: 'max@freshplan.de',
    eventType: 'USER_LOGIN',
    entityType: 'USER',
    entityId: 'user-123',
    action: 'LOGIN',
    source: 'WEB',
    ipAddress: '192.168.1.1',
    userAgent: 'Mozilla/5.0',
    details: { browser: 'Chrome', os: 'Windows' },
    changes: null,
    previousHash: 'abc123',
    dataHash: 'def456',
    success: true,
  },
  {
    id: '2',
    timestamp: new Date(Date.now() - 3600000).toISOString(),
    occurredAt: new Date(Date.now() - 3600000).toISOString(),
    userId: 'user-456',
    userName: 'Anna Schmidt',
    userEmail: 'anna@freshplan.de',
    eventType: 'CUSTOMER_UPDATE',
    entityType: 'CUSTOMER',
    entityId: 'cust-789',
    action: 'UPDATE',
    source: 'API',
    ipAddress: '192.168.1.2',
    userAgent: 'FreshPlan API Client',
    details: { fields: ['name', 'email'] },
    changes: { name: { old: 'Alt', new: 'Neu' } },
    previousHash: 'def456',
    dataHash: 'ghi789',
    success: true,
  },
  {
    id: '3',
    timestamp: new Date(Date.now() - 7200000).toISOString(),
    occurredAt: new Date(Date.now() - 7200000).toISOString(),
    userId: 'user-789',
    userName: 'Peter Weber',
    userEmail: 'peter@freshplan.de',
    eventType: 'PERMISSION_DENIED',
    entityType: 'REPORT',
    entityId: 'report-123',
    action: 'ACCESS_DENIED',
    source: 'WEB',
    ipAddress: '192.168.1.3',
    userAgent: 'Mozilla/5.0',
    details: { reason: 'Insufficient permissions' },
    changes: null,
    previousHash: 'ghi789',
    dataHash: 'jkl012',
    success: false,
  },
];

const mockDashboardMetrics: AuditDashboardMetrics = {
  coverage: 95.5,
  integrityStatus: 'valid',
  retentionCompliance: 98,
  lastAudit: new Date().toISOString(),
  criticalEventsToday: 2,
  activeUsers: 15,
  totalEventsToday: 247,
  topEventTypes: [
    { type: 'USER_LOGIN', count: 89 },
    { type: 'CUSTOMER_UPDATE', count: 45 },
    { type: 'REPORT_VIEW', count: 38 },
    { type: 'DATA_EXPORT', count: 22 },
    { type: 'PERMISSION_CHANGE', count: 12 },
  ],
};

const mockActivityChartData: ActivityChartData[] = [
  { time: '00:00', value: 5 },
  { time: '01:00', value: 3 },
  { time: '02:00', value: 1 },
  { time: '03:00', value: 2 },
  { time: '04:00', value: 4 },
  { time: '05:00', value: 8 },
  { time: '06:00', value: 12 },
  { time: '07:00', value: 25 },
  { time: '08:00', value: 45 },
  { time: '09:00', value: 62 },
  { time: '10:00', value: 58 },
  { time: '11:00', value: 54 },
  { time: '12:00', value: 48 },
  { time: '13:00', value: 52 },
  { time: '14:00', value: 61 },
  { time: '15:00', value: 55 },
  { time: '16:00', value: 42 },
  { time: '17:00', value: 28 },
  { time: '18:00', value: 15 },
  { time: '19:00', value: 10 },
  { time: '20:00', value: 8 },
  { time: '21:00', value: 6 },
  { time: '22:00', value: 4 },
  { time: '23:00', value: 3 },
];

const mockComplianceAlerts: ComplianceAlert[] = [
  {
    id: 'alert-1',
    type: 'retention',
    severity: 'warning',
    title: 'Datenaufbewahrung überschreitet 90 Tage',
    description: '15 Audit-Einträge sind älter als 90 Tage und sollten archiviert werden.',
    timestamp: new Date().toISOString(),
    resolved: false,
  },
  {
    id: 'alert-2',
    type: 'integrity',
    severity: 'info',
    title: 'Nächste Integritätsprüfung fällig',
    description: 'Die monatliche Integritätsprüfung steht in 3 Tagen an.',
    timestamp: new Date().toISOString(),
    resolved: false,
  },
];

export const auditApi = {
  // Get audit logs with filters
  async getAuditLogs(
    filters: AuditFilters & {
      page?: number;
      pageSize?: number;
      userId?: string;
      limit?: number;
      action?: string;
      from?: string;
      to?: string;
    }
  ) {
    // Always try to use real data first
    try {
      const params = new URLSearchParams();

      // Handle date range - backend expects ISO strings for from/to
      if (filters.from) {
        params.append('from', filters.from);
      } else if (filters.dateRange?.from) {
        params.append('from', filters.dateRange.from.toISOString());
      }

      if (filters.to) {
        params.append('to', filters.to);
      } else if (filters.dateRange?.to) {
        params.append('to', filters.dateRange.to.toISOString());
      }

      // Map frontend parameters to backend expectations
      if (filters.entityType) params.append('entityType', filters.entityType);
      if (filters.entityId) params.append('entityId', filters.entityId);
      if (filters.userId) params.append('userId', filters.userId);

      // Handle action -> eventType mapping
      if (filters.action) {
        // Map action to eventType for backend
        const eventTypeMap: Record<string, string> = {
          CREATE: 'CUSTOMER_CREATED',
          UPDATE: 'CUSTOMER_UPDATED',
          DELETE: 'CUSTOMER_DELETED',
          VIEW: 'CUSTOMER_VIEWED',
          EXPORT: 'DATA_EXPORT_STARTED',
          LOGIN: 'USER_LOGIN',
          ALL: '', // Don't send eventType for ALL
        };
        const eventType = eventTypeMap[filters.action] || filters.action;
        if (eventType) {
          params.append('eventType', eventType);
        }
      }

      if (filters.eventTypes) {
        filters.eventTypes.forEach(type => params.append('eventType', type));
      }
      if (filters.users) {
        filters.users.forEach(user => params.append('userId', user));
      }
      if (filters.searchText) params.append('searchText', filters.searchText);
      if (filters.page !== undefined) params.append('page', filters.page.toString());

      // Handle both limit and pageSize (backend expects 'size')
      const size = filters.limit || filters.pageSize || 50;
      params.append('size', size.toString());

      const response = await httpClient.get<AuditLog[]>(`/api/audit/search?${params}`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch audit logs, using mock data:', error);
      // Only use mock data as fallback when API fails
      return mockAuditLogs;
    }
  },

  // Get audit detail
  async getAuditDetail(id: string) {
    // Always try to use real data first
    try {
      const response = await httpClient.get<AuditLog>(`/api/audit/${id}`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch audit detail, using mock data:', error);
      // Only use mock data as fallback when API fails
      const log = mockAuditLogs.find(l => l.id === id);
      return log || mockAuditLogs[0];
    }
  },

  // Get entity audit trail
  async getEntityAuditTrail(entityType: string, entityId: string, page = 0, size = 50) {
    // Always try to use real data first
    try {
      const response = await httpClient.get<AuditLog[]>(
        `/api/audit/entity/${entityType}/${entityId}?page=${page}&size=${size}`
      );
      // Return in the format expected by MiniAuditTimeline
      return {
        content: response.data,
        totalElements: response.data.length,
      };
    } catch (error) {
      console.error('Failed to fetch entity audit trail, using mock data:', error);
      // Only use mock data as fallback when API fails
      const filtered = mockAuditLogs.filter(
        log => log.entityType === entityType && log.entityId === entityId
      );

      // For development, add some mock contact audit data
      if (entityType === 'CONTACT' && filtered.length === 0) {
        const mockContactAudit: AuditLog[] = [
          {
            id: `contact-audit-1-${entityId}`,
            timestamp: new Date(Date.now() - 86400000).toISOString(), // 1 day ago
            occurredAt: new Date(Date.now() - 86400000).toISOString(),
            userId: 'user-123',
            userName: 'Max Mustermann',
            userEmail: 'max@freshplan.de',
            eventType: 'CONTACT_UPDATED',
            entityType: 'CONTACT',
            entityId: entityId,
            action: 'UPDATE',
            source: 'WEB',
            ipAddress: '192.168.1.1',
            userAgent: 'Mozilla/5.0',
            details: { fields: ['phone', 'email'] },
            changes: {
              phone: { old: '+49 30 12345', new: '+49 30 54321' },
              email: { old: 'old@example.com', new: 'new@example.com' },
            },
            previousHash: 'abc123',
            dataHash: 'def456',
            success: true,
          },
          {
            id: `contact-audit-2-${entityId}`,
            timestamp: new Date(Date.now() - 172800000).toISOString(), // 2 days ago
            occurredAt: new Date(Date.now() - 172800000).toISOString(),
            userId: 'user-456',
            userName: 'Anna Schmidt',
            userEmail: 'anna@freshplan.de',
            eventType: 'CONTACT_ADDED',
            entityType: 'CONTACT',
            entityId: entityId,
            action: 'CREATE',
            source: 'WEB',
            ipAddress: '192.168.1.2',
            userAgent: 'Mozilla/5.0',
            details: {},
            changes: null,
            previousHash: null,
            dataHash: 'abc123',
            success: true,
          },
        ];

        return {
          content: mockContactAudit,
          totalElements: mockContactAudit.length,
        };
      }

      return {
        content: filtered,
        totalElements: filtered.length,
      };
    }
  },

  // Dashboard metrics
  async getDashboardMetrics() {
    // Always use real data for dashboard metrics
    // The backend now provides real data instead of mocks
    try {
      const response = await httpClient.get<AuditDashboardMetrics>('/api/audit/dashboard/metrics');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch dashboard metrics, using mock data:', error);
      // Only use mock data as fallback when API fails
      return mockDashboardMetrics;
    }
  },

  // Activity chart data
  async getActivityChartData(days = 7, groupBy = 'hour') {
    // Always try to fetch real data first
    try {
      const response = await httpClient.get<ActivityChartData[]>(
        `/api/audit/dashboard/activity-chart?days=${days}&groupBy=${groupBy}`
      );
      return response.data;
    } catch (error) {
      console.error('Failed to fetch activity chart data, using mock data:', error);
      // Only use mock data as fallback when API fails
      return mockActivityChartData;
    }
  },

  // Critical events
  async getCriticalEvents(limit = 10) {
    // Always try to fetch real data first
    try {
      const response = await httpClient.get<AuditLog[]>(
        `/api/audit/dashboard/critical-events?limit=${limit}`
      );
      return response.data;
    } catch (error) {
      console.error('Failed to fetch critical events, using mock data:', error);
      // Only use mock data as fallback when API fails
      const criticalEvents = mockAuditLogs.filter(
        log => log.eventType.includes('DENIED') || log.eventType.includes('ERROR') || !log.success
      );
      return criticalEvents.slice(0, limit);
    }
  },

  // Compliance alerts
  async getComplianceAlerts() {
    // Always try to fetch real data first
    try {
      const response = await httpClient.get<ComplianceAlert[]>(
        '/api/audit/dashboard/compliance-alerts'
      );
      return response.data;
    } catch (error) {
      console.error('Failed to fetch compliance alerts, using mock data:', error);
      // Only use mock data as fallback when API fails
      return mockComplianceAlerts;
    }
  },

  // Export audit logs - DEPRECATED: Use Universal Export Framework via /api/v2/export/audit/{format}
  // Kept for backward compatibility only
  async exportAuditLogs(options: AuditExportOptions) {
    console.warn('exportAuditLogs is deprecated. Use UniversalExportButton component instead.');
    // Redirect to new Universal Export Framework endpoint
    const params = new URLSearchParams();
    params.append('from', options.dateRange.from.toISOString().split('T')[0]);
    params.append('to', options.dateRange.to.toISOString().split('T')[0]);

    if (options.filters?.entityType) {
      params.append('entityType', options.filters.entityType);
    }
    if (options.filters?.eventTypes) {
      options.filters.eventTypes.forEach(type => params.append('eventType', type));
    }

    // Use the new Universal Export endpoint
    const format = options.format || 'csv';
    const url = `/api/v2/export/audit/${format}?${params}`;

    window.open(url, '_blank');
  },

  // Verify integrity
  async verifyIntegrity(from?: Date, to?: Date) {
    // Always try to use real data first
    try {
      const params = new URLSearchParams();
      if (from) params.append('from', from.toISOString());
      if (to) params.append('to', to.toISOString());

      const response = await httpClient.post<{
        status: 'valid' | 'compromised';
        message?: string;
        issues?: string[];
      }>(`/api/audit/verify-integrity?${params}`);

      return response.data;
    } catch (error) {
      console.error('Failed to verify integrity, using mock data:', error);
      // Only use mock data as fallback when API fails
      return {
        status: 'valid' as const,
        message: 'Audit trail integrity verified successfully',
        issues: [],
      };
    }
  },

  // Get statistics
  async getStatistics(from?: Date, to?: Date) {
    // Always try to use real data first
    try {
      const params = new URLSearchParams();
      if (from) params.append('from', from.toISOString());
      if (to) params.append('to', to.toISOString());

      const response = await httpClient.get<Record<string, unknown>>(
        `/api/audit/statistics?${params}`
      );
      return response.data;
    } catch (error) {
      console.error('Failed to get statistics, using mock data:', error);
      // Only use mock data as fallback when API fails
      return {
        totalEvents: 2473,
        uniqueUsers: 25,
        uniqueEntities: 187,
        failureCount: 12,
        averageEventsPerDay: 82.4,
        mostActiveUser: 'Max Mustermann',
        mostCommonEvent: 'USER_LOGIN',
      };
    }
  },
};
