/**
 * Audit Types for Mini Timeline
 * 
 * @module audit.types
 * @since FC-005 PR4
 */

export interface AuditEntry {
  id: string;
  timestamp: string;
  eventType: string;
  entityType: string;
  entityId: string;
  userName?: string;
  userId: string;
  changes?: any;
  ipAddress?: string;
  userAgent?: string;
  source?: string;
}

export interface AuditResponse {
  content: AuditEntry[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}