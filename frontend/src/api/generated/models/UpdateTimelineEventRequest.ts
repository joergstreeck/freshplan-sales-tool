/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ImportanceLevel } from './ImportanceLevel';
export type UpdateTimelineEventRequest = {
  title?: string;
  description?: string;
  importance?: ImportanceLevel;
  businessImpact?: string;
  revenueImpact?: number;
  tags?: Array<string>;
  updatedBy: string;
};
