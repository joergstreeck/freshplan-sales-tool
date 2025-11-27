/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { LocalDateTime } from './LocalDateTime';
export type FollowUpMetric = {
  leadId?: string;
  companyName?: string;
  followUpType?: string;
  scheduledAt?: LocalDateTime;
  completedAt?: LocalDateTime;
  success?: boolean;
  responseStatus?: string;
};
