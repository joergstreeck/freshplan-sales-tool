/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateHelpContentRequest } from '../models/CreateHelpContentRequest';
import type { FeedbackRequest } from '../models/FeedbackRequest';
import type { UUID } from '../models/UUID';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class HelpSystemResourceService {
  /**
   * Get Analytics
   * @returns any OK
   * @throws ApiError
   */
  public static getApiHelpAnalytics(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/help/analytics',
    });
  }
  /**
   * Get Feature Analytics
   * @returns any OK
   * @throws ApiError
   */
  public static getApiHelpAnalytics1({ feature }: { feature: string }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/help/analytics/{feature}',
      path: {
        feature: feature,
      },
    });
  }
  /**
   * Create Help Content
   * @returns any OK
   * @throws ApiError
   */
  public static postApiHelpContent({
    requestBody,
  }: {
    requestBody: CreateHelpContentRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/help/content',
      body: requestBody,
      mediaType: 'application/json',
    });
  }
  /**
   * Get Help For Feature
   * @returns any OK
   * @throws ApiError
   */
  public static getApiHelpContent({
    feature,
    firstTime = false,
    sessionId,
    type,
    userId,
    userLevel = 'BEGINNER',
    userRoles,
  }: {
    feature: string;
    firstTime?: boolean;
    sessionId?: string;
    type?: string;
    userId?: string;
    userLevel?: string;
    userRoles?: Array<string>;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/help/content/{feature}',
      path: {
        feature: feature,
      },
      query: {
        firstTime: firstTime,
        sessionId: sessionId,
        type: type,
        userId: userId,
        userLevel: userLevel,
        userRoles: userRoles,
      },
    });
  }
  /**
   * Toggle Help Content
   * @returns any OK
   * @throws ApiError
   */
  public static putApiHelpContentToggle({
    helpId,
    active,
    updatedBy,
  }: {
    helpId: UUID;
    active?: boolean;
    updatedBy?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'PUT',
      url: '/api/help/content/{helpId}/toggle',
      path: {
        helpId: helpId,
      },
      query: {
        active: active,
        updatedBy: updatedBy,
      },
    });
  }
  /**
   * Submit Feedback
   * @returns any OK
   * @throws ApiError
   */
  public static postApiHelpFeedback({
    requestBody,
  }: {
    requestBody: FeedbackRequest;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/help/feedback',
      body: requestBody,
      mediaType: 'application/json',
    });
  }
  /**
   * Get Health Status
   * @returns any OK
   * @throws ApiError
   */
  public static getApiHelpHealth(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/help/health',
    });
  }
  /**
   * Search Help
   * @returns any OK
   * @throws ApiError
   */
  public static getApiHelpSearch({
    q,
    userId,
    userLevel = 'BEGINNER',
    userRoles,
  }: {
    q?: string;
    userId?: string;
    userLevel?: string;
    userRoles?: Array<string>;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/help/search',
      query: {
        q: q,
        userId: userId,
        userLevel: userLevel,
        userRoles: userRoles,
      },
    });
  }
}
