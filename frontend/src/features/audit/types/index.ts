// Audit System Types

export enum AuditAction {
  // CRUD Operations
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  BULK_DELETE = 'BULK_DELETE',
  VIEW = 'VIEW',
  
  // Business Operations
  EXPORT = 'EXPORT',
  IMPORT = 'IMPORT',
  MERGE = 'MERGE',
  ARCHIVE = 'ARCHIVE',
  RESTORE = 'RESTORE',
  
  // Permission & Security
  LOGIN = 'LOGIN',
  LOGOUT = 'LOGOUT',
  FAILED_LOGIN = 'FAILED_LOGIN',
  PERMISSION_CHANGE = 'PERMISSION_CHANGE',
  PASSWORD_CHANGE = 'PASSWORD_CHANGE',
  
  // Compliance
  CONSENT_GIVEN = 'CONSENT_GIVEN',
  CONSENT_WITHDRAWN = 'CONSENT_WITHDRAWN',
  DATA_REQUEST = 'DATA_REQUEST',
  DATA_DELETION = 'DATA_DELETION',
  
  // System
  SYSTEM_EVENT = 'SYSTEM_EVENT',
  CONFIG_CHANGE = 'CONFIG_CHANGE',
  ERROR = 'ERROR'
}

export enum EntityType {
  CUSTOMER = 'CUSTOMER',
  OPPORTUNITY = 'OPPORTUNITY',
  CONTRACT = 'CONTRACT',
  CONTACT = 'CONTACT',
  USER = 'USER',
  SYSTEM = 'SYSTEM',
  AUDIT = 'AUDIT'
}

export enum LegalBasis {
  CONSENT = 'CONSENT',
  CONTRACT = 'CONTRACT',
  LEGAL_OBLIGATION = 'LEGAL_OBLIGATION',
  VITAL_INTERESTS = 'VITAL_INTERESTS',
  PUBLIC_TASK = 'PUBLIC_TASK',
  LEGITIMATE_INTERESTS = 'LEGITIMATE_INTERESTS'
}

export interface AuditLog {
  id: string;
  occurredAt: string;
  entityType: EntityType;
  entityId: string;
  entityName?: string;
  action: AuditAction;
  
  // User information
  userId: string;
  userName: string;
  userRole: string;
  
  // Change details
  oldValues?: string;
  newValues?: string;
  changedFields?: string;
  reason?: string;
  comment?: string;
  
  // Context
  ipAddress?: string;
  userAgent?: string;
  sessionId?: string;
  requestId?: string;
  transactionId?: string;
  
  // Compliance
  isDsgvoRelevant: boolean;
  legalBasis?: LegalBasis;
  consentId?: string;
  retentionUntil?: string;
  
  // Security
  currentHash: string;
  previousHash: string;
  isCritical: boolean;
}

export interface AuditFilters {
  dateRange?: {
    from: Date;
    to: Date;
  };
  entityType?: EntityType;
  entityId?: string;
  eventTypes?: AuditAction[];
  users?: string[];
  searchText?: string;
}

export interface AuditDashboardMetrics {
  coverage: number;
  integrityStatus: string;
  retentionCompliance: number;
  lastAudit: string;
  criticalEventsToday: number;
  activeUsers: number;
  totalEventsToday: number;
  topEventTypes: Array<{
    eventType: string;
    count: number;
  }>;
}

export interface ActivityChartData {
  period: string;
  count: number;
}

export interface ComplianceAlert {
  type: 'ERROR' | 'WARNING' | 'INFO';
  message: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
}

export interface AuditExportOptions {
  format: 'csv' | 'json' | 'pdf' | 'excel';
  dateRange: {
    from: Date;
    to: Date;
  };
  filters?: AuditFilters;
  includeDetails: boolean;
  anonymize: boolean;
}