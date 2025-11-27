/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { LocalDateTime } from './LocalDateTime';
import type { RiskLevel } from './RiskLevel';
import type { UUID } from './UUID';
export type RiskCustomer = {
  id?: UUID;
  customerNumber?: string;
  companyName?: string;
  lastContactDate?: LocalDateTime;
  daysSinceLastContact?: number;
  riskReason?: string;
  riskLevel?: RiskLevel;
  recommendedAction?: string;
};
