/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { InteractionType } from './InteractionType';
import type { LocalDateTime } from './LocalDateTime';
import type { UUID } from './UUID';
export type ContactInteractionDTO = {
  id?: UUID;
  contactId: UUID;
  type: InteractionType;
  timestamp: LocalDateTime;
  sentimentScore?: number;
  engagementScore?: number;
  responseTimeMinutes?: number;
  wordCount?: number;
  initiatedBy?: string;
  subject?: string;
  summary?: string;
  fullContent?: string;
  channel?: string;
  channelDetails?: string;
  outcome?: string;
  nextAction?: string;
  nextActionDate?: LocalDateTime;
  externalRefId?: string;
  externalRefType?: string;
  createdAt?: LocalDateTime;
  createdBy?: string;
};
