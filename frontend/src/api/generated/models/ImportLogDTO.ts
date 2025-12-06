/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LocalDateTime } from './LocalDateTime';
import type { UUID } from './UUID';
export type ImportLogDTO = {
  id?: UUID;
  userId?: string;
  importedAt?: LocalDateTime;
  totalRows?: number;
  importedCount?: number;
  skippedCount?: number;
  errorCount?: number;
  duplicateRate?: number;
  source?: string;
  fileName?: string;
  fileSizeBytes?: number;
  fileType?: string;
  status?: string;
  approvedBy?: string;
  approvedAt?: LocalDateTime;
  rejectionReason?: string;
};
