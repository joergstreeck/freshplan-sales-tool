/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { DashboardAlert } from './DashboardAlert';
import type { DashboardStatistics } from './DashboardStatistics';
import type { DashboardTask } from './DashboardTask';
import type { LeadWidget } from './LeadWidget';
import type { RiskCustomer } from './RiskCustomer';
export type SalesCockpitDashboard = {
  todaysTasks?: Array<DashboardTask>;
  riskCustomers?: Array<RiskCustomer>;
  statistics?: DashboardStatistics;
  alerts?: Array<DashboardAlert>;
  leadWidget?: LeadWidget;
};
