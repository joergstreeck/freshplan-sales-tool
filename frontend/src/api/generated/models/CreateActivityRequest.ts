/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { ActivityOutcome } from './ActivityOutcome';
import type { ActivityType } from './ActivityType';
import type { LocalDateTime } from './LocalDateTime';
export type CreateActivityRequest = {
  activityType: ActivityType;
  summary: string;
  description?: string;
  outcome?: ActivityOutcome;
  activityDate?: LocalDateTime;
};
