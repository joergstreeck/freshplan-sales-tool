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
  ERROR = 'ERROR',
}

export enum EntityType {
  CUSTOMER = 'CUSTOMER',
  OPPORTUNITY = 'OPPORTUNITY',
  CONTRACT = 'CONTRACT',
  CONTACT = 'CONTACT',
  USER = 'USER',
  SYSTEM = 'SYSTEM',
  AUDIT = 'AUDIT',
}

export enum LegalBasis {
  CONSENT = 'CONSENT',
  CONTRACT = 'CONTRACT',
  LEGAL_OBLIGATION = 'LEGAL_OBLIGATION',
  VITAL_INTERESTS = 'VITAL_INTERESTS',
  PUBLIC_TASK = 'PUBLIC_TASK',
  LEGITIMATE_INTERESTS = 'LEGITIMATE_INTERESTS',
}

export interface AuditLog {
  id: string;
  timestamp?: string; // Backend uses timestamp
  occurredAt?: string; // Frontend compatibility
  entityType: string; // Backend uses String, not enum
  entityId: string;
  entityName?: string;
  action: string; // Backend uses String for action
  eventType?: string; // Backend field name

  // User information
  userId: string;
  userName: string;
  userEmail?: string;
  userRole?: string;

  // Change details
  oldValue?: any; // Backend uses oldValue (singular)
  newValue?: any; // Backend uses newValue (singular)
  oldValues?: string; // Frontend compatibility
  newValues?: string; // Frontend compatibility
  changes?: any; // Additional changes object
  changedFields?: string;
  changeReason?: string; // Backend field name
  reason?: string; // Frontend compatibility
  comment?: string;
  details?: any; // Additional details object

  // Context
  ipAddress?: string;
  userAgent?: string;
  sessionId?: string;
  requestId?: string;
  transactionId?: string;
  source?: string; // Backend source field (WEB, API, etc.)

  // Compliance
  isDsgvoRelevant?: boolean;
  legalBasis?: LegalBasis;
  consentId?: string;
  retentionUntil?: string;

  // Security
  dataHash?: string; // Backend field name
  currentHash?: string; // Frontend compatibility
  previousHash?: string;
  isCritical?: boolean;
  success?: boolean; // Backend field for operation success
}

export interface AuditFilters {
  dateRange?: {
    from: Date;
    to: Date;
  };
  entityType?: string; // Backend uses String
  entityId?: string;
  eventTypes?: string[]; // Backend uses String array
  users?: string[];
  searchText?: string;
  // Additional fields for backend compatibility
  userId?: string;
  action?: string;
  limit?: number;
  page?: number;
  pageSize?: number;
  from?: string; // ISO string
  to?: string; // ISO string
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
    type?: string; // Frontend uses 'type'
    eventType?: string; // Backend might use 'eventType'
    count: number;
  }>;
}

export interface ActivityChartData {
  time?: string; // Frontend uses 'time'
  period?: string; // Backend might use 'period'
  value?: number; // Frontend uses 'value'
  count?: number; // Backend might use 'count'
  critical?: number; // Anzahl kritischer Events
  users?: number; // Anzahl aktiver Benutzer
}

export interface ComplianceAlert {
  id?: string;
  type?: 'retention' | 'integrity' | 'ERROR' | 'WARNING' | 'INFO';
  severity?: 'warning' | 'info' | 'error' | 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  title?: string;
  description?: string;
  message?: string;
  timestamp?: string;
  resolved?: boolean;
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
