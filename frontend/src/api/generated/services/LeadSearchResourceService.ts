/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class LeadSearchResourceService {
  /**
   * Find Duplicates
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeadsSearchDuplicates({
    companyName,
    email,
    limit = 5,
  }: {
    companyName?: string;
    email?: string;
    limit?: number;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/search/duplicates',
      query: {
        companyName: companyName,
        email: email,
        limit: limit,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Fuzzy Search
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeadsSearchFuzzy({
    includeInactive = false,
    limit = 20,
    q,
  }: {
    includeInactive?: boolean;
    limit?: number;
    q?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/search/fuzzy',
      query: {
        includeInactive: includeInactive,
        limit: limit,
        q: q,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Calculate Similarity
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeadsSearchSimilarity({
    text1,
    text2,
  }: {
    text1?: string;
    text2?: string;
  }): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/search/similarity',
      query: {
        text1: text1,
        text2: text2,
      },
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
  /**
   * Get Search Status
   * @returns any OK
   * @throws ApiError
   */
  public static getApiLeadsSearchStatus(): CancelablePromise<any> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/leads/search/status',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
