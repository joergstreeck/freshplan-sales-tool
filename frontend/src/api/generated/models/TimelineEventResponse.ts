/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { EventCategory } from './EventCategory';
import type { ImportanceLevel } from './ImportanceLevel';
import type { LocalDateTime } from './LocalDateTime';
import type { UUID } from './UUID';
export type TimelineEventResponse = {
  id?: UUID;
  eventType?: string;
  eventDate?: LocalDateTime;
  title?: string;
  description?: string;
  category?: EventCategory;
  importance?: ImportanceLevel;
  performedBy?: string;
  performedByRole?: string;
  communicationChannel?: string;
  communicationDirection?: string;
  communicationDuration?: number;
  requiresFollowUp?: boolean;
  followUpDate?: LocalDateTime;
  followUpNotes?: string;
  followUpCompleted?: boolean;
  relatedContactId?: UUID;
  relatedContactName?: string;
  relatedLocationId?: UUID;
  relatedLocationName?: string;
  businessImpact?: string;
  revenueImpact?: number;
  tags?: Array<string>;
  labels?: Array<string>;
  isDeleted?: boolean;
  createdAt?: LocalDateTime;
  updatedAt?: LocalDateTime;
  eventAgeInDays?: number;
  isFollowUpOverdue?: boolean;
  summary?: string;
};
