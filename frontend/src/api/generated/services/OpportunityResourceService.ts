/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ConvertToCustomerRequest } from '../models/ConvertToCustomerRequest';
import type { CreateOpportunityForCustomerRequest } from '../models/CreateOpportunityForCustomerRequest';
import type { CreateOpportunityFromLeadRequest } from '../models/CreateOpportunityFromLeadRequest';
import type { CreateOpportunityRequest } from '../models/CreateOpportunityRequest';
import type { OpportunityStage } from '../models/OpportunityStage';
import type { UpdateOpportunityRequest } from '../models/UpdateOpportunityRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class OpportunityResourceService {
  /**
   * Get All Opportunities
   * @returns any OK
   * @throws ApiError
   */
  public static getApiOpportunities({
    page,
    size = 20,
  }: {
    page?: number;
    size?: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/opportunities',
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
   * Create Opportunity
   * @returns any OK
   * @throws ApiError
   */
  public static postApiOpportunities({
    requestBody,
  }: {
    requestBody: CreateOpportunityRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/opportunities',
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Opportunities By Assigned To
   * @returns any OK
   * @throws ApiError
   */
  public static getApiOpportunitiesAssigned({ userId }: { userId: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/opportunities/assigned/{userId}',
      path: {
        userId: userId,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Create For Customer
   * @returns any OK
   * @throws ApiError
   */
  public static postApiOpportunitiesForCustomer({
    customerId,
    requestBody,
  }: {
    customerId: UUID;
    requestBody: CreateOpportunityForCustomerRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/opportunities/for-customer/{customerId}',
      path: {
        customerId: customerId,
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
   * Create From Lead
   * @returns any OK
   * @throws ApiError
   */
  public static postApiOpportunitiesFromLead({
    leadId,
    requestBody,
  }: {
    leadId: number;
    requestBody: CreateOpportunityFromLeadRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/opportunities/from-lead/{leadId}',
      path: {
        leadId: leadId,
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
   * Health
   * @returns any OK
   * @throws ApiError
   */
  public static getApiOpportunitiesHealth(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/opportunities/health',
    });
  }
  /**
   * Get Pipeline Overview
   * @returns any OK
   * @throws ApiError
   */
  public static getApiOpportunitiesPipelineOverview(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/opportunities/pipeline/overview',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Opportunities By Stage
   * @returns any OK
   * @throws ApiError
   */
  public static getApiOpportunitiesStage({
    stage,
  }: {
    stage: OpportunityStage;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/opportunities/stage/{stage}',
      path: {
        stage: stage,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Update Opportunity
   * @returns any OK
   * @throws ApiError
   */
  public static putApiOpportunities({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: UpdateOpportunityRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/opportunities/{id}',
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
   * Get Opportunity
   * @returns any OK
   * @throws ApiError
   */
  public static getApiOpportunities1({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/opportunities/{id}',
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
   * Delete Opportunity
   * @returns any OK
   * @throws ApiError
   */
  public static deleteApiOpportunities({ id }: { id: UUID }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'DELETE',
      url: '/api/opportunities/{id}',
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
   * Add Activity
   * @returns any OK
   * @throws ApiError
   */
  public static postApiOpportunitiesActivities({
    id,
    description,
    title,
    type,
  }: {
    id: UUID;
    description?: string;
    title?: string;
    type?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/opportunities/{id}/activities',
      path: {
        id: id,
      },
      query: {
        description: description,
        title: title,
        type: type,
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
  public static postApiOpportunitiesConvertToCustomer({
    id,
    requestBody,
  }: {
    id: UUID;
    requestBody: ConvertToCustomerRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/opportunities/{id}/convert-to-customer',
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
   * Change Stage
   * @returns any OK
   * @throws ApiError
   */
  public static putApiOpportunitiesStage({
    id,
    stage,
    reason,
  }: {
    id: UUID;
    stage: OpportunityStage;
    reason?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/opportunities/{id}/stage/{stage}',
      path: {
        id: id,
        stage: stage,
      },
      query: {
        reason: reason,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
