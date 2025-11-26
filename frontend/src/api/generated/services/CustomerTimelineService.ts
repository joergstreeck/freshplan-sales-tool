/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */

import type { CreateCommunicationRequest } from '../models/CreateCommunicationRequest';
import type { CreateNoteRequest } from '../models/CreateNoteRequest';
import type { CreateTimelineEventRequest } from '../models/CreateTimelineEventRequest';
import type { TimelineEventResponse } from '../models/TimelineEventResponse';
import type { TimelineListResponse } from '../models/TimelineListResponse';
import type { TimelineSummaryResponse } from '../models/TimelineSummaryResponse';
import type { UpdateTimelineEventRequest } from '../models/UpdateTimelineEventRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CustomerTimelineService {
  /**
   * Get customer timeline
   * Retrieves paginated timeline events for a specific customer
   * @returns TimelineListResponse Timeline events retrieved successfully
   * @throws ApiError
   */
  public static getApiCustomersTimeline({
    customerId,
    category,
    page,
    search,
    size = 20,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    /**
     * Filter by event category
     */
    category?: string;
    /**
     * Page number (0-based)
     */
    page?: number;
    /**
     * Search in title and description
     */
    search?: string;
    /**
     * Page size
     */
    size?: number;
  }): CancelablePromise<TimelineListResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/timeline',
      path: {
        customerId: customerId,
      },
      query: {
        category: category,
        page: page,
        search: search,
        size: size,
      },
      errors: {
        400: `Invalid request parameters`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
  /**
   * Create timeline event
   * Creates a new timeline event for a customer
   * @returns TimelineEventResponse Timeline event created successfully
   * @throws ApiError
   */
  public static postApiCustomersTimeline({
    customerId,
    requestBody,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    requestBody: CreateTimelineEventRequest;
  }): CancelablePromise<TimelineEventResponse> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{customerId}/timeline',
      path: {
        customerId: customerId,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid request data`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
  /**
   * Create communication
   * Records a communication event (call, email, meeting)
   * @returns TimelineEventResponse Communication recorded successfully
   * @throws ApiError
   */
  public static postApiCustomersTimelineCommunications({
    customerId,
    requestBody,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    requestBody: CreateCommunicationRequest;
  }): CancelablePromise<TimelineEventResponse> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{customerId}/timeline/communications',
      path: {
        customerId: customerId,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid request data`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
  /**
   * Get recent communications
   * Retrieves recent communication history for a customer
   * @returns TimelineEventResponse Recent communications retrieved successfully
   * @throws ApiError
   */
  public static getApiCustomersTimelineCommunicationsRecent({
    customerId,
    days = 30,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    /**
     * Number of days to look back
     */
    days?: number;
  }): CancelablePromise<TimelineEventResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/timeline/communications/recent',
      path: {
        customerId: customerId,
      },
      query: {
        days: days,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update timeline event
   * Updates an existing timeline event
   * @returns TimelineEventResponse Timeline event updated successfully
   * @throws ApiError
   */
  public static putApiCustomersTimelineEvents({
    customerId,
    eventId,
    requestBody,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    /**
     * Event ID
     */
    eventId: UUID;
    requestBody: UpdateTimelineEventRequest;
  }): CancelablePromise<TimelineEventResponse> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/customers/{customerId}/timeline/events/{eventId}',
      path: {
        customerId: customerId,
        eventId: eventId,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid request data`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Event not found`,
      },
    });
  }
  /**
   * Delete timeline event
   * Soft deletes a timeline event
   * @returns void
   * @throws ApiError
   */
  public static deleteApiCustomersTimelineEvents({
    customerId,
    eventId,
    deletedBy,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    /**
     * Event ID
     */
    eventId: UUID;
    /**
     * User who deleted the event
     */
    deletedBy: string;
  }): CancelablePromise<void> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/customers/{customerId}/timeline/events/{eventId}',
      path: {
        customerId: customerId,
        eventId: eventId,
      },
      query: {
        deletedBy: deletedBy,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Event not found`,
      },
    });
  }
  /**
   * Complete follow-up
   * Marks a follow-up as completed
   * @returns void
   * @throws ApiError
   */
  public static postApiCustomersTimelineEventsCompleteFollowUp({
    customerId,
    eventId,
    completedBy,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    /**
     * Event ID
     */
    eventId: UUID;
    /**
     * User who completed the follow-up
     */
    completedBy: string;
  }): CancelablePromise<void> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{customerId}/timeline/events/{eventId}/complete-follow-up',
      path: {
        customerId: customerId,
        eventId: eventId,
      },
      query: {
        completedBy: completedBy,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Event not found`,
      },
    });
  }
  /**
   * Get follow-up events
   * Retrieves all events requiring follow-up for a customer
   * @returns TimelineEventResponse Follow-up events retrieved successfully
   * @throws ApiError
   */
  public static getApiCustomersTimelineFollowUps({
    customerId,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
  }): CancelablePromise<TimelineEventResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/timeline/follow-ups',
      path: {
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
  /**
   * Get overdue follow-ups
   * Retrieves overdue follow-up events for a customer
   * @returns TimelineEventResponse Overdue follow-ups retrieved successfully
   * @throws ApiError
   */
  public static getApiCustomersTimelineFollowUpsOverdue({
    customerId,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
  }): CancelablePromise<TimelineEventResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/timeline/follow-ups/overdue',
      path: {
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
  /**
   * Create note
   * Creates a quick note for a customer
   * @returns TimelineEventResponse Note created successfully
   * @throws ApiError
   */
  public static postApiCustomersTimelineNotes({
    customerId,
    requestBody,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
    requestBody: CreateNoteRequest;
  }): CancelablePromise<TimelineEventResponse> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/{customerId}/timeline/notes',
      path: {
        customerId: customerId,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid request data`,
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
  /**
   * Get timeline summary
   * Retrieves summary statistics for customer timeline
   * @returns TimelineSummaryResponse Timeline summary retrieved successfully
   * @throws ApiError
   */
  public static getApiCustomersTimelineSummary({
    customerId,
  }: {
    /**
     * Customer ID
     */
    customerId: UUID;
  }): CancelablePromise<TimelineSummaryResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/{customerId}/timeline/summary',
      path: {
        customerId: customerId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
        404: `Customer not found`,
      },
    });
  }
}
