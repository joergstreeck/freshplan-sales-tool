/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LocalDateTime } from './LocalDateTime';
export type GdprDataRequestDTO = {
  id?: number;
  entityType?: string;
  entityId?: number;
  requestedBy?: string;
  requestedAt?: LocalDateTime;
  pdfGenerated?: boolean;
  pdfGeneratedAt?: LocalDateTime;
};
