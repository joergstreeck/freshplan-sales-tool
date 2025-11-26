/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { FollowUpMetric } from './FollowUpMetric';
import type { LeadStatistics } from './LeadStatistics';
import type { LocalDateTime } from './LocalDateTime';
import type { RecentLeadActivity } from './RecentLeadActivity';
import type { StatusDistribution } from './StatusDistribution';
export type LeadWidget = {
  leadStats?: LeadStatistics;
  recentFollowUps?: Array<FollowUpMetric>;
  pendingT3Count?: number;
  pendingT7Count?: number;
  completedT3Today?: number;
  completedT7Today?: number;
  t3ResponseRate?: number;
  t7ResponseRate?: number;
  averageResponseTime?: number;
  statusDistribution?: StatusDistribution;
  recentActivities?: Array<RecentLeadActivity>;
  lastUpdated?: LocalDateTime;
  realTimeEnabled?: boolean;
  pipelineHealthScore?: number;
};
