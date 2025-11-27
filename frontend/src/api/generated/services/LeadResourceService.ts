/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ActivityRequest } from '../models/ActivityRequest';
import type { AddFirstContactRequest } from '../models/AddFirstContactRequest';
import type { BackdatingRequest } from '../models/BackdatingRequest';
import type { LeadContactDTO } from '../models/LeadContactDTO';
import type { LeadConvertRequest } from '../models/LeadConvertRequest';
import type { LeadCreateRequest } from '../models/LeadCreateRequest';
import type { LeadStatus } from '../models/LeadStatus';
import type { LeadUpdateRequest } from '../models/LeadUpdateRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class LeadResourceService {
  /**
   * List Leads
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeads({
    direction = 'DESC',
    ownerUserId,
    page,
    search,
    size = 20,
    sort = 'createdAt',
    status,
    territoryId,
  }: {
    direction?: string;
    ownerUserId?: string;
    page?: number;
    search?: string;
    size?: number;
    sort?: string;
    status?: LeadStatus;
    territoryId?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads',
      query: {
        direction: direction,
        ownerUserId: ownerUserId,
        page: page,
        search: search,
        size: size,
        sort: sort,
        status: status,
        territoryId: territoryId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Create Lead
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeads({
    requestBody,
  }: {
    requestBody: LeadCreateRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update Lead
   * @returns any OK
   * @throws ApiError
   */
  public static patchApiLeads({
    id,
    requestBody,
    ifMatch,
  }: {
    id: number;
    requestBody: LeadUpdateRequest;
    ifMatch?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PATCH',
      url: '/api/leads/{id}',
      path: {
        id: id,
      },
      headers: {
        'If-Match': ifMatch,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Lead
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeads1({
    id,
    ifNoneMatch,
  }: {
    id: number;
    ifNoneMatch?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/{id}',
      path: {
        id: id,
      },
      headers: {
        'If-None-Match': ifNoneMatch,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Delete Lead
   * @returns any OK
   * @throws ApiError
   */
  public static deleteApiLeads({
    id,
    ifMatch,
  }: {
    id: number;
    ifMatch?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/leads/{id}',
      path: {
        id: id,
      },
      headers: {
        'If-Match': ifMatch,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Activities
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeadsActivities({
    id,
    page,
    size = 50,
  }: {
    id: number;
    page?: number;
    size?: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/{id}/activities',
      path: {
        id: id,
      },
      query: {
        page: page,
        size: size,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Add Activity
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeadsActivities({
    id,
    requestBody,
  }: {
    id: number;
    requestBody: ActivityRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads/{id}/activities',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Lead Contacts
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeadsContacts({ id }: { id: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/{id}/contacts',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Create Lead Contact
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeadsContacts({
    id,
    requestBody,
  }: {
    id: number;
    requestBody: LeadContactDTO;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads/{id}/contacts',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update Lead Contact
   * @returns any OK
   * @throws ApiError
   */
  public static patchApiLeadsContacts({
    contactId,
    id,
    requestBody,
  }: {
    contactId: string;
    id: number;
    requestBody: LeadContactDTO;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PATCH',
      url: '/api/leads/{id}/contacts/{contactId}',
      path: {
        contactId: contactId,
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Delete Lead Contact
   * @returns any OK
   * @throws ApiError
   */
  public static deleteApiLeadsContacts({
    contactId,
    id,
  }: {
    contactId: string;
    id: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/leads/{id}/contacts/{contactId}',
      path: {
        contactId: contactId,
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Set Contact As Primary
   * @returns any OK
   * @throws ApiError
   */
  public static patchApiLeadsContactsPrimary({
    contactId,
    id,
  }: {
    contactId: string;
    id: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PATCH',
      url: '/api/leads/{id}/contacts/{contactId}/primary',
      path: {
        contactId: contactId,
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Convert To Customer
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeadsConvert({
    id,
    requestBody,
  }: {
    id: number;
    requestBody: LeadConvertRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads/{id}/convert',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Add First Contact
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeadsFirstContact({
    id,
    requestBody,
  }: {
    id: number;
    requestBody: AddFirstContactRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads/{id}/first-contact',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Lead Opportunities
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeadsOpportunities({ id }: { id: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/{id}/opportunities',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Recalculate Score
   * @returns any OK
   * @throws ApiError
   */
  public static postApiLeadsRecalculateScore({ id }: { id: number }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/leads/{id}/recalculate-score',
      path: {
        id: id,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update Registered At
   * @returns any OK
   * @throws ApiError
   */
  public static putApiLeadsRegisteredAt({
    id,
    requestBody,
  }: {
    id: number;
    requestBody: BackdatingRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/leads/{id}/registered-at',
      path: {
        id: id,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
