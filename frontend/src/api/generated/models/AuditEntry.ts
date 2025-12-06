/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AuditEventType } from './AuditEventType';
import type { AuditSource } from './AuditSource';
import type { Instant } from './Instant';
import type { UUID } from './UUID';
export type AuditEntry = {
  id?: UUID;
  timestamp?: Instant;
  eventType?: AuditEventType;
  entityType?: string;
  entityId?: UUID;
  userId?: UUID;
  userName?: string;
  userRole?: string;
  oldValue?: string;
  newValue?: string;
  changeReason?: string;
  userComment?: string;
  ipAddress?: string;
  userAgent?: string;
  sessionId?: UUID;
  source?: AuditSource;
  apiEndpoint?: string;
  requestId?: UUID;
  dataHash?: string;
  previousHash?: string;
  schemaVersion?: number;
  testEnvironment?: boolean;
  failure?: boolean;
  securityRelevant?: boolean;
};
