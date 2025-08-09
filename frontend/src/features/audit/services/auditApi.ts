import { httpClient } from '@/lib/apiClient';
import { isFeatureEnabled } from '@/config/featureFlags';
import type {
  AuditLog,
  AuditFilters,
  AuditDashboardMetrics,
  ActivityChartData,
  ComplianceAlert,
  AuditExportOptions
} from '../types';

// Mock data for development
const mockAuditLogs: AuditLog[] = [
  {
    id: '1',
    timestamp: new Date().toISOString(),
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
    previousHash: 'abc123',
    dataHash: 'def456',
    success: true
  },
  {
    id: '2',
    timestamp: new Date(Date.now() - 3600000).toISOString(),
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
    previousHash: 'def456',
    dataHash: 'ghi789',
    success: true
  },
  {
    id: '3',
    timestamp: new Date(Date.now() - 7200000).toISOString(),
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
    previousHash: 'ghi789',
    dataHash: 'jkl012',
    success: false
  }
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
    { type: 'PERMISSION_CHANGE', count: 12 }
  ]
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
  { time: '23:00', value: 3 }
];

const mockComplianceAlerts: ComplianceAlert[] = [
  {
    id: 'alert-1',
    type: 'retention',
    severity: 'warning',
    title: 'Datenaufbewahrung überschreitet 90 Tage',
    description: '15 Audit-Einträge sind älter als 90 Tage und sollten archiviert werden.',
    timestamp: new Date().toISOString(),
    resolved: false
  },
  {
    id: 'alert-2',
    type: 'integrity',
    severity: 'info',
    title: 'Nächste Integritätsprüfung fällig',
    description: 'Die monatliche Integritätsprüfung steht in 3 Tagen an.',
    timestamp: new Date().toISOString(),
    resolved: false
  }
];

export const auditApi = {
  // Get audit logs with filters
  async getAuditLogs(filters: AuditFilters & { page?: number; pageSize?: number }) {
    // Use mock data in development if enabled
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      return Promise.resolve(mockAuditLogs);
    }

    const params = new URLSearchParams();
    
    if (filters.dateRange) {
      params.append('from', filters.dateRange.from.toISOString());
      params.append('to', filters.dateRange.to.toISOString());
    }
    if (filters.entityType) params.append('entityType', filters.entityType);
    if (filters.entityId) params.append('entityId', filters.entityId);
    if (filters.eventTypes) {
      filters.eventTypes.forEach(type => params.append('eventType', type));
    }
    if (filters.users) {
      filters.users.forEach(user => params.append('userId', user));
    }
    if (filters.searchText) params.append('searchText', filters.searchText);
    if (filters.page !== undefined) params.append('page', filters.page.toString());
    if (filters.pageSize) params.append('size', filters.pageSize.toString());
    
    const response = await httpClient.get<AuditLog[]>(`/api/audit/search?${params}`);
    return response.data;
  },
  
  // Get audit detail
  async getAuditDetail(id: string) {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      const log = mockAuditLogs.find(l => l.id === id);
      return Promise.resolve(log || mockAuditLogs[0]);
    }
    
    const response = await httpClient.get<AuditLog>(`/api/audit/${id}`);
    return response.data;
  },
  
  // Get entity audit trail
  async getEntityAuditTrail(entityType: string, entityId: string, page = 0, size = 50) {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      return Promise.resolve(mockAuditLogs.filter(
        log => log.entityType === entityType && log.entityId === entityId
      ));
    }
    
    const response = await httpClient.get<AuditLog[]>(
      `/api/audit/entity/${entityType}/${entityId}?page=${page}&size=${size}`
    );
    return response.data;
  },
  
  // Dashboard metrics
  async getDashboardMetrics() {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      return Promise.resolve(mockDashboardMetrics);
    }
    
    const response = await httpClient.get<AuditDashboardMetrics>('/api/audit/dashboard/metrics');
    return response.data;
  },
  
  // Activity chart data
  async getActivityChartData(days = 7, groupBy = 'hour') {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      return Promise.resolve(mockActivityChartData);
    }
    
    const response = await httpClient.get<ActivityChartData[]>(
      `/api/audit/dashboard/activity-chart?days=${days}&groupBy=${groupBy}`
    );
    return response.data;
  },
  
  // Critical events
  async getCriticalEvents(limit = 10) {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      const criticalEvents = mockAuditLogs.filter(
        log => log.eventType.includes('DENIED') || log.eventType.includes('ERROR') || !log.success
      );
      return Promise.resolve(criticalEvents.slice(0, limit));
    }
    
    const response = await httpClient.get<AuditLog[]>(
      `/api/audit/dashboard/critical-events?limit=${limit}`
    );
    return response.data;
  },
  
  // Compliance alerts
  async getComplianceAlerts() {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      return Promise.resolve(mockComplianceAlerts);
    }
    
    const response = await httpClient.get<ComplianceAlert[]>('/api/audit/dashboard/compliance-alerts');
    return response.data;
  },
  
  // Export audit logs
  async exportAuditLogs(options: AuditExportOptions) {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      // Mock export - create CSV content
      const csvContent = [
        'ID,Timestamp,User,Event,Entity,Action,Success',
        ...mockAuditLogs.map(log => 
          `${log.id},${log.timestamp},${log.userName},${log.eventType},${log.entityType},${log.action},${log.success}`
        )
      ].join('\n');
      
      const blob = new Blob([csvContent], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `audit_trail_${new Date().toISOString().split('T')[0]}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      return;
    }
    
    const params = new URLSearchParams();
    params.append('format', options.format);
    params.append('from', options.dateRange.from.toISOString().split('T')[0]);
    params.append('to', options.dateRange.to.toISOString().split('T')[0]);
    
    if (options.filters?.entityType) {
      params.append('entityType', options.filters.entityType);
    }
    if (options.filters?.eventTypes) {
      options.filters.eventTypes.forEach(type => params.append('eventType', type));
    }
    
    const response = await httpClient.get(`/api/audit/export?${params}`, {
      responseType: 'blob'
    });
    
    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `audit_trail_${new Date().toISOString().split('T')[0]}.${options.format}`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
  
  // Verify integrity
  async verifyIntegrity(from?: Date, to?: Date) {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      return Promise.resolve({
        status: 'valid' as const,
        message: 'Audit trail integrity verified successfully',
        issues: []
      });
    }
    
    const params = new URLSearchParams();
    if (from) params.append('from', from.toISOString());
    if (to) params.append('to', to.toISOString());
    
    const response = await httpClient.post<{
      status: 'valid' | 'compromised';
      message?: string;
      issues?: string[];
    }>(`/api/audit/verify-integrity?${params}`);
    
    return response.data;
  },
  
  // Get statistics
  async getStatistics(from?: Date, to?: Date) {
    if (isFeatureEnabled('useMockData') || isFeatureEnabled('authBypass')) {
      return Promise.resolve({
        totalEvents: 2473,
        uniqueUsers: 25,
        uniqueEntities: 187,
        failureCount: 12,
        averageEventsPerDay: 82.4,
        mostActiveUser: 'Max Mustermann',
        mostCommonEvent: 'USER_LOGIN'
      });
    }
    
    const params = new URLSearchParams();
    if (from) params.append('from', from.toISOString());
    if (to) params.append('to', to.toISOString());
    
    const response = await httpClient.get<Record<string, any>>(`/api/audit/statistics?${params}`);
    return response.data;
  }
};