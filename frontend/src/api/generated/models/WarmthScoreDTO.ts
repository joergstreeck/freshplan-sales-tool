/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { LocalDateTime } from './LocalDateTime';
import type { UUID } from './UUID';
export type WarmthScoreDTO = {
  contactId?: UUID;
  warmthScore?: number;
  confidence?: number;
  dataPoints?: number;
  lastCalculated?: LocalDateTime;
  trend?: string;
  recommendation?: string;
  warmthLevel?: string;
  highConfidence?: boolean;
};
