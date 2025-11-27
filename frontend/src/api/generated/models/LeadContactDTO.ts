/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { LocalDate } from './LocalDate';
import type { LocalDateTime } from './LocalDateTime';
import type { UUID } from './UUID';
export type LeadContactDTO = {
  id?: UUID;
  leadId?: number;
  salutation?: string;
  title?: string;
  firstName?: string;
  lastName?: string;
  position?: string;
  decisionLevel?: string;
  email?: string;
  phone?: string;
  mobile?: string;
  isPrimary?: boolean;
  isActive?: boolean;
  birthday?: LocalDate;
  hobbies?: string;
  familyStatus?: string;
  childrenCount?: number;
  personalNotes?: string;
  warmthScore?: number;
  warmthConfidence?: number;
  lastInteractionDate?: LocalDateTime;
  interactionCount?: number;
  dataQualityScore?: number;
  dataQualityRecommendations?: string;
  isDecisionMaker?: boolean;
  isDeleted?: boolean;
  createdAt?: LocalDateTime;
  updatedAt?: LocalDateTime;
  createdBy?: string;
  updatedBy?: string;
  fullName?: string;
  displayName?: string;
  primary?: boolean;
  active?: boolean;
};
