/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CustomerSearchRequest } from '../models/CustomerSearchRequest';
import type { PagedCustomerResponse } from '../models/PagedCustomerResponse';
import type { SmartSearchRequest } from '../models/SmartSearchRequest';
import type { SmartSortStrategyInfo } from '../models/SmartSortStrategyInfo';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class CustomerSearchService {
  /**
   * Search customers with query parameter
   * Simple search endpoint using query parameter
   * @returns PagedCustomerResponse Search completed successfully
   * @throws ApiError
   */
  public static getApiCustomersSearch({
    page,
    query,
    size = 20,
  }: {
    /**
     * Page number (0-based)
     */
    page?: number;
    /**
     * Search query
     */
    query?: string;
    /**
     * Page size
     */
    size?: number;
  }): CancelablePromise<PagedCustomerResponse> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/search',
      query: {
        page: page,
        query: query,
        size: size,
      },
      errors: {
        400: `Invalid search parameters`,
        401: `User not authenticated`,
        403: `Not Allowed`,
        500: `Internal server error`,
      },
    });
  }
  /**
   * Search customers with dynamic filters
   * Performs an advanced search on customers with support for global search, multiple filters, and custom sorting
   * @returns PagedCustomerResponse Search completed successfully
   * @throws ApiError
   */
  public static postApiCustomersSearch({
    requestBody,
    page,
    size = 20,
  }: {
    /**
     * Search criteria including filters and sorting
     */
    requestBody: CustomerSearchRequest;
    /**
     * Page number (0-based)
     */
    page?: number;
    /**
     * Page size
     */
    size?: number;
  }): CancelablePromise<PagedCustomerResponse> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/search',
      query: {
        page: page,
        size: size,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid search parameters`,
        401: `User not authenticated`,
        403: `Not Allowed`,
        500: `Internal server error`,
      },
    });
  }
  /**
   * Search customers with smart sorting strategies
   * Performs customer search with predefined smart sorting strategies optimized for sales, risk management, and engagement
   * @returns PagedCustomerResponse Smart search completed successfully
   * @throws ApiError
   */
  public static postApiCustomersSearchSmart({
    requestBody,
    page,
    size = 20,
  }: {
    /**
     * Search criteria for smart sorting
     */
    requestBody: SmartSearchRequest;
    /**
     * Page number (0-based)
     */
    page?: number;
    /**
     * Page size
     */
    size?: number;
  }): CancelablePromise<PagedCustomerResponse> {
    return __request(OpenAPI, {
      method: 'POST',
      url: '/api/customers/search/smart',
      query: {
        page: page,
        size: size,
      },
      body: requestBody,
      mediaType: 'application/json',
      errors: {
        400: `Invalid search parameters`,
        401: `User not authenticated`,
        403: `Not Allowed`,
        500: `Internal server error`,
      },
    });
  }
  /**
   * Get available smart sort strategies
   * Returns all available smart sorting strategies with descriptions
   * @returns SmartSortStrategyInfo Smart sort strategies retrieved successfully
   * @throws ApiError
   */
  public static getApiCustomersSearchSmartStrategies(): CancelablePromise<SmartSortStrategyInfo> {
    return __request(OpenAPI, {
      method: 'GET',
      url: '/api/customers/search/smart/strategies',
      errors: {
        401: `Not Authorized`,
        403: `Not Allowed`,
      },
    });
  }
}
