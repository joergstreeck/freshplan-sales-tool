/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LocalDateTime } from './LocalDateTime';
export type GdprDeletionLogDTO1 = {
  id?: number;
  entityType?: string;
  entityId?: number;
  deletedBy?: string;
  deletedAt?: LocalDateTime;
  deletionReason?: string;
  originalDataHash?: string;
};
