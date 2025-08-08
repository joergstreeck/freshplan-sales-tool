import { httpClient } from '@/lib/apiClient';
import type {
  AuditLog,
  AuditFilters,
  AuditDashboardMetrics,
  ActivityChartData,
  ComplianceAlert,
  AuditExportOptions
} from '../types';

export const auditApi = {
  // Get audit logs with filters
  async getAuditLogs(filters: AuditFilters & { page?: number; pageSize?: number }) {
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
    
    const response = await api.get<AuditLog[]>(`/api/audit/search?${params}`);
    return response.data;
  },
  
  // Get audit detail
  async getAuditDetail(id: string) {
    const response = await api.get<AuditLog>(`/api/audit/${id}`);
    return response.data;
  },
  
  // Get entity audit trail
  async getEntityAuditTrail(entityType: string, entityId: string, page = 0, size = 50) {
    const response = await api.get<AuditLog[]>(
      `/api/audit/entity/${entityType}/${entityId}?page=${page}&size=${size}`
    );
    return response.data;
  },
  
  // Dashboard metrics
  async getDashboardMetrics() {
    const response = await api.get<AuditDashboardMetrics>('/api/audit/dashboard/metrics');
    return response.data;
  },
  
  // Activity chart data
  async getActivityChartData(days = 7, groupBy = 'hour') {
    const response = await api.get<ActivityChartData[]>(
      `/api/audit/dashboard/activity-chart?days=${days}&groupBy=${groupBy}`
    );
    return response.data;
  },
  
  // Critical events
  async getCriticalEvents(limit = 10) {
    const response = await api.get<AuditLog[]>(
      `/api/audit/dashboard/critical-events?limit=${limit}`
    );
    return response.data;
  },
  
  // Compliance alerts
  async getComplianceAlerts() {
    const response = await api.get<ComplianceAlert[]>('/api/audit/dashboard/compliance-alerts');
    return response.data;
  },
  
  // Export audit logs
  async exportAuditLogs(options: AuditExportOptions) {
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
    
    const response = await api.get(`/api/audit/export?${params}`, {
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
    const params = new URLSearchParams();
    if (from) params.append('from', from.toISOString());
    if (to) params.append('to', to.toISOString());
    
    const response = await api.post<{
      status: 'valid' | 'compromised';
      message?: string;
      issues?: string[];
    }>(`/api/audit/verify-integrity?${params}`);
    
    return response.data;
  },
  
  // Get statistics
  async getStatistics(from?: Date, to?: Date) {
    const params = new URLSearchParams();
    if (from) params.append('from', from.toISOString());
    if (to) params.append('to', to.toISOString());
    
    const response = await api.get<Record<string, any>>(`/api/audit/statistics?${params}`);
    return response.data;
  }
};