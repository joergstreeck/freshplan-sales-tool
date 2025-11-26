/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { EventCategory } from './EventCategory';
import type { ImportanceLevel } from './ImportanceLevel';
import type { LocalDateTime } from './LocalDateTime';
import type { UUID } from './UUID';
export type CreateTimelineEventRequest = {
  eventType: string;
  title: string;
  description?: string;
  category: EventCategory;
  importance?: ImportanceLevel;
  performedBy: string;
  performedByRole?: string;
  eventDate?: LocalDateTime;
  communicationChannel?: string;
  communicationDirection?: string;
  communicationDuration?: number;
  requiresFollowUp?: boolean;
  followUpDate?: LocalDateTime;
  followUpNotes?: string;
  relatedContactId?: UUID;
  relatedLocationId?: UUID;
  relatedDocumentId?: UUID;
  businessImpact?: string;
  revenueImpact?: number;
  tags?: Array<string>;
  labels?: Array<string>;
  externalId?: string;
  externalUrl?: string;
};
