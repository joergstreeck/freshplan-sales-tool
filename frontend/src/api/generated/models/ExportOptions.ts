/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { LocalDateTime } from './LocalDateTime';
export type ExportOptions = {
  reportType: string;
  dateFrom?: LocalDateTime;
  dateTo?: LocalDateTime;
  entityTypes?: Array<string>;
  eventTypes?: Array<string>;
  includeStatistics?: boolean;
  includeCharts?: boolean;
  includeSummary?: boolean;
  format?: string;
  customOptions?: Record<string, any>;
};
