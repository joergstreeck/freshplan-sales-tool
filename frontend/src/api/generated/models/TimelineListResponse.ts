/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { TimelineEventResponse } from './TimelineEventResponse';
export type TimelineListResponse = {
  content?: Array<TimelineEventResponse>;
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
  first?: boolean;
  last?: boolean;
};
