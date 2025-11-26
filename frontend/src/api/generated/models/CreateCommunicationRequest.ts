/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { LocalDateTime } from './LocalDateTime';
import type { UUID } from './UUID';
export type CreateCommunicationRequest = {
  channel: string;
  direction: string;
  description: string;
  performedBy: string;
  duration?: number;
  relatedContactId?: UUID;
  requiresFollowUp?: boolean;
  followUpDate?: LocalDateTime;
  followUpNotes?: string;
};
